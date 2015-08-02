package com.itic.mobile.app;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;

import com.itic.mobile.Config;

/**
 * 
 * @author andrew
 * 
 */
public class BaseApp extends Application {
	protected static final String TAG = "BaseApp";

	private static Context sContext;

	@Override
	public void onCreate() {
		/*执行严格模式*/
		if (Config.IS_DOGFOOD_BUILD
				&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectAll().penaltyDialog().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectAll().penaltyDeath().build());
		}
		super.onCreate();
		sContext = getApplicationContext();
	}

	public static Context getContext() {
		return sContext;
	}
}
