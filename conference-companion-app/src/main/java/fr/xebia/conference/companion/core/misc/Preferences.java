package fr.xebia.conference.companion.core.misc;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private static final String APP_PREFS = "ApplicationPreferences";
    private static final String CURRENT_CONFERENCE = "SynchroOver";
    private static final String CURRENT_CONFERENCE_DEVOXX = "DevoxxConference";
    private static final String NFC_TAG = "NfcTag";
    private static final String BLE_HINT_DISPLAYED = "bleHintDisplayed";
    private static final String CURRENT_CONFERENCE_END_TIME = "CurrentConferenceEndTime";

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
        return getAppPreferences(context).getInt(CURRENT_CONFERENCE_END_TIME, -1);
    }

    public static void setSelectedConferenceEndTime(Context context, long conferenceEndTime) {
        getAppPreferences(context).edit().putLong(CURRENT_CONFERENCE_END_TIME, conferenceEndTime).apply();
    }

    public static SharedPreferences getAppPreferences(Context context) {
        return context.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isTagRegistered(Context context) {
        return getAppPreferences(context).getString(NFC_TAG, null) != null;
    }

    public static void saveDevoxxianTag(Context context, String currentNfcId) {
        getAppPreferences(context).edit().putString(NFC_TAG, currentNfcId).apply();
    }

    public static String getNfcId(Context context) {
        return getAppPreferences(context).getString(NFC_TAG, "");
    }

    public static boolean isBleHintDisplayed(Context context) {
        return getAppPreferences(context).getBoolean(BLE_HINT_DISPLAYED, false);
    }

    public static void setBleHintDisplayed(Context context, boolean displayed) {
        getAppPreferences(context).edit().putBoolean(BLE_HINT_DISPLAYED, displayed).apply();
    }

    public static boolean isCurrentConferenceDevoxx(Context context) {
        return getAppPreferences(context).getBoolean(CURRENT_CONFERENCE_DEVOXX, false);
    }

    public static void setCurrentConferenceDevoxx(Context context, boolean currentConferenceDevoxx) {
        getAppPreferences(context).edit().putBoolean(CURRENT_CONFERENCE_DEVOXX, currentConferenceDevoxx).apply();
    }
}
