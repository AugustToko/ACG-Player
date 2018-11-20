/*
 * ************************************************************
 * 文件：PublicActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月20日 21:06:43
 * 上次修改时间：2018年11月20日 07:30:05
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.os.Bundle;

import top.geek_studio.chenlongcould.musicplayer.R;

public class PublicActivity extends MyBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recent);

        String type = getIntent().getStringExtra("start_by");
        switch (type) {
            case "add recent": {
                // TODO: 2018/11/11  add recent
            }
            break;
            case "favourite music": {
                // TODO: 2018/11/11  favourite music

            }
            break;
            default:
        }
    }
}
