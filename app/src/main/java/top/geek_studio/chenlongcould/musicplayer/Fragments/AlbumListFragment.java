/*
 * ************************************************************
 * 文件：AlbumListFragment.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月23日 16:43:35
 * 上次修改时间：2018年11月23日 15:00:15
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Adapters.MyRecyclerAdapter2AlbumList;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.Models.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class AlbumListFragment extends Fragment {
    private static boolean sIsScrolling = false;

    private RecyclerView mRecyclerView;

    private MyRecyclerAdapter2AlbumList mMyRecyclerAdapter2AlbumList;

    //temp
    private ArrayList<AlbumItem> mAlbumList = new ArrayList<>();

    private NotLeakHandler mNotLeakHandler;

    private HandlerThread mHandlerThread;

    private MainActivity mMainActivity;

    private boolean ON_CREATE_VIEW_DONE = false;

    //实例化一个fragment
    public static AlbumListFragment newInstance() {
        return new AlbumListFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mHandlerThread = new HandlerThread("Handler Thread in AlbumListFragment");
        mHandlerThread.start();
        mNotLeakHandler = new NotLeakHandler(this, mHandlerThread.getLooper());
        mMainActivity = (MainActivity) getActivity();

        //wait MusicFragment data init
        sureGetDataDone();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_list, container, false);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        //        needn't
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    sIsScrolling = true;
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (sIsScrolling) {
                        GlideApp.with(mMainActivity).resumeRequests();
                    } else {
                        GlideApp.with(mMainActivity).pauseAllRequests();
                    }
                    sIsScrolling = false;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        ON_CREATE_VIEW_DONE = true;
        return view;
    }

    private void sureGetDataDone() {
        if (Values.MUSIC_DATA_INIT_DONE) {
            new Thread(() -> {
//                //去除重复数据
//                List<String> temp = new ArrayList<>();
//                for (MusicItem item : Data.sMusicItems) {
//                    temp.add(item.getMusicAlbum());
//                }
//                HashSet<String> hashSet = new HashSet<>(temp);
//                arrayList = new ArrayList<>(hashSet);
//                arrayList.sort(Values.sort);

                Cursor cursor = mMainActivity.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    do {
                        String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM));
                        String albumId = cursor.getString(cursor.getColumnIndexOrThrow("_id"));

                        mAlbumList.add(new AlbumItem(albumName, Integer.parseInt(albumId)));
                    } while (cursor.moveToNext());

                    cursor.close();
                }

                mNotLeakHandler.sendEmptyMessage(Values.HandlerWhat.GET_DATA_DONE);
            }).start();
        } else {
            new Handler().postDelayed(this::sureGetDataDone, 500);
        }
    }

    private void sureCreateViewDone() {
        if (ON_CREATE_VIEW_DONE) {
            mMainActivity.runOnUiThread(() -> {
                mMyRecyclerAdapter2AlbumList = new MyRecyclerAdapter2AlbumList(mMainActivity, mAlbumList);
                mRecyclerView.addItemDecoration(new DividerItemDecoration(mMainActivity, DividerItemDecoration.VERTICAL));
                mRecyclerView.setAdapter(mMyRecyclerAdapter2AlbumList);
            });
        } else {
            new Handler().postDelayed(this::sureGetDataDone, 500);
        }
    }

    @SuppressWarnings("unused")
    public NotLeakHandler getNotLeakHandler() {
        return mNotLeakHandler;
    }

    @Override
    public void onDestroy() {
        mHandlerThread.quitSafely();
        super.onDestroy();
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    class NotLeakHandler extends Handler {
        @SuppressWarnings("unused")
        private WeakReference<AlbumListFragment> mWeakReference;

        NotLeakHandler(AlbumListFragment fragment, Looper looper) {
            super(looper);
            mWeakReference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Values.HandlerWhat.GET_DATA_DONE: {
                    sureCreateViewDone();
                }
                break;
                default:
                    break;
            }
        }
    }
}
