/*
 * ************************************************************
 * 文件：MyApplication.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月28日 07:49:20
 * 上次修改时间：2018年12月28日 07:49:02
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import top.geek_studio.chenlongcould.musicplayer.Fragments.AlbumListFragment;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    public static SharedPreferences mDefSharedPreferences;

    public static final String SHORT_CUT_ID_1 = "id1";
    public static final String SHORT_CUT_ID_2 = "id2";
    public static final String SHORT_CUT_ID_3 = "id3";
    private ShortcutManager mShortcutManager;

    @Override
    public void onCreate() {
        super.onCreate();

        if (getProcessName(this).equals(getPackageName())) {

            //监听耳机(有线或无线)的插拔动作, 拔出暂停音乐
            IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
            intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
            registerReceiver(Data.mMyHeadSetPlugReceiver, intentFilter);

            Intent intent = new Intent(this, MyMusicService.class);
            startService(intent);
            bindService(intent, Data.sServiceConnection, BIND_AUTO_CREATE);

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

            Utils.Ui.inDayNightSet(mDefSharedPreferences);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                mShortcutManager = getSystemService(ShortcutManager.class);
                getNewShortcutInfo();
            }

        } else {
            //action in MusicService Process
        }

    }

    /**
     * 动态添加三个
     */
    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    private void getNewShortcutInfo() {
        ShortcutInfo shortcut = new ShortcutInfo.Builder(this, SHORT_CUT_ID_1)
                .setShortLabel("baidu")
                .setLongLabel("第一个")
                .setIcon(Icon.createWithResource(this, R.drawable.ic_play_arrow_black_24dp))
                .setIntent(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.baidu.com/")))
                .build();
        ShortcutInfo shortcut2 = new ShortcutInfo.Builder(this, SHORT_CUT_ID_2)
                .setShortLabel("csdn")
                .setLongLabel("第二个")
                .setIcon(Icon.createWithResource(this, R.drawable.ic_play_arrow_black_24dp))
                .setIntent(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.csdn.com/")))
                .build();
        ShortcutInfo shortcut3 = new ShortcutInfo.Builder(this, SHORT_CUT_ID_3)
                .setShortLabel("github")
                .setLongLabel("第三个")
                .setIcon(Icon.createWithResource(this, R.drawable.ic_play_arrow_black_24dp))
                .setIntent(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.github.com/")))
                .build();
        mShortcutManager.setDynamicShortcuts(Arrays.asList(shortcut, shortcut2, shortcut3));
    }

    /**
     * 获取进程名。
     * 由于app是一个多进程应用，因此每个进程被os创建时，
     * onCreate()方法均会被执行一次，
     * 进行辨别初始化，针对特定进程进行相应初始化工作，
     * 此方法可以提高一半启动时间。
     *
     * @param context 上下文环境对象
     * @return 获取此进程的进程名
     */
    private String getProcessName(@NonNull Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        if (runningAppProcesses == null) {
            return "";
        }

        for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
            if (runningAppProcess.pid == android.os.Process.myPid() && !TextUtils.isEmpty(runningAppProcess.processName)) {
                return runningAppProcess.processName;
            }
        }
        return "";
    }

    @Override
    public void onTrimMemory(int level) {

        if (getProcessName(this).equals(getPackageName())) {
            if (level == TRIM_MEMORY_MODERATE) {
                if (AlbumListFragment.VIEW_HAS_LOAD) {
                    Data.sAlbumItems.clear();
                    Log.d(TAG, "onTrimMemory: AlbumFragment recycled");
                }
            }
        }

        GlideApp.get(this).trimMemory(level);

        super.onTrimMemory(level);
    }

}
