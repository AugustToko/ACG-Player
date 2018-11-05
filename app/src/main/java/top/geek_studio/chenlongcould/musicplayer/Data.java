package top.geek_studio.chenlongcould.musicplayer;

public class Data {

    private MyRecyclerAdapter.ViewHolder mViewHolder;

    private String path;

    private int position;

    Data(MyRecyclerAdapter.ViewHolder viewHolder, String path, int position) {
        mViewHolder = viewHolder;
        this.path = path;
        this.position = position;
    }

    MyRecyclerAdapter.ViewHolder getViewHolder() {
        return mViewHolder;
    }

    String getPath() {
        return path;
    }

    public void setViewHolder(MyRecyclerAdapter.ViewHolder viewHolder) {
        mViewHolder = viewHolder;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
