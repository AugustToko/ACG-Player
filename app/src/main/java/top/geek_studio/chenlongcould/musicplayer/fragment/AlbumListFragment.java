package top.geek_studio.chenlongcould.musicplayer.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.main.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter2AlbumList;
import top.geek_studio.chenlongcould.musicplayer.database.DataModel;
import top.geek_studio.chenlongcould.musicplayer.model.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.model.Item;
import top.geek_studio.chenlongcould.musicplayer.threadPool.CustomThreadPool;
import top.geek_studio.chenlongcould.musicplayer.utils.MusicUtil;

/**
 * @author chenlongcould
 */
public final class AlbumListFragment extends BaseListFragment {

	public static final String TAG = "AlbumListFragment";

	private FastScrollRecyclerView mRecyclerView;

	private MainActivity mMainActivity;

	private MyRecyclerAdapter2AlbumList mAdapter2AlbumListAdapter;

	private List<AlbumItem> albumItemList = new ArrayList<>();

	private List<AlbumItem> albumItemListBackup = new ArrayList<>();

	private View mView;

	private DataModel dataModel;

	public static AlbumListFragment newInstance() {
		return new AlbumListFragment();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		albumItemList.clear();
		albumItemListBackup.clear();
	}

	@Override
	public void reloadData() {
		Log.d(TAG, "reloadData: reload albumList");
		albumItemList.clear();
		mAdapter2AlbumListAdapter.notifyDataSetChanged();
		initAlbumData();
	}

	@Override
	public void onAttach(@NotNull Context context) {
		super.onAttach(context);
		mMainActivity = (MainActivity) getActivity();
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		dataModel = ViewModelProviders.of(getActivity()).get(DataModel.class);
		initAlbumData();
	}

	@Override
	public FragmentType getFragmentType() {
		return FragmentType.ALBUM_LIST_FRAGMENT;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_album_list, container, false);
		initRecyclerView();
		return mView;
	}

	private void initAlbumData() {
		if (ContextCompat.checkSelfPermission(mMainActivity,
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(mMainActivity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, Values.REQUEST_WRITE_EXTERNAL_STORAGE);
		} else {
			Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
				if (albumItemList.size() == 0) {
					final Cursor cursor = mMainActivity.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, null, null, null);
					if (cursor != null) {
						cursor.moveToFirst();
						do {
							String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM));
							String albumId = cursor.getString(cursor.getColumnIndexOrThrow("_id"));

							String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST));
							final AlbumItem albumItem = new AlbumItem(albumName, Integer.parseInt(albumId), artist);
							albumItemList.add(albumItem);
							albumItemListBackup.add(albumItem);
							CustomThreadPool.post(() -> MusicUtil.findArtworkWithId(mMainActivity, albumItem));
						} while (cursor.moveToNext());

						cursor.close();
					}   //initData
				}
				emitter.onComplete();
			}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).safeSubscribe(new Observer<Integer>() {

				@Override
				public void onSubscribe(Disposable d) {
					dataModel.mDisposables.add(d);
				}

				@Override
				public void onNext(Integer integer) {

				}

				@Override
				public void onError(Throwable e) {

				}

				@Override
				public void onComplete() {
					if (mAdapter2AlbumListAdapter != null)
						mAdapter2AlbumListAdapter.notifyDataSetChanged();
				}
			});
		}
	}

	public RecyclerView getRecyclerView() {
		return mRecyclerView;
	}

	public MyRecyclerAdapter2AlbumList getAdapter() {
		return mAdapter2AlbumListAdapter;
	}

	public List<AlbumItem> getAlbumItemList() {
		return albumItemList;
	}

	public List<AlbumItem> getAlbumItemListBackup() {
		return albumItemListBackup;
	}

	@Override
	public boolean removeItem(@Nullable Item item) {
		if (!(item instanceof AlbumItem)) return false;

		final AlbumItem albumItem = (AlbumItem) item;

		if (albumItemList != null && mAdapter2AlbumListAdapter != null && albumItem.getAlbumId() != -1) {
			int pos = albumItemList.indexOf(albumItem);
			boolean result = albumItemList.remove(albumItem);
			mAdapter2AlbumListAdapter.notifyItemRemoved(pos);
			return result;
		}
		return false;
	}

	@Override
	public boolean addItem(@Nullable Item item) {
		if (!(item instanceof AlbumItem)) return false;

		final AlbumItem albumItem = (AlbumItem) item;

		if (albumItemList != null && mAdapter2AlbumListAdapter != null && albumItem.getAlbumId() != -1) {
			boolean result = albumItemList.add(albumItem);
			mAdapter2AlbumListAdapter.notifyItemInserted(albumItemList.size() - 1);
			return result;
		}
		return false;
	}

	public void initRecyclerView() {
		if (mView == null) return;

		mRecyclerView = mView.findViewById(R.id.recycler_view);
//		mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//			@Override
//			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//					GlideApp.with(mMainActivity).resumeRequests();
//				} else {
//					GlideApp.with(mMainActivity).pauseRequests();
//				}
//			}
//		});

		mRecyclerView.setHasFixedSize(true);

		//get type
		final SharedPreferences mDef = preferences;
		int type = mDef.getInt(Values.SharedPrefsTag.ALBUM_LIST_DISPLAY_TYPE, MyRecyclerAdapter2AlbumList.GRID_TYPE);
		switch (type) {
			case MyRecyclerAdapter2AlbumList.LINEAR_TYPE: {
				LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mMainActivity) {
					@Override
					protected int getExtraLayoutSpace(RecyclerView.State state) {
						return 700;
					}
				};
				linearLayoutManager.setItemPrefetchEnabled(true);
				linearLayoutManager.setInitialPrefetchItemCount(15);
				mRecyclerView.setLayoutManager(linearLayoutManager);
			}
			break;
			case MyRecyclerAdapter2AlbumList.GRID_TYPE: {
				GridLayoutManager gridLayoutManager = new GridLayoutManager(mMainActivity
						, mDef.getInt(Values.SharedPrefsTag.ALBUM_LIST_GRID_TYPE_COUNT, 2)) {
					@Override
					protected int getExtraLayoutSpace(RecyclerView.State state) {
						return 700;
					}
				};
				gridLayoutManager.setItemPrefetchEnabled(true);
				gridLayoutManager.setInitialPrefetchItemCount(15);
				mRecyclerView.setLayoutManager(gridLayoutManager);
			}
			break;
			default:
		}

		mAdapter2AlbumListAdapter = new MyRecyclerAdapter2AlbumList(mMainActivity, albumItemList, type);

		mRecyclerView.setItemViewCacheSize(15);
		mRecyclerView.setAdapter(mAdapter2AlbumListAdapter);

		mRecyclerView.setOnFastScrollStateChangeListener(new OnFastScrollStateChangeListener() {
			@Override
			public void onFastScrollStart() {
				MyRecyclerAdapter2AlbumList.stopLoadImage = true;
				GlideApp.with(mMainActivity).pauseRequests();
			}

			@Override
			public void onFastScrollStop() {
				MyRecyclerAdapter2AlbumList.stopLoadImage = false;
				GlideApp.with(mMainActivity).resumeRequests();
				mAdapter2AlbumListAdapter.notifyDataSetChanged();
			}
		});
	}
}
