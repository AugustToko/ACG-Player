/*
 * ************************************************************
 * 文件：MyRecyclerAdapter.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月27日 13:11:38
 * 上次修改时间：2019年01月27日 13:08:44
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.adapter;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.parser.XmlTreeBuilder;
import org.jsoup.select.Elements;
import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import top.geek_studio.chenlongcould.geeklibrary.DownloadUtil;
import top.geek_studio.chenlongcould.geeklibrary.HttpUtil;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.MyApplication;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.AlbumDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.PublicActivity;
import top.geek_studio.chenlongcould.musicplayer.broadcasts.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.database.CustomAlbumPath;
import top.geek_studio.chenlongcould.musicplayer.fragment.MusicListFragment;
import top.geek_studio.chenlongcould.musicplayer.thread_pool.ItemCoverThreadPool;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private static final String TAG = "MyRecyclerAdapter";

    /**
     * @see R.layout#recycler_music_list_item_mod
     */
    private static final int MOD_TYPE = -1;

    public boolean IS_CHOOSE = false;

    /**
     * MAIN
     */
    private MainActivity mMainActivity;

    private Activity mContext;

    //ui position, like: MainActivity or-> xxFragment...
    private String mCurrentUiPosition;
    /**
     * 媒体库，默认顺序排序
     */
    private List<MusicItem> mMusicItems;

    private ArrayList<ItemHolder> mViewHolders = new ArrayList<>();

    private int mStyleId = 0;

    /**
     * @param currentUiPosition current activity showed
     */
    public MyRecyclerAdapter(List<MusicItem> musicItems, Activity context, String currentUiPosition, int... styleId) {
        mMainActivity = (MainActivity) Data.sActivities.get(0);
        mMusicItems = musicItems;
        mContext = context;
        mCurrentUiPosition = currentUiPosition;

        if (styleId != null && styleId.length != 0) {
            mStyleId = styleId[0];
        }
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return String.valueOf(mMusicItems.get(position).getMusicName().charAt(0));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int itemType) {

        View view;

        ItemHolder holder;

        /*
         * ModHolder: baseHolder + ModHolder
         * baseHolder: common item
         * ModHolder: common item + fastPlay item
         * */
        //在 MusicListFragment 的第一选项上面添加"快速随机播放项目"
        if (itemType == MOD_TYPE && mCurrentUiPosition.equals(MusicListFragment.TAG)) {

            //style switch
            switch (mStyleId) {
                case 1: {
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_music_list_item_mod_style_1, viewGroup, false);
                    holder = new ModHolderS1(view);
                }
                break;
                default: {
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_music_list_item_mod, viewGroup, false);
                    holder = new ModHolder(view);
                }

            }
            onMusicItemClick(view.findViewById(R.id.recycler_music_item_view), holder);

            //when clicked ModHolder(fastPlay item)
            holder.itemView.setOnClickListener(v -> {
                Utils.DataSet.makeARandomList();
                Utils.SendSomeThing.sendPlay(mContext, ReceiverOnMusicPlay.TYPE_SHUFFLE, TAG);
            });

        } else {
            switch (mStyleId) {
                case 1: {
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_music_list_item_style_1, viewGroup, false);
                    holder = new ItemHolderS1(view);
                }
                break;
                default: {
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_music_list_item, viewGroup, false);
                    holder = new ItemHolder(view);
                }
            }

            onMusicItemClick(view, holder);
        }


        mViewHolders.add(holder);

        //默认设置扩展button opacity 0, (default)
        holder.mButton1.setAlpha(0);
        holder.mButton2.setAlpha(0);
        holder.mButton3.setAlpha(0);
        holder.mButton4.setAlpha(0);
        holder.mButton1.setVisibility(View.GONE);
        holder.mButton2.setVisibility(View.GONE);
        holder.mButton3.setVisibility(View.GONE);
        holder.mButton4.setVisibility(View.GONE);
        holder.mExpandText.setVisibility(View.GONE);

        holder.mMusicCoverImage.setOnClickListener(v -> {

            holder.mExpandView.clearAnimation();

            holder.mButton1.setVisibility(View.VISIBLE);
            holder.mButton2.setVisibility(View.VISIBLE);
            holder.mButton3.setVisibility(View.VISIBLE);
            holder.mButton4.setVisibility(View.VISIBLE);
            holder.mExpandText.setVisibility(View.VISIBLE);

            final ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) holder.mExpandView.getLayoutParams();

            final ValueAnimator animator = new ValueAnimator();
            animator.setDuration(300);

            if (((ConstraintLayout.LayoutParams) holder.mExpandView.getLayoutParams()).topMargin == 0) {
                animator.setIntValues(0, (int) mContext.getResources().getDimension(R.dimen.recycler_expand_view));
                holder.setIsRecyclable(false);
                holder.mExpandView.setVisibility(View.VISIBLE);
                animator.setInterpolator(new OvershootInterpolator());

                /*--- button alpha animation ---*/
                ValueAnimator alphaAnim = new ValueAnimator();
                alphaAnim.setFloatValues(0f, 1f);
                alphaAnim.setDuration(500);
                alphaAnim.addUpdateListener(animation -> {
                    holder.mButton1.setAlpha((Float) animation.getAnimatedValue());
                    holder.mButton2.postDelayed(() -> holder.mButton2.setAlpha((Float) animation.getAnimatedValue()), 100);
                    holder.mButton3.postDelayed(() -> holder.mButton3.setAlpha((Float) animation.getAnimatedValue()), 200);
                    holder.mButton4.postDelayed(() -> holder.mButton4.setAlpha((Float) animation.getAnimatedValue()), 300);
                });
                alphaAnim.start();
                /*--- button alpha animation ---*/

            } else {
                animator.setIntValues((int) mContext.getResources().getDimension(R.dimen.recycler_expand_view), 0);
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //set default...
                        holder.mButton1.setAlpha(0);
                        holder.mButton2.setAlpha(0);
                        holder.mButton3.setAlpha(0);
                        holder.mButton4.setAlpha(0);

                        holder.mButton1.setVisibility(View.GONE);
                        holder.mButton2.setVisibility(View.GONE);
                        holder.mButton3.setVisibility(View.GONE);
                        holder.mButton4.setVisibility(View.GONE);
                        holder.mExpandText.setVisibility(View.GONE);

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }

            //cover rotation
            ValueAnimator rotationAnima = new ValueAnimator();
            rotationAnima.setFloatValues(0f, 360f);
            rotationAnima.setDuration(300);
            rotationAnima.setInterpolator(new OvershootInterpolator());
            rotationAnima.addUpdateListener(animation -> holder.mMusicCoverImage.setRotation((Float) animation.getAnimatedValue()));
            rotationAnima.start();

            animator.addUpdateListener(animation -> {
                layoutParams.setMargins(0, (int) animation.getAnimatedValue(), 0, 0);
                holder.mExpandView.setLayoutParams(layoutParams);
                holder.mExpandView.requestLayout();
            });

            animator.start();

        });

        // TODO: 2018/12/5 button
        holder.mButton1.setOnClickListener(v -> Toast.makeText(mContext, "1", Toast.LENGTH_SHORT).show());
        holder.mButton2.setOnClickListener(v -> Toast.makeText(mContext, "2", Toast.LENGTH_SHORT).show());
        holder.mButton3.setOnClickListener(v -> Utils.Audio.setRingtone(mContext, mMusicItems.get(holder.getAdapterPosition()).getMusicID()));
        holder.mButton4.setOnClickListener(v -> mContext.startActivity(Intent.createChooser(Utils.Audio.createShareSongFileIntent(mMusicItems.get(holder.getAdapterPosition()), mContext), null)));

        holder.mItemMenuButton.setOnClickListener(v -> {
            holder.mPopupMenu.show();
        });
        holder.mPopupMenu.setOnMenuItemClickListener(item -> {

            @SuppressWarnings("UnnecessaryLocalVariable") int index = holder.getAdapterPosition();

            Values.CurrentData.CURRENT_SELECT_ITEM_INDEX_WITH_ITEM_MENU = index;

            switch (item.getItemId()) {
                //noinspection PointlessArithmeticExpression
                case Menu.FIRST + 0: {
                    Data.sNextWillPlayItem = mMusicItems.get(holder.getAdapterPosition());
                }
                break;

                //add to list
                case Menu.FIRST + 2: {
                    Utils.DataSet.addListDialog(mMainActivity, mMusicItems.get(holder.getAdapterPosition()));
                }
                break;

                /* in PublicActivity */
                case Menu.FIRST + 3: {

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

                //share
                case Menu.FIRST + 6: {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    Log.d(TAG, "onCreateViewHolder: share: " + new File(mMusicItems.get(holder.getAdapterPosition()).getMusicPath()).exists());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName(), new File(mMusicItems.get(holder.getAdapterPosition()).getMusicPath())));
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setType("audio/*");
                    } else {
                        intent.setDataAndType(Uri.fromFile(new File(mMusicItems.get(holder.getAdapterPosition()).getMusicPath())), "audio/*");
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    /*
                     * by Karim Abou Zeid (kabouzeid)
                     * */
                    try {
                        mContext.startActivity(intent);
                    } catch (IllegalArgumentException e) {
                        // TODO the path is most likely not like /storage/emulated/0/... but something like /storage/28C7-75B0/...
                        e.printStackTrace();
                        Toast.makeText(mContext, "Could not share this file, I'm aware of the issue.", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }

            Values.CurrentData.CURRENT_SELECT_ITEM_INDEX_WITH_ITEM_MENU = -1;

            return true;
        });

        holder.itemView.setOnLongClickListener(v -> {
            for (int id : Data.sSelections) {
                if (id == mMusicItems.get(holder.getAdapterPosition()).getMusicID()) {
                    holder.mBody.setBackgroundColor(ContextCompat.getColor(mContext, R.color.card_bg));
                    Data.sSelections.remove((Integer) mMusicItems.get(holder.getAdapterPosition()).getMusicID());
                    if (Data.sSelections.size() == 0) {
                        IS_CHOOSE = false;
                        mMainActivity.inflateCommonMenu(mMainActivity.getMainBinding().toolBar);
                    }
                    return true;
                }
            }

            Data.sSelections.add(mMusicItems.get(holder.getAdapterPosition()).getMusicID());
            IS_CHOOSE = true;

            holder.mBody.setBackgroundColor(Utils.Ui.getAccentColor(mContext));

            mMainActivity.inflateChooseMenu();
            MainActivity.CURRENT_MENU = MainActivity.MENU_CHOOSE;
            return true;
        });


        return holder;
    }

    private void onMusicItemClick(View view, ViewHolder holder) {
        view.setOnClickListener(v -> {
            //在多选模式下
            if (IS_CHOOSE) {
                for (int id : Data.sSelections) {
                    if (id == mMusicItems.get(holder.getAdapterPosition()).getMusicID()) {
                        ((ItemHolder) holder).mBody.setBackgroundColor(ContextCompat.getColor(mContext, R.color.card_bg));
                        Data.sSelections.remove((Integer) mMusicItems.get(holder.getAdapterPosition()).getMusicID());
                        if (Data.sSelections.size() == 0) {
                            IS_CHOOSE = false;
                            mMainActivity.inflateCommonMenu(mMainActivity.getMainBinding().toolBar);
                        }
                        return;
                    }
                }

                Data.sSelections.add(mMusicItems.get(holder.getAdapterPosition()).getMusicID());
                ((ItemHolder) holder).mBody.setBackgroundColor(Utils.Ui.getAccentColor(mContext));
            } else {

                //在通常模式（非多选）下
                final Disposable disposable = Observable.create((ObservableOnSubscribe<Integer>) observableEmitter -> {

                    //设置全局ITEM
//                Data.sCurrentMusicItem = mMusicItems.get(holder.getAdapterPosition());

                    //cover set
                    Bitmap bitmap = Utils.Audio.getCoverBitmap(mContext, mMusicItems.get(holder.getAdapterPosition()).getAlbumId());
                    Data.setCurrentCover(bitmap);

                    for (int i = 0; i < Data.sMusicItems.size(); i++) {
                        MusicItem item = Data.sMusicItems.get(i);
                        if (item.getMusicID() == mMusicItems.get(holder.getAdapterPosition()).getMusicID()) {
//                            Log.d(TAG, "onMusicItemClick: " + holder.getAdapterPosition() + " " + i + " " + item.getMusicName());
                            observableEmitter.onNext(i);
                            break;
                        }
                    }
                }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(integer -> Utils.SendSomeThing.sendPlay(mContext, ReceiverOnMusicPlay.TYPE_ITEM_CLICK, integer.toString()), Throwable::printStackTrace);
                Data.sDisposables.add(disposable);

                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... voids) {
                        //因为mMusicItems 与 Data.sPlayOrderList 不同步, 所以需要转换index
                        //MUSIC INDEX
                        for (int i = 0; i < Data.sPlayOrderList.size(); i++) {
                            if (Data.sPlayOrderList.get(i).getMusicID() == mMusicItems.get(holder.getAdapterPosition()).getMusicID()) {
                                Values.CurrentData.CURRENT_MUSIC_INDEX = i;
                            }
                        }
                        return null;
                    }
                }.execute();
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        final ItemHolder holder = ((ItemHolder) viewHolder);

        //check selection
        if (Data.sSelections.contains(mMusicItems.get(holder.getAdapterPosition()).getMusicID())) {
            holder.mBody.setBackgroundColor(Utils.Ui.getAccentColor(mContext));
        } else {
            holder.mBody.setBackgroundColor(ContextCompat.getColor(mContext, R.color.card_bg));
        }

        holder.mMusicText.setText(mMusicItems.get(holder.getAdapterPosition()).getMusicName());
        holder.mMusicAlbumName.setText(mMusicItems.get(holder.getAdapterPosition()).getMusicAlbum());
        String prefix = mMusicItems.get(holder.getAdapterPosition()).getMusicPath().substring(mMusicItems.get(holder.getAdapterPosition()).getMusicPath().lastIndexOf(".") + 1);
        holder.mMusicExtName.setText(prefix);
        holder.mTime.setText(Data.sSimpleDateFormat.format(new Date(mMusicItems.get(holder.getAdapterPosition()).getDuration())));

        ItemCoverThreadPool.post(() -> {
            /* show song name, use songNameList */
            Values.CurrentData.CURRENT_BIND_INDEX_MUSIC_LIST = holder.getAdapterPosition();

            /*--- 添加标记以便避免ImageView因为ViewHolder的复用而出现混乱 ---*/
            holder.mMusicCoverImage.setTag(R.string.key_id_1, holder.getAdapterPosition());

            try {
                albumLoader(mMainActivity, holder.mMusicCoverImage, mMusicItems.get(holder.getAdapterPosition()).getAlbumId()
                        , mMusicItems.get(holder.getAdapterPosition()).getArtist(), mMusicItems.get(holder.getAdapterPosition()).getMusicAlbum(), holder.getAdapterPosition());
            } catch (Exception e) {
                e.printStackTrace();
            }

            switch (mStyleId) {
                case 1: {
                    ItemHolderS1 holderS1 = (ItemHolderS1) holder;
                    final Bitmap bitmap = Utils.Ui.readBitmapFromFile(Utils.Audio.getCoverPath(mContext, mMusicItems.get(holder.getAdapterPosition()).getAlbumId()), 50, 50);
                    if (bitmap != null) {
                        //color set (album tag)
                        Palette.from(bitmap).generate(p -> {
                            if (p != null) {
                                @ColorInt int color = p.getVibrantColor(ContextCompat.getColor(mMainActivity, R.color.notVeryBlack));
                                GradientDrawable drawable = new GradientDrawable();
                                drawable.setStroke((int) mContext.getResources().getDimension(R.dimen.frame_width) * 2, color);
                                drawable.setCornerRadius(mContext.getResources().getDimension(R.dimen.frame_width));
                                holderS1.mFrame.setBackground(drawable);
                                bitmap.recycle();
                            }
                        });
                    }

                }
                break;
            }

        });
    }

    /**
     * loader
     * load image to imageView (net, defDB, customDB, defAlbum)
     */
    private void albumLoader(Activity activity, ImageView imageView, int albumId, String artist, String albumName, int index) {
        final String[] albumPath = {null};

        final Cursor cursor = activity.getContentResolver().query(
                Uri.parse(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI + String.valueOf(File.separatorChar) + albumId)
                , new String[]{MediaStore.Audio.Albums.ALBUM_ART}, null, null, null);
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            albumPath[0] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
            cursor.close();
        }

        //检查是否勾选了网络Album
        if (PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(Values.SharedPrefsTag.USE_NET_WORK_ALBUM, false)) {
            final List<CustomAlbumPath> customs = LitePal.where("mAlbumId = ?", String.valueOf(albumId)).find(CustomAlbumPath.class);
            if (customs.size() != 0) {
                final CustomAlbumPath custom = customs.get(0);
                try {

                    //如果系统扫描的Album是否存在且正确
                    if (TextUtils.isEmpty(albumPath[0]) || custom.isForceUse()) {
                        File file = new File(custom.getAlbumArt());

                        //判断CUSTOM_DB下albumArt是否存在
                        if (custom.getAlbumArt().equals("null") && !file.exists()) {
                            //download
                            StringBuilder request = new StringBuilder("http://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=")
                                    .append(MyApplication.LAST_FM_KEY)
                                    .append("&artist=")
                                    .append(artist)
                                    .append("&album=")
                                    .append(albumName);
                            HttpUtil httpUtil = new HttpUtil();
                            httpUtil.sedOkHttpRequest(request.toString(), new Callback() {
                                @Override
                                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                    activity.runOnUiThread(() -> Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show());
                                }

                                @Override
                                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                    if (response.body() != null) {

                                        final Document document = Jsoup.parse(response.body().string(), "UTF-8", new Parser(new XmlTreeBuilder()));
                                        final Elements content = document.getElementsByAttribute("status");
                                        String status = content.select("lfm[status]").attr("status");

                                        if (status.equals("ok")) {
                                            StringBuilder img = new StringBuilder(content.select("image[size=extralarge]").text());

                                            if (img.toString().contains("http") && img.toString().contains("https")) {
                                                Log.d(TAG, "onResponse: ok, now downloading...");

                                                List<CustomAlbumPath> list = LitePal.where("mAlbumId = ?", String.valueOf(albumId)).find(CustomAlbumPath.class);

                                                DownloadUtil.get().download(img.toString(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + "AlbumCovers"
                                                        , albumId + "." + img.substring(img.lastIndexOf(".") + 1), new DownloadUtil.OnDownloadListener() {
                                                            @Override
                                                            public void onDownloadSuccess(File file) {
                                                                Log.d(TAG, "onDownloadSuccess: " + file.getAbsolutePath());
                                                                content.clear();

                                                                String newPath = file.getAbsolutePath();
                                                                CustomAlbumPath c = list.get(0);
                                                                c.setAlbumArt(newPath);
                                                                c.save();

                                                                if (verify(imageView, index)) {
                                                                    Log.d(TAG, "onDownloadSuccess: load... from custom(justDownload)");
                                                                    imageView.post(() -> GlideApp.with(activity)
                                                                            .load(file)
                                                                            .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                                                                            .centerCrop()
                                                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                                            .override(100, 100)
                                                                            .into(imageView));
//                                                                            imageView.setTag(R.string.key_id_1, null);

                                                                    //UPDATE CURRENT DATA if this bind item now playing
                                                                    if (Data.sCurrentMusicItem.getAlbumId() == albumId && Data.getCurrentCover() == null) {
                                                                        Data.setCurrentCover(BitmapFactory.decodeFile(albumPath[0]));
                                                                    }
                                                                }
                                                            }

                                                            @Override
                                                            public void onDownloading(int progress) {

                                                            }

                                                            @Override
                                                            public void onDownloadFailed(Exception e) {
                                                                Log.d(TAG, "onDownloadFailed: " + img.toString() + " " + e.getMessage());
                                                            }
                                                        });
                                            } else {
                                                Log.d(TAG, "onResponse: img url error" + img);
                                            }
                                        } else {
                                            Log.d(TAG, "onResponse: result not ok");
                                            loadFromDefaultDB(albumPath[0], imageView, index);
                                        }
                                    } else {
                                        activity.runOnUiThread(() -> Toast.makeText(activity, "response is NUll!", Toast.LENGTH_SHORT).show());
                                    }
                                }
                            });
                        } else {
                            Log.d(TAG, "albumLoader: has data in DB, loading...");
                            if (verify(imageView, index)) {
                                imageView.post(() -> GlideApp.with(mMainActivity)
                                        .load(file)
                                        .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .override(100, 100)
                                        .into(imageView));
                            }
                        }
                    } else {
                        Log.d(TAG, "albumLoader: File exists or force not open, loading default...");
                        loadFromDefaultDB(albumPath[0], imageView, index);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "albumLoader: load customAlbum Error, loading default..., msg: " + e.getMessage());
                    loadFromDefaultDB(albumPath[0], imageView, index);
                }
            } else {
                Log.d(TAG, "customDB size is 0");
                loadFromDefaultDB(albumPath[0], imageView, index);
            }
        } else {
            Log.d(TAG, "albumLoader: load default..., msg: FROM NET switch not checked");
            if (albumPath[0] != null && !TextUtils.isEmpty(albumPath[0])) {
                File file = new File(albumPath[0]);
                if (file.exists()) {
                    Log.d(TAG, "albumLoader: exists...");
                    loadFromDefaultDB(albumPath[0], imageView, index);
                }
            } else {
                Log.d(TAG, "albumLoader: not exists");
                imageView.post(() -> imageView.post(() -> GlideApp.with(imageView)
                        .load(R.drawable.default_album_art)
                        .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                        .centerCrop()
                        .override(100, 100)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(imageView)));
            }
        }
    }

    /**
     * load defaultAlbumImage by DB(from {@link MediaStore.Audio.Albums#ALBUM_ART})
     *
     * @deprecated
     */
    private void loadCoverDefault(Context context, @Nullable ImageView imageView, int album, int index) {
        if (imageView == null) {
            Log.e(TAG, "imageView null");
        } else {
            if (imageView.getTag(R.string.key_id_1) == null) {
                Log.d(TAG, "key null");
            } else {
                //根据position判断是否为复用ViewHolder
                if (((int) imageView.getTag(R.string.key_id_1)) != index) {
                    Log.e(TAG, "doInBackground: key error------------------skip");
                } else {
                    imageView.post(() -> GlideApp.with(context)
                            .load(Utils.Audio.getCoverBitmap(context, album))
                            .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                            .centerCrop()
                            .override(100, 100)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(imageView));
                }
            }
        }
    }

    /**
     * load from defaultDB {@link MediaStore.Audio.Albums}
     */
    private void loadFromDefaultDB(String path, @NonNull ImageView imageView, int index) {
        if (verify(imageView, index)) {
            Log.d(TAG, "loadFromDefaultDB: " + path);
            imageView.post(() -> imageView.post(() -> GlideApp.with(imageView)
                    .load(path)
                    .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                    .centerCrop()
                    .override(100, 100)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imageView)));
        }
    }

    /**
     * verify if key, null
     */
    private boolean verify(@Nullable ImageView imageView, int index) {
        boolean flag = false;
        if (imageView == null) {
            Log.e(TAG, "imageView null clear_image");
        } else {
            if (imageView.getTag(R.string.key_id_1) == null) {
                Log.e(TAG, "key null clear_image");
                GlideApp.with(imageView).clear(imageView);
            } else {
                //根据position判断是否为复用ViewHolder
                if (((int) imageView.getTag(R.string.key_id_1)) != index) {
                    Log.e(TAG, "key error clear_image");
                    GlideApp.with(imageView).clear(imageView);
                } else {
                    flag = true;
                }
            }
        }
        return flag;
    }

    @Override
    public int getItemCount() {
        return mMusicItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return MOD_TYPE;
        }
        return 0;
    }

    public void clearSelection() {
        for (ItemHolder holder : mViewHolders) {
            holder.mBody.setBackgroundColor(ContextCompat.getColor(mContext, R.color.card_bg));
        }
        Data.sSelections.clear();
        mMainActivity.inflateCommonMenu(mMainActivity.getMainBinding().toolBar);
        MainActivity.CURRENT_MENU = MainActivity.MENU_COMMON;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class ItemHolder extends ViewHolder {

        View mBody;

        TextView mExpandText;

        ImageView mMusicCoverImage;

        ImageView mItemMenuButton;

        TextView mMusicText;

        TextView mMusicAlbumName;

        TextView mMusicExtName;

        TextView mTime;

        PopupMenu mPopupMenu;

        Menu mMenu;

        ConstraintLayout mExpandView;

        Button mButton1;

        Button mButton2;

        Button mButton3;

        Button mButton4;

        ItemHolder(@NonNull View itemView) {
            super(itemView);
            mMusicAlbumName = itemView.findViewById(R.id.recycler_item_music_album_name);
            mMusicCoverImage = itemView.findViewById(R.id.recycler_item_album_image);
            mMusicText = itemView.findViewById(R.id.recycler_item_music_name);
            mItemMenuButton = itemView.findViewById(R.id.recycler_item_menu);
            mMusicExtName = itemView.findViewById(R.id.recycler_item_music_type_name);
            mTime = itemView.findViewById(R.id.recycler_item_music_duration);
            mExpandView = itemView.findViewById(R.id.music_item_expand_view);
            mBody = itemView.findViewById(R.id.recycler_music_item_view);
            mExpandText = itemView.findViewById(R.id.expand_text);

            mButton1 = itemView.findViewById(R.id.expand_button_1);
            mButton2 = itemView.findViewById(R.id.expand_button_2);
            mButton3 = itemView.findViewById(R.id.expand_button_3);
            mButton4 = itemView.findViewById(R.id.expand_button_share);

            mPopupMenu = new PopupMenu(mContext, mItemMenuButton);
            mMenu = mPopupMenu.getMenu();

            final Resources resources = mContext.getResources();

            //Menu Load
            //noinspection PointlessArithmeticExpression
            mMenu.add(Menu.NONE, Menu.FIRST + 0, 0, resources.getString(R.string.next_play));
            mMenu.add(Menu.NONE, Menu.FIRST + 2, 0, resources.getString(R.string.add_to_playlist));
            if (!mCurrentUiPosition.equals(AlbumDetailActivity.TAG))
                mMenu.add(Menu.NONE, Menu.FIRST + 4, 0, resources.getString(R.string.show_album));
            mMenu.add(Menu.NONE, Menu.FIRST + 5, 0, resources.getString(R.string.more_info));
            mMenu.add(Menu.NONE, Menu.FIRST + 6, 0, resources.getString(R.string.share));

            MenuInflater menuInflater = mContext.getMenuInflater();
            menuInflater.inflate(R.menu.recycler_song_item_menu, mMenu);
        }
    }

    class ModHolder extends ItemHolder {

        ConstraintLayout mRandomItem;

        ModHolder(@NonNull View itemView) {
            super(itemView);
            mRandomItem = itemView.findViewById(R.id.random_play_item);
        }
    }

    ///////////////////////////////////////////

    class ItemHolderS1 extends ItemHolder {

        ConstraintLayout mFrame;

        ItemHolderS1(@NonNull View itemView) {
            super(itemView);
            mFrame = itemView.findViewById(R.id.line_set);
        }
    }

    class ModHolderS1 extends ItemHolderS1 {

        ConstraintLayout mRandomItem;

        ModHolderS1(@NonNull View itemView) {
            super(itemView);
            mRandomItem = itemView.findViewById(R.id.random_play_item);
        }
    }
}
