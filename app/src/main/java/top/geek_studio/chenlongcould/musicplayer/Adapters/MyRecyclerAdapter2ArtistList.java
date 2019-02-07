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

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.List;

import top.geek_studio.chenlongcould.geeklibrary.Theme.IStyle;
import top.geek_studio.chenlongcould.musicplayer.Activities.AlbumDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.AlbumThreadPool;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.Models.ArtistItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class MyRecyclerAdapter2ArtistList extends RecyclerView.Adapter<MyRecyclerAdapter2ArtistList.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter, IStyle {

    public static final int LINEAR_TYPE = 0;
    public static final int GRID_TYPE = 1;
    private static final String TAG = "ArtistAdapter";
    private static int mType = LINEAR_TYPE;

    private List<ArtistItem> mArtistItems;

    private MainActivity mMainActivity;

    private ViewHolder mCurrentBind;

    public MyRecyclerAdapter2ArtistList(MainActivity activity, List<ArtistItem> artistItems, int type) {
        this.mArtistItems = artistItems;
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
            String keyWords = mArtistItems.get(holder.getAdapterPosition()).getArtistName();

            final ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(mMainActivity, holder.mArtistImage, mMainActivity.getString(R.string.image_trans_album));
            Intent intent = new Intent(mMainActivity, AlbumDetailActivity.class);
            intent.putExtra("key", keyWords);
            intent.putExtra("_id", mArtistItems.get(holder.getAdapterPosition()).getArtistId());
            mMainActivity.startActivity(intent, compat.toBundle());
        });

        return holder;
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        holder.mArtistImage.setTag(R.string.key_id_1, null);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        GlideApp.with(mMainActivity).clear(holder.mArtistImage);
        holder.mArtistImage.setTag(R.string.key_id_1, null);
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        Object tag = viewHolder.mArtistImage.getTag(R.string.key_id_1);
        if (tag != null && (int) tag != i) {
            Log.d(TAG, "onBindViewHolder: this is recycled-holder, clear image");
            GlideApp.with(mMainActivity).clear(viewHolder.mArtistImage);
        }

        mCurrentBind = viewHolder;
        Values.CurrentData.CURRENT_BIND_INDEX_ALBUM_LIST = viewHolder.getAdapterPosition();

        initStyle();

        viewHolder.mArtistText.setText(mArtistItems.get(i).getArtistName());
        viewHolder.mArtistImage.setTag(R.string.key_id_1, i);

        AlbumThreadPool.post(() -> {
            //根据position判断是否为复用ViewHolder

            if (viewHolder.mArtistImage == null || viewHolder.mArtistImage.getTag(R.string.key_id_1) == null) {
                GlideApp.with(mMainActivity).clear(viewHolder.mArtistImage);
                Log.e(TAG, "doInBackground: key null------------------skip");
                return;
            }

            if (((int) viewHolder.mArtistImage.getTag(R.string.key_id_1)) != i) {
                GlideApp.with(mMainActivity).clear(viewHolder.mArtistImage);
                Log.e(TAG, "doInBackground: key error------------------skip");
                return;
            }

            final Cursor cursor = mMainActivity.getContentResolver().query(Uri.parse(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI + String.valueOf(File.separatorChar) + String.valueOf(i)),
                    new String[]{MediaStore.Audio.Albums.ALBUM_ART}, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                if (cursor.getCount() == 0) {
                    return;
                }

                String path = cursor.getString(0);

                //...mode set
                switch (mType) {
                    case GRID_TYPE: {

                        final Bitmap bitmap = Utils.Ui.readBitmapFromFile(path, 100, 100);
                        if (bitmap != null) {
                            //color set (album tag)
                            Palette.from(bitmap).generate(p -> {
                                if (p != null) {
                                    @ColorInt int color = p.getVibrantColor(ContextCompat.getColor(mMainActivity, R.color.notVeryBlack));
                                    if (Utils.Ui.isColorLight(color)) {
                                        viewHolder.mArtistText.setTextColor(ContextCompat.getColor(mMainActivity, R.color.notVeryBlack));
                                    } else {
                                        viewHolder.mArtistText.setTextColor(ContextCompat.getColor(mMainActivity, R.color.notVeryWhite));
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

                viewHolder.mArtistImage.post(() -> {
                    GlideApp.with(mMainActivity)
                            .load(path)
                            .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(viewHolder.mArtistImage);
                    viewHolder.mArtistImage.setTag(R.string.key_id_1, null);
                });
            }

        });
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

    @Override
    public void initStyle() {
        switch (mType) {
            case LINEAR_TYPE: {
                mCurrentBind.mArtistText.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));
            }
            break;
            case GRID_TYPE: {
                mCurrentBind.mArtistText.setTextColor(Color.WHITE);
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

        TextView mArtistText;

        ImageView mArtistImage;

        View mView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mArtistText = itemView.findViewById(R.id.recycler_item_song_album_name);
            mArtistImage = itemView.findViewById(R.id.recycler_item_album_image);
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
