package com.itic.mobile.volley;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * VolleyUtil
 */
public class VolleyUtil {
    private static RequestQueue mRequestQueue;

    /**
     * 初始化volley
     */
    public static void initialize(Context context){
        if (mRequestQueue == null){
            synchronized (VolleyUtil.class){
                if (mRequestQueue == null){
                    mRequestQueue = Volley.newRequestQueue(context);
                }
            }
        }
    }

    /**
     * 获得RequestQueue
     */
    public static RequestQueue getRequestQueue(){
        if (mRequestQueue == null){
            throw new RuntimeException("please init RequestQueue");
        }
        return mRequestQueue;
    }
}
