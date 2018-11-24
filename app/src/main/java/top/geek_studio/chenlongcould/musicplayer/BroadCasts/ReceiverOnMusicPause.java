/*
 * ************************************************************
 * 文件：ReceiverOnMusicPause.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月24日 17:50:10
 * 上次修改时间：2018年11月23日 19:04:07
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.BroadCasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import top.geek_studio.chenlongcould.musicplayer.Activities.MusicDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class ReceiverOnMusicPause extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Values.TAG_UNIVERSAL_ONE, "onReceive: on pause");
        Data.sMusicBinder.pauseMusic();
        if (Data.sActivities.size() != 0) {
            Values.MUSIC_PLAYING = false;

            Utils.HandlerSend.sendToMain(Values.HandlerWhat.SET_MAIN_BUTTON_PAUSE);
            if (Data.sActivities.size() >= 2) {
                MusicDetailActivity musicDetailActivity = (MusicDetailActivity) Data.sActivities.get(1);
                musicDetailActivity.setButtonTypePause();
            }
        }
    }
}
