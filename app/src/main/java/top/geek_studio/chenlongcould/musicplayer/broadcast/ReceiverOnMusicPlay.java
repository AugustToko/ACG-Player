package top.geek_studio.chenlongcould.musicplayer.broadcast;

import android.annotation.TargetApi;
import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.MusicService;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.CarViewActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.fragment.MusicDetailFragment;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.threadPool.CustomThreadPool;
import top.geek_studio.chenlongcould.musicplayer.utils.MusicUtil;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenlongcould
 */
public final class ReceiverOnMusicPlay extends BroadcastReceiver {

	public static final String TAG = "ReceiverOnMusicPlay";

	public static final byte FLASH_UI_COMMON = 127;
	public static final byte PLAY = 100;
	public static final byte PAUSE = 101;

	public static final String INTENT_PLAY_TYPE = "play_type";

	////////////////////////MEDIA CONTROL/////////////////////////////

	public static int getDuration() {
		int duration = 0;
		try {
			duration = Data.sMusicBinder.getDuration();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return duration;
	}

	public static boolean isPlayingMusic() {
		if (Data.sMusicBinder == null) {
			Log.d(TAG, "MusicBinder is null.");
			return false;
		}
		try {
			return Data.sMusicBinder.isPlayingMusic();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static int getCurrentPosition() {
		if (Data.sMusicBinder == null) {
			Log.d(TAG, "MusicBinder is null.");
			return 0;
		}
		try {
			return Data.sMusicBinder.getCurrentPosition();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static void seekTo(int nowPosition) {
		if (Data.sMusicBinder == null) {
			Log.d(TAG, "MusicBinder is null.");
			return;
		}
		try {
			Data.sMusicBinder.seekTo(nowPosition);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	public synchronized static void setMusicItem(final MusicItem item) {
		if (Data.sMusicBinder != null) {
			try {
				Data.sMusicBinder.setCurrentMusicData(item);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Nullable
	public static MusicItem getCurrentItem() {
		if (Data.sMusicBinder == null) {
			Log.d(TAG, "MusicBinder is null.");
			return null;
		}
		try {
			return Data.sMusicBinder.getCurrentItem();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void playFromUri(@NonNull Context context, @Nullable Uri uri) {
		if (Data.sMusicBinder != null && uri != null) {
			List<MusicItem> songs = null;
//
			if (uri.getScheme() != null && uri.getAuthority() != null) {
				if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
					String songId = null;
					if (uri.getAuthority().equals("com.android.providers.media.documents")) {
						songId = getSongIdFromMediaProvider(uri);
						Log.d(TAG, "playFromUri: getSongIdFromMediaProvider: " + songId);
					} else if (uri.getAuthority().equals("media")) {
						songId = uri.getLastPathSegment();
						Log.d(TAG, "playFromUri: getLastPathSegment: " + songId);
					}
					if (songId != null) {
						Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
								null, MediaStore.Audio.AudioColumns._ID + "=?", new String[]{songId}, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
						List<MusicItem> items = new ArrayList<>();
						if (cursor != null && cursor.moveToFirst()) {
							do {
								items.add(MusicUtil.getSongFromCursorImpl(cursor));
							} while (cursor.moveToNext());
						}

						if (cursor != null) {
							cursor.close();
						}

						songs = items;

						Log.d(TAG, "playFromUri: " + songs.get(0).toString());
					}
				}
			}

			if (songs == null) {
				File songFile = null;
				if (uri.getAuthority() != null && uri.getAuthority().equals("com.android.externalstorage.documents")) {
					songFile = new File(Environment.getExternalStorageDirectory(), uri.getPath().split(":", 2)[1]);
				}
				if (songFile == null) {
					String path = getFilePathFromUri(context, uri);
					if (path != null) {
						songFile = new File(path);
					}
				}
				if (songFile == null && uri.getPath() != null) {
					songFile = new File(uri.getPath());
				}
				if (songFile != null) {
					Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
							null, MediaStore.Audio.AudioColumns.DATA + "=?"
							, new String[]{songFile.getAbsolutePath()}, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
					List<MusicItem> items = new ArrayList<>();
					if (cursor != null && cursor.moveToFirst()) {
						do {
							items.add(MusicUtil.getSongFromCursorImpl(cursor));
						} while (cursor.moveToNext());
					}

					if (cursor != null) {
						cursor.close();
					}

					songs = items;
				}
			}

			//noinspection StatementWithEmptyBody
			if (songs != null && !songs.isEmpty()) {
				try {
					Data.sMusicBinder.setNextWillPlayItem(songs.get(0));
					MusicService.MusicControl.next(context);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} else {
				//TODO the file is not listed in the media store
			}
		}
	}

	@Nullable
	private static String getFilePathFromUri(Context context, Uri uri) {
		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {
				column
		};

		try {
			cursor = context.getContentResolver().query(uri, projection, null, null,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	private static String getSongIdFromMediaProvider(Uri uri) {
		return DocumentsContract.getDocumentId(uri).split(":")[1];
	}


	public synchronized static void sureCar() {
		//set data (image and name)
		if (Values.CurrentData.CURRENT_UI_MODE.equals(Values.UIMODE.MODE_CAR)) {
			CarViewActivity.sendEmptyMessage(CarViewActivity.NotLeakHandler.SET_DATA);
		}
	}

	public static void startService(@NonNull Context context, @NonNull String action) {
		final ComponentName serviceName = new ComponentName(context, MusicService.class);
		Intent resumeIntent = new Intent(action);
		resumeIntent.setComponent(serviceName);
		context.startService(resumeIntent);
	}

	public static void startService(@NonNull Context context, @NonNull Intent intent) {
		final ComponentName serviceName = new ComponentName(context, MusicService.class);
		intent.setComponent(serviceName);
		context.startService(intent);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		CustomThreadPool.post(() -> {
			final byte type = intent.getByteExtra(INTENT_PLAY_TYPE, Byte.MAX_VALUE);

			///////////////////////////BEFORE PLAYER SET/////////////////////////////////////////

			switch (type) {
				//clicked by notif, just resume play
				case FLASH_UI_COMMON: {
					Log.d(TAG, "onReceive: common");
					final MusicItem item = intent.getParcelableExtra("item");
					if (item != null) {
						if (item.getMusicID() == Data.sCurrentMusicItem.getMusicID()) {
							break;
						} else {
							Data.sCurrentMusicItem = item;
						}

						final Bitmap cover = Utils.Audio.getCoverBitmapFull(context, Data.sCurrentMusicItem.getAlbumId());
						Data.setCurrentCover(cover);
						MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.INIT_SEEK_BAR);
						MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_BUTTON_PLAY);
						MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_CURRENT_DATA);
						sureCar();
					}
				}
				break;

				case PLAY: {
					MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_BUTTON_PLAY);
					Log.d(TAG, "onReceive: after resume");
				}
				break;

				//pause music
				case PAUSE: {
					MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_BUTTON_PAUSE);
					Log.d(TAG, "onReceive: after pause");
				}
				break;

				default:
			}

			MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.RECYCLER_SCROLL);
			MainActivity.sendEmptyMessage(MainActivity.NotLeakHandler.SET_SLIDE_TOUCH_ENABLE);

			Data.HAS_PLAYED = true;
		});
	}
}
