/*
 * ************************************************************
 * 文件：MusicItem.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:46
 * 上次修改时间：2019年01月17日 17:28:53
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class MusicItem implements Parcelable {

	public static final Creator<MusicItem> CREATOR = new Creator<MusicItem>() {
		@Override
		public MusicItem createFromParcel(Parcel source) {
			return new MusicItem(source);
		}

		@Override
		public MusicItem[] newArray(int size) {
			return new MusicItem[size];
		}
	};
	private String mMimeName;
	private String mMusicName;
	private String mMusicPath;
	private int mMusicID;
	private int mAlbumId;
	private String mMusicAlbum;
	private long mDuration;
	private int mSize;
	private String mArtist;
	private long mAddTime;
	transient private boolean mIsFavourite;
	private int mArtistId;

	private MusicItem(Builder builder) {
		mMusicID = builder.mMusicID;
		mMusicAlbum = builder.mMusicAlbum;
		mMusicName = builder.mMusicName;
		mAddTime = builder.mAddTime;
		mDuration = builder.mDuration;
		mSize = builder.mSize;
		mMusicPath = builder.mMusicPath;
		mArtist = builder.mArtist;
		mAlbumId = builder.mAlbumId;
		mArtistId = builder.mArtistId;
		mIsFavourite = builder.mIsFav;
	}

	protected MusicItem(Parcel in) {
		this.mMimeName = in.readString();
		this.mMusicName = in.readString();
		this.mMusicPath = in.readString();
		this.mMusicID = in.readInt();
		this.mAlbumId = in.readInt();
		this.mMusicAlbum = in.readString();
		this.mDuration = in.readInt();
		this.mSize = in.readInt();
		this.mArtist = in.readString();
		this.mAddTime = in.readInt();
		this.mArtistId = in.readInt();
	}

	public String getMusicName() {
		return mMusicName == null ? "null" : mMusicName;
	}

	public String getMusicPath() {
		return mMusicPath;
	}

	public int getMusicID() {
		return mMusicID;
	}

	public String getMusicAlbum() {
		return mMusicAlbum == null ? "null" : mMusicAlbum;
	}

	public long getDuration() {
		return mDuration;
	}

	public int getSize() {
		return mSize;
	}

	public String getArtist() {
		return mArtist;
	}

	public long getAddTime() {
		return mAddTime;
	}

	public int getArtistId() {
		return mArtistId;
	}

	public String getMimeName() {
		return mMimeName;
	}

	public boolean isFavourite() {
		return mIsFavourite;
	}

	public int getAlbumId() {
		return mAlbumId;
	}

	@NonNull
	@Override
	public String toString() {
		return mMusicName + " @ " + mMusicAlbum + " @ " + mMusicPath + "@" + mArtist;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		boolean result = false;
		if (obj instanceof MusicItem) {
			MusicItem item = (MusicItem) obj;
			if (item.getMusicID() == mMusicID) {
				result = true;
			}
		}
		return result;
	}

	@Override
	public int hashCode() {
		return mMusicID;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.mMimeName);
		dest.writeString(this.mMusicName);
		dest.writeString(this.mMusicPath);
		dest.writeInt(this.mMusicID);
		dest.writeInt(this.mAlbumId);
		dest.writeString(this.mMusicAlbum);
		dest.writeLong(this.mDuration);
		dest.writeInt(this.mSize);
		dest.writeString(this.mArtist);
		dest.writeLong(this.mAddTime);
		dest.writeInt(this.mArtistId);
	}

	public static class Builder {
		private int mMusicID;
		private String mMusicName;
		private String mMusicPath;

		private String mMimeName = "null";
		private String mMusicAlbum = "null";
		private long mDuration = -1;
		private int mSize = -1;
		private String mArtist = "null";
		private long mAddTime = -1;
		private int mAlbumId;
		private int mArtistId;
		private boolean mIsFav;

		public Builder(int musicID, String musicName, String musicPath) {
			mMusicID = musicID;
			mMusicName = musicName;
			mMusicPath = musicPath;
		}

		public Builder mimeName(String mime) {
			mMimeName = mime;
			return this;
		}

		public Builder musicAlbum(String album) {
			mMusicAlbum = album;
			return this;
		}

		public Builder duration(long time) {
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

		public Builder addTime(long time) {
			mAddTime = time;
			return this;
		}

		public Builder addAlbumId(int id) {
			mAlbumId = id;
			return this;
		}

		public Builder addArtistId(int id) {
			mArtistId = id;
			return this;
		}

		public Builder isFavourite(boolean b) {
			mIsFav = b;
			return this;
		}

		public MusicItem build() {
			return new MusicItem(this);
		}

	}
}
