package top.geek_studio.chenlongcould.musicplayer.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.internal.NavigationMenuPresenter;
import com.google.android.material.internal.NavigationMenuView;
import com.google.android.material.navigation.NavigationView;

import org.litepal.LitePal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import top.geek_studio.chenlongcould.geeklibrary.widget.GkToolbar;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.MusicService;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.database.CustomAlbumPath;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;

@SuppressWarnings("WeakerAccess")
public final class Utils {

	public static final String TAG = "Utils";

	private Utils() {
		throw new AssertionError("private!");
	}

	public static final class Audio {

		public static final String NONE = "NONE";

		@NonNull
		public static List<String> extractMetadata(@NonNull MusicItem item) {
			final List<String> temp = new ArrayList<>();

			final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
			retriever.setDataSource(item.getMusicPath());

			temp.add("Title: " + item.getMusicName());
			temp.add("Path: " + item.getMusicPath());
			temp.add("Media type: " + item.getMimeName());
			temp.add("Album: " + item.getMusicAlbum());
			temp.add("AlbumArtist: " + retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST));
			temp.add("Artist: " + retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
			temp.add("Year: " + retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR));
			temp.add("Author: " + retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR));
			temp.add("Bitrate: " + retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
			temp.add("CD track number: " + retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER));
			temp.add("COMPILATION: " + retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPILATION));
			temp.add("COMPOSER: " + retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER));
			temp.add("Date: " + retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE));
			temp.add("Disc number: " + retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER));
			temp.add("Duration: " + item.getDuration());
			temp.add("Genre: " + retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
			temp.add("Writer: " + retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_WRITER));
			temp.add("Size: " + item.getSize());

			final List<String> data = new ArrayList<>();

			for (int i = 0; i < temp.size() - 1; i++) {
				String s = temp.get(i);
				if (s != null) {
					data.add(s);
				} else {
					data.add("None");
				}
			}

			temp.clear();
			retriever.release();

			return data;
		}

		public static void openEqualizer(@NonNull final Activity activity, int id) {
			if (id == AudioEffect.ERROR_BAD_VALUE) {
				Toast.makeText(activity, activity.getResources().getString(R.string.no_audio_ID), Toast.LENGTH_LONG).show();
			} else {
				try {
					final Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
					effects.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, id);
					effects.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
					activity.startActivityForResult(effects, 0);
				} catch (@NonNull final ActivityNotFoundException notFound) {
					notFound.printStackTrace();
					AlertDialog.Builder builder = new AlertDialog.Builder(activity)
							.setTitle("No equalizer found")
							.setMessage("Sorry, I did not find an equalizer on your device. You can install it from your app store!")
							.setPositiveButton("Search in PlayStore", (dialog, which) -> {
								dialog.dismiss();
								Uri uri = Uri.parse("https://play.google.com/store/search?q=equalizer&c=apps");
								activity.startActivity(new Intent(Intent.ACTION_VIEW, uri));
							})
							.setNegativeButton("OK", (dialog, which) -> dialog.dismiss());
					builder.show();
				}
			}
		}

		/**
		 * 获取封面
		 *
		 * @param mediaUri mp3 path
		 */
		@Deprecated
		@NonNull
		private static Bitmap getMp3CoverByMeta(@NonNull Context context, final String mediaUri) {

			final MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

			if (mediaUri == null) {
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_audiotrack_24px);
			}

			final File file = new File(mediaUri);
			if (file.isDirectory() || !file.exists()) {
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_audiotrack_24px);
			}

			mediaMetadataRetriever.setDataSource(mediaUri);
			byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();
			mediaMetadataRetriever.release();

			if (picture != null) {
				return BitmapFactory.decodeByteArray(picture, 0, picture.length);
			} else {
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_audiotrack_24px);
			}
		}

		/**
		 * this
		 * full
		 */
		public static Bitmap getCoverBitmapFull(Context context, int albumId) {

			final String path = getCoverPath(context, albumId);

			if (TextUtils.isEmpty(path) || path.equals("null") || path.equals(NONE)) {
				return getDrawableBitmap(context, R.drawable.default_album_art, true);
			}

			final File file = new File(path);
			if (!file.exists()) {
				return getDrawableBitmap(context, R.drawable.default_album_art, true);
			} else {
				if (file.isDirectory()) {
					return getDrawableBitmap(context, R.drawable.default_album_art, true);
				}
			}

			return BitmapFactory.decodeFile(path);
		}

		@Nullable
		private static String getCoverPathByDefDB(final Context context, final int albumId) {
			String img;
			final Cursor cursor = context.getContentResolver().query(
					Uri.parse(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI + String.valueOf(File.separatorChar) + albumId)
					, new String[]{MediaStore.Audio.Albums.ALBUM_ART}, null, null, null);

			if (cursor != null && cursor.getCount() != 0) {
				cursor.moveToFirst();
				img = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
				cursor.close();
			} else {
				return null;
			}
			return img;
		}

		public static String getCoverPath(final Context context, final int albumId) {
			final String[] albumPath = {null};

			final Cursor cursor = context.getContentResolver().query(
					Uri.parse(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI + String.valueOf(File.separatorChar) + albumId)
					, new String[]{MediaStore.Audio.Albums.ALBUM_ART}, null, null, null);
			if (cursor != null && cursor.getCount() != 0) {
				cursor.moveToFirst();
				albumPath[0] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
				cursor.close();
			}
			if (PreferenceUtil.getDefault(context).getBoolean(Values.SharedPrefsTag.USE_NET_WORK_ALBUM, false)) {
				List<CustomAlbumPath> customs = LitePal.findAll(CustomAlbumPath.class);
				if (customs.size() != 0) {
					final CustomAlbumPath custom = customs.get(0);
					if (TextUtils.isEmpty(albumPath[0]) || custom.isForceUse()) {
						File file = new File(custom.getAlbumArt());
						if (custom.getAlbumArt().equals("null") && !file.exists()) {
							return null;
						} else {
							albumPath[0] = custom.getAlbumArt();
						}
					} else {
						albumPath[0] = getCoverPathByDefDB(context, albumId);
					}
				} else {
					albumPath[0] = getCoverPathByDefDB(context, albumId);
				}
			} else {
				albumPath[0] = getCoverPathByDefDB(context, albumId);
			}
			return albumPath[0];
		}

		/**
		 * private
		 */
		private static Bitmap path2CoverByDB(Context context, String path) {
			if (TextUtils.isEmpty(path)) {
				return getDrawableBitmap(context, R.drawable.default_album_art);
			}

			if (path.equals("null")) {
				return getDrawableBitmap(context, R.drawable.default_album_art);
			}

			if (path.equals(NONE)) {
				return getDrawableBitmap(context, R.drawable.default_album_art);
			}

			File file = new File(path);
			if (!file.exists()) {
				return getDrawableBitmap(context, R.drawable.default_album_art);
			} else {
				if (file.isDirectory()) {
					return getDrawableBitmap(context, R.drawable.default_album_art);
				}
			}

			return BitmapFactory.decodeFile(path);
		}

		/**
		 * 获取封面
		 *
		 * @param mediaUri mp3 path
		 */
		@Deprecated
		private static Bitmap path2CoverByMeta(final String mediaUri, Context context) {

			MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

			if (mediaUri == null)
				return getDrawableBitmap(context, R.drawable.default_album_art);

			if (mediaUri.equals(NONE))
				return getDrawableBitmap(context, R.drawable.default_album_art);

			final File file = new File(mediaUri);
			if (file.isDirectory() || !file.exists())
				return getDrawableBitmap(context, R.drawable.default_album_art);

			mediaMetadataRetriever.setDataSource(mediaUri);
			byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();

			if (picture != null)
				return BitmapFactory.decodeByteArray(picture, 0, picture.length);
			else
				return getDrawableBitmap(context, R.drawable.default_album_art);
		}

		/**
		 * may call from {@link MusicService}
		 */
		@Nullable
		@Deprecated
		private static byte[] path2CoverByteByMeta(final String path, Context context) {
			MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
			final Bitmap bitmap = getDrawableBitmap(context, R.drawable.default_album_art);
			final File file = new File(path);

			if (path == null || file.isDirectory() || !file.exists() || file.isDirectory()) {
				if (bitmap != null) {
					final ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
					return buffer.array();
				} else {
					return null;
				}
			}

			mediaMetadataRetriever.setDataSource(path);
			byte[] cover = mediaMetadataRetriever.getEmbeddedPicture();

			if (cover == null) {
				if (bitmap != null)
					return ByteBuffer.allocate(bitmap.getByteCount()).array();
				else
					return null;
			} else {
				if (bitmap != null) bitmap.recycle();
				return cover;
			}

		}

		@SuppressWarnings("SameParameterValue")
		private static Bitmap getDrawableBitmap(@NonNull Context context, @DrawableRes int vectorDrawableId, boolean... full) {

			boolean b = full.length > 0 && full[0];

			return b ? Ui.readBitmapFromRes(context, vectorDrawableId, 400, 400) : Ui.readBitmapFromRes(context, vectorDrawableId, 100, 100);
		}

		/**
		 * @author Karim Abou Zeid (kabouzeid)
		 */
		@NonNull
		public static Intent createShareSongFileIntent(@NonNull final MusicItem song, Context context) {
			try {
				return new Intent()
						.setAction(Intent.ACTION_SEND)
						.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, context.getPackageName(), new File(song.getMusicPath())))
						.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
						.setType("audio/*");
			} catch (IllegalArgumentException e) {
				// TODO the path is most likely not like /storage/emulated/0/... but something like /storage/28C7-75B0/...
				e.printStackTrace();
				Toast.makeText(context, "Could not share this file, I'm aware of the issue.", Toast.LENGTH_SHORT).show();
				return new Intent();
			}
		}

		/**
		 * @author Karim Abou Zeid (kabouzeid)
		 */
		public static Uri getSongFileUri(int songId) {
			return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId);
		}

//		/**
//		 * @author Karim Abou Zeid (kabouzeid)
//		 */
//		public static void setRingtone(@NonNull final Context context, final int id) {
//			final ContentResolver resolver = context.getContentResolver();
//			final Uri uri = getSongFileUri(id);
//			try {
//				final ContentValues values = new ContentValues(2);
//				values.put(MediaStore.Audio.AudioColumns.IS_RINGTONE, "1");
//				values.put(MediaStore.Audio.AudioColumns.IS_ALARM, "1");
//				resolver.update(uri, values, null, null);
//			} catch (@NonNull final UnsupportedOperationException ignored) {
//				return;
//			}
//
//			try {
//				Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//						new String[]{MediaStore.MediaColumns.TITLE},
//						BaseColumns._ID + "=?",
//						new String[]{String.valueOf(id)},
//						null);
//				try {
//					if (cursor != null && cursor.getCount() == 1) {
//						cursor.moveToFirst();
//						Settings.System.putString(resolver, Settings.System.RINGTONE, uri.toString());
//						final String message = context.getString(R.string.x_has_been_set_as_ringtone, cursor.getString(0));
//						Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
//					}
//				} finally {
//					if (cursor != null) {
//						cursor.close();
//					}
//				}
//			} catch (SecurityException ignored) {
//			}
//		}

		public static AlertDialog getMusicDetailDialog(@NonNull Context context, @Nullable MusicItem item) {
			if (item == null || item.getMusicID() == -1) return null;

			final List<String> data = Utils.Audio.extractMetadata(item);
			ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1
					, data);
			final AlertDialog.Builder builder = new AlertDialog.Builder(context)
					.setTitle(context.getString(R.string.detail))
					.setAdapter(arrayAdapter, (dialog, which) -> {
						final ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
						final ClipData clipData = new ClipData("Copied by song detail", new String[]{"text"}, new ClipData.Item(data.get(which).split(":")[1]));
						clipboardManager.setPrimaryClip(clipData);
						Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show();
					})
					.setCancelable(false)
					.setNegativeButton(context.getString(R.string.done), (dialog, which) -> dialog.dismiss());
			return builder.create();
		}
	}

	public static final class Ui {

		private static final String TAG = "Ui";

		private Bitmap view2Bitmap(View v) {
			int w = v.getWidth();
			int h = v.getHeight();
			final Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(bmp);
			//如果不设置颜色，默认是透明背景
			c.drawColor(Color.WHITE);
			v.layout(0, 0, w, h);
			v.draw(c);
			return bmp;
		}

		public static Point getScreenSize(@NonNull Context c) {
			Display display = ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			return size;
		}

		// FIXME: 2019/5/24
		public static void releaseImageViewResource(@NonNull Context context, @NonNull ImageView imageView, @Nullable Bitmap replace) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof BitmapDrawable) {
				BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
				Bitmap bitmap = bitmapDrawable.getBitmap();
				if (bitmap != null && !bitmap.isRecycled()) {
					GlideApp.with(context).load(replace).into(imageView);
					bitmap.recycle();
					Log.d(TAG, "releaseImageViewResource: released!");
				} else {
					Log.d(TAG, "releaseImageViewResource: bitmap is null or bitmap isRecycled.");
				}
			}
		}

		/**
		 * getAccentColor from {@link SharedPreferences}, default: {@link top.geek_studio.chenlongcould.musicplayer.R.color#colorAccent}
		 *
		 * @return color (int)
		 */
		@ColorInt
		public static int getAccentColor(Context context) {
			return PreferenceUtil.getDefault(context).getInt(Values.SharedPrefsTag.ACCENT_COLOR, ContextCompat.getColor(context, R.color.colorAccent));
		}

		/**
		 * getPrimaryColor from {@link SharedPreferences}, default: {@link top.geek_studio.chenlongcould.musicplayer.R.color#colorPrimary}
		 *
		 * @return color (int)
		 */
		@ColorInt
		public static int getPrimaryColor(Context context) {
			return PreferenceUtil.getDefault(context).getInt(Values.SharedPrefsTag.PRIMARY_COLOR, ContextCompat.getColor(context, R.color.colorPrimary));
		}

		/**
		 * getPrimaryDarkColor from {@link SharedPreferences}, default: {@link top.geek_studio.chenlongcould.musicplayer.R.color#colorPrimaryDark}
		 *
		 * @return color (int)
		 */
		@ColorInt
		public static int getPrimaryDarkColor(Context context) {
			return PreferenceUtil.getDefault(context).getInt(Values.SharedPrefsTag.PRIMARY_DARK_COLOR, ContextCompat.getColor(context, R.color.colorPrimaryDark));
		}

		/**
		 * getTitleColor from {@link SharedPreferences}, default: {@link top.geek_studio.chenlongcould.musicplayer.R.color#def_over_title_color}
		 *
		 * @return color (int)
		 */
		@ColorInt
		public static int getTitleColor(Context context) {
			return PreferenceUtil.getDefault(context).getInt(Values.SharedPrefsTag.TITLE_COLOR, ContextCompat.getColor(context, R.color.def_over_title_color));
		}

		public static Bitmap readBitmapFromFile(String filePath, int width, int height) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(filePath, options);
			float srcWidth = options.outWidth;
			float srcHeight = options.outHeight;
			int inSampleSize = 1;

			if (srcWidth > height && srcWidth > width) {
				inSampleSize = (int) (srcWidth / width);
			} else if (srcWidth < height && srcHeight > height) {
				inSampleSize = (int) (srcHeight / height);
			}

			if (inSampleSize <= 0) {
				inSampleSize = 1;
			}
			options.inJustDecodeBounds = false;
			options.inSampleSize = inSampleSize;

			return BitmapFactory.decodeFile(filePath, options);
		}

		public static Bitmap readBitmapFromRes(Context context, @DrawableRes int filePath, int width, int height) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeResource(context.getResources(), filePath, options);
			float srcWidth = options.outWidth;
			float srcHeight = options.outHeight;
			int inSampleSize = 1;

			if (srcWidth > height && srcWidth > width) {
				inSampleSize = (int) (srcWidth / width);
			} else if (srcWidth < height && srcHeight > height) {
				inSampleSize = (int) (srcHeight / height);
			}

			if (inSampleSize <= 0) {
				inSampleSize = 1;
			}
			options.inJustDecodeBounds = false;
			options.inSampleSize = inSampleSize;

			return BitmapFactory.decodeResource(context.getResources(), filePath, options);
		}

		public static Bitmap readBitmapFromArray(byte[] data, int width, int height) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(data, 0, data.length, options);
			float srcWidth = options.outWidth;
			float srcHeight = options.outHeight;
			int inSampleSize = 1;

			if (srcWidth > height && srcWidth > width) {
				inSampleSize = (int) (srcWidth / width);
			} else if (srcWidth < height && srcHeight > height) {
				inSampleSize = (int) (srcHeight / height);
			}

			if (inSampleSize <= 0) {
				inSampleSize = 1;
			}
			options.inJustDecodeBounds = false;
			options.inSampleSize = inSampleSize;

			return BitmapFactory.decodeByteArray(data, 0, data.length, options);
		}

		/**
		 * @param context context
		 * @param aTitle  theTitle
		 * @return {@link AlertDialog.Builder}
		 * @deprecated use {@see top.geek_studio.chenlongcould.geeklibrary.DialogUtil#getLoadingDialog(Context, String...) }
		 */
		public static androidx.appcompat.app.AlertDialog getLoadingDialog(Context context, String... aTitle) {
			androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
			final View loadView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
			// TODO: 2019/1/7 custom Theme loading animation
			builder.setView(loadView);
			builder.setTitle(aTitle.length == 0 ? "Loading..." : aTitle[0]);
			builder.setCancelable(false);
			return builder.create();
		}

		public static void setNavigationMenuLineStyle(NavigationView navigationView, @ColorInt final int color, final int height) {
			try {
				Field fieldByPressenter = navigationView.getClass().getDeclaredField("presenter");
				fieldByPressenter.setAccessible(true);
				NavigationMenuPresenter menuPresenter = (NavigationMenuPresenter) fieldByPressenter.get(navigationView);
				Field fieldByMenuView = menuPresenter.getClass().getDeclaredField("menuView");
				fieldByMenuView.setAccessible(true);
				final NavigationMenuView mMenuView = (NavigationMenuView) fieldByMenuView.get(menuPresenter);

				mMenuView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
					@Override
					public void onChildViewAttachedToWindow(@NonNull View view) {

						RecyclerView.ViewHolder viewHolder = mMenuView.getChildViewHolder(view);
						if (viewHolder != null && "SeparatorViewHolder".equals(viewHolder.getClass().getSimpleName())) {
							if (viewHolder.itemView instanceof FrameLayout) {
								FrameLayout frameLayout = (FrameLayout) viewHolder.itemView;
								View line = frameLayout.getChildAt(0);
								line.setBackgroundColor(color);
								line.getLayoutParams().height = height;
								line.setLayoutParams(line.getLayoutParams());
							}
						}
					}

					@Override
					public void onChildViewDetachedFromWindow(@NonNull View view) {

					}
				});
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		/**
		 * 获取导航栏高度
		 *
		 * @param context context
		 * @return nav height
		 */
		public static int getNavheight(final Context context) {
			Resources resources = context.getResources();
			int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
			int height = resources.getDimensionPixelSize(resourceId);
			Log.v("dbw", "Navi height:" + height);
			return height;
		}

		public static void setStatusBarTextColor(final Activity activity, @ColorInt int color) {
			final View decor = activity.getWindow().getDecorView();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				double lum = ColorUtils.calculateLuminance(color);
				Log.d(TAG, "setStatusBarTextColor: calculateLuminance: " + lum);
				if (lum <= 0.5) {
					decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
				} else {
					decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
				}
			}
		}

		/**
		 * set color (style)
		 *
		 * @param activity context
		 * @param appBar   appBarLayout
		 * @param toolbar  toolbar
		 */
		public static void setTopBottomColor(@NonNull final Activity activity, @NonNull final AppBarLayout appBar, @NonNull final Toolbar toolbar) {
			setStatusBarTextColor(activity, getPrimaryColor(activity));
			appBar.setBackgroundColor(getPrimaryColor(activity));
			toolbar.setBackgroundColor(getPrimaryColor(activity));
			activity.getWindow().setNavigationBarColor(getPrimaryDarkColor(activity));
		}

		public static AlertDialog createMessageDialog(@NonNull final Activity context, @NonNull final String title, @NonNull final String message, boolean... touch2Dissmiss) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(title);
			builder.setMessage(message);
			builder.setCancelable(touch2Dissmiss.length <= 0 || touch2Dissmiss[0]);
			return builder.create();
		}

		public static void showErrorDialog(@NonNull final Activity context, @NonNull final String title, @NonNull final String message, @NonNull Runnable runnable, int delayTime) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(title);
			builder.setMessage(message);
			builder.setCancelable(true);
			builder.create().show();

			new Handler().postDelayed(runnable, delayTime);
		}

		public static void fastToast(@NonNull final Context context, @NonNull final String content) {
			Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
		}

		/**
		 * 获取图片亮度
		 *
		 * @param bm bitmap
		 */
		public static int getBright(@NonNull Bitmap bm) {
			if (bm.isRecycled()) return 0;

			int width = bm.getWidth() / 4;
			int height = bm.getHeight() / 4;
			int r, g, b;
			int count = 0;
			int bright = 0;
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					count++;
					int localTemp = bm.getPixel(i, j);
					r = (localTemp | 0xff00ffff) >> 16 & 0x00ff;
					g = (localTemp | 0xffff00ff) >> 8 & 0x0000ff;
					b = (localTemp | 0xffffff00) & 0x0000ff;
					bright = (int) (bright + 0.299 * r + 0.587 * g + 0.114 * b);
				}
			}
			return bright / count;
		}

		/**
		 * 判断颜色是不是亮色
		 *
		 * @param color
		 * @return bool
		 */
		public static boolean isColorLight(@ColorInt int color) {
			return ColorUtils.calculateLuminance(color) >= 0.5;
		}

		/**
		 * 设置toolbar上的文字、导航、菜单的图标颜色
		 *
		 * @param toolbar toolbar
		 * @param color   color -> {@code androidx.annotation.ColorInt}
		 * @deprecated use {@link GkToolbar#setOverlayColor(int)}
		 */
		public static void setOverToolbarColor(Toolbar toolbar, @ColorInt int color) {
			if (toolbar.getNavigationIcon() != null) {
				toolbar.getNavigationIcon().setTint(color);
			}
			toolbar.setTitleTextColor(color);

			if (toolbar.getSubtitle() != null) {
				toolbar.setSubtitleTextColor(color);
			}

			if (toolbar.getMenu().size() != 0) {
				if (toolbar.getOverflowIcon() != null) toolbar.getOverflowIcon().setTint(color);
				for (int i = 0; i < toolbar.getMenu().size(); i++) {
					if (toolbar.getMenu().getItem(i).getIcon() != null) {
						toolbar.getMenu().getItem(i).getIcon().clearColorFilter();
						toolbar.getMenu().getItem(i).getIcon().setTint(color);
					}
				}
			}
		}

		public static void setOverToolbarColor(@NonNull final Context context, Menu menu, int... titleColor) {
			int color = titleColor.length > 0 ? titleColor[0] : getTitleColor(context);
			for (int i = 0; i < menu.size(); i++) {
				if (menu.getItem(i).getIcon() != null) {
					menu.getItem(i).getIcon().clearColorFilter();
					menu.getItem(i).getIcon().setTint(color);
				}
			}
		}

		public static Bitmap blurBitmap(Bitmap bitmap, float r, Context context) {

			//Let's create an empty bitmap with the same size of the bitmap we want to blur
			Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

			//Instantiate a new Renderscript
			RenderScript rs = RenderScript.create(context);

			//Create an Intrinsic Blur Script using the Renderscript
			ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

			//Create the Allocations (in/out) with the Renderscript and the in/out bitmaps
			Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
			Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

			//Set the radius of the blur: 0 < radius <= 25
			blurScript.setRadius(25.0f);

			//Perform the Renderscript
			blurScript.setInput(allIn);
			blurScript.forEach(allOut);

			//Copy the final bitmap created by the out Allocation to the outBitmap
			allOut.copyTo(outBitmap);

			//recycle the original bitmap
			bitmap.recycle();

			//After finishing everything, we destroy the Renderscript.
			rs.destroy();

			return outBitmap;

		}

		/**
		 * by kabouzeid
		 */
		@ColorInt
		public static int withAlpha(@ColorInt int baseColor, @FloatRange(from = 0.0D, to = 1.0D) float alpha) {
			int a = Math.min(255, Math.max(0, (int) (alpha * 255.0F))) << 24;
			int rgb = 16777215 & baseColor;
			return a + rgb;
		}
	}

	public static final class M3Utils {
		// TODO: 2018/12/10 read the m3u file
//            new AsyncTask<Void, Void, Void>() {
//
//                @Override
//                protected Void doInBackground(Void... voids) {
//
////                    ArrayList<String> pathList = new ArrayList<>();
////                    ArrayList<String> nameList = new ArrayList<>();
////                    BufferedReader bufr = null;
////                    try {
////                        FileReader fr = new FileReader(file);
////                        bufr = new BufferedReader(fr);
////                        String line;
////
////                        try {
////                            while ((line = bufr.readLine()) != null) {
////                                if (line.contains("#EXTM3U")) continue;
////                                if (line.contains("#EXTINF")) {
////                                    String name = line.substring(line.indexOf(',') + 1);
////                                    nameList.add(name);
////                                    Log.d(TAG, "doInBackground: name: " + name);
////                                }
////                                if (line.contains("/storage/emulated")) {
////                                    pathList.add(line);
////                                    Log.d(TAG, "doInBackground: path: " + line);
////                                }
////                            }
////                        } catch (IOException e) {
////                            e.printStackTrace();
////                        }
////
////                    } catch (FileNotFoundException e) {
////                        e.printStackTrace();
////                    } finally {
////                        if (bufr != null) {
////                            try {
////                                bufr.close();
////                            } catch (IOException e) {
////                                e.printStackTrace();
////                            }
////                        }
////                    }
//                    return null;
//                }
//            }.execute();

	}

	public static final class Res {
		public static String getString(final Context context, @StringRes final int id) {
			return context.getResources().getString(id);
		}
	}

	public static final class IO {

		public static void Unzip(String zipFile, String targetDir) {
			int BUFFER = 4096;          //这里缓冲区我们使用4KB，
			String strEntry;            //保存每个zip的条目名称

			try {
				BufferedOutputStream dest;          //缓冲输出流
				FileInputStream fis = new FileInputStream(zipFile);
				ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
				ZipEntry entry;         //每个zip条目的实例

				while ((entry = zis.getNextEntry()) != null) {

					try {
						Log.i("unzip: ", "=" + entry);

						int count;
						byte[] data = new byte[BUFFER];
						strEntry = entry.getName();

						File entryFile = new File(targetDir + strEntry);
						File entryDir = new File(entryFile.getParent());

						if (!entryDir.exists()) {
							entryDir.mkdirs();
						}

						if (!entry.isDirectory()) {
							FileOutputStream fos = new FileOutputStream(entryFile);

							dest = new BufferedOutputStream(fos, BUFFER);
							while ((count = zis.read(data, 0, BUFFER)) != -1) {
								dest.write(data, 0, count);
							}
							dest.flush();
							dest.close();
						} else {
							entryFile.mkdir();
						}

					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				zis.close();
			} catch (Exception cwj) {
				cwj.printStackTrace();
			}
		}

		@SuppressWarnings("ResultOfMethodCallIgnored")
		public static void delFolder(String folderPath) {
			try {
				delAllFile(folderPath); //删除完里面所有内容
				java.io.File myFilePath = new java.io.File(folderPath);
				myFilePath.delete(); //删除空文件夹
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@SuppressWarnings({"UnusedReturnValue", "ConstantConditions", "ResultOfMethodCallIgnored"})
		private static boolean delAllFile(String path) {
			boolean flag = false;
			File file = new File(path);
			if (!file.exists()) {
				return flag;
			}
			if (!file.isDirectory()) {
				return flag;
			}
			String[] tempList = file.list();
			File temp = null;
			for (String aTempList : tempList) {
				if (path.endsWith(File.separator)) {
					temp = new File(path + aTempList);
				} else {
					temp = new File(path + File.separator + aTempList);
				}
				if (temp.isFile()) {
					temp.delete();
				}
				if (temp.isDirectory()) {
					delAllFile(path + "/" + aTempList);//先删除文件夹里面的文件
					delFolder(path + "/" + aTempList);//再删除空文件夹
					flag = true;
				}
			}
			return flag;
		}

		/**
		 * 压缩一个文件夹
		 */
		public static void zipDirectory(String path, String savePath) throws IOException {
			File file = new File(path);
			File zipFile = new File(savePath);
			Log.d(TAG, "zipDirectory: " + zipFile.getAbsolutePath());
			zipFile.createNewFile();
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
			zip(zos, file, file.getName());
			zos.flush();
			zos.close();
		}

		/**
		 * @param zos  压缩输出流
		 * @param file 当前需要压缩的文件
		 * @param path 当前文件相对于压缩文件夹的路径
		 */
		private static void zip(ZipOutputStream zos, File file, String path) throws IOException {
			// 首先判断是文件，还是文件夹，文件直接写入目录进入点，文件夹则遍历
			if (file.isDirectory()) {
				ZipEntry entry = new ZipEntry(path + File.separator);// 文件夹的目录进入点必须以名称分隔符结尾
				zos.putNextEntry(entry);
				File[] files = file.listFiles();
				for (File x : files) {
					zip(zos, x, path + File.separator + x.getName());
				}
			} else {
				FileInputStream fis = new FileInputStream(file);// 目录进入点的名字是文件在压缩文件中的路径
				ZipEntry entry = new ZipEntry(path);
				zos.putNextEntry(entry);// 建立一个目录进入点

				int len = 0;
				byte[] buf = new byte[1024];
				while ((len = fis.read(buf)) != -1) {
					zos.write(buf, 0, len);
				}
				zos.flush();
				fis.close();
				zos.closeEntry();// 关闭当前目录进入点，将输入流移动下一个目录进入点
			}
		}
	}
}
