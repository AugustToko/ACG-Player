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

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import top.geek_studio.chenlongcould.geeklibrary.theme.IStyle;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

@SuppressLint("Registered")
public abstract class MyBaseCompatActivity extends AppCompatActivity implements IStyle {

    private static final String TAG = "MyBaseCompatActivity";

    private Toolbar mToolbar = null;
    private AppBarLayout mAppBarLayout = null;
    private TextView mSubtitleView = null;

    /**
     * set up status bar color
     */
    protected void setStatusBarTextColor(final Activity activity, @ColorInt int color) {
        final View decor = activity.getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Utils.Ui.isColorLight(color)) {
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
        }
    }

    /**
     * setup TaskCardColor
     * ps: 通用
     */
    protected void setUpTaskCardColor(@ColorInt int color) {
        setTaskDescription(new ActivityManager.TaskDescription((String) getTitle(), null, color));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        //print TAG
        Log.d(TAG, "onCreate: " + getActivityTAG());

        //set taskDescription (all activity extends this)
        setTaskDescription(new ActivityManager.TaskDescription((String) getTitle(), null, Utils.Ui.getPrimaryColor(this)));

        initStyle();

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

        super.onCreate(savedInstanceState);
    }

    /**
     * init permission, every Activity extends {@link MyBaseCompatActivity}
     */
    public boolean initPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Values.REQUEST_WRITE_EXTERNAL_STORAGE);
            return false;
        }
    }

    /**
     * @return ACTIVITY'S TAG
     */
    abstract public String getActivityTAG();

    /**
     * Load the usual menu, no item is selected({@link androidx.recyclerview.widget.RecyclerView})
     */
    abstract public void inflateCommonMenu();

    /**
     * Load the selection menu, has item in selected({@link androidx.recyclerview.widget.RecyclerView})
     */
    abstract public void inflateChooseMenu();

    /**
     * set up view
     */
    protected void initView(@NonNull Toolbar toolbar, @NonNull AppBarLayout appBarLayout) {
        mToolbar = toolbar;
        mAppBarLayout = appBarLayout;
    }

    @Override
    protected void onResume() {
        super.onResume();
        initStyle();
    }

    @Override
    public void initStyle() {
        setStatusBarTextColor(this, Utils.Ui.getPrimaryColor(this));
        if (mAppBarLayout != null && mToolbar != null) {
            Utils.Ui.setTopBottomColor(this, mAppBarLayout, mToolbar);
            Utils.Ui.setOverToolbarColor(mToolbar, Utils.Ui.getTitleColor(this));
        }
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

        final ValueAnimator animator = new ValueAnimator();
        animator.setFloatValues(1f, 0f);
        animator.setDuration(Values.DefaultValues.ANIMATION_DURATION);
        animator.addUpdateListener(animation -> mSubtitleView.setAlpha((Float) animation.getAnimatedValue()));
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mSubtitleView.setText(subtitle);
                ValueAnimator animator1 = new ValueAnimator();
                animator1.setFloatValues(0f, 1f);
                animator1.setDuration(Values.DefaultValues.ANIMATION_DURATION);
                animator1.addUpdateListener(va -> mSubtitleView.setAlpha((Float) va.getAnimatedValue()));
                animator1.start();
            }
        });
        animator.start();

    }


}
