/*
 * ************************************************************
 * 文件：Data.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月06日 19:19:07
 * 上次修改时间：2018年12月06日 19:18:30
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import top.geek_studio.chenlongcould.musicplayer.BroadCasts.MyHeadSetPlugReceiver;
import top.geek_studio.chenlongcould.musicplayer.Models.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.Service.MyMusicService;
import top.geek_studio.chenlongcould.musicplayer.Utils.NotificationUtils;

public final class Data {

    public static List<Activity> sActivities = new ArrayList<>();

    /**
     * data
     * */
    public static volatile List<MusicItem> sMusicItems = new ArrayList<>();

    public static volatile List<AlbumItem> sAlbumItems = new ArrayList<>();

    public static volatile List<MusicItem> sMusicItemsBackUp = new ArrayList<>();

    public static volatile List<MusicItem> sPlayOrderList = new ArrayList<>();

    /**
     * nextWillPlay
     * def null
     */
    public static volatile MusicItem sNextWillPlayItem = null;

    /**
     * 存储播放历史(序列) default...
     */
    public static List<Integer> sHistoryPlayIndex = new ArrayList<>();

    public static MyMusicService sMyMusicService;

    /**
     * sCurrent DATA
     */
    public static String sCurrentMusicName = "null";

    public static volatile Bitmap sCurrentMusicBitmap = null;

    public static String sCurrentMusicAlbum = "null";

    public static MyHeadSetPlugReceiver mMyHeadSetPlugReceiver = new MyHeadSetPlugReceiver();

    public final static SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("mm:ss", Locale.CHINESE);

    public static NotificationUtils notificationUtils;

    public static void saveGlobalCurrentData(String musicName, String albumName, Bitmap cover) {
        sCurrentMusicAlbum = albumName;
        sCurrentMusicName = musicName;
        sCurrentMusicBitmap = cover;
    }

    /**
     * --------------------- Media Player ----------------------
     */
    public static MyMusicService.MusicBinder sMusicBinder;

    public static ServiceConnection sServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sMusicBinder = (MyMusicService.MusicBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

}
