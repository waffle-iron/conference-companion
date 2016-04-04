package fr.xebia.voxxeddays.zurich.ui.talk;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.anddown.AndDown;

import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import fr.xebia.voxxeddays.zurich.BuildConfig;
import fr.xebia.voxxeddays.zurich.R;
import fr.xebia.voxxeddays.zurich.bus.MemoSavedEvent;
import fr.xebia.voxxeddays.zurich.core.misc.Preferences;
import fr.xebia.voxxeddays.zurich.core.utils.Languages;
import fr.xebia.voxxeddays.zurich.model.Speaker;
import fr.xebia.voxxeddays.zurich.model.Talk;
import fr.xebia.voxxeddays.zurich.model.Vote;
import fr.xebia.voxxeddays.zurich.ui.note.MemoActivity;
import fr.xebia.voxxeddays.zurich.ui.speaker.SpeakerDetailsActivity;
import fr.xebia.voxxeddays.zurich.ui.widget.CheckableFrameLayout;
import fr.xebia.voxxeddays.zurich.ui.widget.ObservableScrollView;
import fr.xebia.voxxeddays.zurich.ui.widget.UIUtils;
import fr.xebia.voxxeddays.zurich.ui.widget.UnderlinedTextView;
import icepick.Icepick;
import icepick.Icicle;
import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.ManyQuery;
import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.OneQuery;
import se.emilsjolander.sprinkles.Query;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static fr.xebia.voxxeddays.zurich.core.KouignAmanApplication.BUS;
import static fr.xebia.voxxeddays.zurich.service.NotificationSchedulerIntentService.buildScheduleNotificationIntentFromTalk;
import static fr.xebia.voxxeddays.zurich.service.SendRatingIntentService.buildSendRatingIntent;


public class TalkFragment extends Fragment implements OneQuery.ResultHandler<Talk>, ManyQuery.ResultHandler<Speaker>,
        ObservableScrollView.ScrollViewListener, Toolbar.OnMenuItemClickListener {

    private static final String EXTRA_TALK_ID = "fr.xebia.voxxeddays.zurich.EXTRA_TALK_ID";
    private static final String EXTRA_TALK_TITLE = "fr.xebia.voxxeddays.zurich.EXTRA_TALK_TITLE";
    private static final String EXTRA_TALK_COLOR = "fr.xebia.voxxeddays.zurich.EXTRA_TALK_COLOR";

    private static final float PHOTO_ASPECT_RATIO = 1.8f;
    private static final float GAP_FILL_DISTANCE_MULTIPLIER = 1.5f;
    private static final int SCAN_QR_CODE_REQUEST = 1000;

    @InjectView(R.id.toolbar) Toolbar toolbar;

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
    @InjectView(R.id.talk_rating) UnderlinedTextView mTalkRating;
    @InjectView(R.id.talk_rating_bar) RatingBar mTalkRatingBar;
    @InjectView(R.id.talk_rating_alert) Button mTalkRatingAlert;

    @InjectView(R.id.talk_header) ViewGroup mTalkHeader;
    @InjectView(R.id.talk_header_contents) ViewGroup mTalkHeaderContents;
    @InjectView(R.id.title) TextView mTitle;
    @InjectView(R.id.add_schedule_button) CheckableFrameLayout mAddScheduleBtn;
    @InjectView(R.id.add_schedule_icon) ImageView mAddScheduleIcon;

    @Icicle String mExtraTalkId;
    @Icicle String mExtraTalkTitle;
    @Icicle int mExtraTalkColor;

    private Talk mTalk;
    private Vote mVote;

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
    private int mToolbarHeightPixels;

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_QR_CODE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {

                Pattern pattern = Pattern.compile("([^/]*)");
                Matcher matcher = pattern.matcher(data.getStringExtra("SCAN_RESULT"));

                if (matcher.find()) {
                    Preferences.setUserScanIdForVote(getActivity(), matcher.group(1));
                    Toast.makeText(getActivity(), R.string.able_to_rate, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), R.string.not_able_to_rate, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getActivity(), R.string.not_able_to_rate, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshRatingBarState();
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

    private void inputQrCode() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.input_qr_code_title);

        final EditText input = new EditText(getActivity());
        builder.setView(input);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Preferences.setUserScanIdForVote(getActivity(), input.getText().toString());
                refreshRatingBarState();
                Toast.makeText(getActivity(), R.string.able_to_rate, Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), R.string.not_able_to_rate, Toast.LENGTH_LONG).show();
                dialog.cancel();
            }
        });

        builder.show();
    }


    private void startScanQrCodeActivity() {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, SCAN_QR_CODE_REQUEST);
        } catch (Exception e) {
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
            startActivity(marketIntent);
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


        configureHeaders();
        setupCustomScrolling();

        mTitle.setText(mExtraTalkTitle);
        toolbar.setBackgroundColor(mExtraTalkColor);

        toolbar.setNavigationIcon(R.drawable.ic_up);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavUtils.navigateUpFromSameTask(getActivity());
            }
        });
        getActivity().getMenuInflater().inflate(R.menu.talk, toolbar.getMenu());

        toolbar.setOnMenuItemClickListener(this);

        mTalkHeaderContents.setBackgroundColor(mExtraTalkColor);

        getTalk();
        if (Preferences.hasUserScanIdForVote(getActivity())) {
            Query.one(Vote.class, "SELECT * FROM Votes WHERE _id=? AND conferenceId=?", mExtraTalkId, mConferenceId).getAsync(getLoaderManager
                    (), new OneQuery
                    .ResultHandler<Vote>() {
                @Override
                public boolean handleResult(final Vote vote) {
                    mVote = vote;
                    if (vote == null || getView() == null) {
                        return true;
                    }

                    mTalkRatingBar.post(new Runnable() {
                        @Override
                        public void run() {
                            mTalkRatingBar.setRating(vote.getNote());
                        }
                    });
                    return true;
                }
            }, null);
        }
    }


    @OnClick(R.id.talk_rating_alert)
    public void onRatingAlertClicked() {
        startScanQrCodeActivity();
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


        mTalkRating.setUnderlineColor(dividerColor);
        mTalkRating.setUnderlineHeight(underlineHeight);
        mTalkRating.setTextColor(mExtraTalkColor);
    }

    private void setupCustomScrolling() {
        mScrollView.setScrollViewListener(this);
        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(mGlobalLayoutListener);
        }
    }

    private void recomputePhotoAndScrollingMetrics() {
        mToolbarHeightPixels = toolbar.getHeight();
        mHeaderHeightPixels = mTalkHeaderContents.getHeight();

        mPhotoHeightPixels = (int) (mTalkPhoto.getWidth() / PHOTO_ASPECT_RATIO);
        mPhotoHeightPixels = Math.min(mPhotoHeightPixels, getView().getHeight() * 2 / 3);

        ViewGroup.LayoutParams lp;
        lp = mTalkPhotoContainer.getLayoutParams();
        if (lp.height != mPhotoHeightPixels) {
            lp.height = mPhotoHeightPixels;
            mTalkPhotoContainer.setLayoutParams(lp);
        }

        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)
                mTalkDetailsContainer.getLayoutParams();
        if (mlp.topMargin != mHeaderHeightPixels + mPhotoHeightPixels + mToolbarHeightPixels) {
            mlp.topMargin = mHeaderHeightPixels + mPhotoHeightPixels + mToolbarHeightPixels;
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

        float newTop = Math.max(mPhotoHeightPixels, scrollY);
        mTalkHeader.setTranslationY(newTop);
        mAddScheduleBtn.setTranslationY(newTop + mHeaderHeightPixels + mToolbarHeightPixels - mAddScheduleBtnHeightPixels / 2);

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
                        getActivity().startService(buildScheduleNotificationIntentFromTalk(getActivity(), mTalk));
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
            return true;
        }
        mTitle.setText(talk.getTitle());
        mInformations.setText(String.format("%s | %s | %s | %s | %s", talk.getDay(), talk.getPeriod(), talk.getRoom(), Languages.from(getActivity(), talk.getLanguage()), talk.getType()));

        boolean favorite = mTalk.isFavorite();
        mAddScheduleBtn.setChecked(favorite, false);
        mAddScheduleBtn.setVisibility(talk.isKeynote() ? INVISIBLE : VISIBLE);
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

        mTalkPhoto.setImageResource(getTalkBackgroundResource(talk));

        bindMemo();


        refreshRatingBarState();

        mTalkRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if ((mVote != null && (int) rating == mVote.getNote()) || !fromUser) {
                    return;
                }
                long now = System.currentTimeMillis();
                boolean conferenceEnded = now > Preferences.getSelectedConferenceEndTime(getActivity());
                if (!(now > mTalk.getToUtcTime() - 20 * 60 * 1000 && !conferenceEnded) && !BuildConfig.DEBUG) {
                    Toast.makeText(getActivity(), R.string.too_early_to_vote, Toast.LENGTH_LONG).show();
                    mTalkRatingBar.setRating(0);
                    return;
                }
                if (rating == 0) {
                    mTalkRatingBar.setRating(1);
                } else {
                    Vote vote = new Vote((int) rating, mTalk.getId(), mTalk.getConferenceId());
                    vote.saveAsync(new Model.OnSavedCallback() {
                        @Override
                        public void onSaved() {
                            getActivity().startService(buildSendRatingIntent(getActivity(), mTalk));
                        }
                    });
                }
            }
        });
        if (mTalkRatingBar.getProgressDrawable() != null) {
            try {
                mTalkRatingBar.getProgressDrawable().mutate().setColorFilter(talk.getColor(), PorterDuff.Mode.SRC_IN);
            } catch (Exception e) {
                // TODO Check what happen
                Timber.e(e, "Error mutating rating bar");
            }
        }

        Query.many(Speaker.class, "SELECT * FROM Speakers AS S JOIN Speaker_Talk ST ON S._id=ST.speakerId WHERE ST.talkId=? AND S" +
                        ".conferenceId=?",
                talk.getId(), mConferenceId).getAsync(getLoaderManager(), this);

        if (getView().getAlpha() == 0) {
            getView().animate().alpha(1).start();
        }

        return true;
    }

    private int getTalkBackgroundResource(Talk talk) {
        return getResources().getIdentifier("devoxx_talk_template_" + talk.getPosition() % 11, "drawable", getActivity().getPackageName());
    }

    public void refreshRatingBarState() {
        if (mTalk == null) {
            return;
        }
        mTalkRating.setVisibility(VISIBLE);
        if (Preferences.hasUserScanIdForVote(getActivity())) {
            mTalkRatingBar.setVisibility(VISIBLE);
            mTalkRatingAlert.setVisibility(GONE);
        } else {
            mTalkRatingBar.setVisibility(GONE);
            mTalkRatingAlert.setVisibility(VISIBLE);
        }
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
                        intent.putExtra(SpeakerDetailsActivity.EXTRA_COLOR, mTalk.getColor());
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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_note:
                if (mTalk != null) {
                    Intent intent = new Intent(getActivity(), MemoActivity.class);
                    intent.putExtra(EXTRA_TALK_ID, mTalk.getId());
                    intent.putExtra(EXTRA_TALK_TITLE, mTalk.getTitle());
                    intent.putExtra(EXTRA_TALK_COLOR, mTalk.getColor());
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
                return true;

            /*case R.id.action_ask:
                Intent intent = new Intent(getActivity(), QuestionsActivity.class);
                String room = mTalk.getRoom();
                intent.putExtra(EXTRA_ROOM, room == null ? "" : room);
                startActivity(intent);
                return true;*/

            case R.id.action_scan_qr_code:
                startScanQrCodeActivity();
                return true;

            case R.id.action_input_qr_code:
                inputQrCode();
                return true;

            default:
                return false;
        }
    }
}