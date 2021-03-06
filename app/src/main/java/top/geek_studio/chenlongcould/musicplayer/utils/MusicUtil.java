package top.geek_studio.chenlongcould.musicplayer.utils;

import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.media.MediaDescriptionCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import org.litepal.LitePal;
import org.litepal.LitePalDB;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import top.geek_studio.chenlongcould.geeklibrary.Private;
import top.geek_studio.chenlongcould.musicplayer.App;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.MessageWorker;
import top.geek_studio.chenlongcould.musicplayer.MusicService;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.activity.base.BaseListActivity;
import top.geek_studio.chenlongcould.musicplayer.database.DataModel;
import top.geek_studio.chenlongcould.musicplayer.database.Detail;
import top.geek_studio.chenlongcould.musicplayer.database.MyBlackPath;
import top.geek_studio.chenlongcould.musicplayer.model.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.model.Item;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.model.PlayListItem;
import top.geek_studio.chenlongcould.musicplayer.model.Playlist;
import top.geek_studio.chenlongcould.musicplayer.threadPool.CustomThreadPool;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class MusicUtil {

	private static final String TAG = "MusicUtil";

	public static void setTimer(@NonNull final Context context, final DataModel dataModel) {
		if (dataModel.mCurrentMusicItem == null || dataModel.mCurrentMusicItem.getMusicID() == -1) {
			Toast.makeText(context, "You need play music first!", Toast.LENGTH_SHORT).show();
			return;
		}

		new TimePickerDialog(context, (view, hourOfDay, minute) -> {
			final long time = hourOfDay * 360000 + minute * 60000;
			if (time == 0) {
				Toast.makeText(context, "Why not pause music by button?"
						, Toast.LENGTH_SHORT).show();
				return;
			}
			final Intent intent = new Intent(context, MusicService.class);
			intent.setAction(MusicService.ServiceActions.ACTION_SLEEP);
			intent.putExtra("time", time);
			context.startService(intent);
		}, 0, 0, true).show();

	}

	public static Uri getMediaStoreAlbumCoverUri(int albumId) {
		final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
		return ContentUris.withAppendedId(sArtworkUri, albumId);
	}

	public static Uri getSongFileUri(int songId) {
		return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId);
	}

	public static void sharMusic(@NonNull final Context context, List<MusicItem> items) {
		CustomThreadPool.post(() -> {
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			StringBuilder content = new StringBuilder(context.getResources().getString(R.string.app_name))
					.append("\r\n")
					.append("https://www.coolapk.com/apk/top.geek_studio.chenlongcould.musicplayer.Common")
					.append("\r\n");

			for (final MusicItem item : items) {
				content.append(item.getMusicName()).append("\r\n");
				Log.d(TAG, "sharMusic: append: " + item.getMusicName());
				break;
			}

			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(Intent.EXTRA_TEXT, content.toString());
			context.startActivity(intent);
		});
	}

	public static void findArtworkWithId(@NonNull final Context context, final Item item) {
		MusicItem musicItem = null;
		AlbumItem albumItem = null;
		if (item instanceof MusicItem) musicItem = (MusicItem) item;
		if (item instanceof AlbumItem) albumItem = (AlbumItem) item;

		if (musicItem == null && albumItem == null) return;

		String artwork = null;

		final Cursor cursor1 = context.getContentResolver().query(
				Uri.parse(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI + String.valueOf(File.separatorChar)
						+ (musicItem == null ? albumItem.getAlbumId() : musicItem.getAlbumId()))
				, new String[]{MediaStore.Audio.Albums.ALBUM_ART}, null, null, null);

		if (cursor1 != null && cursor1.getCount() != 0) {
			cursor1.moveToFirst();
			artwork = cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
			cursor1.close();
		} else {
			Log.d(TAG, "loadDataSource: loadArtwork failed!");
		}
		if (musicItem == null) {
			albumItem.setmArtwork(artwork);
		} else {
			musicItem.setArtwork(artwork);
		}
	}

	@Nullable
	public static String findArtworkWithId(@NonNull final Context context, final int albumId) {
		if (albumId < 0) return null;

		String artwork = null;

		final Cursor cursor1 = context.getContentResolver().query(
				Uri.parse(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI + String.valueOf(File.separatorChar)
						+ (albumId))
				, new String[]{MediaStore.Audio.Albums.ALBUM_ART}, null, null, null);

		if (cursor1 != null && cursor1.getCount() != 0) {
			cursor1.moveToFirst();
			artwork = cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
			cursor1.close();
		} else {
			Log.d(TAG, "loadDataSource: loadArtwork failed!");
		}
		return artwork;
	}

	@Nullable
	public static MusicItem d2m(@NonNull final MediaDescriptionCompat descriptionCompat) {
		if (descriptionCompat.getExtras() == null
				|| descriptionCompat.getMediaId() == null
				|| descriptionCompat.getMediaUri() == null) return null;

		Bundle bundle = descriptionCompat.getExtras();

		final MusicItem.Builder b2 = new MusicItem.Builder(Integer.parseInt(descriptionCompat.getMediaId())
				, String.valueOf(descriptionCompat.getTitle())
				, descriptionCompat.getMediaUri().getPath())
				.musicAlbum(String.valueOf(descriptionCompat.getSubtitle()))
				.addTime(bundle.getLong("addTime"))
				.artist(String.valueOf(descriptionCompat.getDescription()))
				.duration(bundle.getLong("duration"))
				.mimeName(bundle.getString("mimeType"))
				.size(bundle.getInt("size"))
				.addAlbumId(bundle.getInt("albumId"))
				.addArtistId(bundle.getInt("artistId"))
				.duration(bundle.getLong("duration"));
		return b2.build();
	}

	@Nullable
	public static MediaDescriptionCompat m2d(@NonNull final MusicItem item) {
		if (item.getMusicID() < 0) return null;

		final Bundle bundle = new Bundle();
		bundle.putInt("albumId", item.getAlbumId());
		bundle.putInt("artistId", item.getArtistId());
		bundle.putLong("addTime", item.getAddTime());
		bundle.putInt("size", item.getSize());
		bundle.putString("mimeType", item.getMimeName());
		bundle.putLong("duration", item.getDuration());

		Log.d(TAG, "m2d: " + Uri.fromFile(new File(item.getMusicPath())));

		final MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder()
				.setTitle(item.getMusicName())
				.setSubtitle(item.getMusicAlbum())
				.setDescription(item.getArtist())
				.setIconBitmap(null)
				.setMediaId(String.valueOf(item.getMusicID()))
				.setMediaUri(Uri.fromFile(new File(item.getMusicPath())))
				.setExtras(bundle);
		return builder.build();
	}

	public static void setRingtone(@NonNull final Context context, final int id) {
		final ContentResolver resolver = context.getContentResolver();
		final Uri uri = getSongFileUri(id);
		try {
			final ContentValues values = new ContentValues(2);
			values.put(MediaStore.Audio.AudioColumns.IS_RINGTONE, "1");
			values.put(MediaStore.Audio.AudioColumns.IS_ALARM, "1");
			resolver.update(uri, values, null, null);
		} catch (@NonNull final UnsupportedOperationException ignored) {
			return;
		}

		try {
			Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					new String[]{MediaStore.MediaColumns.TITLE},
					BaseColumns._ID + "=?",
					new String[]{String.valueOf(id)},
					null);
			try {
				if (cursor != null && cursor.getCount() == 1) {
					cursor.moveToFirst();
					Settings.System.putString(resolver, Settings.System.RINGTONE, uri.toString());
					final String message = context.getString(R.string.x_has_been_set_as_ringtone, cursor.getString(0));
					Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
				}
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		} catch (SecurityException ignored) {
		}
	}

//    @NonNull
//    public static String getArtistInfoString(@NonNull final Context context, @NonNull final Artist artist) {
//        int albumCount = artist.getAlbumCount();
//        int songCount = artist.getSongCount();
//        String albumString = albumCount == 1 ? context.getResources().getString(R.string.album) : context.getResources().getString(R.string.albums);
//        String songString = songCount == 1 ? context.getResources().getString(R.string.song) : context.getResources().getString(R.string.songs);
//        return albumCount + " " + albumString + " • " + songCount + " " + songString;
//    }
//
//    @NonNull
//    public static String getGenreInfoString(@NonNull final Context context, @NonNull final Genre genre) {
//        int songCount = genre.songCount;
//        String songString = songCount == 1 ? context.getResources().getString(R.string.song) : context.getResources().getString(R.string.songs);
//        return songCount + " " + songString;
//    }

	@NonNull
	public static String getPlaylistInfoString(@NonNull final Context context, @NonNull List<MusicItem> songs) {
		final long duration = getTotalDuration(context, songs);
		return MusicUtil.getSongCountString(context, songs.size()) + " • " + MusicUtil.getReadableDurationString(duration);
	}

	@NonNull
	public static String getSongCountString(@NonNull final Context context, int songCount) {
		final String songString = songCount == 1 ? context.getResources().getString(R.string.song) : context.getResources().getString(R.string.songs);
		return songCount + " " + songString;
	}

	@NonNull
	public static String getAlbumCountString(@NonNull final Context context, int albumCount) {
		final String albumString = albumCount == 1 ? context.getResources().getString(R.string.album) : context.getResources().getString(R.string.albums);
		return albumCount + " " + albumString;
	}

	@NonNull
	public static String getYearString(int year) {
		return year > 0 ? String.valueOf(year) : "-";
	}

	public static long getTotalDuration(@NonNull final Context context, @NonNull List<MusicItem> songs) {
		long duration = 0;
		for (int i = 0; i < songs.size(); i++) {
			duration += songs.get(i).getDuration();
		}
		return duration;
	}

	public static String getReadableDurationString(long songDurationMillis) {
		long minutes = (songDurationMillis / 1000) / 60;
		long seconds = (songDurationMillis / 1000) % 60;
		if (minutes < 60) {
			return String.format(Locale.getDefault(), "%01d:%02d", minutes, seconds);
		} else {
			long hours = minutes / 60;
			minutes = minutes % 60;
			return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
		}
	}

	//iTunes uses for example 1002 for track 2 CD1 or 3011 for track 11 CD3.
	//this method converts those values to normal tracknumbers
	public static int getFixedTrackNumber(int trackNumberToFix) {
		return trackNumberToFix % 1000;
	}

	public static void insertAlbumArt(@NonNull Context context, int albumId, String path) {
		ContentResolver contentResolver = context.getContentResolver();

		Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
		contentResolver.delete(ContentUris.withAppendedId(artworkUri, albumId), null, null);

		ContentValues values = new ContentValues();
		values.put("album_id", albumId);
		values.put("_data", path);

		contentResolver.insert(artworkUri, values);
	}

	public static void deleteAlbumArt(@NonNull Context context, int albumId) {
		ContentResolver contentResolver = context.getContentResolver();
		Uri localUri = Uri.parse("content://media/external/audio/albumart");
		contentResolver.delete(ContentUris.withAppendedId(localUri, albumId), null, null);
	}

	@NonNull
	public static File createAlbumArtFile() {
		return new File(createAlbumArtDir(), String.valueOf(System.currentTimeMillis()));
	}

	@NonNull
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static File createAlbumArtDir() {
		File albumArtDir = new File(Environment.getExternalStorageDirectory(), "/albumthumbs/");
		if (!albumArtDir.exists()) {
			albumArtDir.mkdirs();
			try {
				new File(albumArtDir, ".nomedia").createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return albumArtDir;
	}

//    public static void deleteTracks(@NonNull final Context context, @NonNull final List<MusicItem> songs) {
//        final String[] projection = new String[]{
//                BaseColumns._ID, MediaStore.MediaColumns.DATA
//        };
//        final StringBuilder selection = new StringBuilder();
//        selection.append(BaseColumns._ID + " IN (");
//        for (int i = 0; i < songs.size(); i++) {
//            selection.append(songs.get(i).getMusicID());
//            if (i < songs.size() - 1) {
//                selection.append(",");
//            }
//        }
//        selection.append(")");
//
//        try {
//            final Cursor cursor = context.getContentResolver().query(
//                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection.toString(),
//                    null, null);
//            if (cursor != null) {
//                // Step 1: Remove selected tracks from the current playlist, as well
//                // as from the album art cache
//                cursor.moveToFirst();
//                while (!cursor.isAfterLast()) {
//                    final int id = cursor.getInt(0);
//                    final Song song = SongLoader.getSong(context, id);
//                    MusicPlayerRemote.removeFromQueue(song);
//                    cursor.moveToNext();
//                }
//
//                // Step 2: Remove selected tracks from the database
//                context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                        selection.toString(), null);
//
//                // Step 3: Remove files from card
//                cursor.moveToFirst();
//                while (!cursor.isAfterLast()) {
//                    final String name = cursor.getString(1);
//                    try { // File.delete can throw a security exception
//                        final File f = new File(name);
//                        if (!f.delete()) {
//                            // I'm not sure if we'd ever get here (deletion would
//                            // have to fail, but no exception thrown)
//                            Log.e("MusicUtils", "Failed to delete file " + name);
//                        }
//                        cursor.moveToNext();
//                    } catch (@NonNull final SecurityException ex) {
//                        cursor.moveToNext();
//                    } catch (NullPointerException e) {
//                        Log.e("MusicUtils", "Failed to find file " + name);
//                    }
//                }
//                cursor.close();
//            }
//            context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
//            Toast.makeText(context, context.getString(R.string.deleted_x_songs, songs.size()), Toast.LENGTH_SHORT).show();
//        } catch (SecurityException ignored) {
//        }
//    }

	public static boolean isFavoritePlaylist(@NonNull final Context context, @NonNull final Playlist playlist) {
		return playlist.name != null && playlist.name.equals(context.getString(R.string.favorites));
	}

	@Nullable
	public static PlayListItem getFavoritesPlaylist(@NonNull final Context context) {
		return PlaylistLoader.getPlaylist(context, context.getString(R.string.favorites));
	}

	@Private
	private static PlayListItem getOrCreateFavoritesPlaylist(@NonNull final Context context) {
		return PlaylistLoader.getPlaylist(context, PlayListsUtil.createPlaylist(context, context.getString(R.string.favorites)));
	}

	public static boolean isFavorite(@NonNull final Context context, @NonNull final MusicItem song) {
		final PlayListItem item = getFavoritesPlaylist(context);
		if (item != null)
			return PlayListsUtil.doPlaylistContains(context, item.getId(), song.getMusicID());
		else return false;
	}

	public static void toggleFavorite(@NonNull final Context context, @Nullable final MusicItem song) {
		if (song != null && song.getMusicID() != -1) {
			if (isFavorite(context, song)) {
				final PlayListItem item = getFavoritesPlaylist(context);
				if (item != null) PlayListsUtil.removeFromPlaylist(context, song, item.getId());
			} else {
				PlayListsUtil.addToPlaylist(context, song, getOrCreateFavoritesPlaylist(context).getId(), false);
			}
		} else {
			Toast.makeText(context, "Add error, song is null or id == -1", Toast.LENGTH_SHORT).show();
			Log.d(TAG, "toggleFavorite: the song is null.");
		}
	}

//    public static boolean isArtistNameUnknown(@Nullable String artistName) {
//        if (TextUtils.isEmpty(artistName)) return false;
//        if (artistName.equals(Artist.UNKNOWN_ARTIST_DISPLAY_NAME)) return true;
//        artistName = artistName.trim().toLowerCase();
//        return artistName.equals("unknown") || artistName.equals("<unknown>");
//    }

	@NonNull
	public static String getSectionName(@Nullable String musicMediaTitle) {
		if (TextUtils.isEmpty(musicMediaTitle)) return "";
		musicMediaTitle = musicMediaTitle.trim().toLowerCase();
		if (musicMediaTitle.startsWith("the ")) {
			musicMediaTitle = musicMediaTitle.substring(4);
		} else if (musicMediaTitle.startsWith("a ")) {
			musicMediaTitle = musicMediaTitle.substring(2);
		}
		if (musicMediaTitle.isEmpty()) return "";
		return String.valueOf(musicMediaTitle.charAt(0)).toUpperCase();
	}

//    @Nullable
//    public static String getLyrics(Song song) {
//        String lyrics = null;
//
//        File file = new File(song.data);
//
//        try {
//            lyrics = AudioFileIO.read(file).getTagOrCreateDefault().getFirst(FieldKey.LYRICS);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        if (lyrics == null || lyrics.trim().isEmpty() || !AbsSynchronizedLyrics.isSynchronized(lyrics)) {
//            File dir = file.getAbsoluteFile().getParentFile();
//
//            if (dir != null && dir.exists() && dir.isDirectory()) {
//                String format = ".*%s.*\\.(lrc|txt)";
//                String filename = Pattern.quote(FileUtil.stripExtension(file.getName()));
//                String songtitle = Pattern.quote(song.title);
//
//                final ArrayList<Pattern> patterns = new ArrayList<>();
//                patterns.add(Pattern.compile(String.format(format, filename), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
//                patterns.add(Pattern.compile(String.format(format, songtitle), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
//
//                File[] files = dir.listFiles(f -> {
//                    for (Pattern pattern : patterns) {
//                        if (pattern.matcher(f.getName()).matches()) return true;
//                    }
//                    return false;
//                });
//
//                if (files != null && files.length > 0) {
//                    for (File f : files) {
//                        try {
//                            String newLyrics = FileUtil.read(f);
//                            if (newLyrics != null && !newLyrics.trim().isEmpty()) {
//                                if (AbsSynchronizedLyrics.isSynchronized(newLyrics)) {
//                                    return newLyrics;
//                                }
//                                lyrics = newLyrics;
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        }
//
//        return lyrics;
//    }

	@NonNull
	public static MusicItem getSongFromCursorImpl(@NonNull Cursor cursor) {
		final String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
		final int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
		final String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
		final String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
		final String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
		final int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
		final int size = (int) cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
		final String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
		final long addTime = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));
		final int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
		final int artistId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));

		final MusicItem.Builder builder = new MusicItem.Builder(id, name, path)
				.musicAlbum(albumName)
				.addTime(addTime)
				.artist(artist)
				.duration(duration)
				.mimeName(mimeType)
				.size(size)
				.addAlbumId(albumId)
				.addArtistId(artistId);
		return builder.build();
	}

	public synchronized static void deleteTracks(@NonNull final Context context, @NonNull final List<MusicItem> songs
			, @NonNull final BaseListActivity worker) {
		final String[] projection = new String[]{
				BaseColumns._ID, MediaStore.MediaColumns.DATA
		};

		final StringBuilder selection = new StringBuilder();
		selection.append(BaseColumns._ID + " IN (");
		for (int i = 0; i < songs.size(); i++) {
			selection.append(songs.get(i).getMusicID());
			if (i < songs.size() - 1) {
				selection.append(",");
			}
		}
		selection.append(")");

		try {
			final Cursor cursor = context.getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection.toString(),
					null, null);
			if (cursor != null) {
				// Step 1: Remove selected tracks from the current playlist, as well
				// as from the album art cache
				cursor.moveToFirst();

				do {
					final int id = cursor.getInt(0);
					Cursor cursor1 = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
							null, MediaStore.Audio.AudioColumns._ID + "=?"
							, new String[]{String.valueOf(id)}, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
					List<MusicItem> items = new ArrayList<>();
					if (cursor1 != null && cursor1.moveToFirst()) {
						do {
							MusicItem item = MusicUtil.getSongFromCursorImpl(cursor1);
							Log.d(TAG, "deleteTracks: count: " + cursor1.getCount()
									+ "   "
									+ item.getMusicPath());
							items.add(item);
						} while (cursor1.moveToNext());
						cursor1.close();
					}

					for (final MusicItem item : items) {
						Data.sPlayOrderList.remove(item);
						Data.sPlayOrderListBackup.remove(item);
						worker.removeItem(item);
						LitePal.deleteAll(Detail.class, "musicId=?", String.valueOf(item.getMusicID()));
						try {
							Data.sMusicBinder.removeFromOrderList(item);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				} while (cursor.moveToNext());

				// Step 2: Remove selected tracks from the database
				context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
						selection.toString(), null);

				// Step 3: Remove files from card
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					final String name = cursor.getString(1);
					try { // File.delete can throw a security exception
						final File f = new File(name);
						if (!f.delete()) {
							// I'm not sure if we'd ever get here (deletion would
							// have to fail, but no exception thrown)
							Log.e("MusicUtils", "Failed to delete file " + name);
						}
						cursor.moveToNext();
					} catch (@NonNull final SecurityException ex) {
						cursor.moveToNext();
					} catch (NullPointerException e) {
						Log.e("MusicUtils", "Failed to find file " + name);
					}
				}
				cursor.close();
			}
			context.getContentResolver().notifyChange(Uri.parse("content://media"), null);

			// TODO: 2019/6/7 统一使用 worker
			// final reload music item in music list fragment
			worker.sendEmptyMessage(MessageWorker.RELOAD);

//			Toast.makeText(context, context.getString(R.string.deleted_x_songs, songs.size()), Toast.LENGTH_SHORT).show();
		} catch (SecurityException ignored) {
		}
	}

	/**
	 * @see #addToBlackList(List)
	 */
	public synchronized static void addToBlackList(@Nullable MusicItem item) {
		final List<MusicItem> items = new ArrayList<>();
		items.add(item);
		addToBlackList(items);
	}

	/**
	 * add music item to black list
	 */
	public synchronized static void addToBlackList(@Nullable List<MusicItem> items) {
		if (items == null || items.size() == 0) return;

		final LitePalDB blackList = new LitePalDB("BlackList", App.BLACK_LIST_VERSION);
		blackList.addClassName(MyBlackPath.class.getName());
		LitePal.use(blackList);

		for (final MusicItem item : items) {
			if (item.getMusicID() == -1) continue;

			final MyBlackPath blackPath = new MyBlackPath();
			blackPath.setDirPath(item.getMusicPath());
			blackPath.save();
		}

		LitePal.useDefault();
	}

	@NonNull
	public static Intent createShareSongFileIntent(@NonNull final MusicItem song, Context context) {
		try {
			return new Intent()
					.setAction(Intent.ACTION_SEND)
					.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName(), new File(song.getMusicPath())))
					.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
					.setType("audio/*");
		} catch (IllegalArgumentException e) {
			// TODO the path is most likely not like /storage/emulated/0/... but something like /storage/28C7-75B0/...
			e.printStackTrace();
			Toast.makeText(context, "Could not share this file, I'm aware of the issue.", Toast.LENGTH_SHORT).show();
			return new Intent();
		}
	}
}
