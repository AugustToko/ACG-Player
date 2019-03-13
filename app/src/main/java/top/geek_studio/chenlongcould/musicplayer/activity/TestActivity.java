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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);

		Toolbar toolbar = findViewById(R.id.toolbar);

		toolbar.inflateMenu(R.menu.menu_test);

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                ConstraintSet constraintSet = new ConstraintSet();
//                constraintSet.clone((ConstraintLayout) LayoutInflater.from(TestActivity.this).inflate(R.layout.activity_test2, null).findViewById(R.id.body));
//
//                Transition transition = new ChangeBounds();
//                transition.setInterpolator(new AnticipateOvershootInterpolator(0.5f));
//                transition.setDuration(2000);
//                TransitionManager.beginDelayedTransition((ViewGroup) getWindow().getDecorView(), transition);
//
//                constraintSet.applyTo(findViewById(R.id.body));
//            }
//        }, 2000);
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                ConstraintSet constraintSet = new ConstraintSet();
//                constraintSet.clone(TestActivity.this, R.layout.activity_test2);
//
//                Transition transition = new ChangeBounds();
//                transition.setInterpolator(new AnticipateOvershootInterpolator(0.5f));
//                transition.setDuration(2000);
//                TransitionManager.beginDelayedTransition((ViewGroup) getWindow().getDecorView(), transition);
//
//                constraintSet.applyTo(findViewById(R.id.root_view));
//            }
//        }, 1800);

	}
}
