/*
 * ************************************************************
 * 文件：ViewTools.java  模块：geeklibrary  项目：MusicPlayer
 * 当前修改时间：2019年01月14日 14:45:09
 * 上次修改时间：2019年01月14日 14:44:29
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_stusio.chenlongcould.geeklibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.TypedValue;

public final class ViewTools {
    public static Drawable getRippe(@NonNull final Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
        int[] attr = new int[]{android.R.attr.selectableItemBackground};
        TypedArray typedArray = context.obtainStyledAttributes(typedValue.resourceId, attr);
        return typedArray.getDrawable(0);
    }
}
