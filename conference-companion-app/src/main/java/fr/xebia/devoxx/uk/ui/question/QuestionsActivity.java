package fr.xebia.devoxx.uk.ui.question;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.devoxx.uk.BuildConfig;
import fr.xebia.devoxx.uk.R;
import fr.xebia.devoxx.uk.core.utils.Compatibility;

public class QuestionsActivity extends Activity {

    @InjectView(R.id.questions_webview) WebView questionsWebview;
    @InjectView(R.id.questions_progress) ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.questions_activity);
        ButterKnife.inject(this);

        questionsWebview.setVisibility(View.GONE);

        WebSettings settings = questionsWebview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setDomStorageEnabled(true);

        questionsWebview.setWebChromeClient(new WebChromeClient());
        questionsWebview.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                questionsWebview.setVisibility(View.VISIBLE);
            }
        });
        questionsWebview.loadUrl(BuildConfig.SLIDO_URL);

        if (Compatibility.isCompatible(Build.VERSION_CODES.LOLLIPOP)) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.questions_status_bar));
        }
    }

    @Override
    public void onBackPressed() {
        if (questionsWebview.canGoBack()) {
            questionsWebview.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
