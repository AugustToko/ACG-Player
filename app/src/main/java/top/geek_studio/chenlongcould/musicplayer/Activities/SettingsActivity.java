/*
 * ************************************************************
 * 文件：SettingsActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月12日 20:26:06
 * 上次修改时间：2019年01月12日 14:04:33
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.jrummyapps.android.colorpicker.ColorPickerDialog;
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener;

import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.Interface.IStyle;
import top.geek_studio.chenlongcould.musicplayer.MyMusicService;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.databinding.ActivitySettingsBinding;

import static top.geek_studio.chenlongcould.musicplayer.Values.SharedPrefsTag.NOTIFICATION_COLORIZED;

public final class SettingsActivity extends MyBaseCompatActivity implements IStyle {

    public static final String TAG = "SettingsActivity";

    public static final int PRIMARY = 0;

    public static final int PRIMARY_DARK = 1;

    public static final int ACCENT = 2;

    private ActivitySettingsBinding mSettingsBinding;

    private Switch mNightSwitch;

    private Switch mStyleSwitch;

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
        mSettingsBinding = DataBindingUtil.setContentView(this, R.layout.activity_settings);

        mDefPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        //find xx
        final ConstraintLayout primaryOpt = findViewById(R.id.primer_color_option);
        final ConstraintLayout primaryDarkOpt = findViewById(R.id.primer_color_dark_option);
        final ConstraintLayout accentOpt = findViewById(R.id.accent_color_option);
        mPrimaryImage = findViewById(R.id.activity_settings_preview_primary);
        mPrimaryDarkImage = findViewById(R.id.activity_settings_preview_primary_dark);
        mAccentImage = findViewById(R.id.activity_settings_preview_acc);
        mToolbar = findViewById(R.id.activity_settings_toolbar);
        mAppBarLayout = findViewById(R.id.activity_settings_appbar);
        final ConstraintLayout setNightOpt = findViewById(R.id.night_style);
        mNightSwitch = findViewById(R.id.activity_settings_night_switch);
        final ConstraintLayout styleOpt = findViewById(R.id.detail_background_style);
        mStyleSwitch = findViewById(R.id.activity_settings_style_switch);

        ConstraintLayout constraintLayout = findViewById(R.id.theme_settings);
        constraintLayout.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, ThemeActivity.class)));

        mToolbar.inflateMenu(R.menu.menu_toolbar_settings);

        initPreView();

        mToolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.menu_toolbar_settings_reset: {

                    SharedPreferences.Editor editor = mDefPrefs.edit();
                    editor.putInt(Values.SharedPrefsTag.ACCENT_COLOR, ContextCompat.getColor(SettingsActivity.this, R.color.colorAccent));
                    editor.putInt(Values.SharedPrefsTag.PRIMARY_COLOR, ContextCompat.getColor(SettingsActivity.this, R.color.colorPrimary));
                    editor.putInt(Values.SharedPrefsTag.PRIMARY_DARK_COLOR, ContextCompat.getColor(SettingsActivity.this, R.color.colorPrimaryDark));
                    editor.apply();

                    clearAnimation();
                    ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), mDefPrefs.getInt(Values.SharedPrefsTag.PRIMARY_COLOR, ContextCompat.getColor(SettingsActivity.this, R.color.colorPrimary)), ContextCompat.getColor(SettingsActivity.this, R.color.colorPrimary));
                    animator.setDuration(300);
                    animator.addUpdateListener(animation -> {
                        mPrimaryImage.setBackgroundColor((Integer) animation.getAnimatedValue());
                        mAppBarLayout.setBackgroundColor((Integer) animation.getAnimatedValue());
                        mToolbar.setBackgroundColor((Integer) animation.getAnimatedValue());
                    });

                    ValueAnimator animator2 = ValueAnimator.ofObject(new ArgbEvaluator(), mDefPrefs.getInt(Values.SharedPrefsTag.PRIMARY_DARK_COLOR, ContextCompat.getColor(SettingsActivity.this, R.color.colorPrimaryDark)), ContextCompat.getColor(SettingsActivity.this, R.color.colorPrimaryDark));
                    animator2.setDuration(300);
                    animator2.addUpdateListener(animation -> {
                        mPrimaryDarkImage.setBackgroundColor((Integer) animator2.getAnimatedValue());
                        getWindow().setNavigationBarColor((Integer) animator2.getAnimatedValue());
                    });

                    ValueAnimator animator3 = ValueAnimator.ofObject(new ArgbEvaluator(), mDefPrefs.getInt(Values.SharedPrefsTag.ACCENT_COLOR, ContextCompat.getColor(SettingsActivity.this, R.color.colorAccent)), ContextCompat.getColor(SettingsActivity.this, R.color.colorAccent));
                    animator3.setDuration(300);
                    animator3.addUpdateListener(animation -> mAccentImage.setBackgroundColor((Integer) animator3.getAnimatedValue()));

                    animator.start();
                    animator2.start();
                    animator3.start();

                    clearAnimation();
                }
            }
            return false;
        });

        mToolbar.setNavigationOnClickListener(v -> onBackPressed());

        primaryOpt.setOnClickListener(v -> {
            ColorPickerDialog colorPickerDialog = ColorPickerDialog.newBuilder().setColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_COLOR, Color.parseColor("#008577")))
                    .setDialogTitle(R.string.color_picker)
                    .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                    .setShowAlphaSlider(true)
                    .setDialogId(PRIMARY)
                    .setAllowPresets(false)
                    .create();
            colorPickerDialog.setColorPickerDialogListener(pickerDialogListener);
            //noinspection deprecation
            colorPickerDialog.show(getFragmentManager(), "color-picker-dialog");
        });

        primaryDarkOpt.setOnClickListener(v -> {
            ColorPickerDialog colorPickerDialog = ColorPickerDialog.newBuilder().setColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_DARK_COLOR, Color.parseColor("#00574B")))
                    .setDialogTitle(R.string.color_picker)
                    .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                    .setShowAlphaSlider(true)
                    .setDialogId(PRIMARY_DARK)
                    .setAllowPresets(false)
                    .create();
            colorPickerDialog.setColorPickerDialogListener(pickerDialogListener);
            //noinspection deprecation
            colorPickerDialog.show(getFragmentManager(), "color-picker-dialog");
        });

        accentOpt.setOnClickListener(v -> {
            ColorPickerDialog colorPickerDialog = ColorPickerDialog.newBuilder().setColor(mDefPrefs.getInt(Values.ColorInt.ACCENT_COLOR, Color.parseColor("#D81B60")))
                    .setDialogTitle(R.string.color_picker)
                    .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                    .setShowAlphaSlider(true)
                    .setDialogId(ACCENT)
                    .setAllowPresets(false)
                    .create();
            colorPickerDialog.setColorPickerDialogListener(pickerDialogListener);
            //noinspection deprecation
            colorPickerDialog.show(getFragmentManager(), "color-picker-dialog");
        });

//        setNightOpt.setOnClickListener(v -> {
//
//            SharedPreferences.Editor editor = mDefPrefs.edit();
//            if (Values.Style.NIGHT_MODE) {
//                editor.putBoolean(Values.SharedPrefsTag.AUTO_NIGHT_MODE, false);
//                mNightSwitch.setChecked(false);
//                Values.Style.NIGHT_MODE = false;
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//            } else {
//                editor.putBoolean(Values.SharedPrefsTag.AUTO_NIGHT_MODE, true);
//                mNightSwitch.setChecked(true);
//                Values.Style.NIGHT_MODE = true;
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//            }
//            editor.apply();
//
//            Utils.Ui.inDayNightSet(mDefPrefs);
//        });

//        //night opt
//        mNightSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
//
//            SharedPreferences.Editor editor = mDefPrefs.edit();
//            if (isChecked) {
//                editor.putBoolean(Values.SharedPrefsTag.AUTO_NIGHT_MODE, true);
//                Values.Style.NIGHT_MODE = true;
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//            } else {
//                editor.putBoolean(Values.SharedPrefsTag.AUTO_NIGHT_MODE, false);
//                Values.Style.NIGHT_MODE = false;
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//            }
//            editor.apply();
//
//            Utils.Ui.inDayNightSet(mDefPrefs);
//
//        });

        styleOpt.setOnClickListener(v -> {

            SharedPreferences.Editor editor = mDefPrefs.edit();
            if (Values.Style.DETAIL_BACKGROUND.equals(Values.Style.STYLE_BACKGROUND_BLUR)) {
                Values.Style.DETAIL_BACKGROUND = Values.Style.STYLE_BACKGROUND_AUTO_COLOR;
                mStyleSwitch.setChecked(false);
                editor.putString(Values.SharedPrefsTag.DETAIL_BG_STYLE, Values.Style.STYLE_BACKGROUND_AUTO_COLOR);
            } else {
                Values.Style.DETAIL_BACKGROUND = Values.Style.STYLE_BACKGROUND_BLUR;
                mStyleSwitch.setChecked(true);
                editor.putString(Values.SharedPrefsTag.DETAIL_BG_STYLE, Values.Style.STYLE_BACKGROUND_BLUR);
            }
            editor.apply();

        });

        mStyleSwitch.setClickable(false);

//        mStyleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            SharedPreferences.Editor editor = mDefPrefs.edit();
//            mStyleSwitch.setChecked(isChecked);
//            if (Values.Style.DETAIL_BACKGROUND.equals(Values.Style.STYLE_BACKGROUND_BLUR)) {
//                mStyleSwitch.setChecked(false);
//                editor.putString(Values.SharedPrefsTag.DETAIL_BG_STYLE, Values.Style.STYLE_BACKGROUND_AUTO_COLOR);
//            } else {
//                mStyleSwitch.setChecked(true);
//                editor.putString(Values.SharedPrefsTag.DETAIL_BG_STYLE, Values.Style.STYLE_BACKGROUND_BLUR);
//            }
//            editor.apply();
//        });

        mSettingsBinding.colorNoti.setOnClickListener(v -> {
            final SharedPreferences.Editor editor = mDefPrefs.edit();
            final Intent intent = new Intent(this, MyMusicService.class);
            if (mDefPrefs.getBoolean(NOTIFICATION_COLORIZED, true)) {
                editor.putBoolean(NOTIFICATION_COLORIZED, false);
                if (editor.commit()) {
                    mSettingsBinding.colorNotiSwitch.setChecked(false);
                    intent.putExtra(Values.SharedPrefsTag.NOTIFICATION_COLORIZED, false);
                } else
                    Toast.makeText(SettingsActivity.this, "Set Colorized Error...", Toast.LENGTH_SHORT).show();
            } else {
                editor.putBoolean(NOTIFICATION_COLORIZED, true);
                if (editor.commit()) {
                    mSettingsBinding.colorNotiSwitch.setChecked(true);
                    intent.putExtra(Values.SharedPrefsTag.NOTIFICATION_COLORIZED, true);
                } else
                    Toast.makeText(SettingsActivity.this, "Set Colorized Error...", Toast.LENGTH_SHORT).show();
            }

            startService(intent);

            if (Values.HAS_PLAYED) {
                try {
                    if (Data.sMusicBinder.isPlayingMusic()) {
                        Data.sMusicBinder.playMusic();
                    } else {
                        Data.sMusicBinder.pauseMusic();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        mSettingsBinding.statusColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences.Editor editor = mDefPrefs.edit();
                if (mDefPrefs.getBoolean(Values.SharedPrefsTag.TRANSPORT_STATUS, false)) {
                    editor.putBoolean(Values.SharedPrefsTag.TRANSPORT_STATUS, false);
                    mSettingsBinding.colorStatusSwitch.setChecked(false);
                } else {
                    editor.putBoolean(Values.SharedPrefsTag.TRANSPORT_STATUS, true);
                    mSettingsBinding.colorStatusSwitch.setChecked(true);
                }
                editor.apply();
            }
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
        initStyle();
        mNightSwitch.setChecked(Values.Style.NIGHT_MODE);

        //noinspection ConstantConditions
        if (mDefPrefs.getString(Values.SharedPrefsTag.DETAIL_BG_STYLE, Values.Style.STYLE_BACKGROUND_BLUR).equals(Values.Style.STYLE_BACKGROUND_AUTO_COLOR)) {
            mStyleSwitch.setChecked(false);
        } else {
            mStyleSwitch.setChecked(true);
        }

        if (mDefPrefs.getBoolean(NOTIFICATION_COLORIZED, true)) {
            mSettingsBinding.colorNotiSwitch.setChecked(true);
        } else {
            mSettingsBinding.colorNotiSwitch.setChecked(false);
        }

        if (mDefPrefs.getBoolean(Values.SharedPrefsTag.TRANSPORT_STATUS, false)) {
            mSettingsBinding.colorStatusSwitch.setChecked(true);
        } else {
            mSettingsBinding.colorStatusSwitch.setChecked(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initStyle();
    }

    @Override
    public void initStyle() {
        mPrimaryImage.setBackgroundColor(Utils.Ui.getPrimaryColor(this));
        mPrimaryDarkImage.setBackgroundColor(Utils.Ui.getPrimaryDarkColor(this));
        mAccentImage.setBackgroundColor(Utils.Ui.getAccentColor(this));
        Utils.Ui.setTopBottomColor(this, mAppBarLayout, mToolbar);

        final ImageView imageView = findViewById(R.id.theme_preview);

        //load theme
        if (Data.sTheme != null) {
            imageView.setVisibility(View.VISIBLE);
            GlideApp.with(this)
                    .load(Data.sTheme.getThumbnail())
                    .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                    .into(imageView);
        } else {
            imageView.setVisibility(View.GONE);
        }

    }

}
