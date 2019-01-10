/*
 * ************************************************************
 * 文件：MyMusicService.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月10日 21:12:43
 * 上次修改时间：2019年01月10日 21:06:48
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;

public final class MyMusicService extends Service {

    private static final String TAG = "MyMusicService";

    //PI requests
    public static final int REQUEST_PAUSE = 1;
    public static final int REQUEST_PLAY = 2;
    public static final int REQUEST_NEXT = 3;
    public static final int REQUEST_PRE = 4;

    private final MediaPlayer mMediaPlayer = new MediaPlayer();
    //NotificationId
    private String mId = "Player";
    private int mStartNotiId = 1;

    private boolean mColorized = true;

    public MyMusicService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mColorized = intent.getBooleanExtra(Values.SharedPrefsTag.NOTIFICATION_COLORIZED, true);
        Log.d(TAG, "onStartCommand: do it" + mColorized);
        return START_STICKY;
    }

    private AtomicReference<MusicItem> mMusicItem = new AtomicReference<>(new MusicItem.Builder(-1, "null", "null").build());
    private Bitmap mCurrentCover = null;

    @Override
    public IBinder onBind(Intent intent) {
        mColorized = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Values.SharedPrefsTag.NOTIFICATION_COLORIZED, true);
        Log.d(TAG, "onBind: onBind" + mColorized);
        return mMusicBinder;
    }

    private final Binder mMusicBinder = new IMuiscService.Stub() {
        @Override
        public void playMusic() {
            mMediaPlayer.start();
            startFN();
        }

        @Override
        public void pauseMusic() {
            mMediaPlayer.pause();
            startFN();
        }

        @Override
        public void stopMusic() {
            mMediaPlayer.stop();
            startFN();
        }

        @Override
        public boolean isPlayingMusic() {
            return mMediaPlayer.isPlaying();
        }

        @Override
        public void resetMusic() {
            mMediaPlayer.reset();
        }

        @Override
        public void setDataSource(String path) {
            try {
                mMediaPlayer.setDataSource(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void prepare() {
            try {
                mMediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getDuration() {
            return mMediaPlayer.getDuration();
        }

        @Override
        public int getCurrentPosition() {
            return mMediaPlayer.getCurrentPosition();
        }

        @Override
        public void seekTo(int position) {
            mMediaPlayer.seekTo(position);
        }

        @Override
        public void release() {
            mMediaPlayer.release();
        }

        @Override
        public void setCurrentMusicData(MusicItem item) {
            Log.d(TAG, "setCurrentMusicData: do it");
            if (item == null) {
                mMusicItem = new AtomicReference<>(new MusicItem.Builder(-1, "null", "null").build());
                Log.d(TAG, "setCurrentMusicData: null");
            } else
                mMusicItem = new AtomicReference<>(item);

            mCurrentCover = Utils.Audio.getMp3Cover(mMusicItem.get().getMusicPath(), MyMusicService.this);

            if (mCurrentCover == null)
                mCurrentCover = Utils.Audio.getDrawableBitmap(MyMusicService.this, R.drawable.ic_audiotrack_24px);

            Log.d(TAG, "setCurrentMusicData: " + mMusicItem.get().getMusicPath() + " " + String.valueOf(mCurrentCover == null));
        }
    };

    private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean(false);

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: do it");
        mMediaPlayer.setOnCompletionListener(mp -> {
            Utils.SendSomeThing.sendPlay(MyMusicService.this, 6, "next");

//            if (Values.BUTTON_PRESSED) {
//                //来自用户的主动点击
//                if (Values.CurrentData.CURRENT_PLAY_TYPE.equals(Values.TYPE_RANDOM)) {
//                    Utils.SendSomeThing.sendPlay(this, ReceiverOnMusicPlay.TYPE_SHUFFLE);
//                } else if (Values.CurrentData.CURRENT_PLAY_TYPE.equals(Values.TYPE_COMMON)) {
//                    Utils.SendSomeThing.sendPlay(MyMusicService.this, 4);
//                }
//            } else {
//                switch (Values.CurrentData.CURRENT_AUTO_NEXT_TYPE) {
//                    case Values.TYPE_COMMON:
//                        Utils.SendSomeThing.sendPlay(MyMusicService.this, 4);
//                        break;
//                    case Values.TYPE_REPEAT:
//                        if (Values.CurrentData.CURRENT_PLAY_LIST != null && !Values.CurrentData.CURRENT_PLAY_LIST.equals("default") && Data.sCurrentMusicList.size() != 0) {
//                            if (Values.CurrentData.CURRENT_MUSIC_INDEX == Data.sCurrentMusicList.size() - 1) {
//                                Values.CurrentData.CURRENT_MUSIC_INDEX = 0;
//                                mMediaPlayer.reset();
//                                try {
//                                    mMediaPlayer.setDataSource(Data.sCurrentMusicList.get(0));
//                                    mMediaPlayer.prepare();
//                                    mMediaPlayer.start();
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        } else {
//                            Utils.SendSomeThing.sendPlay(MyMusicService.this, 4);
//                        }
//                        break;
//                    case Values.TYPE_REPEAT_ONE:
//                        mMediaPlayer.start();
//                        break;
//                }
//            }
//            Values.BUTTON_PRESSED = false;
        });

        mMediaPlayer.setOnErrorListener((mp, what, extra) -> {
            mp.reset();
            return true;
        });

        //Notification
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(mId, "Now playing", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("playing_notification_description");
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setShowBadge(false);
            channel.setSound(null, null);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }
    }

    private void startFN() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(mStartNotiId, getChannelNotification(mMusicItem.get().getMusicName(), mMusicItem.get().getMusicAlbum(), mCurrentCover, this).build());
        } else {
            startForeground(mStartNotiId, getNotification_25(mMusicItem.get().getMusicName(), mMusicItem.get().getMusicAlbum(), mCurrentCover, this).build());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    private Notification.Builder getChannelNotification(final String title, final String content, final @Nullable Bitmap cover, final Context context) {
        //pi(s)
        Intent intent = new Intent(context, MainActivity.class).putExtra("intent_args", "by_notification");
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pause = new Intent();
        pause.setComponent(new ComponentName(getPackageName(), Values.BroadCast.ReceiverOnMusicPause));
        PendingIntent pauseIntent = PendingIntent.getBroadcast(context, REQUEST_PAUSE, pause, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent play = new Intent();
        play.setComponent(new ComponentName(getPackageName(), Values.BroadCast.ReceiverOnMusicPlay));
        play.putExtra("play_type", 2);
        PendingIntent playIntent = PendingIntent.getBroadcast(context, REQUEST_PLAY, play, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent next = new Intent();
        next.setComponent(new ComponentName(getPackageName(), Values.BroadCast.ReceiverOnMusicPlay));
        next.putExtra("play_type", 6);
        next.putExtra("args", "next");
        PendingIntent nextIntent = PendingIntent.getBroadcast(context, REQUEST_NEXT, next, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent previous = new Intent();
        previous.setComponent(new ComponentName(context.getPackageName(), Values.BroadCast.ReceiverOnMusicPlay));
        previous.putExtra("play_type", 6);
        previous.putExtra("args", "previous");
        PendingIntent previousIntent = PendingIntent.getBroadcast(context, REQUEST_PRE, previous, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.MediaStyle mediaStyle = new Notification.MediaStyle();
        mediaStyle.setShowActionsInCompactView(0, 1, 2);        //小型化通知的按钮布局

        Notification.Builder builder = new Notification.Builder(getApplicationContext(), mId)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(Icon.createWithResource(context, R.drawable.ic_audiotrack_24px))
                .setStyle(mediaStyle)
                .setLargeIcon(cover == null ? BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_audiotrack_24px) : cover)
                .setContentIntent(pi)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setAutoCancel(false)
                .setOngoing(true);

        if (mMediaPlayer.isPlaying()) {
            Notification.Action[] actions = {
                    new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_skip_previous_white_24dp), "previous", previousIntent).build(),
                    new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_pause_white_24dp), "play", pauseIntent).build(),
                    new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_skip_next_white_24dp), "next", nextIntent).build()};
            builder.setActions(actions);
        } else {
            Notification.Action[] actions = {
                    new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_skip_previous_white_24dp), "previous", previousIntent).build(),
                    new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_play_arrow_black_24dp), "play", playIntent).build(),
                    new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_skip_next_white_24dp), "next", nextIntent).build()};
            builder.setActions(actions);
        }

        if (cover != null) {
            Palette palette = Palette.from(cover).generate();
            builder.setColor(palette.getVibrantColor(Color.TRANSPARENT));
        } else {
            builder.setColor(Color.WHITE);
        }

        builder.setColorized(mColorized);

        return builder;
    }

    private NotificationCompat.Builder getNotification_25(final String title, final String content, final @Nullable Bitmap cover, final Context context) {
        Intent intent = new Intent(context, MainActivity.class).putExtra("intent_args", "by_notification");
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);

        @SuppressWarnings("UnnecessaryLocalVariable") NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), mId)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_audiotrack_24px)
                .setLargeIcon(cover == null ? BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_audiotrack_24px) : cover)
                .setContentIntent(pi)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setAutoCancel(false);
        return builder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: ");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind: ");
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        stopForeground(true);
        mIsServiceDestroyed.set(true);
        if (mCurrentCover != null) mCurrentCover.recycle();
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}
