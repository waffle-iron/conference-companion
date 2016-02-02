package fr.xebia.voxxeddays.zurich.ui.browse;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;

import butterknife.InjectView;
import fr.xebia.voxxeddays.zurich.R;
import fr.xebia.voxxeddays.zurich.core.activity.BaseActivity;
import fr.xebia.voxxeddays.zurich.ui.widget.DrawShadowFrameLayout;

public class BrowseTalksActivity extends BaseActivity {

    public static final String EXTRA_TITLE = "fr.xebia.voxxeddays.zurich.EXTRA_TITLE";
    public static String EXTRA_AVAILABLE_TALKS = "fr.xebia.voxxeddays.zurich.EXTRA_AVAILABLE_TALKS";

    @InjectView(R.id.toolbar) Toolbar toolbar;

    public BrowseTalksActivity() {
        super(R.layout.browse_talks_activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getIntent().getStringExtra(EXTRA_TITLE));

        if (getFragmentManager().findFragmentByTag(BrowseTalksFragment.TAG) == null) {
            ArrayList<String> availableTalksIds = getIntent().getStringArrayListExtra(EXTRA_AVAILABLE_TALKS);
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_content, BrowseTalksFragment.newInstance(availableTalksIds), BrowseTalksFragment.TAG)
                    .commit();
        }
    }

}
