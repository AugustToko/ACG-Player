/*
 * ************************************************************
 * 文件：MyDualView.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月12日 11:57:29
 * 上次修改时间：2018年12月12日 11:57:13
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.CustomView;

import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.TextView;

import top.geek_studio.chenlongcould.musicplayer.R;

public class MyDualView extends ConstraintLayout {
    private static final String TAG = "MyDualView";
    boolean mPress = false;
    private ConstraintLayout anotherGroup;
    private ConstraintLayout mExpandView;
    private float mOldWidth;
    private float mSecondPanelWidth;
    private float mLastRawX;
    private float mLastRawY;
    private float moveX;
    private float moveY;

    public MyDualView(Context context) {
        super(context);
        initView(context);
        Log.d(TAG, "MyDualView: 1");
    }

    public MyDualView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        Log.d(TAG, "MyDualView: 2");
    }

    public MyDualView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        Log.d(TAG, "MyDualView: 3");
    }

    private void initView(Context context) {

        anotherGroup = new ConstraintLayout(context);
        mExpandView = new ConstraintLayout(context);
        mExpandView.setBackgroundColor(Color.GREEN);
        anotherGroup.setBackgroundColor(Color.BLUE);
        setBackgroundColor(Color.CYAN);

        //add
        TextView textView = new TextView(context);
        textView.setText("One View");
        textView.setTextSize(50);


        post(() -> {
            mOldWidth = getWidth();

            mSecondPanelWidth = mOldWidth / 3 * 2;

            ViewGroup.LayoutParams params = getLayoutParams();
            params.width = (int) (mSecondPanelWidth + mOldWidth);
            setLayoutParams(params);
            setTranslationX(-mSecondPanelWidth);
            ConstraintLayout toolbar = (ConstraintLayout) getViewById(R.id.include_toolbar);
            ConstraintLayout.LayoutParams toolbarParams = (LayoutParams) toolbar.getLayoutParams();
            toolbarParams.width = (int) mOldWidth;
            toolbar.setLayoutParams(toolbarParams);
            toolbar.setTranslationX(mSecondPanelWidth);
            addView(mExpandView, 0);
            addView(anotherGroup, 1);

            MyDualView.LayoutParams params2 = (LayoutParams) mExpandView.getLayoutParams();
            params2.width = (int) mOldWidth;
            params2.height = LayoutParams.MATCH_PARENT;
            mExpandView.setLayoutParams(params2);
            mExpandView.addView(textView);

            anotherGroup.setTranslationX(mSecondPanelWidth);
            MyDualView.LayoutParams params1 = (LayoutParams) anotherGroup.getLayoutParams();
            params1.width = (int) mOldWidth;
            anotherGroup.setLayoutParams(params1);

        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:

                mLastRawX = event.getRawX();
                mLastRawY = event.getRawY();

                moveX = event.getX();
                moveY = event.getY();

                mPress = true;

                break;
            case MotionEvent.ACTION_MOVE:

                if (event.getX() <= mSecondPanelWidth) {
                    //...
                    if (event.getRawX() > mLastRawX) break;
                }

//                if (getTranslationX() > 0) {
//                    if (event.getRawX() > mLastRawX) break;
//                }
//
//                if (getTranslationX() < -mSecondPanelWidth) {
//                    if (event.getRawX() < mLastRawX) break;
//                    break;
//                }

                float moveVal = getX() + (event.getX() - moveX);

                setTranslationX(moveVal);
                mExpandView.setTranslationX(-mSecondPanelWidth / 2);
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_UP:
                mPress = false;

                if (mLastRawY == event.getRawY() + 10 || mLastRawY == event.getRawY() - 10
                        && mLastRawX == event.getRawX() + 10 || mLastRawX == event.getRawX() - 10) {
                    performClick();
                    Log.d(TAG, "onTouchEvent: move low and will do click");
                    break;
                }

                break;
        }

        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
