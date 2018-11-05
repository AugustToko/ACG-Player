package top.geek_studio.chenlongcould.musicplayer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.lang.ref.WeakReference;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class MusicDetailActivity extends Activity {

    private ImageView mMusicAlbumImage;

    private ImageView mPrimaryBackground;

    private SeekBar mSeekBar;

    private NotLeakHandler mHandler;

    private HandlerThread mHandlerThread;

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

        mHandler.sendEmptyMessage(Values.INIT_SEEK_BAR);
        mHandler.sendEmptyMessage(Values.SEEK_BAR_UPDATE);
    }

    private void initView() {
        mMusicAlbumImage = findViewById(R.id.activity_music_detail_album_image);
        mPrimaryBackground = findViewById(R.id.activity_music_detail_primary_background);
        mSeekBar = findViewById(R.id.seekBar);
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
                    mSeekBar.setMax(((MainActivity) Data.sActivities.get(0)).getMusicBinder().getDuration());
                }
                break;
                case Values.SEEK_BAR_UPDATE: {
                    mSeekBar.setProgress(((MainActivity) Data.sActivities.get(0)).getMusicBinder().getCurrentPosition());

                    mHandler.sendEmptyMessageDelayed(Values.SEEK_BAR_UPDATE, 500);
                }
                default:
            }
        }
    }

}
