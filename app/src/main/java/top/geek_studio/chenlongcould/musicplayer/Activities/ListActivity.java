/*
 * ************************************************************
 * 文件：ListActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月18日 21:28:39
 * 上次修改时间：2018年11月18日 21:28:14
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.Adapters.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.R;

public class ListActivity extends Activity {

    private static final String TAG = "ListActivity";

    private Toolbar mToolbar;

    private AppBarLayout mAppBarLayout;

    private RecyclerView mRecyclerView;

    private ImageView mImageView;

    private CollapsingToolbarLayout mCollapsingToolbarLayout;

    /**
     * ------------- data ---------------
     */
    private List<String> mIds = new ArrayList<>();

    private List<MusicItem> mSongs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mRecyclerView = findViewById(R.id.activity_list_recycler);
        mToolbar = findViewById(R.id.activity_list_toolbar);
        mAppBarLayout = findViewById(R.id.activity_list_app_bar);
        mCollapsingToolbarLayout = findViewById(R.id.activity_list_collapsing_toolbar);
        mImageView = findViewById(R.id.activity_list_head_pic);

        Intent intent = getIntent();
        if (intent != null) {
            String key = intent.getStringExtra("key");
            mCollapsingToolbarLayout.setTitle(key);
            Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Albums._ID}, MediaStore.Audio.Media.ALBUM + " = ?", new String[]{key}, null);
            if (cursor != null) {
                cursor.moveToFirst();
                do {
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID));
                    mIds.add(id);
                } while (cursor.moveToNext());
                cursor.close();
            }

            if (mIds != null && mIds.size() > 0) {
                StringBuilder selection = new StringBuilder(MediaStore.Audio.Media._ID + " IN (");
                for (int i = 0; i < mIds.size(); i++) {
                    selection.append("?");
                    if (i != mIds.size() - 1) {
                        selection.append(",");
                    }
                }
                selection.append(")");
                Cursor cursor2 = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                        selection.toString(), mIds.toArray(new String[0]), MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

                if (cursor2 != null) {
                    cursor2.moveToFirst();
                    do {
                        String path = cursor2.getString(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                        String mimeType = cursor2.getString(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
                        String name = cursor2.getString(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                        String albumName = cursor2.getString(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                        int id = cursor2.getInt(cursor2.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                        int size = (int) cursor2.getLong(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                        int duration = cursor2.getInt(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                        String artist = cursor2.getString(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));

                        mSongs.add(new MusicItem(name, path, id, albumName, duration, size, artist));
                    } while (cursor2.moveToNext());
                    cursor2.close();
                }
            }

            int id = intent.getIntExtra("_id", -1);
            if (id != -1) {
                Cursor cursor2 = getContentResolver().query(Uri.parse(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI + "/" + id), new String[]{MediaStore.Audio.Albums.ALBUM_ART}, null, null, null);
                if (cursor2 != null) {
                    cursor2.moveToFirst();
                    String img = cursor2.getString(0);
                    Bitmap bitmap = BitmapFactory.decodeFile(img);
                    if (bitmap != null) {
                        GlideApp.with(this).load(bitmap).into(mImageView);
                    } else {
                        GlideApp.with(this).load(R.drawable.ic_audiotrack_24px).into(mImageView);
                    }
                    cursor2.close();
                }
            }

            MyRecyclerAdapter adapter = new MyRecyclerAdapter(mSongs, this);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setAdapter(adapter);
        }

    }
}