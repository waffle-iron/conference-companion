package fr.xebia.devoxx.pl.ui.conference;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.view.MenuItem;

import fr.xebia.devoxx.pl.R;
import fr.xebia.devoxx.pl.bus.ConferenceSelectedEvent;
import fr.xebia.devoxx.pl.bus.SynchroFinishedEvent;
import fr.xebia.devoxx.pl.core.activity.BaseActivity;
import fr.xebia.devoxx.pl.ui.HomeActivity;
import fr.xebia.devoxx.pl.ui.synchro.SynchroFragment;

import static fr.xebia.devoxx.pl.core.KouignAmanApplication.BUS;

public class ConferenceChooserActivity extends BaseActivity {

    public static final String EXTRA_SHOW_HOME = "fr.xebia.devoxx.pl.EXTRA_SHOW_HOME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conference_chooser_activity);
        mDontCheckConference = true;
        getWindow().setBackgroundDrawableResource(android.R.color.white);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(Html.fromHtml(getString(R.string.action_bar_default_title)));
        actionBar.setDisplayHomeAsUpEnabled(false);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, SynchroFragment.newInstance(BuildConfig.DEVOXX_UK_CONFERENCE_ID), SynchroFragment.TAG)
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
