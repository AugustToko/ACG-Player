/*
 * ************************************************************
 * 文件：MainActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月05日 20:16:39
 * 上次修改时间：2018年12月05日 20:16:11
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.Adapters.MyPagerAdapter;
import top.geek_studio.chenlongcould.musicplayer.Adapters.MyRecyclerAdapter2AlbumList;
import top.geek_studio.chenlongcould.musicplayer.BroadCasts.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Fragments.AlbumListFragment;
import top.geek_studio.chenlongcould.musicplayer.Fragments.MusicDetailFragment;
import top.geek_studio.chenlongcould.musicplayer.Fragments.MusicListFragment;
import top.geek_studio.chenlongcould.musicplayer.Fragments.PlayListFragment;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.IStyle;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.MyApplication;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Service.MyMusicService;
import top.geek_studio.chenlongcould.musicplayer.Utils.NotificationUtils;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class MainActivity extends MyBaseCompatActivity implements IStyle {

    private static final String TAG = "MainActivity";

    /**
     * @see Message#what
     */
    public static final int UP = 50070;
    /**
     * 检测当前 slide 的位置.
     * 当滑动 slide {@link SlidingUpPanelLayout} 时, 迅速点击可滑动区域外, slide 会卡住.
     * 但 slide 状态会变为 {@link SlidingUpPanelLayout.PanelState#COLLAPSED}.
     * 故立此 FLAG.
     */
    public static float CURRENT_SLIDE_OFFSET = 1;
    public static final int DOWN = 50071;
    public static final int ENABLE_TOUCH = 50072;
    public static final int SET_VIEWPAGER_BG = 50073;
    public static final int SET_TOOLBAR_TITLE = 50075;

    public static boolean ANIMATION_FLAG = true;

    private boolean TOOLBAR_CLICKED = false;

    private boolean BACK_PRESSED = false;
    /**
     * ALL FRAGMENTS
     */
    private List<Fragment> mFragmentList = new ArrayList<>();

    private NotLeakHandler mHandler;

    /**
     * UI
     * */
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MyPagerAdapter mPagerAdapter;
    private final ArrayList<String> mTitles = new ArrayList<>();
    private Toolbar mToolbar;
    private AppBarLayout mAppBarLayout;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ImageView mNavHeaderImageView;
    private Menu mMenu;
    private SearchView mSearchView;
    private SlidingUpPanelLayout mSlidingUpPanelLayout;

    /**
     * ----------------- fragment(s) ----------------------
     */
    private MusicListFragment mMusicListFragment;
    private AlbumListFragment mAlbumListFragment;
    private PlayListFragment mPlayListFragment;
    private MusicDetailFragment mMusicDetailFragment;

    /**
     * onXXX
     * At Override
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(Values.LogTAG.LIFT_TAG, "onCreate: " + TAG);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);

        if (Data.sActivities.isEmpty()) Data.sActivities.add(this);

        mHandler = new NotLeakHandler(this, ((MyApplication) getApplication()).getCustomLooper());

        initView();

        Toast.makeText(this, "Loading", Toast.LENGTH_SHORT).show();

        //service
        Intent intent = new Intent(this, MyMusicService.class);
        startService(intent);
        bindService(intent, Data.sServiceConnection, BIND_AUTO_CREATE);

        new MyDataLoadTask(this).execute();

        if (savedInstanceState != null) {
            mViewPager.setCurrentItem(savedInstanceState.getInt("viewpage", 0), true);
        }

    }

    @Override
    protected void onDestroy() {
        Log.d(Values.LogTAG.LIFT_TAG, "onDestroy: " + TAG);
        goToBg();
        super.onDestroy();
    }

    /**
     * exit app with out stop music
     */
    private void goToBg() {
        AlbumListFragment.VIEW_HAS_LOAD = false;

        // FIXME: 2018/12/4 leak
        try {
            if (Values.SERVICE_RUNNING) unbindService(Data.sServiceConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Data.sActivities.remove(this);
        GlideApp.get(this).clearMemory();
        finish();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof MusicListFragment)
            ((MusicListFragment) fragment).getHandler().sendEmptyMessage(Values.HandlerWhat.INIT_MUSIC_LIST_DONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("viewpage", mViewPager.getCurrentItem());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Values.STYLE_CHANGED) {
            initStyle();
            Values.STYLE_CHANGED = false;
        }
    }

    @Override
    public void onBackPressed() {

        //1
        if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        //2
        if (getMusicDetailFragment().getSlidingUpPanelLayout().getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || MusicDetailFragment.CURRENT_SLIDE_OFFSET != 1) {
            getMusicDetailFragment().getSlidingUpPanelLayout().setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return;
        }

        //3
        if (mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || CURRENT_SLIDE_OFFSET != 1) {
            mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return;
        }

        //4
        if (BACK_PRESSED) {
            finish();
        } else {
            BACK_PRESSED = true;
            Toast.makeText(this, "Press again to exit!", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> BACK_PRESSED = false, 2000);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_toolbar, mMenu);

        MenuItem searchItem = menu.findItem(R.id.menu_toolbar_search);

        mSearchView = (SearchView) searchItem.getActionView();

        mSearchView.setIconifiedByDefault(true);
        mSearchView.setQueryHint("Enter music name...");

        mSearchView.setIconified(true);
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mMusicListFragment.getRecyclerView().stopScroll();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterData(newText);
                return true;
            }
        });

        mSearchView.setOnSearchClickListener(v -> Data.sMusicItemsBackUp.addAll(Data.sMusicItems));

        mSearchView.setOnCloseListener(() -> {
            Data.sMusicItemsBackUp.clear();
            return false;
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_toolbar_exit: {
                exitApp();
            }
            break;

            /*--------------- 快速 随机 播放 ----------------*/
            case R.id.menu_toolbar_fast_play: {
                Data.sHistoryPlayIndex.clear();
                Utils.SendSomeThing.sendPlay(this, ReceiverOnMusicPlay.TYPE_SHUFFLE);
            }
            break;

            case R.id.menu_toolbar_linear: {
                SharedPreferences.Editor editor = MyApplication.mDefSharedPreferences.edit();
                editor.putInt(Values.SharedPrefsTag.ALBUM_LIST_DISPLAY_TYPE, MyRecyclerAdapter2AlbumList.LINEAR_TYPE);
                editor.apply();
                mPagerAdapter.notifyDataSetChanged();
                mAlbumListFragment.setRecyclerViewData();
            }
            break;

            case R.id.menu_toolbar_grid: {
                SharedPreferences.Editor editor = MyApplication.mDefSharedPreferences.edit();
                editor.putInt(Values.SharedPrefsTag.ALBUM_LIST_DISPLAY_TYPE, MyRecyclerAdapter2AlbumList.GRID_TYPE);
                editor.apply();
                mPagerAdapter.notifyDataSetChanged();
                mAlbumListFragment.setRecyclerViewData();
            }
            break;
        }
        return true;
    }

    /**
     * 根据输入框中的值来过滤数据并更新RecyclerView
     *
     * @param filterStr fileName
     */
    private void filterData(String filterStr) {
        // TODO: 2018/11/19 searchView while in the album page...
        if (TextUtils.isEmpty(filterStr)) {
            Data.sMusicItems.clear();
            Data.sMusicItems.addAll(Data.sMusicItemsBackUp);
            mMusicListFragment.getAdapter().notifyDataSetChanged();
        } else {
            Data.sMusicItems.clear();

            //algorithm
            for (MusicItem item : Data.sMusicItemsBackUp) {
                String name = item.getMusicName();
                if (name.toLowerCase().contains(filterStr.toLowerCase()) || name.toUpperCase().contains(filterStr.toUpperCase())) {
                    Data.sMusicItems.add(item);
                }
            }
            mMusicListFragment.getAdapter().notifyDataSetChanged();
        }
    }

    /**
     * init fragments
     */
    private void initFragmentData() {
        runOnUiThread(() -> {
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

            final ArrayList<String> titles = new ArrayList<>();
            titles.add(tab_1);
            titles.add(tab_2);
            titles.add(tab_3);

            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(0)));
            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(1)));
            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(2)));

            mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), mFragmentList, mTitles);
            mViewPager.setOffscreenPageLimit(2);
            mTabLayout.setupWithViewPager(mViewPager);
            mViewPager.setAdapter(mPagerAdapter);
            Values.CurrentData.CURRENT_PAGE_INDEX = 0;
        });
    }

    public final void exitApp() {
        AlbumListFragment.VIEW_HAS_LOAD = false;

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(NotificationUtils.ID);

        Data.sHistoryPlayIndex.clear();
        Data.sMusicItemsBackUp.clear();
        Data.sMusicItems.clear();
        Data.sAlbumItems.clear();

        Data.sMusicBinder.stopMusic();
        Data.sMusicBinder.release();

        stopService(new Intent(MainActivity.this, MyMusicService.class));
        unbindService(Data.sServiceConnection);
        Data.sMusicBinder = null;

        Data.sActivities.remove(this);
        GlideApp.get(this).clearMemory();
        finish();
    }

    /**---------------------- getter --------------------*/
    public final List<Fragment> getFragmentList() {
        return mFragmentList;
    }

    public final ImageView getNavHeaderImageView() {
        return mNavHeaderImageView;
    }

    public Toolbar getToolbar() {
        return mToolbar;
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

    public final SlidingUpPanelLayout getSlidingUpPanelLayout() {
        return mSlidingUpPanelLayout;
    }

    public final NotLeakHandler getHandler() {
        return mHandler;
    }

    private void initView() {

        findView();

        initStyle();

        //根据recycler view的滚动程度, 来判断如何返回顶部
        mToolbar.setOnClickListener(v -> {
            if (TOOLBAR_CLICKED) {
                switch (Values.CurrentData.CURRENT_PAGE_INDEX) {
                    case 0: {
                        if (Values.CurrentData.CURRENT_BIND_INDEX_MUSIC_LIST > 20) {
                            mMusicListFragment.getRecyclerView().scrollToPosition(0);
                        } else {
                            mMusicListFragment.getRecyclerView().smoothScrollToPosition(0);
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
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_24px);
        }

        mNavHeaderImageView = mNavigationView.getHeaderView(0).findViewById(R.id.nav_view_image);
        mNavHeaderImageView.setOnClickListener(v -> {
            if (getMusicDetailFragment().getSlidingUpPanelLayout().getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                getMusicDetailFragment().getSlidingUpPanelLayout().setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                return;
            }

            if (mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                return;
            }

            mDrawerLayout.closeDrawers();
        });

        mSlidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

                CURRENT_SLIDE_OFFSET = slideOffset;

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

                    mTabLayout.setVisibility(View.GONE);
                    mTabLayout.setVisibility(View.GONE);

                    if (AlbumListFragment.VIEW_HAS_LOAD)
                        mAlbumListFragment.visibleOrGone(View.GONE);
                    if (PlayListFragment.HAS_LOAD) mPlayListFragment.visibleOrGone(View.GONE);

                    //start animation
                    if (ANIMATION_FLAG) {
                        ANIMATION_FLAG = false;
                        getMusicDetailFragment().initAnimation();
                    }

                } else {
                    mTabLayout.setVisibility(View.VISIBLE);
                    mTabLayout.setVisibility(View.VISIBLE);
                    mMusicDetailFragment.getNowPlayingBody().setVisibility(View.VISIBLE);
                    if (AlbumListFragment.VIEW_HAS_LOAD)
                        mAlbumListFragment.visibleOrGone(View.VISIBLE);
                    if (PlayListFragment.HAS_LOAD) mPlayListFragment.visibleOrGone(View.VISIBLE);
                    mMusicListFragment.visibleOrGone(View.VISIBLE);
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.show(mAlbumListFragment).show(mPlayListFragment).show(mPlayListFragment);
                    fragmentTransaction.commit();
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED)
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                else if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED)
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
        });

        //First no music no slide
        if (Data.sMusicBinder != null) {
            if (Data.sMusicBinder.isPlayingMusic())
                mSlidingUpPanelLayout.setTouchEnabled(true);
            else
                mSlidingUpPanelLayout.setTouchEnabled(false);
        } else {
            mSlidingUpPanelLayout.setTouchEnabled(false);
        }


        mNavigationView.setNavigationItemSelectedListener(menuItem -> {
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
                    Intent intent = new Intent(MainActivity.this, AboutLic.class);
                    startActivity(intent);
                }
                break;
                case R.id.debug: {
                    startActivity(new Intent(MainActivity.this, UiTest.class));
                }
            }
            return true;
        });

        // 实例代码
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
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
//                mToolbar.setBackgroundColor(evaluate);
//                mTabLayout.setBackgroundColor(evaluate);
            }

            @Override
            public void onPageSelected(int position) {
                Values.CurrentData.CURRENT_PAGE_INDEX = position;
                switch (position) {
                    case 0: {
                        mMenu.findItem(R.id.menu_toolbar_layout).setVisible(false);
                        mMenu.findItem(R.id.menu_toolbar_search).setVisible(true);
                        mToolbar.setSubtitle(Data.sMusicItems.size() + " Songs");
                    }
                    break;
                    case 1: {
                        mMenu.findItem(R.id.menu_toolbar_layout).setVisible(true);
                        mMenu.findItem(R.id.menu_toolbar_search).setVisible(false);
                        mToolbar.setSubtitle(Data.sAlbumItems.size() + " Albums");
                    }
                    break;
                    case 2: {
                        mMenu.findItem(R.id.menu_toolbar_layout).setVisible(false);
                        mMenu.findItem(R.id.menu_toolbar_search).setVisible(false);
                    }
                    break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // 参数：开启抽屉的activity、DrawerLayout的对象、toolbar按钮打开关闭的对象、描述open drawer、描述close drawer
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.open, R.string.close);
        // 添加抽屉按钮，通过点击按钮实现打开和关闭功能; 如果不想要抽屉按钮，只允许在侧边边界拉出侧边栏，可以不写此行代码
        mDrawerToggle.syncState();
        // 设置按钮的动画效果; 如果不想要打开关闭抽屉时的箭头动画效果，可以不写此行代码
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    /**
     * findViewById
     */
    private void findView() {
        mToolbar = findViewById(R.id.activity_main_tool_bar);
        mDrawerLayout = findViewById(R.id.activity_main_drawer_layout);
        mNavigationView = findViewById(R.id.activity_main_nav_view);
        mTabLayout = findViewById(R.id.tab_layout);
        mViewPager = findViewById(R.id.view_pager);
        mAppBarLayout = findViewById(R.id.activity_main_appbar);
        mSlidingUpPanelLayout = findViewById(R.id.activity_main_sliding_layout);
//        mBackgroundImage = findViewById(R.id.back_ground_image);
    }

    @Override
    public void initStyle() {
        Utils.Ui.setAppBarColor(this, mAppBarLayout, mToolbar);
        int color = PreferenceManager.getDefaultSharedPreferences(this).getInt(Values.ColorInt.PRIMARY_COLOR, Color.parseColor("#008577"));
        mTabLayout.setBackgroundColor(color);
    }

    static class MyDataLoadTask extends AsyncTask<Void, Void, Integer> {

        private WeakReference<MainActivity> mWeakReference;

        MyDataLoadTask(MainActivity mainActivity) {
            mWeakReference = new WeakReference<>(mainActivity);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            if (Data.sMusicItems.isEmpty()) {
                /*---------------------- init Data!!!! -------------------*/
                final Cursor cursor = mWeakReference.get().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
                if (cursor != null && cursor.moveToFirst()) {
                    //没有歌曲直接退出app
                    if (cursor.getCount() == 0) {
                        return -2;
                    }
                    do {
                        final String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

                        final File file = new File(path);
                        if (!file.exists()) {
                            Log.e(TAG, "onAttach: song file: " + path + " does not exits, skip this!!!");
                            break;
                        }

                        final String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
                        final String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                        final String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                        final int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                        final int size = (int) cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                        final int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
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

                    } while (cursor.moveToNext());
                    cursor.close();
                    Values.MUSIC_DATA_INIT_DONE = true;

                    return 0;
                } else {

                    //cursor null or getCount == 0
                    return -1;
                }
            } else {
                //already has data -> initFragment
                mWeakReference.get().getHandler().sendEmptyMessage(Values.HandlerWhat.INIT_FRAGMENT);
                Values.MUSIC_DATA_INIT_DONE = true;
                return 0;
            }

        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == -1)
                Utils.Ui.fastToast(mWeakReference.get(), "cursor == null or moveToFirst Fail");
            if (result == -2) {
                Utils.Ui.fastToast(mWeakReference.get(), "Can not find any music!");
                mWeakReference.get().mHandler.postDelayed(() -> mWeakReference.get().exitApp(), 1000);
            }

            mWeakReference.get().getToolbar().setSubtitle(Data.sMusicItems.size() + " Songs");

        }
    }

    public final class NotLeakHandler extends Handler {
        @SuppressWarnings("unused")
        private WeakReference<MainActivity> mWeakReference;

        NotLeakHandler(MainActivity activity, Looper looper) {
            super(looper);
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Values.HandlerWhat.INIT_FRAGMENT: {
                    Log.d(TAG, "handleMessage: initFragmentData");
                    initFragmentData();
                }
                break;
                case Values.HandlerWhat.LOAD_INTO_NAV_IMAGE: {
                    mWeakReference.get().runOnUiThread(() -> {
                        Bitmap cover = (Bitmap) msg.obj;
                        GlideApp.with(mWeakReference.get())
                                .load(cover == null ? R.drawable.ic_audiotrack_24px : cover)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .centerCrop().into(mNavHeaderImageView);
                    });
                }
                break;

                case ENABLE_TOUCH: {
                    mSlidingUpPanelLayout.setTouchEnabled(true);
                }
                break;

                case UP: {
                    mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                }
                break;

                case DOWN: {
                    mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
                break;
                default:
            }
        }

    }
}
