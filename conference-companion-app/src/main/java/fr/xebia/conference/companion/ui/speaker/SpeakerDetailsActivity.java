package fr.xebia.conference.companion.ui.speaker;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.core.activity.BaseActivity;

public class SpeakerDetailsActivity extends BaseActivity {

    public static final String EXTRA_SPEAKER_ID = "fr.xebia.conference.companion.EXTRA_SPEAKER_ID";
    public static final String EXTRA_COLOR = "fr.xebia.conference.companion.EXTRA_COLOR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speaker_details_activity);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.speaker_details);

        Intent intent = getIntent();
        int contextColor = intent.getIntExtra(EXTRA_COLOR, 0);
        if (contextColor != 0) {
            actionBar.setBackgroundDrawable(new ColorDrawable(contextColor));
        }

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_content, SpeakerDetailsFragment.newInstance(intent.getStringExtra(EXTRA_SPEAKER_ID), contextColor))
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
