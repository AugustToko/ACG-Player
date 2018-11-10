package top.geek_studio.chenlongcould.musicplayer.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import top.geek_studio.chenlongcould.musicplayer.R;

public class PlayListFragment extends Fragment {
    //实例化一个fragment
    public static PlayListFragment newInstance(int index) {
        return new PlayListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play_list, container, false);
        return view;
    }

}
