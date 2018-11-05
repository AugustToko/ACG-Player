/*
 * ************************************************************
 * 文件：MyRecyclerAdapter.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月06日 07:32:30
 * 上次修改时间：2018年11月06日 07:32:22
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {

    private List<String> mMusicPathList;

    private List<String> mMusicNameList;

    private List<String> mSongAlbumList;

    private MainActivity mMainActivity;

    private NotLeakHandler mHandler;

    MyRecyclerAdapter(List<String> musicPathList, List<String> musicNameList, List<String> songAlbumList, MainActivity activity, Looper looper) {
        mMusicPathList = musicPathList;
        mMainActivity = activity;
        mMusicNameList = musicNameList;
        mSongAlbumList = songAlbumList;

        mHandler = new NotLeakHandler(this, looper);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView mMusicCloverImage;

        ImageView mItemMenuButton;

        TextView mMusicText;

        TextView mMusicAlbumName;

        TextView mMusicExtName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mMusicAlbumName = itemView.findViewById(R.id.recycler_item_music_album_name);
            mMusicCloverImage = itemView.findViewById(R.id.recycler_item_album_image);
            mMusicText = itemView.findViewById(R.id.recycler_item_music_name);
            mItemMenuButton = itemView.findViewById(R.id.recycler_item_menu);
            mMusicExtName = itemView.findViewById(R.id.recycler_item_music_type_name);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_music_list_item, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);

        view.setOnClickListener(v -> new Thread(() -> {
            String clickedPath = mMusicPathList.get(holder.getAdapterPosition());
            String clickedSongName = mMusicNameList.get(holder.getAdapterPosition());
            String clickedSongAlbumName = mSongAlbumList.get(holder.getAdapterPosition());

            if (mMainActivity.getMusicBinder().isPlayingMusic()) {
                if (clickedPath.equals(Values.CURRENT_SONG_PATH)) {
                    mMainActivity.getMusicBinder().pauseMusic();
                    Values.NOW_PLAYING = false;
                    Utils.Ui.setNowNotPlaying(mMainActivity);
                    return;
                }
            }

            mMainActivity.setCurrentSongInfo(
                    clickedSongName
                    , clickedSongAlbumName
                    , mMusicPathList.get(holder.getAdapterPosition())
                    , Utils.Audio.getMp3Cover(clickedPath)
            );

            Values.CURRENT_SONG_PATH = clickedPath;
            Utils.Ui.setNowPlaying(mMainActivity);

            mMainActivity.getMusicBinder().resetMusic();

            try {
                mMainActivity.getMusicBinder().setDataSource(clickedPath);
                mMainActivity.getMusicBinder().prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMainActivity.getMusicBinder().playMusic();

            Values.NOW_PLAYING = true;
        }).start());

        view.setOnLongClickListener(v -> {
            if (mMainActivity.getMusicBinder().isPlayingMusic()) {
                mMainActivity.getMusicBinder().stopMusic();
                Values.NOW_PLAYING = false;
                Utils.Ui.setNowNotPlaying(mMainActivity);
            }
            return true;
        });

        holder.mItemMenuButton.setOnClickListener(v -> {
            // TODO: 2018/11/4 need more method
            Toast.makeText(mMainActivity, "you clicked the menu button!", Toast.LENGTH_SHORT).show();
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        /* show song name, use songNameList */
        Values.CURRENT_BIND_INDEX = viewHolder.getAdapterPosition();

        viewHolder.mMusicText.setText(mMusicNameList.get(i));
        viewHolder.mMusicAlbumName.setText(mSongAlbumList.get(i));
        String prefix = mMusicPathList.get(i).substring(mMusicPathList.get(i).lastIndexOf(".") + 1);
        viewHolder.mMusicExtName.setText(prefix);

        if (prefix.equals("mp3")) {
            viewHolder.mMusicExtName.setBackgroundResource(R.color.mp3TypeColor);
        }

        Log.d("_position", "----------------onBindViewHolder: item of i: " + i);

        /*--- 添加标记以便避免ImageView因为ViewHolder的复用而出现混乱 ---*/
        viewHolder.mMusicCloverImage.setTag(R.string.key_id_1, i);

        MyTask task = new MyTask(viewHolder.mMusicCloverImage, mMainActivity, mMusicPathList.get(i), i);
        task.execute();
    }

    @Override
    public int getItemCount() {
        return mMusicPathList.size();
    }

    /**
     * 为复用的ImageView清除内存
     */
    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        GlideApp.with(mMainActivity).clear(holder.mMusicCloverImage);
        super.onViewRecycled(holder);
    }

    static class MyTask extends AsyncTask<Void, Void, byte[]> {

        String mPath;

        private WeakReference<ImageView> mImageViewWeakReference;

        private WeakReference<Context> mContextWeakReference;

        private int mPosition;

        MyTask(ImageView imageView, Context context, String path, int position) {
            mImageViewWeakReference = new WeakReference<>(imageView);
            mContextWeakReference = new WeakReference<>(context);
            mPath = path;
            mPosition = position;
        }

        @Override
        protected void onPostExecute(byte[] picData) {
            if (picData == null) {
                return;
            }
            mImageViewWeakReference.get().setTag(null);
            GlideApp.with(mContextWeakReference.get()).load(picData)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .override(Values.MAX_HEIGHT_AND_WIDTH, Values.MAX_HEIGHT_AND_WIDTH)
                    .into(mImageViewWeakReference.get());
        }

        @Override
        protected byte[] doInBackground(Void... voids) {

            //根据position判断是否为复用ViewHolder
            if (((int) mImageViewWeakReference.get().getTag(R.string.key_id_1)) != mPosition) {
                GlideApp.with(mContextWeakReference.get()).clear(mImageViewWeakReference.get());
                return null;
            }
            return Utils.Audio.getAlbumByteImage(mPath);
        }
    }

    class NotLeakHandler extends Handler {
        @SuppressWarnings("unused")
        private WeakReference<MyRecyclerAdapter> mWeakReference;

        NotLeakHandler(MyRecyclerAdapter adapter,Looper looper) {
            super(looper);
            mWeakReference = new WeakReference<>(adapter);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                default:
            }
        }

    }
}
