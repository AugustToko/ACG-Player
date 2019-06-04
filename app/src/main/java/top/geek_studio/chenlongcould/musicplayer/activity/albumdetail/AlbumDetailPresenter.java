package top.geek_studio.chenlongcould.musicplayer.activity.albumdetail;

import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.TextUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.utils.ClearTool;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author : chenlongcould
 * @date : 2019/06/04/13
 */
final public class AlbumDetailPresenter implements AlbumDetailContract.Presenter {

	private final AlbumDetailContract.View mView;

	private ArrayList<MusicItem> mSongs = new ArrayList<>();

	private List<Disposable> mDisposables = new ArrayList<>();

	public AlbumDetailPresenter(AlbumDetailContract.View view) {
		mView = view;
		mView.setPresenter(this);
	}

	@Override
	public void start() {
		load(mView.getAlbumDetailIntent());
	}

	private void load(Intent intent) {
		if (intent == null) {
			return;
		}

		final String albumTitle = intent.getStringExtra(AlbumDetailActivity.IntentKey.ALBUM_NAME);

		final long[] totalDuration = {0};

		// tag
		byte sizeDone = 0;
		byte durationDone = 1;
		byte loadListDone = 2;
		byte loadalbumInfoDone = 3;

		final String[] albumYearText = {"-"};
		final String[] artistOfAlbum = {"-"};

		Observable.create((ObservableOnSubscribe<Byte>) emitter -> {
			List<String> mMusicIds = new ArrayList<>();
			//根据Album名称查music ID
			final Cursor cursor = mView.getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					new String[]{MediaStore.Audio.Media._ID}, MediaStore.Audio.Media.ALBUM + " = ?", new String[]{albumTitle}, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					//get music _id
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

				final Cursor cursor2 = mView.getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
						selection.toString(), mMusicIds.toArray(new String[0]), MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

				//获取数据(该专辑下歌曲)
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
					emitter.onNext(loadListDone);
				}
			}

			/*
			 * 完成数据加载后对用于显示数据统计的 {@link android.widget.TextView} 设置值
			 */
			//get year
			final Cursor albumInfo = mView.getContext().getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null,
					MediaStore.Audio.Media.ALBUM + " = ?", new String[]{albumTitle}, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);

			if (albumInfo != null && albumInfo.getCount() > 0) {
				albumInfo.moveToFirst();
				do {
					if (!TextUtils.isEmpty(albumInfo.getString(albumInfo.getColumnIndexOrThrow(MediaStore.Audio.Albums.FIRST_YEAR)))) {
						albumYearText[0] = albumInfo.getString(albumInfo.getColumnIndexOrThrow(MediaStore.Audio.Albums.FIRST_YEAR));
					}
					if (!TextUtils.isEmpty(albumInfo.getString(albumInfo.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)))) {
						artistOfAlbum[0] = albumInfo.getString(albumInfo.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST));
					}
				} while (albumInfo.moveToNext());
				albumInfo.close();
			}

			emitter.onNext(loadalbumInfoDone);

		}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).safeSubscribe(new Observer<Byte>() {

			@Override
			public void onSubscribe(Disposable d) {
				mDisposables.add(d);
			}

			@Override
			public void onNext(Byte b) {
				if (b == sizeDone) {
					mView.setSongCountText(String.valueOf(mSongs.size()));
				}

				if (b == durationDone) {
					mView.setDurationText(Data.S_SIMPLE_DATE_FORMAT.format(new Date(totalDuration[0])));
				}

				if (b == loadListDone) {
					mView.notifyDataSetChanged();
				}

				if (b == loadalbumInfoDone) {
					mView.setAlbumYearText(albumYearText[0]);
					mView.setArtistText(artistOfAlbum[0]);
				}

			}

			@Override
			public void onError(Throwable e) {

			}

			@Override
			public void onComplete() {
			}
		});

	}

	@Override
	public List<MusicItem> getSongs() {
		return mSongs;
	}

	@Override
	public void close() {
		ClearTool.clearDisposables(mDisposables);
	}
}
