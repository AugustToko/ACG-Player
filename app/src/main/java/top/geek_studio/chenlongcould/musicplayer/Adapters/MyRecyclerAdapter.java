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

package top.geek_studio.chenlongcould.musicplayer.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private List<String> mMusicPathList;

    private List<String> mMusicNameList;

    private List<String> mSongAlbumList;

    private MainActivity mMainActivity;

    private NotLeakHandler mHandler;

    public MyRecyclerAdapter(List<String> musicPathList, List<String> musicNameList, List<String> songAlbumList, MainActivity activity, Looper looper) {
        mMusicPathList = musicPathList;
        mMainActivity = activity;
        mMusicNameList = musicNameList;
        mSongAlbumList = songAlbumList;

        mHandler = new NotLeakHandler(this, looper);
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return String.valueOf(mMusicNameList.get(position).charAt(0));
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

            if (Data.sMusicBinder.isPlayingMusic()) {
                if (clickedPath.equals(Values.CURRENT_SONG_PATH)) {
                    Data.sMusicBinder.pauseMusic();
                    Utils.Ui.setNowNotPlaying(mMainActivity);
                    return;
                }
            }

            Values.NOW_PLAYING = true;

            //set InfoBar
            mMainActivity.setCurrentSongInfo(
                    clickedSongName
                    , clickedSongAlbumName
                    , mMusicPathList.get(holder.getAdapterPosition())
                    , Utils.Audio.getMp3Cover(clickedPath)
            );

            Values.CURRENT_SONG_PATH = clickedPath;

            Utils.Ui.setNowPlaying(mMainActivity);

            Data.sMusicBinder.pauseMusic();
            Data.sMusicBinder.resetMusic();

            try {
                Data.sMusicBinder.setDataSource(clickedPath);
                Data.sMusicBinder.prepare();
                Data.sMusicBinder.playMusic();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start());

        view.setOnLongClickListener(v -> {
            if (Data.sMusicBinder.isPlayingMusic()) {
                Data.sMusicBinder.stopMusic();
                Values.NOW_PLAYING = false;
                Utils.Ui.setNowNotPlaying(mMainActivity);
            }
            return true;
        });

        holder.mItemMenuButton.setOnClickListener(v -> {
            holder.mPopupMenu.show();
        });

        holder.mPopupMenu.setOnMenuItemClickListener(item -> {

            Values.CURRENT_SELECT_ITEM_INDEX_WITH_ITEM_MENU = holder.getAdapterPosition();

            switch (item.getItemId()) {
                case Menu.FIRST: {
                    SharedPreferences mPlayListSpf = mMainActivity.getSharedPreferences(Values.PLAY_LIST_SPF, 0);
                    SharedPreferences.Editor editor = mPlayListSpf.edit();
                    editor.putString(Values.PLAY_LIST_SPF_KEY, mMusicPathList.get(holder.getAdapterPosition()));
                    editor.apply();
                }
                    break;

                case Menu.FIRST + 1: {
                    // TODO: 2018/11/8
                }
                    break;
            }

            Values.CURRENT_SELECT_ITEM_INDEX_WITH_ITEM_MENU = -1;

            return false;
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        //no crash
        if (i < 0 || i > mMusicNameList.size() && viewHolder.getAdapterPosition() < 0 || viewHolder.getAdapterPosition() > mMusicNameList.size()) {
            return;
        }

        //no crash
        if (mMusicNameList.size() == 0 || mMusicPathList.size() == 0 || mSongAlbumList.size() == 0) {
            return;
        }

        /* show song name, use songNameList */
        Values.CURRENT_BIND_INDEX_MUSIC_LIST = viewHolder.getAdapterPosition();

        viewHolder.mMusicText.setText(mMusicNameList.get(viewHolder.getAdapterPosition()));
        viewHolder.mMusicAlbumName.setText(mSongAlbumList.get(viewHolder.getAdapterPosition()));
        String prefix = mMusicPathList.get(i).substring(mMusicPathList.get(i).lastIndexOf(".") + 1);
        viewHolder.mMusicExtName.setText(prefix);

        if (prefix.equals("mp3")) {
            viewHolder.mMusicExtName.setBackgroundResource(R.color.mp3TypeColor);
        }

        /*--- 添加标记以便避免ImageView因为ViewHolder的复用而出现混乱 ---*/
        viewHolder.mMusicCloverImage.setTag(R.string.key_id_1, i);

        MyTask task = new MyTask(viewHolder.mMusicCloverImage, mMainActivity, mMusicPathList.get(i), i);
        task.execute();
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
            if (picData == null || mImageViewWeakReference.get() == null) {
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

            if (mImageViewWeakReference.get() == null) {
                return null;
            }

            //根据position判断是否为复用ViewHolder
            if (((int) mImageViewWeakReference.get().getTag(R.string.key_id_1)) != mPosition) {
                GlideApp.with(mContextWeakReference.get()).clear(mImageViewWeakReference.get());
                return null;
            }
            return Utils.Audio.getAlbumByteImage(mPath);
        }
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

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView mMusicCloverImage;

        ImageView mItemMenuButton;

        TextView mMusicText;

        TextView mMusicAlbumName;

        TextView mMusicExtName;

        PopupMenu mPopupMenu;

        Menu mMenu;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mMusicAlbumName = itemView.findViewById(R.id.recycler_item_music_album_name);
            mMusicCloverImage = itemView.findViewById(R.id.recycler_item_album_image);
            mMusicText = itemView.findViewById(R.id.recycler_item_music_name);
            mItemMenuButton = itemView.findViewById(R.id.recycler_item_menu);
            mMusicExtName = itemView.findViewById(R.id.recycler_item_music_type_name);

            mPopupMenu = new PopupMenu(mMainActivity, mItemMenuButton);
            mMenu = mPopupMenu.getMenu();

            //noinspection PointlessArithmeticExpression
            mMenu.add(Menu.NONE, Menu.FIRST + 0, 0, "下一首播放");
            mMenu.add(Menu.NONE, Menu.FIRST + 1, 0, "加入播放列表");
            mMenu.add(Menu.NONE, Menu.FIRST + 2, 0, "删除");
            mMenu.add(Menu.NONE, Menu.FIRST + 3, 0, "查看专辑");
            mMenu.add(Menu.NONE, Menu.FIRST + 4, 0, "详细信息");

            MenuInflater menuInflater = mMainActivity.getMenuInflater();
            menuInflater.inflate(R.menu.recycler_song_item_menu, mMenu);
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
