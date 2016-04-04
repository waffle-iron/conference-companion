package fr.xebia.voxxeddays.zurich.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.google.gson.JsonSyntaxException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationFieldType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import fr.xebia.voxxeddays.zurich.R;
import fr.xebia.voxxeddays.zurich.bus.SynchroFinishedEvent;
import fr.xebia.voxxeddays.zurich.core.KouignAmanApplication;
import fr.xebia.voxxeddays.zurich.core.misc.Preferences;
import fr.xebia.voxxeddays.zurich.model.Conference;
import fr.xebia.voxxeddays.zurich.model.Speaker;
import fr.xebia.voxxeddays.zurich.model.SpeakerTalk;
import fr.xebia.voxxeddays.zurich.model.Talk;
import fr.xebia.voxxeddays.zurich.model.TrackColors;
import se.emilsjolander.sprinkles.ModelList;
import se.emilsjolander.sprinkles.Query;
import se.emilsjolander.sprinkles.Transaction;
import timber.log.Timber;

import static fr.xebia.voxxeddays.zurich.core.KouignAmanApplication.BUS;
import static fr.xebia.voxxeddays.zurich.core.KouignAmanApplication.getGson;

public class SynchroIntentService extends IntentService {

    private static final String TAG = "SynchroIntentService";

    public static final String EXTRA_CONFERENCE_ID = "fr.xebia.voxxeddays.zurich.EXTRA_CONFERENCE_ID";
    public static final String EXTRA_FROM_APP_CREATE = "fr.xebia.voxxeddays.zurich.EXTRA_FROM_APP_CREATE";

    public enum Conferences {
        ZURICH(R.drawable.voxxeddays_zurich, "{\n" +
                "\"id\": 19,\n" +
                "\"backgroundUrl\": \"https://s3.amazonaws.com/civetta-works/works/xebia/conf-companion/doxxed-days-zurich-2016/Zurich-Photo1.jpg\",\n" +
                "\"logoUrl\": \"https://s3.amazonaws.com/civetta-works/works/xebia/conf-companion/doxxed-days-zurich-2016/iTunesArtwork.png\",\n" +
                "\"iconUrl\": \"http://blog.xebia.fr/images/devoxxuk-2015-icon.png\"," +
                "\"from\": \"2016-03-03 11:00\",\n" +
                "\"name\": \"Voxxed Days Zürich\",\n" +
                "\"description\": \"Voxxed Days Zürich\",\n" +
                "\"location\": \"Sihlcity Cinema\",\n" +
                "\"baseUrl\": \"https://cfp-vdz.exteso.com/api/conferences/VDZ16\",\n" +
                "\"timezone\": \"Europe/Zurich\",\n" +
                "\"enabled\": true,\n" +
                "\"to\": \"2016-03-03 11:00\"\n" +
                "}"),
        TICINO(R.drawable.voxxeddays_ticino, "{\n" +
                "\"id\": 20,\n" +
                "\"backgroundUrl\": \"https://voxxeddays.com/ticino16/wp-content/uploads/sites/14/2015/11/lago.jpg\",\n" +
                "\"logoUrl\": \"https://s3.amazonaws.com/civetta-works/works/xebia/conf-companion/doxxed-days-zurich-2016/iTunesArtwork.png\",\n" +
                "\"iconUrl\": \"http://blog.xebia.fr/images/devoxxuk-2015-icon.png\",\n" +
                "\"from\": \"2016-04-30 11:00\",\n" +
                "\"name\": \"Voxxed Days Ticino\",\n" +
                "\"description\": \"Voxxed Days Ticino\",\n" +
                "\"location\": \"Palazzo dei Congressi, Lugano\",\n" +
                "\"baseUrl\": \"https://cfp-vdt.exteso.com/api/conferences/VDT16\",\n" +
                "\"timezone\": \"Europe/Zurich\",\n" +
                "\"enabled\": true,\n" +
                "\"to\": \"2016-04-30 11:00\"\n" +
                "}");

        public final String infos;
        public int logoId;

        Conferences(int logoId, String infos) {
            this.logoId = logoId;
            this.infos = infos;
        }

        public static Conferences from(int id) {
            switch (id) {
                case 19:
                    return ZURICH;
                case 20:
                    return TICINO;

                default:
                    return null;
            }
        }
    }

    public SynchroIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int conferenceId = intent.getIntExtra(EXTRA_CONFERENCE_ID, -1);
        Conference conference;
        try {
            conference = getGson().fromJson(Conferences.from(conferenceId).infos, Conference.class);
            DateTimeZone utcTimeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC"));
            DateTimeZone apiTimeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Zurich"));
            DateTime jodaStartTime = new DateTime(conference.getFrom(), apiTimeZone);
            DateTime jodaEndTime = new DateTime(conference.getTo(), apiTimeZone);
            conference.setFromUtcTime(jodaStartTime.withZone(utcTimeZone).getMillis());
            conference.setToUtcTime(jodaEndTime.withFieldAdded(DurationFieldType.days(), 1).withZone(utcTimeZone).getMillis());
        } catch (Exception e) {
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
        } finally {
            if (transaction != null) {
                transaction.finish();
            }
        }

        scheduleSync(this, true);
    }

    public static void scheduleSync(Context context, boolean fromAppCreate) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        Intent service = new Intent(context, SynchroIntentService.class)
                .putExtra(SynchroIntentService.EXTRA_CONFERENCE_ID, Preferences.getSelectedConference(context))
                .putExtra(SynchroIntentService.EXTRA_FROM_APP_CREATE, fromAppCreate);

        PendingIntent pendingIntent = PendingIntent.getService(context, 0, service, 0);

        PendingIntent existingPendingIntent = PendingIntent.getService(context, 0, service, PendingIntent.FLAG_NO_CREATE);
        if (existingPendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }

        long intervalMillis = 3600 * 1000;
        alarmManager.setRepeating(
                AlarmManager.RTC,
                Calendar.getInstance().getTimeInMillis() + intervalMillis,
                intervalMillis,
                pendingIntent);
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
        DateTimeZone apiTimeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Zurich"));
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