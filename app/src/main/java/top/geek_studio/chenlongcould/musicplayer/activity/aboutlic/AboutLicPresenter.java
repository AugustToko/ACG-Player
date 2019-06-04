package top.geek_studio.chenlongcould.musicplayer.activity.aboutlic;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author : chenlongcould
 * @date : 2019/06/04/12
 */
final public class AboutLicPresenter implements AboutLicContract.Presenter {

	private final AboutLicContract.View mView;

	private Disposable mDisposable;

	public AboutLicPresenter(@NonNull AboutLicContract.View view) {
		mView = view;
		mView.setPresenter(this);
	}

	@Override
	public void start() {
		mView.showLoad();
		loadLic();
	}

	private void loadLic() {
		Observable.create((ObservableOnSubscribe<String>) observableEmitter -> {
			try {
				final InputStream inputStream = mView.getAsset().open("Licenses");
				byte[] b = new byte[inputStream.available()];
				if (inputStream.read(b) != -1) {
					observableEmitter.onNext(new String(b));
					observableEmitter.onComplete();
				}
			} catch (IOException e) {
				e.printStackTrace();
				observableEmitter.onError(new Throwable("Load Licence Error"));
			}
		}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
			@Override
			public void onSubscribe(Disposable disposable) {
				mDisposable = disposable;
			}

			@Override
			public void onNext(String data) {
				mView.setContentText(data);
				mView.dismissLoad();
			}

			@Override
			public void onError(Throwable throwable) {
				mView.showLoad();
			}

			@Override
			public void onComplete() {

			}
		});
	}

	@Override
	public void close() {
		if (!mDisposable.isDisposed()) mDisposable.dispose();
	}
}
