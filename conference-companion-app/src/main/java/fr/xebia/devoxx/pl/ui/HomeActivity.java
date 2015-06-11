package fr.xebia.devoxx.pl.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

import fr.xebia.devoxx.pl.R;
import fr.xebia.devoxx.pl.core.activity.BaseActivity;
import fr.xebia.devoxx.pl.ui.navigation.DrawerAdapter;
import fr.xebia.devoxx.pl.ui.schedule.ScheduleFragment;


public class HomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
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
    protected int getSelfNavDrawerItem() {
        return DrawerAdapter.MENU_TALKS;
    }

    @Override
    public void onNavigationDrawerToggle(boolean opened) {
        super.onNavigationDrawerToggle(opened);
        if (!opened) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
        }
    }

}
