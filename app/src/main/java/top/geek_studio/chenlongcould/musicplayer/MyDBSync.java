package top.geek_studio.chenlongcould.musicplayer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;

import org.litepal.LitePal;

import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.database.CustomAlbumPath;

public class MyDBSync extends IntentService {

    private static final String ACTON_SYNC_ALBUM = "top.geek_studio.chenlongcould.musicplayer.action.SyncAlbum";

    public static int CURRENT_POSITION = -1;

    public static int TOTAL_COUNT = -1;

    public static boolean WORKING = false;

    public MyDBSync() {
        super("MyDBSync");
    }

    public static void startActionSyncAlbum(Context context) {
        Intent intent = new Intent(context, MyDBSync.class);
        intent.setAction(ACTON_SYNC_ALBUM);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        WORKING = true;
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTON_SYNC_ALBUM.equals(action)) {
                handleActionSyncAlbum();
            }
        }
    }

    private void handleActionSyncAlbum() {
        final Cursor cursor = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
        if (cursor != null && cursor.moveToFirst()) {
            TOTAL_COUNT = cursor.getCount();
            LitePal.useDefault();
            List<CustomAlbumPath> customAlbumPaths = LitePal.findAll(CustomAlbumPath.class);
            do {
                CURRENT_POSITION++;

                int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID));

                //如果大于则增加否则进行检测
                if (CURRENT_POSITION > customAlbumPaths.size() - 1) {
                    CustomAlbumPath customAlbumPath = new CustomAlbumPath();
                    customAlbumPath.setAlbumId(albumId);
                    LitePal.useDefault();
                    customAlbumPath.save();
                } else {
                    if (customAlbumPaths.get(CURRENT_POSITION).getAlbumId() != albumId) {
                        CustomAlbumPath customAlbumPath = LitePal.find(CustomAlbumPath.class, CURRENT_POSITION);
                        if (customAlbumPath != null) {
                            customAlbumPath.setAlbumId(albumId);
                            LitePal.useDefault();
                            customAlbumPath.save();
                        }
                    }
                }

            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WORKING = false;
    }
}
