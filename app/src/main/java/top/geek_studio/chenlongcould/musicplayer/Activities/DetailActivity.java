package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Database.Detail;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.databinding.ActivityDetailBinding;

public class DetailActivity extends MyBaseCompatActivity {

    private ActivityDetailBinding mDetailBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);
        super.initView(mDetailBinding.toolbar, mDetailBinding.appbar);

        setSupportActionBar(mDetailBinding.toolbar);
        mDetailBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        List<Detail> infos = LitePal.findAll(Detail.class);

        if (infos.size() != 0) {
            ArrayList<MusicItem> itemHelperList = new ArrayList<>();
            for (MusicItem item : Data.sMusicItems) {
                for (Detail info : infos) {
                    if (info.getMusicId() == item.getMusicID()) {
                        itemHelperList.add(item);
                    }
                }
            }

            MusicItem itemTimes = null;
            MusicItem itemDuration = null;
            MusicItem itemMinimum = null;
            int maxTimes = -1;
            int minimumTimes = -1;
            long maxDuration = -1;

            int totalDuration = 0;
            int totalTimes = 0;
            for (Detail info : infos) {
                if (info.getPlayTimes() > maxTimes) maxTimes = info.getPlayTimes();
                if (info.getMinimumPlayTimes() > minimumTimes)
                    minimumTimes = info.getMinimumPlayTimes();
                if (info.getPlayDuration() > maxDuration) maxDuration = info.getPlayDuration();
            }
            for (Detail detail : infos) {
                totalDuration += detail.getPlayDuration();
                totalTimes += detail.getPlayTimes();
                if (detail.getPlayTimes() == maxTimes) {
                    if (itemTimes == null) {
                        for (MusicItem item : itemHelperList) {
                            if (item.getMusicID() == detail.getMusicId()) {
                                itemTimes = item;
                                break;
                            }
                        }
                    }
                }

                if (detail.getPlayDuration() == maxDuration) {
                    if (itemDuration == null) {
                        for (MusicItem item : itemHelperList) {
                            if (item.getMusicID() == detail.getMusicId()) {
                                itemDuration = item;
                                break;
                            }
                        }
                    }
                }

                if (detail.getMinimumPlayTimes() == minimumTimes) {
                    if (itemMinimum == null) {
                        for (MusicItem item : itemHelperList) {
                            if (item.getMusicID() == detail.getMusicId()) {
                                itemMinimum = item;
                                break;
                            }
                        }
                    }
                }
            }

            mDetailBinding.includeContent.textTotalPlays.setText(mDetailBinding.includeContent.textTotalPlays.getText() + String.valueOf(totalTimes) + " times");
            mDetailBinding.includeContent.textTotalPlayTime.setText(mDetailBinding.includeContent.textTotalPlayTime.getText() + String.valueOf(Data.sSimpleDateFormat.format(new Date(totalDuration))) + " (mm:ss)");

            mDetailBinding.includeContent.t1.setText(mDetailBinding.includeContent.t1.getText() + String.valueOf(maxTimes) + " times");
            mDetailBinding.includeContent.t2.setText(mDetailBinding.includeContent.t2.getText() + String.valueOf(Data.sSimpleDateFormat.format(new Date(maxDuration))) + " (mm:ss)");
            mDetailBinding.includeContent.t3.setText(mDetailBinding.includeContent.t3.getText() + String.valueOf(minimumTimes) + " times");

            if (itemDuration != null) {
                mDetailBinding.includeContent.includeItemPlayDuration.recyclerItemMusicName.setText(itemDuration.getMusicName());
                mDetailBinding.includeContent.includeItemPlayDuration.recyclerItemMusicAlbumName.setText(itemDuration.getMusicAlbum());
                mDetailBinding.includeContent.includeItemPlayDuration.recyclerItemMusicDuration.setText(String.valueOf(Data.sSimpleDateFormat.format(new Date(itemDuration.getDuration()))));
                final String prefix = itemDuration.getMusicPath().substring(itemDuration.getMusicPath().lastIndexOf(".") + 1);
                mDetailBinding.includeContent.includeItemPlayDuration.recyclerItemMusicTypeName.setText(prefix);
                GlideApp.with(this).load(Utils.Audio.getCoverBitmap(this, itemDuration.getAlbumId()))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(mDetailBinding.includeContent.includeItemPlayDuration.recyclerItemAlbumImage);

                mDetailBinding.includeContent.includeItemPlayDuration.recyclerItemMenu.setVisibility(View.GONE);
            }

            if (itemMinimum != null) {
                mDetailBinding.includeContent.includeItemPlayMinitimes.recyclerItemMusicName.setText(itemMinimum.getMusicName());
                mDetailBinding.includeContent.includeItemPlayMinitimes.recyclerItemMusicAlbumName.setText(itemMinimum.getMusicAlbum());
                mDetailBinding.includeContent.includeItemPlayMinitimes.recyclerItemMusicDuration.setText(String.valueOf(Data.sSimpleDateFormat.format(new Date(itemMinimum.getDuration()))));
                final String prefix = itemMinimum.getMusicPath().substring(itemMinimum.getMusicPath().lastIndexOf(".") + 1);
                mDetailBinding.includeContent.includeItemPlayMinitimes.recyclerItemMusicTypeName.setText(prefix);
                GlideApp.with(this).load(Utils.Audio.getCoverBitmap(this, itemMinimum.getAlbumId()))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(mDetailBinding.includeContent.includeItemPlayMinitimes.recyclerItemAlbumImage);

                mDetailBinding.includeContent.includeItemPlayMinitimes.recyclerItemMenu.setVisibility(View.GONE);
            }

            if (itemTimes != null) {
                mDetailBinding.includeContent.includeItemPlaytimes.recyclerItemMusicName.setText(itemTimes.getMusicName());
                mDetailBinding.includeContent.includeItemPlaytimes.recyclerItemMusicAlbumName.setText(itemTimes.getMusicAlbum());
                mDetailBinding.includeContent.includeItemPlaytimes.recyclerItemMusicDuration.setText(String.valueOf(Data.sSimpleDateFormat.format(new Date(itemTimes.getDuration()))));
                final String prefix = itemTimes.getMusicPath().substring(itemTimes.getMusicPath().lastIndexOf(".") + 1);
                mDetailBinding.includeContent.includeItemPlaytimes.recyclerItemMusicTypeName.setText(prefix);
                GlideApp.with(this).load(Utils.Audio.getCoverBitmap(this, itemTimes.getAlbumId()))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(mDetailBinding.includeContent.includeItemPlaytimes.recyclerItemAlbumImage);

                mDetailBinding.includeContent.includeItemPlaytimes.recyclerItemMenu.setVisibility(View.GONE);
            }

        } else {
            Toast.makeText(this, "Database is empty!", Toast.LENGTH_SHORT).show();
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Clear Data?", Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.sure), v -> LitePal.deleteAll(Detail.class)).show());
    }

    @Override
    public void initStyle() {
        super.initStyle();
    }
}
