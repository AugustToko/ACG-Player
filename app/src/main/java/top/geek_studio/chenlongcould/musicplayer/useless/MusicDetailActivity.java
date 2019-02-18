/*
 * ************************************************************
 * 文件：MusicDetailActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:46
 * 上次修改时间：2019年01月17日 17:28:59
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.useless;

//public final class MusicDetailActivity extends MyBaseActivity implements IStyle, SlidingUpPanelLayout.PanelSlideListener {

//    private static final String TAG = "MusicDetailActivity";
//
//    private boolean HAS_BIG = false;
//
//    private int DEF_TOP = -1;
//
//    private boolean HIDE_TOOLBAR = false;
//
//    private ImageView mMusicAlbumImage;
//
//    private ImageView mPrimaryBackground;
//
//    private ImageView mPrimaryBackground_down;
//
//    private SeekBar mSeekBar;
//
//    public NotLeakHandler mHandler;
//
//    private HandlerThread mHandlerThread;
//
//    private ImageButton mPlayButton;
//
//    private ImageButton mNextButton;
//
//    private ImageButton mPreviousButton;
//
//    private RecyclerView mRecyclerView;
//
//    private LinearLayoutManager mLinearLayoutManager;
//
//    private MainActivity mMainActivity;
//
//    private MusicListFragment mMusicListFragment;
//
//    private ConstraintLayout mInfoBody;
//
//    private CardView mCardView;
//
//    private TextView mIndexTextView;
//
//    private TextView mAlbumNameText;
//
//    private TextView mMusicNameText;
//
//    private ImageButton mRandomButton;
//
//    private ImageButton mRepeatButton;
//
//    private Toolbar mToolbar;
//
//    private AppBarLayout mAppBarLayout;
//
//    private TextView mLeftTime;
//
//    private TextView mRightTime;
//
//    private ImageView mRecyclerMask;
//
//    private TextView mNextWillText;
//
//    /**
//     * menu
//     */
//    private ImageButton mMenuButton;
//
//    private PopupMenu mPopupMenu;
//
//    private SlidingUpPanelLayout mSlidingUpPanelLayout;
//
//    public static boolean isVisBottom(RecyclerView recyclerView) {
//        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//        //屏幕中最后一个可见子项的position
//        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
//        //当前屏幕所看到的子项个数
//        int visibleItemCount = layoutManager.getChildCount();
//        //当前RecyclerView的所有子项个数
//        int totalItemCount = layoutManager.getItemCount();
//        //RecyclerView的滑动状态
//        int state = recyclerView.getScrollState();
//        if (visibleItemCount > 0 && lastVisibleItemPosition == totalItemCount - 1 && state == recyclerView.SCROLL_STATE_IDLE) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//
//        //从通知进入
//        if (Data.sActivities.size() == 0) {
//            startActivity(new Intent(MusicDetailActivity.this, SplashActivity.class));
//            finish();
//            return;
//        }
//
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.unused_activity_music_detail);
//
//        Data.sActivities.add(this);
//        mMainActivity = (MainActivity) Data.sActivities.get(0);
//
//        //statusBar color
//        try {
//            @SuppressLint("PrivateApi") Class decorViewClazz = Class.forName("com.android.internal.policy.DecorView");
//            Field field = decorViewClazz.getDeclaredField("mSemiTransparentStatusBarColor");
//            field.setAccessible(true);
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
//            field.setInt(getWindow().getDecorView(), Color.TRANSPARENT);  //改为透明
//        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
//            e.printStackTrace();
//        }
//
//        mHandlerThread = new HandlerThread("Handler Thread in MusicDetailActivity");
//        mHandlerThread.start();
//        mHandler = new NotLeakHandler(this, mHandlerThread.getLooper());
//
//        initView();
//
//        initData();
//    }
//
//    @Override
//    public void onBackPressed() {
//        if (HAS_BIG) {
//            mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
//        } else {
////            super.onBackPressed();            //scrollBody may case bug
//            finish();           //without animation(image trans)
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        GlideApp.with(this)
//                .load(Data.sCurrentMusicBitmap)
//                .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
//                .into(mMusicAlbumImage);
//
//
//        if (Values.Style.COLOR_CHANGED) {
//            recreate();
//        }
//        super.onResume();
//    }
//
//    @Override
//    protected void onDestroy() {
//        Data.sActivities.remove(this);
//        mHandlerThread.quitSafely();
//        super.onDestroy();
//    }
//
//    private void initAnimation() {
//        /*
//         * init view animation
//         * */
//        //default type is common, but the random button alpha is 1f(it means this button is on), so set animate
//        mRandomButton.setAlpha(0f);
//        mRepeatButton.setAlpha(0f);
//        mRandomButton.clearAnimation();
//        mRepeatButton.clearAnimation();
//        ValueAnimator animator = new ValueAnimator();
//        animator.setStartDelay(500);
//        animator.setDuration(300);
//        if (Values.CurrentData.CURRENT_PLAY_TYPE.equals(Values.TYPE_RANDOM)) {
//            animator.setFloatValues(0f, 1f);
//            animator.addUpdateListener(animation -> mRandomButton.setAlpha((Float) animation.getAnimatedValue()));
//            animator.addListener(new Animator.AnimatorListener() {
//                @Override
//                public void onAnimationStart(Animator animation) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mRandomButton.setAlpha(1f);
//                    mRandomButton.clearAnimation();
//                }
//
//                @Override
//                public void onAnimationCancel(Animator animation) {
//
//                }
//
//                @Override
//                public void onAnimationRepeat(Animator animation) {
//
//                }
//            });
//            animator.start();
//        } else {
//            animator.setFloatValues(0f, 0.3f);
//            animator.addUpdateListener(animation -> mRandomButton.setAlpha((Float) animation.getAnimatedValue()));
//            animator.addListener(new Animator.AnimatorListener() {
//                @Override
//                public void onAnimationStart(Animator animation) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mRandomButton.setAlpha(0.3f);
//                    mRandomButton.clearAnimation();
//                }
//
//                @Override
//                public void onAnimationCancel(Animator animation) {
//
//                }
//
//                @Override
//                public void onAnimationRepeat(Animator animation) {
//
//                }
//            });
//            animator.start();
//        }
//
//        switch (Values.CurrentData.CURRENT_PLAY_TYPE) {
//            case Values.TYPE_COMMON: {
//                mRepeatButton.setImageResource(R.drawable.ic_repeat_white_24dp);
//                animator.setFloatValues(0f, 0.3f);
//                animator.addUpdateListener(animation -> mRepeatButton.setAlpha((Float) animation.getAnimatedValue()));
//                animator.addListener(new Animator.AnimatorListener() {
//                    @Override
//                    public void onAnimationStart(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        mRepeatButton.setAlpha(0.3f);
//                        mRepeatButton.clearAnimation();
//                    }
//
//                    @Override
//                    public void onAnimationCancel(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animator animation) {
//
//                    }
//                });
//                animator.start();
//                break;
//            }
//            case Values.TYPE_REPEAT: {
//                mRepeatButton.setImageResource(R.drawable.ic_repeat_white_24dp);
//                animator.setFloatValues(0f, 1f);
//                animator.addUpdateListener(animation -> mRepeatButton.setAlpha((Float) animation.getAnimatedValue()));
//                animator.addListener(new Animator.AnimatorListener() {
//                    @Override
//                    public void onAnimationStart(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        mRepeatButton.setAlpha(1f);
//                        mRepeatButton.clearAnimation();
//                    }
//
//                    @Override
//                    public void onAnimationCancel(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animator animation) {
//
//                    }
//                });
//                animator.start();
//            }
//            break;
//            case Values.TYPE_REPEAT_ONE: {
//                mRepeatButton.setImageResource(R.drawable.ic_repeat_one_white_24dp);
//                animator.setFloatValues(0f, 1f);
//                animator.addUpdateListener(animation -> mRepeatButton.setAlpha((Float) animation.getAnimatedValue()));
//                animator.addListener(new Animator.AnimatorListener() {
//                    @Override
//                    public void onAnimationStart(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        mRepeatButton.setAlpha(1f);
//                        mRepeatButton.clearAnimation();
//                    }
//
//                    @Override
//                    public void onAnimationCancel(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animator animation) {
//
//                    }
//                });
//                animator.start();
//                break;
//            }
//        }
//
//        ScaleAnimation mPlayButtonScaleAnimation = new ScaleAnimation(0, mPlayButton.getScaleX(), 0, mPlayButton.getScaleY(),
//                Animation.RELATIVE_TO_SELF, mPlayButton.getScaleX() / 2, Animation.RELATIVE_TO_SELF, mPlayButton.getScaleX() / 2);
//        RotateAnimation mPlayButtonRotationAnimation = new RotateAnimation(-90f, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//
//        AnimationSet mPlayButtonAnimationSet = new AnimationSet(true);
//
//        mPlayButtonRotationAnimation.setDuration(300);
//        mPlayButtonRotationAnimation.setFillAfter(true);
//        mPlayButtonScaleAnimation.setDuration(300);
//        mPlayButtonScaleAnimation.setFillAfter(true);
//        mPlayButtonScaleAnimation.setStartOffset(500);
//        mPlayButtonRotationAnimation.setStartOffset(500);
//        mPlayButton.clearAnimation();
//
//        mPlayButtonAnimationSet.addAnimation(mPlayButtonRotationAnimation);
//        mPlayButtonAnimationSet.addAnimation(mPlayButtonScaleAnimation);
//        mPlayButtonAnimationSet.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                showToolbar();
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//
//        mPlayButton.setAnimation(mPlayButtonAnimationSet);
//
//        TranslateAnimation mCardViewTranslateAnimation = new TranslateAnimation(mCardView.getTranslationX(), mCardView.getTranslationX(), 500, mCardView.getTranslationY());
//        mCardViewTranslateAnimation.setDuration(300);
//        mCardViewTranslateAnimation.setFillAfter(true);
//        mCardView.clearAnimation();
//        mCardView.startAnimation(mCardViewTranslateAnimation);
//
//        TranslateAnimation mPreviousButtonTranslateAnimation = new TranslateAnimation(150, mPreviousButton.getTranslationX(), mPreviousButton.getTranslationY(), mPreviousButton.getTranslationY());
//        TranslateAnimation mNextButtonTranslateAnimation = new TranslateAnimation(-150, mNextButton.getTranslationX(), mNextButton.getTranslationY(), mNextButton.getTranslationY());
//        mPreviousButtonTranslateAnimation.setDuration(300);
//        mPreviousButtonTranslateAnimation.setFillAfter(true);
//        mNextButtonTranslateAnimation.setDuration(300);
//        mNextButtonTranslateAnimation.setFillAfter(true);
//        mPreviousButton.clearAnimation();
//        mNextButton.clearAnimation();
//        mNextButton.startAnimation(mNextButtonTranslateAnimation);
//        mPreviousButton.startAnimation(mPreviousButtonTranslateAnimation);
//
//    }
//
//    private void showToolbar() {
//        HIDE_TOOLBAR = false;
//        ValueAnimator anim = ValueAnimator.ofFloat(0, 1f);
//        anim.setDuration(300);
//        anim.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                mAppBarLayout.clearAnimation();
//            }
//
//            @Override
//            public void onAnimationStart(Animator animation) {
//                mAppBarLayout.setVisibility(View.VISIBLE);
//            }
//        });
//        anim.addUpdateListener(animation1 -> mAppBarLayout.setAlpha((Float) animation1.getAnimatedValue()));
//        anim.start();
//    }
//
//    private void hideToolbar() {
//        HIDE_TOOLBAR = true;
//        AlphaAnimation temp = new AlphaAnimation(1f, 0f);
//        temp.setDuration(300);
//        temp.setFillAfter(false);
//        temp.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                mAppBarLayout.clearAnimation();
//                mAppBarLayout.setAlpha(0f);
//                mAppBarLayout.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//        mAppBarLayout.startAnimation(temp);
//    }
//
//    private void initData() {
//        //init view data
//        mHandler.sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);
//        mHandler.sendEmptyMessage(Values.HandlerWhat.SEEK_BAR_UPDATE);
//        mHandler.sendEmptyMessage(Values.HandlerWhat.RECYCLER_SCROLL);
//
////        Intent intent = getIntent();
////        if (intent != null) {
////            String args = getIntent().getStringExtra("intent_args");
////            if (args != null) {
////                if (args.equals("by_clicked_body") || args.equals("clicked by navHeaderImage")) {
////                }
////            }
////        }
//
//        setInfoBar(Data.sCurrentMusicName, Data.sCurrentMusicAlbum);
//
//        if (Data.sMusicBinder.isPlayingMusic()) {
//            mPlayButton.setImageResource(R.drawable.ic_pause_black_24dp);
//        }
//
//        if (Data.sCurrentMusicBitmap != null) {
//            int temp = Data.sCurrentMusicBitmap.getPixel(Data.sCurrentMusicBitmap.getWidth() / 2, Data.sCurrentMusicBitmap.getHeight() / 2);
//            mSeekBar.getThumb().setColorFilter(temp, PorterDuff.Mode.SRC_ATOP);
//        }
//
////        Utils.Ui.setBlurEffect(this, Data.sCurrentMusicBitmap, mPrimaryBackground, mPrimaryBackground_down, mNextWillText);
//    }
//
//    private void initView() {
//        findView();
//
//        initStyle();
//
//        //load animations...
//        initAnimation();
//
//        mAppBarLayout.setVisibility(View.GONE);
//        HIDE_TOOLBAR = true;
//
//        mToolbar.setOnMenuItemClickListener(menuItem -> {
//            switch (menuItem.getItemId()) {
//                case R.id.menu_toolbar_fast_play: {
//                    Utils.SendSomeThing.sendPlay(MusicDetailActivity.this, ReceiverOnMusicPlay.CASE_TYPE_SHUFFLE, TAG);
//                }
//                break;
//            }
//            return false;
//        });
//
//        mToolbar.setNavigationOnClickListener(v -> finish());
//
//        mRepeatButton.setOnClickListener(v -> {
//
//            /*
//             * COMMON = 0f
//             * REPEAT = 1f
//             * REPEAT_ONE = 1f(another pic)
//             * */
//            ValueAnimator animator = new ValueAnimator();
//            animator.setDuration(300);
//            mRepeatButton.clearAnimation();
//            switch (Values.CurrentData.CURRENT_PLAY_TYPE) {
//                case Values.TYPE_COMMON: {
//                    Values.CurrentData.CURRENT_PLAY_TYPE = Values.TYPE_REPEAT;
//                    mRepeatButton.setImageResource(R.drawable.ic_repeat_white_24dp);
//                    animator.setFloatValues(0.3f, 1f);
//                    animator.addUpdateListener(animation -> mRepeatButton.setAlpha((Float) animation.getAnimatedValue()));
//                    animator.addListener(new Animator.AnimatorListener() {
//                        @Override
//                        public void onAnimationStart(Animator animation) {
//
//                        }
//
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            mRepeatButton.setAlpha(1f);
//                            mRepeatButton.clearAnimation();
//                        }
//
//                        @Override
//                        public void onAnimationCancel(Animator animation) {
//
//                        }
//
//                        @Override
//                        public void onAnimationRepeat(Animator animation) {
//
//                        }
//                    });
//                    animator.start();
//                    break;
//                }
//                case Values.TYPE_REPEAT: {
//                    Values.CurrentData.CURRENT_PLAY_TYPE = Values.TYPE_REPEAT_ONE;
//                    mRepeatButton.setImageResource(R.drawable.ic_repeat_one_white_24dp);
//                }
//                break;
//                case Values.TYPE_REPEAT_ONE: {
//                    Values.CurrentData.CURRENT_PLAY_TYPE = Values.TYPE_COMMON;
//                    mRepeatButton.setImageResource(R.drawable.ic_repeat_white_24dp);
//                    animator.setFloatValues(1f, 0.3f);
//                    animator.addUpdateListener(animation -> mRepeatButton.setAlpha((Float) animation.getAnimatedValue()));
//                    animator.addListener(new Animator.AnimatorListener() {
//                        @Override
//                        public void onAnimationStart(Animator animation) {
//
//                        }
//
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            mRepeatButton.setAlpha(0.3f);
//                            mRepeatButton.clearAnimation();
//                        }
//
//                        @Override
//                        public void onAnimationCancel(Animator animation) {
//
//                        }
//
//                        @Override
//                        public void onAnimationRepeat(Animator animation) {
//
//                        }
//                    });
//                    animator.start();
//                    break;
//                }
//            }
//        });
//
//        mToolbar.inflateMenu(R.menu.menu_toolbar_in_detail);
//
//        mMusicAlbumImage.setOnClickListener(v -> {
//
//            if (HIDE_TOOLBAR) {
//                showToolbar();
//            } else {
//                hideToolbar();
//            }
//        });
//
//        mRepeatButton.setOnLongClickListener(v -> {
//            AlertDialog.Builder builder = new AlertDialog.Builder(MusicDetailActivity.this);
//            builder.setTitle("Repeater");
//            builder.setMessage("Building...");
//            builder.setCancelable(true);
//            builder.show();
//            return false;
//        });
//
//        mRandomButton.setOnClickListener(v -> {
//            mRandomButton.clearAnimation();
//            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MusicDetailActivity.this).edit();
//            if (Values.CurrentData.CURRENT_PLAY_TYPE.equals(Values.TYPE_RANDOM)) {
//                Values.CurrentData.CURRENT_PLAY_TYPE = Values.TYPE_COMMON;
//                ValueAnimator animator = new ValueAnimator();
//                animator.setFloatValues(1f, 0.3f);
//                animator.setDuration(300);
//                animator.addUpdateListener(animation -> mRandomButton.setAlpha((Float) animation.getAnimatedValue()));
//                animator.addListener(new Animator.AnimatorListener() {
//                    @Override
//                    public void onAnimationStart(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        mRandomButton.setAlpha(0.3f);
//                        mRandomButton.clearAnimation();
//                        editor.putString(Values.SharedPrefsTag.INTENT_PLAY_TYPE, Values.TYPE_COMMON);
//                        editor.apply();
//                    }
//
//                    @Override
//                    public void onAnimationCancel(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animator animation) {
//
//                    }
//                });
//                animator.start();
//            } else {
//                Values.CurrentData.CURRENT_PLAY_TYPE = Values.TYPE_RANDOM;
//                ValueAnimator animator = new ValueAnimator();
//                animator.setFloatValues(0.3f, 1f);
//                animator.setDuration(300);
//                animator.addUpdateListener(animation -> mRandomButton.setAlpha((Float) animation.getAnimatedValue()));
//                animator.addListener(new Animator.AnimatorListener() {
//                    @Override
//                    public void onAnimationStart(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        mRandomButton.setAlpha(1f);
//                        mRandomButton.clearAnimation();
//                        editor.putString(Values.SharedPrefsTag.INTENT_PLAY_TYPE, Values.TYPE_RANDOM);
//                        editor.apply();
//                    }
//
//                    @Override
//                    public void onAnimationCancel(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animator animation) {
//
//                    }
//                });
//                animator.start();
//            }
//        });
//
//        /*---------------------- Menu -----------------------*/
//        mPopupMenu = new PopupMenu(this, mMenuButton);
//        Menu menu = mPopupMenu.getMenu();
//
//        //noinspection PointlessArithmeticExpression
//        menu.add(Menu.NONE, Menu.FIRST + 0, 0, getResources().getString(R.string.next_play));
//        menu.add(Menu.NONE, Menu.FIRST + 1, 0, "查看专辑");
//        menu.add(Menu.NONE, Menu.FIRST + 2, 0, "详细信息");
//
//        mMenuButton.setOnClickListener(v -> mPopupMenu.show());
//
//        mPopupMenu.setOnMenuItemClickListener(item -> {
//            switch (item.getItemId()) {
//                //noinspection PointlessArithmeticExpression
//                case Menu.FIRST + 0: {
//
//                }
//                break;
//                case Menu.FIRST + 1: {
//                    String albumName = Data.sCurrentMusicAlbum;
//                    Cursor cursor = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null,
//                            MediaStore.Audio.Albums.ALBUM + "= ?", new String[]{albumName}, null);
//
//                    //int MusicDetailActivity
//                    Intent intent = new Intent(MusicDetailActivity.this, AlbumDetailActivity.class);
//                    intent.putExtra("key", albumName);
//                    if (cursor != null) {
//                        cursor.moveToFirst();
//                        int id = Integer.parseInt(cursor.getString(0));
//                        intent.putExtra("_id", id);
//                        cursor.close();
//                    }
//                    startActivity(intent);
//                }
//                break;
//                case Menu.FIRST + 2: {
//                    Intent intent = new Intent(MusicDetailActivity.this, PublicActivity.class);
//                    startActivity(intent);
//                }
//            }
//            return false;
//        });
//        /*---------------------- Menu -----------------------*/
//
//        mInfoBody.setOnClickListener(v -> {
////            if (!HAS_BIG) {
////                infoBodyScrollUp();
////            } else {
////                infoBodyScrollDown();
////                mHandler.sendEmptyMessage(Values.HandlerWhat.RECYCLER_SCROLL);
////            }
//
//            if (mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
//                mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
//            } else {
//                mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
//            }
//
//        });
//
//        mMusicListFragment = (MusicListFragment) ((MainActivity) Data.sActivities.get(0)).getFragmentList().get(0);
//
//        mLinearLayoutManager = new LinearLayoutManager(this);
//
//        mRecyclerView.setLayoutManager(mLinearLayoutManager);
////        mRecyclerView.addItemDecoration(new DividerItemDecoration(mMainActivity, DividerItemDecoration.VERTICAL));
//        mRecyclerView.setAdapter(new MyWaitListAdapter(mMainActivity, Data.sMusicItems));
//
//        mSlidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
//            @Override
//            public void onPanelSlide(View panel, float slideOffset) {
//            }
//
//            @Override
//            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
//
//            }
//        });
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            mRecyclerView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
//                if (isVisBottom(mRecyclerView)) Log.d(TAG, "onScrollChange: is bottom");
//            });
//        }
//
//        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (fromUser) {
//                    Data.sMusicBinder.seekTo(progress);
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                MusicDetailActivity musicDetailActivity = (MusicDetailActivity) Data.sActivities.get(1);
////                Utils.HandlerSend.sendToMain(Values.HandlerWhat.SET_BUTTON_PLAY);
//                musicDetailActivity.setButtonTypePlay();
//
//                Data.sMusicBinder.seekTo(seekBar.getProgress());
//                Data.sMusicBinder.playMusic();
//            }
//        });
//
//        //just pause or play
//        mPlayButton.setOnClickListener(v -> {
//            if (Data.sMusicBinder.isPlayingMusic()) {
//                Utils.SendSomeThing.sendPause(MusicDetailActivity.this);
//            } else {
//                Data.sMusicBinder.playMusic();
//                Utils.Ui.setPlayButtonNowPlaying();
//            }
//        });
//
//        mNextButton.setOnClickListener(v -> Utils.SendSomeThing.sendPlay(MusicDetailActivity.this, 6, null));
//
//        mNextButton.setOnLongClickListener(v -> {
//            Values.BUTTON_PRESSED = true;
//            int nowPosition = mSeekBar.getProgress() + Data.sMusicBinder.getDuration() / 20;
//            if (nowPosition >= mSeekBar.getMax()) {
//                nowPosition = mSeekBar.getMax();
//            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                mSeekBar.setProgress(nowPosition, true);
//            } else {
//                mSeekBar.setProgress(nowPosition);
//            }
//            Data.sMusicBinder.seekTo(nowPosition);
//            Values.BUTTON_PRESSED = false;
//            return true;
//        });
//
//        mPreviousButton.setOnClickListener(v -> Utils.SendSomeThing.sendPlay(MusicDetailActivity.this, 6, null));
//
//        mPreviousButton.setOnLongClickListener(v -> {
//            int nowPosition = mSeekBar.getProgress() - Data.sMusicBinder.getDuration() / 20;
//            if (nowPosition <= 0) {
//                nowPosition = 0;
//            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                mSeekBar.setProgress(nowPosition, true);
//            } else {
//                mSeekBar.setProgress(nowPosition);
//            }
//            Data.sMusicBinder.seekTo(nowPosition);
//            return true;
//        });
//
//    }
//
//    @Override
//    public void initStyle() {
//        mMusicNameText.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));
//        mAlbumNameText.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));
//        if (Values.Style.NIGHT_MODE) {
//            mRecyclerMask.setImageResource(R.drawable.ramp_bg_dark);
//            mRecyclerMask.setAlpha(0.1f);
//        } else {
//            mRecyclerMask.setImageResource(R.drawable.ramp_bg_light);
//            mRecyclerMask.setAlpha(0.5f);
//        }
//    }
//
//    private void findView() {
//        mMusicAlbumImage = findViewById(R.id.activity_music_detail_album_image);
//        mPrimaryBackground = findViewById(R.id.activity_music_detail_primary_background);
//        mPrimaryBackground_down = findViewById(R.id.activity_music_detail_primary_background_down);
//        mSeekBar = findViewById(R.id.seekBar);
//        mNextButton = findViewById(R.id.activity_music_detail_image_next_button);
//        mPreviousButton = findViewById(R.id.activity_music_detail_image_previous_button);
//        mPlayButton = findViewById(R.id.activity_music_detail_image_play_button);
//        mRecyclerView = findViewById(R.id.recycler_view);
//        mInfoBody = findViewById(R.id.item_layout);
//        mCardView = findViewById(R.id.activity_music_detail_card_view);
//        mIndexTextView = findViewById(R.id.item_index_text);
//        mAlbumNameText = findViewById(R.id.item_text_one);
//        mMusicNameText = findViewById(R.id.item_main_text);
//        mMenuButton = findViewById(R.id.item_menu);
//        mRandomButton = findViewById(R.id.activity_music_detail_image_random_button);
//        mRepeatButton = findViewById(R.id.activity_music_detail_image_repeat_button);
//        mToolbar = findViewById(R.id.activity_music_detail_toolbar);
//        mAppBarLayout = findViewById(R.id.activity_music_detail_appbar);
//        mLeftTime = findViewById(R.id.activity_music_detail_left_text);
//        mRightTime = findViewById(R.id.activity_music_detail_right_text);
//        mRecyclerMask = findViewById(R.id.recycler_mask);
//        mSlidingUpPanelLayout = findViewById(R.id.activity_detail_sliding_layout);
//        mNextWillText = findViewById(R.id.next_will_text);
//    }
//
//    //scroll infoBar Down
//    private void infoBodyScrollDown() {
//        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mCardView.getLayoutParams();
//        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(params);
//
//        ValueAnimator anim = ValueAnimator.ofInt(params.topMargin, DEF_TOP);
//        anim.setDuration(250);
//        anim.addUpdateListener(animation -> {
//            int currentValue = (Integer) animation.getAnimatedValue();
//            layoutParams.setMargins(params.leftMargin, currentValue, params.rightMargin, params.bottomMargin);
//            mCardView.setLayoutParams(layoutParams);
//            mCardView.requestLayout();
//        });
//        anim.start();
//        HAS_BIG = false;
//    }
//
//    public final void setButtonTypePause() {
//        runOnUiThread(() -> mPlayButton.setImageResource(R.drawable.ic_play_arrow_black_24dp));
//    }
//
//    public final void setButtonTypePlay() {
//        runOnUiThread(() -> mPlayButton.setImageResource(R.drawable.ic_pause_black_24dp));
//    }
//
//    //scroll infoBar Up
//    private void infoBodyScrollUp() {
//        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mCardView.getLayoutParams();
//        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(params);
//
//        DEF_TOP = params.topMargin;
//
//        ValueAnimator anim = ValueAnimator.ofInt(params.topMargin, params.leftMargin * 2);
//        anim.setDuration(250);
//        anim.addUpdateListener(animation -> {
//            int currentValue = (Integer) animation.getAnimatedValue();
//            layoutParams.setMargins(params.leftMargin, currentValue, params.rightMargin, params.bottomMargin);
//            mCardView.setLayoutParams(layoutParams);
//            mCardView.requestLayout();
//        });
//        anim.start();
//        HAS_BIG = true;
//    }
//
//    //set infoBar set AlbumImage set PrimaryBackground
//    public final void setCurrentSongInfo(String name, String albumName, byte[] cover) {
//        runOnUiThread(() -> {
//            GlideApp.with(MusicDetailActivity.this)
//                    .load(cover)
//                    .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
//                    .centerCrop()
//                    .into(mMusicAlbumImage);
////            Utils.Ui.setBlurEffect(this, cover, mPrimaryBackground, mPrimaryBackground_down, mNextWillText);
//            setInfoBar(name, albumName);
//        });
//    }
//
//    public final NotLeakHandler getHandler() {
//        return mHandler;
//    }
//
//    public final RecyclerView getRecyclerView() {
//        return mRecyclerView;
//    }
//
//    public final SeekBar getSeekBar() {
//        return mSeekBar;
//    }
//
//    //small infoBar
//    public final void setInfoBar(String name, String albumName) {
//        mMusicNameText.setText(name);
//        mAlbumNameText.setText(albumName);
////        mIndexTextView.setText(String.valueOf(Values.CurrentData.CURRENT_MUSIC_INDEX));
//
//    }
//
//    @Override
//    public void onPanelSlide(View panel, float slideOffset) {
//
//    }
//
//    @Override
//    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
//    }
//
//    public final class NotLeakHandler extends Handler {
//        private WeakReference<MusicDetailActivity> mWeakReference;
//
//        NotLeakHandler(MusicDetailActivity activity, Looper looper) {
//            super(looper);
//            mWeakReference = new WeakReference<>(activity);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case Values.HandlerWhat.INIT_SEEK_BAR: {
//                    runOnUiThread(() -> {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                            mWeakReference.get().mSeekBar.setProgress(0, true);
//                        } else {
//                            mWeakReference.get().mSeekBar.setProgress(0);
//                        }
//                        SimpleDateFormat sd = new SimpleDateFormat("mm:ss", Locale.CHINESE);
//                        mWeakReference.get().mRightTime.setText(String.valueOf(sd.format(new Date(Data.sMusicBinder.getDuration()))));
//                        mWeakReference.get().mSeekBar.setMax(Data.sMusicBinder.getDuration());
//                    });
//
//                }
//                break;
//                case Values.HandlerWhat.SEEK_BAR_UPDATE: {
//                    runOnUiThread(() -> {
//                        //点击body 或 music 正在播放 才可以进行seekBar更新
//                        if (Data.sMusicBinder.isPlayingMusic() || Data.sActivities.size() > 0) {
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                                mWeakReference.get().mSeekBar.setProgress(Data.sMusicBinder.getCurrentPosition(), true);
//                            } else {
//                                mWeakReference.get().mSeekBar.setProgress(Data.sMusicBinder.getCurrentPosition());
//                            }
//                            SimpleDateFormat sd = new SimpleDateFormat("mm:ss", Locale.CHINESE);
//                            mWeakReference.get().mLeftTime.setText(String.valueOf(sd.format(new Date(Data.sMusicBinder.getCurrentPosition()))));
//                        }
//
//                        //循环更新 0.5s 一次
//                        mWeakReference.get().mHandler.sendEmptyMessageDelayed(Values.HandlerWhat.SEEK_BAR_UPDATE, 500);
//                    });
//                }
//                break;
//                case Values.HandlerWhat.RECYCLER_SCROLL: {
//                    mWeakReference.get().runOnUiThread(() -> {
//                        mLinearLayoutManager.scrollToPositionWithOffset(Values.CurrentData.CURRENT_MUSIC_INDEX == Data.sMusicItems.size() ? Values.CurrentData.CURRENT_MUSIC_INDEX : Values.CurrentData.CURRENT_MUSIC_INDEX + 1, 0);
//                    });
//                }
//                break;
//                default:
//            }
//        }
//    }
//}
