package fr.xebia.conference.companion.ui.schedule;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.model.ConferenceDay;
import fr.xebia.conference.companion.model.MyScheduleItem;

import java.util.List;

public class MyScheduleFragment extends Fragment {

    @InjectView(android.R.id.list) ListView myScheduleListView;

    private MyScheduleAdapter mAdapter;
    private List<MyScheduleItem> myScheduleItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.my_schedule_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        mAdapter = new MyScheduleAdapter(getActivity());
        myScheduleListView.setAdapter(mAdapter);
        if (myScheduleItems != null) {
            mAdapter.updateItems(myScheduleItems);
        }
    }

    public static Fragment newInstance() {
        return new MyScheduleFragment();
    }

    public void setData(ConferenceDay conferenceDay) {
        myScheduleItems = conferenceDay.myScheduleItems;
        if (mAdapter != null) {
            mAdapter.updateItems(myScheduleItems);
        }
    }
}
