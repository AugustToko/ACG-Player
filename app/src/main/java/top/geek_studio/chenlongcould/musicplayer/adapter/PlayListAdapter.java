package top.geek_studio.chenlongcould.musicplayer.adapter;

import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.activity.ListViewActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.fragment.PlayListFragment;
import top.geek_studio.chenlongcould.musicplayer.model.PlayListItem;
import top.geek_studio.chenlongcould.musicplayer.utils.PlayListsUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenlongcould
 */
public final class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
	
	private List<PlayListItem> mPlayListItems;
	
	private MainActivity mMainActivity;

	private PlayListFragment playListFragment;

	public PlayListAdapter(PlayListFragment fragment, List<PlayListItem> playListItems) {
		mPlayListItems = playListItems;
		playListFragment = fragment;
		mMainActivity = (MainActivity) fragment.getActivity();
	}


	@NonNull
	@Override
	public String getSectionName(int position) {
		return String.valueOf(mPlayListItems.get(position).getName().charAt(0));
	}
	
	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int itemType) {
		View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_play_list_item, viewGroup, false);
		ViewHolder holder = new ViewHolder(view);
		
		view.setOnClickListener(v -> {
			Intent intent = new Intent(mMainActivity, ListViewActivity.class);
			intent.putExtra("start_by", ListViewActivity.ListType.ACTION_PLAY_LIST_ITEM);
			intent.putExtra("play_list_name", mPlayListItems.get(holder.getAdapterPosition()).getName());
			intent.putExtra("play_list_id", mPlayListItems.get(holder.getAdapterPosition()).getId());
			mMainActivity.startActivity(intent);
		});
		
		holder.mItemMenu.setOnClickListener(v -> holder.mPopupMenu.show());
		view.setOnLongClickListener(v -> {
			holder.mPopupMenu.show();
			return true;
		});

		// TODO: 2019/5/27
		holder.mPopupMenu.setOnMenuItemClickListener(item -> {
			switch (item.getItemId()) {
				
				//del
				case Menu.FIRST: {

					final PlayListItem listItem = mPlayListItems.get(holder.getAdapterPosition());
					if (PlayListsUtil.doesPlaylistExist(mMainActivity, listItem.getId())) {
						final ArrayList<PlayListItem> playListItems = new ArrayList<>();
						playListItems.add(listItem);
						PlayListsUtil.deletePlaylists(mMainActivity, playListItems);
						playListFragment.removeItem(listItem);
					}
				}
				break;
				
				case Menu.FIRST + 1: {
					Toast.makeText(mMainActivity, "This feature is coming soon", Toast.LENGTH_SHORT).show();
				}
				break;
				
				case Menu.FIRST + 2: {
					Toast.makeText(mMainActivity, "This feature is coming soon", Toast.LENGTH_SHORT).show();
				}
				break;

				case Menu.FIRST + 3: {
					Toast.makeText(mMainActivity, "This feature is coming soon", Toast.LENGTH_SHORT).show();
				}
				break;

				case Menu.FIRST + 4: {
					Toast.makeText(mMainActivity, "This feature is coming soon", Toast.LENGTH_SHORT).show();
				}
				break;

				// rename
				case Menu.FIRST + 5: {
					final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(mMainActivity);
					builder.setTitle(mMainActivity.getString(R.string.enter_name));
					final EditText et = new EditText(mMainActivity);
					builder.setView(et);
					et.setHint(mMainActivity.getString(R.string.enter_name));
					et.setSingleLine(true);
					builder.setNegativeButton(mMainActivity.getString(R.string.cancel), null);
					builder.setPositiveButton(mMainActivity.getString(R.string.sure), (dialog1, which1) -> {
						if (TextUtils.isEmpty(et.getText())) {
							Toast.makeText(mMainActivity, "Enter name!", Toast.LENGTH_SHORT).show();
							return;
						}

						PlayListsUtil.renamePlaylist(mMainActivity, mPlayListItems.get(holder.getAdapterPosition()).getId(), String.valueOf(et.getText()));
					});
					builder.show();
				}
				break;

				case Menu.FIRST + 6: {
					Toast.makeText(mMainActivity, "This feature is coming soon", Toast.LENGTH_SHORT).show();
				}
				break;
				default:
			}
			return true;
		});
		
		return holder;
	}
	
	@Override
	public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
		viewHolder.mPlayListName.setText(mPlayListItems.get(i).getName());
		if (mPlayListItems.get(i).getName().equals(mMainActivity.getString(R.string.favorites))) {
			viewHolder.mPopupMenu.getMenu().removeItem(Menu.FIRST + 5);
		}
	}
	
	@Override
	public int getItemCount() {
		return mPlayListItems.size();
	}
	
	class ViewHolder extends RecyclerView.ViewHolder {
		
		TextView mPlayListName;
		
		ImageView mItemMenu;
		
		PopupMenu mPopupMenu;
		
		Menu mMenu;
		
		ViewHolder(@NonNull View itemView) {
			super(itemView);
			mPlayListName = itemView.findViewById(R.id.play_list_name);
			mItemMenu = itemView.findViewById(R.id.recycler_playlist_item_menu);
			
			mPopupMenu = new PopupMenu(mMainActivity, mItemMenu);
			mMenu = mPopupMenu.getMenu();

			mMenu.add(Menu.NONE, Menu.FIRST, 0, mMainActivity.getString(R.string.del));
			mMenu.add(Menu.NONE, Menu.FIRST + 1, 0, mMainActivity.getString(R.string.save_as) + "M3U");
			mMenu.add(Menu.NONE, Menu.FIRST + 2, 0, mMainActivity.getString(R.string.play));
			mMenu.add(Menu.NONE, Menu.FIRST + 3, 0, "Duplicate");
			mMenu.add(Menu.NONE, Menu.FIRST + 4, 0, "Shuffle Playback");
			mMenu.add(Menu.NONE, Menu.FIRST + 5, 0, "Rename");
			mMenu.add(Menu.NONE, Menu.FIRST + 6, 0, "Clear this play list");
		}
	}
}
