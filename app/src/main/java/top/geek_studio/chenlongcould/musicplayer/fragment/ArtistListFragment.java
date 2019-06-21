package top.geek_studio.chenlongcould.musicplayer.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.main.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter2ArtistList;
import top.geek_studio.chenlongcould.musicplayer.model.ArtistItem;
import top.geek_studio.chenlongcould.musicplayer.model.Item;
import top.geek_studio.chenlongcould.musicplayer.threadPool.CustomThreadPool;
import top.geek_studio.chenlongcould.musicplayer.utils.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenlongcould
 */
public final class ArtistListFragment extends BaseListFragment {

	public static final String TAG = "ArtistListFragment";

	private RecyclerView mRecyclerView;

	private MainActivity mMainActivity;

	private MyRecyclerAdapter2ArtistList mAdapter2ArtistList;

	private List<ArtistItem> artistItemList = new ArrayList<>();

	private List<ArtistItem> artistItemListBackup = new ArrayList<>();

	public static ArtistListFragment newInstance() {
		return new ArtistListFragment();
	}

	private View mView;

	@Override
	public void onDestroy() {
		super.onDestroy();
		artistItemList.clear();
		artistItemListBackup.clear();
	}

	@Override
	public void onAttach(@NotNull Context context) {
		super.onAttach(context);
		mMainActivity = (MainActivity) getActivity();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_album_list, container, false);
		initArtistData();
		return mView;
	}

	private void initArtistData() {
		if (ContextCompat.checkSelfPermission(mMainActivity,
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(mMainActivity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, Values.REQUEST_WRITE_EXTERNAL_STORAGE);
		} else {
			CustomThreadPool.post(() -> {
				if (artistItemList.size() == 0) {
					final Cursor cursor = mMainActivity.getContentResolver().query(MediaStore.Audio.Artists
							.EXTERNAL_CONTENT_URI, null, null, null, null);
					if (cursor != null) {
						cursor.moveToFirst();
						if (cursor.getCount() > 0) {
							do {
								String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST));
								String albumId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID));
								ArtistItem artistItem;
								try {
									artistItem = new ArtistItem(albumName, Integer.parseInt(albumId));
								} catch (NumberFormatException e) {
									Toast.makeText(mMainActivity, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
									return;
								}
								artistItemList.add(artistItem);
								artistItemListBackup.add(artistItem);
							} while (cursor.moveToNext());

						}

						cursor.close();
						mView.post(() -> {
							if (mAdapter2ArtistList != null) mAdapter2ArtistList.notifyDataSetChanged();
						});
					}   //initData
				}
			});
		}
	}

	public RecyclerView getRecyclerView() {
		return mRecyclerView;
	}

	public MyRecyclerAdapter2ArtistList getAdapter() {
		return mAdapter2ArtistList;
	}

	public List<ArtistItem> getArtistItemList() {
		return artistItemList;
	}

	public List<ArtistItem> getArtistItemListBackup() {
		return artistItemListBackup;
	}

	@Override
	public FragmentType getFragmentType() {
		return FragmentType.ARTIST_FRAGMENT;
	}
	@Override
	public void reloadData() {
		super.reloadData();
		artistItemList.clear();
		mAdapter2ArtistList.notifyDataSetChanged();
		initArtistData();
	}

	@Override
	public boolean removeItem(@Nullable Item item) {
		if (!(item instanceof ArtistItem)) return false;

		final ArtistItem artistItem = (ArtistItem) item;

		if (mAdapter2ArtistList != null && artistItem.getArtistId() != -1) {
			int pos = artistItemList.indexOf(artistItem);
			boolean result = artistItemList.remove(artistItem);
			mAdapter2ArtistList.notifyItemRemoved(pos);
			return result;
		}
		return false;
	}

	@Override
	public boolean addItem(@Nullable Item item) {
		if (!(item instanceof ArtistItem)) return false;

		final ArtistItem artistItem = (ArtistItem) item;

		if (mAdapter2ArtistList != null && artistItem.getArtistId() != -1) {
			boolean result = artistItemList.add(artistItem);
			mAdapter2ArtistList.notifyItemInserted(artistItemList.size() - 1);
			return result;
		}
		return false;
	}

	public void initRecyclerView() {
		if (mView == null) return;

		mRecyclerView = mView.findViewById(R.id.recycler_view);
		mRecyclerView.setHasFixedSize(true);

		//get type
		final SharedPreferences mDef = PreferenceUtil.getDefault(mMainActivity);
		int type = mDef.getInt(Values.SharedPrefsTag.ARTIST_LIST_DISPLAY_TYPE, MyRecyclerAdapter2ArtistList.GRID_TYPE);
		switch (type) {
			case MyRecyclerAdapter2ArtistList.LINEAR_TYPE: {
				final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mMainActivity);
				linearLayoutManager.setItemPrefetchEnabled(true);
				linearLayoutManager.setInitialPrefetchItemCount(15);
				mRecyclerView.setLayoutManager(linearLayoutManager);
			}
			break;
			case MyRecyclerAdapter2ArtistList.GRID_TYPE: {
				final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity()
						, mDef.getInt(Values.SharedPrefsTag.ALBUM_LIST_GRID_TYPE_COUNT, 2));
				gridLayoutManager.setItemPrefetchEnabled(true);
				gridLayoutManager.setInitialPrefetchItemCount(15);
				mRecyclerView.setLayoutManager(gridLayoutManager);
			}
			break;
			default: {
				mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
			}
		}

		mAdapter2ArtistList = new MyRecyclerAdapter2ArtistList(mMainActivity, artistItemList, type);
		mRecyclerView.setItemViewCacheSize(15);
		mRecyclerView.setAdapter(mAdapter2ArtistList);
	}
}
