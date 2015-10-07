package fr.xebia.xebicon.ui.timeline;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.widget.ListView;
import android.widget.TextView;

import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.xebicon.R;
import fr.xebia.xebicon.core.activity.BaseActivity;
import fr.xebia.xebicon.core.activity.NavigationActivity;

public class TimelineActivity extends NavigationActivity {

    @InjectView(android.R.id.list) ListView listView;
    @InjectView(android.R.id.empty) TextView emptyView;

    public TimelineActivity() {
        super(R.layout.timeline_activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.nav_timeline);

        final SearchTimeline searchTimeline = new SearchTimeline.Builder()
                .query("#XebiconFr OR @XebiaFr")
                .build();
        listView.setAdapter(new TweetTimelineListAdapter(this, searchTimeline));
        listView.setEmptyView(emptyView);
    }

    @Override
    protected int getNavId() {
        return R.id.nav_timeline;
    }
}
