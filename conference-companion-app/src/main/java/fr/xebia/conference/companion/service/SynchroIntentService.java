package fr.xebia.conference.companion.service;

import android.app.IntentService;
import android.content.Intent;
import fr.xebia.conference.companion.bus.SynchroFinishedEvent;
import fr.xebia.conference.companion.core.KouignAmanApplication;
import fr.xebia.conference.companion.model.Speaker;
import fr.xebia.conference.companion.model.SpeakerTalk;
import fr.xebia.conference.companion.model.Talk;
import fr.xebia.conference.companion.model.TrackColors;
import retrofit.RetrofitError;
import se.emilsjolander.sprinkles.ModelList;

import java.util.HashMap;
import java.util.List;

import static fr.xebia.conference.companion.core.KouignAmanApplication.BUS;

public class SynchroIntentService extends IntentService {

    private static final String TAG = "SynchroIntentService";

    public static final String EXTRA_CONFERENCE_ID = "fr.xebia.conference.companion.EXTRA_CONFERENCE_ID";

    public SynchroIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int conferenceId = intent.getIntExtra(EXTRA_CONFERENCE_ID, -1);
        try {
            if (conferenceId == -1) {
                BUS.post(new SynchroFinishedEvent(false, conferenceId));
            } else {
                loadSpeakers(conferenceId);
                loadTalks(conferenceId);
                BUS.post(new SynchroFinishedEvent(true, conferenceId));
            }
        } catch (RetrofitError e) {
            BUS.post(new SynchroFinishedEvent(false, conferenceId));
        }
    }

    private void loadTalks(int conferenceId) {
        List<Talk> talks = KouignAmanApplication.getConferenceApi().getTalks(conferenceId);

        generateColorByTrack(talks);

        ModelList<Talk> talksToStore = new ModelList<>();
        talksToStore.addAll(talks);
        talksToStore.saveAll();

        ModelList<SpeakerTalk> speakerTalks = new ModelList<>();
        for (Talk talk : talks) {
            List<Speaker> speakers = talk.getSpeakers();
            if (speakers != null) {
                for (Speaker speaker : speakers) {
                    speakerTalks.add(new SpeakerTalk(speaker.getId(), talk.getId(), conferenceId));
                }
            }
        }
        speakerTalks.saveAll();
    }

    private void generateColorByTrack(List<Talk> talks) {
        HashMap<String, Integer> colorByTrack = new HashMap<>();
        int position = 0;
        for (Talk talk : talks) {
            Integer color = colorByTrack.get(talk.getTrack());
            if (color == null) {
                color = TrackColors.LIST.get(position);
                colorByTrack.put(talk.getTrack(), color);
                position++;
                position = position % TrackColors.LIST.size();
            }
            talk.setColor(color);
        }
    }

    private void loadSpeakers(int conferenceId) {
        List<Speaker> speakers = KouignAmanApplication.getConferenceApi().getSpeakers(conferenceId);
        ModelList<Speaker> speakersToStore = new ModelList<>();
        speakersToStore.addAll(speakers);
        speakersToStore.saveAll();
    }
}
