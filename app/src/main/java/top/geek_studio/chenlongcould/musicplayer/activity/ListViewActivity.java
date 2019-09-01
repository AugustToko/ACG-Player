package top.geek_studio.chenlongcould.musicplayer.activity;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.MessageWorker;
import top.geek_studio.chenlongcould.musicplayer.MusicService;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.base.BaseListActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.main.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.broadcast.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.utils.MusicUtil;
import top.geek_studio.chenlongcould.musicplayer.utils.PreferenceUtil;

/**
 * @author chenlongcould
 */
public final class ListViewActivity extends BaseListActivity {

	public static final String TAG = "ListViewActivity";

	public static NotLeakHandler handler;

	public static volatile boolean initDone = false;

	private AppBarLayout mAppBarLayout;
	private Toolbar mToolbar;
	private RecyclerView mRecyclerView;
	private MyRecyclerAdapter adapter;

	/**
	 * 保存播放列表下的Music (如果当前type是play_list_item的话 {@link #mType} )
	 */
	private List<MusicItem> mMusicItemList = new ArrayList<>();

	/**
	 * save current playlist name, if current type is play_list_item {@link #mType}
	 */
	private String currentListName;

	/**
	 * different type enter different UI(Activity)
	 * @see ListType
	 */
	private String mType = "null";

	private Disposable mDisposable;

	public static boolean sendMessageStatic(@NonNull Message message) {
		boolean result = false;
		if (handler != null) {
			handler.sendMessage(message);
			result = true;
		}
		return result;
	}

	@SuppressWarnings("UnusedReturnValue")
	public static boolean sendEmptyMessageStatic(int what) {
		boolean result = false;
		if (handler != null) {
			handler.sendEmptyMessage(what);
			result = true;
		}
		return result;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_add_recent);

		handler = new NotLeakHandler(this, Looper.getMainLooper());

		mRecyclerView = findViewById(R.id.activity_add_recent_recycler);
		mAppBarLayout = findViewById(R.id.app_bar_layout);
		mToolbar = findViewById(R.id.toolbar);

		inflateCommonMenu();

		super.initView(mToolbar, mAppBarLayout);
		super.onCreate(savedInstanceState);

		mToolbar.setNavigationOnClickListener(v -> onBackPressed());
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

		mType = getIntent().getStringExtra(IntentTag.INTENT_START_BY);

		if (mType != null) {
			switch (mType) {
				case ListType.ACTION_ADD_RECENT: {
					mToolbar.setTitle(getResources().getString(R.string.add_recent));

					mMusicItemList.addAll(MainActivity.activityWeakReference.get().getDataModel().mMusicItems);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
						mMusicItemList.sort((o1, o2) -> {
							if (o1 == null || o2 == null) {
								return 0;
							}
							return Long.compare(o1.getAddTime(), o2.getAddTime());
						});
					}

					adapter = new MyRecyclerAdapter(this, mMusicItemList, new MyRecyclerAdapter.Config(
							PreferenceUtil.getDefault(this).getInt(Values.SharedPrefsTag.RECYCLER_VIEW_ITEM_STYLE, 0)
							, false));
					mRecyclerView.setAdapter(adapter);
				}
				break;
				case ListType.ACTION_PLAY_LIST_ITEM: {
					mToolbar.setTitle(getIntent().getStringExtra("play_list_name"));

					currentListName = getIntent().getStringExtra("play_list_name");

					adapter = new MyRecyclerAdapter(ListViewActivity.this, mMusicItemList
							, new MyRecyclerAdapter.Config(PreferenceUtil.getDefault(this)
							.getInt(Values.SharedPrefsTag.RECYCLER_VIEW_ITEM_STYLE, 0), true));
					mRecyclerView.setAdapter(adapter);

					mDisposable = Observable.create((ObservableOnSubscribe<Integer>) observableEmitter -> {
						//data

						//get musicId in PlayList
						Cursor cursor = getContentResolver().query(MediaStore.Audio.Playlists.Members.getContentUri("external", getIntent().getIntExtra("play_list_id", -1))
								, null, null, null, MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
						if (cursor != null && cursor.moveToFirst()) {
							cursor.moveToFirst();
							do {

								//search music (with audioId)
								int audioId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID));
								Cursor cursor1 = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.MediaColumns._ID + " = ?", new String[]{String.valueOf(audioId)}, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
								if (cursor1 != null && cursor1.moveToFirst()) {
									do {
										final String mimeType = cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE));
										final String name = cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE));
										final String albumName = cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
										final int id = cursor1.getInt(cursor1.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
										final int size = (int) cursor1.getLong(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
										final int duration = cursor1.getInt(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
										final String artist = cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
										final long addTime = cursor1.getLong(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));
										final int albumId = cursor1.getInt(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
										final String path = cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

										final MusicItem.Builder builder = new MusicItem.Builder(id, name, path)
												.musicAlbum(albumName)
												.addTime((int) addTime)
												.artist(artist)
												.duration(duration)
												.mimeName(mimeType)
												.size(size)
												.addAlbumId(albumId)
												.setArtwork(MusicUtil.findArtworkWithId(ListViewActivity.this
														, albumId));

										mMusicItemList.add(builder.build());
									} while (cursor1.moveToNext());
									cursor1.close();
								}

							} while (cursor.moveToNext());
							cursor.close();
						}
						observableEmitter.onNext(0);
					}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
							.subscribe(i -> {
								if (i != 0) {
									return;
								}
								adapter.notifyDataSetChanged();
							});
				}
				break;

				case ListType.ACTION_HISTORY: {
					mToolbar.setTitle(getString(R.string.history));
					mMusicItemList.clear();
					mMusicItemList.addAll(Data.sHistoryPlayed);

					adapter = new MyRecyclerAdapter(ListViewActivity.this, mMusicItemList
							, new MyRecyclerAdapter.Config(PreferenceUtil.getDefault(this)
							.getInt(Values.SharedPrefsTag
							.RECYCLER_VIEW_ITEM_STYLE, 0), false));
					mRecyclerView.setAdapter(adapter);
				}
				break;

//				case ListType.ACTION_TRASH_CAN: {
//					if (PreferenceUtil.getDefault(this).getBoolean(Values.SharedPrefsTag.TRASH_CAN_INFO
//							, true)) {
//						AlertDialog.Builder builder = new AlertDialog.Builder(this)
//								.setTitle("About trash can")
//								.setMessage("You can throw the music into the trash, so you won't hear the song recently unless you actively play it.")
//								.setNegativeButton("OK", (dialog, which) -> {
//									PreferenceUtil.getDefault(ListViewActivity.this).edit().putBoolean(Values.SharedPrefsTag.TRASH_CAN_INFO, false).apply();
//									dialog.dismiss();
//								});
//						builder.show();
//					}
//
//					mToolbar.setTitle(getString(R.string.trash_can));
//					mMusicItemList.addAll(Data.S_TRASH_CAN_LIST);
//
//					adapter = new MyRecyclerAdapter(ListViewActivity.this, mMusicItemList
//							, new MyRecyclerAdapter.Config(PreferenceUtil.getDefault(this)
//							.getInt(Values.SharedPrefsTag
//							.RECYCLER_VIEW_ITEM_STYLE, 0), false));
//					mRecyclerView.setAdapter(adapter);
//
//				}
//				break;
				default:
			}

			initDone = true;
		}

	}

	@Override
	public boolean removeItem(MusicItem item) {
		if (mMusicItemList != null && adapter != null && item != null && item.getMusicID() != -1) {
			final int position = mMusicItemList.indexOf(item);
			final boolean result = mMusicItemList.remove(item);
			adapter.notifyItemRemoved(position);
			return result;
		}
		return false;
	}

	@Override
	public boolean addItem(@Nullable MusicItem item) {
		if (mMusicItemList != null && adapter != null && item != null && item.getMusicID() != -1) {
			final boolean result = mMusicItemList.add(item);
			adapter.notifyItemInserted(0);
			return result;
		}
		return false;
	}

	public void inflateCommonMenu() {
		mToolbar.getMenu().clear();
		mToolbar.inflateMenu(R.menu.menu_public);
		mToolbar.setOnMenuItemClickListener(item -> {
			switch (item.getItemId()) {
				case R.id.menu_public_random: {
					if (mMusicItemList.size() == 0) return true;
					int index = new Random().nextInt(mMusicItemList.size());
					ReceiverOnMusicPlay.onItemClicked(mMusicItemList.get(index), index);
				}
				break;

				case R.id.menu_public_m3u: {
					Toast.makeText(this, "Building...", Toast.LENGTH_SHORT).show();
				}
				break;
				default:
			}
			return true;
		});
	}

	public void inflateChooseMenu() {
		mToolbar.getMenu().clear();
		mToolbar.inflateMenu(R.menu.menu_toolbar_main_choose);
		mToolbar.setOnMenuItemClickListener(item -> {
			switch (item.getItemId()) {
				case R.id.menu_toolbar_main_choose_share: {
					MusicUtil.sharMusic(ListViewActivity.this, new ArrayList<>(adapter.getSelected()));
				}
				break;

				default: {
					// TODO: 2019/6/2 多选菜单
					Toast.makeText(this, "Building...", Toast.LENGTH_SHORT).show();
				}
			}

			return true;
		});
	}

	@Override
	protected void onDestroy() {
		if (mDisposable != null && !mDisposable.isDisposed()) {
			mDisposable.dispose();
		}

		mType = null;
		adapter = null;
		mRecyclerView = null;
		mMusicItemList.clear();

		MainActivity.startService(this, MusicService.ServiceActions.ACTION_RESET_LIST);

		super.onDestroy();
	}

	@Override
	public void sendEmptyMessage(int what) {
		if (handler != null) handler.sendEmptyMessage(what);
	}

	@Override
	public void sendMessage(Message message) {
	}

	public interface ListType {
		String ACTION_ADD_RECENT = "add recent";
		@Deprecated
		String ACTION_FAVOURITE = "favourite music";
		String ACTION_HISTORY = "play history";
		@Deprecated
		String ACTION_TRASH_CAN = "trash can";
		String ACTION_PLAY_LIST_ITEM = "play_list_item";
	}

	@Override
	public String getActivityTAG() {
		return TAG;
	}

	public interface IntentTag {
		String INTENT_START_BY = "start_by";
	}


	@Override
	public void onBackPressed() {
		if (!initDone) Toast.makeText(this, "Wait, loading data...", Toast.LENGTH_SHORT).show();

		super.onBackPressed();
	}

	public MyRecyclerAdapter getAdapter() {
		return adapter;
	}

	public static final class NotLeakHandler extends Handler {

		public static final byte NOTI_ADAPTER_CHANGED = 127;

		@SuppressWarnings("unused")
		private WeakReference<ListViewActivity> mWeakReference;

		NotLeakHandler(ListViewActivity activity, Looper looper) {
			super(looper);
			mWeakReference = new WeakReference<>(activity);
		}

		@Override
		public final void handleMessage(Message msg) {
			switch (msg.what) {

				case NOTI_ADAPTER_CHANGED: {
					if (mWeakReference != null && mWeakReference.get() != null && mWeakReference.get().adapter != null
							&& mWeakReference.get().mType != null && mWeakReference.get().mType.equals(ListType.ACTION_HISTORY)) {
						mWeakReference.get().adapter.notifyDataSetChanged();
					}
				}
				break;

				case MessageWorker.RELOAD: {
					if (mWeakReference.get().adapter != null) mWeakReference.get().adapter.notifyDataSetChanged();
				}
				break;
			}
		}

	}
}
