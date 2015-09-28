package fr.xebia.xebicon.ui.settings;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

import fr.xebia.xebicon.R;
import fr.xebia.xebicon.core.activity.BaseActivity;

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

}
