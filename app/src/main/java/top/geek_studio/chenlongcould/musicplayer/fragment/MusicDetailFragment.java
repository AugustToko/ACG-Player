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
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
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
import android.widget.FrameLayout;
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

import org.jetbrains.annotations.NotNull;

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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jp.wasabeef.glide.transformations.BlurTransformation;
import top.geek_studio.chenlongcould.geeklibrary.widget.GkSnackbar;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.AlbumDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.PublicActivity;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyWaitListAdapter;
import top.geek_studio.chenlongcould.musicplayer.broadcast.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.custom_view.AlbumImageView;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.utils.MusicUtil;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

/**
 * @author chenlongcould
 */
public final class MusicDetailFragment extends Fragment {

	//////////////////////////////////////////////////////////////////////////////////////

	public static final String TAG = "MusicDetailFragment";

	/**
	 * @see MainActivity#CURRENT_SLIDE_OFFSET
	 */
	public static float CURRENT_SLIDE_OFFSET = 1;

	/**
	 * Handler
	 */
	private NotLeakHandler mHandler;
	private BroadcastReceiver mButtonChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action == null) {
				return;
			}
			switch (action) {
				case BroadCastAction.ACTION_SCROLL: {
					mHandler.sendEmptyMessage(HandlerWhat.RECYCLER_SCROLL);
				}
				break;
				case BroadCastAction.ACTION_CHANGE_BUTTON_PAUSE: {
					mHandler.sendEmptyMessage(HandlerWhat.SET_BUTTON_PAUSE);
				}
				break;
				case BroadCastAction.ACTION_INIT_SEEK_BAR: {
					mHandler.sendEmptyMessage(HandlerWhat.INIT_SEEK_BAR);
				}
				break;
				case BroadCastAction.ACTION_UPDATE_CURRENT_INFO: {
					setCurrentInfo(Data.sCurrentMusicItem.getMusicName(), Data.sCurrentMusicItem.getMusicAlbum(), Utils.Audio.getCoverBitmap(context, Data.sCurrentMusicItem.getAlbumId()));
				}
				break;
				default:
			}
		}
	};

	float mLastX = 0;
	float mLastY = 0;
	float moveX = 0;
	float moveY = 0;
	private boolean HIDE_TOOLBAR = false;
	private boolean SNACK_NOTICE = false;
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

	/**
	 * @see #mMusicAlbumImage 的滑动模式
	 * 0: just x
	 * 1: x & y
	 */
	private int mode = 0;

	private AlbumImageView mMusicAlbumImage;
	private AlbumImageView mMusicAlbumImageOth2;
	private AlbumImageView mMusicAlbumImageOth3;
	private MyWaitListAdapter mMyWaitListAdapter;
	/**
	 * 记录是否为长按 (2000ms)
	 */
	private volatile AtomicBoolean lc = new AtomicBoolean(false);

	/**
	 * changeButton play or pause
	 */
	private void receiveButtonChange() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BroadCastAction.ACTION_CHANGE_BUTTON_PAUSE);
		intentFilter.addAction(BroadCastAction.ACTION_SCROLL);
		intentFilter.addAction(BroadCastAction.ACTION_INIT_SEEK_BAR);
		intentFilter.addAction(BroadCastAction.ACTION_UPDATE_CURRENT_INFO);
		mBroadcastManager.registerReceiver(mButtonChangeReceiver, intentFilter);
	}

	private LocalBroadcastManager mBroadcastManager;

	/**
	 * init Views
	 */
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

		mCurrentInfoSeek.setBackgroundColor(Utils.Ui.getAccentColor(mMainActivity));

		setDefAnimation();
		clearAnimations();

		setUpToolbar();

		mSlidingUpPanelLayout.post(() -> {
			int val0 = view.getHeight() - view.findViewById(R.id.frame_ctrl).getBottom();
			mSlidingUpPanelLayout.setPanelHeight(val0);
		});

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
					GlideApp.with(MusicDetailFragment.this)
							.load(befItem == null ? R.drawable.default_album_art : Utils.Audio.getCoverBitmap(mMainActivity, befItem.getAlbumId()))
							.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
							.diskCacheStrategy(DiskCacheStrategy.NONE)
							.into(mMusicAlbumImageOth3);

					GlideApp.with(MusicDetailFragment.this)
							.load(nexItem == null ? R.drawable.default_album_art : Utils.Audio.getCoverBitmap(mMainActivity, nexItem.getAlbumId()))
							.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
							.diskCacheStrategy(DiskCacheStrategy.NONE)
							.into(mMusicAlbumImageOth2);

					moveX = event.getX();
					moveY = event.getY();
					mLastX = event.getRawX();
					mLastY = event.getRawY();

					break;
				case MotionEvent.ACTION_MOVE:

//                    if (mSlidingUpPanelLayout.getPanelState() != SlidingUpPanelLayout.PanelState.EXPANDED || CURRENT_SLIDE_OFFSET != 0)
					if (mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
						break;
					}

					//首尾禁止对应边缘滑动
					if (Values.CurrentData.CURRENT_MUSIC_INDEX == 0) {
						if (event.getRawX() > mLastX) {
							break;
						}
					}

					if (Values.CurrentData.CURRENT_MUSIC_INDEX == Data.sPlayOrderList.size() - 1) {
						if (event.getRawX() < mLastX) {
							break;
						}
					}

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
								Bitmap bitmap = null;
								if (finalNexItem != null) {
									bitmap = Utils.Audio.getCoverBitmap(mMainActivity, finalNexItem.getAlbumId());
								}
								GlideApp.with(MusicDetailFragment.this)
										.load(finalNexItem == null ? R.drawable.default_album_art : bitmap)
										.into(mMusicAlbumImage);
								setIcoLightOrDark(bitmap);
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
								Bitmap bitmap = null;
								if (finalBefItem != null) {
									bitmap = Utils.Audio.getCoverBitmap(mMainActivity, finalBefItem.getAlbumId());
								}
								GlideApp.with(MusicDetailFragment.this)
										.load(finalBefItem == null ? R.drawable.default_album_art : bitmap)
										.into(mMusicAlbumImage);
								setIcoLightOrDark(bitmap);
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
				default:
			}
			return true;
		});

		setClickListener();

		/*---------------------- Menu -----------------------*/
		mPopupMenu = new PopupMenu(mMainActivity, mMenuButton);

		final Menu menu = mPopupMenu.getMenu();

		menu.add(Menu.NONE, Menu.FIRST + 1, 0, "查看专辑");
		menu.add(Menu.NONE, Menu.FIRST + 2, 0, "详细信息");

		mPopupMenu.setOnMenuItemClickListener(item -> {
			switch (item.getItemId()) {
				case Menu.FIRST + 1: {
					final MusicItem currentItem = ReceiverOnMusicPlay.getCurrentItem();
					if (currentItem != null) {
						final String albumName = currentItem.getMusicAlbum();
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
				}
				break;
				case Menu.FIRST + 2: {
					Intent intent = new Intent(mMainActivity, PublicActivity.class);
					intent.putExtra("start_by", "detail");
					startActivity(intent);
				}
				break;
				default:
			}
			return false;
		});

		mCurrentInfoBody.setOnClickListener(v -> {

			if (mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
				mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
			} else {
				mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
			}

			mHandler.sendEmptyMessage(HandlerWhat.RECYCLER_SCROLL);

		});

		mCurrentInfoBody.setOnLongClickListener(v -> {
			dropToTrash(ReceiverOnMusicPlay.getCurrentItem());
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
				mHandler.sendEmptyMessage(HandlerWhat.SET_BUTTON_PLAY);
			}
		});

		mSlidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
			@Override
			public void onPanelSlide(View panel, float slideOffset) {
				CURRENT_SLIDE_OFFSET = slideOffset;
				mSlideUpGroup.setTranslationY(0 - slideOffset * 120);
				if (slideOffset == 0) {
					mMainActivity.getMainBinding().slidingLayout.setTouchEnabled(true);
				}
			}

			@Override
			public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
				if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
					mMainActivity.getMainBinding().slidingLayout.setTouchEnabled(true);
					mHandler.sendEmptyMessage(HandlerWhat.RECYCLER_SCROLL);
				} else {
					mMainActivity.getMainBinding().slidingLayout.setTouchEnabled(false);
				}
			}
		});

	}

	@Override
	public void onAttach(@NotNull Context context) {
		Log.d(Values.LogTAG.LAG_TAG, "onAttach: MusicDetailFragment");
		super.onAttach(context);
		mMainActivity = (MainActivity) context;

		mHandlerThread = new HandlerThread("Handler Thread in MusicDetailActivity");
		mHandlerThread.start();
		mHandler = new MusicDetailFragment.NotLeakHandler(mMainActivity, mHandlerThread.getLooper());

		mBroadcastManager = LocalBroadcastManager.getInstance(context);
		receiveButtonChange();
	}

	public static MusicDetailFragment newInstance() {
		return new MusicDetailFragment();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_music_detail, container, false);

		initView(view);

		//init seekBar
		mHandler.sendEmptyMessage(HandlerWhat.INIT_SEEK_BAR);
		//let seekBar loop update
		mHandler.sendEmptyMessage(HandlerWhat.SEEK_BAR_UPDATE);
		//scroll to the position{@Values.CurrentData.CURRENT_MUSIC_INDEX}
		mHandler.sendEmptyMessage(HandlerWhat.RECYCLER_SCROLL);

		if (Data.sMusicBinder != null) {
			//检测后台播放
			try {

				MusicItem item = Data.sMusicBinder.getCurrentItem();
				if (item.getMusicID() != -1) {
					mCurrentMusicNameText.setText(item.getMusicName());
					mCurrentAlbumNameText.setText(item.getMusicAlbum());

					Bitmap bitmap = Utils.Audio.getCoverBitmap(getContext(), item.getAlbumId());
					mNowPlayingStatusImage.setImageResource(R.drawable.ic_pause_black_24dp);
					mPlayButton.setImageResource(R.drawable.ic_pause_black_24dp);

					setSlideInfo(item.getMusicName(), item.getMusicAlbum(), bitmap);
					setCurrentInfo(item.getMusicName(), item.getMusicAlbum(), bitmap);

					mMainActivity.getMainBinding().slidingLayout.setTouchEnabled(true);

				} else {

				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}

		} else {
			if (Data.sCurrentMusicItem.getMusicID() != -1) {
				final Bitmap cover = Utils.Audio.getCoverBitmap(getContext(), Data.sCurrentMusicItem.getAlbumId());
				setCurrentInfo(Data.sCurrentMusicItem.getMusicName(), Data.sCurrentMusicItem.getMusicAlbum(), cover);
			}
		}
		return view;
	}

	public final void setCurrentInfo(@NonNull final String name, @NonNull final String albumName, final Bitmap cover) {
		mMainActivity.runOnUiThread(() -> {
			if (Data.getCurrentCover() != null && !Data.getCurrentCover().isRecycled()) {
				Data.getCurrentCover().recycle();
			}

			Data.setCurrentCover(cover);

			mCurrentMusicNameText.setText(name);
			mCurrentAlbumNameText.setText(albumName);

			setSlideInfo(name, albumName, cover);

			GlideApp.with(mMainActivity)
					.load(cover)
					.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
					.centerCrop()
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.into(mMusicAlbumImage);

			final MusicItem item = ReceiverOnMusicPlay.getCurrentItem();
			if (item != null) {
				updateFav(item);
			}

			Utils.Ui.setBlurEffect(mMainActivity, cover, mBGup, mBGdown, mNextWillText);
		});
	}

	public final ConstraintLayout getNowPlayingBody() {
		return mNowPlayingBody;
	}

	/**
	 * clearData all animations
	 */
	public void clearAnimations() {
		mRandomButton.clearAnimation();
		mRepeatButton.clearAnimation();
		mPlayButton.clearAnimation();
		mPreviousButton.clearAnimation();
		mNextButton.clearAnimation();
	}

	/**
	 * set all animations Default
	 */
	public final void setDefAnimation() {
		mRandomButton.setAlpha(0f);
		mRepeatButton.setAlpha(0f);
		mPlayButton.setRotation(-90f);
		mPlayButton.setScaleX(0);
		mPlayButton.setScaleY(0);
	}

	/**
	 * init & start animations
	 */
	public final void initAnimation() {
		setIcoLightOrDark(Data.getCurrentCover());
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
	 * set Info (auto put in data.)
	 *
	 * @param songName  music name
	 * @param albumName music album name
	 * @param cover     music cover image, it is @NullAble(some types of music do not have cover)
	 */
	private void setSlideInfo(String songName, String albumName, Bitmap cover) {
		mMainActivity.runOnUiThread(() -> {
			setIcoLightOrDark(cover);

			mNowPlayingSongText.setText(songName);
			mNowPlayingSongAlbumText.setText(albumName);

			GlideApp.with(MusicDetailFragment.this).load(cover)
					.transition(DrawableTransitionOptions.withCrossFade())
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.into(mNowPlayingSongImage);

			GlideApp.with(MusicDetailFragment.this).load(cover)
					.transition(DrawableTransitionOptions.withCrossFade())
					.centerCrop()
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.into(mMainActivity.getNavHeaderImageView());

			//load blur...
			GlideApp.with(MusicDetailFragment.this).load(cover)
					.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
					.apply(bitmapTransform(new BlurTransformation(30, 20)))
					.override(50, 50)
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.into(mNowPlayingBackgroundImage);
		});
	}

	/**
	 * find view by id
	 *
	 * @see android.app.Activity#findViewById(int)
	 */
	private void findView(View view) {
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
	}

	private void setUpToolbar() {
		/*------------------toolbar--------------------*/
		mToolbar.inflateMenu(R.menu.menu_toolbar_in_detail);

		mToolbar.setOnMenuItemClickListener(menuItem -> {
			switch (menuItem.getItemId()) {
				case R.id.menu_toolbar_fast_play: {
					Utils.SendSomeThing.sendPlay(mMainActivity, ReceiverOnMusicPlay.CASE_TYPE_SHUFFLE, PlayListFragment.TAG);
				}
				break;

				case R.id.menu_toolbar_love: {
					MusicUtil.toggleFavorite(mMainActivity, ReceiverOnMusicPlay.getCurrentItem());
					updateFav(ReceiverOnMusicPlay.getCurrentItem());
					Toast.makeText(mMainActivity, getString(R.string.done), Toast.LENGTH_SHORT).show();
				}
				break;

				case R.id.menu_toolbar_eq: {
					MusicItem item = ReceiverOnMusicPlay.getCurrentItem();
					if (item != null) {
						Utils.Audio.openEqualizer(mMainActivity, item.getAlbumId());
					}
				}

				case R.id.menu_toolbar_debug: {
				}
				break;
				case R.id.menu_toolbar_trash_can: {
					dropToTrash(ReceiverOnMusicPlay.getCurrentItem());
				}
				break;
				default:
			}
			return false;
		});

		mToolbar.setNavigationOnClickListener(v -> mMainActivity.getHandler().sendEmptyMessage(MainActivity.DOWN));
	}

	private void setClickListener() {
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
				default:
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

		mMusicAlbumImage.setOnClickListener(v -> {
			if (HIDE_TOOLBAR) {
				showToolbar();
			} else {
				hideToolbar();
			}
		});

		mMenuButton.setOnClickListener(v -> mPopupMenu.show());

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
			//noinspection AlibabaUndefineMagicConstant
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
			if (Data.HAS_PLAYED) {
				if (ReceiverOnMusicPlay.isPlayingMusic()) {
					Utils.SendSomeThing.sendPause(mMainActivity);
				} else {
					Utils.SendSomeThing.sendPlay(mMainActivity, ReceiverOnMusicPlay.CASE_TYPE_NOTIFICATION_RESUME, null);
				}
			} else {
				Utils.SendSomeThing.sendPlay(mMainActivity, ReceiverOnMusicPlay.CASE_TYPE_SHUFFLE, TAG);
			}
		});

		mNowPlayingFavButton.setOnClickListener(v -> {
			MusicUtil.toggleFavorite(mMainActivity, ReceiverOnMusicPlay.getCurrentItem());
			updateFav(ReceiverOnMusicPlay.getCurrentItem());
			Toast.makeText(mMainActivity, getString(R.string.done), Toast.LENGTH_SHORT).show();
		});

		mNowPlayingBody.setOnClickListener(v -> {
			if (Data.HAS_PLAYED) {
				mMainActivity.getHandler().sendEmptyMessage(MainActivity.UP);
			} else {
				Utils.Ui.fastToast(mMainActivity, "No music playing.");
			}
		});
	}

	/**
	 * the action drop to crash
	 */
	private void dropToTrash(@Nullable MusicItem item) {
		if (item != null) {
			if (PreferenceManager.getDefaultSharedPreferences(mMainActivity).getBoolean(Values.SharedPrefsTag.TIP_NOTICE_DROP_TRASH, true)) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
				builder.setTitle(getString(R.string.sure_int));
				builder.setMessage(getString(R.string.drop_to_trash_can));
				final FrameLayout frameLayout = new FrameLayout(mMainActivity);
				final CheckBox checkBox = new CheckBox(mMainActivity);
				checkBox.setText(getString(R.string.do_not_show_again));
				frameLayout.addView(checkBox);
				final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				params.leftMargin = (int) mMainActivity.getResources().getDimension(R.dimen.margin_16);
				checkBox.setLayoutParams(params);
				builder.setView(frameLayout);
				builder.setCancelable(true);
				builder.setNegativeButton(getString(R.string.sure), (dialog, which) -> {
					if (checkBox.isChecked()) {
						PreferenceManager.getDefaultSharedPreferences(mMainActivity).edit().putBoolean(Values.SharedPrefsTag.TIP_NOTICE_DROP_TRASH, false).apply();
					}
					Data.sTrashCanList.add(item);
					dialog.dismiss();
				});
				builder.setPositiveButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
				builder.show();
			} else {
				Data.sTrashCanList.add(item);
			}
		}
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

	public final void setCurrentInfoWithoutMainImage(@NonNull final String name, @NonNull final String albumName, final Bitmap cover) {
		mMainActivity.runOnUiThread(() -> {
			mCurrentMusicNameText.setText(name);
			mCurrentAlbumNameText.setText(albumName);
			Utils.Ui.setBlurEffect(mMainActivity, cover, mBGup, mBGdown, mNextWillText);
		});
	}

	/**
	 * for {@link #mHandler}
	 */
	public interface HandlerWhat {
		int INIT_SEEK_BAR = 54;
		int RECYCLER_SCROLL = 55001;
		int SET_BUTTON_PAUSE = 57;
		int SEEK_BAR_UPDATE = 53;
		int SET_BUTTON_PLAY = 58;
	}

	public final NotLeakHandler getHandler() {
		return mHandler;
	}

	/**
	 * update Favourite music icon
	 */
	private void updateFav(@Nullable MusicItem item) {
		if (item != null) {
			@DrawableRes int id = MusicUtil.isFavorite(mMainActivity, item) ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp;
			mToolbar.getMenu().findItem(R.id.menu_toolbar_love).setIcon(id);
			mNowPlayingFavButton.setImageResource(id);
		}
	}

	/**
	 * action for broadcast {@link #mBroadcastManager}
	 */
	public interface BroadCastAction {
		String ACTION_CHANGE_BUTTON_PAUSE = "ACTION_CHANGE_BUTTON_PAUSE";
		String ACTION_SCROLL = "ACTION_SCROLL";
		String ACTION_INIT_SEEK_BAR = "ACTION_INIT_SEEK_BAR";
		String ACTION_UPDATE_CURRENT_INFO = "ACTION_UPDATE_CURRENT_INFO";
	}

	/**
	 * setIcon White or Black (by bitmap light)
	 *
	 * @param bitmap backgroundImage
	 */
	@SuppressLint("StaticFieldLeak")
	private void setIcoLightOrDark(@Nullable final Bitmap bitmap) {
		if (bitmap != null) {
			new AsyncTask<Void, Void, Integer>() {

				@Override
				protected Integer doInBackground(Void... voids) {
					//InfoBar background color AND text color balance
					final int currentBright = Utils.Ui.getBright(bitmap);
					return currentBright;
				}

				@Override
				protected void onPostExecute(Integer integer) {
					if (integer > (255 / 2)) {
						@ColorInt final int target = ContextCompat.getColor(mMainActivity, R.color.notVeryBlack);

						ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), mNextWillText.getCurrentTextColor(), target);
						animator.setDuration(0);
						animator.setStartDelay(300);
						animator.addUpdateListener(animation -> {
							@ColorInt int val = (int) animation.getAnimatedValue();
							mNowPlayingSongText.setTextColor(val);
							mNowPlayingSongAlbumText.setTextColor(val);
							mNowPlayingStatusImage.setColorFilter(val);
							mNowPlayingFavButton.setColorFilter(val);

							mRandomButton.setColorFilter(val);
							mRepeatButton.setColorFilter(val);
							mNextButton.setColorFilter(val);
							mPreviousButton.setColorFilter(val);
							mLeftTime.setTextColor(val);
							mRightTime.setTextColor(val);

							mSeekBar.getProgressDrawable().setTint(val);
							mSeekBar.getThumb().setTint(val);
						});
						animator.start();
					} else {
						@ColorInt final int target = ContextCompat.getColor(mMainActivity, R.color.notVeryWhite);

						ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), mNextWillText.getCurrentTextColor(), target);
						animator.setDuration(0);
						animator.addUpdateListener(animation -> {
							@ColorInt int val = (int) animation.getAnimatedValue();
							mNowPlayingSongText.setTextColor(val);
							mNowPlayingSongAlbumText.setTextColor(val);
							mNowPlayingStatusImage.setColorFilter(val);
							mNowPlayingFavButton.setColorFilter(val);

							mRandomButton.setColorFilter(val);
							mRepeatButton.setColorFilter(val);
							mNextButton.setColorFilter(val);
							mPreviousButton.setColorFilter(val);
							mLeftTime.setTextColor(val);
							mRightTime.setTextColor(val);

							mSeekBar.getProgressDrawable().setTint(val);
							mSeekBar.getThumb().setTint(val);
						});
						animator.start();
					}

				}
			}.execute();
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
				case HandlerWhat.INIT_SEEK_BAR: {
					mWeakReference.get().runOnUiThread(() -> {
						if (Data.sMusicBinder == null) {
							return;
						}

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

				case HandlerWhat.SEEK_BAR_UPDATE: {
					mWeakReference.get().runOnUiThread(() -> {
						//点击body 或 music 正在播放 才可以进行seekBar更新
						if (Data.sMusicBinder == null) {
							return;
						}

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

									final GkSnackbar gkSnackbar = new GkSnackbar(mSlidingUpPanelLayout, getString(R.string.next_will_play_x,
											Data.sPlayOrderList.get(Values.CurrentData.CURRENT_MUSIC_INDEX + 1 != Data.sPlayOrderList.size() ? Values.CurrentData.CURRENT_MUSIC_INDEX + 1 : 0).getMusicName())
											, Snackbar.LENGTH_LONG);
									gkSnackbar.setAction(getString(R.string.skip), v -> {
										//点击右侧的按钮之后的操作
										Values.CurrentData.CURRENT_MUSIC_INDEX += 1;
										Utils.SendSomeThing.sendPlay(mMainActivity, 6, ReceiverOnMusicPlay.TYPE_NEXT);
									});
									gkSnackbar.addCallback(new Snackbar.Callback() {
										@Override
										public void onDismissed(Snackbar transientBottomBar, int event) {
											SNACK_NOTICE = false;
										}
									});
									gkSnackbar.setBackgroundColor(Utils.Ui.getPrimaryColor(getActivity()))
											.setBodyViewAlpha(0.8f).setActionTextColor(Color.BLACK);
									gkSnackbar.show();

								}

							}

						}
					});

					//循环更新 0.5s 一次
					mHandler.sendEmptyMessageDelayed(HandlerWhat.SEEK_BAR_UPDATE, 500);
				}
				break;

				case HandlerWhat.RECYCLER_SCROLL: {
					mWeakReference.get().runOnUiThread(() -> mLinearLayoutManager.scrollToPositionWithOffset(Values.CurrentData.CURRENT_MUSIC_INDEX == Data.sMusicItems.size() ?
							Values.CurrentData.CURRENT_MUSIC_INDEX : Values.CurrentData.CURRENT_MUSIC_INDEX + 1, 0));
				}
				break;

				case HandlerWhat.SET_BUTTON_PLAY: {
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

				case HandlerWhat.SET_BUTTON_PAUSE: {
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
