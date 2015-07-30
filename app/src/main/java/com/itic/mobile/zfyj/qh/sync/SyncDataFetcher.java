package com.itic.mobile.zfyj.qh.sync;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.android.volley.Request;
import com.itic.mobile.util.datetime.TimeUtils;
import com.itic.mobile.zfyj.qh.Config;
import com.turbomanage.httpclient.BasicHttpClient;
import com.turbomanage.httpclient.ConsoleRequestLogger;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.RequestLogger;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 数据同步（SyncAdapter中使用）
 */
public class SyncDataFetcher {
    private static final String TAG = "SyncDataFetcher";
    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";

    // URL of the remote manifest file
    private String mUrl = null;

    // timestamp of the manifest file on the server
    private String mServerTimestamp = null;

    // the set of cache files we have used -- we use this for cache cleanup.
    private HashSet<String> mCacheFilesToKeep = new HashSet<String>();

    // total # of bytes downloaded (approximate)
    private long mBytesDownloaded = 0;

    // total # of bytes read from cache hits (approximate)
    private long mBytesReadFromCache = 0;

    private Map<String, String> mHttpHeader = null;
    private Map<String, String> mHttpParams = null;

    public SyncDataFetcher(String url) {
        mUrl = url;
    }

    public void setHttpHeader(Map<String, String> httpHeader){
        mHttpHeader = httpHeader;
    }

    public void setHttpParams(Map<String, String> httpParams){
        mHttpParams = httpParams;
    }

    public String getUrl() {
        return prepareURL(DEFAULT_PARAMS_ENCODING);
    }

    private String prepareURL(String paramsEncoding) {
        StringBuffer urlStr = new StringBuffer();
        try {
            urlStr.append(Config.SERVER_HOST + mUrl);
            if (mHttpParams != null) {
                urlStr.append(URLEncoder.encode("?", paramsEncoding));
                for (Map.Entry<String, String> entry : mHttpParams.entrySet()) {
                    urlStr.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                    urlStr.append("=");
                    urlStr.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                    urlStr.append("&");
                }
            }
            return urlStr.toString();
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }

    /**
     * ��Զ�˷���������Fetches data from the remote server.
     *
     * @param refTimestamp ʱ��������Զ�����ݱȸ�ʱ����ɣ�����Ҫ�������ݲ��Ҹ÷��������� null
     * @return �������ݣ����߷��� null
     * @throws IOException ��������ʱ�����쳣
     */
    public String fetchConferenceDataIfNewer(String refTimestamp) throws IOException {
        if (TextUtils.isEmpty(getUrl())) {
            Log.w(TAG, "Manifest URL is empty (remote sync disabled!).");
            return null;
        }

        BasicHttpClient httpClient = new BasicHttpClient();
        httpClient.setRequestLogger(mQuietLogger);

        // Only download if data is newer than refTimestamp
        // Cloud Storage is very picky with the If-Modified-Since format. If it's in a wrong
        // format, it refuses to serve the file, returning 400 HTTP error. So, if the
        // refTimestamp is in a wrong format, we simply ignore it. But pay attention to this
        // warning in the log, because it might mean unnecessary data is being downloaded.
        if (!TextUtils.isEmpty(refTimestamp)) {
            if (TimeUtils.isValidFormatForIfModifiedSinceHeader(refTimestamp)) {
                httpClient.addHeader("If-Modified-Since", refTimestamp);
            } else {
                Log.w(TAG, "Could not set If-Modified-Since HTTP header. Potentially downloading " +
                        "unnecessary data. Invalid format of refTimestamp argument: " + refTimestamp);
            }
        }

        if (mHttpHeader != null){
            for (Map.Entry<String, String> entry : mHttpHeader.entrySet()) {
                httpClient.addHeader(URLEncoder.encode(entry.getKey(), DEFAULT_PARAMS_ENCODING),URLEncoder.encode(entry.getValue(), DEFAULT_PARAMS_ENCODING));
            }
        }

        HttpResponse response = httpClient.get(getUrl(), null);
        if (response == null) {
            Log.w(TAG, "Request for manifest returned null response.");
            throw new IOException("Request for data manifest returned null response.");
        }

        int status = response.getStatus();
        if (status == HttpURLConnection.HTTP_OK) {
            Log.w(TAG, "Server returned HTTP_OK, so new data is available.");
            mServerTimestamp = getLastModified(response);
            Log.d(TAG, "Server timestamp for new data is: " + mServerTimestamp);
            String body = response.getBodyAsString();
            if (TextUtils.isEmpty(body)) {
                Log.e(TAG, "Request for manifest returned empty data.");
                throw new IOException("Error fetching conference data manifest: no data.");
            }
            Log.d(TAG, "Manifest " + mUrl + " read, contents: " + body);
            mBytesDownloaded += body.getBytes().length;
            return processManifest(body);
        } else if (status == HttpURLConnection.HTTP_NOT_MODIFIED) {
            // data on the server is not newer than our data
            Log.d(TAG, "HTTP_NOT_MODIFIED: data has not changed since " + refTimestamp);
            return null;
        } else {
            Log.e(TAG, "Error fetching conference data: HTTP status " + status);
            throw new IOException("Error fetching conference data: HTTP status " + status);
        }
    }

    // Returns the timestamp of the data downloaded from the server
    public String getServerDataTimestamp() {
        return mServerTimestamp;
    }

    /**
     * Process the data manifest and download data files referenced from it.
     *
     * @param manifestJson The JSON of the manifest file.
     * @return The contents of the set of files referenced from the manifest, or null
     * if none could be retrieved.
     * @throws IOException If an error occurs while retrieving information.
     */
    private String processManifest(String manifestJson) throws IOException {
        Log.d(TAG, "Processing data manifest, length " + manifestJson.length());

        try {
            JSONObject obj = new JSONObject(manifestJson);
            int resCode = Integer.valueOf(String.valueOf(obj.get("res_code")));
            if (resCode == Config.RESULT_STATUS_ERROR_SEVER_STATUS) {
                return null;
            } else if (resCode == Config.RESULT_STATUS_ERROR_SID) {
                return null;
            }
            return manifestJson;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    public long getTotalBytesDownloaded() {
        return mBytesDownloaded;
    }

    public long getTotalBytesReadFromCache() {
        return mBytesReadFromCache;
    }

    private String getLastModified(HttpResponse resp) {
        if (!resp.getHeaders().containsKey("Last-Modified")) {
            return "";
        }

        List<String> s = resp.getHeaders().get("Last-Modified");
        return s.isEmpty() ? "" : s.get(0);
    }

    /**
     * A type of ConsoleRequestLogger that does not log requests and responses.
     */
    private RequestLogger mQuietLogger = new ConsoleRequestLogger() {
        @Override
        public void logRequest(HttpURLConnection uc, Object content) throws IOException {
        }

        @Override
        public void logResponse(HttpResponse res) {
        }
    };


}
