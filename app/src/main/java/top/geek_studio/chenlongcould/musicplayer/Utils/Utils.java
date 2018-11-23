/*
 * ************************************************************
 * 文件：Utils.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月23日 11:17:30
 * 上次修改时间：2018年11月23日 11:16:10
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Utils;

import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.io.IOException;
import java.util.Random;

import jp.wasabeef.glide.transformations.BlurTransformation;
import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.MusicDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public final class Utils {

    public static class Audio {
        private static MediaMetadataRetriever sMediaMetadataRetriever = new MediaMetadataRetriever();

        /**
         * 检测播放器是否准备完毕 (默认进app 为true)
         */
        private static volatile boolean READY = true;

        /**
         * 获取封面
         * @param mediaUri mp3 path
         */
        @NonNull
        public static Bitmap getMp3Cover(@NonNull String mediaUri) {

            //检测不支持封面的音乐类型
            if (mediaUri.contains("ogg")) {
                return BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.ic_audiotrack_24px);
            }

            sMediaMetadataRetriever.setDataSource(mediaUri);
            byte[] picture = sMediaMetadataRetriever.getEmbeddedPicture();
            try {
                return BitmapFactory.decodeByteArray(picture, 0, picture.length);
            } catch (NullPointerException e) {
                e.printStackTrace();
                return BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.ic_audiotrack_24px);
            }
        }

        public static byte[] getAlbumByteImage(@NonNull String path) {
            sMediaMetadataRetriever.setDataSource(path);
            return sMediaMetadataRetriever.getEmbeddedPicture();
        }

        public static void preLoadAlubmImage(@NonNull Context context, @NonNull String path) {
            sMediaMetadataRetriever.setDataSource(path);
            GlideApp.with(context).load(sMediaMetadataRetriever.getEmbeddedPicture()).override(50, 50).preload();
        }

        public static String getAlbumText(@NonNull String path) {
            sMediaMetadataRetriever.setDataSource(path);
            return sMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        }

        /**
         * 加载封面
         *
         * @param mediaUri MP3文件路径
         */
        public static void loadingCoverIntoImageView(@NonNull Context context, @NonNull ImageView musicCover, @NonNull String mediaUri) {
            sMediaMetadataRetriever.setDataSource(mediaUri);
            byte[] picture = sMediaMetadataRetriever.getEmbeddedPicture();
            GlideApp.with(context).load(picture).transition(DrawableTransitionOptions.withCrossFade()).override(50, 50).into(musicCover);
        }

        /**
         * 当下一首歌曲存在(被手动指定时), auto-next-play and next-play will call this method
         */
        public static void doesNextHasMusic() {
            if (Data.sNextWillPlayIndex != -1) {
                Data.sMusicBinder.resetMusic();

                String path = Data.sMusicItems.get(Data.sNextWillPlayIndex).getMusicPath();
                String musicName = Data.sMusicItems.get(Data.sNextWillPlayIndex).getMusicName();
                String albumName = Data.sMusicItems.get(Data.sNextWillPlayIndex).getMusicAlbum();

                Bitmap cover = Utils.Audio.getMp3Cover(path);

                Ui.setPlayButtonNowPlaying();

                MainActivity mainActivity = (MainActivity) Data.sActivities.get(0);
                mainActivity.setCurrentSongInfo(musicName, albumName, path, cover);

                if (Data.sActivities.size() >= 2) {
                    MusicDetailActivity musicDetailActivity = (MusicDetailActivity) Data.sActivities.get(1);
                    musicDetailActivity.setCurrentSongInfo(musicName, albumName, Utils.Audio.getAlbumByteImage(path));
                    musicDetailActivity.getRecyclerView().scrollToPosition(Data.sNextWillPlayIndex);
                    musicDetailActivity.getSeekBar().getThumb().setColorFilter(cover.getPixel(cover.getWidth() / 2, cover.getHeight() / 2), PorterDuff.Mode.SRC_ATOP);
                }

                Values.MUSIC_PLAYING = true;
                Values.HAS_PLAYED = true;
                Values.CurrentData.CURRENT_MUSIC_INDEX = Data.sNextWillPlayIndex;
                Values.CurrentData.CURRENT_SONG_PATH = path;

                Data.sNextWillPlayIndex = -1;

                try {
                    Data.sMusicBinder.setDataSource(path);
                    Data.sMusicBinder.prepare();
                    Data.sMusicBinder.playMusic();
                } catch (IOException e) {
                    e.printStackTrace();
                    Data.sMusicBinder.resetMusic();
                }
            }
        }

        /**
         * play_type: random, without history clear
         */
        //shufflePlayback
        public static void shufflePlayback() {
            if (READY) {            //default: true
                if (Values.MUSIC_DATA_INIT_DONE) {
                    READY = false;
                    Data.sMusicBinder.resetMusic();

                    Random random = new Random();
                    int index = random.nextInt(Data.sMusicItems.size() - 1);
                    String path = Data.sMusicItems.get(index).getMusicPath();
                    String musicName = Data.sMusicItems.get(index).getMusicName();
                    String albumName = Data.sMusicItems.get(index).getMusicAlbum();

                    Data.sHistoryPlayIndex.add(index);

                    Bitmap cover = Utils.Audio.getMp3Cover(path);
                    Data.saveGlobalCurrentData(musicName, albumName, cover);

                    Values.MUSIC_PLAYING = true;
                    Values.HAS_PLAYED = true;
                    Values.CurrentData.CURRENT_MUSIC_INDEX = index;
                    Values.CurrentData.CURRENT_SONG_PATH = path;

                    Ui.setPlayButtonNowPlaying();

                    if (Data.sActivities.size() >= 1) {
                        MainActivity activity = (MainActivity) Data.sActivities.get(0);
                        //first set backgroundImage, then set bg(layout) black. To crossFade more Smooth
                        activity.setCurrentSongInfo(musicName, albumName, path, cover);
                    }

                    if (Data.sActivities.size() >= 2) {
                        MusicDetailActivity musicDetailActivity = (MusicDetailActivity) Data.sActivities.get(1);
                        musicDetailActivity.setCurrentSongInfo(musicName, albumName, getAlbumByteImage(path));
                        //设置seekBar颜色
                        musicDetailActivity.getSeekBar().getThumb().setColorFilter(cover.getPixel(cover.getWidth() / 2, cover.getHeight() / 2), PorterDuff.Mode.SRC_ATOP);
                    }

                    try {
                        Data.sMusicBinder.setDataSource(path);
                        Data.sMusicBinder.prepare();
                        Data.sMusicBinder.playMusic();          //has played, now playing

                        if (Data.sActivities.size() >= 2) {
                            MusicDetailActivity musicDetailActivity = (MusicDetailActivity) Data.sActivities.get(1);
                            MusicDetailActivity.NotLeakHandler notLeakHandler = musicDetailActivity.getHandler();
                            //music after mediaPlayer.setDataSource, because of "Values.HandlerWhat.INIT_SEEK_BAR"
                            notLeakHandler.sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        Data.sMusicBinder.resetMusic();
                    }
                    READY = true;
                } else {
                    if (Data.sActivities.size() >= 1) {
                        MainActivity activity = (MainActivity) Data.sActivities.get(0);
                        activity.runOnUiThread(() -> Utils.Ui.createMessageDialog(activity, "Error", "MUSIC_DATA_INIT_FAIL").show());
                    }
                }
            } else {
                if (Data.sActivities.size() >= 1) {
                    MainActivity activity = (MainActivity) Data.sActivities.get(0);
                    Ui.fastToast(activity, "Preparing...");
                }
            }
        }

    }

    public static class Ui {

        /**
         * set color (style)
         *
         * @param context      context
         * @param toolBarColor appBarLayout
         * @param toolbar      toolbar
         */
        public static void setAppBarColor(Activity context, AppBarLayout toolBarColor, Toolbar toolbar) {
            SharedPreferences mDefPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            toolBarColor.setBackgroundColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_COLOR, Color.parseColor("#008577")));
            toolbar.setBackgroundColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_COLOR, Color.parseColor("#008577")));
            context.getWindow().setNavigationBarColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_DARK_COLOR, Color.parseColor("#00574B")));
        }

        public static void setAppBarColor(Activity context, AppBarLayout toolBarColor, android.support.v7.widget.Toolbar toolbar) {
            SharedPreferences mDefPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            toolBarColor.setBackgroundColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_COLOR, Color.parseColor("#008577")));
            toolbar.setBackgroundColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_COLOR, Color.parseColor("#008577")));
            context.getWindow().setNavigationBarColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_DARK_COLOR, Color.parseColor("#00574B")));
        }

        public static void setPlayButtonNowPlaying() {
            if (Data.sActivities.size() != 0) {
                MainActivity activity = (MainActivity) Data.sActivities.get(0);
                activity.runOnUiThread(() -> {
                    MainActivity mainActivity = (MainActivity) Data.sActivities.get(0);
                    mainActivity.setButtonTypePlay();
                    if (Data.sActivities.size() >= 2) {
                        MusicDetailActivity musicDetailActivity = (MusicDetailActivity) Data.sActivities.get(1);
                        musicDetailActivity.setButtonTypePlay();
                    }
                });
            }
        }

        public static AlertDialog createMessageDialog(@NonNull Activity context, @NonNull String title, @NonNull String message) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setCancelable(true);
            return builder.create();
        }

        public static void fastToast(@NonNull Context context, @NonNull String content) {
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

        /**
         * 设置背景与动画 (blur style)
         */
        public static void setBlurEffect(@NonNull Activity context, @Nullable byte[] bitmap, @NonNull ImageView primaryBackground, @NonNull ImageView primaryBackgroundDown) {
            primaryBackground.setVisibility(View.VISIBLE);
            context.runOnUiThread(() -> {
                primaryBackgroundDown.post(() -> GlideApp.with(context)
                        .load(bitmap)
                        .dontAnimate()
                        .apply(bitmapTransform(new BlurTransformation(15, 30)))
                        .into(primaryBackgroundDown));

                primaryBackgroundDown.post(() -> {
                    Animator animator = ViewAnimationUtils.createCircularReveal(
                            primaryBackgroundDown, primaryBackgroundDown.getWidth() / 2, 170,
                            0,
                            (float) Math.hypot(primaryBackgroundDown.getWidth(), primaryBackgroundDown.getHeight()));
                    animator.setInterpolator(new AccelerateInterpolator());
                    animator.setDuration(700);
                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            GlideApp.with(context)
                                    .load(bitmap)
                                    .dontAnimate()
                                    .apply(bitmapTransform(new BlurTransformation(15, 30)))
                                    .into(primaryBackground);
                            primaryBackground.setVisibility(View.INVISIBLE);
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
            });
        }

        public static void setBlurEffect(@NonNull Activity context, @Nullable Bitmap bitmap, @NonNull ImageView primaryBackground, @NonNull ImageView primaryBackgroundDown) {
            primaryBackground.setVisibility(View.VISIBLE);
            context.runOnUiThread(() -> {
                primaryBackgroundDown.post(() -> GlideApp.with(context)
                        .load(bitmap)
                        .dontAnimate()
                        .apply(bitmapTransform(new BlurTransformation(15, 30)))
                        .into(primaryBackgroundDown));

                primaryBackgroundDown.post(() -> {
                    Animator animator = ViewAnimationUtils.createCircularReveal(primaryBackgroundDown, primaryBackgroundDown.getWidth() / 2, 170, 0,
                            (float) Math.hypot(primaryBackgroundDown.getWidth(), primaryBackgroundDown.getHeight()));
                    animator.setInterpolator(new AccelerateInterpolator());
                    animator.setDuration(700);
                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            primaryBackgroundDown.post(() -> GlideApp.with(context)
                                    .load(bitmap)
                                    .dontAnimate()
                                    .apply(bitmapTransform(new BlurTransformation(15, 30)))
                                    .into(primaryBackground));
                            primaryBackground.setVisibility(View.INVISIBLE);
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

            });

        }

        public static boolean isColorLight(@ColorInt int color) {
            double darkness = 1.0D - (0.299D * (double) Color.red(color) + 0.587D * (double) Color.green(color) + 0.114D * (double) Color.blue(color)) / 255.0D;
            return darkness < 0.4D;
        }
    }

    /**
     * start activity, broadcast, service...
     */
    public static final class SendSomeThing {

        public static final String TAG = "SendSomeThing";

        /**
         * send broadcast by pause
         * @param context context
         */
        public static void sendPause(Context context) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(context.getPackageName(), Values.BroadCast.ReceiverOnMusicPause));
            context.sendBroadcast(intent, Values.Permission.BROAD_CAST);
        }

        public static void sendPlay(Context context, int playType) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(context.getPackageName(), Values.BroadCast.ReceiverOnMusicPlay));
            Log.d(TAG, "sendPlay: " + context.getPackageName());
            intent.putExtra("play_type", playType);
            context.sendBroadcast(intent, Values.Permission.BROAD_CAST);
        }
    }
}
