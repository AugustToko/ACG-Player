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
import top.geek_studio.chenlongcould.musicplayer.App;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.Models.ArtistItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.ArtistDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.database.ArtistArtPath;
import top.geek_studio.chenlongcould.musicplayer.thread_pool.AlbumThreadPool;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

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
        holder.mView.setBackgroundColor(ContextCompat.getColor(mMainActivity, R.color.notVeryBlack));
        holder.mArtistText.setTextColor(ContextCompat.getColor(mMainActivity, R.color.notVeryWhite));
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
        viewHolder.mArtistText.setText(mArtistItems.get(viewHolder.getAdapterPosition()).getArtistName());
        viewHolder.mArtistImage.setTag(R.string.key_id_2, viewHolder.getAdapterPosition());

        dataSet(viewHolder.getAdapterPosition(), viewHolder.mArtistImage, viewHolder.mArtistText, viewHolder.mView);
    }

    private void dataSet(int index, ImageView imageView, TextView textView, View view) {
        AlbumThreadPool.post(() -> {

            ArtistItem artistItem = mArtistItems.get(index);
            int artistId = artistItem.getArtistId();
            String artistName = artistItem.getArtistName();

            List<ArtistArtPath> customs = LitePal.where("mArtistId = ?", String.valueOf(artistId)).find(ArtistArtPath.class);
            Log.d(TAG, "onBindViewHolder: id: " + artistId + " name: " + artistName);
            if (customs.size() != 0) {
                final ArtistArtPath custom = customs.get(0);
                String path = custom.getArtistArt();
                if (TextUtils.isEmpty(path) || path.equals("null") || path.toLowerCase().equals("none")) {

                    String mayPath = ifExists(artistId);
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
                        }

                    } else {
                        Log.d(TAG, "onBindViewHolder: (in CUSTOM_DB) DB not ability, path not ability, download...");
                        //download
                        HttpUtil httpUtil = new HttpUtil();
                        String request = "http://ws.audioscrobbler.com/2.0/?method=artist.getinfo&artist="
                                + artistName
                                + "&api_key="
                                + App.LAST_FM_KEY;

                        httpUtil.sedOkHttpRequest(request, new Callback() {
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

                                        Log.d(TAG, "onResponse: imgUrl: " + img + " albumName is: " + artistName);

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
                                                                setUpTagColor(newPath, textView, view);
                                                                imageView.post(() -> GlideApp.with(mMainActivity)
                                                                        .load(file)
                                                                        .placeholder(R.drawable.default_album_art)
                                                                        .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                                                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                                        .into(imageView));
//                                                                viewHolder.mArtistImage.setTag(R.string.key_id_2, null);
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
                        loadDEF(imageView, view, textView, index);
                    }
                }
            } else {
                Log.d(TAG, "customDB size is 0, load DEF_ALBUM.png");
                loadDEF(imageView, view, textView, index);
            }
        });
    }

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
            if (imageView.getTag(R.string.key_id_2) == null) {
                Log.e(TAG, "key null clear_image");
            } else {
                //根据position判断是否为复用ViewHolder
                if (((int) imageView.getTag(R.string.key_id_2)) != index) {
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

//    static class MyTask extends AsyncTask<Void, Void, String> {
//
//        private static final String TAG = "MyTask";
//
//        private final WeakReference<MainActivity> mContextWeakReference;
//
//        private final WeakReference<ViewHolder> mViewHolderWeakReference;
//
//        private final int mPosition;
//
//        MyTask(ViewHolder holder, MainActivity context, int position) {
//            mContextWeakReference = new WeakReference<>(context);
//            mViewHolderWeakReference = new WeakReference<>(holder);
//            mPosition = position;
//        }
//
//        @Override
//        protected void onPostExecute(String albumArt) {
//            if (mViewHolderWeakReference.get() == null) {
//                return;
//            }
//
//            if (albumArt == null) {
//                GlideApp.with(mContextWeakReference.get())
//                        .load(R.drawable.ic_audiotrack_24px)
//                        .into(mViewHolderWeakReference.get().mArtistImage);
//                return;
//            }
//
//            final File file = new File(albumArt);
//            if (file.exists()) {
//                final Bitmap bitmap = Utils.Ui.readBitmapFromFile(albumArt, 100, 100);
//
//                //...mode set
//                switch (mType) {
//                    case GRID_TYPE: {
//
//                        //color set (album tag)
//                        Palette.from(bitmap).generate(p -> {
//                            if (p != null) {
//                                @ColorInt int color = p.getVibrantColor(ContextCompat.getColor(mContextWeakReference.get(), R.color.notVeryBlack));
//                                if (Utils.Ui.isColorLight(color)) {
//                                    mViewHolderWeakReference.get().mArtistText.setTextColor(ContextCompat.getColor(mContextWeakReference.get(), R.color.notVeryBlack));
//                                } else {
//                                    mViewHolderWeakReference.get().mArtistText.setTextColor(ContextCompat.getColor(mContextWeakReference.get(), R.color.notVeryWhite));
//                                }
//                                mViewHolderWeakReference.get().mView.setBackgroundColor(color);
//
//                                bitmap.recycle();
//                            } else {
//                                mViewHolderWeakReference.get().mView.setBackgroundColor(ContextCompat.getColor(mContextWeakReference.get(), R.color.notVeryBlack));
//                            }
//                        });
//                    }
//                    break;
//                }
//
//                GlideApp.with(mContextWeakReference.get())
//                        .load(albumArt)
//                        .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
//                        .centerCrop()
//                        .into(mViewHolderWeakReference.get().mArtistImage);
//            } else {
//                GlideApp.with(mContextWeakReference.get())
//                        .load(R.drawable.ic_audiotrack_24px)
//                        .into(mViewHolderWeakReference.get().mArtistImage);
//                Log.e(TAG, "onPostExecute: file not exits");
//            }
//
//            mViewHolderWeakReference.get().mArtistImage.setTag(R.string.key_id_1, null);
//
//            cancel(true);
//        }
//
//        @Override
//        protected String doInBackground(Void... voids) {
//
//            //根据position判断是否为复用ViewHolder
//            if (mViewHolderWeakReference.get() == null) {
//                return "null";
//            }
//
//            final ImageView imageView = mViewHolderWeakReference.get().mArtistImage;
//
//            if (imageView == null || imageView.getTag(R.string.key_id_1) == null) {
//                Log.e(TAG, "doInBackground: key null------------------skip");
//                return null;
//            }
//
//            if (((int) imageView.getTag(R.string.key_id_1)) != mPosition - 1) {
//                GlideApp.with(mContextWeakReference.get()).clearData(imageView);
//                Log.e(TAG, "doInBackground: key error------------------skip");
//                return null;
//            }
//
//            String img = "null";
//            Cursor cursor = mContextWeakReference.get().getContentResolver().query(Uri.parse(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI + String.valueOf(File.separatorChar) + mPosition),
//                    new String[]{MediaStore.Audio.Albums.ALBUM_ART}, null, null, null);
//            if (cursor != null) {
//                cursor.moveToFirst();
//                if (cursor.getCount() == 0) {
//                    return "null";
//                }
//                img = cursor.getString(0);
//                cursor.close();
//            }
//            return img;
//        }
//    }

    class ViewHolder extends RecyclerView.ViewHolder {

        View mUView;

        TextView mArtistText;

        ImageView mArtistImage;

        View mView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mArtistText = itemView.findViewById(R.id.recycler_item_song_album_name);
            mArtistImage = itemView.findViewById(R.id.recycler_item_album_image);
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
