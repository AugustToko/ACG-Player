package top.geek_studio.chenlongcould.musicplayer;

import android.content.Context;
import android.os.RemoteException;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jp.wasabeef.glide.transformations.BlurTransformation;
import top.geek_studio.chenlongcould.geeklibrary.recycler_tools.RecycleViewDivider;
import top.geek_studio.chenlongcould.geeklibrary.theme.Theme;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;

/**
 * @author chenlongcould
 */
public final class Data {

	private static final String TAG = "Data";

	public volatile static boolean HAS_BIND = false;

	/**
	 * 检测app打开后, 是否播放过音乐 (如果没, 默认点击播放按钮为快速随机播放)
	 */
	public volatile static boolean HAS_PLAYED = false;

	public final static SimpleDateFormat S_SIMPLE_DATE_FORMAT = new SimpleDateFormat("mm:ss", Locale.CHINESE);

	public final static SimpleDateFormat S_SIMPLE_DATE_FORMAT_FILE = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.CHINESE);

	public static BlurTransformation sBlurTransformation = new BlurTransformation(5, 30);

	public static BlurTransformation sBlurTransformationCarView = new BlurTransformation(5, 10);

	private static RecyclerView.ItemDecoration mItemDecoration;

	public static RecyclerView.ItemDecoration getItemDecoration(Context context) {
		if (mItemDecoration == null) {
			mItemDecoration = new RecycleViewDivider(
					context, LinearLayoutManager.VERTICAL, 2, ContextCompat.getColor(context, R.color.line_color));
		}
		return mItemDecoration;
	}

	//////////////////////////////////////////////////////////////

	public static List<MusicItem> sPlayOrderList = new ArrayList<>();

	public static List<MusicItem> sHistoryPlayed = new ArrayList<>();

	public static IMuiscService sMusicBinder;

	public static int getCurrentIndex() {
		if (Data.sMusicBinder == null) return 0;
		try {
			return sMusicBinder.getCurrentIndex();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * for {@link top.geek_studio.chenlongcould.musicplayer.activity.ListViewActivity}
	 */
	public static List<MusicItem> sPlayOrderListBackup = new ArrayList<>();

	public static Theme sTheme = new Theme("null", "null", "null", "null", "null"
			, "null", "null", "null", "null"
			, "null", "null", "null", null);

}
