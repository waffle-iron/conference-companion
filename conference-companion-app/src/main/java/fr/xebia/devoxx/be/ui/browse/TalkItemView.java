package fr.xebia.devoxx.be.ui.browse;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.devoxx.be.R;
import fr.xebia.devoxx.be.model.Talk;

public class TalkItemView extends FrameLayout {

    @InjectView(R.id.talk_photo) ImageView mTalkPhoto;
    @InjectView(R.id.talk_category) TextView mTalkCategory;
    @InjectView(R.id.talk_title) TextView mTalkTitle;
    @InjectView(R.id.talk_subtitle) TextView mTalkSubTitle;
    @InjectView(R.id.talk_snippet) TextView mTalkSnippet;
    @InjectView(R.id.indicator_in_schedule) ImageView mInSchedule;
    @InjectView(R.id.info_box) ViewGroup mInfoBox;
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;

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
        mGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                if (mTalkSnippet.getLineCount() > 3) {
                    int lineEndIndex = mTalkSnippet.getLayout().getLineEnd(2);
                    String text = mTalkSnippet.getText().subSequence(0, lineEndIndex - 3) + "...";
                    mTalkSnippet.setText(text);
                }

            }
        };
    }


    public void bind(Talk talk, boolean conferenceEnded) {
        setTag(talk);
        setBackgroundColor(talk.getColor());
        Picasso.with(getContext()).load(getItemBackgroundResource(talk))
                .fit()
                .config(Bitmap.Config.RGB_565)
                .centerCrop()
                .into(mTalkPhoto);
        mTalkCategory.setText(talk.getType());
        mTalkTitle.setText(talk.getTitle());

        if (!conferenceEnded && System.currentTimeMillis() > talk.getToUtcTime()) {
            mTalkSubTitle.setText(getResources().getString(R.string.ended));
        } else {
            mTalkSubTitle.setText(String.format("%s | %s | %s", talk.getDay(), talk.getPeriod(), talk.getRoom()));
        }

        mTalkSnippet.setText(Html.fromHtml(talk.getSummary()));
        mInSchedule.setVisibility(talk.isFavorite() ? VISIBLE : GONE);
        mInfoBox.setBackgroundColor(talk.getColor());

        getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
    }

    private int getItemBackgroundResource(Talk talk) {
        return getResources().getIdentifier("devoxx_talk_template_" + talk.getPosition() % 19, "drawable", getContext().getPackageName());
    }
}
