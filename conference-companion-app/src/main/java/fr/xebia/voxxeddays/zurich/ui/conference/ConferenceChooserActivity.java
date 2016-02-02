package fr.xebia.voxxeddays.zurich.ui.conference;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.view.MenuItem;

import fr.xebia.voxxeddays.zurich.BuildConfig;
import fr.xebia.voxxeddays.zurich.R;
import fr.xebia.voxxeddays.zurich.core.activity.BaseActivity;
import fr.xebia.voxxeddays.zurich.core.activity.NavigationActivity;
import fr.xebia.voxxeddays.zurich.ui.synchro.SynchroFragment;

public class ConferenceChooserActivity extends BaseActivity {

    public ConferenceChooserActivity() {
        super(R.layout.conference_chooser_activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDontCheckConference = true;
        getWindow().setBackgroundDrawableResource(android.R.color.white);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, SynchroFragment.newInstance(BuildConfig.DEVOXX_BE_CONFERENCE_ID), SynchroFragment.TAG)
                    .addToBackStack("synchro")
                    .commit();
        }
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
