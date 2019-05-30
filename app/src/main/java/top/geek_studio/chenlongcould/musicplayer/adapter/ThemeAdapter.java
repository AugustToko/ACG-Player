/*
 * ************************************************************
 * 文件：ThemeAdapter.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月18日 18:58:29
 * 上次修改时间：2019年01月18日 12:12:48
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.adapter;

import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.geeklibrary.theme.Theme;
import top.geek_studio.chenlongcould.geeklibrary.theme.ThemeStore;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.ThemeActivity;
import top.geek_studio.chenlongcould.musicplayer.databinding.DialogThemeBinding;
import top.geek_studio.chenlongcould.musicplayer.utils.PreferenceUtil;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

	public static final String TAG = "ThemeAdapter";

	private List<Theme> mThemes;

	private ThemeActivity mThemeActivity;

	private ArrayList<Disposable> mDisposables = new ArrayList<>();

	private SharedPreferences preferences;

	public ThemeAdapter(ThemeActivity themeActivity, List<Theme> themes) {
		mThemes = themes;
		mThemeActivity = themeActivity;
		preferences = PreferenceUtil.getDefault(themeActivity);
	}

	@NonNull
	@Override
	public String getSectionName(int position) {
		return String.valueOf(mThemes.get(position).getTitle().charAt(0));
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int itemType) {
		View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_theme_item, viewGroup, false);
		ViewHolder holder = new ViewHolder(view);

		view.setOnClickListener(v -> createDetailDialog(holder));

		holder.mItemMenu.setOnClickListener(v -> holder.mPopupMenu.show());

		view.setOnLongClickListener(v -> {
			holder.mPopupMenu.show();
			return true;
		});

		holder.mPopupMenu.setOnMenuItemClickListener(item -> {
			switch (item.getItemId()) {

				//del
				case Menu.FIRST: {
					if (preferences.getString(Values.SharedPrefsTag.SELECT_THEME, "null").equals(mThemes.get(holder.getAdapterPosition()).getId())) {
						Toast.makeText(mThemeActivity, mThemeActivity.getString(R.string.theme_is_in_use), Toast.LENGTH_SHORT).show();
						break;
					}

					AlertDialog.Builder sure = new AlertDialog.Builder(mThemeActivity);
					sure.setCancelable(true);
					sure.setTitle("Are you sure?");
					sure.setMessage("REMOVE?");
					sure.setNeutralButton("Yes", (dialog12, which12) -> {
						Disposable disposable = Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
							Utils.IO.delFolder(mThemes.get(holder.getAdapterPosition()).getPath());
						}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
								.subscribe(result -> {
									dialog12.dismiss();
									mThemeActivity.getThemes().clear();
									mThemeActivity.loadDataUI();
								});
						disposable.dispose();
						dialog12.cancel();
					});
					sure.setNegativeButton("Cancel", (dialog1, which1) -> dialog1.cancel());
					sure.show();
				}
				break;

				//save as
				case Menu.FIRST + 1: {
					final Disposable disposable = Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
						try {
							if (!Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).exists()) {
								boolean mkdirs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).mkdirs();
								Log.d(TAG, "onCreateViewHolder: makedirs " + mkdirs);
							}
							Utils.IO.zipDirectory(mThemes.get(holder.getAdapterPosition()).getPath()
									, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + File.separatorChar + new Date(System.currentTimeMillis()).toString() + ".zip");

						} catch (IOException e) {
							e.printStackTrace();
						}
					}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
							.subscribe(result -> {
								//...
								Toast.makeText(mThemeActivity, mThemeActivity.getString(R.string.saved_to_doc), Toast.LENGTH_SHORT).show();
							});
					mDisposables.add(disposable);
				}
				break;
				default:
			}
			return true;
		});

		return holder;
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Override
	public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

		if (mThemes.get(i).getId().equals(preferences.getString(Values.SharedPrefsTag.SELECT_THEME, "null"))) {
			((CardView) viewHolder.itemView).setCardBackgroundColor(ContextCompat.getColor(mThemeActivity, R.color.theme_in_use_item_bg_color));
		} else {
			((CardView) viewHolder.itemView).setCardBackgroundColor(ContextCompat.getColor(mThemeActivity, R.color.win_background_color));
		}

		viewHolder.mTitle.setText(mThemes.get(i).getTitle());
		viewHolder.mAuthor.setText(mThemes.get(i).getAuthor());
		viewHolder.mId.setText(String.valueOf(mThemes.get(i).getId()));

		viewHolder.mIco.setTag(R.string.key_id_2, i);

		final Disposable disposable = Observable.create((ObservableOnSubscribe<String>) emitter -> {
			if (viewHolder.mIco.getTag(R.string.key_id_2) == null)
				Log.e(TAG, "onBindViewHolder: tag null");

			if (((int) viewHolder.mIco.getTag(R.string.key_id_2)) != i) {
				Log.e(TAG, "onBindViewHolder: not much");
				emitter.onNext(null);
			}
			emitter.onNext(mThemes.get(i).getThumbnail());
		}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
				.subscribe(result -> {
					if (result == null) return;

					GlideApp.with(mThemeActivity)
							.load(result)
							.override(Values.MAX_HEIGHT_AND_WIDTH, Values.MAX_HEIGHT_AND_WIDTH)
							.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
//                            .diskCacheStrategy(DiskCacheStrategy.NONE)
							.into(viewHolder.mIco);
				});
		mDisposables.add(disposable);

	}

	@Override
	public int getItemCount() {
		return mThemes.size();
	}

	private void createDetailDialog(ViewHolder holder) {
		final DialogThemeBinding dialogThemeBinding = DataBindingUtil.inflate(mThemeActivity.getLayoutInflater(), R.layout.dialog_theme, null, false);
		final ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) dialogThemeBinding.nestedScrollView.getLayoutParams();
		params.height = 0;
		dialogThemeBinding.nestedScrollView.setLayoutParams(params);


		final AlertDialog.Builder builder = new AlertDialog.Builder(mThemeActivity);
		builder.setCancelable(true);

		//REMOVE THEME
		builder.setNeutralButton(mThemeActivity.getString(R.string.del), (dialog, which) -> {

			final String r1 = preferences.getString(Values.SharedPrefsTag.SELECT_THEME, null);
			final String r2 = mThemes.get(holder.getAdapterPosition()).getId();

			if (r1 != null && r1.equals(r2)) {
				Toast.makeText(mThemeActivity, mThemeActivity.getString(R.string.theme_is_in_use), Toast.LENGTH_SHORT).show();
				return;
			}

			final AlertDialog.Builder sure = new AlertDialog.Builder(mThemeActivity);
			sure.setCancelable(true);
			sure.setTitle(mThemeActivity.getString(R.string.are_u_sure));
			sure.setMessage(mThemeActivity.getString(R.string.remove_int));
			sure.setNeutralButton(mThemeActivity.getString(R.string.sure), (dialog12, which12) -> {
				Utils.IO.delFolder(mThemes.get(holder.getAdapterPosition()).getPath());
				mThemeActivity.loadDataUI();
				dialog12.dismiss();
				dialog12.cancel();
			});
			sure.setNegativeButton(mThemeActivity.getString(R.string.cancel), (dialog1, which1) -> {
				dialog1.cancel();
				createDetailDialog(holder);
			});
			sure.show();

		});

		//apply!!
		builder.setNegativeButton(mThemeActivity.getString(R.string.apply), (dialog, which) -> {
			Data.sTheme = mThemes.get(holder.getAdapterPosition());
			final SharedPreferences.Editor editor = preferences.edit();
			editor.putString(Values.SharedPrefsTag.SELECT_THEME, mThemes.get(holder.getAdapterPosition()).getId());
			editor.apply();
			dialog.dismiss();
			mThemeActivity.getThemes().clear();
			mThemeActivity.loadDataUI();
		});

		//setData
		dialogThemeBinding.title.setText(mThemes.get(holder.getAdapterPosition()).getTitle());
		dialogThemeBinding.author.setText(mThemes.get(holder.getAdapterPosition()).getAuthor());
		dialogThemeBinding.date.setText(mThemes.get(holder.getAdapterPosition()).getDate());
		dialogThemeBinding.idText.setText(String.valueOf(mThemes.get(holder.getAdapterPosition()).getId()));

		//load icon
		final Disposable disposableIcon = Observable.create((ObservableOnSubscribe<Bitmap>) emitter -> emitter.onNext(BitmapFactory.decodeFile(mThemes.get(holder.getAdapterPosition()).getThumbnail())))
				.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
				.subscribe(result -> GlideApp.with(mThemeActivity)
						.load(result)
						.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
//                        .diskCacheStrategy(DiskCacheStrategy.NONE)
						.into(dialogThemeBinding.ico));
		mDisposables.add(disposableIcon);

		//load nav preview
		if (mThemes.get(holder.getAdapterPosition()).getSupport_area().contains(ThemeStore.SupportArea.NAV)) {
			final Disposable disposable1 = Observable.create((ObservableOnSubscribe<Bitmap>) emitter -> {
				File[] imgs = new File(mThemes.get(holder.getAdapterPosition()).getPath() + File.separatorChar + ThemeStore.DIR_IMG_NAV).listFiles();
				emitter.onNext(BitmapFactory.decodeFile(imgs[0].getAbsolutePath()));
			}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
					.subscribe(result -> GlideApp.with(mThemeActivity)
							.load(result)
							.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
							.diskCacheStrategy(DiskCacheStrategy.NONE)
							.into(dialogThemeBinding.includeDialogContent.navImage));
			mDisposables.add(disposable1);
		}

		//load bg preview
		if (mThemes.get(holder.getAdapterPosition()).getSupport_area().contains(ThemeStore.SupportArea.BG)) {

			final Disposable disposableImgs = Observable.create((ObservableOnSubscribe<Bitmap>) emitter -> {
				final File[] imgs = new File(mThemes.get(holder.getAdapterPosition()).getPath() + File.separatorChar + ThemeStore.DIR_IMG_BG).listFiles();
				emitter.onNext(BitmapFactory.decodeFile(imgs[0].getAbsolutePath()));
			}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
					.subscribe(result -> GlideApp.with(mThemeActivity)
							.load(result)
							.override(1280, 720)
							.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
//                            .diskCacheStrategy(DiskCacheStrategy.NONE)
							.into(dialogThemeBinding.includeDialogContent.bgImage));
			mDisposables.add(disposableImgs);
		}

		//load slide preview
		if (mThemes.get(holder.getAdapterPosition()).getSupport_area().contains(ThemeStore.SupportArea.SLIDE)) {

			final Disposable disposable = Observable.create((ObservableOnSubscribe<Bitmap>) emitter -> {
				final File[] imgs = new File(mThemes.get(holder.getAdapterPosition()).getPath() + File.separatorChar + ThemeStore.DIR_IMG_SLIDE).listFiles();
				emitter.onNext(BitmapFactory.decodeFile(imgs[0].getAbsolutePath()));
			}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
					.subscribe(result -> GlideApp.with(mThemeActivity)
							.load(result)
							.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
//                            .diskCacheStrategy(DiskCacheStrategy.NONE)
							.into(dialogThemeBinding.includeDialogContent.slideImage));
			mDisposables.add(disposable);
		}
		builder.setView(dialogThemeBinding.getRoot());

		builder.show();

		new Handler().postDelayed(() -> {
			final ValueAnimator animator = new ValueAnimator();
			animator.setIntValues(0, (int) mThemeActivity.getResources().getDimension(R.dimen.nested_scroll_height));
			animator.addUpdateListener(animation -> {
				params.height = (int) animation.getAnimatedValue();
				dialogThemeBinding.nestedScrollView.setLayoutParams(params);
				dialogThemeBinding.nestedScrollView.requestLayout();
			});
			animator.start();
		}, 500);
	}

	///////////////////////////////////////////////////////////////////////////

	public ArrayList<Disposable> getDisposables() {
		return mDisposables;
	}

	class ViewHolder extends RecyclerView.ViewHolder {

		TextView mId;

		TextView mTitle;

		TextView mAuthor;

		ImageView mItemMenu;

		ImageView mIco;

		PopupMenu mPopupMenu;

		Menu mMenu;

		ViewHolder(@NonNull View itemView) {
			super(itemView);
			mTitle = itemView.findViewById(R.id.name);
			mItemMenu = itemView.findViewById(R.id.menu);
			mIco = itemView.findViewById(R.id.image);
			mAuthor = itemView.findViewById(R.id.author);
			mId = itemView.findViewById(R.id.theme_id_text);

			mPopupMenu = new PopupMenu(mThemeActivity, mItemMenu);
			mMenu = mPopupMenu.getMenu();

			Resources resources = mThemeActivity.getResources();

			mMenu.add(Menu.NONE, Menu.FIRST, 0, resources.getString(R.string.del));
			mMenu.add(Menu.NONE, Menu.FIRST + 1, 0, mThemeActivity.getString(R.string.save_as));
		}
	}
}
