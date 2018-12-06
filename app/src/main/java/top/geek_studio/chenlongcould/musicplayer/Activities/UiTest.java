/*
 * ************************************************************
 * 文件：UiTest.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月06日 19:19:07
 * 上次修改时间：2018年12月06日 11:43:12
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import top.geek_studio.chenlongcould.musicplayer.MyCardView;
import top.geek_studio.chenlongcould.musicplayer.R;

public class UiTest extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui_test);
        MyCardView cardView = findViewById(R.id.card_view);

    }
}
