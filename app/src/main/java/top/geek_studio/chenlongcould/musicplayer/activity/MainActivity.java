package top.geek_studio.chenlongcould.musicplayer.activity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.*;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.palette.graphics.Palette;
import androidx.viewpager.widget.ViewPager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.tabs.TabLayout;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.glide.transformations.BlurTransformation;
import org.litepal.LitePal;
import org.litepal.LitePalDB;
import top.geek_studio.chenlongcould.geeklibrary.DialogUtil;
import top.geek_studio.chenlongcould.geeklibrary.theme.IStyle;
import top.geek_studio.chenlongcould.geeklibrary.theme.Theme;
import top.geek_studio.chenlongcould.geeklibrary.theme.ThemeStore;
import top.geek_studio.chenlongcould.geeklibrary.theme.ThemeUtils;
import top.geek_studio.chenlongcould.musicplayer.*;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyPagerAdapter;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter2AlbumList;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter2ArtistList;
import top.geek_studio.chenlongcould.musicplayer.broadcast.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.database.MyBlackPath;
import top.geek_studio.chenlongcould.musicplayer.databinding.ActivityMainBinding;
import top.geek_studio.chenlongcould.musicplayer.fragment.*;
import top.geek_studio.chenlongcould.musicplayer.model.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.model.ArtistItem;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.threadPool.AlbumThreadPool;
import top.geek_studio.chenlongcould.musicplayer.threadPool.ArtistThreadPool;
import top.geek_studio.chenlongcould.musicplayer.threadPool.CustomThreadPool;
import top.geek_studio.chenlongcould.musicplayer.threadPool.ItemCoverThreadPool;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

/**
 * @author chenlongcould
 */
public final class MainActivity extends BaseCompatActivity implements IStyle {

	public static final String TAG = "MainActivity";

	public static final char MUSIC_DETAIL_FRAGMENT_ID = '0';
	public static final char MUSIC_LIST_FRAGMENT_ID = '1';
	public static final char ALBUM_LIST_FRAGMENT_ID = '2';
	public static final char ARTIST_LIST_FRAGMENT_ID = '3';
	public static final char PLAYT_LIST_FRAGMENT_ID = '4';
	public static final char FILE_LIST_FRAGMENT_ID = '5';

	/**
	 * 1 is MUSIC TAB
	 * 2 is ALBUM TAB
	 * 3 is ARTIST TAB
	 * 4 is PLAYLIST TAB
	 * 5 is FILE MANAGER TAB
	 * default tab order is: 12345
	 */
	public static final String DEFAULT_TAB_ORDER = "12345";

	/**
	 * 检测当前 slide 的位置.
	 * 当滑动 slide {@link SlidingUpPanelLayout} 时, 迅速点击可滑动区域外, slide 会卡住.
	 * 但 slide 状态会变为 {@link SlidingUpPanelLayout.PanelState#COLLAPSED}.
	 * 故立此 FLAG.
	 */
	public static float CURRENT_SLIDE_OFFSET = 1;

	public static boolean ANIMATION_FLAG = true;
	public static boolean NEED_RELOAD = false;
	private final ArrayList<String> mTitles = new ArrayList<>();
	private boolean toolbarClicked = false;
	private boolean backPressed = false;
	/**
	 * ALL FRAGMENTS
	 */
	private List<BaseFragment> mFragmentList = new ArrayList<>();

	private MusicDetailFragment musicDetailFragment;
	private MusicListFragment musicListFragment;
	private AlbumListFragment albumListFragment;
	private ArtistListFragment artistListFragment;
	private FileViewFragment fileViewFragment;
	private PlayListFragment playListFragment;

	/**
	 * handler
	 */
	public static NotLeakHandler mHandler = null;

	private ActivityMainBinding mMainBinding;
	private ActionBarDrawerToggle mDrawerToggle;
	private MyPagerAdapter mPagerAdapter;
	private ImageView mNavHeaderImageView;

	@SuppressWarnings("FieldCanBeLocal")
	private MaterialSearchView mSearchView;

	/**
	 * ----------------- fragment(s) ----------------------
	 */
	private HandlerThread mHandlerThread;

	private AlertDialog load;

	/**
	 * onXXX
	 * At Override
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Data.init(this);

		mMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
		initView();
		super.onCreate(savedInstanceState);

		mHandlerThread = new HandlerThread("Handler Thread in MainActivity");
		mHandlerThread.start();
		mHandler = new NotLeakHandler(this, mHandlerThread.getLooper());

		//监听耳机(有线或无线)的插拔动作, 拔出暂停音乐
		final IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
		registerReceiver(Data.mMyHeadSetPlugReceiver, intentFilter);

		loadData();

	}

	/**
	 * clearData data
	 */
	public static void clearData() {

		//lists
		Data.sPlayOrderList.clear();
		Data.sMusicItems.clear();
		Data.sAlbumItems.clear();
		Data.sArtistItems.clear();
		Data.sMusicItemsBackUp.clear();
		Data.sAlbumItemsBackUp.clear();
		Data.sArtistItemsBackUp.clear();

		Data.S_HISTORY_PLAY.clear();
		Data.S_TRASH_CAN_LIST.clear();

		App.clearDisposable();

		if (Data.getCurrentCover() != null && !Data.getCurrentCover().isRecycled()) {
			Data.getCurrentCover().recycle();
		}
	}

	@Override
	protected void onDestroy() {
		GlideApp.get(this).clearMemory();

		try {
			unbindService(Data.sServiceConnection);
		} catch (Exception e) {
			Log.d(TAG, "onDestroy: " + e.getMessage());
		}

		try {
			unregisterReceiver(Data.mMyHeadSetPlugReceiver);
		} catch (Exception e) {
			Log.d(TAG, "onDestroy: " + e.getMessage());
		}

		Data.HAS_BIND = false;
		Data.sActivities.clear();
		if (Data.sMainRef != null) {
			Data.sMainRef.clear();
		}

		Data.sTheme = null;

		mHandlerThread.quit();
		mFragmentList.clear();

		AlbumThreadPool.finish();
		ItemCoverThreadPool.finish();
		ArtistThreadPool.finish();
		CustomThreadPool.finish();

		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (musicListFragment != null) {
			musicListFragment.getMusicListBinding().includeRecycler.recyclerView.scheduleLayoutAnimation();
		}

		//数据重载
		if (NEED_RELOAD) {
			mFragmentList.clear();
			mTitles.clear();
			mMainBinding.tabLayout.removeAllTabs();
			Data.sMusicItems.clear();
			Data.sPlayOrderList.clear();
			Data.sMusicItemsBackUp.clear();
			Data.sAlbumItemsBackUp.clear();
			Data.sAlbumItems.clear();
			Data.sArtistItems.clear();

			loadData();
		}

	}

	@Override
	public final void onBackPressed() {

		//1
		if (mMainBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
			mMainBinding.drawerLayout.closeDrawers();
			return;
		}

		if (mSearchView.isSearchOpen()) {
			mSearchView.closeSearch();
			return;
		}

		//2
		if (musicDetailFragment.getSlidingUpPanelLayout() != null
				&& musicDetailFragment.getSlidingUpPanelLayout().getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
			musicDetailFragment.getSlidingUpPanelLayout().setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
			return;
		}

		//3
		if (mMainBinding.slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
			mMainBinding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
			return;
		}

		final File file = fileViewFragment.getCurrentFile();
		if (file != null && !file.getPath().equals(Environment.getExternalStorageDirectory().getPath())) {
			fileViewFragment.onBackPressed();
			return;
		}

		//4
		if (backPressed) {
			finish();
		} else {
			backPressed = true;
			Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();
			new Handler().postDelayed(() -> backPressed = false, 2000);
		}

	}

	@Override
	public String getActivityTAG() {
		return TAG;
	}

	@Override
	public void inflateCommonMenu() {
		final Toolbar toolbar = mMainBinding.toolBar;
		toolbar.getMenu().clear();
		toolbar.inflateMenu(R.menu.menu_toolbar_main_common);
		final Menu menu = mMainBinding.toolBar.getMenu();

		final MenuItem searchItem = menu.findItem(R.id.menu_toolbar_search);
		mSearchView.setMenuItem(searchItem);

		toolbar.setOnMenuItemClickListener(item -> {
			final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();

			switch (item.getItemId()) {
				case R.id.menu_toolbar_exit: {
					fullExit();
				}
				break;

				/*--------------- 快速 随机 播放 ----------------*/
				case R.id.menu_toolbar_fast_play: {
					//just fast random play, without change Data.sPlayOrderList
					Utils.SendSomeThing.sendPlay(MainActivity.this, ReceiverOnMusicPlay.CASE_TYPE_SHUFFLE, TAG);
				}
				break;

				case R.id.menu_toolbar_album_linear: {
					editor.putInt(Values.SharedPrefsTag.ALBUM_LIST_DISPLAY_TYPE, MyRecyclerAdapter2AlbumList.LINEAR_TYPE);
					editor.apply();
					mPagerAdapter.notifyDataSetChanged();
					albumListFragment.setRecyclerViewData();
				}
				break;

				case R.id.menu_toolbar_album_grid: {
					editor.putInt(Values.SharedPrefsTag.ALBUM_LIST_DISPLAY_TYPE, MyRecyclerAdapter2AlbumList.GRID_TYPE);
					editor.apply();
					mPagerAdapter.notifyDataSetChanged();

					albumListFragment.setRecyclerViewData();
				}
				break;

				case R.id.menu_toolbar_artist_linear: {
					editor.putInt(Values.SharedPrefsTag.ARTIST_LIST_DISPLAY_TYPE, MyRecyclerAdapter2ArtistList.LINEAR_TYPE);
					editor.apply();
					mPagerAdapter.notifyDataSetChanged();
					artistListFragment.setRecyclerViewData();
				}
				break;

				case R.id.menu_toolbar_artist_grid: {
					editor.putInt(Values.SharedPrefsTag.ARTIST_LIST_DISPLAY_TYPE, MyRecyclerAdapter2ArtistList.GRID_TYPE);
					editor.apply();
					mPagerAdapter.notifyDataSetChanged();

					artistListFragment.setRecyclerViewData();
				}
				break;

				case R.id.menu_toolbar_reload: {
					reload();
				}
				break;
				default:
			}
			return true;
		});
	}

	/**
	 * reload data, like first enter app
	 */
	private void reload() {
		mFragmentList.clear();
		mTitles.clear();
		mMainBinding.tabLayout.removeAllTabs();
		Data.sMusicItems.clear();
		Data.sPlayOrderList.clear();
		Data.sMusicItemsBackUp.clear();
		Data.sAlbumItemsBackUp.clear();
		Data.sAlbumItems.clear();

		loadData();
	}

	/**
	 * use this
	 */
	@SuppressLint("StaticFieldLeak")
	@Override
	public void inflateChooseMenu() {
		mMainBinding.toolBar.getMenu().clear();
		mMainBinding.toolBar.inflateMenu(R.menu.menu_toolbar_main_choose);
		mMainBinding.toolBar.setOnMenuItemClickListener(menuItem -> {
			switch (menuItem.getItemId()) {
				case R.id.menu_toolbar_main_choose_addlist: {
					ArrayList<MusicItem> helper = new ArrayList<>();

					for (MusicItem item : Data.sMusicItems) {
						for (int id : musicListFragment.getAdapter().getSelected()) {
							if (id == item.getMusicID()) {
								helper.add(item);
							}
						}
					}

					Utils.DataSet.addListDialog(MainActivity.this, helper);
					musicListFragment.getAdapter().clearSelection();
				}
				break;
				case R.id.menu_toolbar_main_choose_share: {
					new AsyncTask<Void, Void, Void>() {

						@Override
						protected Void doInBackground(Void... voids) {
							Intent intent = new Intent(Intent.ACTION_SEND);
							intent.setType("text/plain");
							StringBuilder content = new StringBuilder(getResources().getString(R.string.app_name))
									.append("\r\n")
									.append("https://www.coolapk.com/apk/top.geek_studio.chenlongcould.musicplayer.Common")
									.append("\r\n");

							for (final MusicItem item : Data.sMusicItems) {
								for (int id : musicListFragment.getAdapter().getSelected()) {
									if (id == item.getMusicID()) {
										content.append(item.getMusicName()).append("\r\n");
										break;
									}
								}
							}

							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.putExtra(Intent.EXTRA_TEXT, content.toString());
							startActivity(intent);
							return null;
						}
					}.execute();
				}
				break;
				default:
			}
			return true;
		});
	}

	private void loadData() {
		load = DialogUtil.getLoadingDialog(this, "Loading...");
		load.show();

		Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
			final LitePalDB blackList = new LitePalDB("BlackList", 1);
			blackList.addClassName(MyBlackPath.class.getName());
			LitePal.use(blackList);

			ArrayList<String> data = new ArrayList<>();
			List<MyBlackPath> lists = LitePal.findAll(MyBlackPath.class);
			for (MyBlackPath path : lists) {
				data.add(path.getDirPath());
			}
			LitePal.useDefault();

			if (Data.sMusicItems.isEmpty()) {
				/*---------------------- init Data!!!! -------------------*/
				final Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
				if (cursor != null && cursor.moveToFirst()) {
					//没有歌曲直接退出app
					if (cursor.getCount() == 0) {
						emitter.onNext(-1);
					} else {

						final boolean skipShort = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Values.SharedPrefsTag.HIDE_SHORT_SONG, true);

						do {
							final String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
							boolean skip = false;
							for (String bl : data) {
								if (path.contains(bl)) {
									skip = true;
									break;
								}
							}
							if (skip) {
								continue;
							}

							final int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
							if (duration <= 0) {
								Log.d(TAG, "onCreate: the music-file duration is " + duration + ", skip...");
								continue;
							}

							if (skipShort && duration < 20) {
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
									.addTime((int) addTime)
									.artist(artist)
									.duration(duration)
									.mimeName(mimeType)
									.size(size)
									.addAlbumId(albumId)
									.addArtistId(artistId);

							if (Data.sCurrentMusicItem.getMusicID() == -1 && PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getInt(Values.SharedPrefsTag.LAST_PLAY_MUSIC_ID, -99) == id) {
								Data.sCurrentMusicItem = builder.build();
								Log.d(TAG, "onNext: the last data: name: " + Data.sCurrentMusicItem.getMusicName());
							}

							Data.sMusicItems.add(builder.build());
							Data.sMusicItemsBackUp.add(builder.build());
							Data.sPlayOrderList.add(builder.build());
						}
						while (cursor.moveToNext());
						NEED_RELOAD = false;
						Log.i(Values.TAG_UNIVERSAL_ONE, "onCreate: The MusicData load done.");
						cursor.close();

						//sync
						DBArtSync.startActionSyncAlbum(this);
						DBArtSync.startActionSyncArtist(this);

						if (PreferenceManager.getDefaultSharedPreferences(this).getString(Values.SharedPrefsTag.ORDER_TYPE, Values.TYPE_COMMON).equals(Values.TYPE_RANDOM)) {
							Collections.shuffle(Data.sPlayOrderList);
						}

						emitter.onNext(0);
					}
				} else {
					//cursor null or getCount == 0
					emitter.onNext(-1);
				}
			} else {
				//already has data -> initFragment
				emitter.onNext(0);
			}

		}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).safeSubscribe(new Observer<Integer>() {
			@Override
			public final void onSubscribe(Disposable disposable) {
				Data.sDisposables.add(disposable);
			}

			@Override
			public final void onNext(Integer result) {
				load.dismiss();

				if (result == -1) {
//					Utils.Ui.fastToast(MainActivity.this, "cursor is null or moveToFirst Fail");
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setTitle("Error")
							.setMessage("Can not find any music or the cursor is null, Will exit.")
							.setCancelable(false)
							.setNegativeButton("Exit", (dialog, which) -> {
								dialog.cancel();
								fullExit();
//								mHandler.postDelayed(MainActivity.this::fullExit, 1000);
							});
					builder.show();
					return;
				}

				if (getIntent().getStringExtra(Values.IntentTAG.SHORTCUT_TYPE) != null) {
					switch (getIntent().getStringExtra("SHORTCUT_TYPE")) {
						case App.SHORTCUT_RANDOM: {
							Utils.SendSomeThing.sendPlay(MainActivity.this, ReceiverOnMusicPlay.CASE_TYPE_SHUFFLE, null);
						}
						break;
						default:
							break;
					}
				}

				initFragmentData();
				setSubtitle(Data.sPlayOrderList.size() + " Songs");

				//service
				final Intent intent = new Intent(MainActivity.this, MusicService.class);
				startService(intent);
				Data.HAS_BIND = bindService(intent, Data.sServiceConnection, BIND_AUTO_CREATE);
			}

			@Override
			public final void onError(Throwable throwable) {
				load.dismiss();
				Toast.makeText(MainActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
				fullExit();
			}

			@Override
			public final void onComplete() {
				load.dismiss();
			}
		});

	}

	/**
	 * 根据输入框中的值来过滤数据并更新RecyclerView
	 *
	 * @param filterStr fileName
	 */
	private void filterData(String filterStr) {
		final String tabOrder = PreferenceManager.getDefaultSharedPreferences(this).getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER);

		if (tabOrder.charAt(Values.CurrentData.CURRENT_PAGE_INDEX) == MUSIC_LIST_FRAGMENT_ID) {
			if (TextUtils.isEmpty(filterStr)) {
				Data.sMusicItems.clear();
				Data.sMusicItems.addAll(Data.sMusicItemsBackUp);
				musicListFragment.getAdapter().notifyDataSetChanged();
			} else {
				Data.sMusicItems.clear();

				//algorithm
				for (MusicItem item : Data.sMusicItemsBackUp) {
					final String name = item.getMusicName();
					if (name.contains(filterStr.toLowerCase()) || name.contains(filterStr.toUpperCase())) {
						Data.sMusicItems.add(item);
					}
				}
				musicListFragment.getAdapter().notifyDataSetChanged();
			}

		}

		if (tabOrder.charAt(Values.CurrentData.CURRENT_PAGE_INDEX) == '2') {
			if (TextUtils.isEmpty(filterStr)) {
				Data.sAlbumItems.clear();
				Data.sAlbumItems.addAll(Data.sAlbumItemsBackUp);
				albumListFragment.getAdapter2AlbumList().notifyDataSetChanged();
			} else {
				Data.sAlbumItems.clear();

				//algorithm
				for (AlbumItem item : Data.sAlbumItemsBackUp) {
					final String name = item.getAlbumName();
					if (name.contains(filterStr.toLowerCase()) || name.contains(filterStr.toUpperCase())) {
						Data.sAlbumItems.add(item);
					}
				}

				albumListFragment.getAdapter2AlbumList().notifyDataSetChanged();
			}
		}

		//artist
		if (tabOrder.charAt(Values.CurrentData.CURRENT_PAGE_INDEX) == '3') {
			if (TextUtils.isEmpty(filterStr)) {
				Data.sArtistItems.clear();
				Data.sArtistItems.addAll(Data.sArtistItemsBackUp);
				artistListFragment.getAdapter2ArtistList().notifyDataSetChanged();
			} else {
				Data.sArtistItems.clear();

				//algorithm
				for (ArtistItem item : Data.sArtistItemsBackUp) {
					String name = item.getArtistName();
					if (name.contains(filterStr.toLowerCase()) || name.contains(filterStr.toUpperCase())) {
						Data.sArtistItems.add(item);
					}
				}

				artistListFragment.getAdapter2ArtistList().notifyDataSetChanged();
			}
		}
	}

	private void setTabLongClickListener() {
		for (int i = 0; i < mMainBinding.tabLayout.getTabCount(); i++) {
			TabLayout.Tab tab = mMainBinding.tabLayout.getTabAt(i);
			if (tab != null) {
				if (tab.view != null) {
					int finalI = i;
					((LinearLayout) tab.view).setOnLongClickListener(v -> {
						int pos = tab.getPosition();

						AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
						builder.setTitle(getString(R.string.are_u_sure));
						builder.setMessage(getString(R.string.close_the_tab_x_int, tab.getText()));
						builder.setCancelable(true);
						builder.setPositiveButton(getString(R.string.sure), (dialog, which) -> {
							final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
							final SharedPreferences.Editor editor = preferences.edit();
							final StringBuilder currentOrder = new StringBuilder(preferences.getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER));
							currentOrder.deleteCharAt(pos);
							editor.putString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, currentOrder.toString());

							boolean result = editor.commit();

							if (result) {
								mTitles.remove(pos);
								mFragmentList.remove(pos);
								mPagerAdapter.notifyDataSetChanged();
								mMainBinding.tabLayout.removeTabAt(finalI);

								mMainBinding.viewPager.setOffscreenPageLimit(mTitles.size() > 1 ? mTitles.size() - 1 : 1);
							}

							dialog.dismiss();
						});
						builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
						builder.show();
						return true;
					});
				}
			}
		}
	}

	/**
	 * init fragments
	 */
	private void initFragmentData() {

		final String tabOrder = PreferenceManager.getDefaultSharedPreferences(this).getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER);

		assert tabOrder != null;

		for (char c : tabOrder.toCharArray()) {
			if (c == '1') {
				final String tab1 = getResources().getString(R.string.music);
				mTitles.add(tab1);
				musicListFragment = MusicListFragment.newInstance();
				mFragmentList.add(musicListFragment);
				mMainBinding.tabLayout.addTab(mMainBinding.tabLayout.newTab().setText(tab1));
			}

			if (c == '2') {
				final String tab2 = getResources().getString(R.string.album);
				mTitles.add(tab2);
				albumListFragment = AlbumListFragment.newInstance();
				mFragmentList.add(albumListFragment);
				mMainBinding.tabLayout.addTab(mMainBinding.tabLayout.newTab().setText(tab2));
			}

			if (c == '3') {
				final String tab3 = getResources().getString(R.string.artist);
				mTitles.add(tab3);
				artistListFragment = ArtistListFragment.newInstance();
				mFragmentList.add(artistListFragment);
				mMainBinding.tabLayout.addTab(mMainBinding.tabLayout.newTab().setText(tab3));
			}

			if (c == '4') {
				final String tab4 = getResources().getString(R.string.play_list);
				mTitles.add(tab4);
				playListFragment = PlayListFragment.newInstance();
				mFragmentList.add(playListFragment);
				mMainBinding.tabLayout.addTab(mMainBinding.tabLayout.newTab().setText(tab4));
			}

			if (c == '5') {
				final String tab5 = getResources().getString(R.string.tab_file);
				mTitles.add(tab5);
				fileViewFragment = FileViewFragment.newInstance();
				mFragmentList.add(fileViewFragment);
				mMainBinding.tabLayout.addTab(mMainBinding.tabLayout.newTab().setText(tab5));
			}
		}

		musicDetailFragment = MusicDetailFragment.newInstance();
		final FragmentManager fragmentManager = getSupportFragmentManager();
		final FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.replace(R.id.frame_wait, musicDetailFragment);
		transaction.commit();

		Values.CurrentData.CURRENT_PAGE_INDEX = 0;

		mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), mFragmentList, mTitles);
		mMainBinding.viewPager.setAdapter(mPagerAdapter);
		mMainBinding.viewPager.setOffscreenPageLimit(mTitles.size() > 1 ? mTitles.size() - 1 : 1);

		inflateCommonMenu();

		//hide
		getMenu().findItem(R.id.menu_toolbar_album_layout).setVisible(false);
		getMenu().findItem(R.id.menu_toolbar_artist_layout).setVisible(false);

		setTabLongClickListener();

		final int position = mMainBinding.tabLayout.getTabCount();

		mMainBinding.tabLayout.addTab(mMainBinding.tabLayout.newTab().setIcon(R.drawable.ic_add_white_24dp), position);
		TabLayout.Tab addTab = mMainBinding.tabLayout.getTabAt(position);
		if (addTab != null) {
			if (addTab.view != null) {
				addTab.setCustomView(addTab.getCustomView());
				PopupMenu popupMenu = new PopupMenu(this, addTab.view);
				popupMenu.getMenu().add(Menu.NONE, Menu.FIRST, 0, getString(R.string.tab_music));
				popupMenu.getMenu().add(Menu.NONE, Menu.FIRST + 1, 0, getString(R.string.tab_album));
				popupMenu.getMenu().add(Menu.NONE, Menu.FIRST + 2, 0, getString(R.string.tab_artist));
				popupMenu.getMenu().add(Menu.NONE, Menu.FIRST + 3, 0, getString(R.string.tab_playlist));
				popupMenu.getMenu().add(Menu.NONE, Menu.FIRST + 4, 0, getString(R.string.tab_fileviewer));

				popupMenu.setOnMenuItemClickListener(item -> {

					boolean added = false;

					String typeAdded = "0";
					int befAdd = mMainBinding.tabLayout.getTabCount();

					switch (item.getItemId()) {

						case Menu.FIRST: {
							if (!PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER).contains("1")) {
								final String tab1 = getResources().getString(R.string.music);
								mTitles.add(tab1);
								mFragmentList.add(MusicListFragment.newInstance());
								mMainBinding.tabLayout.addTab(mMainBinding.tabLayout.newTab().setText(tab1), mMainBinding.tabLayout.getTabCount() - 1);
								added = true;
								typeAdded = "1";
							}
						}
						break;

						case Menu.FIRST + 1: {
							if (!PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER).contains("2")) {
								final String tab2 = getResources().getString(R.string.album);
								mTitles.add(tab2);
								mFragmentList.add(AlbumListFragment.newInstance());
								mMainBinding.tabLayout.addTab(mMainBinding.tabLayout.newTab().setText(tab2), mMainBinding.tabLayout.getTabCount() - 1);
								added = true;
								typeAdded = "2";
							}
						}
						break;

						case Menu.FIRST + 2: {
							if (!PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER).contains("3")) {
								final String tab3 = getResources().getString(R.string.artist);
								mTitles.add(tab3);
								mFragmentList.add(ArtistListFragment.newInstance());
								mMainBinding.tabLayout.addTab(mMainBinding.tabLayout.newTab().setText(tab3), mMainBinding.tabLayout.getTabCount() - 1);
								added = true;
								typeAdded = "3";
							}
						}
						break;

						case Menu.FIRST + 3: {
							if (!PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER).contains("4")) {
								final String tab4 = getResources().getString(R.string.play_list);
								mTitles.add(tab4);
								mFragmentList.add(PlayListFragment.newInstance());
								mMainBinding.tabLayout.addTab(mMainBinding.tabLayout.newTab().setText(tab4), mMainBinding.tabLayout.getTabCount() - 1);
								added = true;
								typeAdded = "4";
							}
						}
						break;

						case Menu.FIRST + 4: {
							if (!PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER).contains("5")) {
								final String tab5 = getResources().getString(R.string.tab_file);
								mTitles.add(tab5);
								mFragmentList.add(FileViewFragment.newInstance());
								mMainBinding.tabLayout.addTab(mMainBinding.tabLayout.newTab().setText(tab5), mMainBinding.tabLayout.getTabCount() - 1);
								added = true;
								typeAdded = "5";
							}
						}
						break;

						default:

					}

					if (!added) {
						Toast.makeText(MainActivity.this, getString(R.string.already_exsits), Toast.LENGTH_SHORT).show();
					} else {
						SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
						editor.putString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER) + typeAdded);
						editor.apply();
						mPagerAdapter.notifyDataSetChanged();

						//添加tab 后重设监听
						mMainBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
							@Override
							public void onTabSelected(TabLayout.Tab tab) {
								//点击加号不会滑动ViewPager
								if (tab.getPosition() != mMainBinding.tabLayout.getTabCount() - 1) {
									mMainBinding.viewPager.setCurrentItem(tab.getPosition(), true);
								}
							}

							@Override
							public void onTabUnselected(TabLayout.Tab tab) {

							}

							@Override
							public void onTabReselected(TabLayout.Tab tab) {

							}
						});

						setTabLongClickListener();

						//重设缓存
						mMainBinding.viewPager.setOffscreenPageLimit(mTitles.size() > 1 ? mTitles.size() - 1 : 1);

						//滚动
						mMainBinding.viewPager.setCurrentItem(befAdd - 1);

					}
					return true;
				});

				((LinearLayout) addTab.view).setOnClickListener(v -> popupMenu.show());
			}
		}

		mMainBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int i, float v, int i1) {

			}

			@Override
			public void onPageSelected(int i) {
				Values.CurrentData.CURRENT_PAGE_INDEX = i;

				TabLayout.Tab tab = mMainBinding.tabLayout.getTabAt(i);
				if (tab != null) {
					tab.select();
				}

				String order = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER);
				if (order.charAt(i) == '1') {
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_album_layout), false);
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_artist_layout), false);
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_search), true);
					setSubtitle(Data.sMusicItems.size() + " Songs");
				}

				if (order.charAt(i) == '2') {
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_album_layout), true);
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_artist_layout), false);
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_search), true);
					setSubtitle(Data.sAlbumItems.size() + " Albums");
				}

				if (order.charAt(i) == '3') {
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_album_layout), false);
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_artist_layout), true);
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_search), true);
					setSubtitle(Data.sArtistItems.size() + " Artists");
				}

				//playlist
				if (order.charAt(i) == '4') {
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_album_layout), false);
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_artist_layout), false);
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_search), false);
					setSubtitle(Data.sPlayListItems.size() + " Playlists");
				}

				//fileviewer
				if (order.charAt(i) == '5') {
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_album_layout), false);
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_artist_layout), false);
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_search), false);
				}
			}

			@Override
			public void onPageScrollStateChanged(int i) {

			}
		});

		mMainBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				//点击加号不会滑动ViewPager
				if (tab.getPosition() != mMainBinding.tabLayout.getTabCount() - 1) {
					mMainBinding.viewPager.setCurrentItem(tab.getPosition(), true);
				}
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {

			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});

	}

	public void fullExit() {
		try {
			unbindService(Data.sServiceConnection);
		} catch (Exception e) {
			Log.d(TAG, "fullExit: " + e.getMessage());
		}
		try {
			stopService(new Intent(MainActivity.this, MusicService.class));
		} catch (Exception e) {
			Log.d(TAG, "fullExit: " + e.getMessage());
		}
		Data.sMusicBinder = null;
		clearData();
		finish();
	}

	@Override
	public final void initStyle() {
		mDrawerToggle.getDrawerArrowDrawable().setColor(Utils.Ui.getTitleColor(MainActivity.this));
		setTaskDescription(new ActivityManager.TaskDescription((String) getTitle(), null, Utils.Ui.getPrimaryColor(MainActivity.this)));
		mMainBinding.tabLayout.setTabTextColors(ColorStateList.valueOf(Utils.Ui.getTitleColor(MainActivity.this)));
		mMainBinding.tabLayout.setSelectedTabIndicatorColor(Utils.Ui.getAccentColor(MainActivity.this));
		Utils.Ui.setOverToolbarColor(mMainBinding.toolBar, Utils.Ui.getTitleColor(MainActivity.this));

		getWindow().setNavigationBarColor(Utils.Ui.getPrimaryDarkColor(this));

		Observable.create((ObservableOnSubscribe<Theme>) emitter -> {
			final String themeId = PreferenceManager.getDefaultSharedPreferences(this).getString(Values.SharedPrefsTag.SELECT_THEME, ThemeActivity.DEFAULT_THEME);
			if (themeId != null && !"null".equals(themeId)) {
				final File themeFile = ThemeUtils.getThemeFile(this, themeId);
				Data.sTheme = ThemeUtils.fileToTheme(themeFile);
			} else {
				Data.sTheme = null;
			}

			emitter.onNext(Data.sTheme);
			emitter.onComplete();
		}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Theme>() {
			@Override
			public final void onSubscribe(Disposable disposable) {

			}

			@Override
			public final void onNext(Theme theme) {

				mMainBinding.styleNav.setVisibility(View.VISIBLE);
				mMainBinding.styleTextNavTitle.setText(theme.getTitle());
				mMainBinding.styleTextNavName.setText(theme.getNav_name());

				for (String area : theme.getSelect().split(",")) {

					//检测是否匹配到NAV
					if (area.contains(ThemeStore.SupportArea.NAV)) {

						String bgPath = theme.getPath() + File.separatorChar + ThemeStore.DIR_IMG_NAV + File.separatorChar + area;

						GlideApp.with(MainActivity.this)
								.load(bgPath)
								.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
								.diskCacheStrategy(DiskCacheStrategy.NONE)
								.into(mMainBinding.styleImgNav);
					}

					//检测是否匹配到BG
					if (area.contains(ThemeStore.SupportArea.BG)) {

						final String bgPath = ThemeUtils.getBgFileByName(theme, area);

						mMainBinding.toolBar.setBackgroundColor(Color.TRANSPARENT);
						mMainBinding.tabLayout.setBackgroundColor(Color.TRANSPARENT);
						mMainBinding.appbar.setBackgroundColor(Color.TRANSPARENT);

						GlideApp.with(MainActivity.this).load(bgPath)
								.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
								.apply(bitmapTransform(new BlurTransformation(10, 10)))
								.into(mMainBinding.bgImage);

						setStatusBarTextColor(MainActivity.this, new Palette.Builder(Utils.Ui.readBitmapFromFile(bgPath, 50, 50))
								.generate().getVibrantColor(Utils.Ui.getPrimaryColor(MainActivity.this)));
					}
				}
			}

			@Override
			public final void onError(Throwable throwable) {
				Log.d(TAG, "onError: " + throwable.getMessage());

				//theme is null, call onError

				mMainBinding.styleNav.setVisibility(View.INVISIBLE);

				GlideApp.with(mMainBinding.bgImage).clear(mMainBinding.bgImage);

				@ColorInt int color = Utils.Ui.getPrimaryColor(MainActivity.this);
				setStatusBarTextColor(MainActivity.this, color);
				mMainBinding.tabLayout.setBackgroundColor(color);
				mMainBinding.appbar.setBackgroundColor(color);
				mMainBinding.toolBar.setBackgroundColor(color);
			}

			@Override
			public final void onComplete() {
			}
		});
	}

	/**
	 * setUp status bright or dark by a bitmap
	 */
	@ColorInt
	private int setUpStatus(String bgPath) {
		final int[] color = new int[1];
		final Bitmap bitmap = Utils.Ui.readBitmapFromFile(bgPath, 50, 50);
		if (bitmap != null) {
			//color set (album tag)
			Palette.from(bitmap).generate(p -> {
				if (p != null) {
					color[0] = p.getVibrantColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
					setStatusBarTextColor(MainActivity.this, color[0]);
					bitmap.recycle();
				}
			});
		}
		return color[0];
	}

	protected void initView() {
		super.initView(mMainBinding.toolBar, mMainBinding.appbar);

		mSearchView = findViewById(R.id.search_view);
		mSearchView.setHint(getString(R.string.type_somthing));
		mSearchView.setHintTextColor(Color.BLACK);
		mSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				musicListFragment.getMusicListBinding().includeRecycler.recyclerView.stopScroll();
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				filterData(newText);
				return true;
			}
		});

//        根据recycler view的滚动程度, 来判断如何返回顶部
		mMainBinding.toolBar.setOnClickListener(v -> {
			if (toolbarClicked) {
				switch (Values.CurrentData.CURRENT_PAGE_INDEX) {
					case 0: {
						musicListFragment.getMusicListBinding().includeRecycler.recyclerView.smoothScrollToPosition(0);
					}
					break;
					case 1: {
						albumListFragment.getRecyclerView().smoothScrollToPosition(0);
					}
					break;
					default:
				}

			}
			toolbarClicked = true;

			//双击机制
			new Handler().postDelayed(() -> toolbarClicked = false, 1000);
		});

		setSupportActionBar(mMainBinding.toolBar);

		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_24px);
		}

		mNavHeaderImageView = mMainBinding.navigationView.getHeaderView(0).findViewById(R.id.nav_view_image);

		mNavHeaderImageView.setOnClickListener(v -> {
			if (musicDetailFragment.getSlidingUpPanelLayout().getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
				musicDetailFragment.getSlidingUpPanelLayout().setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
				return;
			}

			if (mMainBinding.slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
				mMainBinding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
				return;
			}

			mMainBinding.drawerLayout.closeDrawers();
		});

		mMainBinding.slidingLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
			@Override
			public void onPanelSlide(View panel, float slideOffset) {

				musicDetailFragment.getHandler().sendEmptyMessage(MusicDetailFragment.NotLeakHandler.RECYCLER_SCROLL);

				CURRENT_SLIDE_OFFSET = slideOffset;

				mMainBinding.mainBody.setTranslationY(0 - slideOffset * 120);

				float current = 1 - slideOffset;
				musicDetailFragment.getNowPlayingBody().setAlpha(current);

				//在底部, 重置动画初始位置
				if (current == 1) {
					musicDetailFragment.setDefAnimation();
					musicDetailFragment.clearAnimations();
					ANIMATION_FLAG = true;
				}

				if (current == 0) {
					//隐藏BODY
					musicDetailFragment.getNowPlayingBody().setVisibility(View.INVISIBLE);

					//start animation
					if (ANIMATION_FLAG) {
						ANIMATION_FLAG = false;
						musicDetailFragment.initAnimation();
					}

				} else {
					mMainBinding.tabLayout.setVisibility(View.VISIBLE);
					mMainBinding.tabLayout.setVisibility(View.VISIBLE);
					musicDetailFragment.getNowPlayingBody().setVisibility(View.VISIBLE);
				}
			}

			@Override
			public final void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
				if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
					mMainBinding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
				} else if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
					mMainBinding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
				}
			}
		});

		//First no music no slide
		if (Data.sMusicBinder != null) {
			if (Data.HAS_PLAYED) {
				mMainBinding.slidingLayout.setTouchEnabled(true);
			} else {
				mMainBinding.slidingLayout.setTouchEnabled(false);
			}
		} else {
			mMainBinding.slidingLayout.setTouchEnabled(false);
		}

		mMainBinding.navigationView.setNavigationItemSelectedListener(menuItem -> {
			switch (menuItem.getItemId()) {
				case R.id.menu_nav_exit: {
					fullExit();
				}
				break;
				case R.id.menu_nav_detail_info: {
					Intent intent = new Intent(MainActivity.this, DetailActivity.class);
					startActivity(intent);
				}
				break;
				case R.id.menu_nav_setting: {
					Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
					startActivity(intent);
				}
				break;
				case R.id.menu_nav_about: {
					Intent intent = new Intent(MainActivity.this, AboutActivity.class);
					startActivity(intent);
				}
				break;
				case R.id.car_mode: {
					if (Data.HAS_PLAYED) {
						startActivity(new Intent(MainActivity.this, CarViewActivity.class));
					} else {
						Toast.makeText(this, "No music playing...", Toast.LENGTH_SHORT).show();
					}
				}
				break;
				case R.id.menu_nav_ad: {
					Toast.makeText(this, "MAKINGS", Toast.LENGTH_SHORT).show();
				}
				break;
				case R.id.debug: {
					startActivity(new Intent(this, TestActivity.class));
				}
				break;
				default:
			}
			return true;
		});

		mDrawerToggle = new ActionBarDrawerToggle(this, mMainBinding.drawerLayout, mMainBinding.toolBar, R.string.open, R.string.close);
		mDrawerToggle.syncState();
		mMainBinding.drawerLayout.addDrawerListener(mDrawerToggle);

	}

	public void setSubtitle(CharSequence subTitle) {
		try {
			super.setToolbarSubTitleWithAlphaAnimation(mMainBinding.toolBar, subTitle);
		} catch (NoSuchFieldException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		} catch (IllegalAccessException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	////////////get///////////////////////get///////////////////////get/////////////////

	public final ImageView getNavHeaderImageView() {
		return mNavHeaderImageView;
	}

//	@Deprecated
//	@Nullable
//	public final MusicListFragment getMusicListFragment() {
//		if (mFragmentList.size() == 0) {
//			return null;
//		}
//
//		final String order = PreferenceManager.getDefaultSharedPreferences(this).getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER);
//		if (order.contains(TAB_MUSIC)) {
//			MusicListFragment musicListFragment = (MusicListFragment) mFragmentList.get(order.indexOf(TAB_MUSIC));
//			if (musicListFragment != null) {
//				return musicListFragment;
//			}
//		}
//		return null;
//	}
//
//	@Deprecated
//	public final MusicDetailFragment getMusicDetailFragment() {
//		return musicDetailFragment;
//	}
//
//	@Deprecated
//	@Nullable
//	public final AlbumListFragment getAlbumListFragment() {
//		if (mFragmentList.size() == 0) {
//			return null;
//		}
//		String order = PreferenceManager.getDefaultSharedPreferences(this).getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER);
//
//		if (order.contains(TAB_ALBUM)) {
//			AlbumListFragment musicListFragment = (AlbumListFragment) mFragmentList.get(order.indexOf(TAB_ALBUM));
//			if (musicListFragment != null) {
//				return musicListFragment;
//			}
//		}
//		return null;
//	}
//
//	@Deprecated
//	@Nullable
//	public final PlayListFragment getPlayListFragment() {
//		if (mFragmentList.size() == 0) {
//			return null;
//		}
//		final String order = PreferenceManager.getDefaultSharedPreferences(this).getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER);
//		if (order.contains(TAB_PLAYLIST)) {
//			PlayListFragment musicListFragment = (PlayListFragment) mFragmentList.get(order.indexOf(TAB_PLAYLIST));
//			if (musicListFragment != null) {
//				return musicListFragment;
//			}
//		}
//		return null;
//	}
//
//	@Deprecated
//	@Nullable
//	public final FileViewFragment getFileViewerFragment() {
//		if (mFragmentList.size() == 0) {
//			return null;
//		}
//		String order = PreferenceManager.getDefaultSharedPreferences(this).getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER);
//		if (order.contains(TAB_FILE)) {
//			FileViewFragment musicListFragment = (FileViewFragment) mFragmentList.get(order.indexOf(TAB_FILE));
//			if (musicListFragment != null) {
//				return musicListFragment;
//			}
//		}
//		return null;
//	}
//
//	@Deprecated
//	@Nullable
//	public final ArtistListFragment getArtistFragment() {
//		if (mFragmentList.size() == 0) {
//			return null;
//		}
//		final String order = PreferenceManager.getDefaultSharedPreferences(this).getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER);
//		assert order != null;
//		if (order.contains(TAB_ARTIST)) {
//			ArtistListFragment fragment = (ArtistListFragment) mFragmentList.get(order.indexOf(TAB_ARTIST));
//			if (fragment != null) {
//				return fragment;
//			}
//		}
//		return null;
//	}

	@Nullable
	public final BaseFragment getFragment(final BaseFragment.FragmentType type) {
		if (mFragmentList.size() == 0) {
			return null;
		}

		switch (type) {
			case MUSIC_LIST_FRAGMENT: {
				return musicListFragment;
			}
			case ARTIST_FRAGMENT: {
				return artistListFragment;
			}
			case FILE_VIEW_FRAGMENT: {
				return fileViewFragment;
			}
			case PLAY_LIST_FRAGMENT: {
				return playListFragment;
			}
			case ALBUM_LIST_FRAGMENT: {
				return albumListFragment;
			}
			case MUSIC_DETAIL_FRAGMENT: {
				return musicDetailFragment;
			}
		}
		return null;
	}

	public final NotLeakHandler getHandler() {
		return mHandler;
	}

	public ActivityMainBinding getMainBinding() {
		return mMainBinding;
	}

	public final Menu getMenu() {
		return mMainBinding.toolBar.getMenu();
	}

////////////get///////////////////////get///////////////////////get/////////////////

	public final class NotLeakHandler extends Handler {

		/**
		 * @see Message#what
		 */
		public static final int UP = 50070;
		public static final int DOWN = 50071;
		public static final int LOAD_INTO_NAV_IMAGE = 5003;
		public static final int SET_SLIDE_TOUCH_ENABLE = 5004;
		public static final int SET_SLIDE_TOUCH_DISABLE = 5005;

		@SuppressWarnings("unused")
		private WeakReference<MainActivity> mWeakReference;

		NotLeakHandler(MainActivity activity, Looper looper) {
			super(looper);
			mWeakReference = new WeakReference<>(activity);
		}

		@Override
		public final void handleMessage(Message msg) {
			switch (msg.what) {
				case LOAD_INTO_NAV_IMAGE: {
					mWeakReference.get().runOnUiThread(() -> {
						final Bitmap cover = (Bitmap) msg.obj;
						GlideApp.with(mWeakReference.get())
								.load(cover == null ? R.drawable.ic_audiotrack_24px : cover)
								.transition(DrawableTransitionOptions.withCrossFade())
								.diskCacheStrategy(DiskCacheStrategy.NONE)
								.into(mNavHeaderImageView);
					});
				}
				break;
				case UP: {
					mMainBinding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
				}
				break;

				case DOWN: {
					mMainBinding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
				}
				break;

				case SET_SLIDE_TOUCH_ENABLE: {
					mMainBinding.slidingLayout.setTouchEnabled(true);
				}
				break;

				case SET_SLIDE_TOUCH_DISABLE: {
					mMainBinding.slidingLayout.setTouchEnabled(false);
				}
				default:
			}
		}

	}

}
