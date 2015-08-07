package com.itic.mobile.net;

import android.os.AsyncTask;

import com.itic.mobile.Config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by andrew on 2014/8/19.
 */
@Deprecated
public class NetConnection {

    /**
     * 通用网络连接
     */
    public NetConnection(final boolean isAsyncTask,final String url, final HttpMethod method ,final Callback callback , final String... kvs) {
        if (isAsyncTask){
            netConnectionTask(url,method,callback,kvs);
        }else{
            netConnection(url,method,callback,kvs);
        }
    }

    private void netConnection(String url, HttpMethod method ,Callback callback , String... kvs){
        String result = connect(url,method,callback,kvs);
        if (result!=null) {
            if (callback!=null) {
                callback.onSuccess(result);
            }
        }else{
            if (callback!=null) {
                callback.onFail();
            }
        }
    }

    private void netConnectionTask(final String url, final HttpMethod method ,final Callback callback , final String... kvs){
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                return connect(url,method,callback,kvs);
            }

            @Override
            protected void onPostExecute(String result) {
                if (result!=null) {
                    if (callback!=null) {
                        callback.onSuccess(result);
                    }
                }else{
                    if (callback!=null) {
                        callback.onFail();
                    }
                }
                super.onPostExecute(result);
            }
        }.execute();
    }

    private String connect(String url, HttpMethod method ,Callback callback , String... kvs){
        callback.onLoading();
        StringBuffer paramsStr = new StringBuffer();
        for (int i = 0; i < kvs.length; i+=2) {
            paramsStr.append(kvs[i]).append("=").append(kvs[i+1]).append("&");
        }
        try {
            URLConnection mUrlConnection;

            switch (method) {
                case POST:
                    mUrlConnection = new URL(url).openConnection();
                    mUrlConnection.setDoOutput(true);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(mUrlConnection.getOutputStream(), Config.CHARSET));
                    bw.write(paramsStr.toString());
                    bw.flush();
                    break;
                default:
                    String str = url+"?"+paramsStr.toString();
                    mUrlConnection = new URL(str).openConnection();
//                            mUrlConnection = new URL(url+"?"+paramsStr.toString()).openConnection();
                    break;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(mUrlConnection.getInputStream(), Config.CHARSET));
            String line = null;
            StringBuffer result = new StringBuffer();
            while((line=br.readLine())!=null){
                result.append(line);
            }

            return result.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static interface Callback{
        void onSuccess(String result);
        void onFail();
        void onLoading();
    }
}
