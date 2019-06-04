package top.geek_studio.chenlongcould.musicplayer.activity.artistdetail;

import android.content.ContentResolver;
import android.content.Intent;
import androidx.annotation.NonNull;
import top.geek_studio.chenlongcould.musicplayer.BasePresenter;
import top.geek_studio.chenlongcould.musicplayer.BaseView;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;

import java.util.List;

/**
 * @author : chenlongcould
 * @date : 2019/06/04/14
 */
public interface ArtistDetailContract {

	interface View extends BaseView<Presenter> {
		ContentResolver getContentResolver();

		Intent getIntent();

		void setSongCountText(@NonNull final String data);

		void setDurationText(@NonNull final String data);

		void setAlbumCountText(@NonNull final String data);

		void notifyDataSetChanged();
	}

	interface Presenter extends BasePresenter {
		void close();

		List<MusicItem> getSongs();
	}

}
