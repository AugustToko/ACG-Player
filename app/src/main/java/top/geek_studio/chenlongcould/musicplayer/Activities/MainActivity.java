/*
 * ************************************************************
 * 文件：MainActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月01日 16:21:06
 * 上次修改时间：2018年12月01日 16:20:49
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
import android.os.HandlerThread;
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

import jp.wasabeef.glide.transformations.BlurTransformation;
import top.geek_studio.chenlongcould.musicplayer.Adapters.MyPagerAdapter;
import top.geek_studio.chenlongcould.musicplayer.Adapters.MyRecyclerAdapter2AlbumList;
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

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public final class MainActivity extends MyBaseCompatActivity implements IStyle {

    private static final String TAG = "MainActivity";

    public static final int UP = 50070;

    public static final int DOWN = 50071;

    public static final int ENABLE_TOUCH = 50072;

    public static final int SET_VIEWPAGER_BG = 50073;

    public static boolean ANIMATION_FLAG = true;

    private boolean TOOLBAR_CLICKED = false;

    private boolean BACK_PRESSED = false;

    private List<Fragment> mFragmentList = new ArrayList<>();

    private NotLeakHandler mHandler;

    private HandlerThread mHandlerThread;

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

    private ImageView mBackgroundImage;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);

        Data.sActivities.add(this);

        //service
        Intent intent = new Intent(this, MyMusicService.class);
        startService(intent);
        Values.BIND_SERVICE = bindService(intent, Data.sServiceConnection, BIND_AUTO_CREATE);

        mHandlerThread = new HandlerThread("Handler Thread in MainActivity");
        mHandlerThread.start();
        mHandler = new NotLeakHandler(this, mHandlerThread.getLooper());

        initView();

        Toast.makeText(this, "Loading", Toast.LENGTH_SHORT).show();
        new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... voids) {
                if (Data.sMusicItems.isEmpty()) {
                    /*---------------------- init Data!!!! -------------------*/
                    Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
                    if (cursor != null && cursor.moveToFirst()) {
                        //没有歌曲直接退出app
                        if (cursor.getCount() == 0) {
                            return -2;
                        }
                        do {
                            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

                            File file = new File(path);
                            if (!file.exists()) {
                                Log.e(TAG, "onAttach: song file: " + path + " does not exits, skip this!!!");
                                break;
                            }

                            Log.i(TAG, "onAttach: music path: " + path);

                            String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
                            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                            String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                            int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                            int size = (int) cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                            int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                            String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                            long addTime = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));

                            MusicItem.Builder builder = new MusicItem.Builder(id, name, path)
                                    .musicAlbum(albumName)
                                    .addTime((int) addTime)
                                    .artist(artist)
                                    .duration(duration)
                                    .mimeName(mimeType)
                                    .size(size);

                            Data.sMusicItems.add(builder.build());
                            Data.sMusicItemsBackUp.add(builder.build());

                        } while (cursor.moveToNext());
                        cursor.close();
                        Values.MUSIC_DATA_INIT_DONE = true;
                    } else {
                        return -1;
                    }
                }
                return 0;
            }

            @Override
            protected void onPostExecute(Integer result) {
                if (result == -1)
                    Utils.Ui.fastToast(MainActivity.this, "cursor == null or moveToFirst Fail");
                if (result == -2) {
                    Utils.Ui.fastToast(MainActivity.this, "Can not find any music!");
                    mHandler.postDelayed(() -> exitApp(), 1000);
                }
                mToolbar.setSubtitle(Data.sMusicItems.size() + " Songs");
            }
        }.execute();

        if (savedInstanceState != null) {
            mViewPager.setCurrentItem(savedInstanceState.getInt("viewpage", 0), true);
        }

    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        Log.d(TAG, "onAttachFragment: " + fragment.getClass().getName());
        super.onAttachFragment(fragment);
        if (fragment instanceof MusicDetailFragment)
            mMusicDetailFragment.getHandler().sendEmptyMessage(Values.HandlerWhat.INIT_MUSIC_LIST_DONE);
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
    protected void onDestroy() {
        mHandlerThread.quitSafely();
        Data.sActivities.remove(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        if (getMusicDetailFragment().getSlidingUpPanelLayout().getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            getMusicDetailFragment().getSlidingUpPanelLayout().setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return;
        }

        if (mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return;
        }

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
//                mSearchView.setIconified(true);
//                filterData(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterData(newText);
                return true;
            }
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
                Utils.Audio.shufflePlayback();
            }
            break;

            case R.id.menu_toolbar_linear: {
                SharedPreferences.Editor editor = MyApplication.mDefSharedPreferences.edit();
                editor.putInt(Values.SharedPrefsTag.ALBUM_LIST_DISPLAY_TYPE, MyRecyclerAdapter2AlbumList.LINEAR_TYPE);
                editor.apply();
                mPagerAdapter.notifyDataSetChanged();
            }
            break;

            case R.id.menu_toolbar_grid: {
                SharedPreferences.Editor editor = MyApplication.mDefSharedPreferences.edit();
                editor.putInt(Values.SharedPrefsTag.ALBUM_LIST_DISPLAY_TYPE, MyRecyclerAdapter2AlbumList.GRID_TYPE);
                editor.apply();
                mPagerAdapter.notifyDataSetChanged();
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
    private void initData() {

        runOnUiThread(() -> {
            String tab_1 = getResources().getString(R.string.music);
            mTitles.add(tab_1);
            mMusicListFragment = MusicListFragment.newInstance();
            mFragmentList.add(mMusicListFragment);

            mMusicDetailFragment = MusicDetailFragment.newInstance();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frame_wait, mMusicDetailFragment);
            transaction.commit();

            String tab_2 = getResources().getString(R.string.album);
            mTitles.add(tab_2);
            mAlbumListFragment = AlbumListFragment.newInstance();
            mFragmentList.add(mAlbumListFragment);

            String tab_3 = getResources().getString(R.string.play_list);
            mTitles.add(tab_3);
            mPlayListFragment = PlayListFragment.newInstance(2);
            mFragmentList.add(mPlayListFragment);

            ArrayList<String> titles = new ArrayList<>();
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
//                            mViewPager.setCurrentItem(0);
            Values.CurrentData.CURRENT_PAGE_INDEX = 0;

        });

    }

    public final void exitApp() {
        mHandlerThread.quitSafely();
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(NotificationUtils.ID);

        mMusicDetailFragment.onDestroyView();

        Data.sHistoryPlayIndex.clear();
        Data.sMusicItemsBackUp.clear();
        Data.sMusicItems.clear();

        Data.sMusicBinder.stopMusic();
        Data.sMusicBinder.release();

        Data.sMusicBinder = null;
        stopService(new Intent(MainActivity.this, MyMusicService.class));
        unbindService(Data.sServiceConnection);

        Data.sActivities.remove(this);
        finish();
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
                float current = 1 - slideOffset;
                mMusicDetailFragment.getNowPlayingBody().setAlpha(current);
                if (current == 0) {
                    mMusicDetailFragment.getNowPlayingBody().setVisibility(View.GONE);
                } else {
                    mMusicDetailFragment.getNowPlayingBody().setVisibility(View.VISIBLE);
                }

                if (current == 0) {
                    if (ANIMATION_FLAG) getMusicDetailFragment().initAnimation();
                    ANIMATION_FLAG = false;
                }

                if (current == 1) {
                    mMusicDetailFragment.setDefAnimation();
                    mMusicDetailFragment.clearAnimations();
                    ANIMATION_FLAG = true;
                }

                //滑动一半的时候, 进行 recycler 跳转
                if (current == 0.7) {
                    getMusicDetailFragment().getHandler().sendEmptyMessage(Values.HandlerWhat.RECYCLER_SCROLL);
                }

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
            }
        });

        //First no music no slide
        mSlidingUpPanelLayout.setTouchEnabled(false);

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
                        mToolbar.setSubtitle(mAlbumListFragment.getAlbumList().size() + " Albums");
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
        mBackgroundImage = findViewById(R.id.back_ground_image);
    }

    @Override
    public void initStyle() {
        Utils.Ui.setAppBarColor(this, mAppBarLayout, mToolbar);
        int color = PreferenceManager.getDefaultSharedPreferences(this).getInt(Values.ColorInt.PRIMARY_COLOR, Color.parseColor("#008577"));
        mTabLayout.setBackgroundColor(color);
    }

    public final NotLeakHandler getHandler() {
        return mHandler;
    }

    /**
     * getter
     */
    public final List<Fragment> getFragmentList() {
        return mFragmentList;
    }

    public final ImageView getNavHeaderImageView() {
        return mNavHeaderImageView;
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
                case Values.HandlerWhat.ON_SERVICE_START: {
                    initData();
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

                case SET_VIEWPAGER_BG: {
                    mBackgroundImage.post(() -> GlideApp
                            .with(mWeakReference.get())
                            .load(Data.sCurrentMusicBitmap)
                            .apply(bitmapTransform(new BlurTransformation(10, 10)))
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(mBackgroundImage));
                }
                break;

                default:
            }
        }


    }

}
