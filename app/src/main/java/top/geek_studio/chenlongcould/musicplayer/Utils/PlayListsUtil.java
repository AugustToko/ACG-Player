/*
 * ************************************************************
 * 文件：PlayListsUtil.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月05日 09:52:36
 * 上次修改时间：2019年01月05日 09:50:17
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.Models.PlayListItem;

import static android.provider.MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;

public final class PlayListsUtil {

    public static final String DEFAULT_LIST = "Default";

    /**
     * doesPlaylistExist, search by id
     *
     * @param context    context
     * @param playlistId music play list id
     */
    public static boolean doesPlaylistExist(@NonNull final Context context, final int playlistId) {
        return playlistId != -1 && doesPlaylistExist(context, MediaStore.Audio.Playlists._ID + "=?", new String[]{String.valueOf(playlistId)});
    }

    /**
     * doesPlaylistExist, search by name
     *
     * @param context context
     * @param name    music play list name
     */
    public static boolean doesPlaylistExist(@NonNull final Context context, final String name) {
        return doesPlaylistExist(context, MediaStore.Audio.PlaylistsColumns.NAME + "=?", new String[]{name});
    }

    private static boolean doesPlaylistExist(@NonNull Context context, @NonNull final String selection, @NonNull final String[] values) {
        Cursor cursor = context.getContentResolver().query(EXTERNAL_CONTENT_URI, new String[]{}, selection, values, null);

        boolean exists = false;
        if (cursor != null) {
            exists = cursor.getCount() != 0;
            cursor.close();
        }
        return exists;
    }

    /**
     * create play list
     *
     * @param context context
     * @param name    music play list name
     * @return return the list id that yuo created
     */
    public static int createPlaylist(@NonNull final Context context, @Nullable final String name) {
        int id = -1;
        if (name != null && name.length() > 0) {
            try {
                Cursor cursor = context.getContentResolver().query(EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Playlists._ID}, MediaStore.Audio.PlaylistsColumns.NAME + "=?", new String[]{name}, null);
                if (cursor == null || cursor.getCount() < 1) {
                    final ContentValues values = new ContentValues(1);

                    //add
                    values.put(MediaStore.Audio.PlaylistsColumns.NAME, name);

                    final Uri uri = context.getContentResolver().insert(EXTERNAL_CONTENT_URI, values);
                    if (uri != null) {
                        // Necessary because somehow the MediaStoreObserver is not notified when adding a playlist
                        context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
                        String temp = uri.getLastPathSegment();
                        if (temp != null && temp.length() > 0) {
                            id = Integer.parseInt(uri.getLastPathSegment());
                            Toast.makeText(context, "PlayList: " + name + " id: " + id, Toast.LENGTH_SHORT).show();
                        } else return -1;
                    } else return -1;
                } else {
                    // Playlist exists
                    if (cursor.moveToFirst()) {
                        id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SecurityException ignored) {
            }
        }
        if (id == -1) {
            Toast.makeText(context, "Error, msg: id == -1", Toast.LENGTH_SHORT).show();
        }
        return id;
    }

    /**
     * del list
     *
     * @param context   context
     * @param playlists playList that will del
     */
    public static void deletePlaylists(@NonNull final Context context, @NonNull final ArrayList<PlayListItem> playlists) {
        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Playlists._ID + " IN (");
        for (int i = 0; i < playlists.size(); i++) {
            selection.append(playlists.get(i).getId());
            if (i < playlists.size() - 1) {
                selection.append(",");
            }
        }
        selection.append(")");
        try {
            context.getContentResolver().delete(EXTERNAL_CONTENT_URI, selection.toString(), null);
            context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
        } catch (SecurityException ignored) {
        }
    }

    public static void addToPlaylist(@NonNull final Context context, final MusicItem song, final int playlistId, final boolean showToastOnFinish) {
        List<MusicItem> helperList = new ArrayList<>();
        helperList.add(song);
        addToPlaylist(context, helperList, playlistId, showToastOnFinish);
    }

    public static void addToPlaylist(@NonNull final Context context, @NonNull final List<MusicItem> songs, final int playlistId, final boolean showToastOnFinish) {
        final int size = songs.size();
        final ContentResolver resolver = context.getContentResolver();
        final String[] projection = new String[]{
                "max(" + MediaStore.Audio.Playlists.Members.PLAY_ORDER + ")",
        };
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        Cursor cursor = null;
        int base = 0;

        try {
            try {
                cursor = resolver.query(uri, projection, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    base = cursor.getInt(0) + 1;
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            int numInserted = 0;
            for (int offSet = 0; offSet < size; offSet += 1000)
                numInserted += resolver.bulkInsert(uri, makeInsertItems(songs, offSet, 1000, base));

            if (showToastOnFinish) {
                Toast.makeText(context, "context.getResources().getString(R.string.inserted_x_songs_into_playlist_x, numInserted, getNameForPlaylist(context, playlistId))", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException ignored) {
        }
    }

    @NonNull
    public static ContentValues[] makeInsertItems(@NonNull final List<MusicItem> songs, final int offset, int len, final int base) {
        if (offset + len > songs.size()) {
            len = songs.size() - offset;
        }

        ContentValues[] contentValues = new ContentValues[len];

        for (int i = 0; i < len; i++) {
            contentValues[i] = new ContentValues();
            contentValues[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + offset + i);
            contentValues[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songs.get(offset + i).getMusicID());
        }
        return contentValues;
    }

    public static String getNameForPlaylist(@NonNull final Context context, final long id) {
        try {
            Cursor cursor = context.getContentResolver().query(EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.PlaylistsColumns.NAME},
                    BaseColumns._ID + "=?",
                    new String[]{String.valueOf(id)},
                    null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        return cursor.getString(0);
                    }
                } finally {
                    cursor.close();
                }
            }
        } catch (SecurityException ignored) {

        }
        return "";
    }

    public static void removeFromPlaylist(@NonNull final Context context, @NonNull final MusicItem song, int playlistId) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
                "external", playlistId);
        String selection = MediaStore.Audio.Playlists.Members.AUDIO_ID + " =?";
        String[] selectionArgs = new String[]{String.valueOf(song.getMusicID())};

        try {
            context.getContentResolver().delete(uri, selection, selectionArgs);
        } catch (SecurityException ignored) {
        }
    }

    public static void renamePlaylist(@NonNull final Context context, final long id, final String newName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Audio.PlaylistsColumns.NAME, newName);
        try {
            context.getContentResolver().update(EXTERNAL_CONTENT_URI,
                    contentValues,
                    MediaStore.Audio.Playlists._ID + "=?",
                    new String[]{String.valueOf(id)});
            context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
        } catch (SecurityException ignored) {
        }
    }

}
