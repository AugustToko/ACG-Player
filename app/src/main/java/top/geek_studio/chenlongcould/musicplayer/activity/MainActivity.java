package top.geek_studio.chenlongcould.musicplayer.activity;

import android.app.ActivityManager;
import android.content.*;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
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
import top.geek_studio.chenlongcould.geeklibrary.theme.IStyle;
import top.geek_studio.chenlongcould.geeklibrary.theme.Theme;
import top.geek_studio.chenlongcould.geeklibrary.theme.ThemeStore;
import top.geek_studio.chenlongcould.geeklibrary.theme.ThemeUtils;
import top.geek_studio.chenlongcould.musicplayer.*;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyPagerAdapter;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter2AlbumList;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter2ArtistList;
import top.geek_studio.chenlongcould.musicplayer.broadcast.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.database.Detail;
import top.geek_studio.chenlongcould.musicplayer.databinding.ActivityMainBinding;
import top.geek_studio.chenlongcould.musicplayer.fragment.*;
import top.geek_studio.chenlongcould.musicplayer.model.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.model.ArtistItem;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.threadPool.AlbumThreadPool;
import top.geek_studio.chenlongcould.musicplayer.threadPool.ArtistThreadPool;
import top.geek_studio.chenlongcould.musicplayer.threadPool.CustomThreadPool;
import top.geek_studio.chenlongcould.musicplayer.threadPool.ItemCoverThreadPool;
import top.geek_studio.chenlongcould.musicplayer.utils.MusicUtil;
import top.geek_studio.chenlongcould.musicplayer.utils.PreferenceUtil;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

/**
 * @author chenlongcould
 */
public final class MainActivity extends BaseCompatActivity implements IStyle {

	public static final String TAG = "MainActivity";

	public ServiceConnection sServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Data.sMusicBinder = IMuiscService.Stub.asInterface(service);

			if (Values.TYPE_RANDOM.equals(preferences.getString(Values.SharedPrefsTag.ORDER_TYPE, Values.TYPE_COMMON))) {
				Data.shuffleOrderListSync(MainActivity.this, false);
			}

//			if (Data.sMusicBinder != null && Data.sCurrentMusicItem.getMusicID() != -1) {
//				try {
//					Data.sMusicBinder.setCurrentMusicData(Data.sCurrentMusicItem);
//				} catch (RemoteException e) {
//					e.printStackTrace();
//				}
//			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
	};

	public static final char MUSIC_LIST_FRAGMENT_ID = '1';
	public static final char ALBUM_LIST_FRAGMENT_ID = '2';
	public static final char ARTIST_LIST_FRAGMENT_ID = '3';
	public static final char PLAYT_LIST_FRAGMENT_ID = '4';
	public static final char FILE_LIST_FRAGMENT_ID = '5';
	public static final char MUSIC_DETAIL_FRAGMENT_ID = '6';

	/**
	 * skip short song (if below 20s)
	 */
	public static final int DEFAULT_SHORT_DURATION = 20000;

	/**
	 * 1 is MUSIC_LIST TAB
	 * 2 is ALBUM TAB
	 * 3 is ARTIST TAB
	 * 4 is PLAYLIST TAB
	 * 5 is FILE MANAGER TAB
	 * <p>
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

	private BaseFragment mCurrentShowedFragment;

	/**
	 * handler
	 */
	private static NotLeakHandler mHandler = null;

	@SuppressWarnings("unused")
	public static boolean sendMessage(@NonNull Message message) {
		boolean result = false;
		if (mHandler != null) {
			mHandler.sendMessage(message);
			result = true;
		}
		return result;
	}

	@SuppressWarnings("UnusedReturnValue")
	public static boolean sendEmptyMessage(int what) {
		boolean result = false;
		if (mHandler != null) {
			mHandler.sendEmptyMessage(what);
			result = true;
		}
		return result;
	}

	private SharedPreferences preferences;

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

	/**
	 * onXXX
	 * At Override
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mHandlerThread = new HandlerThread("HandlerThread@MainActivity");
		mHandlerThread.start();
		mHandler = new NotLeakHandler(this, mHandlerThread.getLooper());
		preferences = PreferenceUtil.getDefault(this);
		mMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

		loadData();

		initView();

		initFragmentData();

		receivedIntentCheck(getIntent());

		super.onCreate(savedInstanceState);
	}

	private void loadData() {
		DBArtSync.startActionSyncAlbum(this);
		DBArtSync.startActionSyncArtist(this);

		// clear old data
		CustomThreadPool.post(() -> {
			List<Detail> details = LitePal.findAll(Detail.class);
			for (Detail d : details) {
				Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
						null, MediaStore.MediaColumns._ID + "=?", new String[]{String.valueOf(d.getMusicId())}
						, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

				if (cursor != null && cursor.moveToNext()) {
					if (cursor.getCount() == 0) {
						Log.d(TAG, "onNext: " + d.getMusicId());
						LitePal.deleteAll(Detail.class, "musicId=?", String.valueOf(d.getMusicId()));
					}
					cursor.close();
				}
			}
		});
	}

	/**
	 * check if open file, uri, http or others.
	 */
	private void receivedIntentCheck(@Nullable Intent intent) {
		if (intent == null) {
			return;
		}

		Uri uri = intent.getData();
		String mimeType = intent.getType();
		String action = intent.getAction();

//		Log.d(TAG, "receivedIntentCheck: uri: " + uri);
//		Log.d(TAG, "receivedIntentCheck: mimeType: " + mimeType);
//		Log.d(TAG, "receivedIntentCheck: action: " + action);

		ReceiverOnMusicPlay.playFromUri(this, uri);

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		receivedIntentCheck(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (musicDetailFragment == null) {
			musicDetailFragment = MusicDetailFragment.newInstance();
			final FragmentManager fragmentManager = getSupportFragmentManager();
			final FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.replace(R.id.frame_wait, musicDetailFragment);
			transaction.commit();
		}

		if (getIntent().getStringExtra(Values.IntentTAG.SHORTCUT_TYPE) != null) {
			//noinspection SwitchStatementWithTooFewBranches
			switch (getIntent().getStringExtra("SHORTCUT_TYPE")) {
				case App.SHORTCUT_RANDOM: {
					ReceiverOnMusicPlay.startService(this, MusicService.ServiceActions.ACTION_FAST_SHUFFLE);
				}
				break;
				default:
					break;
			}
		}
	}

	@Override
	protected void onDestroy() {
		GlideApp.get(this).clearMemory();

		try {
			unbindService(sServiceConnection);
		} catch (Exception e) {
			Log.d(TAG, "onDestroy: " + e.getMessage());
		}

		Data.HAS_BIND = false;
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
		if (musicDetailFragment != null && musicDetailFragment.getSlidingUpPanelLayout() != null
				&& musicDetailFragment.getSlidingUpPanelLayout().getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
			musicDetailFragment.getSlidingUpPanelLayout().setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
			return;
		}

		//3
		if (mMainBinding.slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
			mMainBinding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
			return;
		}

		if (fileViewFragment != null) {
			final File file = fileViewFragment.getCurrentFile();
			if (file != null && !file.getPath().equals(Environment.getExternalStorageDirectory().getPath())) {
				fileViewFragment.onBackPressed();
				return;
			}
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
			final SharedPreferences.Editor editor = preferences.edit();

			switch (item.getItemId()) {
				case R.id.menu_toolbar_exit: {
					fullExit();
				}
				break;

				/*--------------- 快速 随机 播放 ----------------*/
				case R.id.menu_toolbar_fast_play: {
					//just fast random play, without change Data.sPlayOrderList
					ReceiverOnMusicPlay.startService(this, MusicService.ServiceActions.ACTION_FAST_SHUFFLE);
				}
				break;

				case R.id.menu_toolbar_album_linear: {
					editor.putInt(Values.SharedPrefsTag.ALBUM_LIST_DISPLAY_TYPE, MyRecyclerAdapter2AlbumList.LINEAR_TYPE);
					editor.apply();
					albumListFragment.setRecyclerViewData(albumListFragment.getView());
				}
				break;

				case R.id.menu_toolbar_album_grid: {
					editor.putInt(Values.SharedPrefsTag.ALBUM_LIST_DISPLAY_TYPE, MyRecyclerAdapter2AlbumList.GRID_TYPE);
					editor.apply();
					albumListFragment.setRecyclerViewData(albumListFragment.getView());
				}
				break;

				case R.id.menu_toolbar_artist_linear: {
					editor.putInt(Values.SharedPrefsTag.ARTIST_LIST_DISPLAY_TYPE, MyRecyclerAdapter2ArtistList.LINEAR_TYPE);
					editor.apply();
					artistListFragment.setRecyclerViewData(artistListFragment.getView());
				}
				break;

				case R.id.menu_toolbar_artist_grid: {
					editor.putInt(Values.SharedPrefsTag.ARTIST_LIST_DISPLAY_TYPE, MyRecyclerAdapter2ArtistList.GRID_TYPE);
					editor.apply();
					artistListFragment.setRecyclerViewData(artistListFragment.getView());
				}
				break;

				case R.id.menu_toolbar_reload: {
					if (mCurrentShowedFragment != null) mCurrentShowedFragment.reloadData();

				}
				break;
				default:
			}
			return true;
		});
	}

	public void reloadMusicItems() {
		Data.sMusicItems.clear();
		Data.sMusicItemsBackUp.clear();
		MusicUtil.loadDataSource(this);
		musicListFragment.getAdapter().notifyDataSetChanged();
	}

	/**
	 * use this
	 */
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
					CustomThreadPool.post(() -> {
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
					});
				}
				break;
				default:
			}
			return true;
		});
	}


	/**
	 * 根据输入框中的值来过滤数据并更新RecyclerView
	 *
	 * @param filterStr fileName
	 */
	private void filterData(String filterStr) {
		final String tabOrder = preferences.getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER);
		assert tabOrder != null;

		if (tabOrder.charAt(Values.CurrentData.CURRENT_PAGE_INDEX) == MUSIC_LIST_FRAGMENT_ID) {
			if (TextUtils.isEmpty(filterStr)) {
				Data.sMusicItems.clear();
				Data.sMusicItems.addAll(Data.sMusicItemsBackUp);
				musicListFragment.getAdapter().notifyDataSetChanged();
			} else {
				Data.sMusicItems.clear();

				//algorithm
				for (final MusicItem item : Data.sMusicItemsBackUp) {
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
				albumListFragment.getAlbumItemList().clear();
				albumListFragment.getAlbumItemList().addAll(albumListFragment.getAlbumItemListBackup());
				albumListFragment.getAdapter().notifyDataSetChanged();
			} else {
				albumListFragment.getAlbumItemList().clear();

				//algorithm
				for (final AlbumItem item : albumListFragment.getAlbumItemListBackup()) {
					final String name = item.getAlbumName();
					if (name.contains(filterStr.toLowerCase()) || name.contains(filterStr.toUpperCase())) {
						albumListFragment.getAlbumItemList().add(item);
					}
				}

				albumListFragment.getAdapter().notifyDataSetChanged();
			}
		}

		//artist
		if (tabOrder.charAt(Values.CurrentData.CURRENT_PAGE_INDEX) == '3') {
			if (TextUtils.isEmpty(filterStr)) {
				artistListFragment.getArtistItemList().clear();
				artistListFragment.getArtistItemList().addAll(artistListFragment.getArtistItemListBackup());
				artistListFragment.getAdapter().notifyDataSetChanged();
			} else {
				artistListFragment.getArtistItemList().clear();

				//algorithm
				for (ArtistItem item : artistListFragment.getArtistItemListBackup()) {
					String name = item.getArtistName();
					if (name.contains(filterStr.toLowerCase()) || name.contains(filterStr.toUpperCase())) {
						artistListFragment.getArtistItemList().add(item);
					}
				}

				artistListFragment.getAdapter().notifyDataSetChanged();
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
							final SharedPreferences.Editor editor = preferences.edit();
							final String tabOrder = preferences.getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER);
							assert tabOrder != null;
							final StringBuilder currentOrder = new StringBuilder(tabOrder);
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

		String tabOrder = preferences.getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER);
		if (tabOrder == null) tabOrder = DEFAULT_TAB_ORDER;

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

		Values.CurrentData.CURRENT_PAGE_INDEX = 0;

		mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), mFragmentList, mTitles);
		mMainBinding.viewPager.setAdapter(mPagerAdapter);
		mMainBinding.viewPager.setOffscreenPageLimit(mTitles.size() > 1 ? mTitles.size() - 1 : 1);

		inflateCommonMenu();

		runOnUiThread(() -> {
			//hide
			getMenu().findItem(R.id.menu_toolbar_album_layout).setVisible(false);
			getMenu().findItem(R.id.menu_toolbar_artist_layout).setVisible(false);
		});

		setTabLongClickListener();

		final int position = mMainBinding.tabLayout.getTabCount();

		final TabLayout.Tab addTab = mMainBinding.tabLayout.newTab();
		final AppCompatImageView icAdd = new AppCompatImageView(this);
		icAdd.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add_white_24dp));
		addTab.setCustomView(icAdd);
		mMainBinding.tabLayout.addTab(addTab, position);

		final PopupMenu popupMenu = new PopupMenu(this, addTab.view);
		//noinspection PointlessArithmeticExpression
		popupMenu.getMenu().add(Menu.NONE, Menu.FIRST + 0, 0, getString(R.string.tab_music));
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
					//noinspection ConstantConditions
					if (!preferences
							.getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER).contains("1")) {
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
					//noinspection ConstantConditions
					if (!preferences
							.getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER).contains("2")) {
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
					//noinspection ConstantConditions
					if (!preferences
							.getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER).contains("3")) {
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
					//noinspection ConstantConditions
					if (!preferences
							.getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER).contains("4")) {
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
					//noinspection ConstantConditions
					if (!preferences
							.getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER).contains("5")) {
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
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, preferences.getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER) + typeAdded);
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

		mMainBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int i, float v, int i1) {

			}

			@Override
			public void onPageSelected(int i) {
				Values.CurrentData.CURRENT_PAGE_INDEX = i;

				musicListFragment.getAdapter().clearSelection();

				TabLayout.Tab tab = mMainBinding.tabLayout.getTabAt(i);
				if (tab != null) {
					tab.select();
				}

				String order = preferences.getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER);

				if (order == null) {
					order = DEFAULT_TAB_ORDER;
				}

				if (order.charAt(i) == '1') {
					mCurrentShowedFragment = musicListFragment;

					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_album_layout), false, true);
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_artist_layout), false, true);
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_search), true, false);
					setSubtitle(Data.sMusicItems.size() + " Songs");
				}

				if (order.charAt(i) == '2') {
					mCurrentShowedFragment = albumListFragment;

					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_album_layout), true, true);
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_artist_layout), false, true);
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_search), true, false);
					setSubtitle(albumListFragment.getAlbumItemList().size() + " Albums");
				}

				if (order.charAt(i) == '3') {
					mCurrentShowedFragment = artistListFragment;

					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_album_layout), false, true);
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_artist_layout), true, true);
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_search), true, false);
					setSubtitle(artistListFragment.getArtistItemList().size() + " Artists");
				}

				//playlist
				if (order.charAt(i) == '4') {
					mCurrentShowedFragment = playListFragment;

					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_album_layout), false, true);
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_artist_layout), false, true);
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_search), false, false);
					setSubtitle(Data.sPlayListItems.size() + " Playlists");
				}

				//fileviewer
				if (order.charAt(i) == '5') {
					mCurrentShowedFragment = fileViewFragment;

					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_album_layout), false, true);
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_artist_layout), false, true);
					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_search), false, false);
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

		//default
		mCurrentShowedFragment = musicListFragment;

	}

	public void fullExit() {
		try {
			unbindService(sServiceConnection);
		} catch (Exception e) {
			Log.d(TAG, "fullExit: " + e.getMessage());
		}
		try {
			stopService(new Intent(MainActivity.this, MusicService.class));
		} catch (Exception e) {
			Log.d(TAG, "fullExit: " + e.getMessage());
		}
		try {
			stopService(new Intent(MainActivity.this, MyTileService.class));
		} catch (Exception e) {
			Log.d(TAG, "fullExit: " + e.getMessage());
		}
		Data.sMusicBinder = null;

		if (Data.getCurrentCover() != null && !Data.getCurrentCover().isRecycled()) {
			Data.getCurrentCover().recycle();
		}

		App.clearDisposable();

		//lists
		Data.sPlayOrderList.clear();
		Data.sMusicItems.clear();
		Data.sMusicItemsBackUp.clear();
		Data.sHistoryPlayed.clear();
		Data.S_TRASH_CAN_LIST.clear();

		finish();
	}

	@Override
	public final void initStyle() {
		Log.d(TAG, "initStyle: Main");
		mDrawerToggle.getDrawerArrowDrawable().setColor(Utils.Ui.getTitleColor(MainActivity.this));
		setTaskDescription(new ActivityManager.TaskDescription((String) getTitle(), null, Utils.Ui.getPrimaryColor(MainActivity.this)));
		mMainBinding.tabLayout.setTabTextColors(ColorStateList.valueOf(Utils.Ui.getTitleColor(MainActivity.this)));
		mMainBinding.tabLayout.setSelectedTabIndicatorColor(Utils.Ui.getAccentColor(MainActivity.this));
		Utils.Ui.setOverToolbarColor(mMainBinding.toolBar, Utils.Ui.getTitleColor(MainActivity.this));

		getWindow().setNavigationBarColor(Utils.Ui.getPrimaryDarkColor(this));

		Observable.create((ObservableOnSubscribe<Theme>) emitter -> {
			final String themeId = preferences.getString(Values.SharedPrefsTag.SELECT_THEME, ThemeActivity.DEFAULT_THEME);
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

				if (musicDetailFragment == null || musicDetailFragment.getNowPlayingBody() == null) {
					return;
				}

				MusicDetailFragment.sendEmptyMessage(MusicDetailFragment.NotLeakHandler.RECYCLER_SCROLL);

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

	public static NotLeakHandler getHandler() {
		return mHandler;
	}

	public ActivityMainBinding getMainBinding() {
		return mMainBinding;
	}

	public final Menu getMenu() {
		return mMainBinding.toolBar.getMenu();
	}

////////////get///////////////////////get///////////////////////get/////////////////

	public static final class NotLeakHandler extends Handler {

		/**
		 * @see Message#what
		 */
		public static final int UP = 50070;
		public static final int DOWN = 50071;
		public static final int LOAD_INTO_NAV_IMAGE = 5003;
		public static final int SET_SLIDE_TOUCH_ENABLE = 5004;
		public static final int SET_SLIDE_TOUCH_DISABLE = 5005;
		public static final byte RELOAD_MUSIC_ITEMS = 127;

		public static final byte LOAD_DATA_DONE = 126;

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
								.into(mWeakReference.get().mNavHeaderImageView);
					});
				}
				break;
				case UP: {
					mWeakReference.get().getMainBinding().slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
				}
				break;

				case DOWN: {
					mWeakReference.get().getMainBinding().slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
				}
				break;

				case SET_SLIDE_TOUCH_ENABLE: {
					mWeakReference.get().getMainBinding().slidingLayout.setTouchEnabled(true);
				}
				break;

				case SET_SLIDE_TOUCH_DISABLE: {
					mWeakReference.get().getMainBinding().slidingLayout.setTouchEnabled(false);
				}
				break;

				case RELOAD_MUSIC_ITEMS: {
					mWeakReference.get().runOnUiThread(() -> mWeakReference.get().musicListFragment.reloadData());
				}
				break;

				case LOAD_DATA_DONE: {
					//service
					final Intent intent = new Intent(mWeakReference.get(), MusicService.class);
					mWeakReference.get().startService(intent);
					Data.HAS_BIND = mWeakReference.get().bindService(intent, mWeakReference.get().sServiceConnection, Context.BIND_AUTO_CREATE);

					mWeakReference.get().setSubtitle(Data.sPlayOrderList.size() + " Songs");
				}
				break;
				default:
			}
		}

	}

}
