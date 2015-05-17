package fr.xebia.devoxx.uk.ui.synchro;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import fr.xebia.devoxx.uk.R;
import fr.xebia.devoxx.uk.service.SynchroIntentService;

public class SynchroFragment extends Fragment {

    public static final String TAG = "SynchroFragment";

    public static final String ARG_CONFERENCE_ID = "fr.xebia.devoxx.uk.EXTRA_CONFERENCE_ID";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Intent intent = new Intent(getActivity(), SynchroIntentService.class);
        intent.putExtra(SynchroIntentService.EXTRA_CONFERENCE_ID, getArguments().getInt(ARG_CONFERENCE_ID));
        getActivity().startService(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.synchro_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static Fragment newInstance(int conferenceId) {
        SynchroFragment synchroFragment = new SynchroFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARG_CONFERENCE_ID, conferenceId);
        synchroFragment.setArguments(arguments);
        return synchroFragment;
    }
}