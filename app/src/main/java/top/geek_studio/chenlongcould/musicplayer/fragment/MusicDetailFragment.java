/*
 * ************************************************************
 * 文件：MusicDetailFragment.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月27日 13:11:38
 * 上次修改时间：2019年01月27日 13:08:44
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.fragment;

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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jp.wasabeef.glide.transformations.BlurTransformation;
import top.geek_studio.chenlongcould.geeklibrary.theme.IStyle;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.AlbumDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.PublicActivity;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyWaitListAdapter;
import top.geek_studio.chenlongcould.musicplayer.broadcasts.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.custom_view.AlbumImageView;
import top.geek_studio.chenlongcould.musicplayer.utils.MusicUtil;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public final class MusicDetailFragment extends Fragment implements IStyle {

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

    private boolean SNACK_NOTICE = false;

    float moveY = 0;
    private ImageView.ScaleType mScaleType;
    private float defXScale;
    private float defYScale;

    private ImageView mBGup;

    private ImageView mBGdown;

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

    private ImageView mNowPlayingFavButton;

    private ConstraintLayout mNowPlayingBody;

    private ConstraintLayout mSlideUpGroup;

    //实例化一个fragment
    public static MusicDetailFragment newInstance() {
        return new MusicDetailFragment();
    }

    @Override
    public void onAttach(Context context) {
        Log.d(Values.LogTAG.LAG_TAG, "onAttach: MusicDetailFragment");
        super.onAttach(context);
        mMainActivity = (MainActivity) context;

        mHandlerThread = new HandlerThread("Handler Thread in MusicDetailActivity");
        mHandlerThread.start();
        mHandler = new MusicDetailFragment.NotLeakHandler(mMainActivity, mHandlerThread.getLooper());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_detail, container, false);

        initView(view);

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

            Bitmap bitmap = Utils.Audio.getCoverBitmap(getActivity(), Data.sCurrentMusicItem.getAlbumId());

            //nullable
            setIcoLightOrDark(bitmap);
            setSlideInfo(Data.sCurrentMusicItem.getMusicName(), Data.sCurrentMusicItem.getMusicAlbum(), bitmap);
            setCurrentInfo(Data.sCurrentMusicItem.getMusicName(), Data.sCurrentMusicItem.getMusicAlbum(), bitmap);

            mMainActivity.getMainBinding().slidingLayout.setTouchEnabled(true);
        } else {
            mPlayButton.setImageResource(R.drawable.ic_play_arrow_grey_600_24dp);
        }


        return view;
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
        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(Values.SharedPrefsTag.ORDER_TYPE, Values.TYPE_COMMON).equals(Values.TYPE_RANDOM)) {
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
        switch (Values.CurrentData.CURRENT_PLAY_TYPE) {
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
            default: {
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
        mMusicAlbumImage = view.findViewById(R.id.activity_music_detail_album_image);
        mBGup = view.findViewById(R.id.activity_music_detail_primary_background_up);
        mBGdown = view.findViewById(R.id.activity_music_detail_primary_background_down);
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
        mNowPlayingFavButton = view.findViewById(R.id.activity_main_info_bar_fav_image);
        mNowPlayingBackgroundImage = view.findViewById(R.id.current_info_background);
        mNowPlayingSongText = view.findViewById(R.id.activity_main_now_playing_name);
        mNowPlayingSongImage = view.findViewById(R.id.recycler_item_clover_image);
        mSlideUpGroup = view.findViewById(R.id.detail_body);

        mSlidingUpPanelLayout.post(() -> {
            int val0 = view.getHeight() - view.findViewById(R.id.frame_ctrl).getBottom();
            Log.d(TAG, "initView: upSlide the val is:" + val0);
            mSlidingUpPanelLayout.setPanelHeight(val0);
        });

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

            MusicItem befItem = null;
            MusicItem nexItem = null;
            if (Values.CurrentData.CURRENT_MUSIC_INDEX > 0 && Values.CurrentData.CURRENT_MUSIC_INDEX < Data.sPlayOrderList.size() - 1) {
                befItem = Data.sPlayOrderList.get(Values.CurrentData.CURRENT_MUSIC_INDEX - 1);
            }
            if (Values.CurrentData.CURRENT_MUSIC_INDEX < Data.sPlayOrderList.size() - 1 && Values.CurrentData.CURRENT_MUSIC_INDEX > 0) {
                nexItem = Data.sPlayOrderList.get(Values.CurrentData.CURRENT_MUSIC_INDEX + 1);
            }

            mMusicAlbumImageOth2.setVisibility(View.VISIBLE);
            mMusicAlbumImageOth3.setVisibility(View.VISIBLE);

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    //预加载
                    GlideApp.with(getActivity())
                            .load(befItem == null ? R.drawable.ic_audiotrack_24px : Utils.Audio.getCoverPath(mMainActivity, befItem.getAlbumId()))
                            .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(mMusicAlbumImageOth3);

                    GlideApp.with(getActivity())
                            .load(nexItem == null ? R.drawable.ic_audiotrack_24px : Utils.Audio.getCoverPath(mMainActivity, nexItem.getAlbumId()))
                            .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
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
                        MusicItem finalNexItem = nexItem;
                        animatorMain.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                Utils.SendSomeThing.sendPlay(mMainActivity, 6, "next_slide");
                                GlideApp.with(MusicDetailFragment.this)
                                        .load(finalNexItem == null ? R.drawable.ic_audiotrack_24px : Utils.Audio.getCoverPath(mMainActivity, finalNexItem.getAlbumId()))
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
                        MusicItem finalBefItem = befItem;
                        animatorMain.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                Utils.SendSomeThing.sendPlay(mMainActivity, 6, "previous_slide");
                                GlideApp.with(MusicDetailFragment.this)
                                        .load(finalBefItem == null ? R.drawable.ic_audiotrack_24px : Utils.Audio.getCoverPath(mMainActivity, finalBefItem.getAlbumId()))
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
                    MusicUtil.toggleFavorite(mMainActivity, Data.sCurrentMusicItem);
                    updateFav();
                    Toast.makeText(mMainActivity, getString(R.string.done), Toast.LENGTH_SHORT).show();
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
                case R.id.menu_toolbar_trash_can: {
                    dropToTrash();
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
            switch (Values.CurrentData.CURRENT_PLAY_TYPE) {
                case Values.TYPE_COMMON: {
                    Values.CurrentData.CURRENT_PLAY_TYPE = Values.TYPE_REPEAT;
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
                    Values.CurrentData.CURRENT_PLAY_TYPE = Values.TYPE_REPEAT_ONE;
                }
                break;
                case Values.TYPE_REPEAT_ONE: {
                    Values.CurrentData.CURRENT_PLAY_TYPE = Values.TYPE_COMMON;
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
            builder.setMessage("I am a Repeater...");
            builder.setCancelable(true);
            builder.show();
            return false;
        });

        mRandomButton.setOnClickListener(v -> {
            mRandomButton.clearAnimation();
            final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mMainActivity).edit();
            if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(Values.SharedPrefsTag.ORDER_TYPE, Values.TYPE_COMMON).equals(Values.TYPE_RANDOM)) {
                editor.putString(Values.SharedPrefsTag.ORDER_TYPE, Values.TYPE_COMMON).apply();
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
                editor.putString(Values.SharedPrefsTag.ORDER_TYPE, Values.TYPE_RANDOM).apply();
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

        //noinspection PointlessArithmeticExpression
        menu.add(Menu.NONE, Menu.FIRST + 1, 0, "查看专辑");
        menu.add(Menu.NONE, Menu.FIRST + 2, 0, "详细信息");

        mMenuButton.setOnClickListener(v -> mPopupMenu.show());

        mPopupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case Menu.FIRST + 1: {
                    final String albumName = Data.sCurrentMusicItem.getMusicAlbum();
                    final Cursor cursor = mMainActivity.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null,
                            MediaStore.Audio.Albums.ALBUM + "= ?", new String[]{albumName}, null);

                    //int MusicDetailActivity
                    final Intent intent = new Intent(mMainActivity, AlbumDetailActivity.class);
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

        /*--------------------new panel----------------------*/
        mCurrentInfoBody.setOnClickListener(v -> {

            if (mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            } else {
                mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }

            mHandler.sendEmptyMessage(Values.HandlerWhat.RECYCLER_SCROLL);

        });
        mCurrentInfoBody.setOnLongClickListener(v -> {
            dropToTrash();
            return true;
        });

        mLinearLayoutManager = new LinearLayoutManager(mMainActivity);

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
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

        mNextButton.setOnClickListener(v -> Utils.SendSomeThing.sendPlay(mMainActivity, 6, ReceiverOnMusicPlay.TYPE_NEXT));

        mNextButton.setOnLongClickListener(v -> {
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
                Utils.SendSomeThing.sendPlay(mMainActivity, ReceiverOnMusicPlay.TYPE_SHUFFLE, TAG);
            }
        });

        mNowPlayingFavButton.setOnClickListener(v -> {
            MusicUtil.toggleFavorite(mMainActivity, Data.sCurrentMusicItem);
            updateFav();
            Toast.makeText(mMainActivity, getString(R.string.done), Toast.LENGTH_SHORT).show();
        });

        mNowPlayingBody.setOnClickListener(v -> {
            if (Values.HAS_PLAYED) {
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

    private void dropToTrash() {
        if (PreferenceManager.getDefaultSharedPreferences(mMainActivity).getBoolean(Values.SharedPrefsTag.TIP_NOTICE_DROP_TRASH, true)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
            builder.setTitle(getString(R.string.sure_int));
            builder.setMessage("Drop to trash can?");
            CheckBox checkBox = new CheckBox(mMainActivity);
            checkBox.setText("Don not show again");
            builder.setView(checkBox);
            builder.setCancelable(true);
            builder.setNegativeButton("Sure", (dialog, which) -> {
                if (checkBox.isChecked()) {
                    PreferenceManager.getDefaultSharedPreferences(mMainActivity).edit().putBoolean(Values.SharedPrefsTag.TIP_NOTICE_DROP_TRASH, false).apply();
                }
                Data.sTrashCanList.add(Data.sCurrentMusicItem);
                dialog.dismiss();
            });
            builder.setPositiveButton("NO", (dialog, which) -> dialog.dismiss());
            builder.show();
        } else {
            Data.sTrashCanList.add(Data.sCurrentMusicItem);
        }
    }

    @Override
    public void initStyle() {
        mCurrentInfoSeek.setBackgroundColor(Utils.Ui.getAccentColor(mMainActivity));
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

//    //set infoBar set AlbumImage set PrimaryBackground
//    public final void setCurrentInfo(@NonNull String name, @NonNull String albumName, byte[] cover) {
//        mMainActivity.runOnUiThread(() -> {
//            mCurrentMusicNameText.setText(name);
//            mCurrentAlbumNameText.setText(albumName);
//
//            if (cover != null) {
//                GlideApp.with(this).clear(mMusicAlbumImage);
//                GlideApp.with(this)
//                        .load(cover)
//                        .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
//                        .centerCrop()
//                        .into(mMusicAlbumImage);
//                Utils.Ui.setBlurEffect(mMainActivity, cover, mBGup, mBGdown, mNextWillText);
//            } else {
//                GlideApp.with(this)
//                        .load(R.drawable.ic_audiotrack_24px)
//                        .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
//                        .centerCrop()
//                        .into(mMusicAlbumImage);
//                mBGup.setImageDrawable(null);
//                mBGup.setBackgroundColor(Color.GRAY);
//                mBGdown.setImageDrawable(null);
//                mBGdown.setBackgroundColor(Color.GRAY);
//            }
//        });
//    }

    public final void setCurrentInfoWithoutMainImage(@NonNull final String name, @NonNull final String albumName, final Bitmap cover) {
        mMainActivity.runOnUiThread(() -> {
            mCurrentMusicNameText.setText(name);
            mCurrentAlbumNameText.setText(albumName);
            Utils.Ui.setBlurEffect(mMainActivity, cover, mBGup, mBGdown, mNextWillText);
        });
    }

    public final void setCurrentInfo(@NonNull final String name, @NonNull final String albumName, final Bitmap cover) {
        mMainActivity.runOnUiThread(() -> {
            mCurrentMusicNameText.setText(name);
            mCurrentAlbumNameText.setText(albumName);

            GlideApp.with(mMainActivity)
                    .load(cover)
                    .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(mMusicAlbumImage);

            updateFav();

            Utils.Ui.setBlurEffect(mMainActivity, cover, mBGup, mBGdown, mNextWillText);
        });
    }

    public final NotLeakHandler getHandler() {
        return mHandler;
    }

    public final void updateFav() {
        @DrawableRes int id = MusicUtil.isFavorite(mMainActivity, Data.sCurrentMusicItem) ?
                R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp;
        mToolbar.getMenu().findItem(R.id.menu_toolbar_love).setIcon(id);
        mNowPlayingFavButton.setImageResource(id);
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
                GlideApp.with(mMainActivity).load(cover).transition(DrawableTransitionOptions.withCrossFade()).diskCacheStrategy(DiskCacheStrategy.NONE).into(mNowPlayingSongImage);
                GlideApp.with(mMainActivity).load(cover).transition(DrawableTransitionOptions.withCrossFade()).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(mMainActivity.getNavHeaderImageView());

                setIcoLightOrDark(cover);

                GlideApp.with(mMainActivity).load(cover)
                        .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                        .apply(bitmapTransform(new BlurTransformation(30, 20)))
                        .override(100, 100)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
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
            @ColorInt final int target = ContextCompat.getColor(mMainActivity, R.color.notVeryBlack);
            mNowPlayingSongText.setTextColor(target);
            mNowPlayingSongAlbumText.setTextColor(target);
            mNowPlayingStatusImage.setColorFilter(target);
            mNowPlayingFavButton.setColorFilter(target);

            mRandomButton.setColorFilter(target);
            mRepeatButton.setColorFilter(target);
            mNextButton.setColorFilter(target);
            mPreviousButton.setColorFilter(target);
            mLeftTime.setTextColor(target);
            mRightTime.setTextColor(target);

            mSeekBar.getProgressDrawable().setTint(target);
            mSeekBar.getThumb().setTint(target);

        } else {
            @ColorInt final int target = ContextCompat.getColor(mMainActivity, R.color.notVeryWhite);
            mNowPlayingSongText.setTextColor(target);
            mNowPlayingSongAlbumText.setTextColor(target);
            mNowPlayingStatusImage.setColorFilter(target);
            mNowPlayingFavButton.setColorFilter(target);

            mRandomButton.setColorFilter(target);
            mRepeatButton.setColorFilter(target);
            mNextButton.setColorFilter(target);
            mPreviousButton.setColorFilter(target);
            mLeftTime.setTextColor(target);
            mRightTime.setTextColor(target);

            mSeekBar.getProgressDrawable().setTint(target);
            mSeekBar.getThumb().setTint(target);
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

                            Log.i(TAG, "handleMessage: current position " + ReceiverOnMusicPlay.getCurrentPosition() + " ------------ " + ReceiverOnMusicPlay.getDuration());

                            //播放模式不为循环单曲时，跳出提示
                            if (!Values.CurrentData.CURRENT_PLAY_TYPE.equals(Values.TYPE_REPEAT_ONE)) {
                                if (ReceiverOnMusicPlay.getCurrentPosition() / 1000 == ReceiverOnMusicPlay.getDuration() / 1000 - 5 && !SNACK_NOTICE) {
                                    SNACK_NOTICE = true;

                                    Snackbar snackbar = Snackbar.make(mSlidingUpPanelLayout,
                                            getString(R.string.next_will_play_x, Data.sPlayOrderList.get(Values.CurrentData.CURRENT_MUSIC_INDEX + 1 != Data.sPlayOrderList.size() ? Values.CurrentData.CURRENT_MUSIC_INDEX + 1 : 0).getMusicName())
                                            , Snackbar.LENGTH_LONG).setAction(getString(R.string.skip), v -> {
                                        //点击右侧的按钮之后的操作
                                        Values.CurrentData.CURRENT_MUSIC_INDEX += 1;
                                        Utils.SendSomeThing.sendPlay(mMainActivity, 6, ReceiverOnMusicPlay.TYPE_NEXT);
                                    });
                                    snackbar.addCallback(new Snackbar.Callback() {
                                        @Override
                                        public void onDismissed(Snackbar transientBottomBar, int event) {
                                            SNACK_NOTICE = false;
                                        }
                                    });

                                    snackbar.show();
                                }

                            }

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
                                .load(R.drawable.ic_play_arrow_grey_600_24dp)
                                .into(mNowPlayingStatusImage);
                        GlideApp.with(mMainActivity)
                                .load(R.drawable.ic_play_arrow_grey_600_24dp)
                                .into(mPlayButton);
                    });
                }
                break;
                default:
            }

        }
    }
}
