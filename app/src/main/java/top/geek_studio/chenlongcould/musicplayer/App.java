package top.geek_studio.chenlongcould.musicplayer;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import io.reactivex.disposables.Disposable;
import org.litepal.LitePal;
import top.geek_studio.chenlongcould.geeklibrary.theme.ThemeStore;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.database.ArtistArtPath;
import top.geek_studio.chenlongcould.musicplayer.database.CustomAlbumPath;
import top.geek_studio.chenlongcould.musicplayer.fragment.MusicDetailFragment;
import top.geek_studio.chenlongcould.musicplayer.utils.PreferenceUtil;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author chenlongcould
 */
public final class App extends Application {
	
	public static final String SHORT_CUT_ID_1 = "id1";
	public static final String LAST_FM_KEY = "726e129841377374f2c8c804facb6d11";
	public static final String MY_WEB_SITE = "http://www.geek-cloud.top/";
	public static final String PRIVACY_POLICY_URL = "https://github.com/AugustToko/ACG-Player/blob/master/Privacy%20policy.md";
	public static final String ISSUE_URL = "https://github.com/AugustToko/ACG-Player/issues/new/choose";
	public static final String VERSION_CODE = "ver_code";
	public static final int VER_CODE = 108;
	public static final String SHORTCUT_RANDOM = "SHORTCUT_RANDOM";
	private static final String TAG = "App";
	
	private static final String PKG_1 = "top.geek_studio.chenlongcould.musicplayer";
	
	private static final String PKG_SUFFIX = ".Common";

	public static final int BLACK_LIST_VERSION = 1;
	
	private ShortcutManager mShortcutManager;
	
	/**
	 * @see Disposable#dispose()
	 */
	public static void clearDisposable() {
		for (Disposable disposable : Data.sDisposables) {
			if (disposable != null && !disposable.isDisposed()) {
				disposable.dispose();
			}
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		LitePal.initialize(this);
		
		String processName = getProcessName(this);

		Log.d(TAG, "onCreate: " + processName);

		if (processName != null && !processName.contains("remote")) {

			// 升级版本清空数据
			if (PreferenceUtil.getDefault(this).getInt(VERSION_CODE, 0) != VER_CODE) {
				Log.d(TAG, "onCreate: clear data");

				final File file = getExternalFilesDir(ThemeStore.DIR_NAME);
				if (file != null && file.exists()) {
					Utils.IO.delFolder(file.getAbsolutePath());
				}

				//add version code
				final SharedPreferences.Editor verEdit = PreferenceUtil.getDefault(this).edit();
				verEdit.putLong(VERSION_CODE, VER_CODE);
				verEdit.apply();

				LitePal.deleteAll(CustomAlbumPath.class);
				LitePal.deleteAll(ArtistArtPath.class);


			}

			MusicDetailFragment.BackgroundStyle.DETAIL_BACKGROUND = PreferenceManager.getDefaultSharedPreferences(this)
					.getString(Values.SharedPrefsTag.DETAIL_BG_STYLE, MusicDetailFragment.BackgroundStyle.STYLE_BACKGROUND_BLUR);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
				mShortcutManager = getSystemService(ShortcutManager.class);
				getNewShortcutInfo();
			}
		}

	}
	
	/**
	 * 获取进程名。
	 * 由于app是一个多进程应用，因此每个进程被os创建时，
	 * onCreate()方法均会被执行一次，
	 * 进行辨别初始化，针对特定进程进行相应初始化工作，
	 * 此方法可以提高一半启动时间。
	 *
	 * @param context 上下文环境对象
	 *
	 * @return 获取此进程的进程名
	 */
	@Nullable
	private String getProcessName(@NonNull Context context) {
		final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
		if (runningAppProcesses == null) {
			return null;
		}
		
		for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
			if (runningAppProcess.pid == android.os.Process.myPid() && !TextUtils.isEmpty(runningAppProcess.processName)) {
				return runningAppProcess.processName;
			}
		}
		return null;
	}
	
	@Override
	public void onTrimMemory(int level) {
		Log.d(TAG, "onTrimMemory: the level is " + level);
		
		if (PKG_1.equals(getProcessName(this))
				|| "top.geek_studio.chenlongcould.musicplayer.Common".equals(getProcessName(this))) {
			
			if (level == TRIM_MEMORY_MODERATE) {
				Data.S_TRASH_CAN_LIST.clear();
				
			}
			
			GlideApp.get(this).trimMemory(level);
		}
		
		super.onTrimMemory(level);
	}
	
	/**
	 * 动态添加
	 */
	@RequiresApi(api = Build.VERSION_CODES.N_MR1)
	private void getNewShortcutInfo() {
		
		Intent randomPlay = new Intent(this, MainActivity.class);
		randomPlay.setAction(Intent.ACTION_MAIN);
		randomPlay.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		randomPlay.putExtra("SHORTCUT_TYPE", SHORTCUT_RANDOM);
		
		ShortcutInfo shortcut = new ShortcutInfo.Builder(this, SHORT_CUT_ID_1)
				.setShortLabel(getString(R.string.random_play))
				.setLongLabel(getString(R.string.random_play))
				.setIcon(Icon.createWithResource(this, R.drawable.ic_shuffle_blue_24dp))
				.setIntent(randomPlay)
				.build();

		mShortcutManager.setDynamicShortcuts(Collections.singletonList(shortcut));
	}
}
