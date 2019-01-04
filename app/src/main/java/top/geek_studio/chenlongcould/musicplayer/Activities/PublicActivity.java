/*
 * ************************************************************
 * 文件：PublicActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月04日 20:36:03
 * 上次修改时间：2019年01月04日 19:14:30
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.musicplayer.Adapters.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.BroadCasts.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Interface.IStyle;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public class PublicActivity extends AppCompatActivity implements IStyle {

    public static final String TAG = "PublicActivity";

    public static final String PLAY_LIST_ITEM = "play_list_item";

    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;

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

        mType = getIntent().getStringExtra("start_by");

        if (mType != null) {
            switch (mType) {
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
                    mToolbar.setTitle(getResources().getString(R.string.my_favourite));

                    mMusicItemList = new ArrayList<>();

                    SharedPreferences mDef = PreferenceManager.getDefaultSharedPreferences(this);
                    int id = mDef.getInt(Values.SharedPrefsTag.FAVOURITE_LIST_ID, -1);
                    if (id != -1) {
                        Observable.create((ObservableOnSubscribe<Integer>) observableEmitter -> {
                            //data

                            //get musicId in PlayList
                            Cursor cursor = getContentResolver().query(MediaStore.Audio.Playlists.Members.getContentUri("external", id)
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
                                    MyRecyclerAdapter adapter = new MyRecyclerAdapter(mMusicItemList, PublicActivity.this, TAG + "favourite");
                                    mRecyclerView.setAdapter(adapter);

                                    mToolbar.setOnMenuItemClickListener(menuItem -> {
                                        switch (menuItem.getItemId()) {
                                            case R.id.menu_random_play: {
                                                Data.sPlayOrderList.clear();
                                                Data.sPlayOrderList.addAll(mMusicItemList);        //更新数据
                                                Collections.shuffle(Data.sPlayOrderList);
                                                Utils.SendSomeThing.sendPlay(PublicActivity.this, ReceiverOnMusicPlay.TYPE_SHUFFLE, TAG);
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
                case PLAY_LIST_ITEM: {

                    mToolbar.setTitle(getIntent().getStringExtra("play_list_name"));
                    mToolbar.inflateMenu(R.menu.menu_in_play_list_activity);
                    mMusicItemList = new ArrayList<>();
                    currentListName = getIntent().getStringExtra("play_list_name");

                    Observable.create((ObservableOnSubscribe<Integer>) observableEmitter -> {
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
                                MyRecyclerAdapter adapter = new MyRecyclerAdapter(mMusicItemList, PublicActivity.this, TAG + "PlayList");
                                mRecyclerView.setAdapter(adapter);

                                mToolbar.setOnMenuItemClickListener(menuItem -> {
                                    switch (menuItem.getItemId()) {
                                        case R.id.menu_random_play: {
                                            Data.sPlayOrderList.clear();
                                            Data.sPlayOrderList.addAll(mMusicItemList);        //更新数据
                                            if (Values.CurrentData.CURRENT_PLAY_TYPE.equals(Values.TYPE_RANDOM))
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

                case "play history": {
                    mToolbar.setTitle(getString(R.string.history));
                    mRecyclerView.setAdapter(new MyRecyclerAdapter(Data.sHistoryPlay, PublicActivity.this, TAG));
                }
                default:
            }
        }

    }

    @Override
    public void onBackPressed() {
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
        Utils.Ui.setTopBottomColor(this, mAppBarLayout, mToolbar);
    }
}
