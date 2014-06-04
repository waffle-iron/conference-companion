package fr.xebia.conference.companion.ui.conference;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.bus.ConferenceFetchedEvent;
import fr.xebia.conference.companion.bus.ConferenceSelectedEvent;
import fr.xebia.conference.companion.core.misc.RestoreActionBarFragment;
import fr.xebia.conference.companion.model.Conference;
import fr.xebia.conference.companion.service.ConferencesFetcherIntentService;
import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.ManyQuery;
import se.emilsjolander.sprinkles.Query;

import java.util.List;

import static fr.xebia.conference.companion.core.KouignAmanApplication.BUS;

public class ConferenceChooserFragment extends Fragment implements ManyQuery.ResultHandler<Conference>, RestoreActionBarFragment {


    public static final String TAG = "ConferenceChooserFragment";

    @InjectView(R.id.progress_container) ViewGroup mProgressContainer;

    @InjectView(R.id.content_container) ViewGroup mContentContainer;
    @InjectView(R.id.conferences_list) ListView mConferencesListView;

    @InjectView(R.id.error_container) ViewGroup mErrorContainer;

    private List<Conference> mConferences;
    private ConferenceAdapter mConferenceAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        BUS.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.conference_chooser_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);

        mConferencesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BUS.post(new ConferenceSelectedEvent(mConferenceAdapter.getItem(position).getId()));
            }
        });

        if (mConferences == null || mConferences.isEmpty()) {
            mProgressContainer.setVisibility(View.VISIBLE);
            mContentContainer.setVisibility(View.GONE);
            mErrorContainer.setVisibility(View.GONE);
            Query.many(Conference.class, "SELECT * FROM Conferences ORDER BY fromTime DESC").getAsync(getLoaderManager(), this);
            getActivity().startService(new Intent(getActivity(), ConferencesFetcherIntentService.class));
        } else {
            mConferenceAdapter = new ConferenceAdapter(getActivity(), R.layout.conference_item_view, mConferences);
            mConferencesListView.setAdapter(mConferenceAdapter);
        }
    }

    @Override
    public void onStop() {
        BUS.unregister(this);
        super.onStop();
    }

    public void onEventMainThread(ConferenceFetchedEvent event) {
        if (event.success) {
            Toast.makeText(getActivity(), R.string.conferences_fetched, Toast.LENGTH_SHORT).show();
        } else {
            mErrorContainer.setVisibility(View.VISIBLE);
            mContentContainer.setVisibility(View.GONE);
            mProgressContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void restoreActionBar() {
        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().setDisplayShowTitleEnabled(true);
        getActivity().getActionBar().setTitle(R.string.speakers);
    }

    @Override
    public boolean handleResult(CursorList<Conference> conferencesCursor) {
        mConferences = conferencesCursor.asList();
        if (getView() != null && !mConferences.isEmpty()) {
            if (mConferenceAdapter == null) {
                mConferenceAdapter = new ConferenceAdapter(getActivity(), R.layout.conference_item_view, mConferences);
                mConferencesListView.setAdapter(mConferenceAdapter);
            } else {
                mConferenceAdapter.switchData(mConferences);
            }

            mContentContainer.setVisibility(View.VISIBLE);
            mErrorContainer.setVisibility(View.GONE);
            mProgressContainer.setVisibility(View.GONE);
        }
        return true;
    }
}
