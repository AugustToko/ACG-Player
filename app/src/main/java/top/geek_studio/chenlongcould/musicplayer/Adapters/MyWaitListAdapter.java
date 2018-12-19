/*
 * ************************************************************
 * 文件：MyWaitListAdapter.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月19日 12:56:02
 * 上次修改时间：2018年12月19日 12:46:08
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.musicplayer.Activities.AlbumDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.PublicActivity;
import top.geek_studio.chenlongcould.musicplayer.BroadCasts.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Interface.IStyle;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.PlayListsUtil;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public final class MyWaitListAdapter extends RecyclerView.Adapter<MyWaitListAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter, IStyle {

    private static final String TAG = "MyWaitListAdapter";

    private List<MusicItem> mMusicItems;

    private Activity mActivity;

    private MainActivity mMainActivity;

    private ViewHolder currentBind;

    public MyWaitListAdapter(Activity activity, List<MusicItem> musicItems) {
        mMusicItems = musicItems;
        mActivity = activity;
        mMainActivity = (MainActivity) Data.sActivities.get(0);
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
                    Data.sNextWillPlayItem = Data.sPlayOrderList.get(holder.getAdapterPosition());
                }
                break;

                case Menu.FIRST + 1: {
                    // TODO: 2018/11/8 待完善(最喜爱歌曲列表)
                    final SharedPreferences mPlayListSpf = mActivity.getSharedPreferences(Values.SharedPrefsTag.PLAY_LIST_SPF_NAME_MY_FAVOURITE, 0);
                    final SharedPreferences.Editor editor = mPlayListSpf.edit();
                    editor.putString(Values.PLAY_LIST_SPF_KEY, mMusicItems.get(index).getMusicPath());
                    editor.apply();

                    Utils.Ui.fastToast(mActivity, "Done!");
                }
                break;

                case Menu.FIRST + 2: {
                    // TODO: 2018/11/18 test play list
                    PlayListsUtil.createPlaylist(mActivity, String.valueOf(new Random(1000)));
                }
                break;

                // TODO: 2018/11/30 to new
                case Menu.FIRST + 4: {
                    final String albumName = mMusicItems.get(holder.getAdapterPosition()).getMusicAlbum();
                    final Cursor cursor = mActivity.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null,
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
                    mActivity.startActivity(intent);

                }
                break;

                case Menu.FIRST + 5: {
                    final Intent intent = new Intent(mActivity, PublicActivity.class);
                    intent.putExtra("start_by", "detail");
                    mActivity.startActivity(intent);
                }
                break;
            }

            Values.CurrentData.CURRENT_SELECT_ITEM_INDEX_WITH_ITEM_MENU = -1;

            return false;
        });

        return holder;
    }

    private void onMusicItemClick(View view, ViewHolder holder) {
        view.setOnClickListener(v -> Observable.create((ObservableOnSubscribe<Integer>) observableEmitter -> {

            if (!ReceiverOnMusicPlay.READY.get()) {
                observableEmitter.onNext(-1);
            }

            ReceiverOnMusicPlay.READY.set(false);

            ReceiverOnMusicPlay.resetMusic();

            Values.CurrentData.CURRENT_MUSIC_INDEX = holder.getAdapterPosition();

            //set current data
            Data.setCurrentMusicItem(mMusicItems.get(holder.getAdapterPosition()));

            //get & save data
            Data.setCurrentCover(Utils.Audio.getMp3Cover(Data.sCurrentMusicItem.getMusicPath()));

            ReceiverOnMusicPlay.setDataSource(Data.sCurrentMusicItem.getMusicPath());
            ReceiverOnMusicPlay.prepare();
            ReceiverOnMusicPlay.playMusic();
            Values.HAS_PLAYED = true;
            ((MainActivity) Data.sActivities.get(0)).getMusicDetailFragment().getHandler().sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);         //update seek

            observableEmitter.onNext(0);
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {

                    if (integer == -1) {
                        Toast.makeText(mActivity, "Wait...", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (Values.CurrentData.UI_MODE.equals(Values.UIMODE.MODE_CAR)) {
                        Data.sCarViewActivity.getFragmentLandSpace().setData();
                    }
                    mMainActivity.getMusicDetailFragment().setSlideInfo(Data.sCurrentMusicItem.getMusicName(), Data.sCurrentMusicItem.getMusicAlbum(), Data.getCurrentCover());
                    mMainActivity.getMusicDetailFragment().setCurrentInfo(Data.sCurrentMusicItem.getMusicName(), Data.sCurrentMusicItem.getMusicAlbum(), Data.getCurrentCover());

                    Utils.Ui.setPlayButtonNowPlaying();

                }, Throwable::printStackTrace));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        currentBind = viewHolder;

        final String prefix = mMusicItems.get(i).getMusicPath().substring(mMusicItems.get(i).getMusicPath().lastIndexOf(".") + 1);

        viewHolder.mIndexText.setText(String.valueOf(i));
        viewHolder.mMusicNameText.setText(mMusicItems.get(i).getMusicName());
        viewHolder.mAlbumText.setText(mMusicItems.get(i).getMusicAlbum());
        viewHolder.mExtName.setText(prefix);

        initStyle();

    }

    @Override
    public void initStyle() {

//        //type_background
//        if (Values.Style.NIGHT_MODE) {
//            currentBind.mExtName.setBackgroundColor(Color.GRAY);
//        } else {
//            if (currentBind.mExtName.getText().equals("mp3")) {
//                currentBind.mExtName.setBackgroundResource(R.color.mp3TypeColor);
//            } else {
//                currentBind.mExtName.setBackgroundColor(Color.CYAN);
//            }
//        }

        if (Values.CurrentData.UI_MODE.equals(Values.UIMODE.MODE_CAR)) {
            currentBind.mMusicNameText.setTextColor(Color.WHITE);
            currentBind.mAlbumText.setTextColor(Color.WHITE);
            currentBind.mIndexText.setTextColor(Color.WHITE);
            currentBind.mExtName.setTextColor(Color.WHITE);
            currentBind.mItemMenuButton.setColorFilter(Color.WHITE);
        } else {
            //style
            currentBind.mMusicNameText.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));
            currentBind.mAlbumText.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));
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

            mPopupMenu = new PopupMenu(mActivity, mItemMenuButton);
            mMenu = mPopupMenu.getMenu();

            //noinspection PointlessArithmeticExpression
            mMenu.add(Menu.NONE, Menu.FIRST + 0, 0, "下一首播放");
            mMenu.add(Menu.NONE, Menu.FIRST + 1, 0, "喜欢");
            mMenu.add(Menu.NONE, Menu.FIRST + 2, 0, "加入播放列表");
            mMenu.add(Menu.NONE, Menu.FIRST + 4, 0, "查看专辑");
            mMenu.add(Menu.NONE, Menu.FIRST + 5, 0, "详细信息");

            MenuInflater menuInflater = Data.sActivities.get(0).getMenuInflater();
            menuInflater.inflate(R.menu.recycler_song_item_menu, mMenu);
        }
    }
}
