/*
 * ************************************************************
 * 文件：MyRecyclerAdapter2AlbumList.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月28日 16:12:44
 * 上次修改时间：2018年11月28日 16:12:26
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.Activities.AlbumDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.IStyle;
import top.geek_studio.chenlongcould.musicplayer.Models.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class MyRecyclerAdapter2AlbumList extends RecyclerView.Adapter<MyRecyclerAdapter2AlbumList.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter, IStyle {

    public static final int LINEAR_TYPE = 0;

    public static final int GRID_TYPE = 1;

    private static int mType = LINEAR_TYPE;

    private List<AlbumItem> mAlbumNameList;

    private Context mContext;

    private ViewHolder mCurrentBind;

    public MyRecyclerAdapter2AlbumList(Context context, List<AlbumItem> albumNameList, int type) {
        this.mAlbumNameList = albumNameList;
        mContext = context;
        mType = type;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = null;
        switch (mType) {
            case LINEAR_TYPE: {
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_music_album_list_item, viewGroup, false);
            }
            break;
            case GRID_TYPE: {
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item_album_grid, viewGroup, false);
            }
        }
        assert view != null;
        ViewHolder holder = new ViewHolder(view);

        view.setOnClickListener(v -> {
            String keyWords = mAlbumNameList.get(holder.getAdapterPosition()).getAlbumName();

            MainActivity mainActivity = (MainActivity) Data.sActivities.get(0);
            ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(mainActivity, holder.mAlbumImage, mContext.getString(R.string.image_trans_album));
            Intent intent = new Intent(mainActivity, AlbumDetailActivity.class);
            intent.putExtra("key", keyWords);
            intent.putExtra("_id", mAlbumNameList.get(holder.getAdapterPosition()).getAlbumId());
            mContext.startActivity(intent, compat.toBundle());
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        mCurrentBind = viewHolder;
        Values.CurrentData.CURRENT_BIND_INDEX_ALBUM_LIST = viewHolder.getAdapterPosition();

        initStyle();

        viewHolder.mAlbumText.setText(mAlbumNameList.get(i).getAlbumName());
        viewHolder.mAlbumImage.setTag(R.string.key_id_1, i);
        viewHolder.mAlbumImage.setTag(R.string.key_id_1, i);
        new MyTask(viewHolder, mContext, i + 1).execute();
    }

    @Override
    public int getItemCount() {
        return mAlbumNameList.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.mAlbumImage.setTag(null);
        GlideApp.with(mContext).clear(holder.mAlbumImage);
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

    static class MyTask extends AsyncTask<Void, Void, String> {

        private static final String TAG = "MyTask";

        private final WeakReference<Context> mContextWeakReference;

        private final WeakReference<ViewHolder> mViewHolderWeakReference;

        private final int mPosition;

        MyTask(ViewHolder holder, Context context, int position) {
            mContextWeakReference = new WeakReference<>(context);
            mViewHolderWeakReference = new WeakReference<>(holder);
            mPosition = position;
        }

        @Override
        protected void onPostExecute(String albumArt) {
            if (albumArt == null || mViewHolderWeakReference.get() == null) {
                return;
            }
            mViewHolderWeakReference.get().mAlbumImage.setTag(null);
            File file = new File(albumArt);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(albumArt);

                //...mode set
                switch (mType) {
                    case GRID_TYPE: {
                        Palette.from(bitmap).generate(p -> {
                            if (p != null) {
                                @ColorInt int color = p.getVibrantColor(Color.parseColor(Values.Color.NOT_VERY_BLACK));
                                if (Utils.Ui.isColorLight(color)) {
                                    mViewHolderWeakReference.get().mAlbumText.setTextColor(Color.parseColor(Values.Color.NOT_VERY_BLACK));
                                } else {
                                    mViewHolderWeakReference.get().mAlbumText.setTextColor(Color.parseColor(Values.Color.NOT_VERY_WHITE));
                                }
                                mViewHolderWeakReference.get().mView.setBackgroundColor(color);
                            }
                        });
                    }
                    break;
                }

                Log.d(TAG, "onPostExecute: file exits");
                GlideApp.with(mContextWeakReference.get())
                        .load(bitmap)
                        .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                        .centerCrop()
                        .into(mViewHolderWeakReference.get().mAlbumImage);
            } else {
                GlideApp.with(mContextWeakReference.get())
                        .load(R.drawable.ic_audiotrack_24px)
                        .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                        .into(mViewHolderWeakReference.get().mAlbumImage);
                Log.d(TAG, "onPostExecute: file not exits");
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            //根据position判断是否为复用ViewHolder

            ImageView imageView = mViewHolderWeakReference.get().mAlbumImage;

            if (imageView == null) {
                return null;
            }

            if (imageView.getTag(R.string.key_id_1) == null) {
                return null;
            }
            if (((int) imageView.getTag(R.string.key_id_1)) != mPosition - 1) {
                GlideApp.with(mContextWeakReference.get()).clear(imageView);
                return null;
            }

            String img = "null";
            Cursor cursor = mContextWeakReference.get().getContentResolver().query(Uri.parse(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI + String.valueOf(File.separatorChar) + mPosition),
                    new String[]{MediaStore.Audio.Albums.ALBUM_ART}, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                if (cursor.getCount() == 0) {
                    return "null";
                }
                img = cursor.getString(0);
                cursor.close();
            }
            return img;
        }

//        public String getAlbumArt(int album_id) {
//            String mUriAlbums = "content://media/external/audio/albums";
//            String[] projection = new String[]{"album_art"};
//            Cursor cur = mContextWeakReference.get().getContentResolver().query(Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)), projection, null, null, null);
//            String album_art = null;
//            if (cur != null) {
//                if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
//                    cur.moveToNext();
//                    album_art = cur.getString(0);
//                }
//                cur.close();
//            }
//
//            return album_art;
//        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView mAlbumText;

        ImageView mAlbumImage;

        View mView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mAlbumText = itemView.findViewById(R.id.recycler_item_song_album_name);
            mAlbumImage = itemView.findViewById(R.id.recycler_item_album_image);

            switch (mType) {
                case GRID_TYPE: {
                    mView = itemView.findViewById(R.id.mask);
                }
                break;
            }
        }
    }
}
