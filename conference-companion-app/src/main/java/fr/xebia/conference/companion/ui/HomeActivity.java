package fr.xebia.conference.companion.ui;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import com.crashlytics.android.Crashlytics;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.bus.TagRegisteredEvent;
import fr.xebia.conference.companion.core.activity.BaseActivity;
import fr.xebia.conference.companion.ui.navigation.DrawerAdapter;
import fr.xebia.conference.companion.ui.schedule.ScheduleFragment;
import fr.xebia.conference.companion.ui.vote.ScanNfcFragment;
import fr.xebia.conference.companion.ui.vote.VoteFragment;

import static fr.xebia.conference.companion.core.KouignAmanApplication.BUS;


public class HomeActivity extends BaseActivity {

    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

        setContentView(R.layout.home_activity);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (!isFinishing() && savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_content, new ScheduleFragment(), HOME_FRAG_TAG)
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
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
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
                .replace(R.id.main_content, new VoteFragment(), HOME_FRAG_TAG)
                .commit();
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
