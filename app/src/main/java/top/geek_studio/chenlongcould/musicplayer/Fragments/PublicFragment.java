/*
 * ************************************************************
 * 文件：PublicFragment.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月05日 17:54:16
 * 上次修改时间：2018年11月05日 17:53:35
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;

public class PublicFragment extends Fragment {

    //实例化一个fragment
    public static PublicFragment newInstance(int index) {
        PublicFragment myFragment = new PublicFragment();

        Bundle bundle = new Bundle();
        //传递参数
        // TODO: 2018/9/28
        bundle.putInt(Values.INDEX, index);
        myFragment.setArguments(bundle);
        return myFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_public_fragment_layout, container, false);

        TextView textView = view.findViewById(R.id.text_view_frag);

        if (this.getArguments() != null) {
            textView.setText(String.valueOf(this.getArguments().getInt(Values.INDEX, -1)));
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
