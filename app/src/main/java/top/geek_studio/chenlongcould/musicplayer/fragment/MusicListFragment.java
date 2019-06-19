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
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;
import org.litepal.LitePalDB;
import top.geek_studio.chenlongcould.musicplayer.*;
import top.geek_studio.chenlongcould.musicplayer.activity.main.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.database.MyBlackPath;
import top.geek_studio.chenlongcould.musicplayer.databinding.FragmentMusicListBinding;
import top.geek_studio.chenlongcould.musicplayer.model.Item;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.threadPool.CustomThreadPool;
import top.geek_studio.chenlongcould.musicplayer.utils.MusicUtil;
import top.geek_studio.chenlongcould.musicplayer.utils.PreferenceUtil;

import java.util.List;

/**
 * @author chenlongcould
 */
public final class MusicListFragment extends BaseListFragment {

	public static final String TAG = "MusicListFragment";
	private FragmentMusicListBinding mMusicListBinding;
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
		adapter.notifyDataSetChanged();
		loadData();
	}

	@Override
	public void onAttach(@NotNull Context context) {
		super.onAttach(context);
		mActivity = (MainActivity) getActivity();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mMusicListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_list, container, false);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity) {
			@Override
			protected int getExtraLayoutSpace(RecyclerView.State state) {
				return 500;
			}
		};
		linearLayoutManager.setItemPrefetchEnabled(true);
		linearLayoutManager.setInitialPrefetchItemCount(6);

		adapter = new MyRecyclerAdapter(mActivity, Data.sMusicItems
				, new MyRecyclerAdapter.Config(preferences.getInt(Values.SharedPrefsTag
				.RECYCLER_VIEW_ITEM_STYLE, 0), true));

		mMusicListBinding.includeRecycler.recyclerView.setLayoutManager(linearLayoutManager);
		mMusicListBinding.includeRecycler.recyclerView.setHasFixedSize(true);
		mMusicListBinding.includeRecycler.recyclerView.setItemViewCacheSize(5);
		mMusicListBinding.includeRecycler.recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
		mMusicListBinding.includeRecycler.recyclerView.setDrawingCacheEnabled(true);
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

		loadData();
		return mMusicListBinding.getRoot();
	}

	private void loadData() {
		if (ContextCompat.checkSelfPermission(mActivity,
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(mActivity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, Values.REQUEST_WRITE_EXTERNAL_STORAGE);
		} else {
			Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
				if (Data.sMusicItems.isEmpty()) {

					SharedPreferences preferences = PreferenceUtil.getDefault(mActivity);

					boolean loadorder = Data.sPlayOrderList.size() == 0;

					/*---------------------- init Data!!!! -------------------*/
					final Cursor cursor = mActivity.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
					if (cursor != null && cursor.moveToFirst()) {
						//没有歌曲直接退出app
						if (cursor.getCount() != 0) {
							// skip short
							final boolean skipShort = preferences
									.getBoolean(Values.SharedPrefsTag.HIDE_SHORT_SONG, true);

							// black list
							final LitePalDB blackList = new LitePalDB("BlackList", App.BLACK_LIST_VERSION);
							blackList.addClassName(MyBlackPath.class.getName());
							LitePal.use(blackList);
							List<MyBlackPath> lists = LitePal.findAll(MyBlackPath.class);
							LitePal.useDefault();

							// music that you last played
							int lastId = preferences.getInt(Values.SharedPrefsTag.LAST_PLAY_MUSIC_ID, -1);

							do {
								final String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

								boolean skip = false;

								for (int i = 0; i < lists.size(); i++) {
									final MyBlackPath bp = lists.get(i);

									if (bp.getDirPath().contains(path) || bp.getDirPath().equals(path)) {
										skip = true;
										lists.remove(bp);
										break;
									}

								}

								if (skip) {
									Log.d(TAG, "loadDataSource: skip the song that in the blacklist");
									continue;
								}

								final int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
								if (skipShort && duration <= MainActivity.DEFAULT_SHORT_DURATION) {
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


								if (lastId == id) {
									Data.sCurrentMusicItem = builder.build();
								}

								final MusicItem item = builder.build();
								Data.sMusicItems.add(item);
								Data.sMusicItemsBackUp.add(item);

								if (loadorder) {
									Data.sPlayOrderList.add(item);
									Data.sPlayOrderListBackup.add(item);
								}

								emitter.onNext(Data.sMusicItems.size());

								CustomThreadPool.post(() -> MusicUtil.findArtworkWithId(mActivity, item));
							}
							while (cursor.moveToNext());
							cursor.close();
						}
					}
				}
			}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).safeSubscribe(new Observer<Integer>() {
				@Override
				public final void onSubscribe(Disposable disposable) {
					Data.sDisposables.add(disposable);
				}

				@Override
				public final void onNext(Integer result) {
					Log.d(TAG, "onNext: music list: " + result);
//				if (result == -1) {
//					AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
//					builder.setTitle("Error")
//							.setMessage("Can not find any music or the cursor is null, Will exit.")
//							.setCancelable(false)
//							.setNegativeButton("OK", (dialog, which) -> dialog.cancel());
//					builder.show();
//				}

					adapter.notifyDataSetChanged();
				}

				@Override
				public final void onError(Throwable throwable) {
					Toast.makeText(mActivity, throwable.getMessage(), Toast.LENGTH_SHORT).show();
				}

				@Override
				public final void onComplete() {

				}
			});
		}
	}

	public final MyRecyclerAdapter getAdapter() {
		return adapter;
	}

	public FragmentMusicListBinding getMusicListBinding() {
		return mMusicListBinding;
	}

	@Override
	public boolean removeItem(@Nullable Item item) {
		if (item instanceof MusicItem) {
			Data.sMusicItems.remove(item);
			Data.sMusicItemsBackUp.remove(item);
			Data.sPlayOrderList.remove(item);
			Data.sPlayOrderListBackup.remove(item);
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
		}
		return false;
	}

}
