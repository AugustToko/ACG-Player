/*
 * ************************************************************
 * 文件：MusicListFragment.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月19日 12:56:02
 * 上次修改时间：2018年12月19日 12:46:08
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Adapters.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Interface.VisibleOrGone;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.databinding.FragmentMusicListLayoutBinding;

public final class MusicListFragment extends Fragment implements VisibleOrGone {

    public static final String TAG = "MusicListFragment";

    private FragmentMusicListLayoutBinding mMusicListBinding;

    @SuppressWarnings("unused")
    private static boolean sIsScrolling = false;

    private MyRecyclerAdapter adapter;

    private MainActivity mActivity;

    private Handler mHandler;

    private boolean CREATE_VIEW_DONE = false;

    //实例化一个fragment
    public static MusicListFragment newInstance() {
        return new MusicListFragment();
    }

//    /**
//     * old version
//     *
//     * @param dir music dir
//     */
//    private void findMp3(File dir) {
//        Log.d(TAG, "findMp3: doing");
//        if (!dir.isDirectory()) {
//            return;
//        }
//
//        for (File f : dir.listFiles()) {
//            if (!f.isDirectory()) {
//                mMusicPathList.add(f.getPath());
//
//                String fileName = f.getName();
//                String prefix = fileName.substring(fileName.lastIndexOf(".") + 1);//如果想获得不带点的后缀，变为fileName.lastIndexOf(".")+1
//
//                String fileOtherName = fileName.substring(0, fileName.length() - prefix.length());//得到文件名。去掉了后缀
//                mSongNameList.add(fileOtherName);
//
//            } else {
//                findMp3(f);
//            }
//        }
//    }

    @Override
    public void onAttach(Context context) {
        Log.d(Values.LogTAG.LIFT_TAG, "onAttach: " + TAG);
        super.onAttach(context);
        mActivity = (MainActivity) getActivity();

        mHandler = new NotLeakHandler(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(Values.LogTAG.LIFT_TAG, "onCreate: " + TAG);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(Values.LogTAG.LIFT_TAG, "onCreateView: " + TAG);
        mMusicListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_list_layout, container, false);
        mMusicListBinding.includeRecycler.recyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL));
        mMusicListBinding.includeRecycler.recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mMusicListBinding.includeRecycler.recyclerView.setHasFixedSize(true);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        CREATE_VIEW_DONE = true;
    }

    @Override
    public void onDetach() {
        mMusicListBinding = null;
        super.onDetach();
    }

    /**
     * 延迟循环确认是否已经createView
     */
    private void sureCreateViewDone() {
        if (CREATE_VIEW_DONE) {
            adapter.notifyDataSetChanged();
        } else {
            new Handler().postDelayed(this::sureCreateViewDone, 500);      //循环一秒
        }
    }

    public final MyRecyclerAdapter getAdapter() {
        return adapter;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public FragmentMusicListLayoutBinding getMusicListBinding() {
        return mMusicListBinding;
    }

    @Override
    public void visibleOrGone(int status) {
        if (mMusicListBinding.includeRecycler.recyclerView != null)
            mMusicListBinding.includeRecycler.recyclerView.setVisibility(status);
    }

    final class NotLeakHandler extends Handler {
        @SuppressWarnings("unused")
        private WeakReference<MusicListFragment> mWeakReference;

        NotLeakHandler(MusicListFragment fragment) {
            mWeakReference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Values.HandlerWhat.INIT_MUSIC_LIST_DONE: {
                    mActivity.runOnUiThread(() -> {
                        if (CREATE_VIEW_DONE) {
                            sureCreateViewDone();
                            Toast.makeText(mWeakReference.get().getActivity(), "Loading", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                default:
                    break;
            }
        }
    }

}
