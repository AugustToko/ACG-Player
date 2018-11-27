/*
 * ************************************************************
 * 文件：MyWaitListAdapter.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月28日 07:53:38
 * 上次修改时间：2018年11月28日 07:51:22
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.R;

public final class MyWaitListAdapter extends RecyclerView.Adapter<MyWaitListAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private List<MusicItem> mMusicItems;

    private Context mContext;

    public MyWaitListAdapter(Context context, List<MusicItem> musicItems) {
        mMusicItems = musicItems;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_in_detail, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);

        view.setOnClickListener(v -> {
            // TODO: 2018/11/27 play
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.mIndexText.setText(i);
        viewHolder.mMusicNameText.setText(mMusicItems.get(i).getMusicName());
        viewHolder.mAlbumText.setText(mMusicItems.get(i).getMusicAlbum());
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

        TextView mMusicNameText;

        TextView mAlbumText;

        TextView mIndexText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mMusicNameText = itemView.findViewById(R.id.item_main_text);
            mAlbumText = itemView.findViewById(R.id.album_text);
            mIndexText = itemView.findViewById(R.id.item_index_text);
        }
    }
}
