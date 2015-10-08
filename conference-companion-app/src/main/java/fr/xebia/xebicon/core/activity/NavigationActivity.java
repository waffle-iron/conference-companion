package fr.xebia.xebicon.core.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import butterknife.InjectView;
import fr.xebia.xebicon.R;
import fr.xebia.xebicon.ui.ExploreActivity;
import fr.xebia.xebicon.ui.about.AboutActivity;
import fr.xebia.xebicon.ui.expert.ExpertActivity;
import fr.xebia.xebicon.ui.map.MapActivity;
import fr.xebia.xebicon.ui.schedule.MyScheduleActivity;
import fr.xebia.xebicon.ui.settings.SettingsActivity;
import fr.xebia.xebicon.ui.speaker.SpeakerActivity;
import fr.xebia.xebicon.ui.timeline.TimelineActivity;
import fr.xebia.xebicon.ui.video.VideoActivity;

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
        switch (item.getItemId()){
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

        switch (menuItem.getItemId()){
            case R.id.nav_myschedule:
                goTo(new Intent(this, MyScheduleActivity.class));
                break;

            case R.id.nav_talks:
                goTo(new Intent(this, ExploreActivity.class));
                break;

            case R.id.nav_speakers:
                goTo(new Intent(this, SpeakerActivity.class));
                break;

            case R.id.nav_experts:
                goTo(new Intent(this, ExpertActivity.class));
                break;

            case R.id.nav_timeline:
                goTo(new Intent(this, TimelineActivity.class));
                break;

            case R.id.nav_video:
                goTo(new Intent(this, VideoActivity.class));
                break;

            case R.id.nav_about:
                goTo(new Intent(this, AboutActivity.class));
                break;

            case R.id.nav_settings:
                goTo(new Intent(this, SettingsActivity.class));
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
        handler.postDelayed(() -> {
            startActivity(intent);
            finish();
        }, 300);
    }

    protected int getNavId(){
        return R.id.nav_talks;
    }
}
