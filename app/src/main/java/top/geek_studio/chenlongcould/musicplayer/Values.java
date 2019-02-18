/*
 * ************************************************************
 * 文件：Values.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月27日 13:11:38
 * 上次修改时间：2019年01月27日 13:08:50
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

@SuppressWarnings("WeakerAccess")
public interface Values {

    int DEF_CROSS_FATE_TIME = 500;

    /**
     * final string(s), TAGs
     */
    String INDEX = "index";
    String TAG_UNIVERSAL_ONE = "TAG_UNIVERSAL_ONE";
    String TYPE_RANDOM = "RANDOM";
    String TYPE_COMMON = "COMMON";
    String TYPE_REPEAT = "REPEAT";
    String TYPE_REPEAT_ONE = "REPEAT_ONE";

    /**
     * permission RequestCode
     */
    int REQUEST_WRITE_EXTERNAL_STORAGE = 60;
    int MAX_HEIGHT_AND_WIDTH = 100;

    /*
     * 判断在MusicDetailActivity下动画是否加载完成
     * */
    boolean ON_ANIMATION_FINISH = true;

    /**
     * sharedPrefs tag
     */
    interface SharedPrefsTag {

        //color
        String PRIMARY_COLOR = "mPrimaryColor";
        String PRIMARY_DARK_COLOR = "mPrimaryDarkColor";
        String ACCENT_COLOR = "mAccentColor";
        String TITLE_COLOR = "mTitleColor";

        String DETAIL_BG_STYLE = "DETAIL_BG_STYLE";
        String AUTO_NIGHT_MODE = "NIGHT_MODE";
        String ORDER_TYPE = "ORDER_TYPE";
        String PLAY_TYPE = Values.TYPE_COMMON;

        String ALBUM_LIST_DISPLAY_TYPE = "ALBUM_LIST_DISPLAY_TYPE";
        String ARTIST_LIST_DISPLAY_TYPE = "ARTIST_LIST_DISPLAY_TYPE";
        String ALBUM_LIST_GRID_TYPE_COUNT = "ALBUM_LIST_GRID_TYPE_COUNT";

        //theme
        String SELECT_THEME = "SELECT_THEME";
        String THEME_USE_NOTE = "THEME_USE_NOTE";

        String NOTIFICATION_COLORIZED = "NOTIFICATION_COLORIZED";

        String TRANSPORT_STATUS = "TRANSPORT_STATUS";

        String HIDE_SHORT_SONG = "HIDE_SHORT_SONG";

        String USE_NET_WORK_ALBUM = "USE_NET_WORK_ALBUM";

        /**
         * 1 is MUSIC TAB
         * 2 is ALBUM TAB
         * 3 is ARTIST TAB
         * 4 is PLAYLIST TAB
         * 5 is FILE MANAGER TAB
         * <p>
         * default tab order is: 12345
         */
        String CUSTOM_TAB_LAYOUT = "CUSTOM_TAB_LAYOUT";

        //tips
        /**
         * 提示是否扔进垃圾桶的警告
         */
        String TIP_NOTICE_DROP_TRASH = "TIP_NOTICE_DROP_TRASH";

        String RECYCLER_VIEW_ITEM_STYLE = "RECYCLER_VIEW_ITEM_STYLE";

        String LAST_PLAY_MUSIC_ID = "LAST_PLAY_MUSIC_ID";
    }

    interface BroadCast {
        String ReceiverOnMusicPlay = "top.geek_studio.chenlongcould.musicplayer.broadcasts.ReceiverOnMusicPlay";
        String ReceiverOnMusicStop = "top.geek_studio.chenlongcould.musicplayer.broadcasts.ReceiverOnMusicStop";
        String ReceiverOnMusicPause = "top.geek_studio.chenlongcould.musicplayer.broadcasts.ReceiverOnMusicPause";
        String DayNightReceiver = "top.geek_studio.chenlongcould.musicplayer.broadcasts.DayNightReceiver";
    }

    interface DefaultValues {
        int ANIMATION_DURATION = 300;
    }

    interface Permission {
        String BROAD_CAST = "top.geek_studio.chenlongcould.musicplayer.broadcasts";
    }

    final class LogTAG {
        public static final String LIFT_TAG = "THE_TAG_OF_LIFE";

        public static final String LAG_TAG = "LAG_TAG";
    }

    final class HandlerWhat {

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
    final class ColorName {
        public static final String PRIMARY_COLOR = "mPrimaryColor";

        public static final String PRIMARY_DARK_COLOR = "mPrimaryDarkColor";

        public static final String ACCENT_COLOR = "mAccentColor";
    }

    final class CurrentData {

        /**
         * same as
         * @see UIMODE#MODE_CAR
         * @see UIMODE#MODE_COMMON
         *
         * @deprecated use {@link UIMODE#MODE_COMMON}, {@link UIMODE#MODE_CAR}
         */
        @Deprecated
        public static String MODE_COMMON = UIMODE.MODE_COMMON;
        @Deprecated
        public static String MODE_CAR = UIMODE.MODE_CAR;

        public static String CURRENT_UI_MODE = UIMODE.MODE_COMMON;

        /**
         * TEMP DATA
         */
        //default value -1 or null
        public static int CURRENT_BIND_INDEX_MUSIC_LIST = -1;
        public static int CURRENT_BIND_INDEX_ALBUM_LIST = -1;
        public static int CURRENT_PAGE_INDEX = -1;
        public static int CURRENT_SELECT_ITEM_INDEX_WITH_ITEM_MENU = -1;            //存储使浮动菜单弹出的item,def = -1

        /**
         * 当前序列指针, 指向 {@link Data#sPlayOrderList} 的位置
         */
        public static int CURRENT_MUSIC_INDEX = -1;

        /**
         * 用户手动切歌的播放模式
         * 1. random
         * 2. common
         *
         * auto switch
         * 1. common
         * 2. repeat one
         * 3. repeat list
         * 4. random
         */
        public static String CURRENT_PLAY_TYPE = "COMMON";

        /**
         * current play order (default = all music)
         */
        public static String CURRENT_PLAY_LIST = "default";

    }

    final class UIMODE {
        public static final String MODE_COMMON = "common";
        public static final String MODE_CAR = "car";
    }

    final class Style {
        public static final String STYLE_BACKGROUND_BLUR = "BLUR";
        public static final String STYLE_BACKGROUND_AUTO_COLOR = "AUTO_COLOR";

        public static boolean NIGHT_MODE = false;

        //background style model
        public static String DETAIL_BACKGROUND = STYLE_BACKGROUND_BLUR;
    }

}
