/*
 * ************************************************************
 * 文件：ReceiverOnMusicPlay.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月25日 18:47:45
 * 上次修改时间：2018年11月25日 17:47:56
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
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.MusicDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class ReceiverOnMusicPlay extends BroadcastReceiver {
    private static final String TAG = "ReceiverOnMusicPlay";

    public static final int TYPE_SHUFFLE = 90;

    private boolean READY = true;

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
                return;
            }

            case 0: {
                Data.sMusicBinder.playMusic();
            }
            break;

            //Type Random
            case TYPE_SHUFFLE: {
                Utils.Audio.shufflePlayback();
            }
            break;

            /*
             * must by MusicDetailActivity, just resume play
             * */
            case 2: {
                Data.sMusicBinder.playMusic();
                if (Data.sActivities.size() >= 1) {
                    Utils.HandlerSend.sendToMain(Values.HandlerWhat.SET_MAIN_BUTTON_PLAY);
                }
                if (Data.sActivities.size() >= 2) {
                    MusicDetailActivity musicDetailActivity = (MusicDetailActivity) Data.sActivities.get(1);
                    musicDetailActivity.setButtonTypePlay();
                    MusicDetailActivity.NotLeakHandler notLeakHandler = musicDetailActivity.getHandler();
                    notLeakHandler.sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);
                }
            }
            break;

            /*
             * must by MainActivity, just resume play
             * */
            case 3: {
                Data.sMusicBinder.playMusic();
                Utils.HandlerSend.sendToMain(Values.HandlerWhat.SET_MAIN_BUTTON_PLAY);
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
                    MusicDetailActivity a = (MusicDetailActivity) Data.sActivities.get(1);
                    Data.sMusicBinder.seekTo(0);
                    a.getHandler().sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);
                }

                Values.CurrentData.CURRENT_MUSIC_INDEX = targetIndex;
                Values.MUSIC_PLAYING = true;
                Values.HAS_PLAYED = true;
                Values.CurrentData.CURRENT_SONG_PATH = Data.sMusicItems.get(targetIndex).getMusicPath();

                String path = Data.sMusicItems.get(targetIndex).getMusicPath();
                String musicName = Data.sMusicItems.get(targetIndex).getMusicName();
                String albumName = Data.sMusicItems.get(targetIndex).getMusicAlbum();

                Data.sHistoryPlayIndex.add(targetIndex);

                Bitmap cover = Utils.Audio.getMp3Cover(path);

                Utils.Ui.setPlayButtonNowPlaying();

                if (Data.sActivities.size() >= 1) {
                    MainActivity mainActivity = (MainActivity) Data.sActivities.get(0);
                    mainActivity.setCurrentSongInfo(musicName, albumName, path, cover);
                }

                if (Data.sActivities.size() >= 2) {
                    MusicDetailActivity musicDetailActivity = (MusicDetailActivity) Data.sActivities.get(1);
                    musicDetailActivity.setCurrentSongInfo(musicName, albumName, Utils.Audio.getAlbumByteImage(path));

                    //seekBar(Thumb) change color
                    musicDetailActivity.getSeekBar().getThumb().setColorFilter(cover.getPixel(cover.getWidth() / 2, cover.getHeight() / 2), PorterDuff.Mode.SRC_ATOP);
                }

                Data.saveGlobalCurrentData(musicName, albumName, cover);

                Values.CurrentData.CURRENT_SONG_PATH = path;

                try {
                    Data.sMusicBinder.setDataSource(Data.sMusicItems.get(targetIndex).getMusicPath());
                    Data.sMusicBinder.prepare();
                    Data.sMusicBinder.playMusic();

                    if (Data.sActivities.size() >= 2) {
                        MusicDetailActivity musicDetailActivity = (MusicDetailActivity) Data.sActivities.get(1);
                        MusicDetailActivity.NotLeakHandler notLeakHandler = musicDetailActivity.getHandler();
                        notLeakHandler.sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Data.sMusicBinder.resetMusic();
                }
            }
            break;

            //by MusicDetailActivity preview imageButton (view history song list)
            //当进度条大于播放总长 1/20 那么重新播放该歌曲
            case 5: {
                if (Data.sMusicBinder.getCurrentPosition() > Data.sMusicBinder.getDuration() / 20) {
                    Data.sMusicBinder.seekTo(0);
                } else {
                    if (READY) {

                        if (Data.sActivities.size() == 1) {
                            Data.sMusicBinder.seekTo(0);
                            return;
                        }

                        READY = false;

                        Data.sMusicBinder.resetMusic();
                        int tempSize = Data.sHistoryPlayIndex.size();

                        int index = Data.sHistoryPlayIndex.get(0);
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
                        String path = Data.sMusicItems.get(index).getMusicPath();
                        String musicName = Data.sMusicItems.get(index).getMusicName();
                        String albumName = Data.sMusicItems.get(index).getMusicAlbum();
                        Bitmap cover = Utils.Audio.getMp3Cover(path);

                        Data.saveGlobalCurrentData(musicName, albumName, cover);

                        Values.MUSIC_PLAYING = true;
                        Values.HAS_PLAYED = true;
                        Values.CurrentData.CURRENT_MUSIC_INDEX = index;
                        Values.CurrentData.CURRENT_SONG_PATH = path;

                        Utils.Ui.setPlayButtonNowPlaying();

                        //UI sets
                        if (Data.sActivities.size() >= 2) {
                            MusicDetailActivity activity = (MusicDetailActivity) Data.sActivities.get(1);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                activity.getSeekBar().setProgress(0, true);
                            } else {
                                activity.getSeekBar().setProgress(0);
                            }
                            MainActivity mainActivity = (MainActivity) Data.sActivities.get(0);
                            mainActivity.setCurrentSongInfo(musicName, albumName, path, cover);
                            activity.setCurrentSongInfo(musicName, albumName, Utils.Audio.getAlbumByteImage(path));
                            activity.getSeekBar().getThumb().setColorFilter(cover.getPixel(cover.getWidth() / 2, cover.getHeight() / 2), PorterDuff.Mode.SRC_ATOP);
                            activity.getRecyclerView().scrollToPosition(index);
                        }

                        try {
                            Data.sMusicBinder.setDataSource(path);
                            Data.sMusicBinder.prepare();
                            Data.sMusicBinder.playMusic();

                            if (Data.sActivities.size() >= 2) {
                                MusicDetailActivity activity = (MusicDetailActivity) Data.sActivities.get(1);
                                activity.mHandler.sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);
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
                if (Data.sActivities.size() == 2) {
                    MusicDetailActivity activity = (MusicDetailActivity) Data.sActivities.get(1);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        activity.getSeekBar().setProgress(0, true);            //防止seekBar跳动到Max
                    } else {
                        activity.getSeekBar().setProgress(0);            //防止seekBar跳动到Max
                    }
                }

                if (Data.sNextWillPlayIndex != -1) {
                    Utils.Audio.doesNextHasMusic();
                    return;
                }

                switch (Values.CurrentData.CURRENT_PLAY_TYPE) {
                    case Values.TYPE_RANDOM:
                        Utils.Audio.shufflePlayback();
                        break;
                    case Values.TYPE_COMMON:
                        Utils.SendSomeThing.sendPlay(context, 4);
                        break;
                    default:
                        break;
                }
                Values.BUTTON_PRESSED = false;
            }
        }
    }
}
