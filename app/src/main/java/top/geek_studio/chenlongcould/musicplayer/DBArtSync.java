package top.geek_studio.chenlongcould.musicplayer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import org.litepal.LitePal;

import top.geek_studio.chenlongcould.musicplayer.database.ArtistArtPath;
import top.geek_studio.chenlongcould.musicplayer.database.CustomAlbumPath;

public final class DBArtSync extends IntentService {

    private static final String TAG = "DBArtSync";

    private static final String ACTON_SYNC_ALBUM = "top.geek_studio.chenlongcould.musicplayer.action.SyncAlbum";

    private static final String ACTON_SYNC_ARTIST = "top.geek_studio.chenlongcould.musicplayer.action.SyncArtist";

    public static int CURRENT_POSITION_ALBUM = 0;

    public static int CURRENT_POSITION_ARTIST = 0;

//    public static int TOTAL_COUNT = -1;

    public static boolean WORKING = false;

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
        WORKING = true;
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTON_SYNC_ALBUM.equals(action)) {
                Log.d(TAG, "onHandleIntent: do album " + Thread.currentThread().getName());
                handleActionSyncAlbum();
            }

            if (ACTON_SYNC_ARTIST.equals(action)) {
                Log.d(TAG, "onHandleIntent: do artist " + Thread.currentThread().getName());
                handleActionSyncArtist();
            }
        }
    }

    private void handleActionSyncAlbum() {
        final Cursor cursor = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
        if (cursor != null && cursor.moveToFirst()) {
            LitePal.useDefault();
//            List<CustomAlbumPath> customAlbumPaths = LitePal.findAll(CustomAlbumPath.class);

            do {
                CURRENT_POSITION_ALBUM++;

                int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID));

                if (LitePal.where("mAlbumId = ?", String.valueOf(albumId)).find(CustomAlbumPath.class).size() == 0) {
                    Log.d(TAG, "handleActionSyncAlbum: not exists... add albumId: " + albumId);
                    CustomAlbumPath customAlbumPath = new CustomAlbumPath();
                    customAlbumPath.setAlbumId(albumId);
                    customAlbumPath.save();
                } else {
                    Log.d(TAG, "handleActionSyncAlbum: albumId: " + albumId + " exists...");
                }

//                if (CURRENT_POSITION_ALBUM > customAlbumPaths.size()) {
//                    CustomAlbumPath customAlbumPath = new CustomAlbumPath();
//                    customAlbumPath.setAlbumId(albumId);
//                    LitePal.useDefault();
//                    customAlbumPath.save();
//                    Log.d(TAG, "handleActionSyncAlbum:(the DEF_DB size > CUSTOM_ALBUM_DB size) add: albumId: " + albumId + " size: " + customAlbumPaths.size());
//                } else {
//                    if (customAlbumPaths.get(CURRENT_POSITION_ALBUM - 1).getAlbumId() != albumId) {
//                        CustomAlbumPath customAlbumPath = LitePal.find(CustomAlbumPath.class, CURRENT_POSITION_ALBUM);
//                        if (customAlbumPath != null) {
//                            customAlbumPath.setAlbumId(albumId);
//                            LitePal.useDefault();
//                            customAlbumPath.save();
//                            Log.d(TAG, "handleActionSyncAlbum: sync fix: " + albumId);
//                        }
//                    } else {
//                        Log.d(TAG, "handleActionSyncAlbum: syncing: " + albumId);
//                    }
//                }

            } while (cursor.moveToNext());
            cursor.close();
            WORKING = false;
        }
    }

    private void handleActionSyncArtist() {
        final Cursor cursor = getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);
        if (cursor != null && cursor.moveToFirst()) {
            LitePal.useDefault();
            do {
                CURRENT_POSITION_ARTIST++;

                int artistId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID));

                if (LitePal.where("mArtistId = ?", String.valueOf(artistId)).find(ArtistArtPath.class).size() == 0) {
                    Log.d(TAG, "handleActionSyncAlbum: not exists... add artist: " + artistId);
                    ArtistArtPath artistArtPath = new ArtistArtPath();
                    artistArtPath.setArtistId(artistId);
                    artistArtPath.save();
                } else {
                    Log.d(TAG, "handleActionSyncAlbum: mArtistId: " + artistId + " exists...");
                }

            } while (cursor.moveToNext());
            cursor.close();
            WORKING = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WORKING = false;
    }
}
