/*
 * ************************************************************
 * 文件：LicTools.java  模块：geeklibrary  项目：MusicPlayer
 * 当前修改时间：2019年01月16日 20:44:58
 * 上次修改时间：2019年01月16日 08:12:04
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.geeklibrary.ActivityTools;

import android.app.Activity;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.geeklibrary.DialogUtil;
import top.geek_studio.chenlongcould.geeklibrary.R;

public class LicTools {

    private Activity mActivity;

    private View mRoot;

    private Toolbar mToolbar;

    public LicTools(@NonNull Activity activity, @NonNull String licFileName) {
        mActivity = activity;
        mRoot = LayoutInflater.from(activity).inflate(R.layout.activity_about_lic, null);
        mToolbar = mRoot.findViewById(R.id.toolbar);
        mToolbar.setTitle("License");
        mToolbar.setNavigationOnClickListener(v -> mActivity.finish());

        mRoot.findViewById(R.id.close_button).setOnClickListener(v -> mActivity.onBackPressed());

        final AlertDialog load = DialogUtil.getLoadingDialog(mActivity, "Loading...");
        load.show();

        final TextView textView = mRoot.findViewById(R.id.license_content);

        Observable.create((ObservableOnSubscribe<String>) observableEmitter -> {
            try {
                InputStream inputStream = mActivity.getAssets().open(licFileName);
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
                Toast.makeText(mActivity, throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {

            }
        });
    }

    public void colorSet(@ColorInt int primary) {
        mToolbar.setBackgroundColor(primary);
        mRoot.findViewById(R.id.appbar).setBackgroundColor(primary);
    }

    public View getRoot() {
        return mRoot;
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

}
