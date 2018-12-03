/*
 * ************************************************************
 * 文件：MyMusicService.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月03日 15:10:53
 * 上次修改时间：2018年12月03日 15:10:19
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.NotificationUtils;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

// FIXME: 2018/11/26 new remote
public final class MyMusicService extends Service {

    private static final String TAG = "MyMusicService";

    private final MediaPlayer mMediaPlayer = new MediaPlayer();

    private final MusicBinder mMusicBinder = new MusicBinder();

    private NotificationUtils notificationUtils;

    public MyMusicService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Values.SERVICE_RUNNING = true;

        mMediaPlayer.setOnCompletionListener(mp -> {
            if (Data.sNextWillPlayIndex != -1) {
                Utils.Audio.doesNextHasMusic();
                return;
            }

            if (Values.BUTTON_PRESSED) {
                //来自用户的主动点击
                if (Values.CurrentData.CURRENT_PLAY_TYPE.equals(Values.TYPE_RANDOM)) {
                    Utils.Audio.shufflePlayback();
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
    public IBinder onBind(Intent intent) {
        if (Data.sActivities.size() >= 1)
            ((MainActivity) Data.sActivities.get(0))
                    .getHandler()
                    .sendEmptyMessage(Values.HandlerWhat.ON_SERVICE_START);
        return mMusicBinder;
    }

    public final class MusicBinder extends Binder {

        public final void playMusic() {
//            Log.d(TAG, "playMusic: ");
            if (Data.sActivities.size() > 0)
                ((MainActivity) Data.sActivities.get(0)).getHandler().sendEmptyMessage(MainActivity.ENABLE_TOUCH);

            Values.MUSIC_PLAYING = true;
            Values.HAS_PLAYED = true;
            mMediaPlayer.start();
            notificationUtils = new NotificationUtils(MyMusicService.this, "Now Playing...");
            notificationUtils.start(notificationUtils.getNot(Data.sCurrentMusicName, Data.sCurrentMusicAlbum, R.drawable.ic_pause_white_24dp, MyMusicService.this));
        }

        public final void stopMusic() {
            Values.MUSIC_PLAYING = false;
            mMediaPlayer.stop();
        }

        public final boolean isPlayingMusic() {
//            Log.d(TAG, "isPlayingMusic: ");
            return mMediaPlayer.isPlaying();
        }

        public final void pauseMusic() {
            Log.d(TAG, "pauseMusic: ");
            mMediaPlayer.pause();
            notificationUtils = new NotificationUtils(MyMusicService.this, "Now Playing...");
            notificationUtils.start(notificationUtils.getNot(Data.sCurrentMusicName, Data.sCurrentMusicAlbum, R.drawable.ic_play_arrow_black_24dp, MyMusicService.this));
        }

        public final void resetMusic() {
//            Log.d(TAG, "resetMusic: ");
            mMediaPlayer.reset();
        }

        public final void setDataSource(String path) {
//            Log.d(TAG, "setDataSource: ");
            mMediaPlayer.reset();
            try {
                mMediaPlayer.setDataSource(path);
            } catch (IOException e) {
                Toast.makeText(MyMusicService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        public final void prepare() throws IOException {
//            Log.d(TAG, "prepare: ");
            mMediaPlayer.prepare();
        }

        public final int getDuration() {
//            Log.d(TAG, "getDuration: ");
            return mMediaPlayer.getDuration();
        }

        public final int getCurrentPosition() {
//            Log.d(TAG, "getCurrentPosition: ");
            return mMediaPlayer.getCurrentPosition();
        }

        public final void seekTo(int position) {
//            Log.d(TAG, "seekTo: ");
            mMediaPlayer.seekTo(position);
        }

        public final void release() {
            mMediaPlayer.release();
        }

    }
}
