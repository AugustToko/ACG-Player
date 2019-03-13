/*
 * ************************************************************
 * 文件：PlayListAdapter.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月27日 13:11:38
 * 上次修改时间：2019年01月21日 20:08:09
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.PublicActivity;
import top.geek_studio.chenlongcould.musicplayer.fragment.PlayListFragment;
import top.geek_studio.chenlongcould.musicplayer.model.PlayListItem;
import top.geek_studio.chenlongcould.musicplayer.utils.PlayListsUtil;

public final class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

	private List<PlayListItem> mPlayListItems;

	private MainActivity mMainActivity;

	public PlayListAdapter(MainActivity activity, List<PlayListItem> playListItems) {
		mPlayListItems = playListItems;
		mMainActivity = activity;
	}

	@NonNull
	@Override
	public String getSectionName(int position) {
		return String.valueOf(mPlayListItems.get(position).getName().charAt(0));
	}

	@SuppressLint("StaticFieldLeak")
	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int itemType) {
		View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_play_list_item, viewGroup, false);
		ViewHolder holder = new ViewHolder(view);

		view.setOnClickListener(v -> {
			Intent intent = new Intent(mMainActivity, PublicActivity.class);
			intent.putExtra("start_by", PlayListFragment.ACTION_PLAY_LIST_ITEM);
			intent.putExtra("play_list_name", mPlayListItems.get(holder.getAdapterPosition()).getName());
			intent.putExtra("play_list_id", mPlayListItems.get(holder.getAdapterPosition()).getId());
			mMainActivity.startActivity(intent);
		});

		holder.mItemMenu.setOnClickListener(v -> holder.mPopupMenu.show());
		view.setOnLongClickListener(v -> {
			holder.mPopupMenu.show();
			return true;
		});
		holder.mPopupMenu.setOnMenuItemClickListener(item -> {
			switch (item.getItemId()) {

				//del
				case Menu.FIRST: {
					PlayListItem listItem = mPlayListItems.get(holder.getAdapterPosition());
					if (PlayListsUtil.doesPlaylistExist(mMainActivity, listItem.getId())) {
						ArrayList<PlayListItem> playListItems = new ArrayList<>();
						playListItems.add(listItem);
						PlayListsUtil.deletePlaylists(mMainActivity, playListItems);

						for (int i = 0; i < Data.sPlayListItems.size(); i++) {
							if (Data.sPlayListItems.get(i).getId() == listItem.getId()) {
								Data.sPlayListItems.remove(i);
								mMainActivity.getPlayListFragment().getPlayListAdapter().notifyItemRemoved(i);
								break;
							}
						}

					}
				}
				break;

				case Menu.FIRST + 1: {
					Toast.makeText(mMainActivity, "Building...", Toast.LENGTH_SHORT).show();
				}
				break;

				case Menu.FIRST + 2: {
					Toast.makeText(mMainActivity, "Building...", Toast.LENGTH_SHORT).show();
				}
				break;
			}
			return true;
		});

		return holder;
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
		viewHolder.mPlayListName.setText(mPlayListItems.get(i).getName());
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

			Resources resources = mMainActivity.getResources();

			mMenu.add(Menu.NONE, Menu.FIRST, 0, resources.getString(R.string.del));
			mMenu.add(Menu.NONE, Menu.FIRST + 1, 0, resources.getString(R.string.save_as));
			mMenu.add(Menu.NONE, Menu.FIRST + 2, 0, resources.getString(R.string.play));
		}
	}
}
