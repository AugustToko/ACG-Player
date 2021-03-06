package top.geek_studio.chenlongcould.musicplayer.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.PlaylistsColumns;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import top.geek_studio.chenlongcould.musicplayer.model.PlayListItem;

import java.util.ArrayList;

/**
 * @author kabouzeid
 */
public class PlaylistLoader {

	@NonNull
	public static ArrayList<PlayListItem> getAllPlaylists(@NonNull final Context context) {
		return getAllPlaylists(makePlaylistCursor(context, null, null));
	}

	@NonNull
	public static PlayListItem getPlaylist(@NonNull final Context context, final int playlistId) {
		return getPlaylist(makePlaylistCursor(
				context,
				BaseColumns._ID + "=?",
				new String[]{
						String.valueOf(playlistId)
				}
		));
	}

	@Nullable
	public static PlayListItem getPlaylist(@NonNull final Context context, final String playlistName) {
		return getPlaylist(makePlaylistCursor(
				context,
				PlaylistsColumns.NAME + "=?",
				new String[]{
						playlistName
				}
		));
	}

	public static PlayListItem getPlaylist(@Nullable final Cursor cursor) {
		PlayListItem playlist = null;

		if (cursor != null && cursor.moveToFirst()) {
			playlist = getPlaylistFromCursorImpl(cursor);
		}
		if (cursor != null)
			cursor.close();
		return playlist;
	}

	@NonNull
	public static ArrayList<PlayListItem> getAllPlaylists(@Nullable final Cursor cursor) {
		ArrayList<PlayListItem> playlists = new ArrayList<>();

		if (cursor != null && cursor.moveToFirst()) {
			do {
				playlists.add(getPlaylistFromCursorImpl(cursor));
			} while (cursor.moveToNext());
		}
		if (cursor != null)
			cursor.close();
		return playlists;
	}

	@NonNull
	private static PlayListItem getPlaylistFromCursorImpl(@NonNull final Cursor cursor) {
		final int id = cursor.getInt(0);
		final String name = cursor.getString(1);
		return new PlayListItem(id, name);
	}

	@Nullable
	public static Cursor makePlaylistCursor(@NonNull final Context context, final String selection, final String[] values) {
		try {
			return context.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
					new String[]{
							/* 0 */
							BaseColumns._ID,
							/* 1 */
							PlaylistsColumns.NAME
					}, selection, values, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
		} catch (SecurityException e) {
			return null;
		}
	}
}