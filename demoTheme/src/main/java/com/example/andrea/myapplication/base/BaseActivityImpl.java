package com.example.andrea.myapplication.base;

import android.accounts.Account;
import android.content.Intent;

import com.example.andrea.myapplication.MainActivity1;
import com.example.andrea.myapplication.MainActivity2;
import com.example.andrea.myapplication.MainActivity3;
import com.example.andrea.myapplication.MainActivity4;
import com.example.andrea.myapplication.MainActivity5;
import com.example.andrea.myapplication.R;

public class BaseActivityImpl extends AbstractBaseActivity {

    protected static final int NAVDRAWER_ITEM_1 = 0;
    protected static final int NAVDRAWER_ITEM_2 = 1;
    protected static final int NAVDRAWER_ITEM_3 = 2;
    protected static final int NAVDRAWER_ITEM_4 = 3;
    protected static final int NAVDRAWER_ITEM_5 = 4;

    private static final int[] NAVDRAWER_TITLE_RES_ID = new int[]{
            R.string.title_activity_main1,
            R.string.title_activity_main2,
            R.string.title_activity_main3,
            R.string.title_activity_main4,
            R.string.title_activity_main5
    };

    private static final int[] NAVDRAWER_ICON_RES_ID = new int[]{
            R.mipmap.ic_drawer_item1,
            R.mipmap.ic_drawer_item2,
            R.mipmap.ic_drawer_item3,
            R.mipmap.ic_drawer_item4,
            R.mipmap.ic_drawer_item5
    };

    @Override
    protected void requestDataRefresh() {

    }

    @Override
    protected String getAccountType() {
        return null;
    }

    @Override
    protected void goToNavDrawerItem(int item) {
        Intent intent;
        switch(item){
            case NAVDRAWER_ITEM_1:
                intent = new Intent(getApplicationContext(),MainActivity1.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_2:
                intent = new Intent(getApplicationContext(),MainActivity2.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_3:
                intent = new Intent(getApplicationContext(),MainActivity3.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_4:
                intent = new Intent(getApplicationContext(),MainActivity4.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_5:
                intent = new Intent(getApplicationContext(),MainActivity5.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void watchSyncStateChange() {

    }

    @Override
    protected void startLoginProcess() {

    }

    @Override
    protected boolean isSpecialItem(int itemId) {
        return itemId == NAVDRAWER_ITEM_5;
    }

    @Override
    protected void populateActiveAccount(Account account) {

    }

    @Override
    protected void populateNavDrawer() {
        mNavDrawerItems.add(NAVDRAWER_ITEM_1);
        mNavDrawerItems.add(NAVDRAWER_ITEM_2);
        mNavDrawerItems.add(NAVDRAWER_ITEM_3);
        mNavDrawerItems.add(NAVDRAWER_ITEM_4);
        mNavDrawerItems.add(NAVDRAWER_ITEM_5);
        createNavDrawerItems();
    }

    @Override
    protected int getDrawerItemIconID(int itemID) {
        return itemID >= 0 && itemID < NAVDRAWER_ICON_RES_ID.length ? NAVDRAWER_ICON_RES_ID[itemID] : 0;
    }

    @Override
    protected int getDrawerItemTitleID(int itemID) {
        return itemID >= 0 && itemID < NAVDRAWER_TITLE_RES_ID.length ?NAVDRAWER_TITLE_RES_ID[itemID] : 0;
    }
}
