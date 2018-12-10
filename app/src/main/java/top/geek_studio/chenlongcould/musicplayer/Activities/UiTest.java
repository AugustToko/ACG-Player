/*
 * ************************************************************
 * 文件：UiTest.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月10日 14:49:08
 * 上次修改时间：2018年12月07日 11:25:12
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import top.geek_studio.chenlongcould.musicplayer.CustomView.MyCardView;
import top.geek_studio.chenlongcould.musicplayer.R;

public class UiTest extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui_test);
        MyCardView cardView = findViewById(R.id.card_view);

    }
}
