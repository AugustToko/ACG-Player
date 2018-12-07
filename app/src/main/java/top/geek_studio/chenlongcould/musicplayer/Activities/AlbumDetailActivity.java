/*
 * ************************************************************
 * 文件：AlbumDetailActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月07日 08:59:28
 * 上次修改时间：2018年12月07日 07:56:09
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.Adapters.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.R;

public final class AlbumDetailActivity extends Activity {

    public static final String TAG = "AlbumDetailActivity";

    private Toolbar mToolbar;

    private AppBarLayout mAppBarLayout;

    private RecyclerView mRecyclerView;

    private ImageView mImageView;

    private CollapsingToolbarLayout mCollapsingToolbarLayout;

    /**
     * ------------- data ---------------
     */
    private List<String> mMusicIds = new ArrayList<>();

    private List<MusicItem> mSongs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        initView();

        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            String key = intent.getStringExtra("key");
            mCollapsingToolbarLayout.setTitle(key);

            //根据Album名称查music ID
            Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Media._ID}, MediaStore.Audio.Media.ALBUM + " = ?", new String[]{key}, null);
            if (cursor != null) {
                cursor.moveToFirst();
                do {
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));     //get music _id
                    mMusicIds.add(id);
                } while (cursor.moveToNext());
                cursor.close();
            }

            //selection...
            if (mMusicIds != null && mMusicIds.size() > 0) {
                StringBuilder selection = new StringBuilder(MediaStore.Audio.Media._ID + " IN (");
                for (int i = 0; i < mMusicIds.size(); i++) {
                    selection.append("?");
                    if (i != mMusicIds.size() - 1) {
                        selection.append(",");
                    }
                }
                selection.append(")");

                Cursor cursor2 = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                        selection.toString(), mMusicIds.toArray(new String[0]), MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

                //获取数据(该专辑下歌曲)
                if (cursor2 != null) {
                    cursor2.moveToFirst();
                    do {
                        final String path = cursor2.getString(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

                        if (!new File(path).exists()) return;

                        final String mimeType = cursor2.getString(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
                        final String name = cursor2.getString(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                        final String albumName = cursor2.getString(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                        final int id = cursor2.getInt(cursor2.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                        final int size = (int) cursor2.getLong(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                        final int duration = cursor2.getInt(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                        final String artist = cursor2.getString(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                        final long addTime = cursor2.getLong(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));
                        final int albumId = cursor2.getInt(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

                        MusicItem.Builder builder = new MusicItem.Builder(id, name, path)
                                .musicAlbum(albumName)
                                .addTime((int) addTime)
                                .artist(artist)
                                .duration(duration)
                                .mimeName(mimeType)
                                .size(size)
                                .addAlbumId(albumId);

                        mSongs.add(builder.build());
                    } while (cursor2.moveToNext());
                    cursor2.close();
                }
            }

            //获取MainAlbum图像
            int id = intent.getIntExtra("_id", -1);
            if (id != -1) {
                Cursor cursor2 = getContentResolver().query(Uri.parse(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI + String.valueOf(File.separatorChar) + id), new String[]{MediaStore.Audio.Albums.ALBUM_ART}, null, null, null);

                if (cursor2 != null) {
                    cursor2.moveToFirst();
                    String img = cursor2.getString(0);
                    if (img != null) {
                        File file = new File(img);
                        if (file.exists())
                            GlideApp.with(AlbumDetailActivity.this).load(file).into(mImageView);
                        else
                            GlideApp.with(AlbumDetailActivity.this).load(R.drawable.ic_audiotrack_24px).into(mImageView);
                    } else
                        GlideApp.with(AlbumDetailActivity.this).load(R.drawable.ic_audiotrack_24px).into(mImageView);
                    cursor2.close();
                } else {
                    return;
                }
            } else {
                Toast.makeText(this, "Get Image error (-1)", Toast.LENGTH_SHORT).show();
            }

            MyRecyclerAdapter adapter = new MyRecyclerAdapter(mSongs, this, TAG);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setAdapter(adapter);
        }

    }

    private void initView() {
        mRecyclerView = findViewById(R.id.activity_list_recycler);
        mToolbar = findViewById(R.id.activity_list_toolbar);
        mAppBarLayout = findViewById(R.id.activity_list_app_bar);
        mCollapsingToolbarLayout = findViewById(R.id.activity_list_collapsing_toolbar);
        mImageView = findViewById(R.id.activity_list_head_pic);
        mToolbar.inflateMenu(R.menu.menu_toolbar_album_detail);

        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
}
