/*
 * ************************************************************
 * 文件：MyApplication.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月18日 18:58:29
 * 上次修改时间：2019年01月18日 18:57:37
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Fragments.AlbumListFragment;
import top.geek_studio.chenlongcould.musicplayer.Utils.PlayListsUtil;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;

public final class MyApplication extends Application {

    private FirebaseAnalytics mFirebaseAnalytics;

    public static WeakReference<Context> mAppContext;

    private static final String TAG = "MyApplication";

    public static SharedPreferences mDefSharedPreferences;

    public static final String SHORT_CUT_ID_1 = "id1";
    public static final String SHORT_CUT_ID_2 = "id2";
    public static final String SHORT_CUT_ID_3 = "id3";

    private ShortcutManager mShortcutManager;

    public static final String MY_WEB_SITE = "https://blog.geek-studio.top/";

    public static final String VERSION_CODE = "ver_code";

    public static final String AD_ID = "ca-app-pub-1302949087387063/3066110293";

    public static final String AD_ID_TEST = "ca-app-pub-3940256099942544/5224354917";

    public static final String APP_ID = "ca-app-pub-1302949087387063~1079129255";

    public static final int VER_CODE = 46;

    public static PackageInfo sPackageInfo;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();

        mAppContext = new WeakReference<>(this);

        try {
            sPackageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (getProcessName(this).equals(getPackageName())) {

            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            mDefSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

            // TODO: 2019/1/6 ver
            //noinspection StatementWithEmptyBody
            if (mDefSharedPreferences.getLong(VERSION_CODE, -1) != VER_CODE) {
                Toast.makeText(this, "建议手动清除该应用程序数据...", Toast.LENGTH_SHORT).show();
            }

            //add version code
            final SharedPreferences.Editor ver_edit = mDefSharedPreferences.edit();
            ver_edit.putLong(VERSION_CODE, VER_CODE);
            ver_edit.apply();

            int id = mDefSharedPreferences.getInt(Values.SharedPrefsTag.FAVOURITE_LIST_ID, -1);
            if (id == -1) {
                id = PlayListsUtil.createPlaylist(this, "Favourite List");
                SharedPreferences.Editor editor = mDefSharedPreferences.edit();
                editor.putInt(Values.SharedPrefsTag.FAVOURITE_LIST_ID, id);
                editor.apply();
            }

            //监听耳机(有线或无线)的插拔动作, 拔出暂停音乐
            IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
            intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
            registerReceiver(Data.mMyHeadSetPlugReceiver, intentFilter);

            //set language
            Resources resources = getResources();
            DisplayMetrics dm = resources.getDisplayMetrics();
            Configuration config = resources.getConfiguration();
            config.locale = Locale.getDefault();
            resources.updateConfiguration(config, dm);

            //first or not
            Values.FIRST_USE = mDefSharedPreferences.getBoolean(Values.SharedPrefsTag.FIRST_USE, true);

            //bg style
            Values.Style.DETAIL_BACKGROUND = mDefSharedPreferences.getString(Values.SharedPrefsTag.DETAIL_BG_STYLE, Values.Style.STYLE_BACKGROUND_BLUR);

            //update style
            Utils.Ui.upDateStyle(mDefSharedPreferences);

            //set play type
            Values.CurrentData.CURRENT_PLAY_TYPE = mDefSharedPreferences.getString(Values.SharedPrefsTag.PLAY_TYPE, Values.TYPE_COMMON);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                mShortcutManager = getSystemService(ShortcutManager.class);
                getNewShortcutInfo();
            }

        } else {
            //action in MusicService Process
        }

    }

    public static final String SHORTCUT_RANDOM = "SHORTCUT_RANDOM";

    /**
     * 动态添加三个
     */
    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    private void getNewShortcutInfo() {

        Intent randomPlay = new Intent(this, MainActivity.class);
        randomPlay.setAction(Intent.ACTION_MAIN);
        randomPlay.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        randomPlay.putExtra("shortcut_type", SHORTCUT_RANDOM);

        ShortcutInfo shortcut = new ShortcutInfo.Builder(this, SHORT_CUT_ID_1)
                .setShortLabel(getString(R.string.random_play))
                .setLongLabel(getString(R.string.random_play))
                .setIcon(Icon.createWithResource(this, R.drawable.ic_shuffle_blue_24dp))
                .setIntent(randomPlay)
                .build();

//        ShortcutInfo shortcut2 = new ShortcutInfo.Builder(this, SHORT_CUT_ID_2)
//                .setShortLabel("csdn")
//                .setLongLabel("第二个")
//                .setIcon(Icon.createWithResource(this, R.drawable.ic_play_arrow_black_24dp))
//                .setIntent(new Intent(Intent.ACTION_VIEW,
//                        Uri.parse("https://www.csdn.com/")))
//                .build();
//
//        ShortcutInfo shortcut3 = new ShortcutInfo.Builder(this, SHORT_CUT_ID_3)
//                .setShortLabel("github")
//                .setLongLabel("第三个")
//                .setIcon(Icon.createWithResource(this, R.drawable.ic_play_arrow_black_24dp))
//                .setIntent(new Intent(Intent.ACTION_VIEW,
//                        Uri.parse("https://www.github.com/")))
//                .build();

        mShortcutManager.setDynamicShortcuts(Collections.singletonList(shortcut));
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
        final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
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
