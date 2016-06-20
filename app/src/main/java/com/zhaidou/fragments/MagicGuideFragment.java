package com.zhaidou.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.HomePTActivity;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.view.CustomProgressWebview;

/**
 * 软装指南
 */
public class MagicGuideFragment extends BaseFragment implements View.OnClickListener{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private View rootView;
    private CustomProgressWebview webview;


    public static MagicGuideFragment newInstance(String param1, String param2) {
        MagicGuideFragment fragment = new MagicGuideFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public MagicGuideFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != rootView)
        {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent)
            {
                parent.removeView(rootView);
            }
        } else
        {
            rootView = inflater.inflate(R.layout.fragment_magic_guide, container, false);
            initView();
        }
        return rootView;
    }

    private void initView()
    {
        rootView.findViewById(R.id.ll_back).setOnClickListener(this);
        webview=(CustomProgressWebview)rootView.findViewById(R.id.webView);
        webview.loadUrl(ZhaiDou.MagicGuideUrl);
        WebSettings webSettings=webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webview.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onPageFinished(WebView view, String url)
            {
                view.loadUrl("javascript:!function(){" +

                        "s=document.createElement('style');s.innerHTML="

                        + "\"@font-face{font-family:FZLTXHK;src:url('**injection**/FZLTXHK.TTF');}*{font-family:FZLTXHK !important;}\";"

                        + "document.getElementsByTagName('head')[0].appendChild(s);" +

                        "document.getElementsByTagName('body')[0].style.fontFamily = \"FZLTXHK\";}()");
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                Intent intent = new Intent(getActivity(), HomePTActivity.class);
                intent.putExtra("url", url);
                intent.putExtra("title", "");
                startActivity(intent);
                return true;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ll_back:
                ((BaseActivity)getActivity()).popToStack(MagicGuideFragment.this);
                break;
        }
    }



    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_main_magic_guide)); //统计页面
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_main_magic_guide));
    }
}
