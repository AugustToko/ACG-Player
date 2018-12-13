/*
 * ************************************************************
 * 文件：ReceiverOnMusicPause.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月13日 10:03:03
 * 上次修改时间：2018年12月12日 17:04:01
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

import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class ReceiverOnMusicPause extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Values.TAG_UNIVERSAL_ONE, "onReceive: on pause");
        ReceiverOnMusicPlay.pauseMusic();
        Utils.Ui.setPlayButtonNowPause();
    }
}
