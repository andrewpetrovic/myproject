package com.itic.mobile.zfyj.qh.login;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.itic.mobile.accounts.AccountUtils;
import com.itic.mobile.util.app.PrefUtils;
import com.itic.mobile.zfyj.qh.Config;
import com.itic.mobile.zfyj.qh.R;
import com.itic.mobile.zfyj.qh.login.model.LoginModel;
import com.itic.mobile.zfyj.qh.observer.IObserver;
import com.itic.mobile.zfyj.qh.observer.ObserverMessage;
import com.itic.mobile.zfyj.qh.push.SetDeleteTagReceiver;
import com.itic.mobile.txpush.receiver.MessageReceiver;
import com.itic.mobile.util.string.MD5Utils;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;

import java.lang.ref.WeakReference;

import static com.itic.mobile.util.logcat.LogUtils.LOGD;
import static com.itic.mobile.util.logcat.LogUtils.LOGW;


/**
 * 用于验证登陆信息是否正确，正确则以当前登陆用户身份注册push服务，不正确则回调UI
 */
public class LoginAndAuthHelper implements XGIOperateCallback,IObserver {
    private static final String TAG = LoginAndAuthHelper.class.getSimpleName();

    private static final int REQUEST_AUTHENTICATE = 100;
    private static final int REQUEST_RECOVER_FROM_AUTH_ERROR = 101;
    private static final int REQUEST_RECOVER_FROM_PLAY_SERVICES_ERROR = 102;
    private static final int REQUEST_PLAY_SERVICES_ERROR_DIALOG = 103;

    private String mAccountName;
    private boolean mStarted = false;
    boolean mResolving = false;
    private WeakReference<Activity> mActivityRef;
    private WeakReference<Callback> mCallbacksRef;
    private Context mAppContext;
    private LoginModel loginModel;

    private AccountManager accountManager;

    private GetTokenTask mTokenTask;

    public LoginAndAuthHelper(Activity activity, Callback callback, String accountName){
        this.mAccountName = accountName;
        this.mActivityRef = new WeakReference<Activity>(activity);
        this.mCallbacksRef = new WeakReference<Callback>(callback);
        this.mAppContext = activity.getApplicationContext();
        accountManager = (AccountManager) mAppContext.getSystemService(Context.ACCOUNT_SERVICE);
    }

    private Activity getActivity(String methodName) {
        Activity activity = mActivityRef.get();
        if (activity == null) {
            Log.i(TAG, "Helper lost Activity reference, ignoring (" + methodName + ")");
        }
        return activity;
    }

    public String getAccountName() {
        return mAccountName;
    }

    public boolean isStarted() {
        return mStarted;
    }

    public void start(){
        Activity activity = getActivity("start()");
        if (activity == null) {
            return;
        }
        if (mStarted) {
            Log.i(TAG, "Helper already started. Ignoring redundant call.");
            return;
        }
        mStarted = true;
        if (mResolving) {
            // if resolving, don't reconnect the plus client
            Log.i(TAG, "Helper ignoring signal to start because we're resolving a failure.");
            return;
        }
        Log.i(TAG, "Helper starting. now verify account " + mAccountName);
        String password = accountManager.getPassword(AccountUtils.getActiveAccount(mAppContext, Config.ACCOUNT_TYPE));
        LoginAction loginAction = new LoginAction(mAccountName, password, XGPushConfig.getToken(mAppContext), MD5Utils.md5(mAccountName + password + XGPushConfig.getToken(mAppContext) + Config.sKey),
                new LoginAction.LoginSuccessCallback() {
                    @Override
                    public void onSuccess(LoginModel mLoginModel) {
                        // 使用当前username注册信鸽Push平台
                        loginModel = mLoginModel;
                        registerPushWithUsername(mAccountName,loginModel);
                    }
                },
                new LoginAction.LoginFailCallback() {
                    @Override
                    public void onFail() {
                        //用户名密码错误
                        Callback callbacks;
                        if (null != (callbacks = mCallbacksRef.get())) {
                            callbacks.onAuthFailure(mAccountName,Config.RESULT_STATUS_ERROR_USERNAME_PWD);
                        }
                    }
                },
                new LoginAction.LoginErrorCallback() {

                    @Override
                    public void onError(int errorCode) {
                        //服务器异常
                        if (errorCode == Config.RESULT_STATUS_ERROR_SEVER_STATUS) {
                            Log.i(TAG,"server errror, need re verify account");
                            mStarted = false;
                        }
                        //SID错误
                        if (errorCode == Config.RESULT_STATUS_ERROR_SID) {
                            Callback callbacks;
                            if (null != (callbacks = mCallbacksRef.get())) {
                                callbacks.onAuthFailure(mAccountName,errorCode);
                            }
                        }
                        //上报token与后台记录不匹配
                        if (errorCode == Config.RESULT_STATUS_ERROR_TOKEN) {
                            Callback callbacks;
                            if (null != (callbacks = mCallbacksRef.get())) {
                                callbacks.onAuthFailure(mAccountName,errorCode);
                            }
                        }
                        //缺少参数
                        if (errorCode == Config.RESULT_STATUS_ERROR_PARAM) {
                            Callback callbacks;
                            if (null != (callbacks = mCallbacksRef.get())) {
                                callbacks.onAuthFailure(mAccountName,errorCode);
                            }
                        }
                        //网络连接异常
                        if (errorCode  == Config.RESULT_STATUS_ERROR_CONNECTION) {
                            mStarted = false;
                            Callback callbacks;
                            if (null != (callbacks = mCallbacksRef.get())) {
                                callbacks.onAuthFailure(mAccountName,errorCode);
                            }
                        }
                    }
                }
        );
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        Activity activity = getActivity("onActivityResult()");
        if (activity == null) {
            return false;
        }

        if (requestCode == REQUEST_AUTHENTICATE ||
                requestCode == REQUEST_RECOVER_FROM_AUTH_ERROR ||
                requestCode == REQUEST_PLAY_SERVICES_ERROR_DIALOG) {

            LOGD(TAG, "onActivityResult, req=" + requestCode + ", result=" + resultCode);
            if (requestCode == REQUEST_RECOVER_FROM_PLAY_SERVICES_ERROR) {
                mResolving = false;
            }

            if (resultCode == Activity.RESULT_OK) {
                XGPushManager.registerPush(mAppContext,mAccountName,this);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                LOGD(TAG, "User explicitly cancelled sign-in/auth flow.");
                // save this as a preference so we don't annoy the user again
                PrefUtils.markUserRefusedSignIn(mAppContext);
            } else {
                LOGW(TAG, "Failed to recover from a login/auth failure, resultCode=" + resultCode);
            }
            return true;
        }
        return false;
    }

    private void registerPushWithUsername(String mAccountName,LoginModel mLoginModel) {
        XGPushManager.registerPush(mAppContext,mAccountName,this);
    }

    /**
     * 注册成功时执行设置/删除tag、修改数据操作
     * @param data
     * @param flag
     */
    @Override
    public void onSuccess(Object data, int flag) {
        //注册receiver，用来接收 设置/删除tag广播
        SetDeleteTagReceiver setDeleteTagReceiver = new SetDeleteTagReceiver();
        //为receiver设置观察者，监听广播类型
        setDeleteTagReceiver.attachObserver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MessageReceiver.TAG_ACTION);
        mAppContext.registerReceiver(setDeleteTagReceiver,intentFilter);

        //判断server侧组织机构id是否已经变更
        if (!loginModel.zzjg_id.equals(accountManager.getUserData(AccountUtils.getActiveAccount(mAppContext, Config.ACCOUNT_TYPE),AccountUtils.KEY_ZZJG_ID))){
            //如果变更，则在信鸽推送平台删除旧的组织机构id（deleteTag），然后在handleObserverMessage方法中设置新的组织机构id(setTag)
            XGPushManager.deleteTag(mAppContext,accountManager.getUserData(AccountUtils.getActiveAccount(mAppContext, Config.ACCOUNT_TYPE),AccountUtils.KEY_ZZJG_ID));
        }else{
            //如果没有变更，直接setTag
            XGPushManager.setTag(mAppContext,loginModel.zzjg_id);
        }
        //如果server侧用户姓名变更，变更accountmanager缓存姓名
        if (!loginModel.user_xm.equals(accountManager.getUserData(AccountUtils.getActiveAccount(mAppContext, Config.ACCOUNT_TYPE),AccountUtils.KEY_USER_XM))){
            accountManager.setUserData(AccountUtils.getActiveAccount(mAppContext, Config.ACCOUNT_TYPE),AccountUtils.KEY_USER_XM,loginModel.user_xm);
        }
        //如果用户角色类型变更，更新accountmanager缓存用户角色类型
        if (!loginModel.yhlx_code.equals(accountManager.getUserData(AccountUtils.getActiveAccount(mAppContext, Config.ACCOUNT_TYPE),AccountUtils.KEY_YHLX_CODE))){
            accountManager.setUserData(AccountUtils.getActiveAccount(mAppContext, Config.ACCOUNT_TYPE),AccountUtils.KEY_YHLX_CODE,loginModel.yhlx_code);
        }

        // 应该在连接成功后就回调通知activity已经登录成功，至于是否成功设置tag，应当另外处理
        //通过判断token是否为空决定是否是第一次登陆，这里设置token是假的，为以后使用OAuth2.0授权机制预留
        if (!AccountUtils.hasToken(mAppContext, AccountUtils.getActiveAccountName(mAppContext))) {
            Log.i(TAG, "We don't have auth token for " + mAccountName + " yet, so getting it.");
            mTokenTask = new GetTokenTask();
            mTokenTask.execute();
        }else{
            Log.i(TAG, "No need for auth token, we already have it.");
            reportAuthSuccess(false);
        }
    }

    /**
     * 推送服务注册失败时进行相关UI提示
     * @param data
     * @param errCode
     * @param msg
     */
    @Override
    public void onFail(Object data, int errCode, String msg) {
        Toast.makeText(mAppContext, R.string.error_register,Toast.LENGTH_SHORT).show();
        Callback callbacks;
        if (null != (callbacks = mCallbacksRef.get())) {
            callbacks.onAuthFailure(mAccountName,Config.RESULT_STATUS_ERROR_PUSH_REGISTER);
        }
    }

    @Override
    public void handleObserverMessage(ObserverMessage message) {
        //删除tag成功后，重新设置tag
        if (message.getMessageType() == Config.BROADCAST_DELETE_TAG_SUCCESS){
            Log.i(TAG,"tag delete success");
            //当删除成功时，设置新的tag
            XGPushManager.setTag(mAppContext,loginModel.zzjg_id);
        }
        //设置tag成功
        if (message.getMessageType() == Config.BROADCAST_SET_TAG_SUCCESS){
            Log.i(TAG,"new tag set success");
            //如果需要更新tag
            if (!loginModel.zzjg_id.equals(accountManager.getUserData(AccountUtils.getActiveAccount(mAppContext, Config.ACCOUNT_TYPE),AccountUtils.KEY_ZZJG_ID))){
                accountManager.setUserData(AccountUtils.getActiveAccount(mAppContext, Config.ACCOUNT_TYPE),AccountUtils.KEY_ZZJG_ID,loginModel.zzjg_id);
                accountManager.setUserData(AccountUtils.getActiveAccount(mAppContext, Config.ACCOUNT_TYPE),AccountUtils.KEY_ZZJG_MC,loginModel.zzjg_mc);
            }
//            //通过判断token是否为空决定是否是第一次登陆，这里设置token是假的，为以后使用OAuth2.0授权机制预留
//            if (!AccountUtils.hasToken(mAppContext, AccountUtils.getActiveAccountName(mAppContext))) {
//                Log.i(TAG, "We don't have auth token for " + mAccountName + " yet, so getting it.");
//                mTokenTask = new GetTokenTask();
//                mTokenTask.execute();
//            }else{
//                Log.i(TAG, "No need for auth token, we already have it.");
//                reportAuthSuccess(false);
//            }
        }
    }

    private class GetTokenTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            final String token = "token";
            Log.i(TAG, "Saving token: " + (token == null ? "(null)" : "(length " +
                    token.length() + ")") + " for account "  + AccountUtils.getActiveAccountName(mAppContext));
            AccountUtils.setAuthToken(mAppContext,AccountUtils.getActiveAccountName(mAppContext),token);
            return token;
        }

        @Override
        protected void onPostExecute(String token) {
            super.onPostExecute(token);
            reportAuthSuccess(true);
        }
    }

    private void reportAuthSuccess(boolean newlyAuthenticated) {
        Log.i(TAG, "Auth success for account " + AccountUtils.getActiveAccountName(mAppContext) + ", newlyAuthenticated=" + newlyAuthenticated);
        Log.i(TAG,"");
        Callback callback;
        if (null != (callback = mCallbacksRef.get())) {
            callback.onAuthSuccess(AccountUtils.getActiveAccountName(mAppContext), newlyAuthenticated);
        }
    }
}
