package fr.xebia.conference.companion.ui.settings;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.core.activity.BaseActivity;
import fr.xebia.conference.companion.ui.navigation.DrawerAdapter;

public class SettingsActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.settings);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new SettingsFragment(), SettingsFragment.TAG)
                    .commit();
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return DrawerAdapter.MENU_SETTINGS;
    }
}
