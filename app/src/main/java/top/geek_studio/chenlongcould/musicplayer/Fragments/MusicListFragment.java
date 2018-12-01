/*
 * ************************************************************
 * 文件：MusicListFragment.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月01日 16:21:06
 * 上次修改时间：2018年12月01日 16:20:48
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.lang.ref.WeakReference;
import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Adapters.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class MusicListFragment extends Fragment {

    private static final String TAG = "MusicListFragment";

    @SuppressWarnings("unused")
    private static boolean sIsScrolling = false;

    private MyRecyclerAdapter adapter;

    private FastScrollRecyclerView mRecyclerView;

    private MainActivity mActivity;

    private NotLeakHandler mHandler;

    private HandlerThread mHandlerThread;

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
        super.onAttach(context);

        mActivity = (MainActivity) getActivity();

        mHandlerThread = new HandlerThread("Handler Thread in MusicListFragment");
        mHandlerThread.start();
        mHandler = new NotLeakHandler(this, mHandlerThread.getLooper());

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_list_layout, container, false);
        findId(view);

        ViewPreloadSizeProvider<MusicItem> preloadSizeProvider = new ViewPreloadSizeProvider<>();
        MyPreloadModelProvider preloadModelProvider = new MyPreloadModelProvider();

        RecyclerViewPreloader<MusicItem> preLoader = new RecyclerViewPreloader<>(this, preloadModelProvider
                , preloadSizeProvider, 10);

        mRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mRecyclerView.setHasFixedSize(true);
        adapter = new MyRecyclerAdapter(Data.sMusicItems, mActivity);
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.addOnScrollListener(preLoader);

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

        CREATE_VIEW_DONE = true;
        return view;
    }

    private void findId(View view) {
        mRecyclerView = view.findViewById(R.id.recycler_view);
    }

    /**
     * 延迟循环确认是否已经createView
     */
    private void sureCreateViewDone() {
        if (CREATE_VIEW_DONE) {
            adapter.notifyDataSetChanged();
        } else {
            new Handler().postDelayed(this::sureCreateViewDone, 1000);      //循环一秒
        }
    }

    public final MyRecyclerAdapter getAdapter() {
        return adapter;
    }

    public final RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    final class NotLeakHandler extends Handler {
        @SuppressWarnings("unused")
        private WeakReference<MusicListFragment> mWeakReference;

        NotLeakHandler(MusicListFragment fragment, Looper looper) {
            super(looper);
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

    @Override
    public void onDestroy() {
        mHandlerThread.quitSafely();
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    public class MyPreloadModelProvider implements ListPreloader.PreloadModelProvider<MusicItem> {
        @NonNull
        @Override
        public List<MusicItem> getPreloadItems(int position) {
            return Data.sMusicItems.subList(position, position + 1);
        }

        @Nullable
        @Override
        public RequestBuilder<?> getPreloadRequestBuilder(@NonNull MusicItem item) {
            return GlideApp.with(mActivity).load(item);
        }
    }


}
