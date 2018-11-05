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
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

class Data {
    static List<Activity> sActivities = new ArrayList<>();

    static String sCurrentMusicName;

    static String sCurrentMusicPath;

    static Bitmap sCurrentMusicBitmap;

    static String sCurrentMusicAlbum;
}
