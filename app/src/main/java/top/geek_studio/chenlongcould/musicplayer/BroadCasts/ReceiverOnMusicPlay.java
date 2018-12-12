/*
 * ************************************************************
 * 文件：ReceiverOnMusicPlay.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月12日 11:57:29
 * 上次修改时间：2018年12月11日 15:08:10
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.BroadCasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Message;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Fragments.MusicDetailFragment;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class ReceiverOnMusicPlay extends BroadcastReceiver {

    private static final String TAG = "ReceiverOnMusicPlay";

    /**
     * @see Message#what
     */
    public static final int TYPE_SHUFFLE = 90;

    /**
     * the mediaPlayer is Ready?
     */
    public static boolean READY = true;

    /**
     * set SeekBar Color
     *
     * @param cover               AlbumImage
     * @param musicDetailFragment Fragment
     */
    private static void setSeekBarColor(Bitmap cover, MusicDetailFragment musicDetailFragment) {
        //seekBarColor set
        final Message setColor = Message.obtain();
        setColor.what = MusicDetailFragment.SET_SEEK_BAR_COLOR;
        if (cover != null) {
            Palette.from(cover).generate(palette -> {

                if (palette != null) {
                    setColor.arg1 = palette.getVibrantColor(cover.getPixel(cover.getWidth() / 2, cover.getHeight() / 2));
                } else {
                    setColor.arg1 = cover.getPixel(cover.getWidth() / 2, cover.getHeight() / 2);
                }

                if (setColor.arg1 == Color.WHITE) setColor.arg1 = Color.BLACK;

                musicDetailFragment.getHandler().sendMessage(setColor);
            });
        } else {
            setColor.arg1 = Color.WHITE;
            musicDetailFragment.getHandler().sendMessage(setColor);
        }
    }

    /**
     * setSeekBar
     */
    private static void reSetSeekBar() {
        final MusicDetailFragment musicDetailFragment = ((MainActivity) Data.sActivities.get(0)).getMusicDetailFragment();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            musicDetailFragment.getSeekBar().setProgress(0, true);
        } else {
            musicDetailFragment.getSeekBar().setProgress(0);
        }
    }

    /**
     * setButtonPlay
     * setSeekBarColor
     * setSeekBarPosition
     * setSlideBar
     *
     * @param musicDetailFragment fragment
     * @param targetIndex         index
     */
    private static void uiSet(final MusicDetailFragment musicDetailFragment, final int targetIndex) {
        final String path = Data.sPlayOrderList.get(targetIndex).getMusicPath();
        final String musicName = Data.sPlayOrderList.get(targetIndex).getMusicName();
        final String albumName = Data.sPlayOrderList.get(targetIndex).getMusicAlbum();
        final Bitmap cover = Utils.Audio.getMp3Cover(path);
        Data.saveGlobalCurrentData(musicName, albumName, cover);

        Utils.Ui.setPlayButtonNowPlaying();
        setSeekBarColor(cover, musicDetailFragment);
        musicDetailFragment.setSlideInfo(musicName, albumName, cover);
        musicDetailFragment.setCurrentInfo(musicName, albumName, cover);

        reSetSeekBar();         //防止seekBar跳动到Max
        musicDetailFragment.getHandler().sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);
        musicDetailFragment.getHandler().sendEmptyMessage(Values.HandlerWhat.RECYCLER_SCROLL);
    }

    /**
     * @param targetIndex index
     */
    private static void saveCurrentData(int targetIndex) {
        final String path = Data.sPlayOrderList.get(targetIndex).getMusicPath();
        final String musicName = Data.sPlayOrderList.get(targetIndex).getMusicName();
        final String albumName = Data.sPlayOrderList.get(targetIndex).getMusicAlbum();
        final Bitmap cover = Utils.Audio.getMp3Cover(path);
        Data.saveGlobalCurrentData(musicName, albumName, cover);
    }

    /**
     * setFlags
     *
     * @param targetIndex index
     */
    private static void setFlags(int targetIndex) {
        Values.CurrentData.CURRENT_MUSIC_INDEX = targetIndex;
        Values.MUSIC_PLAYING = true;
        Values.HAS_PLAYED = true;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!Utils.Ui.ANIMATION_IN_DETAIL_DONE) {
            Toast.makeText(context, "Wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        int type = intent.getIntExtra("play_type", 0);

        Log.d(TAG, "onReceive: " + type);

        switch (type) {
            case 0: {
                break;
            }

            //clicked by notif
            case 2: {
                Data.sMusicBinder.playMusic();
                Utils.Ui.setPlayButtonNowPlaying();
            }
            break;

            //Type Random (play)
            case TYPE_SHUFFLE: {
                if (!READY) break;
                new FastShufflePlayback().execute();
            }
            break;

            /*
             * just resume play (form pause to play...)
             * */
            case 3: {
                Data.sMusicBinder.playMusic();
                final MainActivity activity = (MainActivity) Data.sActivities.get(0);
                activity.getMusicDetailFragment().getHandler().sendEmptyMessage(Values.HandlerWhat.SET_BUTTON_PLAY);
            }
            break;

            case 4:
            case 5:

                //by next button...(in detail or noti) (must ActivityList isn't empty)
                //by auto-next(mediaPlayer OnCompletionListener) of next-play by user, at this time MainActivity is present
                //by MusicDetailFragment preview imageButton (view history song list)
            case 6: {
                if (!READY) break;

                //检测是否指定下一首播放
                if (Data.sNextWillPlayItem != null) {
                    new DoesHasNextPlay().execute();
                    break;
                }

                //检测前后播放
                int targetIndex = 0;
                if (intent.getStringExtra("args") != null) {
                    if (intent.getStringExtra("args").contains("next")) {
                        Log.d(TAG, "onReceive: next");
                        targetIndex = Values.CurrentData.CURRENT_MUSIC_INDEX + 1;
                        if (targetIndex > Data.sPlayOrderList.size() - 1) {            //超出范围自动跳转0
                            targetIndex = 0;
                        }
                    } else if (intent.getStringExtra("args").contains("previous")) {
                        Log.d(TAG, "onReceive: previous");
                        targetIndex = Values.CurrentData.CURRENT_MUSIC_INDEX - 1;
                        if (targetIndex < 0) {
                            targetIndex = Data.sPlayOrderList.size() - 1;      //超出范围超转最后
                        }
                    }
                }
                Values.CurrentData.CURRENT_MUSIC_INDEX = targetIndex;

                Data.sMusicBinder.resetMusic();

                try {
                    Data.sMusicBinder.setDataSource(Data.sPlayOrderList.get(targetIndex).getMusicPath());
                    Data.sMusicBinder.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                    Data.sMusicBinder.resetMusic();
                }

                setFlags(targetIndex);

                if (!Data.sActivities.isEmpty()) {
                    final MusicDetailFragment musicDetailFragment = ((MainActivity) Data.sActivities.get(0)).getMusicDetailFragment();

                    //slide 滑动切歌无需再次加载albumCover
                    if (intent.getStringExtra("args") != null && intent.getStringExtra("args").contains("slide")) {
                        final String path = Data.sPlayOrderList.get(targetIndex).getMusicPath();
                        final String musicName = Data.sPlayOrderList.get(targetIndex).getMusicName();
                        final String albumName = Data.sPlayOrderList.get(targetIndex).getMusicAlbum();
                        final Bitmap cover = Utils.Audio.getMp3Cover(path);
                        Data.saveGlobalCurrentData(musicName, albumName, cover);
                        Utils.Ui.setPlayButtonNowPlaying();
                        setSeekBarColor(cover, musicDetailFragment);
                        musicDetailFragment.setSlideInfo(musicName, albumName, cover);
                        musicDetailFragment.setCurrentInfoWithoutMainImage(musicName, albumName, Utils.Audio.getAlbumByteImage(path));
                        reSetSeekBar();         //防止seekBar跳动到Max
                        musicDetailFragment.getHandler().sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);
                        musicDetailFragment.getHandler().sendEmptyMessage(Values.HandlerWhat.RECYCLER_SCROLL);
                    } else {
                        uiSet(musicDetailFragment, targetIndex);
                    }
                }

                Data.sMusicBinder.playMusic();

                READY = true;
            }
            break;

        }

        //after type set
        if (!Data.sActivities.isEmpty()) {
            ((MainActivity) Data.sActivities.get(0)).getMusicDetailFragment().getHandler().sendEmptyMessage(Values.HandlerWhat.RECYCLER_SCROLL);
        }

    }

    /**
     * 当下一首歌曲存在(被手动指定时), auto-next-play and next-play will call this method
     */
    public static class DoesHasNextPlay extends AsyncTask<Void, Void, Integer> {

        Bitmap cover;

        @Override
        protected Integer doInBackground(Void... voids) {
            if (Data.sNextWillPlayItem != null) {
                Data.sMusicBinder.resetMusic();

                cover = Utils.Audio.getMp3Cover(Data.sNextWillPlayItem.getMusicPath());

                Utils.Ui.setPlayButtonNowPlaying();

                Values.MUSIC_PLAYING = true;
                Values.HAS_PLAYED = true;

                try {
                    Data.sMusicBinder.setDataSource(Data.sNextWillPlayItem.getMusicPath());
                    Data.sMusicBinder.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                    Data.sMusicBinder.resetMusic();
                    return -1;
                }

                Data.sMusicBinder.playMusic();

            } else {
                return -1;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer status) {

            if (status != 0) return;

            if (!Data.sActivities.isEmpty()) {

                MusicDetailFragment musicDetailFragment = ((MainActivity) Data.sActivities.get(0)).getMusicDetailFragment();

                final Bitmap cover = Utils.Audio.getMp3Cover(Data.sNextWillPlayItem.getMusicPath());
                Data.saveGlobalCurrentData(Data.sNextWillPlayItem.getMusicName(), Data.sNextWillPlayItem.getMusicAlbum(), cover);

                Utils.Ui.setPlayButtonNowPlaying();
                setSeekBarColor(cover, musicDetailFragment);
                musicDetailFragment.setSlideInfo(Data.sNextWillPlayItem.getMusicName(), Data.sNextWillPlayItem.getMusicAlbum(), cover);
                musicDetailFragment.setCurrentInfo(Data.sNextWillPlayItem.getMusicName(), Data.sNextWillPlayItem.getMusicAlbum(), cover);

                reSetSeekBar();         //防止seekBar跳动到Max
                musicDetailFragment.getHandler().sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);
                musicDetailFragment.getHandler().sendEmptyMessage(Values.HandlerWhat.RECYCLER_SCROLL);
            }

            Data.sNextWillPlayItem = null;
        }
    }

    /**
     * play_type: random, without history clear (by nextButton or auto-next)\
     * just make a random Index (data by {@link Data#sPlayOrderList})
     */
    public static class FastShufflePlayback extends AsyncTask<String, Void, Integer> {

        String path;

        String musicName;

        String albumName;

        Bitmap cover;

        int index;

        @Override
        protected Integer doInBackground(String... strings) {
            READY = false;
            Data.sMusicBinder.resetMusic();

            if (Data.sPlayOrderList.isEmpty()) return -1;

            //get data
            final Random random = new Random();
            final int index = random.nextInt(Data.sPlayOrderList.size());
            this.index = index;
            Values.CurrentData.CURRENT_MUSIC_INDEX = index;

            path = Data.sPlayOrderList.get(index).getMusicPath();
            musicName = Data.sPlayOrderList.get(index).getMusicName();
            albumName = Data.sPlayOrderList.get(index).getMusicAlbum();
            cover = Utils.Audio.getMp3Cover(path);

            setFlags(index);

            Data.saveGlobalCurrentData(musicName, albumName, cover);

            try {
                Data.sMusicBinder.setDataSource(path);
                Data.sMusicBinder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
                Data.sMusicBinder.resetMusic();
                return -1;
            }

            Data.sMusicBinder.playMusic();          //has played, now playing

            return 0;
        }

        @Override
        protected void onPostExecute(Integer status) {
            if (status != 0) return;

            if (Data.sActivities.size() >= 1) {
                uiSet(((MainActivity) Data.sActivities.get(0)).getMusicDetailFragment(), index);
            }

            READY = true;
        }
    }

}
