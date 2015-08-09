package com.example.andrea.demologin.base;


import android.accounts.Account;
import android.content.Intent;
import android.util.Log;

import com.example.andrea.demologin.Config;
import com.example.andrea.demologin.LoginActivity;
import com.itic.mobile.accounts.AccountUtils;

public class BaseActivityImpl extends AbstractBaseActivity{

    private static final String TAG = "BaseActivityImpl";

    @Override
    protected void requestDataRefresh() {

    }

    @Override
    protected String getAccountType() {
        return Config.ACCOUNT_TYPE;
    }

    @Override
    protected void goToNavDrawerItem(int item) {

    }

    @Override
    protected void watchSyncStateChange() {

    }

    @Override
    protected void startLoginProcess() {
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
    }

    @Override
    protected boolean isSpecialItem(int itemId) {
        return false;
    }

    @Override
    protected void populateActiveAccount(Account account) {

    }

    @Override
    protected void populateNavDrawer() {

    }

    @Override
    protected int getDrawerItemIconID(int itemID) {
        return 0;
    }

    @Override
    protected int getDrawerItemTitleID(int itemID) {
        return 0;
    }
}
