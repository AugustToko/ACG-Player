/*
 * ************************************************************
 * 文件：FileViewFragment.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月14日 14:45:09
 * 上次修改时间：2019年01月14日 14:44:29
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Fragments;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.databinding.FragmentFileViewerBinding;
import top.geek_stusio.chenlongcould.geeklibrary.OpenFile;

public final class FileViewFragment extends Fragment {

    private FragmentFileViewerBinding mFileViewerBinding;

    private boolean HAS_LOAD = false;

    private MainActivity mMainActivity;

    private List<File> mFileItems = new ArrayList<>();

    private MyAdapter mMyAdapter;

    private File mCurrentFile;

    public static FileViewFragment newInstance() {
        return new FileViewFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainActivity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFileViewerBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_file_viewer, container, false);
        mFileViewerBinding.includeRecyclerView.recyclerView.setLayoutManager(new LinearLayoutManager(mMainActivity));
        mFileViewerBinding.includeRecyclerView.recyclerView.addItemDecoration(new DividerItemDecoration(mMainActivity, DividerItemDecoration.VERTICAL));

        if (!HAS_LOAD) {
            final File file = Environment.getExternalStorageDirectory();
            mFileItems.addAll(new ArrayList<>(Arrays.asList(file.listFiles())));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) mFileItems.sort(File::compareTo);
            mCurrentFile = file;
        }

        mMyAdapter = new MyAdapter(mFileItems);
        mFileViewerBinding.includeRecyclerView.recyclerView.setAdapter(mMyAdapter);
        HAS_LOAD = true;

        return mFileViewerBinding.getRoot();
    }

    @Override
    public void onDetach() {
        mFileItems.clear();
        super.onDetach();
    }

    public File getCurrentFile() {
        return mCurrentFile;
    }

    public void onBackPressed() {
        mCurrentFile = mCurrentFile.getParentFile();
        mFileItems.clear();
        mFileItems.addAll(new ArrayList<>(Arrays.asList(mCurrentFile.listFiles())));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mFileItems.sort(File::compareTo);
        }
        mMyAdapter.notifyDataSetChanged();
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

        private List<File> mFiles;

        MyAdapter(List<File> files) {
            mFiles = files;
        }

        String getFileSize(File f) {
            float fileSize = f.length() / 1024;        //kb

            int sizeLength = String.valueOf((int) fileSize).length();
            if (sizeLength <= 3) {
                return String.valueOf((double) Math.round(fileSize * 100) / 100 + "KB");
            } else if (sizeLength < 7) {
                return String.valueOf((double) Math.round(fileSize / 1024 * 100) / 100 + "MB");
            } else {
                return String.valueOf((double) Math.round(fileSize / 1024 / 1024 * 100) / 100 + "GB");
            }

        }

        @Override
        public void onBindViewHolder(@NonNull MyAdapter.ViewHolder holder, int position) {

//            show file size (b kb mb gb)
            if (!mFiles.get(position).isDirectory()) {
                holder.subTile_2.setText(String.valueOf(getFileSize(mFiles.get(holder.getAdapterPosition()))));
            } else {
                holder.subTile_2.setText("---");
            }
            //local
            holder.subTitle.setText(String.valueOf(Data.sSimpleDateFormatFile.format(new Date(mFiles.get(position).lastModified()))));
            holder.itemText.setText(mFiles.get(position).getName());

            if (mFiles.get(position).isDirectory()) {
                GlideApp.with(mMainActivity).load(R.drawable.ic_folder_24px).into(holder.itemIco);
            } else {

                File file = mFiles.get(position);

                String end = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length()).toLowerCase();

                if ("mp4".equals(end) || "avi".equals(end) || "wmv".equals(end) || "mov".equals(end)) {
                    GlideApp.with(mMainActivity).load(R.drawable.ic_movie_creation_24px).into(holder.itemIco);

                } else if ("jpg".equals(end) || "png".equals(end) || "gif".equals(end) || "psd".equals(end) || "ai".equals(end)) {
                    GlideApp.with(mMainActivity).load(R.drawable.ic_image_24px).into(holder.itemIco);

                } else if ("mp3".equals(end) || "wav".equals(end) || "dsd".equals(end) || "dst".equals(end) || "ogg".equals(end) || "flac".equals(end)) {
                    GlideApp.with(mMainActivity).load(R.drawable.ic_audiotrack_24px).into(holder.itemIco);
                } else {
                    GlideApp.with(mMainActivity).load(R.drawable.ic_insert_drive_file_24px).into(holder.itemIco);
                }

            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_file_item, parent, false);
            ViewHolder holder = new ViewHolder(view);

            holder.itemView.setOnClickListener(v -> {

                if (mFileItems.get(holder.getAdapterPosition()).isFile()) {

                    Intent intent;

                    OpenFile.init("top.geek_studio.chenlongcould.musicplayer.fileprovider");
                    if ((intent = OpenFile.openFile(mMainActivity, mFileItems.get(holder.getAdapterPosition()).getPath())) != null) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(mMainActivity, "Open File", Toast.LENGTH_LONG).show();
                    }
                    return;
                }

                final ArrayList<File> temp = new ArrayList<>(Arrays.asList(mFileItems.get(holder.getAdapterPosition()).listFiles()));
                mCurrentFile = mFiles.get(holder.getAdapterPosition());
                mFileItems.clear();
                mFileItems.addAll(temp);
                //sort
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mFileItems.sort(File::compareTo);
                }
                mMyAdapter.notifyDataSetChanged();
            });

            return holder;
        }

        @NonNull
        @Override
        public String getSectionName(int position) {
            return String.valueOf(mFileItems.get(position).getName().charAt(0));
        }

        @Override
        public int getItemCount() {
            return mFileItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            ImageView itemIco;
            TextView itemText;
            TextView subTitle;
            TextView subTile_2;

            ViewHolder(View itemView) {
                super(itemView);
                itemIco = itemView.findViewById(R.id.item_ico);
                itemText = itemView.findViewById(R.id.item_text);
                subTitle = itemView.findViewById(R.id.item_small_text);
                subTile_2 = itemView.findViewById(R.id.item_small_text_2);
            }
        }

    }
}
