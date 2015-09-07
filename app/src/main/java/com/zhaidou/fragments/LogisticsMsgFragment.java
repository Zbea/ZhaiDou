package com.zhaidou.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ExpandableListView;

import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;

public class LogisticsMsgFragment extends BaseFragment {
    private static final String ARG_TYPE = "type";
    private static final String ARG_NUMBER = "number";

    private String mType;
    private String mNumber;

    private ExpandableListView mLogisticsView;

    private Context context;
    private WebView mWebView;


    public static LogisticsMsgFragment newInstance(String type, String number) {
        LogisticsMsgFragment fragment = new LogisticsMsgFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putString(ARG_NUMBER, number);
        fragment.setArguments(args);
        return fragment;
    }
    public LogisticsMsgFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getString(ARG_TYPE);
            mNumber = getArguments().getString(ARG_NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_logistics, container, false);
        context = getActivity();
        mLogisticsView = (ExpandableListView)view.findViewById(R.id.lv_logistics);
        mWebView=(WebView)view.findViewById(R.id.wv_logistics);
        mWebView.getSettings().setJavaScriptEnabled(true);
        WebSettings webSettings = mWebView.getSettings();

        String url="http://m.kuaidi100.com/index_all.html?type="+(TextUtils.isEmpty(mType)?"huitongkuaidi":mType)+"&postid="+mNumber+"#result";
        mWebView.loadUrl(url);

        Log.i("url------------>",url);
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                mWebView.loadUrl("javascript:$('.smart-header').remove();$('.adsbygoogle').hide();$('#result').css('padding-top','0px');" +
                        "$('.smart-footer').remove();");
            }
        });
        return view;
    }
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_logistics));
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_logistics));
    }
}