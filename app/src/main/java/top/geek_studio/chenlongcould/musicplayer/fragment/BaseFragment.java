package top.geek_studio.chenlongcould.musicplayer.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

	private static final String TAG = "BaseFragment";

	protected SharedPreferences preferences;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		preferences = PreferenceUtil.getDefault(context);
	}

	/**
	 * Get fragment type
	 */
	public abstract FragmentType getFragmentType();

	/**
	 * override this method, and call {@code super.reloadData()}
	 */
	public void reloadData() {
		Log.d(TAG, "reloadData: reloading... " + getFragmentType());
	}

	/**
	 * fragment type for {@link #getFragmentType()}
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
	};
}
