package fr.xebia.voxxeddays.zurich.ui.timeline;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.voxxeddays.zurich.BuildConfig;
import fr.xebia.voxxeddays.zurich.R;
import fr.xebia.voxxeddays.zurich.core.activity.NavigationActivity;
import fr.xebia.voxxeddays.zurich.ui.widget.ObservableListView;

public class TimelineActivity extends NavigationActivity implements SwipeRefreshLayout.OnRefreshListener {

    @InjectView(android.R.id.list) ObservableListView listView;
    @InjectView(android.R.id.empty) TextView emptyView;
    @InjectView(R.id.swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;

    public TimelineActivity() {
        super(R.layout.timeline_activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.nav_timeline);

        refreshListing();

        swipeRefreshLayout.setOnRefreshListener(this);
    }

    private void refreshListing() {
        final SearchTimeline searchTimeline = new SearchTimeline.Builder()
                .query(BuildConfig.TWITTER_QUERY)
                .build();
        listView.setAdapter(new TweetTimelineListAdapter(this, searchTimeline));
        listView.setEmptyView(emptyView);

        swipeRefreshLayout.setRefreshing(false);
    }


    @Override
    protected int getNavId() {
        return R.id.nav_timeline;
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        refreshListing();
    }
}
