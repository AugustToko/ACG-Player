package top.geek_studio.chenlongcould.musicplayer.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.ListViewActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.fragment.MusicDetailFragment;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author chenlongcould
 */
public final class ReceiverOnMusicPlay extends BroadcastReceiver {

	public static final String TAG = "ReceiverOnMusicPlay";

	public static final int CASE_TYPE_SHUFFLE = 90;
	public static final int CASE_TYPE_ITEM_CLICK = 15;
	public static final int CASE_TYPE_NOTIFICATION_RESUME = 2;
	public static final int RECEIVE_TYPE_COMMON = 6;


	public static final String INTENT_PLAY_TYPE = "play_type";

	public static final String INTENT_ARGS = "args";

	public static final String TYPE_NEXT = "next";
	public static final String TYPE_PREVIOUS = "previous";
	public static final String TYPE_SLIDE = "slide";

	/**
	 * the mediaPlayer is Ready?
	 */
	public static AtomicBoolean READY = new AtomicBoolean(true);

	public static final Object object = new Object();

//	/**
//	 * setButtonPlay
//	 * setSeekBarColor
//	 * setSeekBarPosition
//	 * setSlideBar
//	 *
//	 * @param context     context
//	 * @param targetIndex index
//	 */
//	private static void setUIMusicData(final Context context, final int targetIndex) {
//		Data.sCurrentMusicItem = Data.sPlayOrderList.get(targetIndex);
//		final Bitmap cover = Utils.Audio.getCoverBitmap(context, Data.sCurrentMusicItem.getAlbumId());
//		MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.INIT_SEEK_BAR);
//		MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_BUTTON_PLAY);
//
//		final Message message = Message.obtain();
//		message.what = MusicDetailFragment.NotLeakHandler.SET_CURRENT_DATA;
//		message.obj = cover;
//		final Bundle bundle = new Bundle();
//		bundle.putString("name", Data.sCurrentMusicItem.getMusicName());
//		bundle.putString("albumName", Data.sCurrentMusicItem.getMusicAlbum());
//		message.setData(bundle);
//		MusicDetailFragment.sendMessage(message);
//
//		MusicDetailFragment.setSeekBar(0);         //防止seekBar跳动到Max
//		MainActivity.sendEmptyMessage(MainActivity.NotLeakHandler.SET_SLIDE_TOUCH_ENABLE);
//	}

	/**
	 * setFlags
	 *
	 * @param targetIndex index
	 */
	private static void setFlags(int targetIndex) {
		Values.CurrentData.CURRENT_MUSIC_INDEX = targetIndex;
	}

	////////////////////////MEDIA CONTROL/////////////////////////////

	public synchronized static void setDataSource(@NonNull final MusicItem item) {
		if (Data.sMusicBinder == null) {
			Log.d(TAG, "resetMusic: MusicBinder is null.");
			return;
		}
		ListViewActivity.addToHistory(item);
		try {
			Data.sMusicBinder.setCurrentMusicData(item);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public synchronized static void playMusic() {
		if (Data.sMusicBinder == null) {
			Log.d(TAG, "resetMusic: MusicBinder is null.");
			return;
		}
		try {
			Data.sMusicBinder.playMusic();
			Data.HAS_PLAYED = true;
		} catch (RemoteException e) {
			e.printStackTrace();
			try {
				Data.sMusicBinder.resetMusic();
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		}
	}

	public synchronized static void pauseMusic() {
		if (Data.sMusicBinder == null) {
			Log.d(TAG, "resetMusic: MusicBinder is null.");
			return;
		}
		try {
			Data.sMusicBinder.pauseMusic();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public synchronized static void resetMusic() {
		if (Data.sMusicBinder == null) {
			Log.d(TAG, "resetMusic: MusicBinder is null.");
			return;
		}
		try {
			Data.sMusicBinder.resetMusic();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public synchronized static void prepare() {
		if (Data.sMusicBinder == null) {
			Log.d(TAG, "resetMusic: MusicBinder is null.");
			return;
		}
		try {
			Data.sMusicBinder.prepare();
		} catch (RemoteException e) {
			e.printStackTrace();
			try {
				Data.sMusicBinder.resetMusic();
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		}
	}

	public synchronized static int getDuration() {
		int duration = 1;
		if (Data.sMusicBinder == null) {
			Log.d(TAG, "resetMusic: MusicBinder is null.");
			return duration;
		}
		try {
			duration = Data.sMusicBinder.getDuration();
			//issue: duration may br zero, if quick click "fast play" button
			return duration == 0 ? 1 : duration;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return duration;
	}

	public synchronized static boolean isPlayingMusic() {
		if (Data.sMusicBinder == null) {
			Log.d(TAG, "resetMusic: MusicBinder is null.");
			return false;
		}
		try {
			return Data.sMusicBinder.isPlayingMusic();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	public synchronized static int getCurrentPosition() {
		if (Data.sMusicBinder == null) {
			Log.d(TAG, "resetMusic: MusicBinder is null.");
			return 0;
		}
		try {
			return Data.sMusicBinder.getCurrentPosition();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public synchronized static void seekTo(int nowPosition) {
		if (Data.sMusicBinder == null) {
			Log.d(TAG, "resetMusic: MusicBinder is null.");
			return;
		}
		try {
			Data.sMusicBinder.seekTo(nowPosition);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	public synchronized static void stopMusic() {
		if (Data.sMusicBinder == null) {
			Log.d(TAG, "resetMusic: MusicBinder is null.");
			return;
		}
		try {
			Data.sMusicBinder.stopMusic();
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
	public synchronized static MusicItem getCurrentItem() {
		if (Data.sMusicBinder == null) {
			Log.d(TAG, "resetMusic: MusicBinder is null.");
			return null;
		}
		try {
			return Data.sMusicBinder.getCurrentItem();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	////////////////////////MEDIA CONTROL/////////////////////////////

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.d(TAG, "onReceive: done");

		final int type = intent.getIntExtra(INTENT_PLAY_TYPE, 0);

		///////////////////////////BEFORE PLAYER SET/////////////////////////////////////////

		switch (type) {
			//clicked by notif, just resume play
			case CASE_TYPE_NOTIFICATION_RESUME: {
				MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_BUTTON_PLAY);
				playMusic();
			}
			break;

			//pause music
			case -1: {
				pauseMusic();
				MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_BUTTON_PAUSE);
			}
			break;

			//Type Random (play)
			case CASE_TYPE_SHUFFLE: {

				if (Data.sPlayOrderList.isEmpty()) {
					return;
				}

				resetMusic();

				//get data
				final Random random = new Random();
				int index = random.nextInt(Data.sPlayOrderList.size());

				// 循环检测是否播放到 “垃圾桶” 中的歌曲，如是，则跳过
				for (; ; ) {
					if (Data.S_TRASH_CAN_LIST.contains(Data.sPlayOrderList.get(index))) {
						index = random.nextInt(Data.sPlayOrderList.size());
					} else {
						Values.CurrentData.CURRENT_MUSIC_INDEX = index;
						break;
					}
				}

				Data.sCurrentMusicItem = Data.sPlayOrderList.get(index);
				setFlags(index);

				setDataSource(Data.sCurrentMusicItem);
				prepare();
				playMusic();

				final Bitmap cover = Utils.Audio.getCoverBitmap(context, Data.sCurrentMusicItem.getAlbumId());
				Data.setCurrentCover(cover);

//				MusicDetailFragment.setSeekBar(0);         //防止seekBar跳动到Max
				MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.INIT_SEEK_BAR);
				MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_BUTTON_PLAY);
				MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_CURRENT_DATA);

//				final Message message = Message.obtain();
//				message.what = MusicDetailFragment.NotLeakHandler.SET_CURRENT_DATA;
//				message.obj = cover;
//				final Bundle bundle = new Bundle();
//				bundle.putString("name", Data.sCurrentMusicItem.getMusicName());
//				bundle.putString("albumName", Data.sCurrentMusicItem.getMusicAlbum());
//				message.setData(bundle);
//				MusicDetailFragment.sendMessage(message);


				sureCar();

			}
			break;

			//by next button...(in detail or noti) (must ActivityList isn't empty)
			//by auto-next(mediaPlayer OnCompletionListener) of next-play by user, at this time MainActivity is present
			//by MusicDetailFragment preview imageButton (view history song list)
			case RECEIVE_TYPE_COMMON: {

				//检测是否指定下一首播放
				if (Data.sNextWillPlayItem != null) {
					new DoesHasNextPlay(context).execute();
					break;
				}

				//检测循环
				if (Values.CurrentData.CURRENT_PLAY_TYPE.equals(Values.TYPE_REPEAT_ONE)) {
					seekTo(0);
					playMusic();
					break;
				}

				//检测大小
				if (Data.sPlayOrderList.size() <= 0) {
					Toast.makeText(context, "Data.sPlayOrderList.size() <= 0", Toast.LENGTH_SHORT).show();
					break;
				}

				resetMusic();

				int targetIndex = getIndex(intent.getStringExtra(INTENT_ARGS));

				// 循环检测是否播放到 “垃圾桶” 中的歌曲，如是，则跳过
				for (; ; ) {
					if (Data.S_TRASH_CAN_LIST.contains(Data.sPlayOrderList.get(targetIndex))) {
						targetIndex = getIndex(intent.getStringExtra(INTENT_ARGS));
					} else {
						Values.CurrentData.CURRENT_MUSIC_INDEX = targetIndex;
						break;
					}
				}

				Data.sCurrentMusicItem = Data.sPlayOrderList.get(targetIndex);

				setDataSource(Data.sCurrentMusicItem);
				prepare();
				playMusic();

				setFlags(targetIndex);

				final Bitmap cover = Utils.Audio.getCoverBitmap(context, Data.sCurrentMusicItem.getAlbumId());
				Data.setCurrentCover(cover);

				//load data

				//slide 滑动切歌无需再次加载albumCover
				if (intent.getStringExtra(INTENT_ARGS) != null && intent.getStringExtra(INTENT_ARGS).contains(TYPE_SLIDE)) {
					final String musicName = Data.sPlayOrderList.get(targetIndex).getMusicName();
					final String albumName = Data.sPlayOrderList.get(targetIndex).getMusicAlbum();

					MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.INIT_SEEK_BAR);
					//set music data
					final Message message = Message.obtain();
					message.what = MusicDetailFragment.NotLeakHandler.setCurrentInfoWithoutMainImage;
					final Bundle bundle = new Bundle();
					bundle.putString("name", musicName);
					bundle.putString("albumName", albumName);
					message.setData(bundle);
					message.obj = cover;
					MusicDetailFragment.sendMessage(message);
				} else {
					MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.INIT_SEEK_BAR);
					MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_BUTTON_PLAY);
					MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_CURRENT_DATA);
				}
				sureCar();
			}
			break;

			//by MusicListFragment item click
			case CASE_TYPE_ITEM_CLICK: {

				Data.setCurrentCover(Utils.Audio.getCoverBitmap(context, Data.sCurrentMusicItem.getAlbumId()));

				//set current data
				resetMusic();
				setDataSource(Data.sCurrentMusicItem);
				prepare();
				playMusic();

				sureCar();

				MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.INIT_SEEK_BAR);
				MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_BUTTON_PLAY);
				MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_CURRENT_DATA);

			}
			break;
			default:
		}

		///////////////////////////AFTER PLAYER SET/////////////////////////////////////////

		MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.RECYCLER_SCROLL);
		MainActivity.sendEmptyMessage(MainActivity.NotLeakHandler.SET_SLIDE_TOUCH_ENABLE);

	}

	////////////////////////////////////////////////////////////////

	/**
	 * 获取下个播放的 index
	 *
	 * @param playType 播放的模式 (向前 或者 向后)
	 */
	private int getIndex(@NonNull String playType) {
		int targetIndex = 0;
		if (playType.contains(TYPE_NEXT)) {
			targetIndex = Values.CurrentData.CURRENT_MUSIC_INDEX + 1;
			//超出范围自动跳转0
			if (targetIndex > Data.sPlayOrderList.size() - 1) {
				targetIndex = 0;
			}
		} else if (playType.contains(TYPE_PREVIOUS)) {
			targetIndex = Values.CurrentData.CURRENT_MUSIC_INDEX - 1;
			if (targetIndex < 0) {
				//超出范围超转最后
				targetIndex = Data.sPlayOrderList.size() - 1;
			}
		}
		return targetIndex;
	}

	public static void sureCar() {
		//set data (image and name)
		if (Values.CurrentData.CURRENT_UI_MODE.equals(Values.CurrentData.MODE_CAR)) {
			Data.sCarViewActivity.getFragmentLandSpace().setData();
		}
	}

	/**
	 * Receive Types
	 */
	public interface ReceiveType {
		int CASE_TYPE_SHUFFLE = 90;
		int CASE_TYPE_ITEM_CLICK = 15;
		int CASE_TYPE_NOTIFICATION_RESUME = 2;
		int RECEIVE_TYPE_COMMON = 6;
	}

	/**
	 * 当下一首歌曲存在(被手动指定时), auto-next-play and next-play will call this method
	 */
// FIXME: 2019/5/23 static context
	public static class DoesHasNextPlay extends AsyncTask<Void, Void, Integer> {

		private WeakReference<Context> mContext;

		public DoesHasNextPlay(@NonNull Context context) {
			mContext = new WeakReference<>(context);
		}

		@Override
		protected Integer doInBackground(Void... voids) {

			if (Data.sNextWillPlayItem != null) {
				Data.sCurrentMusicItem = Data.sNextWillPlayItem;
				resetMusic();
				ReceiverOnMusicPlay.setDataSource(Data.sNextWillPlayItem);
				prepare();
				playMusic();
			} else {
				return -1;
			}

			return 0;
		}

		@Override
		protected void onPostExecute(Integer status) {

			//error
			if (status != 0) {
				return;
			}

			final Bitmap cover = Utils.Audio.getCoverBitmap(mContext.get(), Data.sNextWillPlayItem.getAlbumId());
			Data.setCurrentCover(cover);

			MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.INIT_SEEK_BAR);
			MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_BUTTON_PLAY);
			MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_CURRENT_DATA);

			sureCar();

			Data.sNextWillPlayItem = null;
		}
	}

//	/**
//	 * play_type: random (by nextButton or auto-next)
//	 * just make a random Index (data by {@link Data#sPlayOrderList})
//	 */
//	public static class FastShufflePlayback extends AsyncTask<String, Void, Integer> {
//
//		private WeakReference<Context> mContext;
//
//		FastShufflePlayback(final Context context) {
//			mContext = new WeakReference<>(context);
//		}
//
//		@Override
//		protected Integer doInBackground(String... strings) {
//			if (Data.sPlayOrderList.isEmpty()) {
//				return -1;
//			}
//			resetMusic();
//
//			//get data
//			final Random random = new Random();
//			int index = random.nextInt(Data.sPlayOrderList.size());
//
//			// 循环检测是否播放到 “垃圾桶” 中的歌曲，如是，则跳过
//			for (; ; ) {
//				if (Data.S_TRASH_CAN_LIST.contains(Data.sPlayOrderList.get(index))) {
//					index = random.nextInt(Data.sPlayOrderList.size());
//				} else {
//					Values.CurrentData.CURRENT_MUSIC_INDEX = index;
//					break;
//				}
//			}
//
//			Data.sCurrentMusicItem = Data.sPlayOrderList.get(index);
//			setFlags(index);
//
//			setDataSource(Data.sCurrentMusicItem);
//			prepare();
//			playMusic();
//
//			return index;
//		}
//
//		@Override
//		protected void onPostExecute(Integer result) {
//			if (result < 0) {
//				return;
//			}
//
//			setUIMusicData(mContext.get(), result);
//			sureCar();
//		}
//
//		@Override
//		protected void onCancelled() {
//		}
//	}

}
