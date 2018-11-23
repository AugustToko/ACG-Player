/*
 * ************************************************************
 * 文件：MusicListFragment.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月23日 11:17:30
 * 上次修改时间：2018年11月22日 13:39:28
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.lang.ref.WeakReference;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Adapters.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
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

        // TODO: 2018/11/2 需要转为线程池的使用。
        new Thread(() -> {

            if (Data.sMusicItems.isEmpty()) {
                /*---------------------- init Data!!!! -------------------*/
                Cursor cursor = mActivity.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
                if (cursor != null && cursor.moveToFirst()) {
                    //没有歌曲直接退出app
                    if (cursor.getCount() == 0) {
                        mActivity.runOnUiThread(() -> Utils.Ui.fastToast(mActivity, "Can not find any music!"));
                        Data.sActivities.get(0).finish();
                        return;
                    }
                    do {
                        String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                        String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
                        String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                        String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                        int size = (int) cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                        int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                        String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                        long addTime = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));

                        Data.sMusicItems.add(new MusicItem(name, path, id, albumName, duration, size, artist, (int) addTime));
                        Data.sMusicItemsBackUp.add(new MusicItem(name, path, id, albumName, duration, size, artist, (int) addTime));

                    } while (cursor.moveToNext());
                    cursor.close();
                    mHandler.sendEmptyMessage(Values.HandlerWhat.INIT_MUSIC_LIST);
                    Values.MUSIC_DATA_INIT_DONE = true;
                } else {
                    mActivity.runOnUiThread(() -> Utils.Ui.fastToast(mActivity, "cursor == null or moveToFirst Fail"));
                }
            }

        }).start();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_list_layout, container, false);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mRecyclerView.setHasFixedSize(true);

        adapter = new MyRecyclerAdapter(Data.sMusicItems, mActivity);

        mRecyclerView.setAdapter(adapter);

//        needn't
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    sIsScrolling = true;
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (sIsScrolling) {
                        GlideApp.with(mActivity).resumeRequests();

                    }
                    sIsScrolling = false;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        CREATE_VIEW_DONE = true;
        return view;
    }

    /**
     * 延迟循环确认是否已经createView
     */
    private void sureCreateViewDone() {
        if (CREATE_VIEW_DONE) {
            mActivity.setToolbarSubTitle(Data.sMusicItems.size() + " Songs");
            adapter.notifyDataSetChanged();
        } else {
            new Handler().postDelayed(this::sureCreateViewDone, 1000);      //循环一秒
        }
    }

    class NotLeakHandler extends Handler {
        @SuppressWarnings("unused")
        private WeakReference<MusicListFragment> mWeakReference;

        NotLeakHandler(MusicListFragment fragment, Looper looper) {
            super(looper);
            mWeakReference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Values.HandlerWhat.INIT_MUSIC_LIST: {
                    mActivity.runOnUiThread(() -> {
                        if (CREATE_VIEW_DONE) {
                            sureCreateViewDone();
                            Log.d(TAG, "onStart: setAdapter done!");
                        }
                    });

                }
                default:
                    break;
            }
        }
    }

    public MyRecyclerAdapter getAdapter() {
        return adapter;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Override
    public void onDestroy() {
        mHandlerThread.quitSafely();
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

}
