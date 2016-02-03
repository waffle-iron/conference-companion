package fr.xebia.voxxeddays.zurich.ui.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.voxxeddays.zurich.R;
import fr.xebia.voxxeddays.zurich.core.KouignAmanApplication;
import fr.xebia.voxxeddays.zurich.model.Conference;
import fr.xebia.voxxeddays.zurich.service.SynchroIntentService;

public class NavigationHeaderView extends RelativeLayout {

    @InjectView(R.id.image_background) ImageView background;

    public NavigationHeaderView(Context context) {
        super(context);
    }

    public NavigationHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NavigationHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ButterKnife.inject(this);

        bindView();
    }

    private void bindView() {
        Conference conference = KouignAmanApplication.getGson().fromJson(SynchroIntentService.VOXXEDDAYS_ZURICH_CONFERENCE, Conference.class);
        Picasso.with(getContext())
                .load(conference.getBackgroundUrl())
                .fit()
                .centerCrop()
                .into(background);
    }

}
