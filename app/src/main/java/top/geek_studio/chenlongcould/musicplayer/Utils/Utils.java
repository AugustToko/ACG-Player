/*
 * ************************************************************
 * 文件：Utils.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月25日 08:45:54
 * 上次修改时间：2018年12月25日 08:45:35
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Utils;

import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.audiofx.AudioEffect;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Collections;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

@SuppressWarnings("WeakerAccess")
public final class Utils {

    private Utils() {
    }

    public static final class Audio {

        private final static MediaMetadataRetriever sMediaMetadataRetriever = new MediaMetadataRetriever();

        private static final String TAG = "Audio";

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

//            //检测不支持封面的音乐类型
//            if (mediaUri.contains("ogg") || mediaUri.contains("flac")) {
//                return BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.ic_audiotrack_24px);
//            }

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
    }

    public static final class Ui {

        private static final String TAG = "Ui";

        public static int POSITION = 200;

        public static void inDayNightSet(final SharedPreferences sharedPreferences) {
            if (sharedPreferences.getBoolean(Values.SharedPrefsTag.AUTO_NIGHT_MODE, false)) {
                Values.Color.TEXT_COLOR = Values.Color.TEXT_COLOR_IN_NIGHT;
            } else {
                Values.Color.TEXT_COLOR = Values.Color.TEXT_COLOR_IN_DAY;
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
         * @param context      context
         * @param toolBarColor appBarLayout
         * @param toolbar      toolbar
         */
        public static void setAppBarColor(final Activity context, final AppBarLayout toolBarColor, final android.support.v7.widget.Toolbar toolbar) {
            SharedPreferences mDefPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            toolBarColor.setBackgroundColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_COLOR, Color.parseColor("#008577")));
            toolbar.setBackgroundColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_COLOR, Color.parseColor("#008577")));
            context.getWindow().setNavigationBarColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_DARK_COLOR, Color.parseColor("#00574B")));
        }

        public static void setPlayButtonNowPlaying() {
            if (!Data.sActivities.isEmpty()) {
                MainActivity activity = (MainActivity) Data.sActivities.get(0);
                activity.getMusicDetailFragment().getHandler().sendEmptyMessage(Values.HandlerWhat.SET_BUTTON_PLAY);

                if (Values.CurrentData.UI_MODE.equals(Values.CurrentData.MODE_CAR)) {
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

                if (Values.CurrentData.UI_MODE.equals(Values.CurrentData.MODE_CAR)) {
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

        public static boolean ANIMATION_IN_DETAIL_DONE = true;

        /**
         * 设置背景与动画 (blur style)
         *
         * @param activity if use fragment may case {@link java.lang.NullPointerException}, glide will call {@link Fragment#getActivity()}
         */
        public static void setBlurEffect(@NonNull final MainActivity activity, @NonNull final byte[] bitmap, @NonNull final ImageView primaryBackground, @NonNull final ImageView primaryBackgroundBef, final TextView nextText) {
            new Handler(Looper.getMainLooper()).post(() -> {

                ANIMATION_IN_DETAIL_DONE = false;
                primaryBackground.setVisibility(View.VISIBLE);

                Palette.from(BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length)).generate(p -> {
                    if (p != null) {
//                        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), Color.BLACK, p.getVibrantColor(Color.parseColor(Values.Color.NOT_VERY_BLACK)));
//                        animator.setDuration(300);
//                        animator.addUpdateListener(animation -> nextText.setTextColor((Integer) animation.getAnimatedValue()));
//                        animator.start();
                        nextText.setTextColor(p.getVibrantColor(Color.parseColor(Values.Color.TEXT_COLOR)));
                    }
                });

                //clear
                GlideApp.with(activity).clear(primaryBackgroundBef);
                if (Values.Style.DETAIL_BACKGROUND.equals(Values.Style.STYLE_BACKGROUND_BLUR)) {
                    primaryBackgroundBef.post(() -> GlideApp.with(activity)
                            .load(bitmap)
                            .dontAnimate()
                            .apply(bitmapTransform(Data.sBlurTransformation))
                            .into(primaryBackgroundBef));
                } else {
                    Palette.from(BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length)).generate(p -> {
                        if (p != null) {
                            primaryBackgroundBef.setBackgroundColor(p.getVibrantColor(Color.TRANSPARENT));
                        }
                    });

                }

                Animator animator = ViewAnimationUtils.createCircularReveal(
                        primaryBackgroundBef, primaryBackgroundBef.getWidth() / 2, POSITION,
                        0,
                        (float) Math.hypot(primaryBackgroundBef.getWidth(), primaryBackgroundBef.getHeight()));

                animator.setInterpolator(new AccelerateInterpolator());
                animator.setDuration(700);
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        GlideApp.with(activity).clear(primaryBackground);
                        if (Values.Style.DETAIL_BACKGROUND.equals(Values.Style.STYLE_BACKGROUND_BLUR)) {
                            GlideApp.with(activity)
                                    .load(bitmap)
                                    .dontAnimate()
                                    .apply(bitmapTransform(Data.sBlurTransformation))
                                    .into(primaryBackground);
                        } else {
                            Palette.from(BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length)).generate(p -> {
                                if (p != null) {
                                    primaryBackground.setBackgroundColor(p.getVibrantColor(Color.TRANSPARENT));
                                }
                            });
                        }

                        primaryBackground.setVisibility(View.GONE);
                        ANIMATION_IN_DETAIL_DONE = true;
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

        public static void setBlurEffect(@NonNull final MainActivity activity, @NonNull final Bitmap bitmap, @NonNull final ImageView primaryBackground, @NonNull final ImageView primaryBackgroundBef, final TextView nextText) {
            new Handler(Looper.getMainLooper()).post(() -> {
                ANIMATION_IN_DETAIL_DONE = false;
                primaryBackground.setVisibility(View.VISIBLE);

                Palette.from(bitmap).generate(p -> {
                    if (p != null) {
//                        nextText.clearAnimation();
//                        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), Color.BLACK, p.getVibrantColor(Color.parseColor(Values.Color.NOT_VERY_BLACK)));
//                        animator.setDuration(300);
//                        animator.addUpdateListener(animation -> nextText.setTextColor((Integer) animation.getAnimatedValue()));
                        nextText.setTextColor(p.getVibrantColor(Color.parseColor(Values.Color.TEXT_COLOR)));
                    }
                });

                //clear
                GlideApp.with(activity).clear(primaryBackgroundBef);
                if (Values.Style.DETAIL_BACKGROUND.equals(Values.Style.STYLE_BACKGROUND_BLUR)) {
                    primaryBackgroundBef.post(() -> GlideApp.with(activity)
                            .load(bitmap)
                            .dontAnimate()
                            .apply(bitmapTransform(Data.sBlurTransformation))
                            .into(primaryBackgroundBef));
                } else {
                    Palette.from(bitmap).generate(p -> {
                        if (p != null) {
                            primaryBackgroundBef.setBackgroundColor(p.getVibrantColor(Color.TRANSPARENT));
                        }
                    });
                }

                Animator animator = ViewAnimationUtils.createCircularReveal(
                        primaryBackgroundBef, primaryBackgroundBef.getWidth() / 2, POSITION,
                        0,
                        (float) Math.hypot(primaryBackgroundBef.getWidth(), primaryBackgroundBef.getHeight()));

                animator.setInterpolator(new AccelerateInterpolator());
                animator.setDuration(700);
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        GlideApp.with(activity).clear(primaryBackground);
                        if (Values.Style.DETAIL_BACKGROUND.equals(Values.Style.STYLE_BACKGROUND_BLUR)) {
                            GlideApp.with(activity)
                                    .load(bitmap)
                                    .dontAnimate()
                                    .apply(bitmapTransform(Data.sBlurTransformation))
                                    .into(primaryBackground);
                        } else {
                            Palette.from(bitmap).generate(p -> {
                                if (p != null) {
                                    primaryBackground.setBackgroundColor(p.getVibrantColor(Color.TRANSPARENT));
                                }
                            });
                        }

                        primaryBackground.setVisibility(View.GONE);
                        ANIMATION_IN_DETAIL_DONE = true;
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
//
//
//
//                    return null;
//                }
//            }.execute();

    }

    public static final class Res {
        public static String getString(final Context context, @StringRes final int id) {
            return context.getResources().getString(id);
        }
    }
}
