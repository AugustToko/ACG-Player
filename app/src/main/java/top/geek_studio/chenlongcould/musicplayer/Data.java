/*
 * ************************************************************
 * 文件：Data.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月27日 13:11:38
 * 上次修改时间：2019年01月27日 13:08:50
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

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.disposables.Disposable;
import jp.wasabeef.glide.transformations.BlurTransformation;
import top.geek_studio.chenlongcould.geeklibrary.recycler_tools.RecycleViewDivider;
import top.geek_studio.chenlongcould.geeklibrary.theme.Theme;
import top.geek_studio.chenlongcould.musicplayer.Models.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.Models.ArtistItem;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.Models.PlayListItem;
import top.geek_studio.chenlongcould.musicplayer.activity.CarViewActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.broadcasts.MyHeadSetPlugReceiver;

public final class Data {

    public volatile static boolean HAS_BIND = false;

    public static ArrayList<Disposable> sDisposables = new ArrayList<>();

    public static WeakReference<MainActivity> sMainRef;

    private static RecyclerView.ItemDecoration mItemDecoration;

    public static RecyclerView.ItemDecoration getItemDecoration(Context context) {
        if (mItemDecoration == null) {
            mItemDecoration = new RecycleViewDivider(
                    context, LinearLayoutManager.VERTICAL, 2, ContextCompat.getColor(context, R.color.line_color));
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
    public static List<MusicItem> sMusicItemsBackUp = new ArrayList<>();

    public static CarViewActivity sCarViewActivity = null;
    public static List<AlbumItem> sAlbumItems = new ArrayList<>();
    public static List<AlbumItem> sAlbumItemsBackUp = new ArrayList<>();
    public static List<MusicItem> sPlayOrderList = new ArrayList<>();
    public static List<PlayListItem> sPlayListItems = new ArrayList<>();
    public static List<ArtistItem> sArtistItems = new ArrayList<>();
    public static List<ArtistItem> sArtistItemsBackUp = new ArrayList<>();

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

    /**
     * 垃圾箱(dislike)
     */
    public final static List<MusicItem> sTrashCanList = new ArrayList<>();

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
    //多选存储, 存储MusicItem id
    public static ArrayList<Integer> sSelections = new ArrayList<>();

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

}
