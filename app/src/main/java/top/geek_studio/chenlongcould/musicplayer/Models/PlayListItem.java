/*
 * ************************************************************
 * 文件：PlayListItem.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月05日 09:52:36
 * 上次修改时间：2019年01月05日 09:50:17
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Models;

public final class PlayListItem {

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
}
