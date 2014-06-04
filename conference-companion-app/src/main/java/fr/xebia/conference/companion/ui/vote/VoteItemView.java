package fr.xebia.conference.companion.ui.vote;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.model.TalkVote;
import fr.xebia.conference.companion.model.TrackResource;

public class VoteItemView extends RelativeLayout {

    @InjectView(R.id.track_image) ImageView mTrackImage;
    @InjectView(R.id.talk_title) TextView mTalkTitle;
    @InjectView(R.id.talk_note) RatingBar mTalkNote;

    public VoteItemView(Context context) {
        super(context);
    }

    public VoteItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VoteItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this, this);
    }

    public void bind(TalkVote talkVote) {
        Resources resources = getResources();
        String track = talkVote.getTrack();
        Drawable trackDrawable = resources.getDrawable(TrackResource.getIconForTrack(track)).mutate();
        trackDrawable.setColorFilter(resources.getColor(TrackResource.getColorResForTrack(track)), PorterDuff.Mode.SRC_IN);
        mTrackImage.setImageDrawable(trackDrawable);
        mTalkTitle.setText(talkVote.getTitle());
        mTalkNote.setRating(talkVote.getNote());
    }
}
