/*
 * ************************************************************
 * 文件：PlayListFragment.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月27日 13:11:38
 * 上次修改时间：2019年01月27日 13:08:50
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Models.PlayListItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.PublicActivity;
import top.geek_studio.chenlongcould.musicplayer.adapter.PlayListAdapter;
import top.geek_studio.chenlongcould.musicplayer.databinding.FragmentPlaylistBinding;

public final class PlayListFragment extends Fragment {

    public static final String TAG = "PlayListFragment";

    public static final String ACTION_ADD_RECENT = "add recent";
    public static final String ACTION_FAVOURITE = "favourite music";
    public static final String ACTION_HISTORY = "play history";
    public static final String ACTION_TRASH_CAN = "trash can";
    public static final String ACTION_PLAY_LIST_ITEM = "play_list_item";
    /**
     * by playlist item clicked
     */

    public static final int RE_LOAD_PLAY_LIST = 80001;

    private FragmentPlaylistBinding mPlayListBinding;

    private MainActivity mMainActivity;

    private Handler mHandler;

    private PlayListAdapter mPlayListAdapter;

    //实例化一个fragment
    public static PlayListFragment newInstance() {
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
        mPlayListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_playlist, container, false);

        mPlayListBinding.addRecent.setOnClickListener(v -> {
            Intent intent = new Intent(mMainActivity, PublicActivity.class);
            intent.putExtra(PublicActivity.INTENT_START_BY, ACTION_ADD_RECENT);
            startActivity(intent);
        });

        mPlayListBinding.favourite.setOnClickListener(v -> {
            Intent intent = new Intent(mMainActivity, PublicActivity.class);
            intent.putExtra(PublicActivity.INTENT_START_BY, ACTION_FAVOURITE);
            startActivity(intent);
        });

        mPlayListBinding.history.setOnClickListener(v -> {
            Intent intent = new Intent(mMainActivity, PublicActivity.class);
            intent.putExtra(PublicActivity.INTENT_START_BY, ACTION_HISTORY);
            startActivity(intent);
        });

        mPlayListBinding.trashCan.setOnClickListener(v -> {
            Intent intent = new Intent(mMainActivity, PublicActivity.class);
            intent.putExtra(PublicActivity.INTENT_START_BY, ACTION_TRASH_CAN);
            startActivity(intent);
        });

        mPlayListBinding.recentName.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));
        mPlayListBinding.favouriteName.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));
        mPlayListBinding.historyName.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));

        mPlayListBinding.recyclerView.setLayoutManager(new LinearLayoutManager(mMainActivity));
        mPlayListBinding.recyclerView.setHasFixedSize(true);
        mPlayListBinding.recyclerView.addItemDecoration(Data.getItemDecoration(mMainActivity));

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

//    @Override
//    public void visibleOrGone(int status) {
//        if (mPlayListBinding.favourite != null) mPlayListBinding.favourite.setVisibility(status);
//        if (mPlayListBinding.addRecent != null) mPlayListBinding.addRecent.setVisibility(status);
//    }

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
