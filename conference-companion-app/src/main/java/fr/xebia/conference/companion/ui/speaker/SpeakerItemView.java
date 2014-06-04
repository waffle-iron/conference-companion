package fr.xebia.conference.companion.ui.speaker;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.picasso.Picasso;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.core.transform.CircleTransform;
import fr.xebia.conference.companion.model.Speaker;

import static android.text.Html.fromHtml;

public class SpeakerItemView extends LinearLayout {

    @InjectView(R.id.speaker_image) ImageView mSpeakerImage;
    @InjectView(R.id.speaker_name) TextView mSpeakerName;

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

    public void bind(Speaker speaker) {
        String company = speaker.getCompany();
        if (company == null) {
            mSpeakerName.setText(fromHtml(getResources().getString(R.string.speaker_format_without_company, speaker.getFirstName(),
                    speaker.getLastName())));
        } else {
            mSpeakerName.setText(fromHtml(getResources().getString(R.string.speaker_format, speaker.getFirstName(),
                    speaker.getLastName(), company)));
        }

        Picasso.with(getContext()).load(speaker.getImageURL())
                .placeholder(R.drawable.ic_default_gravatar)
                .transform(CircleTransform.getInstance())
                .noFade()
                .fit()
                .centerCrop()
                .into(mSpeakerImage);
    }
}
