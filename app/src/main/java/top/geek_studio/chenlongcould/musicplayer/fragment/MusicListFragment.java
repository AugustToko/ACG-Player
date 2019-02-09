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

package top.geek_studio.chenlongcould.musicplayer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import top.geek_studio.chenlongcould.geeklibrary.theme.IStyle;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.databinding.FragmentMusicListBinding;

public final class MusicListFragment extends Fragment implements IStyle {

    public static final String TAG = "MusicListFragment";

    private FragmentMusicListBinding mMusicListBinding;

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
        mMusicListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_list, container, false);
//        mMusicListBinding.includeRecycler.recyclerView.addItemDecoration(Data.getItemDecoration(mActivity));
        mMusicListBinding.includeRecycler.recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mMusicListBinding.includeRecycler.recyclerView.setHasFixedSize(true);
        adapter = new MyRecyclerAdapter(Data.sMusicItems, mActivity, TAG);
        mMusicListBinding.includeRecycler.recyclerView.setAdapter(adapter);

        return mMusicListBinding.getRoot();
    }


    public final MyRecyclerAdapter getAdapter() {
        return adapter;
    }

    public FragmentMusicListBinding getMusicListBinding() {
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
