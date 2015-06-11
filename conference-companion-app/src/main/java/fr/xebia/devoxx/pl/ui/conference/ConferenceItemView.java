package fr.xebia.devoxx.pl.ui.conference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import fr.xebia.devoxx.pl.R;
import fr.xebia.devoxx.pl.model.Conference;

public class ConferenceItemView extends TextView implements Target {

    public static final int DEVOXX_CONF_ID = 14;

    public ConferenceItemView(Context context) {
        super(context);
    }

    public ConferenceItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ConferenceItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void bind(Conference conference) {
        setText(conference.getName());
        if (conference.getId() == DEVOXX_CONF_ID) {
            Picasso.with(getContext()).load(R.drawable.devoxx_2014_logo)
                    .resizeDimen(R.dimen.conference_icon, R.dimen.conference_icon).into(this);
        } else {
            Picasso.with(getContext()).load(conference.getLogoUrl())
                    .resizeDimen(R.dimen.conference_icon, R.dimen.conference_icon).into(this);
        }
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
        setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(getResources(), bitmap), null, null, null);
    }

    @Override
    public void onBitmapFailed(Drawable drawable) {

    }

    @Override
    public void onPrepareLoad(Drawable drawable) {

    }
}
