package com.itic.mobile.zfyj.qh.sync;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by JEEKR on 2015/5/26.
 */
public class VolleyUtil {
    private static RequestQueue mRequestQueue;

    public static void initialize(Context context){
        if (mRequestQueue == null){
            synchronized (VolleyUtil.class){
                if (mRequestQueue == null){
                    mRequestQueue = Volley.newRequestQueue(context);
                }
            }
        }
    }
    public static RequestQueue getRequestQueue(){
        if (mRequestQueue == null){
            throw new RuntimeException("please init RequestQueue");
        }
        return mRequestQueue;
    }
}
