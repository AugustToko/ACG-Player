/*
 * ************************************************************
 * 文件：VisibleOrGone.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月13日 13:55:33
 * 上次修改时间：2018年12月13日 12:52:21
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Interface;

public interface VisibleOrGone {

    /**
     * 动态显示或隐藏View(比如切换不同的Fragment {@link android.support.v4.app.Fragment#setUserVisibleHint(boolean)} )
     */
    void visibleOrGone(int status);
}