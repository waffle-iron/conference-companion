package fr.xebia.conference.companion.ui.talk;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.*;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.commonsware.cwac.anddown.AndDown;
import fr.xebia.conference.companion.core.misc.Preferences;
import fr.xebia.conference.companion.model.Vote;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.model.Speaker;
import fr.xebia.conference.companion.model.Talk;
import fr.xebia.conference.companion.ui.widget.UnderlinedTextView;
import fr.xebia.conference.companion.ui.speaker.SpeakerDetailsActivity;
import fr.xebia.conference.companion.ui.speaker.SpeakerItemView;
import icepick.Icepick;
import icepick.Icicle;
import se.emilsjolander.sprinkles.*;

public class TalkFragment extends Fragment implements OneQuery.ResultHandler<Talk>, ManyQuery.ResultHandler<Speaker> {

    @InjectView(R.id.title) TextView mTitle;
    @InjectView(R.id.track) UnderlinedTextView mTrack;
    @InjectView(R.id.track_content) TextView mTrackContent;
    @InjectView(R.id.informations) TextView mInformations;
    @InjectView(R.id.summary) UnderlinedTextView mSummary;
    @InjectView(R.id.summary_content) TextView mSummaryContent;
    @InjectView(R.id.speakers) UnderlinedTextView mSpeakers;
    @InjectView(R.id.speakers_container) ViewGroup mSpeakersContainer;
    @InjectView(R.id.talk_note) RatingBar mTalkNote;

    private static final String EXTRA_TALK_ID = "fr.xebia.devoxx.EXTRA_TALK_ID";
    @Icicle String mEtraTalkId;
    private Menu mOptionsMenu;
    private Talk mTalk;
    private int mConferenceId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Icepick.restoreInstanceState(this, savedInstanceState);
        setHasOptionsMenu(true);
        mConferenceId = Preferences.getSelectedConference(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.talk_fragment, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.talk, menu);
        mOptionsMenu = menu;
        if (mTalk != null) {
            setFavoriteState(mTalk.isFavorite());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_favorite:
                if (mTalk != null) {
                    mTalk.setFavorite(!mTalk.isFavorite());
                    mTalk.saveAsync(new Model.OnSavedCallback() {
                        @Override
                        public void onSaved() {
                            setFavoriteState(mTalk.isFavorite());
                            Toast.makeText(getActivity(), mTalk.isFavorite() ? R.string.added_to_favorites : R.string
                                    .removed_from_favorites, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        configureHeaders();
        if (mEtraTalkId == null) {
            mEtraTalkId = getArguments().getString(EXTRA_TALK_ID);
        }
        Query.one(Talk.class, "SELECT * FROM Talks WHERE _id=? AND conferenceId=?", mEtraTalkId, mConferenceId).getAsync(getLoaderManager(), this, null);
        Query.one(Vote.class, "SELECT * FROM Votes WHERE _id=? AND conferenceId=?", mEtraTalkId, mConferenceId).getAsync(getLoaderManager(), new OneQuery
                .ResultHandler<Vote>() {
            @Override
            public boolean handleResult(Vote vote) {
                if (getView() == null) {
                    return false;
                }

                if (vote == null) {
                    mTalkNote.setVisibility(View.GONE);
                } else {
                    mTalkNote.setVisibility(View.VISIBLE);
                    mTalkNote.setRating(vote.getNote());
                }
                return false;
            }
        }, null);
    }

    private void configureHeaders() {
        int color = getResources().getColor(R.color.xebia_color);
        int underlineHeight = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
        mSummary.setUnderlineColor(color);
        mSummary.setUnderlineHeight(underlineHeight);

        mSpeakers.setUnderlineColor(color);
        mSpeakers.setUnderlineHeight(underlineHeight);

        mTrack.setUnderlineColor(color);
        mTrack.setUnderlineHeight(underlineHeight);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onDestroyView() {
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mTalk = null;
        super.onDestroy();
    }

    public static Fragment newInstance(String talkId) {
        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_TALK_ID, talkId);
        Fragment fragment = new TalkFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public boolean handleResult(Talk talk) {
        mTalk = talk;
        if (talk == null) {
            getActivity().finish();
            return false;
        }

        setFavoriteState(mTalk.isFavorite());

        if (getView() == null) {
            return false;
        }

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(R.string.talk_details);
        mTitle.setText(talk.getTitle());
        mInformations.setText(String.format("%s | %s | %s\n%s", talk.getDay(), talk.getPeriod(), talk.getRoom(), talk.getType()));
        try {
            mSummaryContent.setText(Html.fromHtml(new AndDown().markdownToHtml(talk.getSummary())));
        } catch (Exception e) {
            mSummaryContent.setText(talk.getSummary());
        }

        String track = talk.getTrack();
        Resources resources = getResources();
        Drawable trackDrawable = resources.getDrawable(R.drawable.ic_talk).mutate();
        trackDrawable.setColorFilter(talk.getColor(), PorterDuff.Mode.SRC_IN);
        mTrackContent.setText(track);
        mTrackContent.setCompoundDrawablesWithIntrinsicBounds(trackDrawable, null, null, null);


        Query.many(Speaker.class, "SELECT * FROM Speakers AS S JOIN Speaker_Talk ST ON S._id=ST.speakerId WHERE ST.talkId=? AND S.conferenceId=?",
                talk.getId(), mConferenceId).getAsync(getLoaderManager(), this);

        return false;
    }

    @Override
    public boolean handleResult(CursorList<Speaker> speakers) {
        if (getView() == null) {
            return false;
        }

        mSpeakersContainer.removeAllViews();
        if (speakers != null) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            for (final Speaker speaker : speakers.asList()) {
                SpeakerItemView speakerView = (SpeakerItemView) inflater.inflate(R.layout.speaker_short_item, mSpeakersContainer, false);
                speakerView.bind(speaker);
                speakerView.setBackgroundResource(R.drawable.text_view_selector);
                speakerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), SpeakerDetailsActivity.class);
                        intent.putExtra(SpeakerDetailsActivity.EXTRA_SPEAKER_ID, speaker.getId());
                        startActivity(intent);
                    }
                });
                mSpeakersContainer.addView(speakerView);
            }
        }
        return false;
    }

    public void setFavoriteState(final boolean favorite) {
        if (mOptionsMenu != null) {
            final MenuItem refreshItem = mOptionsMenu.findItem(R.id.action_favorite);
            if (refreshItem != null) {
                refreshItem.setIcon(favorite ? R.drawable.ic_action_important : R.drawable.ic_action_not_important);
            }
        }
    }
}
