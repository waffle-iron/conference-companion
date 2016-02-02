package fr.xebia.voxxeddays.zurich.ui.settings;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

import fr.xebia.voxxeddays.zurich.R;
import fr.xebia.voxxeddays.zurich.core.activity.NavigationActivity;

public class SettingsActivity extends NavigationActivity {

    public SettingsActivity() {
        super(R.layout.settings_activity);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new SettingsFragment(), SettingsFragment.TAG)
                    .commit();
        }
    }

    @Override
    protected int getNavId() {
        return R.id.nav_settings;
    }
}
