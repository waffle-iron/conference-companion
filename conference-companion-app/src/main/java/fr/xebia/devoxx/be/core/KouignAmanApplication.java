package fr.xebia.devoxx.be.core;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import com.crashlytics.android.Crashlytics;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.okhttp.OkHttpClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.tweetui.TweetUi;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import fr.xebia.devoxx.be.BuildConfig;
import fr.xebia.devoxx.be.api.ConferenceApi;
import fr.xebia.devoxx.be.api.VoteApi;
import fr.xebia.devoxx.be.bus.SyncEvent;
import fr.xebia.devoxx.be.core.db.DbSchema;
import fr.xebia.devoxx.be.core.misc.Preferences;
import fr.xebia.devoxx.be.core.network.JacksonConverter;
import fr.xebia.devoxx.be.service.SynchroIntentService;
import io.fabric.sdk.android.Fabric;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import se.emilsjolander.sprinkles.Migration;
import se.emilsjolander.sprinkles.Sprinkles;
import timber.log.Timber;


public class KouignAmanApplication extends Application {

    private static ConferenceApi sConferenceApi;
    private static VoteApi sVoteApi;

    public static final EventBus BUS = EventBus.getDefault();

    @Override
    public void onCreate() {
        super.onCreate();

        LeakCanary.install(this);

        if(Preferences.getSelectedConference(this) != BuildConfig.DEVOXX_BE_CONFERENCE_ID) {
            Preferences.removeSelectedConference(this);
        }

        TwitterAuthConfig authConfig =  new TwitterAuthConfig(BuildConfig.TWITTER_KEY, BuildConfig.TWITTER_SECRET);
        Fabric.with(this, new Crashlytics(), new TwitterCore(authConfig), new TweetUi());

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }

        Context applicationContext = getApplicationContext();
        if (!Preferences.isDeviceIdGenerated(applicationContext)) {
            Preferences.saveGeneratedDeviceId(applicationContext, UUID.randomUUID().toString());
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        RestAdapter.Builder restAdapterBuilder = new RestAdapter.Builder()
                .setClient(new OkClient(okHttpClient))
                .setConverter(new JacksonConverter());

        sConferenceApi = restAdapterBuilder.setEndpoint(BuildConfig.BACKEND_URL).build().create(ConferenceApi.class);

        sVoteApi = restAdapterBuilder.setEndpoint(BuildConfig.VOTE_URL).build().create(VoteApi.class);

        Sprinkles sprinkles = Sprinkles.init(applicationContext, "conferences.db", 0);

        sprinkles.addMigration(new Migration() {
            @Override
            protected void doMigration(SQLiteDatabase sqLiteDatabase) {
                sqLiteDatabase.execSQL(DbSchema.SPEAKERS);
                sqLiteDatabase.execSQL(DbSchema.TALKS);
                sqLiteDatabase.execSQL(DbSchema.SPEAKER_TALKS);
                sqLiteDatabase.execSQL(DbSchema.VOTES);
                sqLiteDatabase.execSQL(DbSchema.CONFERENCES);
            }
        });

        sprinkles.addMigration(new Migration() {
            @Override
            protected void doMigration(SQLiteDatabase sqLiteDatabase) {
                sqLiteDatabase.execSQL(DbSchema.TALKS_ADD_FROM_UTC_TIME);
                sqLiteDatabase.execSQL(DbSchema.TALKS_ADD_TO_UTC_TIME);
                sqLiteDatabase.execSQL(DbSchema.CONFERENCES_ADD_FROM_UTC_TIME);
                sqLiteDatabase.execSQL(DbSchema.CONFERENCES_ADD_TO_UTC_TIME);
            }
        });

        sprinkles.addMigration(new Migration() {
            @Override
            protected void doMigration(SQLiteDatabase sqLiteDatabase) {
                sqLiteDatabase.execSQL(DbSchema.TALKS_ADD_POSITION);
            }
        });

        // TODO temporary hack to send sync event
        new Timer(true).scheduleAtFixedRate(new SendSyncEventTask(), new Date(), 5_000);

        if (Preferences.hasSelectedConference(this)) {
            Intent intent = new Intent(this, SynchroIntentService.class);
            intent.putExtra(SynchroIntentService.EXTRA_CONFERENCE_ID, Preferences.getSelectedConference(this));
            intent.putExtra(SynchroIntentService.EXTRA_FROM_APP_CREATE, true);
            startService(intent);
        } else {
            SynchroIntentService.scheduleSync(this, true);
        }
    }

    /**
     * A tree which logs important information for crash reporting.
     */
    private static class CrashReportingTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (t != null) {
                Crashlytics.logException(t);
            }
        }
    }

    public static class SendSyncEventTask extends TimerTask {
        @Override
        public void run() {
            BUS.post(SyncEvent.getInstance());
        }
    }

    public static ConferenceApi getConferenceApi() {
        return sConferenceApi;
    }

    public static VoteApi getVoteApi() {
        return sVoteApi;
    }


}
