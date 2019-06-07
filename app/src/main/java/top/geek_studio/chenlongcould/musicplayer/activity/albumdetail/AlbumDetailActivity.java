package top.geek_studio.chenlongcould.musicplayer.activity.albumdetail;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Toast;
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
import top.geek_studio.chenlongcould.musicplayer.database.CustomAlbumPath;
import top.geek_studio.chenlongcould.musicplayer.databinding.ActivityAlbumDetailOthBinding;
import top.geek_studio.chenlongcould.musicplayer.misc.SimpleObservableScrollViewCallbacks;
import top.geek_studio.chenlongcould.musicplayer.model.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

import java.util.List;

/**
 * a activity that show Music Album Detail data
 * has dataBinding
 *
 * @author chenlongcould
 * @apiNote some by others
 */
public final class AlbumDetailActivity extends BaseListActivity implements AlbumDetailContract.View {
	
	public static final String TAG = "AlbumDetailActivity";
	
	private MyRecyclerAdapter mAdapter;

	private AlbumDetailContract.Presenter mPresenter;
	
	private final SimpleObservableScrollViewCallbacks observableScrollViewCallbacks = new SimpleObservableScrollViewCallbacks() {
		@Override
		public void onScrollChanged(int scrollY, boolean b, boolean b2) {
			scrollY += headerViewHeight;
			
			// Change alpha of overlay
			float headerAlpha = Math.max(0, Math.min(1, (float) 2 * scrollY / headerViewHeight));
			mAlbumDetailBinding.headerOverlay.setBackgroundColor(Utils.Ui.withAlpha(toolbarColor, headerAlpha));
			
			// Translate name text
			mAlbumDetailBinding.header.setTranslationY(Math.max(-scrollY, -headerViewHeight));
			mAlbumDetailBinding.headerOverlay.setTranslationY(Math.max(-scrollY, -headerViewHeight));
			mAlbumDetailBinding.image.setTranslationY(Math.max(-scrollY, -headerViewHeight));
		}
	};
	
	private ActivityAlbumDetailOthBinding mAlbumDetailBinding;
	
	private int headerViewHeight;
	
	@ColorInt
	private int toolbarColor;

	private String intentAlbumId = "0";

	private List<CustomAlbumPath> paths;
	
	@Override
	public void inflateCommonMenu() {
		mAlbumDetailBinding.toolbar.inflateMenu(R.menu.menu_toolbar_album_detail);
		//update menu checkbox
		if (!AlbumItem.DEFAULT_ALBUM_ID.equals(intentAlbumId) && paths.size() > 0) {
			mAlbumDetailBinding.toolbar.getMenu().findItem(R.id.menu_toolbar_album_force_album).setChecked(paths.get(0).isForceUse());
		}
		
		mAlbumDetailBinding.toolbar.setOnMenuItemClickListener(menuItem -> {
			switch (menuItem.getItemId()) {
				case R.id.menu_toolbar_album_force_album: {
					if (!AlbumItem.DEFAULT_ALBUM_ID.equals(intentAlbumId)) {
						if (paths.size() > 0) {
							final CustomAlbumPath customAlbumPath = paths.get(0);
							if (menuItem.isChecked()) {
								menuItem.setChecked(false);
								customAlbumPath.setForceUse(false);
							} else {
								menuItem.setChecked(true);
								customAlbumPath.setForceUse(true);
							}
							customAlbumPath.save();
						} else {
							Toast.makeText(getApplicationContext(), "CustomAlbumPath DB is empty."
									, Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(this, "Album Id Error..., finishing...", Toast.LENGTH_SHORT).show();
						finish();
					}
				}
				break;
				default:
			}
			return true;
		});
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAlbumDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_album_detail_oth);
		mAlbumDetailBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
		headerViewHeight = getResources().getDimensionPixelSize(R.dimen.detail_header_height);

		new AlbumDetailPresenter(this);

		init();
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
	
	@Override
	public void inflateChooseMenu() {
	
	}

	private void setViewData(Intent intent) {

		final String albumTitle = intent.getStringExtra(IntentKey.ALBUM_NAME);
		mAlbumDetailBinding.toolbar.setTitle(albumTitle);
		int sourceAlbumId = intent.getIntExtra(IntentKey.ID, -1);
		if (sourceAlbumId == -1) {
			Toast.makeText(this, "When get your album id there was an error.", Toast.LENGTH_SHORT).show();
			finish();
		}
		intentAlbumId = String.valueOf(sourceAlbumId);
		paths = LitePal.where("mAlbumId = ?", intentAlbumId).find(CustomAlbumPath.class);

		setupAlbumCover(intent);
		inflateCommonMenu();

		mAlbumDetailBinding.recyclerView.setPadding(0, headerViewHeight, 0, 0);
		mAlbumDetailBinding.recyclerView.setScrollViewCallbacks(observableScrollViewCallbacks);
		final View contentView = getWindow().getDecorView().findViewById(android.R.id.content);
		contentView.post(() -> observableScrollViewCallbacks.onScrollChanged(-headerViewHeight, false, false));
		mAlbumDetailBinding.recyclerView.setLayoutManager(new GridLayoutManager(AlbumDetailActivity.this, 1));
		mAdapter = new MyRecyclerAdapter(this, mPresenter.getSongs(), new MyRecyclerAdapter.Config(preferences.getInt(Values.SharedPrefsTag.RECYCLER_VIEW_ITEM_STYLE, 0), false));
		mAlbumDetailBinding.recyclerView.setAdapter(mAdapter);
		mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onChanged() {
				super.onChanged();
				if (mAdapter.getItemCount() == 0) {
					finish();
				}
			}
		});
	}
	
	/**
	 * loadData
	 */
	private void init() {
		final Intent intent = getIntent();
		if (intent == null) {
			return;
		}

		setViewData(intent);

		mPresenter.start();
	}
	
	private void setUpColor() {
		mAlbumDetailBinding.toolbar.setBackgroundColor(toolbarColor);
		mAlbumDetailBinding.appbar.setBackgroundColor(toolbarColor);
		mAlbumDetailBinding.header.setBackgroundColor(toolbarColor);
		if (Utils.Ui.isColorLight(toolbarColor)) {
			Utils.Ui.setOverToolbarColor(mAlbumDetailBinding.toolbar, Color.BLACK);
			mAlbumDetailBinding.artistText.setTextColor(Color.BLACK);
			mAlbumDetailBinding.songCountText.setTextColor(Color.BLACK);
			mAlbumDetailBinding.albumYearText.setTextColor(Color.BLACK);
			mAlbumDetailBinding.durationText.setTextColor(Color.BLACK);
			
			mAlbumDetailBinding.artistIcon.setColorFilter(Color.BLACK);
			mAlbumDetailBinding.songCountIcon.setColorFilter(Color.BLACK);
			mAlbumDetailBinding.durationIcon.setColorFilter(Color.BLACK);
			mAlbumDetailBinding.albumYearIcon.setColorFilter(Color.BLACK);
		} else {
			Utils.Ui.setOverToolbarColor(mAlbumDetailBinding.toolbar, Color.WHITE);
			mAlbumDetailBinding.artistText.setTextColor(Color.WHITE);
			mAlbumDetailBinding.songCountText.setTextColor(Color.WHITE);
			mAlbumDetailBinding.albumYearText.setTextColor(Color.WHITE);
			mAlbumDetailBinding.durationText.setTextColor(Color.WHITE);
			
			mAlbumDetailBinding.artistIcon.setColorFilter(Color.WHITE);
			mAlbumDetailBinding.songCountIcon.setColorFilter(Color.WHITE);
			mAlbumDetailBinding.durationIcon.setColorFilter(Color.WHITE);
			mAlbumDetailBinding.albumYearIcon.setColorFilter(Color.WHITE);
		}
	}
	
	private void setupAlbumCover(Intent intent) {
		//获取MainAlbum图像
		int id = intent.getIntExtra(IntentKey.ID, -1);
		if (id == -1) {
			Toast.makeText(this, "Can not set up album cover.", Toast.LENGTH_SHORT).show();
			return;
		}
		final Bitmap bitmap = Utils.Audio.getCoverBitmapFull(this, id);
		if (bitmap != null) {
			toolbarColor = Palette.from(bitmap).generate().getVibrantColor(Utils.Ui.getPrimaryColor(this));
		} else {
			toolbarColor = Utils.Ui.getPrimaryColor(this);
		}
		setUpColor();
		GlideApp.with(this)
				.load(bitmap)
				.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
				.diskCacheStrategy(DiskCacheStrategy.NONE)
				.into(mAlbumDetailBinding.image);
	}

	@Override
	public void setPresenter(AlbumDetailContract.Presenter presenter) {
		mPresenter = presenter;
	}

	@Override
	public Intent getAlbumDetailIntent() {
		return getIntent();
	}

	@Override
	public Context getContext() {
		return this;
	}

	@Override
	public void notifyDataSetChanged() {
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void setSongCountText(@NonNull String data) {
		mAlbumDetailBinding.songCountText.setText(data);
	}

	@Override
	public void setDurationText(@NonNull String data) {
		mAlbumDetailBinding.durationText.setText(data);
	}

	@Override
	public void setAlbumYearText(@NonNull String data) {
		mAlbumDetailBinding.albumYearText.setText(data);
	}

	@Override
	public void setArtistText(@NonNull String data) {
		mAlbumDetailBinding.artistText.setText(data);
	}

	@Override
	public void sendEmptyMessage(int what) {

	}

	@Override
	public void sendMessage(Message message) {

	}

	public interface IntentKey {
		String ID = "_id";
		String ALBUM_NAME = "key";
	}
	
}
