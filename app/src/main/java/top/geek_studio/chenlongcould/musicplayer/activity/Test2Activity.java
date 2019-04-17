package top.geek_studio.chenlongcould.musicplayer.activity;

import android.os.Bundle;

import top.geek_studio.chenlongcould.musicplayer.R;

/**
 * @author chenlongcould
 */
public class Test2Activity extends BaseCompatActivity {

	public static final String TAG = "Test2Activity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
	}
	
	@Override
	public String getActivityTAG() {
		return TAG;
	}

	@Override
	public void inflateCommonMenu() {

	}

	@Override
	public void inflateChooseMenu() {

	}
}
