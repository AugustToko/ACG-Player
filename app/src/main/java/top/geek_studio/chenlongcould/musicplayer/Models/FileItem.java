/*
 * ************************************************************
 * 文件：FileItem.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月04日 20:36:03
 * 上次修改时间：2018年12月28日 07:54:34
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public final class FileItem implements Parcelable {
    public static final Parcelable.Creator<FileItem> CREATOR = new Parcelable.Creator<FileItem>() {
        @Override
        public FileItem createFromParcel(Parcel source) {
            return new FileItem(source);
        }

        @Override
        public FileItem[] newArray(int size) {
            return new FileItem[size];
        }
    };
    private boolean isFile;
    private String mFileName;
    private String mFileTime;
    private String mFileSize;
    private File mFile;

    public FileItem(boolean isFile, String fileName, String fileTime, String fileSize, File file) {
        this.isFile = isFile;
        mFileName = fileName;
        mFileTime = fileTime;
        mFileSize = fileSize;
        mFile = file;
    }

    protected FileItem(Parcel in) {
        this.isFile = in.readByte() != 0;
        this.mFileName = in.readString();
        this.mFileTime = in.readString();
        this.mFileSize = in.readString();
        this.mFile = (File) in.readSerializable();
    }

    public boolean isFile() {
        return isFile;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    public String getFileTime() {
        return mFileTime;
    }

    public void setFileTime(String fileTime) {
        mFileTime = fileTime;
    }

    public String getFileSize() {
        return mFileSize;
    }

    public void setFileSize(String fileSize) {
        mFileSize = fileSize;
    }

    public void setFile(boolean file) {
        isFile = file;
    }

    public File getFile() {
        return mFile;
    }

    public void setFile(File file) {
        mFile = file;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.isFile ? (byte) 1 : (byte) 0);
        dest.writeString(this.mFileName);
        dest.writeString(this.mFileTime);
        dest.writeString(this.mFileSize);
        dest.writeSerializable(this.mFile);
    }
}
