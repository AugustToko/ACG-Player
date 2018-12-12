// IMuiscService.aidl
package top.geek_studio.chenlongcould.musicplayer;

// Declare any non-default types here with import statements

interface IMuiscService {
    void play();
    void pause();
    void stop();
    boolean isPlaying();
    void reset();
    void setDataSource(String path);
    void prepare();
    int getDuration();
    int getCurrentPosition();
    void seekTo(int position);
    void release();
}
