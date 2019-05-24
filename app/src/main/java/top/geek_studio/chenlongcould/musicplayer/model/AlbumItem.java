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

package top.geek_studio.chenlongcould.musicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author chenlongcould
 */
public final class AlbumItem implements Parcelable {

	public static final String DEFAULT_ALBUM_ID = "-1";
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
	private String mAlbumName;
	private int mAlbumId;
	private String mArtist;

	public AlbumItem(String albumName, int albumId, String artist) {
		mAlbumName = albumName;
		mAlbumId = albumId;
		mArtist = artist;
	}

	protected AlbumItem(Parcel in) {
		this.mAlbumName = in.readString();
		this.mAlbumId = in.readInt();
	}

	public String getArtist() {
		return mArtist;
	}

	public int getAlbumId() {
		return mAlbumId;
	}

	public String getAlbumName() {
		return mAlbumName;
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
