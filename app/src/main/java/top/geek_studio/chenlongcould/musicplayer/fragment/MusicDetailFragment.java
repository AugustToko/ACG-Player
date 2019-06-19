package top.geek_studio.chenlongcould.musicplayer.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.*;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.*;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.glide.transformations.BlurTransformation;
import org.jetbrains.annotations.NotNull;
import top.geek_studio.chenlongcould.geeklibrary.widget.GkSnackbar;
import top.geek_studio.chenlongcould.geeklibrary.widget.GkToolbar;
import top.geek_studio.chenlongcould.musicplayer.*;
import top.geek_studio.chenlongcould.musicplayer.activity.albumdetail.AlbumDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.main.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyWaitListAdapter;
import top.geek_studio.chenlongcould.musicplayer.broadcast.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.customView.PlayPauseDrawable;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.utils.MusicUtil;
import top.geek_studio.chenlongcould.musicplayer.utils.PlayListsUtil;
import top.geek_studio.chenlongcould.musicplayer.utils.PreferenceUtil;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

/**
 * @author chenlongcould
 */
public final class MusicDetailFragment extends BaseFragment {

	public static final String TAG = "MusicDetailFragment";

	/**
	 * key for albumImage slide
	 */
	public static final int key = -465854196;
	/**
	 * Handler
	 */
	private static NotLeakHandler mHandler = null;

	/**
	 * for {@link #setUpImage(Bitmap, ImageView, ImageView, TextView)} bitmap. width, height 150
	 */
	private static final int SIZE_OF_BLUR = 150;
	private volatile ImageView MV;
	private volatile ImageView mMusicAlbumImage;
	private volatile ImageView mMusicAlbumImageOth2;
	private volatile ImageView mMusicAlbumImageOth3;
	private ImageView mBGup;
	private ImageView mBGdown;
	private SeekBar mSeekBar;
	private View mInfoBarInfoSeek;

	////////////////////INFO_BAR//////////////////////////
	private TextView mInfoBarSongText;
	private ImageView mInfoBarSongImage;
	private TextView mInfoBarAlbumText;
	private ImageView mInfoBarBackgroundImage;
	private ImageView mInfoBarFavButton;
	private ImageView mInfoBarPlayButton;
	////////////////////INFO_BAR//////////////////////////

	private FloatingActionButton mPlayButton;
	private PlayPauseDrawable mPlayPauseDrawable;
	private PlayPauseDrawable mPlayPauseDrawable2InfoBar;
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
	private GkToolbar mToolbar;
	private AppBarLayout mAppBarLayout;
	private TextView mLeftTime;
	private TextView mRightTime;
	private TextView mNextWillText;
	private SlidingUpPanelLayout mSlidingUpPanelLayout;
	private ConstraintLayout mNowPlayingBody;
	private ConstraintLayout mSlideUpGroup;

	private HandlerThread mHandlerThread;
	/**
	 * 最后一次加载的album id
	 *
	 * @see #setCurrentData(MusicItem, Bitmap, boolean...)
	 */
	private static int mLastAlbumId = -1;
	/**
	 * for {@link #setUpImage(Bitmap, ImageView, ImageView, TextView)}
	 *
	 * @see ViewAnimationUtils#createCircularReveal
	 */
	private final int m_Position = 200;

	private final PopupMenu.OnMenuItemClickListener clickListener = item -> {
		switch (item.getItemId()) {
			case Menu.FIRST + 1: {
				final MusicItem currentItem = ReceiverOnMusicPlay.getCurrentItem();
				if (currentItem != null) {
					final String albumName = currentItem.getMusicAlbum();
					final Cursor cursor = mMainActivity.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null,
							MediaStore.Audio.Albums.ALBUM + "= ?", new String[]{albumName}, null);

					//int MusicDetailActivity
					final Intent intent = new Intent(mMainActivity, AlbumDetailActivity.class);
					intent.putExtra(AlbumDetailActivity.IntentKey.ALBUM_NAME, albumName);
					if (cursor != null) {
						cursor.moveToFirst();
						int id = Integer.parseInt(cursor.getString(0));
						intent.putExtra(AlbumDetailActivity.IntentKey.ID, id);
						cursor.close();
					}
					startActivity(intent);
				}
			}
			break;
			case Menu.FIRST + 2: {
				AlertDialog dialog = Utils.Audio.getMusicDetailDialog(mMainActivity, Data.sCurrentMusicItem);
				if (dialog != null) dialog.show();
			}
			break;

			case Menu.FIRST + 3: {
				final MusicItem target = ReceiverOnMusicPlay.getCurrentItem();
				if (target != null) {
					MusicUtil.dropToTrash(mMainActivity, target);
				}
			}
			break;

			// add to playlist
			case Menu.FIRST + 4: {
				PlayListsUtil.addListDialog(mMainActivity, Data.sCurrentMusicItem);
			}
			break;
			default:
		}
		return true;
	};

	@SuppressWarnings("unused")
	public static boolean sendMessage(@NonNull Message message) {
		boolean result = false;
		if (mHandler != null) {
			mHandler.sendMessage(message);
			result = true;
		}
		return result;
	}

	@SuppressWarnings("UnusedReturnValue")
	public static boolean sendEmptyMessage(int what) {
		boolean result = false;
		if (mHandler != null) {
			mHandler.sendEmptyMessage(what);
			result = true;
		}
		return result;
	}

	/**
	 * init Views
	 */
	private void initView(@NonNull final View view) {

		mPlayPauseDrawable = new PlayPauseDrawable(mMainActivity);
		mPlayPauseDrawable2InfoBar = new PlayPauseDrawable(mMainActivity);
		mPlayPauseDrawable.setPlay(true);
		mPlayPauseDrawable2InfoBar.setPlay(true);

		//get Default values
		defXScale = mPlayButton.getScaleX();
		defYScale = mPlayButton.getScaleY();

		mPlayButton.setImageDrawable(mPlayPauseDrawable);
		mInfoBarPlayButton.setImageDrawable(mPlayPauseDrawable2InfoBar);
		mPlayButton.setColorFilter(Color.BLACK);

		mInfoBarInfoSeek.setBackgroundColor(Utils.Ui.getAccentColor(mMainActivity));

		setDefAnimation();
		clearAnimations();

		setUpToolbar();


		mMusicAlbumImageOth3.setX(0 - mMusicAlbumImage.getWidth());
		mMusicAlbumImageOth2.setX(mMusicAlbumImage.getWidth() * 2);

		MV = mMusicAlbumImage;

//		mMusicAlbumImage.setTag(key, 1);
//		mMusicAlbumImageOth3.setTag(key, 0);
//		mMusicAlbumImageOth2.setTag(key, 2);

//		View.OnTouchListener touchListener = (v, event) -> {
//			final int action = event.getAction();
//
//			MusicItem befItem = null;
//			MusicItem nexItem = null;
//			if (Values.CurrentData.CURRENT_MUSIC_INDEX > 0 && Values.CurrentData.CURRENT_MUSIC_INDEX < Data.sPlayOrderList.size() - 1) {
//				befItem = Data.sPlayOrderList.get(Values.CurrentData.CURRENT_MUSIC_INDEX - 1);
//			}
//			if (Values.CurrentData.CURRENT_MUSIC_INDEX < Data.sPlayOrderList.size() - 1 && Values.CurrentData.CURRENT_MUSIC_INDEX > 0) {
//				nexItem = Data.sPlayOrderList.get(Values.CurrentData.CURRENT_MUSIC_INDEX + 1);
//			}
//
//			switch (action) {
//				case MotionEvent.ACTION_DOWN:
//
//					//预加载
//					GlideApp.with(MusicDetailFragment.this)
//							.load(befItem == null ? R.drawable.default_album_art : Utils.Audio.getCoverBitmapFull(mMainActivity, befItem.getAlbumId()))
//							.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
//							.diskCacheStrategy(DiskCacheStrategy.NONE)
//							.into(getL());
//
//					GlideApp.with(MusicDetailFragment.this)
//							.load(nexItem == null ? R.drawable.default_album_art : Utils.Audio.getCoverBitmapFull(mMainActivity, nexItem.getAlbumId()))
//							.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
//							.diskCacheStrategy(DiskCacheStrategy.NONE)
//							.into(getR());
//
//					moveX = event.getX();
//					mLastX = event.getRawX();
//
//					break;
//				case MotionEvent.ACTION_MOVE:
//
//					if (mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
//						break;
//					}
//
//					//首尾禁止对应边缘滑动
//					if (Values.CurrentData.CURRENT_MUSIC_INDEX == 0) {
//						if (event.getRawX() > mLastX) {
//							break;
//						}
//					}
//
//					if (Values.CurrentData.CURRENT_MUSIC_INDEX == Data.sPlayOrderList.size() - 1) {
//						if (event.getRawX() < mLastX) {
//							break;
//						}
//					}
//
//					float val = MV.getX() + (event.getX() - moveX);
//					MV.setTranslationX(val);
//					RV.setTranslationX(+val);
//					LV.setTranslationX(0 - mMusicAlbumImage.getWidth() + val);
//					break;
//
//				case MotionEvent.ACTION_CANCEL:
//				case MotionEvent.ACTION_UP:
//
//					mSlidingUpPanelLayout.setTouchEnabled(true);
//
//					if (Math.abs(Math.abs(event.getRawX()) - Math.abs(mLastX)) < 5) {
//						MV.performClick();
//						break;
//					}
//
//					/*
//					 * enter Animation...
//					 * */
//					ValueAnimator animatorMain = new ValueAnimator();
//					animatorMain.setDuration(300);
//
//					//左滑一半 滑过去
//					if (MV.getX() < 0 && Math.abs(MV.getX()) >= mMusicAlbumImage.getWidth() / 2) {
//						animatorMain.setFloatValues(MV.getX(), 0 - mMusicAlbumImage.getWidth());
//						MusicItem finalNexItem = nexItem;
//						animatorMain.addListener(new Animator.AnimatorListener() {
//							@Override
//							public void onAnimationStart(Animator animation) {
//							}
//
//							@Override
//							public void onAnimationEnd(Animator animation) {
//								Utils.SendSomeThing.sendPlay(mMainActivity, 6, "next_slide");
//
//								if (finalNexItem != null) {
//									Bitmap bitmap = Utils.Audio.getCoverBitmapFull(mMainActivity, finalNexItem.getAlbumId());
//									setIconLightOrDark(bitmap);
//									mHandler.postDelayed(() -> {
//										if (bitmap != null && !bitmap.isRecycled()) {
//											bitmap.recycle();
//										}
//									}, 5000);
//								}
//
//								MV.setTranslationX(mMusicAlbumImage.getWidth() * 2);
//								ImageView temp = MV;
//								MV = RV;
//								RV = temp;
//
//								RV.setTag(key, 2);
//								MV.setTag(key, 1);
//								LV.setTag(key, 0);
//							}
//
//							@Override
//							public void onAnimationCancel(Animator animation) {
//
//							}
//
//							@Override
//							public void onAnimationRepeat(Animator animation) {
//
//							}
//						});
//
//						/*右滑一半 滑过去*/
//					} else if (MV.getX() > 0 && Math.abs(MV.getX()) >= mMusicAlbumImage.getWidth() / 2) {
//						animatorMain.setFloatValues(MV.getX(), mMusicAlbumImage.getWidth());
//						MusicItem finalBefItem = befItem;
//						animatorMain.addListener(new Animator.AnimatorListener() {
//							@Override
//							public void onAnimationStart(Animator animation) {
//
//							}
//
//							@Override
//							public void onAnimationEnd(Animator animation) {
//								Utils.SendSomeThing.sendPlay(mMainActivity, 6, "previous_slide");
//								if (finalBefItem != null) {
//									Bitmap bitmap = Utils.Audio.getCoverBitmapFull(mMainActivity, finalBefItem.getAlbumId());
//									setIconLightOrDark(bitmap);
//									mHandler.postDelayed(() -> {
//										if (bitmap != null && !bitmap.isRecycled()) {
//											bitmap.recycle();
//										}
//									}, 5000);
//								}
//								MV.setTranslationX(0 - mMusicAlbumImage.getWidth());
//								ImageView temp = MV;
//								MV = LV;
//								LV = temp;
//
//								RV.setTag(key, 2);
//								MV.setTag(key, 1);
//								LV.setTag(key, 0);
//							}
//
//							@Override
//							public void onAnimationCancel(Animator animation) {
//
//							}
//
//							@Override
//							public void onAnimationRepeat(Animator animation) {
//
//							}
//						});
//
//					} else {
//						animatorMain.setFloatValues(MV.getX(), 0);
//
//						animatorMain.addListener(new Animator.AnimatorListener() {
//							@Override
//							public void onAnimationStart(Animator animation) {
//
//							}
//
//							@Override
//							public void onAnimationEnd(Animator animation) {
//								MV.setTranslationX(0);
//								RV.setTranslationX(mMusicAlbumImage.getWidth() * 2);
//								LV.setTranslationX(0 - mMusicAlbumImage.getWidth());
//
//								RV.setTag(key, 2);
//								MV.setTag(key, 1);
//								LV.setTag(key, 0);
//							}
//
//							@Override
//							public void onAnimationCancel(Animator animation) {
//
//							}
//
//							@Override
//							public void onAnimationRepeat(Animator animation) {
//
//							}
//						});
//					}
//
//					animatorMain.addUpdateListener(animation -> {
//						MV.setTranslationX((Float) animation.getAnimatedValue());
//						RV.setTranslationX((Float) animation.getAnimatedValue() + mMusicAlbumImage.getWidth());
//						LV.setTranslationX(0 - mMusicAlbumImage.getWidth() + (Float) animation.getAnimatedValue());
//					});
//					animatorMain.start();
//
//					return true;
//				default:
//			}
//			return true;
//		};

		//todo
//		mMusicAlbumImage.setOnTouchListener(touchListener);
//		mMusicAlbumImageOth2.setOnTouchListener(touchListener);
//		mMusicAlbumImageOth3.setOnTouchListener(touchListener);

		setClickListener();

		mLinearLayoutManager = new LinearLayoutManager(mMainActivity);
		mRecyclerView.setLayoutManager(mLinearLayoutManager);
		mMyWaitListAdapter = new MyWaitListAdapter(mMainActivity, Data.sPlayOrderList);
		mRecyclerView.setAdapter(mMyWaitListAdapter);

		mSlidingUpPanelLayout.post(() -> {
			View v = view.findViewById(R.id.frame_ctrl);
			// FIXME: 2019/6/15 在系统显示状态为默认时会造成覆盖现象
			mSlidingUpPanelLayout.setPanelHeight(view.getHeight() - v.getTop() - v.getHeight());
		});

	}

	private MyWaitListAdapter mMyWaitListAdapter;

	/**
	 * menu
	 */
	private AppCompatImageView mCurrentInfoBodyMenuButton;
	private float mLastX = 0;
	//	private float mLastY = 0;
	private float moveX = 0;
	//	private float moveY = 0;
	private boolean hideToolbar = false;
	private boolean snackNotice = false;
	private float defXScale;
	private float defYScale;

	/**
	 * TargetColor
	 */
	@ColorInt
	private int targetColor;
	private List<Disposable> disposables = new ArrayList<>();

	private void setUpImage(@Nullable final Bitmap bitmap, @NonNull final ImageView bgUp
			, @NonNull final ImageView bgDown, final TextView nextText) {

		// set main album image
		GlideApp.with(mMainActivity)
				.load(bitmap)
				.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
				.centerCrop()
				.diskCacheStrategy(DiskCacheStrategy.NONE)
				.into(MV);

		bgDown.setVisibility(View.VISIBLE);

		@ColorInt final int defColor = ContextCompat.getColor(mMainActivity, R.color.colorPrimary);

		if (bitmap != null && !bitmap.isRecycled()) {
			Palette.from(bitmap).generate(palette -> {
				if (palette != null) {
					targetColor = palette.getVibrantColor(defColor);
					nextText.setTextColor(targetColor);
				}
			});
		} else {
			nextText.setTextColor(defColor);
		}

		if (MusicDetailFragment.BackgroundStyle.DETAIL_BACKGROUND.equals(MusicDetailFragment.BackgroundStyle
				.STYLE_BACKGROUND_BLUR)) {

			bgUp.post(() -> GlideApp.with(this)
					.load(bitmap)
					.dontAnimate()
					.apply(bitmapTransform(Data.sBlurTransformation))
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.override(SIZE_OF_BLUR, SIZE_OF_BLUR)
					.into(bgUp));
		} else {
			if (bitmap != null) {
				bgUp.setBackgroundColor(targetColor);
			} else {
				bgUp.setBackgroundColor(defColor);
			}
		}

		bgUp.post(() -> {
			final Animator animator = ViewAnimationUtils.createCircularReveal(
					bgUp, bgUp.getWidth() / 2, m_Position, 0, (float) Math.hypot(bgUp.getWidth()
							, bgUp.getHeight()));

			animator.setInterpolator(new AccelerateInterpolator());
			animator.setDuration(500);
			animator.addListener(new Animator.AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animation) {

				}

				@Override
				public void onAnimationEnd(Animator animation) {
					GlideApp.with(MusicDetailFragment.this).clear(bgDown);
					if (MusicDetailFragment.BackgroundStyle.DETAIL_BACKGROUND.equals(MusicDetailFragment.BackgroundStyle.STYLE_BACKGROUND_BLUR)) {
						GlideApp.with(MusicDetailFragment.this)
								.load(bitmap)
								.dontAnimate()
								.apply(bitmapTransform(Data.sBlurTransformation))
								.diskCacheStrategy(DiskCacheStrategy.NONE)
								.override(SIZE_OF_BLUR, SIZE_OF_BLUR)
								.into(bgDown);
					} else {
						if (bitmap != null) {
							bgDown.setBackgroundColor(targetColor);
						} else {
							bgDown.setBackgroundColor(defColor);
						}
					}
					bgDown.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationCancel(Animator animation) {
				}

				@Override
				public void onAnimationRepeat(Animator animation) {

				}
			});
			animator.start();
		});

	}

	/**
	 * preLoad data
	 */
	private void preLoad() {
		final MusicItem item = Data.sCurrentMusicItem;
		if (item != null && item.getMusicID() != -1) {
			final Bitmap cover = Utils.Audio.getCoverBitmapFull(getContext(), item.getAlbumId());
			setCurrentData(item, cover, true);

			try {
				if (Data.sMusicBinder != null && Data.sMusicBinder.isPlayingMusic()) {
					mPlayPauseDrawable.setPause(true);
					mPlayPauseDrawable2InfoBar.setPause(true);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}

		} else {
			Log.d(TAG, "preLoad: item is null");
		}
	}

	public static MusicDetailFragment newInstance() {
		return new MusicDetailFragment();
	}

	@Override
	public void onAttach(@NotNull Context context) {
		super.onAttach(context);
		mMainActivity = (MainActivity) context;

		mHandlerThread = new HandlerThread("Handler Thread in MusicDetailActivity");
		mHandlerThread.start();
		mHandler = new MusicDetailFragment.NotLeakHandler(mMainActivity, this, mHandlerThread.getLooper());
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container
			, @Nullable Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_music_detail, container, false);

		findView(view);

		initView(view);

		preLoad();

		//init seekBar
		mHandler.sendEmptyMessage(NotLeakHandler.INIT_SEEK_BAR);
		//let seekBar loop update
		mHandler.sendEmptyMessage(NotLeakHandler.SEEK_BAR_UPDATE);
		//scroll to the position{@Values.CurrentData.CURRENT_MUSIC_INDEX}
		mHandler.sendEmptyMessage(NotLeakHandler.RECYCLER_SCROLL);

		return view;
	}

	private void setUpToolbar() {
		/*------------------toolbar--------------------*/
		mToolbar.inflateMenu(R.menu.menu_toolbar_in_detail);

		mToolbar.setOnMenuItemClickListener(menuItem -> {
			switch (menuItem.getItemId()) {
				case R.id.menu_toolbar_fast_play: {
					ReceiverOnMusicPlay.startService(mMainActivity, MusicService.ServiceActions.ACTION_FAST_SHUFFLE);
				}
				break;

				case R.id.menu_toolbar_love: {
					MusicUtil.toggleFavorite(mMainActivity, ReceiverOnMusicPlay.getCurrentItem());
					updateFav(ReceiverOnMusicPlay.getCurrentItem());
//					Toast.makeText(mMainActivity, getString(R.string.done), Toast.LENGTH_SHORT).show();
				}
				break;

				case R.id.menu_toolbar_eq: {
					final MusicItem item = ReceiverOnMusicPlay.getCurrentItem();
					if (item != null) {
						Utils.Audio.openEqualizer(mMainActivity, item.getAlbumId());
					}
				}
				break;

				case R.id.menu_toolbar_timer: {
					new TimePickerDialog(mMainActivity, (view, hourOfDay, minute) -> {
						final long time = hourOfDay * 360000 + minute * 60000;
						if (time == 0) {
							Toast.makeText(mMainActivity, "Why not pause music by button?"
									, Toast.LENGTH_SHORT).show();
							return;
						}
						final Intent intent = new Intent(mMainActivity, MusicService.class);
						intent.setAction(MusicService.ServiceActions.ACTION_SLEEP);
						intent.putExtra("time", time);
						mMainActivity.startService(intent);
					}, 0, 0, true).show();

				}
				break;

				case R.id.menu_toolbar_debug: {
					// none
				}
				break;

				case R.id.menu_toolbar_trash_can: {
					MusicUtil.dropToTrash(mMainActivity, Data.sCurrentMusicItem);
				}
				break;
				default:
			}
			return false;
		});

		mToolbar.setNavigationOnClickListener(v -> MainActivity.getHandler()
				.sendEmptyMessage(MainActivity.NotLeakHandler.DOWN));

		mToolbar.setOverlayColor(Color.WHITE);
	}

	@Nullable
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

	private void setCurrentData(@NonNull final MusicItem item, @Nullable final Bitmap cover, boolean... forceLoadImage) {
		mMainActivity.runOnUiThread(() -> {
			setSlideInfoBar(item.getMusicName(), item.getMusicAlbum(), cover);
			mCurrentMusicNameText.setText(item.getMusicName());
			mCurrentAlbumNameText.setText(item.getMusicAlbum());

			// 根据专辑图更改状态栏明暗
//			if (cover != null && !cover.isRecycled()) {
//				Utils.Ui.setStatusBarTextColor(mMainActivity, new Palette.Builder(cover)
//						.generate().getLightVibrantColor(Utils.Ui.getPrimaryColor(mMainActivity)));
//			}

			// update album id
			if (item.getAlbumId() != mLastAlbumId || forceLoadImage.length > 0) {
				setUpImage(cover, mBGup, mBGdown, mNextWillText);
				mLastAlbumId = item.getAlbumId();
			}

			updateFav(item);

		});
	}

	/**
	 * init & start animations
	 */
	public final void initAnimation() {
		setIconLightOrDark(Data.getCurrentCover());
		/*
		 * init view animation
		 * */
		//default type is common, but the random button alpha is 1f(it means this button is on), so set animate
		if (Values.TYPE_RANDOM.equals(PreferenceUtil.getDefault(mMainActivity).getString(Values.SharedPrefsTag.ORDER_TYPE, Values.TYPE_COMMON))) {
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
		//noinspection ConstantConditions
		switch (PreferenceUtil.getDefault(mMainActivity).getString(Values.SharedPrefsTag.PLAY_TYPE, MusicService.PlayType.REPEAT_NONE)) {
			case MusicService.PlayType.REPEAT_NONE: {
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
			case MusicService.PlayType.REPEAT_LIST: {
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
			case MusicService.PlayType.REPEAT_ONE: {
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
	private void setSlideInfoBar(final String songName, final String albumName, @Nullable final Bitmap cover) {

		mMainActivity.runOnUiThread(() -> {
			setIconLightOrDark(cover);

			mInfoBarSongText.setText(songName);
			mInfoBarAlbumText.setText(albumName);

			if (cover != null) {
				GlideApp.with(this).load(cover)
						.transition(DrawableTransitionOptions.withCrossFade())
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						.into(mInfoBarSongImage);

				GlideApp.with(this).load(cover)
						.transition(DrawableTransitionOptions.withCrossFade())
						.centerCrop()
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						.into(mMainActivity.getNavHeaderImageView());

				//load blur...
				GlideApp.with(this).load(cover)
						.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
						.apply(bitmapTransform(new BlurTransformation(5, 20)))
						.override(50, 50)
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						.into(mInfoBarBackgroundImage);
			}
		});
	}

	/**
	 * find view by id
	 *
	 * @see android.app.Activity#findViewById(int)
	 */
	private void findView(final View view) {
		mMusicAlbumImage = view.findViewById(R.id.activity_music_detail_album_image);
		mBGup = view.findViewById(R.id.activity_music_detail_primary_background_up);
		mBGdown = view.findViewById(R.id.activity_music_detail_primary_background_down);
		mSeekBar = view.findViewById(R.id.seekBar);
		mInfoBarInfoSeek = view.findViewById(R.id.info_bar_seek);
		mNextButton = view.findViewById(R.id.next_button);
		mPreviousButton = view.findViewById(R.id.previous_button);
		mPlayButton = view.findViewById(R.id.play_button);
		mRecyclerView = view.findViewById(R.id.recycler_view);
		mCurrentInfoBody = view.findViewById(R.id.item_layout);
		mCurrentAlbumNameText = view.findViewById(R.id.item_text_one);
		mCurrentMusicNameText = view.findViewById(R.id.item_main_text);
		mCurrentInfoBodyMenuButton = view.findViewById(R.id.item_menu);
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
		mInfoBarAlbumText = view.findViewById(R.id.info_bar_album);
		mNowPlayingBody = view.findViewById(R.id.current_info);
		mInfoBarFavButton = view.findViewById(R.id.info_bar_fav_img);
		mInfoBarPlayButton = view.findViewById(R.id.info_bar_play_pause_img);
		mInfoBarBackgroundImage = view.findViewById(R.id.info_bar_background);
		mInfoBarSongText = view.findViewById(R.id.info_bar_music_name);
		mInfoBarSongImage = view.findViewById(R.id.info_bar_clover);
		mSlideUpGroup = view.findViewById(R.id.detail_body);
	}

	@Override
	public void reloadData() {
		//needn't
	}

	private void showPopupMenu(Context context, View view) {
		if (Data.sCurrentMusicItem == null || Data.sCurrentMusicItem.getMusicID() == -1) return;

		/*---------------------- Menu -----------------------*/
		PopupMenu mPopupMenu = new PopupMenu(context, view);

		mPopupMenu.setGravity(GravityCompat.END);

		final Menu menu = mPopupMenu.getMenu();
		menu.clear();

		menu.add(Menu.NONE, Menu.FIRST + 1, 0, getString(R.string.show_album));
		menu.add(Menu.NONE, Menu.FIRST + 2, 0, getString(R.string.show_detail));
		menu.add(Menu.NONE, Menu.FIRST + 3, 0, getString(R.string.drop_to_trash_can));
		menu.add(Menu.NONE, Menu.FIRST + 4, 0, getString(R.string.add_to_playlist));

		mPopupMenu.setOnMenuItemClickListener(clickListener);

		mPopupMenu.show();
	}

	private void setClickListener() {

		mCurrentInfoBody.setOnClickListener(v -> {

			if (mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
				mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
			} else {
				mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
			}

			mHandler.sendEmptyMessage(NotLeakHandler.RECYCLER_SCROLL);
		});

		mCurrentInfoBody.setOnLongClickListener(v -> {
//			final MusicItem item = ReceiverOnMusicPlay.getCurrentItem();
//			if (item != null) {
//				MusicUtil.dropToTrash(mMainActivity, item);
//			}
			showPopupMenu(mMainActivity, mCurrentInfoBody);

			return true;
		});

		mCurrentInfoBodyMenuButton.setOnClickListener(v -> showPopupMenu(mMainActivity, mCurrentInfoBodyMenuButton));

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
				ReceiverOnMusicPlay.startService(mMainActivity, MusicService.ServiceActions.ACTION_PLAY);
			}
		});

		mSlidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
			@Override
			public void onPanelSlide(View panel, float slideOffset) {
				mSlideUpGroup.setTranslationY(0 - slideOffset * 120);
				if (slideOffset == 0) {
					mMainActivity.getMainBinding().slidingLayout.setTouchEnabled(true);
					mMainActivity.getMainBinding().slidingLayout.setEnabled(true);
				} else {
					mMainActivity.getMainBinding().slidingLayout.setTouchEnabled(false);
					mMainActivity.getMainBinding().slidingLayout.setEnabled(false);
				}
			}

			@Override
			public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
				if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
					mMainActivity.getMainBinding().slidingLayout.setTouchEnabled(true);
					mMainActivity.getMainBinding().slidingLayout.setEnabled(true);
					mHandler.sendEmptyMessage(NotLeakHandler.RECYCLER_SCROLL);
				} else {
					mMainActivity.getMainBinding().slidingLayout.setTouchEnabled(false);
					mMainActivity.getMainBinding().slidingLayout.setEnabled(false);
				}
			}
		});

		mRepeatButton.setOnClickListener(v -> {

			/*
			 * COMMON = 0f
			 * REPEAT = 1f
			 * REPEAT_ONE = 1f(another pic)
			 * */
			final ValueAnimator animator = new ValueAnimator();
			animator.setDuration(300);
			mRepeatButton.clearAnimation();
			//noinspection ConstantConditions
			switch (PreferenceUtil.getDefault(mMainActivity).getString(Values.SharedPrefsTag.PLAY_TYPE, MusicService.PlayType.REPEAT_NONE)) {
				case MusicService.PlayType.REPEAT_NONE: {
					PreferenceUtil.getDefault(mMainActivity).edit().putString(Values.SharedPrefsTag.PLAY_TYPE, MusicService.PlayType.REPEAT_LIST).apply();
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
				}
				break;
				case MusicService.PlayType.REPEAT_LIST: {
					mRepeatButton.setImageResource(R.drawable.ic_repeat_one_white_24dp);
					PreferenceUtil.getDefault(mMainActivity).edit().putString(Values.SharedPrefsTag.PLAY_TYPE, MusicService.PlayType.REPEAT_ONE).apply();
				}
				break;
				case MusicService.PlayType.REPEAT_ONE: {
					PreferenceUtil.getDefault(mMainActivity).edit().putString(Values.SharedPrefsTag.PLAY_TYPE, MusicService.PlayType.REPEAT_NONE).apply();
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
				}
				break;
				default:
			}

			Log.d(TAG, "setClickListener: current repeat mode: " + PreferenceUtil.getDefault(mMainActivity)
					.getString(Values.SharedPrefsTag.PLAY_TYPE, MusicService.PlayType.REPEAT_NONE));
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
			final SharedPreferences.Editor editor = PreferenceUtil.getDefault(mMainActivity).edit();
			if (Values.TYPE_RANDOM.equals(PreferenceUtil.getDefault(mMainActivity).getString(Values.SharedPrefsTag.ORDER_TYPE, Values.TYPE_COMMON))) {
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

				Data.shuffleOrderListSync(mMainActivity, true);

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

				Data.shuffleOrderListSync(mMainActivity, false);
				mMyWaitListAdapter.notifyDataSetChanged();
			}
		});

		View.OnClickListener listener = v -> {
			if (hideToolbar) {
				showToolbar();
			} else {
				hideToolbar();
			}
		};

		mMusicAlbumImage.setOnClickListener(listener);
		mMusicAlbumImageOth2.setOnClickListener(listener);
		mMusicAlbumImageOth3.setOnClickListener(listener);


		//just intentPause or play
		mPlayButton.setOnClickListener(v -> playPause());

		mNextButton.setOnClickListener(v -> MusicService.MusicControl.intentNext(mMainActivity));

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
			Intent next = new Intent(MusicService.ServiceActions.ACTION_PN);
			next.putExtra("pnType", MusicService.ServiceActions.ACTION_PN_PREVIOUS);
			ReceiverOnMusicPlay.startService(mMainActivity, next);
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

		mInfoBarFavButton.setOnClickListener(v -> {
			MusicUtil.toggleFavorite(mMainActivity, ReceiverOnMusicPlay.getCurrentItem());
			updateFav(ReceiverOnMusicPlay.getCurrentItem());
			Toast.makeText(mMainActivity, getString(R.string.done), Toast.LENGTH_SHORT).show();
		});

		mInfoBarPlayButton.setOnClickListener(v -> playPause());

		mNowPlayingBody.setOnClickListener(v -> MainActivity.getHandler().sendEmptyMessage(MainActivity.NotLeakHandler.UP));

		mNowPlayingBody.setOnLongClickListener(v -> {
			showPopupMenu(mMainActivity, mNowPlayingBody);
			return true;
		});
	}

	/**
	 * 播放按钮逻辑
	 * <p>
	 * {@link #mInfoBarPlayButton}
	 * {@link #mPlayButton}
	 */
	private void playPause() {
		// 判断是否播放过, 如没有默认随机播放
		if (Data.sCurrentMusicItem.getMusicID() != -1) {
			if (ReceiverOnMusicPlay.isPlayingMusic()) {
				ReceiverOnMusicPlay.startService(mMainActivity, MusicService.ServiceActions.ACTION_PAUSE);
			} else {
				ReceiverOnMusicPlay.startService(mMainActivity, MusicService.ServiceActions.ACTION_PLAY);
			}
		} else {
			ReceiverOnMusicPlay.startService(mMainActivity, MusicService.ServiceActions.ACTION_FAST_SHUFFLE);
		}
	}

	/**
	 * hide or show toolbar
	 */
	private void showToolbar() {
		hideToolbar = false;
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

	private void setCurrentInfoWithoutMainImage(@NonNull final String name, @NonNull final String albumName, final Bitmap cover) {
		mMainActivity.runOnUiThread(() -> {
			mCurrentMusicNameText.setText(name);
			mCurrentAlbumNameText.setText(albumName);
			setUpImage(cover, mBGup, mBGdown, mNextWillText);
			setSlideInfoBar(name, albumName, cover);
		});
	}

	private void hideToolbar() {
		hideToolbar = true;
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

	/**
	 * update Favourite music icon
	 */
	private void updateFav(@Nullable MusicItem item) {
		if (item != null) {
			@DrawableRes int id = MusicUtil.isFavorite(mMainActivity, item) ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp;
			mToolbar.getMenu().findItem(R.id.menu_toolbar_love).setIcon(id);
			mInfoBarFavButton.setImageResource(id);
		}
	}

	public final SlidingUpPanelLayout getSlidingUpPanelLayout() {
		return mSlidingUpPanelLayout;
	}

	/**
	 * setIcon White or Black (by bitmap light)
	 *
	 * @param bitmap backgroundImage
	 */
	private void setIconLightOrDark(@Nullable final Bitmap bitmap) {
		if (bitmap != null) {

			final Disposable disposable = Observable.create((ObservableOnSubscribe<Integer>) observableEmitter ->
					observableEmitter.onNext(Utils.Ui.getBright(bitmap))).subscribeOn(Schedulers.newThread())
					.observeOn(AndroidSchedulers.mainThread()).subscribe(i -> {

						if (i > (255 / 2)) {
							@ColorInt final int target = ContextCompat.getColor(mMainActivity, R.color.notVeryBlack);

							final ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), mNextWillText.getCurrentTextColor(), target);
							animator.setDuration(0);
							animator.setStartDelay(300);
							animator.addUpdateListener(animation -> {
								@ColorInt int val = (int) animation.getAnimatedValue();
								mInfoBarSongText.setTextColor(val);
								mInfoBarAlbumText.setTextColor(val);
								mInfoBarFavButton.setColorFilter(val);

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

							final ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), mNextWillText.getCurrentTextColor(), target);
							animator.setDuration(0);
							animator.addUpdateListener(animation -> {
								@ColorInt int val = (int) animation.getAnimatedValue();
								mInfoBarSongText.setTextColor(val);
								mInfoBarAlbumText.setTextColor(val);
								mInfoBarFavButton.setColorFilter(val);

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
					});

			disposables.add(disposable);
		}
	}

	private void clearDisposable() {
		for (final Disposable disposable : disposables) {
			if (disposable != null && !disposable.isDisposed()) {
				disposable.dispose();
			}
		}
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause: ");
		clearDisposable();
		super.onPause();
	}

	@Override
	public void onDestroyView() {
		mHandlerThread.quitSafely();
		clearDisposable();
		super.onDestroyView();
	}

	@Override
	public FragmentType getFragmentType() {
		return FragmentType.MUSIC_DETAIL_FRAGMENT;
	}

	/**
	 * 对于 {@link top.geek_studio.chenlongcould.musicplayer.fragment.MusicDetailFragment} 中的背景进行样式设定
	 */
	public static class BackgroundStyle {
		public static String STYLE_BACKGROUND_BLUR = "BLUR";
		public static String STYLE_BACKGROUND_AUTO_COLOR = "AUTO_COLOR";

		/**
		 * background style model
		 */
		public static String DETAIL_BACKGROUND = STYLE_BACKGROUND_BLUR;
	}

	@SuppressWarnings("WeakerAccess")
	public static final class NotLeakHandler extends Handler {

		/**
		 * For {@link #mHandler}
		 */
		public static final int INIT_SEEK_BAR = 54;        //just for this
		public static final int SET_SEEK_BAR = 59;
		public static final int RECYCLER_SCROLL = 55001;
		public static final int SET_BUTTON_PAUSE = 57;
		public static final int SET_BUTTON_PLAY = 58;
		public static final int setCurrentInfoWithoutMainImage = 60;
		public static final int TOGGLE_FAV = 61;
		private static final int SEEK_BAR_UPDATE = 53;        //just for this

		public static final int SET_CURRENT_DATA = 55;

		private WeakReference<MainActivity> mWeakReference;

		private WeakReference<MusicDetailFragment> mFragmentWeakReference;

		NotLeakHandler(@NonNull MainActivity activity, MusicDetailFragment fragment, @Nullable Looper looper) {
			super(looper == null ? Looper.myLooper() : looper);
			mWeakReference = new WeakReference<>(activity);
			mFragmentWeakReference = new WeakReference<>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
				case INIT_SEEK_BAR: {
					mWeakReference.get().runOnUiThread(() -> {
						if (!MusicUtil.availableCurrentItem()) {
							return;
						}

						int duration = (int) Data.sCurrentMusicItem.getDuration();
						if (duration == 0) return;

						Log.d(TAG, "handleMessage: INIT_SEEK_BAR: duration: " + duration);

						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
							mFragmentWeakReference.get().mSeekBar.setProgress(0, true);
						} else {
							mFragmentWeakReference.get().mSeekBar.setProgress(0);
						}

						mFragmentWeakReference.get().mInfoBarInfoSeek.getLayoutParams().width = 0;
						mFragmentWeakReference.get().mInfoBarInfoSeek.setLayoutParams(mFragmentWeakReference.get()
								.mInfoBarInfoSeek.getLayoutParams());
						mFragmentWeakReference.get().mInfoBarInfoSeek.requestLayout();

						mFragmentWeakReference.get().mRightTime.setText(String.valueOf(Data.S_SIMPLE_DATE_FORMAT
								.format(new Date(duration))));

						mFragmentWeakReference.get().mSeekBar.setMax(duration);
					});
				}
				break;

				case SEEK_BAR_UPDATE: {
					mWeakReference.get().runOnUiThread(() -> {
						//点击body 或 music 正在播放 才可以进行seekBar更新
						if (Data.sMusicBinder == null || !MusicUtil.availableCurrentItem()
								|| !ReceiverOnMusicPlay.isPlayingMusic()) {
							return;
						}

						int duration = (int) Data.sCurrentMusicItem.getDuration();

						// sometimes it may be zero (0)
						if (duration == 0) duration = 1;

						int position = ReceiverOnMusicPlay.getCurrentPosition();

						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
							mFragmentWeakReference.get().mSeekBar.setProgress(position, true);
						} else {
							mFragmentWeakReference.get().mSeekBar.setProgress(position);
						}

						mFragmentWeakReference.get().mInfoBarInfoSeek.getLayoutParams().width
								= mFragmentWeakReference.get().mCurrentInfoBody.getWidth() * position / duration;
						mFragmentWeakReference.get().mInfoBarInfoSeek.setLayoutParams(mFragmentWeakReference
								.get().mInfoBarInfoSeek.getLayoutParams());
						mFragmentWeakReference.get().mInfoBarInfoSeek.requestLayout();

						mFragmentWeakReference.get().mLeftTime.setText(String.valueOf(Data.S_SIMPLE_DATE_FORMAT
								.format(new Date(position))));

						//播放模式不为循环单曲时，跳出提示
						if (!MusicService.PlayType.REPEAT_ONE.equals(PreferenceUtil.getDefault(mWeakReference.get())
								.getString(Values.SharedPrefsTag.PLAY_TYPE, MusicService.PlayType.REPEAT_NONE))) {

							if (ReceiverOnMusicPlay.getCurrentPosition() / 1000 == ReceiverOnMusicPlay
									.getDuration() / 1000 - 5 && !mFragmentWeakReference.get().snackNotice) {

								mFragmentWeakReference.get().snackNotice = true;

								final GkSnackbar gkSnackbar = new GkSnackbar(mFragmentWeakReference.get()
										.mSlidingUpPanelLayout, mWeakReference.get()
										.getString(R.string.next_will_play_x,
												Data.sPlayOrderList.get(Data.getCurrentIndex() +
														1 != Data.sPlayOrderList.size() ? Data.getCurrentIndex() + 1 : 0)
														.getMusicName()
										)
										, Snackbar.LENGTH_LONG);

								gkSnackbar.setAction(mWeakReference.get().getString(R.string.skip), v -> {
									//点击右侧的按钮之后的操作
									try {
										Data.sMusicBinder.setCurrentIndex(Data.getCurrentIndex() + 1);
									} catch (RemoteException e) {
										e.printStackTrace();
									}
									MusicService.MusicControl.intentNext(mWeakReference.get());
								});
								gkSnackbar.addCallback(new Snackbar.Callback() {
									@Override
									public void onDismissed(Snackbar transientBottomBar, int event) {
										mFragmentWeakReference.get().snackNotice = false;
									}
								});
								gkSnackbar.setBackgroundColor(Utils.Ui.getPrimaryColor(mWeakReference.get()))
										.setBodyViewAlpha(0.8f).setActionTextColor(Color.BLACK);
								gkSnackbar.show();

							}
						}
					});

					//循环更新 1s 一次
					mHandler.sendEmptyMessageDelayed(SEEK_BAR_UPDATE, 1000);
				}
				break;

				case RECYCLER_SCROLL: {
					mWeakReference.get().runOnUiThread(() -> mFragmentWeakReference.get().mLinearLayoutManager
							.scrollToPositionWithOffset(Data.getCurrentIndex() == Data.sMusicItems.size()
									? Data.getCurrentIndex()
									: Data.getCurrentIndex() + 1, 0));
				}
				break;

				case SET_BUTTON_PLAY: {
					mWeakReference.get().runOnUiThread(() -> {
						mFragmentWeakReference.get().mPlayPauseDrawable.setPause(true);
						mFragmentWeakReference.get().mPlayPauseDrawable2InfoBar.setPause(true);
					});
				}
				break;

				case SET_BUTTON_PAUSE: {
					mWeakReference.get().runOnUiThread(() -> {
						mFragmentWeakReference.get().mPlayPauseDrawable.setPlay(true);
						mFragmentWeakReference.get().mPlayPauseDrawable2InfoBar.setPlay(true);
					});
				}
				break;

				case SET_SEEK_BAR: {
					int value = msg.arg1;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
						mFragmentWeakReference.get().mSeekBar.setProgress(value, true);
					} else {
						mFragmentWeakReference.get().mSeekBar.setProgress(value);
					}
				}
				break;

				case setCurrentInfoWithoutMainImage: {
					final Bundle data = msg.getData();
					mFragmentWeakReference.get().setCurrentInfoWithoutMainImage(Data.sCurrentMusicItem.getMusicName()
							, Data.sCurrentMusicItem.getMusicAlbum(), Data.getCurrentCover());
					data.clear();
				}
				break;

				case SET_CURRENT_DATA: {
					mFragmentWeakReference.get().setCurrentData(Data.sCurrentMusicItem, Data.getCurrentCover());
				}
				break;

				case TOGGLE_FAV: {
					mWeakReference.get().runOnUiThread(() -> mFragmentWeakReference.get().updateFav(Data.sCurrentMusicItem));
				}
				break;

				default: {

				}
			}
		}
	}
}
