package fr.xebia.voxxeddays.zurich.ui.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.voxxeddays.zurich.R;
import fr.xebia.voxxeddays.zurich.core.KouignAmanApplication;
import fr.xebia.voxxeddays.zurich.core.misc.Preferences;
import fr.xebia.voxxeddays.zurich.model.Conference;
import fr.xebia.voxxeddays.zurich.service.SynchroIntentService;

public class NavigationHeaderView extends RelativeLayout {

    @InjectView(R.id.image_background) ImageView background;
    @InjectView(R.id.logo) ImageView logo;
    @InjectView(R.id.name) TextView name;
    @InjectView(R.id.date) TextView date;

    SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE dd MMMM yyyy");

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
        try {
            SynchroIntentService.Conferences currentConf = SynchroIntentService.Conferences.from(Preferences.getSelectedConference(getContext()));

            int resId = currentConf.logoId;
            String confInfos = currentConf.infos;

            Conference conference = KouignAmanApplication.getGson().fromJson(confInfos, Conference.class);

            name.setText(conference.getName());
            date.setText(dateFormat.format(conference.getFrom()));

            Picasso.with(getContext())
                    .load(resId)
                    .into(logo);

            Picasso.with(getContext())
                    .load(conference.getBackgroundUrl())
                    .fit()
                    .centerCrop()
                    .into(background);
        } catch (Exception e) {

        }

    }

}
