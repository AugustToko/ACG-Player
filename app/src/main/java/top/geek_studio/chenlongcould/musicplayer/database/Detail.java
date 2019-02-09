package top.geek_studio.chenlongcould.musicplayer.database;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class Detail extends LitePalSupport {

    private int MusicId;

    @Column(defaultValue = "0")
    private int PlayTimes;

    @Column(defaultValue = "0")
    private long PlayDuration;

    @Column(defaultValue = "0")
    private int MinimumPlayTimes;

    public int getMusicId() {
        return MusicId;
    }

    public void setMusicId(int musicId) {
        MusicId = musicId;
    }

    public int getPlayTimes() {
        return PlayTimes;
    }

    public void setPlayTimes(int playTimes) {
        PlayTimes = playTimes;
    }

    public long getPlayDuration() {
        return PlayDuration;
    }

    public void setPlayDuration(long playDuration) {
        PlayDuration = playDuration;
    }

    public int getMinimumPlayTimes() {
        return MinimumPlayTimes;
    }

    public void setMinimumPlayTimes(int minimumPlayTimes) {
        MinimumPlayTimes = minimumPlayTimes;
    }
}
