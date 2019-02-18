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
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.databinding.FragmentMusicListBinding;

public final class MusicListFragment extends Fragment {

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
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mMusicListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_list, container, false);
        mMusicListBinding.includeRecycler.recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mMusicListBinding.includeRecycler.recyclerView.setHasFixedSize(true);
        adapter = new MyRecyclerAdapter(mActivity, Data.sMusicItems, PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(Values.SharedPrefsTag.RECYCLER_VIEW_ITEM_STYLE, 0));
        mMusicListBinding.includeRecycler.recyclerView.setAdapter(adapter);

        return mMusicListBinding.getRoot();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (Data.sMusicItems.size() > 0 && adapter != null && !isVisibleToUser)
            adapter.clearSelection();
    }

    public final MyRecyclerAdapter getAdapter() {
        return adapter;
    }

    public FragmentMusicListBinding getMusicListBinding() {
        return mMusicListBinding;
    }

}
