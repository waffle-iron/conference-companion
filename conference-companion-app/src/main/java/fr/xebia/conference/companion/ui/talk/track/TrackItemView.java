package fr.xebia.conference.companion.ui.talk.track;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.TextView;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.model.Track;
import fr.xebia.conference.companion.model.TrackResource;

public class TrackItemView extends TextView {

    public TrackItemView(Context context) {
        super(context);
    }

    public TrackItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void bind(Track track) {
        String trackTitle = track.getTitle();

        Resources resources = getResources();
        Drawable trackDrawable = resources.getDrawable(TrackResource.getIconForTrack(trackTitle)).mutate();
        trackDrawable.setColorFilter(resources.getColor(TrackResource.getColorResForTrack(trackTitle)), PorterDuff.Mode.SRC_IN);

        Drawable arrowRight = resources.getDrawable(R.drawable.ic_arrow_right);
        arrowRight.setColorFilter(resources.getColor(R.color.xebia_color), PorterDuff.Mode.SRC_IN);

        setCompoundDrawablesWithIntrinsicBounds(trackDrawable, null, arrowRight, null);

        String trackName = resources.getString(R.string.track_format, trackTitle, track.getCount());
        setText(Html.fromHtml(trackName.replaceAll("<Devoxx>", "&lt;Devoxx&gt;")));
    }
}
