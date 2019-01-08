/*
 * ************************************************************
 * 文件：Values.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月08日 10:05:13
 * 上次修改时间：2019年01月08日 10:05:09
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

@SuppressWarnings("WeakerAccess")
public final class Values {

    public static int DEF_CROSS_FATE_TIME = 500;

    public static boolean FIRST_USE = true;

    public static final class FLAG {
        public static boolean THEME_CHANGED = false;
    }

    /**
     * final string(s), TAGs
     */
    public static final String INDEX = "index";
    public static final String TAG_UNIVERSAL_ONE = "TAG_UNIVERSAL_ONE";
    public static final String SURE_GET_PERMISSION = "SURE_GET_PERMISSION";
    public static final String PLAY_LIST_SPF_KEY = "PLAY_LIST_SPF_KEY";
    public static final String TYPE_RANDOM = "RANDOM";
    public static final String TYPE_COMMON = "COMMON";
    public static final String TYPE_REPEAT = "REPEAT";
    public static final String TYPE_REPEAT_ONE = "REPEAT_ONE";

    public static boolean HAS_PLAYED = false;           //检测app打开后, 是否播放过音乐 (如果没, 默认点击播放按钮为快速随机播放)

    public static boolean PHONE_HAS_NAV = false;        //phone has navBar?

    public static final class LogTAG {
        public static final String LIFT_TAG = "THE_TAG_OF_LIFE";
    }

    /**
     * permission RequestCode
     */
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 60;
    public static final int MAX_HEIGHT_AND_WIDTH = 100;

    public static boolean BIND_SERVICE = false;

    public static boolean STYLE_CHANGED = false;

    public static boolean BUTTON_PRESSED = false;

    /**
     * result(s), status
     */
    //default value false
    public static boolean MUSIC_DATA_INIT_DONE = false;

    public static final class HandlerWhat {

        /**
         * Handler msg.what
         */
        public static final int INIT_MUSIC_LIST_DONE = 50;
        public static final int SEEK_BAR_UPDATE = 53;
        public static final int INIT_SEEK_BAR = 54;

        /**
         * in MainActivity
         */
        public static final int SET_BUTTON_PLAY = 58;
        public static final int SET_BUTTON_PAUSE = 57;
        public static final int LOAD_INTO_NAV_IMAGE = 5003;

        public static final int RECYCLER_SCROLL = 55001;

    }

    /**
     * @deprecated use {@link SharedPrefsTag#PRIMARY_COLOR}
     * {@link SharedPrefsTag#PRIMARY_DARK_COLOR}
     * {@link SharedPrefsTag#ACCENT_COLOR}
     */
    public static final class ColorInt {
        public static final String PRIMARY_COLOR = "mPrimaryColor";

        public static final String PRIMARY_DARK_COLOR = "mPrimaryDarkColor";

        public static final String ACCENT_COLOR = "mAccentColor";
    }

    /*
     * 判断在MusicDetailActivity下动画是否加载完成
     * */
    public static boolean ON_ANIMATION_FINISH = true;

    public static final class CurrentData {

        /**
         * same as
         * @see UIMODE#MODE_CAR
         * @see UIMODE#MODE_COMMON
         *
         * @deprecated use {@link UIMODE#MODE_COMMON}, {@link UIMODE#MODE_CAR}
         */
        @Deprecated
        public static final String MODE_COMMON = UIMODE.MODE_COMMON;
        @Deprecated
        public static final String MODE_CAR = UIMODE.MODE_CAR;

        public static String CURRENT_UI_MODE = UIMODE.MODE_COMMON;

        /**
         * TEMP DATA
         */
        //default value -1 or null
        public static int CURRENT_BIND_INDEX_MUSIC_LIST = -1;
        public static int CURRENT_BIND_INDEX_ALBUM_LIST = -1;
        public static int CURRENT_PAGE_INDEX = -1;
        public static int CURRENT_SELECT_ITEM_INDEX_WITH_ITEM_MENU = -1;            //存储使浮动菜单弹出的item,def = -1
        public static int CURRENT_MUSIC_INDEX = -1;

        /**
         * 用户手动切歌的播放模式
         * 1. random
         * 2. common
         */
        public static String CURRENT_PLAY_TYPE = "COMMON";

        /**
         * auto switch
         * 1. common
         * 2. repeat one
         * 3. repeat list
         * 4. random
         */
        public static String CURRENT_AUTO_NEXT_TYPE = "COMMON";

        /**
         * current play order (default = all music)
         */
        public static String CURRENT_PLAY_LIST = "default";

    }

    public static final class UIMODE {
        public static final String MODE_COMMON = "common";
        public static final String MODE_CAR = "car";
    }

    /**
     * sharedPrefs tag
     */
    public static final class SharedPrefsTag {

        //color
        public static final String PRIMARY_COLOR = "mPrimaryColor";
        public static final String PRIMARY_DARK_COLOR = "mPrimaryDarkColor";
        public static final String ACCENT_COLOR = "mAccentColor";

        public static final String DETAIL_BG_STYLE = "DETAIL_BG_STYLE";
        public static final String AUTO_NIGHT_MODE = "NIGHT_MODE";
        public static String PLAY_TYPE = Values.TYPE_COMMON;
        public static final String FIRST_USE = "FIRST_USE";

        public static final String ALBUM_LIST_DISPLAY_TYPE = "ALBUM_LIST_DISPLAY_TYPE";
        public static final String ALBUM_LIST_GRID_TYPE_COUNT = "ALBUM_LIST_GRID_TYPE_COUNT";

        public static final String FAVOURITE_LIST_ID = "FAVOURITE_LIST_ID";

        //theme
        public static final String SELECT_THEME = "SELECT_THEME";
        public static final String THEME_USE_NOTE = "THEME_USE_NOTE";
    }

    public static final class BroadCast {
        public static String ReceiverOnMusicPlay = "top.geek_studio.chenlongcould.musicplayer.BroadCasts.ReceiverOnMusicPlay";
        public static String ReceiverOnMusicStop = "top.geek_studio.chenlongcould.musicplayer.BroadCasts.ReceiverOnMusicStop";
        public static String ReceiverOnMusicPause = "top.geek_studio.chenlongcould.musicplayer.BroadCasts.ReceiverOnMusicPause";
        public static String DayNightReceiver = "top.geek_studio.chenlongcould.musicplayer.BroadCasts.DayNightReceiver";
    }

    public static final class Color {

        public static final String TEXT_COLOR_IN_NIGHT = "#C6C6C6";

        /**
         * default
         */
        public static final String PRIMARY = "#008577";
        public static final String PRIMARY_DARK = "#00574B";
        public static final String ACCENT = "#D81B60";

        /**
         * Ico color
         */
        public static final String NOT_VERY_BLACK = "#1d1d1d";
        public static final String NOT_VERY_WHITE = "#dddddd";
        public static final String TEXT_COLOR_IN_DAY = "#434343";
        public static final String THEME_IN_USE = "#7a00f2ff";
        public static final String WIN_BACKGROUND_COLOR = "#fafafa";
        /**
         * item title color (dym)
         */
        public static String TEXT_COLOR = "#3c3c3c";

    }

    public static final class Permission {
        public static final String BROAD_CAST = "top.geek_studio.chenlongcould.musicplayer.BroadCasts";
    }

    public static final class Style {
        public static final String STYLE_BACKGROUND_BLUR = "BLUR";
        public static final String STYLE_BACKGROUND_AUTO_COLOR = "AUTO_COLOR";

        public static boolean NIGHT_MODE = false;

        //background style model
        public static String DETAIL_BACKGROUND = "AUTO_COLOR";
    }

}
