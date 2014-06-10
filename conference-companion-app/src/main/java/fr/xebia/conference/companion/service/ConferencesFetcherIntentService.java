package fr.xebia.conference.companion.service;

import android.app.IntentService;
import android.content.Intent;
import fr.xebia.conference.companion.bus.ConferenceFetchedEvent;
import fr.xebia.conference.companion.core.KouignAmanApplication;
import fr.xebia.conference.companion.model.Conference;
import retrofit.RetrofitError;
import se.emilsjolander.sprinkles.ModelList;
import se.emilsjolander.sprinkles.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.xebia.conference.companion.core.KouignAmanApplication.BUS;

public class ConferencesFetcherIntentService extends IntentService {

    public ConferencesFetcherIntentService() {
        super("ConferencesFetcherIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            List<Conference> conferences = KouignAmanApplication.getConferenceApi().getAvailableConferences();
            Map<Integer, Conference> conferencesInDbById = retrieveConferencesInDbById();
            ModelList<Conference> conferencesToStore = new ModelList<>();
            for (Conference conference : conferences) {
                Conference conferenceInDb = conferencesInDbById.remove(conference.getId());
                if (conferenceInDb != null) {
                    conference.setNfcTag(conferenceInDb.getNfcTag());
                }
                conferencesToStore.add(conference);
            }
            conferencesToStore.addAll(conferences);
            conferencesToStore.saveAll();

            if (conferencesInDbById.size() > 0) {
                new ModelList<>(conferencesInDbById.values()).deleteAll();
            }
            BUS.post(new ConferenceFetchedEvent(true));
        } catch (RetrofitError e) {
            BUS.post(new ConferenceFetchedEvent(false));
        }
    }

    private Map<Integer, Conference> retrieveConferencesInDbById() {
        List<Conference> conferences = Query.many(Conference.class, "SELECT * FROM Conferences").get().asList();
        Map<Integer, Conference> conferencesInDbById = new HashMap<>();
        for (Conference conference : conferences) {
            conferencesInDbById.put(conference.getId(), conference);
        }
        return conferencesInDbById;
    }
}
