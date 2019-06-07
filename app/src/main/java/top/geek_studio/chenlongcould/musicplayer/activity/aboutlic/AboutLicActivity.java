package top.geek_studio.chenlongcould.musicplayer.activity.aboutlic;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import top.geek_studio.chenlongcould.geeklibrary.DialogUtil;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.activity.base.BaseCompatActivity;

/**
 * @author chenlongcould
 */
public final class AboutLicActivity extends BaseCompatActivity implements AboutLicContract.View {
	
	private static final String TAG = "AboutLicActivity";

	private AboutLicContract.Presenter mPresenter;

	private AlertDialog load;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_lic);
		((Toolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> onBackPressed());

		load = DialogUtil.getLoadingDialog(this, "Loading...");

		final Button close = findViewById(R.id.close_button_activity_lic);

		close.setEnabled(true);
		close.setClickable(true);
		close.setOnClickListener(v -> finish());

		new AboutLicPresenter(this);

	}

	@Override
	protected void onResume() {
		super.onResume();
		mPresenter.start();
	}

	@Override
	protected void onDestroy() {
		mPresenter.close();
		super.onDestroy();
	}

	@Override
	public String getActivityTAG() {
		return TAG;
	}
	
	@Override
	public void inflateCommonMenu() {
		//not need
	}
	
	@Override
	public void inflateChooseMenu() {
		//not need
	}

	@Override
	public void setPresenter(AboutLicContract.Presenter presenter) {
		mPresenter = presenter;
	}

	@Override
	public void showLoad() {
		load.show();
	}

	@Override
	public void dismissLoad() {
		load.dismiss();
	}

	@Override
	public void setContentText(@NonNull String content) {
		((TextView) findViewById(R.id.show_lic_activity_lic)).setText(content);
	}

	@Override
	public AssetManager getAsset() {
		return getAssets();
	}
}
