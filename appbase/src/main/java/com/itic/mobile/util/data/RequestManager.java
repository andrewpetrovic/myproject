package com.itic.mobile.util.data;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.itic.mobile.app.BaseApp;

/**
 * 
 * @author andrew
 *
 */
public class RequestManager {
    public static RequestQueue mRequestQueue = Volley.newRequestQueue(BaseApp.getContext());

    private RequestManager() {
        // no instances
    }

    public static void addRequest(Request<?> request, Object tag) {
        if (tag != null) {
            request.setTag(tag);
        }
        mRequestQueue.add(request);
    }

    public static void cancelAll(Object tag) {
        mRequestQueue.cancelAll(tag);
    }
}
