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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

final class Utils {

    static class Audio {
        private static MediaMetadataRetriever sMediaMetadataRetriever = new MediaMetadataRetriever();

        /**
         * 获取封面
         *
         * @param mediaUri mp3 path
         */
        static Bitmap getMp3Cover(String mediaUri) {
            sMediaMetadataRetriever.setDataSource(mediaUri);
            byte[] picture = sMediaMetadataRetriever.getEmbeddedPicture();
            return BitmapFactory.decodeByteArray(picture, 0, picture.length);
        }

        static byte[] getAlbumByteImage(String path) {
            sMediaMetadataRetriever.setDataSource(path);
            return sMediaMetadataRetriever.getEmbeddedPicture();
        }

        static void preLoadAlubmImage(Context context, String path) {
            sMediaMetadataRetriever.setDataSource(path);
            GlideApp.with(context).load(sMediaMetadataRetriever.getEmbeddedPicture()).override(50, 50).preload();
        }

        static String getAlbumText(String path) {
            sMediaMetadataRetriever.setDataSource(path);
            return sMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        }

        /**
         * 加载封面
         *
         * @param mediaUri MP3文件路径
         */
        static void loadingCoverIntoImageView(Context context, ImageView musicCover, String mediaUri) {
            sMediaMetadataRetriever.setDataSource(mediaUri);
            byte[] picture = sMediaMetadataRetriever.getEmbeddedPicture();
            GlideApp.with(context).load(picture).transition(DrawableTransitionOptions.withCrossFade()).override(50, 50).into(musicCover);
        }

    }

    static class Ui {
        static void setPrimaryColor(int color, View... views) {
            for (View v : views) {
                v.setBackgroundColor(color);
            }
        }

        static void setNowPlaying(MainActivity activity, Bitmap bitmap) {
            // TODO: 2018/11/6 need get albumPic primaryColor
            activity.runOnUiThread(() -> activity.getNowPlayingBody().setBackgroundResource(R.color.infoBodyPlaying));
        }

        static void setNowPlaying(MainActivity activity) {
            // TODO: 2018/11/6 need get albumPic primaryColor
            activity.runOnUiThread(() -> activity.getNowPlayingBody().setBackgroundResource(R.color.infoBodyPlaying));
        }

        static void setNowNotPlaying(MainActivity activity) {
            activity.runOnUiThread(() -> {
                activity.getNowPlayingBody().setBackgroundResource(R.color.infoBodyNotPlaying);
                activity.setCurrentSongInfoStop();
            });
        }
    }

}
