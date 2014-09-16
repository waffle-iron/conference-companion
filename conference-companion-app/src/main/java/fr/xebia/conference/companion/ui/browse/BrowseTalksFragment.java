package fr.xebia.conference.companion.ui.browse;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.core.activity.BaseActivity;
import fr.xebia.conference.companion.core.misc.Preferences;
import fr.xebia.conference.companion.core.utils.SqlUtils;
import fr.xebia.conference.companion.model.Talk;
import fr.xebia.conference.companion.ui.talk.TalkActivity;
import fr.xebia.conference.companion.ui.widget.UIUtils;
import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.ManyQuery;
import se.emilsjolander.sprinkles.Query;

import java.util.ArrayList;
import java.util.List;

import static com.fluent.android.bundle.FluentBundle.newFluentBundle;
import static com.fluent.android.bundle.FragmentArgsSetter.setFragmentArguments;

public class BrowseTalksFragment extends ListFragment implements ManyQuery.ResultHandler<Talk> {

    public static final String TAG = "BrowseTalksFragment";

    public static String ARG_AVAILABLE_TALKS = "fr.xebia.conference.companion.ARG_AVAILABLE_TALKS";

    private BrowseTalksAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int conferenceId = Preferences.getSelectedConference(getActivity());
        List<String> availableTalksIds = getArguments().getStringArrayList(ARG_AVAILABLE_TALKS);
        Query.many(Talk.class, "SELECT * FROM Talks WHERE conferenceId=? AND _id IN (" +
                SqlUtils.toSqlArray(availableTalksIds) + ") ORDER BY fromTime ASC", conferenceId).getAsync(getLoaderManager(), this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListShown(true);
        ListView listView = getListView();
        listView.setDividerHeight(0);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Talk talk = (Talk) getListAdapter().getItem(position);
                Intent intent = new Intent(getActivity(), TalkActivity.class);
                intent.putExtra(TalkActivity.EXTRA_TALK_ID, talk.getId());
                intent.putExtra(TalkActivity.EXTRA_TALK_COLOR, talk.getColor());
                intent.putExtra(TalkActivity.EXTRA_TALK_TITLE, talk.getTitle());
                startActivity(intent);
            }
        });
        listView.setClipToPadding(false);
        listView.setDrawSelectorOnTop(false);
        listView.setSelector(android.R.color.transparent);
        listView.setPadding(listView.getPaddingLeft(), UIUtils.calculateActionBarSize(getActivity()), listView.getPaddingRight(),
                listView.getPaddingBottom());
        ((BaseActivity) getActivity()).enableActionBarAutoHide(listView);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) {
            setListShownNoAnimation(true);
        }
    }

    @Override
    public void onDestroyView() {
        getListView().setOnScrollListener(null);
        super.onDestroyView();
    }

    public static BrowseTalksFragment newInstance(ArrayList<String> availableTalks) {
        return setFragmentArguments(new BrowseTalksFragment(), newFluentBundle().put(ARG_AVAILABLE_TALKS, availableTalks));
    }

    @Override
    public boolean handleResult(CursorList<Talk> cursorList) {

        if (mAdapter == null) {
            mAdapter = new BrowseTalksAdapter(getActivity(), R.layout.talk_item_view, cursorList.asList());
            setListAdapter(mAdapter);
        } else {
            mAdapter.switchData(cursorList.asList());
        }

        if (getView() != null) {
            setListShownNoAnimation(true);
        }

        return true;
    }
}
