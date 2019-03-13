/*
 * ************************************************************
 * 文件：MyPagerAdapter.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:46
 * 上次修改时间：2019年01月17日 17:29:00
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.adapter;

import android.util.Log;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

public final class MyPagerAdapter extends FragmentStatePagerAdapter {

	private static final String TAG = "MyPagerAdapter";
	private List<Fragment> mFragmentList;
	private List<String> mTitles;

	public MyPagerAdapter(FragmentManager fm, List<Fragment> fragmentList, List<String> titles) {
		super(fm);
		mFragmentList = fragmentList;
		mTitles = titles;
	}

	public Fragment getItem(int position) {
		return mFragmentList.get(position);
	}

	@Override
	public int getCount() {
		return mFragmentList.size();
	}

	@Override
	public int getItemPosition(@NonNull Object object) {
		return PagerAdapter.POSITION_NONE;
	}

	public CharSequence getPageTitle(int position) {
		Log.d(TAG, "getPageTitle: " + position);
		return mTitles.get(position);
	}
}
