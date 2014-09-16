package fr.xebia.conference.companion.ui.speaker;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;
import butterknife.ButterKnife;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.model.Talk;

public class SpeakerScheduleItemView extends TextView {

    private int mDotSize;
    private ShapeDrawable mColorDrawable;

    public SpeakerScheduleItemView(Context context) {
        super(context);
    }

    public SpeakerScheduleItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
        mDotSize = getContext().getResources().getDimensionPixelSize(R.dimen.tag_color_dot_size);
    }

    public void bind(Talk talk) {
        Resources resources = getResources();
        if (!TextUtils.isEmpty(talk.getPrettySpeakers())) {
            setText(Html.fromHtml(resources.getString(R.string.schedule_format, talk.getTitle(),
                    String.format("%s | %s | %s | %s", talk.getDay(), talk.getPeriod(), talk.getRoom(), talk.getPrettySpeakers()))));
        } else {
            setText(Html.fromHtml(resources.getString(R.string.schedule_format, talk.getTitle(),
                    String.format("%s | %s | %s", talk.getDay(), talk.getPeriod(), talk.getRoom()))));
        }

        if (mColorDrawable == null) {
            mColorDrawable = new ShapeDrawable(new OvalShape());
            mColorDrawable.setIntrinsicWidth(mDotSize);
            mColorDrawable.setIntrinsicHeight(mDotSize);
            mColorDrawable.getPaint().setStyle(Paint.Style.FILL);
            setCompoundDrawablesWithIntrinsicBounds(null, null, mColorDrawable, null);
        }

        mColorDrawable.getPaint().setColor(talk.getColor());
    }

}
