/*
 * ************************************************************
 * 文件：SplashActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:46
 * 上次修改时间：2019年01月17日 17:28:59
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Android 5.0 以上 全透明
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // 状态栏（以上几行代码必须，参考setStatusBarColor|setNavigationBarColor方法源码）
            window.setStatusBarColor(Color.TRANSPARENT);
            // 虚拟导航键
            window.setNavigationBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_splash);

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
