/*
 * ************************************************************
 * 文件：MyApplication.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月27日 13:11:38
 * 上次修改时间：2019年01月27日 13:08:44
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
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import org.litepal.LitePal;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.fragment.AlbumListFragment;
import top.geek_studio.chenlongcould.musicplayer.fragment.ArtistListFragment;
import top.geek_studio.chenlongcould.musicplayer.utils.MusicUtil;
import top.geek_studio.chenlongcould.musicplayer.utils.PlayListsUtil;

public final class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    public static final String SHORT_CUT_ID_1 = "id1";
    public static final String SHORT_CUT_ID_2 = "id2";
    public static final String SHORT_CUT_ID_3 = "id3";

    private ShortcutManager mShortcutManager;

    public static final String LAST_FM_KEY = "726e129841377374f2c8c804facb6d11";

    public static final String MY_WEB_SITE = "https://blog.geek-studio.top/";

    public static final String VERSION_CODE = "ver_code";

    public static final String AD_ID = "ca-app-pub-1302949087387063/3066110293";

    public static final String AD_ID_TEST = "ca-app-pub-3940256099942544/5224354917";

    public static final String APP_ID = "ca-app-pub-1302949087387063~1079129255";

    public static final int VER_CODE = 46;

    @Override
    public void onCreate() {
        super.onCreate();

        LitePal.initialize(this);

        if (getProcessName(this).equals(getPackageName())) {

//            // TODO: 2019/1/6 ver
//            //noinspection StatementWithEmptyBody
//            if (mDefSharedPreferences.getLong(VERSION_CODE, -1) != VER_CODE) {
//                Toast.makeText(this, "建议手动清除该应用程序数据...", Toast.LENGTH_SHORT).show();
//            }
//
//            //add version code
//            final SharedPreferences.Editor ver_edit = mDefSharedPreferences.edit();
//            ver_edit.putLong(VERSION_CODE, VER_CODE);
//            ver_edit.apply();
//
//            int id = mDefSharedPreferences.getInt(Values.SharedPrefsTag.FAVOURITE_LIST_ID, -1);
//            if (id == -1) {
//                id = PlayListsUtil.createPlaylist(this, "Favourite List");
//                SharedPreferences.Editor editor = mDefSharedPreferences.edit();
//                editor.putInt(Values.SharedPrefsTag.FAVOURITE_LIST_ID, id);
//                editor.apply();
//            }

//            //set language
//            Resources resources = getResources();
//            DisplayMetrics dm = resources.getDisplayMetrics();
//            Configuration config = resources.getConfiguration();
//            config.locale = Locale.getDefault();
//            resources.updateConfiguration(config, dm);

            Values.Style.DETAIL_BACKGROUND = PreferenceManager.getDefaultSharedPreferences(this).getString(Values.SharedPrefsTag.DETAIL_BG_STYLE, Values.Style.STYLE_BACKGROUND_BLUR);

            //init favourite
            if (MusicUtil.getFavoritesPlaylist(this) == null)
                PlayListsUtil.createPlaylist(this, getString(R.string.favorites));

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
     * 动态添加
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
                Data.sTrashCanList.clear();
                Data.sSelections.clear();

                AlbumListFragment.VIEW_HAS_LOAD = false;
                Data.sAlbumItems.clear();

                ArtistListFragment.VIEW_HAS_LOAD = false;
                Data.sArtistItems.clear();
            }
        }

        GlideApp.get(this).trimMemory(level);

        super.onTrimMemory(level);
    }
}
