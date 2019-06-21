package top.geek_studio.chenlongcould.musicplayer.adapter;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import org.jetbrains.annotations.NotNull;
import top.geek_studio.chenlongcould.musicplayer.fragment.BaseFragment;

import java.util.List;

public final class MyPagerAdapter extends FragmentStatePagerAdapter {

	private static final String TAG = "MyPagerAdapter";
	private List<BaseFragment> mFragmentList;
	private List<String> mTitles;

	public MyPagerAdapter(FragmentManager fm, List<BaseFragment> fragmentList, List<String> titles) {
		super(fm, fragmentList.size());
		mFragmentList = fragmentList;
		mTitles = titles;
	}

	@NotNull
	@Override
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

	@Override
	public CharSequence getPageTitle(int position) {
		Log.d(TAG, "getPageTitle: " + position);
		return mTitles.get(position);
	}

}
