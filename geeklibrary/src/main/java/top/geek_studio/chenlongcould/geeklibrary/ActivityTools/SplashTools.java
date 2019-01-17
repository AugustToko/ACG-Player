/*
 * ************************************************************
 * 文件：SplashTools.java  模块：geeklibrary  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:47
 * 上次修改时间：2019年01月17日 17:29:00
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.geeklibrary.ActivityTools;

import android.app.Activity;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import top.geek_studio.chenlongcould.geeklibrary.R;

public class SplashTools {

    private Activity mActivity;

    private View mRoot;

    public SplashTools(@NonNull Activity activity, String appName, @DrawableRes int iconId) {
        mActivity = activity;
        mRoot = LayoutInflater.from(activity).inflate(R.layout.activity_splash, null);
        ((TextView) mRoot.findViewById(R.id.splash_text)).setText(appName);
        ((ImageView) mRoot.findViewById(R.id.ico)).setImageResource(iconId);
    }
}
