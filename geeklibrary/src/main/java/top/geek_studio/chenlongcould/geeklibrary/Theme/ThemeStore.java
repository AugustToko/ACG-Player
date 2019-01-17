/*
 * ************************************************************
 * 文件：ThemeStore.java  模块：geeklibrary  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:47
 * 上次修改时间：2019年01月17日 17:28:59
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.geeklibrary.Theme;

import java.io.File;

import top.geek_studio.chenlongcould.geeklibrary.Required;

@SuppressWarnings("WeakerAccess")
public final class ThemeStore {

    /**
     * 一个主题中项目数量的最小值
     */
    public static final int MIN_ITEM = 7;

    /**
     * DATABASE NAME
     */
    public static final String DATA_BASE_NAME = "ThemeStore.db";

    /**
     * DB TABLE
     */
    public static final String TABLE = "themes";

    /**
     * /sdcard/Android/data/<package-name>/themes
     */
    public static final String DIR_NAME = "themes";

    /**
     * ALL IMAGES DIR NAME
     * /sdcard/Android/data/<package-name>/themes/img
     */
    public static final String DIR_IMG = "img";

    /**
     * NAV IMAGES
     * /sdcard/Android/data/<package-name>/themes/img/nav
     *
     * nav: {@link android.support.design.widget.NavigationView}
     */
    public static final String DIR_IMG_NAV = DIR_IMG + File.separatorChar + SupportArea.NAV;

    /**
     * SLIDE IMAGES
     * /sdcard/Android/data/<package-name>/themes/img/slide
     */
    public static final String DIR_IMG_SLIDE = DIR_IMG + File.separatorChar + SupportArea.SLIDE;

    /**
     * bg IMAGES
     * /sdcard/Android/data/<package-name>/themes/img/bg
     */
    public static final String DIR_IMG_BG = DIR_IMG + File.separatorChar + SupportArea.BG;

    /**
     * THEME DETAIL
     * /sdcard/Android/data/<package-name>/themes/
     */
    public static final String DETAIL_FILE_NAME = "detail.txt";

    /**
     * THEME thumbnail
     * */
    public static final String ICO_FILE_NAME = "thumbnail.png";

    /**
     * THEME IMAGE SUPPORT AREA
     * */
    public interface SupportArea {
        String NAV = "nav";
        String SLIDE = "slide";
        String BG = "bg";
    }

    public interface ThemeColumns {
        /**
         * theme author
         */
        @Required
        String AUTHOR = "author";

        /**
         * theme title(show in recyclerView)
         */
        @Required
        String TITLE = "title";

        /**
         * show in {@link android.support.design.widget.NavigationView}
         */
        @Required
        String NAV_NAME = "nav_name";

        /**
         * theme thumbnail
         */
        @Required
        String THUMBNAIL = "thumbnail";

        /**
         * support area(for images)
         */
        @Required
        String SUPPORT_AREA = "support_area";

        /**
         * theme primary color
         */
        String PRIMARY_COLOR = "primary_color";             //NN

        /**
         * theme primaryDark color
         * */
        String PRIMARY_COLOR_DARK = "primary_color_dark";   //NN

        /**
         * theme accent color
         * */
        String ACCENT_COLOR = "accent_color";               //NN

        /**
         * theme build date
         * */
        String DATE = "date";

        /**
         * default images selected
         * */
        @Required
        String SELECT = "select";
    }
}
