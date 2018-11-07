package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.lang.ref.WeakReference;

import jp.wasabeef.glide.transformations.BlurTransformation;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public final class MusicDetailActivity extends Activity {

    private ImageView mMusicAlbumImage;

    private ImageView mPrimaryBackground;

    private SeekBar mSeekBar;

    private NotLeakHandler mHandler;

    private HandlerThread mHandlerThread;

//    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_detail);

        mHandlerThread = new HandlerThread("Handler Thread in MusicDetailActivity");
        mHandlerThread.start();
        mHandler = new NotLeakHandler(this, mHandlerThread.getLooper());

        initView();

        initData();
    }

    private void initData() {
        GlideApp.with(this)
                .load(Data.sCurrentMusicBitmap)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(mMusicAlbumImage);

        GlideApp.with(this)
                .load(Data.sCurrentMusicBitmap)
                .apply(bitmapTransform(new BlurTransformation
                        (20, 10)))
                .into(mPrimaryBackground);
        mHandler.sendEmptyMessage(Values.INIT_SEEK_BAR);
        mHandler.sendEmptyMessage(Values.SEEK_BAR_UPDATE);
    }

    private void initView() {
        mMusicAlbumImage = findViewById(R.id.activity_music_detail_album_image);
        mPrimaryBackground = findViewById(R.id.activity_music_detail_primary_background);
        mSeekBar = findViewById(R.id.seekBar);
//        mToolbar = findViewById(R.id.activity_music_detail_toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    class NotLeakHandler extends Handler {
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
                    mSeekBar.setProgress(Data.sMusicBinder.getCurrentPosition());

                    //循环更新 0.5s 一次
                    mHandler.sendEmptyMessageDelayed(Values.SEEK_BAR_UPDATE, 500);
                }
                default:
            }
        }
    }

}
