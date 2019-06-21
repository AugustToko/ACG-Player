package top.geek_studio.chenlongcould.musicplayer.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import org.jetbrains.annotations.NotNull;
import top.geek_studio.chenlongcould.geeklibrary.OpenFile;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.activity.main.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.databinding.FragmentFileViewerBinding;
import top.geek_studio.chenlongcould.musicplayer.model.Item;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

// FIXME: 2019/5/22 bugs...
public final class FileViewFragment extends BaseListFragment {

	public static final String TAG = "FileViewFragment";

	private FragmentFileViewerBinding mFileViewerBinding;

	private MainActivity mMainActivity;

	private List<File> mFileItems = new ArrayList<>();

	private MyAdapter mMyAdapter;

	private File mCurrentFile;

	public static FileViewFragment newInstance() {
		return new FileViewFragment();
	}

	@Override
	public FragmentType getFragmentType() {
		return FragmentType.FILE_VIEW_FRAGMENT;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mFileItems.clear();
	}

	@Override
	public void reloadData() {
		super.reloadData();
		mFileItems.clear();
		initData();
	}

	private void initData() {
		if (ContextCompat.checkSelfPermission(mMainActivity,
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
			final File file = Environment.getExternalStorageDirectory();
			if (file != null && file.exists()) {
				List<File> files = Arrays.asList(file.listFiles());
				if (files != null && files.size() > 0) {
					mFileItems.addAll(files);
				}
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					mFileItems.sort(File::compareTo);
				}
				mCurrentFile = file;
				mMyAdapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onAttach(@NotNull Context context) {
		super.onAttach(context);
		mMainActivity = (MainActivity) context;
	}

	@NotNull
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mFileViewerBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_file_viewer, container, false);
		mFileViewerBinding.includeRecyclerView.recyclerView.setLayoutManager(new LinearLayoutManager(mMainActivity));
		mFileViewerBinding.includeRecyclerView.recyclerView.addItemDecoration(new DividerItemDecoration(mMainActivity, DividerItemDecoration.VERTICAL));
		mMyAdapter = new MyAdapter(mFileItems);
		mFileViewerBinding.includeRecyclerView.recyclerView.setAdapter(mMyAdapter);

		initData();

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

	@Override
	public boolean removeItem(@Nullable Item item) {
		// none
		return false;
	}

	@Override
	public boolean addItem(@Nullable Item item) {
		// none
		return false;
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
				return (double) Math.round(fileSize * 100) / 100 + "KB";
			} else if (sizeLength < 7) {
				return (double) Math.round(fileSize / 1024 * 100) / 100 + "MB";
			} else {
				return (double) Math.round(fileSize / 1024 / 1024 * 100) / 100 + "GB";
			}

		}

		@Override
		public void onBindViewHolder(@NonNull MyAdapter.ViewHolder holder, int position) {

			if (position == 0) holder.itemView.setPadding(0, MainActivity.PADDING, 0, 0);

//            show file size (b kb mb gb)
			if (!mFiles.get(position).isDirectory()) {
				holder.subTile_2.setText(String.valueOf(getFileSize(mFiles.get(holder.getAdapterPosition()))));
			} else {
				holder.subTile_2.setText("---");
			}
			//local
			holder.subTitle.setText(String.valueOf(Data.S_SIMPLE_DATE_FORMAT_FILE.format(new Date(mFiles.get(position).lastModified()))));
			holder.itemText.setText(mFiles.get(position).getName());

			if (mFiles.get(position).isDirectory()) {
				GlideApp.with(mMainActivity).load(R.drawable.ic_folder_24px).into(holder.itemIco);
			} else {

				File file = mFiles.get(position);

				String end = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();

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

					OpenFile.init(mMainActivity.getPackageName());
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
		public void onViewRecycled(@NonNull ViewHolder holder) {
			super.onViewRecycled(holder);

			// reset
			holder.itemView.setPadding(0, 0, 0, 0);
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
