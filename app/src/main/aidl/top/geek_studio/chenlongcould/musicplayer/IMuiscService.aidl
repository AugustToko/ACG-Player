// IMuiscService.aidl
package top.geek_studio.chenlongcould.musicplayer;

import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;

interface IMuiscService {
    void pause();
    void start();
    void reset();
    void release();
    int getDuration();
    boolean isPlayingMusic();
    void seekTo(in int position);
    int getCurrentPosition();

// ----------

    void addNextWillPlayItem(in MusicItem item);

    void addToOrderList(in MusicItem item);

    void removeFromOrderList(in MusicItem item);

    void syncOrderList(in int[] array);

    MusicItem getCurrentItem();

    int getCurrentIndex();

    void setCurrentIndex(in int index);

    void loadMusicItem(in MusicItem item);

    void syncOderList(in long seed);

    // ----------

    void onItemClick(in MusicItem item);

    void next();

    void previous();
}
