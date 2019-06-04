package top.geek_studio.chenlongcould.musicplayer.activity.aboutlic;

import android.content.res.AssetManager;
import androidx.annotation.NonNull;
import top.geek_studio.chenlongcould.musicplayer.BasePresenter;
import top.geek_studio.chenlongcould.musicplayer.BaseView;

/**
 * @author : chenlongcould
 * @date : 2019/06/04/12
 */
public interface AboutLicContract {

	interface View extends BaseView<Presenter> {

		/**
		 * show {@link androidx.appcompat.app.AlertDialog}
		 */
		void showLoad();

		/**
		 * close {@link androidx.appcompat.app.AlertDialog}
		 */
		void dismissLoad();

		void setContentText(@NonNull final String content);

		AssetManager getAsset();
	}

	interface Presenter extends BasePresenter {

		/**
		 * 关闭 {@link Presenter} 以释放资源，例如 {@link io.reactivex.disposables.Disposable}
		 */
		void close();
	}

}
