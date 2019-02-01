/*
 * ************************************************************
 * 文件：MusicListFragment.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月27日 13:11:38
 * 上次修改时间：2019年01月27日 13:08:44
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import top.geek_studio.chenlongcould.geeklibrary.Theme.IStyle;
import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Adapters.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.databinding.FragmentMusicListLayoutBinding;

public final class MusicListFragment extends Fragment implements IStyle {

    public static final String TAG = "MusicListFragment";

    private FragmentMusicListLayoutBinding mMusicListBinding;

    @SuppressWarnings("unused")
    private static boolean sIsScrolling = false;

    private MyRecyclerAdapter adapter;

    private MainActivity mActivity;

    //实例化一个fragment
    public static MusicListFragment newInstance() {
        return new MusicListFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mMusicListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_list_layout, container, false);
        mMusicListBinding.includeRecycler.recyclerView.addItemDecoration(Data.getItemDecoration(mActivity));
        mMusicListBinding.includeRecycler.recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mMusicListBinding.includeRecycler.recyclerView.setHasFixedSize(true);
        adapter = new MyRecyclerAdapter(Data.sMusicItems, mActivity, TAG);
        mMusicListBinding.includeRecycler.recyclerView.setAdapter(adapter);

        return mMusicListBinding.getRoot();
    }


    public final MyRecyclerAdapter getAdapter() {
        return adapter;
    }

    public FragmentMusicListLayoutBinding getMusicListBinding() {
        return mMusicListBinding;
    }

//    @Override
//    public void visibleOrGone(int status) {
//        if (mMusicListBinding.includeRecycler.recyclerView != null)
//            mMusicListBinding.includeRecycler.recyclerView.setVisibility(status);
//    }

    @Override
    public void initStyle() {
    }
}
