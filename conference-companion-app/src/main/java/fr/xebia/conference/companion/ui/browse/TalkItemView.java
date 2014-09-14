package fr.xebia.conference.companion.ui.browse;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.picasso.Picasso;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.model.Talk;

public class TalkItemView extends LinearLayout {

    @InjectView(R.id.talk_photo) ImageView mTalkPhoto;
    @InjectView(R.id.talk_category) TextView mTalkCategory;
    @InjectView(R.id.talk_title) TextView mTalkTitle;
    @InjectView(R.id.talk_subtitle) TextView mTalkSubTitle;
    @InjectView(R.id.talk_snippet) TextView mTalkSnippet;
    @InjectView(R.id.indicator_in_schedule) ImageView mInSchedule;
    @InjectView(R.id.info_box) ViewGroup mInfoBox;

    public TalkItemView(Context context) {
        super(context);
    }

    public TalkItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TalkItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
    }

    public void bind(Talk talk) {
        setTag(talk);
        Picasso.with(getContext()).load(R.drawable.devoxx_talk_template).into(mTalkPhoto);
        mTalkCategory.setText(talk.getType());
        mTalkTitle.setText(talk.getTitle());
        mTalkSubTitle.setText(talk.getRoom());
        mTalkSnippet.setText(talk.getSummary());
        mInSchedule.setVisibility(talk.isFavorite() ? VISIBLE : GONE);
        mInfoBox.setBackgroundColor(talk.getColor());
    }
}
