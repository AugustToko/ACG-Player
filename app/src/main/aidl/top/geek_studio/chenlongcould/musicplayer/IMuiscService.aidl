// IMuiscService.aidl
package top.geek_studio.chenlongcould.musicplayer;

import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;

interface IMuiscService {
    void playMusic();
    void pauseMusic();
    void stopMusic();
    boolean isPlayingMusic();
    void resetMusic();
    void setDataSource(String path);
    void prepare();
    int getDuration();
    int getCurrentPosition();
    void seekTo(int position);
    void release();

    //other data input
    void setCurrentMusicData(in MusicItem item);

    MusicItem getCurrentItem();
}
