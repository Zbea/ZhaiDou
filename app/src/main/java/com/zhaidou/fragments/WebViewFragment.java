package com.zhaidou.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.activities.WebViewActivity;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;

public class WebViewFragment extends BaseFragment{

    private WebView webView;
    private String url;
    private boolean isShowTitle;

    public String preUrl;


    private OnFragmentInteractionListener mListener;

    public static WebViewFragment newInstance(String url,boolean isShowTitle) {
        WebViewFragment fragment = new WebViewFragment();
        Bundle args = new Bundle();
        args.putString("URL", url);
        args.putBoolean("isShowTitle",isShowTitle);
        fragment.setArguments(args);
        return fragment;
    }
    public WebViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            url = getArguments().getString("URL");
            isShowTitle=getArguments().getBoolean("isShowTitle");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.whole_projects, container, false);

        view.findViewById(R.id.rl_back).setVisibility(isShowTitle?View.VISIBLE:View.GONE);
        webView = (WebView) view.findViewById(R.id.strategyView);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Intent intent = new Intent();
                    intent.putExtra("url", url);
                    intent.putExtra("from","beauty");
                    intent.setClass(getActivity(), ItemDetailActivity.class);
                    getActivity().startActivity(intent);

                return true;
            }
//
//            public void onPageFinished(WebView view, String url) {
//                loading.hide();
//            }

        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        webView.loadUrl(url);

        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }
}
