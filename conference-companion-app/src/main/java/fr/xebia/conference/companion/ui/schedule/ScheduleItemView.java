package fr.xebia.conference.companion.ui.schedule;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.model.Talk;

public class ScheduleItemView extends RelativeLayout {

    @InjectView(R.id.schedule_text) TextView mText;
    @InjectView(R.id.schedule_color) View mColor;

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

    public void bind(Talk talk) {
        Resources resources = getResources();
        bindIcons(resources, talk);
        mText.setText(Html.fromHtml(resources.getString(R.string.schedule_format, talk.getTitle(),
                String.format("%s | %s", talk.getPeriod(), talk.getRoom()))));
    }

    public void bindWithDay(Talk talk) {
        Resources resources = getResources();
        bindIcons(resources, talk);
        mText.setText(Html.fromHtml(resources.getString(R.string.schedule_format, talk.getTitle(),
                String.format("%s | %s | %s", talk.getDay(), talk.getPeriod(), talk.getRoom()))));
    }

    private void bindIcons(Resources resources, Talk talk) {
        Drawable favoriteDrawable = talk.isFavorite() ? resources.getDrawable(R.drawable.ic_menu_action_important).mutate() : null;
        if (favoriteDrawable != null) {
            favoriteDrawable.setColorFilter(resources.getColor(R.color.xebia_menu_color), PorterDuff.Mode.SRC_IN);
        }
        mText.setCompoundDrawablesWithIntrinsicBounds(null, null, favoriteDrawable, null);

        mColor.setBackgroundColor(talk.getColor());
    }
}
