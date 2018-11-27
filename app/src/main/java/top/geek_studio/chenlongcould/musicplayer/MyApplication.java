/*
 * ************************************************************
 * 文件：MyApplication.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月27日 11:16:33
 * 上次修改时间：2018年11月27日 11:16:22
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    private SharedPreferences mDefSharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");

        mDefSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Values.Style.DETAIL_BACKGROUND = mDefSharedPreferences.getString(Values.SharedPrefsTag.DETAIL_BG_STYLE, Values.Style.STYLE_BACKGROUND_BLUR);

        Utils.Ui.upDateStyle(mDefSharedPreferences);

        Values.CurrentData.CURRENT_PLAY_TYPE = mDefSharedPreferences.getString(Values.SharedPrefsTag.PLAY_TYPE, Values.TYPE_COMMON);
    }

}
