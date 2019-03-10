package top.geek_studio.chenlongcould.geeklibrary.widget;

import android.content.res.ColorStateList;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.snackbar.SnackbarContentLayout;

import java.lang.reflect.Field;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

/**
 * @author chenlongcould
 */
@SuppressWarnings({"FieldCanBeLocal", "UnusedReturnValue", "unused"})
public class GkSnackbar {

    private Snackbar mSnackbar;

    @Nullable
    private Snackbar.SnackbarLayout layout = null;

    private Button mActionView = null;

    public GkSnackbar(@NonNull View view, @NonNull CharSequence charSequence, @Snackbar.Duration int duration) {
        mSnackbar = Snackbar.make(view, charSequence, duration);
        layout = (Snackbar.SnackbarLayout) mSnackbar.getView();
//        setLayout(mSnackbar);
    }

    public GkSnackbar(@NonNull View view, @StringRes int id, @Snackbar.Duration int duration) {
        mSnackbar = Snackbar.make(view, id, duration);
        layout = (Snackbar.SnackbarLayout) mSnackbar.getView();
    }

    /**
     * setViewBody background color
     */
    public GkSnackbar setBackgroundColor(@ColorInt int color) {
        if (layout != null) layout.setBackgroundColor(color);
        return this;
    }

    /**
     * set Button(ActionView)'s textColor
     *
     * @deprecated use {@link this#setActionTextColor(int)}
     */
    public GkSnackbar setActionButtonTextColor(@ColorInt int color) {
        if (layout != null) {
            try {
                SnackbarContentLayout contentLayout = (SnackbarContentLayout) layout.getChildAt(0);
                Field aF = Class.forName("com.google.android.material.snackbar.SnackbarContentLayout").getDeclaredField("actionView");
                aF.setAccessible(true);
                mActionView = (Button) aF.get(contentLayout);
                mActionView.setTextColor(color);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            throw new NullPointerException("layout is null!");
        }
        return this;
    }

    public GkSnackbar setActionTextColor(@ColorInt int color) {
        mSnackbar.setActionTextColor(color);
        return this;
    }

    public GkSnackbar setActionTextColor(ColorStateList color) {
        mSnackbar.setActionTextColor(color);
        return this;
    }

    /**
     * @see Snackbar#setAction(CharSequence, View.OnClickListener)
     */
    public GkSnackbar setAction(CharSequence text, final View.OnClickListener listener) {
        mSnackbar.setAction(text, listener);
        return this;
    }

    /**
     * @see Snackbar#setAction(int, View.OnClickListener)
     */
    public GkSnackbar setAction(@StringRes int id, final View.OnClickListener listener) {
        mSnackbar.setAction(id, listener);
        return this;
    }

    /**
     * set Snackbar body view alpha
     *
     * @param val value
     */
    public GkSnackbar setBodyViewAlpha(@FloatRange(from = 0f, to = 1f) float val) {
        if (layout != null) layout.setAlpha(val);
        return this;
    }

    /**
     * @see Snackbar#addCallback(BaseTransientBottomBar.BaseCallback)
     */
    public GkSnackbar addCallback(Snackbar.Callback callback) {
        mSnackbar.addCallback(callback);
        return this;
    }

    // TODO: 2019/2/9
    private GkSnackbar addIcon(@DrawableRes int ico, FrameLayout.LayoutParams params) {
        ImageView imageView = new ImageView(mSnackbar.getContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(ico);
        imageView.setLayoutParams(params);
        layout.addView(imageView, 0);
        return this;
    }

    /**
     * @see Snackbar#show()
     */
    public void show() {
        mSnackbar.show();
    }

    public void dismiss() {
        mSnackbar.dismiss();
    }

    public boolean isShown() {
        return mSnackbar.isShown();
    }

    public Button getActionView() {
        return mActionView;
    }

    ////////////////////////getter///////////////////////////

    public Snackbar getSnackbar() {
        return mSnackbar;
    }

    @Nullable
    public Snackbar.SnackbarLayout getLayout() {
        return layout;
    }

    /**
     * init Snackbar body layout
     *
     * @deprecated use {@link Snackbar#getView()}
     */
    private void setLayout(Snackbar snackbar) {
        try {
            Class<?> c = snackbar.getClass().getSuperclass();
            Field field = c.getDeclaredField("view");
            field.setAccessible(true);
            layout = (Snackbar.SnackbarLayout) field.get(snackbar);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Snackbar.Duration
    public int getDuration() {
        return mSnackbar.getDuration();
    }

}
