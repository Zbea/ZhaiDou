
package com.zhaidou.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.callback.CallbackContext;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.fragments.LoginFragment;
import com.zhaidou.fragments.RegisterFragment;
import com.zhaidou.model.Article;
import com.zhaidou.model.User;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomProgressWebview;

import java.io.IOException;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;


public class HomeCompetitionActivity extends BaseActivity implements View.OnClickListener,
        RegisterFragment.RegisterOrLoginListener,
        LoginFragment.BackClickListener

{
    private CustomProgressWebview webView;
    private String title;
    private String url;
    private TextView tv_back;
    private TextView mTitleView;
    private int userId;
    private String token;
    private String nickName;

    private LoginFragment loginFragment;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_competition);

        mSharedPreferences = getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        userId = mSharedPreferences.getInt("userId", -1);
        token = mSharedPreferences.getString("token", null);
        nickName = mSharedPreferences.getString("nickName", "");

        tv_back = (TextView) findViewById(R.id.tv_back);
        mTitleView = (TextView) findViewById(R.id.tv_title);
        mChildContainer = (FrameLayout) findViewById(R.id.fl_child_container);

        if (!NetworkUtils.isNetworkAvailable(this))
        {
            Toast.makeText(this, "抱歉，请检查网络", Toast.LENGTH_SHORT).show();
        }

        loginFragment = LoginFragment.newInstance("", "");
        loginFragment.setRegisterOrLoginListener(this);
        loginFragment.setBackClickListener(this);
        tv_back.setOnClickListener(this);

        /* WebView Settings */
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
                getDeviceId();
                if ("mobile://login?false".equalsIgnoreCase(url))
                {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fl_child_container, loginFragment)
                            .addToBackStack(null).commit();
                    mChildContainer.setVisibility(View.VISIBLE);
                    return true;
                } else if (url.contains("taobao"))
                {
                    Intent intent = new Intent();
                    intent.putExtra("url", url);
                    intent.setClass(HomeCompetitionActivity.this, WebViewActivity.class);
                    HomeCompetitionActivity.this.startActivity(intent);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                view.loadUrl("javascript:!function(){" +

                        "s=document.createElement('style');s.innerHTML="

                        + "\"@font-face{font-family:FZLTXHK;src:url('**injection**/FZLTXHK.TTF');}*{font-family:FZLTXHK !important;}\";"

                        + "document.getElementsByTagName('head')[0].appendChild(s);" +

                        "document.getElementsByTagName('body')[0].style.fontFamily = \"FZLTXHK\";}()");
                if (!TextUtils.isEmpty(token))
                {
                    webView.loadUrl("javascript:ReceiveUserInfo(" + userId + ", '" + token + "'," + getDeviceId() + ",'" + nickName + "')");
                } else
                {
                    webView.loadUrl("javascript:ReceiveUserInfo(" + userId + ", '" + "" + "'," + getDeviceId() + ",'" + nickName + "')");
                }
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
                if (newProgress == 100)
                {
                    webView.progressBar.setVisibility(View.GONE);

                } else
                {
                    webView.progressBar.setVisibility(View.VISIBLE);
                    webView.progressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });

        url = getIntent().getStringExtra("url");
        webView.loadUrl(url + "?open=app");
        this.setTitle("");
        title = getIntent().getStringExtra("title");
        if (!TextUtils.isEmpty(title))
        {
            mTitleView.setText(title);
        }

    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.tv_back:
                if (webView.canGoBack())
                {
                    webView.goBack();
                    return;
                }
                finish();
                break;
        }
    }

    @Override
    public void onRegisterOrLoginSuccess(User user, Fragment fragment)
    {
        webView.loadUrl("javascript:ReceiveUserInfo(" + user.getId() + ", '" + user.getAuthentication_token() + "'," + getDeviceId() + ",'" + user.getNickName() + "')");
                         super.onRegisterOrLoginSuccess(user, fragment);
    }


    public String getDeviceId()
    {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    @Override
    public void onBackClick(Fragment fragment)
    {
        webView.reload();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        CallbackContext.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("天天刮奖");
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("天天刮奖");
        MobclickAgent.onPause(this);
    }
}