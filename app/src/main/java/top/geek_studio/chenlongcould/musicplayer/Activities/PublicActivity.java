/*
 * ************************************************************
 * 文件：PublicActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月10日 14:49:08
 * 上次修改时间：2018年12月10日 14:47:45
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.Adapters.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.IStyle;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public class PublicActivity extends AppCompatActivity implements IStyle {

    public static final String TAG = "PublicActivity";

    private AppBarLayout mAppBarLayout;

    private Toolbar mToolbar;

    private RecyclerView mRecyclerView;

    private List<MusicItem> mMusicItemList;

    private String currentListName;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recent);
        mRecyclerView = findViewById(R.id.activity_add_recent_recycler);
        mAppBarLayout = findViewById(R.id.app_bar_layout);
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        initStyle();

        String type = getIntent().getStringExtra("start_by");

        if (type != null) {
            switch (type) {
                case "add recent": {
                    mToolbar.setTitle(getResources().getString(R.string.add_recent));
                    ArrayList<MusicItem> musicItems = new ArrayList<>(Data.sMusicItems);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        musicItems.sort((o1, o2) -> {
                            if (o1 == null || o2 == null) {
                                return 0;
                            }
                            return Integer.compare(o1.getAddTime(), o2.getAddTime());
                        });
                    } else {
                        // TODO: 2018/11/22 quickSort
                        System.exit(0);
                    }
                    MyRecyclerAdapter adapter = new MyRecyclerAdapter(musicItems, this, TAG);
                    mRecyclerView.setAdapter(adapter);
                }
                break;
                case "favourite music": {
                    // TODO: 2018/11/11  favourite music
                    mToolbar.setTitle(getResources().getString(R.string.my_favourite));
                }
                break;

                case "play_list_item": {

                    new AsyncTask<Void, Void, Void>() {

//                        ArrayList<MusicItem> listMusicItems = new ArrayList<>();

                        @Override
                        protected void onPreExecute() {
                            mToolbar.setTitle(getIntent().getStringExtra("play_list_name"));
                            mToolbar.inflateMenu(R.menu.menu_in_play_list_activity);
                            mMusicItemList = new ArrayList<>();
                            currentListName = getIntent().getStringExtra("play_list_name");
                        }

                        @Override
                        protected Void doInBackground(Void... voids) {
                            //data

                            //get musicId in PlayList
                            Cursor cursor = getContentResolver().query(MediaStore.Audio.Playlists.Members.getContentUri("external", getIntent().getIntExtra("play_list_id", -1))
                                    , null, null, null, MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
                            if (cursor != null && cursor.moveToFirst()) {
                                cursor.moveToFirst();
                                do {

                                    //search music (with audioId)
                                    int audioId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID));
                                    Cursor cursor1 = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.MediaColumns._ID + " = ?", new String[]{String.valueOf(audioId)}, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
                                    if (cursor1 != null && cursor1.moveToFirst()) {
                                        do {
                                            final String mimeType = cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE));
                                            final String name = cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE));
                                            final String albumName = cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                                            final int id = cursor1.getInt(cursor1.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                                            final int size = (int) cursor1.getLong(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                                            final int duration = cursor1.getInt(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                                            final String artist = cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                                            final long addTime = cursor1.getLong(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));
                                            final int albumId = cursor1.getInt(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                                            final String path = cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

                                            final MusicItem.Builder builder = new MusicItem.Builder(id, name, path)
                                                    .musicAlbum(albumName)
                                                    .addTime((int) addTime)
                                                    .artist(artist)
                                                    .duration(duration)
                                                    .mimeName(mimeType)
                                                    .size(size)
                                                    .addAlbumId(albumId);
                                            mMusicItemList.add(builder.build());
                                        } while (cursor1.moveToNext());
                                        cursor1.close();
                                    }

                                } while (cursor.moveToNext());
                                cursor.close();
                            }

                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            MyRecyclerAdapter adapter = new MyRecyclerAdapter(mMusicItemList, PublicActivity.this, TAG);
                            mRecyclerView.setAdapter(adapter);

                            mToolbar.setOnMenuItemClickListener(menuItem -> {
                                switch (menuItem.getItemId()) {
                                    case R.id.menu_play_list_random_play: {
                                        if (Values.CurrentData.CURRENT_PLAY_LIST.equals(getIntent().getStringExtra("play_list_name")))
                                            break;
                                        Values.CurrentData.CURRENT_PLAY_LIST = getIntent().getStringExtra("play_list_name");
                                        Data.sPlayOrderList.clear();
                                        Data.sPlayOrderList.addAll(mMusicItemList);
                                    }
                                }
                                return true;
                            });
                        }
                    }.execute();
                }
                default:
            }
        }

    }

    @Override
    public void onBackPressed() {
        mRecyclerView.stopScroll();
        super.onBackPressed();
    }

    public List<MusicItem> getMusicItemList() {
        return mMusicItemList;
    }

    public String getCurrentListName() {
        return currentListName;
    }

    @Override
    public void initStyle() {
        Utils.Ui.setAppBarColor(this, mAppBarLayout, mToolbar);
    }
}
