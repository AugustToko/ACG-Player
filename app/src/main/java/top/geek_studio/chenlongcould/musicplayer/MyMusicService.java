package top.geek_studio.chenlongcould.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class MyMusicService extends Service {

    private static final String TAG = "MyMusicService";
    private MediaPlayer mMediaPlayer = new MediaPlayer();

    private MusicBinder mMusicBinder = new MusicBinder();

    public MyMusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer.setOnCompletionListener(mp -> {
            if (!Data.sActivities.isEmpty()) {
                //播放完成自带更新状态
                ((MainActivity) Data.sActivities.get(0)).setCurrentSongInfoStop();
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
        super.onDestroy();
        mMediaPlayer.release();
        Log.d(TAG, "onDestroy: ");
    }

    class MusicBinder extends Binder {
        MediaPlayer getMediaPlayer() {
            return mMediaPlayer;
        }

        void playMusic() {
            Values.HAS_PLAYED = true;
            mMediaPlayer.start();
        }

        void stopMusic() {
            mMediaPlayer.stop();
        }

        boolean isPlayingMusic() {
            return mMediaPlayer.isPlaying();
        }

        void pauseMusic() {
            mMediaPlayer.pause();
        }

        void resetMusic() {
            mMediaPlayer.reset();
        }

        void setDataSource(String path) throws IOException {
            mMediaPlayer.setDataSource(path);
        }

        void prepare() throws IOException {
            mMediaPlayer.prepare();
        }

        int getDuration() {
            return mMediaPlayer.getDuration();
        }

        int getCurrentPosition() {
            return mMediaPlayer.getCurrentPosition();
        }
    }

}
