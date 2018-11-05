/*
 * ************************************************************
 * 文件：Values.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月05日 17:54:16
 * 上次修改时间：2018年11月05日 17:53:35
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

abstract class Values {

    static final String INDEX = "index";

    static int CURRENT_PAGE_INDEX = -1;

    static int FRAGMENTS_NUM = -1;

    //handler start with 5
    static final int INIT_MUSIC_LIST_DONE = 50;
    //permission code start with 6
    static final int REQUEST_WRITE_EXTERNAL_STORAGE = 60;

    static String CURRENT_SONG_PATH = "null";

    static int CURRENT_BIND_INDEX;

    /**
     * in recycler viewHolder
     * MAX = LIST.SIZE
     * MAIN = ITEMS in phone screen
     * */
    static int MAX_BIND_RELORD_MUN = 15;
}
