/*
 * ************************************************************
 * 文件：MyRecyclerAdapter.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月05日 17:54:16
 * 上次修改时间：2018年11月05日 17:53:45
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

import android.media.MediaPlayer;
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

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {

    private static final String TAG = "MyRecyclerAdapter";

    private List<String> mMusicPathList;

    private List<String> mMusicNameList;

    private List<String> mSongAlbumList;

    private MainActivity mMainActivity;

    private MediaPlayer mMediaPlayer;

    private NotLeakHandler mHandler;

    MyRecyclerAdapter(List<String> musicPathList, List<String> musicNameList, List<String> songAlbumList, MainActivity activity, Looper looper) {
        mMusicPathList = musicPathList;
        mMainActivity = activity;
        mMusicNameList = musicNameList;
        mSongAlbumList = songAlbumList;
        mMediaPlayer = activity.getMediaPlayer();

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

        view.setOnClickListener(v -> {
            try {

                String clickedPath = mMusicPathList.get(holder.getAdapterPosition());
                String clickedSongName = mMusicNameList.get(holder.getAdapterPosition());
                String clickedSongAlbumName = mSongAlbumList.get(holder.getAdapterPosition());

                if (mMediaPlayer.isPlaying() && clickedPath.equals(Values.CURRENT_SONG_PATH)) {
                    mMediaPlayer.pause();
                    return;
                }

                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(clickedPath);
                mMediaPlayer.prepare();

                mMediaPlayer.start();

                mMainActivity.setCurrentSongInfo(clickedSongName, clickedSongAlbumName, Utils.getMp3Cover(clickedPath));

                Values.CURRENT_SONG_PATH = clickedPath;

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        view.setOnLongClickListener(v -> {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
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

        Log.d("look position", "----------------onBindViewHolder: mub of i: " + i);

        MyTask task = new MyTask();
        task.execute(new Data(viewHolder, mMusicPathList.get(i), i));
    }

    @Override
    public int getItemCount() {
        return mMusicPathList.size();
    }

    class NotLeakHandler extends Handler {
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

    class MyTask extends AsyncTask<Data, Void, byte[]>{

        String path;

        ImageView mImageView;

        ViewHolder mViewHolder;

        Data mData;

        String name;

        @Override
        protected void onPostExecute(byte[] picData) {
            if (picData == null) {
                return;
            }
            GlideApp.with(mMainActivity).load(picData).override(50, 50).into(mImageView);
        }

        @Override
        protected byte[] doInBackground(Data... data) {
            mData = data[0];

            // TODO: 2018/11/4 避免重复加载
            path = mData.getPath();
            mImageView = mData.getViewHolder().mMusicCloverImage;
            mViewHolder = mData.getViewHolder();

            return Utils.getByteImage(path);
        }
    }
}
