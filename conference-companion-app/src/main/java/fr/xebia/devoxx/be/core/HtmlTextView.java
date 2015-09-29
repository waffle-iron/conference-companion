package fr.xebia.devoxx.be.core;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import static android.text.Html.fromHtml;

public class HtmlTextView extends TextView {
    public HtmlTextView(Context context) {
        super(context);
    }

    public HtmlTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HtmlTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(fromHtml(text.toString()), type);
    }
}
