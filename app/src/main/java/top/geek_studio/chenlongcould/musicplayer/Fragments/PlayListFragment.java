/*
 * ************************************************************
 * 文件：PlayListFragment.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月24日 17:50:10
 * 上次修改时间：2018年11月24日 09:56:51
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import top.geek_studio.chenlongcould.musicplayer.Activities.PublicActivity;
import top.geek_studio.chenlongcould.musicplayer.Adapters.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.R;

public final class PlayListFragment extends Fragment {

    private ConstraintLayout mAddRecentItem;

    private ConstraintLayout mFavouriteMusic;

    private FastScrollRecyclerView mRecyclerView;

    private Activity mActivity;

    //实例化一个fragment
    public static PlayListFragment newInstance(int index) {
        return new PlayListFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play_list, container, false);
        mAddRecentItem = view.findViewById(R.id.fragment_play_list_add_recent);
        mFavouriteMusic = view.findViewById(R.id.fragment_play_list_play_favourite);
        mRecyclerView = view.findViewById(R.id.fragment_play_list_recycler);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mRecyclerView.setHasFixedSize(true);
        MyRecyclerAdapter adapter = new MyRecyclerAdapter(Data.sMusicItems, mActivity);
        mRecyclerView.setAdapter(adapter);

        mAddRecentItem.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, PublicActivity.class);
            intent.putExtra("start_by", "add recent");
            startActivity(intent);
        });

        mFavouriteMusic.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, PublicActivity.class);
            intent.putExtra("start_by", "favourite music");
            startActivity(intent);
        });
        return view;
    }

}
