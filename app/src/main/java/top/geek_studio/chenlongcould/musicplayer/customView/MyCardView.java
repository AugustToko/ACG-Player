/*
 * ************************************************************
 * 文件：MyCardView.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:46
 * 上次修改时间：2019年01月17日 17:28:59
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.customView;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

public class MyCardView extends CardView {
	private static final String TAG = "MyCardView";

	float moveX = 0;
	float moveY = 0;

	float mRawX;

	//追踪速度关键的类。没有这个这篇文章将毫无意义
	VelocityTracker velocityTracker;

	public MyCardView(@NonNull Context context) {
		super(context);
	}

	public MyCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public MyCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.d(TAG, "onTouchEvent: on touch");
		int action = event.getAction();

		switch (action) {
			case MotionEvent.ACTION_DOWN:
				moveX = event.getX();
				moveY = event.getY();

				mRawX = event.getRawX();

//                velocityTracker = VelocityTracker.obtain();

				break;
			case MotionEvent.ACTION_MOVE:

//                velocityTracker.addMovement(event);
//                velocityTracker.computeCurrentVelocity(1000);

				setTranslationX(getX() + (event.getX() - moveX));
//                setTranslationY(getY() + (event.getY() - moveY));

				Log.d(TAG, "onTouchEvent: getx:" + getX() + " event.getx: " + event.getX() + " pointX: " + moveX + " transX: " + getTranslationX());

				break;

			case MotionEvent.ACTION_CANCEL:
				break;
			case MotionEvent.ACTION_UP:

				break;
		}

		return true;
	}

	@Override
	public boolean performClick() {
		Log.d(TAG, "performClick: do");
		return super.performClick();
	}
}
