package fr.xebia.devoxx.uk.ui.browse;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.devoxx.uk.R;
import fr.xebia.devoxx.uk.core.activity.BaseActivity;
import fr.xebia.devoxx.uk.core.misc.Preferences;
import fr.xebia.devoxx.uk.core.utils.SqlUtils;
import fr.xebia.devoxx.uk.model.Talk;
import fr.xebia.devoxx.uk.ui.widget.CollectionView;
import fr.xebia.devoxx.uk.ui.widget.UIUtils;
import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.ManyQuery;
import se.emilsjolander.sprinkles.Query;

import static com.fluent.android.bundle.FluentBundle.newFluentBundle;
import static com.fluent.android.bundle.FragmentArgsSetter.setFragmentArguments;

public class BrowseTalksFragment extends Fragment implements ManyQuery.ResultHandler<Talk> {

    public static final String TAG = "BrowseTalksFragment";
    private static final int DEFAULT_GROUP_ID = 0;
    private static final int PAST_GROUP_ID = 1;

    public static String ARG_AVAILABLE_TALKS = "fr.xebia.devoxx.uk.ARG_AVAILABLE_TALKS";

    @InjectView(R.id.empty_id) TextView mEmptyText;
    @InjectView(R.id.talks_grid) CollectionView mTalksGrid;

    private List<Talk> mTalks;
    private boolean mLandscapeMode;
    private boolean mWideMode;
    private boolean mResumed;
    private boolean mPopulated;

    public static BrowseTalksFragment newInstance(ArrayList<String> availableTalks) {
        return setFragmentArguments(new BrowseTalksFragment(), newFluentBundle().put(ARG_AVAILABLE_TALKS, availableTalks));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int conferenceId = Preferences.getSelectedConference(getActivity());
        mLandscapeMode = getResources().getBoolean(R.bool.landscape);
        mWideMode = getResources().getBoolean(R.bool.wide_mode);
        List<String> availableTalksIds = getArguments().getStringArrayList(ARG_AVAILABLE_TALKS);
        Query.many(Talk.class, "SELECT * FROM Talks WHERE conferenceId=? AND _id IN (" +
                SqlUtils.toSqlArray(availableTalksIds) + ") ORDER BY fromTime ASC, toTime ASC, _id ASC", conferenceId).getAsync(getLoaderManager(), this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.browse_talks_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        mTalksGrid.setContentTopClearance(UIUtils.calculateActionBarSize(getActivity()) +
                getResources().getDimensionPixelSize(R.dimen.explore_grid_padding));
        ((BaseActivity) getActivity()).enableActionBarAutoHide(mTalksGrid);
    }

    @Override
    public void onResume() {
        super.onResume();
        mResumed = true;
        if (mTalks != null && !mPopulated) {
            populateTalksGrid();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mResumed = false;
    }

    @Override
    public void onDestroyView() {
        mTalksGrid.setOnScrollListener(null);
        super.onDestroyView();
    }

    @Override
    public boolean handleResult(CursorList<Talk> cursorList) {
        mTalks = cursorList.asList();

        if (getView() != null) {
            populateTalksGrid();
        }

        return true;
    }

    private void populateTalksGrid() {
        if (mTalks.isEmpty()) {
            mEmptyText.setVisibility(View.VISIBLE);
            mTalksGrid.setVisibility(View.GONE);
        } else {
            mTalksGrid.setVisibility(View.VISIBLE);
            mEmptyText.setVisibility(View.GONE);

            mTalksGrid.setCollectionAdapter(new BrowseTalksAdapter(getActivity(), R.layout.talk_item_view, mTalks));

            long selectedConferenceEndTime = Preferences.getSelectedConferenceEndTime(getActivity());
            List<Talk> futureTalks = new ArrayList<>();
            List<Talk> pastTalks = new ArrayList<>();
            long currentTime = System.currentTimeMillis();
            for (Talk talk : mTalks) {
                if (currentTime > talk.getToUtcTime()&& !(currentTime > selectedConferenceEndTime)) {
                    pastTalks.add(talk);
                } else {
                    futureTalks.add(talk);
                }
            }

            int numColumns = getNumColumns();
            CollectionView.Inventory inventory = new CollectionView.Inventory();

            CollectionView.InventoryGroup futureTalksGroup = new CollectionView.InventoryGroup(DEFAULT_GROUP_ID)
                    .setDisplayCols(numColumns)
                    .setShowHeader(false)
                    .setHeaderLabel("")
                    .setItemCount(futureTalks.size());
            inventory.addGroup(futureTalksGroup);

            if (pastTalks.size() > 0) {
                CollectionView.InventoryGroup pastTalksGroup = new CollectionView.InventoryGroup(PAST_GROUP_ID)
                        .setDisplayCols(numColumns)
                        .setShowHeader(true)
                        .setHeaderLabel(getString(R.string.talks_ended))
                        .setOffset(futureTalks.size())
                        .setItemCount(pastTalks.size());
                inventory.addGroup(pastTalksGroup);
            }

            mTalksGrid.updateInventory(inventory, mResumed);
        }
        mPopulated = true;
    }

    private int getNumColumns() {
        int numColumns = mLandscapeMode ? 2 : 1;
        if (mWideMode) {
            numColumns++;
        }
        return numColumns;
    }
}
