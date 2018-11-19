/*
 * ************************************************************
 * 文件：AlbumItem.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月19日 18:40:42
 * 上次修改时间：2018年11月19日 17:29:57
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Models;

public final class AlbumItem {

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
