/*
 * ************************************************************
 * 文件：MyThemeDBHelper.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:46
 * 上次修改时间：2019年01月17日 17:28:53
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;
import top.geek_studio.chenlongcould.geeklibrary.Theme.ThemeStore;

public class MyThemeDBHelper extends SQLiteOpenHelper {

    public static final String TAG = "MyThemeDBHelper";
    private static final String CREATE_THEME = "create table " + ThemeStore.TABLE + " ("
            + "id integer primary key autoincrement, "
            + "author text, "
            + "title text, "
            + "nav_name text, "
            + "thumbnail text, "
            + "support_area text, "
            + "primary_color text, "
            + "date text)";
    private Context mContext;
    private String mDBName;

    public MyThemeDBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
        mDBName = name;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_THEME);
        Log.d(TAG, "onCreate: create db: " + mDBName + " done");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
