package top.geek_studio.chenlongcould.musicplayer.Database;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

/**
 * just save albumArt
 *
 * @author chenlongcould
 */
public class CustomAlbumPath extends LitePalSupport {

    private int mAlbumId;

    @Column(defaultValue = "null")
    private String mAlbumArt;

    @Column(defaultValue = "false")
    private boolean mForceUse;

    public int getAlbumId() {
        return mAlbumId;
    }

    public void setAlbumId(int albumId) {
        mAlbumId = albumId;
    }

    public String getAlbumArt() {
        return mAlbumArt;
    }

    public void setAlbumArt(String albumArt) {
        mAlbumArt = albumArt;
    }

    public boolean isForceUse() {
        return mForceUse;
    }

    public void setForceUse(boolean forceUse) {
        mForceUse = forceUse;
    }
}
