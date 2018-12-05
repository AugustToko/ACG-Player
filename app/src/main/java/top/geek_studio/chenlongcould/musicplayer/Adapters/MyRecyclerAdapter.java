/*
 * ************************************************************
 * 文件：MyRecyclerAdapter.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月05日 09:30:08
 * 上次修改时间：2018年12月05日 09:29:15
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.Activities.AlbumDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.PublicActivity;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.IStyle;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter, IStyle {

    private static final String TAG = "MyRecyclerAdapter";

    private volatile boolean READY = true;

    private boolean HAS_EXPAND = false;

    private List<MusicItem> mMusicItems;

    private MainActivity mMainActivity;

    private Context mContext;

    private ViewHolder currentBind;

    public MyRecyclerAdapter(List<MusicItem> musicItems, Context context) {
        mMusicItems = musicItems;
        mMainActivity = (MainActivity) Data.sActivities.get(0);
        mContext = context;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return String.valueOf(mMusicItems.get(position).getMusicName().charAt(0));
    }

    @SuppressLint("StaticFieldLeak")
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_music_list_item, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);

        /*
         * show ExpandView (more opt)...
         * */
        // TODO: 2018/12/4 do
//        holder.mMusicCoverImage.setOnClickListener(v -> {
//
//            holder.mExpandView.clearAnimation();
//
//            final ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) holder.mExpandView.getLayoutParams();
//
//            Log.d(TAG, "onCreateViewHolder: topMargin: " + layoutParams.topMargin + " HAS_EXPAND: " + HAS_EXPAND);
//
//            final ValueAnimator animator = new ValueAnimator();
//            animator.setInterpolator(new OvershootInterpolator());
//            animator.setDuration(300);
//
//
//            animator.setIntValues((int) mMainActivity.getResources().getDimension(R.dimen.recycler_expand_view), 0);
//            animator.setIntValues(0, (int) mMainActivity.getResources().getDimension(R.dimen.recycler_expand_view));
//
////            if (!HAS_EXPAND) {
////                HAS_EXPAND = true;
////                animator.setIntValues(0, (int) mMainActivity.getResources().getDimension(R.dimen.recycler_expand_view));
////            } else {
////                HAS_EXPAND = false;
////                animator.setIntValues((int) mMainActivity.getResources().getDimension(R.dimen.recycler_expand_view), 0);
////            }
//
//            animator.addUpdateListener(animation -> {
//                layoutParams.setMargins(0, (int) animation.getAnimatedValue(), 0, 0);
//                holder.mExpandView.setLayoutParams(layoutParams);
//                holder.mExpandView.requestLayout();
//            });
//
//            animator.start();
//
//            Log.d(TAG, "onCreateViewHolder: " + layoutParams.leftMargin + " " + layoutParams.topMargin + " " + layoutParams.leftMargin + " " + layoutParams.bottomMargin);
//
//        });

        view.setOnClickListener(v -> new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... voids) {
                Log.d(TAG, "onCreateViewHolder: current status " + READY);

                if (!READY) {
                    Log.d(TAG, "onCreateViewHolder: MusicBinder not ready!!!");
                    mMainActivity.runOnUiThread(() -> Toast.makeText(mMainActivity, "Wait...", Toast.LENGTH_SHORT).show());
                    return null;
                }
                READY = false;

                String clickedPath = mMusicItems.get(holder.getAdapterPosition()).getMusicPath();

                //song clicked same as playing
                if (Data.sMusicBinder.isPlayingMusic()) {
                    if (clickedPath.equals(Values.CurrentData.CURRENT_SONG_PATH)) {
                        Utils.SendSomeThing.sendPause(mContext);
                        return null;
                    }
                }

                Data.sMusicBinder.resetMusic();

                String clickedSongName = mMusicItems.get(holder.getAdapterPosition()).getMusicName();
                String clickedSongAlbumName = mMusicItems.get(holder.getAdapterPosition()).getMusicAlbum();

                //清楚播放队列, 并加入当前歌曲序列
                Data.sHistoryPlayIndex.clear();
                Data.sHistoryPlayIndex.add(holder.getAdapterPosition());

                Bitmap cover = Utils.Audio.getMp3Cover(clickedPath);

                Data.sCurrentMusicAlbum = clickedSongAlbumName;
                Data.sCurrentMusicName = clickedSongName;
                Data.sCurrentMusicBitmap = cover;

                //set InfoBar
                mMainActivity.getMusicDetailFragment().setSlideInfo(clickedSongName, clickedSongAlbumName, cover);
                mMainActivity.getMusicDetailFragment().setCurrentInfo(clickedSongName, clickedSongAlbumName, cover);

                Values.MUSIC_PLAYING = true;
                Values.HAS_PLAYED = true;
                Values.CurrentData.CURRENT_MUSIC_INDEX = holder.getAdapterPosition();
                Values.CurrentData.CURRENT_SONG_PATH = clickedPath;

                try {
                    Data.sMusicBinder.setDataSource(clickedPath);
                    Data.sMusicBinder.prepare();
                    Data.sMusicBinder.playMusic();

                    Utils.Ui.setPlayButtonNowPlaying();
                    mMainActivity.getMusicDetailFragment().getHandler().sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);

                } catch (IOException e) {
                    e.printStackTrace();
                    Data.sMusicBinder.resetMusic();
                    Toast.makeText(mMainActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Integer result) {
                READY = true;
            }
        }.execute());

        view.setOnLongClickListener(v -> {
            if (Data.sMusicBinder.isPlayingMusic()) {
                Utils.SendSomeThing.sendPause(mContext);
            }
            return true;
        });

        holder.mItemMenuButton.setOnClickListener(v -> holder.mPopupMenu.show());

        holder.itemView.setOnLongClickListener(v -> {
            holder.mPopupMenu.show();
            return true;
        });

        holder.mPopupMenu.setOnMenuItemClickListener(item -> {

            @SuppressWarnings("UnnecessaryLocalVariable") int index = holder.getAdapterPosition();

            Values.CurrentData.CURRENT_SELECT_ITEM_INDEX_WITH_ITEM_MENU = index;

            switch (item.getItemId()) {
                //noinspection PointlessArithmeticExpression
                case Menu.FIRST + 0: {
                    Data.sNextWillPlayIndex = holder.getAdapterPosition();
                }
                break;

                case Menu.FIRST + 1: {
                    // TODO: 2018/11/8 待完善(最喜爱歌曲列表)
                    Toast.makeText(mContext, "Building...", Toast.LENGTH_SHORT).show();
//                    SharedPreferences mPlayListSpf = mMainActivity.getSharedPreferences(Values.SharedPrefsTag.PLAY_LIST_SPF_NAME_MY_FAVOURITE, 0);
//                    SharedPreferences.Editor editor = mPlayListSpf.edit();
//                    editor.putString(Values.PLAY_LIST_SPF_KEY, mMusicItems.get(index).getMusicPath());
//                    editor.apply();
//
//                    Utils.Ui.fastToast(Data.sActivities.get(0).getApplicationContext(), "Done!");
                }
                break;

                case Menu.FIRST + 2: {
                    // TODO: 2018/11/18 test play list
                    Toast.makeText(mContext, "Building...", Toast.LENGTH_SHORT).show();
//                    PlayListsUtil.createPlaylist(mContext, String.valueOf(new Random(1000)));
                }
                break;

                case Menu.FIRST + 4: {
                    String albumName = mMusicItems.get(holder.getAdapterPosition()).getMusicAlbum();
                    Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null,
                            MediaStore.Audio.Albums.ALBUM + "= ?", new String[]{mMusicItems.get(holder.getAdapterPosition()).getMusicAlbum()}, null);

                    //int MainActivity
                    MainActivity mainActivity = (MainActivity) Data.sActivities.get(0);
                    Intent intent = new Intent(mainActivity, AlbumDetailActivity.class);
                    intent.putExtra("key", albumName);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int id = Integer.parseInt(cursor.getString(0));
                        intent.putExtra("_id", id);
                        cursor.close();
                    }
                    mContext.startActivity(intent);

                }
                break;

                case Menu.FIRST + 5: {
                    Intent intent = new Intent(mContext, PublicActivity.class);
                    intent.putExtra("start_by", "detail");
                    mContext.startActivity(intent);
                }
                break;
            }

            Values.CurrentData.CURRENT_SELECT_ITEM_INDEX_WITH_ITEM_MENU = -1;

            return false;
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        Object tag = viewHolder.mMusicCoverImage.getTag(R.string.key_id_1);
        if (tag != null && (int) tag != i) {
            GlideApp.with(mMainActivity).clear(viewHolder.mMusicCoverImage);
        }

        currentBind = viewHolder;

        /* show song name, use songNameList */
        Values.CurrentData.CURRENT_BIND_INDEX_MUSIC_LIST = viewHolder.getAdapterPosition();

        viewHolder.mMusicText.setText(mMusicItems.get(i).getMusicName());
        viewHolder.mMusicAlbumName.setText(mMusicItems.get(i).getMusicAlbum());
        String prefix = mMusicItems.get(i).getMusicPath().substring(mMusicItems.get(i).getMusicPath().lastIndexOf(".") + 1);
        viewHolder.mMusicExtName.setText(prefix);
        viewHolder.mTime.setText(Data.sSimpleDateFormat.format(new Date(mMusicItems.get(i).getDuration())));

        initStyle();

        /*--- 添加标记以便避免ImageView因为ViewHolder的复用而出现混乱 ---*/
        viewHolder.mMusicCoverImage.setTag(R.string.key_id_1, i);
//        Log.i(TAG, "onBindViewHolder: AdapterPosition: " + viewHolder.getAdapterPosition() + ", i: " + i);

        new MyTask(viewHolder.mMusicCoverImage, mMusicItems, mContext, i).execute();

    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        holder.mMusicCoverImage.setTag(R.string.key_id_1, null);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {

//        final ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) holder.mExpandView.getLayoutParams();
//        layoutParams.setMargins(0, 0, 0, 0);
//        holder.itemView.setLayoutParams(layoutParams);
//        holder.itemView.requestLayout();

        GlideApp.with(mMainActivity).clear(holder.mMusicCoverImage);
        super.onViewRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(@NonNull ViewHolder holder) {
        Log.d(TAG, "onFailedToRecycleView: " + holder.mMusicText);
        GlideApp.with(mMainActivity).clear(holder.mMusicCoverImage);
        holder.itemView.setBackgroundColor(Color.RED);
        holder.mMusicText.setText("This item recycler failed...");
        return super.onFailedToRecycleView(holder);
    }

    @Override
    public int getItemCount() {
        return mMusicItems.size();
    }

    @Override
    public void initStyle() {

        //type_background
        if (Values.Style.NIGHT_MODE) currentBind.mMusicExtName.setBackgroundColor(Color.GRAY);
        else {
            if (currentBind.mMusicExtName.getText().equals("mp3"))
                currentBind.mMusicExtName.setBackgroundResource(R.color.mp3TypeColor);
            else
                currentBind.mMusicExtName.setBackgroundColor(Color.CYAN);
        }

        //style
        currentBind.mMusicText.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));
    }

    static class MyTask extends AsyncTask<Void, Void, String> {

        private WeakReference<ImageView> mImageViewWeakReference;

        private WeakReference<Context> mContextWeakReference;

        private WeakReference<List<MusicItem>> mListWeakReference;

        private int mPosition;

        MyTask(ImageView imageView, List<MusicItem> musicItems, Context context, int position) {
            mImageViewWeakReference = new WeakReference<>(imageView);
            mContextWeakReference = new WeakReference<>(context);
            mPosition = position;
            mListWeakReference = new WeakReference<>(musicItems);
        }

        @Override
        protected void onPostExecute(String result) {

            if (mImageViewWeakReference.get() == null || result == null) return;

            if (result.equals("null")) {
                GlideApp.with(mContextWeakReference.get()).load(R.drawable.ic_audiotrack_24px).into(mImageViewWeakReference.get());
                return;
            }

            GlideApp.with(mContextWeakReference.get()).load(result)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .override(Values.MAX_HEIGHT_AND_WIDTH, Values.MAX_HEIGHT_AND_WIDTH)
                    .skipMemoryCache(true)
                    .into(mImageViewWeakReference.get());
        }

        @Override
        protected String doInBackground(Void... voids) {

            if (mImageViewWeakReference.get() == null || mImageViewWeakReference.get().getTag(R.string.key_id_1) == null) {
                Log.e(TAG, "doInBackground: key null------------------skip");
                return null;
            }

            //根据position判断是否为复用ViewHolder
            if (((int) mImageViewWeakReference.get().getTag(R.string.key_id_1)) != mPosition) {
                Log.e(TAG, "doInBackground: key error------------------skip");
                GlideApp.with(mContextWeakReference.get()).clear(mImageViewWeakReference.get());
                return null;
            }

            String img;
            Cursor cursor = mContextWeakReference.get().getContentResolver().query(
                    Uri.parse(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI + String.valueOf(File.separatorChar) + mListWeakReference.get().get(mPosition).getAlbumId())
                    , new String[]{MediaStore.Audio.Albums.ALBUM_ART}, null, null, null);

            if (cursor != null && cursor.getCount() != 0) {
                cursor.moveToFirst();
                img = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
                cursor.close();
                return img;
            } else {
                return "null";
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView mMusicCoverImage;

        ImageView mItemMenuButton;

        TextView mMusicText;

        TextView mMusicAlbumName;

        TextView mMusicExtName;

        TextView mTime;

        PopupMenu mPopupMenu;

        Menu mMenu;

        ConstraintLayout mExpandView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mMusicAlbumName = itemView.findViewById(R.id.recycler_item_music_album_name);
            mMusicCoverImage = itemView.findViewById(R.id.recycler_item_album_image);
            mMusicText = itemView.findViewById(R.id.recycler_item_music_name);
            mItemMenuButton = itemView.findViewById(R.id.recycler_item_menu);
            mMusicExtName = itemView.findViewById(R.id.recycler_item_music_type_name);
            mTime = itemView.findViewById(R.id.recycler_item_time);
            mExpandView = itemView.findViewById(R.id.music_item_expand_view);

            mPopupMenu = new PopupMenu(mMainActivity, mItemMenuButton);
            mMenu = mPopupMenu.getMenu();

            //noinspection PointlessArithmeticExpression
            mMenu.add(Menu.NONE, Menu.FIRST + 0, 0, "下一首播放");
//            mMenu.add(Menu.NONE, Menu.FIRST + 1, 0, "喜欢");
//            mMenu.add(Menu.NONE, Menu.FIRST + 2, 0, "加入播放列表");
            mMenu.add(Menu.NONE, Menu.FIRST + 4, 0, "查看专辑");
            mMenu.add(Menu.NONE, Menu.FIRST + 5, 0, "详细信息");

            MenuInflater menuInflater = mMainActivity.getMenuInflater();
            menuInflater.inflate(R.menu.recycler_song_item_menu, mMenu);
        }
    }

}
