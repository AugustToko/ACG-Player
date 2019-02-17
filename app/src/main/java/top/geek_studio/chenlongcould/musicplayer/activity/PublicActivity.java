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
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.broadcasts.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.fragment.PlayListFragment;
import top.geek_studio.chenlongcould.musicplayer.utils.MusicUtil;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

public class PublicActivity extends MyBaseCompatActivity {

    public static final String TAG = "PublicActivity";

    public static final String INTENT_START_BY = "start_by";

    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private MyRecyclerAdapter adapter;

    /**
     * 保存播放列表下的Music (如果当前type是play_list_item的话 {@link #mType} )
     */
    private List<MusicItem> mMusicItemList;

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
                    } else {
                        // TODO: 2018/11/22 quickSort
                        System.exit(0);
                    }
                    adapter = new MyRecyclerAdapter(musicItems, this, TAG);
                    mRecyclerView.setAdapter(adapter);
                }
                break;

                case PlayListFragment.ACTION_FAVOURITE: {
                    mToolbar.setTitle(getResources().getString(R.string.my_favourite));

                    mMusicItemList = new ArrayList<>();

                    int id = MusicUtil.getFavoritesPlaylist(this).getId();
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
                                    if (i != 0) return;
                                    adapter = new MyRecyclerAdapter(mMusicItemList, PublicActivity.this, TAG + PlayListFragment.ACTION_FAVOURITE);
                                    mRecyclerView.setAdapter(adapter);

                                    mToolbar.setOnMenuItemClickListener(menuItem -> {
                                        switch (menuItem.getItemId()) {
                                            case R.id.menu_random_play: {
                                                Data.sPlayOrderList.clear();
                                                Data.sPlayOrderList.addAll(mMusicItemList);        //更新数据
                                                Collections.shuffle(Data.sPlayOrderList);
                                                Utils.SendSomeThing.sendPlay(PublicActivity.this, ReceiverOnMusicPlay.TYPE_SHUFFLE, TAG + PlayListFragment.ACTION_FAVOURITE);
                                            }
                                            break;
                                        }
                                        return false;
                                    });

                                });
                    }
                }
                break;

                //点击播放列表中的一项
                case PlayListFragment.ACTION_PLAY_LIST_ITEM: {

                    mToolbar.setTitle(getIntent().getStringExtra("play_list_name"));
                    mToolbar.inflateMenu(R.menu.menu_in_play_list_activity);
                    mMusicItemList = new ArrayList<>();
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
                                if (i != 0) return;
                                adapter = new MyRecyclerAdapter(mMusicItemList, PublicActivity.this, TAG + "PlayList");
                                mRecyclerView.setAdapter(adapter);

                                mToolbar.setOnMenuItemClickListener(menuItem -> {
                                    switch (menuItem.getItemId()) {
                                        case R.id.menu_random_play: {
                                            Data.sPlayOrderList.clear();
                                            Data.sPlayOrderList.addAll(mMusicItemList);        //更新数据
                                            if (PreferenceManager.getDefaultSharedPreferences(PublicActivity.this).getString(Values.SharedPrefsTag.ORDER_TYPE, Values.TYPE_COMMON).equals(Values.TYPE_RANDOM))
                                                Collections.shuffle(Data.sPlayOrderList);
                                            Utils.SendSomeThing.sendPlay(PublicActivity.this, ReceiverOnMusicPlay.TYPE_SHUFFLE, TAG);
                                        }
                                        break;
                                    }
                                    return false;
                                });

                            });
                }
                break;

                case PlayListFragment.ACTION_HISTORY: {
                    mToolbar.setTitle(getString(R.string.history));
                    mToolbar.inflateMenu(R.menu.menu_public_trash_can);
                    mRecyclerView.setAdapter(new MyRecyclerAdapter(Data.sHistoryPlay, PublicActivity.this, TAG));
                }
                break;

                case PlayListFragment.ACTION_TRASH_CAN: {
                    mToolbar.setTitle(getString(R.string.trash_can));
                    mToolbar.inflateMenu(R.menu.menu_public_trash_can);
                    mRecyclerView.setAdapter(new MyRecyclerAdapter(Data.sTrashCanList, PublicActivity.this, TAG));
                }
                break;
                default:
            }
        }

    }

    @Override
    protected String getActivityTAG() {
        return TAG;
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null && !mDisposable.isDisposed()) mDisposable.dispose();
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
