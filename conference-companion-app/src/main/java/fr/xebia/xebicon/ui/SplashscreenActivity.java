package fr.xebia.xebicon.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import fr.xebia.xebicon.BuildConfig;
import fr.xebia.xebicon.R;
import fr.xebia.xebicon.bus.SynchroFinishedEvent;
import fr.xebia.xebicon.core.misc.Preferences;
import fr.xebia.xebicon.service.SynchroIntentService;

import static fr.xebia.xebicon.core.XebiConApplication.BUS;

public class SplashscreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splashscreen_activity);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (!Preferences.hasSelectedConference(this)){
            Intent intent = new Intent(this, SynchroIntentService.class);
            intent.putExtra(SynchroIntentService.EXTRA_CONFERENCE_ID, BuildConfig.XEBICON_CONFERENCE_ID);
            startService(intent);
        } else {
            gotToHome();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        BUS.register(this);
    }

    @Override
    public void onStop() {
        BUS.unregister(this);
        super.onStop();
    }

    public void onEventMainThread(SynchroFinishedEvent synchroFinishedEvent) {
        gotToHome();
    }

    private void gotToHome() {
        Intent homeIntent = new Intent(this, ExploreActivity.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
    }
}
