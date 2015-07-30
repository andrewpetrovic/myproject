package com.itic.mobile.zfyj.qh.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.itic.mobile.accounts.AccountUtils;
import com.itic.mobile.util.app.PrefUtils;
import com.itic.mobile.util.datetime.DateTimeUtils;
import com.itic.mobile.util.string.MD5Utils;
import com.itic.mobile.util.ui.UIUtils;
import com.itic.mobile.zfyj.qh.Config;
import com.itic.mobile.zfyj.qh.QHYJApi;
import com.itic.mobile.zfyj.qh.login.model.LoginModel;
import com.itic.mobile.zfyj.qh.provider.Contract;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by JEEKR on 2015/1/27.
 */
public class SyncHelper {

    private static final String TAG = "SyncHelper";

    private Context mContext;

    private AppDataHandler appDataHandler;

    public SyncHelper(Context context) {
        mContext = context;
        appDataHandler = new AppDataHandler(context);
    }

    public static void CreateSyncAccount(Context context, String username, String password, LoginModel mLoginModel) {
        //创建account对象，并且为account设置username
        Account account = AccountUtils.GetAccount(Config.ACCOUNT_TYPE, username);
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        //将用户信息保存在bundle中
        Bundle userdataBundle = new Bundle();
        userdataBundle.putString(AccountUtils.KEY_USER_ID, mLoginModel.user_id);
        userdataBundle.putString(AccountUtils.KEY_USER_XM, mLoginModel.user_xm);
        userdataBundle.putString(AccountUtils.KEY_YHLX_CODE, mLoginModel.yhlx_code);
        userdataBundle.putString(AccountUtils.KEY_ZZJG_ID, mLoginModel.zzjg_id);
        userdataBundle.putString(AccountUtils.KEY_ZZJG_MC, mLoginModel.zzjg_mc);
        if (accountManager.addAccountExplicitly(account, password, userdataBundle)) {
            //将username设置为当前活动帐号
            AccountUtils.setActiveAccount(context, username);
        }
    }


    public static void requestManualSync(Account mChosenAccount){
        requestManualSync(mChosenAccount, false);
    }

    public static void requestManualSync(Account mChosenAccount, boolean userDataSyncOnly) {
        if (mChosenAccount != null) {
            Log.d(TAG, "Requesting manual sync for account " + mChosenAccount.name
                    +" userDataSyncOnly="+userDataSyncOnly);
            Bundle b = new Bundle();
            b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            if (userDataSyncOnly) {
                b.putBoolean(SyncAdapter.EXTRA_SYNC_USER_DATA_ONLY, true);
            }
            ContentResolver.setSyncAutomatically(mChosenAccount, Contract.CONTENT_AUTHORITY, true);
            ContentResolver.setIsSyncable(mChosenAccount, Contract.CONTENT_AUTHORITY, 1);

            boolean pending = ContentResolver.isSyncPending(mChosenAccount,
                    Contract.CONTENT_AUTHORITY);
            if (pending) {
                Log.d(TAG, "Warning: sync is PENDING. Will cancel.");
            }
            boolean active = ContentResolver.isSyncActive(mChosenAccount,
                    Contract.CONTENT_AUTHORITY);
            if (active) {
                Log.d(TAG, "Warning: sync is ACTIVE. Will cancel.");
            }

            if (pending || active) {
                Log.d(TAG, "Cancelling previously pending/active sync.");
                ContentResolver.cancelSync(mChosenAccount, Contract.CONTENT_AUTHORITY);
            }

            Log.d(TAG, "Requesting sync now.");
            ContentResolver.requestSync(mChosenAccount, Contract.CONTENT_AUTHORITY, b);
        } else {
            Log.d(TAG, "Can't request manual sync -- no chosen account.");
        }
    }

    public void performSync(SyncResult syncResult, Account account, Bundle extras) {
        boolean dataChanged = false;

        if (!PrefUtils.isDataBootstrapDone(mContext)) {
            Log.d(TAG, "Sync aborting (data bootstrap not done yet)");
            return;
        }

        final boolean userDataOnly = extras.getBoolean(SyncAdapter.EXTRA_SYNC_USER_DATA_ONLY, false);

        Log.i(TAG, "Performing sync for account: " + account);
        PrefUtils.markSyncAttemptedNow(mContext);

        final int OP_REMOTE_SYNC = 0;
        final int OP_USER_DATA_SYNC = 1;

        int[] opsToPerform = userDataOnly ?
                new int[]{OP_USER_DATA_SYNC} :
                new int[]{OP_REMOTE_SYNC, OP_USER_DATA_SYNC};


        for (int op : opsToPerform) {
            try {
                switch (op) {
                    case OP_REMOTE_SYNC:
                        doRemoteSync();
                        break;
                    case OP_USER_DATA_SYNC:
                        doUserScheduleSync(account.name);
                        break;
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                Log.e(TAG, "Error performing remote sync.");
                increaseIoExceptions(syncResult);
            }
        }

        int operations = appDataHandler.getContentProviderOperationsDone();
        if (syncResult != null && syncResult.stats != null) {
            syncResult.stats.numEntries += operations;
            syncResult.stats.numUpdates += operations;
        }

        Log.i(TAG, "End of sync (" + (dataChanged ? "data changed" : "no data change") + ")");

        updateSyncInterval(mContext, account);
    }

    private void doUserScheduleSync(String name) {
    }

    private void doRemoteSync() throws IOException {
        if (!isOnline()) {
            Log.d(TAG, "Not attempting remote sync because device is OFFLINE");
            return;
        }

        SyncDataFetcher fetcher = new SyncDataFetcher(QHYJApi.GetTXL);
        Map<String, String> requestParams = new HashMap<String, String>();
        AccountManager am = AccountManager.get(mContext);
        Account account = AccountUtils.getActiveAccount(mContext, Config.ACCOUNT_TYPE);
        requestParams.put("a", am.getUserData(account, AccountUtils.KEY_USER_ID));
        requestParams.put("sid", MD5Utils.md5(am.getUserData(account, AccountUtils.KEY_USER_ID) + am.getUserData(account, AccountUtils.KEY_ZZJG_ID) + Config.sKey));
        fetcher.setHttpParams(requestParams);
        String data = fetcher.fetchConferenceDataIfNewer(appDataHandler.getDataTimestamp());

        if (data != null){
            Log.i(TAG, "Applying remote data.");

            appDataHandler.applyConferenceData(new String[]{data},
                    fetcher.getServerDataTimestamp(), true);
            Log.i(TAG, "Done applying remote data.");

            PrefUtils.markSyncSucceededNow(mContext);
        }else {
            // no data to process (everything is up to date)

            // mark that conference data sync succeeded
            PrefUtils.markSyncSucceededNow(mContext);
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void increaseIoExceptions(SyncResult syncResult) {
        if (syncResult != null && syncResult.stats != null) {
            ++syncResult.stats.numIoExceptions;
        }
    }

    public static long calculateRecommendedSyncInterval(final Context context) {
        long now = UIUtils.getCurrentTime(context);
        long tomorrow = UIUtils.getNextDayTime();
        //如果同步时间早于19点，则当天19点时再发起同步，否则明天同一时间同步
        if (tomorrow - now < (UIUtils.HOUR_MILLIS * 6)) {
            return UIUtils.DAY_MILLIS;
        } else {
            return (tomorrow - now - (UIUtils.HOUR_MILLIS * 6));
        }
    }

    public static void updateSyncInterval(final Context context, final Account account) {
        Log.d(TAG, "Checking sync interval for " + account);
        long recommended = calculateRecommendedSyncInterval(context);
        long current = PrefUtils.getCurSyncInterval(context);
        Log.d(TAG, "Recommended sync interval " + recommended + ", current " + current);
        if (recommended != current) {
            Log.d(TAG, "Setting up sync for account " + account + ", interval " + recommended + "ms");
            ContentResolver.setIsSyncable(account, Contract.CONTENT_AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(account, Contract.CONTENT_AUTHORITY, true);
            ContentResolver.addPeriodicSync(account, Contract.CONTENT_AUTHORITY,
                    new Bundle(), recommended / 1000L);
            PrefUtils.setCurSyncInterval(context, recommended);
        } else {
            Log.d(TAG, "No need to update sync interval.");
        }
    }

//    public static class AuthException extends RuntimeException {
//    }
}
