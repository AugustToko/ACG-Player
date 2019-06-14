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
import androidx.annotation.Nullable;

/**
 * @author chenlongcould
 */
public final class AlbumItem extends Item implements Parcelable {

	public static final String DEFAULT_ALBUM_ID = "-1";

	private String mAlbumName;
	private int mAlbumId;
	private String mArtist;
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

	public AlbumItem(String albumName, int albumId, String artist) {
		mAlbumName = albumName;
		mAlbumId = albumId;
		mArtist = artist;
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

	@Nullable
	private String mArtwork = null;

	protected AlbumItem(Parcel in) {
		this.mAlbumName = in.readString();
		this.mAlbumId = in.readInt();
		this.mArtist = in.readString();
		this.mArtwork = in.readString();
	}

	@Override
	public int hashCode() {
		return mAlbumId;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public String getmArtwork() {
		return mArtwork;
	}

	public void setmArtwork(@Nullable String mArtwork) {
		this.mArtwork = mArtwork;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.mAlbumName);
		dest.writeInt(this.mAlbumId);
		dest.writeString(this.mArtist);
		dest.writeString(this.mArtwork);
	}
}
