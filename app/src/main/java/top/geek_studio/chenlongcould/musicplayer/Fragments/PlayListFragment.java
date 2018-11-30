/*
 * ************************************************************
 * 文件：PlayListFragment.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月30日 20:36:09
 * 上次修改时间：2018年11月30日 20:35:23
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import top.geek_studio.chenlongcould.musicplayer.Activities.PublicActivity;
import top.geek_studio.chenlongcould.musicplayer.Adapters.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.IStyle;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;

// TODO: 2018/11/26 text color ...
public final class PlayListFragment extends Fragment implements IStyle {

    private ConstraintLayout mAddRecentItem;

    private ConstraintLayout mFavouriteMusic;

//    private FastScrollRecyclerView mRecyclerView;

    private View mView;

    private TextView mNameRecent;

    private TextView mFavourite;

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
        findId(view);

        initStyle();

        MyRecyclerAdapter adapter = new MyRecyclerAdapter(Data.sMusicItems, mActivity);
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
//        mRecyclerView.setHasFixedSize(true);
//        mRecyclerView.setAdapter(adapter);

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

    private void findId(View view) {
        mView = view.findViewById(R.id.view_line_one);
        mAddRecentItem = view.findViewById(R.id.fragment_play_list_add_recent);
        mFavouriteMusic = view.findViewById(R.id.fragment_play_list_play_favourite);
        mFavourite = view.findViewById(R.id.fragment_play_list_play_favourite_name);
        mNameRecent = view.findViewById(R.id.fragment_play_list_add_recent_name);
//        mRecyclerView = view.findViewById(R.id.fragment_play_list_recycler);
    }

    @Override
    public void initStyle() {
        if (Values.Style.NIGHT_MODE) {
            mView.setBackgroundColor(Color.parseColor("#7c7c7c"));
        } else {
            mView.setBackgroundColor(Color.parseColor("#e6e6e6"));
        }

        mNameRecent.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));
        mFavourite.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));

    }
}
