package fr.xebia.conference.companion.core.misc;

import android.content.Context;
import android.content.SharedPreferences;

import fr.xebia.conference.companion.model.Talk;

public class Preferences {

    private static final String APP_PREFS = "ApplicationPreferences";
    private static final String CURRENT_CONFERENCE = "SynchroOver";
    private static final String CURRENT_CONFERENCE_DEVOXX = "DevoxxConference";
    private static final String CURRENT_CONFERENCE_END_TIME = "CurrentConferenceEndTime";
    private static final String CURRENT_CONFERENCE_START_TIME = "CurrentConferenceStartTime";
    private static final String GENERATED_DEVICE_ID = "GeneratedDeviceId";

    private Preferences() {

    }

    public static int getSelectedConference(Context context) {
        return getAppPreferences(context).getInt(CURRENT_CONFERENCE, -1);
    }

    public static void setSelectedConference(Context context, int conferenceId) {
        getAppPreferences(context).edit().putInt(CURRENT_CONFERENCE, conferenceId).apply();
    }

    public static boolean hasSelectedConference(Context context) {
        return getAppPreferences(context).contains(CURRENT_CONFERENCE);
    }

    public static long getSelectedConferenceEndTime(Context context) {
        return getAppPreferences(context).getLong(CURRENT_CONFERENCE_END_TIME, -1);
    }

    public static void setSelectedConferenceEndTime(Context context, long conferenceEndTime) {
        getAppPreferences(context).edit().putLong(CURRENT_CONFERENCE_END_TIME, conferenceEndTime).apply();
    }

    public static long getSelectedConferenceStartTime(Context context) {
        return getAppPreferences(context).getLong(CURRENT_CONFERENCE_START_TIME, -1);
    }

    public static void setSelectedConferenceStartTime(Context context, long conferenceStartTime) {
        getAppPreferences(context).edit().putLong(CURRENT_CONFERENCE_START_TIME, conferenceStartTime).apply();
    }

    public static SharedPreferences getAppPreferences(Context context) {
        return context.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isDeviceIdGenerated(Context context) {
        return getAppPreferences(context).getString(GENERATED_DEVICE_ID, null) != null;
    }

    public static String getGeneratedDeviceId(Context context) {
        return getAppPreferences(context).getString(GENERATED_DEVICE_ID, null);
    }

    public static void saveGeneratedDeviceId(Context context, String generatedDeviceId) {
        getAppPreferences(context).edit().putString(GENERATED_DEVICE_ID, generatedDeviceId).apply();
    }

    public static boolean isCurrentConferenceDevoxx(Context context) {
        return getAppPreferences(context).getBoolean(CURRENT_CONFERENCE_DEVOXX, false);
    }

    public static void setCurrentConferenceDevoxx(Context context, boolean currentConferenceDevoxx) {
        getAppPreferences(context).edit().putBoolean(CURRENT_CONFERENCE_DEVOXX, currentConferenceDevoxx).apply();
    }

    public static boolean isTalkAlreadyNotified(Context context, Talk talk) {
        return getAppPreferences(context).getBoolean(String.format("notification_%d_%s", talk.getConferenceId(), talk.getId()), false);
    }

    public static void flagTalkAsNotified(Context context, Talk talk) {
        getAppPreferences(context).edit().putBoolean(String.format("notification_%d_%s", talk.getConferenceId(), talk.getId()), true).apply();
    }

    public static boolean isTalkFeedbackAlreadyNotified(Context context, Talk talk) {
        return getAppPreferences(context).getBoolean(String.format("notification_feedback_%d_%s", talk.getConferenceId(), talk.getId()), false);
    }

    public static void flagTalkFeedbackAsNotified(Context context, Talk talk) {
        getAppPreferences(context).edit().putBoolean(String.format("notification_feedback_%d_%s", talk.getConferenceId(), talk.getId()), true).apply();
    }
}
