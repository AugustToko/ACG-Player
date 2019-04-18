/*
 * ************************************************************
 * 文件：ReceiverOnMusicPlay.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月27日 13:11:38
 * 上次修改时间：2019年01月27日 13:08:44
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.fragment.MusicDetailFragment;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

/**
 * @author chenlongcould
 */
public final class ReceiverOnMusicPlay extends BroadcastReceiver {

	public static final String TAG = "ReceiverOnMusicPlay";
	
	@Deprecated
	public static final int CASE_TYPE_SHUFFLE = 90;
	@Deprecated
	public static final int CASE_TYPE_ITEM_CLICK = 15;
	@Deprecated
	public static final int CASE_TYPE_NOTIFICATION_RESUME = 2;
	@Deprecated
	public static final int RECEIVE_TYPE_COMMON = 6;
	
	public static void setDataSource(String path) {
		Data.S_HISTORY_PLAY.add(Data.sCurrentMusicItem);
		try {
			Data.sMusicBinder.setDataSource(path);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

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
	 * setSeekBar
	 */
	private static void reSetSeekBar() {
		final MusicDetailFragment musicDetailFragment = ((MainActivity) Data.sActivities.get(0)).getMusicDetailFragment();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			musicDetailFragment.getSeekBar().setProgress(0, true);
		} else {
			musicDetailFragment.getSeekBar().setProgress(0);
		}
	}

	/**
	 * setButtonPlay
	 * setSeekBarColor
	 * setSeekBarPosition
	 * setSlideBar
	 *
	 * @param musicDetailFragment fragment
	 * @param targetIndex         index
	 */
	private static void uiSet(final MusicDetailFragment musicDetailFragment, final int targetIndex) {
		final String musicName = Data.sPlayOrderList.get(targetIndex).getMusicName();
		final String albumName = Data.sPlayOrderList.get(targetIndex).getMusicAlbum();
		final Bitmap cover = Utils.Audio.getCoverBitmap(musicDetailFragment.getActivity(), Data.sPlayOrderList.get(targetIndex).getAlbumId());

		Utils.Ui.setPlayButtonNowPlaying();
		musicDetailFragment.setCurrentInfo(musicName, albumName, cover);

		reSetSeekBar();         //防止seekBar跳动到Max
		musicDetailFragment.getHandler().sendEmptyMessage(MusicDetailFragment.HandlerWhat.INIT_SEEK_BAR);
		musicDetailFragment.getHandler().sendEmptyMessage(MusicDetailFragment.HandlerWhat.RECYCLER_SCROLL);
		((MainActivity) Data.sActivities.get(0)).getMainBinding().slidingLayout.setTouchEnabled(true);
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

	public static void playMusic() {
		try {
			Data.sMusicBinder.setCurrentMusicData(Data.sCurrentMusicItem);
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

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive: done");

		final int type = intent.getIntExtra(INTENT_PLAY_TYPE, 0);

		final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);

		///////////////////////////BEFORE PLAYER SET/////////////////////////////////////////

		switch (type) {
			//clicked by notif, just resume play
			case CASE_TYPE_NOTIFICATION_RESUME: {
				Utils.Ui.setPlayButtonNowPlaying();
				playMusic();
			}
			break;

			//pause music
			case -1: {
				pauseMusic();
				Intent pauseIntent = new Intent();
				pauseIntent.setAction(MusicDetailFragment.BroadCastAction.ACTION_CHANGE_BUTTON_PAUSE);
				LocalBroadcastManager.getInstance(context).sendBroadcast(pauseIntent);
			}
			break;

			//Type Random (play)
			case CASE_TYPE_SHUFFLE: {
				if (!READY.get()) {
					break;
				}
				new FastShufflePlayback().execute();
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

				//检测前后播放
				int targetIndex = 0;
				if (intent.getStringExtra(INTENT_ARGS) != null) {
					if (intent.getStringExtra(INTENT_ARGS).contains(TYPE_NEXT)) {
						targetIndex = Values.CurrentData.CURRENT_MUSIC_INDEX + 1;
						//超出范围自动跳转0
						if (targetIndex > Data.sPlayOrderList.size() - 1) {
							targetIndex = 0;
						}
					} else if (intent.getStringExtra(INTENT_ARGS).contains(TYPE_PREVIOUS)) {
						targetIndex = Values.CurrentData.CURRENT_MUSIC_INDEX - 1;
						if (targetIndex < 0) {
							//超出范围超转最后
							targetIndex = Data.sPlayOrderList.size() - 1;
						}
					}
				}
				Values.CurrentData.CURRENT_MUSIC_INDEX = targetIndex;

				Data.sCurrentMusicItem = Data.sPlayOrderList.get(targetIndex);

				playMusicInOne(Data.sPlayOrderList.get(targetIndex).getMusicPath());

				setFlags(targetIndex);

				//load data
				if (!Data.sActivities.isEmpty()) {
					final MusicDetailFragment musicDetailFragment = ((MainActivity) Data.sActivities.get(0)).getMusicDetailFragment();

					//slide 滑动切歌无需再次加载albumCover
					if (intent.getStringExtra(INTENT_ARGS) != null && intent.getStringExtra(INTENT_ARGS).contains(TYPE_SLIDE)) {
						final String musicName = Data.sPlayOrderList.get(targetIndex).getMusicName();
						final String albumName = Data.sPlayOrderList.get(targetIndex).getMusicAlbum();
						final Bitmap cover = Utils.Audio.getCoverBitmap(context, Data.sPlayOrderList.get(targetIndex).getAlbumId());

						Utils.Ui.setPlayButtonNowPlaying();
						musicDetailFragment.setCurrentInfoWithoutMainImage(musicName, albumName, cover);
						reSetSeekBar();

						musicDetailFragment.getHandler().sendEmptyMessage(MusicDetailFragment.HandlerWhat.INIT_SEEK_BAR);
						musicDetailFragment.getHandler().sendEmptyMessage(MusicDetailFragment.HandlerWhat.RECYCLER_SCROLL);
					} else {
						uiSet(musicDetailFragment, targetIndex);
					}
					sureCar();
				}


			}
			break;

			//by MusicListFragment item click
			case CASE_TYPE_ITEM_CLICK: {
				//set current data
				Data.sCurrentMusicItem = Data.sMusicItems.get(Integer.parseInt(intent.getStringExtra(INTENT_ARGS)));

				playMusicInOne(Data.sCurrentMusicItem.getMusicPath());

				sureCar();

				Utils.Ui.setPlayButtonNowPlaying();

				//update seek
				final Intent initSeekBarIntent = new Intent();
				initSeekBarIntent.setAction(MusicDetailFragment.BroadCastAction.ACTION_INIT_SEEK_BAR);

				final Intent updateInfo = new Intent();
				updateInfo.setAction(MusicDetailFragment.BroadCastAction.ACTION_UPDATE_CURRENT_INFO);

				manager.sendBroadcast(updateInfo);
				manager.sendBroadcast(initSeekBarIntent);

				MainActivity.mHandler.sendEmptyMessage(MainActivity.SET_SLIDE_TOUCH_ENABLE);

			}
			break;
			default:
		}

		///////////////////////////AFTER PLAYER SET/////////////////////////////////////////

		final Intent pauseIntent = new Intent();
		pauseIntent.setAction(MusicDetailFragment.BroadCastAction.ACTION_SCROLL);
		LocalBroadcastManager.getInstance(context).sendBroadcast(pauseIntent);

		if (!Data.sActivities.isEmpty()) {
			((MainActivity) Data.sActivities.get(0)).getMainBinding().slidingLayout.setTouchEnabled(true);
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
	
	public static void stopMusic() {
		try {
			Data.sMusicBinder.stopMusic();
		} catch (RemoteException e) {
			e.printStackTrace();
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

	private void playMusicInOne(@NonNull String path) {
		resetMusic();
		setDataSource(path);
		prepare();
		playMusic();
	}

	/**
	 * 当下一首歌曲存在(被手动指定时), auto-next-play and next-play will call this method
	 */
	public static class DoesHasNextPlay extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... voids) {

			if (Data.sNextWillPlayItem != null) {
				resetMusic();
				ReceiverOnMusicPlay.setDataSource(Data.sNextWillPlayItem.getMusicPath());
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

				final MusicDetailFragment musicDetailFragment = ((MainActivity) Data.sActivities.get(0)).getMusicDetailFragment();

				final Bitmap cover = Utils.Audio.getCoverBitmap(musicDetailFragment.getContext(), Data.sNextWillPlayItem.getAlbumId());
				sureCar();

				Utils.Ui.setPlayButtonNowPlaying();
				musicDetailFragment.setCurrentInfo(Data.sNextWillPlayItem.getMusicName(), Data.sNextWillPlayItem.getMusicAlbum(), cover);

				reSetSeekBar();         //防止seekBar跳动到Max
				musicDetailFragment.getHandler().sendEmptyMessage(MusicDetailFragment.HandlerWhat.INIT_SEEK_BAR);
				musicDetailFragment.getHandler().sendEmptyMessage(MusicDetailFragment.HandlerWhat.RECYCLER_SCROLL);
			}

			Data.sNextWillPlayItem = null;
		}
	}

	/**
	 * play_type: random (by nextButton or auto-next)
	 * just make a random Index (data by {@link Data#sPlayOrderList})
	 */
	public static class FastShufflePlayback extends AsyncTask<String, Void, Integer> {

		String path;

		int index;

		@Override
		protected Integer doInBackground(String... strings) {
			if (Data.sPlayOrderList.isEmpty()) {
				return -1;
			}

			READY.set(false);
			resetMusic();

			//get data
			final Random random = new Random();
			final int index = random.nextInt(Data.sPlayOrderList.size());
			this.index = index;
			Values.CurrentData.CURRENT_MUSIC_INDEX = index;

			Data.sCurrentMusicItem = Data.sPlayOrderList.get(index);

			path = Data.sPlayOrderList.get(index).getMusicPath();

			setFlags(index);

			setDataSource(path);
			prepare();
			playMusic();

			return 0;
		}

		@Override
		protected void onPostExecute(Integer status) {
			if (status != 0) {
				return;
			}

			if (Data.sActivities.size() >= 1) {
				uiSet(((MainActivity) Data.sActivities.get(0)).getMusicDetailFragment(), index);
				sureCar();
			}

			READY.set(true);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			READY.set(true);
		}
	}

}
