package top.geek_studio.chenlongcould.musicplayer.BroadCasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.IOException;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.MusicDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

import static top.geek_studio.chenlongcould.musicplayer.Data.mMusicPathList;
import static top.geek_studio.chenlongcould.musicplayer.Data.mSongAlbumList;
import static top.geek_studio.chenlongcould.musicplayer.Data.mSongNameList;

public class ReceiverOnMusicPlay extends BroadcastReceiver {
    private static final String TAG = "ReceiverOnMusicPlay";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "initView: common");

        int type = intent.getIntExtra("play_type", -1);
        switch (type) {
            case -1: {
                return;
            }

            //Type Random
            case 1: {
                Utils.Audio.shufflePlayback();
            }
            break;

            /*
             * must by MusicDetailActivity, just resume play
             * */
            case 2: {
                Data.sMusicBinder.playMusic();
                MusicDetailActivity musicDetailActivity = (MusicDetailActivity) Data.sActivities.get(1);
                MainActivity mainActivity = (MainActivity) Data.sActivities.get(0);
                musicDetailActivity.setButtonTypePlay();
                mainActivity.setButtonTypePlay();
            }
            break;

            /*
             * must by MainActivity, just resume play
             * */
            case 3: {
                Data.sMusicBinder.playMusic();
                MainActivity mainActivity = (MainActivity) Data.sActivities.get(0);
                mainActivity.setButtonTypePlay();
            }
            break;

            //by auto-next(mediaPlayer OnCompletionListener)
            case 4: {
                Data.sMusicBinder.resetMusic();
                int targetIndex = Values.CURRENT_MUSIC_INDEX + 1;
                if (targetIndex > Data.mMusicPathList.size() - 1) {
                    targetIndex = 0;
                }


                Values.CURRENT_MUSIC_INDEX = targetIndex;
                Values.MUSIC_PLAYING = true;
                Values.HAS_PLAYED = true;
                Values.CURRENT_SONG_PATH = Data.mMusicPathList.get(targetIndex);

                String path = mMusicPathList.get(targetIndex);
                String musicName = mSongNameList.get(targetIndex);
                String albumName = mSongAlbumList.get(targetIndex);

                Data.sHistoryPlayIndex.add(targetIndex);

                Bitmap cover = Utils.Audio.getMp3Cover(path);

                Utils.Ui.setNowPlaying();

                if (Data.sActivities.size() >= 1) {
                    MainActivity mainActivity = (MainActivity) Data.sActivities.get(0);
                    mainActivity.setCurrentSongInfo(musicName, albumName, path, cover);
                }

                if (Data.sActivities.size() >= 2) {
                    MusicDetailActivity musicDetailActivity = (MusicDetailActivity) Data.sActivities.get(1);
                    musicDetailActivity.setCurrentSongInfo(musicName, albumName, Utils.Audio.getAlbumByteImage(path));
                }

                Data.sCurrentMusicAlbum = albumName;
                Data.sCurrentMusicName = musicName;
                Data.sCurrentMusicBitmap = cover;
                Values.CURRENT_SONG_PATH = path;

                try {
                    Data.sMusicBinder.setDataSource(Data.mMusicPathList.get(targetIndex));
                    Data.sMusicBinder.prepare();
                    Data.sMusicBinder.playMusic();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
