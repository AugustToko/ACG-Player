package top.geek_studio.chenlongcould.musicplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Fade;

import androidx.appcompat.app.AppCompatActivity;

import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.main.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.threadPool.CustomThreadPool;

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
		CustomThreadPool.post(() -> {
			while (Values.TEMP.switchNightDone) {
				Values.TEMP.switchNightDone = false;
				startActivity(new Intent(EmptyActivity.this, MainActivity.class));
				finish();
			}
		});
	}

	@Override
	public void onBackPressed() {
		finish();
	}
}
