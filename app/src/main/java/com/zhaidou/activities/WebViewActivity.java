package com.zhaidou.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.fragments.GoodsDetailsFragment;
import com.zhaidou.utils.Api;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomProgressWebview;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class WebViewActivity extends BaseActivity implements View.OnClickListener {

    private String url;
    private CustomProgressWebview webView;
    private String token;
    private Integer userId;
    private String userName;
    private DialogUtils mDialogUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        findViewById(R.id.ll_back).setOnClickListener(this);
        mChildContainer = (android.widget.FrameLayout) findViewById(R.id.fl_child_container);

        webView = (CustomProgressWebview) findViewById(R.id.webView);
        token = (String) SharedPreferencesUtil.getData(WebViewActivity.this, "token", "");
        userId = (Integer) SharedPreferencesUtil.getData(WebViewActivity.this, "userId", 0);
        userName = (String) SharedPreferencesUtil.getData(WebViewActivity.this, "nickName", "");

        mDialogUtils = new DialogUtils(this);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                System.out.println("url = " + url);
                if (url.startsWith("taobao://"))
                    return true;
                if (url.contains("zhaidouappproduct:")) {
                    String substring = url.substring(url.lastIndexOf("/") + 1, url.length());
                    System.out.println("substring = " + substring);
                    GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance("", substring);
                    navigationToFragmentWithAnim(goodsDetailsFragment);
                    return true;
                } else if (url.contains("zhaidouappgetcoupon://")) {
                    final String substring = url.substring(url.lastIndexOf("/") + 1, url.length());
                    mDialogUtils.showLoadingDialog();
                    if (substring.contains("_")) {
                        final String[] split = substring.split("_");
                        String item = "'";
                        for (int i = 0; i < split.length; i++) {
                            item += split[i] + "','";
                        }
                        final String finalItem = item.substring(0, item.length() - 2);
                        Api.activateAllCouponsByOneClick(split, new Api.SuccessListener() {
                            @Override
                            public void onSuccess(Object object) {
                                if (object != null)
                                    Toast.makeText(WebViewActivity.this, "领取优惠卷成功", Toast.LENGTH_SHORT).show();
                                System.out.println("item = " + finalItem);
//                                webView.loadUrl("javascript:GetCoupon('" + split[0]+"','"+split[1]+ "');");
                                for (int i = 0; i < split.length; i++) {
                                    webView.loadUrl("javascript:GetCoupon('" + split[i] + "')");
                                }
                                System.out.println("\"javascript:GetCoupon(\" + finalItem + \")\" = " + "javascript:GetCoupon(" + finalItem + ")");
                                mDialogUtils.dismiss();
                            }
                        }, new Api.ErrorListener() {
                            @Override
                            public void onError(Object object) {
                                mDialogUtils.dismiss();
                            }
                        });
                    } else {
                        Api.activateCoupons(substring, new Api.SuccessListener() {
                            @Override
                            public void onSuccess(Object object) {
                                if (object != null)
                                    Toast.makeText(WebViewActivity.this, "领取优惠卷成功", Toast.LENGTH_SHORT).show();
                                webView.loadUrl("javascript:GetCoupon('" + substring + "')");
                                        mDialogUtils.dismiss();
                            }
                        }, new Api.ErrorListener() {
                            @Override
                            public void onError(Object object) {
                                mDialogUtils.dismiss();
                            }
                        });
                    }
                    return true;
                } else if ("mobile://login?false".equalsIgnoreCase(url)) {
                    Intent intent = new Intent(WebViewActivity.this, LoginActivity.class);
                    intent.setFlags(2);
                    startActivityForResult(intent, 10000);
                    return true;
                }
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                webView.loadUrl("javascript:ReceiveUserInfo(" + userId + ", '" + token + "'," + getDeviceId() + ",'" + userName + "')");
                super.onPageFinished(view, url);
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

        webView.setDownloadListener(new DownloadListener() {
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
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        String url = getIntent().getStringExtra("url");

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("ZhaidouVesion", getResources().getString(R.string.app_versionName));
        if (url.contains("receive_coupon"))
            url+="&source=android";
        ToolUtils.setLog(url);
        webView.loadUrl(url, headers);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_back:
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 2000:
                token = (String) SharedPreferencesUtil.getData(WebViewActivity.this, "token", "");
                userId = (Integer) SharedPreferencesUtil.getData(WebViewActivity.this, "userId", 0);
                userName = (String) SharedPreferencesUtil.getData(WebViewActivity.this, "nickName", "");
                webView.reload();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("WebViewActivity");
        try
        {
            webView.getClass().getMethod("onResume").invoke(webView,(Object[])null);
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        } catch (InvocationTargetException e)
        {
            e.printStackTrace();
        } catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        }
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("WebViewActivity");
        try
        {
            webView.getClass().getMethod("onPause").invoke(webView,(Object[])null);
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        } catch (InvocationTargetException e)
        {
            e.printStackTrace();
        } catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        }
        MobclickAgent.onPause(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode==KeyEvent.KEYCODE_BACK&webView.canGoBack())
        {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
