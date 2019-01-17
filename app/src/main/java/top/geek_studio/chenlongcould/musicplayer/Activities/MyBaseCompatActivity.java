/*
 * ************************************************************
 * 文件：MyBaseCompatActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:46
 * 上次修改时间：2019年01月17日 17:28:59
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

@SuppressLint("Registered")
public class MyBaseCompatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Values.SharedPrefsTag.TRANSPORT_STATUS, false)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            }
        }

        //noinspection deprecation
        setTaskDescription(new ActivityManager.TaskDescription((String) getTitle(), null, Utils.Ui.getPrimaryColor(this)));

    }

    protected void setUpTaskCardColor(@ColorInt int color) {
        //noinspection deprecation
        setTaskDescription(new ActivityManager.TaskDescription((String) getTitle(), null, color));
    }

    @Override
    protected void onResume() {
        //noinspection deprecation
        setTaskDescription(new ActivityManager.TaskDescription((String) getTitle(), null, Utils.Ui.getPrimaryColor(this)));
        super.onResume();
    }
}
