package top.geek_studio.chenlongcould.musicplayer.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;
import org.litepal.LitePalDB;

import java.util.HashSet;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.musicplayer.App;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.MusicService;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.main.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.database.MyBlackPath;
import top.geek_studio.chenlongcould.musicplayer.databinding.FragmentMusicListBinding;
import top.geek_studio.chenlongcould.musicplayer.model.Item;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.threadPool.CustomThreadPool;
import top.geek_studio.chenlongcould.musicplayer.utils.MusicUtil;
import top.geek_studio.chenlongcould.musicplayer.utils.PreferenceUtil;

/**
 * @author chenlongcould
 */
public final class MusicListFragment extends BaseListFragment {

	public static final String TAG = "MusicListFragment";

	public FragmentMusicListBinding mMusicListBinding;

	private MyRecyclerAdapter adapter;

	private MainActivity mActivity;

	public static MusicListFragment newInstance() {
		return new MusicListFragment();
	}

	@Override
	public FragmentType getFragmentType() {
		return FragmentType.MUSIC_LIST_FRAGMENT;
	}

	@Override
	public void reloadData() {
		Data.sMusicItems.clear();
		Data.sMusicItemsBackUp.clear();
		mActivity.runOnUiThread(() -> adapter.notifyDataSetChanged());
		loadData();
	}

	@Override
	public void onAttach(@NotNull Context context) {
		super.onAttach(context);
		mActivity = (MainActivity) getActivity();
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mMusicListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_list, container, false);
		loadData();
		return mMusicListBinding.getRoot();
	}

	private void loadData() {
		if (ContextCompat.checkSelfPermission(mActivity,
				Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Values.PermissionCode.REQUEST_WRITE_EXTERNAL_STORAGE);
			return;
		}

		Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
			Data.sMusicItems.clear();
			Data.sPlayOrderList.clear();

			/*---------------------- init Data!!!! -------------------*/
			final Cursor cursor = mActivity.getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null
					, MediaStore.Audio.Media.DEFAULT_SORT_ORDER
			);

			if (cursor == null || !cursor.moveToFirst()) return;

			if (cursor.getCount() == 0) {
				// TODO: 没有歌曲提示空
				return;
			}

			// skip short
			final boolean skipShort = preferences
					.getBoolean(Values.SharedPrefsTag.HIDE_SHORT_SONG, true);

			// check black list
			final LitePalDB blackList = new LitePalDB("BlackList", App.BLACK_LIST_VERSION);
			blackList.addClassName(MyBlackPath.class.getName());
			LitePal.use(blackList);
			HashSet<String> blackPaths = new HashSet<>();

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				LitePal.findAll(MyBlackPath.class).forEach(myBlackPath -> blackPaths.add(myBlackPath.getDirPath()));
			} else {
				for (MyBlackPath s : LitePal.findAll(MyBlackPath.class)) {
					blackPaths.add(s.getDirPath());
				}
			}

			LitePal.useDefault();

			// music that you last played
			int lastId = preferences.getInt(Values.SharedPrefsTag.LAST_PLAY_MUSIC_ID, -1);

			do {
				final String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
				if (blackPaths.contains(path)) return;

				final int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

				if (skipShort && duration <= MusicService.DEFAULT_SHORT_DURATION) {
					Log.d(TAG, "loadDataSource: the music-file duration is " + duration + " (too short)" +
							", skip...");
					continue;
				}

				final String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
				final String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
				final String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
				final int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
				final int size = (int) cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
				final String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
				final long addTime = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));
				final int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
				final int artistId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));

				final MusicItem.Builder builder = new MusicItem.Builder(id, name, path)
						.musicAlbum(albumName)
						.addTime(addTime)
						.artist(artist)
						.duration(duration)
						.mimeName(mimeType)
						.size(size)
						.addAlbumId(albumId)
						.addArtistId(artistId);

				final MusicItem item = builder.build();

				Data.sMusicItems.add(item);
				Data.sMusicItemsBackUp.add(item);

				Data.sPlayOrderList.add(item);
				Data.sPlayOrderListBackup.add(item);

				if (lastId == id) {
					MusicUtil.findArtworkWithId(mActivity, item);
					Data.sCurrentMusicItem = item;
				} else {
					CustomThreadPool.post(() -> MusicUtil.findArtworkWithId(mActivity, item));
				}

			}
			while (cursor.moveToNext());
			cursor.close();
			emitter.onComplete();
		}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).safeSubscribe(new Observer<Integer>() {
			@Override
			public final void onSubscribe(Disposable disposable) {
				Data.sDisposables.add(disposable);
			}

			@Override
			public final void onNext(Integer result) {
//				if (result == -1) {
//					AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
//					builder.setTitle("Error")
//							.setMessage("Can not find any music or the cursor is null, Will exit.")
//							.setCancelable(false)
//							.setNegativeButton("OK", (dialog, which) -> dialog.cancel());
//					builder.show();
//				}
			}

			@Override
			public final void onError(Throwable throwable) {
				Toast.makeText(mActivity, throwable.getMessage(), Toast.LENGTH_SHORT).show();
			}

			@Override
			public final void onComplete() {
				if (adapter != null) adapter.notifyDataSetChanged();

				// 更新最后播放
				if (Data.sCurrentMusicItem != null && Data.sCurrentMusicItem.getMusicID() > -1) {
					final Message message = Message.obtain();
					message.obj = Data.sCurrentMusicItem;
					message.what = MusicDetailFragment.NotLeakHandler.SETUP_MUSIC_DATA;
					MusicDetailFragment.sendMessage(message);
				}
			}
		});
	}

	public final MyRecyclerAdapter getAdapter() {
		return adapter;
	}

	@Override
	public boolean removeItem(@Nullable Item item) {
		if (item instanceof MusicItem) {
			Data.sMusicItems.remove(item);
			Data.sMusicItemsBackUp.remove(item);
			Data.sPlayOrderList.remove(item);
			Data.sPlayOrderListBackup.remove(item);
			return true;
		}
		return false;
	}

	@Override
	public boolean addItem(@Nullable Item item) {
		if (item instanceof MusicItem) {
			Data.sMusicItems.add((MusicItem) item);
			Data.sMusicItemsBackUp.add((MusicItem) item);
			Data.sPlayOrderList.add((MusicItem) item);
			Data.sPlayOrderListBackup.add((MusicItem) item);
			return true;
		}
		return false;
	}

	@Override
	public void onDestroy() {
		mMusicListBinding.unbind();
		super.onDestroy();
	}

	public void initRecyclerView() {
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
		linearLayoutManager.setItemPrefetchEnabled(true);
		linearLayoutManager.setInitialPrefetchItemCount(15);

		adapter = new MyRecyclerAdapter(mActivity, Data.sMusicItems
				, new MyRecyclerAdapter.Config(PreferenceUtil.getDefault(mActivity).getInt(Values.SharedPrefsTag
				.RECYCLER_VIEW_ITEM_STYLE, 0), true, true));
		mMusicListBinding.includeRecycler.recyclerView.setLayoutManager(linearLayoutManager);
		mMusicListBinding.includeRecycler.recyclerView.setHasFixedSize(true);
		mMusicListBinding.includeRecycler.recyclerView.setItemViewCacheSize(15);

		mMusicListBinding.includeRecycler.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
					GlideApp.with(mActivity).resumeRequests();
				} else {
					GlideApp.with(mActivity).pauseRequests();
				}
			}
		});

		mMusicListBinding.includeRecycler.recyclerView.setAdapter(adapter);
		mMusicListBinding.includeRecycler.recyclerView.setOnFastScrollStateChangeListener(new OnFastScrollStateChangeListener() {
			@Override
			public void onFastScrollStart() {
				MyRecyclerAdapter.stopLoadImage = true;
				GlideApp.with(mActivity).pauseRequests();
			}

			@Override
			public void onFastScrollStop() {
				MyRecyclerAdapter.stopLoadImage = false;
				GlideApp.with(mActivity).resumeRequests();
				adapter.notifyDataSetChanged();
			}
		});
	}
}
