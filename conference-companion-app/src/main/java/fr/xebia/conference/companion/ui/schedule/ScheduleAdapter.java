package fr.xebia.conference.companion.ui.schedule;

import android.content.Context;
import android.view.View;
import fr.xebia.conference.companion.core.adapter.BaseAdapter;
import fr.xebia.conference.companion.model.Talk;
import fr.xebia.conference.companion.ui.browse.TalkItemView;

import java.util.List;

public class ScheduleAdapter extends BaseAdapter<List<Talk>> {

    private boolean mShowDays;

    public ScheduleAdapter(Context context, int viewResId, List<Talk> data) {
        this(context, viewResId, data, false);
    }

    public ScheduleAdapter(Context context, int viewResId, List<Talk> data, boolean showDays) {
        super(context, viewResId, data);
        mShowDays = showDays;
    }

    @Override
    protected void bindView(int position, View view) {
        if (view instanceof ScheduleItemView) {
            ((ScheduleItemView) view).bind(getItem(position));
        } else {
            ((TalkItemView) view).bind(getItem(position));
        }
    }

    @Override
    public int getCount() {
        List<Talk> data = getData();
        return data == null ? 0 : data.size();
    }

    @Override
    public Talk getItem(int position) {
        return getData().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
