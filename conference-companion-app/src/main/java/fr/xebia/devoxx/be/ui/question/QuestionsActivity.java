package fr.xebia.devoxx.be.ui.question;

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
import fr.xebia.devoxx.be.BuildConfig;
import fr.xebia.devoxx.be.R;
import fr.xebia.devoxx.be.core.utils.Compatibility;

public class QuestionsActivity extends Activity {

    public static final String EXTRA_ROOM = "fr.xebia.devoxx.be.EXTRA_ROOM";

    @InjectView(R.id.questions_webview) WebView questionsWebview;
    @InjectView(R.id.questions_progress) ProgressBar progressBar;

    private static final String ROOM_SELECTION_SCRIPT = "javascript:setInterval(function () {var room = '%s' ;\n" +
            "var choices = document.querySelectorAll('#sections-chooser li');\n" +
            " if(choices.length > 1){ clearInterval(); }" +
            "for(var i = 0; i<choices.length; i++) {\n" +
            "  if(choices[i].querySelector('.title').innerHTML.trim().toUpperCase() === room){\n" +
            "    choices[i].click();\n" +
            "  }\n" +
            "}}, 200)";

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
        questionsWebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                questionsWebview.setVisibility(View.VISIBLE);
                questionsWebview.loadUrl(String.format(ROOM_SELECTION_SCRIPT, getIntent().getStringExtra(EXTRA_ROOM).trim().toUpperCase()));
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
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
