package com.zhaidou.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.zhaidou.ZhaiDou;
import com.zhaidou.R;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

public class ItemDetailActivity extends Activity {

    private WebView webView;

    /* 以下代码应该封装为一个对象 */
    private String title;
    private String coverUrl;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        String postId = getIntent().getStringExtra("id");

        /* WebView Settings */
        webView = (WebView) findViewById(R.id.detailView);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setInitialScale(1);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = new Intent();
                intent.putExtra("url", url);
                intent.setClass(ItemDetailActivity.this, WebViewActivity.class);
                ItemDetailActivity.this.startActivity(intent);
                return true;
            }

        });

        String postUrl = ZhaiDou.HOME_BASE_URL + "?p=" + postId;
        webView.loadUrl(postUrl);
        this.setTitle("");

        title = getIntent().getStringExtra("title");
        coverUrl = getIntent().getStringExtra("cover_url");
        url = getIntent().getStringExtra("url");

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

        ActionBar actionBar = getActionBar();
        actionBar.setIcon(R.drawable.actionbar_back);
        actionBar.setHomeButtonEnabled(true);
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
}
