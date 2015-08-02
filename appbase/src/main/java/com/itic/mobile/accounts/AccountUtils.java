package com.itic.mobile.accounts;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.itic.mobile.Config;


public class AccountUtils {

    private static final String TAG = AccountUtils.class.getSimpleName();

    public static String KEY_YHLX_CODE = "KEY_YHLX_CODE";
    public static String KEY_USER_ID = "KEY_USER_ID";
    public static String KEY_USER_XM = "KEY_USER_XM";
    public static String KEY_ZZJG_ID = "KEY_ZZJG_ID";
    public static String KEY_ZZJG_MC = "KEY_ZZJG_MC";

    private static final String PREF_ACTIVE_ACCOUNT = "chosen_account";
    private static final String PREFIX_PREF_AUTH_TOKEN = "auth_token_";

    /**
     * 根据指定的用户名获取帐号对象
     * @param accountType
     * @param username
     * @return
     */
    public static Account GetAccount(String accountType,String username) {
        final String accountName = username;
        return new Account(accountName,accountType);
    }

    private static SharedPreferences getSharedPreferences(final Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * sp文件缓存当前活动帐号
     */
    public static boolean setActiveAccount(final Context context,final String accountName){
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putString(PREF_ACTIVE_ACCOUNT,accountName).commit();
        return true;
    }

    /**
     * 判断当前是否有活动帐号
     * @param context
     * @return
     */
    public static boolean hasActiveAccount(final Context context){
        return !TextUtils.isEmpty(getActiveAccountName(context));
    }

    /**
     * 从SP中获取当前活动帐号名
     * @param context
     * @return
     */
    public static String getActiveAccountName(final Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getString(PREF_ACTIVE_ACCOUNT,null);
    }

    /**
     * 返回当前活动帐号对象
     * @param context
     * @return
     */
    public static Account getActiveAccount(Context context,String accountType) {
        String account = getActiveAccountName(context);
        if (account != null){
            return new Account(account, accountType);
        }else{
            return null;
        }
    }

    public static boolean hasToken(final Context context, final String accountName) {
        SharedPreferences sp = getSharedPreferences(context);
        return !TextUtils.isEmpty(sp.getString(makeAccountSpecificPrefKey(accountName,
                PREFIX_PREF_AUTH_TOKEN), null));
    }

    public static String getAuthToken(final Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        return hasActiveAccount(context) ?
                sp.getString(makeAccountSpecificPrefKey(context, PREFIX_PREF_AUTH_TOKEN), null) : null;
    }

    public static void setAuthToken(final Context context, final String accountName, final String authToken) {
        Log.i(TAG, "Auth token of length "
                + (TextUtils.isEmpty(authToken) ? 0 : authToken.length()) + " for "
                + accountName);
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putString(makeAccountSpecificPrefKey(accountName, PREFIX_PREF_AUTH_TOKEN),
                authToken).commit();
        Log.i(TAG, "Auth Token: " + authToken);
    }

    public static void setAuthToken(final Context context, final String authToken) {
        if (hasActiveAccount(context)) {
            setAuthToken(context, getActiveAccountName(context), authToken);
        } else {
            Log.i(TAG, "Can't set auth token because there is no chosen account!");
        }
    }

    private static String makeAccountSpecificPrefKey(Context ctx, String prefix) {
        return hasActiveAccount(ctx) ? makeAccountSpecificPrefKey(getActiveAccountName(ctx),
                prefix) : null;
    }

    private static String makeAccountSpecificPrefKey(String accountName, String prefix) {
        return prefix + accountName;
    }
}
