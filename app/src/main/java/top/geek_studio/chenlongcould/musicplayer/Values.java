package top.geek_studio.chenlongcould.musicplayer;

/**
 * @author chenlongcould
 */
public interface Values {

	int DEF_CROSS_FATE_TIME = 500;

	String TYPE_RANDOM = "RANDOM";
	String TYPE_COMMON = "COMMON";

	/**
	 * permission RequestCode
	 * @deprecated use {@link PermissionCode#REQUEST_WRITE_EXTERNAL_STORAGE}
	 */
	@Deprecated
	byte REQUEST_WRITE_EXTERNAL_STORAGE = 60;

	interface PermissionCode {
		byte REQUEST_WRITE_EXTERNAL_STORAGE = 60;
	}

	/**
	 * For {@link top.geek_studio.chenlongcould.geeklibrary.theme.ThemeStore.ThemeColumns#THUMBNAIL}
	 * height and width
	 */
	byte MAX_HEIGHT_AND_WIDTH = 100;

	/**
	 * sharedPrefs tag
	 */
	interface SharedPrefsTag {

		/**
		 * color
		 */
		String PRIMARY_COLOR = "mPrimaryColor";
		String PRIMARY_DARK_COLOR = "mPrimaryDarkColor";
		String ACCENT_COLOR = "mAccentColor";
		String TITLE_COLOR = "mTitleColor";

		String DETAIL_BG_STYLE = "DETAIL_BG_STYLE";
		String ORDER_TYPE = "ORDER_TYPE";
		String PLAY_TYPE = Values.TYPE_COMMON;

		String ALBUM_LIST_DISPLAY_TYPE = "ALBUM_LIST_DISPLAY_TYPE";
		String ARTIST_LIST_DISPLAY_TYPE = "ARTIST_LIST_DISPLAY_TYPE";
		String ALBUM_LIST_GRID_TYPE_COUNT = "ALBUM_LIST_GRID_TYPE_COUNT";

		String SELECT_THEME = "SELECT_THEME";
		String THEME_USE_NOTE = "THEME_USE_NOTE";
		String LOAD_DEFAULT_THEME = "LOAD_DEFAULT_THEME";

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

		/**
		 * 提示是否扔进垃圾桶的警告
		 */
		String TIP_NOTICE_DROP_TRASH = "TIP_NOTICE_DROP_TRASH";

		@Deprecated
		String RECYCLER_VIEW_ITEM_STYLE = "RECYCLER_VIEW_ITEM_STYLE";

		/**
		 * 存储最后播放的音乐的Id
		 */
		String LAST_PLAY_MUSIC_ID = "LAST_PLAY_MUSIC_ID";

		String TRASH_CAN_INFO = "TRASH_CAN_INFO";
		String SHOW_NOTICE_ADD_TAB = "SHOW_NOTICE_ADD_TAB";
		String ALBUM_LOCK_SCREEN = "ALBUM_LOCK_SCREEN";
		String BLUR_ALBUM_LOCK_SCREEN = "BLUR_ALBUM_LOCK_SCREEN";
		String DARK_MODE = "DARK_MODE";
	}

	interface IntentTAG {
		String SHORTCUT_TYPE = "SHORTCUT_TYPE";
	}

	interface BroadCast {
		String ReceiverOnMusicPlay = "top.geek_studio.chenlongcould.musicplayer.broadcast.ReceiverOnMusicPlay";
	}

	interface DefaultValues {
		int ANIMATION_DURATION = 300;
	}

	interface Permission {
		String BROAD_CAST = "top.geek_studio.chenlongcould.musicplayer.broadcast";
	}

	final class CurrentData {

		public static String CURRENT_UI_MODE = UIMODE.MODE_COMMON;

		/**
		 * TEMP DATA
		 * default value -1 or null
		 */
		public static int CURRENT_PAGE_INDEX = 0;

	}

	final class UIMODE {
		public static final String MODE_COMMON = "common";
		public static final String MODE_CAR = "car";
	}

	/**
	 * 对于 {@link top.geek_studio.chenlongcould.musicplayer.fragment.MusicDetailFragment} 中的背景进行样式设定
	 *
	 * @deprecated use {@link top.geek_studio.chenlongcould.musicplayer.fragment.MusicDetailFragment.BackgroundStyle}
	 */
	@Deprecated
	final class BackgroundStyle {
		public static final String STYLE_BACKGROUND_BLUR = "BLUR";
		public static final String STYLE_BACKGROUND_AUTO_COLOR = "AUTO_COLOR";

		/**
		 * background style model
		 */
		public static String DETAIL_BACKGROUND = STYLE_BACKGROUND_BLUR;
	}

	final class TEMP {
		public static boolean switchNightDone = false;
	}
}
