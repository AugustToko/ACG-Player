/*
 * ************************************************************
 * 文件：PlayListItem.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:46
 * 上次修改时间：2019年01月17日 17:28:59
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;

public final class PlayListItem implements Parcelable {

	public static final Parcelable.Creator<PlayListItem> CREATOR = new Parcelable.Creator<PlayListItem>() {
		@Override
		public PlayListItem createFromParcel(Parcel source) {
			return new PlayListItem(source);
		}

		@Override
		public PlayListItem[] newArray(int size) {
			return new PlayListItem[size];
		}
	};
	private int mId;
	private String mName;
	private String mPath = "null";
	private long mAddTime = -1;

	public PlayListItem(int id, String name, String path, long addTime) {
		mId = id;
		mName = name;
		mPath = path;
		mAddTime = addTime;
	}

	public PlayListItem(int id, String name) {
		mId = id;
		mName = name;
	}

	protected PlayListItem(Parcel in) {
		this.mId = in.readInt();
		this.mName = in.readString();
		this.mPath = in.readString();
		this.mAddTime = in.readLong();
	}

	public int getId() {
		return mId;
	}

	public String getName() {
		return mName;
	}

	public String getPath() {
		return mPath;
	}

	public long getAddTime() {
		return mAddTime;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.mId);
		dest.writeString(this.mName);
		dest.writeString(this.mPath);
		dest.writeLong(this.mAddTime);
	}
}
