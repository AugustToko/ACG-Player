/*
 * ************************************************************
 * 文件：Playlist.java  模块：app  项目：MusicPlayer
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
import androidx.annotation.NonNull;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public final class Playlist extends Item implements Parcelable {
	public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
		public Playlist createFromParcel(Parcel source) {
			return new Playlist(source);
		}

		public Playlist[] newArray(int size) {
			return new Playlist[size];
		}
	};
	public final int id;
	public final String name;

	public Playlist(final int id, final String name) {
		this.id = id;
		this.name = name;
	}

	public Playlist() {
		this.id = -1;
		this.name = "";
	}

	protected Playlist(Parcel in) {
		this.id = in.readInt();
		this.name = in.readString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Playlist playlist = (Playlist) o;

		if (id != playlist.id) return false;
		return name != null ? name.equals(playlist.name) : playlist.name == null;

	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		return result;
	}

	@NonNull
	@Override
	public String toString() {
		return "Playlist{" +
				"id=" + id +
				", name='" + name + '\'' +
				'}';
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.id);
		dest.writeString(this.name);
	}
}