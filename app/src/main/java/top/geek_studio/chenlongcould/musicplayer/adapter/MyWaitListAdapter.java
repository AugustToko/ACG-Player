/*
 * ************************************************************
 * 文件：MyWaitListAdapter.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月18日 18:58:29
 * 上次修改时间：2019年01月18日 12:11:18
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.adapter;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.AlbumDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.PublicActivity;
import top.geek_studio.chenlongcould.musicplayer.broadcasts.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

public final class MyWaitListAdapter extends RecyclerView.Adapter<MyWaitListAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private static final String TAG = "MyWaitListAdapter";

    /**
     * 与 {@link Data#sPlayOrderList} 完全同步
     */
    private List<MusicItem> mMusicItems;

    private MainActivity mMainActivity;

    private ViewHolder currentBind;

    public MyWaitListAdapter(MainActivity mainActivity, List<MusicItem> musicItems) {
        mMusicItems = musicItems;
        mMainActivity = mainActivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_in_detail, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);

        onMusicItemClick(view, holder);

        holder.mItemMenuButton.setOnClickListener(v -> holder.mPopupMenu.show());

        holder.mPopupMenu.setOnMenuItemClickListener(item -> {

            final int index = holder.getAdapterPosition();

            Values.CurrentData.CURRENT_SELECT_ITEM_INDEX_WITH_ITEM_MENU = index;

            switch (item.getItemId()) {
                //noinspection PointlessArithmeticExpression
                case Menu.FIRST + 0: {
                    Data.sNextWillPlayItem = mMusicItems.get(holder.getAdapterPosition());
                }
                break;

                case Menu.FIRST + 2: {
                    Utils.DataSet.addListDialog(mMainActivity, mMusicItems.get(holder.getAdapterPosition()));
                }
                break;

                // TODO: 2018/11/30 to new
                case Menu.FIRST + 4: {
                    final String albumName = mMusicItems.get(holder.getAdapterPosition()).getMusicAlbum();
                    final Cursor cursor = mMainActivity.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null,
                            MediaStore.Audio.Albums.ALBUM + "= ?", new String[]{mMusicItems.get(holder.getAdapterPosition()).getMusicAlbum()}, null);
                    //int MainActivity
                    final MainActivity mainActivity = (MainActivity) Data.sActivities.get(0);
                    final Intent intent = new Intent(mainActivity, AlbumDetailActivity.class);
                    intent.putExtra("key", albumName);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        final int id = Integer.parseInt(cursor.getString(0));
                        intent.putExtra("_id", id);
                        cursor.close();
                    }
                    mMainActivity.startActivity(intent);

                }
                break;

                case Menu.FIRST + 5: {
                    final Intent intent = new Intent(mMainActivity, PublicActivity.class);
                    intent.putExtra("start_by", "detail");
                    mMainActivity.startActivity(intent);
                }
                break;
            }

            Values.CurrentData.CURRENT_SELECT_ITEM_INDEX_WITH_ITEM_MENU = -1;

            return false;
        });

        view.setOnLongClickListener(v -> {
            holder.mPopupMenu.show();
            return true;
        });

        return holder;
    }

    private void onMusicItemClick(View view, ViewHolder holder) {
        view.setOnClickListener(v -> {
            final Disposable disposable = Observable.create((ObservableOnSubscribe<Integer>) observableEmitter -> {

                //因为mMusicItems 与 Data.sPlayOrderList 同步, 所以无需转换index
//                for (int i = 0; i < Data.sPlayOrderList.size(); i++) {
//                    if (Data.sPlayOrderList.get(i).getMusicID() == mMusicItems.get(holder.getAdapterPosition()).getMusicID()) {
//                        Values.CurrentData.CURRENT_MUSIC_INDEX = i;
//                    }
//                }
                Values.CurrentData.CURRENT_MUSIC_INDEX = holder.getAdapterPosition();

                ReceiverOnMusicPlay.resetMusic();

                //cover set
                Bitmap img = Utils.Audio.getCoverBitmap(mMainActivity, mMusicItems.get(holder.getAdapterPosition()).getAlbumId());
                Data.setCurrentCover(img);

                for (int i = 0; i < Data.sMusicItems.size(); i++) {
                    MusicItem item = Data.sMusicItems.get(i);
                    if (item.getMusicID() == mMusicItems.get(holder.getAdapterPosition()).getMusicID()) {
                        observableEmitter.onNext(i);
                    }
                }

            }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(integer -> Utils.SendSomeThing.sendPlay(mMainActivity, ReceiverOnMusicPlay.TYPE_ITEM_CLICK, integer.toString()), Throwable::printStackTrace);
            Data.sDisposables.add(disposable);
        });
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

        ImageView mItemMenuButton;

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

            mPopupMenu = new PopupMenu(mMainActivity, mItemMenuButton);
            mMenu = mPopupMenu.getMenu();

            //noinspection PointlessArithmeticExpression
            mMenu.add(Menu.NONE, Menu.FIRST + 0, 0, "下一首播放");
            mMenu.add(Menu.NONE, Menu.FIRST + 2, 0, "加入播放列表");
            mMenu.add(Menu.NONE, Menu.FIRST + 4, 0, "查看专辑");
            mMenu.add(Menu.NONE, Menu.FIRST + 5, 0, "详细信息");

            MenuInflater menuInflater = Data.sActivities.get(0).getMenuInflater();
            menuInflater.inflate(R.menu.recycler_song_item_menu, mMenu);
        }
    }
}
