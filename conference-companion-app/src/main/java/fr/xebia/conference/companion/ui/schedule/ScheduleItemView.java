package fr.xebia.conference.companion.ui.schedule;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.model.Talk;
import fr.xebia.conference.companion.ui.widget.UIUtils;

public class ScheduleItemView extends RelativeLayout implements Callback {

    @InjectView(R.id.schedule_bg_img) ImageView mScheduleBgImg;
    @InjectView(R.id.schedule_title) TextView mScheduleTitle;
    @InjectView(R.id.schedule_speakers) TextView mScheduleSpeakers;
    @InjectView(R.id.schedule_room) TextView mScheduleRoom;

    public ScheduleItemView(Context context) {
        super(context);
    }

    public ScheduleItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    public void bind(Talk talk) {
        Resources resources = getResources();
        bindIcons(resources, talk);
        mScheduleTitle.setText(talk.getTitle());
        mScheduleRoom.setText(String.format("%s | %s\n%s", talk.getDay(), talk.getPeriod(), talk.getRoom()));
        mScheduleSpeakers.setText(talk.getPrettySpeakers());
        if (!TextUtils.isEmpty(talk.getPrettySpeakers())) {
            mScheduleSpeakers.setText(talk.getPrettySpeakers());
            mScheduleSpeakers.setVisibility(VISIBLE);
        } else {
            mScheduleSpeakers.setVisibility(INVISIBLE);
        }
    }

    public void bindWithDay(Talk talk) {
        Resources resources = getResources();
        bindIcons(resources, talk);
        mScheduleTitle.setText(talk.getTitle());
        mScheduleRoom.setText(String.format("%s - %s\n%s", talk.getDay(), talk.getPeriod(), talk.getRoom()));
        mScheduleSpeakers.setText(talk.getPrettySpeakers());
        if (!TextUtils.isEmpty(talk.getPrettySpeakers())) {
            mScheduleSpeakers.setText(talk.getPrettySpeakers());
            mScheduleSpeakers.setVisibility(VISIBLE);
        } else {
            mScheduleSpeakers.setVisibility(INVISIBLE);
        }
    }

    private void bindIcons(Resources resources, Talk talk) {
        setBackgroundColor(UIUtils.setColorAlpha(talk.getColor(), 0.65f));
        mScheduleBgImg.setColorFilter(UIUtils.setColorAlpha(talk.getColor(), 0.65f));
        Picasso.with(getContext())
                .load(R.drawable.devoxx_template)
                .fit()
                .centerInside()
                .into(mScheduleBgImg);
    }

    @Override
    public void onSuccess() {
        setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    @Override
    public void onError() {

    }
}
