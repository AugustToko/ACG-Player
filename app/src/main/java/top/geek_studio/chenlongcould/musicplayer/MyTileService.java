/*
 * ************************************************************
 * 文件：MyTileService.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月18日 18:58:29
 * 上次修改时间：2019年01月18日 18:57:39
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.Collections;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.broadcasts.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

@RequiresApi(Build.VERSION_CODES.N)
public class MyTileService extends TileService {

    private static final String TAG = "MyTileService";

    private boolean mEnable = false;

    private Disposable mDisposable;

    public MyTileService() {
        super();
        Log.d(TAG, "MyTileService: ");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (ContextCompat.checkSelfPermission(MyTileService.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            loadData();
            if (!Data.HAS_BIND) {
                Intent intent = new Intent(this, MusicService.class);
                startService(intent);
                Data.HAS_BIND = bindService(intent, Data.sServiceConnection, BIND_AUTO_CREATE);
            }
        } else {
            Toast.makeText(this, "Need Permission, please open the app...", Toast.LENGTH_SHORT).show();
        }

    }

    public void loadData() {
        mDisposable = Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            if (Data.sMusicItems.isEmpty()) {
                /*---------------------- init Data!!!! -------------------*/
                final Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
                if (cursor != null && cursor.moveToFirst()) {
                    //没有歌曲直接退出app
                    if (cursor.getCount() == 0) {
                        emitter.onNext(-2);
                    } else {

                        final boolean skipShort = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Values.SharedPrefsTag.HIDE_SHORT_SONG, true);

                        do {
                            final String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                            final File file = new File(path);
                            if (!file.exists()) {
                                Log.e(TAG, "onAttach: song file: " + path + " does not exits, skip this!!!");
                                continue;
                            }

                            final int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                            if (duration <= 0) {
                                Log.d(TAG, "onCreate: the music-file duration is " + duration + ", skip...");
                                continue;
                            }
                            if (skipShort && duration < 10) {
                                continue;
                            }

                            final String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
                            final String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                            final String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                            final int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                            final int size = (int) cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                            final String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                            final long addTime = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));
                            final int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

                            final MusicItem.Builder builder = new MusicItem.Builder(id, name, path)
                                    .musicAlbum(albumName)
                                    .addTime((int) addTime)
                                    .artist(artist)
                                    .duration(duration)
                                    .mimeName(mimeType)
                                    .size(size)
                                    .addAlbumId(albumId);

                            Data.sMusicItems.add(builder.build());
                            Data.sMusicItemsBackUp.add(builder.build());
                            Data.sPlayOrderList.add(builder.build());

                        } while (cursor.moveToNext());

                        Log.i(Values.TAG_UNIVERSAL_ONE, "onCreate: The MusicData load done.");
                        cursor.close();

                        if (PreferenceManager.getDefaultSharedPreferences(this).getString(Values.SharedPrefsTag.ORDER_TYPE, Values.TYPE_COMMON).equals(Values.TYPE_RANDOM))
                            Collections.shuffle(Data.sPlayOrderList);

                        emitter.onNext(0);
                    }
                } else {
                    //cursor null or getCount == 0
                    emitter.onNext(-1);
                }
            } else {
                //already has data -> initFragment
                emitter.onNext(0);
            }

        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(result -> {
            if (result == -1) {
                Utils.Ui.fastToast(MyTileService.this, "cursor is null or moveToFirst Fail");
                return;
            }
            if (result == -2) {
                Utils.Ui.fastToast(MyTileService.this, "Can not find any music!");
                return;
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: ");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind: ");
        super.onRebind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved: ");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onClick() {
        if (Data.sPlayOrderList.size() == 0) {
            if (!mEnable) {
                mEnable = true;
                getQsTile().setState(Tile.STATE_ACTIVE);
                getQsTile().setLabel("Playing...");
                getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_audiotrack_24px));
                getQsTile().updateTile();
                Utils.SendSomeThing.sendPlay(this, ReceiverOnMusicPlay.CASE_TYPE_SHUFFLE, null);
            } else {
                mEnable = false;
                getQsTile().setState(Tile.STATE_INACTIVE);
                getQsTile().setLabel(getString(R.string.fast_play));
                getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_audiotrack_24px));
                getQsTile().updateTile();
                try {
                    Data.sMusicBinder.stopMusic();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(this, "Music data loading...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        getQsTile().setState(Tile.STATE_INACTIVE);
        getQsTile().setLabel(getString(R.string.fast_play));
        getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_audiotrack_24px));
        getQsTile().updateTile();

        if (mDisposable != null && !mDisposable.isDisposed()) mDisposable.dispose();
        if (Data.HAS_BIND) {
            try {
                unbindService(Data.sServiceConnection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (Data.sActivities.size() == 0) {
            MainActivity.clearData();
        }

        super.onDestroy();
    }

    @Override
    public void onStartListening() {
        Log.d(TAG, "onStartListening: ");
        super.onStartListening();
    }

    @Override
    public void onStopListening() {
        Log.d(TAG, "onStopListening: ");
        super.onStopListening();
    }

    @Override
    public void onTileAdded() {
        Log.d(TAG, "onTileAdded: ");
        super.onTileAdded();
    }

    @Override
    public void onTileRemoved() {
        Log.d(TAG, "onTileRemoved: ");
        super.onTileRemoved();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        return super.onBind(intent);
    }
}
