/*
 * ************************************************************
 * 文件：Data.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月30日 20:36:09
 * 上次修改时间：2018年11月30日 13:07:32
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.BroadCasts.MyHeadSetPlugReceiver;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.Service.MyMusicService;

public final class Data {

    public static List<Activity> sActivities = new ArrayList<>();

    /**
     * data
     * */
    public static volatile List<MusicItem> sMusicItems = new ArrayList<>();

    public static volatile List<MusicItem> sMusicItemsBackUp = new ArrayList<>();

    public static volatile List<String> sFavouriteMusic = new ArrayList<>();

    public static volatile List<String> sNotSupportType = new ArrayList<>();

    /**
     * 存储播放历史(序列) default...
     */
    public static List<Integer> sHistoryPlayIndex = new ArrayList<>();

    /**
     * sCurrent DATA
     */
    public static String sCurrentMusicName = "null";

    public static volatile Bitmap sCurrentMusicBitmap = null;

    public static String sCurrentMusicAlbum = "null";

    public static List<String> sCurrentMusicList = new ArrayList<>();


    public static int sNextWillPlayIndex = -1;

    public static ColorStateList sDefTextColorStateList = null;

    public static ColorStateList sDefIcoColorStateList = null;

    public static MyHeadSetPlugReceiver mMyHeadSetPlugReceiver = new MyHeadSetPlugReceiver();

    public static void saveGlobalCurrentData(String musicName, String albumName, Bitmap cover) {
        sCurrentMusicAlbum = albumName;
        sCurrentMusicName = musicName;
        sCurrentMusicBitmap = cover;
    }

    public static String getCurrentMusicName() {
        return sCurrentMusicName;
    }

    public static Bitmap getCurrentMusicBitmap() {
        return sCurrentMusicBitmap;
    }

    public static String getCurrentMusicAlbum() {
        return sCurrentMusicAlbum;
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
