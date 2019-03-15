/*
 * ************************************************************
 * 文件：TestActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:46
 * 上次修改时间：2019年01月17日 17:28:59
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.activity;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import top.geek_studio.chenlongcould.musicplayer.R;

public final class TestActivity extends Activity {

	private static final String TAG = "TestActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);

		Toolbar toolbar = findViewById(R.id.toolbar);

		toolbar.inflateMenu(R.menu.menu_test);

	}
}
