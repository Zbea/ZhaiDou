package com.zhaidou.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaidou.R;

public class StrategyActivity extends Activity {

    private WebView webView;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strategy);

        webView = (WebView)findViewById(R.id.strategyView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.loadUrl("http://www.zhaidou.com/m.html");
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                loading.hide();
            }
        });

        setTitle("全新加居生活");

        loading = ProgressDialog.show(this, "", "正在努力加载中...", true);

    }

    @Override
    public void onBackPressed() {

        Log.v("Debug Info", "Current url: " + webView.getUrl());

        if ("http://www.zhaidou.com/m.html".equals(webView.getUrl())) {
            super.onBackPressed();
        }
        webView.goBack();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.strategy, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
