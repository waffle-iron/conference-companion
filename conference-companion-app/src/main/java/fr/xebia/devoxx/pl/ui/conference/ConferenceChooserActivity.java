package fr.xebia.devoxx.pl.ui.conference;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.view.MenuItem;

import fr.xebia.devoxx.pl.BuildConfig;
import fr.xebia.devoxx.pl.R;
import fr.xebia.devoxx.pl.core.activity.BaseActivity;
import fr.xebia.devoxx.pl.ui.synchro.SynchroFragment;

public class ConferenceChooserActivity extends BaseActivity {

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
                    .replace(R.id.container, SynchroFragment.newInstance(BuildConfig.DEVOXX_PL_CONFERENCE_ID), SynchroFragment.TAG)
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
