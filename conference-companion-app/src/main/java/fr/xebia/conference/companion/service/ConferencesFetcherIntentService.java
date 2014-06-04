package fr.xebia.conference.companion.service;

import android.app.IntentService;
import android.content.Intent;
import fr.xebia.conference.companion.bus.ConferenceFetchedEvent;
import fr.xebia.conference.companion.core.KouignAmanApplication;
import fr.xebia.conference.companion.model.Conference;
import retrofit.RetrofitError;
import se.emilsjolander.sprinkles.ModelList;

import java.util.List;

import static fr.xebia.conference.companion.core.KouignAmanApplication.BUS;

public class ConferencesFetcherIntentService extends IntentService {

    public ConferencesFetcherIntentService() {
        super("ConferencesFetcherIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            List<Conference> conferences = KouignAmanApplication.getConferenceApi().getAvailableConferences();
            ModelList<Conference> conferencesToStore = new ModelList<>();
            conferencesToStore.addAll(conferences);
            conferencesToStore.saveAll();
            BUS.post(new ConferenceFetchedEvent(true));
        } catch (RetrofitError e) {
            BUS.post(new ConferenceFetchedEvent(false));
        }
    }
}
