package top.geek_studio.chenlongcould.musicplayer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import org.litepal.LitePal;

import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.database.ArtistArtPath;
import top.geek_studio.chenlongcould.musicplayer.database.CustomAlbumPath;

public class MyDBAlbumSync extends IntentService {

    private static final String TAG = "MyDBAlbumSync";

    private static final String ACTON_SYNC_ALBUM = "top.geek_studio.chenlongcould.musicplayer.action.SyncAlbum";

    private static final String ACTON_SYNC_ARTIST = "top.geek_studio.chenlongcould.musicplayer.action.SyncArtist";

    public static int CURRENT_POSITION_ALBUM = -1;

    public static int CURRENT_POSITION_ARTIST = -1;

//    public static int TOTAL_COUNT = -1;

    public static boolean WORKING = false;

    public MyDBAlbumSync() {
        super("MyDBAlbumSync");
    }

    /**
     * @see #ACTON_SYNC_ALBUM
     */
    public static void startActionSyncAlbum(Context context) {
        Intent intent = new Intent(context, MyDBAlbumSync.class);
        intent.setAction(ACTON_SYNC_ALBUM);
        context.startService(intent);
    }

    /**
     * @see #ACTON_SYNC_ARTIST
     */
    public static void startActionSyncArtist(Context context) {
        Intent intent = new Intent(context, MyDBAlbumSync.class);
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
//            TOTAL_COUNT = cursor.getCount();
            LitePal.useDefault();
            List<CustomAlbumPath> customAlbumPaths = LitePal.findAll(CustomAlbumPath.class);
            do {
                CURRENT_POSITION_ALBUM++;

                int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID));

                //如果大于则增加否则进行检测
                if (CURRENT_POSITION_ALBUM > customAlbumPaths.size() - 1) {
                    CustomAlbumPath customAlbumPath = new CustomAlbumPath();
                    customAlbumPath.setAlbumId(albumId);
                    LitePal.useDefault();
                    customAlbumPath.save();
                    Log.d(TAG, "handleActionSyncAlbum:(the DEF_DB size > CUSTOM_ALBUM_DB size) add: albumId: " + albumId);
                } else {
                    if (customAlbumPaths.get(CURRENT_POSITION_ALBUM).getAlbumId() != albumId) {
                        CustomAlbumPath customAlbumPath = LitePal.find(CustomAlbumPath.class, CURRENT_POSITION_ALBUM);
                        if (customAlbumPath != null) {
                            customAlbumPath.setAlbumId(albumId);
                            LitePal.useDefault();
                            customAlbumPath.save();
                            Log.d(TAG, "handleActionSyncAlbum: sync fix: " + albumId);
                        }
                    } else {
                        Log.d(TAG, "handleActionSyncAlbum: syncing: " + albumId);
                    }
                }

            } while (cursor.moveToNext());
            cursor.close();
            WORKING = false;
        }
    }

    private void handleActionSyncArtist() {
        final Cursor cursor = getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);
        if (cursor != null && cursor.moveToFirst()) {
//            TOTAL_COUNT = cursor.getCount();
            LitePal.useDefault();
            List<ArtistArtPath> artistArtPaths = LitePal.findAll(ArtistArtPath.class);
            do {
                CURRENT_POSITION_ARTIST++;

                int artistId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID));

                //如果大于则增加否则进行检测
                if (CURRENT_POSITION_ARTIST > artistArtPaths.size() - 1) {
                    ArtistArtPath artistArtPath = new ArtistArtPath();
                    artistArtPath.setArtistId(artistId);
                    LitePal.useDefault();
                    artistArtPath.save();
                    Log.d(TAG, "handleActionSyncArtist:(the DEF_DB size > CUSTOM_ARTIST_DB size) add: albumId: " + artistId);
                } else {
                    if (artistArtPaths.get(CURRENT_POSITION_ARTIST).getArtistId() != artistId) {
                        ArtistArtPath artistArtPath = LitePal.find(ArtistArtPath.class, CURRENT_POSITION_ARTIST);
                        if (artistArtPath != null) {
                            artistArtPath.setArtistId(artistId);
                            LitePal.useDefault();
                            artistArtPath.save();
                            Log.d(TAG, "handleActionSyncAlbum: sync fix: " + artistId);
                        }
                    } else {
                        Log.d(TAG, "handleActionSyncArtist: syncing: " + artistId);
                    }
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
