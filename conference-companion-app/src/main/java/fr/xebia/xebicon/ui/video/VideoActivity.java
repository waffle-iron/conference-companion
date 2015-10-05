package fr.xebia.xebicon.ui.video;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

import fr.xebia.xebicon.R;
import fr.xebia.xebicon.core.activity.BaseActivity;

public class VideoActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_library);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.nav_video);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new VideoFragment(), "VIDEOS")
                    .commit();
        }
    }
}
