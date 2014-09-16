package fr.xebia.conference.companion.ui.speaker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.picasso.Picasso;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.model.Speaker;

public class SpeakerItemView extends LinearLayout {

    @InjectView(R.id.speaker_image) ImageView mSpeakerImage;
    @InjectView(R.id.speaker_name) TextView mSpeakerName;
    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;

    public SpeakerItemView(Context context) {
        super(context);
    }

    public SpeakerItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpeakerItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this, this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mSpeakerImage.getLayoutParams().height = getWidth() - mSpeakerName.getHeight();
                mSpeakerImage.invalidate();
            }
        };
        mSpeakerImage.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
    }

    public void bind(final Speaker speaker) {
        mSpeakerName.setText(String.format("%s %s", speaker.getFirstName(), speaker.getLastName()));
        post(new Runnable() {
            @Override
            public void run() {
                int measuredWidth = getMeasuredWidth();
                int measuredHeight = measuredWidth - mSpeakerName.getMeasuredHeight();
                mSpeakerImage.getLayoutParams().height = measuredHeight;
                mSpeakerImage.invalidate();
                Picasso.with(getContext()).load(speaker.getImageURL())
                        .placeholder(R.drawable.speaker_placeholder)
                        .resize(measuredWidth, measuredHeight)
                        .centerCrop()
                        .into(mSpeakerImage);
            }

        });

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mSpeakerImage.getViewTreeObserver().removeGlobalOnLayoutListener(globalLayoutListener);
    }
}
