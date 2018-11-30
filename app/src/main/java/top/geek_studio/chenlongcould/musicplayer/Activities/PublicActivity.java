/*
 * ************************************************************
 * 文件：PublicActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月30日 20:36:09
 * 上次修改时间：2018年11月30日 18:45:38
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;

import top.geek_studio.chenlongcould.musicplayer.Adapters.MyRecyclerAdapter;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;

public class PublicActivity extends MyBaseActivity {

    private AppBarLayout mAppBarLayout;

    private Toolbar mToolbar;

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recent);

        mRecyclerView = findViewById(R.id.activity_add_recent_recycler);
        mAppBarLayout = findViewById(R.id.app_bar_layout);
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());

        Utils.Ui.setAppBarColor(this, mAppBarLayout, mToolbar);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        String type = getIntent().getStringExtra("start_by");
        switch (type) {
            case "add recent": {
                // TODO: 2018/11/11  add recent
                mToolbar.setTitle(getResources().getString(R.string.add_recent));
                ArrayList<MusicItem> musicItems = new ArrayList<>(Data.sMusicItems);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    musicItems.sort((o1, o2) -> {
                        if (o1 == null || o2 == null) {
                            return 0;
                        }
                        return Integer.compare(o1.getAddTime(), o2.getAddTime());
                    });
                } else {
                    // TODO: 2018/11/22 quickSort
                    System.exit(0);
                }
                MyRecyclerAdapter adapter = new MyRecyclerAdapter(musicItems, this);
                mRecyclerView.setAdapter(adapter);
            }
            break;
            case "favourite music": {
                // TODO: 2018/11/11  favourite music
                mToolbar.setTitle(getResources().getString(R.string.my_favourite));
            }
            break;
            default:
        }
    }

    @Override
    public void onBackPressed() {
        mRecyclerView.stopScroll();
        super.onBackPressed();
    }
}
