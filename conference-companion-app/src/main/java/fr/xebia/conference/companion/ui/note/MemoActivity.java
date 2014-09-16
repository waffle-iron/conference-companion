package fr.xebia.conference.companion.ui.note;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.InjectView;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.bus.MemoSavedEvent;
import fr.xebia.conference.companion.core.activity.BaseActivity;
import fr.xebia.conference.companion.model.Talk;
import fr.xebia.conference.companion.ui.talk.TalkActivity;
import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.OneQuery;
import se.emilsjolander.sprinkles.Query;

import static android.widget.Toast.LENGTH_SHORT;
import static fr.xebia.conference.companion.core.KouignAmanApplication.BUS;

public class MemoActivity extends BaseActivity implements OneQuery.ResultHandler<Talk> {

    @InjectView(R.id.container) EditText mText;

    private Talk mTalk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        Intent intent = getIntent();
        String talkId = intent.getStringExtra(TalkActivity.EXTRA_TALK_ID);

        Query.one(Talk.class, "SELECT * FROM Talks WHERE _id=?", talkId).getAsync(getLoaderManager(), this, null);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_bar_note);
        }

        Toast.makeText(this, R.string.markdown_allowed, LENGTH_SHORT).show();
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
        if (mText.getText().length() > 0) {
            if (mTalk != null) {
                mTalk.setMemo(mText.getText().toString());
                mTalk.saveAsync(new Model.OnSavedCallback() {
                    @Override
                    public void onSaved() {
                        Toast.makeText(getApplicationContext(), R.string.memo_saved, LENGTH_SHORT).show();
                        BUS.post(MemoSavedEvent.INSTANCE);
                        finish();
                    }
                });
            }
        } else {
            finish();
        }
    }

    @Override
    public boolean handleResult(Talk talk) {
        mTalk = talk;
        bind();
        return false;
    }

    private void bind() {
        mText.setText(mTalk.getMemo());
    }
}
