
package com.zhaidou.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
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

import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.fragments.RegisterFragment;
import com.zhaidou.model.Article;
import com.zhaidou.model.User;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomProgressWebview;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;

public class ItemDetailActivity extends BaseActivity implements View.OnClickListener,
        RegisterFragment.RegisterOrLoginListener

{

    private CustomProgressWebview webView;

    /* 以下代码应该封装为一个对象 */
    private String title;
    private String coverUrl;
    private String url;
    private TextView tv_back;
    private ImageView iv_share, mHeaderView, iv_shadow;
    private FrameLayout mChildContainer;
    private TextView mTitleView, mHeaderText;
    private Article article;
    private String is_new;
    private String is_id;

    private int screenWidth;

    private int userId;
    private String token;
    private String nickName;
    private boolean isShowHeader;
    private RelativeLayout imageView;

    private SharedPreferences mSharedPreferences;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        mContext=this;
        WindowManager wm = getWindowManager();
        screenWidth = wm.getDefaultDisplay().getWidth();

        from = getIntent().getStringExtra("from");
        article = (Article) getIntent().getSerializableExtra("article");

        mSharedPreferences = getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        userId = mSharedPreferences.getInt("userId", -1);
        token = mSharedPreferences.getString("token", null);
        nickName = mSharedPreferences.getString("nickName", "");
        tv_back = (TextView) findViewById(R.id.tv_back);
        iv_share = (ImageView) findViewById(R.id.iv_share);
        iv_shadow = (ImageView) findViewById(R.id.iv_shadow);
        mChildContainer = (FrameLayout) findViewById(R.id.fl_child_container);
        mTitleView = (TextView) findViewById(R.id.tv_title);

        mHeaderView = (ImageView) findViewById(R.id.iv_header);
        mHeaderView.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenWidth * 316 / 722));

        mHeaderText = (TextView) findViewById(R.id.tv_msg);
        imageView = (RelativeLayout) findViewById(R.id.imageView);

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


        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                getDeviceId();
                if ("mobile://login?false".equalsIgnoreCase(url)) {
                    Intent intent = new Intent(ItemDetailActivity.this, LoginActivity.class);
                    intent.setFlags(2);
                    startActivityForResult(intent, 10000);
                    return true;
                } else if (url.contains("taobao")) {
                    Intent intent = new Intent();
                    intent.putExtra("url", url);
                    intent.setClass(ItemDetailActivity.this, WebViewActivity.class);
                    ItemDetailActivity.this.startActivity(intent);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:!function(){" +

                        "s=document.createElement('style');s.innerHTML="

                        + "\"@font-face{font-family:FZLTXHK;src:url('**injection**/FZLTXHK.TTF');}*{font-family:FZLTXHK !important;}\";"

                        + "document.getElementsByTagName('head')[0].appendChild(s);" +

                        "document.getElementsByTagName('body')[0].style.fontFamily = \"FZLTXHK\";}()");
                if ("lottery".equalsIgnoreCase(from)) {
                    if (!TextUtils.isEmpty(token)) {
                        webView.loadUrl("javascript:ReceiveUserInfo(" + userId + ", '" + token + "'," + getDeviceId() + ",'" + nickName + "')");
                    } else {
                        webView.loadUrl("javascript:ReceiveUserInfo(" + userId + ", '" + "" + "'," + getDeviceId() + ",'" + nickName + "')");
                    }

                } else if ("product".equalsIgnoreCase(from)) {
                    if (!TextUtils.isEmpty(token))
                        webView.loadUrl("javascript:ReceiveUserInfo(" + userId + ", '" + token + "')");
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

        url = getIntent().getStringExtra("url");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("ZhaidouVesion", getResources().getString(R.string.app_versionName));
        headers.put("SECAuthorization", token);
        webView.loadUrl(url + "?open=app", headers);


        this.setTitle("");

        title = getIntent().getStringExtra("title");
        coverUrl = getIntent().getStringExtra("cover_url");
        isShowHeader = getIntent().getBooleanExtra("show_header", false);

        if (!TextUtils.isEmpty(title)) {
            mTitleView.setText(title);
            mTitleView.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(coverUrl)&&isShowHeader) {
            ToolUtils.setImageCacheUrl(coverUrl, mHeaderView);
            mTitleView.setVisibility(View.GONE);
            mHeaderText.setText(title);
            imageView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                if (webView.canGoBack()) {
                    if ("product".equalsIgnoreCase(from)) {
                        iv_share.setVisibility(View.VISIBLE);
                    }
                    webView.goBack();
                    return;
                }
                finish();
                break;
            case R.id.iv_share:
                System.out.println("ItemDetailActivity.onClick");
                doShare();
                break;
        }
    }

    private void doShare() {
        DialogUtils mDialogUtils=new DialogUtils(this);
        System.out.println("ItemDetailActivity.doShare-->"+title+"---"+url+"----"+coverUrl);
        mDialogUtils.showShareDialog(title,title+"  "+url,coverUrl,url,new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> stringObjectHashMap) {
                System.out.println("ItemDetailActivity.onComplete");
                Toast.makeText(ItemDetailActivity.this,mContext.getString(R.string.share_completed),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                System.out.println("ItemDetailActivity.onError"+"--------"+i+"------"+throwable.getMessage());
                Toast.makeText(ItemDetailActivity.this,mContext.getString(R.string.share_error),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel(Platform platform, int i) {
                System.out.println("ItemDetailActivity.onCancel");
                Toast.makeText(ItemDetailActivity.this,mContext.getString(R.string.share_cancel),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRegisterOrLoginSuccess(User user, Fragment fragment) {

        if ("lottery".equalsIgnoreCase(from)) {
            Log.i("onRegisterOrLoginSuccess--lottery----------->", "onPageFinished" + "------" + token);
            webView.loadUrl("javascript:ReceiveUserInfo(" + user.getId() + ", '" + user.getAuthentication_token() + "'," + getDeviceId() + ",'" + user.getNickName() + "')");
        } else if ("product".equalsIgnoreCase(from)) {
            webView.loadUrl("javascript:ReceiveUserInfo(" + user.getId() + ", '" + user.getAuthentication_token() + "')");
        }
        super.onRegisterOrLoginSuccess(user, fragment);
    }


    public String getDeviceId() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode){
            case 2000:
                token = mSharedPreferences.getString("token", null);
                System.out.println("HomeCompetitionActivity.onActivityResult---------->"+token);
                webView.reload();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("ItemDetailActivity");
        MobclickAgent.onResume(this);
        System.out.println("ItemDetailActivity.onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("ItemDetailActivity");
        MobclickAgent.onPause(this);
    }
}