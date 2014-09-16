package fr.xebia.conference.companion.ui.browse;

import android.app.Activity;
import android.os.Bundle;
import fr.xebia.conference.companion.R;

import java.util.ArrayList;

public class BrowseTalksActivity extends Activity {

    public static final String EXTRA_TITLE = "fr.xebia.conference.companion.EXTRA_TITLE";
    public static String EXTRA_AVAILABLE_TALKS = "fr.xebia.conference.companion.EXTRA_AVAILABLE_TALKS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse_talks_activity);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        getActionBar().setTitle(title);

        if (getFragmentManager().findFragmentByTag(BrowseTalksFragment.TAG) == null) {
            ArrayList<String> availableTalksIds = getIntent().getStringArrayListExtra(EXTRA_AVAILABLE_TALKS);
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, BrowseTalksFragment.newInstance(availableTalksIds), BrowseTalksFragment.TAG)
                    .commit();
        }

    }


}
