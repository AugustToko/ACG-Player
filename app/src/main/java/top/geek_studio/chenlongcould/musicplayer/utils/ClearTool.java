package top.geek_studio.chenlongcould.musicplayer.utils;

import androidx.annotation.NonNull;
import io.reactivex.disposables.Disposable;

import java.util.List;

/**
 * clear data(s)
 *
 * @author : chenlongcould
 * @date : 2019/06/04/13
 */
public class ClearTool {
	public static void clearDisposables(@NonNull final List<Disposable> disposables) {
		for (final Disposable disposable : disposables) {
			if (!disposable.isDisposed()) disposable.dispose();
		}
	}
}
