package fr.xebia.conference.companion.ui.conference;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.model.Conference;

public class ConferenceItemView extends LinearLayout {

    @InjectView(R.id.conference_name) TextView mConferenceName;

    public ConferenceItemView(Context context) {
        super(context);
    }

    public ConferenceItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ConferenceItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this, this);
    }

    public void bind(Conference conference) {
        mConferenceName.setText(conference.getName());
    }
}
