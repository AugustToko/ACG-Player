/*
 * ************************************************************
 * 文件：ReceiverOnMusicPlay.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月05日 09:30:08
 * 上次修改时间：2018年12月05日 09:06:23
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
import android.graphics.PorterDuff;
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

// fixme: 2018/11/28 need optimization
public final class ReceiverOnMusicPlay extends BroadcastReceiver {

    private static final String TAG = "ReceiverOnMusicPlay";

    public static final int TYPE_SHUFFLE = 90;

    private static boolean READY = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");

        if (!Utils.Ui.ANIMATION_IN_DETAIL_DONE) {
            Toast.makeText(context, "Wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        int type = intent.getIntExtra("play_type", -1);

        switch (type) {
            case -1: {
                break;
            }

            //clicked by notif
            case 2: {
                Data.sMusicBinder.playMusic();
            }
            break;

            //Type Random (play)
            case TYPE_SHUFFLE: {
                new ShufflePlayback().execute();
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

            //by auto-next(mediaPlayer OnCompletionListener) of next-play by user, at this time MainActivity is present
            case 4: {
                Data.sMusicBinder.resetMusic();
                int targetIndex = Values.CurrentData.CURRENT_MUSIC_INDEX + 1;
                if (targetIndex > Data.sMusicItems.size() - 1) {            //超出范围自动跳转0
                    targetIndex = 0;
                }

                //防止下一首歌曲与当前相同
                if (Data.sMusicItems.get(targetIndex).getMusicPath().equals(Values.CurrentData.CURRENT_SONG_PATH)) {
                    Data.sMusicBinder.seekTo(0);

                    final MainActivity activity = (MainActivity) Data.sActivities.get(0);
                    activity.getMusicDetailFragment().getHandler().sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);
                }

                Values.CurrentData.CURRENT_MUSIC_INDEX = targetIndex;
                Values.MUSIC_PLAYING = true;
                Values.HAS_PLAYED = true;
                Values.CurrentData.CURRENT_SONG_PATH = Data.sMusicItems.get(targetIndex).getMusicPath();

                final String path = Data.sMusicItems.get(targetIndex).getMusicPath();
                final String musicName = Data.sMusicItems.get(targetIndex).getMusicName();
                final String albumName = Data.sMusicItems.get(targetIndex).getMusicAlbum();

                Data.sHistoryPlayIndex.add(targetIndex);

                final Bitmap cover = Utils.Audio.getMp3Cover(path);

                Utils.Ui.setPlayButtonNowPlaying();

                if (Data.sActivities.size() >= 1) {
                    final MainActivity mainActivity = (MainActivity) Data.sActivities.get(0);
                    final MusicDetailFragment musicDetailFragment = mainActivity.getMusicDetailFragment();
                    musicDetailFragment.setSlideInfo(musicName, albumName, cover);
                    musicDetailFragment.setCurrentInfo(musicName, albumName, Utils.Audio.getAlbumByteImage(path));

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

                Data.saveGlobalCurrentData(musicName, albumName, cover);

                Values.CurrentData.CURRENT_SONG_PATH = path;

                try {
                    Data.sMusicBinder.setDataSource(Data.sMusicItems.get(targetIndex).getMusicPath());
                    Data.sMusicBinder.prepare();
                    Data.sMusicBinder.playMusic();

                    if (Data.sActivities.size() != 0) {
                        final MainActivity mainActivity = (MainActivity) Data.sActivities.get(0);
                        final MusicDetailFragment musicDetailFragment = mainActivity.getMusicDetailFragment();

                        musicDetailFragment.getHandler().sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);
                        musicDetailFragment.getHandler().sendEmptyMessage(Values.HandlerWhat.RECYCLER_SCROLL);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Data.sMusicBinder.resetMusic();
                }
            }
            break;

            //by MusicDetailFragment preview imageButton (view history song list)
            //当进度条大于播放总长 1/20 那么重新播放该歌曲
            case 5: {
                if (Data.sMusicBinder.getCurrentPosition() > Data.sMusicBinder.getDuration() / 20) {
                    Data.sMusicBinder.seekTo(0);
                } else {
                    if (READY) {

                        if (Data.sHistoryPlayIndex.size() == 1) {
                            Data.sMusicBinder.seekTo(0);
                            break;
                        }

                        READY = false;

                        Data.sMusicBinder.resetMusic();
                        final int tempSize = Data.sHistoryPlayIndex.size();

                        int index = Data.sHistoryPlayIndex.get(0);          //default val

                        switch (tempSize) {
                            case 0: {
                                Data.sMusicBinder.seekTo(0);
                            }
                            break;

                            case 1: {
                                Data.sMusicBinder.seekTo(0);
                            }
                            break;

                            default:
                                index = Data.sHistoryPlayIndex.get(tempSize - 2);
                                Data.sHistoryPlayIndex.remove(tempSize - 1);
                        }
                        final String path = Data.sMusicItems.get(index).getMusicPath();
                        final String musicName = Data.sMusicItems.get(index).getMusicName();
                        final String albumName = Data.sMusicItems.get(index).getMusicAlbum();
                        final Bitmap cover = Utils.Audio.getMp3Cover(path);

                        Data.saveGlobalCurrentData(musicName, albumName, cover);

                        Values.MUSIC_PLAYING = true;
                        Values.HAS_PLAYED = true;
                        Values.CurrentData.CURRENT_MUSIC_INDEX = index;
                        Values.CurrentData.CURRENT_SONG_PATH = path;

                        Utils.Ui.setPlayButtonNowPlaying();

                        if (Data.sActivities.size() >= 1) {
                            //UI sets
                            final MainActivity activity = (MainActivity) Data.sActivities.get(0);
                            final MusicDetailFragment musicDetailFragment = activity.getMusicDetailFragment();

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                musicDetailFragment.getSeekBar().setProgress(0, true);
                            } else {
                                musicDetailFragment.getSeekBar().setProgress(0);
                            }

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

                            musicDetailFragment.setSlideInfo(musicName, albumName, cover);
                            musicDetailFragment.setCurrentInfo(musicName, albumName, Utils.Audio.getAlbumByteImage(path));
                            musicDetailFragment.getHandler().sendEmptyMessage(Values.HandlerWhat.RECYCLER_SCROLL);

                        }

                        try {
                            Data.sMusicBinder.setDataSource(path);
                            Data.sMusicBinder.prepare();
                            Data.sMusicBinder.playMusic();

                            if (Data.sActivities.size() > 0) {
                                final MainActivity activity = (MainActivity) Data.sActivities.get(0);
                                MusicDetailFragment musicDetailFragment = activity.getMusicDetailFragment();
                                musicDetailFragment.getHandler().sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            Data.sMusicBinder.resetMusic();
                        }
                        READY = true;
                    }
                }

            }
            break;

            //by next button...(in detail or noti)
            case 6: {

                Values.BUTTON_PRESSED = true;

                if (Data.sActivities.size() != 0) {
                    final MusicDetailFragment musicDetailFragment = ((MainActivity) Data.sActivities.get(0)).getMusicDetailFragment();
                    //防止seekBar跳动到Max
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        musicDetailFragment.getSeekBar().setProgress(0, true);
                    } else {
                        musicDetailFragment.getSeekBar().setProgress(0);
                    }
                }

                if (Data.sNextWillPlayIndex != -1) {
                    new DoesHasNextPlay().execute();
                } else {
                    switch (Values.CurrentData.CURRENT_PLAY_TYPE) {
                        case Values.TYPE_RANDOM:
                            new ShufflePlayback().execute();
                            break;
                        case Values.TYPE_COMMON:
                            Utils.SendSomeThing.sendPlay(context, 4);
                            break;
                        default:
                            break;
                    }
                }

                Values.BUTTON_PRESSED = false;
            }
            break;

            case 7: {
                new DoesHasNextPlay().execute();
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

        String path;

        String musicName;

        String albumName;

        Bitmap cover;

        @Override
        protected Integer doInBackground(Void... voids) {
            if (Data.sNextWillPlayIndex != -1) {
                Data.sMusicBinder.resetMusic();

                path = Data.sMusicItems.get(Data.sNextWillPlayIndex).getMusicPath();
                musicName = Data.sMusicItems.get(Data.sNextWillPlayIndex).getMusicName();
                albumName = Data.sMusicItems.get(Data.sNextWillPlayIndex).getMusicAlbum();
                cover = Utils.Audio.getMp3Cover(path);

                Utils.Ui.setPlayButtonNowPlaying();

                Values.MUSIC_PLAYING = true;
                Values.HAS_PLAYED = true;
                Values.CurrentData.CURRENT_MUSIC_INDEX = Data.sNextWillPlayIndex;
                Values.CurrentData.CURRENT_SONG_PATH = path;

                Data.sNextWillPlayIndex = -1;

                try {
                    Data.sMusicBinder.setDataSource(path);
                    Data.sMusicBinder.prepare();
                    Data.sMusicBinder.playMusic();
                } catch (IOException e) {
                    e.printStackTrace();
                    Data.sMusicBinder.resetMusic();
                    return -1;
                }
            } else {
                return -1;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer status) {

            if (status != 0) return;

            final MainActivity mainActivity = (MainActivity) Data.sActivities.get(0);
            mainActivity.getMusicDetailFragment().setSlideInfo(musicName, albumName, cover);

            mainActivity.getMusicDetailFragment().setCurrentInfo(musicName, albumName, Utils.Audio.getAlbumByteImage(path));
            mainActivity.getMusicDetailFragment().getSeekBar().getThumb()
                    .setColorFilter(cover == null ? Color.WHITE : cover.getPixel(cover.getWidth() / 2, cover.getHeight() / 2), PorterDuff.Mode.SRC_ATOP);

        }
    }

    /**
     * play_type: random, without history clear (by nextButton or auto-next)
     */
    public static class ShufflePlayback extends AsyncTask<Void, Void, Integer> {

        String path;

        String musicName;

        String albumName;

        Bitmap cover;

        @Override
        protected Integer doInBackground(Void... voids) {
            READY = false;
            Data.sMusicBinder.resetMusic();

            //get data
            final Random random = new Random();
            final int index = random.nextInt(Data.sMusicItems.size() - 1);

            path = Data.sMusicItems.get(index).getMusicPath();
            musicName = Data.sMusicItems.get(index).getMusicName();
            albumName = Data.sMusicItems.get(index).getMusicAlbum();

            cover = Utils.Audio.getMp3Cover(path);

            Data.sHistoryPlayIndex.add(index);
            Values.CurrentData.CURRENT_MUSIC_INDEX = index;
            Values.MUSIC_PLAYING = true;
            Values.HAS_PLAYED = true;
            Values.CurrentData.CURRENT_MUSIC_INDEX = index;
            Values.CurrentData.CURRENT_SONG_PATH = path;

            Data.saveGlobalCurrentData(musicName, albumName, cover);

            try {
                Data.sMusicBinder.setDataSource(path);
                Data.sMusicBinder.prepare();
                Data.sMusicBinder.playMusic();          //has played, now playing
            } catch (IOException e) {
                e.printStackTrace();
                Data.sMusicBinder.resetMusic();
                return -1;
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer status) {

            if (status != 0) return;

            Utils.Ui.setPlayButtonNowPlaying();

            if (Data.sActivities.size() >= 1) {
                final MainActivity activity = (MainActivity) Data.sActivities.get(0);
                //music after mediaPlayer.setDataSource, because of "Values.HandlerWhat.INIT_SEEK_BAR"
                activity.getMusicDetailFragment().getHandler().sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);

                //first set backgroundImage, then set bg(layout) black. To crossFade more Smooth
                activity.getMusicDetailFragment().setSlideInfo(musicName, albumName, cover);
                activity.getMusicDetailFragment().setCurrentInfo(musicName, albumName, cover);
                //设置seekBar颜色
                activity.getMusicDetailFragment().getSeekBar().getThumb()
                        .setColorFilter(cover == null ? Color.GRAY : cover.getPixel(cover.getWidth() / 2, cover.getHeight() / 2), PorterDuff.Mode.SRC_ATOP);

            }

            READY = true;
        }
    }
}
