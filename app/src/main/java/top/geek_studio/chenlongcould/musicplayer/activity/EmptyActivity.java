package top.geek_studio.chenlongcould.musicplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import androidx.appcompat.app.AppCompatActivity;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.activity.main.MainActivity;

/**
 * 过渡 Activity
 */
public class EmptyActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().setEnterTransition(new Fade().setDuration(500));
		getWindow().setExitTransition(new Fade().setDuration(500));
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_empty);
		new Handler().post(() -> startActivity(new Intent(EmptyActivity.this, MainActivity.class)));
		finish();
	}

	@Override
	public void onBackPressed() {
		finish();
	}
}
