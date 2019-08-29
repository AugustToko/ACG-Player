package top.geek_studio.chenlongcould.musicplayer.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.activity.ui.newsettings.NewSettingsFragment;

public class NewSettingsActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_settings_activity);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, NewSettingsFragment.newInstance())
					.commitNow();
		}
	}
}
