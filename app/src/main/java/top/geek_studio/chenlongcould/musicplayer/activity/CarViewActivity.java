/*
 * ************************************************************
 * 文件：CarViewActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:46
 * 上次修改时间：2019年01月17日 17:28:59
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.fragment.MusicDetailFragmentLandSpace;

/**
 * @author chenlongcould
 */
public final class CarViewActivity extends AppCompatActivity {

	private static final int UI_ANIMATION_DELAY = 300;
	private static final String TAG = "CarViewActivity";
	private final Handler mHideHandler = new Handler();
	private final Runnable mShowPart2Runnable = () -> {
		// Delayed display of UI elements
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.show();
		}
	};
	private boolean backPressed = false;
	private View mContentView;
	private final Runnable mHidePart2Runnable = new Runnable() {
		@Override
		public void run() {
			// Delayed removal of status and navigation bar

			// Note that some of these constants are new as of API 16 (Jelly Bean)
			// and API 19 (KitKat). It is safe to use them, as they are inlined
			// at compile-time and do nothing on earlier devices.
			mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
	};
	private boolean mVisible;
	private final Runnable mHideRunnable = this::hide;

	private MusicDetailFragmentLandSpace mFragmentLandSpace;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Values.CurrentData.CURRENT_UI_MODE = Values.CurrentData.MODE_CAR;
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_car_view);

		Data.sCarViewActivity = this;

		mVisible = true;
		mContentView = findViewById(R.id.fullscreen_content);

		// Set up the user interaction to manually show or hide the system UI.
		mContentView.setOnClickListener(view -> toggle());

		mFragmentLandSpace = MusicDetailFragmentLandSpace.newInstance();
		final FragmentManager fragmentManager = getSupportFragmentManager();
		final FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.replace(R.id.frag_land_space, mFragmentLandSpace);
		transaction.commit();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (mVisible) {
			mHideHandler.removeCallbacks(mHideRunnable);
			mHideHandler.postDelayed(mHideRunnable, 100);
		}
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy: ");
		super.onDestroy();
	}
	
	public MusicDetailFragmentLandSpace getFragmentLandSpace() {
		return mFragmentLandSpace;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, 100);
	}
	
	private void toggle() {
		if (mVisible) {
			hide();
		} else {
			show();
		}
	}

	public void hide() {
		// Hide UI first
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}

		mVisible = false;

		// Schedule a runnable to remove the status and navigation bar after a delay
		mHideHandler.removeCallbacks(mShowPart2Runnable);
		mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
	}

	public void show() {
		// Show the system bar
		mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
		mVisible = true;

		// Schedule a runnable to display UI elements after a delay
		mHideHandler.removeCallbacks(mHidePart2Runnable);
		mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
	}

	@Override
	public void onBackPressed() {
		if (backPressed) {
			return;
		}
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		show();
		findViewById(R.id.frag_land_space).setVisibility(View.GONE);
		mContentView.setVisibility(View.GONE);
		getWindow().setBackgroundDrawable(null);

		backPressed = true;
		Data.sCarViewActivity = null;
		Toast.makeText(this, "Exiting", Toast.LENGTH_SHORT).show();
		Values.CurrentData.CURRENT_UI_MODE = Values.UIMODE.MODE_COMMON;

		new Handler().postDelayed(this::finish, 1000);
	}

	public boolean isVisible() {
		return mVisible;
	}
	
	@Override
	protected void finalize() throws Throwable {
		Log.d(TAG, "finalize: ");
		super.finalize();
	}
}
