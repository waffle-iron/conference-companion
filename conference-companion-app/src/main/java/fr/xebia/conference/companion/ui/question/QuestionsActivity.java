package fr.xebia.conference.companion.ui.question;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.conference.companion.R;

public class QuestionsActivity extends Activity {

    @InjectView(R.id.questions_webview) WebView questionsWebview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.questions_activity);
        ButterKnife.inject(this);
        WebSettings settings = questionsWebview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSaveFormData(false);
        settings.setSavePassword(false);
        settings.setAppCacheEnabled(true);
        settings.setDomStorageEnabled(true);
        questionsWebview.setWebChromeClient(new WebChromeClient());
        questionsWebview.setWebViewClient(new WebViewClient());
        questionsWebview.loadUrl("https://app.sli.do/event/3cpvcsvp/ask");
    }

}
