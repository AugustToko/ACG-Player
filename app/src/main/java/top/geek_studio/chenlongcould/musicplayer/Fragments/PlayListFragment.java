/*
 * ************************************************************
 * 文件：PlayListFragment.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:46
 * 上次修改时间：2019年01月17日 17:29:00
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.lang.ref.WeakReference;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.geeklibrary.Theme.IStyle;
import top.geek_studio.chenlongcould.geeklibrary.VisibleOrGone;
import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.PublicActivity;
import top.geek_studio.chenlongcould.musicplayer.Adapters.PlayListAdapter;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Models.PlayListItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.databinding.FragmentPlayListBinding;

public final class PlayListFragment extends Fragment implements IStyle, VisibleOrGone {

    public static final String TAG = "PlayListFragment";

    public static final int RE_LOAD_PLAY_LIST = 80001;

    private FragmentPlayListBinding mPlayListBinding;

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
        mPlayListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_play_list, container, false);

        mPlayListBinding.addRecent.setOnClickListener(v -> {
            Intent intent = new Intent(mMainActivity, PublicActivity.class);
            intent.putExtra("start_by", "add recent");
            startActivity(intent);
        });

        mPlayListBinding.favourite.setOnClickListener(v -> {
            Intent intent = new Intent(mMainActivity, PublicActivity.class);
            intent.putExtra("start_by", "favourite music");
            startActivity(intent);
        });

        mPlayListBinding.history.setOnClickListener(v -> {
            Intent intent = new Intent(mMainActivity, PublicActivity.class);
            intent.putExtra("start_by", "play history");
            startActivity(intent);
        });

        initStyle();

        mPlayListBinding.recyclerView.setLayoutManager(new LinearLayoutManager(mMainActivity));
        mPlayListBinding.recyclerView.setHasFixedSize(true);
        mPlayListBinding.recyclerView.addItemDecoration(new DividerItemDecoration(mMainActivity, DividerItemDecoration.VERTICAL));

        initData();
        return mPlayListBinding.getRoot();
    }

    /**
     * load data
     */
    @SuppressLint("CheckResult")
    private void initData() {
        Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            Data.sPlayListItems.clear();
            Cursor cursor = mMainActivity.getContentResolver()
                    .query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    final String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME));

                    if (name.equals("Favourite List")) {
                        continue;
                    }

                    final String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.DATA));
                    final int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists._ID));
                    final long addTime = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.DATE_ADDED));

                    // TODO: 2018/12/10 M3U FILE
                    final File file = new File(filePath + ".m3u");

                    Data.sPlayListItems.add(new PlayListItem(id, name, filePath, addTime));
                } while (cursor.moveToNext());
                cursor.close();
            }

            //done
            emitter.onNext(0);
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (integer == 0) {

                        //load recyclerView
                        mPlayListAdapter = new PlayListAdapter(mMainActivity, Data.sPlayListItems);
                        mPlayListBinding.recyclerView.setAdapter(mPlayListAdapter);
                    }
                });
    }

    public PlayListAdapter getPlayListAdapter() {
        return mPlayListAdapter;
    }

    public Handler getHandler() {
        return mHandler;
    }

    @Override
    public void initStyle() {
        mPlayListBinding.recentName.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));
        mPlayListBinding.favouriteName.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));
        mPlayListBinding.historyName.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));
    }

    @Override
    public void visibleOrGone(int status) {
        if (mPlayListBinding.favourite != null) mPlayListBinding.favourite.setVisibility(status);
        if (mPlayListBinding.addRecent != null) mPlayListBinding.addRecent.setVisibility(status);
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
