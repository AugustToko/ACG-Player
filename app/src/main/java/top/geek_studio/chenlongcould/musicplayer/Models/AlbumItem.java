/*
 * ************************************************************
 * 文件：AlbumItem.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月05日 09:52:36
 * 上次修改时间：2019年01月05日 09:50:17
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Models;

import android.os.Parcel;
import android.os.Parcelable;

public final class AlbumItem implements Parcelable {

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

    public static final Parcelable.Creator<AlbumItem> CREATOR = new Parcelable.Creator<AlbumItem>() {
        @Override
        public AlbumItem createFromParcel(Parcel source) {
            return new AlbumItem(source);
        }

        @Override
        public AlbumItem[] newArray(int size) {
            return new AlbumItem[size];
        }
    };

    protected AlbumItem(Parcel in) {
        this.mAlbumName = in.readString();
        this.mAlbumId = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mAlbumName);
        dest.writeInt(this.mAlbumId);
    }
}
