package top.geek_studio.chenlongcould.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.reactivex.disposables.Disposable;
import jp.wasabeef.glide.transformations.BlurTransformation;
import top.geek_studio.chenlongcould.geeklibrary.recycler_tools.RecycleViewDivider;
import top.geek_studio.chenlongcould.geeklibrary.theme.Theme;
import top.geek_studio.chenlongcould.musicplayer.broadcast.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;

/**
 * @author chenlongcould
 */
public final class Data {

	/**
	 * 垃圾箱 (dislike)
	 * TODO 与 MusicService 同步
	 */
	public final static List<MusicItem> S_TRASH_CAN_LIST = new ArrayList<>();

	public final static SimpleDateFormat S_SIMPLE_DATE_FORMAT = new SimpleDateFormat("mm:ss", Locale.CHINESE);
	public final static SimpleDateFormat S_SIMPLE_DATE_FORMAT_FILE = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.CHINESE);

	private static final String TAG = "Data";

	public volatile static boolean HAS_BIND = false;

	/**
	 * 检测app打开后, 是否播放过音乐 (如果没, 默认点击播放按钮为快速随机播放)
	 */
	public volatile static boolean HAS_PLAYED = false;

	public static ArrayList<Disposable> sDisposables = new ArrayList<>();

	public static List<MusicItem> sMusicItems = new ArrayList<>();

	public static List<MusicItem> sMusicItemsBackUp = new ArrayList<>();

	public static List<MusicItem> sPlayOrderList = new ArrayList<>();
	public static List<MusicItem> sHistoryPlayed = new ArrayList<>();

	public synchronized static void syncPlayOrderList(final Context context, final List<MusicItem> items) {
		Data.sPlayOrderList.clear();
		Data.sPlayOrderList.addAll(items);
		ReceiverOnMusicPlay.startService(context, MusicService.ServiceActions.ACTION_CLEAR_ITEMS);

		// TODO: 2019/5/31 性能问题
		for (MusicItem item : Data.sPlayOrderList) {
			try {
				if (Data.sMusicBinder == null) return;
				Data.sMusicBinder.addToOrderList(item);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	public static int getCurrentIndex() {
		if (Data.sMusicBinder == null) return 0;
		try {
			return sMusicBinder.getCurrentIndex();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static void shuffleOrderListSync(@NonNull final Context context, boolean reset) {
		Intent intent = new Intent(MusicService.ServiceActions.ACTION_SHUFFLE_ORDER_LIST);

		if (reset) {
			Data.sPlayOrderList.clear();
			Data.sPlayOrderList.addAll(Data.sMusicItems);
			intent.putExtra("random_seed", 0L);
		} else {
			long seed = new Random().nextLong();
			Collections.shuffle(Data.sPlayOrderList, new Random(seed));
			intent.putExtra("random_seed", seed);
		}

		ReceiverOnMusicPlay.startService(context, intent);
	}

	/**
	 * for {@link top.geek_studio.chenlongcould.musicplayer.activity.ListViewActivity}
	 */
	public static List<MusicItem> sPlayOrderListBackup = new ArrayList<>();

	public static Theme sTheme = new Theme("null", "null", "null", "null", "null"
			, "null", "null", "null", "null"
			, "null", "null", "null", null);

	/**
	 * sCurrent DATA
	 */
	public static MusicItem sCurrentMusicItem = new MusicItem.Builder(-1, "null", "null").build();

	public static BlurTransformation sBlurTransformation = new BlurTransformation(5, 30);

	public static BlurTransformation sBlurTransformationCarView = new BlurTransformation(5, 10);

	/**
	 * public static MusicService.MusicBinder sMusicBinder;
	 */
	public static IMuiscService sMusicBinder;

	public synchronized static void syncPlayOrderList(final Context context) {
		ReceiverOnMusicPlay.startService(context, MusicService.ServiceActions.ACTION_CLEAR_ITEMS);

		// TODO: 2019/5/31 性能问题
		for (final MusicItem item : Data.sPlayOrderList) {
			try {
				if (Data.sMusicBinder == null) return;
				Data.sMusicBinder.addToOrderList(item);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private static RecyclerView.ItemDecoration mItemDecoration;

	public static RecyclerView.ItemDecoration getItemDecoration(Context context) {
		if (mItemDecoration == null) {
			mItemDecoration = new RecycleViewDivider(
					context, LinearLayoutManager.VERTICAL, 2, ContextCompat.getColor(context, R.color.line_color));
		}
		return mItemDecoration;
	}

	/**
	 * save temp bitmap
	 * TODO 减少内存占用
	 */
	@Nullable
	private static Bitmap sCurrentCover = null;

	@Nullable
	private static Bitmap sCurrentCoverBk = null;

	@Nullable
	public synchronized static Bitmap getCurrentCover() {
		return sCurrentCover == null ? sCurrentCoverBk : sCurrentCover;
	}

	public synchronized static void setCurrentCover(@Nullable final Bitmap currentCover) {
		if (currentCover == null || currentCover.isRecycled()) return;

		sCurrentCover = currentCover;

//		if (sCurrentCover == null && sCurrentCoverBk == null) {
//			sCurrentCover = currentCover;
//			Log.d(TAG, "setCurrentCover: all null");
//			return;
//		}
//
//		if (sCurrentCover != null) {
//			sCurrentCoverBk = currentCover;
//
//			if (!sCurrentCover.isRecycled()) {
//				sCurrentCover.recycle();
//				sCurrentCover = null;
//				Log.d(TAG, "setCurrentCover: recycle sCurrentCover");
//			}
//		} else {
//			sCurrentCover = currentCover;
//
//			if (!sCurrentCoverBk.isRecycled()) {
//				sCurrentCoverBk.recycle();
//				sCurrentCoverBk = null;
//				Log.d(TAG, "setCurrentCover: recycle sCurrentCoverBk");
//			}
//		}
	}

}
