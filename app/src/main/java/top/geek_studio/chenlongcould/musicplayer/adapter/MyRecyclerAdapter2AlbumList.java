/*
 * ************************************************************
 * 文件：MyRecyclerAdapter2AlbumList.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月27日 13:11:38
 * 上次修改时间：2019年01月27日 13:08:44
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.adapter;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.parser.XmlTreeBuilder;
import org.jsoup.select.Elements;
import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import top.geek_studio.chenlongcould.geeklibrary.DownloadUtil;
import top.geek_studio.chenlongcould.geeklibrary.HttpUtil;
import top.geek_studio.chenlongcould.musicplayer.App;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.AlbumDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.database.CustomAlbumPath;
import top.geek_studio.chenlongcould.musicplayer.model.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.thread_pool.AlbumThreadPool;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

public final class MyRecyclerAdapter2AlbumList extends RecyclerView.Adapter<MyRecyclerAdapter2AlbumList.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

	public static final int LINEAR_TYPE = 0;
	public static final int GRID_TYPE = 1;
	private static final String TAG = "AlbumAdapter";
	private static int mType = LINEAR_TYPE;

	private List<AlbumItem> mAlbumNameList;

	private MainActivity mMainActivity;

//    private ViewHolder mCurrentBind;

	public MyRecyclerAdapter2AlbumList(MainActivity activity, List<AlbumItem> albumNameList, int type) {
		this.mAlbumNameList = albumNameList;
		mMainActivity = activity;
		mType = type;
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
			break;
			default:
				view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item_album_grid, viewGroup, false);
		}
		final ViewHolder holder = new ViewHolder(view);

		holder.mUView.setOnClickListener(v -> {
			String keyWords = mAlbumNameList.get(holder.getAdapterPosition()).getAlbumName();

			final ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(mMainActivity, holder.mImageViewReference.get(), mMainActivity.getString(R.string.transition_album_art));
			Intent intent = new Intent(mMainActivity, AlbumDetailActivity.class);
			intent.putExtra("key", keyWords);
			intent.putExtra("_id", mAlbumNameList.get(holder.getAdapterPosition()).getAlbumId());
			mMainActivity.startActivity(intent, compat.toBundle());
		});

		return holder;
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
		viewHolder.mAlbumText.setText(mAlbumNameList.get(viewHolder.getAdapterPosition()).getAlbumName());
		viewHolder.mImageViewReference.get().setTag(R.string.key_id_3, viewHolder.getAdapterPosition());
		imageLoader(viewHolder.mImageViewReference.get(), viewHolder.mAlbumText, viewHolder.mView, viewHolder.getAdapterPosition());
	}

	private void imageLoader(ImageView imageView, TextView textView, View bgView, int i) {
		AlbumThreadPool.post(() -> {
			final String[] albumPath = {null};

			final AlbumItem albumItem = mAlbumNameList.get(i);
			int albumId = albumItem.getAlbumId();
			final String albumName = albumItem.getAlbumName();
			final String artist = albumItem.getArtist();

			final Cursor cursor = mMainActivity.getContentResolver().query(
					Uri.parse(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI + String.valueOf(File.separatorChar) + albumId)
					, new String[]{MediaStore.Audio.Albums.ALBUM_ART}, null, null, null);
			if (cursor != null && cursor.getCount() != 0) {
				cursor.moveToFirst();

				//set DefaultVal by MediaStore
				albumPath[0] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
				cursor.close();
			}

			final String coverPath = albumPath[0];

			if (coverPath != null && !TextUtils.isEmpty(coverPath)) {
				final File file = new File(coverPath);
				if (file.exists()) {
					Log.d(TAG, "albumLoader: the album id DEFAULT_DB is ability, loading def");
					loadPath2ImageView(coverPath, imageView);
				} else {
					//load default res
					loadDefaultArt(imageView);
				}
			} else {
				if (PreferenceManager.getDefaultSharedPreferences(mMainActivity).getBoolean(Values.SharedPrefsTag.USE_NET_WORK_ALBUM, false)) {
					List<CustomAlbumPath> customs = LitePal.where("mAlbumId = ?", String.valueOf(albumId)).find(CustomAlbumPath.class);
					if (customs.size() != 0) {

						final CustomAlbumPath custom = customs.get(0);
						String path = custom.getAlbumArt();

						if (TextUtils.isEmpty(path)) {
							Log.d(TAG, "onBindViewHolder: (in CUSTOM_DB) has val but the val is not ability, will remove it..., download...");

							String mayPath = ifExists(albumId);
							if (mayPath != null) {

								Log.d(TAG, "onBindViewHolder: (in CUSTOM_DB) DB not ability, path is ability, save in db and loading...");

								custom.setAlbumArt(mayPath);
								custom.save();

								loadPath2ImageView(mayPath, imageView);
							} else {
								//download
								HttpUtil httpUtil = new HttpUtil();
								String request = "http://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=" +
										App.LAST_FM_KEY +
										"&artist=" +
										artist +
										"&album=" +
										albumName;

								httpUtil.sedOkHttpRequest(request, new Callback() {
									@Override
									public void onFailure(@NotNull Call call, @NotNull IOException e) {
										imageView.post(() -> Toast.makeText(mMainActivity, e.getMessage(), Toast.LENGTH_SHORT).show());
										loadDefaultArt(imageView);
									}

									@Override
									public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
										if (response.body() != null) {
											String body = response.body().string();
											final Document document = Jsoup.parse(body, "UTF-8", new Parser(new XmlTreeBuilder()));
											Elements content = document.getElementsByAttribute("status");
											String status = content.select("lfm[status]").attr("status");

											if ("ok".equals(status)) {
												StringBuilder img = new StringBuilder(content.select("image[size=extralarge]").text());
												Log.d(TAG, "onResponse: imgUrl: " + img + " albumName is: " + albumName);
												if (img.toString().contains("http") && img.toString().contains("https")) {

													Log.d(TAG, "onResponse: ok, now downloading..." + " albumName is: " + albumName);

													DownloadUtil.get().download(img.toString(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + "AlbumCovers"
															, albumId + "." + img.substring(img.lastIndexOf(".") + 1), new DownloadUtil.OnDownloadListener() {
																@Override
																public void onDownloadSuccess(File file) {
																	String newPath = file.getAbsolutePath();
																	Log.d(TAG, "onDownloadSuccess: " + newPath + " albumName is: " + albumName);


																	CustomAlbumPath c = customs.get(0);
																	c.setAlbumArt(newPath);
																	c.save();

																	loadPath2ImageView(file.getAbsolutePath(), imageView);
																}

																@Override
																public void onDownloading(int progress) {
																	Log.d(TAG, "onDownloading: " + albumName + " progress: " + progress);
																}

																@Override
																public void onDownloadFailed(Exception e) {
																	Log.d(TAG, "onDownloadFailed: " + img.toString() + " " + e.getMessage());
																	loadDefaultArt(imageView);
																}
															});
												} else {
													Log.d(TAG, "onResponse: img url error" + img.toString() + " albumName is: " + albumName);
													loadDefaultArt(imageView);
												}
											} else {
												Log.d(TAG, "onResponse: result not ok, load DEF_ALBUM");
												loadDefaultArt(imageView);
											}
										} else {
											mMainActivity.runOnUiThread(() -> Toast.makeText(mMainActivity, "response is NUll! " + " albumName is: " + albumName, Toast.LENGTH_SHORT).show());
											loadDefaultArt(imageView);
										}
									}
								});
							}
						} else {
							Log.d(TAG, "already in customDB or not forceLoad, loading data from customDB " + " albumName is: " + albumName +
									"the path is: " + path);
							loadPath2ImageView(path, imageView);
						}
					} else {
						Log.d(TAG, "customDB size is 0, load DEF_ALBUM.png");
						loadDefaultArt(imageView);
					}
				} else {
					Log.d(TAG, "onBindViewHolder: switch is not open, load DEF_ALBUM.png");
					Log.d(TAG, "onBindViewHolder: album in DEFAULT_DB has value, load...");
					loadDefaultArt(imageView);
				}
			}

			//...mode set
			switch (mType) {
				case GRID_TYPE: {

					final Bitmap bitmap = Utils.Ui.readBitmapFromFile(coverPath, 100, 100);
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
		});
	}

	/**
	 * load from defaultDB {@link MediaStore.Audio.Albums}
	 */
	private void loadPath2ImageView(@NonNull final String path, @NonNull final ImageView imageView) {
		if (verify(imageView)) {
			imageView.post(() -> GlideApp.with(imageView)
					.load(path)
					.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
					.centerCrop()
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.into(imageView));
		}
	}

	@Override
	public void onViewRecycled(@NonNull ViewHolder holder) {
		super.onViewRecycled(holder);
		holder.mImageViewReference.get().setTag(R.string.key_id_3, null);
		GlideApp.with(mMainActivity).clear(holder.mImageViewReference.get());
		holder.mView.setBackgroundColor(ContextCompat.getColor(mMainActivity, R.color.notVeryBlack));
	}

	private String ifExists(int id) {
		String mayPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
				+ File.separatorChar + "ArtistCovers"
				+ File.separatorChar + id + ".";

		if (new File(mayPath + "png").exists()) {
			return mayPath + "png";
		}

		if (new File(mayPath + "jpg").exists()) {
			return mayPath + "jpg";
		}

		if (new File(mayPath + "gif").exists()) {
			return mayPath + "gif";
		}

		return null;
	}

	/**
	 * @param imageView ImageView
	 *
	 * @see MyRecyclerAdapter#loadDefaultArt(ImageView)
	 */
	private void loadDefaultArt(@NonNull final ImageView imageView) {
		if (verify(imageView)) {
			imageView.post(() -> GlideApp.with(imageView)
					.load(R.drawable.default_album_art)
					.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
					.centerCrop()
					.override(100, 100)
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.into(imageView));
		}
	}

	/**
	 * verify if key, null
	 *
	 * @param imageView the imageView to verify
	 */
	private boolean verify(@NonNull final ImageView imageView) {
		boolean flag = false;
		if (imageView.getTag(R.string.key_id_3) == null) {
			Log.e(TAG, "key null clear_image");
			GlideApp.with(imageView).clear(imageView);
		} else {
			//根据position判断是否为复用ViewHolder
			flag = true;
		}
		return flag;
	}

	@Override
	public int getItemCount() {
		return mAlbumNameList.size();
	}

	@NonNull
	@Override
	public String getSectionName(int position) {
		return String.valueOf(mAlbumNameList.get(position).getAlbumName().charAt(0));
	}

	class ViewHolder extends RecyclerView.ViewHolder {

		View mUView;

		TextView mAlbumText;

		WeakReference<ImageView> mImageViewReference;

		View mView;

		ViewHolder(@NonNull View itemView) {
			super(itemView);
			mAlbumText = itemView.findViewById(R.id.recycler_item_song_album_name);
			mImageViewReference = new WeakReference<>(itemView.findViewById(R.id.recycler_item_album_image));
			mUView = itemView.findViewById(R.id.u_view);
			itemView.setBackground(null);//新增代码

			switch (mType) {
				case GRID_TYPE: {
					mView = itemView.findViewById(R.id.mask);
				}
				break;
				default:
			}
		}
	}
}
