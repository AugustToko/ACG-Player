/*
 * ************************************************************
 * 文件：SplashActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月27日 13:11:38
 * 上次修改时间：2019年01月27日 13:08:44
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

/**
 * @author chenlongcould
 */
public final class SplashActivity extends BaseCompatActivity {

    private static final String TAG = "SplashActivity";

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Values.REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initDone();
                } else {
                    Utils.Ui.fastToast(this, "Failed to get permission, again!");
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Failed to get permission");
                    builder.setMessage("Try again?");
                    builder.setCancelable(false);
                    builder.setNegativeButton("Sure!", (dialog, which) -> initPermission());
                    builder.setNeutralButton("Cancel!", (dialog, which) -> {
                        dialog.dismiss();
                        finish();
                    });
                    builder.show();
                }
            }
            break;
            default:
        }
    }

    private void initDone() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_splash);
        } catch (Exception e) {
            Log.d(TAG, "onCreate: " + e.getMessage());
        }

        // Android 5.0 以上 全透明
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // 状态栏（以上几行代码必须，参考setStatusBarColor|setNavigationBarColor方法源码）
        window.setStatusBarColor(Color.TRANSPARENT);
        // 虚拟导航键
        window.setNavigationBarColor(Color.TRANSPARENT);

        if (initPermission()) {
            initDone();
        }
    }

    @Override
    public String getActivityTAG() {
        return TAG;
    }

    @Override
    public void inflateCommonMenu() {

    }

    @Override
    public void inflateChooseMenu() {

    }

}
