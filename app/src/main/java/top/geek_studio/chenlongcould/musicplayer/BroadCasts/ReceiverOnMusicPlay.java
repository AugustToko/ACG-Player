package top.geek_studio.chenlongcould.musicplayer.BroadCasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.MusicDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Utils;

public class ReceiverOnMusicPlay extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int type = intent.getIntExtra("play_type", -1);
        switch (type) {
            case -1: {
                return;
            }

            case 1: {
                Utils.Audio.shufflePlayback();
            }
            break;

            //by MusicDetailActivity
            case 2: {
                Data.sMusicBinder.playMusic();
                MusicDetailActivity musicDetailActivity = (MusicDetailActivity) Data.sActivities.get(1);
                MainActivity mainActivity = (MainActivity) Data.sActivities.get(0);
                musicDetailActivity.setButtonTypePlay();
                mainActivity.setButtonTypePlay();
            }
            break;

            case 3: {
                Data.sMusicBinder.playMusic();
                MainActivity mainActivity = (MainActivity) Data.sActivities.get(0);
                mainActivity.setButtonTypePlay();
            }
        }
    }
}
