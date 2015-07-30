package com.itic.mobile.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public abstract class SyncService extends Service {
    private static final String TAG = SyncService.class.getSimpleName();
    protected static final Object sSyncAdapterLock = new Object();
    protected static BaseSyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, TAG + " onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.i(TAG,"SyncService onBind");
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
