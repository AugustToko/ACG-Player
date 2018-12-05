/*
 * ************************************************************
 * 文件：MyCardView.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月05日 20:16:39
 * 上次修改时间：2018年12月05日 20:16:08
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;

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

                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000);

                setTranslationX(getX() + (event.getX() - moveX));
//                setTranslationY(getY() + (event.getY() - moveY));
                break;

            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_UP:

//                this.clearAnimation();
//
//                ValueAnimator animator = new ValueAnimator();
//
//                if (mRawX > event.getRawX()) {
//                    animator.setFloatValues(event.getRawX(), (float) (event.getRawX() + velocityTracker.getXVelocity() * 0.1));
//                } else {
//                    animator.setFloatValues(event.getRawX(), (float) (event.getRawX() - velocityTracker.getXVelocity() * 0.1));
//                }
//
//                animator.setDuration(500);
//                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                    @Override
//                    public void onAnimationUpdate(ValueAnimator animation) {
//                        setTranslationX(getX() + ((float) animation.getAnimatedValue() - moveX));
//                    }
//                });
//                animator.addListener(new Animator.AnimatorListener() {
//                    @Override
//                    public void onAnimationStart(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        velocityTracker.clear();
//                        velocityTracker.recycle();
//                    }
//
//                    @Override
//                    public void onAnimationCancel(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animator animation) {
//
//                    }
//                });
//                animator.start();

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
