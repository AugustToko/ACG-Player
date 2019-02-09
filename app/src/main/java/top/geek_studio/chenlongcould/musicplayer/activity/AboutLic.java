/*
 * ************************************************************
 * 文件：AboutLic.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月18日 18:58:29
 * 上次修改时间：2019年01月18日 18:57:39
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

import androidx.appcompat.app.AlertDialog;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

public final class AboutLic extends MyBaseCompatActivity {

    private static final String TAG = "AboutLic";

    private Button close;

    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_lic);

        close = findViewById(R.id.close_button_activity_lic);

        close.setEnabled(true);
        close.setClickable(true);
        close.setOnClickListener(v -> finish());

        final TextView textView = findViewById(R.id.show_lic_activity_lic);

        final AlertDialog load = Utils.Ui.getLoadingDialog(this, "Loading...");
        load.show();

        Observable.create((ObservableOnSubscribe<String>) observableEmitter -> {
            try {
                InputStream inputStream = getAssets().open("Licenses");
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
                textView.setText(data);
                load.dismiss();
            }

            @Override
            public void onError(Throwable throwable) {
                load.dismiss();
                throwable.printStackTrace();
                Toast.makeText(AboutLic.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    protected void onStop() {
        mDisposable.dispose();
        super.onStop();
    }
}
