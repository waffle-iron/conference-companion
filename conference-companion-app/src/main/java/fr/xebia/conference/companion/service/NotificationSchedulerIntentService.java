package fr.xebia.conference.companion.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.core.misc.Preferences;
import fr.xebia.conference.companion.model.Talk;
import fr.xebia.conference.companion.ui.schedule.MyScheduleActivity;
import fr.xebia.conference.companion.ui.talk.TalkActivity;
import se.emilsjolander.sprinkles.Query;

public class NotificationSchedulerIntentService extends IntentService {

    public static final String ACTION_SCHEDULE_NOTIFICATION = "fr.xebia.conference.companion.service.ACTION_SCHEDULE_NOTIFICATION";
    public static final String ACTION_SEND_NOTIFICATION = "fr.xebia.conference.companion.service.ACTION_SEND_NOTIFICATION";

    public static final String EXTRA_CONFERENCE_ID = "fr.xebia.conference.companion.service.EXTRA_CONFERENCE_ID";
    public static final String EXTRA_TALK_ID = "fr.xebia.conference.companion.service.EXTRA_TALK_ID";
    public static final int TALK_NOTIFICATION_ID = 100;

    private static final int NOTIFICATION_LED_ON_MS = 100;
    private static final int NOTIFICATION_LED_OFF_MS = 1000;
    private static final int MILLIS_IN_MIN = 60 * 1000;
    private static final int MILLIS_GAP_FOR_NOTIFICATION = 5 * 60 * 1000;

    private AlarmManager mAlarmManager;

    public NotificationSchedulerIntentService() {
        super("NotificationSchedulerIntentService");
    }

    public static Intent buildScheduleNotificationIntentFromTalk(Talk talk) {
        Intent sendNotificationIntent = new Intent(ACTION_SCHEDULE_NOTIFICATION);
        sendNotificationIntent.putExtra(EXTRA_CONFERENCE_ID, talk.getConferenceId());
        sendNotificationIntent.putExtra(EXTRA_TALK_ID, talk.getId());
        return sendNotificationIntent;
    }

    public static Intent buildSendNotificationIntentFromTalk(Talk talk) {
        Intent sendNotificationIntent = new Intent(ACTION_SEND_NOTIFICATION);
        sendNotificationIntent.putExtra(EXTRA_CONFERENCE_ID, talk.getConferenceId());
        sendNotificationIntent.putExtra(EXTRA_TALK_ID, talk.getId());
        return sendNotificationIntent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
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
        }
    }

    private void sendNotification(Talk talk) {
        if (!talk.isFavorite() /*|| Preferences.isTalkAlreadyNotified(this, talk) Disable for testing*/) {
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

    private void scheduleNotification(Talk talk) {
        PendingIntent sendNotificationPendingIntent =
                PendingIntent.getService(this, 2, buildSendNotificationIntentFromTalk(talk), PendingIntent.FLAG_CANCEL_CURRENT);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, talk.getFromUtcTime() - MILLIS_GAP_FOR_NOTIFICATION, sendNotificationPendingIntent);
    }
}
