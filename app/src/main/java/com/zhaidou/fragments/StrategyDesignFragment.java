package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.WebViewActivity;
import com.zhaidou.adapter.RecommendAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.RecommendItem;
import com.zhaidou.utils.DeviceUtils;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by roy on 15/11/11.
 */
public class StrategyDesignFragment extends BaseFragment {
    private static final String DATA = "page";
    private static final String INDEX = "index";

    private View mView;

    private String mPage;
    private String mIndex;
    private Context mContext;
    private TextView backBtn;
    private RelativeLayout designBtn;





    private View.OnClickListener onClickListener=new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.back_btn:
                    ((MainActivity) getActivity()).popToStack(StrategyDesignFragment.this);
                    break;
                case R.id.design_rl:
                    if (DeviceUtils.isApkInstalled(getActivity(), "com.tencent.mobileqq")){
                        String url = "mqqwpa://im/chat?chat_type=wpa&uin=" + mContext.getResources().getString(R.string.QQ_design);
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    }else {
                        ShowToast("没有安装QQ客户端哦");
                    }
                    break;
            }
        }
    };

    public static StrategyDesignFragment newInstance(String page, String index) {
        StrategyDesignFragment fragment = new StrategyDesignFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATA, page);
        args.putSerializable(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    public StrategyDesignFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getString(DATA);
            mIndex = getArguments().getString(INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.strategy_design_page, container, false);
            mContext = getActivity();
            initView();
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) mView.getParent();
        if (parent != null) {
            parent.removeView(mView);
        }

        return mView;
    }

    /**
     * 初始化
     */
    private void initView() {

        backBtn = (TextView) mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);

        designBtn = (RelativeLayout) mView.findViewById(R.id.design_rl);
        designBtn.setOnClickListener(onClickListener);
    }


    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("设计方案");
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("设计方案");
    }

}
