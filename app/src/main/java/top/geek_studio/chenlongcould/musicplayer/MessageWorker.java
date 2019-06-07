package top.geek_studio.chenlongcould.musicplayer;

import android.os.Message;

/**
 * Can send massage by SomeClass.SendMessage()
 *
 * @author : chenlongcould
 * @date : 2019/05/26/18
 */
public interface MessageWorker {

	int RELOAD = 9920;

	/**
	 * send empty message
	 *
	 * @see android.os.Handler#sendEmptyMessage(int)
	 */
	void sendEmptyMessage(final int what);

	/**
	 * send message
	 *
	 * @see android.os.Handler#sendMessage(Message)
	 */
	void sendMessage(final Message message);
}
