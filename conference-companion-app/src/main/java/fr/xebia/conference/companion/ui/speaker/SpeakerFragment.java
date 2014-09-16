package fr.xebia.conference.companion.ui.speaker;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.core.activity.BaseActivity;
import fr.xebia.conference.companion.core.misc.Preferences;
import fr.xebia.conference.companion.model.Speaker;
import fr.xebia.conference.companion.ui.widget.DrawShadowFrameLayout;
import fr.xebia.conference.companion.ui.widget.HeaderGridView;
import fr.xebia.conference.companion.ui.widget.UIUtils;
import icepick.Icepick;
import icepick.Icicle;
import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.ManyQuery;
import se.emilsjolander.sprinkles.Query;

import java.util.List;

public class SpeakerFragment extends Fragment implements ManyQuery.ResultHandler<Speaker>, BaseActivity.OnActionBarAutoShowOrHideListener {

    @InjectView(R.id.container) DrawShadowFrameLayout mContainer;
    @InjectView(R.id.empty_id) TextView mEmptyText;
    @InjectView(R.id.speakers_grid) HeaderGridView mSpeakersGrid;

    @Icicle Parcelable mListViewState;

    private List<Speaker> mSpeakers;
    private View mHeaderSpacer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.speaker_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        enableTransition();
        ((BaseActivity) getActivity()).enableActionBarAutoHide(mSpeakersGrid);
        ((BaseActivity) getActivity()).setActionBarAutoShowOrHideListener(this);
        mSpeakersGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), SpeakerDetailsActivity.class);
                intent.putExtra(SpeakerDetailsActivity.EXTRA_SPEAKER_ID, mSpeakers.get(position).getId());
                startActivity(intent);
            }
        });
        mSpeakersGrid.addHeaderView(buildSpacerView());

        int conferenceId = Preferences.getSelectedConference(getActivity());
        Query.many(Speaker.class, "SELECT * FROM Speakers WHERE conferenceId=? ORDER BY firstName ASC, lastName ASC",
                conferenceId).getAsync(getLoaderManager(), this);
    }

    private View buildSpacerView() {
        Activity context = getActivity();
        mHeaderSpacer = new View(context);
        mHeaderSpacer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                UIUtils.calculateActionBarSize(context)));
        return mHeaderSpacer;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getResources().getBoolean(R.bool.landscape)) {
            mSpeakersGrid.setNumColumns(4);
        } else {
            mSpeakersGrid.setNumColumns(3);
        }
    }

    @Override
    public void onPause() {
        mListViewState = mSpeakersGrid.onSaveInstanceState();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }


    @Override
    public void onDestroyView() {
        ((BaseActivity) getActivity()).setActionBarAutoShowOrHideListener(null);
        mSpeakersGrid.setOnScrollListener(null);
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    private void enableTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            LayoutTransition layoutTransition = new LayoutTransition();
            layoutTransition.enableTransitionType(LayoutTransition.APPEARING);
            mContainer.setLayoutTransition(layoutTransition);
        }
    }

    @Override
    public boolean handleResult(CursorList<Speaker> speakers) {
        mSpeakers = speakers.asList();
        if (getView() == null) {
            return false;
        }

        if (mSpeakers == null || mSpeakers.isEmpty()) {
            mEmptyText.setText(getString(R.string.no_data));
            mEmptyText.setVisibility(View.VISIBLE);
            mSpeakersGrid.setVisibility(View.GONE);
        } else {
            mEmptyText.setText("");
            mEmptyText.setVisibility(View.GONE);
            mSpeakersGrid.setVisibility(View.VISIBLE);
            mSpeakersGrid.setAdapter(new SpeakerAdapter(getActivity(), R.layout.speaker_short_item, mSpeakers, true));
            if (mListViewState != null) {
                mSpeakersGrid.onRestoreInstanceState(mListViewState);
            }
        }

        return false;
    }

    @Override
    public void onActionBarAutoShowOrHide(boolean shown) {
        if (mContainer != null) {
            mContainer.setShadowVisible(shown, true);
        }
    }
}
