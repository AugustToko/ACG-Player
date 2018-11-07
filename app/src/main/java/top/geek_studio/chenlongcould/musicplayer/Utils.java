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
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;

public class Utils {

    public static class Audio {
        private static MediaMetadataRetriever sMediaMetadataRetriever = new MediaMetadataRetriever();

        /**
         * 获取封面
         *
         * @param mediaUri mp3 path
         */
        public static Bitmap getMp3Cover(String mediaUri) {
            sMediaMetadataRetriever.setDataSource(mediaUri);
            byte[] picture = sMediaMetadataRetriever.getEmbeddedPicture();
            return BitmapFactory.decodeByteArray(picture, 0, picture.length);
        }

        public static byte[] getAlbumByteImage(String path) {
            sMediaMetadataRetriever.setDataSource(path);
            return sMediaMetadataRetriever.getEmbeddedPicture();
        }

        public static void preLoadAlubmImage(Context context, String path) {
            sMediaMetadataRetriever.setDataSource(path);
            GlideApp.with(context).load(sMediaMetadataRetriever.getEmbeddedPicture()).override(50, 50).preload();
        }

        public static String getAlbumText(String path) {
            sMediaMetadataRetriever.setDataSource(path);
            return sMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        }

        /**
         * 加载封面
         *
         * @param mediaUri MP3文件路径
         */
        public static void loadingCoverIntoImageView(Context context, ImageView musicCover, String mediaUri) {
            sMediaMetadataRetriever.setDataSource(mediaUri);
            byte[] picture = sMediaMetadataRetriever.getEmbeddedPicture();
            GlideApp.with(context).load(picture).transition(DrawableTransitionOptions.withCrossFade()).override(50, 50).into(musicCover);
        }

    }

    public static class Ui {
        static void setPrimaryColor(int color, View... views) {
            for (View v : views) {
                v.setBackgroundColor(color);
            }
        }

        public static void setNowPlaying(MainActivity activity) {
            // TODO: 2018/11/6 need get albumPic primaryColor
            activity.runOnUiThread(activity::setCurrentSongInfoPlay);
        }

        public static void setNowNotPlaying(MainActivity activity) {
            activity.runOnUiThread(activity::setCurrentSongInfoStop);
        }

        public static void setInfoBarBackgroundBlack() {
            ((MainActivity) Data.sActivities.get(0)).getNowPlayingBody().setBackgroundResource(R.color.notVeryBlack);
            Values.HAS_SET_INFO_BAR_BACKGROUND_BACK = true;
        }

        public static AlertDialog createMessageDialog(Activity context, String title, String message) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setCancelable(true);
            return builder.create();
        }

        public static void fastToast(Context context, String content) {
            Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
        }

        public static int getBright(Bitmap bm) {
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
    }

}
