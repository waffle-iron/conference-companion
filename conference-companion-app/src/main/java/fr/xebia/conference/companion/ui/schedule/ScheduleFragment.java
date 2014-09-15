package fr.xebia.conference.companion.ui.schedule;

import android.animation.LayoutTransition;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
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
import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.core.activity.BaseActivity;
import fr.xebia.conference.companion.core.misc.Preferences;
import fr.xebia.conference.companion.core.misc.RestoreActionBarFragment;
import fr.xebia.conference.companion.model.Schedule;
import fr.xebia.conference.companion.model.TagMetadata;
import fr.xebia.conference.companion.model.Tags;
import fr.xebia.conference.companion.model.Talk;
import fr.xebia.conference.companion.ui.talk.TalkActivity;
import fr.xebia.conference.companion.ui.widget.DrawShadowFrameLayout;
import fr.xebia.conference.companion.ui.widget.HeaderGridView;
import fr.xebia.conference.companion.ui.widget.UIUtils;
import icepick.Icepick;
import icepick.Icicle;
import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.ManyQuery;
import se.emilsjolander.sprinkles.Query;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.List;

public class ScheduleFragment extends Fragment implements ManyQuery.ResultHandler<Talk>, RestoreActionBarFragment,
        BaseActivity.OnActionBarAutoShowOrHideListener {

    public static final String TAG = "ScheduleFragment";

    public static final String EXTRA_TRACK_NAME = "fr.xebia.devoxx.EXTRA_TRACK_NAME";
    public static final String EXTRA_FAVORITE_ONLY = "fr.xebia.devoxx.EXTRA_FAVORITE_ONLY";

    @InjectView(R.id.container) DrawShadowFrameLayout mContainer;
    @InjectView(R.id.empty_id) TextView mEmptyText;
    @InjectView(R.id.schedule_grid) HeaderGridView mGridView;
    @InjectView(R.id.filters_box) ViewGroup mFiltersBox;
    @InjectView(R.id.secondary_filter_spinner_1) Spinner mSecondaryFilterSpinner1;
    @InjectView(R.id.secondary_filter_spinner_2) Spinner mSecondaryFilterSpinner2;

    private Schedule mSchedule;

    @Icicle String mTrack;
    @Icicle boolean mFavoriteOnly;

    // filter tags that are currently selected
    @Icicle String[] mFilterTags = {"", "", ""};

    // filter tags that we have to restore (as a result of Activity recreation)
    @Icicle String[] mFilterTagsToRestore = {null, null, null};

    private Spinner mSpinner;
    private FilterScheduleSpinnerAdapter mFilterScheduleSpinnerAdapter;

    private TagMetadata mTagMetadata;

    private View mHeaderSpacer;
    private boolean mLandscapeMode;
    private ScheduleAdapter mGridAdapter;

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
        mLandscapeMode = getResources().getBoolean(R.bool.landscape);
        enableTransition();
        BaseActivity activity = (BaseActivity) getActivity();
        activity.enableActionBarAutoHide(mGridView);
        activity.registerHideableHeaderView(mFiltersBox);
        activity.setActionBarAutoShowOrHideListener(this);

        // TODO export num columns to resources
        if (mLandscapeMode) {
            mGridView.setNumColumns("".equals(mFilterTags[0]) ? 3 : 2);
        } else {
            mGridView.setNumColumns("".equals(mFilterTags[0]) ? 2 : 1);
        }
        mGridView.addHeaderView(buildSpacerView());
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
        Query.many(Talk.class, "SELECT * FROM Talks WHERE conferenceId=? ORDER BY fromTime ASC", conferenceId)
                .getAsync(getLoaderManager(), this);
        configureActionBarSpinner();
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
        filterData(true);
    }

    private void filterData(boolean reset) {
        boolean filtering = !"".equals(mFilterTags[0]);
        int itemLayout = filtering ? R.layout.talk_item_view : R.layout.schedule_item_view;
        if (mLandscapeMode) {
            mGridView.setNumColumns(filtering ? 2 : 3);
        } else {
            mGridView.setNumColumns(filtering ? 1 : 2);
        }
        List<Talk> filteredTalks = mSchedule.getFilteredTalks(mTagMetadata.getTag(mFilterTags[0]), mTagMetadata.getTag(mFilterTags[1]),
                mTagMetadata.getTag(mFilterTags[2]));
        mGridAdapter = new ScheduleAdapter(getActivity(), itemLayout, filteredTalks);

        Parcelable gridViewState = null;
        if (!reset) {
            gridViewState = mGridView.onSaveInstanceState();
        }
        mGridView.setAdapter(mGridAdapter);
        if (gridViewState != null) {
            mGridView.onRestoreInstanceState(gridViewState);
        }

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
                    filterData(true);
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
        ViewGroup.LayoutParams layoutParams = mHeaderSpacer.getLayoutParams();
        layoutParams.height = actionBarSize + filterBoxSize;
        mHeaderSpacer.setLayoutParams(layoutParams);
        mContainer.setShadowTopOffset(layoutParams.height);
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

    private View buildSpacerView() {
        Activity context = getActivity();
        mHeaderSpacer = new View(context);
        mHeaderSpacer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                UIUtils.calculateActionBarSize(context)));
        return mHeaderSpacer;
    }

    private void enableTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            LayoutTransition layoutTransition = new LayoutTransition();
            layoutTransition.enableTransitionType(LayoutTransition.APPEARING);
            mContainer.setLayoutTransition(layoutTransition);
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

        computeActionBarSpinnerAdapter();

        mEmptyText.setText("");
        mEmptyText.setVisibility(View.GONE);

        mGridView.setVisibility(View.VISIBLE);

        filterData(false);

        return true;
    }

    @Override
    public void restoreActionBar() {
        /*ActionBar actionBar = getActivity().getActionBar();
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
        }*/
    }

    @Override
    public void onDestroyView() {
        mSpinner = null;
        BaseActivity activity = (BaseActivity) getActivity();
        activity.setActionBarAutoShowOrHideListener(null);
        activity.deregisterHideableHeaderView(mFiltersBox);
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

    @Override
    public void onActionBarAutoShowOrHide(boolean shown) {
        if (mContainer != null) {
            mContainer.setShadowVisible(shown, true);
        }
    }
}
