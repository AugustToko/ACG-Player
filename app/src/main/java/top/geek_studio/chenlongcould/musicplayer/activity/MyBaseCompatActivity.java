/*
 * ************************************************************
 * 文件：MyBaseCompatActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:46
 * 上次修改时间：2019年01月17日 17:28:59
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;

import java.lang.reflect.Field;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import top.geek_studio.chenlongcould.geeklibrary.theme.IStyle;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

@SuppressLint("Registered")
public class MyBaseCompatActivity extends AppCompatActivity implements IStyle {

    private static final String TAG = "MyBaseCompatActivity";

    private Toolbar mToolbar;
    private AppBarLayout mAppBarLayout;

    private TextView mSubtitleView = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置状态栏是否全透明
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Values.SharedPrefsTag.TRANSPORT_STATUS, false)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            }
        }

    }

    /**
     * setup TaskCardColor
     */
    protected void setUpTaskCardColor(@ColorInt int color) {
        setTaskDescription(new ActivityManager.TaskDescription((String) getTitle(), null, color));
    }

    protected void initView(Toolbar toolbar, AppBarLayout appBarLayout) {
        mToolbar = toolbar;
        mAppBarLayout = appBarLayout;
    }

    @Override
    public void initStyle() {
        Log.d(TAG, "initStyle: ");
        setTaskDescription(new ActivityManager.TaskDescription((String) getTitle(), null, Utils.Ui.getPrimaryColor(this)));
        Utils.Ui.setTopBottomColor(this, mAppBarLayout, mToolbar);
        Utils.Ui.setOverToolbarColor(mToolbar, Utils.Ui.getTitleColor(this));
    }

    /**
     * set Toolbar subTile with AlphaAnimation {@link android.view.animation.AlphaAnimation}
     *
     * @param toolbar  toolbar
     * @param subtitle subTitle
     */
    public void setToolbarSubTitleWithAlphaAni(Toolbar toolbar, CharSequence subtitle) throws NoSuchFieldException, IllegalAccessException {
        if (mSubtitleView == null) {
            Field f = toolbar.getClass().getDeclaredField("mSubtitleTextView");
            f.setAccessible(true);
            mSubtitleView = (TextView) f.get(toolbar);
        }

        ValueAnimator animator = new ValueAnimator();
        animator.setFloatValues(1f, 0f);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> mSubtitleView.setAlpha((Float) animation.getAnimatedValue()));
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mSubtitleView.setText(subtitle);
                ValueAnimator animator1 = new ValueAnimator();
                animator1.setFloatValues(0f, 1f);
                animator1.setDuration(300);
                animator1.addUpdateListener(va -> mSubtitleView.setAlpha((Float) va.getAnimatedValue()));
                animator1.start();
            }
        });
        animator.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Data.sSelections.clear();
        super.onPause();
    }
}
