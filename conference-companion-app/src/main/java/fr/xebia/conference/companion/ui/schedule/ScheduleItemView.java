package fr.xebia.conference.companion.ui.schedule;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.model.Talk;
import fr.xebia.conference.companion.ui.widget.ExtendedRelativeLayout;
import fr.xebia.conference.companion.ui.widget.UIUtils;

public class ScheduleItemView extends ExtendedRelativeLayout implements Callback {

    @InjectView(R.id.schedule_bg_img) ImageView mScheduleBgImg;
    @InjectView(R.id.schedule_title) TextView mScheduleTitle;
    @InjectView(R.id.schedule_speakers) TextView mScheduleSpeakers;
    @InjectView(R.id.schedule_room) TextView mScheduleRoom;
    @InjectView(R.id.indicator_in_schedule) View mInSchedule;
    private boolean wideMode;
    private boolean landscape;

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
        wideMode = getContext().getResources().getBoolean(R.bool.wide_mode);
        landscape = getContext().getResources().getBoolean(R.bool.landscape);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        super.onMeasure(widthMeasureSpec, wideMode && landscape ?
                MeasureSpec.makeMeasureSpec((int) (widthSize / 1.7f), widthMode) : widthMeasureSpec);
    }

    public void bind(Talk talk, boolean conferenceEnded) {
        setBackgroundColor(UIUtils.setColorAlpha(talk.getColor(), 0.65f));
        mScheduleBgImg.setColorFilter(UIUtils.setColorAlpha(talk.getColor(), 0.65f));
        mScheduleBgImg.setBackgroundColor(talk.getColor());
        Picasso.with(getContext())
                .load(R.drawable.devoxx_template)
                .fit()
                .centerCrop()
                .into(mScheduleBgImg);

        mScheduleTitle.setText(talk.getTitle());

        if (!conferenceEnded && System.currentTimeMillis() > talk.getToUtcTime()) {
            mScheduleRoom.setText(getResources().getString(R.string.ended));
        } else {
            mScheduleRoom.setText(String.format("%s | %s\n%s", talk.getDay(), talk.getPeriod(), talk.getRoom()));
        }

        mScheduleSpeakers.setText(talk.getPrettySpeakers());
        mScheduleSpeakers.setVisibility(TextUtils.isEmpty(talk.getPrettySpeakers()) ? INVISIBLE : VISIBLE);

        mInSchedule.setVisibility(talk.isFavorite() ? VISIBLE : GONE);
    }

    @Override
    public void onSuccess() {
        setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    @Override
    public void onError() {

    }
}
