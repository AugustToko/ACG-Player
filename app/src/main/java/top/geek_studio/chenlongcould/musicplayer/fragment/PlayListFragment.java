package top.geek_studio.chenlongcould.musicplayer.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
import top.geek_studio.chenlongcould.musicplayer.database.DataModel;
import top.geek_studio.chenlongcould.musicplayer.databinding.FragmentPlaylistBinding;
import top.geek_studio.chenlongcould.musicplayer.model.Item;
import top.geek_studio.chenlongcould.musicplayer.model.PlayListItem;

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

	private DataModel dataModel;

	/**
	 * add playlist item
	 *
	 * @deprecated use {@link #addItem(Item)}
	 */
	@Deprecated
	public static void sendAddPlayList(@Nullable PlayListItem playListItem) {
		Message message = Message.obtain();
		message.what = PlayListFragment.NotLeakHandler.ADD_TO_PLAYLIST;
		message.obj = playListItem;
		PlayListFragment.mHandler.sendMessage(message);
	}

	/**
	 * add playlist item
	 *
	 * @deprecated use {@link #removeItem(Item)}
	 */
	@Deprecated
	public static void sendRemovePlayList(@Nullable PlayListItem playListItem) {
		Message message = Message.obtain();
		message.what = NotLeakHandler.REMOVE_TO_PLAYLIST;
		message.obj = playListItem;
		PlayListFragment.mHandler.sendMessage(message);
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

	@Override
	public void onDestroy() {
		super.onDestroy();
		mPlayListBinding.unbind();
		mPlayListItemList.clear();
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

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		Log.d(TAG, "onSaveInstanceState: ");
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList("data", (ArrayList<? extends Parcelable>) mPlayListItemList);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		dataModel = ViewModelProviders.of(getActivity()).get(DataModel.class);
		initData();
	}

	/**
	 * load data
	 */
	private void initData() {
		if (ContextCompat.checkSelfPermission(mMainActivity,
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(mMainActivity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, Values.REQUEST_WRITE_EXTERNAL_STORAGE);
		} else {
			final Disposable disposable = Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
				mPlayListItemList.clear();
				final Cursor cursor = mMainActivity.getContentResolver()
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
//					DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffCallback(mPlayListItemList
//							, mPlayListAdapter.getPlayListItems()));
//					mPlayListAdapter.setPlayListItems(mPlayListItemList);
					emitter.onNext(true);
				}

			}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(result -> {
				if (result) {
					mPlayListAdapter.notifyDataSetChanged();
				}
			});
			dataModel.mDisposables.add(disposable);
		}
	}

	@Override
	public void reloadData() {
		super.reloadData();
		mHandler.sendEmptyMessage(NotLeakHandler.RE_LOAD_PLAY_LIST);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mPlayListItemList.addAll(Objects.requireNonNull(savedInstanceState.getParcelableArrayList("data")));
			Log.d(TAG, "onCreateView: " + mPlayListItemList.size());
		}
		mPlayListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_playlist, container, false);
		mPlayListBinding.getRoot().setPadding(0, 350, 0, 0);
		mPlayListBinding.addRecent.setOnClickListener(v -> {
			Intent intent = new Intent(mMainActivity, ListViewActivity.class);
			intent.putExtra(ListViewActivity.IntentTag.INTENT_START_BY, ListViewActivity.ListType.ACTION_ADD_RECENT);
			startActivity(intent);
		});

		mPlayListBinding.history.setOnClickListener(v -> {
			Intent intent = new Intent(mMainActivity, ListViewActivity.class);
			intent.putExtra(ListViewActivity.IntentTag.INTENT_START_BY, ListViewActivity.ListType.ACTION_HISTORY);
			startActivity(intent);
		});

//		mPlayListBinding.trashCan.setOnClickListener(v -> {
//			Intent intent = new Intent(mMainActivity, ListViewActivity.class);
//			intent.putExtra(ListViewActivity.IntentTag.INTENT_START_BY, ListViewActivity.ListType.ACTION_TRASH_CAN);
//			startActivity(intent);
//		});

		mPlayListBinding.recyclerView.setLayoutManager(new MyLM(mMainActivity));
		mPlayListBinding.recyclerView.setHasFixedSize(true);
		mPlayListBinding.recyclerView.addItemDecoration(Data.getItemDecoration(mMainActivity));

		mPlayListAdapter = new PlayListAdapter(this, mPlayListItemList);
		mPlayListBinding.recyclerView.setAdapter(mPlayListAdapter);

		return mPlayListBinding.getRoot();
	}

	@Override
	public boolean removeItem(@Nullable Item item) {
		if (!(item instanceof PlayListItem) || item.hashCode() < 0) return false;
		Log.d(TAG, "remove: " + item.hashCode());

		final PlayListItem playListItem = (PlayListItem) item;

		Message message = Message.obtain();
		message.what = PlayListFragment.NotLeakHandler.REMOVE_TO_PLAYLIST;
		message.obj = playListItem;
		PlayListFragment.mHandler.sendMessage(message);
		return true;
	}

	private static class DiffCallback extends DiffUtil.Callback {

		private List<PlayListItem> oldList, newList;

		public DiffCallback(List<PlayListItem> newList, List<PlayListItem> oldList) {
			this.newList = newList;
			this.oldList = oldList;
		}

		@Override
		public int getOldListSize() {
			return oldList.size();
		}

		@Override
		public int getNewListSize() {
			return newList.size();
		}

		@Override
		public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
			return oldList.get(oldItemPosition).hashCode() == newList.get(newItemPosition).hashCode();
		}

		@Override
		public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
			return oldList.get(oldItemPosition).hashCode() == newList.get(newItemPosition).hashCode();
		}
	}

	public FragmentPlaylistBinding getPlayListBinding() {
		return mPlayListBinding;
	}

	@Override
	public boolean addItem(@Nullable Item item) {
		if (!(item instanceof PlayListItem) || item.hashCode() < 0) return false;
		Log.d(TAG, "addItem: " + item.hashCode());

		Message message = Message.obtain();
		message.what = PlayListFragment.NotLeakHandler.ADD_TO_PLAYLIST;
		message.obj = item;
		PlayListFragment.mHandler.sendMessage(message);
		return true;
	}

	public static class NotLeakHandler extends Handler {

		private WeakReference<PlayListFragment> mWeakReference;

		public static final int RE_LOAD_PLAY_LIST = 80001;
		public static final int ADD_TO_PLAYLIST = 80002;
		public static final int REMOVE_TO_PLAYLIST = 80003;

		NotLeakHandler(PlayListFragment fragment) {
			mWeakReference = new WeakReference<>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case RE_LOAD_PLAY_LIST: {
					mWeakReference.get().initData();
				}
				break;

				case ADD_TO_PLAYLIST: {
					PlayListItem item = (PlayListItem) msg.obj;
					mWeakReference.get().mPlayListItemList.add(item);
					mWeakReference.get().mPlayListAdapter.notifyDataSetChanged();
//					final Disposable disposable = Observable.create((ObservableOnSubscribe<DiffUtil.DiffResult>) emitter -> {
//						final PlayListItem playListItem = (PlayListItem) msg.obj;
//						mWeakReference.get().mPlayListItemList.add(playListItem);
//
//						List<PlayListItem> old = mWeakReference.get().getPlayListAdapter().getPlayListItems();
//						final DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffCallback(
//								mWeakReference.get().mPlayListItemList, old), true);
//
//						mWeakReference.get().mPlayListAdapter.setPlayListItems(mWeakReference.get().mPlayListItemList);
//						emitter.onNext(result);
//					}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(result
//							-> result.dispatchUpdatesTo(mWeakReference.get().mPlayListAdapter));
//
//					Data.mDisposables.add(disposable);
				}
				break;
				case REMOVE_TO_PLAYLIST: {
					PlayListItem item = (PlayListItem) msg.obj;
					mWeakReference.get().mPlayListItemList.remove(item);
					mWeakReference.get().mPlayListAdapter.notifyDataSetChanged();
//					final Disposable disposable = Observable.create((ObservableOnSubscribe<DiffUtil.DiffResult>) emitter -> {
//						final PlayListItem playListItem = (PlayListItem) msg.obj;
//						mWeakReference.get().mPlayListItemList.remove(playListItem);
//
//						List<PlayListItem> old = mWeakReference.get().getPlayListAdapter().getPlayListItems();
//						final DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffCallback(
//								mWeakReference.get().mPlayListItemList, old), true);
//
//						mWeakReference.get().mPlayListAdapter.setPlayListItems(mWeakReference.get().mPlayListItemList);
//						emitter.onNext(result);
//					}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(result
//							-> result.dispatchUpdatesTo(mWeakReference.get().mPlayListAdapter));
//
//					Data.mDisposables.add(disposable);
				}
				break;
				default:
			}
		}
	}

	private class MyLM extends LinearLayoutManager {

		public MyLM(Context context) {
			super(context);
		}

		@Override
		public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
			try {
				super.onLayoutChildren(recycler, state);
			} catch (Exception e) {
				Toast.makeText(mMainActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	}

}
