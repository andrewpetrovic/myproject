package com.itic.mobile.zfyj.qh.splashscreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.WindowManager;

import com.itic.mobile.zfyj.qh.R;
import com.itic.mobile.zfyj.qh.contacts.ui.BrowseContactsActivity;

/**
 * Created by JEEKR on 2015/1/27.
 */
public class SplashScreenActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        findViewById(R.id.logoImgView).setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        getSupportActionBar().hide();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        finishSplashCcreen();
    }

    private void finishSplashCcreen() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new Handler(getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
//                        Intent intent = new Intent(getApplicationContext(), BrowseJobsActivity.class);
                        Intent intent = new Intent(getApplicationContext(), BrowseContactsActivity.class);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                        finish();
                    }
                }.sendEmptyMessageDelayed(1, 3000);
            }
        }).start();
    }
}
