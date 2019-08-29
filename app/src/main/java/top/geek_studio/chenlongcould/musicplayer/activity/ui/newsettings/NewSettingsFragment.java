package top.geek_studio.chenlongcould.musicplayer.activity.ui.newsettings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceFragmentCompat;

import top.geek_studio.chenlongcould.musicplayer.R;

public class NewSettingsFragment extends PreferenceFragmentCompat {

	private NewSettingsViewModel mViewModel;

	public static NewSettingsFragment newInstance() {
		return new NewSettingsFragment();
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.settings, rootKey);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_settings_fragment, container, false);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mViewModel = ViewModelProviders.of(this).get(NewSettingsViewModel.class);
		// TODO: Use the ViewModel
	}

}
