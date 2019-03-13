package top.geek_studio.chenlongcould.musicplayer.database;

import org.litepal.crud.LitePalSupport;

public class MyBlackPath extends LitePalSupport {

	private String mDirPath;

	public String getDirPath() {
		return mDirPath;
	}

	public void setDirPath(String dirPath) {
		mDirPath = dirPath;
	}

}
