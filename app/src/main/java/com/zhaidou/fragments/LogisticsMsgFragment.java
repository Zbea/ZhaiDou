package com.zhaidou.fragments;



import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.model.Order;
import com.zhaidou.view.CustomProgressWebview;

import java.util.ArrayList;
import java.util.List;

public class LogisticsMsgFragment extends BaseFragment {
    private static final String ARG_TYPE = "type";
    private static final String ARG_NUMBER = "number";

    private String mType;
    private String mNumber;
    private Order mOrder;

    private ExpandableListView mLogisticsView;

    private Context context;
    private CustomProgressWebview mWebView;
    private TextView logisticsNum,orderNum;


    public static LogisticsMsgFragment newInstance(String type, String number,Order order) {
        LogisticsMsgFragment fragment = new LogisticsMsgFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putString(ARG_NUMBER, number);
        args.putSerializable("order", order);
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
            mOrder = (Order)getArguments().getSerializable("order");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_logistics, container, false);
        context = getActivity();

        orderNum=(TextView)view.findViewById(R.id.tv_order_number);
        logisticsNum=(TextView)view.findViewById(R.id.tv_order_amount);
        orderNum.setText(""+mOrder.getNumber());
        logisticsNum.setText(""+mOrder.logisticsNum);

        mWebView=(CustomProgressWebview)view.findViewById(R.id.wv_logistics);
        mWebView.getSettings().setJavaScriptEnabled(true);

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

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    mWebView.progressBar.setVisibility(View.GONE);

                } else {
                    mWebView.progressBar.setVisibility(View.VISIBLE);
                    mWebView.progressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
        return view;
    }
}