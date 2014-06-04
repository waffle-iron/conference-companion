package fr.xebia.conference.companion.ui.schedule;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.TextView;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.model.Talk;
import fr.xebia.conference.companion.model.TrackResource;

public class ScheduleItemView extends TextView {

    public ScheduleItemView(Context context) {
        super(context);
    }

    public ScheduleItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // TODO refactor
    public void bind(Talk talk) {
        Resources resources = getResources();
        String track = talk.getTrack();
        Drawable trackDrawable = resources.getDrawable(TrackResource.getIconForTrack(track)).mutate();
        trackDrawable.setColorFilter(resources.getColor(TrackResource.getColorResForTrack(track)), PorterDuff.Mode.SRC_IN);

        Drawable favoriteDrawable = talk.isFavorite() ? resources.getDrawable(R.drawable.ic_menu_action_important).mutate() : null;
        if(favoriteDrawable != null){
            favoriteDrawable.setColorFilter(resources.getColor(R.color.xebia_menu_color), PorterDuff.Mode.SRC_IN);
        }
        trackDrawable.setColorFilter(resources.getColor(TrackResource.getColorResForTrack(track)), PorterDuff.Mode.SRC_IN);
        setCompoundDrawablesWithIntrinsicBounds(trackDrawable, null, favoriteDrawable, null);
        setText(Html.fromHtml(resources.getString(R.string.schedule_format, talk.getTitle(),
                String.format("%s | %s", talk.getPeriod(), talk.getRoom()))));
    }

    public void bindWithDay(Talk talk) {
        Resources resources = getResources();
        String track = talk.getTrack();
        Drawable trackDrawable = resources.getDrawable(TrackResource.getIconForTrack(track)).mutate();
        trackDrawable.setColorFilter(resources.getColor(TrackResource.getColorResForTrack(track)), PorterDuff.Mode.SRC_IN);
        Drawable favoriteDrawable = talk.isFavorite() ? resources.getDrawable(R.drawable.ic_menu_action_important).mutate() : null;
        if(favoriteDrawable != null){
            favoriteDrawable.setColorFilter(resources.getColor(R.color.xebia_menu_color), PorterDuff.Mode.SRC_IN);
        }
        setCompoundDrawablesWithIntrinsicBounds(trackDrawable, null, favoriteDrawable, null);
        setText(Html.fromHtml(resources.getString(R.string.schedule_format, talk.getTitle(),
                String.format("%s | %s | %s", talk.getDay(), talk.getPeriod(), talk.getRoom()))));
    }
}
