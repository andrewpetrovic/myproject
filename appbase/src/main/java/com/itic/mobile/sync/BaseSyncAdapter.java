package com.itic.mobile.sync;

import android.content.AbstractThreadedSyncAdapter;
import android.content.Context;

/**
 * Created by andrew on 2014/8/19.
 */
public abstract class BaseSyncAdapter extends AbstractThreadedSyncAdapter {

    protected Context context;

    public BaseSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.context = context;
    }

    public BaseSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        this.context = context;
    }

}
