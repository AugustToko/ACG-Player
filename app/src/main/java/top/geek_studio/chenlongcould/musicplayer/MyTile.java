/*
 * ************************************************************
 * 文件：MyTile.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:46
 * 上次修改时间：2019年01月17日 17:29:00
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;

import top.geek_studio.chenlongcould.musicplayer.BroadCasts.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;

@RequiresApi(Build.VERSION_CODES.N)
public class MyTile extends TileService {

    private boolean mEnable = false;

    @Override
    public void onClick() {
        if (!mEnable) {
            mEnable = true;
            getQsTile().setState(Tile.STATE_ACTIVE);
            getQsTile().setLabel("Playing...");
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_audiotrack_24px));
            getQsTile().updateTile();
            Utils.SendSomeThing.sendPlay(this, ReceiverOnMusicPlay.TYPE_SHUFFLE, null);
        } else {
            mEnable = false;
            getQsTile().setState(Tile.STATE_INACTIVE);
            getQsTile().setLabel(getString(R.string.fast_play));
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_audiotrack_24px));
            getQsTile().updateTile();
            Utils.SendSomeThing.sendPause(this);
        }
    }

    @Override
    public void onDestroy() {
        getQsTile().setState(Tile.STATE_INACTIVE);
        getQsTile().setLabel(getString(R.string.fast_play));
        getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_audiotrack_24px));
        getQsTile().updateTile();
        super.onDestroy();
    }
}
