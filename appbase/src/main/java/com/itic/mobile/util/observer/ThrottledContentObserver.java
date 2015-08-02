package com.itic.mobile.util.observer;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

/**
 * 通过Handler对ContentObserver，使其在大量数据变动的时候，尽量保证1秒钟向UI线程通知一次的频率。
 */
public class ThrottledContentObserver extends ContentObserver {
    Handler mMyHandler;
    Runnable mScheduledRun = null;
    private static final int THROTTLE_DELAY = 1000;
    Callbacks mCallback = null;

    public interface Callbacks {
        public void onThrottledContentObserverFired();
    }

    public ThrottledContentObserver(Callbacks callback) {
        super(null);
        mMyHandler = new Handler();
        mCallback = callback;
    }

    @Override
    public void onChange(boolean selfChange) {
        if (mScheduledRun != null) {
            mMyHandler.removeCallbacks(mScheduledRun);
        } else {
            mScheduledRun = new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.onThrottledContentObserverFired();
                    }
                }
            };
        }
        mMyHandler.postDelayed(mScheduledRun, THROTTLE_DELAY);
    }

    public void cancelPendingCallback() {
        if (mScheduledRun != null) {
            mMyHandler.removeCallbacks(mScheduledRun);
        }
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        onChange(selfChange);
    }
}
