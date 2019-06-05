package top.geek_studio.chenlongcould.musicplayer.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.activity.ListViewActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.adapter.PlayListAdapter;
import top.geek_studio.chenlongcould.musicplayer.databinding.FragmentPlaylistBinding;
import top.geek_studio.chenlongcould.musicplayer.model.PlayListItem;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * @author chenlongcould
 */
public final class PlayListFragment extends BaseFragment {

	public static final String TAG = "PlayListFragment";

	private LocalBroadcastManager mBroadcastManager;
	private FragmentPlaylistBinding mPlayListBinding;
	private MainActivity mMainActivity;
	private static Handler mHandler;
	private PlayListAdapter mPlayListAdapter;
	private BroadcastReceiver mRefreshReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mPlayListAdapter.notifyDataSetChanged();
		}
	};

	/**
	 * 实例化 {@link PlayListFragment}
	 */
	public static PlayListFragment newInstance() {
		return new PlayListFragment();
	}

	/**
	 * receive broadcast
	 *
	 * @see PlayListFragment#mBroadcastManager
	 * @see PlayListFragment.ItemChange#ACTION_REFRESH_LIST
	 */
	private void receiveItemChange() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ItemChange.ACTION_REFRESH_LIST);
		mBroadcastManager.registerReceiver(mRefreshReceiver, intentFilter);
	}

	@Override
	public FragmentType getFragmentType() {
		return FragmentType.PLAY_LIST_FRAGMENT;
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		mMainActivity = (MainActivity) getActivity();
		mHandler = new NotLeakHandler(this);

		mBroadcastManager = LocalBroadcastManager.getInstance(context);
		receiveItemChange();
	}

	/**
	 * load data
	 */
	private void initData() {
		final Disposable disposable = Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
			Data.sPlayListItems.clear();
			Cursor cursor = mMainActivity.getContentResolver()
					.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, null, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				do {
					final int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists._ID));
//					if (item != null && item.getId() == id) {
//						//匹配到喜爱列表 跳过
//						continue;
//					}
					final String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME));
					final String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.DATA));
					final long addTime = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.DATE_ADDED));

					// TODO: 2018/12/10 M3U FILE
					final File file = new File(filePath + ".m3u");

					Data.sPlayListItems.add(new PlayListItem(id, name, filePath, addTime));
				} while (cursor.moveToNext());
				cursor.close();
			}

			//done
			emitter.onNext(0);
		}).subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(integer -> {
					if (integer == 0) {

						//load recyclerView
						mPlayListAdapter = new PlayListAdapter(mMainActivity, Data.sPlayListItems);
						mPlayListBinding.recyclerView.setAdapter(mPlayListAdapter);
					}
				});
		Data.sDisposables.add(disposable);
	}

	public static Handler getHandler() {
		return mHandler;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mBroadcastManager.unregisterReceiver(mRefreshReceiver);
	}

	public PlayListAdapter getPlayListAdapter() {
		return mPlayListAdapter;
	}

	public static void reloadDataByHandler() {
		mHandler.sendEmptyMessage(NotLeakHandler.RE_LOAD_PLAY_LIST);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mPlayListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_playlist, container, false);

		mPlayListBinding.addRecent.setOnClickListener(v -> {
			Intent intent = new Intent(mMainActivity, ListViewActivity.class);
			intent.putExtra(ListViewActivity.IntentTag.INTENT_START_BY, ListViewActivity.FragmentType.ACTION_ADD_RECENT);
			startActivity(intent);
		});

//		mPlayListBinding.favourite.setOnClickListener(v -> {
//			Intent intent = new Intent(mMainActivity, ListViewActivity.class);
//			intent.putExtra(ListViewActivity.IntentTag.INTENT_START_BY, ListViewActivity.FragmentType.ACTION_FAVOURITE);
//			startActivity(intent);
//		});

		mPlayListBinding.history.setOnClickListener(v -> {
			Intent intent = new Intent(mMainActivity, ListViewActivity.class);
			intent.putExtra(ListViewActivity.IntentTag.INTENT_START_BY, ListViewActivity.FragmentType.ACTION_HISTORY);
			startActivity(intent);
		});

		mPlayListBinding.trashCan.setOnClickListener(v -> {
			Intent intent = new Intent(mMainActivity, ListViewActivity.class);
			intent.putExtra(ListViewActivity.IntentTag.INTENT_START_BY, ListViewActivity.FragmentType.ACTION_TRASH_CAN);
			startActivity(intent);
		});

		mPlayListBinding.recentName.setTextColor(ContextCompat.getColor(mMainActivity, R.color.title_color));
//		mPlayListBinding.favouriteName.setTextColor(ContextCompat.getColor(mMainActivity, R.color.title_color));
		mPlayListBinding.historyName.setTextColor(ContextCompat.getColor(mMainActivity, R.color.title_color));

		mPlayListBinding.recyclerView.setLayoutManager(new LinearLayoutManager(mMainActivity));
		mPlayListBinding.recyclerView.setHasFixedSize(true);
		mPlayListBinding.recyclerView.addItemDecoration(Data.getItemDecoration(mMainActivity));

		initData();
		return mPlayListBinding.getRoot();
	}

	@Override
	public void reloadData() {
		super.reloadData();
		mHandler.sendEmptyMessage(NotLeakHandler.RE_LOAD_PLAY_LIST);
	}

	/**
	 * for intent use
	 */
	public interface ItemChange {
		/**
		 * for broadcasts
		 */
		String ACTION_REFRESH_LIST = "ACTION_REFRESH_LIST";
	}

	private static class NotLeakHandler extends Handler {
		
		private WeakReference<PlayListFragment> mWeakReference;

		public static final int RE_LOAD_PLAY_LIST = 80001;

		NotLeakHandler(PlayListFragment fragment) {
			mWeakReference = new WeakReference<>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			//noinspection SwitchStatementWithTooFewBranches
			switch (msg.what) {
				case RE_LOAD_PLAY_LIST: {
					mWeakReference.get().initData();
					mWeakReference.get().getPlayListAdapter().notifyDataSetChanged();
				}
				break;
				default:
			}
		}
	}
}
