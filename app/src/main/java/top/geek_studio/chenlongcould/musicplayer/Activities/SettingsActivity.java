/*
 * ************************************************************
 * 文件：SettingsActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月20日 21:06:43
 * 上次修改时间：2018年11月20日 21:06:05
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.jrummyapps.android.colorpicker.ColorPickerDialog;
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener;

import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public class SettingsActivity extends MyBaseActivity {

    public static final String TAG = "SettingsActivity";

    public static final int PRIMARY = 0;

    public static final int PRIMARY_DARK = 1;

    public static final int ACCENT = 2;

    private ConstraintLayout mPrimaryOpt;

    private ConstraintLayout mPrimaryDarkOpt;

    private ConstraintLayout mAccentOpt;

    private ImageView mPrimaryImage;

    private ImageView mPrimaryDarkImage;

    private ImageView mAccentImage;

    private Toolbar mToolbar;

    private AppBarLayout mAppBarLayout;

    private SharedPreferences mDefPrefs;
    private ColorPickerDialogListener pickerDialogListener = new ColorPickerDialogListener() {
        @Override
        public void onColorSelected(int dialogId, @ColorInt int color) {
            SharedPreferences.Editor editor = mDefPrefs.edit();
            Values.STYLE_CHANGED = true;
            switch (dialogId) {
                case PRIMARY: {
                    mPrimaryImage.clearAnimation();
                    ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), mDefPrefs.getInt(Values.ColorInt.PRIMARY_COLOR, R.color.colorPrimary), color);
                    animator.setDuration(300);
                    animator.addUpdateListener(animation -> {
                        mPrimaryImage.setBackgroundColor((Integer) animation.getAnimatedValue());
                        mAppBarLayout.setBackgroundColor((Integer) animation.getAnimatedValue());
                    });
                    animator.start();
                    editor.putInt(Values.ColorInt.PRIMARY_COLOR, color);
                    editor.apply();
                    mPrimaryImage.clearAnimation();
                }
                break;

                case PRIMARY_DARK: {
                    mPrimaryDarkImage.clearAnimation();
                    ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), mDefPrefs.getInt(Values.ColorInt.PRIMARY_DARK_COLOR, R.color.colorPrimaryDark), color);
                    animator.setDuration(300);
                    animator.addUpdateListener(animation -> {
                        mPrimaryDarkImage.setBackgroundColor((Integer) animation.getAnimatedValue());
                        getWindow().setNavigationBarColor((Integer) animation.getAnimatedValue());
                    });
                    animator.start();
                    editor.putInt(Values.ColorInt.PRIMARY_DARK_COLOR, color);
                    editor.apply();
                    mPrimaryDarkImage.clearAnimation();
                }
                break;

                case ACCENT: {
                    mAccentImage.setBackgroundColor(color);
                    editor.putInt(Values.ColorInt.ACCENT_COLOR, color);
                    editor.apply();
                }
                break;
            }
        }

        @Override
        public void onDialogDismissed(int dialogId) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        mDefPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        mPrimaryOpt = findViewById(R.id.primer_color_option);
        mPrimaryDarkOpt = findViewById(R.id.primer_color_dark_option);
        mAccentOpt = findViewById(R.id.accent_color_option);
        mPrimaryImage = findViewById(R.id.activity_settings_preview_primary);
        mPrimaryDarkImage = findViewById(R.id.activity_settings_preview_primary_dark);
        mAccentImage = findViewById(R.id.activity_settings_preview_acc);
        mToolbar = findViewById(R.id.activity_settings_toolbar);
        mAppBarLayout = findViewById(R.id.activity_settings_appbar);

        mToolbar.inflateMenu(R.menu.menu_toolbar_settings);

        initPreView();

        mToolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.menu_toolbar_settings_reset: {
                    SharedPreferences.Editor editor = mDefPrefs.edit();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        editor.putInt(Values.ColorInt.ACCENT_COLOR, getResources().getColor(R.color.colorAccent, getTheme()));
                        editor.putInt(Values.ColorInt.PRIMARY_COLOR, getResources().getColor(R.color.colorPrimary, getTheme()));
                        editor.putInt(Values.ColorInt.PRIMARY_DARK_COLOR, getResources().getColor(R.color.colorPrimaryDark, getTheme()));
                    } else {
                        editor.putInt(Values.ColorInt.ACCENT_COLOR, R.color.colorAccent);
                        editor.putInt(Values.ColorInt.PRIMARY_COLOR, R.color.colorPrimary);
                        editor.putInt(Values.ColorInt.PRIMARY_DARK_COLOR, R.color.colorPrimaryDark);
                    }
                    editor.apply();
                }
            }
            return false;
        });

        mToolbar.setNavigationOnClickListener(v -> onBackPressed());

        mPrimaryOpt.setOnClickListener(v -> {
            ColorPickerDialog colorPickerDialog = ColorPickerDialog.newBuilder().setColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_COLOR, R.color.colorPrimary))
                    .setDialogTitle(R.string.color_picker)
                    .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                    .setShowAlphaSlider(true)
                    .setDialogId(PRIMARY)
                    .setAllowPresets(false)
                    .create();
            colorPickerDialog.setColorPickerDialogListener(pickerDialogListener);
            colorPickerDialog.show(getFragmentManager(), "color-picker-dialog");
        });

        mPrimaryDarkOpt.setOnClickListener(v -> {
            ColorPickerDialog colorPickerDialog = ColorPickerDialog.newBuilder().setColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_DARK_COLOR, R.color.colorPrimaryDark))
                    .setDialogTitle(R.string.color_picker)
                    .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                    .setShowAlphaSlider(true)
                    .setDialogId(PRIMARY_DARK)
                    .setAllowPresets(false)
                    .create();
            colorPickerDialog.setColorPickerDialogListener(pickerDialogListener);
            colorPickerDialog.show(getFragmentManager(), "color-picker-dialog");
        });

        mAccentOpt.setOnClickListener(v -> {
            ColorPickerDialog colorPickerDialog = ColorPickerDialog.newBuilder().setColor(mDefPrefs.getInt(Values.ColorInt.ACCENT_COLOR, R.color.colorAccent))
                    .setDialogTitle(R.string.color_picker)
                    .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                    .setShowAlphaSlider(true)
                    .setDialogId(ACCENT)
                    .setAllowPresets(false)
                    .create();
            colorPickerDialog.setColorPickerDialogListener(pickerDialogListener);
            colorPickerDialog.show(getFragmentManager(), "color-picker-dialog");
        });
    }

    private void initPreView() {
        mPrimaryImage.setBackgroundColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_COLOR, R.color.colorPrimary));
        mPrimaryDarkImage.setBackgroundColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_DARK_COLOR, R.color.colorPrimaryDark));
        mAccentImage.setBackgroundColor(mDefPrefs.getInt(Values.ColorInt.ACCENT_COLOR, R.color.colorAccent));
        getWindow().setNavigationBarColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_DARK_COLOR, R.color.colorPrimaryDark));
        Utils.Ui.setAppBarColor(this, mAppBarLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Values.STYLE_CHANGED) {
            Utils.Ui.setAppBarColor(this, mAppBarLayout);
            Values.STYLE_CHANGED = false;
        }
    }
}
