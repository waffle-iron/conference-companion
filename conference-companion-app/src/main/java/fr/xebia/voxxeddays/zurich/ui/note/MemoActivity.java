package fr.xebia.voxxeddays.zurich.ui.note;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.InjectView;
import fr.xebia.voxxeddays.zurich.R;
import fr.xebia.voxxeddays.zurich.bus.MemoSavedEvent;
import fr.xebia.voxxeddays.zurich.core.activity.BaseActivity;
import fr.xebia.voxxeddays.zurich.core.activity.NavigationActivity;
import fr.xebia.voxxeddays.zurich.core.utils.Compatibility;
import fr.xebia.voxxeddays.zurich.model.Talk;
import fr.xebia.voxxeddays.zurich.ui.talk.TalkActivity;
import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.OneQuery;
import se.emilsjolander.sprinkles.Query;

import static android.widget.Toast.LENGTH_SHORT;
import static fr.xebia.voxxeddays.zurich.core.KouignAmanApplication.BUS;

public class MemoActivity extends BaseActivity implements OneQuery.ResultHandler<Talk> {

    @InjectView(R.id.toolbar) Toolbar toolbar;
    @InjectView(R.id.container) EditText mText;

    private Talk mTalk;

    public MemoActivity() {
        super(R.layout.note_activity);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String talkId = intent.getStringExtra(TalkActivity.EXTRA_TALK_ID);

        Query.one(Talk.class, "SELECT * FROM Talks WHERE _id=?", talkId).getAsync(getLoaderManager(), this, null);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setBackgroundDrawable(new ColorDrawable(intent.getIntExtra(TalkActivity.EXTRA_TALK_COLOR, 0)));
            actionBar.setTitle(R.string.action_bar_note);
            if (Compatibility.isCompatible(Build.VERSION_CODES.LOLLIPOP)) {
                getWindow().setStatusBarColor(Compatibility.darker(getIntent().getIntExtra(TalkActivity.EXTRA_TALK_COLOR, Color.BLACK)));
            }
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
