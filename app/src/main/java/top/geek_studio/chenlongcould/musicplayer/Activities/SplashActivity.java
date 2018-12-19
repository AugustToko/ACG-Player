/*
 * ************************************************************
 * 文件：SplashActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月19日 12:56:02
 * 上次修改时间：2018年12月19日 12:49:27
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class SplashActivity extends MyBaseActivity {

    private static final String TAG = "SplashActivity";

    public static boolean phoneHasNav(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hasNavigationBar;

    }

    private void initPermission() {
        if (ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            initDone();
        } else {
            ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Values.REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Values.REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: grant");

                    //检测是否得到过权限
                    if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Values.SURE_GET_PERMISSION, false)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                        editor.putBoolean(Values.SURE_GET_PERMISSION, true);
                        editor.apply();

                        Utils.Ui.fastToast(this, "Succeed to get permission!");
                    }

                    initDone();
                } else {
                    Utils.Ui.fastToast(this, "Failed to get permission, again!");
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Failed to get permission");
                    builder.setMessage("Try again?");
                    builder.setCancelable(false);
                    builder.setNegativeButton("Sure!", (dialog, which) -> initPermission());
                    builder.setNeutralButton("Cancel!", (dialog, which) -> {
                        dialog.dismiss();
                        finish();
                    });
                    builder.show();
                }
            }
        }
    }

    private void initDone() {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 500);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        try {
            @SuppressLint("PrivateApi") Class decorViewClazz = Class.forName("com.android.internal.policy.DecorView");
            Field field = decorViewClazz.getDeclaredField("mSemiTransparentStatusBarColor");
            field.setAccessible(true);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            field.setInt(getWindow().getDecorView(), Color.TRANSPARENT);  //改为透明
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        Values.PHONE_HAS_NAV = phoneHasNav(this);


        initPermission();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: done");
        super.onDestroy();
    }

    protected void finalize() throws Throwable {
        Log.d(TAG, "finalize: done!!!!");
        super.finalize();
    }
}
