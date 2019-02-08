/*
 * ************************************************************
 * 文件：AboutTools.java  模块：geeklibrary  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:47
 * 上次修改时间：2019年01月17日 17:28:59
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.geeklibrary.ActivityTools;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import top.geek_studio.chenlongcould.geeklibrary.R;

/**
 * @author chenlongcould
 */
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public final class AboutTools {

    private Activity mActivity;

    private View mRoot;

    private AppBarLayout mAppBarLayout;

    private Toolbar mToolbar;

    private LinearLayout mCardView1;

    private LinearLayout mCardView2;

    private LinearLayout mCardView3;

    private ScrollView mScrollView;

    public AboutTools(@NonNull final Activity activity, @DrawableRes int iconId, String appName) {
        mActivity = activity;
        mRoot = LayoutInflater.from(activity).inflate(R.layout.activity_about_app, null);
        mAppBarLayout = mRoot.findViewById(R.id.appbar);
        mToolbar = mRoot.findViewById(R.id.toolbar);
        mCardView1 = mRoot.findViewById(R.id.about_card_1);
        mCardView2 = mRoot.findViewById(R.id.card_2);
        mCardView3 = mRoot.findViewById(R.id.card_3);

        ((ImageView) mRoot.findViewById(R.id.about_ico)).setImageResource(iconId);
        ((TextView) mRoot.findViewById(R.id.card_1_title)).setText(appName);

        mToolbar.setTitle("About");
        mToolbar.setNavigationOnClickListener(v -> activity.onBackPressed());
    }

    public void colorSet(@ColorInt int primary) {
        mAppBarLayout.setBackgroundColor(primary);
        mToolbar.setBackgroundColor(primary);
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    public View getSingleItem() {
        return LayoutInflater.from(mActivity).inflate(R.layout.about_template_single_text, null);
    }

    public View getDulItem() {
        return LayoutInflater.from(mActivity).inflate(R.layout.about_template_dul_text, null);
    }

    public View getModItem() {
        return LayoutInflater.from(mActivity).inflate(R.layout.about_template_dul_text, null);
    }

    public LinearLayout getCardView1() {
        return mCardView1;
    }

    public LinearLayout getCardView2() {
        return mCardView2;
    }

    public LinearLayout getCardView3() {
        return mCardView3;
    }

    /**
     * add view {@link R.layout#about_template_dul_text} to given parent
     *
     * @return the view added
     */
    public View addItemCard(String title, String subTitle, @DrawableRes int iconId, ViewGroup parent) {
        final View item = getDulItem();
        ((TextView) item.findViewById(R.id.main_text)).setText(title);
        ((TextView) item.findViewById(R.id.sub_text)).setText(subTitle);
        ((ImageView) item.findViewById(R.id.ico)).setImageResource(iconId);
        parent.addView(item);
        return item;
    }

    /**
     * add view {@link R.layout#about_template_single_text} to given parent
     *
     * @return the view added
     */
    public View addItemCard(String title, @DrawableRes int iconId, ViewGroup parent) {
        final View item = getSingleItem();
        ((TextView) item.findViewById(R.id.text)).setText(title);
        ((ImageView) item.findViewById(R.id.ico)).setImageResource(iconId);
        parent.addView(item);
        return item;
    }

    public View getRoot() {
        return mRoot;
    }
}
