/*
 * ************************************************************
 * 文件：MyMusicService.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月27日 13:11:38
 * 上次修改时间：2019年01月19日 14:07:05
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
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.broadcasts.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.database.Detail;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

public final class MyMusicService extends Service {

    private static final String TAG = "MyMusicService";

    public static final int MINIMUM_PLAY_TIME = 3000;
    private boolean HAS_PLAYED = false;

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

    private PowerManager.WakeLock wakeLock;
    private AtomicReference<MusicItem> mMusicItem = new AtomicReference<>(new MusicItem.Builder(-1, "null", "null").build());

    private Bitmap mCurrentCover = null;

    public MyMusicService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mColorized = intent.getBooleanExtra(Values.SharedPrefsTag.NOTIFICATION_COLORIZED, true);
        Log.d(TAG, "onStartCommand: do it" + mColorized);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        mColorized = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Values.SharedPrefsTag.NOTIFICATION_COLORIZED, true);
        return mMusicBinder;
    }

    private final Binder mMusicBinder = new IMuiscService.Stub() {
        @Override
        public void playMusic() {
            HAS_PLAYED = true;
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
            stopForeground(true);
        }

        @Override
        public boolean isPlayingMusic() {
            return mMediaPlayer.isPlaying();
        }

        @Override
        public void resetMusic() {
            if (HAS_PLAYED) {
                final List<Detail> infos = LitePal.where("MusicId = ?", String.valueOf(mMusicItem.get().getMusicID())).find(Detail.class);
                if (infos.size() > 0) {
                    Detail detail = infos.get(0);
                    detail.setPlayDuration(detail.getPlayDuration() + mMediaPlayer.getCurrentPosition());
                    if (mMediaPlayer.getCurrentPosition() < MINIMUM_PLAY_TIME) {
                        detail.setMinimumPlayTimes(detail.getMinimumPlayTimes() + 1);
                    }
                    detail.save();
                } else {
                    Detail detail = new Detail();
                    detail.setMusicId(mMusicItem.get().getMusicID());
                    if (mMediaPlayer.getCurrentPosition() < MINIMUM_PLAY_TIME) {
                        detail.setMinimumPlayTimes(detail.getMinimumPlayTimes() + 1);
                    }
                    detail.setPlayTimes(getCurrentPosition());
                    detail.setPlayDuration(detail.getPlayDuration() + mMediaPlayer.getCurrentPosition());
                    detail.save();
                }
            }

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
            if (item == null) {
                mMusicItem = new AtomicReference<>(new MusicItem.Builder(-1, "null", "null").build());
            } else {
                mMusicItem = new AtomicReference<>(item);
            }

            mCurrentCover = Utils.Audio.getCoverBitmap(MyMusicService.this, mMusicItem.get().getAlbumId());
        }

        @Override
        public MusicItem getCurrentItem() throws RemoteException {
            return mMusicItem.get();
        }
    };

    private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean(false);

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: do it");
        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wakeLock.setReferenceCounted(false);

        mMediaPlayer.setOnCompletionListener(mp -> Utils.SendSomeThing.sendPlay(MyMusicService.this, 6, ReceiverOnMusicPlay.TYPE_NEXT));

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
                    new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_play_arrow_grey_600_24dp), "play", playIntent).build(),
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
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(false);
        return builder;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        mMediaPlayer.release();
        stopForeground(true);
        mIsServiceDestroyed.set(true);
        if (mCurrentCover != null) mCurrentCover.recycle();
        wakeLock.release();
        mMusicItem = null;
        super.onDestroy();
    }

}
