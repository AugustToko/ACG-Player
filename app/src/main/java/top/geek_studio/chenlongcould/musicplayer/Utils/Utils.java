/*
 * ************************************************************
 * 文件：Utils.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月07日 16:30:28
 * 上次修改时间：2019年01月07日 16:29:51
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
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
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
import android.support.v4.app.Fragment;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.ThemeActivity;
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
            appBar.setBackgroundColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_COLOR, Color.parseColor("#008577")));
            toolbar.setBackgroundColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_COLOR, Color.parseColor("#008577")));
            activity.getWindow().setNavigationBarColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_DARK_COLOR, Color.parseColor("#00574B")));
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

        /**
         * 设置背景与动画 (blur style)
         *
         * @param activity if use fragment may case {@link java.lang.NullPointerException}, glide will call {@link Fragment#getActivity()}
         */
        public static void setBlurEffect(@NonNull final MainActivity activity, @NonNull final byte[] bitmap, @NonNull final ImageView primaryBackground, @NonNull final ImageView primaryBackgroundBef, final TextView nextText) {
            new Handler(Looper.getMainLooper()).post(() -> {

                ANIMATION_IN_DETAIL_DONE.set(false);
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

        public static void setBlurEffect(@NonNull final MainActivity activity, @NonNull final Bitmap bitmap, @NonNull final ImageView primaryBackground, @NonNull final ImageView primaryBackgroundBef, final TextView nextText) {
            new Handler(Looper.getMainLooper()).post(() -> {
                ANIMATION_IN_DETAIL_DONE.set(false);
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

    public static final class ThemeUtils {
        public static boolean checkTheme(final String themePath) {
            final File file = new File(themePath);
            if (!file.exists() || file.isFile() || file.listFiles().length == 0) {
                Log.e(TAG, "checkTheme: theme file error");
                return false;
            }

            final File detailFile = new File(themePath + File.separatorChar + ThemeStore.DETAIL_FILE_NAME);
            if (!detailFile.exists() || detailFile.isDirectory() || detailFile.length() == 0) {
                Log.e(TAG, "checkTheme: detail file error");
                return false;
            }

            final File imgDir = new File(themePath + File.separatorChar + ThemeStore.DIR_IMG);
            if (!imgDir.exists() || imgDir.isFile() || imgDir.listFiles().length == 0) {
                Log.e(TAG, "checkTheme: img dir error");
                return false;
            }

            final File ico = new File(themePath + File.separatorChar + ThemeStore.ICO_FILE_NAME.toLowerCase());
            if (!ico.exists() || ico.isDirectory()) {
                Log.e(TAG, "checkTheme: ico error");
                return false;
            }
            return true;
        }

        public static File getThemeFile(@NonNull final Context context, final String themeId) {
            return new File(context.getExternalFilesDir(ThemeStore.DIR_NAME).getAbsolutePath() + File.separatorChar + themeId);
        }

        @Nullable
        public static ThemeActivity.Theme fileToTheme(final File f) {
            if (!checkTheme(f.getAbsolutePath())) return null;

            try {

                if (f.isDirectory()) {
                    final File detailText = new File(f.getPath() + File.separatorChar + ThemeStore.DETAIL_FILE_NAME);

                    //temp
                    String title = "null";
                    String date = "null";
                    String nav_name = "null";
                    String author = "null";
                    String support_area = "null";
                    String primary_color = "null";
                    String accent_color = "null";
                    String primary_color_dark = "null";
                    String thumbnail = "null";
                    String select = "null";
                    String path = f.getPath();

                    final BufferedReader bufferedReader = new BufferedReader(new FileReader(detailText));
                    String line;

                    int items = 0;
                    while ((line = bufferedReader.readLine()) != null) {
                        if (line.contains(ThemeStore.ThemeColumns.AUTHOR)) {
                            author = line.split(":")[1];
                            Log.d(TAG, "doInBackground: " + author + " @ " + detailText.getPath());
                            items++;
                        }

                        if (line.contains(ThemeStore.ThemeColumns.TITLE)) {
                            title = line.split(":")[1];
                            Log.d(TAG, "doInBackground: " + title + " @ " + detailText.getPath());
                            items++;
                        }

                        if (line.contains(ThemeStore.ThemeColumns.NAV_NAME)) {
                            nav_name = line.split(":")[1];
                            Log.d(TAG, "doInBackground: " + nav_name + " @ " + detailText.getPath());
                            items++;
                        }

                        if (line.contains(ThemeStore.ThemeColumns.THUMBNAIL)) {
                            thumbnail = f.getPath() + File.separatorChar + line.split(":")[1];
                            Log.d(TAG, "doInBackground: " + thumbnail + " @ " + detailText.getPath());
                            items++;
                        }

                        if (line.contains(ThemeStore.ThemeColumns.SUPPORT_AREA)) {
                            support_area = line.split(":")[1];
                            Log.d(TAG, "doInBackground: " + support_area + " @ " + detailText.getPath());
                            items++;
                        }

                        if (line.contains(ThemeStore.ThemeColumns.PRIMARY_COLOR)) {
                            primary_color = line.split(":")[1];
                            Log.d(TAG, "doInBackground: " + primary_color);
                            items++;
                        }

                        if (line.contains(ThemeStore.ThemeColumns.PRIMARY_COLOR_DARK)) {
                            primary_color_dark = line.split(":")[1];
                            Log.d(TAG, "doInBackground: " + select);
                            items++;
                        }

                        if (line.contains(ThemeStore.ThemeColumns.ACCENT_COLOR)) {
                            accent_color = line.split(":")[1];
                            Log.d(TAG, "doInBackground: " + select);
                            items++;
                        }

                        if (line.contains(ThemeStore.ThemeColumns.DATE)) {
                            date = line.split(":")[1];
                            Log.d(TAG, "doInBackground: " + date);
                            items++;
                        }

                        if (line.contains(ThemeStore.ThemeColumns.SELECT)) {
                            select = line.split(":")[1];
                            Log.d(TAG, "doInBackground: " + select);
                            items++;
                        }
                    }

                    if (items >= ThemeStore.MIN_ITEM) {
                        Log.d(TAG, "fileToTheme: theme id is: " + f.getName());
                        return new ThemeActivity.Theme.Builder(f.getName())
                                .setAccentColor(accent_color)
                                .setAuthor(author)
                                .setDate(date)
                                .setNavName(nav_name)
                                .setPath(path).setPrimaryColorDark(primary_color_dark)
                                .setSupportArea(support_area)
                                .setSelect(select)
                                .setTitle(title)
                                .setThumbnail(thumbnail)
                                .setPrimaryColor(primary_color)
                                .build();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
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
