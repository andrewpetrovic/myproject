package com.example.andrea.demologin.base;


import android.accounts.Account;

public class BaseActivityImpl extends AbstractBaseActivity{
    @Override
    protected void requestDataRefresh() {

    }

    @Override
    protected String getAccountType() {
        return null;
    }

    @Override
    protected void goToNavDrawerItem(int item) {

    }

    @Override
    protected void watchSyncStateChange() {

    }

    @Override
    protected void startLoginProcess() {

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
