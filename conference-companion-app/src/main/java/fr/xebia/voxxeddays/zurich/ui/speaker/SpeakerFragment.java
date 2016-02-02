package fr.xebia.voxxeddays.zurich.ui.speaker;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.voxxeddays.zurich.R;
import fr.xebia.voxxeddays.zurich.core.adapter.BaseRecyclerAdapter;
import fr.xebia.voxxeddays.zurich.core.misc.Preferences;
import fr.xebia.voxxeddays.zurich.model.Speaker;
import icepick.Icepick;
import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.ManyQuery;
import se.emilsjolander.sprinkles.Query;

public class SpeakerFragment extends Fragment implements ManyQuery.ResultHandler<Speaker> {

    @InjectView(R.id.container) FrameLayout mContainer;
    @InjectView(R.id.empty_id) TextView mEmptyText;
    @InjectView(R.id.speakers_grid) RecyclerView mSpeakersGrid;

    private BaseRecyclerAdapter<Speaker, SpeakerItemView> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        setRetainInstance(true);

        adapter = new BaseRecyclerAdapter<>(getActivity(), R.layout.speaker_short_item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.speaker_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);

        boolean isLandscape = getResources().getBoolean(R.bool.landscape);
        mSpeakersGrid.setLayoutManager(new GridLayoutManager(getActivity(), isLandscape ? 4 : 3));

        int conferenceId = Preferences.getSelectedConference(getActivity());
        Query.many(Speaker.class, "SELECT * FROM Speakers WHERE conferenceId=? ORDER BY firstName ASC, lastName ASC",
                conferenceId).getAsync(getLoaderManager(), this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }


    @Override
    public void onDestroyView() {
        mSpeakersGrid.setOnScrollListener(null);
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    @Override
    public boolean handleResult(CursorList<Speaker> speakersCursor) {
        List<Speaker> speakers = speakersCursor.asList();
        if (getView() == null) {
            return false;
        }

        if (speakers == null || speakers.isEmpty()) {
            mEmptyText.setText(getString(R.string.no_data));
            mEmptyText.setVisibility(View.VISIBLE);
            mSpeakersGrid.setVisibility(View.GONE);
        } else {
            mEmptyText.setText("");
            mEmptyText.setVisibility(View.GONE);
            mSpeakersGrid.setVisibility(View.VISIBLE);

            adapter.setDatas(speakers);
            mSpeakersGrid.setAdapter(adapter);
        }

        return false;

    }

}
