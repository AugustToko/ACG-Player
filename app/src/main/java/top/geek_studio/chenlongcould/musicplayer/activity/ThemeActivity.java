/*
 * ************************************************************
 * 文件：ThemeActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月18日 18:58:29
 * 上次修改时间：2019年01月18日 12:25:42
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.geeklibrary.DialogUtil;
import top.geek_studio.chenlongcould.geeklibrary.theme.Theme;
import top.geek_studio.chenlongcould.geeklibrary.theme.ThemeStore;
import top.geek_studio.chenlongcould.geeklibrary.theme.ThemeUtils;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.base.BaseCompatActivity;
import top.geek_studio.chenlongcould.musicplayer.adapter.ThemeAdapter;
import top.geek_studio.chenlongcould.musicplayer.databinding.ActivityThemeBinding;
import top.geek_studio.chenlongcould.musicplayer.utils.MyThemeDBHelper;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author chenlongcould
 */
public final class ThemeActivity extends BaseCompatActivity {

	public static final String TAG = "ThemeActivity";

	public static final String DEFAULT_THEME = "null";

	private static final int REQUEST_ADD_THEME = 1;

	private File themeDir;

	private MyThemeDBHelper mDBHelper;

	private ActivityThemeBinding mThemeBinding;

	private ThemeAdapter mThemeAdapter;

	private ArrayList<Theme> mThemes = new ArrayList<>();

	private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
			= item -> {
		switch (item.getItemId()) {
			case R.id.navigation_home:
				return true;
			case R.id.navigation_dashboard:
				return true;
			case R.id.navigation_notifications:
				return true;
			default:
		}
		return false;
	};

	private Disposable mDisposable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mThemeBinding = DataBindingUtil.setContentView(this, R.layout.activity_theme);
		super.initView(mThemeBinding.toolbar, mThemeBinding.appBarLayout);
		super.onCreate(savedInstanceState);

		noteCheck();

		themeDir = getExternalFilesDir(ThemeStore.DIR_NAME);

		inflateCommonMenu();
		mThemeBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

		mDBHelper = new MyThemeDBHelper(this, ThemeStore.DATA_BASE_NAME, null, 1);
		mThemeBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
		mThemeAdapter = new ThemeAdapter(ThemeActivity.this, mThemes);
		mThemeBinding.recyclerView.setAdapter(mThemeAdapter);

		loadDataUI();

		BottomNavigationView navigation = findViewById(R.id.navigation);
		navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
	}

	@Override
	protected void onDestroy() {
		mDBHelper.close();
		if (mDisposable != null && !mDisposable.isDisposed()) {
			mDisposable.dispose();
		}
		for (Disposable disposable : mThemeAdapter.getDisposables()) {
			if (disposable != null && !disposable.isDisposed()) {
				disposable.dispose();
			}
		}
		super.onDestroy();
	}

	@Override
	public String getActivityTAG() {
		return TAG;
	}

	@Override
	public void inflateCommonMenu() {
		mThemeBinding.toolbar.inflateMenu(R.menu.menu_toolbar_theme);
		mThemeBinding.toolbar.setOnMenuItemClickListener(menuItem -> {
			switch (menuItem.getItemId()) {
				case R.id.menu_toolbar_theme_add: {
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.setType("application/zip");
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					startActivityForResult(intent, REQUEST_ADD_THEME);
				}
				break;

				case R.id.menu_toolbar_theme_reset: {
					final AlertDialog.Builder builder = new AlertDialog.Builder(ThemeActivity.this);
					builder.setTitle(getString(R.string.sure_int));
					builder.setMessage(getString(R.string.sure_set_def_theme_int));
					builder.setCancelable(true);
					builder.setNegativeButton(getString(R.string.sure), (dialog, which) -> {
						Data.sTheme = null;
						SharedPreferences.Editor editor = super.preferences.edit();
						editor.putString(Values.SharedPrefsTag.SELECT_THEME, DEFAULT_THEME);
						editor.apply();
						editor.putBoolean(Values.SharedPrefsTag.THEME_USE_NOTE, false);
						editor.apply();

						loadDataUI();
					});
					builder.setPositiveButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
					builder.show();

				}
				break;

				case R.id.menu_toolbar_theme_sync: {
					loadDataUI();
				}
				break;

				case R.id.menu_toolbar_theme_load_def: {
					preferences.edit().putBoolean(Values.SharedPrefsTag.LOAD_DEFAULT_THEME, true).apply();
					loadDataUI();
				}
				break;

				case R.id.menu_toolbar_theme_note: {
					final SharedPreferences.Editor preferences = super.preferences.edit();
					preferences.putBoolean(Values.SharedPrefsTag.THEME_USE_NOTE, false);
					preferences.apply();
					noteCheck();
				}
				break;

				default:
			}
			return true;
		});
	}

	@Override
	public void inflateChooseMenu() {

	}

	private void noteCheck() {
		if (!preferences.getBoolean(Values.SharedPrefsTag.THEME_USE_NOTE, false)) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setCancelable(false);
			builder.setTitle(getString(R.string.theme_use_note));
			builder.setMessage(getString(R.string.theme_note));
			builder.setNeutralButton(getString(R.string.agree), (dialog, which) -> {
				SharedPreferences.Editor editor = preferences.edit();
				editor.putBoolean(Values.SharedPrefsTag.THEME_USE_NOTE, true);
				editor.apply();
				dialog.dismiss();
			});

			builder.setPositiveButton(getString(R.string.refuse), (dialog, which) -> finish());
			builder.show();
		}
	}

	@SuppressLint("CheckResult")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case REQUEST_ADD_THEME: {
				if (resultCode == RESULT_OK && data != null) {
					Uri uri = data.getData();
					if (uri != null) {
						final Cursor cursor = getContentResolver().query(uri, null, null, null, null);
						if (cursor != null) {
							cursor.moveToFirst();

							final String documentId = cursor.getString(cursor.getColumnIndexOrThrow("document_id"));
							final String path = Environment.getExternalStorageDirectory().getPath() + File.separatorChar + documentId.split(":")[1];

							final AlertDialog load = DialogUtil.getLoadingDialog(ThemeActivity.this, "Loading...");
							load.show();

							mDisposable = Observable.create((ObservableOnSubscribe<Theme>) emitter -> {
								final long name = System.currentTimeMillis();
								Utils.IO.Unzip(path, themeDir.getAbsolutePath() + File.separatorChar + name + File.separatorChar);

								final File themeFile = new File(themeDir.getAbsolutePath() + File.separatorChar + name);
								final Theme theme = ThemeUtils.fileToTheme(themeFile);

								if (theme != null) {
									emitter.onNext(theme);
								} else {
									Utils.IO.delFolder(themeFile.getAbsolutePath());
								}

								cursor.close();
							}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
									.subscribe(result -> {
										mThemes.add(result);
										mThemeAdapter.notifyItemInserted(mThemes.size() - 1);
										load.dismiss();
									});
						}
					}
				}
			}
			break;
			default:
		}
	}

	public void loadDataUI() {
		mThemes.clear();

		AlertDialog mDialog = DialogUtil.getLoadingDialog(ThemeActivity.this, "Loading...");
		mDialog.show();
		mDisposable = Observable.create((ObservableOnSubscribe<Integer>) observableEmitter -> {
			final File themeDir = getExternalFilesDir(ThemeStore.DIR_NAME);

			if (themeDir == null) {
				return;
			}

			if (preferences.getBoolean(Values.SharedPrefsTag.LOAD_DEFAULT_THEME, true)) {
				final File defTheme1 = new File(themeDir.getAbsolutePath() + File.separatorChar + "0_def");
				final File defTheme2 = new File(themeDir.getAbsolutePath() + File.separatorChar + "01_def");

				//load default themes
				if (defTheme1.exists()) {
					//noinspection ResultOfMethodCallIgnored
					defTheme1.delete();
				}
				if (defTheme2.exists()) {
					//noinspection ResultOfMethodCallIgnored
					defTheme2.delete();
				}
				runOnUiThread(() -> Toast.makeText(ThemeActivity.this, "Loading Default Theme...", Toast.LENGTH_SHORT).show());
				File defFile1 = new File(themeDir.getAbsolutePath() + File.separatorChar + "content_1.zip");
				File defFile2 = new File(themeDir.getAbsolutePath() + File.separatorChar + "content_2.zip");
				try {
					InputStream inputStream1 = getAssets().open("content_1.zip");
					InputStream inputStream2 = getAssets().open("content_2.zip");

					byte[] b1 = new byte[inputStream1.available()];
					if (inputStream1.read(b1) != -1) {
						OutputStream output = new FileOutputStream(defFile1);
						BufferedOutputStream bufferedOutput = new BufferedOutputStream(output);
						bufferedOutput.write(b1);
					}

					byte[] b2 = new byte[inputStream2.available()];
					if (inputStream2.read(b2) != -1) {
						OutputStream output = new FileOutputStream(defFile2);
						BufferedOutputStream bufferedOutput = new BufferedOutputStream(output);
						bufferedOutput.write(b2);
					}

					Utils.IO.Unzip(defFile1.getAbsolutePath(), themeDir.getAbsolutePath() + File.separatorChar + "0_def" + File.separatorChar);
					Utils.IO.Unzip(defFile2.getAbsolutePath(), themeDir.getAbsolutePath() + File.separatorChar + "01_def" + File.separatorChar);

					//noinspection ResultOfMethodCallIgnored
					defFile1.delete();
					//noinspection ResultOfMethodCallIgnored
					defFile2.delete();

					preferences.edit().putBoolean(Values.SharedPrefsTag.LOAD_DEFAULT_THEME, false).apply();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			final File[] themeFiles = themeDir.listFiles();
			if (themeFiles.length > 500) {
				Toast.makeText(ThemeActivity.this, "Themes > 500, too more!", Toast.LENGTH_SHORT).show();
				finish();
			}

			final ArrayList<File> fileArrayList = new ArrayList<>(Arrays.asList(themeFiles));
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				fileArrayList.sort(File::compareTo);
			}

			//转换File 为 Theme
			for (File f : fileArrayList) {
				mThemes.add(ThemeUtils.fileToTheme(f));
			}

			observableEmitter.onNext(0);
		}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(i -> {
			mDialog.dismiss();
			mThemeAdapter.notifyDataSetChanged();
			Log.d(TAG, "loadDataUI: " + mThemes.size());
		});
	}

	public ViewGroup getRoot() {
		return getWindow().getDecorView().findViewById(android.R.id.content);
	}

	public ThemeAdapter getThemeAdapter() {
		return mThemeAdapter;
	}

	public ActivityThemeBinding getThemeBinding() {
		return mThemeBinding;
	}

	public ArrayList<Theme> getThemes() {
		return mThemes;
	}

}
