package fr.xebia.devoxx.uk.ui.plan;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.devoxx.uk.R;

public class PlanFragment extends Fragment {

    private static final String ARG_IMAGE_URL = "fr.xebia.devoxx.uk.ARG_IMAGE_URL";

    @InjectView(R.id.plan_img) ImageView planImageView;

    public PlanFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.plan_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        Picasso.with(getActivity()).load(getArguments().getString(ARG_IMAGE_URL)).fit().centerInside().into(planImageView);
    }

    public static PlanFragment newInstance(String imageUrl) {
        PlanFragment planFragment = new PlanFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_URL, imageUrl);
        planFragment.setArguments(args);
        return planFragment;
    }
}
