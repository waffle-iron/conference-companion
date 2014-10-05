package fr.xebia.conference.companion.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import fr.xebia.conference.companion.core.KouignAmanApplication;
import fr.xebia.conference.companion.core.misc.Preferences;
import fr.xebia.conference.companion.model.Rating;
import fr.xebia.conference.companion.model.Talk;
import fr.xebia.conference.companion.model.Vote;
import retrofit.client.Response;
import se.emilsjolander.sprinkles.Query;

public class SendRatingIntentService extends IntentService {

    public static final String ACTION_SEND_RATING = "fr.xebia.conference.companion.service.ACTION_SEND_RATING";

    private static final String EXTRA_CONFERENCE_ID = "fr.xebia.conference.companion.service.EXTRA_CONFERENCE_ID";
    private static final String EXTRA_TALK_ID = "fr.xebia.conference.companion.service.EXTRA_TALK_ID";

    private static final int ONE_HOUR_MILLIS = 60 * 60 * 1000;

    public static Intent buildSendRatingIntent(Context context, Talk talk) {
        Intent sendRatingIntent = new Intent(ACTION_SEND_RATING, null, context, SendRatingIntentService.class);
        sendRatingIntent.putExtra(EXTRA_CONFERENCE_ID, talk.getConferenceId());
        sendRatingIntent.putExtra(EXTRA_TALK_ID, talk.getId());
        sendRatingIntent.setData(new Uri.Builder().authority("fr.xebia.conference.companion")
                .path(String.valueOf(talk.getConferenceId())).path(talk.getId()).build());
        return sendRatingIntent;
    }

    public static Intent buildSendRatingIntent(Context context, Vote vote) {
        Intent sendRatingIntent = new Intent(ACTION_SEND_RATING, null, context, SendRatingIntentService.class);
        sendRatingIntent.putExtra(EXTRA_CONFERENCE_ID, vote.getConferenceId());
        sendRatingIntent.putExtra(EXTRA_TALK_ID, vote.getTalkId());
        sendRatingIntent.setData(new Uri.Builder().authority("fr.xebia.conference.companion")
                .path(String.valueOf(vote.getConferenceId())).path(vote.getTalkId()).build());
        return sendRatingIntent;
    }

    public SendRatingIntentService() {
        super("SendRatingIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int conferenceId = intent.getIntExtra(EXTRA_CONFERENCE_ID, -1);
        String talkId = intent.getStringExtra(EXTRA_TALK_ID);
        Vote vote = Query.one(Vote.class, "SELECT * FROM Votes WHERE _id=? AND conferenceId=?", talkId, conferenceId).get();
        try {
            Response response = KouignAmanApplication.getConferenceApi().sendRating(conferenceId, buildRating(vote));
            if (response == null || response.getStatus() != 201) {
                rescheduleSendRating(vote);
            }
        } catch (Exception e) {
            rescheduleSendRating(vote);
        }
    }

    private void rescheduleSendRating(Vote vote) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + ONE_HOUR_MILLIS,
                PendingIntent.getService(getBaseContext(), 1, buildSendRatingIntent(getBaseContext(), vote), PendingIntent.FLAG_UPDATE_CURRENT));
    }

    private Rating buildRating(Vote vote) {
        return new Rating(Preferences.getGeneratedDeviceId(getBaseContext()), vote.getConferenceId(),
                vote.getNote(), vote.getTalkId());
    }
}
