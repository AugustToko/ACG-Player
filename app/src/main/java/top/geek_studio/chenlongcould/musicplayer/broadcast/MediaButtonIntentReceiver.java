/*
 * Copyright (C) 2007 The Android Open Source Project Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

// Modified for Phonograph by Karim Abou Zeid (kabouzeid).
// Modified for ACG Player by Gikode.

package top.geek_studio.chenlongcould.musicplayer.broadcast;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.KeyEvent;

import top.geek_studio.chenlongcould.musicplayer.MusicService;
import top.geek_studio.chenlongcould.musicplayer.activity.main.MainActivity;

/**
 * Used to control headset playback.
 * Single press: intentPause/resume
 * Double press: intentNext track
 * Triple press: intentPrevious track
 */
final public class MediaButtonIntentReceiver extends BroadcastReceiver {
	public static final String TAG = MediaButtonIntentReceiver.class.getSimpleName();

	private static final int MSG_HEADSET_DOUBLE_CLICK_TIMEOUT = 2;

	private static final int DOUBLE_CLICK = 400;

	private static WakeLock mWakeLock = null;
	private static int mClickCounter = 0;
	private static long mLastClickTime = 0;

	/**
	 * 媒体按钮次数检测
	 */
	@SuppressLint("HandlerLeak") // false alarm, handler is already static
	private static Handler mHandler = new Handler() {

		@Override
		public void handleMessage(final Message msg) {
			//noinspection SwitchStatementWithTooFewBranches
			switch (msg.what) {
				case MSG_HEADSET_DOUBLE_CLICK_TIMEOUT:
					final int clickCount = msg.arg1;

					Log.v(TAG, "Handling headset click, count = " + clickCount);
					final Intent intent = new Intent();

					switch (clickCount) {
						case 1:
							intent.setAction(MusicService.ServiceActions.ACTION_TOGGLE_PLAY_PAUSE);
							break;
						case 2:
							intent.setAction(MusicService.ServiceActions.ACTION_PN);
							intent.putExtra("pnType", MusicService.ServiceActions.ACTION_PN_NEXT);
							break;
						case 3:
							intent.setAction(MusicService.ServiceActions.ACTION_PN);
							intent.putExtra("pnType", MusicService.ServiceActions.ACTION_PN_PREVIOUS);
							break;

						case 4: {
							intent.setAction(MusicService.ServiceActions.ACTION_TOGGLE_FAVOURITE);
						}
						break;
						default:
							intent.setAction(null);
							break;
					}

					if (intent.getAction() != null) {
						final Context context = (Context) msg.obj;
						MainActivity.startService(context, intent);
					}
					break;
			}
			releaseWakeLockIfHandlerIdle();
		}
	};

	public static boolean handleIntent(final Context context, final Intent intent) {
		final String intentAction = intent.getAction();
		if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
			final KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			if (event == null) {
				return false;
			}

			final int keycode = event.getKeyCode();
			final int action = event.getAction();
			final long eventTime = event.getEventTime() != 0 ? event.getEventTime() : System.currentTimeMillis();
			// Fallback to system time if event time was not available.

			String command = null;
			String pnType = null;
			switch (keycode) {
				case KeyEvent.KEYCODE_MEDIA_STOP:
					command = MusicService.ServiceActions.ACTION_STOP;
					break;
				case KeyEvent.KEYCODE_HEADSETHOOK:
				case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
					command = MusicService.ServiceActions.ACTION_TOGGLE_PLAY_PAUSE;
					break;
				case KeyEvent.KEYCODE_MEDIA_NEXT:
					command = MusicService.ServiceActions.ACTION_PN;        //pn
					pnType = MusicService.ServiceActions.ACTION_PN_NEXT;
					break;
				case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
					command = MusicService.ServiceActions.ACTION_PN;        //pn
					pnType = MusicService.ServiceActions.ACTION_PN_PREVIOUS;
					break;
				case KeyEvent.KEYCODE_MEDIA_PAUSE:
					command = MusicService.ServiceActions.ACTION_PAUSE;
					break;
				case KeyEvent.KEYCODE_MEDIA_PLAY:
					command = MusicService.ServiceActions.ACTION_PLAY;
					break;
			}
			if (command != null) {
				if (action == KeyEvent.ACTION_DOWN) {
					if (event.getRepeatCount() == 0) {
						Log.d(TAG, "handleIntent: intoer3");
						// Only consider the first event in a sequence, not the repeat events,
						// so that we don't trigger in cases where the first event went to
						// a different app (e.g. when the user ends a phone call by
						// long pressing the headset button)

						// The service may or may not be running, but we need to send it
						// a command.
						if (keycode == KeyEvent.KEYCODE_HEADSETHOOK || keycode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
							if (eventTime - mLastClickTime >= DOUBLE_CLICK) {
								mClickCounter = 0;
							}

							mClickCounter++;
							Log.v(TAG, "Got headset click, count = " + mClickCounter);
							mHandler.removeMessages(MSG_HEADSET_DOUBLE_CLICK_TIMEOUT);

							Message msg = mHandler.obtainMessage(
									MSG_HEADSET_DOUBLE_CLICK_TIMEOUT, mClickCounter, 0, context);

							long delay = mClickCounter < 4 ? DOUBLE_CLICK : 0;

							if (mClickCounter >= 4) {
								mClickCounter = 0;
							}

							mLastClickTime = eventTime;
							acquireWakeLockAndSendMessage(context, msg, delay);
						} else {
							Intent i = new Intent(command);
							if (pnType != null) {
								i.putExtra("pnType", pnType);
							}
							MainActivity.startService(context, i);
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	private static void acquireWakeLockAndSendMessage(Context context, Message msg, long delay) {
		if (mWakeLock == null) {
			final Context appContext = context.getApplicationContext();
			final PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
			if (pm != null) {
				mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, context.getPackageName() + ":wakelockB");
				mWakeLock.setReferenceCounted(false);
			}
		}

		// 获取唤醒锁定和发送
		Log.v(TAG, "Acquiring wake lock and sending " + msg.what);
		// 确保在任何情况下我们都不会无限期地保持唤醒锁定
		mWakeLock.acquire(10000);
		mHandler.sendMessageDelayed(msg, delay);
	}

	private static void releaseWakeLockIfHandlerIdle() {
		if (mHandler.hasMessages(MSG_HEADSET_DOUBLE_CLICK_TIMEOUT)) {
			Log.v(TAG, "Handler still has messages pending, not releasing wake lock");
			return;
		}

		if (mWakeLock != null) {
			Log.v(TAG, "Releasing wake lock");
			mWakeLock.release();
			mWakeLock = null;
		}
	}

	@Override
	public void onReceive(final Context context, final Intent intent) {
		Log.v(TAG, "Received intent: " + intent);
		if (handleIntent(context, intent) && isOrderedBroadcast()) {
			abortBroadcast();
		}
	}

}
