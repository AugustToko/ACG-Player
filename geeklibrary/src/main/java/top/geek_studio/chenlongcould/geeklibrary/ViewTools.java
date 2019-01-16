/*
 * ************************************************************
 * 文件：ViewTools.java  模块：geeklibrary  项目：MusicPlayer
 * 当前修改时间：2019年01月16日 20:44:58
 * 上次修改时间：2019年01月15日 20:31:59
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.geeklibrary;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.ViewGroup;

public final class ViewTools {
    /**
     * @return TypedArray need Recycle {@link TypedArray#recycle()}
     * @see TypedArray#getDrawable(int) int is 0
     */
    public static TypedArray getRippe(@NonNull final Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
        int[] attr = new int[]{android.R.attr.selectableItemBackground};
        return context.obtainStyledAttributes(typedValue.resourceId, attr);
    }

    public static ViewGroup getRootView(Activity context) {
        return ((ViewGroup) ((ViewGroup) context.findViewById(android.R.id.content)).getChildAt(0));
    }
}
