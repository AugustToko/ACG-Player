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

    //other data input
    void setCurrentMusicData(in MusicItem item);

    void setNextWillPlayItem(in MusicItem item);

    void addToOrderList(in MusicItem item);

    MusicItem getCurrentItem();

    int getCurrentIndex();

    void setCurrentIndex(in int index);
}
