package top.geek_studio.chenlongcould.musicplayer;

import android.app.*;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.*;
import android.provider.MediaStore;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;
import org.litepal.LitePal;
import org.litepal.LitePalDB;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.broadcast.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.database.Detail;
import top.geek_studio.chenlongcould.musicplayer.database.MyBlackPath;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.utils.PreferenceUtil;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author chenlongcould
 */
// FIXME: 2019/5/23 bugs... when broadcast dead, the action can not send
public final class MusicService extends Service {

	private static final String TAG = "MusicService";

	public static final String ACG_PLAYER_PACKAGE_NAME = "top.geek_studio.chenlongcould.musicplayer.Common";

	@Override
	public void onCreate() {
		serviceWeakReference = new WeakReference<>(this);
		final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
		wakeLock.setReferenceCounted(false);

		MusicControl.mediaPlayer.setOnCompletionListener(mp -> {
			switch (PreferenceUtil.getDefault(this).getString(Values.SharedPrefsTag.PLAY_TYPE, MusicService.PlayType.REPEAT_NONE)) {
				case PlayType.REPEAT_LIST: {
					// TODO: 2019/5/30
				}
				break;

				case PlayType.REPEAT_NONE: {
					if (ItemList.CURRENT_MUSIC_INDEX != ItemList.playOrderList.size()) {
						Intent intent = new Intent(ServiceActions.ACTION_PN);
						intent.putExtra("pnType", ServiceActions.ACTION_PN_NEXT);
						ReceiverOnMusicPlay.startService(this, intent);
					}
				}
				break;

				case PlayType.REPEAT_ONE: {
					MusicControl.seekTo(0);
					MusicControl.playMusic();
				}
				break;
			}
		});

		MusicControl.mediaPlayer.setOnErrorListener((mp, what, extra) -> {
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

		loadDataSource();

		//监听耳机(有线或无线)的插拔动作, 拔出暂停音乐
		final IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
		registerReceiver(Data.mMyHeadSetPlugReceiver, intentFilter);
	}

	/**
	 * 最短播放时间为 3000 毫秒
	 */
	public static final int MINIMUM_PLAY_TIME = 3000;
	public static final int DEFAULT_SHORT_DURATION = 20000;
	private static WeakReference<MusicService> serviceWeakReference;
	private static boolean HAS_PLAYED = false;
	private static AtomicReference<MusicItem> mMusicItem = new AtomicReference<>(null);
	private final Binder mMusicBinder = new IMuiscService.Stub() {

		@Override
		public boolean isPlayingMusic() {
			return MusicControl.isPlayingMusic();
		}

		@Override
		public int getDuration() {
			return MusicControl.getDuration();
		}

		@Override
		public int getCurrentPosition() {
			return MusicControl.getCurrentPosition();
		}

		@Override
		public void seekTo(int position) {
			MusicControl.seekTo(position);
		}

		@Override
		public void release() {
			MusicControl.release();
		}

		@Override
		public void setCurrentMusicData(MusicItem item) {
			if (item != null && item.getMusicID() != -1) {
				mMusicItem.set(item);
				ItemList.CURRENT_MUSIC_INDEX = ItemList.playOrderList.indexOf(mMusicItem.get());
			}
		}

		@Override
		public void setNextWillPlayItem(MusicItem item) throws RemoteException {
			ItemList.nextItem = item;
		}

		@Override
		public void addToOrderList(MusicItem item) throws RemoteException {
			ItemList.playOrderList.add(item);
		}

		@Override
		public MusicItem getCurrentItem() {
			return mMusicItem.get();
		}

		@Override
		public int getCurrentIndex() throws RemoteException {
			return ItemList.CURRENT_MUSIC_INDEX;
		}

		@Override
		public void setCurrentIndex(int index) throws RemoteException {
			ItemList.CURRENT_MUSIC_INDEX = index;
		}
	};
	private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean(false);
	/**
	 * NotificationId
	 */
	private String mId = "Player";
	private int mStartNotificationId = 1;
	private PowerManager.WakeLock wakeLock;


	public MusicService() {
		Log.d(TAG, "MusicService: ");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();

		if (action != null && ItemList.playOrderList.size() > 0) {
			Intent updateUI = new Intent();
			updateUI.setComponent(new ComponentName(getPackageName(), Values.BroadCast.ReceiverOnMusicPlay));

			byte flashMode = -1;

			switch (action) {
				//no ui update
				case ServiceActions.ACTION_CLEAR_ITEMS: {
					ItemList.musicItems.clear();
					ItemList.playOrderList.clear();
				}
				break;

				//no ui update
				case ServiceActions.ACTION_SHUFFLE_ORDER_LIST: {
					shuffleList(intent.getLongExtra("random_seed", new Random().nextLong()));
				}
				break;

				//no ui update
				case ServiceActions.ACTION_INSERT_MUSIC: {
					int[] musicIds = intent.getIntArrayExtra("insert_music_id");
					if (musicIds != null && musicIds.length > 0) {
						for (final int id : musicIds) {
							for (final MusicItem item : ItemList.musicItems) {
								if (item.getMusicID() == id) {
									ItemList.playOrderList.add(ItemList.CURRENT_MUSIC_INDEX, item);
								}
							}
						}
					}
				}
				break;

				case ServiceActions.ACTION_PAUSE: {
					MusicControl.pauseMusic();
					flashMode = ReceiverOnMusicPlay.PAUSE;
				}
				break;

				case ServiceActions.ACTION_PLAY: {
					// 从未播放过说明没有设置过DataSource，同时也不要记录播放统计
					if (!HAS_PLAYED) {
						MusicControl.reset(false);
						MusicControl.setDataSource(mMusicItem.get());
						MusicControl.prepare();
					}
					MusicControl.playMusic();
					flashMode = ReceiverOnMusicPlay.PLAY;
				}
				break;

				// TODO: 2019/5/29
				case ServiceActions.ACTION_PN: {
					//检测是否指定下一首播放
					if (ItemList.nextItem != null && ItemList.nextItem.getMusicID() != -1) {
						mMusicItem.set(ItemList.nextItem);
						MusicControl.reset(true);
						MusicControl.setDataSource(mMusicItem.get());
						MusicControl.prepare();
						MusicControl.playMusic();

						ItemList.nextItem = null;

						flashMode = ReceiverOnMusicPlay.FLASH_UI_COMMON;

						break;
					}

					Log.d(TAG, "onStartCommand: REPEAT MODE: " + PreferenceUtil.getDefault(this)
							.getString(Values.SharedPrefsTag.PLAY_TYPE, PlayType.REPEAT_NONE));

					//检测循环
					// NO UI UPDATE
					if (PlayType.REPEAT_ONE.equals(PreferenceUtil.getDefault(this)
							.getString(Values.SharedPrefsTag.PLAY_TYPE, PlayType.REPEAT_NONE))) {
						MusicControl.seekTo(0);
						MusicControl.playMusic();
						break;
					}

					// previous or next => pn
					final String pnType = intent.getStringExtra("pnType");

					if (ServiceActions.ACTION_PN_PREVIOUS.equals(pnType)
							&& MusicControl.getCurrentPosition() / mMusicItem.get().getDuration() > 20) {
						MusicControl.seekTo(0);
						break;
					}

					ItemList.CURRENT_MUSIC_INDEX = getIndex(pnType);

					// 循环检测是否播放到 “垃圾桶” 中的歌曲，如是，则跳过
					for (; ; ) {
						final MusicItem item = ItemList.playOrderList.get(ItemList.CURRENT_MUSIC_INDEX);
						if (ItemList.trashCanList.contains(item)) {
							ItemList.CURRENT_MUSIC_INDEX = getIndex(pnType);
						} else {
							break;
						}
					}

					mMusicItem.set(ItemList.playOrderList.get(ItemList.CURRENT_MUSIC_INDEX));
					MusicControl.reset(true);
					MusicControl.setDataSource(mMusicItem.get());
					MusicControl.prepare();
					MusicControl.playMusic();

					flashMode = ReceiverOnMusicPlay.FLASH_UI_COMMON;
				}
				break;

				case ServiceActions.ACTION_FAST_SHUFFLE: {
					MusicControl.reset(true);

					//get data
					final Random random = new Random();
					int index = random.nextInt(ItemList.playOrderList.size() - 1);

					// 循环检测是否播放到 “垃圾桶” 中的歌曲，如是，则跳过
					for (; ; ) {
						if (ItemList.trashCanList.contains(ItemList.playOrderList.get(index))) {
							index = random.nextInt(ItemList.playOrderList.size() - 1);
						} else {
							ItemList.CURRENT_MUSIC_INDEX = index;
							break;
						}
					}

					mMusicItem.set(ItemList.playOrderList.get(index));
					MusicControl.setDataSource(mMusicItem.get());
					MusicControl.prepare();
					MusicControl.playMusic();

					flashMode = ReceiverOnMusicPlay.FLASH_UI_COMMON;

				}
				break;

				case ServiceActions.ACTION_ITEM_CLICK: {
					mMusicItem.set(intent.getParcelableExtra("item"));
					ItemList.CURRENT_MUSIC_INDEX = ItemList.playOrderList.indexOf(mMusicItem.get());
					MusicControl.reset(true);
					MusicControl.setDataSource(mMusicItem.get());
					MusicControl.prepare();
					MusicControl.playMusic();

					flashMode = ReceiverOnMusicPlay.FLASH_UI_COMMON;
				}
				break;
			}

			if (flashMode != -1) {
				updateUI.putExtra("play_type", flashMode);
				updateUI.putExtra("item", mMusicItem.get());
				sendBroadcast(updateUI, Values.Permission.BROAD_CAST);
			}
		}

		return START_STICKY;
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	@NonNull
	private Notification.Builder getChannelNotification(final String title, final String content, final @Nullable Bitmap cover, final Context context) {
		final ComponentName serviceName = new ComponentName(context, MusicService.class);

		//pi(s)
		Intent intent = new Intent(context, MainActivity.class).putExtra("intent_args", "by_notification");
		PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		Intent pause = new Intent(ServiceActions.ACTION_PAUSE);
		pause.setComponent(serviceName);
		PendingIntent pauseIntent = PendingIntent.getService(context, 1, pause, PendingIntent.FLAG_UPDATE_CURRENT);

		//resume play...(before show notification, must has music in playing...)
		Intent play = new Intent(ServiceActions.ACTION_PLAY);
		play.setComponent(serviceName);
		PendingIntent playIntent = PendingIntent.getService(context, 2, play, PendingIntent.FLAG_UPDATE_CURRENT);

		Intent next = new Intent(ServiceActions.ACTION_PN);
		next.putExtra("pnType", ACG_PLAYER_PACKAGE_NAME + ".pnnext");
		next.setComponent(serviceName);
		PendingIntent nextIntent = PendingIntent.getService(context, 3, next, PendingIntent.FLAG_UPDATE_CURRENT);

		Intent previous = new Intent(ServiceActions.ACTION_PN);
		previous.putExtra("pnType", ACG_PLAYER_PACKAGE_NAME + ".pnprevious");
		previous.setComponent(serviceName);
		PendingIntent previousIntent = PendingIntent.getService(context, 4, previous, PendingIntent.FLAG_UPDATE_CURRENT);

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
				.setWhen(System.currentTimeMillis())
				.setOngoing(true);

		if (MusicControl.mediaPlayer.isPlaying()) {
			Notification.Action[] actions = {
					new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_skip_previous_white_24dp), "previous", previousIntent).build(),
					new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_pause_white_24dp), "pause", pauseIntent).build(),
					new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_skip_next_white_24dp), "next", nextIntent).build()
			};
			builder.setActions(actions);
		} else {
			Notification.Action[] actions = {
					new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_skip_previous_white_24dp), "previous", previousIntent).build(),
					new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_play_arrow_grey_600_24dp), "play", playIntent).build(),
					new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_skip_next_white_24dp), "next", nextIntent).build()
			};
			builder.setActions(actions);
		}

		if (cover != null) {
			Palette palette = Palette.from(cover).generate();
			builder.setColor(palette.getVibrantColor(Color.TRANSPARENT));
		} else {
			builder.setColor(Color.WHITE);
		}

		builder.setColorized(PreferenceUtil.getDefault(this).getBoolean(Values.SharedPrefsTag.NOTIFICATION_COLORIZED, true));

		return builder;
	}

	/**
	 * 获取下个播放的 index
	 *
	 * @param playType 播放的模式 (向前 或者 向后)
	 */
	public int getIndex(@NonNull String playType) {
		int targetIndex = 0;
		if (playType.contains(ServiceActions.ACTION_PN_NEXT)) {
			targetIndex = ItemList.CURRENT_MUSIC_INDEX + 1;
			//超出范围自动跳转0
			if (targetIndex > ItemList.playOrderList.size() - 1) {
				targetIndex = 0;
			}
		} else if (playType.contains(ServiceActions.ACTION_PN_PREVIOUS)) {
			targetIndex = ItemList.CURRENT_MUSIC_INDEX - 1;
			if (targetIndex < 0) {
				//超出范围超转最后
				targetIndex = ItemList.playOrderList.size() - 1;
			}
		}
		return targetIndex;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind: ");
		return mMusicBinder;
	}

	private PendingIntent buildPendingIntent(Context context, final String action, final ComponentName serviceName) {
		Intent intent = new Intent(action);
		intent.setComponent(serviceName);
		return PendingIntent.getService(context, 0, intent, 0);
	}

	private void loadDataSource() {
		ItemList.musicItems.clear();
		ItemList.playOrderList.clear();

		/*---------------------- init Data!!!! -------------------*/
		final Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		if (cursor != null && cursor.moveToFirst()) {
			//没有歌曲直接退出app

			final boolean skipShort = PreferenceUtil.getDefault(this)
					.getBoolean(Values.SharedPrefsTag.HIDE_SHORT_SONG, true);

			final LitePalDB blackList = new LitePalDB("BlackList", 1);
			blackList.addClassName(MyBlackPath.class.getName());
			LitePal.use(blackList);
			List<MyBlackPath> lists = LitePal.findAll(MyBlackPath.class);
			LitePal.useDefault();

			do {
				final String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
				boolean skip = false;

				for (int i = 0; i < lists.size(); i++) {
					MyBlackPath bp = lists.get(i);
					if (path.contains(bp.getDirPath())) {
						skip = true;
						lists.remove(bp);
						break;
					}
					lists.remove(bp);
				}

				if (skip) {
					Log.d(TAG, "loadData: skip the song that in the blacklist");
					continue;
				}

				final int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
				if (skipShort && duration <= DEFAULT_SHORT_DURATION) {
					Log.d(TAG, "loadDataSource: the music-file duration is " + duration + " (too short), skip...");
					continue;
				}

				final String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
				final String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
				final String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
				final int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
				final int size = (int) cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
				final String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
				final long addTime = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));
				final int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
				final int artistId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));

				final MusicItem.Builder builder = new MusicItem.Builder(id, name, path)
						.musicAlbum(albumName)
						.addTime(addTime)
						.artist(artist)
						.duration(duration)
						.mimeName(mimeType)
						.size(size)
						.addAlbumId(albumId)
						.addArtistId(artistId);

				final MusicItem item = builder.build();

				ItemList.musicItems.add(item);
				ItemList.playOrderList.add(item);
			}
			while (cursor.moveToNext());
			cursor.close();
		}
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
				.setWhen(System.currentTimeMillis())
				.setOngoing(true);
		return builder;
	}

	private void startFN() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForeground(mStartNotificationId, getChannelNotification(mMusicItem.get().getMusicName(), mMusicItem.get().getMusicAlbum(), ItemList.mCurrentCover, this).build());
		} else {
			startForeground(mStartNotificationId, getNotification25(mMusicItem.get().getMusicName(), mMusicItem.get().getMusicAlbum(), ItemList.mCurrentCover, this).build());
		}
	}

	@Override
	public void onDestroy() {
		stopForeground(true);
		MusicControl.mediaPlayer.release();
		mIsServiceDestroyed.set(true);
		wakeLock.release();
		mMusicItem = null;
		if (ItemList.mCurrentCover != null && !ItemList.mCurrentCover.isRecycled()) {
			ItemList.mCurrentCover.recycle();
		}

		try {
			unregisterReceiver(Data.mMyHeadSetPlugReceiver);
		} catch (Exception e) {
			Log.d(TAG, "onDestroy: " + e.getMessage());
		}

		super.onDestroy();
	}

	private void shuffleList(long seed) {
		if (seed == 0) {
			ItemList.playOrderList.clear();
			ItemList.playOrderList.addAll(ItemList.musicItems);
		} else {
			Collections.shuffle(ItemList.playOrderList, new Random(seed));
		}
	}

	static class Config {

	}

	public interface PlayType {
		String REPEAT_NONE = "REPEAT_NONE";
		String REPEAT_LIST = "REPEAT_LIST";
		String REPEAT_ONE = "REPEAT_ONE";
	}

	public interface ServiceActions {
		/**
		 * special
		 */
		String ACTION_CLEAR_ITEMS = ACG_PLAYER_PACKAGE_NAME + ".clearitems";

		String ACTION_SHUFFLE_ORDER_LIST = ACG_PLAYER_PACKAGE_NAME + ".shuffleorderlist";

		/**
		 * common
		 */
		String ACTION_PLAY = ACG_PLAYER_PACKAGE_NAME + ".play";
		String ACTION_PAUSE = ACG_PLAYER_PACKAGE_NAME + ".pause";
		String ACTION_PN = ACG_PLAYER_PACKAGE_NAME + ".pntype";
		String ACTION_PN_NEXT = ACG_PLAYER_PACKAGE_NAME + ".pnnext";
		String ACTION_PN_PREVIOUS = ACG_PLAYER_PACKAGE_NAME + ".pnprevious";

		/**
		 * other
		 */
		String ACTION_FAST_SHUFFLE = ACG_PLAYER_PACKAGE_NAME + ".fastshuffle";

		String ACTION_ITEM_CLICK = ACG_PLAYER_PACKAGE_NAME + ".itemclick";

		/**
		 * int extra key: next_item_id
		 */
		@Deprecated
		String ACTION_NEXT_WILL_PLAY = ACG_PLAYER_PACKAGE_NAME + ".nextwillplay";

		/**
		 * @see Intent#getIntArrayExtra(String)
		 * key: insert_music_id
		 */
		String ACTION_INSERT_MUSIC = ACG_PLAYER_PACKAGE_NAME + ".insertmusic";
	}

	/**
	 * for {@link PendingIntent#getBroadcast(Context, int, Intent, int)}
	 */
	@Deprecated
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

	private static class ItemList {
		static int CURRENT_MUSIC_INDEX = 0;
		static List<MusicItem> musicItems = new ArrayList<>();

		///////////////////////////DATA//////////////////////////
		static List<MusicItem> playOrderList = new ArrayList<>();
		static List<MusicItem> trashCanList = new ArrayList<>();
		static MusicItem nextItem = null;
		private static Bitmap mCurrentCover = null;

		static void updateCurrentCover(@Nullable MusicItem item) {
			if (item == null || item.getMusicID() == -1) {
				return;
			}
			ItemList.mCurrentCover = Utils.Audio.getCoverBitmapFull(serviceWeakReference.get(), mMusicItem.get().getAlbumId());
		}
	}

	public static class MusicControl {
		private static final MediaPlayer mediaPlayer = new MediaPlayer();

		private static void reset(boolean saveData) {
			/*
			 * 记录播放信息
			 * */
			if (mMusicItem.get() != null && saveData) {
				if (HAS_PLAYED) {
					final List<Detail> infos = LitePal.where("MusicId = ?", String.valueOf(mMusicItem.get().getMusicID())).find(Detail.class);
					if (infos.size() > 0) {
						Detail detail = infos.get(0);
						if (mediaPlayer.getCurrentPosition() < MINIMUM_PLAY_TIME) {
							detail.setMinimumPlayTimes(detail.getMinimumPlayTimes() + 1);
						} else {
							detail.setPlayDuration(detail.getPlayDuration() + mediaPlayer.getCurrentPosition());
						}
						detail.setPlayTimes(detail.getPlayTimes() + 1);
						detail.save();
					} else {
						Detail detail = new Detail();
						detail.setMusicId(mMusicItem.get().getMusicID());
						if (mediaPlayer.getCurrentPosition() < MINIMUM_PLAY_TIME) {
							detail.setMinimumPlayTimes(detail.getMinimumPlayTimes() + 1);
						} else {
							detail.setPlayDuration(detail.getPlayDuration() + mediaPlayer.getCurrentPosition());
						}
						detail.setPlayTimes(detail.getPlayTimes() + 1);
						detail.save();
					}
				}
			}

			mediaPlayer.reset();

		}

		private static void playMusic() {
			mediaPlayer.start();
			HAS_PLAYED = true;
			serviceWeakReference.get().startFN();
			// update last played
			if (mMusicItem.get() != null) {
				PreferenceUtil.getDefault(serviceWeakReference.get()).edit()
						.putInt(Values.SharedPrefsTag.LAST_PLAY_MUSIC_ID, mMusicItem.get().getMusicID()).apply();
			}
		}

		private static void pauseMusic() {
			mediaPlayer.pause();
			serviceWeakReference.get().startFN();
		}

		private static void stopMusic() {
			mediaPlayer.stop();
		}

		private static boolean isPlayingMusic() {
			return mediaPlayer.isPlaying();
		}

		private static void setDataSource(@Nullable MusicItem item) {
			if (item == null || item.getMusicID() == -1) {
				return;
			}

			try {
				mediaPlayer.setDataSource(item.getMusicPath());
				ItemList.updateCurrentCover(item);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private static void prepare() {
			try {
				mediaPlayer.prepare();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private static int getDuration() {
			return mediaPlayer.isPlaying() ? mediaPlayer.getDuration() : 0;
		}

		private static int getCurrentPosition() {
			return mediaPlayer.isPlaying() ? mediaPlayer.getCurrentPosition() : 0;
		}

		private static void seekTo(int position) {
			mediaPlayer.seekTo(position);
		}

		private static void release() {
			mediaPlayer.release();
		}

		//////////////////////EXP

		public static void next(@Nullable final Context context) {
			if (context == null) return;
			Intent next = new Intent(MusicService.ServiceActions.ACTION_PN);
			next.putExtra("pnType", MusicService.ServiceActions.ACTION_PN_NEXT);
			ReceiverOnMusicPlay.startService(context, next);
		}

		public static void previous(@Nullable final Context context) {
			if (context == null) return;
			Intent next = new Intent(MusicService.ServiceActions.ACTION_PN);
			next.putExtra("pnType", ServiceActions.ACTION_PN_PREVIOUS);
			ReceiverOnMusicPlay.startService(context, next);
		}

		public static void itemClick(@NonNull final Context context, @NonNull MusicItem item) {
			final Intent intent = new Intent(MusicService.ServiceActions.ACTION_ITEM_CLICK);
			intent.putExtra("item", item);
			ReceiverOnMusicPlay.startService(context, intent);
		}
	}

}
