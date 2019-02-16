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
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import top.geek_studio.chenlongcould.geeklibrary.DownloadUtil;
import top.geek_studio.chenlongcould.geeklibrary.HttpUtil;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.Models.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.MyApplication;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.AlbumDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.database.CustomAlbumPath;
import top.geek_studio.chenlongcould.musicplayer.thread_pool.AlbumThreadPool;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

public final class MyRecyclerAdapter2AlbumList extends RecyclerView.Adapter<MyRecyclerAdapter2AlbumList.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private static final String TAG = "AlbumAdapter";

    public static final int LINEAR_TYPE = 0;

    public static final int GRID_TYPE = 1;

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
            default:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item_album_grid, viewGroup, false);
        }
        final ViewHolder holder = new ViewHolder(view);

        holder.mUView.setOnClickListener(v -> {
            String keyWords = mAlbumNameList.get(holder.getAdapterPosition()).getAlbumName();

            final ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(mMainActivity, holder.mAlbumImage, mMainActivity.getString(R.string.transition_album_art));
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
        viewHolder.mAlbumImage.setTag(R.string.key_id_3, viewHolder.getAdapterPosition());      //set TAG

        AlbumThreadPool.post(() -> {
            final String[] albumPath = {null};

            AlbumItem albumItem = mAlbumNameList.get(i);
            int albumId = albumItem.getAlbumId();
            String albumName = albumItem.getAlbumName();
            String artist = albumItem.getArtist();

            final Cursor cursor = mMainActivity.getContentResolver().query(
                    Uri.parse(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI + String.valueOf(File.separatorChar) + albumId)
                    , new String[]{MediaStore.Audio.Albums.ALBUM_ART}, null, null, null);
            if (cursor != null && cursor.getCount() != 0) {
                cursor.moveToFirst();

                //set DefaultVal by MediaStore
                albumPath[0] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
                cursor.close();
            }

            if (TextUtils.isEmpty(albumPath[0]) || albumPath[0].equals("null") || albumPath[0].equals("none") || albumPath.equals("NONE")) {
                Log.d(TAG, "onBindViewHolder: album in DEFAULT_DB has not value, try load from CUSTOM_DB...");

                if (PreferenceManager.getDefaultSharedPreferences(mMainActivity).getBoolean(Values.SharedPrefsTag.USE_NET_WORK_ALBUM, false)) {
                    List<CustomAlbumPath> customs = LitePal.where("mAlbumId = ?", String.valueOf(albumId)).find(CustomAlbumPath.class);
                    if (customs.size() != 0) {

                        final CustomAlbumPath custom = customs.get(0);
                        String path = custom.getAlbumArt();

                        if (TextUtils.isEmpty(path) || path.equals("null") || path.toLowerCase().equals("none")) {
                            Log.d(TAG, "onBindViewHolder: (in CUSTOM_DB) has val but the val is not ability, will remove it..., download...");

                            String mayPath = ifExists(albumId);
                            if (mayPath != null) {

                                Log.d(TAG, "onBindViewHolder: (in CUSTOM_DB) DB not ability, path is ability, save in db and loading...");

                                custom.setAlbumArt(mayPath);
                                custom.save();

                                if (verify(viewHolder.mAlbumImage, viewHolder.getAdapterPosition())) {
                                    viewHolder.mAlbumImage.post(() -> GlideApp.with(mMainActivity)
                                            .load(mayPath)
                                            .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .into(viewHolder.mAlbumImage));
                                }
                            } else {
                                //download
                                HttpUtil httpUtil = new HttpUtil();
                                String request = "http://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=" +
                                        MyApplication.LAST_FM_KEY +
                                        "&artist=" +
                                        artist +
                                        "&album=" +
                                        albumName;

                                httpUtil.sedOkHttpRequest(request, new Callback() {
                                    @Override
                                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                        viewHolder.mAlbumImage.post(() -> Toast.makeText(mMainActivity, e.getMessage(), Toast.LENGTH_SHORT).show());
                                        if (verify(viewHolder.mAlbumImage, viewHolder.getAdapterPosition()))
                                            viewHolder.mAlbumImage.post(() -> GlideApp.with(mMainActivity)
                                                    .load(R.drawable.default_album_art)
                                                    .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                    .into(viewHolder.mAlbumImage));
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
                                                Log.d(TAG, "onResponse: imgUrl: " + img + " albumName is: " + albumName);
                                                if (img.toString().contains("http") && img.toString().contains("https")) {
                                                    Log.d(TAG, "onResponse: ok, now downloading..." + " albumName is: " + albumName);
//                                                List<CustomAlbumPath> list = LitePal.where("mAlbumId = ?", String.valueOf(albumId)).find(CustomAlbumPath.class);
                                                    DownloadUtil.get().download(img.toString(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + "AlbumCovers"
                                                            , albumId + "." + img.substring(img.lastIndexOf(".") + 1), new DownloadUtil.OnDownloadListener() {
                                                                @Override
                                                                public void onDownloadSuccess(File file) {
                                                                    String newPath = file.getAbsolutePath();
                                                                    Log.d(TAG, "onDownloadSuccess: " + newPath + " albumName is: " + albumName);


                                                                    CustomAlbumPath c = customs.get(0);
                                                                    c.setAlbumArt(newPath);
                                                                    c.save();

                                                                    if (verify(viewHolder.mAlbumImage, i)) {
                                                                        Log.d(TAG, "onDownloadSuccess: loading,,," + " albumName is: " + albumName);
                                                                        viewHolder.mAlbumImage.post(() -> GlideApp.with(mMainActivity)
                                                                                .load(file)
                                                                                .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                                                                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                                                .into(viewHolder.mAlbumImage));
//                                                                        viewHolder.mAlbumImage.setTag(R.string.key_id_3, null);
                                                                    }
                                                                }

                                                                @Override
                                                                public void onDownloading(int progress) {
                                                                    Log.d(TAG, "onDownloading: " + albumName + " progress: " + progress);
                                                                }

                                                                @Override
                                                                public void onDownloadFailed(Exception e) {
                                                                    Log.d(TAG, "onDownloadFailed: " + img.toString() + " " + e.getMessage());
                                                                    if (verify(viewHolder.mAlbumImage, viewHolder.getAdapterPosition()))
                                                                        viewHolder.mAlbumImage.post(() -> GlideApp.with(mMainActivity)
                                                                                .load(R.drawable.default_album_art)
                                                                                .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                                                                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                                                .into(viewHolder.mAlbumImage));
                                                                }
                                                            });
                                                } else {
                                                    Log.d(TAG, "onResponse: img url error" + img.toString() + " albumName is: " + albumName);
                                                    if (verify(viewHolder.mAlbumImage, viewHolder.getAdapterPosition()))
                                                        viewHolder.mAlbumImage.post(() -> GlideApp.with(mMainActivity)
                                                                .load(R.drawable.default_album_art)
                                                                .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                                                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                                .into(viewHolder.mAlbumImage));
                                                }
                                            } else {
                                                Log.d(TAG, "onResponse: result not ok, load DEF_ALBUM");
                                                if (verify(viewHolder.mAlbumImage, viewHolder.getAdapterPosition()))
                                                    viewHolder.mAlbumImage.post(() -> GlideApp.with(mMainActivity)
                                                            .load(R.drawable.default_album_art)
                                                            .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                            .into(viewHolder.mAlbumImage));
                                            }
                                        } else {
                                            mMainActivity.runOnUiThread(() -> Toast.makeText(mMainActivity, "response is NUll! " + " albumName is: " + albumName, Toast.LENGTH_SHORT).show());
                                            if (verify(viewHolder.mAlbumImage, viewHolder.getAdapterPosition()))
                                                viewHolder.mAlbumImage.post(() -> GlideApp.with(mMainActivity)
                                                        .load(R.drawable.default_album_art)
                                                        .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                        .into(viewHolder.mAlbumImage));
                                        }
                                    }
                                });
                            }
                        } else {
                            Log.d(TAG, "already in customDB or not forceLoad, loading data from customDB " + " albumName is: " + albumName +
                                    "the path is: " + path);
                            if (verify(viewHolder.mAlbumImage, viewHolder.getAdapterPosition()))
                                viewHolder.mAlbumImage.post(() -> GlideApp.with(mMainActivity)
                                        .load(path)
                                        .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .into(viewHolder.mAlbumImage));
                        }
                    } else {
                        Log.d(TAG, "customDB size is 0, load DEF_ALBUM.png");
                        if (verify(viewHolder.mAlbumImage, viewHolder.getAdapterPosition())) {
                            viewHolder.mAlbumImage.post(() -> GlideApp.with(mMainActivity)
                                    .load(R.drawable.default_album_art)
                                    .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .into(viewHolder.mAlbumImage));
                        }
                    }
                } else {
                    Log.d(TAG, "onBindViewHolder: switch is not open, load DEF_ALBUM.png");
                    if (verify(viewHolder.mAlbumImage, viewHolder.getAdapterPosition())) {
                        Log.d(TAG, "onBindViewHolder: album in DEFAULT_DB has value, load...");
                        viewHolder.mAlbumImage.post(() -> GlideApp.with(mMainActivity)
                                .load(R.drawable.default_album_art)
                                .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .into(viewHolder.mAlbumImage));
                    }
                }
            } else {
                File file = new File(albumPath[0]);
                if (verify(viewHolder.mAlbumImage, viewHolder.getAdapterPosition())) {
                    Log.d(TAG, "onBindViewHolder: album in DEFAULT_DB has value, load...");
                    viewHolder.mAlbumImage.post(() -> GlideApp.with(mMainActivity)
                            .load(file.exists() ? file : R.drawable.default_album_art)
                            .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(viewHolder.mAlbumImage));
                }
            }

            //...mode set
            switch (mType) {
                case GRID_TYPE: {

                    final Bitmap bitmap = Utils.Ui.readBitmapFromFile(albumPath[0], 100, 100);
                    if (bitmap != null) {
                        //color set (album tag)
                        Palette.from(bitmap).generate(p -> {
                            if (p != null) {
                                @ColorInt int color = p.getVibrantColor(ContextCompat.getColor(mMainActivity, R.color.notVeryBlack));
                                if (Utils.Ui.isColorLight(color)) {
                                    viewHolder.mAlbumText.setTextColor(ContextCompat.getColor(mMainActivity, R.color.notVeryBlack));
                                } else {
                                    viewHolder.mAlbumText.setTextColor(ContextCompat.getColor(mMainActivity, R.color.notVeryWhite));
                                }
                                viewHolder.mView.setBackgroundColor(color);

                                bitmap.recycle();
                            } else {
                                viewHolder.mView.setBackgroundColor(ContextCompat.getColor(mMainActivity, R.color.colorPrimary));
                            }
                        });
                    }

                }
                break;
            }
        });
    }

    private String ifExists(int id) {
        String mayPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                + File.separatorChar + "ArtistCovers"
                + File.separatorChar + id + ".";

        if (new File(mayPath + "png").exists())
            return mayPath + "png";

        if (new File(mayPath + "jpg").exists())
            return mayPath + "jpg";

        if (new File(mayPath + "gif").exists())
            return mayPath + "gif";

        return null;
    }

    /**
     * verify if key, null
     */
    private boolean verify(@Nullable ImageView imageView, int index) {
        boolean flag = false;
        if (imageView == null) {
            Log.e(TAG, "imageView null clear_image");
        } else {
            if (imageView.getTag(R.string.key_id_3) == null) {
                Log.e(TAG, "key null clear_image");
                GlideApp.with(imageView).clear(imageView);
            } else {
                //根据position判断是否为复用ViewHolder
                if (((int) imageView.getTag(R.string.key_id_3)) != index) {
                    Log.e(TAG, "key error clear_image");
                    GlideApp.with(imageView).clear(imageView);
                } else {
                    flag = true;
                }
            }
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

        ImageView mAlbumImage;

        View mView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mAlbumText = itemView.findViewById(R.id.recycler_item_song_album_name);
            mAlbumImage = itemView.findViewById(R.id.recycler_item_album_image);
            mUView = itemView.findViewById(R.id.u_view);
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
