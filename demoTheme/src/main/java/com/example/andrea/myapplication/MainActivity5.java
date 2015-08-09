package com.example.andrea.myapplication;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.andrea.myapplication.base.BaseActivityImpl;

public class MainActivity5 extends BaseActivityImpl {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        final Toolbar toolbar = getActionBarToolbar();
        toolbar.setNavigationIcon(R.drawable.ic_up);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //一级界面必须覆盖这个方法，返回值为BaseActivityImpl中定义的与该acivity对应的NAVDRAWER_ITEM值
    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_5;
    }
}
