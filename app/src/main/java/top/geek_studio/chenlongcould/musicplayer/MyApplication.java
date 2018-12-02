/*
 * ************************************************************
 * 文件：MyApplication.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月02日 20:56:24
 * 上次修改时间：2018年12月02日 10:19:32
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

import top.geek_studio.chenlongcould.musicplayer.Fragments.AlbumListFragment;
import top.geek_studio.chenlongcould.musicplayer.Fragments.PlayListFragment;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    public static SharedPreferences mDefSharedPreferences;

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

        Utils.Ui.inDayNightSet(mDefSharedPreferences);
    }

    @Override
    public void onTrimMemory(int level) {
        Log.d(TAG, "onTrimMemory: do");

        if (level == TRIM_MEMORY_MODERATE) {
            if (AlbumListFragment.HAS_LOAD) {
                Data.sAlbumItems.clear();
                Log.d(TAG, "onTrimMemory: AlbumFragment recycled");
            }

            //noinspection StatementWithEmptyBody
            if (PlayListFragment.HAS_LOAD) {
                //No data can be recycled
            }
            Data.sCurrentMusicBitmap = null;
        }
        GlideApp.get(this).trimMemory(level);

        super.onTrimMemory(level);
    }

}
