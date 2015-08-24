
package com.zhaidou.activities;

import android.app.Activity;
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
import android.view.ViewGroup;
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
import com.android.volley.AuthFailureError;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.fragments.LoginFragment;
import com.zhaidou.fragments.RegisterFragment;
import com.zhaidou.model.Article;
import com.zhaidou.model.User;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.ToolUtils;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.zhaidou.view.CustomProgressWebview;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class ItemDetailActivity extends BaseActivity implements View.OnClickListener,
        RegisterFragment.RegisterOrLoginListener,
        LoginFragment.BackClickListener

{

    private CustomProgressWebview webView;

    /* 以下代码应该封装为一个对象 */
    private String title;
    private String coverUrl;
    private String url;
    private TextView tv_back;
    private ImageView iv_share, mHeaderView;
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

    //    private String from;
    private LoginFragment loginFragment;
    private RegisterFragment registerFragment;

    private SharedPreferences mSharedPreferences;
    //    private Dialog mDialog;
    public static RefreshNotifyListener refreshNotifyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        WindowManager wm = getWindowManager();
        screenWidth = wm.getDefaultDisplay().getWidth();

        from = getIntent().getStringExtra("from");
        article = (Article) getIntent().getSerializableExtra("article");
        if (article != null) {
            is_id = String.valueOf(article.getId());
            is_new = article.getIs_new();

            if (is_new.equals("true")) {
                SharedPreferences sharedPreferences = getSharedPreferences(is_id, 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("is_new", true);
                editor.commit();
                Intent intent = new Intent(ZhaiDou.IntentRefreshListTag);
                sendBroadcast(intent);
            }

        }

        mSharedPreferences = getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        userId = mSharedPreferences.getInt("userId", -1);
        token = mSharedPreferences.getString("token", null);
        nickName = mSharedPreferences.getString("nickName", "");
        tv_back = (TextView) findViewById(R.id.tv_back);
        iv_share = (ImageView) findViewById(R.id.iv_share);
        mChildContainer = (FrameLayout) findViewById(R.id.fl_child_container);
        mTitleView = (TextView) findViewById(R.id.tv_title);

        mHeaderView = (ImageView) findViewById(R.id.iv_header);
        mHeaderView.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenWidth * 316 / 722));

        mHeaderText = (TextView) findViewById(R.id.tv_msg);
        imageView = (RelativeLayout) findViewById(R.id.imageView);

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "抱歉，请检查网络", Toast.LENGTH_SHORT).show();
        }

        loginFragment = LoginFragment.newInstance("", "");
        loginFragment.setRegisterOrLoginListener(this);
        loginFragment.setBackClickListener(this);
        tv_back.setOnClickListener(this);
        iv_share.setOnClickListener(this);


        //String postId = getIntent().getStringExtra("id");

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

        if (!"lottery".equalsIgnoreCase(from))
            webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.setVerticalScrollBarEnabled(false);
        webView.setVerticalScrollbarOverlay(false);
        webView.setHorizontalScrollbarOverlay(false);
        webView.setHorizontalFadingEdgeEnabled(false);
        webView.setInitialScale(1);
        webView.setWebChromeClient(new WebChromeClient() {

        });
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i("shouldOverrideUrlLoading---------------->", url);
                getDeviceId();
                if ("mobile://login?false".equalsIgnoreCase(url)) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fl_child_container, loginFragment)
                            .addToBackStack(null).commit();
                    mChildContainer.setVisibility(View.VISIBLE);
//                    }
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
        headers.put("ZhaidouVesion", "2.2");
        headers.put("SECAuthorization",token);
        webView.loadUrl(url + "?open=app", headers);


        this.setTitle("");

        title = getIntent().getStringExtra("title");
        coverUrl = getIntent().getStringExtra("cover_url");

        if (!TextUtils.isEmpty(title)) {
            mTitleView.setText(title);
        }
        if (!TextUtils.isEmpty(coverUrl)) {
            ToolUtils.setImageCacheUrl(coverUrl, mHeaderView);
            mTitleView.setVisibility(View.GONE);
            mHeaderText.setText(title);
            imageView.setVisibility(View.VISIBLE);
        }
        if ("lottery".equals(from) || "beauty".equals(from) || "competition".equalsIgnoreCase(from)) {
            iv_share.setVisibility(View.GONE);
        }
        if ("beauty1".equalsIgnoreCase(from)) {
            iv_share.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
        }

        isShowHeader = getIntent().getBooleanExtra("show_header", false);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.item_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        if (id == R.id.action_share) {
            ShareSDK.initSDK(this);
            OnekeyShare oks = new OnekeyShare();
            //关闭sso授权
            oks.disableSSOWhenAuthorize();

// 分享时Notification的图标和文字  2.5.9以后的版本不调用此方法
            //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
            // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
            oks.setTitle(getString(R.string.share));
            // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
            oks.setTitleUrl(url);
            // text是分享文本，所有平台都需要这个字段
            oks.setText(title);
            // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
            oks.setImageUrl(coverUrl);//确保SDcard下面存在此张图片
            // url仅在微信（包括好友和朋友圈）中使用
            oks.setUrl(url);
            // comment是我对这条分享的评论，仅在人人网和QQ空间使用
//            oks.setComment("我是测试评论文本");
            // site是分享此内容的网站名称，仅在QQ空间使用
            oks.setSite(getString(R.string.app_name));
            // siteUrl是分享此内容的网站地址，仅在QQ空间使用
            oks.setSiteUrl(url);

            oks.show(this);
        }
        return super.onOptionsItemSelected(item);
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
                doShare();
                break;
        }
    }

    private void doShare() {
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

// 分享时Notification的图标和文字  2.5.9以后的版本不调用此方法
        //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(title);
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl(url);
        // text是分享文本，所有平台都需要这个字段
        oks.setText(title + "   " + url);
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        oks.setImageUrl(coverUrl);//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl(url);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
//            oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(url);

        oks.show(this);
    }

    //    public void popToStack(Fragment fragment){
//
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        Log.i("childFragmentManager--->", fragmentManager.getBackStackEntryCount()+"");
//        fragmentManager.popBackStack();
//        Log.i("childFragmentManager--->", fragmentManager.getBackStackEntryCount()+"");
//    }
//
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
//    private void saveUserToSP(User user){
//        SharedPreferences.Editor editor = mSharedPreferences.edit();
//        editor.putInt("userId",user.getId());
//        editor.putString("email", user.getEmail());
//        editor.putString("token",user.getAuthentication_token());
//        editor.putString("avatar",user.getAvatar());
//        editor.putString("nickName",user.getNickName());
//        editor.commit();
//    }
//
//    public void navigationToFragment(Fragment fragment){
//        getSupportFragmentManager().beginTransaction().replace(R.id.fl_child_container,fragment)
//                .addToBackStack(null).commit();
//    }

    public String getDeviceId() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    @Override
    public void onBackClick(Fragment fragment) {
        Log.i("ItemDetailActivity--fragment----->", fragment.getClass().getSimpleName());
        webView.reload();
    }

    public void setRefreshNotifyListenter(RefreshNotifyListener refreshNotifyListenter) {
        this.refreshNotifyListener = refreshNotifyListenter;
    }

    public interface RefreshNotifyListener {
        public void setRefreshList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CallbackContext.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}