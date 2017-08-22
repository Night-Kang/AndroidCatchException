package com.night.catchexception.androidcatchexception.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.night.catchexception.androidcatchexception.MainApplication;
import com.night.catchexception.androidcatchexception.common.Constant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by night on 22/08/2017.
 * Description:
 */

public class CrashException implements Thread.UncaughtExceptionHandler {

    //系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mCrash;
    //程序的Context对象
    private Context mContext;
    //用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<>();
    //用于格式化日期，作为日志文件名的一部分
    private DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private CrashException() {
        mContext = MainApplication.getContext();
    }

    private static class CrashExceptionHolder{
         static final CrashException instance = new CrashException();
    }

    public static CrashException getInstance() {
        return CrashExceptionHolder.instance;
    }

    public void init() {
        //获取系统默认的UncaughtException处理器
        mCrash = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }
    
    private void collectDeviceInfo(Context context) {
        try {
            PackageManager pm =context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            if (packageInfo != null) {
                String versionName =
                        packageInfo.versionName == null ? "null":packageInfo.versionName;
                String versionCode = packageInfo.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean handleException(Throwable throwable) {
        if (throwable == null) {
            return true;
        }

        new Thread(){
            @Override
            public void run() {
                Looper.prepare();
                Log.e(Constant.TAG, "----run----");
                Toast.makeText(mContext, "Something error,will exit", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }.start();
        //手机设备参数信息
        collectDeviceInfo(mContext);
        //保存日志文件
        saveCrashInfoToFile(throwable);
        return true;
    }

    private void saveCrashInfoToFile(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append("=").append(value).append("\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        Throwable cause = throwable.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);

        long timestamp = System.currentTimeMillis();
        String time = format.format(new Date());
        String fileName = "crash-" + time + "-" + timestamp + ".log";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

//            String path = "/sdcard/crash/";
            String path = Environment.getExternalStorageDirectory().getPath() + "/crash/";
            Log.e(Constant.TAG, "---> Path:" + path);
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdir();
            }
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {

        if (!handleException(throwable) && mCrash != null) {
            mCrash.uncaughtException(thread, throwable);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //exit app
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }

    }
}
