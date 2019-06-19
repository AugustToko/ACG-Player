package top.geek_studio.chenlongcould.musicplayer.activity.main;

import android.app.ActivityManager;
import android.app.UiModeManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.appbar.AppBarLayout;
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
import top.geek_studio.chenlongcould.geeklibrary.theme.Theme;
import top.geek_studio.chenlongcould.geeklibrary.theme.ThemeStore;
import top.geek_studio.chenlongcould.geeklibrary.theme.ThemeUtils;
import top.geek_studio.chenlongcould.musicplayer.*;
import top.geek_studio.chenlongcould.musicplayer.activity.*;
import top.geek_studio.chenlongcould.musicplayer.activity.base.BaseListActivity;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyPagerAdapter;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter2AlbumList;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter2ArtistList;
import top.geek_studio.chenlongcould.musicplayer.broadcast.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.databinding.ActivityMainBinding;
import top.geek_studio.chenlongcould.musicplayer.fragment.*;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.threadPool.AlbumThreadPool;
import top.geek_studio.chenlongcould.musicplayer.threadPool.ArtistThreadPool;
import top.geek_studio.chenlongcould.musicplayer.threadPool.CustomThreadPool;
import top.geek_studio.chenlongcould.musicplayer.threadPool.ItemCoverThreadPool;
import top.geek_studio.chenlongcould.musicplayer.utils.MusicUtil;
import top.geek_studio.chenlongcould.musicplayer.utils.PlayListsUtil;
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
public final class MainActivity extends BaseListActivity implements MainContract.View {

	public static final String TAG = "MainActivity";

	private MainContract.Presenter mPresenter;

	public static int PADDING = 0;

	/**
	 * connect {@link MusicService}
	 */
	public ServiceConnection sServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Data.sMusicBinder = IMuiscService.Stub.asInterface(service);

			if (Values.TYPE_RANDOM.equals(PreferenceUtil.getDefault(MainActivity.this)
					.getString(Values.SharedPrefsTag.ORDER_TYPE, Values.TYPE_COMMON))) {

				Data.shuffleOrderListSync(MainActivity.this, false);
			}

			receivedIntentCheck(getIntent());
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Data.sMusicBinder = null;
		}
	};

	private AppBarLayout.OnOffsetChangedListener mOnOffsetChangedListener = new AppBarLayout.OnOffsetChangedListener() {
		@Override
		public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
			mMainBinding.realBlur.setTranslationY(mMainBinding.realBlur.getTranslationX() + verticalOffset);
		}
	};

	public static final char MUSIC_LIST_FRAGMENT_ID = '1';
	public static final char ALBUM_LIST_FRAGMENT_ID = '2';
	public static final char ARTIST_LIST_FRAGMENT_ID = '3';
	public static final char PLAY_LIST_FRAGMENT_ID = '4';
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
	public static boolean sendMessageStatic(@NonNull Message message) {
		boolean result = false;
		if (mHandler != null) {
			mHandler.sendMessage(message);
			result = true;
		}
		return result;
	}

	@SuppressWarnings("UnusedReturnValue")
	public static boolean sendEmptyMessageStatic(int what) {
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
		preferences = PreferenceUtil.getDefault(this);
		mHandlerThread = new HandlerThread("HandlerThread@MainActivity");
		mHandlerThread.start();
		mHandler = new NotLeakHandler(this, mHandlerThread.getLooper());
		mMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

		initView();

		new MainPresenter(this);

		mPresenter.initPermission(this);

		super.onCreate(savedInstanceState);

		mPresenter.checkUpdate(this);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case Values.REQUEST_WRITE_EXTERNAL_STORAGE: {
				if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
					Utils.Ui.fastToast(this, "Failed to get permission, again!");
					final AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Failed to get permission");
					builder.setMessage("Try again?");
					builder.setCancelable(false);
					builder.setNegativeButton("Sure!", (dialog, which) ->
							mPresenter.initPermission(MainActivity.this));
					builder.setNeutralButton("Cancel!", (dialog, which) -> {
						dialog.dismiss();
						finish();
					});
					builder.show();
				} else {
					mPresenter.initData(MainActivity.this);
				}
			}
			break;
			default:
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		runOnUiThread(() -> GlideApp.with(MainActivity.this).pauseRequests());
	}

	/**
	 * check if open file, uri, http or others.
	 */
	@Override
	public void receivedIntentCheck(@Nullable Intent intent) {
		if (intent == null) {
			return;
		}

		Uri uri = intent.getData();
		String mimeType = intent.getType();
		String action = intent.getAction();

		Log.d(TAG, "receivedIntentCheck: uri: " + uri);
		Log.d(TAG, "receivedIntentCheck: mimeType: " + mimeType);
		Log.d(TAG, "receivedIntentCheck: action: " + action);

		if (Data.sMusicBinder != null && uri != null) {
			List<MusicItem> songs = null;
//
			if (uri.getScheme() != null && uri.getAuthority() != null) {
				if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
					String songId = null;
					if (uri.getAuthority().equals("com.android.providers.media.documents")) {
						songId = ReceiverOnMusicPlay.getSongIdFromMediaProvider(uri);
						Log.d(TAG, "playFromUri: getSongIdFromMediaProvider: " + songId);
					} else if (uri.getAuthority().equals("media")) {
						songId = uri.getLastPathSegment();
						Log.d(TAG, "playFromUri: getLastPathSegment: " + songId);
					}
					if (songId != null) {
						Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
								null, MediaStore.Audio.AudioColumns._ID + "=?", new String[]{songId}, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
						List<MusicItem> items = new ArrayList<>();
						if (cursor != null && cursor.moveToFirst()) {
							do {
								items.add(MusicUtil.getSongFromCursorImpl(cursor));
							} while (cursor.moveToNext());
						}

						if (cursor != null) {
							cursor.close();
						}

						songs = items;

						Log.d(TAG, "playFromUri: " + songs.get(0).toString());
					}
				}
			}

			if (songs == null) {
				File songFile = null;
				if (uri.getAuthority() != null && uri.getAuthority().equals("com.android.externalstorage.documents")) {
					songFile = new File(Environment.getExternalStorageDirectory(), uri.getPath().split(":", 2)[1]);
				}
				if (songFile == null) {
					String path = ReceiverOnMusicPlay.getFilePathFromUri(this, uri);
					if (path != null) {
						songFile = new File(path);
					}
				}
				if (songFile == null && uri.getPath() != null) {
					songFile = new File(uri.getPath());
				}
				if (songFile != null) {
					Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
							null, MediaStore.Audio.AudioColumns.DATA + "=?"
							, new String[]{songFile.getAbsolutePath()}, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
					List<MusicItem> items = new ArrayList<>();
					if (cursor != null && cursor.moveToFirst()) {
						do {
							items.add(MusicUtil.getSongFromCursorImpl(cursor));
						} while (cursor.moveToNext());
					}

					if (cursor != null) {
						cursor.close();
					}

					songs = items;
				}
			}

			//noinspection StatementWithEmptyBody
			if (songs != null && !songs.isEmpty()) {
				try {
					final MusicItem item = songs.get(0);
					Data.sMusicBinder.addNextWillPlayItem(item);
					MusicService.MusicControl.intentNext(this);
					Log.d(TAG, "playFromUri: done");
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} else {
				//TODO the file is not listed in the media store
			}
		}
	}

	@NonNull
	@Override
	public Context getContext() {
		return this;
	}

	@Override
	public void notifyAdapter(char fragmentId) {
		switch (fragmentId) {
			case MainActivity.MUSIC_LIST_FRAGMENT_ID: {
				if (musicListFragment.getAdapter() != null)
					musicListFragment.getAdapter().notifyDataSetChanged();
			}
			break;
			case ALBUM_LIST_FRAGMENT_ID: {
				if (albumListFragment.getAdapter() != null)
					albumListFragment.getAdapter().notifyDataSetChanged();
			}
			break;
			case ARTIST_LIST_FRAGMENT_ID: {
				if (artistListFragment.getAdapter() != null)
					artistListFragment.getAdapter().notifyDataSetChanged();
			}
			break;
			default:
				break;
		}
	}

	@NonNull
	@Override
	public BaseFragment getFragment(char fragmentId) {
		BaseFragment fragment;
		switch (fragmentId) {
			case MainActivity.MUSIC_LIST_FRAGMENT_ID: {
				fragment = musicListFragment;
			}
			break;
			case ALBUM_LIST_FRAGMENT_ID: {
				fragment = albumListFragment;
			}
			break;
			case ARTIST_LIST_FRAGMENT_ID: {
				fragment = artistListFragment;
			}
			break;
			default:
				fragment = musicListFragment;
		}
		return fragment;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(TAG, "onNewIntent: " + intent.toString());
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

		if (mCurrentShowedFragment instanceof FileViewFragment) {
			final FileViewFragment fragment = (FileViewFragment) mCurrentShowedFragment;
			final File file = fragment.getCurrentFile();
			if (file != null && !file.getPath().equals(Environment.getExternalStorageDirectory().getPath())) {
				fragment.onBackPressed();
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
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (musicListFragment == null || musicListFragment.getAdapter() == null
				|| musicListFragment.getAdapter().getSelected().size() == 0) {
			getMenuInflater().inflate(R.menu.menu_toolbar_main_common, menu);
			if (mCurrentShowedFragment != null) {
				boolean showAlbumMenu = false;
				boolean showArtistMenu = false;
				switch (Values.CurrentData.CURRENT_PAGE_INDEX) {
					case 0: {
						showAlbumMenu = false;
						showArtistMenu = false;
					}
					break;
					case 1: {
						showAlbumMenu = true;
						showArtistMenu = false;
					}
					break;
					case 2: {
						showAlbumMenu = false;
						showArtistMenu = true;
					}
					break;
					case 3: {
						showAlbumMenu = false;
						showArtistMenu = false;
					}
					break;
					case 4: {
						showAlbumMenu = false;
						showArtistMenu = false;
					}
					break;
				}
				setMenuIconAlphaAnimation(menu.findItem(R.id.menu_toolbar_album_layout)
						, showAlbumMenu, true);
				setMenuIconAlphaAnimation(menu.findItem(R.id.menu_toolbar_artist_layout)
						, showArtistMenu, true);
			}
		} else {
			getMenuInflater().inflate(R.menu.menu_toolbar_main_choose, menu);
		}
		final MenuItem searchItem = menu.findItem(R.id.menu_toolbar_search);
		if (searchItem != null) mSearchView.setMenuItem(searchItem);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final SharedPreferences.Editor editor = preferences.edit();
		switch (item.getItemId()) {
			case R.id.menu_toolbar_exit: {
				fullExit();
			}
			break;

			// common
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

			// choose
			case R.id.menu_toolbar_main_choose_addlist: {
				ArrayList<MusicItem> helper = new ArrayList<>(musicListFragment.getAdapter().getSelected());
				PlayListsUtil.addListDialog(MainActivity.this, helper);
				musicListFragment.getAdapter().clearSelection();
			}
			break;
			case R.id.menu_toolbar_main_choose_share: {
				MusicUtil.sharMusic(MainActivity.this, musicListFragment.getAdapter().getSelected());
				musicListFragment.getAdapter().clearSelection();
			}
			break;
			case R.id.menu_toolbar_main_choose_waitplay: {
				for (final MusicItem musicItem : musicListFragment.getAdapter().getSelected()) {
					try {
						Data.sMusicBinder.addNextWillPlayItem(musicItem);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				musicListFragment.getAdapter().clearSelection();
			}
			break;
			default:
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * init fragments
	 */
	@Override
	public void initFragmentData() {

		mFragmentList.clear();

		String tabOrder = preferences.getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER);
		if (tabOrder == null) tabOrder = DEFAULT_TAB_ORDER;

		for (final char c : tabOrder.toCharArray()) {
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

		mMainBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int i, float v, int i1) {

			}

			@Override
			public void onPageSelected(int i) {
				mMainBinding.realBlur.invalidate();
				mMainBinding.realBlur.requestLayout();
				mMainBinding.realBlur.refreshDrawableState();

				Values.CurrentData.CURRENT_PAGE_INDEX = i;

				if (musicListFragment != null && musicListFragment.getAdapter() != null) {
					musicListFragment.getAdapter().clearSelection();
				}

				TabLayout.Tab tab = mMainBinding.tabLayout.getTabAt(i);
				if (tab != null) {
					tab.select();
				}

				String order = preferences.getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT, DEFAULT_TAB_ORDER);

				if (order == null) {
					order = DEFAULT_TAB_ORDER;
				}

				char type = order.charAt(i);
				setSubTitleType(type);

				if (type == '1') {
					mCurrentShowedFragment = musicListFragment;
//					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_album_layout), false, true);
//					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_artist_layout), false, true);
				}

				if (type == '2') {
					mCurrentShowedFragment = albumListFragment;
//					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_album_layout), true, true);
//					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_artist_layout), false, true);
				}

				if (type == '3') {
					mCurrentShowedFragment = artistListFragment;
//					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_album_layout), false, true);
//					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_artist_layout), true, true);
				}

				//playlist
				if (type == '4') {
					mCurrentShowedFragment = playListFragment;
//					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_album_layout), false, true);
//					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_artist_layout), false, true);
				}

				//file_viewer
				if (type == '5') {
					mCurrentShowedFragment = fileViewFragment;
//					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_album_layout), false, true);
//					setMenuIconAlphaAnimation(getMenu().findItem(R.id.menu_toolbar_artist_layout), false, true);
				}

				supportInvalidateOptionsMenu();
			}

			@Override
			public void onPageScrollStateChanged(int i) {

			}
		});
		mMainBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				mMainBinding.realBlur.invalidate();
				mMainBinding.realBlur.requestLayout();
				mMainBinding.realBlur.refreshDrawableState();

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

		initTab();

		//default
		mCurrentShowedFragment = musicListFragment;
	}

	private void setSubTitleType(final char type) {
		String content = "-";

		switch (type) {
			case '1': {
				content = Data.sMusicItems.size() + " Songs";
			}
			break;

			case '2': {
				if (albumListFragment == null || albumListFragment.getAlbumItemList() == null) break;
				content = albumListFragment.getAlbumItemList().size() + " Albums";
			}
			break;

			case '3': {
				if (artistListFragment == null || artistListFragment.getArtistItemList() == null) break;
				content = artistListFragment.getArtistItemList().size() + " Artists";
			}
			break;

			case '4': {
				content = playListFragment.mPlayListItemList.size() + " Playlists";
			}
			break;

			case '5': {

			}
			break;
		}
		setSubtitle(content);
	}

	@Override
	protected void onDestroy() {
		try {
			unbindService(sServiceConnection);
		} catch (Exception e) {
			Log.d(TAG, "onDestroy: " + e.getMessage());
		}

		if (Data.getCurrentCover() != null && !Data.getCurrentCover().isRecycled()) {
			Data.getCurrentCover().recycle();
		}

		Data.HAS_BIND = false;
		mHandlerThread.quit();
		mFragmentList.clear();
		Data.sHistoryPlayed.clear();

		AlbumThreadPool.finish();
		ItemCoverThreadPool.finish();
		ArtistThreadPool.finish();
		CustomThreadPool.finish();

		App.clearDisposable();

		new Thread(() -> {
			GlideApp.get(MainActivity.this).clearDiskCache();
			GlideApp.get(MainActivity.this).clearMemory();
		});

		super.onDestroy();
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

		//lists
		Data.sPlayOrderList.clear();
		Data.sMusicItems.clear();
		Data.sMusicItemsBackUp.clear();
		Data.S_TRASH_CAN_LIST.clear();

		Data.sMusicBinder = null;
		Data.sTheme = null;
		Data.sCurrentMusicItem = null;
		Data.HAS_PLAYED = false;

		mHandlerThread.quit();
		CustomThreadPool.finish();
		AlbumThreadPool.finish();
		ArtistThreadPool.finish();
		ItemCoverThreadPool.finish();

		runOnUiThread(this::finish);
	}

	@Override
	public final void initStyle() {
		mDrawerToggle.getDrawerArrowDrawable().setColor(Color.BLACK);
		mMainBinding.tabLayout.setTabTextColors(ColorStateList.valueOf(Color.BLACK));
		Utils.Ui.setOverToolbarColor(mMainBinding.toolBar, Color.BLACK);

		mMainBinding.toolBar.setBackgroundColor(Color.TRANSPARENT);
		mMainBinding.tabLayout.setBackgroundColor(Color.TRANSPARENT);
		mMainBinding.appbar.setBackgroundColor(Color.TRANSPARENT);
		setStatusBarTextColor(this, Color.WHITE);

		CustomThreadPool.post(() ->
				setTaskDescription(new ActivityManager.TaskDescription((String) getTitle()
						, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round)
						, Utils.Ui.getPrimaryColor(MainActivity.this))));
		mMainBinding.tabLayout.setSelectedTabIndicatorColor(Utils.Ui.getAccentColor(MainActivity.this));

		getWindow().setNavigationBarColor(Utils.Ui.getPrimaryDarkColor(this));

		Observable.create((ObservableOnSubscribe<Theme>) emitter -> {
			final String themeId = preferences.getString(Values.SharedPrefsTag.SELECT_THEME, ThemeActivity.DEFAULT_THEME);
			if (themeId != null && !ThemeActivity.DEFAULT_THEME.equals(themeId) && (Data.sTheme.getId().equals("null"))) {
				final File themeFile = ThemeUtils.getThemeFile(this, themeId);
				Data.sTheme = ThemeUtils.fileToTheme(themeFile);
			}
			emitter.onNext(Data.sTheme);
		}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Theme>() {
			@Override
			public void onSubscribe(Disposable d) {
				Data.sDisposables.add(d);
			}

			@Override
			public void onNext(Theme theme) {
				if ("null".equals(theme.getId())) return;

				mMainBinding.styleNav.setVisibility(View.VISIBLE);
				mMainBinding.styleTextNavTitle.setText(theme.getTitle());
				mMainBinding.styleTextNavName.setText(theme.getNav_name());

				for (String area : theme.getSelect().split(",")) {

					//检测是否匹配到NAV
					if (area.contains(ThemeStore.SupportArea.NAV)) {

						final String bgPath = theme.getPath()
								+ File.separatorChar
								+ ThemeStore.DIR_IMG_NAV
								+ File.separatorChar
								+ area;

						GlideApp.with(MainActivity.this)
								.load(bgPath)
								.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
								.diskCacheStrategy(DiskCacheStrategy.NONE)
								.into(mMainBinding.styleImgNav);
					}

					//检测是否匹配到BG
					if (area.contains(ThemeStore.SupportArea.BG)) {

						final String bgPath = ThemeUtils.getBgFileByName(theme, area);

						// todo 自定义模糊
						GlideApp.with(MainActivity.this).load(bgPath)
								.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
								.apply(bitmapTransform(new BlurTransformation(10, 10)))
								.into(mMainBinding.bgImage);

//						setStatusBarTextColor(MainActivity.this, new Palette.Builder(Utils.Ui.readBitmapFromFile(bgPath, 50, 50))
//								.generate().getVibrantColor(Utils.Ui.getPrimaryColor(MainActivity.this)));
					}
				}
			}

			@Override
			public void onError(Throwable e) {
				Log.d(TAG, "onError: " + e.getLocalizedMessage());
				mMainBinding.styleNav.setVisibility(View.INVISIBLE);
				GlideApp.with(MainActivity.this).clear(mMainBinding.bgImage);
			}

			@Override
			public void onComplete() {

			}
		});
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
				mPresenter.filterData(newText);
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
			if (musicDetailFragment != null && musicDetailFragment.getSlidingUpPanelLayout().getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
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
//					if (Data.getCurrentCover() != null && !Data.getCurrentCover().isRecycled()) {
//						setStatusBarTextColor(MainActivity.this, new Palette.Builder(Data.getCurrentCover())
//								.generate().getLightVibrantColor(Utils.Ui.getPrimaryColor(MainActivity.this)));
//					}
				} else if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
					mMainBinding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
//					final Drawable drawable = mMainBinding.bgImage.getDrawable();
//					if (drawable instanceof BitmapDrawable) {
//						BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
//						Bitmap bitmap = bitmapDrawable.getBitmap();
//						if (bitmap != null && !bitmap.isRecycled()) {
//							setStatusBarTextColor(MainActivity.this, new Palette.Builder(bitmap)
//									.generate().getLightVibrantColor(Utils.Ui.getPrimaryColor(MainActivity.this)));
//						} else {
//							setStatusBarTextColor(MainActivity.this, Utils.Ui.getPrimaryColor(MainActivity.this));
//						}
//					} else {
//						setStatusBarTextColor(MainActivity.this, Utils.Ui.getPrimaryColor(MainActivity.this));
//					}
					Log.d(TAG, "onPanelStateChanged: COLLAPSED");
				}
			}
		});

		mMainBinding.navigationView.inflateMenu(R.menu.menu_nav);

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
					Toast.makeText(this, "This feature is coming soon", Toast.LENGTH_SHORT).show();
				}
				break;
				case R.id.debug: {
					Toast.makeText(this, "NONE", Toast.LENGTH_SHORT).show();
				}
				break;
				case R.id.dart_mode: {
					boolean isDarkMode = PreferenceUtil.getDefault(MainActivity.this)
							.getBoolean(Values.SharedPrefsTag.DART_MODE, true);
					if (isDarkMode) {
						menuItem.setCheckable(false);
						PreferenceUtil.getDefault(MainActivity.this)
								.edit().putBoolean(Values.SharedPrefsTag.DART_MODE, false).apply();
						UiModeManager UiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
						UiModeManager.setNightMode(android.app.UiModeManager.MODE_NIGHT_NO);
					} else {
						menuItem.setCheckable(true);
						PreferenceUtil.getDefault(MainActivity.this)
								.edit().putBoolean(Values.SharedPrefsTag.DART_MODE, true).apply();
						UiModeManager UiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
						UiModeManager.setNightMode(android.app.UiModeManager.MODE_NIGHT_YES);
					}
				}
				break;
				default:
			}
			return true;
		});

		mDrawerToggle = new ActionBarDrawerToggle(this, mMainBinding.drawerLayout, mMainBinding.toolBar, R.string.open, R.string.close);
		mDrawerToggle.syncState();
		mMainBinding.drawerLayout.addDrawerListener(mDrawerToggle);
		boolean isDarkMode = PreferenceUtil.getDefault(MainActivity.this)
				.getBoolean(Values.SharedPrefsTag.DART_MODE, true);

		// init menu
		if (isDarkMode) {
			mMainBinding.navigationView.getMenu().findItem(R.id.dart_mode).setChecked(true);
		} else {
			mMainBinding.navigationView.getMenu().findItem(R.id.dart_mode).setChecked(false);
		}

		// at the end, set real_blur
		new Handler().post(() -> {
			CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mMainBinding.realBlur.getLayoutParams();
			layoutParams.height = mMainBinding.appbar.getHeight();

			PADDING = layoutParams.height;

			mMainBinding.realBlur.setLayoutParams(layoutParams);

			mMainBinding.appbar.addOnOffsetChangedListener(mOnOffsetChangedListener);
		});
	}

	private void initTab() {
		setTabLongClickListener();

		final int position = mMainBinding.tabLayout.getTabCount();

		final TabLayout.Tab addTab = mMainBinding.tabLayout.newTab();
		final AppCompatImageView icAdd = new AppCompatImageView(this);
		Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_add_white_24dp);
		if (drawable != null) {
			drawable.setTint(Color.BLACK);
		}
		icAdd.setImageDrawable(drawable);
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

		((LinearLayout) addTab.view).setOnClickListener(v -> {
			if (preferences.getBoolean(Values.SharedPrefsTag.SHOW_NOTICE_ADD_TAB, true)) {
				//show dialog
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
						.setTitle("About add tab")
						.setMessage(getString(R.string.tab_add_notice))
						.setCancelable(false)
						.setNegativeButton("OK", (dialog, which) -> {
							preferences.edit().putBoolean(Values.SharedPrefsTag.SHOW_NOTICE_ADD_TAB, false).apply();
							dialog.dismiss();
						});
				builder.show();
			}
			popupMenu.show();
		});

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
	 * setup subtitle with animation
	 */
	public void setSubtitle(@NonNull final CharSequence subTitle) {
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

	public static NotLeakHandler getHandler() {
		return mHandler;
	}

	public ActivityMainBinding getMainBinding() {
		return mMainBinding;
	}

	@Override
	public void sendEmptyMessage(int what) {
		mHandler.sendEmptyMessage(what);
	}

	@Override
	public void sendMessage(Message message) {
		mHandler.sendMessage(message);
	}

	@Override
	public void setPresenter(MainContract.Presenter presenter) {
		mPresenter = presenter;
	}

////////////get///////////////////////get///////////////////////get/////////////////

	@SuppressWarnings("WeakerAccess")
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

				case MessageWorker.RELOAD: {
					if (mWeakReference.get().musicListFragment != null) {
						mWeakReference.get().musicListFragment.reloadData();
					}
				}
				default:
			}
		}

	}

}
