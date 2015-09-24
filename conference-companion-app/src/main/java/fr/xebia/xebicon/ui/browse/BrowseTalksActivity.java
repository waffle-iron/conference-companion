package fr.xebia.xebicon.ui.browse;

import android.os.Bundle;

import java.util.ArrayList;

import butterknife.InjectView;
import fr.xebia.xebicon.R;
import fr.xebia.xebicon.core.activity.BaseActivity;
import fr.xebia.xebicon.core.activity.BaseActivity.OnActionBarAutoShowOrHideListener;
import fr.xebia.xebicon.ui.widget.DrawShadowFrameLayout;

public class BrowseTalksActivity extends BaseActivity implements OnActionBarAutoShowOrHideListener {

    public static final String EXTRA_TITLE = "fr.xebia.xebicon.EXTRA_TITLE";
    public static String EXTRA_AVAILABLE_TALKS = "fr.xebia.xebicon.EXTRA_AVAILABLE_TALKS";

    @InjectView(R.id.main_content) DrawShadowFrameLayout mDrawShadowFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse_talks_activity);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getFragmentManager().findFragmentByTag(BrowseTalksFragment.TAG) == null) {
            ArrayList<String> availableTalksIds = getIntent().getStringArrayListExtra(EXTRA_AVAILABLE_TALKS);
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_content, BrowseTalksFragment.newInstance(availableTalksIds), BrowseTalksFragment.TAG)
                    .commit();
        }

        setActionBarAutoShowOrHideListener(this);
    }

    public void onActionBarAutoShowOrHide(boolean shown) {
        mDrawShadowFrameLayout.setShadowVisible(shown, true);
    }

}
