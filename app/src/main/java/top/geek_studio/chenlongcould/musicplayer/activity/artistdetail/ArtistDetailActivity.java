package top.geek_studio.chenlongcould.musicplayer.activity.artistdetail;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import org.litepal.LitePal;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.base.BaseListActivity;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.database.ArtistArtPath;
import top.geek_studio.chenlongcould.musicplayer.databinding.ActivityArtistDetailOthBinding;
import top.geek_studio.chenlongcould.musicplayer.misc.SimpleObservableScrollViewCallbacks;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

import java.util.List;

/**
 * a activity that show Music Album Detail data
 * <p>
 * has dataBinding
 *
 * @author chenlongcould
 * @apiNote some by others
 */
public final class ArtistDetailActivity extends BaseListActivity implements ArtistDetailContract.View {

	public static final String TAG = "ArtistDetailActivity";

	private ActivityArtistDetailOthBinding mArtistDetailOthBinding;

	private int headerViewHeight;

	private ArtistDetailContract.Presenter mPresenter;

	@ColorInt
	private int toolbarColor;
	private final SimpleObservableScrollViewCallbacks observableScrollViewCallbacks = new SimpleObservableScrollViewCallbacks() {
		@Override
		public void onScrollChanged(int scrollY, boolean b, boolean b2) {
			scrollY += headerViewHeight;

			// Change alpha of overlay
			float headerAlpha = Math.max(0, Math.min(1, (float) 2 * scrollY / headerViewHeight));
			mArtistDetailOthBinding.headerOverlay.setBackgroundColor(Utils.Ui.withAlpha(toolbarColor, headerAlpha));

			// Translate name text
			mArtistDetailOthBinding.header.setTranslationY(Math.max(-scrollY, -headerViewHeight));
			mArtistDetailOthBinding.headerOverlay.setTranslationY(Math.max(-scrollY, -headerViewHeight));
			mArtistDetailOthBinding.image.setTranslationY(Math.max(-scrollY, -headerViewHeight));
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mArtistDetailOthBinding = DataBindingUtil.setContentView(this, R.layout.activity_artist_detail_oth);
		mArtistDetailOthBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
		headerViewHeight = getResources().getDimensionPixelSize(R.dimen.detail_header_height);

		new ArtistDetailPresenter(this);

		initData();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mPresenter.close();
	}
	
	@Override
	public String getActivityTAG() {
		return TAG;
	}

	private void initData() {
		final Intent intent = getIntent();

		final String artistName = intent.getStringExtra("key");
		final String intentArtistId = String.valueOf(intent.getIntExtra("_id", -10));

		mArtistDetailOthBinding.toolbar.setTitle(artistName);

		final List<ArtistArtPath> paths = LitePal.where("mArtistId = ?", intentArtistId).find(ArtistArtPath.class);
		if (paths.size() > 0) {
			ArtistArtPath art = paths.get(0);
			if (!"null".equals(art.getArtistArt())) {
				final Bitmap bitmap = Utils.Ui.readBitmapFromFile(paths.get(0).getArtistArt(), 50, 50);
				if (bitmap != null) {
					toolbarColor = Palette.from(bitmap).generate().getVibrantColor(Utils.Ui.getPrimaryColor(this));
				} else {
					toolbarColor = Utils.Ui.getPrimaryColor(this);
				}
				setUpColor();
				GlideApp.with(this)
						.load(art.getArtistArt())
						.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						.into(mArtistDetailOthBinding.image);
			} else {
				toolbarColor = Utils.Ui.getPrimaryColor(this);
				setUpColor();
				GlideApp.with(this)
						.load(R.drawable.default_album_art)
						.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						.into(mArtistDetailOthBinding.image);
			}
		} else {
			toolbarColor = Utils.Ui.getPrimaryColor(this);
			setUpColor();
			GlideApp.with(this)
					.load(R.drawable.default_album_art)
					.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.into(mArtistDetailOthBinding.image);
		}

		mArtistDetailOthBinding.recyclerView.setPadding(0, headerViewHeight, 0, 0);
		mArtistDetailOthBinding.recyclerView.setScrollViewCallbacks(observableScrollViewCallbacks);
		final View contentView = getWindow().getDecorView().findViewById(android.R.id.content);
		contentView.post(() -> observableScrollViewCallbacks.onScrollChanged(-headerViewHeight, false, false));
		mArtistDetailOthBinding.recyclerView.setLayoutManager(new GridLayoutManager(ArtistDetailActivity.this, 1));
		final MyRecyclerAdapter adapter = new MyRecyclerAdapter(this, mPresenter.getSongs(), new MyRecyclerAdapter.Config(preferences.getInt(Values.SharedPrefsTag.RECYCLER_VIEW_ITEM_STYLE, 0), false));
		mArtistDetailOthBinding.recyclerView.setAdapter(adapter);
		adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onChanged() {
				super.onChanged();
				if (adapter.getItemCount() == 0) {
					finish();
				}
			}
		});

		mPresenter.start();
	}

	private void setUpColor() {
		mArtistDetailOthBinding.toolbar.setBackgroundColor(toolbarColor);
		mArtistDetailOthBinding.appbar.setBackgroundColor(toolbarColor);
		mArtistDetailOthBinding.header.setBackgroundColor(toolbarColor);
		if (Utils.Ui.isColorLight(toolbarColor)) {
			Utils.Ui.setOverToolbarColor(mArtistDetailOthBinding.toolbar, Color.BLACK);
			mArtistDetailOthBinding.songCountText.setTextColor(Color.BLACK);
			mArtistDetailOthBinding.artistAlbumCountText.setTextColor(Color.BLACK);
			mArtistDetailOthBinding.durationText.setTextColor(Color.BLACK);

			mArtistDetailOthBinding.songCountIcon.setColorFilter(Color.BLACK);
			mArtistDetailOthBinding.durationIcon.setColorFilter(Color.BLACK);
			mArtistDetailOthBinding.artistAlbumCount.setColorFilter(Color.BLACK);
		} else {
			Utils.Ui.setOverToolbarColor(mArtistDetailOthBinding.toolbar, Color.WHITE);
			mArtistDetailOthBinding.songCountText.setTextColor(Color.WHITE);
			mArtistDetailOthBinding.artistAlbumCountText.setTextColor(Color.WHITE);
			mArtistDetailOthBinding.durationText.setTextColor(Color.WHITE);

			mArtistDetailOthBinding.songCountIcon.setColorFilter(Color.WHITE);
			mArtistDetailOthBinding.durationIcon.setColorFilter(Color.WHITE);
			mArtistDetailOthBinding.artistAlbumCount.setColorFilter(Color.WHITE);
		}
	}

	@Override
	public void setPresenter(ArtistDetailContract.Presenter presenter) {
		mPresenter = presenter;
	}

	@Override
	public void setSongCountText(@NonNull String data) {
		mArtistDetailOthBinding.songCountText.setText(data);
	}

	@Override
	public void setDurationText(@NonNull String data) {
		mArtistDetailOthBinding.durationText.setText(data);
	}

	@Override
	public void setAlbumCountText(@NonNull String data) {
		mArtistDetailOthBinding.artistAlbumCountText.setText(data);
	}

	@Override
	public void notifyDataSetChanged() {
		mArtistDetailOthBinding.recyclerView.getAdapter().notifyDataSetChanged();
	}

	@NonNull
	@Override
	public Context getContext() {
		return this;
	}

	@Override
	public void sendEmptyMessage(int what) {

	}

	@Override
	public void sendMessage(Message message) {

	}
}
