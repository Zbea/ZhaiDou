
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
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.fragments.ProfileAddrFragment;
import com.zhaidou.fragments.RegisterFragment;
import com.zhaidou.model.User;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomProgressWebview;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class HomeCompetitionActivity extends BaseActivity implements View.OnClickListener,
        RegisterFragment.RegisterOrLoginListener

{
    private CustomProgressWebview webView;
    private String title;
    private String url;
    private TextView tv_back;
    private TextView mTitleView;
    private int userId;
    private String profileId;
    private String token;
    private String nickName;
    private String mLocationStr;

    private boolean isProfileLoad=false;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_competition);

        mSharedPreferences = getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        userId = mSharedPreferences.getInt("userId", -1);
        token = mSharedPreferences.getString("token", null);
        nickName = mSharedPreferences.getString("nickName", "");

        getUserData();
        tv_back = (TextView) findViewById(R.id.tv_back);
        mTitleView = (TextView) findViewById(R.id.tv_title);
        mChildContainer = (FrameLayout) findViewById(R.id.fl_child_container);

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "抱歉，请检查网络", Toast.LENGTH_SHORT).show();
        }
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

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                System.out.println("view = [" + view + "], url = [" + url + "]-------------->token-------->"+token);
                if ("mobile://login?false".equalsIgnoreCase(url)&&!TextUtils.isEmpty(token)){
                    System.out.println("HomeCompetitionActivity.shouldOverrideUrlLoading---->\"mobile://login?false\".equalsIgnoreCase(url)&&!TextUtils.isEmpty(token)");
                    return true;
                }
                getDeviceId();
                if ("mobile://login?false".equalsIgnoreCase(url)) {
                    ToolUtils.setLog("登录");
                    System.out.println("HomeCompetitionActivity.shouldOverrideUrlLoading------->\"mobile://login?false\".equalsIgnoreCase(url)");
                    Intent intent = new Intent(HomeCompetitionActivity.this, LoginActivity.class);
                    intent.setFlags(2);
                    HomeCompetitionActivity.this.startActivityForResult(intent, 10000);
                    return true;
                } else if ("mobile://address".equalsIgnoreCase(url)) {
                    System.out.println("HomeCompetitionActivity.shouldOverrideUrlLoading----------->\"mobile://address\".equalsIgnoreCase(url)----->"+isProfileLoad);
                    if (isProfileLoad){
                        System.out.println("view = [" + view + "], url = [" + url + "]");
                        System.out.println("HomeCompetitionActivity.shouldOverrideUrlLoading--------->" + user.getFirst_name());
                        System.out.println("HomeCompetitionActivity.shouldOverrideUrlLoading--------->" + user.getMobile());
                        System.out.println("HomeCompetitionActivity.shouldOverrideUrlLoading--------->" + user.getAddress2());
                        System.out.println("HomeCompetitionActivity.shouldOverrideUrlLoading--------->" + profileId);
                        String address = TextUtils.isEmpty(user.getAddress2()) || "null".equalsIgnoreCase(user.getAddress2()) ? "" : user.getAddress2();
                        String locationStr=TextUtils.isEmpty(mLocationStr)?user.getProvince()+"-"+user.getCity()+"-"+user.getProvider():mLocationStr;
                        ProfileAddrFragment profileAddrFragment = ProfileAddrFragment.newInstance(user.getFirst_name(), user.getMobile(),locationStr,address,user.getAddress1(), profileId);
                        getSupportFragmentManager().beginTransaction().replace(R.id.fl_child_container, profileAddrFragment)
                                .addToBackStack(null).commit();
                        profileAddrFragment.setAddressListener(new ProfileAddrFragment.AddressListener() {
                            @Override
                            public void onAddressDataChange(String name, String mobile,String locationStr, String address) {
                                mLocationStr=locationStr;
                                System.out.println("name = [" + name + "], mobile = [" + mobile + "], address = [" + address + "]");
                                user.setFirst_name(name);
                                user.setMobile(mobile);
                                user.setAddress2(address);
                            }
                        });
                        mChildContainer.setVisibility(View.VISIBLE);
                    }
                    return true;

                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!TextUtils.isEmpty(token)) {
                    webView.loadUrl("javascript:ReceiveUserInfo(" + userId + ", '" + token + "'," + getDeviceId() + ",'" + nickName + "')");
                } else {
                    webView.loadUrl("javascript:ReceiveUserInfo(" + userId + ", '" + "" + "'," + getDeviceId() + ",'" + nickName + "')");
                }
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

        url = getIntent().getStringExtra("url");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("ZhaidouVesion", getResources().getString(R.string.app_versionName));
        headers.put("SECAuthorization", token);
        webView.loadUrl(url + "?open=app",headers);
        this.setTitle("");
        title = getIntent().getStringExtra("title");
        if (!TextUtils.isEmpty(title)) {
            mTitleView.setText(title);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                if (webView.canGoBack()) {
                    webView.goBack();
                    return;
                }
                finish();
                break;
        }
    }

    @Override
    public void onRegisterOrLoginSuccess(User user, Fragment fragment) {
        webView.loadUrl("javascript:ReceiveUserInfo(" + user.getId() + ", '" + user.getAuthentication_token() + "'," + getDeviceId() + ",'" + user.getNickName() + "')");
        super.onRegisterOrLoginSuccess(user, fragment);
    }
    public void getUserData() {
        userId = mSharedPreferences.getInt("userId", -1);
        token = mSharedPreferences.getString("token", null);
        nickName = mSharedPreferences.getString("nickName", "");
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.USER_DETAIL_PROFILE_URL + "?id=" + userId, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                int status = jsonObject.optInt("status");
                String msg = jsonObject.optString("message");
                if (status == 200) {
                    JSONObject dataObj = jsonObject.optJSONObject("data");
                    JSONObject userObj = dataObj.optJSONObject("profile");
                    if (userObj == null) return;
                    String mobile = userObj.optString("mobile");
                    mobile = mobile.equals("null") ? "" : mobile;
                    String description = userObj.optString("description");
                    description = description.equals("null") ? "" : description;
                    profileId = userObj.optString("id");
                    boolean verified = userObj.optBoolean("verified");
                    String first_name = userObj.optString("first_name");
                    String address2 = userObj.optString("address2");
                    String city_name = userObj.optString("city_name");
                    String province_name = userObj.optString("province_name");
                    String provider_name = userObj.optString("provider_name");
                    String address1 = userObj.optString("address1");
                    user= new User(null, null, null, verified, mobile, description);
                    user.setAddress2(address2);
                    user.setFirst_name(first_name);
                    user.setCity(city_name);
                    user.setProvince(province_name);
                    user.setProvider(provider_name);
                    user.setAddress1(address1);
                    isProfileLoad=true;

                } else {
                    Toast.makeText(HomeCompetitionActivity.this,msg,Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        });
        ((ZDApplication)getApplication()).mRequestQueue.add(request);
    }

    public String getDeviceId() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 2000:
                token = mSharedPreferences.getString("token", null);
                getUserData();
                webView.reload();
                break;
        }
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