package top.geek_studio.chenlongcould.musicplayer.activity.main;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import top.geek_studio.chenlongcould.geeklibrary.GCSutil;
import top.geek_studio.chenlongcould.geeklibrary.GeekProject;
import top.geek_studio.chenlongcould.geeklibrary.PackageTool;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.base.BaseCompatActivity;
import top.geek_studio.chenlongcould.musicplayer.database.DataModel;
import top.geek_studio.chenlongcould.musicplayer.fragment.AlbumListFragment;
import top.geek_studio.chenlongcould.musicplayer.fragment.ArtistListFragment;
import top.geek_studio.chenlongcould.musicplayer.model.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.model.ArtistItem;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.threadPool.CustomThreadPool;
import top.geek_studio.chenlongcould.musicplayer.utils.PreferenceUtil;

/**
 * MAIN (P)
 *
 * @author : chenlongcould
 * @date : 2019/06/15/08
 */
public class MainPresenter implements MainContract.Presenter {

	private static final String TAG = "MainPresenter";

	private final MainContract.View mView;

	private SharedPreferences preferences;

	private DataModel dataModel;

	MainPresenter(MainContract.View mView) {
		this.mView = mView;
		mView.setPresenter(this);
		preferences = PreferenceUtil.getDefault(mView.getContext());
		dataModel = mView.getDataModel();
	}

	@Override
	public void start() {
		// none
	}

	@Override
	public void checkUpdate(@NonNull final BaseCompatActivity activity) {
		CustomThreadPool.post(() -> GCSutil.checkUpdate(activity, GeekProject.ACG_Player
				, PackageTool.getVerCode(activity)));
	}

	@Override
	public void initPermission(@NonNull MainActivity activity) {
		if (ContextCompat.checkSelfPermission(activity,
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}
					, Values.PermissionCode.REQUEST_WRITE_EXTERNAL_STORAGE);
		} else {
			initData(activity);
		}
	}

	@Override
	public void initData(@NonNull MainActivity activity) {
//		DBArtSync.startActionSyncArtist(activity);
//		DBArtSync.startActionSyncAlbum(activity);

		mView.initFragmentData();
	}

	@Override
	public void filterData(String key) {
		final String tabOrder = preferences.getString(Values.SharedPrefsTag.CUSTOM_TAB_LAYOUT
				, MainActivity.DEFAULT_TAB_ORDER);

		if (tabOrder.charAt(MainActivity.CURRENT_PAGE_INDEX) == MainActivity.MUSIC_LIST_FRAGMENT_ID) {
			if (TextUtils.isEmpty(key)) {
				dataModel.mMusicItems.clear();
				dataModel.mMusicItems.addAll(dataModel.mMusicItemsBackUp);

				mView.notifyAdapter(MainActivity.MUSIC_LIST_FRAGMENT_ID);

			} else {
				dataModel.mMusicItems.clear();

				//algorithm
				for (final MusicItem item : dataModel.mMusicItemsBackUp) {
					final String name = item.getMusicName();
					if (name.toLowerCase().contains(key.toLowerCase())) {
						Log.d(TAG, "filterData: ------- " + name);
						dataModel.mMusicItems.add(item);
					}
				}
				mView.notifyAdapter(MainActivity.MUSIC_LIST_FRAGMENT_ID);
			}

		}

		if (tabOrder.charAt(MainActivity.CURRENT_PAGE_INDEX) == MainActivity.ALBUM_LIST_FRAGMENT_ID) {
			final AlbumListFragment albumListFragment =
					((AlbumListFragment) mView.getFragment(MainActivity.ALBUM_LIST_FRAGMENT_ID));

			if (TextUtils.isEmpty(key)) {
				albumListFragment.getAlbumItemList().clear();
				albumListFragment.getAlbumItemList().addAll(albumListFragment.getAlbumItemListBackup());
				mView.notifyAdapter(MainActivity.ALBUM_LIST_FRAGMENT_ID);
			} else {
				albumListFragment.getAlbumItemList().clear();

				//algorithm
				for (final AlbumItem item : albumListFragment.getAlbumItemListBackup()) {
					final String name = item.getAlbumName();
					if (name.toLowerCase().contains(key.toLowerCase())) {
						albumListFragment.getAlbumItemList().add(item);
					}
				}

				mView.notifyAdapter(MainActivity.ALBUM_LIST_FRAGMENT_ID);
			}
		}

		//artist
		if (tabOrder.charAt(MainActivity.CURRENT_PAGE_INDEX) == '3') {
			final ArtistListFragment artistListFragment =
					((ArtistListFragment) mView.getFragment(MainActivity.ARTIST_LIST_FRAGMENT_ID));

			if (TextUtils.isEmpty(key)) {
				artistListFragment.getArtistItemList().clear();
				artistListFragment.getArtistItemList().addAll(artistListFragment.getArtistItemListBackup());

				mView.notifyAdapter(MainActivity.ARTIST_LIST_FRAGMENT_ID);
			} else {
				artistListFragment.getArtistItemList().clear();

				//algorithm
				for (ArtistItem item : artistListFragment.getArtistItemListBackup()) {
					String name = item.getArtistName();
					if (name.contains(key.toLowerCase()) || name.contains(key.toUpperCase())) {
						artistListFragment.getArtistItemList().add(item);
					}
				}

				mView.notifyAdapter(MainActivity.ARTIST_LIST_FRAGMENT_ID);
			}
		}
	}
}
