package top.geek_studio.chenlongcould.musicplayer.activity;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.jrummyapps.android.colorpicker.ColorPickerDialog;
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener;
import org.litepal.LitePal;
import org.litepal.LitePalDB;
import top.geek_studio.chenlongcould.geeklibrary.DialogUtil;
import top.geek_studio.chenlongcould.musicplayer.*;
import top.geek_studio.chenlongcould.musicplayer.broadcast.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.database.MyBlackPath;
import top.geek_studio.chenlongcould.musicplayer.databinding.ActivitySettingsBinding;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static top.geek_studio.chenlongcould.musicplayer.Values.SharedPrefsTag.*;

/**
 * @author chenlongcould
 */
public final class SettingsActivity extends BaseCompatActivity {

	public static final String TAG = "SettingsActivity";

	public static final int PRIMARY = 0;

	public static final int PRIMARY_DARK = 1;

	public static final int ACCENT = 2;

	public static final int TITLE = 3;

	private ActivitySettingsBinding mSettingsBinding;

	private Switch mStyleSwitch;

	private ImageView mPrimaryImage;

	private ImageView mPrimaryDarkImage;

	private ImageView mAccentImage;

	private ImageView mTitleImage;

	private Toolbar mToolbar;

	private AppBarLayout mAppBarLayout;

	private ColorPickerDialogListener pickerDialogListener = new ColorPickerDialogListener() {
		@Override
		public void onColorSelected(int dialogId, @ColorInt int color) {
			SharedPreferences.Editor editor = preferences.edit();
			switch (dialogId) {

				/*
				 * Primary (toolbar tabLayout...)
				 * */
				case PRIMARY: {
					mPrimaryImage.clearAnimation();
					ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), preferences.getInt(Values.SharedPrefsTag.PRIMARY_COLOR, R.color.colorPrimary), color);
					animator.setDuration(300);
					animator.addUpdateListener(animation -> {
						mPrimaryImage.setBackgroundColor((Integer) animation.getAnimatedValue());
						mAppBarLayout.setBackgroundColor((Integer) animation.getAnimatedValue());
						mToolbar.setBackgroundColor((Integer) animation.getAnimatedValue());
					});
					animator.start();
					editor.putInt(Values.SharedPrefsTag.PRIMARY_COLOR, color);
					editor.apply();
					mPrimaryImage.clearAnimation();

					//set cardColor
					setUpTaskCardColor(color);
				}
				break;

				/*
				 * Dark Primary
				 * */
				case PRIMARY_DARK: {
					mPrimaryDarkImage.clearAnimation();
					ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), preferences.getInt(Values.SharedPrefsTag.PRIMARY_DARK_COLOR, R.color.colorPrimaryDark), color);
					animator.setDuration(300);
					animator.addUpdateListener(animation -> {
						mPrimaryDarkImage.setBackgroundColor((Integer) animation.getAnimatedValue());
						getWindow().setNavigationBarColor((Integer) animation.getAnimatedValue());
					});
					animator.start();
					editor.putInt(Values.SharedPrefsTag.PRIMARY_DARK_COLOR, color);
					editor.apply();
					mPrimaryDarkImage.clearAnimation();
				}
				break;

				/*
				 * Accent Color
				 * */
				case ACCENT: {
					mAccentImage.setBackgroundColor(color);
					editor.putInt(Values.SharedPrefsTag.ACCENT_COLOR, color);
					editor.apply();
				}
				break;

				case TITLE: {
					mTitleImage.clearAnimation();
					ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), preferences.getInt(Values.SharedPrefsTag.TITLE_COLOR, R.color.def_over_title_color), color);
					animator.setDuration(300);
					animator.addUpdateListener(animation -> {
						mTitleImage.setBackgroundColor((Integer) animation.getAnimatedValue());
						Utils.Ui.setOverToolbarColor(mToolbar, color);
					});
					animator.start();
					editor.putInt(Values.SharedPrefsTag.TITLE_COLOR, color);
					editor.apply();
					mPrimaryDarkImage.clearAnimation();
				}
				break;

				default:
			}
		}

		@Override
		public void onDialogDismissed(int dialogId) {

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mSettingsBinding = DataBindingUtil.setContentView(this, R.layout.activity_settings);

		mPrimaryImage = findViewById(R.id.activity_settings_preview_primary);
		mPrimaryDarkImage = findViewById(R.id.activity_settings_preview_primary_dark);
		mAccentImage = findViewById(R.id.activity_settings_preview_acc);
		mTitleImage = findViewById(R.id.activity_settings_preview_title);
		mToolbar = findViewById(R.id.activity_settings_toolbar);
		mAppBarLayout = findViewById(R.id.activity_settings_appbar);
		mStyleSwitch = findViewById(R.id.activity_settings_style_switch);

		super.initView(mToolbar, mAppBarLayout);
		super.onCreate(savedInstanceState);

		//find xx
		final ConstraintLayout primaryOpt = findViewById(R.id.primer_color_option);
		final ConstraintLayout primaryDarkOpt = findViewById(R.id.primer_color_dark_option);
		final ConstraintLayout accentOpt = findViewById(R.id.accent_color_option);
		final ConstraintLayout titleOpt = findViewById(R.id.title_color);
		final ConstraintLayout styleOpt = findViewById(R.id.detail_background_style);

		ConstraintLayout constraintLayout = findViewById(R.id.theme_settings);
		constraintLayout.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, ThemeActivity.class)));

		inflateCommonMenu();

		initPreView();

		mToolbar.setOnMenuItemClickListener(menuItem -> {
			switch (menuItem.getItemId()) {
				case R.id.menu_toolbar_settings_reset: {

					SharedPreferences.Editor editor = preferences.edit();
					editor.putInt(Values.SharedPrefsTag.ACCENT_COLOR, ContextCompat.getColor(SettingsActivity.this, R.color.colorAccent));
					editor.putInt(Values.SharedPrefsTag.PRIMARY_COLOR, ContextCompat.getColor(SettingsActivity.this, R.color.colorPrimary));
					editor.putInt(Values.SharedPrefsTag.PRIMARY_DARK_COLOR, ContextCompat.getColor(SettingsActivity.this, R.color.colorPrimaryDark));
					editor.apply();

					clearAnimation();
					ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), preferences.getInt(Values.SharedPrefsTag.PRIMARY_COLOR, ContextCompat.getColor(SettingsActivity.this, R.color.colorPrimary)), ContextCompat.getColor(SettingsActivity.this, R.color.colorPrimary));
					animator.setDuration(300);
					animator.addUpdateListener(animation -> {
						mPrimaryImage.setBackgroundColor((Integer) animation.getAnimatedValue());
						mAppBarLayout.setBackgroundColor((Integer) animation.getAnimatedValue());
						mToolbar.setBackgroundColor((Integer) animation.getAnimatedValue());
					});

					ValueAnimator animator2 = ValueAnimator.ofObject(new ArgbEvaluator(), preferences.getInt(Values.SharedPrefsTag.PRIMARY_DARK_COLOR, ContextCompat.getColor(SettingsActivity.this, R.color.colorPrimaryDark)), ContextCompat.getColor(SettingsActivity.this, R.color.colorPrimaryDark));
					animator2.setDuration(300);
					animator2.addUpdateListener(animation -> {
						mPrimaryDarkImage.setBackgroundColor((Integer) animator2.getAnimatedValue());
						getWindow().setNavigationBarColor((Integer) animator2.getAnimatedValue());
					});

					ValueAnimator animator3 = ValueAnimator.ofObject(new ArgbEvaluator(), preferences.getInt(Values.SharedPrefsTag.ACCENT_COLOR, ContextCompat.getColor(SettingsActivity.this, R.color.colorAccent)), ContextCompat.getColor(SettingsActivity.this, R.color.colorAccent));
					animator3.setDuration(300);
					animator3.addUpdateListener(animation -> mAccentImage.setBackgroundColor((Integer) animator3.getAnimatedValue()));

					animator.start();
					animator2.start();
					animator3.start();

					clearAnimation();
				}
				break;
				default:
			}
			return false;
		});

		mToolbar.setNavigationOnClickListener(v -> onBackPressed());

		primaryOpt.setOnClickListener(v -> {
			ColorPickerDialog colorPickerDialog = ColorPickerDialog.newBuilder().setColor(preferences.getInt(Values.SharedPrefsTag.PRIMARY_COLOR, Color.parseColor("#008577")))
					.setDialogTitle(R.string.color_picker)
					.setDialogType(ColorPickerDialog.TYPE_PRESETS)
					.setShowAlphaSlider(false)
					.setDialogId(PRIMARY)
					.setAllowPresets(false)
					.create();
			colorPickerDialog.setColorPickerDialogListener(pickerDialogListener);
			//noinspection deprecation
			colorPickerDialog.show(getFragmentManager(), "color-picker-dialog");
		});

		primaryDarkOpt.setOnClickListener(v -> {
			ColorPickerDialog colorPickerDialog = ColorPickerDialog.newBuilder().setColor(preferences.getInt(Values.SharedPrefsTag.PRIMARY_DARK_COLOR, Color.parseColor("#00574B")))
					.setDialogTitle(R.string.color_picker)
					.setDialogType(ColorPickerDialog.TYPE_PRESETS)
					.setShowAlphaSlider(true)
					.setDialogId(PRIMARY_DARK)
					.setAllowPresets(false)
					.create();
			colorPickerDialog.setColorPickerDialogListener(pickerDialogListener);
			//noinspection deprecation
			colorPickerDialog.show(getFragmentManager(), "color-picker-dialog");
		});

		accentOpt.setOnClickListener(v -> {
			ColorPickerDialog colorPickerDialog = ColorPickerDialog.newBuilder().setColor(preferences.getInt(Values.SharedPrefsTag.ACCENT_COLOR, Color.parseColor("#D81B60")))
					.setDialogTitle(R.string.color_picker)
					.setDialogType(ColorPickerDialog.TYPE_PRESETS)
					.setShowAlphaSlider(true)
					.setDialogId(ACCENT)
					.setAllowPresets(false)
					.create();
			colorPickerDialog.setColorPickerDialogListener(pickerDialogListener);
			//noinspection deprecation
			colorPickerDialog.show(getFragmentManager(), "color-picker-dialog");
		});


		titleOpt.setOnClickListener(v -> {
			ColorPickerDialog colorPickerDialog = ColorPickerDialog.newBuilder().setColor(preferences.getInt(Values.SharedPrefsTag.TITLE_COLOR, Color.parseColor("#FFFFFF")))
					.setDialogTitle(R.string.color_picker)
					.setDialogType(ColorPickerDialog.TYPE_PRESETS)
					.setShowAlphaSlider(true)
					.setDialogId(TITLE)
					.setAllowPresets(false)
					.create();
			colorPickerDialog.setColorPickerDialogListener(pickerDialogListener);
			//noinspection deprecation
			colorPickerDialog.show(getFragmentManager(), "color-picker-dialog");
		});


//        setNightOpt.setOnClickListener(v -> {
//
//            SharedPreferences.Editor editor = preferences.edit();
//            if (Values.BackgroundStyle.NIGHT_MODE) {
//                editor.putBoolean(Values.SharedPrefsTag.AUTO_NIGHT_MODE, false);
//                mNightSwitch.setChecked(false);
//                Values.BackgroundStyle.NIGHT_MODE = false;
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//            } else {
//                editor.putBoolean(Values.SharedPrefsTag.AUTO_NIGHT_MODE, true);
//                mNightSwitch.setChecked(true);
//                Values.BackgroundStyle.NIGHT_MODE = true;
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//            }
//            editor.apply();
//
//            Utils.Ui.inDayNightSet(preferences);
//        });

//        //night opt
//        mNightSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
//
//            SharedPreferences.Editor editor = preferences.edit();
//            if (isChecked) {
//                editor.putBoolean(Values.SharedPrefsTag.AUTO_NIGHT_MODE, true);
//                Values.BackgroundStyle.NIGHT_MODE = true;
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//            } else {
//                editor.putBoolean(Values.SharedPrefsTag.AUTO_NIGHT_MODE, false);
//                Values.BackgroundStyle.NIGHT_MODE = false;
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//            }
//            editor.apply();
//
//            Utils.Ui.inDayNightSet(preferences);
//
//        });

		styleOpt.setOnClickListener(v -> {

			SharedPreferences.Editor editor = preferences.edit();
			if (Values.BackgroundStyle.DETAIL_BACKGROUND.equals(Values.BackgroundStyle.STYLE_BACKGROUND_BLUR)) {
				Values.BackgroundStyle.DETAIL_BACKGROUND = Values.BackgroundStyle.STYLE_BACKGROUND_AUTO_COLOR;
				mStyleSwitch.setChecked(false);
				editor.putString(Values.SharedPrefsTag.DETAIL_BG_STYLE, Values.BackgroundStyle.STYLE_BACKGROUND_AUTO_COLOR);
			} else {
				Values.BackgroundStyle.DETAIL_BACKGROUND = Values.BackgroundStyle.STYLE_BACKGROUND_BLUR;
				mStyleSwitch.setChecked(true);
				editor.putString(Values.SharedPrefsTag.DETAIL_BG_STYLE, Values.BackgroundStyle.STYLE_BACKGROUND_BLUR);
			}
			editor.apply();

		});

		mStyleSwitch.setClickable(false);

//        mStyleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            SharedPreferences.Editor editor = preferences.edit();
//            mStyleSwitch.setChecked(isChecked);
//            if (Values.BackgroundStyle.DETAIL_BACKGROUND.equals(Values.BackgroundStyle.STYLE_BACKGROUND_BLUR)) {
//                mStyleSwitch.setChecked(false);
//                editor.putString(Values.SharedPrefsTag.DETAIL_BG_STYLE, Values.BackgroundStyle.STYLE_BACKGROUND_AUTO_COLOR);
//            } else {
//                mStyleSwitch.setChecked(true);
//                editor.putString(Values.SharedPrefsTag.DETAIL_BG_STYLE, Values.BackgroundStyle.STYLE_BACKGROUND_BLUR);
//            }
//            editor.apply();
//        });

		mSettingsBinding.colorNoti.setOnClickListener(v -> {
			final SharedPreferences.Editor editor = preferences.edit();
			final Intent intent = new Intent(this, MusicService.class);
			if (preferences.getBoolean(NOTIFICATION_COLORIZED, true)) {
				editor.putBoolean(NOTIFICATION_COLORIZED, false);
				if (editor.commit()) {
					mSettingsBinding.colorNotiSwitch.setChecked(false);
					intent.putExtra(Values.SharedPrefsTag.NOTIFICATION_COLORIZED, false);
				} else {
					Toast.makeText(SettingsActivity.this, "Set Colorized Error...", Toast.LENGTH_SHORT).show();
				}
			} else {
				editor.putBoolean(NOTIFICATION_COLORIZED, true);
				if (editor.commit()) {
					mSettingsBinding.colorNotiSwitch.setChecked(true);
					intent.putExtra(Values.SharedPrefsTag.NOTIFICATION_COLORIZED, true);
				} else {
					Toast.makeText(SettingsActivity.this, "Set Colorized Error...", Toast.LENGTH_SHORT).show();
				}
			}

			startService(intent);

			// TODO: 2019/5/30 专门设立一个action用来更新 notification
			if (Data.HAS_PLAYED) {
				try {
					if (Data.sMusicBinder.isPlayingMusic()) {
						ReceiverOnMusicPlay.startService(this, MusicService.ServiceActions.ACTION_PLAY);
					} else {
						ReceiverOnMusicPlay.startService(this, MusicService.ServiceActions.ACTION_PAUSE);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});

		mSettingsBinding.statusColor.setOnClickListener(v -> {
			final SharedPreferences.Editor editor = preferences.edit();
			if (preferences.getBoolean(Values.SharedPrefsTag.TRANSPORT_STATUS, false)) {
				editor.putBoolean(Values.SharedPrefsTag.TRANSPORT_STATUS, false);
				mSettingsBinding.colorStatusSwitch.setChecked(false);
			} else {
				editor.putBoolean(Values.SharedPrefsTag.TRANSPORT_STATUS, true);
				mSettingsBinding.colorStatusSwitch.setChecked(true);
			}
			editor.apply();
		});

		mSettingsBinding.hideShort.setOnClickListener(v -> {
			final SharedPreferences.Editor editor = preferences.edit();
			if (preferences.getBoolean(HIDE_SHORT_SONG, true)) {
				editor.putBoolean(Values.SharedPrefsTag.HIDE_SHORT_SONG, false);
				mSettingsBinding.filterSwitch.setChecked(false);
			} else {
				editor.putBoolean(Values.SharedPrefsTag.HIDE_SHORT_SONG, true);
				mSettingsBinding.filterSwitch.setChecked(true);
			}
			editor.apply();
		});

		mSettingsBinding.itemBlacklist.setOnClickListener(v -> {
			LitePalDB blackList = new LitePalDB("BlackList", 1);
			blackList.addClassName(MyBlackPath.class.getName());
			LitePal.use(blackList);

			ArrayList<String> data = new ArrayList<>();
			List<MyBlackPath> lists = LitePal.findAll(MyBlackPath.class);
			for (MyBlackPath path : lists) {
				data.add(path.getDirPath());
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
			builder.setTitle(getString(R.string.black_list));
			builder.setCancelable(false);
			ArrayAdapter<String> blackPathAdapter = new ArrayAdapter<>(SettingsActivity.this, android.R.layout.simple_list_item_1, data);
			//item on click
			builder.setAdapter(blackPathAdapter, (dialog, index) -> {
				AlertDialog.Builder rmBuilder = new AlertDialog.Builder(SettingsActivity.this);
				rmBuilder.setTitle(getString(R.string.remove_frome_black_list));
				rmBuilder.setMessage("Remove " + blackPathAdapter.getItem(index) + " from blacklist?");
				rmBuilder.setPositiveButton(getString(R.string.remove), (dialog16, which) -> {
					data.remove(index);
					blackPathAdapter.notifyDataSetChanged();
					dialog16.dismiss();
					builder.show();
				});
				rmBuilder.setNeutralButton(getString(R.string.cancel), (dialog15, which) -> {
					data.clear();
					blackPathAdapter.notifyDataSetChanged();
					dialog15.dismiss();
					builder.show();
				});
				rmBuilder.show();
			});

			builder.setNeutralButton(getString(R.string.clear), (dialog, which) -> {
				AlertDialog.Builder sureBuilder = new AlertDialog.Builder(SettingsActivity.this);
				sureBuilder.setTitle(getString(R.string.are_u_sure));
				sureBuilder.setCancelable(false);
				sureBuilder.setNegativeButton(getString(R.string.sure), (dialog14, which13) -> {
					data.clear();
					blackPathAdapter.notifyDataSetChanged();
					dialog14.dismiss();
					builder.show();
				});
				sureBuilder.setPositiveButton(getString(R.string.cancel), (dialog13, which12) -> {
					dialog13.dismiss();
					builder.show();
				});
				sureBuilder.show();
			});
			builder.setPositiveButton(getString(R.string.add), (dialog, which) -> {
				AlertDialog.Builder dirBuilder = new AlertDialog.Builder(SettingsActivity.this);
				dirBuilder.setCancelable(false);
				File sdcard = Environment.getExternalStorageDirectory();
				final File[] currentDir = {sdcard};
				dirBuilder.setTitle(sdcard.getPath());

				List<String> pathList = new ArrayList<>();

				//sort
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					pathList.sort(String::compareTo);
				}

				for (File file : sdcard.listFiles()) {
					if (file.isFile()) {
						continue;
					}
					pathList.add(file.getName());
				}

				ArrayAdapter<String> pathAdapter = new ArrayAdapter<>(SettingsActivity.this, android.R.layout.simple_list_item_1, pathList);
				dirBuilder.setAdapter(pathAdapter, (dialog1, index) -> {
					if (!currentDir[0].getAbsolutePath().equals(sdcard.getAbsolutePath()) && index == 0) {
						if (currentDir[0].getParentFile() != null) {
							currentDir[0] = currentDir[0].getParentFile();

						}
					} else {
						currentDir[0] = new File(currentDir[0].getAbsolutePath() + "/" + pathList.get(index));
					}
					Log.d(TAG, "onClick: " + currentDir[0].getAbsolutePath());
					pathList.clear();

					if (!currentDir[0].getAbsolutePath().equals(sdcard.getAbsolutePath())) {
						pathList.add("...");
					}

					for (File f : currentDir[0].listFiles()) {
						if (f.isFile()) {
							continue;
						}
						pathList.add(f.getName());
					}

					//sort
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
						pathList.sort(String::compareTo);
					}
					pathAdapter.notifyDataSetChanged();
					dirBuilder.show();
				});
				dirBuilder.setPositiveButton(getString(R.string.confirm), (dialog12, which1) -> {
					data.add(currentDir[0].getAbsolutePath());

					blackPathAdapter.notifyDataSetChanged();
					dialog12.dismiss();
					builder.show();
				});
				dirBuilder.show();
			});

			builder.setNegativeButton(getString(R.string.done), (dialog, which) -> {
				/*
				 * if double click, will throw
				 * no such table: myblackpath (code 1 SQLITE_ERROR):
				 * , while compiling: DELETE FROM myblackpath
				 * */
				try {
					LitePal.deleteAll(MyBlackPath.class);
				} catch (Exception e) {
					Log.d(TAG, "onCreate: " + e.getMessage());
				}

				//add to db
				for (String path : data) {
					MyBlackPath blackPath = new MyBlackPath();
					blackPath.setDirPath(path);
					blackPath.save();
				}
				LitePal.useDefault();
				dialog.dismiss();
			});

			builder.show();
		});

		mSettingsBinding.itemAlbumData.setOnClickListener(v -> {
			final SharedPreferences.Editor editor = preferences.edit();
			if (preferences.getBoolean(USE_NET_WORK_ALBUM, false)) {
				editor.putBoolean(Values.SharedPrefsTag.USE_NET_WORK_ALBUM, false);
				mSettingsBinding.albumSwitch.setChecked(false);
			} else {
				editor.putBoolean(Values.SharedPrefsTag.USE_NET_WORK_ALBUM, true);
				mSettingsBinding.albumSwitch.setChecked(true);
			}
			editor.apply();
		});

		mSettingsBinding.itemStyleSet.setOnClickListener(v -> {
			AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
			builder.setTitle(getString(R.string.set_recyclerview_item_style));

			LinearLayout linearLayout = new LinearLayout(SettingsActivity.this);
			linearLayout.setOrientation(LinearLayout.VERTICAL);

			View s0 = getLayoutInflater().inflate(R.layout.recycler_music_list_item, null);
			View s1 = getLayoutInflater().inflate(R.layout.recycler_music_list_item_style_1, null);

			s0.findViewById(R.id.music_item_expand_view).setVisibility(View.GONE);
			s1.findViewById(R.id.music_item_expand_view).setVisibility(View.GONE);

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			params.topMargin = (int) getResources().getDimension(R.dimen.margin_16);
			params.leftMargin = (int) getResources().getDimension(R.dimen.margin_16);
			params.rightMargin = (int) getResources().getDimension(R.dimen.margin_16);

			s0.setLayoutParams(params);

			LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			params2.topMargin = (int) getResources().getDimension(R.dimen.margin_16);
			params2.leftMargin = (int) getResources().getDimension(R.dimen.margin_16);
			params2.rightMargin = (int) getResources().getDimension(R.dimen.margin_16);
			params2.bottomMargin = (int) getResources().getDimension(R.dimen.margin_16);

			s1.setLayoutParams(params2);

			s0.setOnClickListener(v1 -> {
				PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit().putInt(Values.SharedPrefsTag.RECYCLER_VIEW_ITEM_STYLE, 0).apply();
				Toast.makeText(this, "Select style 0", Toast.LENGTH_SHORT).show();
			});

			s1.setOnClickListener(v1 -> {
				PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit().putInt(Values.SharedPrefsTag.RECYCLER_VIEW_ITEM_STYLE, 1).apply();
				Toast.makeText(this, "Select style 1", Toast.LENGTH_SHORT).show();
			});

			linearLayout.addView(s0);
			linearLayout.addView(s1);

			builder.setView(linearLayout);
			builder.show();
		});

		mSettingsBinding.itemCleanAlbum.setOnClickListener(v -> clearAlbums());

		mSettingsBinding.itemCleanArtist.setOnClickListener(v -> clearArtists());
	}

	@Override
	public String getActivityTAG() {
		return TAG;
	}

	@Override
	public void inflateCommonMenu() {
		mToolbar.inflateMenu(R.menu.menu_toolbar_settings);
	}

	@Override
	public void inflateChooseMenu() {

	}

	private void clearAnimation() {
		mPrimaryImage.clearAnimation();
		mToolbar.clearAnimation();
		mAppBarLayout.clearAnimation();
		mPrimaryDarkImage.clearAnimation();
		mAccentImage.clearAnimation();
	}

	/**
	 * setup imageViews
	 */
	private void initPreView() {
//		mNightSwitch.setChecked(Values.BackgroundStyle.NIGHT_MODE);

		mStyleSwitch.setChecked(Values.BackgroundStyle.STYLE_BACKGROUND_AUTO_COLOR.equals(preferences
				.getString(Values.SharedPrefsTag.DETAIL_BG_STYLE, Values.BackgroundStyle.STYLE_BACKGROUND_BLUR)));

		mSettingsBinding.colorNotiSwitch.setChecked(preferences.getBoolean(NOTIFICATION_COLORIZED, true));

		mSettingsBinding.colorStatusSwitch.setChecked(preferences.getBoolean(Values.SharedPrefsTag.TRANSPORT_STATUS, false));

		mSettingsBinding.filterSwitch.setChecked(preferences.getBoolean(HIDE_SHORT_SONG, true));

		mSettingsBinding.albumSwitch.setChecked(preferences.getBoolean(USE_NET_WORK_ALBUM, false));

		mStyleSwitch.setChecked(Values.BackgroundStyle.STYLE_BACKGROUND_BLUR.equals(preferences
				.getString(Values.SharedPrefsTag.DETAIL_BG_STYLE, Values.BackgroundStyle.STYLE_BACKGROUND_BLUR)));

		mSettingsBinding.textDHideShort.setText(getString(R.string.skip_songs_that_are_less_than_the_minimum_duration
				, MainActivity.DEFAULT_SHORT_DURATION));
	}

	@Override
	public void initStyle() {
		super.initStyle();
		mPrimaryImage.setBackgroundColor(Utils.Ui.getPrimaryColor(this));
		mPrimaryDarkImage.setBackgroundColor(Utils.Ui.getPrimaryDarkColor(this));
		mAccentImage.setBackgroundColor(Utils.Ui.getAccentColor(this));
		mTitleImage.setBackgroundColor(Utils.Ui.getTitleColor(this));

		final ImageView imageView = findViewById(R.id.theme_preview);

		//load theme
		if (Data.sTheme != null) {
			imageView.setVisibility(View.VISIBLE);
			GlideApp.with(this)
					.load(Data.sTheme.getThumbnail())
					.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.into(imageView);
		} else {
			imageView.setVisibility(View.GONE);
		}

	}

	/**
	 * clearAlbums data
	 */
	@SuppressLint("StaticFieldLeak")
	public void clearAlbums() {
		new AsyncTask<Void, Void, Void>() {

			AlertDialog mAlertDialog;

			@Override
			protected void onPreExecute() {
				mAlertDialog = DialogUtil.getLoadingDialog(SettingsActivity.this);
				mAlertDialog.show();
			}

			@Override
			protected Void doInBackground(Void... voids) {
				String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separatorChar + "AlbumCovers";
				Utils.IO.delFolder(path);
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				mAlertDialog.dismiss();
			}
		}.execute();
	}

	/**
	 * clearArtists data
	 */
	@SuppressLint("StaticFieldLeak")
	public void clearArtists() {
		new AsyncTask<Void, Void, Void>() {

			AlertDialog mAlertDialog;

			@Override
			protected void onPreExecute() {
				mAlertDialog = DialogUtil.getLoadingDialog(SettingsActivity.this);
				mAlertDialog.show();
			}

			@Override
			protected Void doInBackground(Void... voids) {
				String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separatorChar + "ArtistCovers";
				Utils.IO.delFolder(path);
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				mAlertDialog.dismiss();
			}
		}.execute();
	}

}
