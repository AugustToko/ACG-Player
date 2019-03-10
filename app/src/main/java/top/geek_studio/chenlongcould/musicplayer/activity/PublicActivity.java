/*
 * ************************************************************
 * 文件：PublicActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月27日 13:11:38
 * 上次修改时间：2019年01月27日 13:08:44
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.activity;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.Models.PlayListItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.broadcast.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.fragment.PlayListFragment;
import top.geek_studio.chenlongcould.musicplayer.utils.MusicUtil;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

/**
 * @author chenlongcould
 */
public final class PublicActivity extends BaseCompatActivity {

    public static final String TAG = "PublicActivity";

    public static final String INTENT_START_BY = "start_by";

    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private MyRecyclerAdapter adapter;

    /**
     * 保存播放列表下的Music (如果当前type是play_list_item的话 {@link #mType} )
     */
    private List<MusicItem> mMusicItemList = new ArrayList<>();

    /**
     * save current playlist name, if current type is play_list_item {@link #mType}
     */
    private String currentListName;

    /**
     * different type enter different UI(Activity)
     */
    private String mType;

    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_add_recent);
        mRecyclerView = findViewById(R.id.activity_add_recent_recycler);
        mAppBarLayout = findViewById(R.id.app_bar_layout);
        mToolbar = findViewById(R.id.toolbar);

        inflateCommonMenu();

        super.initView(mToolbar, mAppBarLayout);
        super.onCreate(savedInstanceState);

        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mType = getIntent().getStringExtra(INTENT_START_BY);

        if (mType != null) {
            switch (mType) {
                case PlayListFragment.ACTION_ADD_RECENT: {
                    mToolbar.setTitle(getResources().getString(R.string.add_recent));

                    ArrayList<MusicItem> musicItems = new ArrayList<>(Data.sMusicItems);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        musicItems.sort((o1, o2) -> {
                            if (o1 == null || o2 == null) {
                                return 0;
                            }
                            return Integer.compare(o1.getAddTime(), o2.getAddTime());
                        });
                    }

                    adapter = new MyRecyclerAdapter(this, musicItems);
                    mRecyclerView.setAdapter(adapter);
                }
                break;

                case PlayListFragment.ACTION_FAVOURITE: {
                    mToolbar.setTitle(getResources().getString(R.string.my_favourite));

                    PlayListItem playListItem = MusicUtil.getFavoritesPlaylist(this);

                    if (playListItem != null) {
                        int id = playListItem.getId();
                        if (id != -1) {
                            mDisposable = Observable.create((ObservableOnSubscribe<Integer>) observableEmitter -> {
                                //data

                                //get musicId in PlayList
                                final Cursor cursor = getContentResolver().query(MediaStore.Audio.Playlists.Members.getContentUri("external", id)
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
                                                final int musicId = cursor1.getInt(cursor1.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                                                final int size = (int) cursor1.getLong(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                                                final int duration = cursor1.getInt(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                                                final String artist = cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                                                final long addTime = cursor1.getLong(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));
                                                final int albumId = cursor1.getInt(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                                                final String path = cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

                                                final MusicItem.Builder builder = new MusicItem.Builder(musicId, name, path)
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

                                observableEmitter.onNext(0);
                            }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(i -> {
                                        if (i != 0) {
                                            return;
                                        }
                                        adapter = new MyRecyclerAdapter(PublicActivity.this, mMusicItemList);
                                        mRecyclerView.setAdapter(adapter);
                                    });
                        }
                    } else {
                        Toast.makeText(this, "ID is null...", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

                //点击播放列表中的一项
                case PlayListFragment.ACTION_PLAY_LIST_ITEM: {
                    mToolbar.setTitle(getIntent().getStringExtra("play_list_name"));

                    currentListName = getIntent().getStringExtra("play_list_name");

                    mDisposable = Observable.create((ObservableOnSubscribe<Integer>) observableEmitter -> {
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

                        observableEmitter.onNext(0);
                    }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                            .subscribe(i -> {
                                if (i != 0) {
                                    return;
                                }
                                adapter = new MyRecyclerAdapter(PublicActivity.this, mMusicItemList);
                                mRecyclerView.setAdapter(adapter);
                            });
                }
                break;

                case PlayListFragment.ACTION_HISTORY: {
                    mToolbar.setTitle(getString(R.string.history));
                    mRecyclerView.setAdapter(new MyRecyclerAdapter(PublicActivity.this, Data.sHistoryPlay));
                }
                break;

                case PlayListFragment.ACTION_TRASH_CAN: {
                    mToolbar.setTitle(getString(R.string.trash_can));
                    mRecyclerView.setAdapter(new MyRecyclerAdapter(PublicActivity.this, Data.sTrashCanList));
                }
                break;
                default:
            }
        }

    }

    @Override
    public String getActivityTAG() {
        return TAG;
    }

    @Override
    public void inflateCommonMenu() {
        mToolbar.getMenu().clear();
        mToolbar.inflateMenu(R.menu.menu_public);
        mToolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_public_random: {
                    Data.sNextWillPlayItem = mMusicItemList.get(new Random().nextInt(mMusicItemList.size()));
                    Utils.SendSomeThing.sendPlay(PublicActivity.this, 6, ReceiverOnMusicPlay.TYPE_NEXT);
                }
                break;

                case R.id.menu_public_m3u: {

                }
                break;
                default:
            }
            return true;
        });
    }

    @Override
    public void inflateChooseMenu() {
        mToolbar.getMenu().clear();
        mToolbar.inflateMenu(R.menu.menu_toolbar_main_choose);
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public List<MusicItem> getMusicItemList() {
        return mMusicItemList;
    }

    public MyRecyclerAdapter getAdapter() {
        return adapter;
    }
}
