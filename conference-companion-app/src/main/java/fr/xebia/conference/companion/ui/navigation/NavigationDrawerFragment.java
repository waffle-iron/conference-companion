package fr.xebia.conference.companion.ui.navigation;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.core.misc.Log;
import fr.xebia.conference.companion.core.misc.Preferences;
import fr.xebia.conference.companion.model.Conference;
import icepick.Icepick;
import icepick.Icicle;
import se.emilsjolander.sprinkles.OneQuery;
import se.emilsjolander.sprinkles.Query;

import static android.view.LayoutInflater.from;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment implements OneQuery.ResultHandler<Conference> {

    private static final String TAG = "NavigationDrawerFragment";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private DrawerAdapter mAdapter;

    @Icicle int mLastSelection = 2;
    private boolean mDrawerOpen;
    private TextView mHeader;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDrawerListView = (ListView) inflater.inflate(R.layout.navigation_drawer_fragment, container, false);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        addHeader(getActivity());
        mAdapter = new DrawerAdapter(getActivity());
        mDrawerListView.setAdapter(mAdapter);
        Query.one(Conference.class, "SELECT * FROM Conferences WHERE _id=?", Preferences.getSelectedConference(getActivity()))
                .getAsync(getLoaderManager(), this);
        return mDrawerListView;
    }

    private void addHeader(Context context) {
        mHeader = (TextView) from(context).inflate(R.layout.navigation_header, mDrawerListView, false);
        mDrawerListView.addHeaderView(mHeader);
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Toggle navigation side bar.
     */
    public void toggle() {
        if (mDrawerLayout != null) {
            if (isDrawerOpen()) {
                mDrawerLayout.closeDrawer(mFragmentContainerView);
            } else {
                mDrawerLayout.openDrawer(mFragmentContainerView);
            }
        }
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new NavigationDrawerToggle(
                getActivity(),
                mDrawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null && position != mLastSelection && position > 0) {
            mCallbacks.onNavigationDrawerItemSelected(position - mDrawerListView.getHeaderViewsCount());
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            Log.e(TAG, e);
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        hideMenuItems(menu, !mDrawerOpen);
        super.onPrepareOptionsMenu(menu);
    }

    private void hideMenuItems(Menu menu, boolean visible) {

        for (int i = 0; i < menu.size(); i++) {

            menu.getItem(i).setVisible(visible);

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getSupportActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public boolean handleResult(Conference conference) {
        if (conference != null && mHeader != null) {
            mHeader.setText(conference.getName());
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        mDrawerListView.setItemChecked(mLastSelection, true);
    }

    public void setSelection(int selection) {
        mLastSelection = selection + 1; // Don't forget the header offset
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(mLastSelection, true);
        }
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);

        void onNavigationDrawerToggle(boolean opened);
    }

    final class NavigationDrawerToggle extends ActionBarDrawerToggle {

        public NavigationDrawerToggle(Activity activity, DrawerLayout drawerLayout, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);
            if (!isAdded()) {
                return;
            }
            mDrawerOpen = false;
            getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            if (mCallbacks != null) {
                mCallbacks.onNavigationDrawerToggle(mDrawerOpen);
            }
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            if (!isAdded()) {
                return;
            }
            mDrawerOpen = true;
            getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            if (mCallbacks != null) {
                mCallbacks.onNavigationDrawerToggle(mDrawerOpen);
            }
        }


    }
}
