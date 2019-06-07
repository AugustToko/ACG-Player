package top.geek_studio.chenlongcould.musicplayer.activity;

import android.database.Cursor;
import android.os.*;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.AppBarLayout;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.musicplayer.*;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.broadcast.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.utils.MusicUtil;
import top.geek_studio.chenlongcould.musicplayer.utils.PreferenceUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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
	 */
	private String mType;
	private Disposable mDisposable;

	/**
	 * add music to history list
	 */
	public static void addToHistory(@NonNull MusicItem item) {
		Data.sHistoryPlayed.add(item);
		if (initDone) {
			handler.sendEmptyMessage(NotLeakHandler.NOTIFICATION_ITEM_INSERT);
		}
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

		Data.sPlayOrderListBackup.clear();
		Data.sPlayOrderListBackup.addAll(Data.sPlayOrderList);


		if (mType != null) {
			switch (mType) {
				case FragmentType.ACTION_ADD_RECENT: {
					mToolbar.setTitle(getResources().getString(R.string.add_recent));

					mMusicItemList.addAll(Data.sMusicItems);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
						mMusicItemList.sort((o1, o2) -> {
							if (o1 == null || o2 == null) {
								return 0;
							}
							return Long.compare(o1.getAddTime(), o2.getAddTime());
						});
					}

					Data.syncPlayOrderList(this, mMusicItemList);
					Data.shuffleOrderListSync(this, false);

					adapter = new MyRecyclerAdapter(this, mMusicItemList, new MyRecyclerAdapter.Config(
							preferences.getInt(Values.SharedPrefsTag.RECYCLER_VIEW_ITEM_STYLE, 0)
							, false));
					mRecyclerView.setAdapter(adapter);
				}
				break;
//				case FragmentType.ACTION_FAVOURITE: {
//					mToolbar.setTitle(getResources().getString(R.string.my_favourite));
//
//					final PlayListItem playListItem = MusicUtil.getFavoritesPlaylist(this);
//
//					if (playListItem != null) {
//						int id = playListItem.getId();
//						if (id != -1) {
//							mDisposable = Observable.create((ObservableOnSubscribe<Integer>) observableEmitter -> {
//								//data
//
//								//get musicId in PlayList
//								final Cursor cursor = getContentResolver().query(MediaStore.Audio.Playlists.Members.getContentUri("external", id)
//										, null, null, null, MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
//								if (cursor != null && cursor.moveToFirst()) {
//									cursor.moveToFirst();
//									do {
//
//										//search music (with audioId)
//										int audioId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID));
//										Cursor cursor1 = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.MediaColumns._ID + " = ?", new String[]{String.valueOf(audioId)}, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
//										if (cursor1 != null && cursor1.moveToFirst()) {
//											do {
//												final String mimeType = cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE));
//												final String name = cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE));
//												final String albumName = cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
//												final int musicId = cursor1.getInt(cursor1.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
//												final int size = (int) cursor1.getLong(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
//												final int duration = cursor1.getInt(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
//												final String artist = cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
//												final long addTime = cursor1.getLong(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));
//												final int albumId = cursor1.getInt(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
//												final String path = cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
//
//												final MusicItem.Builder builder = new MusicItem.Builder(musicId, name, path)
//														.musicAlbum(albumName)
//														.addTime((int) addTime)
//														.artist(artist)
//														.duration(duration)
//														.mimeName(mimeType)
//														.size(size)
//														.addAlbumId(albumId);
//												mMusicItemList.add(builder.build());
//											} while (cursor1.moveToNext());
//											cursor1.close();
//										}
//
//									} while (cursor.moveToNext());
//									cursor.close();
//								}
//
//								observableEmitter.onNext(0);
//							}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
//									.subscribe(i -> {
//										if (i != 0) {
//											return;
//										}
//										adapter = new MyRecyclerAdapter(ListViewActivity.this, mMusicItemList, new MyRecyclerAdapter.Config(0, true));
//										mRecyclerView.setAdapter(adapter);
//									});
//						}
//					} else {
//						Toast.makeText(this, "ID is null...", Toast.LENGTH_SHORT).show();
//					}
//				}
//				break;

				//点击播放列表中的一项
				case FragmentType.ACTION_PLAY_LIST_ITEM: {
					mToolbar.setTitle(getIntent().getStringExtra("play_list_name"));

					currentListName = getIntent().getStringExtra("play_list_name");

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
												.addAlbumId(albumId);
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
								Data.syncPlayOrderList(this, mMusicItemList);
								Data.shuffleOrderListSync(this, false);
								adapter = new MyRecyclerAdapter(ListViewActivity.this, mMusicItemList
										, new MyRecyclerAdapter.Config(preferences.getInt(Values.SharedPrefsTag
										.RECYCLER_VIEW_ITEM_STYLE, 0), true));
								mRecyclerView.setAdapter(adapter);
							});
				}
				break;

				case FragmentType.ACTION_HISTORY: {
					mToolbar.setTitle(getString(R.string.history));
					mMusicItemList.addAll(Data.sHistoryPlayed);

					Data.syncPlayOrderList(this, mMusicItemList);
					Data.shuffleOrderListSync(this, false);

					adapter = new MyRecyclerAdapter(ListViewActivity.this, mMusicItemList
							, new MyRecyclerAdapter.Config(preferences.getInt(Values.SharedPrefsTag
							.RECYCLER_VIEW_ITEM_STYLE, 0), false));
					mRecyclerView.setAdapter(adapter);
				}
				break;

				case FragmentType.ACTION_TRASH_CAN: {
					if (preferences.getBoolean(Values.SharedPrefsTag.TRASH_CAN_INFO, true)) {
						AlertDialog.Builder builder = new AlertDialog.Builder(this)
								.setTitle("About trash can")
								.setMessage("You can throw the music into the trash, so you won't hear the song recently unless you actively play it.")
								.setNegativeButton("OK", (dialog, which) -> {
									PreferenceUtil.getDefault(ListViewActivity.this).edit().putBoolean(Values.SharedPrefsTag.TRASH_CAN_INFO, false).apply();
									dialog.dismiss();
								});
						builder.show();
					}

					mToolbar.setTitle(getString(R.string.trash_can));
					mMusicItemList.addAll(Data.S_TRASH_CAN_LIST);

					Data.syncPlayOrderList(this, mMusicItemList);
					Data.shuffleOrderListSync(this, false);

					adapter = new MyRecyclerAdapter(ListViewActivity.this, mMusicItemList
							, new MyRecyclerAdapter.Config(preferences.getInt(Values.SharedPrefsTag
							.RECYCLER_VIEW_ITEM_STYLE, 0), false));
					mRecyclerView.setAdapter(adapter);

				}
				break;
				default:
			}

			initDone = true;
		}

	}

	@Override
	public boolean removeItem(MusicItem item) {
		return mMusicItemList.remove(item);
	}

	@Override
	public void inflateCommonMenu() {
		mToolbar.getMenu().clear();
		mToolbar.inflateMenu(R.menu.menu_public);
		mToolbar.setOnMenuItemClickListener(item -> {
			switch (item.getItemId()) {
				case R.id.menu_public_random: {
					ReceiverOnMusicPlay.startService(this, MusicService.ServiceActions.ACTION_FAST_SHUFFLE);
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

	@Override
	public void inflateChooseMenu() {
		mToolbar.getMenu().clear();
		mToolbar.inflateMenu(R.menu.menu_toolbar_main_choose);
		mToolbar.setOnMenuItemClickListener(item -> {
			switch (item.getItemId()) {
				case R.id.menu_toolbar_main_choose_share: {
					MusicUtil.sharMusic(ListViewActivity.this, adapter.getSelected());
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

		Data.sPlayOrderList.clear();
		Data.sPlayOrderList.addAll(Data.sPlayOrderListBackup);
		ReceiverOnMusicPlay.startService(this, MusicService.ServiceActions.ACTION_RESET_LIST);

		super.onDestroy();
	}

	@Override
	public void sendEmptyMessage(int what) {
		if (handler != null) handler.sendEmptyMessage(what);
	}

	@Override
	public void sendMessage(Message message) {
	}

	public interface FragmentType {
		String ACTION_ADD_RECENT = "add recent";
		String ACTION_FAVOURITE = "favourite music";
		String ACTION_HISTORY = "play history";
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

	public List<MusicItem> getMusicItemList() {
		return mMusicItemList;
	}

	public MyRecyclerAdapter getAdapter() {
		return adapter;
	}

	public static final class NotLeakHandler extends Handler {

		/**
		 * @see Message#what
		 */
		public static final int NOTIFICATION_ITEM_INSERT = 990;
		public static final int RELOAD = 991;

		@SuppressWarnings("unused")
		private WeakReference<ListViewActivity> mWeakReference;

		NotLeakHandler(ListViewActivity activity, Looper looper) {
			super(looper);
			mWeakReference = new WeakReference<>(activity);
		}

		@Override
		public final void handleMessage(Message msg) {
			switch (msg.what) {
				case NOTIFICATION_ITEM_INSERT: {
					// 只有在历史页面才更新
					// FIXME: 2019/5/26 crash: 播放列表中随机播放几次 然后退出进入MusicDetailFrag 快速点击下个 crash.
					try {
						if (mWeakReference.get().adapter != null && FragmentType.ACTION_HISTORY.equals(mWeakReference.get().mType)) {
							mWeakReference.get().adapter.notifyItemInserted(Data.sHistoryPlayed.size() - 1);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				case MessageWorker.RELOAD: {
					if (mWeakReference.get().adapter != null) mWeakReference.get().adapter.notifyDataSetChanged();
				}
				break;
			}
		}

	}
}
