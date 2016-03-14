package com.zhaidou.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.view.CustomProgressWebview;

import java.util.HashMap;
import java.util.Map;

public class WebViewActivity extends Activity implements View.OnClickListener{

    private String url;
    private CustomProgressWebview webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        findViewById(R.id.ll_back).setOnClickListener(this);

        webView = (CustomProgressWebview) findViewById(R.id.webView);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("taobao://"))
                    return true;
                view.loadUrl(url);
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient()
        {
            @Override
            public void onProgressChanged(WebView view, int newProgress)
            {
                if (newProgress==100)
                {
                    webView.progressBar.setVisibility(View.GONE);

                }
                else
                {
                    webView.progressBar.setVisibility(View.VISIBLE);
                    webView.progressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });

        webView.setDownloadListener(new DownloadListener()
        {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                // 监听下载功能，当用户点击下载链接的时候，直接调用系统的浏览器来下载
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        String url = getIntent().getStringExtra("url");

        Map<String,String> headers=new HashMap<String, String>();
        headers.put("ZhaidouVesion", getResources().getString(R.string.app_versionName));
        webView.loadUrl(url,headers);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ll_back:
                finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("WebViewActivity");
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("WebViewActivity");
        MobclickAgent.onPause(this);
    }
}
