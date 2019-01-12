/*
 * ************************************************************
 * 文件：ReceiverOnMusicPlay.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月12日 20:26:06
 * 上次修改时间：2019年01月12日 08:23:20
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.BroadCasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Fragments.MusicDetailFragment;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class ReceiverOnMusicPlay extends BroadcastReceiver {

    @SuppressWarnings("unused")
    private static final String TAG = "ReceiverOnMusicPlay";

    /**
     * @see Message#what
     */
    public static final int TYPE_SHUFFLE = 90;

    /**
     * the mediaPlayer is Ready?
     */
    public static AtomicBoolean READY = new AtomicBoolean(true);

    /**
     * set SeekBar Color
     *
     * @param cover               AlbumImage
     * @param musicDetailFragment Fragment
     */
    // TODO: 2019/1/11 reset this
    private static void setSeekBarColor(Bitmap cover, MusicDetailFragment musicDetailFragment) {
//        if (cover != null) {
//            Palette.from(cover).generate(palette -> {
//
//                if (palette != null) {
//                    musicDetailFragment.getSeekBar().getProgressDrawable().setTint(palette.getVibrantColor(Color.WHITE));
//                    musicDetailFragment.getSeekBar().getThumb().setTint(palette.getVibrantColor(Color.WHITE));
//                } else {
//                    musicDetailFragment.getSeekBar().getProgressDrawable().setTint(Color.WHITE);
//                    musicDetailFragment.getSeekBar().getThumb().setTint(Color.WHITE);
//                }
//
//            });
//        }
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
        final Bitmap cover = Utils.Audio.getMp3Cover(path, musicDetailFragment.getContext());
        Data.setCurrentCover(cover);

        Utils.Ui.setPlayButtonNowPlaying();
        musicDetailFragment.setSlideInfo(musicName, albumName, cover);
        musicDetailFragment.setCurrentInfo(musicName, albumName, cover);

        reSetSeekBar();         //防止seekBar跳动到Max
        musicDetailFragment.getHandler().sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);
        musicDetailFragment.getHandler().sendEmptyMessage(Values.HandlerWhat.RECYCLER_SCROLL);
    }

    /**
     * setFlags
     * @param targetIndex index
     */
    private static void setFlags(int targetIndex) {
        Values.CurrentData.CURRENT_MUSIC_INDEX = targetIndex;
    }

    public static void playMusic() {
        Values.HAS_PLAYED = true;
        try {
            Data.sMusicBinder.setCurrentMusicData(Data.sCurrentMusicItem);
            Data.sMusicBinder.playMusic();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void pauseMusic() {
        try {
            Data.sMusicBinder.pauseMusic();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void resetMusic() {
        try {
            Data.sMusicBinder.resetMusic();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void prepare() {
        try {
            Data.sMusicBinder.prepare();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void setDataSource(String path) {
        Data.sHistoryPlay.add(Data.sCurrentMusicItem);
        try {
            Data.sMusicBinder.setDataSource(path);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static int getDuration() {
        try {
            return Data.sMusicBinder.getDuration();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean isPlayingMusic() {
        try {
            return Data.sMusicBinder.isPlayingMusic();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getCurrentPosition() {
        try {
            return Data.sMusicBinder.getCurrentPosition();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void seekTo(int nowPosition) {
        try {
            Data.sMusicBinder.seekTo(nowPosition);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void stopMusic() {
        try {
            Data.sMusicBinder.stopMusic();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void sureCar() {
        //set data (image and name)
        if (Values.CurrentData.CURRENT_UI_MODE.equals(Values.CurrentData.MODE_CAR)) {
            Data.sCarViewActivity.getFragmentLandSpace().setData();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!Utils.Ui.ANIMATION_IN_DETAIL_DONE.get() && Values.CurrentData.CURRENT_UI_MODE.equals(Values.CurrentData.MODE_COMMON)) {
            Toast.makeText(context, "Wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        int type = intent.getIntExtra("play_type", 0);

        Log.d(TAG, "onReceive: type: " + type);

        switch (type) {
            case 0:
                break;

            //clicked by notif
            case 2: {
                Utils.Ui.setPlayButtonNowPlaying();
                playMusic();
            }
            break;

            //Type Random (play)
            case TYPE_SHUFFLE: {
                if (!READY.get()) break;
                new FastShufflePlayback().execute();
            }
            break;

            /*
             * just resume play (form pause to play...)
             * */
            case 3: {
                playMusic();
                final MainActivity activity = (MainActivity) Data.sActivities.get(0);
                activity.getMusicDetailFragment().getHandler().sendEmptyMessage(Values.HandlerWhat.SET_BUTTON_PLAY);
            }
            break;

            //by next button...(in detail or noti) (must ActivityList isn't empty)
            //by auto-next(mediaPlayer OnCompletionListener) of next-play by user, at this time MainActivity is present
            //by MusicDetailFragment preview imageButton (view history song list)
            case 6: {
                Log.d(TAG, "onReceive: do 6");

                //检测是否指定下一首播放
                if (Data.sNextWillPlayItem != null) {
                    new DoesHasNextPlay().execute();
                    break;
                }

                if (Values.CurrentData.CURRENT_AUTO_NEXT_TYPE.equals(Values.TYPE_REPEAT_ONE)) {
                    Log.d(TAG, "onReceive: repeat one");
                    seekTo(0);
                    playMusic();
                    break;
                }

                //检测前后播放
                int targetIndex = 0;
                if (intent.getStringExtra("args") != null) {
                    if (intent.getStringExtra("args").contains("next")) {
                        targetIndex = Values.CurrentData.CURRENT_MUSIC_INDEX + 1;
                        if (targetIndex > Data.sPlayOrderList.size() - 1) {            //超出范围自动跳转0
                            targetIndex = 0;
                        }
                    } else if (intent.getStringExtra("args").contains("previous")) {
                        targetIndex = Values.CurrentData.CURRENT_MUSIC_INDEX - 1;
                        if (targetIndex < 0) {
                            targetIndex = Data.sPlayOrderList.size() - 1;      //超出范围超转最后
                        }
                    }
                }
                Values.CurrentData.CURRENT_MUSIC_INDEX = targetIndex;

                Data.setCurrentMusicItem(Data.sPlayOrderList.get(targetIndex));

                resetMusic();
                setDataSource(Data.sPlayOrderList.get(targetIndex).getMusicPath());
                prepare();

                setFlags(targetIndex);

                //load data
                if (!Data.sActivities.isEmpty()) {
                    final MusicDetailFragment musicDetailFragment = ((MainActivity) Data.sActivities.get(0)).getMusicDetailFragment();

                    //slide 滑动切歌无需再次加载albumCover
                    if (intent.getStringExtra("args") != null && intent.getStringExtra("args").contains("slide")) {
                        final String path = Data.sPlayOrderList.get(targetIndex).getMusicPath();
                        final String musicName = Data.sPlayOrderList.get(targetIndex).getMusicName();
                        final String albumName = Data.sPlayOrderList.get(targetIndex).getMusicAlbum();
                        final Bitmap cover = Utils.Audio.getMp3Cover(path, musicDetailFragment.getContext());
                        Data.setCurrentCover(cover);
                        Utils.Ui.setPlayButtonNowPlaying();
                        musicDetailFragment.setSlideInfo(musicName, albumName, cover);
                        musicDetailFragment.setCurrentInfoWithoutMainImage(musicName, albumName, Utils.Audio.getAlbumByteImage(path, context));
                        reSetSeekBar();         //防止seekBar跳动到Max

                        musicDetailFragment.getHandler().sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);
                        musicDetailFragment.getHandler().sendEmptyMessage(Values.HandlerWhat.RECYCLER_SCROLL);
                    } else {
                        uiSet(musicDetailFragment, targetIndex);
                    }
                    sureCar();
                }

                playMusic();

            }
            break;
            default:
        }

        //after type set
        if (!Data.sActivities.isEmpty()) {
            ((MainActivity) Data.sActivities.get(0)).getMusicDetailFragment().getHandler().sendEmptyMessage(Values.HandlerWhat.RECYCLER_SCROLL);
            ((MainActivity) Data.sActivities.get(0)).getMainBinding().slidingLayout.setTouchEnabled(true);
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
                resetMusic();

                cover = Utils.Audio.getMp3Cover(Data.sNextWillPlayItem.getMusicPath());

                Utils.Ui.setPlayButtonNowPlaying();

                ReceiverOnMusicPlay.setDataSource(Data.sNextWillPlayItem.getMusicPath());
                prepare();

                playMusic();

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

                final Bitmap cover = Utils.Audio.getMp3Cover(Data.sNextWillPlayItem.getMusicPath(), musicDetailFragment.getContext());
                Data.setCurrentCover(cover);
                sureCar();

                Utils.Ui.setPlayButtonNowPlaying();
                musicDetailFragment.setSlideInfo(Data.sNextWillPlayItem.getMusicName(), Data.sNextWillPlayItem.getMusicAlbum(), cover);
                musicDetailFragment.setCurrentInfo(Data.sNextWillPlayItem.getMusicName(), Data.sNextWillPlayItem.getMusicAlbum(), cover);

                if (cover != null) cover.recycle();

                reSetSeekBar();         //防止seekBar跳动到Max
                musicDetailFragment.getHandler().sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);
                musicDetailFragment.getHandler().sendEmptyMessage(Values.HandlerWhat.RECYCLER_SCROLL);
            }

            Data.sNextWillPlayItem = null;

//            READY = true;
        }
    }

    /**
     * play_type: random (by nextButton or auto-next)
     * just make a random Index (data by {@link Data#sPlayOrderList})
     */
    public static class FastShufflePlayback extends AsyncTask<String, Void, Integer> {

        String path;

        int index;

        @Override
        protected Integer doInBackground(String... strings) {
            if (Data.sPlayOrderList.isEmpty()) return -1;

            READY.set(false);
            resetMusic();

            //get data
            final Random random = new Random();
            final int index = random.nextInt(Data.sPlayOrderList.size());
            this.index = index;
            Values.CurrentData.CURRENT_MUSIC_INDEX = index;
            Data.setCurrentMusicItem(Data.sPlayOrderList.get(index));

            path = Data.sPlayOrderList.get(index).getMusicPath();

            setFlags(index);

            setDataSource(path);
            prepare();
            playMusic();

            return 0;
        }

        @Override
        protected void onPostExecute(Integer status) {
            if (status != 0) return;

            if (Data.sActivities.size() >= 1) {
                uiSet(((MainActivity) Data.sActivities.get(0)).getMusicDetailFragment(), index);
                sureCar();
            }

            READY.set(true);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            READY.set(true);
        }
    }

}
