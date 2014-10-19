package fr.xebia.conference.companion.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.List;

import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.core.misc.Preferences;
import fr.xebia.conference.companion.model.Talk;
import fr.xebia.conference.companion.ui.schedule.MyScheduleActivity;
import fr.xebia.conference.companion.ui.talk.TalkActivity;
import se.emilsjolander.sprinkles.Query;

public class NotificationSchedulerIntentService extends IntentService {

    public static final String ACTION_SCHEDULE_NOTIFICATION = "fr.xebia.conference.companion.service.ACTION_SCHEDULE_NOTIFICATION";
    public static final String ACTION_SCHEDULE_ALL_NOTIFICATIONS = "fr.xebia.conference.companion.service.ACTION_SCHEDULE_ALL_NOTIFICATION";
    public static final String ACTION_SEND_NOTIFICATION = "fr.xebia.conference.companion.service.ACTION_SEND_NOTIFICATION";
    public static final String ACTION_SEND_FEEDBACK_NOTIFICATION = "fr.xebia.conference.companion.service.ACTION_SEND_FEEDBACK_NOTIFICATION";

    public static final String EXTRA_CONFERENCE_ID = "fr.xebia.conference.companion.service.EXTRA_CONFERENCE_ID";
    public static final String EXTRA_TALK_ID = "fr.xebia.conference.companion.service.EXTRA_TALK_ID";
    public static final int TALK_NOTIFICATION_ID = 100;

    private static final int NOTIFICATION_LED_ON_MS = 100;
    private static final int NOTIFICATION_LED_OFF_MS = 1000;
    private static final int MILLIS_IN_MIN = 60 * 1000;
    private static final int MILLIS_GAP_FOR_NOTIFICATION = 5 * 60 * 1000;
    private static final int MILLIS_GAP_FOR_FEEDBACK_NOTIFICATION = 10 * 60 * 1000;

    private AlarmManager mAlarmManager;

    public NotificationSchedulerIntentService() {
        super("NotificationSchedulerIntentService");
    }

    public static Intent buildScheduleNotificationIntentFromTalk(Context context, Talk talk) {
        Intent sendNotificationIntent = new Intent(ACTION_SCHEDULE_NOTIFICATION, null, context, NotificationSchedulerIntentService.class);
        sendNotificationIntent.putExtra(EXTRA_CONFERENCE_ID, talk.getConferenceId());
        sendNotificationIntent.putExtra(EXTRA_TALK_ID, talk.getId());
        return sendNotificationIntent;
    }

    public static Intent buildSendNotificationIntentFromTalk(Context context, Talk talk) {
        Intent sendNotificationIntent = new Intent(ACTION_SEND_NOTIFICATION, null, context, NotificationSchedulerIntentService.class);
        sendNotificationIntent.putExtra(EXTRA_CONFERENCE_ID, talk.getConferenceId());
        sendNotificationIntent.putExtra(EXTRA_TALK_ID, talk.getId());
        sendNotificationIntent.setData(new Uri.Builder().authority("fr.xebia.conference.companion")
                .path(String.valueOf(talk.getConferenceId())).path(talk.getId()).build());
        return sendNotificationIntent;
    }


    private static Intent buildSendFeedbackNotificationIntentFromTalk(Context context, Talk talk) {
        Intent sendNotificationIntent = new Intent(ACTION_SEND_FEEDBACK_NOTIFICATION, null, context, NotificationSchedulerIntentService.class);
        sendNotificationIntent.putExtra(EXTRA_CONFERENCE_ID, talk.getConferenceId());
        sendNotificationIntent.putExtra(EXTRA_TALK_ID, talk.getId());
        sendNotificationIntent.setData(new Uri.Builder().authority("fr.xebia.conference.companion")
                .path(String.valueOf(talk.getConferenceId())).path(talk.getId()).build());
        return sendNotificationIntent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ACTION_SCHEDULE_ALL_NOTIFICATIONS.equals(intent.getAction())) {
            handleAllTalksNotifications(intent);
        } else {
            handleSingleTalkNotifications(intent);
        }
    }

    private void handleAllTalksNotifications(Intent intent) {
        int conferenceId = intent.getIntExtra(EXTRA_CONFERENCE_ID, -1);
        List<Talk> talks = Query.many(Talk.class, "SELECT * FROM Talks WHERE conferenceId=? AND favorite=1", conferenceId).get().asList();
        for (Talk talk : talks) {
            scheduleNotification(talk);
        }
    }

    private void handleSingleTalkNotifications(Intent intent) {
        int conferenceId = intent.getIntExtra(EXTRA_CONFERENCE_ID, -1);
        String talkId = intent.getStringExtra(EXTRA_TALK_ID);
        Talk talk = Query.one(Talk.class, "SELECT * FROM Talks WHERE conferenceId=? AND _id=?", conferenceId, talkId).get();
        if (talk == null) {
            return;
        }
        switch (intent.getAction()) {
            case ACTION_SCHEDULE_NOTIFICATION:
                scheduleNotification(talk);
                break;
            case ACTION_SEND_NOTIFICATION:
                sendNotification(talk);
                break;
            case ACTION_SEND_FEEDBACK_NOTIFICATION:
                sendFeedbackNotification(talk);
                break;
        }
    }


    private void sendNotification(Talk talk) {
        if (!talk.isFavorite() /*|| Preferences.isTalkAlreadyNotified(this, talk)*/) {
            return;
        }

        final Resources res = getResources();
        String contentText;
        int minutesLeft = Math.round((talk.getFromUtcTime() - System.currentTimeMillis()) / (float) MILLIS_IN_MIN);
        if (minutesLeft < 0) {
            return;
        }

        if (minutesLeft < 1) {
            minutesLeft = 1;
        }

        contentText = res.getString(R.string.session_notification_text, minutesLeft);


        PendingIntent pi = TaskStackBuilder.create(this)
                .addNextIntent(new Intent(this, MyScheduleActivity.class))
                .addNextIntent(TalkActivity.buildIntentFromTalk(this, talk))
                .getPendingIntent(1, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(talk.getTitle())
                .setContentText(contentText)
                .setTicker(res.getString(R.string.scheduled_talk_notification_ticker))
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setLights(
                        talk.getColor(),
                        NotificationSchedulerIntentService.NOTIFICATION_LED_ON_MS,
                        NotificationSchedulerIntentService.NOTIFICATION_LED_OFF_MS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pi)
                .setLocalOnly(true) // make it local to the phone
                .setAutoCancel(true);
        NotificationManager nm = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        nm.notify(talk.getId(), TALK_NOTIFICATION_ID, notifBuilder.build());

        Preferences.flagTalkAsNotified(this, talk);
    }

    private void sendFeedbackNotification(Talk talk) {
        if (!talk.isFavorite() /*|| Preferences.isTalkFeedbackAlreadyNotified(this, talk)*/) {
            return;
        }

        int minutesLeft = Math.round((talk.getToUtcTime() - System.currentTimeMillis()) / (float) MILLIS_IN_MIN);
        if (minutesLeft < 0) {
            return;
        }

        PendingIntent pi = TaskStackBuilder.create(this)
                .addNextIntent(new Intent(this, MyScheduleActivity.class))
                .addNextIntent(TalkActivity.buildIntentFromTalk(this, talk))
                .getPendingIntent(1, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(talk.getTitle())
                .setContentText(getString(R.string.feedback_notification))
                .setTicker(getString(R.string.feedback_notification))
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setLights(
                        talk.getColor(),
                        NotificationSchedulerIntentService.NOTIFICATION_LED_ON_MS,
                        NotificationSchedulerIntentService.NOTIFICATION_LED_OFF_MS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pi)
                .setLocalOnly(true) // make it local to the phone
                .setAutoCancel(true);
        NotificationManager nm = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        nm.notify(talk.getId(), TALK_NOTIFICATION_ID, notifBuilder.build());

        Preferences.flagTalkFeedbackAsNotified(this, talk);
    }

    private void scheduleNotification(Talk talk) {
        PendingIntent sendNotificationPendingIntent =
                PendingIntent.getService(this, 2, buildSendNotificationIntentFromTalk(getBaseContext(), talk), PendingIntent.FLAG_CANCEL_CURRENT);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, talk.getFromUtcTime() - MILLIS_GAP_FOR_NOTIFICATION, sendNotificationPendingIntent);

        PendingIntent sendFeedBackNotificationPendingIntent =
                PendingIntent.getService(this, 3, buildSendFeedbackNotificationIntentFromTalk(getBaseContext(), talk), PendingIntent.FLAG_CANCEL_CURRENT);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, talk.getToUtcTime() - MILLIS_GAP_FOR_FEEDBACK_NOTIFICATION, sendFeedBackNotificationPendingIntent);
    }

}
