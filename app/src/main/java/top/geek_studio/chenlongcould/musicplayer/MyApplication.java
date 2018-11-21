/*
 * ************************************************************
 * 文件：MyApplication.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月21日 11:01:53
 * 上次修改时间：2018年11月21日 09:52:32
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

import android.app.Application;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Locale;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Configuration config = getResources().getConfiguration();
        config.locale = Locale.SIMPLIFIED_CHINESE;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        getResources().updateConfiguration(config, dm);
        Log.d(TAG, "onCreate: ");
    }

}
