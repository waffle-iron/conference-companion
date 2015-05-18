package fr.xebia.devoxx.uk.ui.timeline;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.widget.ListView;
import android.widget.TextView;

import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.devoxx.uk.R;
import fr.xebia.devoxx.uk.core.activity.BaseActivity;
import fr.xebia.devoxx.uk.ui.navigation.DrawerAdapter;

public class TimelineActivity extends BaseActivity {

    @InjectView(android.R.id.list) ListView listView;
    @InjectView(android.R.id.empty) TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timeline_activity);

        ButterKnife.inject(this);

        setTitle("Timeline");

        final SearchTimeline searchTimeline = new SearchTimeline.Builder()
                .query("#DevoxxUK OR @DevoxxUK")
                .build();
        listView.setAdapter(new TweetTimelineListAdapter(this, searchTimeline));
        listView.setEmptyView(emptyView);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return DrawerAdapter.MENU_TIMELINE;
    }

    @Override
    public void onNavigationDrawerToggle(boolean opened) {
        super.onNavigationDrawerToggle(opened);
        if (!opened) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.timeline);
        }
    }
}
