package fr.xebia.conference.companion.core.misc;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private static final String APP_PREFS = "ApplicationPreferences";
    public static final String CURRENT_CONFERENCE = "SynchroOver";
    public static final String NFC_TAG = "NfcTag";
    public static final String BLE_HINT_DISPLAYED = "NfcTag";

    private Preferences() {

    }

    public static int getSelectedConference(Context context) {
        return getAppPreferences(context).getInt(CURRENT_CONFERENCE, -1);
    }

    public static boolean hasSelectedConference(Context context) {
        return getAppPreferences(context).contains(CURRENT_CONFERENCE);
    }

    public static void setSelectedConference(Context context, int conferenceId) {
        SharedPreferences.Editor editor = getAppPreferences(context).edit();
        editor.putInt(CURRENT_CONFERENCE, conferenceId);
        editor.commit();
    }

    public static SharedPreferences getAppPreferences(Context context) {
        return context.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isTagRegistered(Context context) {
        return getAppPreferences(context).getString(NFC_TAG, null) != null;
    }

    public static void saveDevoxxianTag(Context context, String currentNfcId) {
        SharedPreferences.Editor editor = getAppPreferences(context).edit();
        editor.putString(NFC_TAG, currentNfcId);
        editor.commit();
    }

    public static String getNfcId(Context context) {
        return getAppPreferences(context).getString(NFC_TAG, "");
    }

    public static boolean isBleHintDisplayed(Context context) {
        return getAppPreferences(context).getBoolean(BLE_HINT_DISPLAYED, false);
    }

    public static boolean setBleHintDisplayed(Context context, boolean displayed) {
        return getAppPreferences(context).edit().putBoolean(BLE_HINT_DISPLAYED, displayed).commit();
    }
}
