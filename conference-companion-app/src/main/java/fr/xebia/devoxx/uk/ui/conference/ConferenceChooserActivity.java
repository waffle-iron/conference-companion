package fr.xebia.devoxx.uk.ui.conference;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.Toast;

import fr.xebia.devoxx.uk.R;
import fr.xebia.devoxx.uk.bus.ConferenceSelectedEvent;
import fr.xebia.devoxx.uk.bus.SynchroFinishedEvent;
import fr.xebia.devoxx.uk.core.activity.BaseActivity;
import fr.xebia.devoxx.uk.ui.HomeActivity;
import fr.xebia.devoxx.uk.ui.synchro.SynchroFragment;

import static fr.xebia.devoxx.uk.core.KouignAmanApplication.BUS;

public class ConferenceChooserActivity extends BaseActivity {

    public static final String EXTRA_SHOW_HOME = "fr.xebia.conference.companion.EXTRA_SHOW_HOME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conference_chooser_activity);
        mDontCheckConference = true;
        getWindow().setBackgroundDrawableResource(android.R.color.white);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.conferences);
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new ConferenceChooserFragment(), ConferenceChooserFragment.TAG)
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        BUS.register(this);
    }

    public void onEventMainThread(ConferenceSelectedEvent conferenceSelectedEvent) {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, SynchroFragment.newInstance(conferenceSelectedEvent.conferenceId), SynchroFragment.TAG)
                .addToBackStack("synchro")
                .commit();
    }

    public void onEventMainThread(SynchroFinishedEvent synchroFinishedEvent) {
        if (synchroFinishedEvent.success) {
            Intent homeIntent = new Intent(this, HomeActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(homeIntent);
            finish();
        } else {
            Toast.makeText(this, R.string.synchro_failed, Toast.LENGTH_LONG).show();
            getFragmentManager().popBackStack();
        }

    }

    @Override
    protected void onStop() {
        BUS.unregister(this);
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
