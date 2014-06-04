package fr.xebia.conference.companion.ui.speaker;

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
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.core.misc.Preferences;
import fr.xebia.conference.companion.core.misc.RestoreActionBarFragment;
import fr.xebia.conference.companion.model.Speaker;
import icepick.Icepick;
import icepick.Icicle;
import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.ManyQuery;
import se.emilsjolander.sprinkles.Query;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import java.util.List;

public class SpeakerFragment extends Fragment implements ManyQuery.ResultHandler<Speaker>, RestoreActionBarFragment {

    @InjectView(R.id.container) ViewGroup mContainer;
    @InjectView(R.id.empty_id) TextView mEmptyText;
    @InjectView(R.id.sticky_list) StickyListHeadersListView mListView;
    private List<Speaker> mSpeakers;

    @Icicle Parcelable mListViewState;

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
        mListView.setDrawingListUnderStickyHeader(false);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), SpeakerDetailsActivity.class);
                intent.putExtra(SpeakerDetailsActivity.EXTRA_SPEAKER_ID, mSpeakers.get(position).getId());
                startActivity(intent);
            }
        });

        restoreActionBar();

        int conferenceId = Preferences.getSelectedConference(getActivity());
        Query.many(Speaker.class, "SELECT * FROM Speakers WHERE conferenceId=? ORDER BY firstName ASC, lastName ASC",
                conferenceId).getAsync(getLoaderManager(), this);
    }

    @Override
    public void onPause() {
        mListViewState = mListView.onSaveInstanceState();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
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
            mListView.setVisibility(View.GONE);
        } else {
            mEmptyText.setText("");
            mEmptyText.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            mListView.setAdapter(new SpeakerAdapter(getActivity(), R.layout.speaker_short_item, mSpeakers, true));
            if (mListViewState != null) {
                mListView.onRestoreInstanceState(mListViewState);
            }
        }

        return false;
    }

    @Override
    public void restoreActionBar() {
        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().setDisplayShowTitleEnabled(true);
        getActivity().getActionBar().setTitle(R.string.speakers);
    }
}
