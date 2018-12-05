/*
 * ************************************************************
 * 文件：NotificationUtils.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月05日 09:30:08
 * 上次修改时间：2018年12月05日 08:38:08
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;

public class NotificationUtils extends ContextWrapper {

    public static final int REQUEST_PAUSE = 1;
    public static final int REQUEST_PLAY = 2;
    public static final int REQUEST_NEXT = 3;
    public static final int REQUEST_PRE = 4;
    public static final int ID = 1;
    private static final String TAG = "NotificationUtils";
    private NotificationManager manager;

    private String id = "Player";

    private String name;

    public NotificationUtils(Context context, String name) {
        super(context);
        this.name = name;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("playing_notification_description");
        channel.enableLights(false);
        channel.enableVibration(false);
        channel.setShowBadge(false);
        getManager().createNotificationChannel(channel);
    }

    private NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }

        return manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification.Builder getChannelNotification(String title, String content, Context context) {

        // TODO: 2018/11/30
        Intent intent = new Intent(context, MainActivity.class).putExtra("intent_args", "by_notification");
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);

        Intent pause = new Intent();
        pause.setComponent(new ComponentName(getPackageName(), Values.BroadCast.ReceiverOnMusicPause));
        PendingIntent pauseIntent = PendingIntent.getBroadcast(context, REQUEST_PAUSE, pause, 0);

        Intent play = new Intent();
        play.setComponent(new ComponentName(getPackageName(), Values.BroadCast.ReceiverOnMusicPlay));
        play.putExtra("play_type", 2);
        PendingIntent playIntent = PendingIntent.getBroadcast(context, REQUEST_PLAY, play, 0);

        Intent next = new Intent();
        next.setComponent(new ComponentName(getPackageName(), Values.BroadCast.ReceiverOnMusicPlay));
        next.putExtra("play_type", 6);
        PendingIntent nextIntent = PendingIntent.getBroadcast(context, REQUEST_NEXT, next, 0);

        Intent pre = new Intent();
        pre.setComponent(new ComponentName(getPackageName(), Values.BroadCast.ReceiverOnMusicPlay));
        pre.putExtra("play_type", 5);
        PendingIntent preIntent = PendingIntent.getBroadcast(context, REQUEST_PRE, pre, 0);

        Notification.MediaStyle mediaStyle = new Notification.MediaStyle();
        mediaStyle.setShowActionsInCompactView(0, 1, 2);

        Notification.Builder builder = new Notification.Builder(getApplicationContext(), id)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(Icon.createWithResource(context, R.drawable.ic_audiotrack_24px))
                .setStyle(mediaStyle)
                .setLargeIcon(Data.sCurrentMusicBitmap == null ? BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_audiotrack_24px) : Data.sCurrentMusicBitmap)
                .setContentIntent(pi)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setAutoCancel(false)
                .setOngoing(true);

        if (Data.sMusicBinder.isPlayingMusic()) {
            Notification.Action[] actions = {new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_skip_previous_white_24dp), "Pre", preIntent).build()
                    , new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_pause_white_24dp), "play", pauseIntent).build()
                    , new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_skip_next_white_24dp), "next", nextIntent).build()};
            builder.setActions(actions);
        } else {
            Notification.Action[] actions = {new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_skip_previous_white_24dp), "Pre", preIntent).build()
                    , new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_play_arrow_black_24dp), "play", playIntent).build()
                    , new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_skip_next_white_24dp), "next", nextIntent).build()};
            builder.setActions(actions);
        }

        if (Data.sCurrentMusicBitmap != null) {
            Palette palette = Palette.from(Data.sCurrentMusicBitmap).generate();
            builder.setColor(palette.getVibrantColor(Color.TRANSPARENT));
        } else {
            builder.setColor(Color.WHITE);
        }
        builder.setColorized(true);

        return builder;
    }

    private NotificationCompat.Builder getNotification_25(String title, String content, Context context) {
        Intent intent = new Intent(context, MainActivity.class).putExtra("intent_args", "by_notification");
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);

        @SuppressWarnings("UnnecessaryLocalVariable") NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), id)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_audiotrack_24px)
                .setLargeIcon(Data.sCurrentMusicBitmap == null ? BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_audiotrack_24px) : Data.sCurrentMusicBitmap)
                .setContentIntent(pi)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setAutoCancel(false);
        return builder;
    }

    public Notification getNot(String title, String content, Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannel();
            return getChannelNotification(title, content, context).build();
        } else {
            Log.d(TAG, "getNot: get 25 noti");
            return getNotification_25(title, content, context).build();
        }
    }

    public void start(Notification notification) {
        manager.notify(ID, notification);
    }

    public void disMiss(int id) {
        manager.cancel(id);
    }
}