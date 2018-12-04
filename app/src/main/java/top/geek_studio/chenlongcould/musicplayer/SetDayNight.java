/*
 * ************************************************************
 * 文件：SetDayNight.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月04日 11:31:38
 * 上次修改时间：2018年12月03日 15:23:50
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

public interface SetDayNight {

    int MODE_DAY = 0;

    int MODE_NIGHT = 1;

    void setDayNight(int type);
}
