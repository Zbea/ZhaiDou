
package com.zhaidou.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.view.CustomProgressWebview;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;

public class ArticleWebViewActivity extends BaseActivity implements View.OnClickListener

{

    private CustomProgressWebview webView;
    private String title;
    private String url,imageUrl;
    private TextView tv_back,mTitleView;
    private ImageView iv_share;
    private Context mContext;
    private boolean isShowShare,isShowTitle;
    private DialogUtils mDialogUtils;
    private final int UPDATE_SHARE_TOAST=0;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_SHARE_TOAST:
                    mDialogUtils.dismiss();
                    String result = (String) msg.obj;
                    Toast.makeText(mContext,result,Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_share);
        mContext=this;
        tv_back = (TextView) findViewById(R.id.ll_back);
        iv_share = (ImageView) findViewById(R.id.share_iv);
        mTitleView = (TextView) findViewById(R.id.title_tv);

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "抱歉，请检查网络", Toast.LENGTH_SHORT).show();
        }

        tv_back.setOnClickListener(this);
        iv_share.setOnClickListener(this);

        webView = (CustomProgressWebview) findViewById(R.id.detailView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);


        title = getIntent().getStringExtra("title");
        imageUrl = getIntent().getStringExtra("imageUrl");
        url = getIntent().getStringExtra("url");
        isShowShare = getIntent().getBooleanExtra("show_share", true);
        isShowTitle = getIntent().getBooleanExtra("show_title", false);

        mTitleView.setText(title);
        if (isShowShare==false)
        {
            iv_share.setVisibility(View.GONE);
        }
        if (isShowTitle==false)
        {
            mTitleView.setVisibility(View.GONE);
        }

        webView.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onPageFinished(WebView view, String url) {
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

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    webView.progressBar.setVisibility(View.GONE);

                } else {
                    webView.progressBar.setVisibility(View.VISIBLE);
                    webView.progressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("ZhaidouVesion", getResources().getString(R.string.app_versionName));
        webView.loadUrl(url + "?open=app", headers);
        mDialogUtils = new DialogUtils(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_back:
                if (webView.canGoBack()) {
                    webView.goBack();
                    return;
                }
                finish();
                break;
            case R.id.share_iv:
                doShare();
                break;
        }
    }

    private void doShare() {
        mDialogUtils.showShareDialog(title, title + "  " + url, imageUrl, url, new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> stringObjectHashMap) {
                Message message =mHandler.obtainMessage(UPDATE_SHARE_TOAST, mContext.getString(R.string.share_completed));
                mHandler.sendMessage(message);
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                Message message = mHandler.obtainMessage(UPDATE_SHARE_TOAST, mContext.getString(R.string.share_error));
                mHandler.sendMessage(message);
            }

            @Override
            public void onCancel(Platform platform, int i) {
                Message message = mHandler.obtainMessage(UPDATE_SHARE_TOAST, mContext.getString(R.string.share_cancel));
                mHandler.sendMessage(message);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("ArticleWebViewActivity");
        MobclickAgent.onResume(this);
        mDialogUtils.dismiss();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("ArticleWebViewActivity");
        MobclickAgent.onPause(this);
    }
}