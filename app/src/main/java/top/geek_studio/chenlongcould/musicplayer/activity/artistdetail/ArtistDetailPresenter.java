package top.geek_studio.chenlongcould.musicplayer.activity.artistdetail;

import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
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
 * @date : 2019/06/04/14
 */
public class ArtistDetailPresenter implements ArtistDetailContract.Presenter {

	private ArtistDetailContract.View mView;

	private List<MusicItem> mSongs = new ArrayList<>();

	private List<Disposable> mDisposables = new ArrayList<>();

	public ArtistDetailPresenter(@NonNull final ArtistDetailContract.View view) {
		mView = view;
		mView.setPresenter(this);
	}

	@Override
	public void start() {
		load(mView.getIntent());
	}

	private void load(@NonNull final Intent intent) {

		final String artistName = intent.getStringExtra("key");
		final String intentArtistId = String.valueOf(intent.getIntExtra("_id", -10));

		// data
		final long[] totalDuration = {0};
		final int[] albumCount = {0};

		// tags
		int sizeDone = 0;
		int durationDone = 1;
		int notifyAdapter = 2;
		int albumCountDone = 3;

		Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
			final List<String> mMusicIds = new ArrayList<>();

			//根据Album名称查music ID
			final Cursor cursor = mView.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					new String[]{MediaStore.Audio.Media._ID}, MediaStore.Audio.Media.ARTIST + " = ?", new String[]{artistName}, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					final String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
					mMusicIds.add(id);
				} while (cursor.moveToNext());
				cursor.close();
			}

			//selection...
			if (mMusicIds.size() > 0) {
				final StringBuilder selection = new StringBuilder(MediaStore.Audio.Media._ID + " IN (");
				for (int i = 0; i < mMusicIds.size(); i++) {
					selection.append("?");
					if (i != mMusicIds.size() - 1) {
						selection.append(",");
					}
				}
				selection.append(")");

				final Cursor cursor2 = mView.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
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
				}
			}

			final Cursor cursor2 = mView.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null,
					MediaStore.Audio.Artists._ID + " = ?", new String[]{intentArtistId}, MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);
			if (cursor2 != null && cursor2.moveToFirst()) {
				albumCount[0] = cursor2.getInt(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS));
				cursor2.close();
				emitter.onNext(albumCount[0]);
			}

		}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).safeSubscribe(new Observer<Integer>() {

			@Override
			public void onSubscribe(Disposable d) {
				mDisposables.add(d);
			}

			@Override
			public void onNext(Integer integer) {
				if (integer == sizeDone) {
					mView.setSongCountText(String.valueOf(mSongs.size()));
				}

				if (integer == durationDone) {
					mView.setDurationText(Data.S_SIMPLE_DATE_FORMAT.format(new Date(totalDuration[0])));
				}

				if (integer == notifyAdapter) {
					mView.notifyDataSetChanged();
				}

				if (integer == albumCountDone) {
					mView.setAlbumCountText(String.valueOf(albumCount[0]));
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
	public void close() {
		ClearTool.clearDisposables(mDisposables);
	}

	@NonNull
	@Override
	public List<MusicItem> getSongs() {
		return mSongs;
	}
}
