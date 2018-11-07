/*
 * ************************************************************
 * 文件：MyPagerAdapter.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月05日 17:54:16
 * 上次修改时间：2018年11月05日 17:53:35
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Adapters;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;

import java.util.List;

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
        // TODO Auto-generated method stub
        return PagerAdapter.POSITION_NONE;
    }


    public CharSequence getPageTitle(int position) {
        Log.d(TAG, "getPageTitle: " + position);
        return mTitles.get(position);
    }
}
