package com.night.catchexception.androidcatchexception;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.night.catchexception.androidcatchexception.common.Constant;

public class MainActivity extends AppCompatActivity {

    private String s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e(Constant.TAG, "----good----");
        Log.e(Constant.TAG, "---s:" + s.equals("good"));
    }
}
