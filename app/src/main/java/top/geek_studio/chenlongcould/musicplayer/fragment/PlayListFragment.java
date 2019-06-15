package top.geek_studio.chenlongcould.musicplayer.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.ListViewActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.main.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.adapter.PlayListAdapter;
import top.geek_studio.chenlongcould.musicplayer.databinding.FragmentPlaylistBinding;
import top.geek_studio.chenlongcould.musicplayer.model.Item;
import top.geek_studio.chenlongcould.musicplayer.model.PlayListItem;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenlongcould
 */
public final class PlayListFragment extends BaseListFragment {

	public static final String TAG = "PlayListFragment";

	private FragmentPlaylistBinding mPlayListBinding;
	private MainActivity mMainActivity;

	public static NotLeakHandler mHandler;

	private PlayListAdapter mPlayListAdapter;

	public List<PlayListItem> mPlayListItemList = new ArrayList<>();

	/**
	 * add playlist item
	 */
	public static void sendAddPlayList(@Nullable PlayListItem playListItem) {
		Message message = Message.obtain();
		message.what = PlayListFragment.NotLeakHandler.ADD_TO_PLAYLIST;
		message.obj = playListItem;
		PlayListFragment.mHandler.sendEmptyMessage(PlayListFragment.NotLeakHandler.ADD_TO_PLAYLIST);
	}

	/**
	 * 实例化 {@link PlayListFragment}
	 */
	public static PlayListFragment newInstance() {
		return new PlayListFragment();
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
	}

	/**
	 * reload
	 * by create list or rename list
	 */
	public static void reloadDataByHandler() {
		if (mHandler != null) mHandler.sendEmptyMessage(NotLeakHandler.RE_LOAD_PLAY_LIST);
	}

	public PlayListAdapter getPlayListAdapter() {
		return mPlayListAdapter;
	}

	/**
	 * load data
	 */
	private void initData() {
		if (ContextCompat.checkSelfPermission(mMainActivity,
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(mMainActivity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, Values.REQUEST_WRITE_EXTERNAL_STORAGE);
		} else {
			final Disposable disposable = Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
				mPlayListItemList.clear();
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

						mPlayListItemList.add(new PlayListItem(id, name, filePath, addTime));
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
							mPlayListAdapter = new PlayListAdapter(this, mPlayListItemList);
							mPlayListBinding.recyclerView.setAdapter(mPlayListAdapter);
						}
					});
			Data.sDisposables.add(disposable);
		}

	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mPlayListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_playlist, container, false);

		mPlayListBinding.addRecent.setOnClickListener(v -> {
			Intent intent = new Intent(mMainActivity, ListViewActivity.class);
			intent.putExtra(ListViewActivity.IntentTag.INTENT_START_BY, ListViewActivity.ListType.ACTION_ADD_RECENT);
			startActivity(intent);
		});

//		mPlayListBinding.favourite.setOnClickListener(v -> {
//			Intent intent = new Intent(mMainActivity, ListViewActivity.class);
//			intent.putExtra(ListViewActivity.IntentTag.INTENT_START_BY, ListViewActivity.ListType.ACTION_FAVOURITE);
//			startActivity(intent);
//		});

		mPlayListBinding.history.setOnClickListener(v -> {
			Intent intent = new Intent(mMainActivity, ListViewActivity.class);
			intent.putExtra(ListViewActivity.IntentTag.INTENT_START_BY, ListViewActivity.ListType.ACTION_HISTORY);
			startActivity(intent);
		});

		mPlayListBinding.trashCan.setOnClickListener(v -> {
			Intent intent = new Intent(mMainActivity, ListViewActivity.class);
			intent.putExtra(ListViewActivity.IntentTag.INTENT_START_BY, ListViewActivity.ListType.ACTION_TRASH_CAN);
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

	@Override
	public boolean removeItem(@Nullable Item item) {
		if (!(item instanceof PlayListItem)) return false;

		final PlayListItem playListItem = (PlayListItem) item;

		if (mPlayListAdapter != null && playListItem.getId() != -1) {
			int pos = mPlayListItemList.indexOf(playListItem);
			boolean result = mPlayListItemList.remove(playListItem);
			mPlayListAdapter.notifyItemRemoved(pos);
			return result;
		}
		return false;
	}

	@Override
	public boolean addItem(@Nullable Item item) {
		if (!(item instanceof PlayListItem)) return false;

		final PlayListItem playListItem = (PlayListItem) item;

		if (mPlayListAdapter != null && playListItem.getId() != -1) {
			boolean result = mPlayListItemList.add(playListItem);
			mPlayListAdapter.notifyItemInserted(mPlayListItemList.size() - 1);
			return result;
		}
		return false;
	}

	public static class NotLeakHandler extends Handler {
		
		private WeakReference<PlayListFragment> mWeakReference;

		public static final int RE_LOAD_PLAY_LIST = 80001;
		public static final int ADD_TO_PLAYLIST = 80002;

		NotLeakHandler(PlayListFragment fragment) {
			mWeakReference = new WeakReference<>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case RE_LOAD_PLAY_LIST: {
					mWeakReference.get().initData();
					mWeakReference.get().getPlayListAdapter().notifyDataSetChanged();
				}
				break;

				case ADD_TO_PLAYLIST: {
					mWeakReference.get().addItem((Item) msg.obj);
				}
				break;
				default:
			}
		}
	}

}
