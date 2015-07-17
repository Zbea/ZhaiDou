package com.zhaidou.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhaidou.R;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.utils.NetworkUtils;

/**
 * Created by roy on 15/7/17.
 */
public class HomePTActivity extends Activity
{

    private TextView tv_back;
    private TextView mTitleView;
    private WebView webView;
    private String title;
    private String url;
    private Dialog mDialog;

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
        if(NetworkUtils.isNetworkAvailable(this))
        {
            mDialog= CustomLoadingDialog.setLoadingDialog(this, "loading");
        }
        else
        {
            Toast.makeText(this, "抱歉，请检查网络", Toast.LENGTH_SHORT).show();
        }

        title = getIntent().getStringExtra("title");
        url = getIntent().getStringExtra("url");

        tv_back=(TextView)findViewById(R.id.tv_back);
        tv_back.setOnClickListener(onClickListener);
        mTitleView=(TextView)findViewById(R.id.tv_title);
        webView = (WebView) findViewById(R.id.detailView);

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
                if (mDialog!=null)
                {
                    mDialog.dismiss();
                }
                super.onPageFinished(view, url);
            }
        });

        url=url+"?open=app";
        Log.i("zhaidou-------->","url:"+url);
        webView.loadUrl(url);
        mTitleView.setText(title);

    }

}
