package fr.xebia.conference.companion.ui.vote;

import android.app.ActionBar;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.core.misc.Preferences;
import fr.xebia.conference.companion.core.misc.RestoreActionBarFragment;
import fr.xebia.conference.companion.model.TalkVote;
import fr.xebia.conference.companion.model.Vote;
import fr.xebia.conference.companion.ui.talk.TalkActivity;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.ManyQuery;
import se.emilsjolander.sprinkles.ModelList;
import se.emilsjolander.sprinkles.Query;

import java.util.Arrays;
import java.util.List;

import static fr.xebia.conference.companion.core.KouignAmanApplication.BUS;
import static fr.xebia.conference.companion.core.KouignAmanApplication.getVoteApi;

public class VoteFragment extends ListFragment implements RestoreActionBarFragment, ManyQuery.ResultHandler<TalkVote> {

    public static final String TAG = "VoteFragment";

    private boolean mIsLoading;

    private boolean mDataLoaded;

    private Menu mOptionsMenu;
    private TalkVoteAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BUS.register(this);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), TalkActivity.class);
                intent.putExtra(TalkActivity.EXTRA_TALK_ID, mAdapter.getItem(position).getTalkId());
                startActivity(intent);
            }
        });
        int padding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));
        getListView().setPadding(padding, 0, padding, 0);
        setListShown(true);
        restoreActionBar();
        if (!mIsLoading && !mDataLoaded) {
            reloadData();
        }
        int conferenceId = Preferences.getSelectedConference(getActivity());
        Query.many(TalkVote.class, "SELECT * FROM Talks as T JOIN Votes as V ON V._id=T._id WHERE T.conferenceId=? ORDER BY T.fromTime " +
                "ASC", conferenceId).getAsync(getLoaderManager(), this);
    }

    private void reloadData() {
        setRefreshActionButtonState(true);
        getVoteApi().getVotes(Preferences.getNfcId(getActivity()), new VotesCallback());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.vote, menu);
        mOptionsMenu = menu;
        if (mIsLoading) {
            setRefreshActionButtonState(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_votes:
                setRefreshActionButtonState(true);
                reloadData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setRefreshActionButtonState(final boolean refreshing) {
        mIsLoading = refreshing;
        if (mOptionsMenu != null) {
            final MenuItem refreshItem = mOptionsMenu.findItem(R.id.action_refresh_votes);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        BUS.unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mAdapter = null;
        mOptionsMenu = null;
        super.onDestroy();
    }

    @Override
    public void restoreActionBar() {
        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().setDisplayShowTitleEnabled(true);
        getActivity().getActionBar().setTitle(R.string.my_votes);
    }

    public void onEventMainThread(VotesResult votesResult) {
        mDataLoaded = true;
        setRefreshActionButtonState(false);
        if (votesResult.retrofitError != null) {
            Toast.makeText(getActivity(), R.string.error_loading_votes, Toast.LENGTH_LONG).show();
        } else {
            ModelList<Vote> modelList = new ModelList<>();
            modelList.addAll(Arrays.asList(votesResult.votes));
            modelList.saveAllAsync(new ModelList.OnAllSavedCallback() {
                @Override
                public void onAllSaved() {
                    Toast.makeText(getActivity(), R.string.data_updated, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public boolean handleResult(CursorList<TalkVote> talkVotesCursor) {
        List<TalkVote> talkVotes = talkVotesCursor.asList();
        if (getView() == null) {
            return true;
        }

        if (talkVotes.isEmpty()) {
            setEmptyText(getString(R.string.no_vote));
            mAdapter = null;
        } else if (mAdapter == null) {
            mAdapter = new TalkVoteAdapter(getActivity(), R.layout.vote_item_view, talkVotes);
            setListAdapter(mAdapter);
        } else {
            mAdapter.switchData(talkVotes);
        }

        return true;
    }


    public static class VotesCallback implements Callback<Vote[]> {
        @Override
        public void success(Vote[] votes, Response response) {
            BUS.post(new VotesResult(votes));
        }

        @Override
        public void failure(RetrofitError error) {
            BUS.post(new VotesResult(error));
        }
    }

    public static class VotesResult {
        public final Vote[] votes;
        public final RetrofitError retrofitError;

        public VotesResult(Vote[] votes) {
            this.votes = votes;
            retrofitError = null;
        }

        public VotesResult(RetrofitError retrofitError) {
            votes = null;
            this.retrofitError = retrofitError;
        }
    }


}
