/*
 * ************************************************************
 * 文件：AlbumListFragment.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月27日 13:11:38
 * 上次修改时间：2019年01月27日 13:08:44
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.geeklibrary.VisibleOrGone;
import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Adapters.MyRecyclerAdapter2AlbumList;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.Models.AlbumItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class AlbumListFragment extends Fragment implements VisibleOrGone {

    public static final String TAG = "AlbumListFragment";

    /**
     * 用于检测此Fragment是否已经创建
     */
    public static boolean VIEW_HAS_LOAD = false;

    private RecyclerView mRecyclerView;

    private MainActivity mMainActivity;

    private MyRecyclerAdapter2AlbumList mAdapter2AlbumList;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_album_list, container, false);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            if (!VIEW_HAS_LOAD || Data.sAlbumItems.size() == 0) {
                initAlbumData();          //getData
                VIEW_HAS_LOAD = true;
            }
        }
    }

    @Override
    public void onDestroyView() {
        VIEW_HAS_LOAD = false;
        super.onDestroyView();
    }

    private void initAlbumData() {

        final AlertDialog load = Utils.Ui.getLoadingDialog(mMainActivity, "Loading");
        load.show();

        Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            if (Data.sAlbumItems.size() == 0) {
                Cursor cursor = mMainActivity.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    do {
                        String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM));
                        String albumId = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                        String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST));
                        Data.sAlbumItems.add(new AlbumItem(albumName, Integer.parseInt(albumId), artist));
                    } while (cursor.moveToNext());

                    cursor.close();
                }   //initData
            }

            emitter.onComplete();
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .safeSubscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        Data.sDisposables.add(disposable);
                    }

                    @Override
                    public void onNext(Integer result) {
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {
                        setRecyclerViewData();
                        load.dismiss();
                        mMainActivity.getMainBinding().toolBar.setSubtitle(Data.sAlbumItems.size() + " Album");
                    }
                });
        
    }

    /**
     * by firstStartApp, change Layout...
     */
    public void setRecyclerViewData() {

        if (getView() != null) {
            mRecyclerView = getView().findViewById(R.id.recycler_view);
            mRecyclerView.setHasFixedSize(true);

            //get type
            final SharedPreferences mDef = PreferenceManager.getDefaultSharedPreferences(getActivity());
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

            mAdapter2AlbumList = new MyRecyclerAdapter2AlbumList(mMainActivity, Data.sAlbumItems, type);
            mRecyclerView.setAdapter(mAdapter2AlbumList);
        }
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public MyRecyclerAdapter2AlbumList getAdapter2AlbumList() {
        return mAdapter2AlbumList;
    }

    @Override
    public void visibleOrGone(int status) {
        if (mRecyclerView != null) mRecyclerView.setVisibility(status);
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
