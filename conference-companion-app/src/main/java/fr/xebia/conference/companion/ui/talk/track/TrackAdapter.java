package fr.xebia.conference.companion.ui.talk.track;

import android.content.Context;
import android.view.View;
import fr.xebia.conference.companion.core.adapter.BaseAdapter;
import fr.xebia.conference.companion.model.Track;

import java.util.List;

public class TrackAdapter extends BaseAdapter<List<Track>> {

    public TrackAdapter(Context context, int viewResId, List<Track> data) {
        super(context, viewResId, data);
    }

    @Override
    protected void bindView(int position, View view) {
        ((TrackItemView) view).bind(getItem(position));
    }

    @Override
    public int getCount() {
        List<Track> data = getData();
        return data == null ? 0 : data.size();
    }

    @Override
    public Track getItem(int position) {
        return getData().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
