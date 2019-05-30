package top.geek_studio.chenlongcould.musicplayer.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import top.geek_studio.chenlongcould.geeklibrary.DialogUtil;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter2AlbumList;
import top.geek_studio.chenlongcould.musicplayer.model.AlbumItem;

/**
 * @author chenlongcould
 */
public final class AlbumListFragment extends BaseFragment {

	public static final String TAG = "AlbumListFragment";

	private RecyclerView mRecyclerView;

	private MainActivity mMainActivity;

	private MyRecyclerAdapter2AlbumList mAdapter2AlbumListAdapter;

	public static AlbumListFragment newInstance() {
		return new AlbumListFragment();
	}

	@Override
	protected void setFragmentType(FragmentType fragmentType) {
		fragmentType = FragmentType.ALBUM_LIST_FRAGMENT;
	}

	@Override
	public void reloadData() {
		Data.sAlbumItems.clear();
		Data.sAlbumItemsBackUp.clear();
		initAlbumData();
	}

	@Override
	public void onAttach(@NotNull Context context) {
		super.onAttach(context);
		mMainActivity = (MainActivity) getActivity();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_album_list, container, false);
		setRecyclerViewData(view);
		initAlbumData();
		return view;
	}

	private void initAlbumData() {

		final AlertDialog load = DialogUtil.getLoadingDialog(mMainActivity, "Loading");
		load.show();

		Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
			if (Data.sAlbumItems.size() == 0) {
				Cursor cursor = mMainActivity.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, null, null, null);
				if (cursor != null) {
					cursor.moveToFirst();

					do {
						String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM));
						String albumId = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
						String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST));
						final AlbumItem albumItem = new AlbumItem(albumName, Integer.parseInt(albumId), artist);
						Data.sAlbumItems.add(albumItem);
						Data.sAlbumItemsBackUp.add(albumItem);
					} while (cursor.moveToNext());

					cursor.close();
				}   //initData
			}

			emitter.onComplete();
		}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
				.safeSubscribe(new Observer<Integer>() {
					@Override
					public void onSubscribe(Disposable disposable) {
						Data.sDisposables.add(disposable);
					}

					@Override
					public void onNext(Integer result) {

					}

					@Override
					public void onError(Throwable throwable) {

					}

					@Override
					public void onComplete() {
						load.dismiss();
						mMainActivity.getMainBinding().toolBar.setSubtitle(Data.sAlbumItems.size() + " Album");
					}
				});

	}

	/**
	 * by firstStartApp, change Layout...
	 */
	public void setRecyclerViewData(@Nullable View view) {
		if (view == null) return;

		mRecyclerView = view.findViewById(R.id.recycler_view);
		mRecyclerView.setHasFixedSize(true);

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

		mAdapter2AlbumListAdapter = new MyRecyclerAdapter2AlbumList(mMainActivity, Data.sAlbumItems, type);
		mRecyclerView.setAdapter(mAdapter2AlbumListAdapter);
	}

	public RecyclerView getRecyclerView() {
		return mRecyclerView;
	}

	public MyRecyclerAdapter2AlbumList getAdapter() {
		return mAdapter2AlbumListAdapter;
	}

}
