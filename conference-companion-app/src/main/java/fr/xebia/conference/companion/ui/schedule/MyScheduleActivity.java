package fr.xebia.conference.companion.ui.schedule;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import butterknife.InjectView;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.core.activity.BaseActivity;
import fr.xebia.conference.companion.core.misc.Preferences;
import fr.xebia.conference.companion.model.MySchedule;
import fr.xebia.conference.companion.model.Schedule;
import fr.xebia.conference.companion.model.Talk;
import fr.xebia.conference.companion.ui.navigation.DrawerAdapter;
import fr.xebia.conference.companion.ui.widget.DrawShadowFrameLayout;
import fr.xebia.conference.companion.ui.widget.UIUtils;
import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.ManyQuery;
import se.emilsjolander.sprinkles.Query;

import java.util.ArrayList;

public class MyScheduleActivity extends BaseActivity implements ManyQuery.ResultHandler<Talk> {

    @InjectView(R.id.main_content) DrawShadowFrameLayout mDrawShadowFrameLayout;
    @InjectView(R.id.pager_strip) PagerTabStrip mPagerStrip;
    @InjectView(R.id.view_pager) ViewPager mViewPager;

    private MySchedulePagerAdapter mAdapter;
    private MySchedule mMySchedule;
    private boolean mStopped;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_schedule_activity);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        int conferenceId = Preferences.getSelectedConference(this);
        String query = "SELECT * FROM Talks WHERE conferenceId=? ORDER BY fromTime ASC";
        Query.many(Talk.class, query, conferenceId).getAsync(getLoaderManager(), this);
        mPagerStrip.setDrawFullUnderline(true);
        getActionBar().setTitle(R.string.my_schedule);
        mDrawShadowFrameLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mDrawShadowFrameLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int shadowTopOffset = UIUtils.calculateActionBarSize(MyScheduleActivity.this) + mPagerStrip.getHeight();
                mDrawShadowFrameLayout.setShadowTopOffset(shadowTopOffset);
            }
        });
    }

    @Override
    public void onNavigationDrawerToggle(boolean opened) {
        super.onNavigationDrawerToggle(opened);
        if (!opened) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setTitle(R.string.my_schedule);
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return DrawerAdapter.MENU_MY_AGENDA;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mStopped = false;
        if (mMySchedule != null) {
            setAdapter();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mStopped = true;
    }

    @Override
    public boolean handleResult(CursorList<Talk> cursorList) {
        mMySchedule = new MySchedule(new Schedule(cursorList == null ? new ArrayList<Talk>() : cursorList.asList()));
        if (mStopped) {
            return true;
        }
        setAdapter();
        return true;
    }

    private void setAdapter() {
        if (mAdapter == null) {
            mAdapter = new MySchedulePagerAdapter(mMySchedule, getFragmentManager());
            mViewPager.setAdapter(mAdapter);
        } else {
            mAdapter.setSchedule(mMySchedule);
        }
    }

    public static class MySchedulePagerAdapter extends FragmentPagerAdapter {

        private MySchedule mySchedule;

        public MySchedulePagerAdapter(MySchedule mySchedule, FragmentManager fm) {
            super(fm);
            this.mySchedule = mySchedule;
        }

        @Override
        public Fragment getItem(int position) {
            return MyScheduleFragment.newInstance();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            MyScheduleFragment myScheduleFragment = (MyScheduleFragment) super.instantiateItem(container, position);
            myScheduleFragment.setData(mySchedule.getConferenceDayAt(position));
            return myScheduleFragment;
        }

        @Override
        public int getCount() {
            return mySchedule.getConferenceDaysCount();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mySchedule.getConferenceDayAt(position).title;
        }

        public void setSchedule(MySchedule mySchedule) {
            this.mySchedule = mySchedule;
            notifyDataSetChanged();
        }
    }
}
