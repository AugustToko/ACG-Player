/*
 * ************************************************************
 * 文件：MyRecyclerAdapter2AlbumList.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月18日 21:28:39
 * 上次修改时间：2018年11月18日 21:28:14
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
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.Activities.ListActivity;
import top.geek_studio.chenlongcould.musicplayer.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class MyRecyclerAdapter2AlbumList extends RecyclerView.Adapter<MyRecyclerAdapter2AlbumList.ViewHolder> {

    private List<AlbumItem> mAlbumNameList;

    private Context mContext;

    public MyRecyclerAdapter2AlbumList(Context context, List<AlbumItem> albumNameList) {
        this.mAlbumNameList = albumNameList;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_music_album_list_item, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);

        view.setOnClickListener(v -> {
            // TODO: 2018/11/5 根据点击的项目查找符合要求的歌曲
            String keyWords = mAlbumNameList.get(holder.getAdapterPosition()).getAlbumName();
            Intent intent = new Intent(mContext, ListActivity.class);
            intent.putExtra("key", keyWords);
            intent.putExtra("_id", mAlbumNameList.get(holder.getAdapterPosition()).getAlbumId());
            mContext.startActivity(intent);
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.mAlbumText.setText(mAlbumNameList.get(i).getAlbumName());
        viewHolder.mAlbumImage.setTag(R.string.key_id_1, i);
        new MyTask(viewHolder.mAlbumImage, mContext, i + 1).execute();
        Values.CurrentData.CURRENT_BIND_INDEX_ALBUM_LIST = viewHolder.getAdapterPosition();
    }

    @Override
    public int getItemCount() {
        return mAlbumNameList.size();
    }

    private String getAlbumArt(int album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cur = mContext.getContentResolver().query(Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)), projection, null, null, null);
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        cur = null;
        return album_art;
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        GlideApp.with(mContext).clear(holder.mAlbumImage);
    }

    static class MyTask extends AsyncTask<Void, Void, String> {

        private static final String TAG = "MyTask";
        private final WeakReference<ImageView> mImageViewWeakReference;

        private final WeakReference<Context> mContextWeakReference;

        private final int mPosition;

        MyTask(ImageView imageView, Context context, int position) {
            mImageViewWeakReference = new WeakReference<>(imageView);
            mContextWeakReference = new WeakReference<>(context);
            mPosition = position;
        }

        @Override
        protected void onPostExecute(String albumArt) {
            if (albumArt == null) {
                return;
            }
            mImageViewWeakReference.get().setTag(null);
            if (albumArt.equals("null")) {
                GlideApp.with(mContextWeakReference.get()).load(R.drawable.ic_audiotrack_24px).into(mImageViewWeakReference.get());
            } else {
                Bitmap bitmap = BitmapFactory.decodeFile(albumArt);
                GlideApp.with(mContextWeakReference.get()).load(bitmap).into(mImageViewWeakReference.get());
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            //根据position判断是否为复用ViewHolder
            if (((int) mImageViewWeakReference.get().getTag(R.string.key_id_1)) != mPosition - 1) {
                GlideApp.with(mContextWeakReference.get()).clear(mImageViewWeakReference.get());
                return null;
            }

            String img = "null";
            Cursor cursor = mContextWeakReference.get().getContentResolver().query(Uri.parse(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI + "/" + mPosition),
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
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView mAlbumText;

        ImageView mAlbumImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mAlbumText = itemView.findViewById(R.id.recycler_item_song_album_name);
            mAlbumImage = itemView.findViewById(R.id.recycler_item_album_image);
        }
    }
}
