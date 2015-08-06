package com.itic.mobile.zfyj.qh.test;

import android.os.Bundle;

import com.itic.mobile.zfyj.qh.R;
import com.itic.mobile.zfyj.qh.base.BaseActivityImpl;


/**
 */
public class TestActivity2 extends BaseActivityImpl {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_test);
    }

    //一级界面必须覆盖这个方法，返回值为BaseActivityImpl中定义的与该acivity对应的NAVDRAWER_ITEM值
    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_SHOW_DUTY;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getSupportFragmentManager().findFragmentById(R.id.test_fragment);
    }


}