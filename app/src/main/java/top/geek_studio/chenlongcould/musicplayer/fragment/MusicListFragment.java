package top.geek_studio.chenlongcould.musicplayer.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.jetbrains.annotations.NotNull;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.MusicService;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.adapter.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.databinding.FragmentMusicListBinding;
import top.geek_studio.chenlongcould.musicplayer.utils.MusicUtil;

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
	public FragmentType getFragmentType() {
		return FragmentType.MUSIC_LIST_FRAGMENT;
	}

	@Override
	public void reloadData() {
		super.reloadData();
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
		adapter = new MyRecyclerAdapter(mActivity, Data.sMusicItems
				, new MyRecyclerAdapter.Config(preferences.getInt(Values.SharedPrefsTag
				.RECYCLER_VIEW_ITEM_STYLE, 0), true));
		mMusicListBinding.includeRecycler.recyclerView.setAdapter(adapter);
		loadData();

		return mMusicListBinding.getRoot();
	}

	private void loadData() {
		Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
			if (!MusicUtil.loadDataSource(mActivity)) {
				emitter.onNext(-1);
			} else {
				Data.sPlayOrderList.addAll(Data.sMusicItems);
				Data.sPlayOrderListBackup.addAll(Data.sMusicItems);
				emitter.onNext(0);
			}
		}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).safeSubscribe(new Observer<Integer>() {
			@Override
			public final void onSubscribe(Disposable disposable) {
				Data.sDisposables.add(disposable);
			}

			@Override
			public final void onNext(Integer result) {
				if (result == -1) {
					AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
					builder.setTitle("Error")
							.setMessage("Can not find any music or the cursor is null, Will exit.")
							.setCancelable(false)
							.setNegativeButton("OK", (dialog, which) -> dialog.cancel());
					builder.show();
				}

				final Intent intent = new Intent(mActivity, MusicService.class);
				mActivity.startService(intent);
				mActivity.bindService(intent, mActivity.sServiceConnection, Context.BIND_AUTO_CREATE);
			}

			@Override
			public final void onError(Throwable throwable) {
				Toast.makeText(mActivity, throwable.getMessage(), Toast.LENGTH_SHORT).show();
			}

			@Override
			public final void onComplete() {

			}
		});

	}

	public final MyRecyclerAdapter getAdapter() {
		return adapter;
	}

	public FragmentMusicListBinding getMusicListBinding() {
		return mMusicListBinding;
	}

}
