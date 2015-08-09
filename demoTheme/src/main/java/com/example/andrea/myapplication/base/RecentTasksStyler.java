package com.example.andrea.myapplication.base;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import com.example.andrea.myapplication.R;

/**
 * 用于生成任务管理器内样式的Helper 类，需要在具体业务中实现
 */
public class RecentTasksStyler {
    private static Bitmap sIcon = null;

    private RecentTasksStyler() {
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void styleRecentTasksEntry(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        Resources resources = activity.getResources();
        String label = resources.getString(activity.getApplicationInfo().labelRes);
        int colorPrimary = resources.getColor(com.itic.mobile.R.color.theme_primary);

        if (sIcon == null) {
            // Cache to avoid decoding the same bitmap on every Activity change
            sIcon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher);
        }

        activity.setTaskDescription(new ActivityManager.TaskDescription(label, sIcon, colorPrimary));
    }
}
