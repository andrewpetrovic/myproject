package com.example.andrea.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.andrea.myapplication.base.BaseActivityImpl;

public class MainActivity2 extends BaseActivityImpl {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        overridePendingTransition(0, 0);
    }

    //一级界面必须覆盖这个方法，返回值为BaseActivityImpl中定义的与该acivity对应的NAVDRAWER_ITEM值
    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_2;
    }
}
