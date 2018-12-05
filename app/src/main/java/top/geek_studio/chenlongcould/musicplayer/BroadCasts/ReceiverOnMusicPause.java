/*
 * ************************************************************
 * 文件：ReceiverOnMusicPause.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月05日 09:30:08
 * 上次修改时间：2018年12月05日 08:27:23
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

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Utils.NotificationUtils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class ReceiverOnMusicPause extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Values.TAG_UNIVERSAL_ONE, "onReceive: on pause");
        Data.sMusicBinder.pauseMusic();
        if (Data.sActivities.size() != 0) {
            Values.MUSIC_PLAYING = false;
            MainActivity activity = (MainActivity) Data.sActivities.get(0);
            activity.getMusicDetailFragment().getHandler().sendEmptyMessage(Values.HandlerWhat.SET_BUTTON_PAUSE);
        }

        if (Data.sMyMusicService != null) {
            Data.sMyMusicService.startForeground(NotificationUtils.ID, Data.notificationUtils
                    .getNot(Data.sCurrentMusicName, Data.sCurrentMusicAlbum, context));
        }
    }
}
