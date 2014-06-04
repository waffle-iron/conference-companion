package fr.xebia.conference.companion.core;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import de.greenrobot.event.EventBus;
import fr.xebia.conference.companion.BuildConfig;
import fr.xebia.conference.companion.api.ConferenceApi;
import fr.xebia.conference.companion.api.VoteApi;
import fr.xebia.conference.companion.core.db.DbSchema;
import fr.xebia.conference.companion.core.network.JacksonConverter;
import retrofit.RestAdapter;
import se.emilsjolander.sprinkles.Migration;
import se.emilsjolander.sprinkles.Sprinkles;

public class KouignAmanApplication extends Application {

    private static VoteApi sVoteApi;
    private static ConferenceApi sConferenceApi;

    public static final EventBus BUS = EventBus.getDefault();

    @Override
    public void onCreate() {
        super.onCreate();
        RestAdapter.Builder restAdapterBuilder = new RestAdapter.Builder().setConverter(new JacksonConverter());

        sVoteApi = restAdapterBuilder.setEndpoint(BuildConfig.ROOT_URL).build().create(VoteApi.class);
        sConferenceApi = restAdapterBuilder.setEndpoint(BuildConfig.BACKEND_URL).build().create(ConferenceApi.class);

        Sprinkles sprinkles = Sprinkles.init(getApplicationContext(), "conferences.db", 0);

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

    }

    public static VoteApi getVoteApi() {
        return sVoteApi;
    }

    public static ConferenceApi getConferenceApi() {
        return sConferenceApi;
    }
}
