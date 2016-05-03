package com.zhaidou.activities;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.view.CustomProgressWebview;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by roy on 15/7/17.
 */
public class HomePTActivity extends Activity
{
    private TextView tv_back;
    private TextView mTitleView;
    private CustomProgressWebview webView;
    private String title;
    private String url;
    private int flags;

    /**
     * 点击事件监听
     */
    private View.OnClickListener onClickListener=new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            if (view==tv_back)
            {
                if(webView.canGoBack())
                {
                    webView.goBack();
                    return;
                }
                finish();

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_home_pt);

        initView();
    }

    /**
     * 初始化View
     */
    private void initView()
    {


        if(!NetworkUtils.isNetworkAvailable(this))
        {
            Toast.makeText(this, "抱歉，请检查网络", Toast.LENGTH_SHORT).show();
        }

        title = getIntent().getStringExtra("title");
        url = getIntent().getStringExtra("url");
        flags=getIntent().getFlags();

        tv_back=(TextView)findViewById(R.id.tv_back);
        tv_back.setOnClickListener(onClickListener);
        mTitleView=(TextView)findViewById(R.id.tv_title);
        webView = (CustomProgressWebview) findViewById(R.id.detailView);
        webView.setBackgroundColor(Color.parseColor("#00000000"));
        webView.setBackgroundResource(R.drawable.btn_click_selector);

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
            public void onPageFinished(WebView view, String url)
            {
                view.loadUrl("javascript:!function(){" +

                        "s=document.createElement('style');s.innerHTML="

                        + "\"@font-face{font-family:FZLTXHK;src:url('**injection**/FZLTXHK.TTF');}*{font-family:FZLTXHK !important;}\";"

                        + "document.getElementsByTagName('head')[0].appendChild(s);" +

                        "document.getElementsByTagName('body')[0].style.fontFamily = \"FZLTXHK\";}()");
                super.onPageFinished(view, url);
            }
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                WebResourceResponse response = super.shouldInterceptRequest(view, url);

                if (url != null && url.contains("**injection**/")) {
                    String assertPath = url.substring(url.indexOf("**injection**/") + "**injection**/".length(), url.length());

                    try {

                        response = new WebResourceResponse("application/x-font-ttf",

                                "UTF8", getAssets().open(assertPath)

                        );

                    } catch (IOException e) {

                        e.printStackTrace();

                    }

                }

                return response;
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

     //   url=url+"?open=app";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("ZhaidouVesion", getResources().getString(R.string.app_versionName));
        webView.loadUrl(url);
        mTitleView.setText(title);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack(); // goBack()表示返回WebView的上一页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("拼贴大赛");
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("拼贴大赛");
        MobclickAgent.onPause(this);
    }
}
