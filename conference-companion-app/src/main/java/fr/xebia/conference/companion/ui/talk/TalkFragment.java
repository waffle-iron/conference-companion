package fr.xebia.conference.companion.ui.talk;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.anddown.AndDown;

import java.util.LinkedHashSet;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.bus.MemoSavedEvent;
import fr.xebia.conference.companion.core.misc.Preferences;
import fr.xebia.conference.companion.model.Speaker;
import fr.xebia.conference.companion.model.Talk;
import fr.xebia.conference.companion.model.Vote;
import fr.xebia.conference.companion.ui.note.MemoActivity;
import fr.xebia.conference.companion.ui.speaker.SpeakerDetailsActivity;
import fr.xebia.conference.companion.ui.widget.CheckableFrameLayout;
import fr.xebia.conference.companion.ui.widget.ObservableScrollView;
import fr.xebia.conference.companion.ui.widget.UIUtils;
import fr.xebia.conference.companion.ui.widget.UnderlinedTextView;
import icepick.Icepick;
import icepick.Icicle;
import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.ManyQuery;
import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.OneQuery;
import se.emilsjolander.sprinkles.Query;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static fr.xebia.conference.companion.core.KouignAmanApplication.BUS;
import static fr.xebia.conference.companion.service.NotificationSchedulerIntentService.buildScheduleNotificationIntentFromTalk;

public class TalkFragment extends Fragment implements OneQuery.ResultHandler<Talk>, ManyQuery.ResultHandler<Speaker>,
        ObservableScrollView.ScrollViewListener {

    private static final String EXTRA_TALK_ID = "fr.xebia.devoxx.EXTRA_TALK_ID";
    private static final String EXTRA_TALK_TITLE = "fr.xebia.devoxx.EXTRA_TALK_TITLE";
    private static final String EXTRA_TALK_COLOR = "fr.xebia.devoxx.EXTRA_TALK_COLOR";

    private static final float PHOTO_ASPECT_RATIO = 1.8f;
    private static final float GAP_FILL_DISTANCE_MULTIPLIER = 1.5f;

    @InjectView(R.id.scroll_view) ObservableScrollView mScrollView;

    @InjectView(R.id.talk_photo_container) ViewGroup mTalkPhotoContainer;
    @InjectView(R.id.talk_photo) ImageView mTalkPhoto;

    @InjectView(R.id.talk_details_container) ViewGroup mTalkDetailsContainer;
    @InjectView(R.id.track) UnderlinedTextView mTrack;
    @InjectView(R.id.track_content) TextView mTrackContent;
    @InjectView(R.id.informations) TextView mInformations;
    @InjectView(R.id.summary) UnderlinedTextView mSummary;
    @InjectView(R.id.summary_content) TextView mSummaryContent;
    @InjectView(R.id.track_memo_title) UnderlinedTextView mMemo;
    @InjectView(R.id.track_memo_value) TextView mMemoContent;
    @InjectView(R.id.speakers) UnderlinedTextView mSpeakers;
    @InjectView(R.id.speakers_container) ViewGroup mSpeakersContainer;
    @InjectView(R.id.talk_note) RatingBar mTalkNote;

    @InjectView(R.id.talk_header) ViewGroup mTalkHeader;
    @InjectView(R.id.talk_header_contents) ViewGroup mTalkHeaderContents;
    @InjectView(R.id.talk_header_background) View mTalkHeaderBackground;
    @InjectView(R.id.title) TextView mTitle;
    @InjectView(R.id.add_schedule_button) CheckableFrameLayout mAddScheduleBtn;
    @InjectView(R.id.add_schedule_icon) ImageView mAddScheduleIcon;

    @Icicle String mExtraTalkId;
    @Icicle String mExtraTalkTitle;
    @Icicle int mExtraTalkColor;

    private Talk mTalk;
    private int mConferenceId;
    private int mAddScheduleBtnHeightPixels;

    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            mAddScheduleBtnHeightPixels = mAddScheduleBtn.getHeight();
            recomputePhotoAndScrollingMetrics();
        }
    };
    private int mPhotoHeightPixels;
    private int mHeaderHeightPixels;
    private int mHeaderTopClearance;
    private boolean mGapFillShown;

    public static Fragment newInstance(String talkId, String talkTitle, int color) {
        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_TALK_ID, talkId);
        arguments.putString(EXTRA_TALK_TITLE, talkTitle);
        arguments.putInt(EXTRA_TALK_COLOR, color);
        Fragment fragment = new TalkFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Icepick.restoreInstanceState(this, savedInstanceState);
        setHasOptionsMenu(true);
        mConferenceId = Preferences.getSelectedConference(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        BUS.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.talk_fragment, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.talk, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_note:
                if (mTalk != null) {
                    Intent intent = new Intent(getActivity(), MemoActivity.class);
                    intent.putExtra(EXTRA_TALK_ID, mTalk.getId());
                    intent.putExtra(EXTRA_TALK_TITLE, mTalk.getTitle());
                    startActivity(intent);
                }
                return true;
            case R.id.action_send:
                if (mTalk != null) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("message/rfc822");
                    intent.putExtra(Intent.EXTRA_SUBJECT, mTalk.getUncotedTitle());
                    intent.putExtra(Intent.EXTRA_TEXT, mTalk.getBody(getActivity()));
                    try {
                        startActivity(Intent.createChooser(intent, getResources().getString(R.string.send_memo_via)));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getActivity(), R.string.cannot_send_email, Toast.LENGTH_SHORT).show();
                    }
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);

        if (mExtraTalkId == null) {
            mExtraTalkId = getArguments().getString(EXTRA_TALK_ID);
            mExtraTalkTitle = getArguments().getString(EXTRA_TALK_TITLE);
            mExtraTalkColor = getArguments().getInt(EXTRA_TALK_COLOR);
        }


        mTalkPhoto.setImageResource(R.drawable.devoxx_talk_template);
        configureHeaders();
        setupCustomScrolling();

        mTitle.setText(mExtraTalkTitle);
        mTalkHeaderBackground.setBackgroundColor(mExtraTalkColor);
        getTalk();
        Query.one(Vote.class, "SELECT * FROM Votes WHERE _id=? AND conferenceId=?", mExtraTalkId, mConferenceId).getAsync(getLoaderManager
                (), new OneQuery
                .ResultHandler<Vote>() {
            @Override
            public boolean handleResult(Vote vote) {
                if (getView() == null) {
                    return false;
                }

                if (vote == null) {
                    mTalkNote.setVisibility(GONE);
                } else {
                    mTalkNote.setVisibility(VISIBLE);
                    mTalkNote.setRating(vote.getNote());
                }
                return false;
            }
        }, null);
    }

    private void getTalk() {
        Query.one(Talk.class, "SELECT * FROM Talks WHERE _id=? AND conferenceId=?", mExtraTalkId, mConferenceId).getAsync(getLoaderManager
                (), this, null);
    }

    private void configureHeaders() {
        int dividerColor = getResources().getColor(android.R.color.darker_gray);
        int underlineHeight = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mSummary.setUnderlineColor(dividerColor);
        mSummary.setUnderlineHeight(underlineHeight);
        mSummary.setTextColor(mExtraTalkColor);

        mSpeakers.setUnderlineColor(dividerColor);
        mSpeakers.setUnderlineHeight(underlineHeight);
        mSpeakers.setTextColor(mExtraTalkColor);

        mTrack.setUnderlineColor(dividerColor);
        mTrack.setUnderlineHeight(underlineHeight);
        mTrack.setTextColor(mExtraTalkColor);

        mMemo.setUnderlineColor(dividerColor);
        mMemo.setUnderlineHeight(underlineHeight);
        mMemo.setTextColor(mExtraTalkColor);
    }

    private void setupCustomScrolling() {
        mScrollView.setScrollViewListener(this);
        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(mGlobalLayoutListener);
        }
    }

    private void recomputePhotoAndScrollingMetrics() {
        final int actionBarSize = UIUtils.calculateActionBarSize(getActivity());
        mHeaderTopClearance = actionBarSize - mTalkHeaderContents.getPaddingTop();
        mHeaderHeightPixels = mTalkHeaderContents.getHeight();

        mPhotoHeightPixels = mHeaderTopClearance;
        mPhotoHeightPixels = (int) (mTalkPhoto.getWidth() / PHOTO_ASPECT_RATIO);
        mPhotoHeightPixels = Math.min(mPhotoHeightPixels, getView().getHeight() * 2 / 3);

        ViewGroup.LayoutParams lp;
        lp = mTalkPhotoContainer.getLayoutParams();
        if (lp.height != mPhotoHeightPixels) {
            lp.height = mPhotoHeightPixels;
            mTalkPhotoContainer.setLayoutParams(lp);
        }

        lp = mTalkHeaderBackground.getLayoutParams();
        if (lp.height != mHeaderHeightPixels) {
            lp.height = mHeaderHeightPixels;
            mTalkHeaderBackground.setLayoutParams(lp);
        }

        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)
                mTalkDetailsContainer.getLayoutParams();
        if (mlp.topMargin != mHeaderHeightPixels + mPhotoHeightPixels) {
            mlp.topMargin = mHeaderHeightPixels + mPhotoHeightPixels;
            mTalkDetailsContainer.setLayoutParams(mlp);
        }

        onScrollChanged(mScrollView, 0, 0, 0, 0);
    }

    @Override
    public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        // Reposition the header bar -- it's normally anchored to the top of the content,
        // but locks to the top of the screen on scroll
        int scrollY = mScrollView.getScrollY();

        float newTop = Math.max(mPhotoHeightPixels, scrollY + mHeaderTopClearance);
        mTalkHeader.setTranslationY(newTop);
        mAddScheduleBtn.setTranslationY(newTop + mHeaderHeightPixels - mAddScheduleBtnHeightPixels / 2);

        mTalkHeaderBackground.setPivotY(mHeaderHeightPixels);
        int gapFillDistance = (int) (mHeaderTopClearance * GAP_FILL_DISTANCE_MULTIPLIER);
        boolean showGapFill = (scrollY > (mPhotoHeightPixels - gapFillDistance));
        float desiredHeaderScaleY = showGapFill ?
                ((mHeaderHeightPixels + gapFillDistance + 1) * 1f / mHeaderHeightPixels) : 1f;

        if (mGapFillShown != showGapFill) {
            mTalkHeaderBackground.animate()
                    .scaleY(desiredHeaderScaleY)
                    .setInterpolator(new DecelerateInterpolator(2f))
                    .setDuration(250)
                    .start();
        }
        mGapFillShown = showGapFill;

        // Move background photo (parallax effect)
        mTalkPhotoContainer.setTranslationY(scrollY * 0.6f);
    }

    @OnClick(R.id.add_schedule_button)
    public void onAddScheduleBtnClicked() {
        if (mTalk != null) {
            mTalk.setFavorite(!mTalk.isFavorite());
            mTalk.saveAsync(new Model.OnSavedCallback() {
                @Override
                public void onSaved() {
                    boolean favorite = mTalk.isFavorite();
                    mAddScheduleBtn.setChecked(favorite, true);
                    UIUtils.setOrAnimatePlusCheckIcon(getActivity(), mAddScheduleIcon, favorite, true);
                    if (favorite && getActivity() != null) {
                        getActivity().startService(buildScheduleNotificationIntentFromTalk(mTalk));
                    }
                }
            });
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onStop() {
        BUS.unregister(this);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        mScrollView.setScrollViewListener(null);
        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.removeGlobalOnLayoutListener(mGlobalLayoutListener);
        }
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mTalk = null;
        super.onDestroy();
    }

    @Override
    public boolean handleResult(Talk talk) {
        mTalk = talk;
        if (talk == null) {
            getActivity().finish();
            return false;
        }

        if (getView() == null) {
            return false;
        }
        mTitle.setText(talk.getTitle());
        mInformations.setText(String.format("%s | %s | %s\n%s", talk.getDay(), talk.getPeriod(), talk.getRoom(), talk.getType()));

        boolean favorite = mTalk.isFavorite();
        mAddScheduleBtn.setChecked(favorite, false);
        mAddScheduleBtn.setVisibility(/*talk.isKeynote() ? INVISIBLE :*/ VISIBLE); // TODO waiting for validation
        UIUtils.setOrAnimatePlusCheckIcon(getActivity(), mAddScheduleIcon, favorite, false);

        if (!TextUtils.isEmpty(talk.getSummary())) {
            try {
                mSummaryContent.setText(Html.fromHtml(new AndDown().markdownToHtml(talk.getSummary())));
            } catch (Exception e) {
                mSummaryContent.setText(talk.getSummary());
            }
        } else {
            mSummaryContent.setText(R.string.no_talk_details);
        }


        mTrackContent.setText(talk.getTrack());

        bindMemo();

        Query.many(Speaker.class, "SELECT * FROM Speakers AS S JOIN Speaker_Talk ST ON S._id=ST.speakerId WHERE ST.talkId=? AND S" +
                        ".conferenceId=?",
                talk.getId(), mConferenceId).getAsync(getLoaderManager(), this);

        if (getView().getAlpha() == 0) {
            getView().animate().alpha(1).start();
        }

        return false;
    }

    @Override
    public boolean handleResult(CursorList<Speaker> speakers) {
        if (getView() == null) {
            return false;
        }

        mSpeakersContainer.removeAllViews();
        if (speakers != null && speakers.size() > 0) {
            mSpeakers.setVisibility(VISIBLE);
            mSpeakersContainer.setVisibility(VISIBLE);
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            for (final Speaker speaker : speakers.asList()) {
                TalkSpeakerItemView speakerView = (TalkSpeakerItemView) inflater.inflate(R.layout.talk_speaker_item_view,
                        mSpeakersContainer, false);
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
            mTalk.setSpeakers(new LinkedHashSet<>(speakers.asList()));
        } else {
            mSpeakers.setVisibility(GONE);
            mSpeakersContainer.setVisibility(GONE);
        }
        return false;
    }

    private void bindMemo() {
        if (mTalk.getMemo() != null && mTalk.getMemo().trim().length() > 0) {
            try {
                mMemoContent.setText(Html.fromHtml(new AndDown().markdownToHtml(mTalk.getMemo())));
            } catch (Exception e) {
                mMemoContent.setText(mTalk.getMemo());
            }
            mMemo.setVisibility(VISIBLE);
            mMemoContent.setVisibility(VISIBLE);
        } else {
            mMemo.setVisibility(GONE);
            mMemoContent.setVisibility(GONE);
        }
    }

    public void onEventMainThread(MemoSavedEvent event) {
        getTalk();
    }
}
