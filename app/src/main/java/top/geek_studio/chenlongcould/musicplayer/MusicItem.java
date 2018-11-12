package top.geek_studio.chenlongcould.musicplayer;

public class MusicItem {

    byte[] mMusicCover;
    private String mMusicName;
    private String mMusicPath;
    private int mMusicID;
    private String mMusicAlbum;

    public MusicItem(String musicName, String musicPath, int musicID, String musicAlbum) {
        mMusicName = musicName;
        mMusicPath = musicPath;
        mMusicID = musicID;
        mMusicAlbum = musicAlbum;
    }

    public String getMusicName() {
        return mMusicName;
    }

    public String getMusicPath() {
        return mMusicPath;
    }

    public int getMusicID() {
        return mMusicID;
    }

    public String getMusicAlbum() {
        return mMusicAlbum;
    }

    public byte[] getMusicCover() {
        return mMusicCover;
    }
}
