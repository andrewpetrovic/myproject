package com.itic.mobile.zfyj.qh.base;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.itic.mobile.zfyj.qh.Config;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

/**
 * HTTPRequest,用于Volley框架
 */
public class HttpRequest extends Request<String> {
    private static final String TAG = "HttpRequest";
    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";

    private Listener<String> mListener;

    private Map<String, String> mHttpHeader = null;
    private Map<String, String> mHttpParams = null;

    public HttpRequest(String url){
        super(Method.GET, url, null);
    }

    public HttpRequest(String url,int method){
        super(method, url, null);
    }

    public HttpRequest(int method, String url, ErrorListener errorListener) {
        super(method, url, errorListener);
    }

    private String prepareURL(String paramsEncoding) {
        StringBuffer urlStr = new StringBuffer();
        try {
            urlStr.append(Config.SERVER_HOST + super.getUrl());
            if (getMethod() == Method.GET) {
                if (mHttpParams != null) {
                    //由于Server侧无法处理编码后的？符号，所以GET请求的？不作编码处理
                    urlStr.append("?");
                    for (Map.Entry<String, String> entry : mHttpParams.entrySet()) {
                        urlStr.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                        urlStr.append("=");
                        urlStr.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                        urlStr.append("&");
                    }
                }
            }
            return urlStr.toString();
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }

    @Override
    public String getUrl() {
        return prepareURL(DEFAULT_PARAMS_ENCODING);
    }

    public void setHeaders(Map<String, String> httpHeader) {
        mHttpHeader = httpHeader;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if (mHttpHeader != null) {
            return mHttpHeader;
        } else {
            return super.getHeaders();
        }
    }

    public void setParams(Map<String, String> httpParams) {
        mHttpParams = httpParams;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        if (mHttpParams != null) {
            return mHttpParams;
        } else {
            return super.getParams();
        }
    }

    public void setListener(Listener<String> listener) {
        mListener = listener;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }
}
