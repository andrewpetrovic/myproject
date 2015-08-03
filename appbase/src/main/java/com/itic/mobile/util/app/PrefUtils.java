package com.itic.mobile.util.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.itic.mobile.Config;
import com.itic.mobile.util.datetime.DateTimeUtils;
import com.itic.mobile.util.datetime.TimeUtils;
import com.itic.mobile.util.ui.UIUtils;

import java.util.TimeZone;

import static com.itic.mobile.util.logcat.LogUtils.LOGD;
import static com.itic.mobile.util.logcat.LogUtils.makeLogTag;

/**
 * SharedPreference相关的工具类和常量.
 */
public class PrefUtils  {
    private static final String TAG = makeLogTag("PrefUtils");

    public static boolean PREF_TOS_APP_IS_RUNNING = false;

    /**
     * 为true时，所有时间采用当前系统时间
     */
    public static final String PREF_LOCAL_TIMES = "pref_local_times";

    /**
     * 是否提供option menu 功能，当为true时，强制不提供option menu
     */
    public static final String PREF_INVALIDATE_OPTION_MENU = "pref_invalidate_option_menu";

    /**
     * 是否完成数据预加载.
     */
    public static final String PREF_DATA_BOOTSTRAP_DONE = "pref_data_bootstrap_done";

    /**
     * 是否在启动时登录 (默认 true).
     */
    public static final String PREF_USER_REFUSED_SIGN_IN = "pref_user_refused_sign_in";

    /**
     * Debug 提示时候显示
     */
    public static final String PREF_DEBUG_BUILD_WARNING_SHOWN = "pref_debug_build_warning_shown";

    /** 用户时候启动了低功耗蓝牙技术 */
    public static final String PREF_BLE_ENABLED = "pref_ble_enabled";

    /** 最后一次尝试同步的时间 */
    public static final String PREF_LAST_SYNC_ATTEMPTED = "pref_last_sync_attempted";

    /** 最后一次同步成功的时间 */
    public static final String PREF_LAST_SYNC_SUCCEEDED = "pref_last_sync_succeeded";

    /** 同步间隔是否设置 */
    public static final String PREF_CUR_SYNC_INTERVAL = "pref_cur_sync_interval";

    /**
     * 是否显示过欢迎界面（只显示一次）
     */
    public static final String PREF_WELCOME_DONE = "pref_welcome_done";

    public static boolean isTosAppIsRunning(){
        return PREF_TOS_APP_IS_RUNNING;
    }

    public static void markTosAppIsRunning(boolean isRunning){
        PrefUtils.PREF_TOS_APP_IS_RUNNING = isRunning;
    }

    public static TimeZone getDisplayTimeZone(Context context) {
        TimeZone defaultTz = TimeZone.getDefault();
        return (isUsingLocalTime(context) && defaultTz != null)
                ? defaultTz : Config.CONFERENCE_TIMEZONE;
    }

    public static boolean isUsingLocalTime(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_LOCAL_TIMES, false);
    }

    public static void setUsingLocalTime(final Context context, final boolean usingLocalTime) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_LOCAL_TIMES, usingLocalTime).commit();
    }

    public static boolean isInvalidateOptionMenu(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_INVALIDATE_OPTION_MENU, true);
    }

    public static void markDataBootstrapDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_DATA_BOOTSTRAP_DONE, true).commit();
    }

    public static boolean isDataBootstrapDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_DATA_BOOTSTRAP_DONE, false);
    }

    public static void init(final Context context) {
        // Check what year we're configured for
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setInvalidateOptionMenu(final Context context, final boolean isAtVenue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_INVALIDATE_OPTION_MENU, isAtVenue).commit();
    }

    public static void markUserRefusedSignIn(final Context context) {
        markUserRefusedSignIn(context, true);
    }

    public static void markUserRefusedSignIn(final Context context, final boolean refused) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_USER_REFUSED_SIGN_IN, refused).commit();
    }

    public static boolean hasUserRefusedSignIn(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_USER_REFUSED_SIGN_IN, false);
    }

    public static boolean wasDebugWarningShown(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_DEBUG_BUILD_WARNING_SHOWN, false);
    }

    public static void markDebugWarningShown(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_DEBUG_BUILD_WARNING_SHOWN, true).commit();
    }

    public static boolean hasEnabledBle(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_BLE_ENABLED, false);
    }

    public static void setBleStatus(final Context context, boolean status) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_BLE_ENABLED, status).commit();
    }

    public static boolean isWelcomeDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_WELCOME_DONE, false);
    }

    public static void markWelcomeDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_WELCOME_DONE, true).commit();
    }

    public static long getLastSyncAttemptedTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_LAST_SYNC_ATTEMPTED, 0L);
    }

    public static void markSyncAttemptedNow(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(PREF_LAST_SYNC_ATTEMPTED, DateTimeUtils.getCurrentTime(context)).commit();
    }

    public static long getLastSyncSucceededTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_LAST_SYNC_SUCCEEDED, 0L);
    }

    public static void markSyncSucceededNow(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(PREF_LAST_SYNC_SUCCEEDED, DateTimeUtils.getCurrentTime(context)).commit();
    }

    public static long getCurSyncInterval(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_CUR_SYNC_INTERVAL, 0L);
    }

    public static void setCurSyncInterval(final Context context, long interval) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(PREF_CUR_SYNC_INTERVAL, interval).commit();
    }

    public static void registerOnSharedPreferenceChangeListener(final Context context,
                                                                SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterOnSharedPreferenceChangeListener(final Context context,
                                                                  SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }
}

