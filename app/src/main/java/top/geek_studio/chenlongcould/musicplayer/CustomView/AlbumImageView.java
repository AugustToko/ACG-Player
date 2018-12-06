/*
 * ************************************************************
 * 文件：AlbumImageView.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月06日 19:19:07
 * 上次修改时间：2018年12月06日 19:18:26
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.CustomView;

import android.content.Context;
import android.util.AttributeSet;

public class AlbumImageView extends android.support.v7.widget.AppCompatImageView {

    private static final String TAG = "AlbumImageView";

    float mLastX = 0;
    float mLastY = 0;

    float moveX = 0;
    float moveY = 0;

    public AlbumImageView(Context context) {
        this(context, null);
    }

    public AlbumImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlbumImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
