package fr.xebia.devoxx.be.ui.timeline;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.devoxx.be.R;
import fr.xebia.devoxx.be.core.activity.BaseActivity;

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
                .query("#Devoxx OR @Devoxx")
                .build();
        listView.setAdapter(new TweetTimelineListAdapter(this, searchTimeline));
        listView.setEmptyView(emptyView);
    }

}
