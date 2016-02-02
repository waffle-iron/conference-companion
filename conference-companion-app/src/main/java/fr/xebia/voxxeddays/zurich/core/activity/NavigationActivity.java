package fr.xebia.voxxeddays.zurich.core.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import butterknife.InjectView;
import fr.xebia.voxxeddays.zurich.R;
import fr.xebia.voxxeddays.zurich.ui.HomeActivity;
import fr.xebia.voxxeddays.zurich.ui.schedule.MyScheduleActivity;
import fr.xebia.voxxeddays.zurich.ui.settings.SettingsActivity;
import fr.xebia.voxxeddays.zurich.ui.speaker.SpeakerActivity;
import fr.xebia.voxxeddays.zurich.ui.timeline.TimelineActivity;

import static android.support.v4.view.GravityCompat.START;

public abstract class NavigationActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;

    @InjectView(R.id.toolbar) Toolbar toolbar;
    @InjectView(R.id.nav_view) NavigationView navigationView;
    @InjectView(R.id.drawer_layout) DrawerLayout drawerLayout;

    private int currentNavId;
    private Handler handler = new Handler();

    public NavigationActivity(int layoutId) {
        super(layoutId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentNavId = getNavId();
        navigationView.getMenu().findItem(currentNavId).setCheckable(true);
        navigationView.setCheckedItem(currentNavId);
        navigationView.setNavigationItemSelectedListener(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        if (currentNavId == menuItem.getItemId()) {
            return false;
        }

        switch (menuItem.getItemId()) {
            case R.id.nav_myschedule:
                goTo(new Intent(NavigationActivity.this, MyScheduleActivity.class));
                break;

            case R.id.nav_talks:
                goTo(new Intent(NavigationActivity.this, HomeActivity.class));
                break;

            case R.id.nav_speakers:
                goTo(new Intent(NavigationActivity.this, SpeakerActivity.class));
                break;

            case R.id.nav_timeline:
                goTo(new Intent(NavigationActivity.this, TimelineActivity.class));
                break;

            case R.id.nav_settings:
                goTo(new Intent(NavigationActivity.this, SettingsActivity.class));
                break;

        }

        drawerLayout.closeDrawers();

        // fade out the main content
        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
        }

        return true;
    }

    private void goTo(final Intent intent) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
                finish();
            }
        }, 300);
    }

    protected int getNavId() {
        return R.id.nav_talks;
    }
}