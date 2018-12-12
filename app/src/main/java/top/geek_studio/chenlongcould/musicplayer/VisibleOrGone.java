/*
 * ************************************************************
 * 文件：VisibleOrGone.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月12日 11:57:29
 * 上次修改时间：2018年12月12日 11:57:13
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

public interface VisibleOrGone {

    /**
     * 动态显示或隐藏View(比如切换不同的Fragment {@link android.support.v4.app.Fragment#setUserVisibleHint(boolean)} )
     */
    void visibleOrGone(int status);
}
