package top.geek_studio.chenlongcould.musicplayer.activity.albumdetail;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import top.geek_studio.chenlongcould.musicplayer.BasePresenter;
import top.geek_studio.chenlongcould.musicplayer.BaseView;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;

import java.util.List;

/**
 * @author : chenlongcould
 * @date : 2019/06/04/13
 */
public interface AlbumDetailContract {

	interface View extends BaseView<Presenter> {

		Intent getAlbumDetailIntent();

		Context getContext();

		void notifyDataSetChanged();

		void setSongCountText(@NonNull final String data);

		void setDurationText(@NonNull final String data);

		void setAlbumYearText(@NonNull final String data);

		void setArtistText(@NonNull final String data);
	}

	interface Presenter extends BasePresenter {

		List<MusicItem> getSongs();

		void close();
	}

}
