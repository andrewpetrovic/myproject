package com.example.andrea.demologin;

import android.os.Bundle;

import com.example.andrea.demologin.base.BaseActivityImpl;

public class MainActivity extends BaseActivityImpl{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        overridePendingTransition(0, 0);
    }

}
