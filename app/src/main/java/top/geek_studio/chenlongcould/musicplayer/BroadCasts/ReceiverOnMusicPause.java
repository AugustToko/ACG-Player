package top.geek_studio.chenlongcould.musicplayer.BroadCasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.MusicDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Values;

public class ReceiverOnMusicPause extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Values.TAG_UNIVERSAL_ONE, "onReceive: on pause");

        if (Data.sActivities.size() != 0) {

            MainActivity mainActivity = (MainActivity) Data.sActivities.get(0);

            Data.sMusicBinder.pauseMusic();
            Values.MUSIC_PLAYING = false;

            mainActivity.setButtonTypePause();

            if (Data.sActivities.size() >= 2) {

                MusicDetailActivity musicDetailActivity = (MusicDetailActivity) Data.sActivities.get(1);
                musicDetailActivity.setButtonTypePause();
            }
        }
    }
}
