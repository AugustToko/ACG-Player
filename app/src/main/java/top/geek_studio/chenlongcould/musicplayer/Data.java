/*
 * ************************************************************
 * 文件：Data.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月13日 16:23:45
 * 上次修改时间：2018年12月13日 16:23:32
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import top.geek_studio.chenlongcould.musicplayer.BroadCasts.MyHeadSetPlugReceiver;
import top.geek_studio.chenlongcould.musicplayer.Models.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.Models.PlayListItem;

public final class Data {

    public static List<Activity> sActivities = new ArrayList<>();

    /**
     * data
     * */
    public static List<MusicItem> sMusicItems = new ArrayList<>();

    public static List<AlbumItem> sAlbumItems = new ArrayList<>();

    public static List<MusicItem> sMusicItemsBackUp = new ArrayList<>();

    public static List<MusicItem> sPlayOrderList = new ArrayList<>();

    public static List<PlayListItem> sPlayListItems = new ArrayList<>();

    /**
     * nextWillPlay
     * def null
     */
    public static MusicItem sNextWillPlayItem = null;

    /**
     * 存储播放历史(序列) default...
     */
    public static List<Integer> sHistoryPlayIndex = new ArrayList<>();

    /**
     * sCurrent DATA
     */
    public static MusicItem sCurrentMusicItem = new MusicItem.Builder(-1, "null", "null").build();

    /**
     * save temp bitmap
     */
    public static Bitmap sCurrentCover = null;
    /**
     * --------------------- Media Player ----------------------
     */
//    public static MyMusicService.MusicBinder sMusicBinder;
    public static IMuiscService sMusicBinder;

    public static MyHeadSetPlugReceiver mMyHeadSetPlugReceiver = new MyHeadSetPlugReceiver();

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
