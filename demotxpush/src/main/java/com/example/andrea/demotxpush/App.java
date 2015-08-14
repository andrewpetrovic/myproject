package com.example.andrea.demotxpush;

import android.app.Application;
import android.util.Log;

import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;

public class App extends Application implements XGIOperateCallback{
    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
        XGPushConfig.enableDebug(getApplicationContext(), true);
        XGPushConfig.setAccessId(getApplicationContext(), 2100141006);
        XGPushConfig.setAccessKey(getApplicationContext(), "A72ZJE521IRN");
        XGPushManager.registerPush(getApplicationContext(),this);
    }

    @Override
    public void onSuccess(Object data, int errCode) {
        Log.w(TAG,
                "+++ register push sucess. token:" + data);
    }

    @Override
    public void onFail(Object data, int errCode, String msg) {
        Log.w(TAG,
                "+++ register push fail. token:" + data
                        + ", errCode:" + errCode + ",msg:"
                        + msg);
    }
}
