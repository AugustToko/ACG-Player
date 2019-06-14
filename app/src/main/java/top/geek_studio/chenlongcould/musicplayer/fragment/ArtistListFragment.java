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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter2ArtistList;
import top.geek_studio.chenlongcould.musicplayer.model.ArtistItem;
import top.geek_studio.chenlongcould.musicplayer.model.Item;
import top.geek_studio.chenlongcould.musicplayer.threadPool.ArtistThreadPool;
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

	@Override
	public void onAttach(@NotNull Context context) {
		super.onAttach(context);
		mMainActivity = (MainActivity) getActivity();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_album_list, container, false);
		setRecyclerViewData(view);
		initArtistData();
		return view;
	}

	public void setRecyclerViewData(@Nullable View view) {
		if (view == null) return;

		mRecyclerView = view.findViewById(R.id.recycler_view);
		mRecyclerView.setHasFixedSize(true);

		//get type
		final SharedPreferences mDef = PreferenceUtil.getDefault(mMainActivity);
		int type = mDef.getInt(Values.SharedPrefsTag.ARTIST_LIST_DISPLAY_TYPE, MyRecyclerAdapter2ArtistList.GRID_TYPE);
		switch (type) {
			case MyRecyclerAdapter2ArtistList.LINEAR_TYPE: {
				final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mMainActivity);
				linearLayoutManager.setItemPrefetchEnabled(true);
				linearLayoutManager.setInitialPrefetchItemCount(6);
				mRecyclerView.setLayoutManager(linearLayoutManager);
			}
			break;
			case MyRecyclerAdapter2ArtistList.GRID_TYPE: {
				final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity()
						, mDef.getInt(Values.SharedPrefsTag.ALBUM_LIST_GRID_TYPE_COUNT, 2));
				gridLayoutManager.setItemPrefetchEnabled(true);
				gridLayoutManager.setInitialPrefetchItemCount(6);
				mRecyclerView.setLayoutManager(gridLayoutManager);
			}
			break;
			default: {
				mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
			}
		}

		mAdapter2ArtistList = new MyRecyclerAdapter2ArtistList(mMainActivity, artistItemList, type);
		mRecyclerView.setAdapter(mAdapter2ArtistList);
	}

	private void initArtistData() {
		ArtistThreadPool.post(() -> {
			if (artistItemList.size() == 0) {
				final Cursor cursor = mMainActivity.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, null, null, null);
				if (cursor != null) {
					cursor.moveToFirst();

					do {
						String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST));
						String albumId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID));
						final ArtistItem artistItem = new ArtistItem(albumName, Integer.parseInt(albumId));
						artistItemList.add(artistItem);
						artistItemListBackup.add(artistItem);
					} while (cursor.moveToNext());

					cursor.close();
				}   //initData
			}
		});
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
}
