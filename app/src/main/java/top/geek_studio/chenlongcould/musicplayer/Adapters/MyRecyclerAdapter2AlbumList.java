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

package top.geek_studio.chenlongcould.musicplayer.Adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import top.geek_studio.chenlongcould.geeklibrary.Theme.IStyle;
import top.geek_studio.chenlongcould.musicplayer.Activities.AlbumDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.AlbumThreadPool;
import top.geek_studio.chenlongcould.musicplayer.Database.CustomAlbumPath;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.Models.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.MyApplication;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class MyRecyclerAdapter2AlbumList extends RecyclerView.Adapter<MyRecyclerAdapter2AlbumList.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter, IStyle {

    private static final String TAG = "AlbumAdapter";

    public static final int LINEAR_TYPE = 0;

    public static final int GRID_TYPE = 1;

    private static int mType = LINEAR_TYPE;

    private List<AlbumItem> mAlbumNameList;

    private MainActivity mMainActivity;

    private ViewHolder mCurrentBind;

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

        view.setOnClickListener(v -> {
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
    public void onViewRecycled(@NonNull ViewHolder holder) {
        GlideApp.with(mMainActivity).clear(holder.mAlbumImage);
        holder.mAlbumImage.setTag(null);
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        //verify AND CLEAR!
        if (viewHolder.mAlbumImage == null) {
            Log.e(TAG, "imageView null clear_image");
            GlideApp.with(mMainActivity).clear(viewHolder.mAlbumImage);
        } else {
            if (viewHolder.mAlbumImage.getTag(R.string.key_id_3) == null) {
                Log.e(TAG, "key null clear_image");
                GlideApp.with(mMainActivity).clear(viewHolder.mAlbumImage);
            } else {
                //根据position判断是否为复用ViewHolder
                if (((int) viewHolder.mAlbumImage.getTag(R.string.key_id_3)) != viewHolder.getAdapterPosition()) {
                    Log.e(TAG, "key error clear_image");
                    GlideApp.with(mMainActivity).clear(viewHolder.mAlbumImage);
                }
            }
        }

        mCurrentBind = viewHolder;
        Values.CurrentData.CURRENT_BIND_INDEX_ALBUM_LIST = viewHolder.getAdapterPosition();

        initStyle();

        viewHolder.mAlbumText.setText(mAlbumNameList.get(viewHolder.getAdapterPosition()).getAlbumName());
        viewHolder.mAlbumImage.setTag(R.string.key_id_3, viewHolder.getAdapterPosition());      //set TAG

        try {
            AlbumThreadPool.post(() -> {

                final String[] albumPath = {null};

                AlbumItem albumItem = mAlbumNameList.get(viewHolder.getAdapterPosition());
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

                if (PreferenceManager.getDefaultSharedPreferences(mMainActivity).getBoolean(Values.SharedPrefsTag.USE_NET_WORK_ALBUM, false)) {
                    List<CustomAlbumPath> customs = LitePal.where("mAlbumId = ?", String.valueOf(albumId)).find(CustomAlbumPath.class);
                    if (customs.size() != 0) {
                        final CustomAlbumPath custom = customs.get(0);
//                    try {
                        if (TextUtils.isEmpty(albumPath[0]) || custom.isForceUse()) {
                            Log.d(TAG, "def: " + albumPath[0]);
                            File file = new File(custom.getAlbumArt());
                            if (custom.getAlbumArt().equals("null") && !file.exists()) {
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
                                        mMainActivity.runOnUiThread(() -> Toast.makeText(mMainActivity, e.getMessage(), Toast.LENGTH_SHORT).show());
                                    }

                                    @Override
                                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                        if (response.body() != null) {
                                            String body = response.body().string();
                                            Log.d(TAG, "onResponse: " + body + " albumName is: " + albumName);
                                            final Document document = Jsoup.parse(body, "UTF-8", new Parser(new XmlTreeBuilder()));
                                            Elements content = document.getElementsByAttribute("status");
                                            String status = content.select("lfm[status]").attr("status");

                                            if (status.equals("ok")) {
                                                StringBuilder img = new StringBuilder(content.select("image[size=extralarge]").text());
                                                Log.d(TAG, "onResponse: imgUrl: " + img + " albumName is: " + albumName);
                                                if (img.toString().contains("http") && img.toString().contains("https")) {
                                                    Log.d(TAG, "onResponse: ok, now downloading..." + " albumName is: " + albumName);
                                                    List<CustomAlbumPath> list = LitePal.where("mAlbumId = ?", String.valueOf(albumId)).find(CustomAlbumPath.class);
                                                    DownloadUtil.get().download(img.toString(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + "AlbumCovers"
                                                            , albumId + "." + img.substring(img.lastIndexOf(".") + 1), new DownloadUtil.OnDownloadListener() {
                                                                @Override
                                                                public void onDownloadSuccess(File file) {
                                                                    Log.d(TAG, "onDownloadSuccess: " + file.getAbsolutePath() + " albumName is: " + albumName);

                                                                    albumPath[0] = file.getAbsolutePath();

                                                                    CustomAlbumPath c = list.get(0);
                                                                    c.setAlbumArt(file.getAbsolutePath());
                                                                    c.save();

                                                                    if (verify(viewHolder.mAlbumImage, viewHolder.getAdapterPosition())) {
                                                                        Log.d(TAG, "onDownloadSuccess: loading,,," + " albumName is: " + albumName);
                                                                        viewHolder.mAlbumImage.post(() -> GlideApp.with(mMainActivity)
                                                                                .load(file)
                                                                                .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                                                                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                                                .into(viewHolder.mAlbumImage));
                                                                    }
                                                                }

                                                                @Override
                                                                public void onDownloading(int progress) {

                                                                }

                                                                @Override
                                                                public void onDownloadFailed(Exception e) {
                                                                    Log.d(TAG, "onDownloadFailed: " + img.toString() + " " + e.getMessage());
                                                                }
                                                            });
                                                } else {
                                                    Log.d(TAG, "onResponse: img url error" + img.toString() + " albumName is: " + albumName);
                                                }
                                            } else {
                                                Log.d(TAG, "onResponse: " + content.select("lfm[status]").attr("status")
                                                        + "_" + content.select("lfm[status]").select("error[code]").attr("code")
                                                        + " : " + content.select("lfm[status]").text() + " albumName is: " + albumName);

                                                if (verify(viewHolder.mAlbumImage, viewHolder.getAdapterPosition())) {
                                                    viewHolder.mAlbumImage.post(() -> GlideApp.with(mMainActivity)
                                                            .load(custom.getAlbumArt())
                                                            .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                            .into(viewHolder.mAlbumImage));
                                                }
                                            }
                                        } else {
                                            mMainActivity.runOnUiThread(() -> Toast.makeText(mMainActivity, "response is NUll! " + " albumName is: " + albumName, Toast.LENGTH_SHORT).show());

                                            if (verify(viewHolder.mAlbumImage, viewHolder.getAdapterPosition())) {
                                                viewHolder.mAlbumImage.post(() -> GlideApp.with(mMainActivity)
                                                        .load(custom.getAlbumArt())
                                                        .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                        .into(viewHolder.mAlbumImage));
                                            }
                                        }
                                    }
                                });
                            } else {
                                Log.d(TAG, "already in customDB or not forceLoad, loading data from customDB " + " albumName is: " + albumName);
                                if (verify(viewHolder.mAlbumImage, viewHolder.getAdapterPosition())) {
                                    viewHolder.mAlbumImage.post(() -> GlideApp.with(mMainActivity)
                                            .load(custom.getAlbumArt())
                                            .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .into(viewHolder.mAlbumImage));
                                }

                            }
                        } else {
                            Log.d(TAG, "albumLoader: File exists or force not open, loading default..." + " albumName is: " + albumName);
                            loadCoverDefault(mMainActivity, viewHolder.mAlbumImage, albumPath[0], viewHolder.getAdapterPosition());
                        }
//                    } catch (Exception e) {
//                        Log.d(TAG, "albumLoader: load customAlbum Error, loading default..., msg: " + e.getMessage());
//                        loadCoverDefault(mMainActivity, viewHolder.mAlbumImage, albumPath[0], viewHolder.getAdapterPosition());
//                    }
                    } else {
                        Log.d(TAG, "customDB size is 0");
                        loadCoverDefault(mMainActivity, viewHolder.mAlbumImage, albumPath[0], viewHolder.getAdapterPosition());
                    }
                } else {
                    Log.d(TAG, "albumLoader: load default...");
                    loadCoverDefault(mMainActivity, viewHolder.mAlbumImage, albumPath[0], viewHolder.getAdapterPosition());
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
        } catch (Exception e) {
            e.printStackTrace();
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
            if (imageView.getTag(R.string.key_id_3) == null) {
                Log.e(TAG, "key null clear_image");
            } else {
                //根据position判断是否为复用ViewHolder
                if (((int) imageView.getTag(R.string.key_id_3)) != index) {
                    Log.e(TAG, "key error clear_image");
                } else {
                    flag = true;
                }
            }
        }
        return flag;
    }

    /**
     * load defaultAlbumImage by DB(from {@link MediaStore.Audio.Albums#ALBUM_ART})
     */
    public void loadCoverDefault(Context context, ImageView imageView, String albumPath, int index) {
        if (verify(imageView, index)) {
            //if verify(,) is true, the imageView must not null

            if (TextUtils.isEmpty(albumPath) || albumPath.equals("null") || albumPath.equals("none")) {
                imageView.post(() -> GlideApp.with(context)
                        .load(R.drawable.default_album_art)
                        .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(imageView));
            } else {
                imageView.post(() -> GlideApp.with(context)
                        .load(albumPath)
                        .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(imageView));
            }


        }
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

    @Override
    public void initStyle() {
        switch (mType) {
            case LINEAR_TYPE: {
                mCurrentBind.mAlbumText.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));
            }
            break;
            case GRID_TYPE: {
                mCurrentBind.mAlbumText.setTextColor(Color.WHITE);
            }
            break;
        }
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
//                GlideApp.with(mContextWeakReference.get()).clear(imageView);
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

        TextView mAlbumText;

        ImageView mAlbumImage;

        View mView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mAlbumText = itemView.findViewById(R.id.recycler_item_song_album_name);
            mAlbumImage = itemView.findViewById(R.id.recycler_item_album_image);
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
