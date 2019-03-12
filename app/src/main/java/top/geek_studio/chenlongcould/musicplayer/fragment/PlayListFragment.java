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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
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
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Models.PlayListItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.PublicActivity;
import top.geek_studio.chenlongcould.musicplayer.adapter.PlayListAdapter;
import top.geek_studio.chenlongcould.musicplayer.databinding.FragmentPlaylistBinding;
import top.geek_studio.chenlongcould.musicplayer.utils.MusicUtil;

public final class PlayListFragment extends Fragment {

    public static final String TAG = "PlayListFragment";

    public static final String ACTION_ADD_RECENT = "add recent";
    public static final String ACTION_FAVOURITE = "favourite music";
    public static final String ACTION_HISTORY = "play history";
    public static final String ACTION_TRASH_CAN = "trash can";
    public static final String ACTION_PLAY_LIST_ITEM = "play_list_item";

    private LocalBroadcastManager mBroadcastManager;
    private BroadcastReceiver mRefreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mPlayListAdapter.notifyDataSetChanged();
        }
    };


    public static final int RE_LOAD_PLAY_LIST = 80001;

    private FragmentPlaylistBinding mPlayListBinding;

    private MainActivity mMainActivity;

    private Handler mHandler;

    private PlayListAdapter mPlayListAdapter;

    /**
     * 实例化 {@link PlayListFragment}
     */
    public static PlayListFragment newInstance() {
        return new PlayListFragment();
    }

    /**
     * receive broadcast
     *
     * @see PlayListFragment#mBroadcastManager
     * @see PlayListFragment.ItemChange#ACTION_REFRESH_LIST
     */
    private void receiveItemChange() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ItemChange.ACTION_REFRESH_LIST);
        mBroadcastManager.registerReceiver(mRefreshReceiver, intentFilter);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mMainActivity = (MainActivity) getActivity();
        mHandler = new NotLeakHandler(this);

        mBroadcastManager = LocalBroadcastManager.getInstance(context);
        receiveItemChange();
    }

    /**
     * load data
     */
    private void initData() {
        final PlayListItem item = MusicUtil.getFavoritesPlaylist(mMainActivity);

        Disposable disposable = Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            Data.sPlayListItems.clear();
            Cursor cursor = mMainActivity.getContentResolver()
                    .query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    final int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists._ID));
                    if (item != null && item.getId() == id) {
                        //匹配到喜爱列表 跳过
                        continue;
                    }
                    final String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME));
                    final String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.DATA));
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
        Data.sDisposables.add(disposable);
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

        mPlayListBinding.recentName.setTextColor(ContextCompat.getColor(mMainActivity, R.color.title_color));
        mPlayListBinding.favouriteName.setTextColor(ContextCompat.getColor(mMainActivity, R.color.title_color));
        mPlayListBinding.historyName.setTextColor(ContextCompat.getColor(mMainActivity, R.color.title_color));

        mPlayListBinding.recyclerView.setLayoutManager(new LinearLayoutManager(mMainActivity));
        mPlayListBinding.recyclerView.setHasFixedSize(true);
        mPlayListBinding.recyclerView.addItemDecoration(Data.getItemDecoration(mMainActivity));

        initData();
        return mPlayListBinding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBroadcastManager.unregisterReceiver(mRefreshReceiver);
    }

    public PlayListAdapter getPlayListAdapter() {
        return mPlayListAdapter;
    }

    public Handler getHandler() {
        return mHandler;
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

    /**
     * for intent use
     */
    public interface ItemChange {
        /**
         * for broadcasts
         * */
        String ACTION_REFRESH_LIST = "ACTION_REFRESH_LIST";
    }
}
