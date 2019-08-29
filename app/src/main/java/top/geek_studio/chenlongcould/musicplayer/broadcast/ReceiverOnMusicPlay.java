package top.geek_studio.chenlongcould.musicplayer.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Message;
import android.os.RemoteException;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.annotation.Nullable;

import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.CarViewActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.main.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.fragment.MusicDetailFragment;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.threadPool.CustomThreadPool;

/**
 * @author chenlongcould
 */
public final class ReceiverOnMusicPlay extends BroadcastReceiver {

	public static final String TAG = "ReceiverOnMusicPlay";

	public static final byte FLASH_UI_COMMON = 127;
	public static final byte FLASH_UI_PLAY = 100;
	public static final byte FLASH_UI_PAUSE = 101;

	public static final String INTENT_PLAY_TYPE = "play_type";
	public static final byte TOGGLE_FAV = 102;

	////////////////////////MEDIA CONTROL/////////////////////////////

	public static int getDuration() {
		if (Data.sMusicBinder == null) {
			Log.d(TAG, "getDuration: MusicBinder is null.");
			return 0;
		}

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
			Log.d(TAG, "isPlayingMusic: MusicBinder is null.");
			return false;
		}
		try {
			return Data.sMusicBinder.isPlayingMusic();
		} catch (RemoteException | IllegalStateException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static int getCurrentPosition() {
		if (Data.sMusicBinder == null) {
			Log.d(TAG, "getCurrentPosition: MusicBinder is null.");
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
			Log.d(TAG, "seekTo: MusicBinder is null.");
			return;
		}
		try {
			Data.sMusicBinder.seekTo(nowPosition);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Nullable
	public static MusicItem getCurrentItem() {
		if (Data.sMusicBinder == null) {
			Log.d(TAG, "getCurrentItem: MusicBinder is null.");
			return null;
		}
		try {
			return Data.sMusicBinder.getCurrentItem();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Nullable
	public static String getFilePathFromUri(Context context, Uri uri) {
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

	public static String getSongIdFromMediaProvider(Uri uri) {
		return DocumentsContract.getDocumentId(uri).split(":")[1];
	}

	public synchronized static void sureCar() {
		//set data (image and name)
		if (Values.CurrentData.CURRENT_UI_MODE.equals(Values.UIMODE.MODE_CAR)) {
			CarViewActivity.sendEmptyMessage(CarViewActivity.NotLeakHandler.SET_DATA);
		}
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
					if (item != null && item.getMusicID() != -1) {

//						if (Data.sCurrentMusicItem != null && item.getMusicID() == Data.sCurrentMusicItem.getMusicID()) {
//							Log.d(TAG, "onReceive: item is same break");
//							break;
//						} else {
//							Data.sCurrentMusicItem = item;
//							Data.sHistoryPlayed.add(item);
//							ListViewActivity.sendEmptyMessageStatic(ListViewActivity.NotLeakHandler.NOTI_ADAPTER_CHANGED);
//							Log.d(TAG, "onReceive: common item is not same, new item name: " + item.toString());
//						}

						final Message message = Message.obtain();
						message.what = MusicDetailFragment.NotLeakHandler.SETUP_MUSIC_DATA;
						message.obj = item;

						MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.INIT_SEEK_BAR);
						MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_BUTTON_PLAY);
						MusicDetailFragment.sendMessage(message);
						sureCar();
					} else {
						Log.d(TAG, "onReceive: common receiver item is null");
					}
				}
				break;

				case FLASH_UI_PLAY: {
					MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_BUTTON_PLAY);
					Log.d(TAG, "onReceive: after resume");
				}
				break;

				//intentPause music
				case FLASH_UI_PAUSE: {
					MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_BUTTON_PAUSE);
					Log.d(TAG, "onReceive: after intentPause");
				}
				break;

				case TOGGLE_FAV: {
					MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.TOGGLE_FAV);
				}
				break;

				default:
			}

			MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.RECYCLER_SCROLL);
			MainActivity.sendEmptyMessageStatic(MainActivity.NotLeakHandler.SET_SLIDE_TOUCH_ENABLE);

			Data.HAS_PLAYED = true;
		});
	}
}
