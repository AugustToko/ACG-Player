// IMuiscService.aidl
package top.geek_studio.chenlongcould.musicplayer;

import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;

interface IMuiscService {
    void reset();
    boolean isPlayingMusic();
    int getDuration();
    int getCurrentPosition();
    void seekTo(in int position);
    void release();

    void addNextWillPlayItem(in MusicItem item);

    void addToOrderList(in MusicItem item);

    void removeFromOrderList(in MusicItem item);

    void syncOrderList(in int[] array);

    MusicItem getCurrentItem();

    int getCurrentIndex();

    void setCurrentIndex(in int index);

    void loadMusicItem(in MusicItem item);
}
