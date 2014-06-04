package fr.xebia.conference.companion.ui.talk.track;

import android.app.ActionBar;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.core.misc.RestoreActionBarFragment;
import fr.xebia.conference.companion.model.Track;
import fr.xebia.conference.companion.ui.schedule.ScheduleActivity;
import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.ManyQuery;
import se.emilsjolander.sprinkles.Query;

import java.util.List;

import static fr.xebia.conference.companion.ui.schedule.ScheduleActivity.EXTRA_TRACK;

public class TrackFragment extends ListFragment implements ManyQuery.ResultHandler<Track>, RestoreActionBarFragment {

    private List<Track> mTracks;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ScheduleActivity.class);
                intent.putExtra(EXTRA_TRACK, mTracks.get(position).getTitle());
                startActivity(intent);
            }
        });

        restoreActionBar();

        setListShown(true);
        if (mTracks != null) {
            setListAdapter(new TrackAdapter(getActivity(), R.layout.track_item_view, mTracks));
        } else {
            Query.many(Track.class, "SELECT track, COUNT(*) as count FROM Talks GROUP BY track ORDER BY track ASC",
                    null).getAsync(getLoaderManager(), this);

        }
    }

    @Override
    public boolean handleResult(CursorList<Track> tracksCursor) {
        if (getView() != null) {
            mTracks = tracksCursor.asList();
            setListAdapter(new TrackAdapter(getActivity(), R.layout.track_item_view, mTracks));
        }
        return false;
    }

    @Override
    public void restoreActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(getString(R.string.talks));
    }
}
