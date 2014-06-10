package fr.xebia.conference.companion.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.service.BluetoothLocationIntentService;
import timber.log.Timber;

public class SettingsFragment extends PreferenceFragment {

    public static final String TAG = "SettingsFragment";

    public static final String KEY_PREF_BLE_LOCATION = "bluetooth_le_location_key";
    public static final String KEY_VERSION = "version_key";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        addPreferencesFromResource(R.xml.settings);
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            Preference versionPref = findPreference(KEY_VERSION);
            versionPref.setSummary(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Timber.d(e, "Couldn't get package name");
        }

        findPreference(KEY_PREF_BLE_LOCATION).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                    activity.startService(new Intent(activity, BluetoothLocationIntentService.class));
                return true;
            }
        });
    }
}
