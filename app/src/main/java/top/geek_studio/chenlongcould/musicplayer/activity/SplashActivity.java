package top.geek_studio.chenlongcould.musicplayer.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.base.BaseCompatActivity;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

/**
 * @author chenlongcould
 * @deprecated use {@link MainActivity}
 */
public final class SplashActivity extends AppCompatActivity {

	private static final String TAG = "SplashActivity";

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case Values.REQUEST_WRITE_EXTERNAL_STORAGE: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					initDone();
				} else {
					Utils.Ui.fastToast(this, "Failed to get permission, again!");
					final AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Failed to get permission");
					builder.setMessage("Try again?");
					builder.setCancelable(false);
					builder.setNegativeButton("Sure!", (dialog, which) -> initPermission());
					builder.setNeutralButton("Cancel!", (dialog, which) -> {
						dialog.dismiss();
						finish();
					});
					builder.show();
				}
			}
			break;
			default:
		}
	}

	/**
	 * init permission, every Activity extends {@link BaseCompatActivity}
	 */
	public boolean initPermission() {
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
			return true;
		} else {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Values.REQUEST_WRITE_EXTERNAL_STORAGE);
			return false;
		}
	}

	private void initDone() {
		startActivity(new Intent(SplashActivity.this, MainActivity.class));
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Android 5.0 以上 全透明
		Window window = getWindow();

		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
				| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

		window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LOW_PROFILE
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
				| WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// 状态栏（以上几行代码必须，参考setStatusBarColor|setNavigationBarColor方法源码）
		window.setStatusBarColor(Color.TRANSPARENT);
		// 虚拟导航键
		window.setNavigationBarColor(Color.TRANSPARENT);

		setContentView(R.layout.activity_splash);

		if (initPermission()) {
			new Handler().post(this::initDone);
		}
	}

}
