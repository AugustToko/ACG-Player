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
import top.geek_studio.chenlongcould.musicplayer.fragment.BaseFragment;
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

	/**
	 * setButtonPlay
	 * setSeekBarColor
	 * setSeekBarPosition
	 * setSlideBar
	 *
	 * @param context     context
	 * @param targetIndex index
	 */
	private static void setUIMusicData(final Context context, final int targetIndex) {
		Data.sCurrentMusicItem = Data.sPlayOrderList.get(targetIndex);
		final Bitmap cover = Utils.Audio.getCoverBitmap(context, Data.sCurrentMusicItem.getAlbumId());
		MusicDetailFragment.mHandler.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.INIT_SEEK_BAR);
		MusicDetailFragment.mHandler.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_BUTTON_PLAY);

		// FIXME: 2019/5/22 
		((MusicDetailFragment) ((MainActivity) Data.sActivities.get(0)).getFragment(BaseFragment.FragmentType.MUSIC_DETAIL_FRAGMENT))
				.setCurrentInfo(Data.sCurrentMusicItem.getMusicName(), Data.sCurrentMusicItem.getMusicAlbum(), cover);

		MusicDetailFragment.setSeekBar(0);         //防止seekBar跳动到Max
		MainActivity.mHandler.sendEmptyMessage(MainActivity.NotLeakHandler.SET_SLIDE_TOUCH_ENABLE);
	}

	/**
	 * setFlags
	 *
	 * @param targetIndex index
	 */
	private static void setFlags(int targetIndex) {
		Values.CurrentData.CURRENT_MUSIC_INDEX = targetIndex;
	}

	////////////////////////MEDIA CONTROL/////////////////////////////

	public static void setDataSource(@NonNull final MusicItem item) {
		ListViewActivity.addToHistory(item);
		try {
			Data.sMusicBinder.setCurrentMusicData(item);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static void playMusic() {
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

	public static void pauseMusic() {
		try {
			Data.sMusicBinder.pauseMusic();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static void resetMusic() {
		try {
			Data.sMusicBinder.resetMusic();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static void prepare() {
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

	public static int getDuration() {
		try {
			return Data.sMusicBinder.getDuration();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static boolean isPlayingMusic() {
		try {
			return Data.sMusicBinder.isPlayingMusic();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static int getCurrentPosition() {
		try {
			return Data.sMusicBinder.getCurrentPosition();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static void seekTo(int nowPosition) {
		try {
			Data.sMusicBinder.seekTo(nowPosition);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	public static void stopMusic() {
		try {
			Data.sMusicBinder.stopMusic();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	public static void setMusicItem(final MusicItem item) {
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
		if (Data.sMusicBinder != null) {
			try {
				return Data.sMusicBinder.getCurrentItem();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			return null;
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
				MusicDetailFragment.mHandler.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_BUTTON_PLAY);
				playMusic();
			}
			break;

			//pause music
			case -1: {
				pauseMusic();
				MusicDetailFragment.mHandler.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_BUTTON_PAUSE);
			}
			break;

			//Type Random (play)
			case CASE_TYPE_SHUFFLE: {
				if (!READY.get()) {
					break;
				}
				new FastShufflePlayback(context).execute();
			}
			break;

			//by next button...(in detail or noti) (must ActivityList isn't empty)
			//by auto-next(mediaPlayer OnCompletionListener) of next-play by user, at this time MainActivity is present
			//by MusicDetailFragment preview imageButton (view history song list)
			case RECEIVE_TYPE_COMMON: {

				//检测是否指定下一首播放
				if (Data.sNextWillPlayItem != null) {
					new DoesHasNextPlay().execute();
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

				resetMusic();
				setDataSource(Data.sPlayOrderList.get(targetIndex));
				prepare();
				playMusic();

				setFlags(targetIndex);

				//load data

				//slide 滑动切歌无需再次加载albumCover
				if (intent.getStringExtra(INTENT_ARGS) != null && intent.getStringExtra(INTENT_ARGS).contains(TYPE_SLIDE)) {
					final String musicName = Data.sPlayOrderList.get(targetIndex).getMusicName();
					final String albumName = Data.sPlayOrderList.get(targetIndex).getMusicAlbum();
					final Bitmap cover = Utils.Audio.getCoverBitmap(context, Data.sPlayOrderList.get(targetIndex).getAlbumId());

					MusicDetailFragment.mHandler.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_BUTTON_PLAY);

					//set music data
					final Message message = Message.obtain();
					final Bundle bundle = new Bundle();
					bundle.putString("name", musicName);
					bundle.putString("albumName", albumName);
					message.setData(bundle);
					message.obj = cover;
					MusicDetailFragment.mHandler.sendMessage(message);
					//防止seekBar跳动到Max
					MusicDetailFragment.setSeekBar(0);

				} else {
					setUIMusicData(context, targetIndex);
				}
				sureCar();


			}
			break;

			//by MusicListFragment item click
			case CASE_TYPE_ITEM_CLICK: {
				//set current data
				resetMusic();
				setDataSource(Data.sCurrentMusicItem);
				prepare();
				playMusic();

				sureCar();

				MusicDetailFragment.mHandler.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_BUTTON_PLAY);
				MusicDetailFragment.mHandler.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.INIT_SEEK_BAR);

				// FIXME: 2019/5/22 
				((MusicDetailFragment) ((MainActivity) Data.sActivities.get(0)).getFragment(BaseFragment.FragmentType.MUSIC_DETAIL_FRAGMENT)).setCurrentInfo
						(Data.sCurrentMusicItem.getMusicName(), Data.sCurrentMusicItem.getMusicAlbum(),
								Utils.Audio.getCoverBitmap(context, Data.sCurrentMusicItem.getAlbumId()));

				MainActivity.mHandler.sendEmptyMessage(MainActivity.NotLeakHandler.SET_SLIDE_TOUCH_ENABLE);

			}
			break;
			default:
		}

		///////////////////////////AFTER PLAYER SET/////////////////////////////////////////

		MusicDetailFragment.mHandler.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.RECYCLER_SCROLL);

		MainActivity.mHandler.sendEmptyMessage(MainActivity.NotLeakHandler.SET_SLIDE_TOUCH_ENABLE);

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

		@Override
		protected Integer doInBackground(Void... voids) {

			if (Data.sNextWillPlayItem != null) {
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

			if (!Data.sActivities.isEmpty()) {

				final MusicDetailFragment musicDetailFragment = ((MusicDetailFragment) ((MainActivity) Data.sActivities.get(0)).getFragment(BaseFragment.FragmentType.MUSIC_DETAIL_FRAGMENT));

				final Bitmap cover = Utils.Audio.getCoverBitmap(musicDetailFragment.getContext(), Data.sNextWillPlayItem.getAlbumId());

				Data.setCurrentCover(cover);

				sureCar();

				MusicDetailFragment.mHandler.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.SET_BUTTON_PLAY);
				musicDetailFragment.setCurrentInfo(Data.sNextWillPlayItem.getMusicName(), Data.sNextWillPlayItem.getMusicAlbum(), cover);

				MusicDetailFragment.setSeekBar(0);         //防止seekBar跳动到Max
			}

			Data.sNextWillPlayItem = null;
		}
	}

	/**
	 * play_type: random (by nextButton or auto-next)
	 * just make a random Index (data by {@link Data#sPlayOrderList})
	 */
	public static class FastShufflePlayback extends AsyncTask<String, Void, Integer> {

		private WeakReference<Context> mContext;

		FastShufflePlayback(final Context context) {
			mContext = new WeakReference<>(context);
		}

		@Override
		protected Integer doInBackground(String... strings) {
			if (Data.sPlayOrderList.isEmpty()) {
				return -1;
			}

			READY.set(false);

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

			return index;
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (result < 0) {
				return;
			}

			setUIMusicData(mContext.get(), result);
			sureCar();

			READY.set(true);
		}

		@Override
		protected void onCancelled() {
			READY.set(true);
		}
	}

}
