/*
 * ************************************************************
 * 文件：MyApplication.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月23日 11:17:30
 * 上次修改时间：2018年11月23日 11:04:30
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    private SharedPreferences mDefSharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
//        Configuration config = getResources().getConfiguration();
//        config.locale = Locale.SIMPLIFIED_CHINESE;
//        DisplayMetrics dm = getResources().getDisplayMetrics();
//        getResources().updateConfiguration(config, dm);
        Log.d(TAG, "onCreate: ");

        mDefSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String DETAIL_BG_STYLE = mDefSharedPreferences.getString(Values.SharedPrefsTag.DETAIL_BG_STYLE, Values.Style.STYLE_BACKGROUND_BLUR);
        if (DETAIL_BG_STYLE != null) {
            switch (DETAIL_BG_STYLE) {
                case Values.Style.STYLE_BACKGROUND_BLUR: {
                    Values.Style.DETAIL_BACKGROUND = Values.Style.STYLE_BACKGROUND_BLUR;
                }
                break;

                case Values.Style.STYLE_BACKGROUND_AUTO_COLOR: {
                    Values.Style.DETAIL_BACKGROUND = Values.Style.STYLE_BACKGROUND_AUTO_COLOR;
                }
            }
        } else {
            Values.Style.DETAIL_BACKGROUND = Values.Style.STYLE_BACKGROUND_BLUR;
        }

        if (mDefSharedPreferences.getBoolean(Values.SharedPrefsTag.AUTO_NIGHT_MODE, false)) {
            Values.Style.AUTO_NIGHT_MODE = true;
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
        }

    }

}
