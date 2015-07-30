package com.itic.mobile.sync;

/**
 * Created by JEEKR on 2014/12/1.
 */
public interface SyncCallBack {
    public void onSyncActiveCallback();
    public void onSyncPendingCallback();
    public void onSyncFinishCallback();
    public void onSync(boolean refreshing);
}
