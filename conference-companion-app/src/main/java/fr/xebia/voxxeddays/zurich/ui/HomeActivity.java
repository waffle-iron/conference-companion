package fr.xebia.voxxeddays.zurich.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.voxxeddays.zurich.R;
import fr.xebia.voxxeddays.zurich.core.activity.NavigationActivity;
import fr.xebia.voxxeddays.zurich.ui.schedule.ScheduleFragment;


public class HomeActivity extends NavigationActivity {

    @InjectView(R.id.toolbar) Toolbar toolbar;

    public HomeActivity() {
        super(R.layout.home_activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSupportActionBar(toolbar);
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

    @Override
    protected int getNavId() {
        return R.id.nav_talks;
    }
}
