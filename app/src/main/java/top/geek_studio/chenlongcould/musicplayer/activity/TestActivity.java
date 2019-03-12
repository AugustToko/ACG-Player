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
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;

import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.ChangeBounds;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import top.geek_studio.chenlongcould.geeklibrary.widget.GkToolbar;
import top.geek_studio.chenlongcould.musicplayer.R;

public final class TestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        GkToolbar toolbar = findViewById(R.id.gk_toolbar);
        toolbar.setTitle("?????");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Transition transition = new ChangeBounds();
                transition.setInterpolator(new AnticipateOvershootInterpolator(0.5f));
                transition.setDuration(3000);

                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(TestActivity.this, R.layout.activity_test_2);
                ViewGroup root = ((ViewGroup) getWindow().getDecorView());
                TransitionManager.beginDelayedTransition(root, transition);
                constraintSet.applyTo(findViewById(R.id.root_view));
            }
        }, 3000);

    }
}
