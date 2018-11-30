/*
 * ************************************************************
 * 文件：MusicItem.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月30日 20:36:09
 * 上次修改时间：2018年11月30日 08:43:36
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

    private MusicItem(Builder builder) {
        mMusicID = builder.mMusicID;
        mMusicAlbum = builder.mMusicAlbum;
        mMusicName = builder.mMusicName;
        mAddTime = builder.mAddTime;
        mDuration = builder.mDuration;
        mSize = builder.mSize;
        mMusicPath = builder.mMusicPath;
        mArtist = builder.mArtist;
        mMusicCover = builder.mMusicCover;
    }

    public static class Builder {
        private int mMusicID;
        private String mMusicName;
        private String mMusicPath;

        private byte[] mMusicCover = null;
        private String mMimeName = "null";
        private String mMusicAlbum = "null";
        private int mDuration = -1;
        private int mSize = -1;
        private String mArtist = "null";
        private int mAddTime = -1;

        public Builder(int musicID, String musicName, String musicPath) {
            mMusicID = musicID;
            mMusicName = musicName;
            mMusicPath = musicPath;
        }

        public Builder musicCover(byte[] cover) {
            mMusicCover = cover;
            return this;
        }

        public Builder mimeName(String mime) {
            mMimeName = mime;
            return this;
        }

        public Builder musicAlbum(String album) {
            mMusicAlbum = album;
            return this;
        }

        public Builder duration(int time) {
            mDuration = time;
            return this;
        }

        public Builder size(int size) {
            mSize = size;
            return this;
        }

        public Builder artist(String artist) {
            mArtist = artist;
            return this;
        }

        public Builder addTime(int time) {
            mAddTime = time;
            return this;
        }

        public MusicItem build() {
            return new MusicItem(this);
        }
    }
}
