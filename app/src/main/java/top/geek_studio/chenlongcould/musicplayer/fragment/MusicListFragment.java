package top.geek_studio.chenlongcould.musicplayer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.jetbrains.annotations.NotNull;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.databinding.FragmentMusicListBinding;

/**
 * @author chenlongcould
 */
public final class MusicListFragment extends BaseFragment {

	public static final String TAG = "MusicListFragment";
	private FragmentMusicListBinding mMusicListBinding;
	private MyRecyclerAdapter adapter;

	private MainActivity mActivity;

	public static MusicListFragment newInstance() {
		return new MusicListFragment();
	}

	@Override
	protected void setFragmentType(FragmentType fragmentType) {
		fragmentType = FragmentType.MUSIC_LIST_FRAGMENT;
	}

	@Override
	public void reloadData() {
		mActivity.reloadMusicItems();
	}

	@Override
	public void onAttach(@NotNull Context context) {
		super.onAttach(context);
		mActivity = (MainActivity) getActivity();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mMusicListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_list, container, false);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
		linearLayoutManager.setItemPrefetchEnabled(true);
		linearLayoutManager.setInitialPrefetchItemCount(6);
		mMusicListBinding.includeRecycler.recyclerView.setLayoutManager(linearLayoutManager);
		mMusicListBinding.includeRecycler.recyclerView.setHasFixedSize(true);
		adapter = new MyRecyclerAdapter(mActivity, Data.sMusicItems, new MyRecyclerAdapter.Config(0, true));
		mMusicListBinding.includeRecycler.recyclerView.setAdapter(adapter);

		return mMusicListBinding.getRoot();
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		if (adapter != null && !isVisibleToUser) {
			adapter.clearSelection();
		}
	}

	public final MyRecyclerAdapter getAdapter() {
		return adapter;
	}

	public FragmentMusicListBinding getMusicListBinding() {
		return mMusicListBinding;
	}

}
