/*
 * ************************************************************
 * 文件：MyPagerAdapter.java  模块：app  项目：Weather
 * 当前修改时间：2018年09月25日 17:12:14
 * 上次修改时间：2018年09月25日 15:51:14
 * 作者：GiKode
 * Geek Studio: https://blog.geek-studio.top
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;

import java.util.List;

public class MyPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = "MyPagerAdapter";
    private List<Fragment> mFragmentList;
    private List<String> mTitles;

    MyPagerAdapter(FragmentManager fm, List<Fragment> fragmentList, List<String> titles) {
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
