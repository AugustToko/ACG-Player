/*
 * ************************************************************
 * 文件：VisibleOrGone.java  模块：geeklibrary  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:47
 * 上次修改时间：2019年01月17日 17:28:59
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.geeklibrary;

import androidx.fragment.app.Fragment;

public interface VisibleOrGone {

    /**
     * 动态显示或隐藏View(比如切换不同的Fragment {@link Fragment#setUserVisibleHint(boolean)} )
     */
    void visibleOrGone(int status);
}
