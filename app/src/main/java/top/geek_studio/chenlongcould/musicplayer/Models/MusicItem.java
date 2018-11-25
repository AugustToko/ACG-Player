/*
 * ************************************************************
 * 文件：MusicItem.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月25日 17:17:32
 * 上次修改时间：2018年11月25日 17:17:26
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Models;

import android.os.Parcel;
import android.os.Parcelable;

public final class MusicItem implements Parcelable {

    private byte[] mMusicCover;
    private String mMimeName;
    private String mMusicName;
    private String mMusicPath;
    private int mMusicID;
    private String mMusicAlbum;
    private int mDuration;
    private int mSize;
    private String mArtist;
    private int mAddTime;

    public MusicItem(String musicName, String musicPath, int musicID, String musicAlbum, int duration, int size, String artist, int addTime) {
        mMusicName = musicName;
        mMusicPath = musicPath;
        mMusicID = musicID;
        mMusicAlbum = musicAlbum;
        mDuration = duration;
        mSize = size;
        mArtist = artist;
        mAddTime = addTime;
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

    public int getAddTime() {
        return mAddTime;
    }

    public static final Parcelable.Creator<MusicItem> CREATOR = new Parcelable.Creator<MusicItem>() {
        @Override
        public MusicItem createFromParcel(Parcel source) {
            return new MusicItem(source);
        }

        @Override
        public MusicItem[] newArray(int size) {
            return new MusicItem[size];
        }
    };

    protected MusicItem(Parcel in) {
        this.mMusicCover = in.createByteArray();
        this.mMimeName = in.readString();
        this.mMusicName = in.readString();
        this.mMusicPath = in.readString();
        this.mMusicID = in.readInt();
        this.mMusicAlbum = in.readString();
        this.mDuration = in.readInt();
        this.mSize = in.readInt();
        this.mArtist = in.readString();
        this.mAddTime = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(this.mMusicCover);
        dest.writeString(this.mMimeName);
        dest.writeString(this.mMusicName);
        dest.writeString(this.mMusicPath);
        dest.writeInt(this.mMusicID);
        dest.writeString(this.mMusicAlbum);
        dest.writeInt(this.mDuration);
        dest.writeInt(this.mSize);
        dest.writeString(this.mArtist);
        dest.writeInt(this.mAddTime);
    }
}
