package top.geek_studio.chenlongcould.musicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.RemoteException;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.disposables.Disposable;
import jp.wasabeef.glide.transformations.BlurTransformation;
import top.geek_studio.chenlongcould.geeklibrary.recycler_tools.RecycleViewDivider;
import top.geek_studio.chenlongcould.geeklibrary.theme.Theme;
import top.geek_studio.chenlongcould.musicplayer.activity.CarViewActivity;
import top.geek_studio.chenlongcould.musicplayer.broadcast.MyHeadSetPlugReceiver;
import top.geek_studio.chenlongcould.musicplayer.broadcast.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.model.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.model.ArtistItem;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.model.PlayListItem;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author chenlongcould
 */
public final class Data {

	/**
	 * 存储播放历史(序列) default...
	 */
	public final static List<MusicItem> sHistoryPlayed = new ArrayList<>();

	/**
	 * 垃圾箱 (dislike)
	 */
	public final static List<MusicItem> S_TRASH_CAN_LIST = new ArrayList<>();

	public final static SimpleDateFormat S_SIMPLE_DATE_FORMAT = new SimpleDateFormat("mm:ss", Locale.CHINESE);
	public final static SimpleDateFormat S_SIMPLE_DATE_FORMAT_FILE = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.CHINESE);
	public volatile static boolean HAS_BIND = false;
	/**
	 * 检测app打开后, 是否播放过音乐 (如果没, 默认点击播放按钮为快速随机播放)
	 */
	public volatile static boolean HAS_PLAYED = false;
	public static ArrayList<Disposable> sDisposables = new ArrayList<>();

	/**
	 * data
	 */
	public static List<MusicItem> sMusicItems = new ArrayList<>();
	public static List<MusicItem> sMusicItemsBackUp = new ArrayList<>();
	@Deprecated
	public static CarViewActivity sCarViewActivity = null;
	public static List<AlbumItem> sAlbumItems = new ArrayList<>();
	public static List<AlbumItem> sAlbumItemsBackUp = new ArrayList<>();

	public static List<MusicItem> sPlayOrderList = new ArrayList<>();

	public static Random random = new Random();

	public static void syncPlayOrderList(final Context context, final List<MusicItem> items) {
		Data.sPlayOrderList.clear();
		Data.sPlayOrderList.addAll(items);

		ReceiverOnMusicPlay.startService(context, MusicService.ServiceActions.ACTION_CLEAR_ITEMS);

		for (MusicItem item : Data.sPlayOrderList) {
			try {
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
		final ComponentName serviceName = new ComponentName(context, MusicService.class);

		if (reset) {
			Data.sPlayOrderList.clear();
			Data.sPlayOrderList.addAll(Data.sMusicItems);
			intent.putExtra("random_seed", 0L);
		} else {
			long seed = new Random().nextLong();
			Collections.shuffle(Data.sPlayOrderList, new Random(seed));
			intent.putExtra("random_seed", seed);
		}

		intent.setComponent(serviceName);
		context.startService(intent);
	}

	/**
	 * for {@link top.geek_studio.chenlongcould.musicplayer.activity.ListViewActivity}
	 */
	public static List<MusicItem> sPlayOrderListBackup = new ArrayList<>();
	public static List<PlayListItem> sPlayListItems = new ArrayList<>();
	public static List<ArtistItem> sArtistItems = new ArrayList<>();
	public static List<ArtistItem> sArtistItemsBackUp = new ArrayList<>();
	/**
	 * nextWillPlay
	 * def null
	 */
	public static MusicItem sNextWillPlayItem = null;
	public static Theme sTheme = null;
	/**
	 * sCurrent DATA
	 */
	public static MusicItem sCurrentMusicItem = new MusicItem.Builder(-1, "null", "null").build();

	public static BlurTransformation sBlurTransformation = new BlurTransformation(20, 30);
	public static BlurTransformation sBlurTransformationCarView = new BlurTransformation(5, 10);
	public static MyHeadSetPlugReceiver mMyHeadSetPlugReceiver = new MyHeadSetPlugReceiver();
	/**
	 * public static MusicService.MusicBinder sMusicBinder;
	 */
	public static IMuiscService sMusicBinder;

	public static long shuffleList(final List<MusicItem> items) {
		long seed = new Random().nextLong();
		Random random = new Random(seed);
		Collections.shuffle(items, random);
		return seed;
	}

	/**
	 * save temp bitmap
	 */
	private static Bitmap sCurrentCover = null;

	private static RecyclerView.ItemDecoration mItemDecoration;

	public static RecyclerView.ItemDecoration getItemDecoration(Context context) {
		if (mItemDecoration == null) {
			mItemDecoration = new RecycleViewDivider(
					context, LinearLayoutManager.VERTICAL, 2, ContextCompat.getColor(context, R.color.line_color));
		}
		return mItemDecoration;
	}

	public static Bitmap getCurrentCover() {
		return sCurrentCover;
	}

	public static void setCurrentCover(@NonNull final Bitmap currentCover) {
		sCurrentCover = currentCover;
	}

}
