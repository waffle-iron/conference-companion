package fr.xebia.devoxx.be.wear;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import fr.xebia.devoxx.be.R;
import timber.log.Timber;

public class HomeListenerService extends WearableListenerService {

    private static final String TAG = "HomeListenerService";

    public final static String ACTION_DISMISS = "fr.xebia.devoxx.be.wear.ACTION_DISMISS";

    public static final String PATH_FEEDBACK = "/companion/feedback/";
    public static final String PATH_RATING = "/companion/rating/";

    private static final int NOTIFICATION_ID = 200;
    private static int notificationCounter = 1;

    public static final String KEY_TALK_ID = "talkId";
    public static final String KEY_TALK_TITLE = "talkTitle";
    public static final String KEY_TALK_SPEAKERS = "talkSpeakers";
    public static final String KEY_TALK_ROOM = "talkRoom";
    public static final String KEY_TALK_COLOR = "talkColor";
    public static final String KEY_NOTIFICATION_ID = "notificationId";

    private GoogleApiClient mGoogleApiClient;
    private final static long TIMEOUT_S = 10;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_DISMISS.equals(action)) {
                dismissPhoneNotification(intent.getStringExtra(KEY_TALK_ID));
            }
        }
        return Service.START_NOT_STICKY;
    }

    /**
     * Removes the Data Item that was used to create a notification on the watch. By deleting the
     * data item, a {@link com.google.android.gms.wearable.WearableListenerService} on the watch
     * will be notified and the notification on the watch will be removed.
     * <p/>
     * Since connection to the Google API client is asynchronous, we spawn a thread nd put it to
     * sleep waiting for the connection to be established before attempting to use the Google API
     * client.
     *
     * @param sessionId The Session ID` of the notification that should be removed
     */
    private void dismissPhoneNotification(final String sessionId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mGoogleApiClient.blockingConnect(TIMEOUT_S, TimeUnit.SECONDS);
                if (!mGoogleApiClient.isConnected()) {
                    return;
                }
                PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(buildFeedbackPath(sessionId));
                if (mGoogleApiClient.isConnected()) {
                    Wearable.DataApi.deleteDataItems(mGoogleApiClient, putDataMapRequest.getUri()).await();
                } else {
                    Timber.e(TAG, "dismissWearableNotification()): No Google API Client connection");
                }
            }
        }).start();
    }

    private String buildFeedbackPath(String talkId) {
        return PATH_FEEDBACK + talkId;
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            Uri uri = event.getDataItem().getUri();
            if (event.getType() == DataEvent.TYPE_DELETED) {
                if (uri.getPath().startsWith(PATH_FEEDBACK)) {
                    dismissLocalNotification(uri.getLastPathSegment());
                }
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                if (uri.getPath().startsWith(PATH_FEEDBACK)) {
                    setupNotification(event.getDataItem());
                }
            }
        }
    }

    private void setupNotification(DataItem dataItem) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest
                .createFromDataMapItem(DataMapItem.fromDataItem(dataItem));
        final DataMap dataMap = putDataMapRequest.getDataMap();
        String talkId = dataMap.getString(KEY_TALK_ID);
        String talkRoom = dataMap.getString(KEY_TALK_ROOM);
        String talkTitle = dataMap.getString(KEY_TALK_TITLE);
        String talkSpeakers = dataMap.getString(KEY_TALK_SPEAKERS);
        int talkColor = dataMap.getInt(KEY_TALK_COLOR);

        int notificationId = (int) new Date().getTime();
        Intent intent = new Intent(ACTION_DISMISS);
        intent.putExtra(KEY_TALK_ID, dataMap.getString(KEY_TALK_ID));

        PendingIntent deleteIntent = PendingIntent.getService(this, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent showCardIntent = showCardIntent(talkId, talkRoom, talkTitle, talkSpeakers, talkColor, notificationId);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.rate_this_presentation))
                .setDeleteIntent(deleteIntent)
                .setContentText(talkTitle)
                .extend(new NotificationCompat.WearableExtender()
                        .setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.feedback_bg))
                        .setCustomSizePreset(NotificationCompat.WearableExtender.SIZE_LARGE)
                        .setDisplayIntent(showCardIntent));

        NotificationManagerCompat.from(this).notify(talkId, NOTIFICATION_ID, builder.build());
    }

    private PendingIntent showCardIntent(String talkId, String talkRoom, String talkTitle, String speakers, int talkColor, int notificationId) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(KEY_TALK_ID, talkId);
        intent.putExtra(KEY_TALK_ROOM, talkRoom);
        intent.putExtra(KEY_TALK_TITLE, talkTitle);
        intent.putExtra(KEY_TALK_SPEAKERS, speakers);
        intent.putExtra(KEY_TALK_COLOR, talkColor);
        intent.putExtra(KEY_NOTIFICATION_ID, notificationId);
        return PendingIntent.getActivity(this, notificationCounter++, intent, 0);
    }

    private void dismissLocalNotification(String sessionId) {
        NotificationManagerCompat.from(this).cancel(sessionId, HomeListenerService.NOTIFICATION_ID);
    }
}
