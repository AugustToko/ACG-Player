/*
 * ************************************************************
 * 文件：Data.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月06日 07:32:30
 * 上次修改时间：2018年11月05日 20:50:32
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

import top.geek_studio.chenlongcould.musicplayer.Service.MyMusicService;

public final class Data {

    public static List<Activity> sActivities = new ArrayList<>();

    /**
     * data
     * */
    public static volatile ArrayList<String> mMusicPathList = new ArrayList<>();

    public static volatile List<String> mSongNameList = new ArrayList<>();

    public static volatile ArrayList<String> mSongAlbumList = new ArrayList<>();

    public static volatile List<Integer> mMusicIdList = new ArrayList<>();

    // TODO: 2018/11/11 待完善
    public static volatile List<String> sFavouriteMusic = new ArrayList<>();

    /**
     * 存储播放历史(序列) default...
     */
    public static List<Integer> sHistoryPlayIndex = new ArrayList<>();

    /**
     * status
     */
    public static String sCurrentMusicName = null;

    public static Bitmap sCurrentMusicBitmap = null;

    public static String sCurrentMusicAlbum = null;

    public static String sNextPlaySongPath = null;

    public static ColorStateList sDefTextColorStateList = null;

    public static ColorStateList sDefIcoColorStateList = null;

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
