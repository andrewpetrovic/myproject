package com.example.andrea.myapplication;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.andrea.myapplication.base.BaseActivityImpl;

public class MainActivity1 extends BaseActivityImpl {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        overridePendingTransition(0, 0);
    }

    //一级界面必须覆盖这个方法，返回值为BaseActivityImpl中定义的与该acivity对应的NAVDRAWER_ITEM值
    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_1;
    }
}
