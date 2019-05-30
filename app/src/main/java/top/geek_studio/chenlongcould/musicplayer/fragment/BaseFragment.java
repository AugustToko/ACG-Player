package top.geek_studio.chenlongcould.musicplayer.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import top.geek_studio.chenlongcould.musicplayer.utils.PreferenceUtil;

/**
 * BaseFragment
 *
 * @author : chenlongcould
 * @date : 2019/04/17/22
 * @see top.geek_studio.chenlongcould.musicplayer.fragment
 */
public abstract class BaseFragment extends Fragment {

	private FragmentType fragmentType = FragmentType.BASE_FRAGMENT;

	protected SharedPreferences preferences;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onAttach(@NonNull Context context) {
		setFragmentType(fragmentType);
		super.onAttach(context);
		preferences = PreferenceUtil.getDefault(context);
	}

	public FragmentType getFragmentType() {
		return fragmentType;
	}

	abstract protected void setFragmentType(FragmentType fragmentType);

	/**
	 * fragment type for {@link #getFragment(int)}
	 */
	public enum FragmentType {
		BASE_FRAGMENT,
		MUSIC_LIST_FRAGMENT,
		MUSIC_DETAIL_FRAGMENT,
		MUSIC_DETAIL_FRAGMENT_LAND_SPACE,
		ALBUM_LIST_FRAGMENT,
		PLAY_LIST_FRAGMENT,
		ARTIST_FRAGMENT,
		FILE_VIEW_FRAGMENT
	}

	public abstract void reloadData();
}
