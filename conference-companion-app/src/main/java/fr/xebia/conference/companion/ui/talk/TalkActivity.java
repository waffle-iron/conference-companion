package fr.xebia.conference.companion.ui.talk;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import butterknife.ButterKnife;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.core.activity.BaseActivity;
import fr.xebia.conference.companion.core.misc.Preferences;

public class TalkActivity extends BaseActivity {

    public static final String EXTRA_TALK_ID = "fr.xebia.devoxx.EXTRA_TALK_ID";
    public static final String EXTRA_TALK_TITLE = "fr.xebia.devoxx.EXTRA_TALK_TITLE";
    public static final String EXTRA_TALK_COLOR = "fr.xebia.devoxx.EXTRA_TALK_COLOR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.talk_activity);
        ButterKnife.inject(this);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, TalkFragment.newInstance(intent.getStringExtra(EXTRA_TALK_ID),
                            intent.getStringExtra(EXTRA_TALK_TITLE),
                            intent.getIntExtra(EXTRA_TALK_COLOR, Color.BLACK)))
                    .commit();
        }
    }

    @Override
    protected void selectTheme() {
        boolean hasSelectedConference = Preferences.hasSelectedConference(this);
        boolean devoxxConf = Preferences.isCurrentConferenceDevoxx(this);
        setTheme(hasSelectedConference && devoxxConf ? R.style.Theme_Devoxx_Companion_TalkDetails : R.style.Theme_Companion_TalkDetails);
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
