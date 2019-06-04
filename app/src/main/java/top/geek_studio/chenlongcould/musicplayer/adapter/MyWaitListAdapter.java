package top.geek_studio.chenlongcould.musicplayer.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.MusicService;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.BaseCompatActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.albumdetail.AlbumDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.utils.MusicUtil;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

import java.util.List;

public final class MyWaitListAdapter extends RecyclerView.Adapter<MyWaitListAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

	private static final String TAG = "MyWaitListAdapter";

	/**
	 * 与 {@link Data#sPlayOrderList} 完全同步
	 */
	private List<MusicItem> mMusicItems;

	private BaseCompatActivity mContext;

	private ViewHolder currentBind;

	public MyWaitListAdapter(BaseCompatActivity context, List<MusicItem> musicItems) {
		mMusicItems = musicItems;
		mContext = context;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_in_detail, viewGroup, false);
		ViewHolder holder = new ViewHolder(view);

		view.setOnClickListener(v -> MusicService.MusicControl.intentItemClick(mContext, mMusicItems.get(holder.getAdapterPosition())));

		holder.mItemMenuButton.setOnClickListener(v -> holder.mPopupMenu.show());

		holder.mPopupMenu.setOnMenuItemClickListener(item -> {

			switch (item.getItemId()) {
				//noinspection PointlessArithmeticExpression
				case Menu.FIRST + 0: {
					final MusicItem nextItem = mMusicItems.get(holder.getAdapterPosition());
					if (Data.sMusicBinder != null && nextItem.getMusicID() != -1) {
						try {
							Data.sMusicBinder.setNextWillPlayItem(nextItem);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}
				break;

				case Menu.FIRST + 2: {
					Utils.DataSet.addListDialog(mContext, mMusicItems.get(holder.getAdapterPosition()));
				}
				break;

				// TODO: 2018/11/30 to new
				case Menu.FIRST + 4: {
					final String albumName = mMusicItems.get(holder.getAdapterPosition()).getMusicAlbum();
					final Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null,
							MediaStore.Audio.Albums.ALBUM + "= ?", new String[]{mMusicItems.get(holder.getAdapterPosition()).getMusicAlbum()}, null);
					//int MainActivity
					final Intent intent = new Intent(mContext, AlbumDetailActivity.class);
					intent.putExtra("key", albumName);
					if (cursor != null) {
						cursor.moveToFirst();
						final int id = Integer.parseInt(cursor.getString(0));
						intent.putExtra("_id", id);
						cursor.close();
					}
					mContext.startActivity(intent);

				}
				break;

				case Menu.FIRST + 5: {
					final List<String> data = Utils.Audio.extractMetadata(mMusicItems.get(holder.getAdapterPosition()));
					ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1
							, data);
					AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
							.setTitle(mContext.getString(R.string.detail))
							.setAdapter(arrayAdapter, (dialog, which) -> {
								final ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
								final ClipData clipData = new ClipData("Copied by song detail", new String[]{"text"}, new ClipData.Item(data.get(which).split(":")[1]));
								clipboardManager.setPrimaryClip(clipData);
							})
							.setCancelable(false)
							.setNegativeButton(mContext.getString(R.string.done), (dialog, which) -> dialog.dismiss());
					builder.show();
				}
				break;

				// drop to trash can
				case Menu.FIRST + 6: {
					MusicUtil.dropToTrash(mContext, mMusicItems.get(holder.getAdapterPosition()));
				}
				break;
			}

			return false;
		});

		view.setOnLongClickListener(v -> {
			holder.mPopupMenu.show();
			return true;
		});

		return holder;
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

		currentBind = viewHolder;

		final String prefix = mMusicItems.get(i).getMusicPath().substring(mMusicItems.get(i).getMusicPath().lastIndexOf(".") + 1);

		viewHolder.mIndexText.setText(String.valueOf(i));
		viewHolder.mMusicNameText.setText(mMusicItems.get(i).getMusicName());
		viewHolder.mAlbumText.setText(mMusicItems.get(i).getMusicAlbum());
		viewHolder.mExtName.setText(prefix);

		if (Values.CurrentData.CURRENT_UI_MODE.equals(Values.UIMODE.MODE_CAR)) {
			// TODO: 2019/1/11 if bg is light
			currentBind.mMusicNameText.setTextColor(Color.WHITE);
			currentBind.mAlbumText.setTextColor(Color.WHITE);
			currentBind.mIndexText.setTextColor(Color.WHITE);
			currentBind.mExtName.setTextColor(Color.WHITE);
			currentBind.mItemMenuButton.setColorFilter(Color.WHITE);
		}
	}

	@Override
	public int getItemCount() {
		return mMusicItems.size();
	}

	@NonNull
	@Override
	public String getSectionName(int position) {
		return String.valueOf(mMusicItems.get(position).getMusicName().charAt(0));
	}

	class ViewHolder extends RecyclerView.ViewHolder {

		AppCompatImageView mItemMenuButton;

		TextView mMusicNameText;

		TextView mAlbumText;

		TextView mIndexText;

		TextView mExtName;

		PopupMenu mPopupMenu;

		Menu mMenu;

		ViewHolder(@NonNull View itemView) {
			super(itemView);
			itemView.setBackground(null);
			mIndexText = itemView.findViewById(R.id.index_textview);
			mMusicNameText = itemView.findViewById(R.id.item_main_text);
			mAlbumText = itemView.findViewById(R.id.album_text);
			mExtName = itemView.findViewById(R.id.item_in_detail_ext);
			mItemMenuButton = itemView.findViewById(R.id.item_menu);

			mPopupMenu = new PopupMenu(mContext, mItemMenuButton);
			mMenu = mPopupMenu.getMenu();

			//noinspection PointlessArithmeticExpression
			mMenu.add(Menu.NONE, Menu.FIRST + 0, 0, mContext.getString(R.string.next_play));
			mMenu.add(Menu.NONE, Menu.FIRST + 2, 0, mContext.getString(R.string.add_to_playlist));
			mMenu.add(Menu.NONE, Menu.FIRST + 4, 0, mContext.getString(R.string.show_album));
			mMenu.add(Menu.NONE, Menu.FIRST + 5, 0, mContext.getString(R.string.more_info));
			mMenu.add(Menu.NONE, Menu.FIRST + 6, 0, mContext.getString(R.string.drop_to_trash_can));

			MenuInflater menuInflater = mContext.getMenuInflater();
			menuInflater.inflate(R.menu.recycler_song_item_menu, mMenu);
		}
	}
}
