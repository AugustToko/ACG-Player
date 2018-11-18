/*
 * ************************************************************
 * 文件：AlbumItem.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月18日 21:28:39
 * 上次修改时间：2018年11月18日 20:29:51
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

public class AlbumItem {

    private String mAlbumName;

    private int mAlbumId;

    public AlbumItem(String albumName, int albumId) {
        mAlbumName = albumName;
        mAlbumId = albumId;
    }

    public int getAlbumId() {
        return mAlbumId;
    }

    public String getAlbumName() {
        return mAlbumName;
    }
}
