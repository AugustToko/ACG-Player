/*
 * ************************************************************
 * 文件：AboutLic.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月23日 11:17:30
 * 上次修改时间：2018年11月22日 14:53:00
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import top.geek_studio.chenlongcould.musicplayer.R;

public final class AboutLic extends Activity {

    private static final String TAG = "AboutLic";

    private Button close;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_lic);

        close = findViewById(R.id.close_button_activity_lic);

        close.setEnabled(true);
        close.setClickable(true);
        close.setOnClickListener(v -> finish());

        TextView textView = findViewById(R.id.show_lic_activity_lic);
        try {
            InputStream inputStream = getAssets().open("Licenses");
            byte[] b = new byte[inputStream.available()];
            if (inputStream.read(b) != -1) {
                String s = new String(b);
                textView.setText(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: done");
        super.onDestroy();
    }

    protected void finalize() throws Throwable {
        Log.d(TAG, "finalize: done!!!!");
        super.finalize();
    }
}
