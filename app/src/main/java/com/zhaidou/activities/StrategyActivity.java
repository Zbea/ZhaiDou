package com.zhaidou.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.zhaidou.R;

public class StrategyActivity extends Activity {

    private WebView webView;
    private ProgressDialog loading;
    private Button livingRoomButton;
    private Button entirePartButton;

    private static final String LIVING_ROOM_TAG = "1";
    private static final String ENTIRE_PART_TAG = "2";

    private static final String LIVING_ROOM_URL = "http://buy.zhaidou.com/?zdclient=ios&tag=006&count=10";
    private static final String ENTIRE_PART_URL = "http://buy.zhaidou.com/gl.html";

    private Button lastButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strategy);

        webView = (WebView) findViewById(R.id.strategyView);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = new Intent();
                intent.putExtra("url", url);
                intent.setClass(StrategyActivity.this, WebViewActivity.class);
                StrategyActivity.this.startActivity(intent);
                return true;
            }

            public void onPageFinished(WebView view, String url) {
                loading.hide();
            }

        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        webView.loadUrl(LIVING_ROOM_URL);

        livingRoomButton = (Button) findViewById(R.id.living_room);
        entirePartButton = (Button) findViewById(R.id.entire_part);
        livingRoomButton.setSelected(true);
        lastButton = livingRoomButton;


        setTitle("全新加居生活");

        loading = ProgressDialog.show(this, "", "正在努力加载中...", true);

    }


//    class MyWebViewClient extends WebViewClient {
//
//        @Override
//        public boolean shouldOverLoadingUrl() {
//
//            Intent intent = new Intent();
//            intent.putExtra("url", url);
//            intent.setClass(StrategyActivity.this, WebViewActivity.class);
//            StrategyActivity.this.startActivity(intent);
//            return true;
//        }
//
//    }

    public void onClick_Event(View view) {
        Button btn = (Button) view;
        if (lastButton != null) {
            lastButton.setSelected(false);
        }
        String tag = (String) btn.getTag();

        if (LIVING_ROOM_TAG.equals(tag)) {
            webView.loadUrl(LIVING_ROOM_URL);
        } else if (ENTIRE_PART_TAG.equals(tag)) {
            webView.loadUrl(ENTIRE_PART_URL);
        }

        lastButton = btn;
        lastButton.setSelected(true);
    }

    @Override
    public void onBackPressed() {

        Log.v("Debug Info", "Current url: " + webView.getUrl());

        if (LIVING_ROOM_URL.equals(webView.getUrl()) || ENTIRE_PART_URL.equals(webView.getUrl())) {
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
