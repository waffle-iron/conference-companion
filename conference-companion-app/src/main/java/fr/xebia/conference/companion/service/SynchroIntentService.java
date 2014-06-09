package fr.xebia.conference.companion.service;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import fr.xebia.conference.companion.bus.SynchroFinishedEvent;
import fr.xebia.conference.companion.core.KouignAmanApplication;
import fr.xebia.conference.companion.model.Speaker;
import fr.xebia.conference.companion.model.SpeakerTalk;
import fr.xebia.conference.companion.model.Talk;
import fr.xebia.conference.companion.model.TrackColors;
import se.emilsjolander.sprinkles.ModelList;
import se.emilsjolander.sprinkles.Query;
import se.emilsjolander.sprinkles.Transaction;
import timber.log.Timber;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Transaction transaction = new Transaction();
        try {
            if (conferenceId == -1) {
                BUS.post(new SynchroFinishedEvent(false, conferenceId));
            } else {
                synchroniseSpeakers(conferenceId, transaction);
                synchroniseTalks(conferenceId, transaction);
                transaction.setSuccessful(true);
                BUS.post(new SynchroFinishedEvent(true, conferenceId));
            }
        } catch (Exception e) {
            Timber.e(e, "Error synchronizing data");
            transaction.setSuccessful(false);
            BUS.post(new SynchroFinishedEvent(false, conferenceId));
        } finally {
            transaction.finish();
        }
    }

    private void synchroniseTalks(int conferenceId, Transaction transaction) {
        Map<String, Talk> talksFromWsById = loadTalks(conferenceId);
        Map<String, Talk> talksInDbById = loadTalksFromDb(conferenceId);
        List<Talk> scheduledTalks = KouignAmanApplication.getConferenceApi().getSchedule(conferenceId);
        HashMap<String, Speaker> everySpeakers = loadEverySpeakers();

        // Save talks keeping favorite info and retrieving date/time from
        ModelList<Talk> talksToSave = new ModelList<>();
        for (Talk talkToSave : scheduledTalks) {
            Talk talkFromDb = talksInDbById.remove(talkToSave.getId());
            if (talkFromDb != null) {
                talkToSave.setFavorite(talkFromDb.isFavorite());
            }

            String talkDetailsId = talkToSave.getDetails().getId();
            talkToSave.setTalkDetailsId(talkDetailsId);

            Talk talkDetails = talksFromWsById.remove(talkToSave.getTalkDetailsId());
            if (talkDetails != null) {
                talkToSave.setPrettySpeakers(talkToSave.getSpeakers(), everySpeakers);
                talkToSave.setSummary(talkDetails.getSummary());
                talkToSave.setTrack(talkDetails.getTrack());
            }
            if (talkDetails != null || talkToSave.isBreak()) {
                talksToSave.add(talkToSave);
            } else {
                // Avoid adding weird presentation (i.e no intialized)
                // So put back to the map for later deletion
                talksInDbById.put(talkToSave.getId(), talkToSave);
            }
        }

        generateColorByTrack(talksToSave);

        talksToSave.saveAll(transaction);

        // Delete obsolete talks
        Collection<Talk> talksToDelete = talksInDbById.values();
        if (!talksToDelete.isEmpty()) {
            new ModelList<>(talksToDelete).deleteAll(transaction);
        }

        List<SpeakerTalk> speakerTalksToDelete = Query.many(SpeakerTalk.class, "SELECT * FROM Speaker_Talk").get().asList();
        ModelList<SpeakerTalk> speakerTalks = new ModelList<>();
        for (Talk talk : scheduledTalks) {
            Collection<Speaker> speakers = talk.getSpeakers();
            if (speakers != null) {
                for (Speaker speaker : speakers) {
                    SpeakerTalk speakerTalk = new SpeakerTalk(speaker.getId(), talk.getId(), conferenceId);
                    speakerTalks.add(speakerTalk);
                    speakerTalksToDelete.remove(speakerTalk);
                }
            }
        }
        // Delete obsolete speaker talks
        if (speakerTalksToDelete.size() > 0) {
            new ModelList<>(speakerTalks).deleteAll(transaction);
        }
        speakerTalks.saveAll(transaction);
    }

    private HashMap<String, Speaker> loadEverySpeakers() {
        List<Speaker> everySpeakers = Query.many(Speaker.class, "SELECT * FROM Speakers").get().asList();
        HashMap<String, Speaker> speakersMap = new HashMap<>();
        for (Speaker speaker : everySpeakers) {
            speakersMap.put(speaker.getId(), speaker);
        }
        return speakersMap;
    }

    private Map<String, Talk> loadTalks(int conferenceId) {
        List<Talk> scheduledTalks = KouignAmanApplication.getConferenceApi().getTalks(conferenceId);
        Map<String, Talk> scheduledTalksById = new HashMap<>();
        for (Talk talk : scheduledTalks) {
            scheduledTalksById.put(talk.getId(), talk);
        }
        return scheduledTalksById;
    }

    private Map<String, Talk> loadTalksFromDb(int conferenceId) {
        List<Talk> talksInDb = Query.many(Talk.class, "SELECT * FROM Talks WHERE conferenceId=?", conferenceId).get().asList();
        Map<String, Talk> talksInDbById = new HashMap<>();
        for (Talk talk : talksInDb) {
            talksInDbById.put(talk.getId(), talk);
        }
        return talksInDbById;
    }

    private void generateColorByTrack(List<Talk> talks) {
        HashMap<String, Integer> colorByTrack = new HashMap<>();
        int position = 0;
        for (Talk talk : talks) {
            String track = talk.getTrack();
            if (TextUtils.isEmpty(track)) {
                talk.setColor(TrackColors.NO_TRACK);
                continue;
            }
            Integer color = colorByTrack.get(track);
            if (color == null) {
                color = TrackColors.LIST.get(position);
                colorByTrack.put(track, color);
                position++;
                position = position % TrackColors.LIST.size();
            }
            talk.setColor(color);
        }
    }

    private void synchroniseSpeakers(int conferenceId, Transaction transaction) {
        List<Speaker> speakers = KouignAmanApplication.getConferenceApi().getSpeakers(conferenceId);
        List<Speaker> speakersToDelete = Query.many(Speaker.class, "SELECT * FROM Speakers").get().asList();
        ModelList<Speaker> speakersToSave = new ModelList<>();

        for (Speaker speaker : speakers) {
            speakersToSave.add(speaker);
            speakersToDelete.remove(speaker);
        }
        speakersToSave.saveAll(transaction);

        if (!speakersToDelete.isEmpty()) {
            speakersToDelete.addAll(speakersToDelete);
            new ModelList<>(speakersToDelete).deleteAll(transaction);
        }
    }
}
