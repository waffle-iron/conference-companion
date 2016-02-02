package fr.xebia.voxxeddays.zurich.ui.talk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import butterknife.ButterKnife;
import fr.xebia.voxxeddays.zurich.R;
import fr.xebia.voxxeddays.zurich.core.activity.BaseActivity;
import fr.xebia.voxxeddays.zurich.core.activity.NavigationActivity;
import fr.xebia.voxxeddays.zurich.core.utils.Compatibility;
import fr.xebia.voxxeddays.zurich.model.Talk;

public class TalkActivity extends BaseActivity {

    public static final String EXTRA_TALK_ID = "fr.xebia.voxxeddays.zurich.EXTRA_TALK_ID";
    public static final String EXTRA_TALK_TITLE = "fr.xebia.voxxeddays.zurich.EXTRA_TALK_TITLE";
    public static final String EXTRA_TALK_COLOR = "fr.xebia.voxxeddays.zurich.EXTRA_TALK_COLOR";

    public static Intent buildIntentFromTalk(Context context, Talk talk) {
        Intent intent = new Intent(context, TalkActivity.class);
        intent.putExtra(TalkActivity.EXTRA_TALK_ID, talk.getId());
        intent.putExtra(TalkActivity.EXTRA_TALK_TITLE, talk.getTitle());
        intent.putExtra(TalkActivity.EXTRA_TALK_COLOR, talk.getColor());
        return intent;
    }

    public TalkActivity() {
        super(R.layout.talk_activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (shouldBeFloatingWindow()) {
            setupFloatingWindow();
        }

        super.onCreate(savedInstanceState);

        if (Compatibility.isCompatible(Build.VERSION_CODES.LOLLIPOP)) {
            getWindow().setStatusBarColor(Compatibility.darker(getIntent().getIntExtra(EXTRA_TALK_COLOR, Color.BLACK)));
        }

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, TalkFragment.newInstance(intent.getStringExtra(EXTRA_TALK_ID),
                            intent.getStringExtra(EXTRA_TALK_TITLE),
                            getIntent().getIntExtra(EXTRA_TALK_COLOR, Color.BLACK)))
                    .commit();
        }

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
