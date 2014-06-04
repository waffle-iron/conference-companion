package fr.xebia.conference.companion.ui.schedule;

import android.app.Activity;
import android.os.Bundle;
import fr.xebia.conference.companion.R;

public class ScheduleActivity extends Activity {

    public static final String EXTRA_TRACK = "fr.xebia.devoxx.EXTRA_TRACK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_activity);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, ScheduleFragment.newInstanceForTrack(getIntent().getExtras().getString(EXTRA_TRACK)))
                    .commit();
        }
    }
}
