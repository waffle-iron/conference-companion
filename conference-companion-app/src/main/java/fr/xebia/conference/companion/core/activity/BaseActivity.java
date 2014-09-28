package fr.xebia.conference.companion.core.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.core.misc.Preferences;
import fr.xebia.conference.companion.ui.HomeActivity;
import fr.xebia.conference.companion.ui.conference.ConferenceChooserActivity;
import fr.xebia.conference.companion.ui.navigation.DrawerAdapter;
import fr.xebia.conference.companion.ui.navigation.NavigationDrawerFragment;
import fr.xebia.conference.companion.ui.schedule.MyScheduleActivity;
import fr.xebia.conference.companion.ui.settings.SettingsActivity;
import fr.xebia.conference.companion.ui.speaker.SpeakerActivity;
import timber.log.Timber;

public class BaseActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final String HOME_FRAG_TAG = "HOME";
    private static final int HEADER_HIDE_ANIM_DURATION = 300;
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;
    protected NavigationDrawerFragment mNavigationDrawerFragment;
    protected boolean mDontCheckConference = false;
    @InjectView(R.id.main_content) @Optional View mMainContent;
    private boolean mActionBarAutoHideEnabled = false;
    private int mActionBarAutoHideSensivity = 0;
    private int mActionBarAutoHideMinY = 0;
    private int mActionBarAutoHideSignal = 0;
    private boolean mActionBarShown = true;
    private OnActionBarAutoShowOrHideListener mActionBarAutoShowOrHideListener;
    private List<View> mHideableHeaderViews = new ArrayList<>();

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectTheme();
    }

    protected void selectTheme() {
        boolean hasSelectedConference = Preferences.hasSelectedConference(this);
        boolean devoxxConf = Preferences.isCurrentConferenceDevoxx(this);
        setTheme(hasSelectedConference && devoxxConf ? R.style.Theme_Devoxx : R.style.AppTheme);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ButterKnife.inject(this);
        boolean hasSelectedConference = Preferences.hasSelectedConference(this);
        if (!hasSelectedConference && !mDontCheckConference) {
            startActivity(new Intent(this, ConferenceChooserActivity.class));
            finish();
        } else {
            mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);

            if (mNavigationDrawerFragment != null) {
                // Set up the drawer.
                mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
                mNavigationDrawerFragment.setSelection(getSelfNavDrawerItem());
            }

            if (mMainContent != null) {
                mMainContent.setAlpha(0);
                mMainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION).setStartDelay(200);
            } else {
                Timber.w("No view with ID main_content to fade in.");
            }
        }
    }

    /**
     * Returns the navigation drawer item that corresponds to this Activity. Subclasses
     * of BaseActivity override this to indicate what nav drawer item corresponds to them
     * Return NAVDRAWER_ITEM_INVALID to mean that this Activity should not have a Nav Drawer.
     */
    protected int getSelfNavDrawerItem() {
        return DrawerAdapter.MENU_INVALID;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Preferences.isBleHintDisplayed(this) && !mDontCheckConference) {
            Preferences.setBleHintDisplayed(this, true);
            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.informations)
                    .setMessage(R.string.informations_ble)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setCancelable(false).create();
            dialog.show();
        }

        if (mMainContent != null && mMainContent.getAlpha() == 0) {
            mMainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        }
    }

    public void enableActionBarAutoHide(final AbsListView listView) {
        initActionBarAutoHide();
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            final static int ITEMS_THRESHOLD = 3;
            int lastFvi = 0;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                onMainContentScrolled(firstVisibleItem <= ITEMS_THRESHOLD ? 0 : Integer.MAX_VALUE,
                        lastFvi - firstVisibleItem > 0 ? Integer.MIN_VALUE :
                                lastFvi == firstVisibleItem ? 0 : Integer.MAX_VALUE
                );
                lastFvi = firstVisibleItem;
            }
        });
    }

    /**
     * Initializes the Action Bar auto-hide (aka Quick Recall) effect.
     */
    private void initActionBarAutoHide() {
        mActionBarAutoHideEnabled = true;
        mActionBarAutoHideMinY = getResources().getDimensionPixelSize(R.dimen.action_bar_auto_hide_min_y);
        mActionBarAutoHideSensivity = getResources().getDimensionPixelSize(R.dimen.action_bar_auto_hide_sensivity);
    }

    /**
     * Indicates that the main content has scrolled (for the purposes of showing/hiding
     * the action bar for the "action bar auto hide" effect). currentY and deltaY may be exact
     * (if the underlying view supports it) or may be approximate indications:
     * deltaY may be INT_MAX to mean "scrolled forward indeterminately" and INT_MIN to mean
     * "scrolled backward indeterminately".  currentY may be 0 to mean "somewhere close to the
     * start of the list" and INT_MAX to mean "we don't know, but not at the start of the list"
     */
    private void onMainContentScrolled(int currentY, int deltaY) {
        if (deltaY > mActionBarAutoHideSensivity) {
            deltaY = mActionBarAutoHideSensivity;
        } else if (deltaY < -mActionBarAutoHideSensivity) {
            deltaY = -mActionBarAutoHideSensivity;
        }

        if (Math.signum(deltaY) * Math.signum(mActionBarAutoHideSignal) < 0) {
            // deltaY is a motion opposite to the accumulated signal, so reset signal
            mActionBarAutoHideSignal = deltaY;
        } else {
            // add to accumulated signal
            mActionBarAutoHideSignal += deltaY;
        }

        boolean shouldShow = currentY < mActionBarAutoHideMinY ||
                (mActionBarAutoHideSignal <= -mActionBarAutoHideSensivity);
        autoShowOrHideActionBar(shouldShow);
    }

    protected void autoShowOrHideActionBar(boolean show) {
        if (show == mActionBarShown) {
            return;
        }

        mActionBarShown = show;
        if (mActionBarShown) {
            getActionBar().show();
        } else {
            getActionBar().hide();
        }
        onActionBarAutoShowOrHide(show);
    }

    protected void onActionBarAutoShowOrHide(boolean shown) {
        for (View view : mHideableHeaderViews) {
            if (shown) {
                view.animate()
                        .translationY(0)
                        .alpha(1)
                        .setDuration(HEADER_HIDE_ANIM_DURATION)
                        .setInterpolator(new DecelerateInterpolator());
            } else {
                view.animate()
                        .translationY(-view.getBottom())
                        .alpha(0)
                        .setDuration(HEADER_HIDE_ANIM_DURATION)
                        .setInterpolator(new DecelerateInterpolator());
            }
        }
        if (mActionBarAutoShowOrHideListener != null) {
            mActionBarAutoShowOrHideListener.onActionBarAutoShowOrHide(shown);
        }
    }

    public void setActionBarAutoShowOrHideListener(OnActionBarAutoShowOrHideListener listener) {
        mActionBarAutoShowOrHideListener = listener;
    }

    public void registerHideableHeaderView(View hideableHeaderView) {
        if (!mHideableHeaderViews.contains(hideableHeaderView)) {
            mHideableHeaderViews.add(hideableHeaderView);
        }
    }

    public void deregisterHideableHeaderView(View hideableHeaderView) {
        if (mHideableHeaderViews.contains(hideableHeaderView)) {
            mHideableHeaderViews.remove(hideableHeaderView);
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if (getSelfNavDrawerItem() == position) {
            return;
        }

        switch (position) {
            case DrawerAdapter.MENU_MY_AGENDA:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(BaseActivity.this, MyScheduleActivity.class));
                        finish();
                    }
                }, 300);
                break;

            case DrawerAdapter.MENU_TALKS:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(BaseActivity.this, HomeActivity.class));
                        finish();
                    }
                }, 300);
                break;
            case DrawerAdapter.MENU_SPEAKERS:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(BaseActivity.this, SpeakerActivity.class));
                        finish();
                    }
                }, 300);
                break;
            case DrawerAdapter.MENU_CONFERENCES:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(BaseActivity.this, ConferenceChooserActivity.class));
                    }
                }, 300);
                break;
            case DrawerAdapter.MENU_SETTINGS:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(BaseActivity.this, SettingsActivity.class));
                        finish();
                    }
                }, 300);
                break;
        }

        // fade out the main content
        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
        }
    }

    @Override
    public void onNavigationDrawerToggle(boolean opened) {
        if (mActionBarAutoHideEnabled && opened) {
            autoShowOrHideActionBar(true);
        }
    }

    public interface OnActionBarAutoShowOrHideListener {
        void onActionBarAutoShowOrHide(boolean shown);
    }
}
