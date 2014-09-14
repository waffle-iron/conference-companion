package fr.xebia.conference.companion.ui.browse;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.model.Talk;
import fr.xebia.conference.companion.ui.talk.TalkActivity;

import java.util.ArrayList;
import java.util.List;

import static com.fluent.android.bundle.FluentBundle.newFluentBundle;
import static com.fluent.android.bundle.FragmentArgsSetter.setFragmentArguments;

public class BrowseTalksFragment extends ListFragment {

    public static final String TAG = "BrowseTalksFragment";

    public static String ARG_AVAILABLE_TALKS = "fr.xebia.conference.companion.ARG_AVAILABLE_TALKS";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<Talk> availableTalks = getArguments().getParcelableArrayList(ARG_AVAILABLE_TALKS);
        setListAdapter(new BrowseTalksAdapter(getActivity(), R.layout.talk_item_view, availableTalks));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListShown(true);
        getListView().setDividerHeight(0);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Talk talk = (Talk) getListAdapter().getItem(position);
                Intent intent = new Intent(getActivity(), TalkActivity.class);
                intent.putExtra(TalkActivity.EXTRA_TALK_ID, talk.getId());
                intent.putExtra(TalkActivity.EXTRA_TALK_COLOR, talk.getColor());
                intent.putExtra(TalkActivity.EXTRA_TALK_TITLE, talk.getTitle());
                startActivity(intent);
            }
        });
    }

    public static BrowseTalksFragment newInstance(ArrayList<Talk> availableTalks) {
        return setFragmentArguments(new BrowseTalksFragment(), newFluentBundle().put(ARG_AVAILABLE_TALKS, availableTalks));
    }
}
