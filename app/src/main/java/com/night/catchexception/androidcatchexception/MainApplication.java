package com.night.catchexception.androidcatchexception;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.night.catchexception.androidcatchexception.common.Constant;
import com.night.catchexception.androidcatchexception.util.CrashException;

/**
 * Created by night on 22/08/2017.
 * Description: MainApplication
 */

public class MainApplication extends Application {


    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Log.e(Constant.TAG, "---create Application---");
        CrashException crashException = CrashException.getInstance();
        crashException.init();
    }
}
