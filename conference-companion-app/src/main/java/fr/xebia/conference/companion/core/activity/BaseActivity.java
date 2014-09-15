package fr.xebia.conference.companion.core.activity;

import android.app.Activity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import fr.xebia.conference.companion.R;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends Activity {

    private static final int HEADER_HIDE_ANIM_DURATION = 300;

    private boolean mActionBarAutoHideEnabled = false;
    private int mActionBarAutoHideSensivity = 0;
    private int mActionBarAutoHideMinY = 0;
    private int mActionBarAutoHideSignal = 0;
    private boolean mActionBarShown = true;

    private List<View> mHideableHeaderViews = new ArrayList<>();

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
}
