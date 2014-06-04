package fr.xebia.conference.companion.ui.vote;

import android.content.Context;
import android.view.View;
import fr.xebia.conference.companion.core.adapter.BaseAdapter;
import fr.xebia.conference.companion.model.TalkVote;

import java.util.List;

public class TalkVoteAdapter extends BaseAdapter<List<TalkVote>> {

    public TalkVoteAdapter(Context context, int viewResId, List<TalkVote> data) {
        super(context, viewResId, data);
    }

    @Override
    protected void bindView(int position, View view) {
        ((VoteItemView) view).bind(getItem(position));
    }

    @Override
    public int getCount() {
        List<TalkVote> data = getData();
        return data == null ? 0 : data.size();
    }

    @Override
    public TalkVote getItem(int position) {
        return getData().get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getFromTime().getTime();
    }
}
