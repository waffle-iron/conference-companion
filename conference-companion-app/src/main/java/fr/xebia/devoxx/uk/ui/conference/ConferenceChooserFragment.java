package fr.xebia.devoxx.uk.ui.conference;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import fr.xebia.devoxx.uk.R;
import fr.xebia.devoxx.uk.bus.ConferenceFetchedEvent;
import fr.xebia.devoxx.uk.bus.ConferenceSelectedEvent;
import fr.xebia.devoxx.uk.core.misc.RestoreActionBarFragment;
import fr.xebia.devoxx.uk.model.Conference;
import fr.xebia.devoxx.uk.service.ConferencesFetcherIntentService;
import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.ManyQuery;
import se.emilsjolander.sprinkles.Query;

import static fr.xebia.devoxx.uk.core.KouignAmanApplication.BUS;

public class ConferenceChooserFragment extends Fragment implements ManyQuery.ResultHandler<Conference>, RestoreActionBarFragment {


    public static final String TAG = "ConferenceChooserFragment";

    @InjectView(R.id.progress_container) ViewGroup mProgressContainer;

    @InjectView(R.id.error_container) ViewGroup mErrorContainer;

    private List<Conference> mConferences;

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

        if (mConferences == null || mConferences.isEmpty()) {
            mProgressContainer.setVisibility(View.VISIBLE);
            mErrorContainer.setVisibility(View.GONE);
            getActivity().startService(new Intent(getActivity(), ConferencesFetcherIntentService.class));
        } else {
            BUS.post(new ConferenceSelectedEvent(mConferences.get(0).getId()));
        }
    }

    @Override
    public void onStop() {
        BUS.unregister(this);
        super.onStop();
    }

    public void onEventMainThread(ConferenceFetchedEvent event) {
        Query.many(Conference.class, "SELECT * FROM Conferences ORDER BY fromDate DESC").getAsync(getLoaderManager(), this);
    }

    @OnClick(R.id.retry_btn)
    public void onRetryButtonClicked() {
        mProgressContainer.setVisibility(View.VISIBLE);
        mErrorContainer.setVisibility(View.GONE);
        getActivity().startService(new Intent(getActivity(), ConferencesFetcherIntentService.class));
    }

    @Override
    public void restoreActionBar() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.speakers);
    }

    @Override
    public boolean handleResult(CursorList<Conference> conferencesCursor) {
        mConferences = conferencesCursor.asList();
        if (getView() == null) {
            return true;
        }
        if (mConferences.isEmpty()) {
            mErrorContainer.setVisibility(View.VISIBLE);
            mProgressContainer.setVisibility(View.GONE);
        } else {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    BUS.post(new ConferenceSelectedEvent(mConferences.get(0).getId()));
                }
            });
            mErrorContainer.setVisibility(View.GONE);
            mProgressContainer.setVisibility(View.GONE);
        }
        return true;
    }
}
