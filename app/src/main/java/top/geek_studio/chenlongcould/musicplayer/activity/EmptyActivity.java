package top.geek_studio.chenlongcould.musicplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Fade;
import androidx.appcompat.app.AppCompatActivity;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.activity.main.MainActivity;

public class EmptyActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().setEnterTransition(new Fade().setDuration(500));
		getWindow().setExitTransition(new Fade().setDuration(500));
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_empty);
	}

	@Override
	public void onBackPressed() {
		startActivity(new Intent(this, MainActivity.class));
		super.onBackPressed();
	}
}
