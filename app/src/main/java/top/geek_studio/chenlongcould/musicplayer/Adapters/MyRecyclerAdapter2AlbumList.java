/*
 * ************************************************************
 * 文件：MyRecyclerAdapter2AlbumList.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月23日 16:43:35
 * 上次修改时间：2018年11月23日 15:29:57
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
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.Activities.AlbumDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.Models.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class MyRecyclerAdapter2AlbumList extends RecyclerView.Adapter<MyRecyclerAdapter2AlbumList.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

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
        viewHolder.mAlbumText.setText(mAlbumNameList.get(i).getAlbumName());
        viewHolder.mAlbumImage.setTag(R.string.key_id_1, i);
        new MyTask(viewHolder.mAlbumImage, mContext, i + 1).execute();
        Values.CurrentData.CURRENT_BIND_INDEX_ALBUM_LIST = viewHolder.getAdapterPosition();
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
            File file = new File(albumArt);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(albumArt);
                GlideApp.with(mContextWeakReference.get()).load(bitmap).into(mImageViewWeakReference.get());
            } else
                GlideApp.with(mContextWeakReference.get()).load(R.drawable.ic_audiotrack_24px).into(mImageViewWeakReference.get());
        }

        @Override
        protected String doInBackground(Void... voids) {
            //根据position判断是否为复用ViewHolder
            if (mImageViewWeakReference.get() == null) {
                return null;
            }

            if (mImageViewWeakReference.get().getTag(R.string.key_id_1) == null) {
                return null;
            }

            if (((int) mImageViewWeakReference.get().getTag(R.string.key_id_1)) != mPosition - 1) {
                GlideApp.with(mContextWeakReference.get()).clear(mImageViewWeakReference.get());
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

//
//        public String getAlbumArt(int album_id) {
//            String mUriAlbums = "content://media/external/audio/albums";
//            String[] projection = new String[]{"album_art"};
//            Cursor cur = mContextWeakReference.get().getContentResolver().query(Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)), projection, null, null, null);
//            String album_art = null;
//            if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
//                cur.moveToNext();
//                album_art = cur.getString(0);
//            }
//            cur.close();
//            return album_art;
//        }
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
