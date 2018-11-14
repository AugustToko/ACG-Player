package top.geek_studio.chenlongcould.musicplayer.BroadCasts;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import top.geek_studio.chenlongcould.musicplayer.Values;

public class MyHeadSetPlugReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
            //暂停操作
            Intent intent1 = new Intent();
            intent1.setComponent(new ComponentName(Values.PKG_NAME, Values.BroadCast.ReceiverOnMusicPause));
            context.sendBroadcast(intent1);
        }
    }
}
