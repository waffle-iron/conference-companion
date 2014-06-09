package fr.xebia.conference.companion.ui.conference;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.bus.ConferenceSelectedEvent;
import fr.xebia.conference.companion.bus.SynchroFinishedEvent;
import fr.xebia.conference.companion.core.misc.Preferences;
import fr.xebia.conference.companion.service.BluetoothLocationIntentService;
import fr.xebia.conference.companion.ui.HomeActivity;
import fr.xebia.conference.companion.ui.synchro.SynchroFragment;

import static fr.xebia.conference.companion.core.KouignAmanApplication.BUS;

public class ConferenceChooserActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conference_chooser_activity);
        getWindow().setBackgroundDrawableResource(android.R.color.white);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.conferences);

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
            Preferences.setSelectedConference(this, synchroFinishedEvent.conferenceId);
            startService(new Intent(this, BluetoothLocationIntentService.class));
            startActivity(new Intent(this, HomeActivity.class));
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
}
