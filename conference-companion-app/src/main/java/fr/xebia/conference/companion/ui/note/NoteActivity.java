package fr.xebia.conference.companion.ui.note;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import butterknife.ButterKnife;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.ui.talk.TalkActivity;

public class NoteActivity extends Activity {

    private String mTalkId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity);
        ButterKnife.inject(this);

        Intent intent = getIntent();
        mTalkId = intent.getStringExtra(TalkActivity.EXTRA_TALK_ID);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_bar_note);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        saveNote();
        super.onBackPressed();
    }

    private void saveNote() {

    }
}
