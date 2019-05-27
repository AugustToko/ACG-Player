package top.geek_studio.chenlongcould.musicplayer;

import android.app.*;
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
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;
import org.litepal.LitePal;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.broadcast.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.database.Detail;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author chenlongcould
 */
// FIXME: 2019/5/23 bugs... when broadcast dead, the action can not send
public final class MusicService extends Service {

	private static final String TAG = "MusicService";

	/**
	 * 最短播放时间为 3000 毫秒
	 */
	public static final int MINIMUM_PLAY_TIME = 3000;
	private AtomicReference<MusicItem> mMusicItem = new AtomicReference<>(null);

	private final MediaPlayer mMediaPlayer = new MediaPlayer();

	private boolean hasPlayed = false;

	/**
	 * NotificationId
	 */
	private String mId = "Player";

	private int mStartNotificationId = 1;

	private boolean mColorized = true;

	private PowerManager.WakeLock wakeLock;
	private final Binder mMusicBinder = new IMuiscService.Stub() {
		@Override
		public void playMusic() {
			mMediaPlayer.start();
			hasPlayed = true;
			startFN();

			if (mMusicItem.get() != null) {
				PreferenceManager.getDefaultSharedPreferences(MusicService.this).edit()
						.putInt(Values.SharedPrefsTag.LAST_PLAY_MUSIC_ID, mMusicItem.get().getMusicID()).apply();
			}
		}

		@Override
		public void pauseMusic() {
			mMediaPlayer.pause();
			startFN();
		}

		@Override
		public void stopMusic() {
			mMediaPlayer.stop();
		}

		@Override
		public boolean isPlayingMusic() {
			return mMediaPlayer.isPlaying();
		}

		@Override
		public void resetMusic() {
			/*
			 * 记录播放信息
			 * */
			if (mMusicItem.get() != null) {
				if (hasPlayed) {
					final List<Detail> infos = LitePal.where("MusicId = ?", String.valueOf(mMusicItem.get().getMusicID())).find(Detail.class);
					if (infos.size() > 0) {
						Detail detail = infos.get(0);
						if (mMediaPlayer.getCurrentPosition() < MINIMUM_PLAY_TIME) {
							detail.setMinimumPlayTimes(detail.getMinimumPlayTimes() + 1);
						} else {
							detail.setPlayDuration(detail.getPlayDuration() + mMediaPlayer.getCurrentPosition());
						}
						detail.setPlayTimes(detail.getPlayTimes() + 1);
						detail.save();
					} else {
						Detail detail = new Detail();
						detail.setMusicId(mMusicItem.get().getMusicID());
						if (mMediaPlayer.getCurrentPosition() < MINIMUM_PLAY_TIME) {
							detail.setMinimumPlayTimes(detail.getMinimumPlayTimes() + 1);
						} else {
							detail.setPlayDuration(detail.getPlayDuration() + mMediaPlayer.getCurrentPosition());
						}
						detail.setPlayTimes(detail.getPlayTimes() + 1);
						detail.save();
					}
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
				mMediaPlayer.reset();
			}
		}

		@Override
		public void prepare() {
			try {
				mMediaPlayer.prepare();
			} catch (IOException e) {
				e.printStackTrace();
				mMediaPlayer.reset();
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
			mMusicItem.set(item);
			setDataSource(item.getMusicPath());
			mCurrentCover = Utils.Audio.getCoverBitmapFull(MusicService.this, mMusicItem.get().getAlbumId());
		}

		@Override
		public MusicItem getCurrentItem() {
			return mMusicItem.get();
		}
	};

	private Bitmap mCurrentCover = null;
	private HashMap<String, Bitmap> bitmapHashMap = new HashMap<>();

	private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean(false);

	@Override
	public void onCreate() {
		final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
		wakeLock.setReferenceCounted(false);

		mMediaPlayer.setOnCompletionListener(mp -> Utils.SendSomeThing.sendPlay(MusicService.this, 6, ReceiverOnMusicPlay.TYPE_NEXT));

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

	public MusicService() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mColorized = intent.getBooleanExtra(Values.SharedPrefsTag.NOTIFICATION_COLORIZED, true);
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		mColorized = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Values.SharedPrefsTag.NOTIFICATION_COLORIZED, true);
		return mMusicBinder;
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	@NonNull
	private Notification.Builder getChannelNotification(final String title, final String content, final @Nullable Bitmap cover, final Context context) {
		//pi(s)
		Intent intent = new Intent(context, MainActivity.class).putExtra("intent_args", "by_notification");
		PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		Intent pause = new Intent();
		pause.setComponent(new ComponentName(getPackageName(), Values.BroadCast.ReceiverOnMusicPlay));
		pause.putExtra("play_type", -1);
		PendingIntent pauseIntent = PendingIntent.getBroadcast(context, PendingIntentType.REQUEST_PAUSE, pause, PendingIntent.FLAG_UPDATE_CURRENT);

		//resume play...(before show notification, must has music in playing...)
		Intent play = new Intent();
		play.setComponent(new ComponentName(getPackageName(), Values.BroadCast.ReceiverOnMusicPlay));
		play.putExtra("play_type", 2);
		PendingIntent playIntent = PendingIntent.getBroadcast(context, PendingIntentType.REQUEST_PLAY, play, PendingIntent.FLAG_UPDATE_CURRENT);

		Intent next = new Intent();
		next.setComponent(new ComponentName(getPackageName(), Values.BroadCast.ReceiverOnMusicPlay));
		next.putExtra("play_type", 6);
		next.putExtra("args", "next");
		PendingIntent nextIntent = PendingIntent.getBroadcast(context, PendingIntentType.REQUEST_NEXT, next, PendingIntent.FLAG_UPDATE_CURRENT);

		Intent previous = new Intent();
		previous.setComponent(new ComponentName(context.getPackageName(), Values.BroadCast.ReceiverOnMusicPlay));
		previous.putExtra("play_type", 6);
		previous.putExtra("args", "previous");
		PendingIntent previousIntent = PendingIntent.getBroadcast(context, PendingIntentType.REQUEST_PRE, previous, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification.MediaStyle mediaStyle = new Notification.MediaStyle();

		//小型化通知的按钮布局
		mediaStyle.setShowActionsInCompactView(0, 1, 2);

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

	private void startFN() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForeground(mStartNotificationId, getChannelNotification(mMusicItem.get().getMusicName(), mMusicItem.get().getMusicAlbum(), mCurrentCover, this).build());
		} else {
			startForeground(mStartNotificationId, getNotification25(mMusicItem.get().getMusicName(), mMusicItem.get().getMusicAlbum(), mCurrentCover, this).build());
		}
	}

	@Override
	public void onDestroy() {
		stopForeground(true);
		mMediaPlayer.release();
		mIsServiceDestroyed.set(true);
		wakeLock.release();
		mMusicItem = null;
		if (mCurrentCover != null && !mCurrentCover.isRecycled()) {
			mCurrentCover.recycle();
		}
		super.onDestroy();
	}

	private NotificationCompat.Builder getNotification25(final String title, final String content, final @Nullable Bitmap cover, final Context context) {
		final Intent intent = new Intent(context, MainActivity.class).putExtra("intent_args", "by_notification");
		final PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);

		@SuppressWarnings("UnnecessaryLocalVariable") NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), mId)
				.setContentTitle(title)
				.setContentText(content)
				.setSmallIcon(R.drawable.ic_audiotrack_24px)
				.setLargeIcon(cover == null ? BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_audiotrack_24px) : cover)
				.setContentIntent(pi)
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setAutoCancel(false)
				.setOngoing(true);
		return builder;
	}

	interface PendingIntentType {

		/**
		 * PI requests
		 *
		 * @see PendingIntent
		 */
		int REQUEST_PAUSE = 1;
		int REQUEST_PLAY = 2;
		int REQUEST_NEXT = 3;
		int REQUEST_PRE = 4;
	}

}
