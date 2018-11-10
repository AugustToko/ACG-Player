package top.geek_studio.chenlongcould.musicplayer.Adapters;

import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;

public class MyAdapterTwo extends MyRecyclerAdapter {

    public MyAdapterTwo(List<String> musicPathList, List<String> musicNameList, List<String> songAlbumList, MainActivity activity) {
        super(musicPathList, musicNameList, songAlbumList, activity);
    }
}
