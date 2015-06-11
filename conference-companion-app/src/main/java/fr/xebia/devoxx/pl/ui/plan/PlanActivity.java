package fr.xebia.devoxx.pl.ui.plan;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;

import com.viewpagerindicator.CirclePageIndicator;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.devoxx.pl.R;
import fr.xebia.devoxx.pl.core.activity.BaseActivity;
import fr.xebia.devoxx.pl.ui.navigation.DrawerAdapter;

public class PlanActivity extends BaseActivity {

    @InjectView(R.id.view_pager) ViewPager viewPager;
    @InjectView(R.id.view_pager_indicator) CirclePageIndicator circlePageIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plan_activity);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.floor_plan);

        ButterKnife.inject(this);

        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return PlanFragment.newInstance(position == 0 ?
                        "http://www.devoxx.co.uk/wp-content/uploads/2015/04/Day-1-flooplan.jpg" :
                        "http://www.devoxx.co.uk/wp-content/uploads/2015/04/Days-23-Floorplan.jpg"
                );
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
        viewPager.setAdapter(adapter);
        circlePageIndicator.setViewPager(viewPager);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return DrawerAdapter.MENU_FLOOR_PLAN;
    }

    @Override
    public void onNavigationDrawerToggle(boolean opened) {
        super.onNavigationDrawerToggle(opened);
        if (!opened) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.floor_plan);
        }
    }
}
