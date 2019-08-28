package top.geek_studio.chenlongcould.musicplayer.adapter;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.albumdetail.AlbumDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.main.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.misc.BlurringView;
import top.geek_studio.chenlongcould.musicplayer.model.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.threadPool.CustomThreadPool;

public final class MyRecyclerAdapter2AlbumList extends RecyclerView.Adapter<MyRecyclerAdapter2AlbumList.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

	/**
	 * 是否停止加载图片
	 */
	public static boolean stopLoadImage = false;

	public static final int LINEAR_TYPE = 0;
	public static final int GRID_TYPE = 1;
	private static final String TAG = "AlbumAdapter";
	private static int mType = LINEAR_TYPE;

	private List<AlbumItem> mAlbumNameList;

	private MainActivity mMainActivity;

	/**
	 * the json data, from network
	 */
	private static final String RESULT_OK = "ok";

	public MyRecyclerAdapter2AlbumList(MainActivity activity, List<AlbumItem> albumNameList, int type) {
		this.mAlbumNameList = albumNameList;
		mMainActivity = activity;
		mType = type;
	}

	public static final int TYPE_PADDING = 1;

	@Override
	public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
		if (viewHolder.getAdapterPosition() == 0 || viewHolder.getAdapterPosition() == 1)
			viewHolder.itemView.setPadding(0, MainActivity.PADDING, 0, 0);
		else viewHolder.itemView.setPadding(0, 0, 0, 0);

		viewHolder.mAlbumText.setText(mAlbumNameList.get(viewHolder.getAdapterPosition()).getAlbumName());

		if (stopLoadImage) return;

		// set tag
		if (mAlbumNameList.get(viewHolder.getAdapterPosition()).getmArtwork() != null) {
			viewHolder.mImageView.setTag(R.string.key_id_3, mAlbumNameList.get(viewHolder.getAdapterPosition()).getmArtwork());
			CustomThreadPool.post(() -> GlideApp.with(mMainActivity)
							.load(viewHolder.mImageView.getTag(R.string.key_id_3))
//					.load(path)
							.diskCacheStrategy(DiskCacheStrategy.NONE)
							.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
							.into(new SimpleTarget<Drawable>() {
								@Override
								public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
									mMainActivity.runOnUiThread(() -> {
										viewHolder.mImageView.setImageDrawable(resource);
										viewHolder.invalidate();
									});
								}
							})
			);
		}
//		else {
//			final AlbumItem albumItem = mAlbumNameList.get(viewHolder.getAdapterPosition());
//			albumLoader(mMainActivity, viewHolder, albumItem);
//		}
	}

//	private void albumLoader(@NonNull final Context activity, @NonNull final ViewHolder holder, final AlbumItem albumItem) {
//
//		final ImageView imageView = holder.mImageView;
//
//		final int albumId = albumItem.getAlbumId();
//		final String artist = albumItem.getArtist();
//		final String albumName = albumItem.getAlbumName();
//
//		final String[] albumPath = {null};
//
//		// load from def-db
////		final Cursor cursor = activity.getContentResolver().query(
////				Uri.parse(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI + String.valueOf(File.separatorChar) + albumId)
////				, new String[]{MediaStore.Audio.Albums.ALBUM_ART}, null, null, null);
////
////		if (cursor != null && cursor.getCount() != 0) {
////			cursor.moveToFirst();
////			albumPath[0] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
////			cursor.close();
////		} else {
////			Log.d(TAG, "albumLoader: the DEFAULT_DB is null or empty!");
////		}
////
////		final String baseCoverPath = albumPath[0];
////
////		if (baseCoverPath != null && !TextUtils.isEmpty(baseCoverPath)) {
////			final File file = new File(baseCoverPath);
////			if (file.exists()) {
////				Log.d(TAG, "albumLoader: the album id DEFAULT_DB is ability, loading def");
////				loadPath2ImageView(baseCoverPath, holder);
////			} else {
////				//load default res
////				imageView.post(() -> GlideApp.with(imageView)
////						.load(R.drawable.default_album_art)
////						.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
////						.centerCrop()
////						.override(100, 100)
////						.diskCacheStrategy(DiskCacheStrategy.NONE)
////						.into(imageView));
////			}
////		} else {
//
//		Log.d(TAG, "albumLoader: the album id DEFAULT_DB is NOT ability, loading from {network or diskCache}");
//
//		//检查是否勾选了网络Album
//		if (PreferenceUtil.getDefault(activity).getBoolean(Values.SharedPrefsTag.USE_NET_WORK_ALBUM, false)) {
//			final List<CustomAlbumPath> customs = LitePal.where("mAlbumId = ?", String.valueOf(albumId)).find(CustomAlbumPath.class);
//
//			//检测DB是否准备完成(IntentService 是否完成)
//			if (customs.size() != 0) {
//				final CustomAlbumPath custom = customs.get(0);
//
//				try {
//					final File file = new File(custom.getAlbumArt());
//
//					//判断CUSTOM_DB下albumArt是否存在
//					if ("null".equals(custom.getAlbumArt()) && !file.exists()) {
//
//						final String mayPath = MyRecyclerAdapter.Loader.ifExists(albumId);
//
//						if (mayPath != null) {
//							Log.d(TAG, "onBindViewHolder: (in CUSTOM_DB) DB not ability, path is ability" +
//									", save in db and loading...");
//
//							custom.setAlbumArt(mayPath);
//							custom.save();
//							loadPath2ImageView(mayPath, holder);
//						} else {
//							//DB内不存在Cover, 且缓存也不存在, 进行下载
//							final String request = "http://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=" +
//									App.LAST_FM_KEY +
//									"&artist=" +
//									artist +
//									"&album=" +
//									albumName;
//
//							HttpUtil.sedOkHttpRequest(request, new Callback() {
//								@Override
//								public void onFailure(@NotNull Call call, @NotNull IOException e) {
//									imageView.post(() -> Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show());
//								}
//
//								@Override
//								public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//									if (response.body() != null) {
//
//										final Document document = Jsoup.parse(response.body().string()
//												, "UTF-8", new Parser(new XmlTreeBuilder()));
//
//										final Elements content = document.getElementsByAttribute("status");
//										final String status = content.select("lfm[status]").attr("status");
//
//										if (RESULT_OK.equals(status)) {
//											StringBuilder img = new StringBuilder(content.select("image[size=extralarge]").text());
//
//											if (img.toString().contains("http") && img.toString().contains("https")) {
//												Log.d(TAG, "onResponse: ok, now downloading...");
//
//												DownloadUtil.get().download(
//														img.toString(),
//														Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separatorChar + "AlbumCovers",
//														albumId + "." + img.substring(img.lastIndexOf(".") + 1), new DownloadUtil.OnDownloadListener() {
//															@Override
//															public void onDownloadSuccess(File file) {
//																Log.d(TAG, "onDownloadSuccess: " + file.getAbsolutePath());
//																content.clear();
//
//																final String newPath = file.getAbsolutePath();
//																final CustomAlbumPath c = customs.get(0);
//																c.setAlbumArt(newPath);
//																c.save();
//
//																try {
//																	if (Data.sMusicBinder.getCurrentItem().getAlbumId() == albumId && Data.getCurrentCover() == null) {
//																		Data.setCurrentCover(BitmapFactory.decodeFile(newPath));
//																	}
//																} catch (RemoteException e) {
//																	e.printStackTrace();
//																}
//
//																loadPath2ImageView(file.getAbsolutePath(), holder);
//															}
//
//															@Override
//															public void onDownloading(int progress) {
//
//															}
//
//															@Override
//															public void onDownloadFailed(Exception e) {
//																Log.d(TAG, "onDownloadFailed: " + img.toString() + " " + e.getMessage());
//															}
//														});
//											} else {
//												Log.d(TAG, "onResponse: img url error" + img);
//												loadDefaultArt(holder);
//											}
//										} else {
//											Log.d(TAG, "onResponse: result not ok");
//											loadDefaultArt(holder);
//										}
//									} else {
//										Log.d(TAG, "onResponse: response is NUll!");
//										imageView.post(() -> Toast.makeText(activity, "response is NUll!", Toast.LENGTH_SHORT).show());
//										loadDefaultArt(holder);
//									}
//								}
//							});
//						}
//					} else {
//						Log.d(TAG, "albumLoader: has data in DB, loading...");
//						loadPath2ImageView(file.getAbsolutePath(), holder);
//					}
//				} catch (Exception e) {
//					Log.d(TAG, "albumLoader: load customAlbum Error, loading default..., msg: " + e.getMessage());
//					loadDefaultArt(holder);
//				}
//			} else {
//				Log.d(TAG, "customDB size is 0");
//				loadDefaultArt(holder);
//			}
//		} else {
//			loadDefaultArt(holder);
////				Log.d(TAG, "albumLoader: load from Cache..., msg: FROM NET switch not checked");
////				if (!TextUtils.isEmpty(baseCoverPath) && !"null".equals(baseCoverPath)) {
////					assert baseCoverPath != null;
////					File file = new File(baseCoverPath);
////					if (file.exists()) {
////						Log.d(TAG, "albumLoader: exists...");
////						loadPath2ImageView(baseCoverPath, holder);
////					} else {
////						loadDefaultArt(holder);
////					}
////				} else {
////					Log.d(TAG, "albumLoader: not exists");
////					loadDefaultArt(holder);
////				}
//		}
//	}
//
//	/**
//	 * load from defaultDB {@link MediaStore.Audio.Albums}
//	 */
//	private void loadPath2ImageView(@NonNull final String path, @NonNull final ViewHolder holder) {
//		if (verify(holder.mImageView)) {
//			holder.mImageView.post(() -> GlideApp.with(holder.mImageView)
//					.load(path)
////						.placeholder(R.drawable.default_album_art)
//					.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
//					.centerCrop()
//					.diskCacheStrategy(DiskCacheStrategy.NONE)
//					.override(250, 250)
//					.into(new CustomTarget<Drawable>() {
//						@Override
//						public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//							holder.mImageView.setImageDrawable(resource);
//							holder.invalidate();
//							holder.setBluredView(holder.mImageView);
//						}
//
//						@Override
//						public void onLoadCleared(@Nullable Drawable placeholder) {
//
//						}
//					}));
//		}
//	}
//
//	/**
//	 * @param holder view holder
//	 */
//	private void loadDefaultArt(@NonNull final ViewHolder holder) {
//		if (verify(holder.mImageView)) {
//			holder.mImageView.post(() -> GlideApp.with(holder.mImageView)
//					.load(R.drawable.default_album_art)
//					.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
//					.centerCrop()
//					.diskCacheStrategy(DiskCacheStrategy.NONE)
//					.into(new CustomTarget<Drawable>() {
//						@Override
//						public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//							holder.mImageView.setImageDrawable(resource);
//							holder.invalidate();
//						}
//
//						@Override
//						public void onLoadCleared(@Nullable Drawable placeholder) {
//
//						}
//					}));
//		}
//	}
//
//	/**
//	 * verify if key, null
//	 *
//	 * @param imageView the imageView to verify
//	 */
//	private boolean verify(@NonNull final ImageView imageView) {
//		boolean flag = false;
//		if (imageView.getTag(R.string.key_id_3) == null) {
//			Log.e(TAG, "key null clear_image");
//			GlideApp.with(imageView).clear(imageView);
//		} else {
//			//根据position判断是否为复用ViewHolder
//			flag = true;
//		}
//		return flag;
//	}

	@Override
	public void onViewRecycled(@NonNull ViewHolder holder) {
		super.onViewRecycled(holder);
		holder.mImageView.setTag(R.string.key_id_3, null);
		holder.invalidate();
	}

	@Override
	public int getItemCount() {
		return mAlbumNameList.size();
	}

	public static final int TYPE_COMMON = 0;

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		View view;
		switch (mType) {
			case LINEAR_TYPE: {
				view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_music_album_list_item, viewGroup, false);
			}
			break;
			case GRID_TYPE: {
				view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item_album_grid, viewGroup, false);
			}
			break;
			default:
				view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item_album_grid, viewGroup, false);
		}

		final ViewHolder holder = new ViewHolder(view);

		holder.mUView.setOnClickListener(v -> {
			String keyWords = mAlbumNameList.get(holder.getAdapterPosition()).getAlbumName();

			final ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(mMainActivity
					, holder.mImageView, mMainActivity.getString(R.string.transition_album_art));
			Intent intent = new Intent(mMainActivity, AlbumDetailActivity.class);
			intent.putExtra(AlbumDetailActivity.IntentKey.ALBUM_NAME, keyWords);
			intent.putExtra(AlbumDetailActivity.IntentKey.ID, mAlbumNameList.get(holder.getAdapterPosition()).getAlbumId());
			mMainActivity.startActivity(intent, compat.toBundle());
		});

		if (i == TYPE_PADDING) {
			holder.itemView.setPadding(0, MainActivity.PADDING, 0, 0);
			Log.d(TAG, "onCreateViewHolder: " + MainActivity.PADDING);
		}

		return holder;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0 || position == 1) {
			return TYPE_PADDING;
		} else {
			return TYPE_COMMON;
		}
	}

	@NonNull
	@Override
	public String getSectionName(int position) {
		return String.valueOf(mAlbumNameList.get(position).getAlbumName().charAt(0));
	}

	class ViewHolder extends RecyclerView.ViewHolder {

		View mUView;

		TextView mAlbumText;

		ImageView mImageView;

		BlurringView blurringView;

		ViewHolder(@NonNull View itemView) {
			super(itemView);
			mAlbumText = itemView.findViewById(R.id.recycler_item_song_album_name);
			mImageView = itemView.findViewById(R.id.recycler_item_album_image);
			mUView = itemView.findViewById(R.id.u_view);
			itemView.setBackground(null);

			if (mType == GRID_TYPE) {
				blurringView = itemView.findViewById(R.id.blurring_view);
				blurringView.setBlurredView(mImageView);
			}

			mAlbumText.setTextColor(ContextCompat.getColor(mMainActivity, R.color.notVeryBlack));

		}

//		void setBluredView(View view) {
//			if (blurringView != null) blurringView.setBlurredView(view);
//		}

		void invalidate() {
			if (blurringView != null) blurringView.invalidate();
		}
	}
}
