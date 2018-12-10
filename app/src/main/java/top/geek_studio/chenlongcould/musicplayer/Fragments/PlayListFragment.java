/*
 * ************************************************************
 * 文件：PlayListFragment.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月10日 14:49:08
 * 上次修改时间：2018年12月10日 14:47:45
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.lang.ref.WeakReference;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.PublicActivity;
import top.geek_studio.chenlongcould.musicplayer.Adapters.PlayListAdapter;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.IStyle;
import top.geek_studio.chenlongcould.musicplayer.Models.PlayListItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.VisibleOrGone;

public final class PlayListFragment extends Fragment implements IStyle, VisibleOrGone {

    private static final String TAG = "PlayListFragment";

    public static final int RE_LOAD_PLAY_LIST = 80001;

    private ConstraintLayout mAddRecentItem;

    private ConstraintLayout mFavouriteMusic;

    private FastScrollRecyclerView mRecyclerView;

    private View mView;

    private TextView mNameRecent;

    private TextView mFavourite;

    private MainActivity mMainActivity;

    private Handler mHandler;

    private PlayListAdapter mPlayListAdapter;

    //实例化一个fragment
    public static PlayListFragment newInstance(int index) {
        return new PlayListFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainActivity = (MainActivity) getActivity();
        mHandler = new NotLeakHandler(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play_list, container, false);
        initView(view);
        initData();
        return view;
    }

    /**
     * load data
     */
    private void initData() {
        new MyLoadListTask(mMainActivity).execute();
    }

    public PlayListAdapter getPlayListAdapter() {
        return mPlayListAdapter;
    }

    public Handler getHandler() {
        return mHandler;
    }

    private void findId(View view) {
        mView = view.findViewById(R.id.view_line_one);
        mAddRecentItem = view.findViewById(R.id.fragment_play_list_add_recent);
        mFavouriteMusic = view.findViewById(R.id.fragment_play_list_play_favourite);
        mFavourite = view.findViewById(R.id.fragment_play_list_play_favourite_name);
        mNameRecent = view.findViewById(R.id.fragment_play_list_add_recent_name);
        mRecyclerView = view.findViewById(R.id.fragment_play_list_recycler);
    }

    private void initView(View view) {
        findId(view);

        initStyle();

        mPlayListAdapter = new PlayListAdapter(mMainActivity, Data.sPlayListItems);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mMainActivity));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mMainActivity, DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mPlayListAdapter);

        mAddRecentItem.setOnClickListener(v -> {
            Intent intent = new Intent(mMainActivity, PublicActivity.class);
            intent.putExtra("start_by", "add recent");
            startActivity(intent);
        });

        mFavouriteMusic.setOnClickListener(v -> {
            Intent intent = new Intent(mMainActivity, PublicActivity.class);
            intent.putExtra("start_by", "favourite music");
            startActivity(intent);
        });

    }

    /**
     * load playlist task
     */
    public static class MyLoadListTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<MainActivity> mWeakReference;

        public MyLoadListTask(MainActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Data.sPlayListItems.clear();
            Cursor cursor = mWeakReference.get().getContentResolver()
                    .query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    final String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.DATA));
                    final int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists._ID));
                    final String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME));
                    final long addTime = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.DATE_ADDED));

                    // TODO: 2018/12/10 M3U FILE
                    final File file = new File(filePath + ".m3u");

                    Data.sPlayListItems.add(new PlayListItem(id, name, filePath, addTime));
                } while (cursor.moveToNext());
                cursor.close();
            }
            return null;
        }
    }

    @Override
    public void initStyle() {
        if (Values.Style.NIGHT_MODE) {
            mView.setBackgroundColor(Color.parseColor("#7c7c7c"));
        } else {
            mView.setBackgroundColor(Color.parseColor("#e6e6e6"));
        }

        mNameRecent.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));
        mFavourite.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));

    }

    @Override
    public void visibleOrGone(int status) {
        mFavouriteMusic.setVisibility(status);
        mAddRecentItem.setVisibility(status);
    }

    static class NotLeakHandler extends Handler {
        @SuppressWarnings("unused")
        private WeakReference<PlayListFragment> mWeakReference;

        NotLeakHandler(PlayListFragment fragment) {
            mWeakReference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RE_LOAD_PLAY_LIST: {
                    mWeakReference.get().initData();
                    mWeakReference.get().getPlayListAdapter().notifyDataSetChanged();
                }
            }
        }
    }
}
