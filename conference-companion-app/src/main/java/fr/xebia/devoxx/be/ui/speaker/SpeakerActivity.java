package fr.xebia.devoxx.be.ui.speaker;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

import fr.xebia.devoxx.be.R;
import fr.xebia.devoxx.be.core.activity.BaseActivity;

public class SpeakerActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speaker_activity);

        currentNavId = R.id.nav_speakers;

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.speakers);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_content, new SpeakerFragment())
                    .commit();
        }
    }

}
