package fr.xebia.conference.companion.core;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.crashlytics.android.Crashlytics;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import fr.xebia.conference.companion.BuildConfig;
import fr.xebia.conference.companion.api.BleLocationApi;
import fr.xebia.conference.companion.api.ConferenceApi;
import fr.xebia.conference.companion.api.VoteApi;
import fr.xebia.conference.companion.bus.SyncEvent;
import fr.xebia.conference.companion.core.db.DbSchema;
import fr.xebia.conference.companion.core.misc.Preferences;
import fr.xebia.conference.companion.core.network.JacksonConverter;
import retrofit.RestAdapter;
import se.emilsjolander.sprinkles.Migration;
import se.emilsjolander.sprinkles.Sprinkles;
import timber.log.Timber;

public class KouignAmanApplication extends Application {

    private static VoteApi sVoteApi;
    private static ConferenceApi sConferenceApi;
    private static BleLocationApi sBleLocationApi;

    public static final EventBus BUS = EventBus.getDefault();

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }

        Context applicationContext = getApplicationContext();
        if(!Preferences.isDeviceIdGenerated(applicationContext)){
            Preferences.saveGeneratedDeviceId(applicationContext, UUID.randomUUID().toString());
        }

        RestAdapter.Builder restAdapterBuilder = new RestAdapter.Builder().setConverter(new JacksonConverter());

        sVoteApi = restAdapterBuilder.setEndpoint(BuildConfig.ROOT_URL).build().create(VoteApi.class);
        sBleLocationApi = restAdapterBuilder.setEndpoint(BuildConfig.LOCATION_URL).build().create(BleLocationApi.class);
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
                sqLiteDatabase.execSQL(DbSchema.TALKS_ADD_POSITION);
                sqLiteDatabase.execSQL(DbSchema.CONFERENCES_ADD_FROM_UTC_TIME);
                sqLiteDatabase.execSQL(DbSchema.CONFERENCES_ADD_TO_UTC_TIME);
            }
        });

        // TODO temporary hack to send sync event
        new Timer(true).scheduleAtFixedRate(new SendSyncEventTask(),new Date(), 5_000);
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

    public static VoteApi getVoteApi() {
        return sVoteApi;
    }

    public static ConferenceApi getConferenceApi() {
        return sConferenceApi;
    }

    public static BleLocationApi getBleLocationApi() {
        return sBleLocationApi;
    }
}
