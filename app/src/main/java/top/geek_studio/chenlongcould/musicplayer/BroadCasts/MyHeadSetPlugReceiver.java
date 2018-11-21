/*
 * ************************************************************
 * 文件：MyHeadSetPlugReceiver.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月21日 11:01:53
 * 上次修改时间：2018年11月21日 11:01:41
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.BroadCasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;

public final class MyHeadSetPlugReceiver extends BroadcastReceiver {

    private static final String TAG = "MyHeadSetPlugReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");
        String action = intent.getAction();
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
            //暂停操作
            Utils.SendSomeThing.sendPause(context);
        }
    }
}
