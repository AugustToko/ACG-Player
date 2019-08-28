package top.geek_studio.chenlongcould.musicplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import io.reactivex.disposables.Disposable;
import top.geek_studio.chenlongcould.musicplayer.activity.main.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.utils.MusicUtil;

/**
 * @author chenlongcould
 */
@RequiresApi(Build.VERSION_CODES.N)
public final class MyTileService extends TileService {

	private static final String TAG = "MyTileService";

	public static final String ACTION_SET_TITLE = "ACTION_SET_TITLE";

	private boolean mEnable = false;

	private Disposable mDisposable;

	private String title = "Fast Play";

	public MyTileService() {
		super();
	}

	public ServiceConnection sServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Data.sMusicBinder = IMuiscService.Stub.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		if (ContextCompat.checkSelfPermission(MyTileService.this,
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
			MusicUtil.loadDataSource(this);
			if (Data.sMusicBinder == null) {
				Intent intent = new Intent(this, MusicService.class);
				startService(intent);
				bindService(intent, sServiceConnection, BIND_AUTO_CREATE);
			}
		} else {
			Toast.makeText(this, "Need Permission, please open the app...", Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && intent.getAction() != null) {
			Log.d(TAG, "onStartCommand: " + intent.getAction());
			switch (intent.getAction()) {
				case ACTION_SET_TITLE: {
					title = intent.getStringExtra("title");

					Tile tile = getQsTile();
					if (tile != null) {
						getQsTile().setState(Tile.STATE_ACTIVE);
						getQsTile().setLabel(title);
						getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_audiotrack_24px));
						getQsTile().updateTile();
					}
				}
				break;
				default:
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Tile tile = getQsTile();
		if (tile != null) {
			getQsTile().setState(Tile.STATE_ACTIVE);
			getQsTile().setLabel(title);
			getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_audiotrack_24px));
			getQsTile().updateTile();
		}
		return super.onBind(intent);
	}

	@Override
	public void onClick() {
		if (Data.sMusicItems.isEmpty()) {
			MusicUtil.loadDataSource(this);
		}

		if (!mEnable) {
			mEnable = true;
			getQsTile().setState(Tile.STATE_ACTIVE);
			getQsTile().setLabel("Playing...");
			getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_audiotrack_24px));
			getQsTile().updateTile();
			MainActivity.startService(this, MusicService.ServiceActions.ACTION_FAST_SHUFFLE);
		} else {
			mEnable = false;
			getQsTile().setState(Tile.STATE_INACTIVE);
			getQsTile().setLabel(title);
			getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_audiotrack_24px));
			getQsTile().updateTile();
			MainActivity.startService(this, MusicService.ServiceActions.ACTION_PAUSE);
		}
	}

	@Override
	public void onTileAdded() {
		super.onTileAdded();
		Tile tile = getQsTile();
		try {
			if (tile != null && Data.sMusicBinder.isPlayingMusic()) {
				getQsTile().setState(Tile.STATE_ACTIVE);
				getQsTile().setLabel(title);
				getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_audiotrack_24px));
				getQsTile().updateTile();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onTileRemoved() {
		super.onTileRemoved();
	}

	@Override
	public void onStartListening() {
		super.onStartListening();
	}

	@Override
	public void onStopListening() {
		super.onStopListening();
	}

	@Override
	public void onDestroy() {
		if (mDisposable != null && !mDisposable.isDisposed()) {
			mDisposable.dispose();
		}

		try {
			unbindService(sServiceConnection);
		} catch (Exception e) {
			Log.d(TAG, "onDestroy: " + e.getMessage());
		}

		super.onDestroy();
	}

}
