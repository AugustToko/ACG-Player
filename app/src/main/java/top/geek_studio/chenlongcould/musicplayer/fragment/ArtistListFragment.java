package top.geek_studio.chenlongcould.musicplayer.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter2ArtistList;
import top.geek_studio.chenlongcould.musicplayer.model.ArtistItem;

/**
 * @author chenlongcould
 */
public final class ArtistListFragment extends BaseFragment {

	public static final String TAG = "ArtistListFragment";

	private RecyclerView mRecyclerView;

	private MainActivity mMainActivity;

	private MyRecyclerAdapter2ArtistList mAdapter2ArtistList;

	public static ArtistListFragment newInstance() {
		return new ArtistListFragment();
	}

	@Override
	public void onAttach(@NotNull Context context) {
		super.onAttach(context);
		mMainActivity = (MainActivity) getActivity();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_album_list, container, false);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		if (isVisibleToUser) {
			if (Data.sArtistItems.size() == 0) {
				initArtistData();
			}
		}
	}

	private void initArtistData() {
		
		final AlertDialog load = DialogUtil.getLoadingDialog(mMainActivity, "Loading");
		load.show();

		Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
			if (Data.sArtistItems.size() == 0) {
				Cursor cursor = mMainActivity.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, null, null, null);
				if (cursor != null) {
					cursor.moveToFirst();

					do {
						String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST));
						String albumId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID));
						Data.sArtistItems.add(new ArtistItem(albumName, Integer.parseInt(albumId)));
						Data.sArtistItemsBackUp.add(new ArtistItem(albumName, Integer.parseInt(albumId)));
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
						setRecyclerViewData();
						mMainActivity.getMainBinding().toolBar.setSubtitle(Data.sArtistItems.size() + " Artists");
						load.dismiss();
					}
				});
	}

	/**
	 * by firstStartApp, change Layout...
	 */
	public void setRecyclerViewData() {

		if (getView() != null) {
			mRecyclerView = getView().findViewById(R.id.recycler_view);
			mRecyclerView.setHasFixedSize(true);

			//get type
			final SharedPreferences mDef = PreferenceManager.getDefaultSharedPreferences(getActivity());
			int type = mDef.getInt(Values.SharedPrefsTag.ARTIST_LIST_DISPLAY_TYPE, MyRecyclerAdapter2ArtistList.GRID_TYPE);
			switch (type) {
				case MyRecyclerAdapter2ArtistList.LINEAR_TYPE: {
					mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
				}
				break;
				case MyRecyclerAdapter2ArtistList.GRID_TYPE: {
					mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), mDef.getInt(Values.SharedPrefsTag.ALBUM_LIST_GRID_TYPE_COUNT, 2)));
				}
				break;
				default:
					mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
			}

			mAdapter2ArtistList = new MyRecyclerAdapter2ArtistList(mMainActivity, Data.sArtistItems, type);
			mRecyclerView.setAdapter(mAdapter2ArtistList);
		}
	}

	public RecyclerView getRecyclerView() {
		return mRecyclerView;
	}

	public MyRecyclerAdapter2ArtistList getAdapter2ArtistList() {
		return mAdapter2ArtistList;
	}

	@Override
	protected void setFragmentType(FragmentType fragmentType) {
		fragmentType = FragmentType.ARTIST_FRAGMENT;
	}
}
