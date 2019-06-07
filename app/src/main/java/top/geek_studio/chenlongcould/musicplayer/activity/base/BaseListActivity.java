package top.geek_studio.chenlongcould.musicplayer.activity.base;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.Nullable;
import top.geek_studio.chenlongcould.musicplayer.MessageWorker;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;

/**
 * 用于展示列表数据的Activity
 *
 * @author chenlongcould
 */
@SuppressLint("Registered")
public abstract class BaseListActivity extends BaseCompatActivity implements MessageWorker {

	private static final String TAG = "BaseListActivity";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/**
	 * 用于对列表的删除操作
	 * 请务必覆盖本方法
	 * todo 使用抽象关键字
	 */
	public boolean removeItem(MusicItem item) {
		return false;
	}
}
