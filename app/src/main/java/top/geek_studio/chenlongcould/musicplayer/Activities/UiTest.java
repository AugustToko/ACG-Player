/*
 * ************************************************************
 * 文件：UiTest.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月12日 11:57:29
 * 上次修改时间：2018年12月12日 11:57:09
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.ImageView;

import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.R;

public class UiTest extends AppCompatActivity {

    float mLastX = 0;
    float mLastY = 0;
    float moveX = 0;
    float moveY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui_test);
        ImageView imageView = findViewById(R.id.image_bg);
        ImageView blurView = findViewById(R.id.blur_view);
        GlideApp.with(this).load(Data.sCurrentMusicBitmap).into(imageView);
        blurView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    moveX = event.getX();
                    moveY = event.getY();
                    mLastX = event.getRawX();
                    mLastY = event.getRawY();
                }
                break;

                case MotionEvent.ACTION_MOVE: {
                    float val = blurView.getX() + (event.getX() - moveX);
                    blurView.setTranslationX(val);

                    float val2 = blurView.getY() + (event.getY() - moveY);
                    blurView.setTranslationY(val2);

                }
                break;

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    break;


            }
            return true;
        });
    }
}
