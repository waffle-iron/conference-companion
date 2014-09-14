package fr.xebia.conference.companion.ui.schedule;

import android.animation.LayoutTransition;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.core.misc.Preferences;
import fr.xebia.conference.companion.core.misc.RestoreActionBarFragment;
import fr.xebia.conference.companion.model.Talk;
import fr.xebia.conference.companion.ui.talk.TalkActivity;
import fr.xebia.conference.companion.model.Schedule;
import icepick.Icepick;
import icepick.Icicle;
import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.ManyQuery;
import se.emilsjolander.sprinkles.Query;

import java.util.ArrayList;
import java.util.List;

public class ScheduleFragment extends Fragment implements ManyQuery.ResultHandler<Talk>, ActionBar.OnNavigationListener,
        RestoreActionBarFragment {

    public static final String TAG = "ScheduleFragment";

    public static final String EXTRA_TRACK_NAME = "fr.xebia.devoxx.EXTRA_TRACK_NAME";
    public static final String EXTRA_FAVORITE_ONLY = "fr.xebia.devoxx.EXTRA_FAVORITE_ONLY";

    @InjectView(R.id.container) ViewGroup mContainer;
    @InjectView(R.id.empty_id) TextView mEmptyText;
    @InjectView(R.id.schedule_grid) GridView mGridView;

    private Schedule mSchedule;
    private ArrayAdapter<String> mSpinnerAdapter;

    @Icicle int mSelectedSpinnerPosition;
    @Icicle Parcelable mListViewState;

    @Icicle String mTrack;
    @Icicle boolean mFavoriteOnly;
    private List<Talk> mTalks = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.schedule_fragment, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        enableTransition();
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Talk talk = (Talk) parent.getAdapter().getItem(position);
                if (!talk.isBreak()) {
                    Intent intent = new Intent(getActivity(), TalkActivity.class);
                    intent.putExtra(TalkActivity.EXTRA_TALK_ID, talk.getId());
                    intent.putExtra(TalkActivity.EXTRA_TALK_TITLE, talk.getTitle());
                    intent.putExtra(TalkActivity.EXTRA_TALK_COLOR, talk.getColor());
                    startActivity(intent);
                }
            }
        });

        Bundle arguments = getArguments();
        mTrack = arguments == null ? null : arguments.getString(EXTRA_TRACK_NAME);
        mFavoriteOnly = arguments != null && arguments.getBoolean(EXTRA_FAVORITE_ONLY, false);
        int conferenceId = Preferences.getSelectedConference(getActivity());
        if (mFavoriteOnly) {
            Query.many(Talk.class, "SELECT * FROM Talks WHERE favorite=? AND conferenceId=? ORDER BY fromTime ASC", true, conferenceId)
                    .getAsync(getLoaderManager(), this);
        } else if (mTrack == null) {
            Query.many(Talk.class, "SELECT * FROM Talks WHERE conferenceId=? ORDER BY fromTime ASC",
                    conferenceId).getAsync(getLoaderManager(), this);
        } else {
            Query.many(Talk.class, "SELECT * FROM Talks WHERE track=? AND conferenceId=? ORDER BY fromTime ASC", mTrack,
                    conferenceId).getAsync(getLoaderManager(), this);
        }
    }

    private void enableTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            LayoutTransition layoutTransition = new LayoutTransition();
            layoutTransition.enableTransitionType(LayoutTransition.APPEARING);
            mContainer.setLayoutTransition(layoutTransition);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mListViewState = mGridView.onSaveInstanceState();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public boolean handleResult(CursorList<Talk> cursorList) {
        if (mTrack == null && !mFavoriteOnly) {
            mSchedule = new Schedule(cursorList == null ? new ArrayList<Talk>() : cursorList.asList());
        } else {
            mTalks.clear();
            mTalks.addAll(cursorList.asList());
        }

        if (getView() == null) {
            return true;
        }

        ActionBar actionBar = getActivity().getActionBar();
        if (((mTrack == null && !mFavoriteOnly) && (mSchedule == null || mSchedule.isEmpty()))
                || ((mTrack != null || mFavoriteOnly) && mTalks.isEmpty())) {
            mEmptyText.setText(mFavoriteOnly ? getString(R.string.no_favorite_talk) : getString(R.string.no_data));
            mGridView.setVisibility(View.GONE);
            mEmptyText.setVisibility(View.VISIBLE);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        } else {
            mEmptyText.setText("");
            mEmptyText.setVisibility(View.GONE);
            mGridView.setVisibility(View.VISIBLE);

            if (mFavoriteOnly) {
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(getString(R.string.my_favorites));
                mGridView.setAdapter(new ScheduleAdapter(getActivity(), R.layout.schedule_item_view, mTalks, true));
            } else if (mTrack == null) {
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                mSpinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item,
                        mSchedule.getFormattedDays());
                actionBar.setListNavigationCallbacks(mSpinnerAdapter, this);
                List<Talk> talksForDay = mSchedule.forDay(mSpinnerAdapter.getItem(mSelectedSpinnerPosition).toLowerCase());
                mGridView.setAdapter(new ScheduleAdapter(getActivity(), R.layout.schedule_item_view, talksForDay));
                actionBar.setSelectedNavigationItem(mSelectedSpinnerPosition);
            } else {
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(mTrack);
                mGridView.setAdapter(new ScheduleAdapter(getActivity(), R.layout.schedule_item_view, mTalks, true));
            }

            if (mListViewState != null) {
                mGridView.onRestoreInstanceState(mListViewState);
            }
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        if (mSelectedSpinnerPosition == itemPosition && mGridView.getAdapter() != null) {
            return true;
        }

        mSelectedSpinnerPosition = itemPosition;
        List<Talk> talksForDay = mSchedule.forDay(mSpinnerAdapter.getItem(itemPosition).toLowerCase());
        mGridView.setAdapter(new ScheduleAdapter(getActivity(), R.layout.schedule_item_view, talksForDay, mTrack != null || mFavoriteOnly));
        return true;
    }

    @Override
    public void restoreActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        if (mFavoriteOnly) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setTitle(getString(R.string.my_favorites));
        } else if (mTrack == null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            if (mSpinnerAdapter != null) {
                actionBar.setListNavigationCallbacks(mSpinnerAdapter, this);
                actionBar.setSelectedNavigationItem(mSelectedSpinnerPosition);
            }
        } else {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setTitle(mTrack);
        }
    }

    @Override
    public void onDestroyView() {
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    public static Fragment newInstanceForTrack(String track) {
        Fragment fragment = new ScheduleFragment();
        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_TRACK_NAME, track);
        fragment.setArguments(arguments);
        return fragment;
    }

    public static Fragment newInstanceForFavorites() {
        Fragment fragment = new ScheduleFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(EXTRA_FAVORITE_ONLY, true);
        fragment.setArguments(arguments);
        return fragment;
    }
}
