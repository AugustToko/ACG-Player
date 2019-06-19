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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.jetbrains.annotations.NotNull;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.main.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter2AlbumList;
import top.geek_studio.chenlongcould.musicplayer.model.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.model.Item;
import top.geek_studio.chenlongcould.musicplayer.threadPool.CustomThreadPool;
import top.geek_studio.chenlongcould.musicplayer.utils.MusicUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenlongcould
 */
public final class AlbumListFragment extends BaseListFragment {

	public static final String TAG = "AlbumListFragment";

	private RecyclerView mRecyclerView;

	private MainActivity mMainActivity;

	private MyRecyclerAdapter2AlbumList mAdapter2AlbumListAdapter;

	private List<AlbumItem> albumItemList = new ArrayList<>();

	private List<AlbumItem> albumItemListBackup = new ArrayList<>();

	private View mView;

	public static AlbumListFragment newInstance() {
		return new AlbumListFragment();
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
	public FragmentType getFragmentType() {
		return FragmentType.ALBUM_LIST_FRAGMENT;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_album_list, container, false);
		setRecyclerViewData(mView);
		initAlbumData();
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
							emitter.onNext(albumItemList.size());
							CustomThreadPool.post(() -> MusicUtil.findArtworkWithId(mMainActivity, albumItem));
						} while (cursor.moveToNext());

						cursor.close();
						mView.post(() -> {
							if (mAdapter2AlbumListAdapter != null) mAdapter2AlbumListAdapter.notifyDataSetChanged();
						});

					}   //initData
				}
			}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).safeSubscribe(new Observer<Integer>() {

				@Override
				public void onSubscribe(Disposable d) {
					Data.sDisposables.add(d);
				}

				@Override
				public void onNext(Integer integer) {
					if (mAdapter2AlbumListAdapter != null) mAdapter2AlbumListAdapter.notifyDataSetChanged();
					Log.d(TAG, "onNext: load data album: " + integer);
				}

				@Override
				public void onError(Throwable e) {

				}

				@Override
				public void onComplete() {

				}
			});
		}
	}

	/**
	 * by firstStartApp, change Layout...
	 */
	public void setRecyclerViewData(@Nullable View view) {
		if (view == null) return;

		mRecyclerView = view.findViewById(R.id.recycler_view);
//		mRecyclerView.setHasFixedSize(true);

		//get type
		final SharedPreferences mDef = preferences;
		int type = mDef.getInt(Values.SharedPrefsTag.ALBUM_LIST_DISPLAY_TYPE, MyRecyclerAdapter2AlbumList.GRID_TYPE);
		switch (type) {
			case MyRecyclerAdapter2AlbumList.LINEAR_TYPE: {
				LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mMainActivity);
				linearLayoutManager.setItemPrefetchEnabled(true);
				linearLayoutManager.setInitialPrefetchItemCount(6);
				mRecyclerView.setLayoutManager(linearLayoutManager);
			}
			break;
			case MyRecyclerAdapter2AlbumList.GRID_TYPE: {
				GridLayoutManager gridLayoutManager = new GridLayoutManager(mMainActivity, mDef.getInt(Values.SharedPrefsTag.ALBUM_LIST_GRID_TYPE_COUNT, 2));
				gridLayoutManager.setItemPrefetchEnabled(true);
				gridLayoutManager.setInitialPrefetchItemCount(6);
				mRecyclerView.setLayoutManager(gridLayoutManager);
			}
			break;
			default:
		}

		mAdapter2AlbumListAdapter = new MyRecyclerAdapter2AlbumList(mMainActivity, albumItemList, type);

		mRecyclerView.setAdapter(mAdapter2AlbumListAdapter);
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

}
