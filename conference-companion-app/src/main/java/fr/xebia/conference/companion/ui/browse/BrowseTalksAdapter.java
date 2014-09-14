package fr.xebia.conference.companion.ui.browse;

import android.content.Context;
import android.view.View;
import fr.xebia.conference.companion.core.adapter.BaseAdapter;
import fr.xebia.conference.companion.model.Talk;

import java.util.List;

public class BrowseTalksAdapter extends BaseAdapter<List<Talk>> {

    public BrowseTalksAdapter(Context context, int viewResId, List<Talk> data) {
        super(context, viewResId, data);
    }

    @Override
    protected void bindView(int position, View view) {
        ((TalkItemView) view).bind(getItem(position));
    }

    @Override
    public int getCount() {
        return getData().size();
    }

    @Override
    public Talk getItem(int position) {
        return getData().get(position);
    }

    @Override
    public long getItemId(int position) {
        return getData().get(position).getTalkDetailsId().hashCode();
    }
}
