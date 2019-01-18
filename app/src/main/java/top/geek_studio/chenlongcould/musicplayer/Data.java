/*
 * ************************************************************
 * 文件：Data.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月18日 18:58:29
 * 上次修改时间：2019年01月18日 18:57:37
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.disposables.Disposable;
import jp.wasabeef.glide.transformations.BlurTransformation;
import top.geek_studio.chenlongcould.geeklibrary.RecyclerViewTools.RecycleViewDivider;
import top.geek_studio.chenlongcould.geeklibrary.Theme.Theme;
import top.geek_studio.chenlongcould.musicplayer.Activities.CarViewActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.BroadCasts.MyHeadSetPlugReceiver;
import top.geek_studio.chenlongcould.musicplayer.Models.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.Models.PlayListItem;

public final class Data {

    public volatile static boolean HAS_BIND = false;

    public static ArrayList<Disposable> sDisposables = new ArrayList<>();

    public static WeakReference<MainActivity> sMainRef;

    private static RecyclerView.ItemDecoration mItemDecoration;

    public static RecyclerView.ItemDecoration getItemDecoration(Context context) {
        if (mItemDecoration == null) {
            mItemDecoration = new RecycleViewDivider(
                    context, LinearLayoutManager.VERTICAL, 1, ContextCompat.getColor(context, R.color.line_color));
        }
        return mItemDecoration;
    }

    /**
     * old
     */
    public static List<Activity> sActivities = new ArrayList<>();
    /**
     * data
     * */
    public static List<MusicItem> sMusicItems = new ArrayList<>();

    public static CarViewActivity sCarViewActivity = null;
    public static List<AlbumItem> sAlbumItems = new ArrayList<>();
    public static List<AlbumItem> sAlbumItemsBackUp = new ArrayList<>();
    public static List<MusicItem> sMusicItemsBackUp = new ArrayList<>();
    public static List<MusicItem> sPlayOrderList = new ArrayList<>();
    public static List<PlayListItem> sPlayListItems = new ArrayList<>();

    /**
     * init Data {@link MainActivity}
     */
    public static void init(final MainActivity activity) {
        sMainRef = new WeakReference<>(activity);
        sActivities.add(sMainRef.get());
    }

    /**
     * nextWillPlay
     * def null
     */
    public static MusicItem sNextWillPlayItem = null;

    /**
     * 存储播放历史(序列) default...
     */
    public final static List<MusicItem> sHistoryPlay = new ArrayList<>();

    public static Theme sTheme = null;

    /**
     * sCurrent DATA
     */
    public static MusicItem sCurrentMusicItem = new MusicItem.Builder(-1, "null", "null").build();

    public static BlurTransformation sBlurTransformation = new BlurTransformation(20, 30);
    public static BlurTransformation sBlurTransformationCarView = new BlurTransformation(5, 10);
    public static MyHeadSetPlugReceiver mMyHeadSetPlugReceiver = new MyHeadSetPlugReceiver();

    /**
     * save temp bitmap
     */
    private static Bitmap sCurrentCover = null;

    public static Bitmap getCurrentCover() {
        return sCurrentCover;
    }

    /**
     * --------------------- Media Player ----------------------
     */
//    public static MyMusicService.MusicBinder sMusicBinder;
    public static IMuiscService sMusicBinder;

    public static void setCurrentCover(Bitmap currentCover) {
//        if (sCurrentCover != null) sCurrentCover.recycle();
        sCurrentCover = currentCover;
    }

    public final static SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("mm:ss", Locale.CHINESE);

    public final static SimpleDateFormat sSimpleDateFormatFile = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.CHINESE);

    public static ServiceConnection sServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sMusicBinder = IMuiscService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public static void setCurrentMusicItem(@NonNull MusicItem item) {
        sCurrentMusicItem = item;
    }

}
