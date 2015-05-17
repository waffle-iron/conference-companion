package fr.xebia.devoxx.uk.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.text.TextUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import fr.xebia.devoxx.uk.bus.SynchroFinishedEvent;
import fr.xebia.devoxx.uk.core.KouignAmanApplication;
import fr.xebia.devoxx.uk.core.misc.Preferences;
import fr.xebia.devoxx.uk.model.Conference;
import fr.xebia.devoxx.uk.model.Speaker;
import fr.xebia.devoxx.uk.model.SpeakerTalk;
import fr.xebia.devoxx.uk.model.Talk;
import fr.xebia.devoxx.uk.model.TrackColors;
import se.emilsjolander.sprinkles.ModelList;
import se.emilsjolander.sprinkles.Query;
import se.emilsjolander.sprinkles.Transaction;
import timber.log.Timber;

import static fr.xebia.devoxx.uk.core.KouignAmanApplication.BUS;

public class SynchroIntentService extends IntentService {

    private static final String TAG = "SynchroIntentService";

    public static final String EXTRA_CONFERENCE_ID = "fr.xebia.devoxx.uk.EXTRA_CONFERENCE_ID";
    public static final String EXTRA_FROM_APP_CREATE = "fr.xebia.devoxx.uk.EXTRA_FROM_APP_CREATE";

    public SynchroIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int conferenceId = intent.getIntExtra(EXTRA_CONFERENCE_ID, -1);
        Conference conference = Query.one(Conference.class, "SELECT * FROM Conferences WHERE _id=?", conferenceId).get();
        Transaction transaction = null;
        boolean sendSynchroEvent = !intent.hasExtra(EXTRA_FROM_APP_CREATE);
        try {
            if (conferenceId == -1) {
                BUS.post(new SynchroFinishedEvent(false, null));
            } else {
                // Load data before starting transaction
                List<Speaker> speakers = KouignAmanApplication.getConferenceApi().getSpeakers(conferenceId);
                Map<String, Talk> talksFromWsById = loadTalks(conferenceId);
                List<Talk> scheduledTalks = KouignAmanApplication.getConferenceApi().getSchedule(conferenceId);
                transaction = new Transaction();
                synchroniseSpeakers(speakers, transaction);
                synchroniseTalks(conference, scheduledTalks, talksFromWsById, transaction);
                transaction.setSuccessful(true);
                Preferences.setSelectedConference(this, conference.getId());
                Preferences.setSelectedConferenceEndTime(this, conference.getToUtcTime());
                Preferences.setSelectedConferenceStartTime(this, conference.getFromUtcTime());
                startService(new Intent(NotificationSchedulerIntentService.ACTION_SCHEDULE_ALL_NOTIFICATIONS, null, this, NotificationSchedulerIntentService.class));
                if (sendSynchroEvent) {
                    BUS.post(new SynchroFinishedEvent(true, conference));
                }
            }
        } catch (Exception e) {
            Timber.d(e, "Error synchronizing data");
            if (transaction != null) {
                transaction.setSuccessful(false);
            }
            if (sendSynchroEvent) {
                BUS.post(new SynchroFinishedEvent(false, null));
            }
            // Retry in 1 hour
            long oneHourLater = System.currentTimeMillis() + 3_600 * 1000;
            ((AlarmManager) getSystemService(ALARM_SERVICE)).set(AlarmManager.RTC, oneHourLater, buildSynchroPendingIntent());
        } finally {
            if (transaction != null) {
                transaction.finish();
            }
        }
    }

    private PendingIntent buildSynchroPendingIntent() {
        Intent intent = new Intent(this, SynchroIntentService.class);
        intent.putExtra(SynchroIntentService.EXTRA_CONFERENCE_ID, Preferences.getSelectedConference(this));
        intent.putExtra(SynchroIntentService.EXTRA_FROM_APP_CREATE, true);
        return PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void synchroniseTalks(Conference conference, List<Talk> scheduledTalks, Map<String, Talk> talksFromWsById, Transaction transaction) {
        int conferenceId = conference.getId();
        Map<String, Talk> talksInDbById = loadTalksFromDb(conferenceId);
        HashMap<String, Speaker> everySpeakers = loadEverySpeakers();

        // Save talks keeping favorite info and retrieving date/time from
        ModelList<Talk> talksToSave = new ModelList<>();
        int index = 0;
        for (Talk talkToSave : scheduledTalks) {
            Talk talkFromDb = talksInDbById.remove(talkToSave.getId());
            if (talkFromDb != null) {
                talkToSave.setFavorite(talkFromDb.isFavorite() || talkToSave.isKeynote());
                talkToSave.setMemo(talkFromDb.getMemo());
            } else {
                talkToSave.setFavorite(talkToSave.isKeynote());
            }

            String talkDetailsId = talkToSave.getDetails().getId();
            talkToSave.setTalkDetailsId(talkDetailsId);

            Talk talkDetails = talksFromWsById.remove(talkDetailsId == null ? talkToSave.getId() : talkDetailsId);
            if (talkDetails != null) {
                talkToSave.setSummary(talkDetails.getSummary());
                talkToSave.setTrack(talkDetails.getTrack());
            }

            if (talkToSave.isKeynote()) {
                talkToSave.setTrack("Keynote");
            }

            talkToSave.setPrettySpeakers(talkToSave.getSpeakers(), everySpeakers);

            setConferenceUtcTime(conference, talkToSave);

            talkToSave.setPosition(index++);
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

    private void setConferenceUtcTime(Conference conference, Talk talkToSave) {
        DateTimeZone apiTimeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        DateTime jodaStartTime = new DateTime(talkToSave.getFromTime(), apiTimeZone);
        DateTime jodaEndTime = new DateTime(talkToSave.getToTime(), apiTimeZone);

        DateTimeZone utcTimeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC"));
        if (conference.getName().toLowerCase().contains("uk")) {
            // Devoxx UK is a specific case
            DateTimeZone ukTimeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/London"));
            talkToSave.getFromTime().setTime(jodaStartTime.withZone(ukTimeZone).withZoneRetainFields(apiTimeZone).getMillis());
            talkToSave.getToTime().setTime(jodaEndTime.withZone(ukTimeZone).withZoneRetainFields(apiTimeZone).getMillis());
        }

        talkToSave.setFromUtcTime(jodaStartTime.withZone(utcTimeZone).getMillis());
        talkToSave.setToUtcTime(jodaEndTime.withZone(utcTimeZone).getMillis());
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
        List<String> availableTracks = new ArrayList<>();
        for (Talk talk : talks) {
            String track = talk.getTrack();
            if (!availableTracks.contains(track)) {
                availableTracks.add(track);
            }
        }
        availableTracks.add("");
        Collections.sort(availableTracks);
        for (String track : availableTracks) {
            colorByTrack.put(track, TrackColors.LIST.get(position));
            position++;
            position = position % TrackColors.LIST.size();
        }
        for (Talk talk : talks) {
            String track = talk.getTrack();
            if (TextUtils.isEmpty(track)) {
                talk.setColor(TrackColors.NO_TRACK);
                continue;
            }
            talk.setColor(colorByTrack.get(track));
        }
    }

    private void synchroniseSpeakers(List<Speaker> speakers, Transaction transaction) {
        List<Speaker> speakersToDelete = Query.many(Speaker.class, "SELECT * FROM Speakers").get().asList();
        ModelList<Speaker> speakersToSave = new ModelList<>();

        for (Speaker speaker : speakers) {
            String firstName = speaker.getFirstName();
            speaker.setFirstName(firstName.substring(0, 1).toUpperCase() + firstName.substring(1));
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
