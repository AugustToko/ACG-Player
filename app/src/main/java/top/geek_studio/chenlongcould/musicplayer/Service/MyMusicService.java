/*
 * ************************************************************
 * 文件：MyMusicService.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月21日 20:55:27
 * 上次修改时间：2018年11月21日 20:48:43
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.NotificationUtils;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class MyMusicService extends Service {

    private static final String TAG = "MyMusicService";

    private MediaPlayer mMediaPlayer = new MediaPlayer();

    private MusicBinder mMusicBinder = new MusicBinder();

    private NotificationUtils notificationUtils;

    public MyMusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Values.SERVICE_RUNNING = true;

        //监听耳机(有线或无线)的插拔动作, 拔出暂停音乐
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(Data.mMyHeadSetPlugReceiver, intentFilter);

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
                            Log.w(TAG, "onCreate: auto-next enter type_repeat", null);
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
        return mMusicBinder;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        unregisterReceiver(Data.mMyHeadSetPlugReceiver);
        Values.SERVICE_RUNNING = false;
        Values.MUSIC_PLAYING = false;
        mMediaPlayer.release();
        Data.sMusicBinder = null;
        if (notificationUtils != null) {
            notificationUtils.disMiss(NotificationUtils.ID);
        }
        super.onDestroy();
    }

    public class MusicBinder extends Binder {

        public void playMusic() {
            Values.MUSIC_PLAYING = true;
            Values.HAS_PLAYED = true;
            mMediaPlayer.start();
            notificationUtils = new NotificationUtils(MyMusicService.this, "Now Playing...");
            notificationUtils.start(notificationUtils.getNot(Data.sCurrentMusicName, Data.sCurrentMusicAlbum, R.drawable.ic_pause_white_24dp, MyMusicService.this));
        }

        public void stopMusic() {
            Values.MUSIC_PLAYING = false;
            mMediaPlayer.stop();
        }

        public boolean isPlayingMusic() {
            return mMediaPlayer.isPlaying();
        }

        public void pauseMusic() {
            mMediaPlayer.pause();
            notificationUtils = new NotificationUtils(MyMusicService.this, "Now Playing...");
            notificationUtils.start(notificationUtils.getNot(Data.sCurrentMusicName, Data.sCurrentMusicAlbum, R.drawable.ic_play_arrow_black_24dp, MyMusicService.this));
        }

        public void resetMusic() {
            mMediaPlayer.reset();
        }

        public void setDataSource(String path) throws IOException {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path);
        }

        public void prepare() throws IOException {
            mMediaPlayer.prepare();
        }

        public int getDuration() {
            return mMediaPlayer.getDuration();
        }

        public int getCurrentPosition() {
            return mMediaPlayer.getCurrentPosition();
        }

        public void seekTo(int position) {
            mMediaPlayer.seekTo(position);
        }

        public void release() {
            mMediaPlayer.release();
        }

    }
}
