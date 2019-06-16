package top.geek_studio.chenlongcould.musicplayer;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Process;
import android.os.*;
import android.provider.MediaStore;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;
import io.reactivex.disposables.Disposable;
import org.litepal.LitePal;
import org.litepal.LitePalDB;
import top.geek_studio.chenlongcould.musicplayer.activity.main.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.broadcast.MediaButtonIntentReceiver;
import top.geek_studio.chenlongcould.musicplayer.broadcast.MyHeadSetPlugReceiver;
import top.geek_studio.chenlongcould.musicplayer.broadcast.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.database.Detail;
import top.geek_studio.chenlongcould.musicplayer.database.MyBlackPath;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.threadPool.CustomThreadPool;
import top.geek_studio.chenlongcould.musicplayer.utils.MusicUtil;
import top.geek_studio.chenlongcould.musicplayer.utils.PreferenceUtil;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author chenlongcould
 */
public final class MusicService extends Service {

	private static final String TAG = "MusicService";

	/**
	 * package name {@code release}
	 */
	public static final String ACG_PLAYER_PACKAGE_NAME = "top.geek_studio.chenlongcould.musicplayer.Common";

	/**
	 * 短音频排除基准 2s
	 */
	public static final int DEFAULT_SHORT_DURATION = 20000;
	/**
	 * "短播放" 收录基准
	 */
	public static final int MINIMUM_PLAY_TIME = 3000;
	/**
	 * 耳机插拔监听
	 */
	public static MyHeadSetPlugReceiver mMyHeadSetPlugReceiver = new MyHeadSetPlugReceiver();

	/**
	 * 当前加载完成的 musicItem {@link MusicItem}
	 */
	private static MusicItem mMusicItem = null;
	/**
	 * service instance of {@link MusicService}
	 */
	private static WeakReference<MusicService> serviceWeakReference;
	/**
	 * 是否播放过音乐
	 */
	private static boolean HAS_PLAYED = false;
	/**
	 * Binder
	 */
	private final Binder mMusicBinder = new IMuiscService.Stub() {

		@Override
		public void reset() {
			MusicControl.reset(false);
		}

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
				mMusicItem = item;
				ItemList.CURRENT_MUSIC_INDEX = ItemList.playOrderList.indexOf(mMusicItem);
			}
		}

		@Override
		public void addNextWillPlayItem(MusicItem item) {
			ItemList.nextWillplay.add(MusicUtil.m2d(item));
		}

		@Override
		public void addToOrderList(MusicItem item) {
			ItemList.playOrderList.add(MusicUtil.m2d(item));
		}

		@Override
		public void removeFromOrderList(MusicItem item) {
			ItemList.playOrderList.remove(item);
		}

		@Override
		public void syncOrderList(int[] array) {
			ItemList.playOrderList.clear();
			for (int i : array) {
				Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null
						, MediaStore.Audio.Media._ID + "=?", new String[]{String.valueOf(i)}, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
				if (cursor != null && cursor.moveToFirst() && cursor.getCount() != 0) {
					final String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
					final int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
					final String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
					final String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
					final String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
					final int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
					final int size = (int) cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
					final String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
					final long addTime = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));
					final int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
					final int artistId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));

//					final MusicItem.Builder builder = new MusicItem.Builder(id, name, path)
//							.musicAlbum(albumName)
//							.addTime(addTime)
//							.artist(artist)
//							.duration(duration)
//							.mimeName(mimeType)
//							.size(size)
//							.addAlbumId(albumId)
//							.addArtistId(artistId);
					final Bundle bundle = new Bundle();
					bundle.putInt("albumId", albumId);
					bundle.putInt("artistId", artistId);
					bundle.putLong("addTime", addTime);
					bundle.putInt("size", size);
					bundle.putString("mimeType", mimeType);

					final MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder()
							.setTitle(name)
							.setSubtitle(albumName)
							.setDescription(artist)
							.setIconBitmap(null)
							.setMediaId(String.valueOf(id))
							.setMediaUri(Uri.fromFile(new File(path)))
							.setExtras(bundle);

					ItemList.playOrderList.add(builder.build());
					cursor.close();
				}
			}
		}

		@Override
		public MusicItem getCurrentItem() {
			return mMusicItem;
		}

		@Override
		public int getCurrentIndex() {
			return ItemList.CURRENT_MUSIC_INDEX;
		}

		@Override
		public void setCurrentIndex(int index) {
			ItemList.CURRENT_MUSIC_INDEX = index;
		}
	};

	private MediaSessionCompat mediaSession;

	private PlaybackStateCompat.Builder stateBuilder;

	private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean(false);

	private int mStartNotificationId = 1;

	private PowerManager.WakeLock wakeLock;

	private List<Disposable> disposableList = new ArrayList<>();

	public MusicService() {
		Log.d(TAG, "MusicService: ");
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate: ");

		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			Log.d(TAG, "onCreate: start service, but do not have permission \" WRITE_EXTERNAL_STORAGE \"");
			stopSelf();
			return;
		}

		serviceWeakReference = new WeakReference<>(this);

		final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
		wakeLock.setReferenceCounted(false);

		MusicControl.init(MusicService.this);

		NotificationTool.init(MusicService.this);


		final ComponentName mediaButtonReceiverComponentName = new ComponentName(getApplicationContext()
				, MediaButtonIntentReceiver.class);
		final Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		mediaButtonIntent.setComponent(mediaButtonReceiverComponentName);
		final PendingIntent mediaButtonReceiverPendingIntent = PendingIntent.getBroadcast(getApplicationContext()
				, 0, mediaButtonIntent, 0);

		mediaSession = new MediaSessionCompat(this, "ACG-Player", mediaButtonReceiverComponentName
				, mediaButtonReceiverPendingIntent);
		mediaSession.setCallback(new MediaSessionCompat.Callback() {
			@Override
			public void onPlay() {
				MusicControl.intentPlay(MusicService.this);
			}

			@Override
			public void onPause() {
				MusicControl.intentPause(MusicService.this);
			}

			@Override
			public void onSkipToNext() {
				MusicControl.intentNext(MusicService.this);
			}

			@Override
			public void onSkipToPrevious() {
				MusicControl.intentPrevious(MusicService.this);
			}

			@Override
			public void onStop() {
				// TODO: 2019/6/3 待完善
				MusicControl.stopMusic();
				stopForeground(true);
			}

			@Override
			public void onSeekTo(long pos) {
				MusicControl.seekTo((int) pos);
			}

			@Override
			public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
				return MediaButtonIntentReceiver.handleIntent(MusicService.this, mediaButtonEvent);
			}
		});
		mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
				| MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
				| MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);
		mediaSession.setMediaButtonReceiver(mediaButtonReceiverPendingIntent);
		mediaSession.setActive(true);

		stateBuilder = new PlaybackStateCompat.Builder()
				.setActions(
						PlaybackStateCompat.ACTION_PLAY |
								PlaybackStateCompat.ACTION_PLAY_PAUSE);
		mediaSession.setPlaybackState(stateBuilder.build());

		loadDataSource();

		//监听耳机(有线或无线)的插拔动作, 拔出暂停音乐
		final IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
		registerReceiver(mMyHeadSetPlugReceiver, intentFilter);
	}

	private void updateMediaSessionMetaData() {
		CustomThreadPool.post(() -> {
			final MusicItem song = mMusicItem;
			Log.d(TAG, "updateMediaSessionMetaData: " + song.getMusicID());

			if (song.getMusicID() == -1) {
				mediaSession.setMetadata(null);
				return;
			}

			final MediaMetadataCompat.Builder metaData = new MediaMetadataCompat.Builder()
					.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getArtist())
					.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, song.getArtist())
					.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.getMusicAlbum())
					.putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getMusicName())
					.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.getDuration())
					.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, ItemList.CURRENT_MUSIC_INDEX + 1)
					// TODO: 2019/6/4 add  year
					.putLong(MediaMetadataCompat.METADATA_KEY_YEAR, 0);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				metaData.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, ItemList.playOrderList.size());
			}
			metaData.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, ItemList.mCurrentCover);
			mediaSession.setMetadata(metaData.build());
			Log.d(TAG, "updateMediaSessionMetaData: build done");

		});
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			Log.d(TAG, "onStartCommand: intent == null");
			return START_NOT_STICKY;
		} else {
			Log.d(TAG, "onStartCommand: action: " + intent.getAction() + " extra: " + intent.getExtras());
		}

		if (ItemList.playOrderList.size() == 0) {
			Log.d(TAG, "onStartCommand: order list is empty, reload...");
			loadDataSource();
		}

		final String action = intent.getAction();

		if (action != null && ItemList.playOrderList.size() > 0) {
			final Intent updateUI = new Intent();
			updateUI.setComponent(new ComponentName(getPackageName(), Values.BroadCast.ReceiverOnMusicPlay));

			// 刷新ui的模式
			byte flashMode = -1;

			switch (action) {
				//no ui update
				case ServiceActions.ACTION_CLEAR_ITEMS: {
					ItemList.playOrderList.clear();
					ItemList.CURRENT_MUSIC_INDEX = 0;
				}
				break;

				//no ui update
				case ServiceActions.ACTION_RESET_LIST: {
					ItemList.playOrderList.clear();
					ItemList.playOrderList.addAll(ItemList.playOrderListBK);
					ItemList.CURRENT_MUSIC_INDEX = 0;
				}
				break;

				//no ui update
				case ServiceActions.ACTION_SHUFFLE_ORDER_LIST: {
					shuffleList(intent.getLongExtra("random_seed", new Random().nextLong()));
				}
				break;

//				//no ui update
//				case ServiceActions.ACTION_INSERT_MUSIC: {
//					int[] musicIds = intent.getIntArrayExtra("insert_music_id");
//					if (musicIds != null && musicIds.length > 0) {
//						for (final int id : musicIds) {
//							for (final MusicItem item : ItemList.musicItems) {
//								if (item.getMusicID() == id) {
//									ItemList.playOrderList.add(ItemList.CURRENT_MUSIC_INDEX, item);
//								}
//							}
//						}
//					}
//				}
//				break;

				case ServiceActions.ACTION_PAUSE: {
					MusicControl.pauseMusic();
					flashMode = ReceiverOnMusicPlay.PAUSE;
				}
				break;

				case ServiceActions.ACTION_PLAY: {

					Log.d(TAG, "onStartCommand: case play");

					// 1 没有播放过 也没有找到上次打开app 最后播放的数据
					if (!HAS_PLAYED && mMusicItem == null) {
						setRandomItemPrepare();
						flashMode = ReceiverOnMusicPlay.FLASH_UI_COMMON;
						updateMediaSessionMetaData();

						Log.d(TAG, "onStartCommand: has not play");
						// 2 已经播放过了
					} else if (HAS_PLAYED && mMusicItem != null) {
						flashMode = ReceiverOnMusicPlay.PLAY;
						MusicControl.playMusic();
						Log.d(TAG, "onStartCommand: play: already played");
					}

				}
				break;

				case ServiceActions.ACTION_PN: {
					//检测是否指定下一首播放
					if (ItemList.nextWillplay.size() != 0) {
						MediaDescriptionCompat descriptionCompat = ItemList.nextWillplay.get(0);
						mMusicItem = MusicUtil.d2m(descriptionCompat);
						MusicControl.reset(true);
						MusicControl.setDataSource(this, descriptionCompat);

						updateMediaSessionMetaData();

						MusicControl.prepareAndPlay();

						ItemList.nextWillplay.remove(descriptionCompat);

						flashMode = ReceiverOnMusicPlay.FLASH_UI_COMMON;

						break;
					}

					//检测循环
					// NO UI UPDATE
					if (PlayType.REPEAT_ONE.equals(PreferenceUtil.getDefault(serviceWeakReference.get())
							.getString(Values.SharedPrefsTag.PLAY_TYPE, PlayType.REPEAT_NONE))) {
						MusicControl.seekTo(0);
						break;
					}

					// intentPrevious or intentNext => pn
					final String pnType = intent.getStringExtra(IntentTAG.PN_TYPE);

					if (ServiceActions.ACTION_PN_PREVIOUS.equals(pnType)
							&& MusicControl.getCurrentPosition() / mMusicItem.getDuration() > 20) {
						MusicControl.seekTo(0);
						break;
					}

					ItemList.CURRENT_MUSIC_INDEX = getIndex(pnType);

					// 循环检测是否播放到 “垃圾桶” 中的歌曲，如是，则跳过
					for (; ; ) {
						final MediaDescriptionCompat item = ItemList.playOrderList.get(ItemList.CURRENT_MUSIC_INDEX);
						if (ItemList.trashCanList.contains(item)) {
							ItemList.CURRENT_MUSIC_INDEX = getIndex(pnType);
						} else {
							break;
						}
					}

					MediaDescriptionCompat descriptionCompat = ItemList.playOrderList.get(ItemList.CURRENT_MUSIC_INDEX);
					mMusicItem = MusicUtil.d2m(descriptionCompat);
					MusicControl.reset(true);
					MusicControl.setDataSource(this, descriptionCompat);

					updateMediaSessionMetaData();

					MusicControl.prepareAndPlay();

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

					MediaDescriptionCompat descriptionCompat = ItemList.playOrderList.get(index);
					mMusicItem = MusicUtil.d2m(descriptionCompat);
					MusicControl.setDataSource(this, descriptionCompat);
					updateMediaSessionMetaData();
					MusicControl.prepareAndPlay();

					flashMode = ReceiverOnMusicPlay.FLASH_UI_COMMON;

				}
				break;

				case ServiceActions.ACTION_ITEM_CLICK: {
					mMusicItem = intent.getParcelableExtra("item");
					MediaDescriptionCompat descriptionCompat = MusicUtil.m2d(mMusicItem);
					ItemList.CURRENT_MUSIC_INDEX = ItemList.playOrderList.indexOf(descriptionCompat);
					MusicControl.reset(true);
					MusicControl.setDataSource(this, descriptionCompat);
					updateMediaSessionMetaData();
					MusicControl.prepareAndPlay();

					flashMode = ReceiverOnMusicPlay.FLASH_UI_COMMON;
				}
				break;

				case ServiceActions.ACTION_TOGGLE_PLAY_PAUSE: {
					if (MusicControl.isPlayingMusic()) {
						MusicControl.pauseMusic();
						flashMode = ReceiverOnMusicPlay.PAUSE;
					} else {
						// 从未播放过说明没有设置过DataSource，同时也不要记录播放统计
						if (!HAS_PLAYED) {
							Log.d(TAG, "onStartCommand: has not played");
							setRandomItemPrepare();
							updateMediaSessionMetaData();
							flashMode = ReceiverOnMusicPlay.FLASH_UI_COMMON;
						} else {
							Log.d(TAG, "onStartCommand: has played");
							flashMode = ReceiverOnMusicPlay.PLAY;
						}
						MusicControl.playMusic();
					}
				}
				break;

				case ServiceActions.ACTION_TOGGLE_FAVOURITE: {
					MusicUtil.toggleFavorite(this, mMusicItem);
					flashMode = ReceiverOnMusicPlay.TOGGLE_FAV;
				}
				break;

				default:
			}

			if (flashMode != -1) {
				updateUI.putExtra("play_type", flashMode);
				updateUI.putExtra("item", mMusicItem);
				sendBroadcast(updateUI, Values.Permission.BROAD_CAST);

				if (flashMode != ReceiverOnMusicPlay.PAUSE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					Intent tile = new Intent(MusicService.this, MyTileService.class);
					tile.setAction(MyTileService.ACTION_SET_TITLE);
					tile.putExtra("title", mMusicItem.getMusicName());
					startService(tile);
				}
			}
		}

		return START_STICKY;
	}

	/**
	 * 随机选择一个item，然后设置，准备播放
	 */
	private void setRandomItemPrepare() {
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

		mMusicItem = MusicUtil.d2m(ItemList.playOrderList.get(index));
		MusicControl.setDataSource(this, ItemList.playOrderList.get(index));
		MusicControl.prepareAndPlay();
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
		return mMusicBinder;
	}

	private synchronized void loadDataSource() {
		if (ContextCompat.checkSelfPermission(this,
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
			ItemList.playOrderList.clear();

			MediaControllerCompat controller = mediaSession.getController();

			/*---------------------- init Data!!!! -------------------*/
			final Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null
					, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
			if (cursor != null && cursor.moveToFirst()) {

				// skip short duration song(s)
				final boolean skipShort = PreferenceUtil.getDefault(serviceWeakReference.get())
						.getBoolean(Values.SharedPrefsTag.HIDE_SHORT_SONG, true);

				// last played music id
				int lastId = PreferenceUtil.getDefault(serviceWeakReference.get())
						.getInt(Values.SharedPrefsTag.LAST_PLAY_MUSIC_ID, -1);

				// black list
				final LitePalDB blackList = new LitePalDB("BlackList", 1);
				blackList.addClassName(MyBlackPath.class.getName());
				LitePal.use(blackList);
				List<String> blackListPaths = new ArrayList<>();
				for (final MyBlackPath blackPath : LitePal.findAll(MyBlackPath.class)) {
					blackListPaths.add(blackPath.getDirPath());
				}
				LitePal.useDefault();

				do {
					final String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

					if (blackListPaths.contains(path)) {
						Log.d(TAG, "loadDataSource: path in black path: " + path);
						continue;
					}

					final int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

					if (skipShort && duration <= DEFAULT_SHORT_DURATION) {
						Log.d(TAG, "loadDataSource: the music-file's duration is " + duration + " (too short), skip...");
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

					final Bundle bundle = new Bundle();
					bundle.putInt("albumId", albumId);
					bundle.putInt("artistId", artistId);
					bundle.putLong("addTime", addTime);
					bundle.putInt("size", size);
					bundle.putString("mimeType", mimeType);
					bundle.putLong("duration", duration);

					final MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder()
							.setTitle(name)
							.setSubtitle(albumName)
							.setDescription(artist)
							.setIconBitmap(null)
							.setMediaId(String.valueOf(id))
							.setMediaUri(Uri.fromFile(new File(path)))
							.setExtras(bundle);

					final MediaDescriptionCompat descriptionCompat = builder.build();
					if (lastId == id) {
						final MusicItem.Builder b2 = new MusicItem.Builder(id, name, path)
							.musicAlbum(albumName)
							.addTime(addTime)
							.artist(artist)
							.duration(duration)
							.mimeName(mimeType)
							.size(size)
							.addAlbumId(albumId)
							.addArtistId(artistId);
						mMusicItem = b2.build();

						MusicControl.setDataSource(MusicService.this, descriptionCompat);
						HAS_PLAYED = true;
					}

					ItemList.playOrderList.add(descriptionCompat);
					ItemList.playOrderListBK.add(descriptionCompat);
					controller.addQueueItem(descriptionCompat);
				}
				while (cursor.moveToNext());
				cursor.close();
			}
		}
	}

	/**
	 * start notification
	 * call by {@link MusicControl#playMusic()}
	 */
	private void startFN() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForeground(mStartNotificationId, NotificationTool.getChannelNotification(this, mediaSession).build());
		} else {
			startForeground(mStartNotificationId, NotificationTool.getNotification25(this, mediaSession).build());
		}
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy: ");
		HAS_PLAYED = false;
		stopForeground(true);
		MusicControl.release();
		MusicControl.mediaPlayer = null;
		mIsServiceDestroyed.set(true);

		if (wakeLock != null) wakeLock.release();

		mMusicItem = null;

		ItemList.historyList.clear();
		ItemList.playOrderList.clear();
		ItemList.playOrderListBK.clear();
		ItemList.trashCanList.clear();

		if (ItemList.mCurrentCover != null && !ItemList.mCurrentCover.isRecycled()) {
			ItemList.mCurrentCover.recycle();
		}

		try {
			unregisterReceiver(mMyHeadSetPlugReceiver);
		} catch (Exception e) {
			Log.d(TAG, "onDestroy: " + e.getMessage());
		}

		super.onDestroy();
		android.os.Process.killProcess(Process.myPid());
	}

	private void shuffleList(long seed) {
		if (seed == 0) {
			loadDataSource();
		} else {
			Collections.shuffle(ItemList.playOrderList, new Random(seed));
		}
	}

	public interface PlayType {
		String REPEAT_NONE = "REPEAT_NONE";
		String REPEAT_LIST = "REPEAT_LIST";
		String REPEAT_ONE = "REPEAT_ONE";
	}

	public interface IntentTAG {
		String PN_TYPE = "pnType";
	}

	public interface ServiceActions {
		/**
		 * special
		 */
		String ACTION_CLEAR_ITEMS = ACG_PLAYER_PACKAGE_NAME + ".clearitems";

		String ACTION_RESET_LIST = ACG_PLAYER_PACKAGE_NAME + ".resetlist";

		String ACTION_SHUFFLE_ORDER_LIST = ACG_PLAYER_PACKAGE_NAME + ".shuffleorderlist";

		/**
		 * common
		 */
		String ACTION_PLAY = ACG_PLAYER_PACKAGE_NAME + ".play";
		String ACTION_PAUSE = ACG_PLAYER_PACKAGE_NAME + ".intentPause";
		String ACTION_PN = ACG_PLAYER_PACKAGE_NAME + ".pntype";
		String ACTION_PN_NEXT = ACG_PLAYER_PACKAGE_NAME + ".pnnext";
		String ACTION_PN_PREVIOUS = ACG_PLAYER_PACKAGE_NAME + ".pnprevious";

		/**
		 * other
		 */
		String ACTION_FAST_SHUFFLE = ACG_PLAYER_PACKAGE_NAME + ".fastshuffle";

		String ACTION_ITEM_CLICK = ACG_PLAYER_PACKAGE_NAME + ".itemclick";

		String ACTION_TOGGLE_FAVOURITE = ACG_PLAYER_PACKAGE_NAME + ".togglefav";

		/**
		 * int extra key: next_item_id
		 */
		@Deprecated
		String ACTION_NEXT_WILL_PLAY = ACG_PLAYER_PACKAGE_NAME + ".nextwillplay";

		/**
		 * key: insert_music_id
		 */
		String ACTION_INSERT_MUSIC = ACG_PLAYER_PACKAGE_NAME + ".insertmusic";

		/**
		 * @apiNote 目前用于媒体按钮
		 */
		String ACTION_TOGGLE_PLAY_PAUSE = ACG_PLAYER_PACKAGE_NAME + ".toggleplaypause";

		// TODO: 2019/6/3 实现功能
		String ACTION_STOP = ACG_PLAYER_PACKAGE_NAME + ".stop";
	}

	/**
	 * data store
	 */
	private static class ItemList {
		static int CURRENT_MUSIC_INDEX = 0;

		///////////////////////////DATA//////////////////////////
		static List<MediaDescriptionCompat> playOrderList = new ArrayList<>();
		static List<MediaDescriptionCompat> playOrderListBK = new ArrayList<>();
		static List<MediaDescriptionCompat> trashCanList = new ArrayList<>();

		/**
		 * 存储播放历史(序列) default...
		 */
		static List<MediaDescriptionCompat> historyList = new ArrayList<>();
		static List<MediaDescriptionCompat> nextWillplay = new ArrayList<>();
		private static Bitmap mCurrentCover = null;

		static void updateCurrentCover(@Nullable final MediaDescriptionCompat item) {
			if (item == null || item.getMediaId() == null || item.getExtras() == null) {
				return;
			}
			ItemList.mCurrentCover = Utils.Audio.getCoverBitmapFull(serviceWeakReference.get()
					, item.getExtras().getInt("albumId"));
		}
	}

	public static class NotificationTool {

		/**
		 * NotificationId
		 */
		private static final String ID = "Player";

		private static NotificationCompat.Action actionSkipPrevious;
		private static NotificationCompat.Action actionSkipnext;

		public static void init(@NonNull final Context context) {
			//Notification
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
				NotificationChannel channel = new NotificationChannel(ID, "Now playing", NotificationManager.IMPORTANCE_DEFAULT);
				channel.setDescription("playing_notification_description");
				channel.enableLights(false);
				channel.enableVibration(false);
				channel.setShowBadge(false);
				channel.setSound(null, null);
				((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
			}


			actionSkipPrevious = new NotificationCompat.Action.Builder(R.drawable.ic_skip_previous_white_24dp, "intentPrevious",
					MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)).build();

			actionSkipnext = new NotificationCompat.Action.Builder(R.drawable.ic_skip_next_white_24dp, "intentNext",
					MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)).build();

		}

		private static NotificationCompat.Builder getNotification25(final Context context, @NonNull final MediaSessionCompat mediaSessionCompat) {

			MediaControllerCompat controller = mediaSessionCompat.getController();
			MediaMetadataCompat mediaMetadata = controller.getMetadata();
			MediaDescriptionCompat description = mediaMetadata.getDescription();

			androidx.media.app.NotificationCompat.MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle();
			mediaStyle.setMediaSession(mediaSessionCompat.getSessionToken())
					//小型化通知的按钮布局
					.setShowActionsInCompactView(0, 1, 2)
					.setShowCancelButton(true)
					.setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context
							, PlaybackStateCompat.ACTION_STOP));

			final Intent intent = new Intent(context, MainActivity.class).putExtra("intent_args", "by_notification");
			final PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ID)
					.setContentTitle(description.getTitle())
					.setContentText(description.getSubtitle())
					.setSubText(description.getDescription())
					.setSmallIcon(R.drawable.ic_audiotrack_24px)
					.setStyle(mediaStyle)
					.setLargeIcon(description.getIconBitmap())
					.setContentIntent(pi)
					.setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context,
							PlaybackStateCompat.ACTION_STOP))
					.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
					.setWhen(System.currentTimeMillis())
					.setColor(Utils.Ui.getPrimaryColor(context))
					.setColorized(PreferenceUtil.getDefault(context).getBoolean(Values.SharedPrefsTag.NOTIFICATION_COLORIZED, true));

			builder.addAction(actionSkipPrevious);

			builder.addAction(new NotificationCompat.Action.Builder(MusicControl.isPlayingMusic() ?
					R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_grey_600_24dp, "togglePausePlay"
					, MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY_PAUSE)).build()
			);

			builder.addAction(actionSkipnext);

			return builder;
		}

		@RequiresApi(api = Build.VERSION_CODES.O)
		@NonNull
		private static NotificationCompat.Builder getChannelNotification(final Context context
				, @NonNull final MediaSessionCompat mediaSessionCompat) {

			MediaControllerCompat controller = mediaSessionCompat.getController();
			@Nullable MediaMetadataCompat mediaMetadata = controller.getMetadata();
			@Nullable MediaDescriptionCompat description = null;
			if (mediaMetadata != null) description = mediaMetadata.getDescription();

			//pi(s)
			Intent intent = new Intent(context, MainActivity.class).putExtra("intent_args", "by_notification");
			PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			androidx.media.app.NotificationCompat.MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle();
			mediaStyle.setMediaSession(mediaSessionCompat.getSessionToken())
					//小型化通知的按钮布局
					.setShowActionsInCompactView(0, 1, 2)
					.setShowCancelButton(true)
					.setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context
							, PlaybackStateCompat.ACTION_STOP));

			NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ID)
					.setContentTitle(description == null ? mMusicItem.getMusicName() : description.getTitle())
					.setContentText(description == null ? mMusicItem.getMusicAlbum() : description.getSubtitle())
					.setSubText(description == null ? mMusicItem.getArtist() : description.getDescription())
					.setSmallIcon(R.drawable.ic_audiotrack_24px)
					.setStyle(mediaStyle)
					.setLargeIcon(description == null ? Utils.Audio.getCoverBitmapFull(context
							, mMusicItem.getAlbumId()) : description.getIconBitmap())
					.setContentIntent(pi)
					.setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context,
							PlaybackStateCompat.ACTION_STOP))
					.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
					.setWhen(System.currentTimeMillis())
					.setColor(Utils.Ui.getPrimaryColor(context))
					.setColorized(PreferenceUtil.getDefault(context).getBoolean(Values.SharedPrefsTag.NOTIFICATION_COLORIZED, true));

			builder.addAction(actionSkipPrevious);

			builder.addAction(new NotificationCompat.Action.Builder(MusicControl.isPlayingMusic() ?
					R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_grey_600_24dp, "togglePausePlay"
					, MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY_PAUSE)).build()
			);

			builder.addAction(actionSkipnext);

			return builder;
		}

	}

	public static class MusicControl {

		private static MediaPlayer mediaPlayer = null;

		public synchronized static void init(@NonNull final Context context) {
			if (mediaPlayer != null) return;

			CustomThreadPool.post(() -> {
				mediaPlayer = new MediaPlayer();
				MusicControl.mediaPlayer.setOnCompletionListener(mp -> {
					//noinspection ConstantConditions
					switch (PreferenceUtil.getDefault(context).getString(Values.SharedPrefsTag.PLAY_TYPE, PlayType.REPEAT_NONE)) {
						case PlayType.REPEAT_LIST: {
							// TODO: 2019/5/30
						}
						break;

						case PlayType.REPEAT_NONE: {
							if (ItemList.CURRENT_MUSIC_INDEX != ItemList.playOrderList.size()) {
								Intent intent = new Intent(ServiceActions.ACTION_PN);
								intent.putExtra(IntentTAG.PN_TYPE, ServiceActions.ACTION_PN_NEXT);
								ReceiverOnMusicPlay.startService(context, intent);
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

				mediaPlayer.setOnPreparedListener(mp -> {
					Log.d(TAG, "onPrepared: done");
					mp.start();
					serviceWeakReference.get().startFN();
					HAS_PLAYED = true;
				});

			});
		}

		private synchronized static void reset(final boolean saveData) {
			if (mediaPlayer == null) return;
			/*
			 * 记录播放信息
			 * */
			if (HAS_PLAYED && mMusicItem != null && saveData) {
				final List<Detail> infos = LitePal.where("MusicId = ?", String.valueOf(mMusicItem.getMusicID())).find(Detail.class);
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
					detail.setMusicId(mMusicItem.getMusicID());
					if (mediaPlayer.getCurrentPosition() < MINIMUM_PLAY_TIME) {
						detail.setMinimumPlayTimes(detail.getMinimumPlayTimes() + 1);
					} else {
						detail.setPlayDuration(detail.getPlayDuration() + mediaPlayer.getCurrentPosition());
					}
					detail.setPlayTimes(detail.getPlayTimes() + 1);
					detail.save();
				}
			}

			mediaPlayer.reset();
		}

		/**
		 * play music
		 * use {@link MediaPlayer#setOnPreparedListener(MediaPlayer.OnPreparedListener)}
		 */
		private synchronized static void playMusic() {
			if (mediaPlayer == null) return;

			mediaPlayer.start();
			HAS_PLAYED = true;
			serviceWeakReference.get().startFN();
			// update last played
			if (mMusicItem != null) {
				PreferenceUtil.getDefault(serviceWeakReference.get()).edit()
						.putInt(Values.SharedPrefsTag.LAST_PLAY_MUSIC_ID, mMusicItem.getMusicID()).apply();
			}
		}

		private synchronized static void pauseMusic() {
			if (mediaPlayer == null) return;

			mediaPlayer.pause();
			serviceWeakReference.get().startFN();
			serviceWeakReference.get().stopForeground(false);
		}

		private synchronized static void stopMusic() {
			if (mediaPlayer == null) return;

			mediaPlayer.stop();
			serviceWeakReference.get().stopForeground(true);
		}

		private synchronized static boolean isPlayingMusic() {
			if (mediaPlayer == null) return false;

			return mediaPlayer.isPlaying();
		}

		private synchronized static void setDataSource(@NonNull final Context context
				, @Nullable MediaDescriptionCompat item) {
			if (mediaPlayer == null || item == null || item.getMediaUri() == null) {
				return;
			}

			try {
				mediaPlayer.setDataSource(context, item.getMediaUri());
				ItemList.historyList.add(item);
				ItemList.updateCurrentCover(item);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
				mediaPlayer.reset();
			}
		}

		private synchronized static void prepareAndPlay() {
			if (mediaPlayer == null) return;

			try {
				mediaPlayer.prepare();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
				mediaPlayer.reset();
			}
		}

		private synchronized static int getDuration() {
			if (mediaPlayer == null) return 0;

			return mediaPlayer.isPlaying() ? mediaPlayer.getDuration() : 0;
		}

		private synchronized static int getCurrentPosition() {
			if (mediaPlayer == null) return 0;

			return mediaPlayer.isPlaying() ? mediaPlayer.getCurrentPosition() : 0;
		}

		private synchronized static void seekTo(int position) {
			if (mediaPlayer == null) return;

			mediaPlayer.seekTo(position);
		}

		private synchronized static void release() {
			if (mediaPlayer == null) return;

			mediaPlayer.release();
		}

		//////////////////////EXP

		public static void intentNext(@Nullable final Context context) {
			if (context == null) return;
			Intent next = new Intent(MusicService.ServiceActions.ACTION_PN);
			next.putExtra(IntentTAG.PN_TYPE, MusicService.ServiceActions.ACTION_PN_NEXT);
			ReceiverOnMusicPlay.startService(context, next);
		}

		public static void intentPrevious(@Nullable final Context context) {
			if (context == null) return;
			Intent next = new Intent(MusicService.ServiceActions.ACTION_PN);
			next.putExtra(IntentTAG.PN_TYPE, ServiceActions.ACTION_PN_PREVIOUS);
			ReceiverOnMusicPlay.startService(context, next);
		}

		public static void intentItemClick(@NonNull final Context context, @NonNull MusicItem item) {
			final Intent intent = new Intent(MusicService.ServiceActions.ACTION_ITEM_CLICK);
			intent.putExtra("item", item);
			ReceiverOnMusicPlay.startService(context, intent);
		}

		public static void intentPlay(@NonNull final Context context) {
			final Intent intent = new Intent(ServiceActions.ACTION_PLAY);
			ReceiverOnMusicPlay.startService(context, intent);
		}

		public static void intentPause(@NonNull final Context context) {
			final Intent intent = new Intent(ServiceActions.ACTION_PAUSE);
			ReceiverOnMusicPlay.startService(context, intent);
		}

		public static void intentTogglePlayPause(@NonNull final Context context) {
			if (isPlayingMusic()) {
				intentPause(context);
			} else {
				intentPlay(context);
			}
		}
	}
}
