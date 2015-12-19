package com.zhaidou.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.model.Store;
import com.zhaidou.view.CustomProgressWebview;

public class OrderLogisticsMsgFragment extends BaseFragment {
    private static final String ARG_STORE= "store";

    private Context context;
    private CustomProgressWebview mWebView;
    private TextView logisticsNum, orderNum,deliveryName;
    private Store mStore;

    public static OrderLogisticsMsgFragment newInstance(Store store) {
        OrderLogisticsMsgFragment fragment = new OrderLogisticsMsgFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_STORE, store);
        fragment.setArguments(args);
        return fragment;
    }

    public OrderLogisticsMsgFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStore = (Store) getArguments().getSerializable(ARG_STORE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logistics, container, false);
        context = getActivity();

        orderNum = (TextView) view.findViewById(R.id.tv_order_number);
        logisticsNum = (TextView) view.findViewById(R.id.tv_order_amount);
        deliveryName= (TextView) view.findViewById(R.id.deliveryName);

        mWebView = (CustomProgressWebview) view.findViewById(R.id.wv_logistics);
        mWebView.getSettings().setJavaScriptEnabled(true);

        orderNum.setText("" + mStore.orderCode);
        logisticsNum.setText("" + mStore.deliveryPO.deliveryNo);
        deliveryName.setText(mStore.deliveryPO.deliveryName);
        String url = "http://m.kuaidi100.com/index_all.html?type=" +mStore.deliveryPO.deliveryCode + "&postid=" + mStore.deliveryPO.deliveryNo + "#result";
        mWebView.loadUrl(url);


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

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_logistics));
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_logistics));
    }
}