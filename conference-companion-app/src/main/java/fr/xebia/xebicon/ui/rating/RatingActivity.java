package fr.xebia.xebicon.ui.rating;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

import fr.xebia.xebicon.R;
import fr.xebia.xebicon.core.activity.BaseActivity;
import fr.xebia.xebicon.ui.talk.TalkActivity;

public class RatingActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.rating_title);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_content,
                            RatingFragment.newInstance(
                                    getIntent().getStringExtra(TalkActivity.EXTRA_TALK_ID),
                                    getIntent().getStringExtra(TalkActivity.EXTRA_TALK_TITLE)))
                    .commit();
        }
    }
}
