/*
 * ************************************************************
 * 文件：AlbumItem.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:46
 * 上次修改时间：2019年01月17日 17:28:52
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Models;

import android.os.Parcel;
import android.os.Parcelable;

public final class ArtistItem implements Parcelable {

    public static final Creator<ArtistItem> CREATOR = new Creator<ArtistItem>() {
        @Override
        public ArtistItem createFromParcel(Parcel source) {
            return new ArtistItem(source);
        }

        @Override
        public ArtistItem[] newArray(int size) {
            return new ArtistItem[size];
        }
    };

    private String mArtistName;
    private int mArtistId;

    public ArtistItem(String artistName, int artistId) {
        mArtistName = artistName;
        mArtistId = artistId;
    }

    protected ArtistItem(Parcel in) {
        this.mArtistName = in.readString();
        this.mArtistId = in.readInt();
    }

    public int getArtistId() {
        return mArtistId;
    }

    public String getArtistName() {
        return mArtistName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mArtistName);
        dest.writeInt(this.mArtistId);
    }
}
