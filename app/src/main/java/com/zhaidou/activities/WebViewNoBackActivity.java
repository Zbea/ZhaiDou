package com.zhaidou.activities;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomProgressWebview;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by roy on 16/7/26.
 */
public class WebViewNoBackActivity extends BaseActivity
{

    private CustomProgressWebview webView;
    private String title;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_webview_no_back);

        initView();
    }

    private void initView()
    {

        if(!NetworkUtils.isNetworkAvailable(this))
        {
            Toast.makeText(this, "抱歉，请检查网络", Toast.LENGTH_SHORT).show();
        }

        title = getIntent().getStringExtra("title");
        url = getIntent().getStringExtra("url");

        webView = (CustomProgressWebview) findViewById(R.id.detailView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        webView.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                ToolUtils.setLog(url);
                if (url.equalsIgnoreCase(ZhaiDou.HOME_BASE_WAP_URL)|url.contains("source=android"))
                    finish();
                return true;
            }
        });

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("ZhaidouVesion", getResources().getString(R.string.app_versionName));
        webView.loadUrl(url,headers);

    }


    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(title);
    }
    public void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd(title);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK & webView.canGoBack())
        {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
