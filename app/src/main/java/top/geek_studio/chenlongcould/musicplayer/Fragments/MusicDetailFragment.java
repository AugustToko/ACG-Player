/*
 * ************************************************************
 * 文件：MusicDetailFragment.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月06日 10:05:15
 * 上次修改时间：2019年01月06日 08:34:11
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
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
import android.graphics.BitmapFactory;
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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import jp.wasabeef.glide.transformations.BlurTransformation;
import top.geek_studio.chenlongcould.musicplayer.Activities.AlbumDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.PublicActivity;
import top.geek_studio.chenlongcould.musicplayer.Adapters.MyWaitListAdapter;
import top.geek_studio.chenlongcould.musicplayer.BroadCasts.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.CustomView.AlbumImageView;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.Interface.IStyle;
import top.geek_studio.chenlongcould.musicplayer.Interface.VisibleOrGone;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public final class MusicDetailFragment extends Fragment implements IStyle, VisibleOrGone {

    /**
     * slide image: {@link MusicListFragment}
     * */
    byte fastView;

    //////////////////////////////////////////////////////////////////////////////////////

    /**
     * @see MainActivity#CURRENT_SLIDE_OFFSET
     */
    public static float CURRENT_SLIDE_OFFSET = 1;

    public static final String TAG = "MusicDetailFragment";

    float mLastX = 0;
    float mLastY = 0;
    float moveX = 0;

    public NotLeakHandler mHandler;

    private boolean HIDE_TOOLBAR = false;

    float moveY = 0;
    private ImageView.ScaleType mScaleType;
    private float defXScale;
    private float defYScale;

    private ImageView mPrimaryBackground;

    private ImageView mPrimaryBackground_down;

    private SeekBar mSeekBar;

    private View mCurrentInfoSeek;

    private HandlerThread mHandlerThread;

    private FloatingActionButton mPlayButton;

    private ImageButton mNextButton;

    private ImageButton mPreviousButton;

    private RecyclerView mRecyclerView;

    private LinearLayoutManager mLinearLayoutManager;

    private MainActivity mMainActivity;

    private ConstraintLayout mCurrentInfoBody;

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

    private ConstraintLayout mSlideUpGroup;

    //实例化一个fragment
    public static MusicDetailFragment newInstance() {
        return new MusicDetailFragment();
    }

    @Override
    public void onAttach(Context context) {
        Log.d(Values.LogTAG.LIFT_TAG, "onAttach: MusicDetailFragment");
        super.onAttach(context);
        mMainActivity = (MainActivity) context;

        mHandlerThread = new HandlerThread("Handler Thread in MusicDetailActivity");
        mHandlerThread.start();
        mHandler = new MusicDetailFragment.NotLeakHandler(mMainActivity, mHandlerThread.getLooper());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(Values.LogTAG.LIFT_TAG, "onCreateView: MusicDetailFragment");
        View view = inflater.inflate(R.layout.fragment_music_detail, container, false);

        initView(view);

        initData();

        return view;
    }

    private void initData() {
        setDefAnimation();
        clearAnimations();

        //init view data
        mHandler.sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);        //init seekBar
        mHandler.sendEmptyMessage(Values.HandlerWhat.SEEK_BAR_UPDATE);      //let seekBar loop update
        mHandler.sendEmptyMessage(Values.HandlerWhat.RECYCLER_SCROLL);      //scroll to the position{@Values.CurrentData.CURRENT_MUSIC_INDEX}

        mCurrentMusicNameText.setText(Data.sCurrentMusicItem.getMusicName());
        mCurrentAlbumNameText.setText(Data.sCurrentMusicItem.getMusicAlbum());

        //检测后台播放
        if (Values.HAS_PLAYED) {
            if (ReceiverOnMusicPlay.isPlayingMusic()) {
                mNowPlayingStatusImage.setImageResource(R.drawable.ic_pause_black_24dp);
                mPlayButton.setImageResource(R.drawable.ic_pause_black_24dp);
            }

            final byte[] cover = Utils.Audio.getAlbumByteImage(Data.sCurrentMusicItem.getMusicPath(), mMainActivity);
            Bitmap bitmap = null;
            if (cover != null) {
                bitmap = BitmapFactory.decodeByteArray(cover, 0, cover.length);
            }

            //set SeekBar color
            if (bitmap != null) {
                final int temp = bitmap.getPixel(bitmap.getWidth() / 2, bitmap.getHeight() / 2);
                mSeekBar.getThumb().setColorFilter(temp, PorterDuff.Mode.SRC_ATOP);
            }

            //nullable
            setIcoLightOrDark(bitmap);
            setSlideInfo(Data.sCurrentMusicItem.getMusicName(), Data.sCurrentMusicItem.getMusicAlbum(), bitmap);
            setCurrentInfo(Data.sCurrentMusicItem.getMusicName(), Data.sCurrentMusicItem.getMusicAlbum(), bitmap);

            mMainActivity.getMainBinding().slidingLayout.setTouchEnabled(true);
        } else {
            mPlayButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }

    }

    private AlbumImageView mMusicAlbumImage;

    public final ConstraintLayout getNowPlayingBody() {
        return mNowPlayingBody;
    }

    private AlbumImageView mMusicAlbumImageOth2;

    public void clearAnimations() {
        mRandomButton.clearAnimation();
        mRepeatButton.clearAnimation();
        mPlayButton.clearAnimation();
        mPreviousButton.clearAnimation();
        mNextButton.clearAnimation();
    }

    private AlbumImageView mMusicAlbumImageOth3;

    private MyWaitListAdapter mMyWaitListAdapter;

    private void findView(View view) {
        mMusicAlbumImage = view.findViewById(R.id.activity_music_detail_album_image);
        mPrimaryBackground = view.findViewById(R.id.activity_music_detail_primary_background);
        mPrimaryBackground_down = view.findViewById(R.id.activity_music_detail_primary_background_down);
        mSeekBar = view.findViewById(R.id.seekBar);
        mCurrentInfoSeek = view.findViewById(R.id.info_bar_seek);
        mNextButton = view.findViewById(R.id.next_button);
        mPreviousButton = view.findViewById(R.id.previous_button);
        mPlayButton = view.findViewById(R.id.play_button);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mCurrentInfoBody = view.findViewById(R.id.item_layout);
        mCurrentAlbumNameText = view.findViewById(R.id.item_text_one);
        mCurrentMusicNameText = view.findViewById(R.id.item_main_text);
        mMenuButton = view.findViewById(R.id.item_menu);
        mRandomButton = view.findViewById(R.id.random_button);
        mRepeatButton = view.findViewById(R.id.repeat_button);
        mToolbar = view.findViewById(R.id.activity_music_detail_toolbar);
        mAppBarLayout = view.findViewById(R.id.activity_music_detail_appbar);
        mLeftTime = view.findViewById(R.id.left_text);
        mRightTime = view.findViewById(R.id.right_text);
        mRecyclerMask = view.findViewById(R.id.recycler_mask);
        mSlidingUpPanelLayout = view.findViewById(R.id.activity_detail_sliding_layout);
        mNextWillText = view.findViewById(R.id.next_will_text);
        mMusicAlbumImageOth2 = view.findViewById(R.id.activity_music_detail_album_image_2);
        mMusicAlbumImageOth3 = view.findViewById(R.id.activity_music_detail_album_image_3);

        mNowPlayingSongAlbumText = view.findViewById(R.id.activity_main_now_playing_album_name);
        mNowPlayingBody = view.findViewById(R.id.current_info);
        mNowPlayingStatusImage = view.findViewById(R.id.activity_main_info_bar_status_image);
        mNowPlayingBackgroundImage = view.findViewById(R.id.current_info_background);
        mNowPlayingSongText = view.findViewById(R.id.activity_main_now_playing_name);
        mNowPlayingSongImage = view.findViewById(R.id.recycler_item_clover_image);
        mSlideUpGroup = view.findViewById(R.id.detail_body);
    }

    public final void setDefAnimation() {
        mRandomButton.setAlpha(0f);
        mRepeatButton.setAlpha(0f);
        mPlayButton.setRotation(-90f);
        mPlayButton.setScaleX(0);
        mPlayButton.setScaleY(0);
    }

    public final void initAnimation() {
        /*
         * init view animation
         * */
        //default type is common, but the random button alpha is 1f(it means this button is on), so set animate
//        animator.setStartDelay(500);
        if (Values.CurrentData.CURRENT_PLAY_TYPE.equals(Values.TYPE_RANDOM)) {
            final ValueAnimator animator = new ValueAnimator();
            animator.setDuration(300);
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
            final ValueAnimator animator = new ValueAnimator();
            animator.setDuration(300);
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

        final ValueAnimator animator = new ValueAnimator();
        animator.setDuration(300);
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

        final ValueAnimator mPlayButtonRotationAnimation = new ValueAnimator();
        mPlayButtonRotationAnimation.setFloatValues(-90f, 0f);
        mPlayButtonRotationAnimation.setDuration(300);
        mPlayButtonRotationAnimation.addUpdateListener(animation -> mPlayButton.setRotation((Float) animation.getAnimatedValue()));
        mPlayButtonRotationAnimation.start();

        final ValueAnimator mPlayButtonScale = new ValueAnimator();
        mPlayButtonScale.setFloatValues(0, defXScale);
        mPlayButtonScale.setDuration(300);
        mPlayButtonScale.addUpdateListener(animation -> {
            mPlayButton.setScaleX((Float) animation.getAnimatedValue());
            mPlayButton.setScaleY((Float) animation.getAnimatedValue());
        });
        mPlayButtonScale.start();

    }

    /**
     * @see #mMusicAlbumImage 的滑动模式
     * 0: just x
     * 1: x & y
     */
    int mode = 0;

    /**
     * 记录是否为长按 (2000ms)
     */
    private volatile AtomicBoolean lc = new AtomicBoolean(false);

    private void initView(View view) {
        findView(view);

        //get Default values
        mScaleType = mPlayButton.getScaleType();
        defXScale = mPlayButton.getScaleX();
        defYScale = mPlayButton.getScaleY();

        mMusicAlbumImageOth3.setX(0 - mMusicAlbumImage.getWidth());
        mMusicAlbumImageOth2.setX(mMusicAlbumImage.getWidth() * 2);
        mMusicAlbumImageOth2.setVisibility(View.GONE);
        mMusicAlbumImageOth3.setVisibility(View.GONE);

        mMusicAlbumImage.setOnTouchListener((v, event) -> {
            final int action = event.getAction();

            String befPath = null;
            String nexPath = null;
            if (Values.CurrentData.CURRENT_MUSIC_INDEX != 0) {
                befPath = Data.sPlayOrderList.get(Values.CurrentData.CURRENT_MUSIC_INDEX - 1).getMusicPath();
            }
            if (Values.CurrentData.CURRENT_MUSIC_INDEX != Data.sPlayOrderList.size() - 1) {
                nexPath = Data.sPlayOrderList.get(Values.CurrentData.CURRENT_MUSIC_INDEX + 1).getMusicPath();
            }

            mMusicAlbumImageOth2.setVisibility(View.VISIBLE);
            mMusicAlbumImageOth3.setVisibility(View.VISIBLE);

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    //预加载
                    GlideApp.with(this)
                            .load(Utils.Audio.getMp3Cover(befPath))
                            .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                            .into(mMusicAlbumImageOth3);

                    GlideApp.with(this)
                            .load(Utils.Audio.getMp3Cover(nexPath))
                            .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                            .into(mMusicAlbumImageOth2);

                    moveX = event.getX();
                    moveY = event.getY();
                    mLastX = event.getRawX();
                    mLastY = event.getRawY();

//                    lc.set(true);

//                    new Handler().postDelayed(() -> {
//                        if (lc.get()) {
//                            mSlidingUpPanelLayout.setTouchEnabled(false);
//                            mode = 1;
//                            mMainActivity.runOnUiThread(() -> Toast.makeText(mMainActivity, "Free move mode", Toast.LENGTH_SHORT).show());
//                        }
//                    }, 2000);

                    break;
                case MotionEvent.ACTION_MOVE:

//                    if (mSlidingUpPanelLayout.getPanelState() != SlidingUpPanelLayout.PanelState.EXPANDED || CURRENT_SLIDE_OFFSET != 0)
                    if (mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
                        break;

                    //首尾禁止对应边缘滑动
                    if (Values.CurrentData.CURRENT_MUSIC_INDEX == 0)
                        if (event.getRawX() > mLastX) break;

                    if (Values.CurrentData.CURRENT_MUSIC_INDEX == Data.sPlayOrderList.size() - 1)
                        if (event.getRawX() < mLastX) break;

                    float val = mMusicAlbumImage.getX() + (event.getX() - moveX);
                    mMusicAlbumImage.setTranslationX(val);
                    mMusicAlbumImageOth2.setTranslationX(mMusicAlbumImage.getWidth() + val);
                    mMusicAlbumImageOth3.setTranslationX(0 - mMusicAlbumImage.getWidth() + val);

                    // FIXME: 2018/12/11 上下滑动删除
//                    if (mode == 1) {
//                        float val2 = mMusicAlbumImage.getY() + (event.getY() - moveY);
//                        mMusicAlbumImage.setTranslationY(val2);
//                    }

                    break;

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:

                    //reset
                    mode = 0;
                    lc.set(false);
                    mSlidingUpPanelLayout.setTouchEnabled(true);

                    if (Math.abs(Math.abs(event.getRawX()) - Math.abs(mLastX)) < 5) {
                        mMusicAlbumImage.performClick();
                        break;
                    }

                    /*
                     * enter Animation...
                     * */
                    ValueAnimator animatorMain = new ValueAnimator();
                    animatorMain.setDuration(300);

                    //左滑一半 滑过去
                    if (mMusicAlbumImage.getX() < 0 && Math.abs(mMusicAlbumImage.getX()) >= mMusicAlbumImage.getWidth() / 2) {
                        animatorMain.setFloatValues(mMusicAlbumImage.getX(), 0 - mMusicAlbumImage.getWidth());
                        String finalNexPath = nexPath;
                        animatorMain.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                Utils.SendSomeThing.sendPlay(mMainActivity, 6, "next_slide");
                                GlideApp.with(MusicDetailFragment.this)
                                        .load(Utils.Audio.getMp3Cover(finalNexPath))
                                        .into(mMusicAlbumImage);
                                mMusicAlbumImage.setTranslationX(0);
                                mMusicAlbumImageOth2.setTranslationX(mMusicAlbumImage.getWidth() * 2);
                                mMusicAlbumImageOth2.setVisibility(View.GONE);
                                GlideApp.with(MusicDetailFragment.this).clear(mMusicAlbumImageOth2);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });

                        /*右滑一半 滑过去*/
                    } else if (mMusicAlbumImage.getX() > 0 && Math.abs(mMusicAlbumImage.getX()) >= mMusicAlbumImage.getWidth() / 2) {
                        animatorMain.setFloatValues(mMusicAlbumImage.getX(), mMusicAlbumImage.getWidth());
                        String finalBefPath = befPath;
                        animatorMain.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                Utils.SendSomeThing.sendPlay(mMainActivity, 6, "previous_slide");
                                GlideApp.with(MusicDetailFragment.this)
                                        .load(Utils.Audio.getMp3Cover(finalBefPath))
                                        .into(mMusicAlbumImage);
                                mMusicAlbumImage.setTranslationX(0);
                                mMusicAlbumImageOth3.setVisibility(View.GONE);
                                mMusicAlbumImageOth3.setTranslationX(0 - mMusicAlbumImage.getWidth());
                                GlideApp.with(MusicDetailFragment.this).clear(mMusicAlbumImageOth3);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });

                    } else {
                        animatorMain.setFloatValues(mMusicAlbumImage.getX(), 0);

                        animatorMain.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mMusicAlbumImage.setTranslationX(0);

                                GlideApp.with(MusicDetailFragment.this).clear(mMusicAlbumImageOth3);
                                GlideApp.with(MusicDetailFragment.this).clear(mMusicAlbumImageOth2);
                                mMusicAlbumImageOth2.setTranslationX(mMusicAlbumImage.getWidth() * 2);
                                mMusicAlbumImageOth3.setTranslationX(0 - mMusicAlbumImage.getWidth());
                                mMusicAlbumImageOth2.setVisibility(View.GONE);
                                mMusicAlbumImageOth3.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                    }

                    ValueAnimator animatorY = new ValueAnimator();
                    animatorY.setDuration(300);
                    animatorY.setFloatValues(mMusicAlbumImage.getTranslationY(), 0);
                    animatorY.addUpdateListener(animation -> mMusicAlbumImage.setTranslationY((Float) animation.getAnimatedValue()));
                    animatorY.start();

                    animatorMain.addUpdateListener(animation -> {
                        mMusicAlbumImage.setTranslationX((Float) animation.getAnimatedValue());
                        mMusicAlbumImageOth2.setTranslationX((Float) animation.getAnimatedValue() + mMusicAlbumImage.getWidth());
                        mMusicAlbumImageOth3.setTranslationX(0 - mMusicAlbumImage.getWidth() + (Float) animation.getAnimatedValue());
                    });
                    animatorMain.start();

                    return true;
            }
            return true;
        });

        if (Values.PHONE_HAS_NAV)
            mSlidingUpPanelLayout.setPanelHeight((int) (mSlidingUpPanelLayout.getPanelHeight() - getResources().getDimension(R.dimen.nav_height)));

        initStyle();

        setDefAnimation();
        clearAnimations();

        /*------------------toolbar--------------------*/
        mToolbar.inflateMenu(R.menu.menu_toolbar_in_detail);

        mToolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.menu_toolbar_fast_play: {
                    Utils.SendSomeThing.sendPlay(mMainActivity, ReceiverOnMusicPlay.TYPE_SHUFFLE, PlayListFragment.TAG);
                }
                break;

                case R.id.menu_toolbar_love: {
                    Utils.DataSet.addToFavourite(mMainActivity, Data.sCurrentMusicItem);
                }
                break;

                case R.id.menu_toolbar_eq: {
                    Utils.Audio.openEqualizer(mMainActivity, Data.sCurrentMusicItem.getAlbumId());
                }

                case R.id.menu_toolbar_debug: {
//                    Snackbar.make(mSlidingUpPanelLayout,
//                            "Next Will play" + Data.sMusicItems.get(Values.CurrentData.CURRENT_MUSIC_INDEX != Data.sMusicItems.size() ? Values.CurrentData.CURRENT_MUSIC_INDEX : 0)
//                            , Snackbar.LENGTH_LONG).setAction("按钮", v -> {
//                        //点击右侧的按钮之后的操作
//                        Utils.SendSomeThing.sendPause(mMainActivity);
//                    }).show();
                }
                break;
            }
            return false;
        });

        mToolbar.setNavigationOnClickListener(v -> mMainActivity.getHandler().sendEmptyMessage(MainActivity.DOWN));

        /*-------------------button---------------------*/
        mRepeatButton.setOnClickListener(v -> {

            /*
             * COMMON = 0f
             * REPEAT = 1f
             * REPEAT_ONE = 1f(another pic)
             * */
            final ValueAnimator animator = new ValueAnimator();
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
                    mRepeatButton.setImageResource(R.drawable.ic_repeat_one_white_24dp);
                    Values.CurrentData.CURRENT_AUTO_NEXT_TYPE = Values.TYPE_REPEAT_ONE;
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
            final AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
            builder.setTitle("Repeater");
            builder.setMessage("Building...");
            builder.setCancelable(true);
            builder.show();
            return false;
        });

        mRandomButton.setOnClickListener(v -> {
            mRandomButton.clearAnimation();
            final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mMainActivity).edit();
            if (Values.CurrentData.CURRENT_PLAY_TYPE.equals(Values.TYPE_RANDOM)) {
                Values.CurrentData.CURRENT_PLAY_TYPE = Values.TYPE_COMMON;
                final ValueAnimator animator = new ValueAnimator();
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

                final MusicItem item = Data.sPlayOrderList.get(Values.CurrentData.CURRENT_MUSIC_INDEX);

                Data.sPlayOrderList.clear();
                Data.sPlayOrderList.addAll(Data.sMusicItems);

                for (int i = 0; i < Data.sMusicItems.size(); i++) {
                    if (Data.sPlayOrderList.get(i).getMusicID() == item.getMusicID()) {
                        Values.CurrentData.CURRENT_MUSIC_INDEX = i;
                    }
                }

                mMyWaitListAdapter.notifyDataSetChanged();

            } else {
                Values.CurrentData.CURRENT_PLAY_TYPE = Values.TYPE_RANDOM;
                final ValueAnimator animator = new ValueAnimator();
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

                final MusicItem item = Data.sPlayOrderList.get(Values.CurrentData.CURRENT_MUSIC_INDEX);
                Collections.shuffle(Data.sPlayOrderList);

                for (int i = 0; i < Data.sMusicItems.size(); i++) {
                    if (Data.sPlayOrderList.get(i).getMusicID() == item.getMusicID()) {
                        Values.CurrentData.CURRENT_MUSIC_INDEX = i;
                    }
                }

                mMyWaitListAdapter.notifyDataSetChanged();

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

        final Menu menu = mPopupMenu.getMenu();

        mMenuButton.setOnClickListener(v -> mPopupMenu.show());

        mPopupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                //noinspection PointlessArithmeticExpression
                case Menu.FIRST + 0: {

                }
                break;
                case Menu.FIRST + 1: {
                    String albumName = Data.sCurrentMusicItem.getMusicAlbum();
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
                    intent.putExtra("start_by", "detail");
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

            mHandler.sendEmptyMessage(Values.HandlerWhat.RECYCLER_SCROLL);

        });

        mLinearLayoutManager = new LinearLayoutManager(mMainActivity);

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(mMainActivity, DividerItemDecoration.VERTICAL));
        mMyWaitListAdapter = new MyWaitListAdapter(mMainActivity, Data.sPlayOrderList);
        mRecyclerView.setAdapter(mMyWaitListAdapter);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    ReceiverOnMusicPlay.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ReceiverOnMusicPlay.seekTo(seekBar.getProgress());
                ReceiverOnMusicPlay.playMusic();
                mHandler.sendEmptyMessage(Values.HandlerWhat.SET_BUTTON_PLAY);
            }
        });

        //just pause or play
        mPlayButton.setOnClickListener(v -> {
            if (ReceiverOnMusicPlay.isPlayingMusic()) {
                Utils.SendSomeThing.sendPause(mMainActivity);
            } else {
                ReceiverOnMusicPlay.playMusic();
                Utils.Ui.setPlayButtonNowPlaying();
            }
        });

        mNextButton.setOnClickListener(v -> Utils.SendSomeThing.sendPlay(mMainActivity, 6, "next"));

        mNextButton.setOnLongClickListener(v -> {
            Values.BUTTON_PRESSED = true;
            int nowPosition = mSeekBar.getProgress() + ReceiverOnMusicPlay.getDuration() / 20;
            if (nowPosition >= mSeekBar.getMax()) {
                nowPosition = mSeekBar.getMax();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mSeekBar.setProgress(nowPosition, true);
            } else {
                mSeekBar.setProgress(nowPosition);
            }
            ReceiverOnMusicPlay.seekTo(nowPosition);
            Values.BUTTON_PRESSED = false;
            return true;
        });

        mPreviousButton.setOnClickListener(v -> {

            //当进度条大于播放总长 1/20 那么重新播放该歌曲
            if (ReceiverOnMusicPlay.getCurrentPosition() > ReceiverOnMusicPlay.getDuration() / 20 || Values.CurrentData.CURRENT_MUSIC_INDEX == 0) {
                ReceiverOnMusicPlay.seekTo(0);
            } else {
                Utils.SendSomeThing.sendPlay(mMainActivity, 6, "previous");
            }

        });

        mPreviousButton.setOnLongClickListener(v -> {
            int nowPosition = mSeekBar.getProgress() - ReceiverOnMusicPlay.getDuration() / 20;
            if (nowPosition <= 0) {
                nowPosition = 0;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mSeekBar.setProgress(nowPosition, true);
            } else {
                mSeekBar.setProgress(nowPosition);
            }
            ReceiverOnMusicPlay.seekTo(nowPosition);
            return true;
        });

        mNowPlayingStatusImage.setOnClickListener(v -> {
            //判断是否播放过, 如没有默认随机播放
            if (Values.HAS_PLAYED) {
                if (ReceiverOnMusicPlay.isPlayingMusic()) {
                    Utils.SendSomeThing.sendPause(mMainActivity);
                } else {
                    Utils.SendSomeThing.sendPlay(mMainActivity, 3, null);
                }
            } else {
//                Toast.makeText(mMainActivity, "Shuffle Playback!", Toast.LENGTH_SHORT).show();
                Utils.SendSomeThing.sendPlay(mMainActivity, ReceiverOnMusicPlay.TYPE_SHUFFLE, TAG);
            }
        });

        mNowPlayingBody.setOnClickListener(v -> {
            if (Values.HAS_PLAYED) {
                if (mMainActivity.getMusicListFragment().getMusicListBinding().includeRecycler.recyclerView != null) {
                    mMainActivity.getMusicListFragment().getMusicListBinding().includeRecycler.recyclerView.stopScroll();
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
                CURRENT_SLIDE_OFFSET = slideOffset;
                mSlideUpGroup.setTranslationY(0 - slideOffset * 120);
                if (slideOffset == 0)
                    mMainActivity.getMainBinding().slidingLayout.setTouchEnabled(true);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    mMainActivity.getMainBinding().slidingLayout.setTouchEnabled(true);
                    mHandler.sendEmptyMessage(Values.HandlerWhat.RECYCLER_SCROLL);
                } else {
                    mMainActivity.getMainBinding().slidingLayout.setTouchEnabled(false);
                }
            }
        });

    }

    @Override
    public void initStyle() {
        mCurrentMusicNameText.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));
        mCurrentAlbumNameText.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));
    }

    /**
     * hide or show toolbar
     */
    private void showToolbar() {
        HIDE_TOOLBAR = false;
        final ValueAnimator anim = ValueAnimator.ofFloat(0, 1f);
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
        final AlphaAnimation temp = new AlphaAnimation(1f, 0f);
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
    public final void setCurrentInfo(@NonNull String name, @NonNull String albumName, byte[] cover) {
        mMainActivity.runOnUiThread(() -> {
            mCurrentMusicNameText.setText(name);
            mCurrentAlbumNameText.setText(albumName);

            if (cover != null) {
                GlideApp.with(this).clear(mMusicAlbumImage);
                GlideApp.with(this)
                        .load(cover)
                        .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                        .centerCrop()
                        .into(mMusicAlbumImage);
                Utils.Ui.setBlurEffect(mMainActivity, cover, mPrimaryBackground, mPrimaryBackground_down, mNextWillText);
            } else {
                GlideApp.with(this)
                        .load(R.drawable.ic_audiotrack_24px)
                        .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                        .centerCrop()
                        .into(mMusicAlbumImage);
                mPrimaryBackground.setImageDrawable(null);
                mPrimaryBackground.setBackgroundColor(Color.GRAY);
                mPrimaryBackground_down.setImageDrawable(null);
                mPrimaryBackground_down.setBackgroundColor(Color.GRAY);
            }
        });
    }

    public final void setCurrentInfoWithoutMainImage(@NonNull final String name, @NonNull final String albumName, final byte[] cover) {
        mMainActivity.runOnUiThread(() -> {
            mCurrentMusicNameText.setText(name);
            mCurrentAlbumNameText.setText(albumName);
            if (cover != null)
                Utils.Ui.setBlurEffect(mMainActivity, cover, mPrimaryBackground, mPrimaryBackground_down, mNextWillText);
        });
    }

    public final void setCurrentInfo(@NonNull final String name, @NonNull final String albumName, @Nullable final Bitmap cover) {
        mMainActivity.runOnUiThread(() -> {
            mCurrentMusicNameText.setText(name);
            mCurrentAlbumNameText.setText(albumName);

            if (cover != null) {
                GlideApp.with(mMainActivity)
                        .load(cover)
                        .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                        .centerCrop()
                        .into(mMusicAlbumImage);
                Utils.Ui.setBlurEffect(mMainActivity, cover, mPrimaryBackground, mPrimaryBackground_down, mNextWillText);

            } else {
                GlideApp.with(this)
                        .load(R.drawable.ic_audiotrack_24px)
                        .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                        .centerCrop()
                        .into(mMusicAlbumImage);
                mPrimaryBackground.setImageDrawable(null);
                mPrimaryBackground.setBackgroundColor(Color.GRAY);
                mPrimaryBackground_down.setImageDrawable(null);
                mPrimaryBackground_down.setBackgroundColor(Color.GRAY);
            }

        });
    }

    public final NotLeakHandler getHandler() {
        return mHandler;
    }

    public MyWaitListAdapter getMyWaitListAdapter() {
        return mMyWaitListAdapter;
    }

    /**
     * set Info (auto put in data.)
     *
     * @param songName  music name
     * @param albumName music album name
     * @param cover     music cover image, it is @NullAble(some types of music do not have cover)
     */
    public void setSlideInfo(String songName, String albumName, @Nullable Bitmap cover) {
        mMainActivity.runOnUiThread(() -> {
            mNowPlayingSongText.setText(songName);
            mNowPlayingSongAlbumText.setText(albumName);
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
            if (cover != null) {
                GlideApp.with(mMainActivity).load(cover).transition(DrawableTransitionOptions.withCrossFade()).into(mNowPlayingSongImage);
                GlideApp.with(mMainActivity).load(cover).transition(DrawableTransitionOptions.withCrossFade()).centerCrop().into(mMainActivity.getNavHeaderImageView());

                setIcoLightOrDark(cover);

                GlideApp.with(mMainActivity).load(cover)
                        .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                        .apply(bitmapTransform(new BlurTransformation(30, 20)))
                        .override(100, 100)
                        .into(mNowPlayingBackgroundImage);

            } else {
                GlideApp.with(mMainActivity).load(R.drawable.ic_audiotrack_24px).transition(DrawableTransitionOptions.withCrossFade()).into(mNowPlayingSongImage);
                GlideApp.with(mMainActivity).load(R.drawable.ic_audiotrack_24px).transition(DrawableTransitionOptions.withCrossFade()).into(mMainActivity.getNavHeaderImageView());

                //cover is null, button set default color (BLACK)
                mNowPlayingSongText.setTextColor(Color.BLACK);
                mNowPlayingSongAlbumText.setTextColor(Color.BLACK);
                mNowPlayingStatusImage.setColorFilter(Color.BLACK);
                mRandomButton.setColorFilter(Color.BLACK);
                mRepeatButton.setColorFilter(Color.BLACK);
                mNextButton.setColorFilter(Color.BLACK);
                mPreviousButton.setColorFilter(Color.BLACK);
                mLeftTime.setTextColor(Color.BLACK);
                mRightTime.setTextColor(Color.BLACK);
            }
        });
    }

    /**
     * setIcon White or Black (by bitmap light)
     *
     * @param bitmap backgroundImage
     */
    private void setIcoLightOrDark(@Nullable final Bitmap bitmap) {

        if (bitmap == null) return;

        //InfoBar background color AND text color balance
        final int currentBright = Utils.Ui.getBright(bitmap);
        if (currentBright > (255 / 2)) {
            @ColorInt final int target = Color.parseColor(Values.Color.NOT_VERY_BLACK);
            mNowPlayingSongText.setTextColor(target);
            mNowPlayingSongAlbumText.setTextColor(target);
            mNowPlayingStatusImage.setColorFilter(target);

            mRandomButton.setColorFilter(target);
            mRepeatButton.setColorFilter(target);
            mNextButton.setColorFilter(target);
            mPreviousButton.setColorFilter(target);
            mLeftTime.setTextColor(target);
            mRightTime.setTextColor(target);
        } else {
            @ColorInt final int target = Color.parseColor(Values.Color.NOT_VERY_WHITE);
            mNowPlayingSongText.setTextColor(target);
            mNowPlayingSongAlbumText.setTextColor(target);
            mNowPlayingStatusImage.setColorFilter(target);

            mRandomButton.setColorFilter(target);
            mRepeatButton.setColorFilter(target);
            mNextButton.setColorFilter(target);
            mPreviousButton.setColorFilter(target);
            mLeftTime.setTextColor(target);
            mRightTime.setTextColor(target);
        }

    }

    public final SlidingUpPanelLayout getSlidingUpPanelLayout() {
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

    @Override
    public void visibleOrGone(int status) {

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
                        if (Data.sMusicBinder == null) return;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            mSeekBar.setProgress(0, true);
                        } else {
                            mSeekBar.setProgress(0);
                        }

                        mCurrentInfoSeek.getLayoutParams().width = 0;
                        mCurrentInfoSeek.setLayoutParams(mCurrentInfoSeek.getLayoutParams());
                        mCurrentInfoSeek.requestLayout();

                        mRightTime.setText(String.valueOf(Data.sSimpleDateFormat.format(new Date(ReceiverOnMusicPlay.getDuration()))));
                        mSeekBar.setMax(ReceiverOnMusicPlay.getDuration());
                    });
                }
                break;

                case Values.HandlerWhat.SEEK_BAR_UPDATE: {
                    mWeakReference.get().runOnUiThread(() -> {
                        //点击body 或 music 正在播放 才可以进行seekBar更新
                        if (Data.sMusicBinder == null) return;

                        if (ReceiverOnMusicPlay.isPlayingMusic()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                mSeekBar.setProgress(ReceiverOnMusicPlay.getCurrentPosition(), true);
                            } else {
                                mSeekBar.setProgress(ReceiverOnMusicPlay.getCurrentPosition());
                            }

                            mCurrentInfoSeek.getLayoutParams().width = mCurrentInfoBody.getWidth() * ReceiverOnMusicPlay.getCurrentPosition() / ReceiverOnMusicPlay.getDuration();
                            mCurrentInfoSeek.setLayoutParams(mCurrentInfoSeek.getLayoutParams());
                            mCurrentInfoSeek.requestLayout();
                            mLeftTime.setText(String.valueOf(Data.sSimpleDateFormat.format(new Date(ReceiverOnMusicPlay.getCurrentPosition()))));

//                            Log.d(TAG, "handleMessage: current position " + ReceiverOnMusicPlay.getCurrentPosition() + " ------------ " + ReceiverOnMusicPlay.getDuration());
                            // FIXME: 2018/12/5 snack break the layout
//                            if (Data.sMusicBinder.getCurrentPosition() / 1000 == Data.sMusicBinder.getDuration() / 1000 - 5 && !SNACK_NOTICE) {
//                                SNACK_NOTICE = true;
//
//                                Snackbar snackbar = Snackbar.make(mSlidingUpPanelLayout,
//                                        "Next Will play " + Data.sMusicItems.get(Values.CurrentData.CURRENT_MUSIC_INDEX != Data.sMusicItems.size() ? Values.CurrentData.CURRENT_MUSIC_INDEX : 0).getMusicName()
//                                        , Snackbar.LENGTH_LONG).setAction("按钮", v -> {
//                                    //点击右侧的按钮之后的操作
//                                    Utils.SendSomeThing.sendPause(mMainActivity);
//                                });
//                                snackbar.addCallback(new Snackbar.Callback() {
//                                    @Override
//                                    public void onDismissed(Snackbar transientBottomBar, int event) {
//                                        SNACK_NOTICE = false;
//                                        Log.d(TAG, "onDismissed: dismissed...");
//                                    }
//                                });
//
//                                snackbar.show();
//                            }

                        }
                    });

                    //循环更新 0.5s 一次
                    mHandler.sendEmptyMessageDelayed(Values.HandlerWhat.SEEK_BAR_UPDATE, 500);
                }
                break;

                case Values.HandlerWhat.RECYCLER_SCROLL: {
                    mWeakReference.get().runOnUiThread(() -> mLinearLayoutManager.scrollToPositionWithOffset(Values.CurrentData.CURRENT_MUSIC_INDEX == Data.sMusicItems.size() ?
                            Values.CurrentData.CURRENT_MUSIC_INDEX : Values.CurrentData.CURRENT_MUSIC_INDEX + 1, 0));
                }
                break;

                case Values.HandlerWhat.SET_BUTTON_PLAY: {
                    mWeakReference.get().runOnUiThread(() -> {
                        GlideApp.with(mMainActivity)
                                .load(R.drawable.ic_pause_black_24dp)
                                .into(mNowPlayingStatusImage);
                        GlideApp.with(mMainActivity)
                                .load(R.drawable.ic_pause_black_24dp)
                                .into(mPlayButton);
                    });
                }
                break;

                case Values.HandlerWhat.SET_BUTTON_PAUSE: {
                    mWeakReference.get().runOnUiThread(() -> {
                        GlideApp.with(mMainActivity)
                                .load(R.drawable.ic_play_arrow_black_24dp)
                                .into(mNowPlayingStatusImage);
                        GlideApp.with(mMainActivity)
                                .load(R.drawable.ic_play_arrow_black_24dp)
                                .into(mPlayButton);
                    });
                }
                break;
                default:
            }

        }
    }
}
