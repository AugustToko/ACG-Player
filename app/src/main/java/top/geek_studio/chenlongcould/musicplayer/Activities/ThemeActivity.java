/*
 * ************************************************************
 * 文件：ThemeActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月16日 20:43:13
 * 上次修改时间：2019年01月16日 08:33:01
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.geeklibrary.Theme.IStyle;
import top.geek_studio.chenlongcould.geeklibrary.Theme.Theme;
import top.geek_studio.chenlongcould.geeklibrary.Theme.ThemeStore;
import top.geek_studio.chenlongcould.geeklibrary.Theme.ThemeUtils;
import top.geek_studio.chenlongcould.musicplayer.Adapters.ThemeAdapter;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.MyThemeDBHelper;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.databinding.ActivityThemeBinding;

public class ThemeActivity extends MyBaseCompatActivity implements IStyle {

    public static final String TAG = "ThemeActivity";

    private static final int REQUEST_ADD_THEME = 1;

    private File themeDir;

    private MyThemeDBHelper mDBHelper;

    private ActivityThemeBinding mThemeBinding;

    private ThemeAdapter mThemeAdapter;

    private ArrayList<Theme> mThemes = new ArrayList<>();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_dashboard:
                    return true;
                case R.id.navigation_notifications:
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mThemeBinding = DataBindingUtil.setContentView(this, R.layout.activity_theme);

        noteCheck();

        themeDir = getExternalFilesDir(ThemeStore.DIR_NAME);

        mThemeBinding.toolbar.inflateMenu(R.menu.menu_toolbar_theme);
        mThemeBinding.toolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.menu_toolbar_theme_add: {
                    Log.d(TAG, "onCreate: add");
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/zip");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, REQUEST_ADD_THEME);
                }
                break;

                case R.id.menu_toolbar_theme_reset: {
                    final SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(this).edit();
                    preferences.putBoolean(Values.SharedPrefsTag.THEME_USE_NOTE, false);
                    preferences.apply();

                    final AlertDialog.Builder builder = new AlertDialog.Builder(ThemeActivity.this);
                    builder.setTitle(getString(R.string.sure_int));
                    builder.setMessage(getString(R.string.sure_set_def_theme_int));
                    builder.setCancelable(true);
                    builder.setNegativeButton(getString(R.string.sure), (dialog, which) -> {
                        Data.sTheme = null;
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ThemeActivity.this).edit();
                        editor.putString(Values.SharedPrefsTag.SELECT_THEME, "null");
                        editor.apply();

                        reLoadDataUi();
                    });
                    builder.setPositiveButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
                    builder.show();

                }
                break;

                case R.id.menu_toolbar_theme_sync: {
                    reLoadDataUi();
                }
                break;

                case R.id.menu_toolbar_theme_note: {
                    final SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(this).edit();
                    preferences.putBoolean(Values.SharedPrefsTag.THEME_USE_NOTE, false);
                    preferences.apply();
                    noteCheck();
                }
            }
            return true;
        });

        mDBHelper = new MyThemeDBHelper(this, ThemeStore.DATA_BASE_NAME, null, 1);
        mThemeBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadDataUI();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    protected void onDestroy() {
        mDBHelper.close();
        super.onDestroy();
    }

    private void noteCheck() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
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

    @Override
    protected void onResume() {
        super.onResume();
        initStyle();
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_THEME: {
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        final Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                        if (cursor != null) {
                            cursor.moveToFirst();

                            final String documentId = cursor.getString(cursor.getColumnIndexOrThrow("document_id"));
                            final String path = Environment.getExternalStorageDirectory().getPath() + File.separatorChar + documentId.split(":")[1];

                            final AlertDialog load = Utils.Ui.getLoadingDialog(ThemeActivity.this, "Loading...");
                            load.show();

                            Observable.create((ObservableOnSubscribe<Theme>) emitter -> {
                                final long name = System.currentTimeMillis();
                                Utils.IO.Unzip(path, themeDir.getAbsolutePath() + File.separatorChar + name + File.separatorChar);

                                final File themeFile = new File(themeDir.getAbsolutePath() + File.separatorChar + name);
                                final Theme theme = ThemeUtils.fileToTheme(themeFile);

                                if (theme != null)
                                    emitter.onNext(theme);
                                else
                                    Utils.IO.delFolder(themeFile.getAbsolutePath());

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

    @SuppressLint("StaticFieldLeak")
    private void loadDataUI() {
        new AsyncTask<Void, Void, Void>() {

            private AlertDialog mDialog;

            @Override
            protected void onPreExecute() {
                mDialog = Utils.Ui.getLoadingDialog(ThemeActivity.this, "Loading...");
                mDialog.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {

                final File themeDir = getExternalFilesDir(ThemeStore.DIR_NAME);

                if (themeDir == null) return null;

                final File defTheme1 = new File(getExternalFilesDir(ThemeStore.DIR_NAME).getAbsolutePath() + File.separatorChar + "0_def");
                final File defTheme2 = new File(getExternalFilesDir(ThemeStore.DIR_NAME).getAbsolutePath() + File.separatorChar + "01_def");

                //load default themes
                if (!defTheme1.exists() || defTheme1.isFile() || !defTheme2.exists() || defTheme2.isFile()) {
                    if (defTheme1.exists()) defTheme1.delete();
                    if (defTheme2.exists()) defTheme2.delete();
                    runOnUiThread(() -> Toast.makeText(ThemeActivity.this, "Loading Default Theme...", Toast.LENGTH_SHORT).show());
                    File defFile1 = new File(getExternalFilesDir(ThemeStore.DIR_NAME).getAbsolutePath() + File.separatorChar + "content_1.zip");
                    File defFile2 = new File(getExternalFilesDir(ThemeStore.DIR_NAME).getAbsolutePath() + File.separatorChar + "content_2.zip");
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

                        defFile1.delete();
                        defFile2.delete();
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

                for (File f : fileArrayList) {
                    mThemes.add(ThemeUtils.fileToTheme(f));
//                        Log.d(TAG, "doInBackground: " + f.getPath());
//                        if (f.isDirectory()) {
//                            final File detailText = new File(f.getPath() + File.separatorChar + ThemeStore.DETAIL_FILE_NAME);
//
//                            themeId++;
//
//                            //temp
//                            String title = "null";
//                            String date = "null";
//                            String nav_name = "null";
//                            String author = "null";
//                            String support_area = "null";
//                            String primary_color = "null";
//                            String accent_color = "null";
//                            String primary_color_dark = "null";
//                            String thumbnail = "null";
//                            String select = "null";
//                            String path = f.getPath();
//
//                            final BufferedReader bufferedReader = new BufferedReader(new FileReader(detailText));
//                            String line;
//
//                            int items = 0;
//                            while ((line = bufferedReader.readLine()) != null) {
//                                if (line.contains(ThemeStore.ThemeColumns.AUTHOR)) {
//                                    author = line.split(":")[1];
//                                    Log.d(TAG, "doInBackground: " + author + " @ " + detailText.getPath());
//                                    items++;
//                                }
//
//                                if (line.contains(ThemeStore.ThemeColumns.TITLE)) {
//                                    title = line.split(":")[1];
//                                    Log.d(TAG, "doInBackground: " + title + " @ " + detailText.getPath());
//                                    items++;
//                                }
//
//                                if (line.contains(ThemeStore.ThemeColumns.NAV_NAME)) {
//                                    nav_name = line.split(":")[1];
//                                    Log.d(TAG, "doInBackground: " + nav_name + " @ " + detailText.getPath());
//                                    items++;
//                                }
//
//                                if (line.contains(ThemeStore.ThemeColumns.THUMBNAIL)) {
//                                    thumbnail = f.getPath() + File.separatorChar + line.split(":")[1];
//                                    Log.d(TAG, "doInBackground: " + thumbnail + " @ " + detailText.getPath());
//                                    items++;
//                                }
//
//                                if (line.contains(ThemeStore.ThemeColumns.SUPPORT_AREA)) {
//                                    support_area = line.split(":")[1];
//                                    Log.d(TAG, "doInBackground: " + support_area + " @ " + detailText.getPath());
//                                    items++;
//                                }
//
//                                if (line.contains(ThemeStore.ThemeColumns.PRIMARY_COLOR)) {
//                                    primary_color = line.split(":")[1];
//                                    Log.d(TAG, "doInBackground: " + primary_color);
//                                    items++;
//                                }
//
//                                if (line.contains(ThemeStore.ThemeColumns.PRIMARY_COLOR_DARK)) {
//                                    primary_color_dark = line.split(":")[1];
//                                    Log.d(TAG, "doInBackground: " + select);
//                                    items++;
//                                }
//
//                                if (line.contains(ThemeStore.ThemeColumns.ACCENT_COLOR)) {
//                                    accent_color = line.split(":")[1];
//                                    Log.d(TAG, "doInBackground: " + select);
//                                    items++;
//                                }
//
//                                if (line.contains(ThemeStore.ThemeColumns.DATE)) {
//                                    date = line.split(":")[1];
//                                    Log.d(TAG, "doInBackground: " + date);
//                                    items++;
//                                }
//
//                                if (line.contains(ThemeStore.ThemeColumns.SELECT)) {
//                                    select = line.split(":")[1];
//                                    Log.d(TAG, "doInBackground: " + select);
//                                    items++;
//                                }
//                            }
//
//                            if (items >= ThemeStore.MIN_ITEM) {
//                                Log.d(TAG, "doInBackground: add!");
//                                final Theme theme = new Theme.Builder(themeId)
//                                        .setAccentColor(accent_color)
//                                        .setAuthor(author)
//                                        .setDate(date)
//                                        .setNavName(nav_name)
//                                        .setPath(path).setPrimaryColorDark(primary_color_dark)
//                                        .setSupportArea(support_area)
//                                        .setSelect(select)
//                                        .setTitle(title)
//                                        .setThumbnail(thumbnail)
//                                        .setPrimaryColor(primary_color)
//                                        .build();
//                                mThemes.add(theme);
//                            }
//                        }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mDialog.dismiss();
                mThemeAdapter = new ThemeAdapter(ThemeActivity.this, mThemes);
                mThemeBinding.recyclerView.setAdapter(mThemeAdapter);
            }
        }.execute();

    }


    @Override
    public void initStyle() {
        Utils.Ui.setTopBottomColor(this, mThemeBinding.appBarLayout, mThemeBinding.toolbar);
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

    /**
     * reload
     *
     * @apiNote use {@link #getThemeAdapter()} notify
     */
    public void reLoadDataUi() {
        mThemes.clear();
        loadDataUI();
    }

}
