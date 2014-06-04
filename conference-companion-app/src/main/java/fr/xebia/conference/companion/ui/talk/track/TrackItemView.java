package fr.xebia.conference.companion.ui.talk.track;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.model.Track;

public class TrackItemView extends LinearLayout {

    @InjectView(R.id.track_category_text) TextView mText;
    @InjectView(R.id.track_category_color) View mColor;

    public TrackItemView(Context context) {
        super(context);
    }

    public TrackItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
    }

    public void bind(Track track) {
        String trackTitle = track.getTitle();

        Resources resources = getResources();
        Drawable arrowRight = resources.getDrawable(R.drawable.ic_arrow_right);
        arrowRight.setColorFilter(resources.getColor(R.color.xebia_color), PorterDuff.Mode.SRC_IN);
        mText.setCompoundDrawablesWithIntrinsicBounds(null, null, arrowRight, null);

        String trackName = resources.getString(R.string.track_format, trackTitle, track.getCount());
        mText.setText(Html.fromHtml(trackName.replaceAll("<Devoxx>", "&lt;Devoxx&gt;")));

        mColor.setBackgroundColor(track.getColor());
    }
}
