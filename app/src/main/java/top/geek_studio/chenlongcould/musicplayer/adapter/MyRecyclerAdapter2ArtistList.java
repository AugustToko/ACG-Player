package top.geek_studio.chenlongcould.musicplayer.adapter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
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
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.artistdetail.ArtistDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.database.ArtistArtPath;
import top.geek_studio.chenlongcould.musicplayer.model.ArtistItem;
import top.geek_studio.chenlongcould.musicplayer.threadPool.AlbumThreadPool;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

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
		holder.mArtistImage.setTag(R.string.key_id_2, -1);
		GlideApp.with(mMainActivity).clear(holder.mArtistImage);
		GlideApp.get(mMainActivity).clearMemory();
		if (mType == GRID_TYPE) {
			holder.mView.setBackgroundColor(ContextCompat.getColor(mMainActivity, R.color.notVeryBlack));
		}
		holder.mArtistText.setTextColor(ContextCompat.getColor(mMainActivity, R.color.notVeryWhite));
//		holder.debugInfo.setText("");
	}

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
			default:
				view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item_album_grid, viewGroup, false);
		}
		final ViewHolder holder = new ViewHolder(view);

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
		dataSet(i, viewHolder.mArtistImage, viewHolder.mArtistText, viewHolder.mView, viewHolder.debugInfo);
	}

	private void dataSet(final int index, final ImageView imageView, final TextView textView, final View view, final TextView debugText) {
		AlbumThreadPool.post(() -> {

			final ArtistItem artistItem = mArtistItems.get(index);
			int artistId = artistItem.getArtistId();
			String artistName = artistItem.getArtistName();

			Log.d(TAG, "onBindViewHolder: id: " + artistId + " name: " + artistName);

			//if artistName is unknown unset data
			if ("<unknown>".equals(artistName)) {
				return;
			}

			List<ArtistArtPath> customs = LitePal.where("mArtistId = ?", String.valueOf(artistId)).find(ArtistArtPath.class);

			if (customs.size() != 0) {
				final ArtistArtPath custom = customs.get(0);

				String path = custom.getArtistArt();

				if (TextUtils.isEmpty(path) || path.toLowerCase().equals("null") || path.toLowerCase().equals("none")) {

					final String mayPath = ifExists(artistId);

					if (mayPath != null) {
						Log.d(TAG, "onBindViewHolder: (in CUSTOM_DB) DB not ability, path is ability, save in db and loading...");

						custom.setArtistArt(mayPath);
						custom.save();

						if (verify(imageView, index)) {
							imageView.post(() -> GlideApp.with(mMainActivity)
									.load(mayPath)
									.placeholder(R.drawable.default_album_art)
									.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
									.diskCacheStrategy(DiskCacheStrategy.NONE)
									.into(imageView));
							setUpTagColor(mayPath, textView, view);

							// TODO: 2019/5/24 debug
//							debugText.setText(mayPath);
						}

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
								loadDEF(imageView, view, textView, index);
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

															if (verify(imageView, index)) {
																Log.d(TAG, "onDownloadSuccess: loading,,," + " albumName is: " + artistName);
																imageView.post(() -> GlideApp.with(mMainActivity)
																		.load(file)
																		.placeholder(R.drawable.default_album_art)
																		.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
																		.diskCacheStrategy(DiskCacheStrategy.NONE)
																		.into(imageView));
																setUpTagColor(newPath, textView, view);
															}
														}

														@Override
														public void onDownloading(int progress) {
															Log.d(TAG, "onDownloading: " + artistName + " progress: " + progress);
														}

														@Override
														public void onDownloadFailed(Exception e) {
															Log.d(TAG, "onDownloadFailed: " + img.toString() + " " + e.getMessage());
															loadDEF(imageView, view, textView, index);
														}
													});
										} else {
											Log.d(TAG, "onResponse: img url error" + img.toString() + " albumName is: " + artistName);
											loadDEF(imageView, view, textView, index);
										}
									} else {
										Log.d(TAG, "onResponse: result not ok, load DEF_ALBUM");
										loadDEF(imageView, view, textView, index);
									}
								} else {
									mMainActivity.runOnUiThread(() -> Toast.makeText(mMainActivity, "response is NUll! " + " albumName is: " + artistName, Toast.LENGTH_SHORT).show());
									loadDEF(imageView, view, textView, index);
								}
							}
						});
					}
				} else {
					File file = new File(path);
					if (file.exists()) {
						Log.d(TAG, "already in customDB or not forceLoad, loading data from customDB " + " artistName is: " + artistName +
								"the path is: " + path);
						if (verify(imageView, index)) {

							setUpTagColor(path, textView, view);
							imageView.post(() -> GlideApp.with(mMainActivity)
									.load(path)
									.placeholder(R.drawable.default_album_art)
									.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
									.diskCacheStrategy(DiskCacheStrategy.NONE)
									.into(imageView));
						}

					} else {
						Log.d(TAG, "onBindViewHolder: already in DB but not a file path");
						// 存在于数据库 但 路径失效了
						loadDEF(imageView, view, textView, index);
					}
				}
			} else {
				Log.d(TAG, "customDB size is 0, load DEF_ALBUM.png");
				loadDEF(imageView, view, textView, index);
			}
		});
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
	 * @param imageView imageView
	 * @param view      title background
	 * @param textView  textView, show artist name
	 * @param index     tag
	 */
	private void loadDEF(ImageView imageView, View view, TextView textView, int index) {
		if (verify(imageView, index)) {
			Log.d(TAG, "key_test(loadDEF): tagId: " + imageView.getTag(R.string.key_id_2) + " pos: " + index);
			imageView.post(() -> {
				GlideApp.with(mMainActivity)
						.load(R.drawable.default_album_art)
						.placeholder(R.drawable.default_album_art)
						.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						.into(imageView);
				if (view != null)
					view.setBackgroundColor(ContextCompat.getColor(mMainActivity, R.color.notVeryBlack));
				if (textView != null)
					textView.setTextColor(ContextCompat.getColor(mMainActivity, R.color.notVeryWhite));
			});

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

	private void setUpTagColor(String artistPath, TextView textView, View bgView) {
		//...mode set
		switch (mType) {
			case GRID_TYPE: {

				final Bitmap bitmap = Utils.Ui.readBitmapFromFile(artistPath, 50, 50);
				if (bitmap != null) {
					//color set (album tag)
					Palette.from(bitmap).generate(p -> {
						if (p != null) {
							@ColorInt int color = p.getVibrantColor(ContextCompat.getColor(mMainActivity, R.color.notVeryBlack));
							if (Utils.Ui.isColorLight(color)) {
								textView.setTextColor(ContextCompat.getColor(mMainActivity, R.color.notVeryBlack));
							} else {
								textView.setTextColor(ContextCompat.getColor(mMainActivity, R.color.notVeryWhite));
							}
							bgView.setBackgroundColor(color);

							bitmap.recycle();
						} else {
							bgView.setBackgroundColor(ContextCompat.getColor(mMainActivity, R.color.colorPrimary));
						}
					});
				}

			}
			break;
			default:
		}
	}

	@Override
	public int getItemCount() {
		return mArtistItems.size();
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

		View mView;

		TextView debugInfo;

		ViewHolder(@NonNull View itemView) {
			super(itemView);
			mArtistText = itemView.findViewById(R.id.recycler_item_song_album_name);
			mArtistImage = itemView.findViewById(R.id.recycler_item_album_image);
			mUView = itemView.findViewById(R.id.u_view);
			debugInfo = itemView.findViewById(R.id.debug_text);
			itemView.setBackground(null);//新增代码

			switch (mType) {
				case GRID_TYPE: {
					mView = itemView.findViewById(R.id.mask);
				}
				break;
			}
		}
	}
}
