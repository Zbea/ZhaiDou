package com.zhaidou.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.zhaidou.R;

public class WebViewActivity extends Activity implements View.OnClickListener{

    private String url;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        findViewById(R.id.ll_back).setOnClickListener(this);

        // Set Icon
//        getActionBar().setIcon(R.drawable.buy_close);
//        getActionBar().setHomeButtonEnabled(true);

        webView = (WebView) findViewById(R.id.webView);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i("shouldOverrideUrlLoading----->", url);
                if (url.startsWith("taobao://"))
                    return true;
                view.loadUrl(url);
                return true;
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);

        String url = getIntent().getStringExtra("url");

        webView.loadUrl(url);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.web_view, menu);
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

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ll_back:
                finish();
                break;
        }
    }
}
