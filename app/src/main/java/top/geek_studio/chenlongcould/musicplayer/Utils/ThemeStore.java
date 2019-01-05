/*
 * ************************************************************
 * 文件：ThemeStore.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月05日 20:52:07
 * 上次修改时间：2019年01月05日 20:42:45
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Utils;

import java.io.File;

public final class ThemeStore {

    public static final int MIN_ITEM = 7;

    public static final String DATA_BASE_NAME = "ThemeStore.db";

    public static final String TABLE = "theme";

    /**
     * /sdcard/Android/data/<package-name>/themes
     */
    public static final String DIR_NAME = "themes";

    /**
     * ALL IMAGES DIR NAME
     * /sdcard/Android/data/<package-name>/themes/img
     */
    public static final String DIR_IMG = "img";

    public static final String DIR_IMG_NAV = DIR_IMG + File.separatorChar + SupportArea.NAV;

    public static final String DIR_IMG_SLIDE = DIR_IMG + File.separatorChar + SupportArea.SLIDE;

    public static final String DIR_IMG_BG = DIR_IMG + File.separatorChar + SupportArea.BG;

    public static final String DETAIL_FILE_NAME = "detail.txt";

    public static final String ICO_FILE_NAME = "thumbnail.png";

    public static final int REQUIRE_ITEMS = 6;

    public interface SupportArea {
        String NAV = "nav";
        String SLIDE = "slide";
        String BG = "bg";
    }

    public interface ThemeColumns {
        String AUTHOR = "author";
        String TITLE = "title";
        String NAV_NAME = "nav_name";
        String THUMBNAIL = "thumbnail";
        String SUPPORT_AREA = "support_area";
        String PRIMARY_COLOR = "primary_color";             //NN
        String PRIMARY_COLOR_DARK = "primary_color_dark";   //NN
        String ACCENT_COLOR = "accent_color";               //NN
        String DATE = "date";
        String SELECT = "select";
    }
}
