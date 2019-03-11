/*
 * ************************************************************
 * 文件：DialogUtil.java  模块：geeklibrary  项目：MusicPlayer
 * 当前修改时间：2019年01月27日 13:11:38
 * 上次修改时间：2019年01月19日 12:17:57
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.geeklibrary;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

/**
 * @author chenlongcould
 */
public class DialogUtil {

    /**
     * @param context context
     * @param aTitle  theTitle
     * @return {@link AlertDialog.Builder}
     */
    public static androidx.appcompat.app.AlertDialog getLoadingDialog(Context context, String... aTitle) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        final View loadView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        // TODO: 2019/1/7 custom Theme loading animation
        builder.setView(loadView);
        builder.setTitle(aTitle.length == 0 ? "Loading..." : aTitle[0]);
        builder.setCancelable(false);
        return builder.create();
    }

}
