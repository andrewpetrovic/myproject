package com.itic.mobile.util.network;

import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkUtils {
	
	private static final String TAG = NetworkUtils.class.getSimpleName();
	/**
	 * 判断网络是否可用，一般在调用网络相关接口前使用
	 * @param context
	 * @return
	 */
	public static boolean isConnect(Context context) {
		boolean isConnected;
		ConnectivityManager mConnectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (mConnectivityManager != null) {
			NetworkInfo mActiveNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mActiveNetworkInfo != null){
				if (mActiveNetworkInfo.isConnected()) {
					if (NetworkInfo.State.CONNECTED == mActiveNetworkInfo.getState()) {
						isConnected = true;
					}else{
						isConnected = false;
						Log.e(TAG, "ActiveNetworkInfo.getState() isn't CONNECT");
					}
				}else{
					isConnected = false;
					Log.e(TAG, "connectivitManager isn't connected");
				}
			}else{
				isConnected = false;
				Log.e(TAG, "Can't get connectivitManager");
			}
		} else {
			isConnected = false;
			Log.e(TAG, "Can't get connectivitManager");
		}
		return isConnected;
	}
	
	/**
	 * 
	 * @param responseBody
	 * @return str 将responseBody解析成为 str，如果不能使用UTF-8编码解析，则返回90，代表解析错误
	 */
	public static String parseResponseBody(byte[] responseBody){
		String str = null;
		try {
			str = new String(responseBody,"UTF-8");
			Log.i(TAG, "str:" + str);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			str = "{\"resCode\":90}";
		}
		return str;
	}
}
