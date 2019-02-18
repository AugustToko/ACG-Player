/*
 * ************************************************************
 * 文件：ReceiverOnMusicPlay.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月27日 13:11:38
 * 上次修改时间：2019年01月27日 13:08:44
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.litepal.LitePal;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.Nullable;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.database.Detail;
import top.geek_studio.chenlongcould.musicplayer.fragment.MusicDetailFragment;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

public final class ReceiverOnMusicPlay extends BroadcastReceiver {

    @SuppressWarnings("unused")
    private static final String TAG = "ReceiverOnMusicPlay";

    //最短播放时间为3000毫秒

    /**
     * @see Message#what
     */
    public static final int CASE_TYPE_SHUFFLE = 90;
    public static final int CASE_TYPE_ITEM_CLICK = 15;
    public static final int CASE_TYPE_NOTIFICATION_RESUME = 2;

    public static final String INTENT_PLAY_TYPE = "play_type";

    public static final String TYPE_NEXT = "next";
    public static final String TYPE_PREVIOUS = "previous";
    public static final String TYPE_SLIDE = "slide";

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
        final String musicName = Data.sPlayOrderList.get(targetIndex).getMusicName();
        final String albumName = Data.sPlayOrderList.get(targetIndex).getMusicAlbum();
        final Bitmap cover = Utils.Audio.getCoverBitmap(musicDetailFragment.getActivity(), Data.sPlayOrderList.get(targetIndex).getAlbumId());

        Utils.Ui.setPlayButtonNowPlaying();
        musicDetailFragment.setCurrentInfo(musicName, albumName, cover);

        reSetSeekBar();         //防止seekBar跳动到Max
        musicDetailFragment.getHandler().sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);
        musicDetailFragment.getHandler().sendEmptyMessage(Values.HandlerWhat.RECYCLER_SCROLL);
        ((MainActivity) Data.sActivities.get(0)).getMainBinding().slidingLayout.setTouchEnabled(true);
    }

    /**
     * setFlags
     *
     * @param targetIndex index
     */
    private static void setFlags(int targetIndex) {
        Values.CurrentData.CURRENT_MUSIC_INDEX = targetIndex;
    }

    public static void playMusic() {
        try {
            Data.sMusicBinder.setCurrentMusicData(Data.sCurrentMusicItem);
            Data.sMusicBinder.playMusic();
            Data.HAS_PLAYED = true;
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

    @Nullable
    public static MusicItem getCurrentItem() {
        try {
            return Data.sMusicBinder.getCurrentItem();
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
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
        Log.d(TAG, "onReceive: done");

        final int type = intent.getIntExtra(INTENT_PLAY_TYPE, 0);

        ///////////////////////////BEFORE PLAYER SET/////////////////////////////////////////

        switch (type) {
            //clicked by notif, just resume play
            case CASE_TYPE_NOTIFICATION_RESUME: {
                Utils.Ui.setPlayButtonNowPlaying();
                playMusic();
            }
            break;

            //Type Random (play)
            case CASE_TYPE_SHUFFLE: {
                if (!READY.get()) break;
                new FastShufflePlayback().execute();
            }
            break;

            //by next button...(in detail or noti) (must ActivityList isn't empty)
            //by auto-next(mediaPlayer OnCompletionListener) of next-play by user, at this time MainActivity is present
            //by MusicDetailFragment preview imageButton (view history song list)
            case 6: {

                //检测是否指定下一首播放
                if (Data.sNextWillPlayItem != null) {
                    new DoesHasNextPlay().execute();
                    break;
                }

                //检测循环
                if (Values.CurrentData.CURRENT_PLAY_TYPE.equals(Values.TYPE_REPEAT_ONE)) {
                    seekTo(0);
                    playMusic();
                    break;
                }

                //检测大小
                if (Data.sPlayOrderList.size() <= 0) {
                    Toast.makeText(context, "Data.sPlayOrderList.size() <= 0", Toast.LENGTH_SHORT).show();
                    break;
                }

                //检测前后播放
                int targetIndex = 0;
                if (intent.getStringExtra("args") != null) {
                    if (intent.getStringExtra("args").contains(TYPE_NEXT)) {
                        targetIndex = Values.CurrentData.CURRENT_MUSIC_INDEX + 1;
                        if (targetIndex > Data.sPlayOrderList.size() - 1) {            //超出范围自动跳转0
                            targetIndex = 0;
                        }
                    } else if (intent.getStringExtra("args").contains(TYPE_PREVIOUS)) {
                        targetIndex = Values.CurrentData.CURRENT_MUSIC_INDEX - 1;
                        if (targetIndex < 0) {
                            targetIndex = Data.sPlayOrderList.size() - 1;      //超出范围超转最后
                        }
                    }
                }
                Values.CurrentData.CURRENT_MUSIC_INDEX = targetIndex;

                Data.sCurrentMusicItem = Data.sPlayOrderList.get(targetIndex);

                resetMusic();
                setDataSource(Data.sPlayOrderList.get(targetIndex).getMusicPath());
                prepare();

                setFlags(targetIndex);

                //load data
                if (!Data.sActivities.isEmpty()) {
                    final MusicDetailFragment musicDetailFragment = ((MainActivity) Data.sActivities.get(0)).getMusicDetailFragment();

                    //slide 滑动切歌无需再次加载albumCover
                    if (intent.getStringExtra("args") != null && intent.getStringExtra("args").contains(TYPE_SLIDE)) {
                        final String musicName = Data.sPlayOrderList.get(targetIndex).getMusicName();
                        final String albumName = Data.sPlayOrderList.get(targetIndex).getMusicAlbum();
                        final Bitmap cover = Utils.Audio.getCoverBitmap(context, Data.sPlayOrderList.get(targetIndex).getAlbumId());

                        Utils.Ui.setPlayButtonNowPlaying();
                        musicDetailFragment.setCurrentInfoWithoutMainImage(musicName, albumName, cover);
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

            //by MusicListFragment item click
            case CASE_TYPE_ITEM_CLICK: {
                //set current data
                Data.sCurrentMusicItem = Data.sMusicItems.get(Integer.parseInt(intent.getStringExtra("args")));
                Log.d(TAG, "onReceive: " + Data.sCurrentMusicItem.getMusicName());

                ReceiverOnMusicPlay.resetMusic();
                ReceiverOnMusicPlay.setDataSource(Data.sCurrentMusicItem.getMusicPath());

                try {
                    ReceiverOnMusicPlay.prepare();
                } catch (Exception e) {
                    e.printStackTrace();
                    ReceiverOnMusicPlay.resetMusic();
                    return;
                }
                ReceiverOnMusicPlay.playMusic();

                sureCar();

                Utils.Ui.setPlayButtonNowPlaying();
                MusicDetailFragment fragment = ((MainActivity) Data.sActivities.get(0)).getMusicDetailFragment();
                fragment.getHandler().sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);         //update seek
                fragment.setCurrentInfo(Data.sCurrentMusicItem.getMusicName(), Data.sCurrentMusicItem.getMusicAlbum(), Utils.Audio.getCoverBitmap(context, Data.sCurrentMusicItem.getAlbumId()));
                ((MainActivity) Data.sActivities.get(0)).getMainBinding().slidingLayout.setTouchEnabled(true);
            }
            break;
            default:
        }

        ///////////////////////////AFTER PLAYER SET/////////////////////////////////////////

        Log.d(TAG, "onReceive: after all");

        List<Detail> infos = LitePal.where("MusicId = ?", String.valueOf(Data.sCurrentMusicItem.getMusicID())).find(Detail.class);
        if (infos.size() == 0) {
            Detail detail = new Detail();
            detail.setMusicId(Data.sCurrentMusicItem.getMusicID());
            detail.setPlayTimes(1);
            Log.d(TAG, "onReceive create: " + detail.save());
        } else {
            Detail info = infos.get(0);
            info.setPlayTimes(info.getPlayTimes() + 1);
            Log.d(TAG, "onReceive create: " + info.save());
        }

        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Values.SharedPrefsTag.LAST_PLAY_MUSIC_ID, Data.sCurrentMusicItem.getMusicID()).apply();

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

        @Override
        protected Integer doInBackground(Void... voids) {

            if (Data.sNextWillPlayItem != null) {
                resetMusic();
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
            //error
            if (status != 0) return;

            if (!Data.sActivities.isEmpty()) {

                MusicDetailFragment musicDetailFragment = ((MainActivity) Data.sActivities.get(0)).getMusicDetailFragment();

                final Bitmap cover = Utils.Audio.getCoverBitmap(Data.sActivities.get(0), Data.sNextWillPlayItem.getAlbumId());
                sureCar();

                Utils.Ui.setPlayButtonNowPlaying();
                musicDetailFragment.setCurrentInfo(Data.sNextWillPlayItem.getMusicName(), Data.sNextWillPlayItem.getMusicAlbum(), cover);

                reSetSeekBar();         //防止seekBar跳动到Max
                musicDetailFragment.getHandler().sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);
                musicDetailFragment.getHandler().sendEmptyMessage(Values.HandlerWhat.RECYCLER_SCROLL);
            }

            Data.sNextWillPlayItem = null;
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

            Data.sCurrentMusicItem = Data.sPlayOrderList.get(index);

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
