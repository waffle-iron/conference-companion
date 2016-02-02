package fr.xebia.voxxeddays.zurich.ui.speaker;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

import fr.xebia.voxxeddays.zurich.R;
import fr.xebia.voxxeddays.zurich.core.activity.NavigationActivity;

public class SpeakerActivity extends NavigationActivity {

    public SpeakerActivity() {
        super(R.layout.speaker_activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.speakers);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_content, new SpeakerFragment())
                    .commit();
        }
    }

    @Override
    protected int getNavId() {
        return R.id.nav_speakers;
    }
}
