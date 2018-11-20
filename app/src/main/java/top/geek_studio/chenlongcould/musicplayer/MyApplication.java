/*
 * ************************************************************
 * 文件：MyApplication.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月20日 21:06:43
 * 上次修改时间：2018年11月20日 21:01:02
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

import android.app.Application;
import android.util.Log;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
    }

}
