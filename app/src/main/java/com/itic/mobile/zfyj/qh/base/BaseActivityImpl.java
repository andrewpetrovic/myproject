package com.itic.mobile.zfyj.qh.base;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itic.mobile.accounts.AccountUtils;
import com.itic.mobile.util.database.JSONHandler;
import com.itic.mobile.util.datetime.DateTimeUtils;
import com.itic.mobile.zfyj.qh.Config;
import com.itic.mobile.zfyj.qh.R;
import com.itic.mobile.zfyj.qh.splashscreen.SplashScreenActivity;
import com.itic.mobile.zfyj.qh.contacts.ui.BrowseContactsActivity;
import com.itic.mobile.zfyj.qh.login.LoginActivity;
import com.itic.mobile.util.app.PrefUtils;
import com.itic.mobile.zfyj.qh.provider.Contract;
import com.itic.mobile.zfyj.qh.jobs.ui.BrowseJobsActivity;
import com.itic.mobile.zfyj.qh.sync.SyncHelper;
import com.itic.mobile.zfyj.qh.test.TestActivity2;
import com.itic.mobile.zfyj.qh.test.TestActivity3;
import com.itic.mobile.zfyj.qh.test.TestActivity4;

import java.io.IOException;

/**
 * Created by JEEKR on 2015/1/27.
 */
public class BaseActivityImpl extends AbstractBaseActivity implements Callback{
    private static final String TAG = "BaseActivityImpl";
    private LoginAndAuthHelper mLoginAndAuthHelper;
    Thread mDataBootstrapThread = null;

    protected static final int NAVDRAWER_ITEM_SHOW_CONTACTS = 0;
    protected static final int NAVDRAWER_ITEM_SHOW_LEADER_JOBS = 1;
    protected static final int NAVDRAWER_ITEM_SHOW_LEADER_ON_JOB_STATES = 2;
    protected static final int NAVDRAWER_ITEM_SHOW_DUTY = 3;
    protected static final int NAVDRAWER_ITEM_ABOUT = 4;

    private static final int[] NAVDRAWER_TITLE_RES_ID = new int[]{
            R.string.navigation_drawer_item_show_contacts,
            R.string.navigation_drawer_item_show_leader_jobs,
            R.string.navigation_drawer_item_show_leader_on_job_state,
            R.string.navigation_drawer_item_show_duty,
            R.string.navigation_drawer_item_about
    };

    private static final int[] NAVDRAWER_ICON_RES_ID = new int[]{
            R.drawable.ic_drawer_contacts,
            R.drawable.ic_drawer_jobs,
            R.drawable.ic_drawer_leader_status,
            R.drawable.ic_drawer_duty,
            R.drawable.ic_drawer_about
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!PrefUtils.isTosAppIsRunning()){
            Intent intent = new Intent(this, SplashScreenActivity.class);
            startActivity(intent);
            finish();
            PrefUtils.markTosAppIsRunning(true);
//            performDataBootstrap();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mLoginAndAuthHelper != null) {
//            mLoginAndAuthHelper.stop();
        }
    }

    /**
     *  1.如果从来没有登录过，进入LoginActivity发起登录
     *  2.如果登录过，但是缓存中没有当前登录帐号，通过AccountManager从系统中获取第一个帐号的name、password属性，然后发起登陆验证
     */
    @Override
    protected void startLoginProcess() {
        //判断当前是否有活动账号，如果没有，通过AccountManager从系统中获取第一个帐号的name属性，并缓存在sp文件
        if (!AccountUtils.hasActiveAccount(getApplicationContext())){
            String defaultAccount = getDefaultAccount();
            //如果系统中没有帐号，或者帐号的name属性为空，则需要进入LoginActivity发起登录
            if (defaultAccount == null){
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
            }
            AccountUtils.setActiveAccount(getApplicationContext(), defaultAccount);
        }
        if (!AccountUtils.hasActiveAccount(getApplicationContext())) {
            Log.i(TAG, "Can't proceed with login -- no account chosen.");
            return;
        } else {
            Log.i(TAG, "Chosen account: " + AccountUtils.getActiveAccountName(getApplicationContext()));
        }

        String accountName = AccountUtils.getActiveAccountName(getApplicationContext());
        Log.i(TAG, "Chosen account: " + AccountUtils.getActiveAccountName(getApplicationContext()));

        //如果 LoginAndAuthHelper不为空，并且 LoginAndAuthHelper.getAccountName.equels(accountName),说明Helper已经设置好，只需要直接启动
        if (mLoginAndAuthHelper != null && mLoginAndAuthHelper.getAccountName().equals(accountName)) {
            Log.i(TAG, "Helper already set up; simply starting it.");
            mLoginAndAuthHelper.start();
            return;
        }

        Log.i(TAG, "Creating and starting new Helper with account: " + accountName);
        mLoginAndAuthHelper = new LoginAndAuthHelper(this,this,accountName);
        mLoginAndAuthHelper.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mLoginAndAuthHelper == null || !mLoginAndAuthHelper.onActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onAuthSuccess(String accountName, boolean newlyAuthenticate) {
        Account account = AccountUtils.getActiveAccount(this,Config.ACCOUNT_TYPE);
        if (newlyAuthenticate) {
            Log.i(TAG, "Enabling auto sync on content provider for account " + accountName);
            //开始预加载数据
            if (!PrefUtils.isDataBootstrapDone(this) && mDataBootstrapThread == null) {
                Log.i(TAG, "One-time data bootstrap not done yet. Doing now.");
                performDataBootstrap();
            }

            //开始同步数据
        }
    }

    /**
     * 初始化数据
     */
    private void performDataBootstrap() {
        final Context appContext = getApplicationContext();
        Log.i(TAG, "Starting data bootstrap background thread.");
        mDataBootstrapThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Starting data bootstrap process.");
                try {
                    // 从raw资源中预加载数据
                    String bootstrapJson = JSONHandler.parseResource(appContext, R.raw.bootstrap_data);
                    AppDataHandler dataHandler = new AppDataHandler(appContext);
                    dataHandler.applyConferenceData(new String[]{bootstrapJson},Long.toString(DateTimeUtils.stringToDateTime("2015-02-09 00:00:00")) ,false);
//                    SyncHelper.performPostSyncChores(appContext);
                    Log.i(TAG, "End of bootstrap -- successful. Marking boostrap as done.");
                    PrefUtils.markSyncSucceededNow(appContext);
                    PrefUtils.markDataBootstrapDone(appContext);
                    getContentResolver().notifyChange(Uri.parse(Contract.CONTENT_AUTHORITY), null, false);
                } catch (IOException ex) {
                    Log.i(TAG, "*** ERROR DURING BOOTSTRAP! Problem in bootstrap data?");
                    Log.i(TAG, "Applying fallback -- marking boostrap as done; sync might fix problem.");
                    PrefUtils.markDataBootstrapDone(appContext);
                }
                mDataBootstrapThread = null;
                SyncHelper.requestManualSync(AccountUtils.getActiveAccount(appContext,Config.ACCOUNT_TYPE));
            }
        });
        mDataBootstrapThread.start();
    }

    @Override
    public void onAuthFailure(String accountName, int errorCode) {
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        AccountManager am = AccountManager.get(this);
        switch(errorCode){
            case Config.RESULT_STATUS_ERROR_SEVER_STATUS:
                break;
            case Config.RESULT_STATUS_ERROR_SID:
                Toast.makeText(getApplicationContext(),
                        R.string.error_sid, Toast.LENGTH_SHORT)
                        .show();
                am.removeAccount(AccountUtils.getActiveAccount(getApplicationContext(),Config.ACCOUNT_TYPE),null,null);
                startActivity(intent);
                finish();
                break;
            case Config.RESULT_STATUS_ERROR_USERNAME_PWD:
                Toast.makeText(getApplicationContext(),
                        R.string.error_incorrect_password, Toast.LENGTH_SHORT)
                        .show();
                am.clearPassword(AccountUtils.getActiveAccount(getApplicationContext(),Config.ACCOUNT_TYPE));
                startActivity(intent);
                finish();
                break;
            case Config.RESULT_STATUS_ERROR_TOKEN:
                Toast.makeText(getApplicationContext(),
                        R.string.error_token, Toast.LENGTH_SHORT)
                        .show();
                am.removeAccount(AccountUtils.getActiveAccount(getApplicationContext(),Config.ACCOUNT_TYPE),null,null);
                startActivity(intent);
                finish();
                break;
            case Config.RESULT_STATUS_ERROR_PARAM:
                Toast.makeText(getApplicationContext(),
                        R.string.error_param, Toast.LENGTH_SHORT)
                        .show();
                am.removeAccount(AccountUtils.getActiveAccount(getApplicationContext(),Config.ACCOUNT_TYPE),null,null);
                startActivity(intent);
                finish();
                break;
            case Config.RESULT_STATUS_ERROR_PUSH_REGISTER:
                break;
            case Config.RESULT_STATUS_ERROR_CONNECTION:
                break;
        }
    }

    /**
     * BaseActivity 调用此方法获得当前项目的ACCOUNT_TYPE
     */
    @Override
    protected String getAccountType() {
        return Config.ACCOUNT_TYPE;
    }

    /**
     * 填充当前活动帐号信息
     */
    @Override
    protected void populateActiveAccount(Account account) {
        LinearLayout activeAccountContents = (LinearLayout) findViewById(R.id.active_account_contents);
        View activeAccountContent = LayoutInflater.from(getApplicationContext()).inflate(R.layout.content_active_account_detail,activeAccountContents);
        TextView nameTextView = (TextView) activeAccountContent.findViewById(R.id.profile_name_text);
        TextView zzjgTextView = (TextView) activeAccountContent.findViewById(R.id.profile_zzjg_text);
        AccountManager am = AccountManager.get(this);
        String userXM = am.getUserData(account,AccountUtils.KEY_USER_XM);
        String zzjg = am.getUserData(account,AccountUtils.KEY_ZZJG_MC);
        if (userXM == null){
            nameTextView.setVisibility(View.GONE);
        }else{
            nameTextView.setText(userXM);
        }
        if (zzjg == null){
            zzjgTextView.setVisibility(View.GONE);
        }else{
            zzjgTextView.setText(zzjg);
        }
    }

    @Override
    protected void populateNavDrawer() {
        mNavDrawerItems.add(NAVDRAWER_ITEM_SHOW_CONTACTS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SHOW_LEADER_JOBS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SHOW_LEADER_ON_JOB_STATES);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SHOW_DUTY);
        mNavDrawerItems.add(NAVDRAWER_ITEM_ABOUT);
        createNavDrawerItems();
    }

    //主菜单点击事件
    @Override
    protected void goToNavDrawerItem(int item) {
        Intent intent;
        switch(item){
            case NAVDRAWER_ITEM_SHOW_CONTACTS:
                intent = new Intent(getApplicationContext(),BrowseContactsActivity.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_SHOW_LEADER_JOBS:
                intent = new Intent(getApplicationContext(),BrowseJobsActivity.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_SHOW_LEADER_ON_JOB_STATES:
                intent = new Intent(getApplicationContext(),TestActivity2.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_SHOW_DUTY:
                intent = new Intent(getApplicationContext(),TestActivity3.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_ABOUT:
                intent = new Intent(getApplicationContext(),TestActivity4.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * BaseActivity调用此方法获得icon id
     */
    @Override
    protected int getDrawerItemIconID(int itemID) {
        return itemID >= 0 && itemID < NAVDRAWER_ICON_RES_ID.length ? NAVDRAWER_ICON_RES_ID[itemID] : 0;
    }
    /**
     * BaseActivity调用此方法获得Title
     */
    @Override
    protected int getDrawerItemTitleID(int itemID) {
        return itemID >= 0 && itemID < NAVDRAWER_TITLE_RES_ID.length ?NAVDRAWER_TITLE_RES_ID[itemID] : 0;
    }

    @Override
    protected boolean isSpecialItem(int itemId) {
        return itemId == NAVDRAWER_ITEM_ABOUT;
    }

}
