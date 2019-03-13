/*
 * ************************************************************
 * 文件：PublicFragment.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:46
 * 上次修改时间：2019年01月17日 17:29:00
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;

public class PublicFragment extends Fragment {

	//实例化一个fragment
	public static PublicFragment newInstance(int index) {
		PublicFragment myFragment = new PublicFragment();
		Bundle bundle = new Bundle();
		//传递参数
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
