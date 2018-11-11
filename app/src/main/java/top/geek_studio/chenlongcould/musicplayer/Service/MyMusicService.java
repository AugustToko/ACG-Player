package top.geek_studio.chenlongcould.musicplayer.Service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Utils;
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

        mMediaPlayer.setOnCompletionListener(mp -> {
            if (Values.CURRENT_PLAY_TYPE.equals(Values.TYPE_RANDOM)) {
                if (!Data.sActivities.isEmpty()) {
                    //when mediaPlayer finishes playing, update InfoBar
                    mMediaPlayer.reset();
                    Utils.Audio.shufflePlayback();
                } else if (Values.CURRENT_PLAY_TYPE.equals(Values.TYPE_COMMON)) {
                    Utils.Ui.setNowNotPlaying(this);
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(Values.PKG_NAME, Values.BroadCast.ReceiverOnMusicPlay));
                    intent.putExtra("play_type", 4);
                    sendBroadcast(intent);
            } else {
                Utils.Ui.setNowNotPlaying(this);
                    mMediaPlayer.reset();
                }
            }
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

    }

}
