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

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.MusicDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private List<MusicItem> mMusicItems;

    private MainActivity mMainActivity;

    private Context mContext;

    public MyRecyclerAdapter(List<MusicItem> musicItems, Context context) {
        mMusicItems = musicItems;
        mMainActivity = (MainActivity) Data.sActivities.get(0);
        mContext = context;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return String.valueOf(mMusicItems.get(position).getMusicName().charAt(0));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_music_list_item, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);

        view.setOnClickListener(v -> new Thread(() -> {
            String clickedPath = mMusicItems.get(holder.getAdapterPosition()).getMusicPath();

            if (Data.sMusicBinder.isPlayingMusic()) {
                if (clickedPath.equals(Values.CurrentData.CURRENT_SONG_PATH)) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(Values.PKG_NAME, Values.BroadCast.ReceiverOnMusicPause));
                    mContext.sendBroadcast(intent);
                    return;
                }
            }

            Data.sMusicBinder.resetMusic();

            String clickedSongName = mMusicItems.get(holder.getAdapterPosition()).getMusicName();
            String clickedSongAlbumName = mMusicItems.get(holder.getAdapterPosition()).getMusicAlbum();

            //清楚播放队列, 并加入当前歌曲序列
            Data.sHistoryPlayIndex.clear();
            Data.sHistoryPlayIndex.add(holder.getAdapterPosition());

            Bitmap cover = Utils.Audio.getMp3Cover(clickedPath);

            //set InfoBar
            mMainActivity.setCurrentSongInfo(
                    clickedSongName
                    , clickedSongAlbumName
                    , mMusicItems.get(holder.getAdapterPosition()).getMusicPath()
                    , cover
            );

            Data.sCurrentMusicAlbum = clickedSongAlbumName;
            Data.sCurrentMusicName = clickedSongName;
            Data.sCurrentMusicBitmap = cover;

            Values.MUSIC_PLAYING = true;
            Values.HAS_PLAYED = true;
            Values.CurrentData.CURRENT_MUSIC_INDEX = holder.getAdapterPosition();
            Values.CurrentData.CURRENT_SONG_PATH = clickedPath;

            Utils.Ui.setPlayButtonNowPlaying();

            if (Data.sActivities.size() >= 2) {
                MusicDetailActivity musicDetailActivity = (MusicDetailActivity) Data.sActivities.get(1);
                musicDetailActivity.setCurrentSongInfo(clickedSongName, clickedSongAlbumName, Utils.Audio.getAlbumByteImage(clickedPath));
            }

            try {
                Data.sMusicBinder.setDataSource(clickedPath);
                Data.sMusicBinder.prepare();
                Data.sMusicBinder.playMusic();

                if (Data.sActivities.size() >= 2) {
                    MusicDetailActivity musicDetailActivity = (MusicDetailActivity) Data.sActivities.get(1);
                    MusicDetailActivity.NotLeakHandler notLeakHandler = musicDetailActivity.getHandler();
                    notLeakHandler.sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Data.sMusicBinder.resetMusic();
            }

        }).start());

        view.setOnLongClickListener(v -> {
            if (Data.sMusicBinder.isPlayingMusic()) {
                Utils.Ui.setNowNotPlaying(mContext);
            }
            return true;
        });

        holder.mItemMenuButton.setOnClickListener(v -> holder.mPopupMenu.show());

        holder.mPopupMenu.setOnMenuItemClickListener(item -> {

            int index = holder.getAdapterPosition();

            Values.CurrentData.CURRENT_SELECT_ITEM_INDEX_WITH_ITEM_MENU = index;

            switch (item.getItemId()) {
                //noinspection PointlessArithmeticExpression
                case Menu.FIRST + 0: {
                    Data.sNextWillPlayIndex = holder.getAdapterPosition();
                }
                break;

                case Menu.FIRST + 1: {
                    // TODO: 2018/11/8 待完善(最喜爱歌曲列表)

                    SharedPreferences mPlayListSpf = mMainActivity.getSharedPreferences(Values.SharedPrefsTag.PLAY_LIST_SPF_NAME_MY_FAVOURITE, 0);
                    SharedPreferences.Editor editor = mPlayListSpf.edit();
                    editor.putString(Values.PLAY_LIST_SPF_KEY, mMusicItems.get(index).getMusicPath());
                    editor.apply();

                    Utils.Ui.fastToast(Data.sActivities.get(0).getApplicationContext(), "Done!");
                }
                break;

                case Menu.FIRST + 2: {

                    SharedPreferences mDefPrefs = PreferenceManager.getDefaultSharedPreferences(mMainActivity);

                    if (mDefPrefs.getInt(Values.SharedPrefsTag.PLAY_LIST_NUM, 0) == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
                        builder.setTitle("Create a list");
                        EditText et = new EditText(mMainActivity);
                        et.setHint("Play List 1");
                        et.setMaxLines(1);
                        et.setSingleLine(true);
                        builder.setView(et);
                        builder.setCancelable(true);
                        builder.setNegativeButton("Cancel", null);
                        builder.setNeutralButton("Enter", (dialog, which) -> {
                            String name = et.getText().toString();
                            if (name.equals("")) {
                                name = "PLAY_LIST_1";
                            }
                            // TODO: 2018/11/11 创建一个列表
                        });
                    } else {

                    }

                }
            }

            Values.CurrentData.CURRENT_SELECT_ITEM_INDEX_WITH_ITEM_MENU = -1;

            return false;
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        //no crash
        if (i < 0 || i > mMusicItems.size() && viewHolder.getAdapterPosition() < 0 || viewHolder.getAdapterPosition() > mMusicItems.size()) {
            return;
        }

        //no crash
        if (mMusicItems.size() == 0) {
            return;
        }

        /* show song name, use songNameList */
        Values.CurrentData.CURRENT_BIND_INDEX_MUSIC_LIST = viewHolder.getAdapterPosition();

        viewHolder.mMusicText.setText(mMusicItems.get(viewHolder.getAdapterPosition()).getMusicName());
        viewHolder.mMusicAlbumName.setText(mMusicItems.get(viewHolder.getAdapterPosition()).getMusicAlbum());
        String prefix = mMusicItems.get(i).getMusicPath().substring(mMusicItems.get(i).getMusicPath().lastIndexOf(".") + 1);
        viewHolder.mMusicExtName.setText(prefix);

        //type_background
        if (prefix.equals("mp3")) {
            viewHolder.mMusicExtName.setBackgroundResource(R.color.mp3TypeColor);
        } else {
            viewHolder.mMusicExtName.setBackgroundColor(Color.CYAN);
            return;
        }

        /*--- 添加标记以便避免ImageView因为ViewHolder的复用而出现混乱 ---*/
        viewHolder.mMusicCloverImage.setTag(R.string.key_id_1, i);

        MyTask task = new MyTask(viewHolder.mMusicCloverImage, mContext, mMusicItems.get(i).getMusicPath(), i);
        task.execute();
    }

    static class MyTask extends AsyncTask<Void, Void, byte[]> {

        private String mPath;

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
        return mMusicItems.size();
    }

    /**
     * 为复用的ImageView清除内存
     */
    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
//        GlideApp.with(mMainActivity).clear(holder.mMusicCloverImage);
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
            mMenu.add(Menu.NONE, Menu.FIRST + 1, 0, "喜欢");
            mMenu.add(Menu.NONE, Menu.FIRST + 2, 0, "加入播放列表");
            mMenu.add(Menu.NONE, Menu.FIRST + 3, 0, "删除");
            mMenu.add(Menu.NONE, Menu.FIRST + 4, 0, "查看专辑");
            mMenu.add(Menu.NONE, Menu.FIRST + 5, 0, "详细信息");

            MenuInflater menuInflater = mMainActivity.getMenuInflater();
            menuInflater.inflate(R.menu.recycler_song_item_menu, mMenu);
        }
    }

    class NotLeakHandler extends Handler {
        @SuppressWarnings("unused")
        private WeakReference<MyRecyclerAdapter> mWeakReference;

        NotLeakHandler(MyRecyclerAdapter adapter, Looper looper) {
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
