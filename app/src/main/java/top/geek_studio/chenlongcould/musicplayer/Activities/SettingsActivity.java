/*
 * ************************************************************
 * 文件：SettingsActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月23日 11:17:30
 * 上次修改时间：2018年11月23日 11:16:10
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.Switch;

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

    @SuppressWarnings("FieldCanBeLocal")
    private ConstraintLayout mPrimaryOpt;

    @SuppressWarnings("FieldCanBeLocal")
    private ConstraintLayout mPrimaryDarkOpt;

    @SuppressWarnings("FieldCanBeLocal")
    private ConstraintLayout mAccentOpt;

    private ConstraintLayout mAutoNightOpt;

    private Switch mNightSwitch;

    private ImageView mPrimaryImage;

    private ImageView mPrimaryDarkImage;

    private ImageView mAccentImage;

    @SuppressWarnings("FieldCanBeLocal")
    private Toolbar mToolbar;

    private AppBarLayout mAppBarLayout;

    private SharedPreferences mDefPrefs;
    private ColorPickerDialogListener pickerDialogListener = new ColorPickerDialogListener() {
        @Override
        public void onColorSelected(int dialogId, @ColorInt int color) {
            SharedPreferences.Editor editor = mDefPrefs.edit();
            Values.STYLE_CHANGED = true;
            switch (dialogId) {

                /*
                 * Primary (toolbar tabLayout...)
                 * */
                case PRIMARY: {
                    mPrimaryImage.clearAnimation();
                    ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), mDefPrefs.getInt(Values.ColorInt.PRIMARY_COLOR, R.color.colorPrimary), color);
                    animator.setDuration(300);
                    animator.addUpdateListener(animation -> {
                        mPrimaryImage.setBackgroundColor((Integer) animation.getAnimatedValue());
                        mAppBarLayout.setBackgroundColor((Integer) animation.getAnimatedValue());
                        mToolbar.setBackgroundColor((Integer) animation.getAnimatedValue());
                    });
                    animator.start();
                    editor.putInt(Values.ColorInt.PRIMARY_COLOR, color);
                    editor.apply();
                    mPrimaryImage.clearAnimation();
                }
                break;

                /*
                 * Dark Primary
                 * */
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

                /*
                 * Accent Color
                 * */
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
        mAutoNightOpt = findViewById(R.id.night_style);
        mNightSwitch = findViewById(R.id.activity_settings_night_switch);

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
                        editor.putInt(Values.ColorInt.ACCENT_COLOR, Color.parseColor(Values.Color.ACCENT));
                        editor.putInt(Values.ColorInt.PRIMARY_COLOR, Color.parseColor(Values.Color.PRIMARY));
                        editor.putInt(Values.ColorInt.PRIMARY_DARK_COLOR, Color.parseColor(Values.Color.PRIMARY_DARK));
                    }
                    editor.apply();

                    clearAnimation();
                    ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), mDefPrefs.getInt(Values.ColorInt.PRIMARY_COLOR, Color.parseColor(Values.Color.PRIMARY)), Color.parseColor(Values.Color.PRIMARY));
                    animator.setDuration(300);
                    animator.addUpdateListener(animation -> {
                        mPrimaryImage.setBackgroundColor((Integer) animation.getAnimatedValue());
                        mAppBarLayout.setBackgroundColor((Integer) animation.getAnimatedValue());
                        mToolbar.setBackgroundColor((Integer) animation.getAnimatedValue());
                    });

                    ValueAnimator animator2 = ValueAnimator.ofObject(new ArgbEvaluator(), mDefPrefs.getInt(Values.ColorInt.PRIMARY_DARK_COLOR, Color.parseColor(Values.Color.PRIMARY_DARK)), Color.parseColor(Values.Color.PRIMARY_DARK));
                    animator2.setDuration(300);
                    animator2.addUpdateListener(animation -> {
                        mPrimaryDarkImage.setBackgroundColor((Integer) animator2.getAnimatedValue());
                        getWindow().setNavigationBarColor((Integer) animator2.getAnimatedValue());
                    });

                    ValueAnimator animator3 = ValueAnimator.ofObject(new ArgbEvaluator(), mDefPrefs.getInt(Values.ColorInt.ACCENT_COLOR, Color.parseColor(Values.Color.ACCENT)), Color.parseColor(Values.Color.ACCENT));
                    animator3.setDuration(300);
                    animator3.addUpdateListener(animation -> {
                        mAccentImage.setBackgroundColor((Integer) animator3.getAnimatedValue());
                    });

                    animator.start();
                    animator2.start();
                    animator3.start();

                    clearAnimation();
                }
            }
            return false;
        });

        mToolbar.setNavigationOnClickListener(v -> onBackPressed());

        mPrimaryOpt.setOnClickListener(v -> {
            ColorPickerDialog colorPickerDialog = ColorPickerDialog.newBuilder().setColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_COLOR, Color.parseColor("#008577")))
                    .setDialogTitle(R.string.color_picker)
                    .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                    .setShowAlphaSlider(true)
                    .setDialogId(PRIMARY)
                    .setAllowPresets(false)
                    .create();
            colorPickerDialog.setColorPickerDialogListener(pickerDialogListener);
            //noinspection deprecation
            colorPickerDialog.show(getFragmentManager(), "color-picker-dialog");
        });

        mPrimaryDarkOpt.setOnClickListener(v -> {
            ColorPickerDialog colorPickerDialog = ColorPickerDialog.newBuilder().setColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_DARK_COLOR, Color.parseColor("#00574B")))
                    .setDialogTitle(R.string.color_picker)
                    .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                    .setShowAlphaSlider(true)
                    .setDialogId(PRIMARY_DARK)
                    .setAllowPresets(false)
                    .create();
            colorPickerDialog.setColorPickerDialogListener(pickerDialogListener);
            //noinspection deprecation
            colorPickerDialog.show(getFragmentManager(), "color-picker-dialog");
        });

        mAccentOpt.setOnClickListener(v -> {
            ColorPickerDialog colorPickerDialog = ColorPickerDialog.newBuilder().setColor(mDefPrefs.getInt(Values.ColorInt.ACCENT_COLOR, Color.parseColor("#D81B60")))
                    .setDialogTitle(R.string.color_picker)
                    .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                    .setShowAlphaSlider(true)
                    .setDialogId(ACCENT)
                    .setAllowPresets(false)
                    .create();
            colorPickerDialog.setColorPickerDialogListener(pickerDialogListener);
            //noinspection deprecation
            colorPickerDialog.show(getFragmentManager(), "color-picker-dialog");
        });

        mAutoNightOpt.setOnClickListener(v -> {
            SharedPreferences.Editor editor = mDefPrefs.edit();
            if (Values.Style.AUTO_NIGHT_MODE) {
                editor.putBoolean(Values.SharedPrefsTag.AUTO_NIGHT_MODE, false);
                mNightSwitch.setChecked(false);
                Values.Style.AUTO_NIGHT_MODE = false;
            } else {
                editor.putBoolean(Values.SharedPrefsTag.AUTO_NIGHT_MODE, true);
                mNightSwitch.setChecked(true);
                Values.Style.AUTO_NIGHT_MODE = true;
            }
            editor.apply();
        });

        mNightSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = mDefPrefs.edit();
            if (isChecked) {
                editor.putBoolean(Values.SharedPrefsTag.AUTO_NIGHT_MODE, true);
                Values.Style.AUTO_NIGHT_MODE = true;
            } else {
                editor.putBoolean(Values.SharedPrefsTag.AUTO_NIGHT_MODE, false);
                Values.Style.AUTO_NIGHT_MODE = false;
            }
            editor.apply();
        });
    }

    private void clearAnimation() {
        mPrimaryImage.clearAnimation();
        mToolbar.clearAnimation();
        mAppBarLayout.clearAnimation();
        mPrimaryDarkImage.clearAnimation();
        mAccentImage.clearAnimation();
    }

    private void initPreView() {
        mPrimaryImage.setBackgroundColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_COLOR, Color.parseColor("#008577")));
        mPrimaryDarkImage.setBackgroundColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_DARK_COLOR, Color.parseColor("#00574B")));
        mAccentImage.setBackgroundColor(mDefPrefs.getInt(Values.ColorInt.ACCENT_COLOR, Color.parseColor("#D81B60")));
        getWindow().setNavigationBarColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_DARK_COLOR, Color.parseColor("#00574B")));
        Utils.Ui.setAppBarColor(this, mAppBarLayout, mToolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Values.STYLE_CHANGED) {
            Utils.Ui.setAppBarColor(this, mAppBarLayout, mToolbar);
            Values.STYLE_CHANGED = false;
        }
    }
}
