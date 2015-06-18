package fr.xebia.devoxx.pl.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.text.TextUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationFieldType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import fr.xebia.devoxx.pl.bus.SynchroFinishedEvent;
import fr.xebia.devoxx.pl.core.KouignAmanApplication;
import fr.xebia.devoxx.pl.core.misc.Preferences;
import fr.xebia.devoxx.pl.model.Conference;
import fr.xebia.devoxx.pl.model.Speaker;
import fr.xebia.devoxx.pl.model.SpeakerTalk;
import fr.xebia.devoxx.pl.model.Talk;
import fr.xebia.devoxx.pl.model.TrackColors;
import se.emilsjolander.sprinkles.ModelList;
import se.emilsjolander.sprinkles.Query;
import se.emilsjolander.sprinkles.Transaction;
import timber.log.Timber;

import static fr.xebia.devoxx.pl.core.KouignAmanApplication.BUS;

public class SynchroIntentService extends IntentService {

    private static final String TAG = "SynchroIntentService";

    public static final String EXTRA_CONFERENCE_ID = "fr.xebia.devoxx.pl.EXTRA_CONFERENCE_ID";
    public static final String EXTRA_FROM_APP_CREATE = "fr.xebia.devoxx.pl.EXTRA_FROM_APP_CREATE";

    public static final String DEVOXX_UK_CONFERENCE = "{\n" +
            "\"id\": 16,\n" +
            "\"backgroundUrl\": \"http://blog.xebia.fr/images/devoxxuk-2015-background.png\",\n" +
            "\"logoUrl\": \"http://blog.xebia.fr/images/devoxxuk-2015-logo.png\",\n" +
            "\"iconUrl\": \"http://blog.xebia.fr/images/devoxxuk-2015-icon.png\",\n" +
            "\"from\": \"2015-06-17\",\n" +
            "\"name\": \"DevoxxUK 2015\",\n" +
            "\"description\": \"DevoxxUK 2015\",\n" +
            "\"location\": \"London - Business Design Centre\",\n" +
            "\"baseUrl\": \"http://cfp.devoxx.co.uk/api/conferences/DevoxxUK2015\",\n" +
            "\"timezone\": \"Europe/London\",\n" +
            "\"enabled\": true,\n" +
            "\"to\": \"2015-06-19\"\n" +
            "}";

    public SynchroIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int conferenceId = intent.getIntExtra(EXTRA_CONFERENCE_ID, -1);
        Conference conference;
        try {
            conference = new ObjectMapper().readValue(DEVOXX_UK_CONFERENCE, Conference.class);
            DateTimeZone utcTimeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC"));
            DateTimeZone apiTimeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/London"));
            DateTime jodaStartTime = new DateTime(conference.getFrom(), apiTimeZone);
            DateTime jodaEndTime = new DateTime(conference.getTo(), apiTimeZone);
            conference.setFromUtcTime(jodaStartTime.withZone(utcTimeZone).getMillis());
            conference.setToUtcTime(jodaEndTime.withFieldAdded(DurationFieldType.days(), 1).withZone(utcTimeZone).getMillis());
        } catch (IOException e) {
            BUS.post(new SynchroFinishedEvent(false, null));
            return;
        }
        Transaction transaction = null;
        boolean sendSynchroEvent = !intent.hasExtra(EXTRA_FROM_APP_CREATE);
        try {
            if (conferenceId == -1) {
                BUS.post(new SynchroFinishedEvent(false, null));
            } else {
                // Load data before starting transaction
                List<Speaker> speakers = KouignAmanApplication.getConferenceApi().getSpeakers(conferenceId);
                List<Talk> scheduledTalks = KouignAmanApplication.getConferenceApi().getSchedule(conferenceId);
                transaction = new Transaction();
                synchroniseSpeakers(speakers, transaction);
                synchroniseTalks(conference, scheduledTalks, transaction);
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

    private void synchroniseTalks(Conference conference, List<Talk> scheduledTalks, Transaction transaction) {
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

            talkToSave.setTalkDetailsId(talkToSave.getId());

            if (talkToSave.isKeynote()) {
                talkToSave.setTrack("Keynote");
            }

            talkToSave.setPrettySpeakers(talkToSave.getSpeakers(), everySpeakers);

            setConferenceUtcTime(conference, talkToSave);

            talkToSave.setPosition(index++);

            talksToSave.add(talkToSave);
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
            if (!TextUtils.isEmpty(track) && !availableTracks.contains(track)) {
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
