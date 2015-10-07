package fr.xebia.xebicon.core.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.WindowManager;

import butterknife.ButterKnife;
import fr.xebia.xebicon.R;
import fr.xebia.xebicon.core.misc.Preferences;
import fr.xebia.xebicon.ui.conference.ConferenceChooserActivity;

public abstract class BaseActivity extends AppCompatActivity {

    public static final String HOME_FRAG_TAG = "HOME";

    protected boolean mDontCheckConference = false;

    private final int layoutId;

    public BaseActivity(int layoutId) {
        this.layoutId = layoutId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layoutId);

        ButterKnife.inject(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);



        boolean hasSelectedConference = Preferences.hasSelectedConference(this);
        if (!hasSelectedConference && !mDontCheckConference) {
            startActivity(new Intent(this, ConferenceChooserActivity.class));
            finish();
        }

    }

    protected void setupFloatingWindow() {
        // configure this Activity as a floating window, dimming the background
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = getResources().getDimensionPixelSize(R.dimen.talk_details_floating_width);
        params.height = getResources().getDimensionPixelSize(R.dimen.talk_details_floating_height);
        params.alpha = 1;
        params.dimAmount = 0.7f;
        params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        getWindow().setAttributes(params);
    }

    protected boolean shouldBeFloatingWindow() {
        Resources.Theme theme = getTheme();
        TypedValue floatingWindowFlag = new TypedValue();
        if (theme == null || !theme.resolveAttribute(R.attr.isFloatingWindow, floatingWindowFlag, true)) {
            // isFloatingWindow flag is not defined in theme
            return false;
        }
        return (floatingWindowFlag.data != 0);
    }

}
