package fr.xebia.xebicon.ui.rating;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import fr.xebia.xebicon.R;

import static com.fluent.android.bundle.FluentBundle.newFluentBundle;
import static com.fluent.android.bundle.FragmentArgsSetter.setFragmentArguments;

public class RatingFragment extends Fragment {

    private static final String EXTRA_TALK_ID = "EXTRA_TALK_ID";
    private static final String EXTRA_TALK_TITLE = "EXTRA_TALK_TITLE";

    @InjectView(R.id.session_title) TextView mSessionTitle;
    @InjectView(R.id.session_feedback_comments) EditText mComments;
    @InjectView(R.id.rating_bar_0) RatingBar mSessionRatingFeedbackBar;
    @InjectView(R.id.rating_bar_1) NumberRatingBar mQ1FeedbackBar;
    @InjectView(R.id.rating_bar_2) NumberRatingBar mQ2FeedbackBar;
    @InjectView(R.id.rating_bar_3) NumberRatingBar mQ3FeedbackBar;
    private String talkId;
    private String talkTitle;

    public static Fragment newInstance(String talkId, String talkTitle) {
        return setFragmentArguments(new RatingFragment(), newFluentBundle().put(EXTRA_TALK_ID, talkId).put(EXTRA_TALK_TITLE, talkTitle));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        talkId = getArguments().getString(EXTRA_TALK_ID);
        talkTitle = getArguments().getString(EXTRA_TALK_TITLE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rating, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.inject(this, view);

        mSessionTitle.setText(talkTitle);
    }

    @OnClick(R.id.submit_feedback_button)
    public void onSubmitClick() {
        int rating = (int) mSessionRatingFeedbackBar.getRating();
        int q1Answer = mQ1FeedbackBar.getProgress();
        int q2Answer = mQ2FeedbackBar.getProgress();
        int q3Answer = mQ3FeedbackBar.getProgress();

        String comments = mComments.getText().toString();

        if (null == comments) {
            comments = "";
        }

        String answers = talkId + ", "
                + rating + ", "
                + q1Answer + ", "
                + q2Answer + ", "
                + q3Answer + ", "
                + comments;

        Log.i("SubmitFeedback", answers);

        /*ContentValues values = new ContentValues();
        values.put(ScheduleContract.Feedback.SESSION_ID, sessionId);
        values.put(ScheduleContract.Feedback.UPDATED, System.currentTimeMillis());
        values.put(ScheduleContract.Feedback.SESSION_RATING, rating);
        values.put(ScheduleContract.Feedback.ANSWER_RELEVANCE, q1Answer);
        values.put(ScheduleContract.Feedback.ANSWER_CONTENT, q2Answer);
        values.put(ScheduleContract.Feedback.ANSWER_SPEAKER, q3Answer);
        values.put(ScheduleContract.Feedback.COMMENTS, comments);

        Uri uri = context.getContentResolver()
                .insert(ScheduleContract.Feedback.buildFeedbackUri(sessionId), values);
        LOGD(TAG, null == uri ? "No feedback was saved" : uri.toString());*/
    }
}
