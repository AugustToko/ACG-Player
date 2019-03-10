package top.geek_studio.chenlongcould.geeklibrary.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Menu;
import android.widget.TextView;

import java.lang.reflect.Field;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

/**
 * @author : chenlongcould
 * date   : 2019/03/09/09
 * version: 0.1
 * project: MusicPlayer
 * pkg: top.geek_studio.chenlongcould.geeklibrary
 */
@SuppressWarnings("unused")
public class GkToolbar extends Toolbar {

    private static final String TARGET_CLASS = "androidx.appcompat.widget.Toolbar";

    private Toolbar mToolbar;

    private Context mContext;

    private TextView mTitleTextView;

    private TextView mSubTitleTextView;

    private Menu mCommonMenu;

    private Menu mSelectedMenu;

    public GkToolbar(Context context) {
        super(context);
        mContext = context;
        mToolbar = this;
        init();
    }

    public GkToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mToolbar = this;
        init();
    }

    public GkToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mToolbar = this;
        init();
    }

    public void init() {
        try {
            Class clz = Class.forName("androidx.appcompat.widget.Toolbar");
            Field f = clz.getDeclaredField("mTitleTextView");
            Field f2 = clz.getDeclaredField("mSubtitleTextView");
            f.setAccessible(true);
            f2.setAccessible(true);
            mTitleTextView = (TextView) f.get(mToolbar);
            mSubTitleTextView = (TextView) f2.get(mToolbar);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * set Toolbar title alpha
     */
    public void setTitleAlpha(@FloatRange(from = 0, to = 1) float value) {
        if (mTitleTextView != null) {
            mTitleTextView.setAlpha(value);
        }
    }

    /**
     * set Toolbar subtitle alpha
     */
    public void setSubTitleAlpha(@FloatRange(from = 0, to = 1) float value) {
        if (mSubTitleTextView != null) {
            mSubTitleTextView.setAlpha(value);
        }
    }

    /**
     * set Toolbar subTile with AlphaAnimation {@link android.view.animation.AlphaAnimation}
     *
     * @param to next subtitle
     */
    public void changeSubTitle(String to) {
        final ValueAnimator animator = new ValueAnimator();
        animator.setFloatValues(1f, 0f);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> mSubTitleTextView.setAlpha((Float) animation.getAnimatedValue()));
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mSubTitleTextView.setText(to);
                ValueAnimator animator1 = new ValueAnimator();
                animator1.setFloatValues(0f, 1f);
                animator1.setDuration(300);
                animator1.addUpdateListener(va -> mSubTitleTextView.setAlpha((Float) va.getAnimatedValue()));
                animator1.start();
            }
        });
        animator.start();
    }

    /**
     * change toolbar title
     *
     * @param to next title
     */
    public void changeTitle(String to) {
        final ValueAnimator animator = new ValueAnimator();
        animator.setFloatValues(1f, 0f);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> mTitleTextView.setAlpha((Float) animation.getAnimatedValue()));
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mTitleTextView.setText(to);
                ValueAnimator animator1 = new ValueAnimator();
                animator1.setFloatValues(0f, 1f);
                animator1.setDuration(300);
                animator1.addUpdateListener(va -> mTitleTextView.setAlpha((Float) va.getAnimatedValue()));
                animator1.start();
            }
        });
        animator.start();
    }

    /**
     * set overlay color on toolbar
     *
     * @param color target color
     */
    public void setOverlayColor(@ColorInt int color) {
        if (getNavigationIcon() != null) getNavigationIcon().setTint(color);
        setTitleTextColor(color);

        if (getSubtitle() != null) {
            setSubtitleTextColor(color);
        }

        if (getMenu().size() != 0) {
            if (getOverflowIcon() != null) getOverflowIcon().setTint(color);
            for (int i = 0; i < getMenu().size(); i++) {
                if (getMenu().getItem(i).getIcon() != null) {
                    getMenu().getItem(i).getIcon().clearColorFilter();
                    getMenu().getItem(i).getIcon().setTint(color);
                }
            }
        }
    }
}
