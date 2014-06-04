package fr.xebia.conference.companion.ui.talk;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import fr.xebia.conference.companion.R;

public class TalkActivity extends Activity {

    public static final String EXTRA_TALK_ID = "fr.xebia.devoxx.EXTRA_TALK_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.talk_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        if(savedInstanceState == null){
            getFragmentManager().beginTransaction()
                    .replace(R.id.container,TalkFragment.newInstance(getIntent().getStringExtra(EXTRA_TALK_ID)))
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
