/*
 * ************************************************************
 * 文件：MainActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月27日 13:11:38
 * 上次修改时间：2019年01月27日 13:08:48
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.geeklibrary.Theme.IStyle;
import top.geek_studio.chenlongcould.geeklibrary.Theme.Theme;
import top.geek_studio.chenlongcould.geeklibrary.Theme.ThemeStore;
import top.geek_studio.chenlongcould.geeklibrary.Theme.ThemeUtils;
import top.geek_studio.chenlongcould.musicplayer.Adapters.MyPagerAdapter;
import top.geek_studio.chenlongcould.musicplayer.Adapters.MyRecyclerAdapter2AlbumList;
import top.geek_studio.chenlongcould.musicplayer.AlbumThreadPool;
import top.geek_studio.chenlongcould.musicplayer.BroadCasts.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.BuildConfig;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Fragments.AlbumListFragment;
import top.geek_studio.chenlongcould.musicplayer.Fragments.FileViewFragment;
import top.geek_studio.chenlongcould.musicplayer.Fragments.MusicDetailFragment;
import top.geek_studio.chenlongcould.musicplayer.Fragments.MusicListFragment;
import top.geek_studio.chenlongcould.musicplayer.Fragments.PlayListFragment;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.ItemCoverThreadPool;
import top.geek_studio.chenlongcould.musicplayer.Models.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.MyApplication;
import top.geek_studio.chenlongcould.musicplayer.MyMusicService;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.databinding.ActivityMainBinding;

public final class MainActivity extends MyBaseCompatActivity implements IStyle {

    public static final String TAG = "MainActivity";

    private SharedPreferences mSharedPreferences;

    /**
     * @see Message#what
     */
    public static final int UP = 50070;
    public static final int DOWN = 50071;

    /**
     * 检测当前 slide 的位置.
     * 当滑动 slide {@link SlidingUpPanelLayout} 时, 迅速点击可滑动区域外, slide 会卡住.
     * 但 slide 状态会变为 {@link SlidingUpPanelLayout.PanelState#COLLAPSED}.
     * 故立此 FLAG.
     */
    public static float CURRENT_SLIDE_OFFSET = 1;

    public static boolean ANIMATION_FLAG = true;

    private boolean TOOLBAR_CLICKED = false;

    private boolean BACK_PRESSED = false;

    /**
     * ALL FRAGMENTS
     */
    private List<Fragment> mFragmentList = new ArrayList<>();

    private NotLeakHandler mHandler;

    private ActivityMainBinding mMainBinding;

    private MyPagerAdapter mPagerAdapter;
    private final ArrayList<String> mTitles = new ArrayList<>();
    private ImageView mNavHeaderImageView;
    private Menu mMenu;

    @SuppressWarnings("FieldCanBeLocal")
    private SearchView mSearchView;

    /**
     * ----------------- fragment(s) ----------------------
     */
    private MusicListFragment mMusicListFragment;
    private AlbumListFragment mAlbumListFragment;
    private PlayListFragment mPlayListFragment;
    private MusicDetailFragment mMusicDetailFragment;
    private FileViewFragment mFileViewFragment;
    private HandlerThread mHandlerThread;

    private AlertDialog load;

    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    private RewardedVideoAd mRewardedVideoAd;

    private FirebaseAnalytics mFirebaseAnalytics;

//    public static void sendKeyEvent(final int KeyCode) {
//        new Thread() {     //不可在主线程中调用
//            public void run() {
//                Instrumentation inst = new Instrumentation();
//                inst.sendKeyDownUpSync(KeyCode);
//            }
//
//        }.start();
//    }

    /**
     * clear data
     */
    public static void clear() {
        Data.sActivities.clear();
        if (Data.sMainRef != null) {
            Data.sMainRef.clear();
        }

        Data.sPlayOrderList.clear();
        Data.sTheme = null;
        Data.sAlbumItems.clear();
        Data.sHistoryPlay.clear();
        Data.sMusicItemsBackUp.clear();
        Data.sMusicItems.clear();
        Data.sAlbumItems.clear();

        AlbumListFragment.VIEW_HAS_LOAD = false;

        for (Disposable disposable : Data.sDisposables) {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
        }

        if (Data.getCurrentCover() != null)
            Data.getCurrentCover().recycle();
    }

    /**
     * show AD
     */
    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd(MyApplication.AD_ID,
                new AdRequest.Builder().build());
    }

    /**
     * onXXX
     * At Override
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        initFragmentData();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        MobileAds.initialize(this, MyApplication.APP_ID);
        // Use an activity context to get the rewarded video instance.
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                Log.d(TAG, "onRewardedVideoAdLoaded: ");
            }

            @Override
            public void onRewardedVideoAdOpened() {
                Log.d(TAG, "onRewardedVideoAdOpened: ");
            }

            @Override
            public void onRewardedVideoStarted() {
                Log.d(TAG, "onRewardedVideoStarted: ");
            }

            @Override
            public void onRewardedVideoAdClosed() {
                Log.d(TAG, "onRewardedVideoAdClosed: ");
                loadRewardedVideoAd();
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                Log.d(TAG, "onRewarded: ");
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                Log.d(TAG, "onRewardedVideoAdLeftApplication: ");
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                Log.d(TAG, "onRewardedVideoAdFailedToLoad: " + i);
                mMainBinding.getRoot().postDelayed(() -> loadRewardedVideoAd(), 2000);
            }

            @Override
            public void onRewardedVideoCompleted() {
                Log.d(TAG, "onRewardedVideoCompleted: ");
            }
        });
        loadRewardedVideoAd();

        //config
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_deaults);
        mFirebaseRemoteConfig.fetch(5000)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "initView: Fetch Succeeded");

                        // After config data is successfully fetched, it must be activated before newly fetched
                        // values are returned.
                        mFirebaseRemoteConfig.activateFetched();
                    } else {
                        Log.i(TAG, "initView: Fetch Failed", new Throwable("Fetch Failed"));
                    }
                    displayWelcomeMessage();
                });

        mHandlerThread = new HandlerThread("Handler Thread in MainActivity");
        mHandlerThread.start();
        mHandler = new NotLeakHandler(this, mHandlerThread.getLooper());

        //监听耳机(有线或无线)的插拔动作, 拔出暂停音乐
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(Data.mMyHeadSetPlugReceiver, intentFilter);

        //service
        Intent intent = new Intent(this, MyMusicService.class);
        startService(intent);
        Data.HAS_BIND = bindService(intent, Data.sServiceConnection, BIND_AUTO_CREATE);

        initView();

        Data.init(this);

        loadData();

        if (savedInstanceState != null)
            mMainBinding.viewPager.setCurrentItem(savedInstanceState.getInt("viewpage", 0), true);

        //set play type
        Values.CurrentData.CURRENT_PLAY_TYPE = mSharedPreferences.getString(Values.SharedPrefsTag.PLAY_TYPE, Values.TYPE_COMMON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRewardedVideoAd.resume(this);
        initStyle();
        if (mMusicDetailFragment != null) {
            mMusicDetailFragment.initStyle();
        }
        if (mMusicListFragment != null) {
            mMusicListFragment.initStyle();
        }
    }

    @Override
    protected void onPause() {
        mRewardedVideoAd.pause(this);
        super.onPause();
    }

    private void loadData() {
        load = Utils.Ui.getLoadingDialog(this, "Loading...");
        load.show();

        Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            if (Data.sMusicItems.isEmpty()) {
                /*---------------------- init Data!!!! -------------------*/
                final Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
                if (cursor != null && cursor.moveToFirst()) {
                    //没有歌曲直接退出app
                    if (cursor.getCount() == 0) {
                        emitter.onNext(-2);
                    } else {

                        final boolean skipShort = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Values.SharedPrefsTag.HIDE_SHORT_SONG, true);

                        do {
                            final String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                            final File file = new File(path);
                            if (!file.exists()) {
                                Log.e(TAG, "onAttach: song file: " + path + " does not exits, skip this!!!");
                                continue;
                            }

                            final int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                            if (duration <= 0) {
                                Log.d(TAG, "onCreate: the music-file duration is " + duration + ", skip...");
                                continue;
                            }
                            if (skipShort && duration < 10) {
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

                            final MusicItem.Builder builder = new MusicItem.Builder(id, name, path)
                                    .musicAlbum(albumName)
                                    .addTime((int) addTime)
                                    .artist(artist)
                                    .duration(duration)
                                    .mimeName(mimeType)
                                    .size(size)
                                    .addAlbumId(albumId);

                            Data.sMusicItems.add(builder.build());
                            Data.sMusicItemsBackUp.add(builder.build());
                            Data.sPlayOrderList.add(builder.build());

                        } while (cursor.moveToNext());
                        Log.i(Values.TAG_UNIVERSAL_ONE, "onCreate: The MusicData load done.");
                        cursor.close();

                        if (Values.CurrentData.CURRENT_PLAY_TYPE.equals(Values.TYPE_RANDOM))
                            Collections.shuffle(Data.sPlayOrderList);

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
                    Utils.Ui.fastToast(MainActivity.this, "cursor is null or moveToFirst Fail");
                    mHandler.postDelayed(MainActivity.this::exitApp, 1000);
                    return;
                }
                if (result == -2) {
                    Utils.Ui.fastToast(MainActivity.this, "Can not find any music!");
                    mHandler.postDelayed(MainActivity.this::exitApp, 1000);
                    return;
                }

                if (getIntent().getStringExtra("shortcut_type") != null) {
                    switch (getIntent().getStringExtra("shortcut_type")) {
                        case MyApplication.SHORTCUT_RANDOM: {
                            Utils.SendSomeThing.sendPlay(MainActivity.this, ReceiverOnMusicPlay.TYPE_SHUFFLE, null);
                        }
                        break;
                        default:
                            break;
                    }
                }

                mMainBinding.toolBar.setSubtitle(Data.sPlayOrderList.size() + " Songs");
            }

            @Override
            public final void onError(Throwable throwable) {
                load.dismiss();
                Toast.makeText(MainActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                exitApp();
            }

            @Override
            public final void onComplete() {
                load.dismiss();
            }
        });

    }

    @Override
    protected void onDestroy() {

        try {
            unbindService(Data.sServiceConnection);
        } catch (Exception e) {
            Log.d(TAG, "onDestroy: " + e);
        }
        unregisterReceiver(Data.mMyHeadSetPlugReceiver);
        mRewardedVideoAd.destroy(this);

        mHandlerThread.quit();
        mFragmentList.clear();
        AlbumThreadPool.finish();
        ItemCoverThreadPool.finish();
        super.onDestroy();
    }

    @Override
    public final void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }

    /**
     * exit app without stop music
     * without clear data...
     */
    private void goToBackground() {
        try {
            unbindService(Data.sServiceConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Data.sActivities.clear();
        Data.sMainRef.clear();
        Data.sMainRef = null;
        GlideApp.get(this).clearMemory();
        finish();
    }

    @Override
    public final void onBackPressed() {

        //1
        if (mMainBinding.drawerLayout.isDrawerOpen(Gravity.START)) {
            mMainBinding.drawerLayout.closeDrawers();
            return;
        }

        //2
        if (getMusicDetailFragment() != null && getMusicDetailFragment().getSlidingUpPanelLayout().getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            getMusicDetailFragment().getSlidingUpPanelLayout().setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return;
        }

        //3
        if (mMainBinding.slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            mMainBinding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return;
        }

        if (!mFileViewFragment.getCurrentFile().getPath().equals(Environment.getExternalStorageDirectory().getPath())) {
            mFileViewFragment.onBackPressed();
            return;
        }

        //4
        if (BACK_PRESSED) {
            goToBackground();
        } else {
            BACK_PRESSED = true;
            Toast.makeText(this, "Press again to exit!", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> BACK_PRESSED = false, 2000);
        }

    }

    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_toolbar_exit: {
                exitApp();
            }
            break;

            /*--------------- 快速 随机 播放 ----------------*/
            case R.id.menu_toolbar_fast_play: {
                //just fast random play, without change Data.sPlayOrderList
                Utils.SendSomeThing.sendPlay(this, ReceiverOnMusicPlay.TYPE_SHUFFLE, TAG);
            }
            break;

            case R.id.menu_toolbar_linear: {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt(Values.SharedPrefsTag.ALBUM_LIST_DISPLAY_TYPE, MyRecyclerAdapter2AlbumList.LINEAR_TYPE);
                editor.apply();
                mPagerAdapter.notifyDataSetChanged();
                mAlbumListFragment.setRecyclerViewData();
            }
            break;

            case R.id.menu_toolbar_grid: {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt(Values.SharedPrefsTag.ALBUM_LIST_DISPLAY_TYPE, MyRecyclerAdapter2AlbumList.GRID_TYPE);
                editor.apply();
                mPagerAdapter.notifyDataSetChanged();
                mAlbumListFragment.setRecyclerViewData();
            }
            break;

            case R.id.menu_toolbar_reload: {
                mFragmentList.clear();
                mTitles.clear();
                Data.sMusicItems.clear();
                Data.sPlayOrderList.clear();
                Data.sMusicItemsBackUp.clear();
                Data.sAlbumItemsBackUp.clear();
                Data.sAlbumItems.clear();

                loadData();
            }
            break;
        }
        return true;
    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_toolbar, mMenu);

        MenuItem searchItem = menu.findItem(R.id.menu_toolbar_search);

        mSearchView = (SearchView) searchItem.getActionView();

        mSearchView.setIconifiedByDefault(true);
        mSearchView.setQueryHint("Enter Name...");
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public final boolean onQueryTextSubmit(String query) {
                mMusicListFragment.getMusicListBinding().includeRecycler.recyclerView.stopScroll();
                return false;
            }

            @Override
            public final boolean onQueryTextChange(String newText) {
                filterData(newText);
                return true;
            }
        });

        return true;
    }

    /**
     * 根据输入框中的值来过滤数据并更新RecyclerView
     *
     * @param filterStr fileName
     */
    private void filterData(String filterStr) {
        // TODO: 2018/11/19 searchView while in the album page...
        if (Values.CurrentData.CURRENT_PAGE_INDEX == 0) {
            if (TextUtils.isEmpty(filterStr)) {
                Data.sMusicItems.clear();
                Data.sMusicItems.addAll(Data.sMusicItemsBackUp);
                mMusicListFragment.getAdapter().notifyDataSetChanged();
            } else {
                Data.sMusicItems.clear();

                //algorithm
                for (MusicItem item : Data.sMusicItemsBackUp) {
                    String name = item.getMusicName();
                    if (name.contains(filterStr.toLowerCase()) || name.contains(filterStr.toUpperCase())) {
                        Data.sMusicItems.add(item);
                    }
                }
                mMusicListFragment.getAdapter().notifyDataSetChanged();
            }
        } else if (Values.CurrentData.CURRENT_PAGE_INDEX == 1) {
            if (TextUtils.isEmpty(filterStr)) {
                Data.sAlbumItems.clear();
                Data.sAlbumItems.addAll(Data.sAlbumItemsBackUp);
                mAlbumListFragment.getAdapter2AlbumList().notifyDataSetChanged();
            } else {
                Data.sAlbumItems.clear();

                //algorithm
                for (AlbumItem item : Data.sAlbumItemsBackUp) {
                    String name = item.getAlbumName();
                    if (name.contains(filterStr.toLowerCase()) || name.contains(filterStr.toUpperCase())) {
                        Data.sAlbumItems.add(item);
                    }
                }
                mAlbumListFragment.getAdapter2AlbumList().notifyDataSetChanged();
            }
        } else if (Values.CurrentData.CURRENT_PAGE_INDEX == 2) {

        }
    }

    /**
     * init fragments
     */
    private void initFragmentData() {

        final String tab_1 = getResources().getString(R.string.music);
        mTitles.add(tab_1);
        mMusicListFragment = MusicListFragment.newInstance();
        mFragmentList.add(mMusicListFragment);

        mMusicDetailFragment = MusicDetailFragment.newInstance();
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_wait, mMusicDetailFragment);
        transaction.commit();

        final String tab_2 = getResources().getString(R.string.album);
        mTitles.add(tab_2);
        mAlbumListFragment = AlbumListFragment.newInstance();
        mFragmentList.add(mAlbumListFragment);

        final String tab_3 = getResources().getString(R.string.play_list);
        mTitles.add(tab_3);
        mPlayListFragment = PlayListFragment.newInstance(2);
        mFragmentList.add(mPlayListFragment);

        final String tab_4 = getResources().getString(R.string.tab_file);
        mTitles.add(tab_4);
        mFileViewFragment = FileViewFragment.newInstance();
        mFragmentList.add(mFileViewFragment);

        Values.CurrentData.CURRENT_PAGE_INDEX = 0;

        mMainBinding.tabLayout.addTab(mMainBinding.tabLayout.newTab().setText(mTitles.get(0)));
        mMainBinding.tabLayout.addTab(mMainBinding.tabLayout.newTab().setText(mTitles.get(1)));
        mMainBinding.tabLayout.addTab(mMainBinding.tabLayout.newTab().setText(mTitles.get(2)));
        mMainBinding.tabLayout.addTab(mMainBinding.tabLayout.newTab().setText(mTitles.get(3)));

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), mFragmentList, mTitles);
        mMainBinding.viewPager.setOffscreenPageLimit(3);
        mMainBinding.tabLayout.setupWithViewPager(mMainBinding.viewPager);
        mMainBinding.viewPager.setAdapter(mPagerAdapter);

    }

    public final void exitApp() {

        if (Values.HAS_PLAYED) {
            ReceiverOnMusicPlay.resetMusic();
        }

        stopService(new Intent(MainActivity.this, MyMusicService.class));

        try {
            unregisterReceiver(Data.mMyHeadSetPlugReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

//        //stop tile
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            stopService(new Intent(this, MyTile.class));
//        }

        Data.sMusicBinder = null;

        Values.HAS_PLAYED = false;

        clear();
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public final void initStyle() {

        Utils.Ui.setTopBottomColor(this, mMainBinding.appbar, mMainBinding.toolBar);
        mMainBinding.tabLayout.setBackgroundColor(Utils.Ui.getPrimaryColor(this));
        mMainBinding.tabLayout.setSelectedTabIndicatorColor(Utils.Ui.getAccentColor(this));
        Observable.create((ObservableOnSubscribe<Theme>) emitter -> {
            final String themeId = PreferenceManager.getDefaultSharedPreferences(this).getString(Values.SharedPrefsTag.SELECT_THEME, "null");
            if (themeId != null && !themeId.equals("null")) {
                final File themeFile = ThemeUtils.getThemeFile(this, themeId);
                final Theme theme = ThemeUtils.fileToTheme(themeFile);
                if (theme != null) {
                    Data.sTheme = theme;
                    if (theme.getSupport_area().contains(ThemeStore.SupportArea.NAV))
                        emitter.onNext(theme);
                } else {
                    emitter.onError(null);
                }
            } else {
                emitter.onError(null);
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Theme>() {
            @Override
            public final void onSubscribe(Disposable disposable) {

            }

            @Override
            public final void onNext(Theme theme) {
                if (theme != null) {
                    mMainBinding.styleNav.setVisibility(View.VISIBLE);
                    mMainBinding.styleTextNavTitle.setText(theme.getTitle());
                    mMainBinding.styleTextNavName.setText(theme.getNav_name());

                    for (String nav : theme.getSelect().split(",")) {
                        if (nav.contains(ThemeStore.SupportArea.NAV)) {
                            GlideApp.with(MainActivity.this)
                                    .load(theme.getPath() + File.separatorChar + ThemeStore.DIR_IMG_NAV + File.separatorChar + nav + ".png")
                                    .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                                    .into(mMainBinding.styleImgNav);
                        }
                    }
                } else {
                    mMainBinding.styleNav.setVisibility(View.GONE);
                }
            }

            @Override
            public final void onError(Throwable throwable) {
                mMainBinding.styleNav.setVisibility(View.GONE);
            }

            @Override
            public final void onComplete() {

            }
        });
    }

    private void initView() {

        //根据recycler view的滚动程度, 来判断如何返回顶部
        mMainBinding.toolBar.setOnClickListener(v -> {
            if (TOOLBAR_CLICKED) {
                switch (Values.CurrentData.CURRENT_PAGE_INDEX) {
                    case 0: {
                        if (Values.CurrentData.CURRENT_BIND_INDEX_MUSIC_LIST > 20) {
                            mMusicListFragment.getMusicListBinding().includeRecycler.recyclerView.scrollToPosition(0);
                        } else {
                            mMusicListFragment.getMusicListBinding().includeRecycler.recyclerView.smoothScrollToPosition(0);
                        }
                    }
                    break;
                    case 1: {
                        if (Values.CurrentData.CURRENT_BIND_INDEX_ALBUM_LIST > 20) {
                            mAlbumListFragment.getRecyclerView().scrollToPosition(0);
                        } else {
                            mAlbumListFragment.getRecyclerView().smoothScrollToPosition(0);
                        }
                    }
                }

            }
            TOOLBAR_CLICKED = true;
            new Handler().postDelayed(() -> TOOLBAR_CLICKED = false, 1000);         //双击机制
        });

        setSupportActionBar(mMainBinding.toolBar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_24px);
        }

        mNavHeaderImageView = mMainBinding.navigationView.getHeaderView(0).findViewById(R.id.nav_view_image);

        mNavHeaderImageView.setOnClickListener(v -> {
            if (getMusicDetailFragment() != null && getMusicDetailFragment().getSlidingUpPanelLayout().getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                getMusicDetailFragment().getSlidingUpPanelLayout().setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
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

                CURRENT_SLIDE_OFFSET = slideOffset;

                mMainBinding.mainBody.setTranslationY(0 - slideOffset * 120);

                float current = 1 - slideOffset;
                mMusicDetailFragment.getNowPlayingBody().setAlpha(current);

                //在底部, 重置动画初始位置
                if (current == 1) {
                    mMusicDetailFragment.setDefAnimation();
                    mMusicDetailFragment.clearAnimations();
                    ANIMATION_FLAG = true;
                    getMusicDetailFragment().getHandler().sendEmptyMessage(Values.HandlerWhat.RECYCLER_SCROLL);
                }

                if (current == 0) {

                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.hide(mAlbumListFragment).hide(mPlayListFragment).hide(mPlayListFragment);
                    fragmentTransaction.commit();
                    mMusicListFragment.visibleOrGone(View.GONE);

                    //隐藏BODY
                    mMusicDetailFragment.getNowPlayingBody().setVisibility(View.GONE);

                    mMainBinding.tabLayout.setVisibility(View.GONE);
                    mMainBinding.tabLayout.setVisibility(View.GONE);

                    if (AlbumListFragment.VIEW_HAS_LOAD)
                        mAlbumListFragment.visibleOrGone(View.GONE);
                    mPlayListFragment.visibleOrGone(View.GONE);

                    //start animation
                    if (ANIMATION_FLAG) {
                        ANIMATION_FLAG = false;
                        getMusicDetailFragment().initAnimation();
                    }

                } else {
                    mMainBinding.tabLayout.setVisibility(View.VISIBLE);
                    mMainBinding.tabLayout.setVisibility(View.VISIBLE);
                    mMusicDetailFragment.getNowPlayingBody().setVisibility(View.VISIBLE);
                    if (AlbumListFragment.VIEW_HAS_LOAD)
                        mAlbumListFragment.visibleOrGone(View.VISIBLE);
                    mPlayListFragment.visibleOrGone(View.VISIBLE);
                    mMusicListFragment.visibleOrGone(View.VISIBLE);
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.show(mAlbumListFragment).show(mPlayListFragment).show(mPlayListFragment);
                    fragmentTransaction.commit();
                }
            }

            @Override
            public final void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    mMainBinding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                } else if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED)
                    mMainBinding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
        });

        //First no music no slide
        if (Data.sMusicBinder != null) {
            if (Values.HAS_PLAYED)
                mMainBinding.slidingLayout.setTouchEnabled(true);
            else
                mMainBinding.slidingLayout.setTouchEnabled(false);
        } else {
            mMainBinding.slidingLayout.setTouchEnabled(false);
        }

        mMainBinding.navigationView.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.menu_nav_exit: {
                    exitApp();
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
                    if (Values.HAS_PLAYED)
                        startActivity(new Intent(MainActivity.this, CarViewActivity.class));
                    else
                        Toast.makeText(this, "No music playing...", Toast.LENGTH_SHORT).show();
                }
                break;
                case R.id.menu_nav_ad: {
                    if (mRewardedVideoAd.isLoaded()) {
                        Toast.makeText(this, "AD showing...", Toast.LENGTH_SHORT).show();
                        mRewardedVideoAd.show();
                    } else {
                        Toast.makeText(this, "AD not loaded try again...", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
                case R.id.debug: {
                    startActivity(new Intent(this, TestActivity.class));
                }
                break;
            }
            return true;
        });

        // 实例代码
        mMainBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public final void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                ArgbEvaluator evaluator = new ArgbEvaluator(); // ARGB求值器
//
//                final int green = 0xFF43CD80, blue = 0xFF11A0F8, yellow = 0xFFFFBB43, red = 0xFFB54B36;
//
//                int evaluate; // 初始默认颜色
//                if (position == 0) {
//                    evaluate = (Integer) evaluator.evaluate(positionOffset, green, blue); // 根据positionOffset和第0页~第1页的颜色转换范围取颜色值
//                } else if (position == 1) {
//                    evaluate = (Integer) evaluator.evaluate(positionOffset, blue, yellow); // 根据positionOffset和第1页~第2页的颜色转换范围取颜色值
//                } else if (position == 2) {
//                    evaluate = (Integer) evaluator.evaluate(positionOffset, yellow, red); // 根据positionOffset和第2页~第3页的颜色转换范围取颜色值
//                } else {
//                    evaluate = red; // 最终第3页的颜色
//                }
//
//                mMainBinding.toolBar.setBackgroundColor(evaluate);
//                mMainBinding.tabLayout.setBackgroundColor(evaluate);
            }

            @Override
            public final void onPageSelected(int position) {
                Values.CurrentData.CURRENT_PAGE_INDEX = position;
                switch (position) {
                    case 0: {
                        mMenu.findItem(R.id.menu_toolbar_layout).setVisible(false);
                        mMainBinding.toolBar.setSubtitle(Data.sPlayOrderList.size() + " Songs");
                    }
                    break;
                    case 1: {
                        mMenu.findItem(R.id.menu_toolbar_layout).setVisible(true);
                        mMainBinding.toolBar.setSubtitle(Data.sAlbumItems.size() + " Album");
                    }
                    break;
                    case 2: {
                        mMenu.findItem(R.id.menu_toolbar_layout).setVisible(false);
                        mMenu.findItem(R.id.menu_toolbar_search).setCheckable(false);
                    }
                    break;
                    case 3: {
                        mMenu.findItem(R.id.menu_toolbar_search).setCheckable(false);
                    }
                    break;
                }
            }

            @Override
            public final void onPageScrollStateChanged(int state) {

            }
        });

        // 参数：开启抽屉的activity、DrawerLayout的对象、toolbar按钮打开关闭的对象、描述open drawer、描述close drawer
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mMainBinding.drawerLayout, mMainBinding.toolBar, R.string.open, R.string.close);
        // 添加抽屉按钮，通过点击按钮实现打开和关闭功能; 如果不想要抽屉按钮，只允许在侧边边界拉出侧边栏，可以不写此行代码
        mDrawerToggle.syncState();
        // 设置按钮的动画效果; 如果不想要打开关闭抽屉时的箭头动画效果，可以不写此行代码
        mMainBinding.drawerLayout.addDrawerListener(mDrawerToggle);

    }

    ////////////get///////////////////////get///////////////////////get/////////////////

    public final ImageView getNavHeaderImageView() {
        return mNavHeaderImageView;
    }

    public final MusicListFragment getMusicListFragment() {
        return mMusicListFragment;
    }

    public final MusicDetailFragment getMusicDetailFragment() {
        return mMusicDetailFragment;
    }

    public final AlbumListFragment getAlbumListFragment() {
        return mAlbumListFragment;
    }

    public final PlayListFragment getPlayListFragment() {
        return mPlayListFragment;
    }

    public final NotLeakHandler getHandler() {
        return mHandler;
    }

    public ActivityMainBinding getMainBinding() {
        return mMainBinding;
    }

    ////////////get///////////////////////get///////////////////////get/////////////////

    public final class NotLeakHandler extends Handler {
        @SuppressWarnings("unused")
        private WeakReference<MainActivity> mWeakReference;

        NotLeakHandler(MainActivity activity, Looper looper) {
            super(looper);
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public final void handleMessage(Message msg) {
            switch (msg.what) {
                case Values.HandlerWhat.LOAD_INTO_NAV_IMAGE: {
                    mWeakReference.get().runOnUiThread(() -> {
                        final Bitmap cover = (Bitmap) msg.obj;
                        GlideApp.with(mWeakReference.get())
                                .load(cover == null ? R.drawable.ic_audiotrack_24px : cover)
                                .transition(DrawableTransitionOptions.withCrossFade())
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
                default:
            }
        }

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void displayWelcomeMessage() {
        String welcomeMessage = mFirebaseRemoteConfig.getString("welcome_message");
        if (mFirebaseRemoteConfig.getBoolean("welcome_message_caps")) {
            welcomeMessage.toUpperCase(Locale.CHINESE);
        } else {
            welcomeMessage.toLowerCase(Locale.CHINESE);
        }
        Toast.makeText(this, welcomeMessage, Toast.LENGTH_SHORT).show();
    }
}
