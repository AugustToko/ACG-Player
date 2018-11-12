package top.geek_studio.chenlongcould.musicplayer.BroadCasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.util.Log;

import java.io.IOException;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.MusicDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class ReceiverOnMusicPlay extends BroadcastReceiver {
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

                MusicDetailActivity.NotLeakHandler notLeakHandler = musicDetailActivity.getHandler();
                notLeakHandler.sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);
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

            //by auto-next(mediaPlayer OnCompletionListener) of next-play by user, at this time MainActivity is present
            case 4: {
                Data.sMusicBinder.resetMusic();
                int targetIndex = Values.CurrentData.CURRENT_MUSIC_INDEX + 1;
                if (targetIndex > Data.sMusicItems.size() - 1) {
                    targetIndex = 0;
                }


                Values.CurrentData.CURRENT_MUSIC_INDEX = targetIndex;
                Values.MUSIC_PLAYING = true;
                Values.HAS_PLAYED = true;
                Values.CurrentData.CURRENT_SONG_PATH = Data.sMusicItems.get(targetIndex).getMusicPath();

                String path = Data.sMusicItems.get(targetIndex).getMusicPath();
                String musicName = Data.sMusicItems.get(targetIndex).getMusicName();
                String albumName = Data.sMusicItems.get(targetIndex).getMusicAlbum();

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
                    musicDetailActivity.getSeekBar().getThumb().setColorFilter(cover.getPixel(cover.getWidth() / 2, cover.getHeight() / 2), PorterDuff.Mode.SRC_ATOP);

                }

                Data.sCurrentMusicAlbum = albumName;
                Data.sCurrentMusicName = musicName;
                Data.sCurrentMusicBitmap = cover;
                Values.CurrentData.CURRENT_SONG_PATH = path;

                try {
                    Data.sMusicBinder.setDataSource(Data.sMusicItems.get(targetIndex).getMusicPath());
                    Data.sMusicBinder.prepare();
                    Data.sMusicBinder.playMusic();

                    if (Data.sActivities.size() >= 2) {
                        MusicDetailActivity musicDetailActivity = (MusicDetailActivity) Data.sActivities.get(1);
                        MusicDetailActivity.NotLeakHandler notLeakHandler = musicDetailActivity.getHandler();
                        notLeakHandler.sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Data.sMusicBinder.resetMusic();
                }
            }
        }
    }
}
