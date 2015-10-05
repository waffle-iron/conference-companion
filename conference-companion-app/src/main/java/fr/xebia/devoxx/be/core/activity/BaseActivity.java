package fr.xebia.devoxx.be.core.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import fr.xebia.devoxx.be.R;
import fr.xebia.devoxx.be.core.misc.Preferences;
import fr.xebia.devoxx.be.core.utils.Compatibility;
import fr.xebia.devoxx.be.ui.HomeActivity;
import fr.xebia.devoxx.be.ui.conference.ConferenceChooserActivity;
import fr.xebia.devoxx.be.ui.schedule.MyScheduleActivity;
import fr.xebia.devoxx.be.ui.settings.SettingsActivity;
import fr.xebia.devoxx.be.ui.speaker.SpeakerActivity;
import fr.xebia.devoxx.be.ui.timeline.TimelineActivity;
import timber.log.Timber;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String HOME_FRAG_TAG = "HOME";
    private static final String NAV_ITEM_ID = "NAV_ITEM_ID";

    @InjectView(R.id.main_content) @Optional View mMainContent;
    @InjectView(R.id.nav_view) @Optional NavigationView navigationView;
    @InjectView(R.id.drawer_layout) @Optional DrawerLayout mDrawerLayout;

    private static final int HEADER_HIDE_ANIM_DURATION = 300;
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;
    protected boolean mDontCheckConference = false;
    private boolean mActionBarAutoHideEnabled = false;
    private int mActionBarAutoHideSensivity = 0;
    private int mActionBarAutoHideMinY = 0;
    private int mActionBarAutoHideSignal = 0;
    private boolean mActionBarShown = true;
    private OnActionBarAutoShowOrHideListener mActionBarAutoShowOrHideListener;
    private List<View> mHideableHeaderViews = new ArrayList<>();

    private Handler handler = new Handler();
    private boolean mMainContentScrolling;

    protected int currentNavId;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectTheme();
    }

    protected void selectTheme() {
        setTheme(R.style.AppTheme);
        if (Compatibility.isCompatible(Build.VERSION_CODES.LOLLIPOP)) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.default_theme_primary_dark));
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        ButterKnife.inject(this);

        if (navigationView != null){
            navigationView.getMenu().findItem(currentNavId).setCheckable(true);
            navigationView.getMenu().findItem(currentNavId).setChecked(true);
            navigationView.setNavigationItemSelectedListener(this);

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);

                    if (mActionBarAutoHideEnabled) {
                        autoShowOrHideActionBar(true);
                    }
                }
            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);

            mDrawerToggle.syncState();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        boolean hasSelectedConference = Preferences.hasSelectedConference(this);
        if (!hasSelectedConference && !mDontCheckConference) {
            startActivity(new Intent(this, ConferenceChooserActivity.class));
            finish();
        } else {
            if (mMainContent != null) {
                mMainContent.setAlpha(0);
                mMainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION).setStartDelay(200);
            } else {
                Timber.w("No view with ID main_content to fade in.");
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
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
                mMainContentScrolling = scrollState != SCROLL_STATE_IDLE;
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

        if (deltaY == 0) {
            return;
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
            getSupportActionBar().show();
        } else {
            getSupportActionBar().hide();
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

    public boolean isMainContentScrolling() {
        return mMainContentScrolling;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        if (currentNavId == menuItem.getItemId()) {
            return false;
        }

        switch (menuItem.getItemId()){
            case R.id.nav_myschedule:
                goTo(new Intent(BaseActivity.this, MyScheduleActivity.class));
                break;

            case R.id.nav_talks:
                goTo(new Intent(BaseActivity.this, HomeActivity.class));
                break;

            case R.id.nav_speakers:
                goTo(new Intent(BaseActivity.this, SpeakerActivity.class));
                break;

            case R.id.nav_timeline:
                goTo(new Intent(BaseActivity.this, TimelineActivity.class));
                break;

            case R.id.nav_settings:
                goTo(new Intent(BaseActivity.this, SettingsActivity.class));
                break;

        }

        mDrawerLayout.closeDrawers();

        // fade out the main content
        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
        }

        return true;
    }

    private void goTo(final Intent intent) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
                finish();
            }
        }, 300);
    }

    public interface OnActionBarAutoShowOrHideListener {
        void onActionBarAutoShowOrHide(boolean shown);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (navigationView != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
