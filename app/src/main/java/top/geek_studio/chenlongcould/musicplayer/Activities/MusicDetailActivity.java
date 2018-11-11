package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.io.IOException;
import java.lang.ref.WeakReference;

import top.geek_studio.chenlongcould.musicplayer.Adapters.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Fragments.MusicListFragment;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public final class MusicDetailActivity extends Activity {

    private static final String TAG = "MusicDetailActivity";

    private boolean READY = true;

    private boolean HAS_BIG = false;

    private int DEF_TOP = -1;

    private ImageView mMusicAlbumImage;

    private ImageView mPrimaryBackground;

    private SeekBar mSeekBar;

    private NotLeakHandler mHandler;

    private HandlerThread mHandlerThread;

    private ImageButton mPlayButton;

    private ImageButton mNextButton;

    private ImageButton mPreviousButton;

    private RecyclerView mRecyclerView;

    private MainActivity mMainActivity;

    private MusicListFragment mMusicListFragment;

    private ConstraintLayout mInfoBody;

    private CardView mCardView;

    private TextView mIndexTextView;

    private TextView mAlbumNameText;

    private TextView mMusicNameText;

    private ImageButton mRandomButton;

    private ImageButton mRepeatButton;

    /**
     * menu
     */
    private ImageButton mMenuButton;

    private PopupMenu mPopupMenu;

    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_detail);

        Data.sActivities.add(this);

        mMainActivity = (MainActivity) Data.sActivities.get(0);

        mHandlerThread = new HandlerThread("Handler Thread in MusicDetailActivity");
        mHandlerThread.start();
        mHandler = new NotLeakHandler(this, mHandlerThread.getLooper());

        initView();

        if (Values.HAS_PLAYED) {
            initData();
        }
    }

    private void initData() {
        GlideApp.with(this)
                .load(Data.sCurrentMusicBitmap)
                .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                .into(mMusicAlbumImage);

        setInfoBar(Data.sCurrentMusicName, Data.sCurrentMusicAlbum);

        if (Values.MUSIC_PLAYING) {
            mPlayButton.setImageResource(R.drawable.ic_pause_black_24dp);
        }

        Utils.Ui.setBlurEffect(this, Data.sCurrentMusicBitmap, mPrimaryBackground);
    }

    private void initAnimation() {
        /*
         * init view animation
         * */
        //default type is common, but the randomButton alpha is 1f(it menes this button is on), so set animate
        AlphaAnimation temp = new AlphaAnimation(0, 0.3f);
        temp.setDuration(500);
        temp.setFillAfter(true);
        mRandomButton.clearAnimation();
        mRandomButton.startAnimation(temp);
        mRepeatButton.clearAnimation();
        mRepeatButton.startAnimation(temp);

        ScaleAnimation mPlayButtonScaleAnimation = new ScaleAnimation(0, mPlayButton.getScaleX(), 0, mPlayButton.getScaleY(),
                Animation.RELATIVE_TO_SELF, mPlayButton.getScaleX() / 2, Animation.RELATIVE_TO_SELF, mPlayButton.getScaleX() / 2);
        mPlayButtonScaleAnimation.setDuration(500);
        mPlayButtonScaleAnimation.setFillAfter(true);
        mPlayButton.clearAnimation();
        mPlayButton.setAnimation(mPlayButtonScaleAnimation);
        mPlayButtonScaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mPlayButton.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        TranslateAnimation mCardViewTranslateAnimation = new TranslateAnimation(mCardView.getTranslationX(), mCardView.getTranslationX(), 500, mCardView.getTranslationY());
        mCardViewTranslateAnimation.setDuration(500);
        mCardViewTranslateAnimation.setFillAfter(true);
        mCardView.clearAnimation();
        mCardView.startAnimation(mCardViewTranslateAnimation);

        TranslateAnimation mPreviousButtonTranslateAnimation = new TranslateAnimation(150, mPreviousButton.getTranslationX(), mPreviousButton.getTranslationY(), mPreviousButton.getTranslationY());
        TranslateAnimation mNextButtonTranslateAnimation = new TranslateAnimation(-150, mNextButton.getTranslationX(), mNextButton.getTranslationY(), mNextButton.getTranslationY());
        mPreviousButtonTranslateAnimation.setDuration(500);
        mPreviousButtonTranslateAnimation.setFillAfter(true);
        mNextButtonTranslateAnimation.setDuration(500);
        mNextButtonTranslateAnimation.setFillAfter(true);
        mPreviousButton.clearAnimation();
        mNextButton.clearAnimation();
        mNextButton.startAnimation(mNextButtonTranslateAnimation);
        mPreviousButton.startAnimation(mPreviousButtonTranslateAnimation);

    }

    private void initView() {
        mMusicAlbumImage = findViewById(R.id.activity_music_detail_album_image);
        mPrimaryBackground = findViewById(R.id.activity_music_detail_primary_background);
        mSeekBar = findViewById(R.id.seekBar);
        mNextButton = findViewById(R.id.activity_music_detail_image_next_button);
        mPreviousButton = findViewById(R.id.activity_music_detail_image_previous_button);
        mPlayButton = findViewById(R.id.activity_music_detail_image_play_button);
        mRecyclerView = findViewById(R.id.recycler_view);
        mInfoBody = findViewById(R.id.item_layout);
        mCardView = findViewById(R.id.activity_music_detail_card_view);
        mIndexTextView = findViewById(R.id.item_index_text);
        mAlbumNameText = findViewById(R.id.item_text_one);
        mMusicNameText = findViewById(R.id.item_main_text);
        mMenuButton = findViewById(R.id.item_menu);
        mRandomButton = findViewById(R.id.activity_music_detail_image_random_button);
        mRepeatButton = findViewById(R.id.activity_music_detail_image_repeat_button);

        initAnimation();

        //init view data
        mHandler.sendEmptyMessage(Values.INIT_SEEK_BAR);
        if (getIntent().getStringExtra("intent_args").equals("by_clicked_body")) {
            mHandler.sendEmptyMessage(Values.SEEK_BAR_UPDATE);
        }

        mRandomButton.setOnClickListener(v -> {
            if (Values.CURRENT_PLAY_TYPE.equals(Values.TYPE_RANDOM)) {
                Values.CURRENT_PLAY_TYPE = Values.TYPE_COMMON;
                AlphaAnimation alphaAnimation = new AlphaAnimation(mRandomButton.getAlpha(), 0.3f);
                alphaAnimation.setDuration(300);
                alphaAnimation.setFillAfter(true);
                mRandomButton.clearAnimation();
                mRandomButton.startAnimation(alphaAnimation);
            } else {
                Values.CURRENT_PLAY_TYPE = Values.TYPE_RANDOM;
                AlphaAnimation alphaAnimation = new AlphaAnimation(mRandomButton.getAlpha(), 1f);
                alphaAnimation.setDuration(300);
                alphaAnimation.setFillAfter(true);
                mRandomButton.clearAnimation();
                mRandomButton.startAnimation(alphaAnimation);
            }
        });

        //Menu...
        mPopupMenu = new PopupMenu(this, mMenuButton);
        mMenu = mPopupMenu.getMenu();

        //noinspection PointlessArithmeticExpression
        mMenu.add(Menu.NONE, Menu.FIRST + 0, 0, "加入播放列表");
        mMenu.add(Menu.NONE, Menu.FIRST + 1, 0, "查看专辑");
        mMenu.add(Menu.NONE, Menu.FIRST + 2, 0, "详细信息");

        mMenuButton.setOnClickListener(v -> mPopupMenu.show());

        mInfoBody.setOnClickListener(v -> {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mCardView.getLayoutParams();
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(params);

//            GlideApp.with(this).pauseAllRequests();

            if (!HAS_BIG) {
                DEF_TOP = params.topMargin;
                ValueAnimator anim = ValueAnimator.ofInt(params.topMargin, params.leftMargin * 2);
                anim.setDuration(250);
                anim.addUpdateListener(animation -> {
                    int currentValue = (Integer) animation.getAnimatedValue();
                    layoutParams.setMargins(params.leftMargin, currentValue, params.rightMargin, params.bottomMargin);
                    mCardView.setLayoutParams(layoutParams);
                    mCardView.requestLayout();
                });

//                anim.addListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        GlideApp.with(MusicDetailActivity.this).resumeRequests();
//                    }
//                });
                anim.start();
                HAS_BIG = true;
            } else {
                ValueAnimator anim = ValueAnimator.ofInt(params.topMargin, DEF_TOP);
                anim.setDuration(250);
                anim.addUpdateListener(animation -> {
                    int currentValue = (Integer) animation.getAnimatedValue();
                    layoutParams.setMargins(params.leftMargin, currentValue, params.rightMargin, params.bottomMargin);
                    mCardView.setLayoutParams(layoutParams);
                    mCardView.requestLayout();
                });

//                anim.addListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        GlideApp.with(MusicDetailActivity.this).resumeRequests();
//                    }
//                });
                anim.start();
                HAS_BIG = false;

                mRecyclerView.scrollToPosition(Values.CURRENT_MUSIC_INDEX);
            }

        });

        mMusicListFragment = (MusicListFragment) ((MainActivity) Data.sActivities.get(0)).getFragmentList().get(0);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new MyRecyclerAdapter(Data.mMusicPathList
                , Data.mSongNameList
                , Data.mSongAlbumList
                , this));

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
                MusicDetailActivity musicDetailActivity = (MusicDetailActivity) Data.sActivities.get(1);
                mMainActivity.setButtonTypePlay();
                musicDetailActivity.setButtonTypePlay();

                Data.sMusicBinder.seekTo(seekBar.getProgress());
                Data.sMusicBinder.playMusic();
//                if (Values.HAS_PLAYED) {
//                    Utils.Ui.fastToast(MusicDetailActivity.this, "Jumping to " + seekBar.getProgress() + " seconds.");
//
//                    new Thread(() -> {
//                        if (!Values.CURRENT_SONG_PATH.equals("null")) {
//                            if (!Data.sMusicBinder.isPlayingMusic()) {
//                                Intent intent = new Intent();
//                                intent.setComponent(new ComponentName(Values.PKG_NAME, Values.BroadCast.ReceiverOnMusicPlay));
//                                sendBroadcast(intent);
//                            }
//                        }
//
//                        //首先获取seekBar拖动后的位置
//                        int progress = seekBar.getProgress();
//                        //跳转到某个位置播放
//                        Data.sMusicBinder.seekTo(progress);
//                    }).start();
//                }
            }
        });

        //just pause or play
        mPlayButton.setOnClickListener(v -> {
            Intent intent;
            if (Values.MUSIC_PLAYING) {
                intent = new Intent();
                intent.setComponent(new ComponentName(Values.PKG_NAME, Values.BroadCast.ReceiverOnMusicPause));
                sendBroadcast(intent);
            } else {
//                intent = new Intent();
//                intent.setComponent(new ComponentName(Values.PKG_NAME, Values.BroadCast.ReceiverOnMusicPlay));
//                intent.putExtra("play_type", 2);
//                sendBroadcast(intent);
                Data.sMusicBinder.playMusic();
                Utils.Ui.setNowPlaying();
            }
        });

        mNextButton.setOnClickListener(v -> {
            mSeekBar.setProgress(0);            //防止seekBar跳动到Max
            if (Values.CURRENT_PLAY_TYPE.equals(Values.TYPE_RANDOM)) {
                Utils.Audio.shufflePlayback();
            } else if (Values.CURRENT_PLAY_TYPE.equals(Values.TYPE_COMMON)) {
                // TODO: 2018/11/10 more models
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(Values.PKG_NAME, Values.BroadCast.ReceiverOnMusicPlay));
                intent.putExtra("play_type", 4);
                sendBroadcast(intent);
            }
        });

        mPreviousButton.setOnClickListener(v -> {
            //当进度条大于播放总长 1/20 那么重新播放该歌曲
            if (Data.sMusicBinder.getCurrentPosition() > Data.sMusicBinder.getDuration() / 20) {
                Data.sMusicBinder.seekTo(0);
            } else {
                if (READY) {
                    READY = false;
                    if (Data.sHistoryPlayIndex.size() == 1) {
                        Data.sMusicBinder.seekTo(0);

                    } else if (Data.sHistoryPlayIndex.size() >= 2) {
                        mSeekBar.setProgress(0);
                        Data.sMusicBinder.resetMusic();

                        int tempSize = Data.sHistoryPlayIndex.size();

                        int index = Data.sHistoryPlayIndex.get(tempSize - 2);
                        Data.sHistoryPlayIndex.remove(tempSize - 1);

                        String path = Data.mMusicPathList.get(index);
                        String musicName = Data.mSongNameList.get(index);
                        String albumName = Data.mSongAlbumList.get(index);

                        Bitmap cover = Utils.Audio.getMp3Cover(path);

                        MainActivity mainActivity = (MainActivity) Data.sActivities.get(0);
                        mainActivity.setCurrentSongInfo(musicName, albumName, path, cover);
                        setCurrentSongInfo(musicName, albumName, Utils.Audio.getAlbumByteImage(path));

                        Utils.Ui.setNowPlaying();

                        Values.MUSIC_PLAYING = true;
                        Values.HAS_PLAYED = true;
                        Values.CURRENT_MUSIC_INDEX = index;
                        Values.CURRENT_SONG_PATH = path;

                        try {
                            Data.sMusicBinder.setDataSource(path);
                            Data.sMusicBinder.prepare();
                            Data.sMusicBinder.playMusic();

                            mHandler.sendEmptyMessage(Values.INIT_SEEK_BAR);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    READY = true;
                }
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    public NotLeakHandler getHandler() {
        return mHandler;
    }

    /*------------------ UI -----------------------*/
    public void setButtonTypePause() {
        runOnUiThread(() -> mPlayButton.setImageResource(R.drawable.ic_play_arrow_black_24dp));
    }

    public void setButtonTypePlay() {
        runOnUiThread(() -> mPlayButton.setImageResource(R.drawable.ic_pause_black_24dp));
    }

    public void setCurrentSongInfo(String name, String albumName, byte[] cover) {
        runOnUiThread(() -> {
            Utils.Ui.setBlurEffect(MusicDetailActivity.this, cover, mPrimaryBackground);
            GlideApp.with(MusicDetailActivity.this).load(cover).transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME)).centerCrop().into(mMusicAlbumImage);
            setInfoBar(name, albumName);
        });
    }

    public void setInfoBar(String name, String albumName) {
        mMusicNameText.setText(name);
        mAlbumNameText.setText(albumName);
        mIndexTextView.setText(String.valueOf(Values.CURRENT_MUSIC_INDEX));
        mRecyclerView.scrollToPosition(Values.CURRENT_MUSIC_INDEX);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
    /*------------------- UI ----------------------*/

    @Override
    protected void onDestroy() {
        Data.sActivities.remove(this);
        super.onDestroy();
    }

    public class NotLeakHandler extends Handler {
        private WeakReference<MusicDetailActivity> mWeakReference;

        NotLeakHandler(MusicDetailActivity activity, Looper looper) {
            super(looper);
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Values.INIT_SEEK_BAR: {
                    mSeekBar.setMax(Data.sMusicBinder.getDuration());
                }
                break;
                case Values.SEEK_BAR_UPDATE: {
                    //点击body 或 music 正在播放 才可以进行seekBar更新
                    if (Data.sMusicBinder.isPlayingMusic()) {
                        Log.d(TAG, "handleMessage: seekBar set");
                        mSeekBar.setProgress(Data.sMusicBinder.getCurrentPosition());
                    }

                    //循环更新 0.5s 一次
                    mHandler.sendEmptyMessageDelayed(Values.SEEK_BAR_UPDATE, 500);
                }
                default:
            }
        }
    }
}
