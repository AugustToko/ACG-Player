package top.geek_studio.chenlongcould.musicplayer;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MusicListFragment extends Fragment {

    private static final String TAG = "MusicListFragment";

    /**------------------------- DATA -----------------------------*/
    private volatile ArrayList<String> mMusicPathList = new ArrayList<>();

    private volatile List<String> mSongNameList = new ArrayList<>();

    private volatile List<String> mSongAlbumList = new ArrayList<>();

    private RecyclerView mRecyclerView;

    private MainActivity mActivity;

    private NotLeakHandler mHandler;

    private HandlerThread mHandlerThread;

    private boolean CREATE_VIEW_DONE = false;

    private static boolean sIsScrolling = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mActivity = (MainActivity) getActivity();

        mHandlerThread = new HandlerThread("Handler Thread in MusicListFragment");
        mHandlerThread.start();
        mHandler = new NotLeakHandler(this, mHandlerThread.getLooper());

        // TODO: 2018/11/2 需要转为线程池的使用。
        new Thread(() -> {

            mMusicPathList.clear();
            mSongNameList.clear();
            mSongAlbumList.clear();

//            findMp3(file);

            Cursor cursor = mActivity.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                cursor.moveToFirst();
                do {
                    mMusicPathList.add(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
                    mSongNameList.add(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
                    mSongAlbumList.add(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
                } while (cursor.moveToNext());
                cursor.close();
            }

            mHandler.sendEmptyMessage(Values.INIT_MUSIC_LIST_DONE);

        }).start();

    }

    private void findMp3(File dir) {
        Log.d(TAG, "findMp3: doing");
        if (!dir.isDirectory()) {
            return;
        }

        for (File f:dir.listFiles()) {
            if (!f.isDirectory()) {
                mMusicPathList.add(f.getPath());

                String fileName = f.getName();
                String prefix = fileName.substring(fileName.lastIndexOf(".") + 1);//如果想获得不带点的后缀，变为fileName.lastIndexOf(".")+1

                String fileOtherName = fileName.substring(0, fileName.length() - prefix.length());//得到文件名。去掉了后缀
                mSongNameList.add(fileOtherName);

            } else {
                findMp3(f);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //实例化一个fragment
    public static MusicListFragment newInstance(int index) {

        MusicListFragment myFragment = new MusicListFragment();

        Bundle bundle = new Bundle();
        //传递参数
        bundle.putInt(Values.INDEX, index);
        myFragment.setArguments(bundle);
        return myFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_list_layout, container, false);
        mRecyclerView = view.findViewById(R.id.music_list_fragment_recycler);

//        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_SETTLING) {
//                    sIsScrolling = true;
//                    GlideApp.with(mActivity).pauseRequests();
//                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    if (sIsScrolling) {
//                        GlideApp.with(mActivity).resumeRequests();
//
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

        mRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mRecyclerView.setHasFixedSize(true);

        CREATE_VIEW_DONE = true;

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    //与 activity 的通信
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void sureCreateViewDone() {
        if (CREATE_VIEW_DONE) {
            mActivity.setToolbarSubTitle(mSongNameList.size() + " songs");

            MyRecyclerAdapter adapter = new MyRecyclerAdapter(mMusicPathList, mSongNameList, mSongAlbumList, mActivity, mHandlerThread.getLooper());
            mRecyclerView.setAdapter(adapter);
        } else {
            new Handler().postDelayed(this::sureCreateViewDone, 1000);
        }
    }

    class NotLeakHandler extends Handler {
        private WeakReference<MusicListFragment> mWeakReference;

        NotLeakHandler(MusicListFragment fragment,Looper looper) {
            super(looper);
            mWeakReference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Values.INIT_MUSIC_LIST_DONE: {
                    mActivity.runOnUiThread(() -> {
                        if (CREATE_VIEW_DONE) {
                            sureCreateViewDone();
                            Log.d(TAG, "onStart: setAdapter done!");
                        }

                    });

                }
                default:
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        mHandlerThread.quitSafely();
    }

}
