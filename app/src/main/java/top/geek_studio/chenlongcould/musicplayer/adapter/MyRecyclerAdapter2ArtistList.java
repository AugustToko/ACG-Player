package top.geek_studio.chenlongcould.musicplayer.adapter;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.parser.XmlTreeBuilder;
import org.jsoup.select.Elements;
import org.litepal.LitePal;
import top.geek_studio.chenlongcould.geeklibrary.DownloadUtil;
import top.geek_studio.chenlongcould.geeklibrary.HttpUtil;
import top.geek_studio.chenlongcould.musicplayer.App;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.artistdetail.ArtistDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.main.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.database.ArtistArtPath;
import top.geek_studio.chenlongcould.musicplayer.misc.BlurringView;
import top.geek_studio.chenlongcould.musicplayer.model.ArtistItem;
import top.geek_studio.chenlongcould.musicplayer.threadPool.CustomThreadPool;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class MyRecyclerAdapter2ArtistList extends RecyclerView.Adapter<MyRecyclerAdapter2ArtistList.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

	public static final int LINEAR_TYPE = 0;
	public static final int GRID_TYPE = 1;
	private static final String TAG = "ArtistAdapter";
	private static int mType = LINEAR_TYPE;

	private List<ArtistItem> mArtistItems;

	private MainActivity mMainActivity;

	public MyRecyclerAdapter2ArtistList(MainActivity activity, List<ArtistItem> artistItems, int type) {
		this.mArtistItems = artistItems;
		mMainActivity = activity;
		mType = type;
	}

	@Override
	public void onViewRecycled(@NonNull ViewHolder holder) {
		super.onViewRecycled(holder);
		holder.itemView.setPadding(0, 0, 0, 0);
		holder.mArtistImage.setTag(R.string.key_id_2, -1);
		holder.invalidate();
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		View view;
		if (mType == LINEAR_TYPE) {
			view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_music_album_list_item, viewGroup, false);
		} else {
			view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item_album_grid, viewGroup, false);
		}
		final ViewHolder holder = new ViewHolder(view);

		if (i == MyRecyclerAdapter2AlbumList.TYPE_PADDING) {
			view.setPadding(0, MainActivity.PADDING, 0, 0);
		}

		holder.mUView.setOnClickListener(v -> {
			String keyWords = mArtistItems.get(holder.getAdapterPosition()).getArtistName();

			final ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(mMainActivity, holder.mArtistImage, mMainActivity.getString(R.string.transition_album_art));
			Intent intent = new Intent(mMainActivity, ArtistDetailActivity.class);
			intent.putExtra("key", keyWords);
			intent.putExtra("_id", mArtistItems.get(holder.getAdapterPosition()).getArtistId());
			mMainActivity.startActivity(intent, compat.toBundle());
		});

		return holder;
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
		viewHolder.mArtistText.setText(mArtistItems.get(i).getArtistName());
		viewHolder.mArtistImage.setTag(R.string.key_id_2, i);
		dataSet(i, viewHolder);
	}

	private void dataSet(final int index, final ViewHolder holder) {
		CustomThreadPool.post(() -> {

			ImageView imageView = holder.mArtistImage;

			final ArtistItem artistItem = mArtistItems.get(index);
			int artistId = artistItem.getArtistId();
			String artistName = artistItem.getArtistName();

			Log.d(TAG, "onBindViewHolder: id: " + artistId + " name: " + artistName);

			//if artistName is unknown unset data
			if ("<unknown>".equals(artistName)) {
				return;
			}

			LitePal.useDefault();
			List<ArtistArtPath> customs = LitePal.where("mArtistId = ?", String.valueOf(artistId))
					.find(ArtistArtPath.class);

			if (customs.size() != 0) {
				final ArtistArtPath custom = customs.get(0);

				final String path = custom.getArtistArt();

				if (TextUtils.isEmpty(path) || path.toLowerCase().equals("null") || path.toLowerCase().equals("none")) {

					final String mayPath = ifExists(artistId);

					if (mayPath != null) {
						Log.d(TAG, "onBindViewHolder: (in CUSTOM_DB) DB not ability, path is ability, save in db " +
								"and loading...");

						custom.setArtistArt(mayPath);
						custom.save();
						loadToImageView(holder, index, mayPath);
					} else {
						Log.d(TAG, "onBindViewHolder: (in CUSTOM_DB) DB not ability, path not ability, download...");
						//download
						String request = "http://ws.audioscrobbler.com/2.0/?method=artist.getinfo&artist="
								+ artistName
								+ "&api_key="
								+ App.LAST_FM_KEY;

						Log.v(TAG, "dataSet: request is: " + request);

						HttpUtil.sedOkHttpRequest(request, new Callback() {
							@Override
							public void onFailure(@NotNull Call call, @NotNull IOException e) {
								imageView.post(() -> Toast.makeText(mMainActivity, e.getMessage(), Toast.LENGTH_SHORT).show());
								loadDEF(holder, index);
							}

							@Override
							public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
								if (response.body() != null) {
									String body = response.body().string();
									final Document document = Jsoup.parse(body, "UTF-8", new Parser(new XmlTreeBuilder()));
									Elements content = document.getElementsByAttribute("status");
									String status = content.select("lfm[status]").attr("status");

									if (status.equals("ok")) {
										StringBuilder img = new StringBuilder(content.select("image[size=extralarge]").text());

										if (!checkStringEmpty(img.toString())) {
											Log.d(TAG, "check_name_and_imageUrl: imgUrl: " + img + " albumName is: " + artistName);
										}

										if (img.toString().contains("http") && img.toString().contains("https")) {

											Log.d(TAG, "onResponse: ok, now downloading..." + " albumName is: " + artistName);

											DownloadUtil.get().download(img.toString(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separatorChar + "ArtistCovers"
													, artistId + "." + img.substring(img.lastIndexOf(".") + 1), new DownloadUtil.OnDownloadListener() {
														@Override
														public void onDownloadSuccess(File file) {
															String newPath = file.getAbsolutePath();

															Log.d(TAG, "onDownloadSuccess: " + newPath + " albumName is: " + artistName);

															ArtistArtPath c = customs.get(0);
															c.setArtistArt(newPath);
															c.save();

															loadToImageView(holder, index, file.getPath());
														}

														@Override
														public void onDownloading(int progress) {
															Log.d(TAG, "onDownloading: " + artistName + " progress: " + progress);
														}

														@Override
														public void onDownloadFailed(Exception e) {
															Log.d(TAG, "onDownloadFailed: " + img.toString() + " " + e.getMessage());
															loadDEF(holder, index);
														}
													});
										} else {
											Log.d(TAG, "onResponse: img url error" + img.toString() + " albumName is: " + artistName);
											loadDEF(holder, index);
										}
									} else {
										Log.d(TAG, "onResponse: result not ok, load DEF_ALBUM");
										loadDEF(holder, index);
									}
								} else {
									mMainActivity.runOnUiThread(() -> Toast.makeText(mMainActivity, "response is NUll! " + " albumName is: " + artistName, Toast.LENGTH_SHORT).show());
									loadDEF(holder, index);
								}
							}
						});
					}
				} else {
					File file = new File(path);
					if (file.exists()) {
						Log.d(TAG, "already in customDB or not forceLoad, loading data from customDB " + " artistName is: " + artistName +
								"the path is: " + path);
						loadToImageView(holder, index, path);
					} else {
						Log.d(TAG, "onBindViewHolder: already in DB but not a file path");
						// 存在于数据库 但 路径失效了
						loadDEF(holder, index);
						custom.delete();
					}
				}
			} else {
				Log.d(TAG, "customDB size is 0, load DEF_ALBUM.png");
				loadDEF(holder, index);
			}
		});
	}

	private void loadToImageView(@NonNull final ViewHolder holder, int index, @NonNull final String path) {
		if (verify(holder.mArtistImage, index)) {
			holder.mArtistImage.post(() -> {
				if (mMainActivity.isDestroyed()) return;
				GlideApp.with(mMainActivity)
						.load(path)
						.placeholder(R.drawable.default_album_art)
						.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						.into(new CustomTarget<Drawable>() {
							@Override
							public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
								if (resource instanceof Animatable) {
									((Animatable) resource).start();
								}
								holder.mArtistImage.setImageDrawable(resource);
								holder.invalidate();
								holder.setBluredView(holder.mArtistImage);
							}

							@Override
							public void onLoadCleared(@Nullable Drawable placeholder) {

							}
						});
			});
		}
	}

	@Nullable
	private String ifExists(int artistId) {
		String mayPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
				+ File.separatorChar + "ArtistCovers"
				+ File.separatorChar + artistId + ".";

		if (new File(mayPath + "png").exists())
			return mayPath + "png";

		if (new File(mayPath + "jpg").exists())
			return mayPath + "jpg";

		if (new File(mayPath + "gif").exists())
			return mayPath + "gif";

		return null;
	}

	@SuppressWarnings("RedundantIfStatement")
	private boolean checkStringEmpty(final String data, boolean... isFile) {
		if (isFile.length > 0 && isFile[0] && new File(data).exists()) {
			return false;
		} else {
			if (TextUtils.isEmpty(data) || "null".equals(data.toLowerCase()) || "none".equals(data.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Load default image into imageView
	 *
	 * @param holder viewHolder
	 * @param index  tag
	 */
	private void loadDEF(ViewHolder holder, int index) {
		if (verify(holder.mArtistImage, index)) {
			if (mMainActivity.isDestroyed()) return;
//			Log.d(TAG, "key_test(loadDEF): tagId: " + imageView.getTag(R.string.key_id_2) + " pos: " + index);
			holder.mArtistImage.post(() -> GlideApp.with(mMainActivity)
					.load(R.drawable.default_album_art)
					.placeholder(R.drawable.default_album_art)
					.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.into(new CustomTarget<Drawable>() {
						@Override
						public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
							holder.mArtistImage.setImageDrawable(resource);
							holder.invalidate();
							holder.setBluredView(holder.mArtistImage);
						}

						@Override
						public void onLoadCleared(@Nullable Drawable placeholder) {

						}
					}));

		}
	}

	/**
	 * verify if key, null
	 */
	private boolean verify(@Nullable ImageView imageView, int index) {
		boolean flag = false;
		if (imageView == null) {
			Log.e(TAG, "imageView null clear_image");
		} else {
			final Object tag = imageView.getTag(R.string.key_id_2);
			if (tag == null || ((int) tag == -1)) {
				Log.e(TAG, "key null clear_image");
			} else {
				//根据position判断是否为复用ViewHolder
				if (((int) tag) != index) {
					Log.e(TAG, "key error clear_image");
				} else {
					flag = true;
				}
			}
		}
		return flag;
	}

	@Override
	public int getItemCount() {
		return mArtistItems.size();
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0 || position == 1) {
			return MyRecyclerAdapter2AlbumList.TYPE_PADDING;
		} else {
			return MyRecyclerAdapter2AlbumList.TYPE_COMMON;
		}
	}

	@NonNull
	@Override
	public String getSectionName(int position) {
		return String.valueOf(mArtistItems.get(position).getArtistName().charAt(0));
	}

	class ViewHolder extends RecyclerView.ViewHolder {

		View mUView;

		TextView mArtistText;

		ImageView mArtistImage;

//		View mView;

		TextView debugInfo;

		BlurringView blurringView;

		ViewHolder(@NonNull View itemView) {
			super(itemView);
			mArtistText = itemView.findViewById(R.id.recycler_item_song_album_name);
			mArtistImage = itemView.findViewById(R.id.recycler_item_album_image);
			mUView = itemView.findViewById(R.id.u_view);
			debugInfo = itemView.findViewById(R.id.debug_text);
			itemView.setBackground(null);//新增代码

			switch (mType) {
				case GRID_TYPE: {
					blurringView = itemView.findViewById(R.id.blurring_view);
					blurringView.setBlurredView(mArtistImage);
				}
				break;
			}
		}

		public void setBluredView(View view) {
			if (blurringView != null) blurringView.setBlurredView(view);
		}

		public void invalidate() {
			if (blurringView != null) blurringView.invalidate();
		}
	}
}
