/*
 * ************************************************************
 * 文件：MyMusicService.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月04日 17:59:25
 * 上次修改时间：2018年12月04日 17:59:10
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.BroadCasts.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Utils.NotificationUtils;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

// FIXME: 2018/11/26 new remote
public final class MyMusicService extends Service {

    private static final String TAG = "MyMusicService";

    private final MediaPlayer mMediaPlayer = new MediaPlayer();

    private final MusicBinder mMusicBinder = new MusicBinder();

    public MyMusicService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Values.SERVICE_RUNNING = true;

        mMediaPlayer.setOnCompletionListener(mp -> {
            if (Data.sNextWillPlayIndex != -1) {
                Utils.SendSomeThing.sendPlay(MyMusicService.this, 7);
                return;
            }

            if (Values.BUTTON_PRESSED) {
                //来自用户的主动点击
                if (Values.CurrentData.CURRENT_PLAY_TYPE.equals(Values.TYPE_RANDOM)) {
                    Utils.SendSomeThing.sendPlay(this, ReceiverOnMusicPlay.TYPE_SHUFFLE);
                } else if (Values.CurrentData.CURRENT_PLAY_TYPE.equals(Values.TYPE_COMMON)) {
                    Utils.SendSomeThing.sendPlay(MyMusicService.this, 4);
                }
            } else {
                switch (Values.CurrentData.CURRENT_AUTO_NEXT_TYPE) {
                    case Values.TYPE_COMMON:
                        Utils.SendSomeThing.sendPlay(MyMusicService.this, 4);
                        break;
                    case Values.TYPE_REPEAT:
                        if (Values.CurrentData.CURRENT_PLAY_LIST != null && !Values.CurrentData.CURRENT_PLAY_LIST.equals("default") && Data.sCurrentMusicList.size() != 0) {
                            if (Values.CurrentData.CURRENT_MUSIC_INDEX == Data.sCurrentMusicList.size() - 1) {
                                Values.CurrentData.CURRENT_MUSIC_INDEX = 0;
                                mMediaPlayer.reset();
                                try {
                                    mMediaPlayer.setDataSource(Data.sCurrentMusicList.get(0));
                                    mMediaPlayer.prepare();
                                    mMediaPlayer.start();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            Utils.SendSomeThing.sendPlay(MyMusicService.this, 4);
                        }
                        break;
                    case Values.TYPE_REPEAT_ONE:
                        mMediaPlayer.start();
                        break;
                }
            }
            Values.BUTTON_PRESSED = false;
        });

        mMediaPlayer.setOnErrorListener((mp, what, extra) -> {
            mp.reset();
            return true;
        });
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: done");
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(NotificationUtils.ID);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Values.BIND_SERVICE = true;
        if (Data.sActivities.size() >= 1)
            ((MainActivity) Data.sActivities.get(0)).getHandler().sendEmptyMessage(Values.HandlerWhat.INIT_FRAGMENT);
        return mMusicBinder;
    }

    public final class MusicBinder extends Binder {

        public final void playMusic() {
            if (Data.sActivities.size() > 0)
                ((MainActivity) Data.sActivities.get(0)).getHandler().sendEmptyMessage(MainActivity.ENABLE_TOUCH);

            Values.MUSIC_PLAYING = true;
            Values.HAS_PLAYED = true;
            mMediaPlayer.start();

            //notification
            Data.notificationUtils.start(Data.notificationUtils.getNot(Data.sCurrentMusicName, Data.sCurrentMusicAlbum, MyMusicService.this));
        }

        public final void stopMusic() {
            Values.MUSIC_PLAYING = false;
            mMediaPlayer.stop();
            Data.notificationUtils.start(Data.notificationUtils.getNot(Data.sCurrentMusicName, Data.sCurrentMusicAlbum, MyMusicService.this));
        }

        public final boolean isPlayingMusic() {
            return mMediaPlayer.isPlaying();
        }

        public final void pauseMusic() {
            mMediaPlayer.pause();
        }

        public final void resetMusic() {
            mMediaPlayer.reset();
        }

        public final void setDataSource(String path) throws IOException {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path);
        }

        public final void prepare() throws IOException {
            mMediaPlayer.prepare();
        }

        public final int getDuration() {
            return mMediaPlayer.getDuration();
        }

        public final int getCurrentPosition() {
            return mMediaPlayer.getCurrentPosition();
        }

        public final void seekTo(int position) {
            mMediaPlayer.seekTo(position);
        }

        public final void release() {
            mMediaPlayer.release();
        }

    }
}
