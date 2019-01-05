/*
 * ************************************************************
 * 文件：ThemeActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月05日 09:52:36
 * 上次修改时间：2019年01月05日 09:50:17
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.musicplayer.Adapters.ThemeAdapter;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Interface.IStyle;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.MyThemeDBHelper;
import top.geek_studio.chenlongcould.musicplayer.Utils.ThemeStore;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.databinding.ActivityThemeBinding;

public class ThemeActivity extends AppCompatActivity implements IStyle {

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
                    AlertDialog.Builder builder = new AlertDialog.Builder(ThemeActivity.this);
                    builder.setTitle(getString(R.string.sure_int));
                    builder.setMessage(getString(R.string.sure_set_def_theme_int));
                    builder.setCancelable(true);
                    builder.setNegativeButton(getString(R.string.sure), (dialog, which) -> {
                        Data.sTheme = null;
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ThemeActivity.this).edit();
                        editor.putInt(Values.SharedPrefsTag.SELECT_THEME, -1);
                        editor.apply();
                    });
                    builder.setPositiveButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
                    builder.show();

                }
                break;
            }
            return true;
        });

        mDBHelper = new MyThemeDBHelper(this, ThemeStore.DATA_BASE_NAME, null, 1);
        mThemeBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        do1();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
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
                        Log.d(TAG, "onActivityResult: " + uri.toString());
                        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                        if (cursor != null) {
                            cursor.moveToFirst();
                            String documentId = cursor.getString(cursor.getColumnIndexOrThrow("document_id"));
                            String path = Environment.getExternalStorageDirectory().getPath() + File.separatorChar + documentId.split(":")[1];

                            Observable.create((ObservableOnSubscribe<Theme>) emitter -> {
                                int name = themeDir.listFiles().length;
                                Utils.IO.Unzip(path, themeDir.getAbsolutePath() + File.separatorChar + name + File.separatorChar);

                                final File themeFile = new File(themeDir.getAbsolutePath() + File.separatorChar + name);
                                final Theme theme = Utils.ThemeUtils.fileToTheme(themeFile);

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
                                    });
                        }
                    }
                }
            }
            break;
            default:
        }
    }

    public AlertDialog fastLoadingDialog(@SuppressWarnings("SameParameterValue") String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.dialog_loading);
        builder.setTitle(title);
        builder.setCancelable(false);
        return builder.create();
    }

    @SuppressLint("StaticFieldLeak")
    private void do1() {
        new AsyncTask<Void, Void, Void>() {

            private AlertDialog mDialog;

            @Override
            protected void onPreExecute() {
                mDialog = fastLoadingDialog("loading...");
                mDialog.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
//                SQLiteDatabase database = mDBHelper.getWritableDatabase();
//                final ContentValues values = new ContentValues();

                final File themeDir = getExternalFilesDir(ThemeStore.DIR_NAME);
                final File defTheme1 = new File(getExternalFilesDir(ThemeStore.DIR_NAME).getAbsolutePath() + File.separatorChar + "0");
                final File defTheme2 = new File(getExternalFilesDir(ThemeStore.DIR_NAME).getAbsolutePath() + File.separatorChar + "1");

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

                        Utils.IO.Unzip(defFile1.getAbsolutePath(), themeDir.getAbsolutePath() + File.separatorChar + 0 + File.separatorChar);
                        Utils.IO.Unzip(defFile2.getAbsolutePath(), themeDir.getAbsolutePath() + File.separatorChar + 1 + File.separatorChar);
                        defFile1.delete();
                        defFile2.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    final File[] files = themeDir.listFiles();
                    if (files.length > 500) {
                        Toast.makeText(ThemeActivity.this, "Themes > 500, too more!", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    ArrayList<File> fileArrayList = new ArrayList<>(Arrays.asList(files));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        fileArrayList.sort(File::compareTo);
                    }

                    int themeId = -1;
                    for (File f : fileArrayList) {
                        Log.d(TAG, "doInBackground: " + f.getPath());
                        if (f.isDirectory()) {
                            final File detailText = new File(f.getPath() + File.separatorChar + ThemeStore.DETAIL_FILE_NAME);

                            themeId++;

                            String title = "null";
                            String date = "null";
                            String nav_name = "null";
                            String author = "null";
                            String support_area = "null";
                            String primary_color = "null";
                            String thumbnail = "null";
                            String select = "null";
                            String path = f.getPath();

                            final BufferedReader bufferedReader = new BufferedReader(new FileReader(detailText));
                            String line;

                            int items = 0;
                            while ((line = bufferedReader.readLine()) != null) {
                                if (line.contains(ThemeStore.ThemeColumns.AUTHOR)) {
                                    author = line.split(":")[1];
                                    Log.d(TAG, "doInBackground: " + author + " @ " + detailText.getPath());
                                    items++;
                                }

                                if (line.contains(ThemeStore.ThemeColumns.TITLE)) {
                                    title = line.split(":")[1];
                                    Log.d(TAG, "doInBackground: " + title + " @ " + detailText.getPath());
                                    items++;
                                }

                                if (line.contains(ThemeStore.ThemeColumns.NAV_NAME)) {
                                    nav_name = line.split(":")[1];
                                    Log.d(TAG, "doInBackground: " + nav_name + " @ " + detailText.getPath());
                                    items++;
                                }

                                if (line.contains(ThemeStore.ThemeColumns.THUMBNAIL)) {
                                    thumbnail = f.getPath() + File.separatorChar + line.split(":")[1];
                                    Log.d(TAG, "doInBackground: " + thumbnail + " @ " + detailText.getPath());
                                    items++;
                                }

                                if (line.contains(ThemeStore.ThemeColumns.SUPPORT_AREA)) {
                                    support_area = line.split(":")[1];
                                    Log.d(TAG, "doInBackground: " + support_area + " @ " + detailText.getPath());
                                    items++;
                                }

                                if (line.contains(ThemeStore.ThemeColumns.PRIMARY_COLOR)) {
                                    primary_color = line.split(":")[1];
                                    Log.d(TAG, "doInBackground: " + primary_color);
                                    items++;
                                }

                                if (line.contains(ThemeStore.ThemeColumns.DATE)) {
                                    date = line.split(":")[1];
                                    Log.d(TAG, "doInBackground: " + date);
                                    items++;
                                }

                                if (line.contains(ThemeStore.ThemeColumns.SELECT)) {
                                    select = line.split(":")[1];
                                    Log.d(TAG, "doInBackground: " + select);
                                    items++;
                                }
                            }

                            if (items >= 8) {
                                Log.d(TAG, "doInBackground: add!");
                                mThemes.add(new Theme(themeId, path, title, date, nav_name, author, support_area, primary_color, thumbnail, select));
//                                values.put(ThemeStore.ThemeColumns.AUTHOR, author);
//                                values.put(ThemeStore.ThemeColumns.TITLE, title);
//                                values.put(ThemeStore.ThemeColumns.NAV_NAME, nav_name);
//                                values.put(ThemeStore.ThemeColumns.THUMBNAIL, thumbnail);
//                                values.put(ThemeStore.ThemeColumns.SUPPORT_AREA, support_area);
//                                values.put(ThemeStore.ThemeColumns.PRIMARY_COLOR, primary_color);
//                                values.put(ThemeStore.ThemeColumns.PRIMARY_COLOR, date);
//                                database.insert(ThemeStore.TABLE, null, values);
                            }
//                            values.clear();
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Log.d(TAG, "onPostExecute: " + mThemes.size() + " " + mThemes.get(1).getPath());
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

    public static class Theme {
        int id;
        String path;
        String title;
        String date;
        String nav_name;
        String author;
        String support_area;
        String primary_color;
        String thumbnail;
        String select;

        public Theme(int id, String path, String title, String date, String nav_name, String author, String support_area, String primary_color, String thumbnail, String select) {
            this.id = id;
            this.path = path;
            this.title = title;
            this.date = date;
            this.nav_name = nav_name;
            this.author = author;
            this.support_area = support_area;
            this.primary_color = primary_color;
            this.thumbnail = thumbnail;
            this.select = select;
        }

        public String getTitle() {
            return title;
        }

        public String getDate() {
            return date;
        }

        public String getNav_name() {
            return nav_name;
        }

        public String getAuthor() {
            return author;
        }

        public String getSupport_area() {
            return support_area;
        }

        public String getPrimary_color() {
            return primary_color;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public int getId() {
            return id;
        }

        public String getPath() {
            return path;
        }
    }

}
