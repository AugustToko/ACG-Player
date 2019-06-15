package top.geek_studio.chenlongcould.musicplayer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;
import androidx.core.content.ContextCompat;
import org.litepal.LitePal;
import top.geek_studio.chenlongcould.musicplayer.database.ArtistArtPath;
import top.geek_studio.chenlongcould.musicplayer.database.CustomAlbumPath;

/**
 * @author chenlongcould
 */
public final class DBArtSync extends IntentService {

	private static final String TAG = "DBArtSync";

	private static final String ACTON_SYNC_ALBUM = "top.geek_studio.chenlongcould.musicplayer.action.SyncAlbum";

	private static final String ACTON_SYNC_ARTIST = "top.geek_studio.chenlongcould.musicplayer.action.SyncArtist";

	public DBArtSync() {
		super("DBArtSync");
	}

	/**
	 * @see #ACTON_SYNC_ALBUM
	 */
	public static void startActionSyncAlbum(Context context) {
		Intent intent = new Intent(context, DBArtSync.class);
		intent.setAction(ACTON_SYNC_ALBUM);
		context.startService(intent);
	}

	/**
	 * @see #ACTON_SYNC_ARTIST
	 */
	public static void startActionSyncArtist(Context context) {
		Intent intent = new Intent(context, DBArtSync.class);
		intent.setAction(ACTON_SYNC_ARTIST);
		context.startService(intent);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			final String action = intent.getAction();
			if (ACTON_SYNC_ALBUM.equals(action)) {
				handleActionSyncAlbum();
			}

			if (ACTON_SYNC_ARTIST.equals(action)) {
				handleActionSyncArtist();
			}
		}
	}

	private void handleActionSyncAlbum() {
		if (ContextCompat.checkSelfPermission(this,
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
			final Cursor cursor = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
			LitePal.useDefault();
			if (cursor != null && cursor.moveToFirst()) {
				if (LitePal.findAll(CustomAlbumPath.class).size() == cursor.getCount()) {
					Log.d(TAG, "handleActionSyncAlbum: count is same return");
					return;
				} else {
					Log.d(TAG, "handleActionSyncAlbum: not same, clear");
					Log.d(TAG, "handleActionSyncAlbum: size: " + LitePal.findAll(CustomAlbumPath.class).size());
					LitePal.deleteAll(CustomAlbumPath.class);
					do {
						int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID));
//					Log.d(TAG, "handleActionSyncAlbum: add albumId: " + albumId);
						CustomAlbumPath customAlbumPath = new CustomAlbumPath();
						customAlbumPath.setAlbumId(albumId);
						customAlbumPath.save();
					} while (cursor.moveToNext());
				}
				cursor.close();
			}
		}

	}

	private void handleActionSyncArtist() {
		if (ContextCompat.checkSelfPermission(this,
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
			final Cursor cursor = getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);
			if (cursor != null && cursor.moveToFirst()) {
				LitePal.useDefault();
				if (LitePal.findAll(ArtistArtPath.class).size() == cursor.getCount()) {
					Log.d(TAG, "handleActionSyncArtist: count is same return");
					return;
				} else {
					Log.d(TAG, "handleActionSyncArtist: not same, clear all");
					Log.d(TAG, "handleActionSyncArtist: size: " + LitePal.findAll(ArtistArtPath.class).size());
					LitePal.deleteAll(ArtistArtPath.class);
					do {
						int artistId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID));
//					Log.d(TAG, "handleActionSyncAlbum: add artist: " + artistId);
						ArtistArtPath artistArtPath = new ArtistArtPath();
						artistArtPath.setArtistId(artistId);
						artistArtPath.save();
					} while (cursor.moveToNext());
				}
				cursor.close();
			}
		}

	}
}
