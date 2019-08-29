package top.geek_studio.chenlongcould.musicplayer.database;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;

/**
 * @author : chenlongcould
 * @date : 2019/08/29/15
 */
public class DataModel extends ViewModel {

	/**
	 * sCurrent DATA
	 */
	public MusicItem mCurrentMusicItem = null;

	public LinkedList<Disposable> mDisposables = new LinkedList<>();

	public List<MusicItem> mMusicItems = new ArrayList<>();

	public List<MusicItem> mMusicItemsBackUp = new ArrayList<>();

	/**
	 * save temp bitmap
	 * TODO 减少内存占用
	 */
	@Nullable
	public Bitmap currentCover = null;

	@Override
	protected void onCleared() {
		for (Disposable d : mDisposables) {
			if (d != null && !d.isDisposed()) d.dispose();
		}

		if (currentCover != null) {
			currentCover.recycle();
		}

		super.onCleared();
	}
}
