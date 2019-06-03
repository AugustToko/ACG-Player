package top.geek_studio.chenlongcould.musicplayer.activity;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.DataBindingUtil;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.snackbar.Snackbar;
import org.litepal.LitePal;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.database.Detail;
import top.geek_studio.chenlongcould.musicplayer.databinding.ActivityInfoDetailBinding;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class DetailActivity extends BaseCompatActivity {

	public static final String TAG = "DetailActivity";

	private ActivityInfoDetailBinding detailBinding;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		detailBinding = DataBindingUtil.setContentView(this, R.layout.activity_info_detail);
		super.initView(detailBinding.toolbar, detailBinding.appbar);
		super.onCreate(savedInstanceState);

		setSupportActionBar(detailBinding.toolbar);
		detailBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

		initStyle();

		List<Detail> infos = LitePal.findAll(Detail.class);

		if (infos.size() != 0) {
			ArrayList<MusicItem> itemHelperList = new ArrayList<>();
			for (MusicItem item : Data.sMusicItems) {
				for (Detail info : infos) {
					if (info.getMusicId() == item.getMusicID()) {
						itemHelperList.add(item);
					}
				}
			}

			MusicItem itemTimes = null;
			MusicItem itemDuration = null;
			MusicItem itemMinimum = null;
			int maxTimes = -1;
			int minimumTimes = -1;
			long maxDuration = -1;

			int totalDuration = 0;
			int totalTimes = 0;
			for (Detail info : infos) {
				if (info.getPlayTimes() > maxTimes) {
					maxTimes = info.getPlayTimes();
				}
				if (info.getMinimumPlayTimes() > minimumTimes) {
					minimumTimes = info.getMinimumPlayTimes();
				}
				if (info.getPlayDuration() > maxDuration) {
					maxDuration = info.getPlayDuration();
				}
			}
			for (Detail detail : infos) {
				totalDuration += detail.getPlayDuration();
				totalTimes += detail.getPlayTimes();
				if (detail.getPlayTimes() == maxTimes) {
					if (itemTimes == null) {
						for (MusicItem item : itemHelperList) {
							if (item.getMusicID() == detail.getMusicId()) {
								itemTimes = item;
								break;
							}
						}
					}
				}

				if (detail.getPlayDuration() == maxDuration) {
					if (itemDuration == null) {
						for (MusicItem item : itemHelperList) {
							if (item.getMusicID() == detail.getMusicId()) {
								itemDuration = item;
								break;
							}
						}
					}
				}

				if (detail.getMinimumPlayTimes() == minimumTimes) {
					if (itemMinimum == null) {
						for (MusicItem item : itemHelperList) {
							if (item.getMusicID() == detail.getMusicId()) {
								itemMinimum = item;
								break;
							}
						}
					}
				}
			}

			detailBinding.includeContent.textTotalPlays.setText(detailBinding.includeContent.textTotalPlays
					.getText() + String.valueOf(totalTimes) + " times");
			detailBinding.includeContent.textTotalPlayTime.setText(detailBinding.includeContent.textTotalPlayTime
					.getText() + String.valueOf(Data.S_SIMPLE_DATE_FORMAT.format(new Date(totalDuration))) + " (mm:ss)");

			detailBinding.includeContent.t1.setText(detailBinding.includeContent.t1.getText()
					+ String.valueOf(maxTimes) + " times");
			detailBinding.includeContent.t2.setText(detailBinding.includeContent.t2.getText()
					+ String.valueOf(Data.S_SIMPLE_DATE_FORMAT.format(new Date(maxDuration)))
					+ " (mm:ss)");
			detailBinding.includeContent.t3.setText(detailBinding.includeContent.t3.getText()
					+ String.valueOf(minimumTimes) + " times");

			if (itemDuration != null) {
				Log.d(TAG, "onCreate: " + itemDuration.getMusicName());

				detailBinding.includeContent.includeItemPlayDuration.recyclerItemMusicName.setText(itemDuration.getMusicName());
				detailBinding.includeContent.includeItemPlayDuration.recyclerItemMusicAlbumName.setText(itemDuration.getMusicAlbum());
				detailBinding.includeContent.includeItemPlayDuration.recyclerItemMusicDuration.setText(String.valueOf(Data.S_SIMPLE_DATE_FORMAT.format(new Date(itemDuration.getDuration()))));
				final String prefix = itemDuration.getMusicPath().substring(itemDuration.getMusicPath().lastIndexOf(".") + 1);
				detailBinding.includeContent.includeItemPlayDuration.recyclerItemMusicTypeName.setText(prefix);
				GlideApp.with(this).load(Utils.Audio.getCoverBitmapFull(this, itemDuration.getAlbumId()))
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						.into(detailBinding.includeContent.includeItemPlayDuration.recyclerItemAlbumImage);

				detailBinding.includeContent.includeItemPlayDuration.recyclerItemMenu.setVisibility(View.GONE);
			}

			if (itemMinimum != null) {
				Log.d(TAG, "onCreate: " + itemMinimum.getMusicName());

				detailBinding.includeContent.includeItemPlayMinitimes.recyclerItemMusicName.setText(itemMinimum.getMusicName());
				detailBinding.includeContent.includeItemPlayMinitimes.recyclerItemMusicAlbumName.setText(itemMinimum.getMusicAlbum());
				detailBinding.includeContent.includeItemPlayMinitimes.recyclerItemMusicDuration.setText(String.valueOf(Data.S_SIMPLE_DATE_FORMAT.format(new Date(itemMinimum.getDuration()))));
				final String prefix = itemMinimum.getMusicPath().substring(itemMinimum.getMusicPath().lastIndexOf(".") + 1);
				detailBinding.includeContent.includeItemPlayMinitimes.recyclerItemMusicTypeName.setText(prefix);
				GlideApp.with(this).load(Utils.Audio.getCoverBitmapFull(this, itemMinimum.getAlbumId()))
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						.into(detailBinding.includeContent.includeItemPlayMinitimes.recyclerItemAlbumImage);

				detailBinding.includeContent.includeItemPlayMinitimes.recyclerItemMenu.setVisibility(View.GONE);
			}

			if (itemTimes != null) {
				Log.d(TAG, "onCreate: " + itemTimes.getMusicName());


				detailBinding.includeContent.includeItemPlaytimes.recyclerItemMusicName.setText(itemTimes.getMusicName());
				detailBinding.includeContent.includeItemPlaytimes.recyclerItemMusicAlbumName.setText(itemTimes.getMusicAlbum());
				detailBinding.includeContent.includeItemPlaytimes.recyclerItemMusicDuration.setText(String.valueOf(Data.S_SIMPLE_DATE_FORMAT.format(new Date(itemTimes.getDuration()))));
				final String prefix = itemTimes.getMusicPath().substring(itemTimes.getMusicPath().lastIndexOf(".") + 1);
				detailBinding.includeContent.includeItemPlaytimes.recyclerItemMusicTypeName.setText(prefix);
				GlideApp.with(this).load(Utils.Audio.getCoverBitmapFull(this, itemTimes.getAlbumId()))
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						.into(detailBinding.includeContent.includeItemPlaytimes.recyclerItemAlbumImage);

				detailBinding.includeContent.includeItemPlaytimes.recyclerItemMenu.setVisibility(View.GONE);
			}

		} else {
//			Toast.makeText(this, "Database is empty!", Toast.LENGTH_SHORT).show();
			ViewGroup group = (ViewGroup) detailBinding.getRoot();
			group.removeView(detailBinding.includeContent.body);
			TextView textView = new TextView(this);
			textView.setText(getString(R.string.data_overview_empty));
			textView.setGravity(Gravity.CENTER);
			textView.setTextSize(30);
			group.addView(textView);
			CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) textView.getLayoutParams();
			params.gravity = Gravity.CENTER;
			textView.setLayoutParams(params);
		}

		detailBinding.fab.setOnClickListener(view -> Snackbar.make(view, "Clear?", Snackbar.LENGTH_LONG)
				.setAction(getString(R.string.sure), v -> {
					LitePal.deleteAll(Detail.class);
					recreate();
				}).show());
	}
	
	@Override
	public String getActivityTAG() {
		return TAG;
	}

	@Override
	public void inflateCommonMenu() {

	}

	@Override
	public void inflateChooseMenu() {

	}

	@Override
	public void initStyle() {
		super.initStyle();
		@ColorInt int accentColor = Utils.Ui.getAccentColor(this);
		detailBinding.fab.setSupportBackgroundTintList(ColorStateList.valueOf(accentColor));
		detailBinding.fab.setColorFilter(Utils.Ui.isColorLight(accentColor) ? Color.BLACK : Color.WHITE);
	}
}
