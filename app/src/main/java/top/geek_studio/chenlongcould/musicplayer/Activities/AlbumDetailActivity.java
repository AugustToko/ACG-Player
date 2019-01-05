/*
 * ************************************************************
 * 文件：AlbumDetailActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月05日 09:52:36
 * 上次修改时间：2019年01月05日 09:50:17
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.Adapters.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.databinding.ActivityAlbumDetailBinding;

/**
 * a activity that show Music Album Detail data
 * <p>
 * has dataBinding
 */
public final class AlbumDetailActivity extends Activity {

    public static final String TAG = "AlbumDetailActivity";

    private ActivityAlbumDetailBinding mAlbumDetailBinding;

    /**
     * ------------- data ---------------
     */
    private List<String> mMusicIds = new ArrayList<>();

    private List<MusicItem> mSongs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAlbumDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_album_detail);
        mAlbumDetailBinding.toolbar.setNavigationOnClickListener(v -> finish());
        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            String key = intent.getStringExtra("key");
            mAlbumDetailBinding.collapsingToolbar.setTitle(key);

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
                            GlideApp.with(AlbumDetailActivity.this).load(file).into(mAlbumDetailBinding.albumImage);
                        else
                            GlideApp.with(AlbumDetailActivity.this).load(R.drawable.ic_audiotrack_24px).into(mAlbumDetailBinding.albumImage);
                    } else
                        GlideApp.with(AlbumDetailActivity.this).load(R.drawable.ic_audiotrack_24px).into(mAlbumDetailBinding.albumImage);
                    cursor2.close();
                } else {
                    return;
                }
            } else {
                Toast.makeText(this, "Get Image error (-1)", Toast.LENGTH_SHORT).show();
            }

            MyRecyclerAdapter adapter = new MyRecyclerAdapter(mSongs, this, TAG);
            mAlbumDetailBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
            mAlbumDetailBinding.recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
            mAlbumDetailBinding.recyclerView.setHasFixedSize(true);
            mAlbumDetailBinding.recyclerView.setAdapter(adapter);
        }

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
