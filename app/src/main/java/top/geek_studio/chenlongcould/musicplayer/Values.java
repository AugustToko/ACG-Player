/*
 * ************************************************************
 * 文件：Values.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月06日 07:32:30
 * 上次修改时间：2018年11月05日 21:21:24
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

import java.util.Comparator;

public final class Values {

    /**
     * final string(s)
     */
    public static final String INDEX = "index";
    public static final String PLAY_LIST_NUM = "PLAY_LIST_NUM";
    //handler start with 5
    public static final int INIT_MUSIC_LIST = 50;
    public static final int GET_DATA_DONE = 52;
    public static final int SEEK_BAR_UPDATE = 53;
    public static final int INIT_SEEK_BAR = 54;
    //permission code start with 6
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 60;
    public static final int MAX_HEIGHT_AND_WIDTH = 100;

    /**
     * TAGs
     */
    public static final String TAG_UNIVERSAL_ONE = "TAG_UNIVERSAL_ONE";
    public static String SURE_GET_PERMISSION = "SURE_GET_PERMISSION";
    public static final String PKG_NAME = "top.geek_studio.chenlongcould.musicplayer";
    public static String PLAY_LIST_SPF_KEY = "PLAY_LIST_SPF_KEY";
    /**
     * TEMP DATA
     */
    //default value -1
    public static String CURRENT_SONG_PATH = "null";
    public static int CURRENT_BIND_INDEX_MUSIC_LIST = -1;
    public static int CURRENT_BIND_INDEX_ALBUM_LIST = -1;
    public static int CURRENT_PAGE_INDEX = -1;
    public static int CURRENT_SELECT_ITEM_INDEX_WITH_ITEM_MENU = -1;
    public static boolean HAS_PLAYED = false;           //检测app打开后, 是否播放过音乐 (如果没, 默认点击播放按钮为随机播放)
    public static String PLAY_LIST_SPF_NAME = "PLAY_LIST_SPF_NAME";
    public static int CURRENT_MUSIC_INDEX = -1;
    public static String CURRENT_PLAY_TYPE = "RANDOM";
    public static String TYPE_RANDOM = "RANDOM";

    /**
     * result(s), status
     * */
    //default value false
    public static boolean MUSIC_DATA_INIT_DONE = false;
    public static boolean SERVICE_RUNNING = false;
    public static boolean MUSIC_PLAYING = false;

    public static boolean HAS_SET_INFO_BAR_BACKGROUND_BACK = false;

    /**
     * sort(s)
     */
    public static Comparator<String> sort = String::compareTo;
    public static int DEF_CROSSFATE_TIME = 500;

    public static class BroadCast {
        public static String ReceiverOnMusicPlay = "top.geek_studio.chenlongcould.musicplayer.BroadCasts.ReceiverOnMusicPlay";
        public static String ReceiverOnMusicStop = "top.geek_studio.chenlongcould.musicplayer.BroadCasts.ReceiverOnMusicStop";
        public static String ReceiverOnMusicPause = "top.geek_studio.chenlongcould.musicplayer.BroadCasts.ReceiverOnMusicPause";
    }
}
