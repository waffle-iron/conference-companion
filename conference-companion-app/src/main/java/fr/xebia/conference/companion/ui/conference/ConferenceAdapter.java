package fr.xebia.conference.companion.ui.conference;

import android.content.Context;
import android.view.View;
import fr.xebia.conference.companion.core.adapter.BaseAdapter;
import fr.xebia.conference.companion.model.Conference;

import java.util.List;

public class ConferenceAdapter extends BaseAdapter<List<Conference>> {

    public ConferenceAdapter(Context context, int viewResId, List<Conference> data) {
        super(context, viewResId, data);
    }

    @Override
    protected void bindView(int position, View view) {
        ((ConferenceItemView) view).bind(getItem(position));
    }

    @Override
    public int getCount() {
        List<Conference> data = getData();
        return data == null ? 0 : data.size();
    }

    @Override
    public Conference getItem(int position) {
        return getData().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
