package fr.xebia.conference.companion.ui.schedule;

import android.animation.LayoutTransition;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.bus.SyncEvent;
import fr.xebia.conference.companion.core.activity.BaseActivity;
import fr.xebia.conference.companion.core.misc.Preferences;
import fr.xebia.conference.companion.model.Schedule;
import fr.xebia.conference.companion.model.TagMetadata;
import fr.xebia.conference.companion.model.Tags;
import fr.xebia.conference.companion.model.Talk;
import fr.xebia.conference.companion.ui.widget.CollectionView;
import fr.xebia.conference.companion.ui.widget.DrawShadowFrameLayout;
import fr.xebia.conference.companion.ui.widget.UIUtils;
import icepick.Icepick;
import icepick.Icicle;
import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.ManyQuery;
import se.emilsjolander.sprinkles.Query;
import timber.log.Timber;

import static fr.xebia.conference.companion.core.KouignAmanApplication.BUS;

public class ScheduleFragment extends Fragment implements ManyQuery.ResultHandler<Talk>, BaseActivity.OnActionBarAutoShowOrHideListener {

    public static final String TAG = "ScheduleFragment";
    public static final int DEFAULT_GROUP_ID = 0;
    private static final int PAST_GROUP_ID = 1;

    @InjectView(R.id.container) DrawShadowFrameLayout mContainer;
    @InjectView(R.id.empty_id) TextView mEmptyText;
    @InjectView(R.id.schedule_grid) CollectionView mScheduleGrid;
    @InjectView(R.id.filters_box) ViewGroup mFiltersBox;
    @InjectView(R.id.secondary_filter_spinner_1) Spinner mSecondaryFilterSpinner1;
    @InjectView(R.id.secondary_filter_spinner_2) Spinner mSecondaryFilterSpinner2;
    // filter tags that are currently selected
    @Icicle String[] mFilterTags = {"", "", ""};
    // filter tags that we have to restore (as a result of Activity recreation)
    @Icicle String[] mFilterTagsToRestore = {null, null, null};

    private Schedule mSchedule;

    private Spinner mSpinner;
    private FilterScheduleSpinnerAdapter mFilterScheduleSpinnerAdapter;

    private TagMetadata mTagMetadata;

    private boolean mLandscapeMode;
    private boolean mWideMode;
    private int mScheduleGridPaddingTop;
    private boolean mResumed;
    private boolean mPopulated;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        BUS.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.schedule_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        mLandscapeMode = getResources().getBoolean(R.bool.landscape);
        mWideMode = getResources().getBoolean(R.bool.wide_mode);
        mScheduleGridPaddingTop = getResources().getDimensionPixelSize(R.dimen.explore_grid_padding);
        enableTransition();
        BaseActivity activity = (BaseActivity) getActivity();
        activity.enableActionBarAutoHide(mScheduleGrid);
        activity.registerHideableHeaderView(mFiltersBox);
        activity.setActionBarAutoShowOrHideListener(this);

        int conferenceId = Preferences.getSelectedConference(getActivity());
        Query.many(Talk.class, "SELECT * FROM Talks WHERE conferenceId=? ORDER BY fromTime ASC", conferenceId)
                .getAsync(getLoaderManager(), this);
        configureActionBarSpinner();
    }

    @Override
    public void onResume() {
        super.onResume();
        mResumed = true;
        if (mSchedule != null && !mPopulated) {
            populateScheduleGrid(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mResumed = false;
    }

    @Override
    public void onStop() {
        BUS.unregister(this);
        super.onStop();
    }

    private void configureActionBarSpinner() {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        View spinnerContainer = LayoutInflater.from(actionBar.getThemedContext()).inflate(R.layout.actionbar_spinner, null);
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        actionBar.setCustomView(spinnerContainer, lp);
        mFilterScheduleSpinnerAdapter = new FilterScheduleSpinnerAdapter(getActivity(), true);

        mSpinner = (Spinner) spinnerContainer.findViewById(R.id.actionbar_spinner);
        mSpinner.setAdapter(mFilterScheduleSpinnerAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long itemId) {
                onActionBarFilterSelected(mFilterScheduleSpinnerAdapter.getTag(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        computeActionBarSpinnerAdapter();
    }

    private void onActionBarFilterSelected(String tag) {
        if (tag.equals(mFilterTags[0])) {
            // nothing to do
            return;
        }

        mFilterTags[0] = tag;

        // Reset secondary filters
        for (int i = 1; i < mFilterTags.length; i++) {
            mFilterTags[i] = "";
        }
        showSecondaryFilters();
        populateScheduleGrid(true);
    }

    private void populateScheduleGrid(boolean reset) {
        computeActionBarSpinnerAdapter();

        boolean filtering = !"".equals(mFilterTags[0]);
        int itemLayout = filtering ? R.layout.talk_item_view : R.layout.schedule_item_view;

        List<Talk> filteredTalks = mSchedule.getFilteredTalks(mTagMetadata.getTag(mFilterTags[0]),
                mTagMetadata.getTag(mFilterTags[1]),
                mTagMetadata.getTag(mFilterTags[2]));

        if (filteredTalks.isEmpty()) {
            mEmptyText.setVisibility(View.VISIBLE);
            mScheduleGrid.setVisibility(View.GONE);
        } else {
            mScheduleGrid.setVisibility(View.VISIBLE);
            mEmptyText.setVisibility(View.GONE);

            Parcelable scheduleGridState = null;
            if (!reset) {
                scheduleGridState = mScheduleGrid.onSaveInstanceState();
            }

            long selectedConferenceEndTime = Preferences.getSelectedConferenceEndTime(getActivity());
            List<Talk> futureTalks = new ArrayList<>();
            final List<Talk> pastTalks = new ArrayList<>();
            long currentTime = System.currentTimeMillis();
            for (Talk talk : filteredTalks) {
                if (currentTime >= talk.getToUtcTime() && !(currentTime > selectedConferenceEndTime)) {
                    pastTalks.add(talk);
                } else {
                    futureTalks.add(talk);
                }
            }

            int numColumns = getNumColumns(filtering);
            CollectionView.Inventory inventory = new CollectionView.Inventory();

            CollectionView.InventoryGroup futureTalksGroup = new CollectionView.InventoryGroup(DEFAULT_GROUP_ID)
                    .setDisplayCols(numColumns)
                    .setShowHeader(!mFilterTags[0].isEmpty())
                    .setHeaderLabel(mFilterTags[0])
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

            List<Talk> mergedTalks = new ArrayList<Talk>(futureTalks) {{
                addAll(pastTalks);
            }};
            mScheduleGrid.setCollectionAdapter(new ScheduleAdapter(getActivity(), itemLayout, mergedTalks));

            mScheduleGrid.updateInventory(inventory, mResumed && reset);

            if (scheduleGridState != null) {
                mScheduleGrid.onRestoreInstanceState(scheduleGridState);
            }
        }

        mPopulated = true;
    }

    private int getNumColumns(boolean filtering) {
        int numColumns;
        if (mLandscapeMode) {
            numColumns = filtering ? 2 : 3;
            if (mWideMode && filtering) {
                numColumns++;
            }
        } else {
            numColumns = filtering ? 1 : 2;
            if (mWideMode) {
                numColumns++;
            }
        }
        return numColumns;
    }

    private void showSecondaryFilters() {
        showFilterBox(false);

        // repopulate secondary filter spinners
        if (!TextUtils.isEmpty(mFilterTags[0])) {
            TagMetadata.Tag topTag = mTagMetadata.getTag(mFilterTags[0]);
            String topCategory = topTag.getCategory();
            if (topCategory.equals(Tags.FILTER_CATEGORIES[0])) {
                populateSecondLevelFilterSpinner(0, 1);
                populateSecondLevelFilterSpinner(1, 2);
            } else if (topCategory.equals(Tags.FILTER_CATEGORIES[1])) {
                populateSecondLevelFilterSpinner(0, 0);
                populateSecondLevelFilterSpinner(1, 2);
            } else {
                populateSecondLevelFilterSpinner(0, 0);
                populateSecondLevelFilterSpinner(1, 1);
            }
            showFilterBox(true);
        }
    }

    private void populateSecondLevelFilterSpinner(int spinnerIndex, int catIndex) {
        String tagToRestore = mFilterTagsToRestore[spinnerIndex + 1];
        Spinner spinner = spinnerIndex == 0 ? mSecondaryFilterSpinner1 : mSecondaryFilterSpinner2;
        final int filterIndex = spinnerIndex + 1;
        String tagCategory = Tags.FILTER_CATEGORIES[catIndex];
        boolean isTopicCategory = Tags.CATEGORY_TOPIC.equals(tagCategory);

        final FilterScheduleSpinnerAdapter adapter = new FilterScheduleSpinnerAdapter(getActivity(), false);
        adapter.addItem("", getString(Tags.EXPLORE_CATEGORY_ALL_STRING[catIndex]), false, 0);

        List<TagMetadata.Tag> tags = mTagMetadata.getTagsInCategory(tagCategory);
        int itemToSelect = spinner.getSelectedItemPosition();
        if (tags != null) {
            for (TagMetadata.Tag tag : tags) {
                adapter.addItem(tag.getId(), tag.getName(), false,
                        isTopicCategory ? tag.getColor() : 0);
                if (!TextUtils.isEmpty(tagToRestore) && tag.getId().equals(tagToRestore)) {
                    itemToSelect = adapter.getCount() - 1;
                    mFilterTagsToRestore[spinnerIndex + 1] = null;
                }
            }
        } else {
            Timber.e("Can't populate spinner. Category has no tags: " + tagCategory);
        }
        mFilterTagsToRestore[spinnerIndex + 1] = null;

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectTag(adapter.getTag(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectTag("");
            }

            private void selectTag(String tag) {
                if (!mFilterTags[filterIndex].equals(tag)) {
                    mFilterTags[filterIndex] = tag;
                    populateScheduleGrid(true);
                }
            }
        });

        if (itemToSelect != spinner.getSelectedItemPosition()) {
            spinner.setSelection(itemToSelect, false);
        }
    }

    private void showFilterBox(boolean show) {
        if (mFiltersBox != null) {
            mFiltersBox.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        updateSpacerHeight();
    }

    private void updateSpacerHeight() {
        boolean filterBoxVisible = mFiltersBox != null && mFiltersBox.getVisibility() == View.VISIBLE;
        int actionBarSize = UIUtils.calculateActionBarSize(getActivity());
        int filterBoxSize = filterBoxVisible ? getResources().getDimensionPixelSize(R.dimen.filterbar_height) : 0;
        int contentTopClearance = actionBarSize + filterBoxSize;
        mScheduleGrid.setContentTopClearance(contentTopClearance + mScheduleGridPaddingTop);
        mContainer.setShadowTopOffset(contentTopClearance);
    }

    private void computeActionBarSpinnerAdapter() {
        mFilterScheduleSpinnerAdapter.clear();

        mFilterScheduleSpinnerAdapter.addItem("", getString(R.string.all_talks), false, 0);

        if (mSchedule != null) {
            int itemToSelect = -1;

            mTagMetadata = TagMetadata.fromSchedule(mSchedule);
            for (int i = 0; i < Tags.FILTER_CATEGORIES.length; i++) {
                String category = Tags.FILTER_CATEGORIES[i];
                String categoryTitle = getString(Tags.FILTER_CATEGORY_TITLE[i]);

                List<TagMetadata.Tag> tags = mTagMetadata.getTagsInCategory(category);
                if (tags != null) {
                    mFilterScheduleSpinnerAdapter.addHeader(categoryTitle);
                    for (TagMetadata.Tag tag : tags) {
                        mFilterScheduleSpinnerAdapter.addItem(tag.getId(), tag.getName(), true, Tags.CATEGORY_TOPIC.equals(category) ? tag
                                .getColor() : 0);
                        if (!TextUtils.isEmpty(mFilterTagsToRestore[0]) && tag.getId().equals(mFilterTagsToRestore[0])) {
                            mFilterTagsToRestore[0] = null;
                            itemToSelect = mFilterScheduleSpinnerAdapter.getCount() - 1;
                        }
                    }
                } else {
                    Timber.w("Ignoring filter category with no tags: " + category);
                }
            }
            mFilterTagsToRestore[0] = null;

            if (itemToSelect >= 0) {
                Timber.d("Restoring item selection to primary spinner: " + itemToSelect);
                mSpinner.setSelection(itemToSelect);
            }
        }

        mFilterScheduleSpinnerAdapter.notifyDataSetChanged();
        showSecondaryFilters();
    }

    private void enableTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            LayoutTransition layoutTransition = new LayoutTransition();
            layoutTransition.enableTransitionType(LayoutTransition.APPEARING);
            mContainer.setLayoutTransition(layoutTransition);
        }
    }


    public void onEventMainThread(SyncEvent syncEvent) {
        BaseActivity baseActivity = (BaseActivity) getActivity();
        if (mScheduleGrid != null && !baseActivity.isMainContentScrolling()) {
            populateScheduleGrid(false);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mFilterTagsToRestore[0] = mFilterTags[0];
        mFilterTagsToRestore[1] = mFilterTags[1];
        mFilterTagsToRestore[2] = mFilterTags[2];
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public boolean handleResult(CursorList<Talk> cursorList) {
        mSchedule = new Schedule(cursorList == null ? new ArrayList<Talk>() : cursorList.asList(), true);

        if (getView() == null) {
            return true;
        }

        populateScheduleGrid(false);

        return true;
    }

    @Override
    public void onDestroyView() {
        mSpinner = null;
        BaseActivity activity = (BaseActivity) getActivity();
        activity.setActionBarAutoShowOrHideListener(null);
        activity.deregisterHideableHeaderView(mFiltersBox);
        mScheduleGrid.setOnScrollListener(null);
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    @Override
    public void onActionBarAutoShowOrHide(boolean shown) {
        if (mContainer != null) {
            mContainer.setShadowVisible(shown, true);
        }
    }
}
