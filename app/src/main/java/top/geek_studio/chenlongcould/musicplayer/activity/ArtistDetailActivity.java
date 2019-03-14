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

package top.geek_studio.chenlongcould.musicplayer.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

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
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.database.ArtistArtPath;
import top.geek_studio.chenlongcould.musicplayer.databinding.ActivityArtistDetailOthBinding;
import top.geek_studio.chenlongcould.musicplayer.misc.SimpleObservableScrollViewCallbacks;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

/**
 * a activity that show Music Album Detail data
 * <p>
 * has dataBinding
 *
 * @author chenlongcould
 * @apiNote some by others
 */
public final class ArtistDetailActivity extends BaseCompatActivity {

	public static final String TAG = "ArtistDetailActivity";

	private ActivityArtistDetailOthBinding mArtistDetailOthBinding;

	private int headerViewHeight;

	@ColorInt
	private int toolbarColor;
	private final SimpleObservableScrollViewCallbacks observableScrollViewCallbacks = new SimpleObservableScrollViewCallbacks() {
		@Override
		public void onScrollChanged(int scrollY, boolean b, boolean b2) {
			scrollY += headerViewHeight;

			// Change alpha of overlay
			float headerAlpha = Math.max(0, Math.min(1, (float) 2 * scrollY / headerViewHeight));
			mArtistDetailOthBinding.headerOverlay.setBackgroundColor(Utils.Ui.withAlpha(toolbarColor, headerAlpha));

			// Translate name text
			mArtistDetailOthBinding.header.setTranslationY(Math.max(-scrollY, -headerViewHeight));
			mArtistDetailOthBinding.headerOverlay.setTranslationY(Math.max(-scrollY, -headerViewHeight));
			mArtistDetailOthBinding.image.setTranslationY(Math.max(-scrollY, -headerViewHeight));
		}
	};
	private List<Disposable> mDisposables = new ArrayList<>();

	private List<MusicItem> mSongs = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mArtistDetailOthBinding = DataBindingUtil.setContentView(this, R.layout.activity_artist_detail_oth);
		mArtistDetailOthBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
		headerViewHeight = getResources().getDimensionPixelSize(R.dimen.detail_header_height);
		initData();
	}

	@Override
	public String getActivityTAG() {
		return TAG;
	}

	@Override
	public void inflateCommonMenu() {

	}

	@Override
	public void inflateChooseMenu() {

	}

	private void initData() {
		final Intent intent = getIntent();

		final String artistName = intent.getStringExtra("key");
		final String intentArtistId = String.valueOf(intent.getIntExtra("_id", -10));

		mArtistDetailOthBinding.toolbar.setTitle(artistName);

		final List<ArtistArtPath> paths = LitePal.where("mArtistId = ?", intentArtistId).find(ArtistArtPath.class);
		if (paths.size() > 0) {
			ArtistArtPath art = paths.get(0);
			if (!"null".equals(art.getArtistArt())) {
				Bitmap bitmap = Utils.Ui.readBitmapFromFile(paths.get(0).getArtistArt(), 50, 50);
				if (bitmap != null) {
					toolbarColor = Palette.from(bitmap).generate().getVibrantColor(Utils.Ui.getPrimaryColor(this));
				} else {
					toolbarColor = Utils.Ui.getPrimaryColor(this);
				}
				setUpColor();
				GlideApp.with(this)
						.load(art.getArtistArt())
						.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						.into(mArtistDetailOthBinding.image);
			} else {
				toolbarColor = Utils.Ui.getPrimaryColor(this);
				setUpColor();
				GlideApp.with(this)
						.load(R.drawable.default_album_art)
						.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						.into(mArtistDetailOthBinding.image);
			}
		} else {
			toolbarColor = Utils.Ui.getPrimaryColor(this);
			setUpColor();
			GlideApp.with(this)
					.load(R.drawable.default_album_art)
					.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.into(mArtistDetailOthBinding.image);
		}

		mArtistDetailOthBinding.recyclerView.setPadding(0, headerViewHeight, 0, 0);
		mArtistDetailOthBinding.recyclerView.setScrollViewCallbacks(observableScrollViewCallbacks);
		final View contentView = getWindow().getDecorView().findViewById(android.R.id.content);
		contentView.post(() -> observableScrollViewCallbacks.onScrollChanged(-headerViewHeight, false, false));
		mArtistDetailOthBinding.recyclerView.setLayoutManager(new GridLayoutManager(ArtistDetailActivity.this, 1));
		final MyRecyclerAdapter adapter = new MyRecyclerAdapter(this, mSongs, 0);
		mArtistDetailOthBinding.recyclerView.setAdapter(adapter);
		adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onChanged() {
				super.onChanged();
				if (adapter.getItemCount() == 0) {
					finish();
				}
			}
		});

		final long[] totalDuration = {0};

		int sizeDone = 0;
		int durationDone = 1;

		Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
			List<String> mMusicIds = new ArrayList<>();

			//根据Album名称查music ID
			final Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					new String[]{MediaStore.Audio.Media._ID}, MediaStore.Audio.Media.ARTIST + " = ?", new String[]{artistName}, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
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

				if (cursor2 != null) {
					cursor2.moveToFirst();
					do {
						final String path = cursor2.getString(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

						if (!new File(path).exists()) {
							return;
						}

						final String mimeType = cursor2.getString(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
						final String name = cursor2.getString(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
						final String albumName = cursor2.getString(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
						final int id = cursor2.getInt(cursor2.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
						final int size = (int) cursor2.getLong(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
						final int duration = cursor2.getInt(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
						final String artist = cursor2.getString(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
						final long addTime = cursor2.getLong(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));
						final int albumId = cursor2.getInt(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

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
					emitter.onNext(durationDone);
					emitter.onNext(sizeDone);
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
				if (integer == sizeDone) {
					mArtistDetailOthBinding.songCountText.setText(String.valueOf(mSongs.size()));
				}

				if (integer == durationDone) {
					mArtistDetailOthBinding.durationText.setText(Data.sSimpleDateFormat.format(new Date(totalDuration[0])));
				}
			}

			@Override
			public void onError(Throwable e) {

			}

			@Override
			public void onComplete() {
				mArtistDetailOthBinding.recyclerView.getAdapter().notifyDataSetChanged();
			}
		});

		Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
			Cursor cursor = getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null,
					MediaStore.Audio.Artists._ID + " = ?", new String[]{intentArtistId}, MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);
			if (cursor != null && cursor.moveToFirst()) {
				int albumCount = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS));
				Log.d(TAG, "initData: " + albumCount);
				cursor.close();
				emitter.onNext(albumCount);
			}
		}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).safeSubscribe(new Observer<Integer>() {

			@Override
			public void onSubscribe(Disposable d) {
				mDisposables.add(d);
			}

			@Override
			public void onNext(Integer integer) {
				mArtistDetailOthBinding.artistAlbumCountText.setText(String.valueOf(integer));
			}

			@Override
			public void onError(Throwable e) {

			}

			@Override
			public void onComplete() {

			}
		});

	}

	private void setUpColor() {
		mArtistDetailOthBinding.toolbar.setBackgroundColor(toolbarColor);
		mArtistDetailOthBinding.appbar.setBackgroundColor(toolbarColor);
		mArtistDetailOthBinding.header.setBackgroundColor(toolbarColor);
		if (Utils.Ui.isColorLight(toolbarColor)) {
			Utils.Ui.setOverToolbarColor(mArtistDetailOthBinding.toolbar, Color.BLACK);
			mArtistDetailOthBinding.songCountText.setTextColor(Color.BLACK);
			mArtistDetailOthBinding.artistAlbumCountText.setTextColor(Color.BLACK);
			mArtistDetailOthBinding.durationText.setTextColor(Color.BLACK);

			mArtistDetailOthBinding.songCountIcon.setColorFilter(Color.BLACK);
			mArtistDetailOthBinding.durationIcon.setColorFilter(Color.BLACK);
			mArtistDetailOthBinding.artistAlbumCount.setColorFilter(Color.BLACK);
		} else {
			Utils.Ui.setOverToolbarColor(mArtistDetailOthBinding.toolbar, Color.WHITE);
			mArtistDetailOthBinding.songCountText.setTextColor(Color.WHITE);
			mArtistDetailOthBinding.artistAlbumCountText.setTextColor(Color.WHITE);
			mArtistDetailOthBinding.durationText.setTextColor(Color.WHITE);

			mArtistDetailOthBinding.songCountIcon.setColorFilter(Color.WHITE);
			mArtistDetailOthBinding.durationIcon.setColorFilter(Color.WHITE);
			mArtistDetailOthBinding.artistAlbumCount.setColorFilter(Color.WHITE);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		for (Disposable d : mDisposables) {
			if (!d.isDisposed()) {
				d.dispose();
			}
		}
	}
}
