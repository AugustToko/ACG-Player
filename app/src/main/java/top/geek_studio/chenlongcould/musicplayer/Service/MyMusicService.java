package top.geek_studio.chenlongcould.musicplayer.Service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
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
            if (!Data.sActivities.isEmpty()) {
                //when mediaPlayer finishes playing, update InfoBar
                Utils.Ui.setNowNotPlaying((MainActivity) Data.sActivities.get(0));
            }
            Values.MUSIC_COMPLETION = true;
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMusicBinder;
    }

    @Override
    public void onDestroy() {
        Values.SERVICE_RUNNING = false;
        Values.NOW_PLAYING = false;
        mMediaPlayer.release();
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    public class MusicBinder extends Binder {

        public void playMusic() {
            if (!Values.HAS_PLAYED) {
                Values.HAS_PLAYED = true;

                //使背景变黑, 使图片过渡更自然
                Utils.Ui.setInfoBarBackgroundBlack();

                Log.d(TAG, "playMusic: has played");
            }
            Values.MUSIC_PLAYING = true;

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
            Values.MUSIC_PLAYING = false;
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
    }

}
