
package com.zhaidou.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.callback.CallbackContext;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.fragments.HomeFragment;
import com.zhaidou.fragments.LoginFragment;
import com.zhaidou.fragments.RegisterFragment;
import com.zhaidou.model.Article;
import com.zhaidou.model.User;
import com.zhaidou.utils.AsyncImageLoader1;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.ToolUtils;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

public class ItemDetailActivity extends BaseActivity implements View.OnClickListener,
        RegisterFragment.RegisterOrLoginListener,
        LoginFragment.BackClickListener

{

    private WebView webView;

    /* 以下代码应该封装为一个对象 */
    private String title;
    private String coverUrl;
    private String url;
    private TextView tv_back;
    private ImageView iv_share,mHeaderView;
    private FrameLayout mChildContainer;
    private TextView mTitleView,mHeaderText;
    private Article article;
    private String is_new;
    private String is_id;

    private int userId;
    private String token;
    private String nickName;
    private boolean isShowHeader;

//    private String from;
    private LoginFragment loginFragment;
    private RegisterFragment registerFragment;

    private SharedPreferences mSharedPreferences;
    private Dialog mDialog;
    public static RefreshNotifyListener refreshNotifyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        from=getIntent().getStringExtra("from");
        article=(Article)getIntent().getSerializableExtra("article");
        if (article!=null)
        {
            is_id=String.valueOf(article.getId());
            is_new=article.getIs_new();

            if(is_new.equals("true"))
            {
                SharedPreferences sharedPreferences=getSharedPreferences(is_id,0);
                SharedPreferences.Editor editor= sharedPreferences.edit();
                editor.putBoolean("is_new",true);
                editor.commit();
                Intent intent=new Intent(ZhaiDou.IntentRefreshListTag);
                sendBroadcast(intent);
            }

        }

        mSharedPreferences=getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        userId=mSharedPreferences.getInt("userId", -1);
        token=mSharedPreferences.getString("token", null);
        nickName=mSharedPreferences.getString("nickName","");
        tv_back=(TextView)findViewById(R.id.tv_back);
        iv_share=(ImageView)findViewById(R.id.iv_share);
        mChildContainer=(FrameLayout)findViewById(R.id.fl_child_container);
        mTitleView=(TextView)findViewById(R.id.tv_title);
        mHeaderView=(ImageView)findViewById(R.id.iv_header);
        mHeaderText=(TextView)findViewById(R.id.tv_msg);

        if(NetworkUtils.isNetworkAvailable(this))
        {
            mDialog= CustomLoadingDialog.setLoadingDialog(this,"loading");
        }
        else
        {
            Toast.makeText(this,"抱歉，请检查网络",Toast.LENGTH_SHORT).show();
        }

        loginFragment=LoginFragment.newInstance("","");
        loginFragment.setRegisterOrLoginListener(this);
        loginFragment.setBackClickListener(this);
        tv_back.setOnClickListener(this);
        iv_share.setOnClickListener(this);



        //String postId = getIntent().getStringExtra("id");

        /* WebView Settings */
        webView = (WebView) findViewById(R.id.detailView);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setJavaScriptEnabled(true);
//        webView.getSettings().setDomStorageEnabled(true);


        webView.setVerticalScrollBarEnabled(false);
        webView.setVerticalScrollbarOverlay(false);
        webView.setHorizontalScrollbarOverlay(false);
        webView.setHorizontalFadingEdgeEnabled(false);
        webView.setInitialScale(1);
        webView.setWebChromeClient(new WebChromeClient(){

        });
        webView.setWebViewClient(new WebViewClient()
        {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                Log.i("shouldOverrideUrlLoading---------------->", url);
                getDeviceId();
                if ("mobile://login?false".equalsIgnoreCase(url))
                {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fl_child_container, loginFragment)
                            .addToBackStack(null).commit();
                    mChildContainer.setVisibility(View.VISIBLE);
//                    }
                    return true;
                } else if (url.contains("taobao"))
                {
                    Intent intent = new Intent();
                    intent.putExtra("url", url);
                    intent.setClass(ItemDetailActivity.this, WebViewActivity.class);
                    ItemDetailActivity.this.startActivity(intent);
                    return true;
                }
                return false;
            }
            @Override
            public void onPageFinished(WebView view, String url)
            {
                if ("lottery".equalsIgnoreCase(from)){
                    Log.i("lottery----------->","onPageFinished"+"------"+token);
                    if (!TextUtils.isEmpty(token)){
                        webView.loadUrl("javascript:ReceiveUserInfo("+userId+", '"+token+"','"+getDeviceId()+"','"+nickName+"')");
                    }else {
                        webView.loadUrl("javascript:ReceiveUserInfo("+userId+", '"+""+"','"+getDeviceId()+"','"+nickName+"')");
                    }
                }else if ("product".equalsIgnoreCase(from)){
                    if (!TextUtils.isEmpty(token))
                        webView.loadUrl("javascript:ReceiveUserInfo("+userId+", '"+token+"')");
                }
                if (mDialog!=null)
                {
                    mDialog.dismiss();
                }
                super.onPageFinished(view, url);
            }
        });
        url = getIntent().getStringExtra("url");
        Log.i("url----------->","url"+"------"+url);
        webView.loadUrl(url+"?open=app");
        this.setTitle("");

        title = getIntent().getStringExtra("title");
        coverUrl = getIntent().getStringExtra("cover_url");

        if (!TextUtils.isEmpty(title)){
            mTitleView.setText(title);
        }
        if (!TextUtils.isEmpty(coverUrl)){
            Log.i("cover_url---------------->", coverUrl);
            mHeaderView.setVisibility(View.VISIBLE);
            ToolUtils.setImageCacheUrl(coverUrl, mHeaderView);
            mTitleView.setVisibility(View.GONE);
            mHeaderText.setText(title);
            mHeaderText.setVisibility(View.VISIBLE);
        }
        if ("lottery".equals(from)||"beauty".equals(from)||"competition".equalsIgnoreCase(from)){
            iv_share.setVisibility(View.GONE);
        }
        if ("beauty1".equalsIgnoreCase(from)){
//            webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            iv_share.setVisibility(View.VISIBLE);
            mHeaderView.setVisibility(View.GONE);
            mHeaderText.setVisibility(View.GONE);
        }

        isShowHeader=getIntent().getBooleanExtra("show_header",false);

        /*
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        View customActionBar = LayoutInflater.from(this).inflate(R.layout.actionbar_with_backbutton, null);
        ImageView backView = (ImageView) customActionBar.findViewById(R.id.actionbar_back_button);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ItemDetailActivity.this.finish();
            }
        });
        getActionBar().setCustomView(customActionBar);
        */

//        ActionBar actionBar = getActionBar();
//        actionBar.setIcon(R.drawable.actionbar_back);
//        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.item_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
        switch (view.getId()){
            case R.id.tv_back:
                if (webView.canGoBack()){
                    if ("product".equalsIgnoreCase(from)){
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

    private void doShare(){
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
        oks.setText(title+"   "+url);
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
    public void onRegisterOrLoginSuccess(User user,Fragment fragment) {

        if ("lottery".equalsIgnoreCase(from)){
            Log.i("onRegisterOrLoginSuccess--lottery----------->",user.getId()+"------"+user.getAuthentication_token()+"-------"+getDeviceId()+"----"+user.getNickName());
            webView.loadUrl("javascript:ReceiveUserInfo("+user.getId()+", '"+user.getAuthentication_token()+"','"+getDeviceId()+"','"+user.getNickName()+"')");
        }else if ("product".equalsIgnoreCase(from)){
//            webView.reload();
            webView.loadUrl("javascript:ReceiveUserInfo("+user.getId()+", '"+user.getAuthentication_token()+"')");
        }
        super.onRegisterOrLoginSuccess(user,fragment);
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

    public String getDeviceId(){
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    @Override
    public void onBackClick(Fragment fragment) {
        Log.i("ItemDetailActivity--fragment----->",fragment.getClass().getSimpleName());
        webView.reload();
    }

    public void setRefreshNotifyListenter(RefreshNotifyListener refreshNotifyListenter)
    {
        this.refreshNotifyListener=refreshNotifyListenter;
    }

    public interface RefreshNotifyListener
    {
        public void setRefreshList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CallbackContext.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
=======
package com.zhaidou.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.callback.CallbackContext;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.fragments.HomeFragment;
import com.zhaidou.fragments.LoginFragment;
import com.zhaidou.fragments.RegisterFragment;
import com.zhaidou.model.Article;
import com.zhaidou.model.User;
import com.zhaidou.utils.AsyncImageLoader1;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomProgressWebview;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

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
    private ImageView iv_share,mHeaderView;
    private FrameLayout mChildContainer;
    private TextView mTitleView,mHeaderText;
    private Article article;
    private String is_new;
    private String is_id;

    private int userId;
    private String token;
    private String nickName;
    private boolean isShowHeader;

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

        from=getIntent().getStringExtra("from");
        article=(Article)getIntent().getSerializableExtra("article");
        if (article!=null)
        {
            is_id=String.valueOf(article.getId());
            is_new=article.getIs_new();

            if(is_new.equals("true"))
            {
                SharedPreferences sharedPreferences=getSharedPreferences(is_id,0);
                SharedPreferences.Editor editor= sharedPreferences.edit();
                editor.putBoolean("is_new",true);
                editor.commit();
                Intent intent=new Intent(ZhaiDou.IntentRefreshListTag);
                sendBroadcast(intent);
            }

        }

        mSharedPreferences=getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        userId=mSharedPreferences.getInt("userId", -1);
        token=mSharedPreferences.getString("token", null);
        nickName=mSharedPreferences.getString("nickName","");
        tv_back=(TextView)findViewById(R.id.tv_back);
        iv_share=(ImageView)findViewById(R.id.iv_share);
        mChildContainer=(FrameLayout)findViewById(R.id.fl_child_container);
        mTitleView=(TextView)findViewById(R.id.tv_title);
        mHeaderView=(ImageView)findViewById(R.id.iv_header);
        mHeaderText=(TextView)findViewById(R.id.tv_msg);

        if(NetworkUtils.isNetworkAvailable(this))
        {
//            mDialog= CustomLoadingDialog.setLoadingDialog(this,"loading");
        }
        else
        {
            Toast.makeText(this,"抱歉，请检查网络",Toast.LENGTH_SHORT).show();
        }

        loginFragment=LoginFragment.newInstance("","");
        loginFragment.setRegisterOrLoginListener(this);
        loginFragment.setBackClickListener(this);
        tv_back.setOnClickListener(this);
        iv_share.setOnClickListener(this);



        //String postId = getIntent().getStringExtra("id");

        /* WebView Settings */
        webView = (CustomProgressWebview) findViewById(R.id.detailView);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.setVerticalScrollBarEnabled(false);
        webView.setVerticalScrollbarOverlay(false);
        webView.setHorizontalScrollbarOverlay(false);
        webView.setHorizontalFadingEdgeEnabled(false);
        webView.setInitialScale(1);
        webView.setWebChromeClient(new WebChromeClient(){

        });
        webView.setWebViewClient(new WebViewClient()
        {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                Log.i("shouldOverrideUrlLoading---------------->", url);
                getDeviceId();
                if ("mobile://login?false".equalsIgnoreCase(url))
                {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fl_child_container, loginFragment)
                            .addToBackStack(null).commit();
                    mChildContainer.setVisibility(View.VISIBLE);
//                    }
                    return true;
                } else if (url.contains("taobao"))
                {
                    Intent intent = new Intent();
                    intent.putExtra("url", url);
                    intent.setClass(ItemDetailActivity.this, WebViewActivity.class);
                    ItemDetailActivity.this.startActivity(intent);
                    return true;
                }
                return false;
            }
            @Override
            public void onPageFinished(WebView view, String url)
            {
                if ("lottery".equalsIgnoreCase(from)){
                    Log.i("lottery----------->","onPageFinished"+"------"+token);
                    if (!TextUtils.isEmpty(token)){
                        webView.loadUrl("javascript:ReceiveUserInfo("+userId+", '"+token+"',"+getDeviceId()+",'"+nickName+"')");
                    }else {
                        webView.loadUrl("javascript:ReceiveUserInfo("+userId+", '"+""+"',"+getDeviceId()+",'"+nickName+"')");
                    }

                }else if ("product".equalsIgnoreCase(from)){
                    if (!TextUtils.isEmpty(token))
                        webView.loadUrl("javascript:ReceiveUserInfo("+userId+", '"+token+"')");
                }
//                if (mDialog!=null)
//                {
//                    mDialog.dismiss();
//                }
                super.onPageFinished(view, url);
            }
        });
        url = getIntent().getStringExtra("url");
        Log.i("url----------->","url"+"------"+url);
        webView.loadUrl(url+"?open=app");
        this.setTitle("");

        title = getIntent().getStringExtra("title");
        coverUrl = getIntent().getStringExtra("cover_url");

        if (!TextUtils.isEmpty(title)){
            mTitleView.setText(title);
        }
        if (!TextUtils.isEmpty(coverUrl)){
            Log.i("cover_url---------------->", coverUrl);
            mHeaderView.setVisibility(View.VISIBLE);
            ToolUtils.setImageCacheUrl(coverUrl, mHeaderView);
            mTitleView.setVisibility(View.GONE);
            mHeaderText.setText(title);
            mHeaderText.setVisibility(View.VISIBLE);
        }
        if ("lottery".equals(from)||"beauty".equals(from)||"competition".equalsIgnoreCase(from)){
            iv_share.setVisibility(View.GONE);
        }
        if ("beauty1".equalsIgnoreCase(from)){
            iv_share.setVisibility(View.VISIBLE);
            mHeaderView.setVisibility(View.GONE);
            mHeaderText.setVisibility(View.GONE);
        }

        isShowHeader=getIntent().getBooleanExtra("show_header",false);

        /*
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        View customActionBar = LayoutInflater.from(this).inflate(R.layout.actionbar_with_backbutton, null);
        ImageView backView = (ImageView) customActionBar.findViewById(R.id.actionbar_back_button);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ItemDetailActivity.this.finish();
            }
        });
        getActionBar().setCustomView(customActionBar);
        */

//        ActionBar actionBar = getActionBar();
//        actionBar.setIcon(R.drawable.actionbar_back);
//        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.item_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
        switch (view.getId()){
            case R.id.tv_back:
                if (webView.canGoBack()){
                    if ("product".equalsIgnoreCase(from)){
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

    private void doShare(){
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
        oks.setText(title+"   "+url);
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
    public void onRegisterOrLoginSuccess(User user,Fragment fragment) {

        if ("lottery".equalsIgnoreCase(from)){
            Log.i("onRegisterOrLoginSuccess--lottery----------->","onPageFinished"+"------"+token);
            webView.loadUrl("javascript:ReceiveUserInfo("+user.getId()+", '"+user.getAuthentication_token()+"',"+getDeviceId()+",'"+user.getNickName()+"')");
        }else if ("product".equalsIgnoreCase(from)){
            webView.loadUrl("javascript:ReceiveUserInfo("+user.getId()+", '"+user.getAuthentication_token()+"')");
        }
        super.onRegisterOrLoginSuccess(user,fragment);
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

    public String getDeviceId(){
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    @Override
    public void onBackClick(Fragment fragment) {
        Log.i("ItemDetailActivity--fragment----->",fragment.getClass().getSimpleName());
        webView.reload();
    }

    public void setRefreshNotifyListenter(RefreshNotifyListener refreshNotifyListenter)
    {
        this.refreshNotifyListener=refreshNotifyListenter;
    }

    public interface RefreshNotifyListener
    {
        public void setRefreshList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CallbackContext.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}