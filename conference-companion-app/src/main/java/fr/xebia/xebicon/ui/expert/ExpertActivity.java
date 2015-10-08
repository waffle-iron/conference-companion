package fr.xebia.xebicon.ui.expert;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import butterknife.InjectView;
import fr.xebia.xebicon.R;
import fr.xebia.xebicon.core.activity.NavigationActivity;

public class ExpertActivity extends NavigationActivity {

    @InjectView(R.id.content) TextView textView;

    public ExpertActivity() {
        super(R.layout.activity_expert);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.nav_experts);

        textView.setText(Html.fromHtml(getString(R.string.experts_content)));
    }

    @Override
    protected int getNavId() {
        return R.id.nav_experts;
    }
}
