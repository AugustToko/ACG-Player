/*
 * ************************************************************
 * 文件：MusicItem.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月14日 15:30:40
 * 上次修改时间：2018年11月14日 15:29:35
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

public class MusicItem {

    byte[] mMusicCover;
    private String mMusicName;
    private String mMusicPath;
    private int mMusicID;
    private String mMusicAlbum;

    public MusicItem(String musicName, String musicPath, int musicID, String musicAlbum) {
        mMusicName = musicName;
        mMusicPath = musicPath;
        mMusicID = musicID;
        mMusicAlbum = musicAlbum;
    }

    public String getMusicName() {
        return mMusicName;
    }

    public String getMusicPath() {
        return mMusicPath;
    }

    public int getMusicID() {
        return mMusicID;
    }

    public String getMusicAlbum() {
        return mMusicAlbum;
    }

    public byte[] getMusicCover() {
        return mMusicCover;
    }
}
