/*
 * ************************************************************
 * 文件：ThemeUtils.java  模块：geeklibrary  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:47
 * 上次修改时间：2019年01月17日 17:29:00
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.geeklibrary.theme;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressWarnings("WeakerAccess")
public final class ThemeUtils {

    public static final String TAG = "ThemeUtils";

    public static boolean checkTheme(final String themePath) {
        final File file = new File(themePath);
        if (!file.exists() || file.isFile() || file.listFiles().length == 0) {
            Log.e(TAG, "checkTheme: theme file error");
            return false;
        }

        final File detailFile = new File(themePath + File.separatorChar + ThemeStore.DETAIL_FILE_NAME);
        if (!detailFile.exists() || detailFile.isDirectory() || detailFile.length() == 0) {
            Log.e(TAG, "checkTheme: detail file error");
            return false;
        }

        final File imgDir = new File(themePath + File.separatorChar + ThemeStore.DIR_IMG);
        if (!imgDir.exists() || imgDir.isFile() || imgDir.listFiles().length == 0) {
            Log.e(TAG, "checkTheme: img dir error");
            return false;
        }

        final File ico = new File(themePath + File.separatorChar + ThemeStore.ICO_FILE_NAME.toLowerCase());
        if (!ico.exists() || ico.isDirectory()) {
            Log.e(TAG, "checkTheme: ico error");
            return false;
        }
        return true;
    }

    /**
     * find theme with id
     */
    public static File getThemeFile(@NonNull final Context context, final String themeId) {
        return new File(context.getExternalFilesDir(ThemeStore.DIR_NAME).getAbsolutePath() + File.separatorChar + themeId);
    }

    /**
     * the theme root dir
     * /sdcard/Android/data/<package-name>/themes
     */
    @Nullable
    public static Theme fileToTheme(final File f) {
        if (!checkTheme(f.getAbsolutePath())) return null;

        try {

            if (f.isDirectory()) {
                final File detailText = new File(f.getPath() + File.separatorChar + ThemeStore.DETAIL_FILE_NAME);

                //temp
                String title = "null";
                String date = "null";
                String nav_name = "null";
                String author = "null";
                String support_area = "null";
                String primary_color = "null";
                String accent_color = "null";
                String primary_color_dark = "null";
                String thumbnail = "null";
                String select = "null";
                String path = f.getPath();

                final BufferedReader bufferedReader = new BufferedReader(new FileReader(detailText));
                String line;

                int items = 0;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains(ThemeStore.ThemeColumns.AUTHOR)) {
                        author = line.split(":")[1];
                        Log.d(TAG, "doInBackground: " + author + " @ " + detailText.getPath());
                        items++;
                    }

                    if (line.contains(ThemeStore.ThemeColumns.TITLE)) {
                        title = line.split(":")[1];
                        Log.d(TAG, "doInBackground: " + title + " @ " + detailText.getPath());
                        items++;
                    }

                    if (line.contains(ThemeStore.ThemeColumns.NAV_NAME)) {
                        nav_name = line.split(":")[1];
                        Log.d(TAG, "doInBackground: " + nav_name + " @ " + detailText.getPath());
                        items++;
                    }

                    if (line.contains(ThemeStore.ThemeColumns.THUMBNAIL)) {
                        thumbnail = f.getPath() + File.separatorChar + line.split(":")[1];
                        Log.d(TAG, "doInBackground: " + thumbnail + " @ " + detailText.getPath());
                        items++;
                    }

                    if (line.contains(ThemeStore.ThemeColumns.SUPPORT_AREA)) {
                        support_area = line.split(":")[1];
                        Log.d(TAG, "doInBackground: " + support_area + " @ " + detailText.getPath());
                        items++;
                    }

                    if (line.contains(ThemeStore.ThemeColumns.PRIMARY_COLOR)) {
                        primary_color = line.split(":")[1];
                        Log.d(TAG, "doInBackground: " + primary_color);
                        items++;
                    }

                    if (line.contains(ThemeStore.ThemeColumns.PRIMARY_COLOR_DARK)) {
                        primary_color_dark = line.split(":")[1];
                        Log.d(TAG, "doInBackground: " + select);
                        items++;
                    }

                    if (line.contains(ThemeStore.ThemeColumns.ACCENT_COLOR)) {
                        accent_color = line.split(":")[1];
                        Log.d(TAG, "doInBackground: " + select);
                        items++;
                    }

                    if (line.contains(ThemeStore.ThemeColumns.DATE)) {
                        date = line.split(":")[1];
                        Log.d(TAG, "doInBackground: " + date);
                        items++;
                    }

                    if (line.contains(ThemeStore.ThemeColumns.SELECT)) {
                        select = line.split(":")[1];
                        Log.d(TAG, "doInBackground: " + select);
                        items++;
                    }
                }

                if (items >= ThemeStore.MIN_ITEM) {
                    Log.d(TAG, "fileToTheme: theme id is: " + f.getName());
                    return new Theme.Builder(f.getName())
                            .setAccentColor(accent_color)
                            .setAuthor(author)
                            .setDate(date)
                            .setNavName(nav_name)
                            .setPath(path).setPrimaryColorDark(primary_color_dark)
                            .setSupportArea(support_area)
                            .setSelect(select)
                            .setTitle(title)
                            .setThumbnail(thumbnail)
                            .setPrimaryColor(primary_color)
                            .build();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
