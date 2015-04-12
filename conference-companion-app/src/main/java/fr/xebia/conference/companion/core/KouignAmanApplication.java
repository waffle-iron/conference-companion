package fr.xebia.conference.companion.core;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.OkHttpClient;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import fr.xebia.conference.companion.BuildConfig;
import fr.xebia.conference.companion.api.ConferenceApi;
import fr.xebia.conference.companion.bus.SyncEvent;
import fr.xebia.conference.companion.core.db.DbSchema;
import fr.xebia.conference.companion.core.misc.Preferences;
import fr.xebia.conference.companion.core.network.JacksonConverter;
import fr.xebia.conference.companion.service.SynchroIntentService;
import io.fabric.sdk.android.Fabric;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import se.emilsjolander.sprinkles.Migration;
import se.emilsjolander.sprinkles.Sprinkles;
import timber.log.Timber;

public class KouignAmanApplication extends Application {

    private static ConferenceApi sConferenceApi;

    public static final EventBus BUS = EventBus.getDefault();

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

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
        RestAdapter.Builder restAdapterBuilder = new RestAdapter.Builder().setClient(new OkClient(okHttpClient)).setConverter(new JacksonConverter());

        sConferenceApi = restAdapterBuilder.setEndpoint(BuildConfig.BACKEND_URL).build().create(ConferenceApi.class);

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
        }
    }

    /**
     * A tree which logs important information for crash reporting.
     */
    private static class CrashReportingTree extends Timber.HollowTree {
        @Override
        public void e(Throwable t, String message, Object... args) {
            e(message, args);
            Crashlytics.logException(t);
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
}
