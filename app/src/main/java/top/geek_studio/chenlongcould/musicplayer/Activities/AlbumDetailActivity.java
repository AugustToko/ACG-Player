/*
 * ************************************************************
 * 文件：AlbumDetailActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月18日 18:58:29
 * 上次修改时间：2019年01月18日 11:15:25
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.databinding.DataBindingUtil;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.musicplayer.Adapters.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Database.CustomAlbumPath;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.databinding.ActivityAlbumDetailOthBinding;
import top.geek_studio.chenlongcould.musicplayer.misc.SimpleObservableScrollViewCallbacks;

/**
 * a activity that show Music Album Detail data
 * <p>
 * has dataBinding
 *
 * @author chenlongcould
 * @apiNote some by others
 */
public final class AlbumDetailActivity extends MyBaseCompatActivity {

    public static final String TAG = "AlbumDetailActivity";

    private ActivityAlbumDetailOthBinding mAlbumDetailBinding;

    private int headerViewHeight;

    @ColorInt
    private int toolbarColor;
    private final SimpleObservableScrollViewCallbacks observableScrollViewCallbacks = new SimpleObservableScrollViewCallbacks() {
        @Override
        public void onScrollChanged(int scrollY, boolean b, boolean b2) {
            scrollY += headerViewHeight;

            // Change alpha of overlay
            float headerAlpha = Math.max(0, Math.min(1, (float) 2 * scrollY / headerViewHeight));
            mAlbumDetailBinding.headerOverlay.setBackgroundColor(Utils.Ui.withAlpha(toolbarColor, headerAlpha));

            // Translate name text
            mAlbumDetailBinding.header.setTranslationY(Math.max(-scrollY, -headerViewHeight));
            mAlbumDetailBinding.headerOverlay.setTranslationY(Math.max(-scrollY, -headerViewHeight));
            mAlbumDetailBinding.image.setTranslationY(Math.max(-scrollY, -headerViewHeight));
        }
    };
    private List<Disposable> mDisposables = new ArrayList<>();

    /**
     * ------------- data ---------------
     */

    private List<MusicItem> mSongs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAlbumDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_album_detail_oth);
        mAlbumDetailBinding.toolbar.setNavigationOnClickListener(v -> finish());
        headerViewHeight = getResources().getDimensionPixelSize(R.dimen.detail_header_height);
        initData();
    }

    private void initData() {
        final Intent intent = getIntent();
        if (intent != null) {
            setupAlbumCover(intent);

            String key = intent.getStringExtra("key");
            mAlbumDetailBinding.toolbar.setTitle(key);
            mAlbumDetailBinding.toolbar.inflateMenu(R.menu.menu_toolbar_album_detail);
            String intentAlbumId = String.valueOf(intent.getIntExtra("_id", -10));
            List<CustomAlbumPath> paths = LitePal.where("mAlbumId = ?", intentAlbumId).find(CustomAlbumPath.class);

            //update menu checkbox
            if (!intentAlbumId.equals("-10") && paths.size() > 0) {
                mAlbumDetailBinding.toolbar.getMenu().findItem(R.id.menu_toolbar_album_force_album).setChecked(paths.get(0).isForceUse());
            }

            mAlbumDetailBinding.toolbar.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.menu_toolbar_album_force_album: {
                        if (!intentAlbumId.equals("-10")) {
                            CustomAlbumPath customAlbumPath = paths.get(0);
                            if (menuItem.isChecked()) {
                                menuItem.setChecked(false);
                                customAlbumPath.setForceUse(false);
                            } else {
                                menuItem.setChecked(true);
                                customAlbumPath.setForceUse(true);
                            }
                            customAlbumPath.save();
                        } else {
                            Toast.makeText(this, "Album Id Error..., finishing...", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    break;
                }
                return true;
            });


            mAlbumDetailBinding.recyclerView.setPadding(0, headerViewHeight, 0, 0);
            mAlbumDetailBinding.recyclerView.setScrollViewCallbacks(observableScrollViewCallbacks);
            final View contentView = getWindow().getDecorView().findViewById(android.R.id.content);
            contentView.post(() -> observableScrollViewCallbacks.onScrollChanged(-headerViewHeight, false, false));
            mAlbumDetailBinding.recyclerView.setLayoutManager(new GridLayoutManager(AlbumDetailActivity.this, 1));
            final MyRecyclerAdapter adapter = new MyRecyclerAdapter(mSongs, AlbumDetailActivity.this, TAG);
            mAlbumDetailBinding.recyclerView.setAdapter(adapter);
            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    if (adapter.getItemCount() == 0) finish();
                }
            });

            final long[] totalDuration = {0};

            int SIZE_DONE = 0;
            int DURATION_DONE = 1;

            Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
                List<String> mMusicIds = new ArrayList<>();
                //根据Album名称查music ID
                final Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Audio.Media._ID}, MediaStore.Audio.Media.ALBUM + " = ?", new String[]{key}, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));     //get music _id
                        mMusicIds.add(id);
                    } while (cursor.moveToNext());
                    cursor.close();
                }

                //selection...
                if (mMusicIds.size() > 0) {
                    StringBuilder selection = new StringBuilder(MediaStore.Audio.Media._ID + " IN (");
                    for (int i = 0; i < mMusicIds.size(); i++) {
                        selection.append("?");
                        if (i != mMusicIds.size() - 1) {
                            selection.append(",");
                        }
                    }
                    selection.append(")");

                    final Cursor cursor2 = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                            selection.toString(), mMusicIds.toArray(new String[0]), MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

                    //获取数据(该专辑下歌曲)
                    if (cursor2 != null) {
                        cursor2.moveToFirst();
                        do {
                            final String path = cursor2.getString(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

                            if (!new File(path).exists()) return;

                            final String mimeType = cursor2.getString(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
                            final String name = cursor2.getString(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                            final String albumName = cursor2.getString(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                            final int id = cursor2.getInt(cursor2.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                            final int size = (int) cursor2.getLong(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                            final int duration = cursor2.getInt(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                            final String artist = cursor2.getString(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                            final long addTime = cursor2.getLong(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));
                            final int albumId = cursor2.getInt(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

                            Log.d(TAG, "initData: " + name + " " + path);

                            final MusicItem.Builder builder = new MusicItem.Builder(id, name, path)
                                    .musicAlbum(albumName)
                                    .addTime((int) addTime)
                                    .artist(artist)
                                    .duration(duration)
                                    .mimeName(mimeType)
                                    .size(size)
                                    .addAlbumId(albumId);

                            totalDuration[0] += duration;

                            mSongs.add(builder.build());
                        } while (cursor2.moveToNext());
                        cursor2.close();
                        emitter.onNext(DURATION_DONE);
                        emitter.onNext(SIZE_DONE);
                        emitter.onComplete();
                    }
                }


            }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).safeSubscribe(new Observer<Integer>() {

                @Override
                public void onSubscribe(Disposable d) {
                    mDisposables.add(d);
                }

                @Override
                public void onNext(Integer integer) {
                    if (integer == SIZE_DONE) {
                        Log.d(TAG, "onNext: " + mSongs.size());
                        mAlbumDetailBinding.songCountText.setText(String.valueOf(mSongs.size()));
                    }

                    if (integer == DURATION_DONE) {
                        mAlbumDetailBinding.durationText.setText(Data.sSimpleDateFormat.format(new Date(totalDuration[0])));
                    }
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {
                    mAlbumDetailBinding.recyclerView.getAdapter().notifyDataSetChanged();
                }
            });

            final String[] date = {"-"};
            final String[] artistOfAlbum = {"-"};
            Observable.create(emitter -> {
                //get year
                final Cursor albumInfo = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null,
                        MediaStore.Audio.Media.ALBUM + " = ?", new String[]{key}, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);

                if (albumInfo != null && albumInfo.getCount() > 0) {
                    albumInfo.moveToFirst();
                    do {
                        if (!TextUtils.isEmpty(albumInfo.getString(albumInfo.getColumnIndexOrThrow(MediaStore.Audio.Albums.FIRST_YEAR)))) {
                            date[0] = albumInfo.getString(albumInfo.getColumnIndexOrThrow(MediaStore.Audio.Albums.FIRST_YEAR));
                        }
                        if (!TextUtils.isEmpty(albumInfo.getString(albumInfo.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)))) {
                            artistOfAlbum[0] = albumInfo.getString(albumInfo.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST));
                        }
                    } while (albumInfo.moveToNext());
                    albumInfo.close();
                    emitter.onComplete();
                }
            }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).safeSubscribe(new Observer<Object>() {
                @Override
                public void onSubscribe(Disposable d) {
                    mDisposables.add(d);
                }

                @Override
                public void onNext(Object o) {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {
                    mAlbumDetailBinding.albumYearText.setText(date[0]);
                    mAlbumDetailBinding.artistText.setText(artistOfAlbum[0]);
                }
            });
        }

    }

    private void setupAlbumCover(Intent intent) {
        //获取MainAlbum图像
        int id = intent.getIntExtra("_id", -1);
        Bitmap bitmap = Utils.Audio.getCoverBitmap(this, id);
        if (bitmap != null) {
            toolbarColor = Palette.from(bitmap).generate().getVibrantColor(Utils.Ui.getPrimaryColor(this));
        } else {
            toolbarColor = Utils.Ui.getPrimaryColor(this);
        }
        setUpColor();
        GlideApp.with(this)
                .load(bitmap)
                .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mAlbumDetailBinding.image);
    }

    private void setUpColor() {
        mAlbumDetailBinding.toolbar.setBackgroundColor(toolbarColor);
        mAlbumDetailBinding.appbar.setBackgroundColor(toolbarColor);
        mAlbumDetailBinding.header.setBackgroundColor(toolbarColor);
        if (Utils.Ui.isColorLight(toolbarColor)) {
            Utils.Ui.setOverToolbarColor(mAlbumDetailBinding.toolbar, Color.BLACK);
            mAlbumDetailBinding.artistText.setTextColor(Color.BLACK);
            mAlbumDetailBinding.songCountText.setTextColor(Color.BLACK);
            mAlbumDetailBinding.albumYearText.setTextColor(Color.BLACK);
            mAlbumDetailBinding.durationText.setTextColor(Color.BLACK);

            mAlbumDetailBinding.artistIcon.setColorFilter(Color.BLACK);
            mAlbumDetailBinding.songCountIcon.setColorFilter(Color.BLACK);
            mAlbumDetailBinding.durationIcon.setColorFilter(Color.BLACK);
            mAlbumDetailBinding.albumYearIcon.setColorFilter(Color.BLACK);
        } else {
            Utils.Ui.setOverToolbarColor(mAlbumDetailBinding.toolbar, Color.WHITE);
            mAlbumDetailBinding.artistText.setTextColor(Color.WHITE);
            mAlbumDetailBinding.songCountText.setTextColor(Color.WHITE);
            mAlbumDetailBinding.albumYearText.setTextColor(Color.WHITE);
            mAlbumDetailBinding.durationText.setTextColor(Color.WHITE);

            mAlbumDetailBinding.artistIcon.setColorFilter(Color.WHITE);
            mAlbumDetailBinding.songCountIcon.setColorFilter(Color.WHITE);
            mAlbumDetailBinding.durationIcon.setColorFilter(Color.WHITE);
            mAlbumDetailBinding.albumYearIcon.setColorFilter(Color.WHITE);
        }
    }

    @Override
    public void initStyle() {
        super.initStyle();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Disposable d : mDisposables) {
            if (!d.isDisposed()) d.dispose();
        }
    }
}
