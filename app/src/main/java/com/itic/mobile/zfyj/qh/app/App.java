package com.itic.mobile.zfyj.qh.app;

import android.util.Log;

import com.itic.mobile.app.BaseApp;
import com.itic.mobile.zfyj.qh.Config;
import com.itic.mobile.zfyj.qh.sync.VolleyUtil;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;

/**
 * Created by JEEKR on 2015/1/27.
 */
public class App extends BaseApp implements XGIOperateCallback {

    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
        XGPushConfig.enableDebug(getApplicationContext(), true);
        XGPushConfig.setAccessId(getApplicationContext(), Config.accessId);
        XGPushConfig.setAccessKey(getApplicationContext(), Config.accessKey);
        XGPushManager.registerPush(getApplicationContext(), this);

        VolleyUtil.initialize(getApplicationContext());
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
