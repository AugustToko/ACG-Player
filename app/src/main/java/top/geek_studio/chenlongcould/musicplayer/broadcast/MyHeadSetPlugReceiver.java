package top.geek_studio.chenlongcould.musicplayer.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import top.geek_studio.chenlongcould.musicplayer.MusicService;

/**
 * @author chenlongcould
 */
public final class MyHeadSetPlugReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
			ReceiverOnMusicPlay.startService(context, MusicService.ServiceActions.ACTION_PAUSE);
		}
	}
}
