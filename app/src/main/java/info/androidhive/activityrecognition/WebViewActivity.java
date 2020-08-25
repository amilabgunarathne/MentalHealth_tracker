package info.androidhive.activityrecognition;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebViewActivity extends AppCompatActivity {
    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Intent intent = getIntent();
        String imei = intent.getStringExtra("IMEI");
        Log.e("IMEI","a"+imei);
        webView = findViewById(R.id.webView);
        String url = "https://google.com";
        WebSettings webSettings = webView.getSettings();
        webSettings.setBuiltInZoomControls(true);
//     webViewActivity = findViewById(R.id.webViewActivity);
        //WebViewActivity browser = findViewById(R.id.webViewActivity);
//        browser.setWebViewClient(new MyBrowser());
        webView.setWebViewClient(new Callback());
        webView.loadUrl(url);
    }
    private class Callback extends WebViewClient{  //HERE IS THE MAIN CHANGE.

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return (false);
        }

    }
}
