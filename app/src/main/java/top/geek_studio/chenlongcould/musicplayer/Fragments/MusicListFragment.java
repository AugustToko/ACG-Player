/*
 * ************************************************************
 * 文件：MusicListFragment.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月16日 20:43:13
 * 上次修改时间：2019年01月16日 08:34:31
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
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import top.geek_studio.chenlongcould.geeklibrary.Theme.IStyle;
import top.geek_studio.chenlongcould.geeklibrary.VisibleOrGone;
import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Adapters.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.databinding.FragmentMusicListLayoutBinding;

public final class MusicListFragment extends Fragment implements VisibleOrGone, IStyle {

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
        Log.d(Values.LogTAG.LIFT_TAG, "onAttach: " + TAG);
        super.onAttach(context);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(Values.LogTAG.LIFT_TAG, "onCreate: " + TAG);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mMusicListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_list_layout, container, false);
        mMusicListBinding.includeRecycler.recyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL));
        mMusicListBinding.includeRecycler.recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mMusicListBinding.includeRecycler.recyclerView.setHasFixedSize(true);
        Log.d(Values.TAG_UNIVERSAL_ONE, "onCreateView: loading adapter");
        adapter = new MyRecyclerAdapter(Data.sMusicItems, mActivity, TAG);
        mMusicListBinding.includeRecycler.recyclerView.setAdapter(adapter);

//        mRecyclerView.setRecyclerListener(holder -> {
//            MyRecyclerAdapter.ViewHolder myViewHolder = (MyRecyclerAdapter.ViewHolder) holder;
//            GlideApp.with(this).clear(myViewHolder.mMusicCloverImage);
//        });

//        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_SETTLING) {
//                    sIsScrolling = true;
//                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    if (sIsScrolling) {
//                        GlideApp.with(mActivity).resumeRequests();
//                    } else {
//                        GlideApp.with(mActivity).pauseAllRequests();
//                    }
//                    sIsScrolling = false;
//                }
//            }
//
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//            }
//        });

        return mMusicListBinding.getRoot();
    }

    @Override
    public void onDetach() {
        mMusicListBinding = null;
        super.onDetach();
    }

    public final MyRecyclerAdapter getAdapter() {
        return adapter;
    }

    public FragmentMusicListLayoutBinding getMusicListBinding() {
        return mMusicListBinding;
    }

    @Override
    public void visibleOrGone(int status) {
        if (mMusicListBinding.includeRecycler.recyclerView != null)
            mMusicListBinding.includeRecycler.recyclerView.setVisibility(status);
    }

    @Override
    public void initStyle() {
    }
}
