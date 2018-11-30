/*
 * ************************************************************
 * 文件：MusicDetailFragment.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月30日 20:36:09
 * 上次修改时间：2018年11月30日 20:35:27
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.wasabeef.glide.transformations.BlurTransformation;
import top.geek_studio.chenlongcould.musicplayer.Activities.AlbumDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.PublicActivity;
import top.geek_studio.chenlongcould.musicplayer.Adapters.MyWaitListAdapter;
import top.geek_studio.chenlongcould.musicplayer.BroadCasts.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.IStyle;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class MusicDetailFragment extends Fragment implements IStyle {

    public static final int SET_SEEK_BAR_COLOR = 9001;

    private static final String TAG = "MusicDetailActivity";
    public MusicDetailFragment.NotLeakHandler mHandler;
    private boolean HIDE_TOOLBAR = false;
    private ImageView mMusicAlbumImage;
    private ImageView mPrimaryBackground;
    private ImageView mPrimaryBackground_down;
    private SeekBar mSeekBar;
    private HandlerThread mHandlerThread;

    private ImageButton mPlayButton;

    private ImageButton mNextButton;

    private ImageButton mPreviousButton;

    private RecyclerView mRecyclerView;

    private LinearLayoutManager mLinearLayoutManager;

    private MainActivity mMainActivity;

    private ConstraintLayout mCurrentInfoBody;

    private CardView mCardView;

    private TextView mIndexTextView;

    private TextView mCurrentAlbumNameText;

    private TextView mCurrentMusicNameText;

    private ImageButton mRandomButton;

    private ImageButton mRepeatButton;

    private Toolbar mToolbar;

    private AppBarLayout mAppBarLayout;

    private TextView mLeftTime;

    private TextView mRightTime;

    private ImageView mRecyclerMask;

    private TextView mNextWillText;

    /**
     * menu
     */
    private ImageButton mMenuButton;

    private PopupMenu mPopupMenu;

    private SlidingUpPanelLayout mSlidingUpPanelLayout;

    /**
     * ----------------- playing info ---------------------
     */
    private TextView mNowPlayingSongText;

    private ImageView mNowPlayingSongImage;

    private TextView mNowPlayingSongAlbumText;

    private ImageView mNowPlayingStatusImage;

    private ImageView mNowPlayingBackgroundImage;

    private ConstraintLayout mNowPlayingBody;

    //实例化一个fragment
    public static MusicDetailFragment newInstance() {
        return new MusicDetailFragment();
    }

    public static boolean isVisBottom(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        //屏幕中最后一个可见子项的position
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
        //当前屏幕所看到的子项个数
        int visibleItemCount = layoutManager.getChildCount();
        //当前RecyclerView的所有子项个数
        int totalItemCount = layoutManager.getItemCount();
        //RecyclerView的滑动状态
        int state = recyclerView.getScrollState();
        if (visibleItemCount > 0 && lastVisibleItemPosition == totalItemCount - 1 && state == recyclerView.SCROLL_STATE_IDLE) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainActivity = (MainActivity) context;

        mHandlerThread = new HandlerThread("Handler Thread in MusicDetailActivity");
        mHandlerThread.start();
        mHandler = new MusicDetailFragment.NotLeakHandler(mMainActivity, mHandlerThread.getLooper());

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_music_detail_new, container, false);

        initView(view);

        initData();

        return view;
    }

    public final void setData() {
        //small infoBar
        mCurrentMusicNameText.setText(Data.sCurrentMusicName);
        mCurrentAlbumNameText.setText(Data.sCurrentMusicAlbum);
    }

    private void initData() {
        //init view data
        mHandler.sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);
        mHandler.sendEmptyMessage(Values.HandlerWhat.SEEK_BAR_UPDATE);
        mHandler.sendEmptyMessage(Values.HandlerWhat.RECYCLER_SCROLL);

//        Intent intent = getIntent();
//        if (intent != null) {
//            String args = getIntent().getStringExtra("intent_args");
//            if (args != null) {
//                if (args.equals("by_clicked_body") || args.equals("clicked by navHeaderImage")) {
//                }
//            }
//        }

        setData();

        if (Values.MUSIC_PLAYING) {
            mPlayButton.setImageResource(R.drawable.ic_pause_black_24dp);
        }

        if (Data.sCurrentMusicBitmap != null) {
            int temp = Data.sCurrentMusicBitmap.getPixel(Data.sCurrentMusicBitmap.getWidth() / 2, Data.sCurrentMusicBitmap.getHeight() / 2);
            mSeekBar.getThumb().setColorFilter(temp, PorterDuff.Mode.SRC_ATOP);
        }

        // TODO: 2018/11/30
//        Utils.Ui.setBlurEffect(mMainActivity, Data.sCurrentMusicBitmap, mPrimaryBackground, mPrimaryBackground_down, mNextWillText);
    }

    private void findView(View view) {
        mMusicAlbumImage = view.findViewById(R.id.activity_music_detail_album_image);
        mPrimaryBackground = view.findViewById(R.id.activity_music_detail_primary_background);
        mPrimaryBackground_down = view.findViewById(R.id.activity_music_detail_primary_background_down);
        mSeekBar = view.findViewById(R.id.seekBar);
        mNextButton = view.findViewById(R.id.activity_music_detail_image_next_button);
        mPreviousButton = view.findViewById(R.id.activity_music_detail_image_previous_button);
        mPlayButton = view.findViewById(R.id.activity_music_detail_image_play_button);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mCurrentInfoBody = view.findViewById(R.id.item_layout);
        mCardView = view.findViewById(R.id.activity_music_detail_card_view);
        mIndexTextView = view.findViewById(R.id.item_index_text);
        mCurrentAlbumNameText = view.findViewById(R.id.item_text_one);
        mCurrentMusicNameText = view.findViewById(R.id.item_main_text);
        mMenuButton = view.findViewById(R.id.item_menu);
        mRandomButton = view.findViewById(R.id.activity_music_detail_image_random_button);
        mRepeatButton = view.findViewById(R.id.activity_music_detail_image_repeat_button);
        mToolbar = view.findViewById(R.id.activity_music_detail_toolbar);
        mAppBarLayout = view.findViewById(R.id.activity_music_detail_appbar);
        mLeftTime = view.findViewById(R.id.activity_music_detail_left_text);
        mRightTime = view.findViewById(R.id.activity_music_detail_right_text);
        mRecyclerMask = view.findViewById(R.id.recycler_mask);
        mSlidingUpPanelLayout = view.findViewById(R.id.activity_detail_sliding_layout);
        mNextWillText = view.findViewById(R.id.next_will_text);

        mNowPlayingSongAlbumText = view.findViewById(R.id.activity_main_now_playing_album_name);
        mNowPlayingBody = view.findViewById(R.id.current_info);
        mNowPlayingStatusImage = view.findViewById(R.id.activity_main_info_bar_status_image);
        mNowPlayingBackgroundImage = view.findViewById(R.id.current_info_background);
        mNowPlayingSongText = view.findViewById(R.id.activity_main_now_playing_name);
        mNowPlayingSongImage = view.findViewById(R.id.recycler_item_clover_image);
    }

    public ConstraintLayout getNowPlayingBody() {
        return mNowPlayingBody;
    }

    public void initAnimation() {
        /*
         * init view animation
         * */
        //default type is common, but the random button alpha is 1f(it means this button is on), so set animate
        mRandomButton.setAlpha(0f);
        mRepeatButton.setAlpha(0f);
        mRandomButton.clearAnimation();
        mRepeatButton.clearAnimation();
        ValueAnimator animator = new ValueAnimator();
        animator.setStartDelay(500);
        animator.setDuration(300);
        if (Values.CurrentData.CURRENT_PLAY_TYPE.equals(Values.TYPE_RANDOM)) {
            animator.setFloatValues(0f, 1f);
            animator.addUpdateListener(animation -> mRandomButton.setAlpha((Float) animation.getAnimatedValue()));
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mRandomButton.setAlpha(1f);
                    mRandomButton.clearAnimation();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        } else {
            animator.setFloatValues(0f, 0.3f);
            animator.addUpdateListener(animation -> mRandomButton.setAlpha((Float) animation.getAnimatedValue()));
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mRandomButton.setAlpha(0.3f);
                    mRandomButton.clearAnimation();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        }

        switch (Values.CurrentData.CURRENT_AUTO_NEXT_TYPE) {
            case Values.TYPE_COMMON: {
                mRepeatButton.setImageResource(R.drawable.ic_repeat_white_24dp);
                animator.setFloatValues(0f, 0.3f);
                animator.addUpdateListener(animation -> mRepeatButton.setAlpha((Float) animation.getAnimatedValue()));
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mRepeatButton.setAlpha(0.3f);
                        mRepeatButton.clearAnimation();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animator.start();
                break;
            }
            case Values.TYPE_REPEAT: {
                mRepeatButton.setImageResource(R.drawable.ic_repeat_white_24dp);
                animator.setFloatValues(0f, 1f);
                animator.addUpdateListener(animation -> mRepeatButton.setAlpha((Float) animation.getAnimatedValue()));
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mRepeatButton.setAlpha(1f);
                        mRepeatButton.clearAnimation();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animator.start();
            }
            break;
            case Values.TYPE_REPEAT_ONE: {
                mRepeatButton.setImageResource(R.drawable.ic_repeat_one_white_24dp);
                animator.setFloatValues(0f, 1f);
                animator.addUpdateListener(animation -> mRepeatButton.setAlpha((Float) animation.getAnimatedValue()));
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mRepeatButton.setAlpha(1f);
                        mRepeatButton.clearAnimation();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animator.start();
                break;
            }
        }

        ScaleAnimation mPlayButtonScaleAnimation = new ScaleAnimation(0, mPlayButton.getScaleX(), 0, mPlayButton.getScaleY(),
                Animation.RELATIVE_TO_SELF, mPlayButton.getScaleX() / 2, Animation.RELATIVE_TO_SELF, mPlayButton.getScaleX() / 2);
        RotateAnimation mPlayButtonRotationAnimation = new RotateAnimation(-90f, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        AnimationSet mPlayButtonAnimationSet = new AnimationSet(true);

        mPlayButtonRotationAnimation.setDuration(300);
        mPlayButtonRotationAnimation.setFillAfter(true);
        mPlayButtonScaleAnimation.setDuration(300);
        mPlayButtonScaleAnimation.setFillAfter(true);
        mPlayButtonScaleAnimation.setStartOffset(500);
        mPlayButtonRotationAnimation.setStartOffset(500);
        mPlayButton.clearAnimation();

        mPlayButtonAnimationSet.addAnimation(mPlayButtonRotationAnimation);
        mPlayButtonAnimationSet.addAnimation(mPlayButtonScaleAnimation);
        mPlayButtonAnimationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mPlayButton.setAnimation(mPlayButtonAnimationSet);

//        TranslateAnimation mCardViewTranslateAnimation = new TranslateAnimation(mCardView.getTranslationX(), mCardView.getTranslationX(), 500, mCardView.getTranslationY());
//        mCardViewTranslateAnimation.setDuration(300);
//        mCardViewTranslateAnimation.setFillAfter(true);
//        mCardView.clearAnimation();
//        mCardView.startAnimation(mCardViewTranslateAnimation);

        TranslateAnimation mPreviousButtonTranslateAnimation = new TranslateAnimation(150, mPreviousButton.getTranslationX(), mPreviousButton.getTranslationY(), mPreviousButton.getTranslationY());
        TranslateAnimation mNextButtonTranslateAnimation = new TranslateAnimation(-150, mNextButton.getTranslationX(), mNextButton.getTranslationY(), mNextButton.getTranslationY());
        mPreviousButtonTranslateAnimation.setDuration(300);
        mPreviousButtonTranslateAnimation.setFillAfter(true);
        mNextButtonTranslateAnimation.setDuration(300);
        mNextButtonTranslateAnimation.setFillAfter(true);
        mPreviousButton.clearAnimation();
        mNextButton.clearAnimation();
        mNextButton.startAnimation(mNextButtonTranslateAnimation);
        mPreviousButton.startAnimation(mPreviousButtonTranslateAnimation);

        // TODO: 2018/11/11 more animation... such as AlbumCover (in MusicDetailActivity)
    }

    private void initView(View view) {
        findView(view);

        initStyle();

        // TODO: 2018/11/30 在上拉完成时， 开始动画
        //load animations...
//        initAnimation();

        mPlayButton.setTranslationX(0);
        mPlayButton.setTranslationY(0);

        /*------------------toolbar--------------------*/
        mToolbar.inflateMenu(R.menu.menu_toolbar_in_detail);

        mToolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.menu_toolbar_fast_play: {
                    Utils.SendSomeThing.sendPlay(mMainActivity, ReceiverOnMusicPlay.TYPE_SHUFFLE);
                }
                break;
            }
            return false;
        });

        mToolbar.setNavigationOnClickListener(v -> {
            mMainActivity.getHandler().sendEmptyMessage(MainActivity.DOWN);
        });

        /*-------------------button---------------------*/
        mRepeatButton.setOnClickListener(v -> {

            /*
             * COMMON = 0f
             * REPEAT = 1f
             * REPEAT_ONE = 1f(another pic)
             * */
            ValueAnimator animator = new ValueAnimator();
            animator.setDuration(300);
            mRepeatButton.clearAnimation();
            switch (Values.CurrentData.CURRENT_AUTO_NEXT_TYPE) {
                case Values.TYPE_COMMON: {
                    Values.CurrentData.CURRENT_AUTO_NEXT_TYPE = Values.TYPE_REPEAT;
                    mRepeatButton.setImageResource(R.drawable.ic_repeat_white_24dp);
                    animator.setFloatValues(0.3f, 1f);
                    animator.addUpdateListener(animation -> mRepeatButton.setAlpha((Float) animation.getAnimatedValue()));
                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mRepeatButton.setAlpha(1f);
                            mRepeatButton.clearAnimation();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    animator.start();
                    break;
                }
                case Values.TYPE_REPEAT: {
                    Values.CurrentData.CURRENT_AUTO_NEXT_TYPE = Values.TYPE_REPEAT_ONE;
                    mRepeatButton.setImageResource(R.drawable.ic_repeat_one_white_24dp);
                }
                break;
                case Values.TYPE_REPEAT_ONE: {
                    Values.CurrentData.CURRENT_AUTO_NEXT_TYPE = Values.TYPE_COMMON;
                    mRepeatButton.setImageResource(R.drawable.ic_repeat_white_24dp);
                    animator.setFloatValues(1f, 0.3f);
                    animator.addUpdateListener(animation -> mRepeatButton.setAlpha((Float) animation.getAnimatedValue()));
                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mRepeatButton.setAlpha(0.3f);
                            mRepeatButton.clearAnimation();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    animator.start();
                    break;
                }
            }
        });

        mRepeatButton.setOnLongClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
            builder.setTitle("Repeater");
            builder.setMessage("Building...");
            builder.setCancelable(true);
            builder.show();
            return false;
        });

        mRandomButton.setOnClickListener(v -> {
            mRandomButton.clearAnimation();
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mMainActivity).edit();
            if (Values.CurrentData.CURRENT_PLAY_TYPE.equals(Values.TYPE_RANDOM)) {
                Values.CurrentData.CURRENT_PLAY_TYPE = Values.TYPE_COMMON;
                ValueAnimator animator = new ValueAnimator();
                animator.setFloatValues(1f, 0.3f);
                animator.setDuration(300);
                animator.addUpdateListener(animation -> mRandomButton.setAlpha((Float) animation.getAnimatedValue()));
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mRandomButton.setAlpha(0.3f);
                        mRandomButton.clearAnimation();
                        editor.putString(Values.SharedPrefsTag.PLAY_TYPE, Values.TYPE_COMMON);
                        editor.apply();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animator.start();
            } else {
                Values.CurrentData.CURRENT_PLAY_TYPE = Values.TYPE_RANDOM;
                ValueAnimator animator = new ValueAnimator();
                animator.setFloatValues(0.3f, 1f);
                animator.setDuration(300);
                animator.addUpdateListener(animation -> mRandomButton.setAlpha((Float) animation.getAnimatedValue()));
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mRandomButton.setAlpha(1f);
                        mRandomButton.clearAnimation();
                        editor.putString(Values.SharedPrefsTag.PLAY_TYPE, Values.TYPE_RANDOM);
                        editor.apply();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animator.start();
            }
        });

        /*-------------------image----------------------*/
        mMusicAlbumImage.setOnClickListener(v -> {

            if (HIDE_TOOLBAR) {
                showToolbar();
            } else {
                hideToolbar();
            }
        });

        /*---------------------- Menu -----------------------*/
        mPopupMenu = new PopupMenu(mMainActivity, mMenuButton);

        Menu menu = mPopupMenu.getMenu();

        mMenuButton.setOnClickListener(v -> mPopupMenu.show());

        mPopupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                //noinspection PointlessArithmeticExpression
                case Menu.FIRST + 0: {

                }
                break;
                case Menu.FIRST + 1: {
                    String albumName = Data.sCurrentMusicAlbum;
                    Cursor cursor = mMainActivity.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null,
                            MediaStore.Audio.Albums.ALBUM + "= ?", new String[]{albumName}, null);

                    //int MusicDetailActivity
                    Intent intent = new Intent(mMainActivity, AlbumDetailActivity.class);
                    intent.putExtra("key", albumName);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int id = Integer.parseInt(cursor.getString(0));
                        intent.putExtra("_id", id);
                        cursor.close();
                    }
                    startActivity(intent);
                }
                break;
                case Menu.FIRST + 2: {
                    Intent intent = new Intent(mMainActivity, PublicActivity.class);
                    startActivity(intent);
                }
            }
            return false;
        });

        //noinspection PointlessArithmeticExpression
        menu.add(Menu.NONE, Menu.FIRST + 0, 0, getResources().getString(R.string.next_play));
        menu.add(Menu.NONE, Menu.FIRST + 1, 0, "查看专辑");
        menu.add(Menu.NONE, Menu.FIRST + 2, 0, "详细信息");

        /*--------------------new panel----------------------*/
        mCurrentInfoBody.setOnClickListener(v -> {

            if (mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            } else {
                mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }

        });

        mNowPlayingBody.setOnClickListener(v -> mMainActivity.getHandler().sendEmptyMessage(MainActivity.UP));

        mLinearLayoutManager = new LinearLayoutManager(mMainActivity);

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(mMainActivity, DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(new MyWaitListAdapter(mMainActivity, Data.sMusicItems));

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Data.sMusicBinder.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Data.sMusicBinder.seekTo(seekBar.getProgress());
                Data.sMusicBinder.playMusic();
                mHandler.sendEmptyMessage(Values.HandlerWhat.SET_BUTTON_PLAY);
            }
        });

        //just pause or play
        mPlayButton.setOnClickListener(v -> {
            if (Values.MUSIC_PLAYING) {
                Utils.SendSomeThing.sendPause(mMainActivity);
            } else {
                Data.sMusicBinder.playMusic();
                Utils.Ui.setPlayButtonNowPlaying();
            }
        });

        mNextButton.setOnClickListener(v -> Utils.SendSomeThing.sendPlay(mMainActivity, 6));

        mNextButton.setOnLongClickListener(v -> {
            Values.BUTTON_PRESSED = true;
            int nowPosition = mSeekBar.getProgress() + Data.sMusicBinder.getDuration() / 20;
            if (nowPosition >= mSeekBar.getMax()) {
                nowPosition = mSeekBar.getMax();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mSeekBar.setProgress(nowPosition, true);
            } else {
                mSeekBar.setProgress(nowPosition);
            }
            Data.sMusicBinder.seekTo(nowPosition);
            Values.BUTTON_PRESSED = false;
            return true;
        });

        mPreviousButton.setOnClickListener(v -> Utils.SendSomeThing.sendPlay(mMainActivity, 5));

        mPreviousButton.setOnLongClickListener(v -> {
            int nowPosition = mSeekBar.getProgress() - Data.sMusicBinder.getDuration() / 20;
            if (nowPosition <= 0) {
                nowPosition = 0;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mSeekBar.setProgress(nowPosition, true);
            } else {
                mSeekBar.setProgress(nowPosition);
            }
            Data.sMusicBinder.seekTo(nowPosition);
            return true;
        });

        mNowPlayingStatusImage.setOnClickListener(v -> {
            //判断是否播放过, 如没有默认随机播放
            if (Values.HAS_PLAYED) {
                if (Values.MUSIC_PLAYING) {
                    Utils.SendSomeThing.sendPause(mMainActivity);
                } else {
                    Utils.SendSomeThing.sendPlay(mMainActivity, 3);
                }
            } else {
                Toast.makeText(mMainActivity, "Shuffle Playback!", Toast.LENGTH_SHORT).show();
                Data.sHistoryPlayIndex.clear();
                Utils.Audio.shufflePlayback();
            }
        });

        mNowPlayingBody.setOnClickListener(v -> {
            if (Values.HAS_PLAYED) {

                // FIXME: 2018/11/23 If scrolling, the recyclerView may case a bug from makeSceneTransitionAnimation
                if (mMainActivity.getMusicListFragment().getRecyclerView() != null) {
                    mMainActivity.getMusicListFragment().getRecyclerView().stopScroll();
                }
                if (mMainActivity.getAlbumListFragment().getRecyclerView() != null) {
                    mMainActivity.getAlbumListFragment().getRecyclerView().stopScroll();
                }

                mMainActivity.getHandler().sendEmptyMessage(MainActivity.UP);

            } else {
                Utils.Ui.fastToast(mMainActivity, "No music playing.");
            }
        });

        mSlidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset == 1) {
                    mMainActivity.getSlidingUpPanelLayout().setTouchEnabled(false);
                }
                if (slideOffset == 0) {
                    mMainActivity.getSlidingUpPanelLayout().setTouchEnabled(true);
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

            }
        });

    }

    @Override
    public void initStyle() {
        mCurrentMusicNameText.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));
        mCurrentAlbumNameText.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));
        if (Values.Style.NIGHT_MODE) {
            mRecyclerMask.setImageResource(R.drawable.ramp_bg_dark);
            mRecyclerMask.setAlpha(0.1f);
        } else {
            mRecyclerMask.setImageResource(R.drawable.ramp_bg_light);
            mRecyclerMask.setAlpha(0.5f);
        }
    }

    //in this Fragment
    private void showToolbar() {
        HIDE_TOOLBAR = false;
        ValueAnimator anim = ValueAnimator.ofFloat(0, 1f);
        anim.setDuration(300);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAppBarLayout.clearAnimation();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mAppBarLayout.setVisibility(View.VISIBLE);
            }
        });
        anim.addUpdateListener(animation1 -> mAppBarLayout.setAlpha((Float) animation1.getAnimatedValue()));
        anim.start();
    }

    private void hideToolbar() {
        HIDE_TOOLBAR = true;
        AlphaAnimation temp = new AlphaAnimation(1f, 0f);
        temp.setDuration(300);
        temp.setFillAfter(false);
        temp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mAppBarLayout.clearAnimation();
                mAppBarLayout.setAlpha(0f);
                mAppBarLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mAppBarLayout.startAnimation(temp);
    }

    //set infoBar set AlbumImage set PrimaryBackground
    public final void setCurrentInfo(String name, String albumName, byte[] cover) {
        mMainActivity.runOnUiThread(() -> {
            mCurrentMusicNameText.setText(name);
            mCurrentAlbumNameText.setText(albumName);

            GlideApp.with(mMainActivity)
                    .load(cover)
                    .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                    .centerCrop()
                    .into(mMusicAlbumImage);
            Utils.Ui.setBlurEffect(mMainActivity, cover, mPrimaryBackground, mPrimaryBackground_down, mNextWillText);
        });
    }

    public final void setCurrentInfo(String name, String albumName, Bitmap cover) {
        mMainActivity.runOnUiThread(() -> {
            mCurrentMusicNameText.setText(name);
            mCurrentAlbumNameText.setText(albumName);

            GlideApp.with(mMainActivity)
                    .load(cover)
                    .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                    .centerCrop()
                    .into(mMusicAlbumImage);
            Utils.Ui.setBlurEffect(mMainActivity, cover, mPrimaryBackground, mPrimaryBackground_down, mNextWillText);
        });
    }


    public final MusicDetailFragment.NotLeakHandler getHandler() {
        return mHandler;
    }

    /**
     * set Info (auto put in data.)
     *
     * @param songName  music name
     * @param albumName music album name
     * @param songPath  music path
     * @param cover     music cover image, it is @NullAble(some types of music do not have cover)
     * @param args      oth params(if "reload", do not need to set InfoBar again)
     */
    public void setSlideInfo(String songName, String albumName, String songPath, @Nullable Bitmap cover, String... args) {

        mMainActivity.runOnUiThread(() -> {
            mNowPlayingSongText.setText(songName);
            mNowPlayingSongAlbumText.setText(albumName);

            if (cover != null) {
                //color set

                GlideApp.with(mMainActivity).load(cover).transition(DrawableTransitionOptions.withCrossFade()).into(mNowPlayingSongImage);
                GlideApp.with(mMainActivity).load(cover).transition(DrawableTransitionOptions.withCrossFade()).into(mMainActivity.getNavHeaderImageView());

//                Palette.Builder paletteBuilder = Palette.from(Data.sCurrentMusicBitmap);
//                paletteBuilder.generate(palette -> {
//                    if (palette != null) {
//                        Palette.Swatch vibrant = palette.getVibrantSwatch();
//                        if (!Utils.Ui.isColorLight(vibrant == null ? Color.TRANSPARENT : vibrant.getRgb())) {
//                            mNowPlayingSongText.setTextColor(Data.sDefTextColorStateList);
//                            mNowPlayingSongAlbumText.setTextColor(Data.sDefTextColorStateList);
//                            mNowPlayingStatusImage.setColorFilter(Color.BLACK);
//                        } else {
//                            mNowPlayingSongText.setTextColor(Color.WHITE);
//                            mNowPlayingSongAlbumText.setTextColor(Color.WHITE);
//                            mNowPlayingStatusImage.setColorFilter(Color.WHITE);
//                        }
//                    }
//                });

                int currentBright = Utils.Ui.getBright(cover);

                //InfoBar background color AND text color balance
                if (currentBright > (255 / 2)) {
                    mNowPlayingSongText.setTextColor(Color.BLACK);
                    mNowPlayingSongAlbumText.setTextColor(Color.BLACK);
                    mNowPlayingStatusImage.setColorFilter(Color.BLACK);

                    mRandomButton.setColorFilter(Color.BLACK);
                    mRepeatButton.setColorFilter(Color.BLACK);
                    mNextButton.setColorFilter(Color.BLACK);
                    mPreviousButton.setColorFilter(Color.BLACK);
                    mLeftTime.setTextColor(Color.BLACK);
                    mRightTime.setTextColor(Color.BLACK);
                } else {
                    mNowPlayingSongText.setTextColor(Color.WHITE);
                    mNowPlayingSongAlbumText.setTextColor(Color.WHITE);
                    mNowPlayingStatusImage.setColorFilter(Color.WHITE);

                    mRandomButton.setColorFilter(Color.WHITE);
                    mRepeatButton.setColorFilter(Color.WHITE);
                    mNextButton.setColorFilter(Color.WHITE);
                    mPreviousButton.setColorFilter(Color.WHITE);
                    mLeftTime.setTextColor(Color.WHITE);
                    mRightTime.setTextColor(Color.WHITE);
                }

                GlideApp.with(mMainActivity).load(cover)
                        .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                        .apply(bitmapTransform(new BlurTransformation(30, 20)))
                        .override(100, 100)
                        .into(mNowPlayingBackgroundImage);
            }

            Utils.Ui.setPlayButtonNowPlaying();
        });
    }

    public SlidingUpPanelLayout getSlidingUpPanelLayout() {
        return mSlidingUpPanelLayout;
    }

    public final SeekBar getSeekBar() {
        return mSeekBar;
    }

    @Override
    public void onDestroyView() {
        mHandlerThread.quitSafely();
        super.onDestroyView();
    }

    public final class NotLeakHandler extends Handler {
        private WeakReference<MainActivity> mWeakReference;

        NotLeakHandler(MainActivity activity, Looper looper) {
            super(looper);
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Values.HandlerWhat.INIT_SEEK_BAR: {
                    mWeakReference.get().runOnUiThread(() -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            mSeekBar.setProgress(0, true);
                        } else {
                            mSeekBar.setProgress(0);
                        }
                        SimpleDateFormat sd = new SimpleDateFormat("mm:ss", Locale.CHINESE);
                        mRightTime.setText(String.valueOf(sd.format(new Date(Data.sMusicBinder.getDuration()))));
                        mSeekBar.setMax(Data.sMusicBinder.getDuration());
                    });

                }
                break;
                case Values.HandlerWhat.SEEK_BAR_UPDATE: {
                    mWeakReference.get().runOnUiThread(() -> {
                        //点击body 或 music 正在播放 才可以进行seekBar更新
                        if (Data.sMusicBinder.isPlayingMusic()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                mSeekBar.setProgress(Data.sMusicBinder.getCurrentPosition(), true);
                            } else {
                                mSeekBar.setProgress(Data.sMusicBinder.getCurrentPosition());
                            }
                            SimpleDateFormat sd = new SimpleDateFormat("mm:ss", Locale.CHINESE);
                            mLeftTime.setText(String.valueOf(sd.format(new Date(Data.sMusicBinder.getCurrentPosition()))));
                        }

                        //循环更新 0.5s 一次
                        mHandler.sendEmptyMessageDelayed(Values.HandlerWhat.SEEK_BAR_UPDATE, 500);
                    });
                }
                break;
                case Values.HandlerWhat.SET_SEEK_STYLE: {
                    // TODO: 2018/11/28 need?? ------yes!
                }
                break;
                case Values.HandlerWhat.RECYCLER_SCROLL: {
                    mWeakReference.get().runOnUiThread(() -> mLinearLayoutManager.scrollToPositionWithOffset(Values.CurrentData.CURRENT_MUSIC_INDEX == Data.sMusicItems.size() ? Values.CurrentData.CURRENT_MUSIC_INDEX : Values.CurrentData.CURRENT_MUSIC_INDEX + 1, 0));
                }
                break;

                case Values.HandlerWhat.SET_BUTTON_PLAY: {
                    mMainActivity.runOnUiThread(() -> {
                        mPlayButton.setImageResource(R.drawable.ic_pause_black_24dp);
                        GlideApp.with(mMainActivity)
                                .load(R.drawable.ic_pause_black_24dp)
                                .into(mNowPlayingStatusImage);
                    });
                }
                break;

                case Values.HandlerWhat.SET_BUTTON_PAUSE: {
                    mMainActivity.runOnUiThread(() -> {
                        mPlayButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                        GlideApp.with(mMainActivity)
                                .load(R.drawable.ic_play_arrow_black_24dp)
                                .into(mNowPlayingStatusImage);
                    });
                }
                break;

                case SET_SEEK_BAR_COLOR: {
                    @ColorInt int color = msg.arg1;
                    mSeekBar.getThumb().setColorFilter(color, PorterDuff.Mode.DST_ATOP);
                }
                break;

                default:
            }
        }
    }
}
