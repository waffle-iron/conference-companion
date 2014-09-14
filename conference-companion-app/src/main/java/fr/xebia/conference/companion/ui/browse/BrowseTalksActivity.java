package fr.xebia.conference.companion.ui.browse;

import android.app.Activity;
import android.os.Bundle;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.model.Talk;

import java.util.ArrayList;

public class BrowseTalksActivity extends Activity {

    public static String EXTRA_AVAILABLE_TALKS = "fr.xebia.conference.companion.EXTRA_AVAILABLE_TALKS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse_talks_activity);

        if (getFragmentManager().findFragmentByTag(BrowseTalksFragment.TAG) == null) {
            ArrayList<Talk> availableTalks = getIntent().getParcelableArrayListExtra(EXTRA_AVAILABLE_TALKS);
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, BrowseTalksFragment.newInstance(availableTalks), BrowseTalksFragment.TAG)
                    .commit();
        }

    }
}
