package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.app.Activity;
import android.os.Bundle;

import top.geek_studio.chenlongcould.musicplayer.R;

public class PublicActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recent);

        String type = getIntent().getStringExtra("start_by");
        switch (type) {
            case "add recent": {
                // TODO: 2018/11/11  add recent
            }
            break;
            case "favourite music": {
                // TODO: 2018/11/11  favourite music

            }
            break;
            default:
        }
    }
}
