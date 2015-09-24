package fr.xebia.xebicon.ui.schedule;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import fr.xebia.xebicon.R;
import fr.xebia.xebicon.core.adapter.BaseAdapter;
import fr.xebia.xebicon.core.misc.Preferences;
import fr.xebia.xebicon.model.Talk;
import fr.xebia.xebicon.ui.browse.TalkItemView;
import fr.xebia.xebicon.ui.talk.TalkActivity;
import fr.xebia.xebicon.ui.widget.CollectionViewCallbacks;

public class ScheduleAdapter extends BaseAdapter<List<Talk>> implements CollectionViewCallbacks {

    private final long conferenceEndTime;

    public ScheduleAdapter(Context context, int viewResId, List<Talk> data) {
        super(context, viewResId, data);
        conferenceEndTime = Preferences.getSelectedConferenceEndTime(context);
    }

    @Override
    protected void bindView(final int position, View view) {
        boolean conferenceEnded = System.currentTimeMillis() > conferenceEndTime;
        if (view instanceof ScheduleItemView) {
            ((ScheduleItemView) view).bind(getItem(position), conferenceEnded);
        } else {
            ((TalkItemView) view).bind(getItem(position), conferenceEnded);
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Talk talk = getItem(position);
                if (!talk.isBreak()) {
                    Intent intent = new Intent(getContext(), TalkActivity.class);
                    intent.putExtra(TalkActivity.EXTRA_TALK_ID, talk.getId());
                    intent.putExtra(TalkActivity.EXTRA_TALK_TITLE, talk.getTitle());
                    intent.putExtra(TalkActivity.EXTRA_TALK_COLOR, talk.getColor());
                    getContext().startActivity(intent);
                }
            }
        });

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
    public View newCollectionHeaderView(Context context, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.grid_header, parent, false);
    }

    @Override
    public void bindCollectionHeaderView(Context context, View view, int groupId, String headerLabel) {
        ((TextView) view).setText(headerLabel);
    }

    @Override
    public View newCollectionItemView(Context context, int index, ViewGroup parent) {
        return newView(parent);
    }

    @Override
    public void bindCollectionItemView(Context context, View view, int index) {
        bindView(index, view);
    }
}
