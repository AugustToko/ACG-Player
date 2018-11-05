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

abstract class Values {

    static final String INDEX = "index";

    static int CURRENT_PAGE_INDEX = -1;

    //handler start with 5
    static final int INIT_MUSIC_LIST = 50;
    static final int WAIT_INIT_MUSIC_DATA = 51;
    static final int GET_DATA_DONE = 52;
    static final int SEEK_BAR_UPDATE = 53;
    static final int INIT_SEEK_BAR = 54;

    //permission code start with 6
    static final int REQUEST_WRITE_EXTERNAL_STORAGE = 60;

    static String CURRENT_SONG_PATH = "null";

    static int CURRENT_BIND_INDEX;

    /**
     * result(s)
     * */
    static boolean MUSIC_DATA_INIT_DONE = false;
    static final int MAX_HEIGHT_AND_WIDTH = 100;

    static Comparator<String> sort = String::compareTo;
    static boolean MUSIC_COMPLETION = false;
    static boolean NOW_PLAYING = false;
    static boolean HAS_PLAYED = false;
    static boolean ACTIVITY_FINISHED = false;

}
