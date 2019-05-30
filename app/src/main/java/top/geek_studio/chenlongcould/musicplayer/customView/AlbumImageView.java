package top.geek_studio.chenlongcould.musicplayer.customView;

import android.content.Context;
import android.util.AttributeSet;

public class AlbumImageView extends androidx.appcompat.widget.AppCompatImageView {

	private static final String TAG = "AlbumImageView";

	public AlbumImageView(Context context) {
		this(context, null);
	}

	public AlbumImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AlbumImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

}
