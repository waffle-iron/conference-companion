package fr.xebia.conference.companion.ui.schedule;

import android.os.Bundle;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.core.activity.BaseActivity;

public class ScheduleActivity extends BaseActivity {

    public static final String EXTRA_TRACK = "fr.xebia.devoxx.EXTRA_TRACK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_activity);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().hide();
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, ScheduleFragment.newInstanceForTrack(getIntent().getExtras().getString(EXTRA_TRACK)))
                    .commit();
        }
    }
}
