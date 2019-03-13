package top.geek_studio.chenlongcould.musicplayer.database;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

/**
 * just save albumArt
 *
 * @author chenlongcould
 */
public class ArtistArtPath extends LitePalSupport {

	private int mArtistId;

	@Column(defaultValue = "null")
	private String mArtistArt;

	@Column(defaultValue = "false")
	private boolean mForceUse;

	public int getArtistId() {
		return mArtistId;
	}

	public void setArtistId(int artistId) {
		mArtistId = artistId;
	}

	public String getArtistArt() {
		return mArtistArt;
	}

	public void setArtistArt(String artistArt) {
		mArtistArt = artistArt;
	}

	public boolean isForceUse() {
		return mForceUse;
	}

	public void setForceUse(boolean forceUse) {
		mForceUse = forceUse;
	}
}
