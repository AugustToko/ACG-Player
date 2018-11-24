/*
 * ************************************************************
 * 文件：NativeTest.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月24日 17:50:10
 * 上次修改时间：2018年11月24日 16:57:14
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Utils;

public class NativeTest {

    static {
        System.loadLibrary("native-lib");
    }

    public static native String getCS();

}
