/*
 * ************************************************************
 * 文件：MyHeadSetPlugReceiver.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:46
 * 上次修改时间：2019年01月17日 17:29:00
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

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
