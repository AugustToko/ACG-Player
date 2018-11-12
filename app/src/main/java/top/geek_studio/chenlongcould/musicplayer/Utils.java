/*
 * ************************************************************
 * 文件：Utils.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月05日 17:54:16
 * 上次修改时间：2018年11月05日 17:53:42
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.io.IOException;
import java.util.Random;

import jp.wasabeef.glide.transformations.BlurTransformation;
import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.MusicDetailActivity;

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
         *
         * @param mediaUri mp3 path
         */
        public static Bitmap getMp3Cover(@NonNull String mediaUri) {
            if (mediaUri.contains("ogg")) {
                return null;
            }
            sMediaMetadataRetriever.setDataSource(mediaUri);
            byte[] picture = sMediaMetadataRetriever.getEmbeddedPicture();
            try {
                return BitmapFactory.decodeByteArray(picture, 0, picture.length);
            } catch (NullPointerException e) {
                e.printStackTrace();
                return null;
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
        public static void loadingCoverIntoImageView(@NonNull Context context, @Nullable ImageView musicCover, @NonNull String mediaUri) {
            sMediaMetadataRetriever.setDataSource(mediaUri);
            byte[] picture = sMediaMetadataRetriever.getEmbeddedPicture();
            GlideApp.with(context).load(picture).transition(DrawableTransitionOptions.withCrossFade()).override(50, 50).into(musicCover);
        }

        public static void doesNextHasMusic() throws IOException {
            if (Data.sNextWillPlaySongPath != null) {
                Data.sMusicBinder.resetMusic();
                try {
                    Data.sMusicBinder.setDataSource(Data.sNextWillPlaySongPath);
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
            MainActivity activity = (MainActivity) Data.sActivities.get(0);
            if (READY) {            //default: true
                new Thread(() -> {
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

                        //first set backgroundImage, then set bg(layout) black. To crossFade more Smooth
                        activity.setCurrentSongInfo(musicName, albumName, path, cover);
                        activity.setButtonTypePlay();

                        Data.sCurrentMusicAlbum = albumName;
                        Data.sCurrentMusicName = musicName;
                        Data.sCurrentMusicBitmap = cover;

                        Values.MUSIC_PLAYING = true;
                        Values.HAS_PLAYED = true;
                        Values.CURRENT_MUSIC_INDEX = index;
                        Values.CURRENT_MUSIC_INDEX = index;
                        Values.CURRENT_SONG_PATH = path;

                        if (Data.sActivities.size() >= 2) {
                            MusicDetailActivity musicDetailActivity = (MusicDetailActivity) Data.sActivities.get(1);
                            musicDetailActivity.setButtonTypePlay();
                            musicDetailActivity.setCurrentSongInfo(musicName, albumName, getAlbumByteImage(path));
                        }

                        try {
                            Data.sMusicBinder.setDataSource(path);
                            Data.sMusicBinder.prepare();
                            Data.sMusicBinder.playMusic();          //has played, now playing

                            if (Data.sActivities.size() >= 2) {
                                MusicDetailActivity musicDetailActivity = (MusicDetailActivity) Data.sActivities.get(1);
                                MusicDetailActivity.NotLeakHandler notLeakHandler = musicDetailActivity.getHandler();
                                notLeakHandler.sendEmptyMessage(Values.INIT_SEEK_BAR);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            Data.sMusicBinder.resetMusic();
                        }
                        READY = true;
                    } else {
                        activity.runOnUiThread(() -> Utils.Ui.createMessageDialog(activity, "Error", "MUSIC_DATA_INIT_FAIL").show());
                    }
                }).start();
            } else {
                activity.runOnUiThread(() -> Ui.fastToast(activity, "Preparing..."));
            }
        }

    }

    public static class Ui {
        //        static void setPrimaryColor(int color, View... views) {
//            for (View v : views) {
//                v.setBackgroundColor(color);
//            }
//        }
        public static void createEditTextDialog(Context context, String title, boolean cancelAble, DialogInterface.OnClickListener onEnterListener) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            EditText et = new EditText(context);
            et.setHint("Play List 1");
            et.setMaxLines(1);
            et.setSingleLine(true);
            builder.setView(et);
            builder.setCancelable(cancelAble);
            builder.setNegativeButton("Cancel", null);
            builder.setNeutralButton("Enter", onEnterListener);
        }

        public static void setNowPlaying() {
            if (Data.sActivities.size() != 0) {
                MainActivity activity = (MainActivity) Data.sActivities.get(0);
                activity.runOnUiThread(() -> {
                    // TODO: 2018/11/6 get albumPic primaryColor
                    MainActivity mainActivity = (MainActivity) Data.sActivities.get(0);
                    mainActivity.setButtonTypePlay();

                    if (Data.sActivities.size() >= 2) {
                        MusicDetailActivity musicDetailActivity = (MusicDetailActivity) Data.sActivities.get(1);
                        musicDetailActivity.setButtonTypePlay();
                    }
                });
            }
        }

        public static void setNowNotPlaying(@NonNull Context context) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(Values.PKG_NAME, Values.BroadCast.ReceiverOnMusicPause));
            context.sendBroadcast(intent);
        }

//        public static void setInfoBarBackgroundBlack() {
//            ((MainActivity) Data.sActivities.get(0)).getNowPlayingBody().setBackgroundResource(R.color.notVeryBlack);
//            Values.HAS_SET_INFO_BAR_BACKGROUND_BACK = true;
//        }
//
//        public static void setInfoBarBackgroundWhite() {
//            ((MainActivity) Data.sActivities.get(0)).getNowPlayingBody().setBackgroundColor(Color.WHITE);
//            Values.HAS_SET_INFO_BAR_BACKGROUND_BACK = false;
//        }

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

        //ogg not support cover, so do not need to set bg
//        public static void firstSetBg2InfoBar(String path) {
//            if (!path.contains("ogg")) {
//                //使背景变黑, 使图片过渡更自然
//                Utils.Ui.setInfoBarBackgroundBlack();
//            }
//        }

//        public static void setDefBg2InfoBar() {
//            Utils.Ui.setInfoBarBackgroundBlack();
//        }

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

        public static void setBlurEffect(@NonNull Activity context, @Nullable byte[] bitmap, @NonNull ImageView view) {
            context.runOnUiThread(() -> GlideApp.with(context)
                    .load(bitmap)
                    .apply(bitmapTransform(new BlurTransformation(15, 30)))
                    .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                    .into(view));
        }

        public static void setBlurEffect(@NonNull Activity context, @Nullable Bitmap bitmap, @NonNull ImageView view) {
            context.runOnUiThread(() -> GlideApp.with(context)
                    .load(bitmap)
                    .apply(bitmapTransform(new BlurTransformation(15, 30)))
                    .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                    .into(view));
        }

    }

}
