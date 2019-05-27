package top.geek_studio.chenlongcould.musicplayer.fragment;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * BaseFragment
 *
 * @author : chenlongcould
 * @date : 2019/04/17/22
 * @see top.geek_studio.chenlongcould.musicplayer.fragment
 */
public abstract class BaseFragment extends Fragment {

	private FragmentType fragmentType = FragmentType.BASE_FRAGMENT;

	@Override
	public void onAttach(@NonNull Context context) {
		setFragmentType(fragmentType);
		super.onAttach(context);
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
