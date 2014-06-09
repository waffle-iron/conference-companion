package fr.xebia.conference.companion.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import com.crashlytics.android.Crashlytics;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.bus.TagRegisteredEvent;
import fr.xebia.conference.companion.core.misc.Preferences;
import fr.xebia.conference.companion.core.misc.RestoreActionBarFragment;
import fr.xebia.conference.companion.ui.conference.ConferenceChooserActivity;
import fr.xebia.conference.companion.ui.navigation.DrawerAdapter;
import fr.xebia.conference.companion.ui.navigation.NavigationDrawerFragment;
import fr.xebia.conference.companion.ui.schedule.ScheduleFragment;
import fr.xebia.conference.companion.ui.speaker.SpeakerFragment;
import fr.xebia.conference.companion.ui.talk.track.TrackFragment;
import fr.xebia.conference.companion.ui.vote.ScanNfcFragment;
import fr.xebia.conference.companion.ui.vote.VoteFragment;

import static fr.xebia.conference.companion.core.KouignAmanApplication.BUS;


public class HomeActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final String HOME_FRAG_TAG = "HOME";

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private NfcAdapter mNfcAdapter;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

        setContentView(R.layout.home_activity);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());

        boolean hasSelectedConference = Preferences.hasSelectedConference(this);
        if (hasSelectedConference) {
            mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
            // Set up the drawer.
            mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
        }

        if (!hasSelectedConference) {
            startActivity(new Intent(this, ConferenceChooserActivity.class));
            finish();
        } else if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new ScheduleFragment(), HOME_FRAG_TAG)
                    .commit();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mNavigationDrawerFragment != null && !mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.home, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    public void restoreActionBar() {
        Fragment fragment = getFragmentManager().findFragmentByTag(HOME_FRAG_TAG);
        if (fragment instanceof RestoreActionBarFragment) {
            ((RestoreActionBarFragment) fragment).restoreActionBar();
        } else {
            getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        BUS.register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, ((Object) this).getClass())
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        BUS.unregister(this);
        super.onStop();
    }

    public void onEventMainThread(TagRegisteredEvent tagRegisteredEvent) {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new VoteFragment(), HOME_FRAG_TAG)
                .commit();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        switch (position - 1) {
            case DrawerAdapter.MENU_SCHEDULE:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getFragmentManager().beginTransaction()
                                .replace(R.id.container, new ScheduleFragment(), HOME_FRAG_TAG)
                                .commit();
                    }
                }, 300);
                break;

            case DrawerAdapter.MENU_MY_AGENDA:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getFragmentManager().beginTransaction()
                                .replace(R.id.container, ScheduleFragment.newInstanceForFavorites(), HOME_FRAG_TAG)
                                .commit();
                    }
                }, 300);
                break;

            case DrawerAdapter.MENU_TALKS:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getFragmentManager().beginTransaction()
                                .replace(R.id.container, new TrackFragment(), HOME_FRAG_TAG)
                                .commit();
                    }
                }, 300);
                break;

            case DrawerAdapter.MENU_SPEAKERS:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getFragmentManager().beginTransaction()
                                .replace(R.id.container, new SpeakerFragment(), HOME_FRAG_TAG)
                                .commit();
                    }
                }, 300);
                break;

            case DrawerAdapter.MENU_MY_VOTES:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getFragmentManager().beginTransaction()
                                .replace(R.id.container, Preferences.isTagRegistered(HomeActivity.this) ? new VoteFragment() : new
                                        ScanNfcFragment(), HOME_FRAG_TAG)
                                .commit();
                    }
                }, 300);

            case DrawerAdapter.MENU_CONFERENCES:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(HomeActivity.this, ConferenceChooserActivity.class));
                    }
                }, 300);
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Fragment fragment = getFragmentManager().findFragmentByTag(HOME_FRAG_TAG);
        if ((fragment instanceof ScanNfcFragment)
                && (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent
                .getAction()))) {
            ((ScanNfcFragment) fragment).initUserForTag(intent.getExtras().<Tag>getParcelable(NfcAdapter.EXTRA_TAG));
        }
    }
}
