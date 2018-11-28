/*
 * ************************************************************
 * 文件：MyApplication.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月28日 16:12:44
 * 上次修改时间：2018年11月28日 16:12:22
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.util.Locale;

import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    private SharedPreferences mDefSharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        //set language
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        config.locale = Locale.getDefault();
        resources.updateConfiguration(config, dm);

        mDefSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //first or not
        Values.FIRST_USE = mDefSharedPreferences.getBoolean(Values.SharedPrefsTag.FIRST_USE, true);

        //bg style
        Values.Style.DETAIL_BACKGROUND = mDefSharedPreferences.getString(Values.SharedPrefsTag.DETAIL_BG_STYLE, Values.Style.STYLE_BACKGROUND_BLUR);

        //update style
        Utils.Ui.upDateStyle(mDefSharedPreferences);

        //set play type
        Values.CurrentData.CURRENT_PLAY_TYPE = mDefSharedPreferences.getString(Values.SharedPrefsTag.PLAY_TYPE, Values.TYPE_COMMON);

        new Thread(() -> {
            File file = new File(getFilesDir().getPath() + File.separatorChar + "AppData");
            Log.d(TAG, "run: " + file.getPath());
        }).start();
    }

    @Override
    public void onTrimMemory(int level) {
        Log.d(TAG, "onTrimMemory: do");
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            GlideApp.get(this).clearMemory();
        }
        GlideApp.get(this).trimMemory(level);
    }

}
