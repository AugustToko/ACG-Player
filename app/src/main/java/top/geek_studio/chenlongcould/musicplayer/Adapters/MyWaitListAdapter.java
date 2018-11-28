/*
 * ************************************************************
 * 文件：MyWaitListAdapter.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月28日 16:12:44
 * 上次修改时间：2018年11月28日 10:34:41
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.provider.MediaStore;
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

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import top.geek_studio.chenlongcould.musicplayer.Activities.AlbumDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.MusicDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.PublicActivity;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.IStyle;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.PlayListsUtil;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class MyWaitListAdapter extends RecyclerView.Adapter<MyWaitListAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter, IStyle {

    private List<MusicItem> mMusicItems;

    private Context mContext;

    private ViewHolder currentBind;

    public MyWaitListAdapter(Context context, List<MusicItem> musicItems) {
        mMusicItems = musicItems;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_in_detail, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);

        view.setOnClickListener(v -> new Thread(() -> {
            String clickedPath = mMusicItems.get(holder.getAdapterPosition()).getMusicPath();

            if (Data.sMusicBinder.isPlayingMusic()) {
                if (clickedPath.equals(Values.CurrentData.CURRENT_SONG_PATH)) {
                    Utils.SendSomeThing.sendPause(mContext);
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

            MainActivity activity = (MainActivity) Data.sActivities.get(0);

            //set InfoBar
            activity.setCurrentSongInfo(
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
                Utils.SendSomeThing.sendPause(mContext);
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
                    SharedPreferences mPlayListSpf = mContext.getSharedPreferences(Values.SharedPrefsTag.PLAY_LIST_SPF_NAME_MY_FAVOURITE, 0);
                    SharedPreferences.Editor editor = mPlayListSpf.edit();
                    editor.putString(Values.PLAY_LIST_SPF_KEY, mMusicItems.get(index).getMusicPath());
                    editor.apply();

                    Utils.Ui.fastToast(Data.sActivities.get(0).getApplicationContext(), "Done!");
                }
                break;

                case Menu.FIRST + 2: {
                    // TODO: 2018/11/18 test play list
                    PlayListsUtil.createPlaylist(mContext, String.valueOf(new Random(1000)));
                }
                break;

                case Menu.FIRST + 4: {
                    String albumName = mMusicItems.get(holder.getAdapterPosition()).getMusicAlbum();
                    Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null,
                            MediaStore.Audio.Albums.ALBUM + "= ?", new String[]{mMusicItems.get(holder.getAdapterPosition()).getMusicAlbum()}, null);
                    if (Data.sActivities.size() == 1) {

                        //int MainActivity
                        MainActivity mainActivity = (MainActivity) Data.sActivities.get(0);
                        Intent intent = new Intent(mainActivity, AlbumDetailActivity.class);
                        intent.putExtra("key", albumName);
                        if (cursor != null) {
                            cursor.moveToFirst();
                            int id = Integer.parseInt(cursor.getString(0));
                            intent.putExtra("_id", id);
                            cursor.close();
                        }
                        mContext.startActivity(intent);
                    } else if (Data.sActivities.size() == 2) {

                        //int MusicDetailActivity
                        MusicDetailActivity activity = (MusicDetailActivity) Data.sActivities.get(1);
                        Intent intent = new Intent(activity, AlbumDetailActivity.class);
                        intent.putExtra("key", albumName);
                        if (cursor != null) {
                            cursor.moveToFirst();
                            int id = Integer.parseInt(cursor.getString(0));
                            intent.putExtra("_id", id);
                            cursor.close();
                        }
                        mContext.startActivity(intent);
                    }

                }
                break;

                case Menu.FIRST + 5: {
                    Intent intent = new Intent(mContext, PublicActivity.class);
                    intent.putExtra("start_by", "detail");
                    mContext.startActivity(intent);
                }
                break;
            }

            Values.CurrentData.CURRENT_SELECT_ITEM_INDEX_WITH_ITEM_MENU = -1;

            return false;
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        //no crash
        if (mMusicItems.size() == 0 || i < 0 || i > mMusicItems.size() && viewHolder.getAdapterPosition() < 0 || viewHolder.getAdapterPosition() > mMusicItems.size()) {
            return;
        }

        currentBind = viewHolder;

        String prefix = mMusicItems.get(i).getMusicPath().substring(mMusicItems.get(i).getMusicPath().lastIndexOf(".") + 1);

        viewHolder.mIndexText.setText(String.valueOf(i));
        viewHolder.mMusicNameText.setText(mMusicItems.get(i).getMusicName());
        viewHolder.mAlbumText.setText(mMusicItems.get(i).getMusicAlbum());
        viewHolder.mExtName.setText(prefix);

        initStyle();
    }

    @Override
    public void initStyle() {

        //type_background
        if (Values.Style.NIGHT_MODE) {
            currentBind.mExtName.setBackgroundColor(Color.GRAY);
        } else {
            if (currentBind.mExtName.getText().equals("mp3")) {
                currentBind.mExtName.setBackgroundResource(R.color.mp3TypeColor);
            } else {
                currentBind.mExtName.setBackgroundColor(Color.CYAN);
                return;
            }
        }

        //style
        currentBind.mMusicNameText.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));
    }


    @Override
    public int getItemCount() {
        return mMusicItems.size();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return String.valueOf(mMusicItems.get(position).getMusicName().charAt(0));
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        volatile ImageView mItemMenuButton;

        TextView mMusicNameText;

        TextView mAlbumText;

        TextView mIndexText;

        TextView mExtName;

        PopupMenu mPopupMenu;

        Menu mMenu;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mIndexText = itemView.findViewById(R.id.index_textview);
            mMusicNameText = itemView.findViewById(R.id.item_main_text);
            mAlbumText = itemView.findViewById(R.id.album_text);
            mExtName = itemView.findViewById(R.id.item_in_detail_ext);
            mItemMenuButton = itemView.findViewById(R.id.item_menu);

            mPopupMenu = new PopupMenu(mContext, mItemMenuButton);
            mMenu = mPopupMenu.getMenu();

            //noinspection PointlessArithmeticExpression
            mMenu.add(Menu.NONE, Menu.FIRST + 0, 0, "下一首播放");
            mMenu.add(Menu.NONE, Menu.FIRST + 1, 0, "喜欢");
            mMenu.add(Menu.NONE, Menu.FIRST + 2, 0, "加入播放列表");
            mMenu.add(Menu.NONE, Menu.FIRST + 4, 0, "查看专辑");
            mMenu.add(Menu.NONE, Menu.FIRST + 5, 0, "详细信息");

            MenuInflater menuInflater = Data.sActivities.get(0).getMenuInflater();
            menuInflater.inflate(R.menu.recycler_song_item_menu, mMenu);
        }
    }
}
