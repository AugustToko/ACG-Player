/*
 * ************************************************************
 * 文件：Utils.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月27日 13:11:38
 * 上次修改时间：2019年01月21日 19:32:58
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Utils;

import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.Settings;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.internal.NavigationMenuPresenter;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.Models.PlayListItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.databinding.DialogLoadingBinding;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

@SuppressWarnings("WeakerAccess")
public final class Utils {

    public static final String TAG = "Utils";

    private Utils() {
    }

    public static final class Audio {

        private final static MediaMetadataRetriever sMediaMetadataRetriever = new MediaMetadataRetriever();

        public static void openEqualizer(@NonNull final Activity activity, int id) {
            if (id == AudioEffect.ERROR_BAD_VALUE) {
                Toast.makeText(activity, activity.getResources().getString(R.string.no_audio_ID), Toast.LENGTH_LONG).show();
            } else {
                try {
                    final Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                    effects.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, id);
                    effects.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
                    activity.startActivityForResult(effects, 0);
                } catch (@NonNull final ActivityNotFoundException notFound) {
                    Toast.makeText(activity, activity.getResources().getString(R.string.no_equalizer), Toast.LENGTH_SHORT).show();
                }
            }
        }

        /**
         * 获取封面
         *
         * @param mediaUri mp3 path
         */
        @NonNull
        public static Bitmap getMp3Cover(final String mediaUri) {

//            //检测不支持封面的音乐类型
//            if (mediaUri.contains("ogg") || mediaUri.contains("flac")) {
//                return BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.ic_audiotrack_24px);
//            }

            if (mediaUri == null)
                return BitmapFactory.decodeResource(Data.sActivities.get(0).getResources(), R.drawable.ic_audiotrack_24px);

            final File file = new File(mediaUri);
            if (file.isDirectory() || !file.exists())
                return BitmapFactory.decodeResource(Data.sActivities.get(0).getResources(), R.drawable.ic_audiotrack_24px);

            sMediaMetadataRetriever.setDataSource(mediaUri);
            byte[] picture = sMediaMetadataRetriever.getEmbeddedPicture();

            if (picture != null)
                return BitmapFactory.decodeByteArray(picture, 0, picture.length);
            else
                return BitmapFactory.decodeResource(Data.sActivities.get(0).getResources(), R.drawable.ic_audiotrack_24px);
        }

        /**
         * 获取封面
         * <p>
         * same as {@link Audio#getMp3Cover(String)}
         * may call from {@link top.geek_studio.chenlongcould.musicplayer.MyMusicService}
         *
         * @param mediaUri mp3 path
         */
        public static Bitmap getMp3Cover(final String mediaUri, Context context) {

            if (mediaUri == null)
                return getDrawableBitmap(context, R.drawable.ic_audiotrack_24px);

            final File file = new File(mediaUri);
            if (file.isDirectory() || !file.exists())
                return getDrawableBitmap(context, R.drawable.ic_audiotrack_24px);

            sMediaMetadataRetriever.setDataSource(mediaUri);
            byte[] picture = sMediaMetadataRetriever.getEmbeddedPicture();

            if (picture != null)
                return BitmapFactory.decodeByteArray(picture, 0, picture.length);
            else
                return getDrawableBitmap(context, R.drawable.ic_audiotrack_24px);

        }

        /**
         * may call from {@link top.geek_studio.chenlongcould.musicplayer.MyMusicService}
         */
        @Nullable
        public static byte[] getAlbumByteImage(final String path, Context context) {
            final Bitmap bitmap = getDrawableBitmap(context, R.drawable.ic_audiotrack_24px);
            final File file = new File(path);

            if (path == null || file.isDirectory() || !file.exists() || file.isDirectory()) {
                if (bitmap != null) {
                    final ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
                    return buffer.array();
                } else {
                    return null;
                }
            }

            sMediaMetadataRetriever.setDataSource(path);
            byte[] cover = sMediaMetadataRetriever.getEmbeddedPicture();

            if (cover == null) {
                if (bitmap != null)
                    return ByteBuffer.allocate(bitmap.getByteCount()).array();
                else
                    return null;
            } else {
                if (bitmap != null) bitmap.recycle();
                return cover;
            }

        }

        @SuppressWarnings("SameParameterValue")
        @Nullable
        public static Bitmap getDrawableBitmap(@NonNull Context context, @DrawableRes int vectorDrawableId) {
            return BitmapFactory.decodeResource(context.getResources(), vectorDrawableId);
        }

        /**
         * @author Karim Abou Zeid (kabouzeid)
         */
        @NonNull
        public static Intent createShareSongFileIntent(@NonNull final MusicItem song, Context context) {
            try {
                return new Intent()
                        .setAction(Intent.ACTION_SEND)
                        .putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, context.getPackageName(), new File(song.getMusicPath())))
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .setType("audio/*");
            } catch (IllegalArgumentException e) {
                // TODO the path is most likely not like /storage/emulated/0/... but something like /storage/28C7-75B0/...
                e.printStackTrace();
                Toast.makeText(context, "Could not share this file, I'm aware of the issue.", Toast.LENGTH_SHORT).show();
                return new Intent();
            }
        }

        /**
         * @author Karim Abou Zeid (kabouzeid)
         */
        public static Uri getSongFileUri(int songId) {
            return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId);
        }

        /**
         * @author Karim Abou Zeid (kabouzeid)
         */
        public static void setRingtone(@NonNull final Context context, final int id) {
            final ContentResolver resolver = context.getContentResolver();
            final Uri uri = getSongFileUri(id);
            try {
                final ContentValues values = new ContentValues(2);
                values.put(MediaStore.Audio.AudioColumns.IS_RINGTONE, "1");
                values.put(MediaStore.Audio.AudioColumns.IS_ALARM, "1");
                resolver.update(uri, values, null, null);
            } catch (@NonNull final UnsupportedOperationException ignored) {
                return;
            }

            try {
                Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.MediaColumns.TITLE},
                        BaseColumns._ID + "=?",
                        new String[]{String.valueOf(id)},
                        null);
                try {
                    if (cursor != null && cursor.getCount() == 1) {
                        cursor.moveToFirst();
                        Settings.System.putString(resolver, Settings.System.RINGTONE, uri.toString());
                        final String message = context.getString(R.string.x_has_been_set_as_ringtone, cursor.getString(0));
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } catch (SecurityException ignored) {
            }
        }

    }

    public static final class Ui {

        private static final String TAG = "Ui";

        public static int POSITION = 200;

        /**
         * getAccentColor from {@link SharedPreferences}, default: {@link R.color#colorAccent}
         *
         * @return color (int)
         */
        public static int getAccentColor(Context context) {
            return PreferenceManager.getDefaultSharedPreferences(context).getInt(Values.SharedPrefsTag.ACCENT_COLOR, ContextCompat.getColor(context, R.color.colorAccent));
        }

        /**
         * getPrimaryColor from {@link SharedPreferences}, default: {@link R.color#colorPrimary}
         *
         * @return color (int)
         */
        public static int getPrimaryColor(Context context) {
            return PreferenceManager.getDefaultSharedPreferences(context).getInt(Values.SharedPrefsTag.PRIMARY_COLOR, ContextCompat.getColor(context, R.color.colorPrimary));
        }

        /**
         * getPrimaryDarkColor from {@link SharedPreferences}, default: {@link R.color#colorPrimaryDark}
         *
         * @return color (int)
         */
        public static int getPrimaryDarkColor(Context context) {
            return PreferenceManager.getDefaultSharedPreferences(context).getInt(Values.SharedPrefsTag.PRIMARY_DARK_COLOR, ContextCompat.getColor(context, R.color.colorPrimaryDark));
        }

        public static Bitmap readBitmapFromFile(String filePath, int width, int height) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);
            float srcWidth = options.outWidth;
            float srcHeight = options.outHeight;
            int inSampleSize = 1;

            if (srcWidth > height && srcWidth > width) {
                inSampleSize = (int) (srcWidth / width);
            } else if (srcWidth < height && srcHeight > height) {
                inSampleSize = (int) (srcHeight / height);
            }

            if (inSampleSize <= 0) {
                inSampleSize = 1;
            }
            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;

            return BitmapFactory.decodeFile(filePath, options);
        }

        public static Bitmap readBitmapFromArray(byte[] data, int width, int height) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, options);
            float srcWidth = options.outWidth;
            float srcHeight = options.outHeight;
            int inSampleSize = 1;

            if (srcWidth > height && srcWidth > width) {
                inSampleSize = (int) (srcWidth / width);
            } else if (srcWidth < height && srcHeight > height) {
                inSampleSize = (int) (srcHeight / height);
            }

            if (inSampleSize <= 0) {
                inSampleSize = 1;
            }
            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;

            return BitmapFactory.decodeByteArray(data, 0, data.length, options);
        }


        /**
         * @param context context
         * @param aTitle  theTitle
         * @return {@link AlertDialog.Builder}
         */
        public static android.support.v7.app.AlertDialog getLoadingDialog(Context context, String... aTitle) {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
            final View loadView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
            // TODO: 2019/1/7 custom Theme loading animation
            final DialogLoadingBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_loading, null, false);
            builder.setView(loadView);
            builder.setTitle(aTitle.length == 0 ? "Loading..." : aTitle[0]);
            builder.setCancelable(false);
            return builder.create();
        }

        public static void setNavigationMenuLineStyle(NavigationView navigationView, @ColorInt final int color, final int height) {
            try {
                Field fieldByPressenter = navigationView.getClass().getDeclaredField("presenter");
                fieldByPressenter.setAccessible(true);
                NavigationMenuPresenter menuPresenter = (NavigationMenuPresenter) fieldByPressenter.get(navigationView);
                Field fieldByMenuView = menuPresenter.getClass().getDeclaredField("menuView");
                fieldByMenuView.setAccessible(true);
                final NavigationMenuView mMenuView = (NavigationMenuView) fieldByMenuView.get(menuPresenter);

                mMenuView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
                    @Override
                    public void onChildViewAttachedToWindow(@NonNull View view) {

                        RecyclerView.ViewHolder viewHolder = mMenuView.getChildViewHolder(view);
                        if (viewHolder != null && "SeparatorViewHolder".equals(viewHolder.getClass().getSimpleName())) {
                            if (viewHolder.itemView instanceof FrameLayout) {
                                FrameLayout frameLayout = (FrameLayout) viewHolder.itemView;
                                View line = frameLayout.getChildAt(0);
                                line.setBackgroundColor(color);
                                line.getLayoutParams().height = height;
                                line.setLayoutParams(line.getLayoutParams());
                            }
                        }
                    }

                    @Override
                    public void onChildViewDetachedFromWindow(@NonNull View view) {

                    }
                });
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        /**
         * 获取导航栏高度
         *
         * @param context context
         * @return nav height
         */
        public static int getNavheight(final Context context) {
            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            int height = resources.getDimensionPixelSize(resourceId);
            Log.v("dbw", "Navi height:" + height);
            return height;
        }

        /**
         * set color (style)
         *
         * @param activity context
         * @param appBar   appBarLayout
         * @param toolbar  toolbar
         */
        public static void setTopBottomColor(final Activity activity, final AppBarLayout appBar, final android.support.v7.widget.Toolbar toolbar) {
            SharedPreferences mDefPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
            appBar.setBackgroundColor(mDefPrefs.getInt(Values.SharedPrefsTag.PRIMARY_COLOR, ContextCompat.getColor(activity, R.color.colorPrimary)));
            toolbar.setBackgroundColor(mDefPrefs.getInt(Values.SharedPrefsTag.PRIMARY_COLOR, ContextCompat.getColor(activity, R.color.colorPrimary)));
            activity.getWindow().setNavigationBarColor(mDefPrefs.getInt(Values.SharedPrefsTag.PRIMARY_DARK_COLOR, ContextCompat.getColor(activity, R.color.colorPrimaryDark)));
        }

        public static void setPlayButtonNowPlaying() {
            if (!Data.sActivities.isEmpty()) {
                MainActivity activity = (MainActivity) Data.sActivities.get(0);
                activity.getMusicDetailFragment().getHandler().sendEmptyMessage(Values.HandlerWhat.SET_BUTTON_PLAY);

                if (Values.CurrentData.CURRENT_UI_MODE.equals(Values.CurrentData.MODE_CAR)) {
                    Data.sCarViewActivity.getFragmentLandSpace().setButtonType("pause");
                }
            }
        }

        /**
         * by broadcast
         */
        public static void setPlayButtonNowPause() {
            if (!Data.sActivities.isEmpty()) {
                MainActivity activity = (MainActivity) Data.sActivities.get(0);
                activity.getMusicDetailFragment().getHandler().sendEmptyMessage(Values.HandlerWhat.SET_BUTTON_PAUSE);

                if (Values.CurrentData.CURRENT_UI_MODE.equals(Values.CurrentData.MODE_CAR)) {
                    Data.sCarViewActivity.getFragmentLandSpace().setButtonType("play");
                }
            }
        }

        public static AlertDialog createMessageDialog(@NonNull final Activity context, @NonNull final String title, @NonNull final String message) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setCancelable(true);
            return builder.create();
        }

        public static void fastToast(@NonNull final Context context, @NonNull final String content) {
            Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
        }

        /**
         * 获取图片亮度
         *
         * @param bm bitmap
         */
        public static int getBright(@NonNull Bitmap bm) {
            int width = bm.getWidth() / 4;
            int height = bm.getHeight() / 4;
            int r, g, b;
            int count = 0;
            int bright = 0;
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    count++;
                    int localTemp = bm.getPixel(i, j);
                    r = (localTemp | 0xff00ffff) >> 16 & 0x00ff;
                    g = (localTemp | 0xffff00ff) >> 8 & 0x0000ff;
                    b = (localTemp | 0xffffff00) & 0x0000ff;
                    bright = (int) (bright + 0.299 * r + 0.587 * g + 0.114 * b);
                }
            }
            return bright / count;
        }

        public static AtomicBoolean ANIMATION_IN_DETAIL_DONE = new AtomicBoolean(true);

//        /**
//         * 设置背景与动画 (blur style)
//         *
//         * @param activity if use fragment may case {@link java.lang.NullPointerException}, glide will call {@link Fragment#getActivity()}
//         */
//        public static void setBlurEffect(@NonNull final MainActivity activity
//                , @NonNull final byte[] bitmap, @NonNull final ImageView primaryBackgroundBef
//                , @NonNull final ImageView primaryBackground, final TextView nextText) {
//
//            ANIMATION_IN_DETAIL_DONE.set(false);
//            primaryBackground.setVisibility(View.VISIBLE);
//
//            final Bitmap data = readBitmapFromArray(bitmap, 100, 100);
//            final AtomicInteger vibrantColor = new AtomicInteger(ContextCompat.getColor(activity, R.color.colorPrimary));
//
//            Palette.from(data).generate(p -> {
//                if (p != null) {
////                        nextText.clearAnimation();
////                        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), Color.BLACK, p.getVibrantColor(Color.parseColor(Values.Color.NOT_VERY_BLACK)));
////                        animator.setDuration(300);
////                        animator.addUpdateListener(animation -> nextText.setTextColor((Integer) animation.getAnimatedValue()));
//                    vibrantColor.set(p.getVibrantColor(vibrantColor.get()));
//                    nextText.setTextColor(vibrantColor.get());
//                } else {
//                    nextText.setTextColor(vibrantColor.get());
//                }
//            });
//
//            //clear
//            GlideApp.with(activity).clear(primaryBackgroundBef);
//            if (Values.Style.DETAIL_BACKGROUND.equals(Values.Style.STYLE_BACKGROUND_BLUR)) {
//                primaryBackgroundBef.post(() -> GlideApp.with(activity)
//                        .load(bitmap)
//                        .dontAnimate()
//                        .apply(bitmapTransform(Data.sBlurTransformation))
//                        .into(primaryBackgroundBef));
//            } else {
//                primaryBackgroundBef.setBackgroundColor(vibrantColor.get());
//            }
//
//            Animator animator = ViewAnimationUtils.createCircularReveal(
//                    primaryBackgroundBef, primaryBackgroundBef.getWidth() / 2, POSITION,
//                    0,
//                    (float) Math.hypot(primaryBackgroundBef.getWidth(), primaryBackgroundBef.getHeight()));
//
//            animator.setInterpolator(new AccelerateInterpolator());
//            animator.setDuration(700);
//            animator.addListener(new Animator.AnimatorListener() {
//                @Override
//                public void onAnimationStart(Animator animation) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    GlideApp.with(activity).clear(primaryBackground);
//                    if (Values.Style.DETAIL_BACKGROUND.equals(Values.Style.STYLE_BACKGROUND_BLUR)) {
//                        GlideApp.with(activity)
//                                .load(bitmap)
//                                .dontAnimate()
//                                .apply(bitmapTransform(Data.sBlurTransformation))
//                                .into(primaryBackground);
//                    } else {
//                        primaryBackground.setBackgroundColor(vibrantColor.get());
//                    }
//
//                    primaryBackground.setVisibility(View.GONE);
//                    ANIMATION_IN_DETAIL_DONE.set(true);
//                }
//
//                @Override
//                public void onAnimationCancel(Animator animation) {
//
//                }
//
//                @Override
//                public void onAnimationRepeat(Animator animation) {
//
//                }
//            });
//            animator.start();
//        }

        public static void setBlurEffect(@NonNull final MainActivity activity
                , @Nullable final Bitmap bitmap, @NonNull final ImageView bgUp
                , @NonNull final ImageView bgDown, final TextView nextText) {

            ANIMATION_IN_DETAIL_DONE.set(false);
            bgDown.setVisibility(View.VISIBLE);

            @ColorInt final int defColor = ContextCompat.getColor(activity, R.color.colorPrimary);

            if (bitmap != null) {
                Palette.from(bitmap).generate(palette -> {
                    if (palette != null) nextText.setTextColor(palette.getVibrantColor(defColor));
                });
            } else {
                nextText.setTextColor(defColor);
            }

            if (Values.Style.DETAIL_BACKGROUND.equals(Values.Style.STYLE_BACKGROUND_BLUR)) {
                Log.d(TAG, "setBlurEffect: blur" + Values.Style.DETAIL_BACKGROUND);
                bgUp.post(() -> GlideApp.with(activity)
                        .load(bitmap)
                        .dontAnimate()
                        .apply(bitmapTransform(Data.sBlurTransformation))
                        .into(bgUp));
            } else {
                Log.d(TAG, "setBlurEffect: not blur" + Values.Style.DETAIL_BACKGROUND);
                if (bitmap != null) {
                    Palette.from(bitmap).generate(palette -> {
                        if (palette != null)
                            bgUp.setBackgroundColor(palette.getVibrantColor(defColor));
                    });
                } else {
                    bgUp.setBackgroundColor(defColor);
                }
            }

            bgUp.post(() -> {
                final Animator animator = ViewAnimationUtils.createCircularReveal(
                        bgUp, bgUp.getWidth() / 2, POSITION, 0, (float) Math.hypot(bgUp.getWidth(), bgUp.getHeight()));

                animator.setInterpolator(new AccelerateInterpolator());
                animator.setDuration(700);
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        GlideApp.with(activity).clear(bgDown);
                        if (Values.Style.DETAIL_BACKGROUND.equals(Values.Style.STYLE_BACKGROUND_BLUR)) {
                            Log.d(TAG, "onAnimationEnd: blur" + Values.Style.DETAIL_BACKGROUND);
                            GlideApp.with(activity)
                                    .load(bitmap)
                                    .dontAnimate()
                                    .apply(bitmapTransform(Data.sBlurTransformation))
                                    .into(bgDown);
                        } else {
                            Log.d(TAG, "onAnimationEnd: not blur" + Values.Style.DETAIL_BACKGROUND);
                            if (bitmap != null) {
                                Palette.from(bitmap).generate(palette -> {
                                    if (palette != null)
                                        bgDown.setBackgroundColor(palette.getVibrantColor(defColor));
                                });
                            } else {
                                bgDown.setBackgroundColor(defColor);
                            }
                        }

                        bgDown.setVisibility(View.GONE);
                        ANIMATION_IN_DETAIL_DONE.set(true);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animator.start();
            });

        }

        public static boolean isColorLight(@ColorInt final int color) {
            double darkness = 1.0D - (0.299D * (double) Color.red(color) + 0.587D * (double) Color.green(color) + 0.114D * (double) Color.blue(color)) / 255.0D;
            return darkness < 0.4D;
        }

        public static void upDateStyle(final SharedPreferences mDefSharedPreferences) {
            if (mDefSharedPreferences.getBoolean(Values.SharedPrefsTag.AUTO_NIGHT_MODE, false)) {
                Values.Style.NIGHT_MODE = true;
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Values.Color.TEXT_COLOR = "#7c7c7c";
            } else {
                Values.Style.NIGHT_MODE = false;
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Values.Color.TEXT_COLOR = "#3c3c3c";
            }
        }

        public static Bitmap blurBitmap(Bitmap bitmap, float r, Context context) {

            //Let's create an empty bitmap with the same size of the bitmap we want to blur
            Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

            //Instantiate a new Renderscript
            RenderScript rs = RenderScript.create(context);

            //Create an Intrinsic Blur Script using the Renderscript
            ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

            //Create the Allocations (in/out) with the Renderscript and the in/out bitmaps
            Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
            Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

            //Set the radius of the blur: 0 < radius <= 25
            blurScript.setRadius(25.0f);

            //Perform the Renderscript
            blurScript.setInput(allIn);
            blurScript.forEach(allOut);

            //Copy the final bitmap created by the out Allocation to the outBitmap
            allOut.copyTo(outBitmap);

            //recycle the original bitmap
            bitmap.recycle();

            //After finishing everything, we destroy the Renderscript.
            rs.destroy();

            return outBitmap;

        }
    }

    public static final class DataSet {

        /**
         * make a random play list, data by {@link Data#sMusicItems}
         */
        public static void makeARandomList() {
            Data.sPlayOrderList.clear();
            Data.sPlayOrderList.addAll(Data.sMusicItems);
            Collections.shuffle(Data.sPlayOrderList);
        }

        /**
         * favourite music settings
         * add or del
         *
         * @param context context
         * @param item    MusicItem
         */
        public static void addToFavourite(Context context, MusicItem item) {
            SharedPreferences mDef = PreferenceManager.getDefaultSharedPreferences(context);
            int id = mDef.getInt(Values.SharedPrefsTag.FAVOURITE_LIST_ID, -1);
            if (id != -1) {
                if (isFav(context, item.getMusicID()) == 1)
                    PlayListsUtil.addToPlaylist(context, item, id, false);
                else Toast.makeText(context, "Already added!", Toast.LENGTH_SHORT).show();

            } else {
                id = PlayListsUtil.createPlaylist(context, "Favourite List");
                SharedPreferences.Editor editor = mDef.edit();
                editor.putInt(Values.SharedPrefsTag.FAVOURITE_LIST_ID, id);
                editor.apply();
                if (id != -1) {
                    if (isFav(context, item.getMusicID()) != 0)
                        PlayListsUtil.addToPlaylist(context, item, id, false);
                    else Toast.makeText(context, "Already added!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        /**
         * add into music List
         *
         * @param activity MainActivity
         * @param item     MusicItem
         */
        public static void addListDialog(MainActivity activity, MusicItem item) {
            final Resources resources = activity.getResources();

            final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(activity);
            builder.setTitle(resources.getString(R.string.add_to_playlist));

            builder.setNegativeButton(resources.getString(R.string.new_list), (dialog, which) -> {
                final android.support.v7.app.AlertDialog.Builder b2 = new android.support.v7.app.AlertDialog.Builder(activity);
                b2.setTitle(resources.getString(R.string.enter_name));

                final EditText et = new EditText(activity);
                b2.setView(et);

                et.setHint(resources.getString(R.string.enter_name));
                et.setSingleLine(true);
                b2.setNegativeButton(resources.getString(R.string.cancel), null);
                b2.setPositiveButton(resources.getString(R.string.sure), (dialog1, which1) -> {
                    if (TextUtils.isEmpty(et.getText())) {
                        Toast.makeText(activity, "name can not empty!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final int result = PlayListsUtil.createPlaylist(activity, et.getText().toString());
                    if (result != -1)
                        PlayListsUtil.addToPlaylist(activity, item, result, false);
                    dialog.dismiss();
                    Data.sPlayListItems.add(0, new PlayListItem(result, et.getText().toString()));

                    //update data
                    activity.getPlayListFragment().getPlayListAdapter().notifyItemInserted(0);
                });
                b2.show();
            });

            builder.setCancelable(true);

            builder.setSingleChoiceItems(activity.getContentResolver()
                            .query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, null, null, null, null),
                    -1, MediaStore.Audio.Playlists.NAME, (dialog, which) -> {
                        PlayListsUtil.addToPlaylist(activity, item, Data.sPlayListItems.get(which).getId(), false);
                        dialog.dismiss();
                    });
            builder.show();

        }

        /**
         * isFav
         *
         * @return 0 == isFav, 1 == notFav, -1 == noList
         */
        public static int isFav(Context context, int idMusic) {
            SharedPreferences mDef = PreferenceManager.getDefaultSharedPreferences(context);
            int id = mDef.getInt(Values.SharedPrefsTag.FAVOURITE_LIST_ID, -1);

            if (id == -1) {
                Toast.makeText(context, "None Fav", Toast.LENGTH_SHORT).show();
                return -1;
            }

            //get musicId in PlayList
            Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Playlists.Members.getContentUri("external", id)
                    , null, null, null, MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
            if (cursor != null && cursor.moveToFirst()) {
                cursor.moveToFirst();
                do {

                    //search music (with audioId)
                    int audioId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID));
                    Cursor cursor1 = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.MediaColumns._ID + " = ?", new String[]{String.valueOf(audioId)}, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
                    if (cursor1 != null && cursor1.moveToFirst()) {
                        do {
                            if (cursor1.getInt(cursor1.getColumnIndexOrThrow(MediaStore.Video.Media._ID)) == idMusic)
                                return 0;
                        } while (cursor1.moveToNext());
                        cursor1.close();
                    }

                } while (cursor.moveToNext());
                cursor.close();
            }
            return 1;
        }
    }

    /**
     * start activity, broadcast, service...
     */
    public static final class SendSomeThing {

        public static final String TAG = "SendSomeThing";

        /**
         * send broadcast by pause
         *
         * @param context context
         */
        public static void sendPause(final Context context) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(context.getPackageName(), Values.BroadCast.ReceiverOnMusicPause));
            context.sendBroadcast(intent, Values.Permission.BROAD_CAST);
        }

        /**
         * @param args play_type: previous, next, slide
         */
        public static void sendPlay(final Context context, final int playType, final String args) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(context.getPackageName(), Values.BroadCast.ReceiverOnMusicPlay));
            intent.putExtra("play_type", playType);
            intent.putExtra("args", args);
            context.sendBroadcast(intent, Values.Permission.BROAD_CAST);
        }
    }

    public static final class M3Utils {
        // TODO: 2018/12/10 read the m3u file
//            new AsyncTask<Void, Void, Void>() {
//
//                @Override
//                protected Void doInBackground(Void... voids) {
//
////                    ArrayList<String> pathList = new ArrayList<>();
////                    ArrayList<String> nameList = new ArrayList<>();
////                    BufferedReader bufr = null;
////                    try {
////                        FileReader fr = new FileReader(file);
////                        bufr = new BufferedReader(fr);
////                        String line;
////
////                        try {
////                            while ((line = bufr.readLine()) != null) {
////                                if (line.contains("#EXTM3U")) continue;
////                                if (line.contains("#EXTINF")) {
////                                    String name = line.substring(line.indexOf(',') + 1);
////                                    nameList.add(name);
////                                    Log.d(TAG, "doInBackground: name: " + name);
////                                }
////                                if (line.contains("/storage/emulated")) {
////                                    pathList.add(line);
////                                    Log.d(TAG, "doInBackground: path: " + line);
////                                }
////                            }
////                        } catch (IOException e) {
////                            e.printStackTrace();
////                        }
////
////                    } catch (FileNotFoundException e) {
////                        e.printStackTrace();
////                    } finally {
////                        if (bufr != null) {
////                            try {
////                                bufr.close();
////                            } catch (IOException e) {
////                                e.printStackTrace();
////                            }
////                        }
////                    }
//                    return null;
//                }
//            }.execute();

    }

    public static final class Res {
        public static String getString(final Context context, @StringRes final int id) {
            return context.getResources().getString(id);
        }
    }

    public static final class IO {

        public static void Unzip(String zipFile, String targetDir) {
            int BUFFER = 4096;          //这里缓冲区我们使用4KB，
            String strEntry;            //保存每个zip的条目名称

            try {
                BufferedOutputStream dest;          //缓冲输出流
                FileInputStream fis = new FileInputStream(zipFile);
                ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
                ZipEntry entry;         //每个zip条目的实例

                while ((entry = zis.getNextEntry()) != null) {

                    try {
                        Log.i("Unzip: ", "=" + entry);

                        int count;
                        byte data[] = new byte[BUFFER];
                        strEntry = entry.getName();

                        File entryFile = new File(targetDir + strEntry);
                        File entryDir = new File(entryFile.getParent());

                        if (!entryDir.exists()) {
                            entryDir.mkdirs();
                        }

                        if (!entry.isDirectory()) {
                            FileOutputStream fos = new FileOutputStream(entryFile);

                            dest = new BufferedOutputStream(fos, BUFFER);
                            while ((count = zis.read(data, 0, BUFFER)) != -1) {
                                dest.write(data, 0, count);
                            }
                            dest.flush();
                            dest.close();
                        } else {
                            entryFile.mkdir();
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                zis.close();
            } catch (Exception cwj) {
                cwj.printStackTrace();
            }
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        public static void delFolder(String folderPath) {
            try {
                delAllFile(folderPath); //删除完里面所有内容
                java.io.File myFilePath = new java.io.File(folderPath);
                myFilePath.delete(); //删除空文件夹
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @SuppressWarnings({"UnusedReturnValue", "ConstantConditions", "ResultOfMethodCallIgnored"})
        private static boolean delAllFile(String path) {
            boolean flag = false;
            File file = new File(path);
            if (!file.exists()) {
                return flag;
            }
            if (!file.isDirectory()) {
                return flag;
            }
            String[] tempList = file.list();
            File temp = null;
            for (String aTempList : tempList) {
                if (path.endsWith(File.separator)) {
                    temp = new File(path + aTempList);
                } else {
                    temp = new File(path + File.separator + aTempList);
                }
                if (temp.isFile()) {
                    temp.delete();
                }
                if (temp.isDirectory()) {
                    delAllFile(path + "/" + aTempList);//先删除文件夹里面的文件
                    delFolder(path + "/" + aTempList);//再删除空文件夹
                    flag = true;
                }
            }
            return flag;
        }

        /**
         * 压缩一个文件夹
         */
        public static void zipDirectory(String path, String savePath) throws IOException {
            File file = new File(path);
            File zipFile = new File(savePath);
            Log.d(TAG, "zipDirectory: " + zipFile.getAbsolutePath());
            zipFile.createNewFile();
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
            zip(zos, file, file.getName());
            zos.flush();
            zos.close();
        }

        /**
         * @param zos  压缩输出流
         * @param file 当前需要压缩的文件
         * @param path 当前文件相对于压缩文件夹的路径
         */
        private static void zip(ZipOutputStream zos, File file, String path) throws IOException {
            // 首先判断是文件，还是文件夹，文件直接写入目录进入点，文件夹则遍历
            if (file.isDirectory()) {
                ZipEntry entry = new ZipEntry(path + File.separator);// 文件夹的目录进入点必须以名称分隔符结尾
                zos.putNextEntry(entry);
                File[] files = file.listFiles();
                for (File x : files) {
                    zip(zos, x, path + File.separator + x.getName());
                }
            } else {
                FileInputStream fis = new FileInputStream(file);// 目录进入点的名字是文件在压缩文件中的路径
                ZipEntry entry = new ZipEntry(path);
                zos.putNextEntry(entry);// 建立一个目录进入点

                int len = 0;
                byte[] buf = new byte[1024];
                while ((len = fis.read(buf)) != -1) {
                    zos.write(buf, 0, len);
                }
                zos.flush();
                fis.close();
                zos.closeEntry();// 关闭当前目录进入点，将输入流移动下一个目录进入点
            }
        }
    }
}
