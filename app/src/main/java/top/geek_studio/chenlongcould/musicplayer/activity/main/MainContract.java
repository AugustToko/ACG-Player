package top.geek_studio.chenlongcould.musicplayer.activity.main;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import top.geek_studio.chenlongcould.musicplayer.BasePresenter;
import top.geek_studio.chenlongcould.musicplayer.BaseView;
import top.geek_studio.chenlongcould.musicplayer.activity.base.BaseCompatActivity;
import top.geek_studio.chenlongcould.musicplayer.fragment.BaseFragment;

/**
 * Main
 *
 * @author : chenlongcould
 * @date : 2019/06/15/08
 */
public interface MainContract {
	interface View extends BaseView<Presenter> {
		void initFragmentData();

		void receivedIntentCheck(@NonNull final Intent intent);

		@NonNull
		Context getContext();

		void notifyAdapter(char fragmentId);

		@NonNull
		BaseFragment getFragment(char fragmentId);
	}

	interface Presenter extends BasePresenter {
		void checkUpdate(@NonNull final BaseCompatActivity activity);

		/**
		 * init permission, every Activity extends {@link BaseCompatActivity}
		 */
		void initPermission(@NonNull final MainActivity activity);

		void initData(@NonNull final MainActivity activity);

		/**
		 * 根据输入框中的值来过滤数据并更新RecyclerView
		 *
		 * @param key name
		 */
		void filterData(String key);
	}
}
