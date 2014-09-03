package com.totemsoft.turnmeoff;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This activity holds some helper info for a user.
 */
public class HelpActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        webView = (WebView) findViewById(R.id.help_webview);
        webView.loadDataWithBaseURL("file:///android_assets/", getHtmlFromAsset(), "text/html", "UTF-8", null);
    }

    /**
     * Gets html content from the assets folder.
     */
    private String getHtmlFromAsset() {
        InputStream is;
        StringBuilder builder = new StringBuilder();
        String htmlString = null;
        try {
            is = getAssets().open(getString(R.string.help_html));
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                htmlString = builder.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return htmlString;
    }
}
