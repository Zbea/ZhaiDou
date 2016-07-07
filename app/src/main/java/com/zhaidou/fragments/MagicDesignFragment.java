package com.zhaidou.fragments;


import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.utils.EaseUtils;

/**
 * Created by roy on 15/11/11.
 */
public class MagicDesignFragment extends BaseFragment {
    private static final String DATA = "page";
    private static final String INDEX = "index";

    private View mView;

    private String mPage;
    private String mIndex;
    private Context mContext;
    private TextView backBtn,caseBtn;
    private LinearLayout designBtn,qqBtn;





    private View.OnClickListener onClickListener=new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.ll_back:
                    ((BaseActivity) getActivity()).popToStack(MagicDesignFragment.this);
                    break;
                case R.id.case_rl:
                    GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance("宅豆软装设计方案", "191105000227");
                    ((BaseActivity) getActivity()).navigationToFragmentWithAnim(goodsDetailsFragment);
                    break;
                case R.id.design_rl:
                    EaseUtils.startDesignerActivity(mContext);
                    break;
                case R.id.caseBtn:
                    MagicClassicCaseFragment magicClassicCaseFragment = MagicClassicCaseFragment.newInstance("", "");
                    ((BaseActivity) getActivity()).navigationToFragment(magicClassicCaseFragment);
                    break;
            }
        }
    };
    private WebView mWebView;

    public static MagicDesignFragment newInstance(String page, String index) {
        MagicDesignFragment fragment = new MagicDesignFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATA, page);
        args.putSerializable(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    public MagicDesignFragment() {
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
            mView = inflater.inflate(R.layout.fragment_magic_design_page, container, false);
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

        backBtn = (TextView) mView.findViewById(R.id.ll_back);
        backBtn.setOnClickListener(onClickListener);

        qqBtn = (LinearLayout) mView.findViewById(R.id.design_rl);
        qqBtn.setOnClickListener(onClickListener);

        designBtn = (LinearLayout) mView.findViewById(R.id.case_rl);
        designBtn.setOnClickListener(onClickListener);

        caseBtn= (TextView) mView.findViewById(R.id.caseBtn);
        caseBtn.setOnClickListener(onClickListener);

        mWebView = (WebView) mView.findViewById(R.id.webView);
        WebSettings webSettings = mWebView.getSettings();
        //设置WebView属性，能够执行Javascript脚本
        webSettings.setJavaScriptEnabled(true);
        //设置可以访问文件
        webSettings.setAllowFileAccess(true);
        //设置支持缩放
        webSettings.setBuiltInZoomControls(true);
        //加载需要显示的网页
        mWebView.loadUrl(ZhaiDou.ONLINE_DESIGN_URL);

        //设置Web视图
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                System.out.println("url = " + url);
                if ("zhaidouappdesigncase://designcase".equalsIgnoreCase(url)) {
                    MagicClassicCaseFragment magicClassicCaseFragment = MagicClassicCaseFragment.newInstance("", "");
                    ((BaseActivity) getActivity()).navigationToFragment(magicClassicCaseFragment);
                    return true;
                } else if ("zhaidouappfaq://faq".equalsIgnoreCase(url)) {
                    MagicGuideFragment magicClassicCaseFragment = MagicGuideFragment.newInstance("", "");
                    ((BaseActivity) getActivity()).navigationToFragment(magicClassicCaseFragment);
                    return true;
                } else if (!TextUtils.isEmpty(url) && url.contains("zhaidouappproduct://")) {
                    String substring = url.substring(url.lastIndexOf("/") + 1, url.length());
                    System.out.println("zhaidouappproduct---substring = " + substring);
                    GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance("", substring);
                    ((BaseActivity) mContext).navigationToFragment(goodsDetailsFragment);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
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
