/*
 * ************************************************************
 * 文件：MusicDetailFragmentLandSpace.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月05日 09:52:36
 * 上次修改时间：2019年01月05日 09:50:18
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.SeekBar;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Date;

import top.geek_studio.chenlongcould.musicplayer.Activities.CarViewActivity;
import top.geek_studio.chenlongcould.musicplayer.Adapters.MyWaitListAdapter;
import top.geek_studio.chenlongcould.musicplayer.BroadCasts.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.databinding.FragmentMusicDetail2Binding;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public final class MusicDetailFragmentLandSpace extends Fragment {

    private static final String TAG = "DetailLandSpace";
    private static boolean HIDE_TOOLBAR = false;
    public NotLeakHandler mHandler;
    private boolean SLIDED = false;
    private FragmentMusicDetail2Binding mMusicDetail2Binding;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView mRecyclerView;
    private MyWaitListAdapter mWaitListAdapter;

    private CarViewActivity mCarViewActivity;

    public static MusicDetailFragmentLandSpace newInstance() {
        return new MusicDetailFragmentLandSpace();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCarViewActivity = (CarViewActivity) context;
        mHandler = new NotLeakHandler(mCarViewActivity);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mMusicDetail2Binding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_detail_2, container, false);

        mRecyclerView = mMusicDetail2Binding.getRoot().findViewById(R.id.recycler_view);

        mLinearLayoutManager = new LinearLayoutManager(mCarViewActivity);
        mRecyclerView = mMusicDetail2Binding.getRoot().findViewById(R.id.recycler_view);
        mWaitListAdapter = new MyWaitListAdapter(mCarViewActivity, Data.sPlayOrderList);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mCarViewActivity, DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mWaitListAdapter);

        mMusicDetail2Binding.toolbar.inflateMenu(R.menu.menu_toolbar_in_detail);
        mMusicDetail2Binding.toolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.menu_toolbar_fast_play: {
                    Utils.SendSomeThing.sendPlay(mCarViewActivity, ReceiverOnMusicPlay.TYPE_SHUFFLE, PlayListFragment.TAG);
                }
                break;

                case R.id.menu_toolbar_eq: {
                    Utils.Audio.openEqualizer(mCarViewActivity, Data.sCurrentMusicItem.getAlbumId());
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
        mMusicDetail2Binding.toolbar.setNavigationOnClickListener(v -> mCarViewActivity.onBackPressed());

        mMusicDetail2Binding.includeSeekBarCar.seekBar.getThumb().setTint(Color.WHITE);
        mMusicDetail2Binding.includeSeekBarCar.seekBar.getProgressDrawable().setTint(Color.WHITE);

        //set menu overflow ico white
        Drawable drawable = mMusicDetail2Binding.toolbar.getOverflowIcon();
        if (drawable != null) {
            drawable.setTint(Color.WHITE);
        }

        setData();

        if (Values.HAS_PLAYED) {
            mMusicDetail2Binding.includePlayerControlCar.playButton.setImageResource(R.drawable.ic_pause_black_24dp);
        } else {
            mMusicDetail2Binding.includePlayerControlCar.playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }

        mMusicDetail2Binding.includePlayerControlCar.nextButton.setOnClickListener(v -> Utils.SendSomeThing.sendPlay(mCarViewActivity, 6, "next"));

        mMusicDetail2Binding.includePlayerControlCar.nextButton.setOnLongClickListener(v -> {
            Values.BUTTON_PRESSED = true;
            int nowPosition = mMusicDetail2Binding.includeSeekBarCar.seekBar.getProgress() + ReceiverOnMusicPlay.getDuration() / 20;
            if (nowPosition >= mMusicDetail2Binding.includeSeekBarCar.seekBar.getMax()) {
                nowPosition = mMusicDetail2Binding.includeSeekBarCar.seekBar.getMax();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mMusicDetail2Binding.includeSeekBarCar.seekBar.setProgress(nowPosition, true);
            } else {
                mMusicDetail2Binding.includeSeekBarCar.seekBar.setProgress(nowPosition);
            }
            ReceiverOnMusicPlay.seekTo(nowPosition);
            Values.BUTTON_PRESSED = false;
            return true;
        });

        mMusicDetail2Binding.includePlayerControlCar.previousButton.setOnClickListener(v -> {

            //当进度条大于播放总长 1/20 那么重新播放该歌曲
            if (ReceiverOnMusicPlay.getCurrentPosition() > ReceiverOnMusicPlay.getDuration() / 20 || Values.CurrentData.CURRENT_MUSIC_INDEX == 0) {
                ReceiverOnMusicPlay.seekTo(0);
            } else {
                Utils.SendSomeThing.sendPlay(mCarViewActivity, 6, "previous");
            }

        });

        mMusicDetail2Binding.includePlayerControlCar.previousButton.setOnLongClickListener(v -> {
            int nowPosition = mMusicDetail2Binding.includeSeekBarCar.seekBar.getProgress() - ReceiverOnMusicPlay.getDuration() / 20;
            if (nowPosition <= 0) nowPosition = 0;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mMusicDetail2Binding.includeSeekBarCar.seekBar.setProgress(nowPosition, true);
            } else {
                mMusicDetail2Binding.includeSeekBarCar.seekBar.setProgress(nowPosition);
            }
            ReceiverOnMusicPlay.seekTo(nowPosition);
            return true;
        });

        mMusicDetail2Binding.includePlayerControlCar.repeatButton.setOnClickListener(v -> {

            /*
             * COMMON = 0f
             * REPEAT = 1f
             * REPEAT_ONE = 1f(another pic)
             * */
            final ValueAnimator animator = new ValueAnimator();
            animator.setDuration(300);
            mMusicDetail2Binding.includePlayerControlCar.repeatButton.clearAnimation();
            switch (Values.CurrentData.CURRENT_AUTO_NEXT_TYPE) {
                case Values.TYPE_COMMON: {
                    Values.CurrentData.CURRENT_AUTO_NEXT_TYPE = Values.TYPE_REPEAT;
                    mMusicDetail2Binding.includePlayerControlCar.repeatButton.setImageResource(R.drawable.ic_repeat_white_24dp);
                    animator.setFloatValues(0.3f, 1f);
                    animator.addUpdateListener(animation -> mMusicDetail2Binding.includePlayerControlCar.repeatButton.setAlpha((Float) animation.getAnimatedValue()));
                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mMusicDetail2Binding.includePlayerControlCar.repeatButton.setAlpha(1f);
                            mMusicDetail2Binding.includePlayerControlCar.repeatButton.clearAnimation();
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
                    mMusicDetail2Binding.includePlayerControlCar.repeatButton.setImageResource(R.drawable.ic_repeat_one_white_24dp);
                }
                break;
                case Values.TYPE_REPEAT_ONE: {
                    Values.CurrentData.CURRENT_AUTO_NEXT_TYPE = Values.TYPE_COMMON;
                    mMusicDetail2Binding.includePlayerControlCar.repeatButton.setImageResource(R.drawable.ic_repeat_white_24dp);
                    animator.setFloatValues(1f, 0.3f);
                    animator.addUpdateListener(animation -> mMusicDetail2Binding.includePlayerControlCar.repeatButton.setAlpha((Float) animation.getAnimatedValue()));
                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mMusicDetail2Binding.includePlayerControlCar.repeatButton.setAlpha(0.3f);
                            mMusicDetail2Binding.includePlayerControlCar.repeatButton.clearAnimation();
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

        mMusicDetail2Binding.includePlayerControlCar.randomButton.setOnClickListener(v -> {
            mMusicDetail2Binding.includePlayerControlCar.randomButton.clearAnimation();
            final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mCarViewActivity).edit();
            if (Values.CurrentData.CURRENT_PLAY_TYPE.equals(Values.TYPE_RANDOM)) {
                Values.CurrentData.CURRENT_PLAY_TYPE = Values.TYPE_COMMON;
                final ValueAnimator animator = new ValueAnimator();
                animator.setFloatValues(1f, 0.3f);
                animator.setDuration(300);
                animator.addUpdateListener(animation -> mMusicDetail2Binding.includePlayerControlCar.randomButton.setAlpha((Float) animation.getAnimatedValue()));
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mMusicDetail2Binding.includePlayerControlCar.randomButton.setAlpha(0.3f);
                        mMusicDetail2Binding.includePlayerControlCar.randomButton.clearAnimation();
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

                mWaitListAdapter.notifyDataSetChanged();

            } else {
                Values.CurrentData.CURRENT_PLAY_TYPE = Values.TYPE_RANDOM;
                final ValueAnimator animator = new ValueAnimator();
                animator.setFloatValues(0.3f, 1f);
                animator.setDuration(300);
                animator.addUpdateListener(animation -> mMusicDetail2Binding.includePlayerControlCar.randomButton.setAlpha((Float) animation.getAnimatedValue()));
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mMusicDetail2Binding.includePlayerControlCar.randomButton.setAlpha(1f);
                        mMusicDetail2Binding.includePlayerControlCar.randomButton.clearAnimation();
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

                mWaitListAdapter.notifyDataSetChanged();

            }
        });

        mMusicDetail2Binding.includePlayerControlCar.playButton.setOnClickListener(v -> {
            if (ReceiverOnMusicPlay.isPlayingMusic()) {
                Utils.SendSomeThing.sendPause(mCarViewActivity);
            } else {
                ReceiverOnMusicPlay.playMusic();
                Utils.SendSomeThing.sendPlay(mCarViewActivity, 2, null);        //just resumePlay
            }
        });

        mMusicDetail2Binding.backgroundImage.setOnClickListener(v -> {

            if (!mCarViewActivity.isVisible()) {
                Log.d(TAG, "onCreateView: show");
//                showToolbar();
                mCarViewActivity.show();
            } else {
                Log.d(TAG, "onCreateView: hide");
//                hideToolbar();
                mCarViewActivity.hide();
            }
        });

        mMusicDetail2Binding.includeSeekBarCar.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                mHandler.sendEmptyMessage(Values.HandlerWhat.SET_BUTTON_PLAY);
            }
        });

        mMusicDetail2Binding.waitPlayBodyUp.setOnClickListener(v -> {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mMusicDetail2Binding.waitPlayBodyUp.getLayoutParams();
            ValueAnimator animator = new ValueAnimator();
            if (SLIDED) {
                GlideApp.with(this).load(R.drawable.ic_keyboard_arrow_up_white_24dp).into(mMusicDetail2Binding.waitPlayBodyUp);
                animator.setFloatValues(getResources().getDimension(R.dimen.margin_car_list), getResources().getDimension(R.dimen.margin_8));
                SLIDED = false;
            } else {
                GlideApp.with(this).load(R.drawable.ic_keyboard_arrow_down_white_24dp).into(mMusicDetail2Binding.waitPlayBodyUp);
                animator.setFloatValues(getResources().getDimension(R.dimen.margin_8), getResources().getDimension(R.dimen.margin_car_list));
                SLIDED = true;
            }
            animator.setDuration(300);
            animator.addUpdateListener(animation -> {
                params.bottomMargin = ((int) (float) animation.getAnimatedValue());
                mMusicDetail2Binding.waitPlayBodyUp.requestLayout();
            });
            animator.start();

            mMusicDetail2Binding.waitPlayBodyUp.setLayoutParams(params);
            mMusicDetail2Binding.waitPlayBodyUp.requestLayout();
        });

        return mMusicDetail2Binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateSeek();

        mMusicDetail2Binding.includeSeekBarCar.seekBar.setProgress(0);
        mMusicDetail2Binding.includeSeekBarCar.rightText.setText(String.valueOf(Data.sSimpleDateFormat.format(new Date(ReceiverOnMusicPlay.getDuration()))));
        mMusicDetail2Binding.includeSeekBarCar.seekBar.setMax(ReceiverOnMusicPlay.getDuration());
    }

    public final void setData() {
        mCarViewActivity.runOnUiThread(() -> {
            //load image
            GlideApp.with(MusicDetailFragmentLandSpace.this)
                    .load(Data.getCurrentCover() == null ? R.drawable.ic_audiotrack_24px : Data.getCurrentCover())
                    .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                    .into(mMusicDetail2Binding.albumImage);

            GlideApp.with(MusicDetailFragmentLandSpace.this)
                    .load(Data.getCurrentCover() == null ? R.drawable.ic_audiotrack_24px : Data.getCurrentCover())
                    .apply(bitmapTransform(Data.sBlurTransformationCarView))
                    .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                    .into(mMusicDetail2Binding.backgroundImage);

            //get name
            mMusicDetail2Binding.albumText.setText(Data.sCurrentMusicItem.getMusicAlbum());
            mMusicDetail2Binding.musicName.setText(Data.sCurrentMusicItem.getMusicName());

            mCarViewActivity.runOnUiThread(() -> mLinearLayoutManager.scrollToPositionWithOffset(Values.CurrentData.CURRENT_MUSIC_INDEX == Data.sMusicItems.size() ?
                    Values.CurrentData.CURRENT_MUSIC_INDEX : Values.CurrentData.CURRENT_MUSIC_INDEX + 1, 0));
        });
    }

    public final void setButtonType(String type) {
        mCarViewActivity.runOnUiThread(() -> GlideApp.with(mCarViewActivity)
                .load(type.equals("play") ? R.drawable.ic_play_arrow_black_24dp : R.drawable.ic_pause_black_24dp)
                .into(mMusicDetail2Binding.includePlayerControlCar.playButton));
    }

    private void updateSeek() {
        //点击body 或 music 正在播放 才可以进行seekBar更新

        if (Data.sMusicBinder != null && ReceiverOnMusicPlay.isPlayingMusic()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mMusicDetail2Binding.includeSeekBarCar.seekBar.setProgress(ReceiverOnMusicPlay.getCurrentPosition(), true);
            } else {
                mMusicDetail2Binding.includeSeekBarCar.seekBar.setProgress(ReceiverOnMusicPlay.getCurrentPosition());
            }
            mMusicDetail2Binding.includeSeekBarCar.leftText.setText(String.valueOf(Data.sSimpleDateFormat.format(new Date(ReceiverOnMusicPlay.getCurrentPosition()))));
        }

        mHandler.postDelayed(this::updateSeek, 100);
    }

    public NotLeakHandler getHandler() {
        return mHandler;
    }

    /**
     * hide or show toolbar
     * same as {@link MusicDetailFragment#showToolbar()}
     * {@link MusicDetailFragment#hideToolbar()}
     */
    private void showToolbar() {
        HIDE_TOOLBAR = false;
        final ValueAnimator anim = ValueAnimator.ofFloat(0, 1f);
        anim.setDuration(300);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mMusicDetail2Binding.appbar.clearAnimation();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mMusicDetail2Binding.appbar.setVisibility(View.VISIBLE);
            }
        });
        anim.addUpdateListener(animation1 -> mMusicDetail2Binding.appbar.setAlpha((Float) animation1.getAnimatedValue()));
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
                mMusicDetail2Binding.appbar.clearAnimation();
                mMusicDetail2Binding.appbar.setAlpha(0f);
                mMusicDetail2Binding.appbar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mMusicDetail2Binding.appbar.startAnimation(temp);
    }

    public FragmentMusicDetail2Binding getMusicDetail2Binding() {
        return mMusicDetail2Binding;
    }

    public static final class NotLeakHandler extends Handler {
        private WeakReference<CarViewActivity> mWeakReference;
        private FragmentMusicDetail2Binding mMusicDetail2Binding;

        NotLeakHandler(CarViewActivity activity) {
            mWeakReference = new WeakReference<>(activity);
            mMusicDetail2Binding = mWeakReference.get().getFragmentLandSpace().getMusicDetail2Binding();
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {

            }

        }
    }

}