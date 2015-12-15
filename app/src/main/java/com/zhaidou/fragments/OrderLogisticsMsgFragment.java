package com.zhaidou.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.model.Store;
import com.zhaidou.view.CustomProgressWebview;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OrderLogisticsMsgFragment extends BaseFragment {
    private static final String ARG_TYPE = "type";
    private static final String ARG_NUMBER = "number";

    private String mType;
    private String mNumber;
    private Store mStore;

    private ExpandableListView mLogisticsView;

    private Context context;
    private CustomProgressWebview mWebView;
    private TextView logisticsNum, orderNum,deliveryName;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mStore = (Store) msg.obj;
            orderNum.setText("" + mStore.orderCode);
            logisticsNum.setText("" + mStore.deliveryPO.deliveryNo);
            deliveryName.setText(mStore.deliveryPO.deliveryName);
            String url = "http://m.kuaidi100.com/index_all.html?type=" +mStore.deliveryPO.deliveryCode + "&postid=" + mStore.deliveryPO.deliveryNo + "#result";
            mWebView.loadUrl(url);
        }
    };


    public static OrderLogisticsMsgFragment newInstance(String type, String number, Store store) {
        OrderLogisticsMsgFragment fragment = new OrderLogisticsMsgFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putString(ARG_NUMBER, number);
        args.putSerializable("store", store);
        fragment.setArguments(args);
        return fragment;
    }

    public OrderLogisticsMsgFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getString(ARG_TYPE);
            mNumber = getArguments().getString(ARG_NUMBER);
            mStore = (Store) getArguments().getSerializable("store");
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
//        orderNum.setText(""+mStore.);
//        logisticsNum.setText(""+mOrder.logisticsNum);

        mWebView = (CustomProgressWebview) view.findViewById(R.id.wv_logistics);
        mWebView.getSettings().setJavaScriptEnabled(true);

        FetchOrderDetail(mStore);

//        String url="http://m.kuaidi100.com/index_all.html?type="+(TextUtils.isEmpty(mType)?"huitongkuaidi":mType)+"&postid="+mOrder.logisticsNum+"#result";
//        mWebView.loadUrl(url);
//
//        Log.i("url------------>",url);
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

    private void FetchOrderDetail(Store store) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("userId", ZhaiDou.TESTUSERID);
        params.put("clientType", "ANDROID");
        params.put("clientVersion", "45");
        params.put("businessType", "01");
        params.put("orderCode", store.orderCode);//"MCMST3407850_1"
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.URL_ORDER_DETAIL, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                JSONObject data = jsonObject.optJSONObject("data");
                Store store = JSON.parseObject(data.toString(), Store.class);
                Message message = new Message();
                message.obj = store;
                mHandler.sendMessage(message);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        ((ZDApplication) getActivity().getApplication()).mRequestQueue.add(request);
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