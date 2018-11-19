/*
 * ************************************************************
 * 文件：MyMusicService.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月19日 18:40:42
 * 上次修改时间：2018年11月19日 17:29:14
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class MyMusicService extends Service {

    private static final String TAG = "MyMusicService";
    private MediaPlayer mMediaPlayer = new MediaPlayer();

    private MusicBinder mMusicBinder = new MusicBinder();

    public MyMusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
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
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(Values.PKG_NAME, Values.BroadCast.ReceiverOnMusicPlay));
                    intent.putExtra("play_type", 4);
                    sendBroadcast(intent);
                }
            } else {
                switch (Values.CurrentData.CURRENT_AUTO_NEXT_TYPE) {
                    case Values.TYPE_COMMON:
                        Log.d(TAG, "onCreate: auto-next type_common" + Values.CurrentData.CURRENT_AUTO_NEXT_TYPE);
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName(Values.PKG_NAME, Values.BroadCast.ReceiverOnMusicPlay));
                        intent.putExtra("play_type", 4);
                        sendBroadcast(intent);
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
                            Log.d(TAG, "onCreate: auto-next type_common");
                            Intent intent2 = new Intent();
                            intent2.setComponent(new ComponentName(Values.PKG_NAME, Values.BroadCast.ReceiverOnMusicPlay));
                            intent2.putExtra("play_type", 4);
                            sendBroadcast(intent2);
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
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMusicBinder;
    }

    @Override
    public void onDestroy() {
        Values.SERVICE_RUNNING = false;
        Values.MUSIC_PLAYING = false;
        mMediaPlayer.release();
        Data.sMusicBinder = null;
        Log.d(TAG, "onDestroy: ");
        unregisterReceiver(Data.mMyHeadSetPlugReceiver);
        super.onDestroy();
    }

    public class MusicBinder extends Binder {

        public void playMusic() {
            Values.MUSIC_PLAYING = true;
            Values.HAS_PLAYED = true;
            mMediaPlayer.start();
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
