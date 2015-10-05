package fr.xebia.devoxx.be.ui;

import android.os.Bundle;

import fr.xebia.devoxx.be.R;
import fr.xebia.devoxx.be.core.activity.BaseActivity;
import fr.xebia.devoxx.be.ui.schedule.ScheduleFragment;


public class HomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        currentNavId = R.id.nav_talks;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (!isFinishing() && savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_container, new ScheduleFragment(), HOME_FRAG_TAG)
                    .commit();
        }
    }

}
