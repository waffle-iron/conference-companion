package fr.xebia.conference.companion.ui.schedule;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import fr.xebia.conference.companion.core.adapter.BaseAdapter;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.model.Talk;
import fr.xebia.conference.companion.ui.widget.UnderlinedTextView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

import java.util.List;

public class ScheduleAdapter extends BaseAdapter<List<Talk>> implements StickyListHeadersAdapter {

    private int mUnderlineColor;
    private int mUnderlineHeight;
    private LayoutInflater inflater;
    private boolean mShowDays;

    public ScheduleAdapter(Context context, int viewResId, List<Talk> data) {
        super(context, viewResId, data);
        init(context);
    }

    private void init(Context context) {
        inflater = LayoutInflater.from(context);
        Resources resources = context.getResources();
        mUnderlineColor = resources.getColor(R.color.xebia_color);
        mUnderlineHeight = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, resources.getDisplayMetrics()));
    }

    public ScheduleAdapter(Context context, int viewResId, List<Talk> data, boolean showDays) {
        super(context, viewResId, data);
        init(context);
        mShowDays = showDays;
    }

    @Override
    protected void bindView(int position, View view) {
        ((ScheduleItemView) view).bind(getItem(position));
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

    @Override
    public View getHeaderView(int position, View view, ViewGroup viewGroup) {
        UnderlinedTextView header = (UnderlinedTextView) view;
        if (view == null) {
            header = (UnderlinedTextView) inflater.inflate(R.layout.schedule_item_header, viewGroup, false);
            header.setUnderlineColor(mUnderlineColor);
            header.setUnderlineHeight(mUnderlineHeight);
        }
        Talk talk = getItem(position);
        if (mShowDays) {
            header.setText(String.format("%s | %s", talk.getDay(), talk.getPeriod()));
        } else {
            header.setText(talk.getPeriod());
        }
        return header;
    }

    @Override
    public long getHeaderId(int i) {
        return getItem(i).getFromTime().getTime();
    }
}
