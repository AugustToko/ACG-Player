/*
 * ************************************************************
 * 文件：MusicItem.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月19日 18:40:42
 * 上次修改时间：2018年11月19日 17:29:57
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Models;

public final class MusicItem {

    private byte[] mMusicCover;
    private String mMimeName;
    private String mMusicName;
    private String mMusicPath;
    private int mMusicID;
    private String mMusicAlbum;
    private int mDuration;
    private int mSize;
    private String mArtist;

    public MusicItem(String musicName, String musicPath, int musicID, String musicAlbum, int duration, int size, String artist) {
        mMusicName = musicName;
        mMusicPath = musicPath;
        mMusicID = musicID;
        mMusicAlbum = musicAlbum;
        mDuration = duration;
        mSize = size;
        mArtist = artist;
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

    public int getDuration() {
        return mDuration;
    }

    public int getSize() {
        return mSize;
    }

    public String getArtist() {
        return mArtist;
    }
}
