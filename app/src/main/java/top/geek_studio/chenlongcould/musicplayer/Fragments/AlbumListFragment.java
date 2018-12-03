/*
 * ************************************************************
 * 文件：AlbumListFragment.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月03日 15:10:53
 * 上次修改时间：2018年12月03日 15:10:19
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.ViewPreloadSizeProvider;

import java.lang.ref.WeakReference;
import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Adapters.MyRecyclerAdapter2AlbumList;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.Models.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.VisibleOrGone;

public final class AlbumListFragment extends Fragment implements VisibleOrGone {

    public static final String TAG = "AlbumListFragment";

    private static boolean sIsScrolling = false;

    public static boolean HAS_LOAD = false;

    public final static int RE_LOAD = 40001;

    private RecyclerView mRecyclerView;

    private MyRecyclerAdapter2AlbumList mMyRecyclerAdapter2AlbumList;

    private Handler mHandler;

    private MainActivity mMainActivity;

    private boolean ON_CREATE_VIEW_DONE = false;


    //实例化一个fragment
    public static AlbumListFragment newInstance() {
        return new AlbumListFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new NotLeakHandler(this, mMainActivity.getLooper());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_album_list, container, false);
    }

    @Override
    public void onResume() {
        ON_CREATE_VIEW_DONE = true;
        super.onResume();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            if (!HAS_LOAD || Data.sAlbumItems.size() == 0) {
                sureGetDataDone();          //getData
                HAS_LOAD = true;
            }
            //...
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void sureGetDataDone() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                Cursor cursor = mMainActivity.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    do {
                        String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM));
                        String albumId = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                        Data.sAlbumItems.add(new AlbumItem(albumName, Integer.parseInt(albumId)));
                    } while (cursor.moveToNext());

                    cursor.close();
                }

                mHandler.sendEmptyMessage(Values.HandlerWhat.GET_DATA_DONE);
                return null;
            }
        }.execute();
    }

    private void setRecyclerViewData() {
        ViewPreloadSizeProvider<AlbumItem> preloadSizeProvider = new ViewPreloadSizeProvider<>();
        AlbumListFragment.MyPreloadModelProvider preloadModelProvider = new AlbumListFragment.MyPreloadModelProvider();
        RecyclerViewPreloader<AlbumItem> preLoader = new RecyclerViewPreloader<>(this, preloadModelProvider, preloadSizeProvider, 2);

        assert getView() != null;
        mRecyclerView = getView().findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(preLoader);

        //get type
        SharedPreferences mDef = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int type = mDef.getInt(Values.SharedPrefsTag.ALBUM_LIST_DISPLAY_TYPE, MyRecyclerAdapter2AlbumList.GRID_TYPE);
        switch (type) {
            case MyRecyclerAdapter2AlbumList.LINEAR_TYPE: {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            }
            break;
            case MyRecyclerAdapter2AlbumList.GRID_TYPE: {
                mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), mDef.getInt(Values.SharedPrefsTag.ALBUM_LIST_GRID_TYPE_COUNT, 2)));
            }
            break;
        }

        mMyRecyclerAdapter2AlbumList = new MyRecyclerAdapter2AlbumList(mMainActivity, Data.sAlbumItems, type);
        mRecyclerView.setAdapter(mMyRecyclerAdapter2AlbumList);
    }

    private void sureCreateViewDone() {
        if (ON_CREATE_VIEW_DONE) {
            mMainActivity.runOnUiThread(() -> {

                setRecyclerViewData();

                Message message = Message.obtain();
                message.what = MainActivity.SET_TOOLBAR_SUBTITLE;
                message.obj = String.valueOf(Data.sAlbumItems.size() + " Albums");
                mMainActivity.getHandler().sendMessage(message);
            });
        } else {
            new Handler().postDelayed(this::sureGetDataDone, 500);
        }
    }

    @SuppressWarnings("unused")
    public Handler getNotLeakHandler() {
        return mHandler;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Override
    public void visibleOrGone(int status) {
        mRecyclerView.setVisibility(status);
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

                case RE_LOAD: {
                    setRecyclerViewData();
                }
                break;
                default:
                    break;
            }
        }
    }

    public class MyPreloadModelProvider implements ListPreloader.PreloadModelProvider<AlbumItem> {
        @NonNull
        @Override
        public List<AlbumItem> getPreloadItems(int position) {
            return Data.sAlbumItems.subList(position, position + 1);
        }

        @Nullable
        @Override
        public RequestBuilder<?> getPreloadRequestBuilder(@NonNull AlbumItem item) {
            return GlideApp.with(mMainActivity).load(item);
        }
    }

}
