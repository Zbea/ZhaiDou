package com.zhaidou.fragments;



import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.pulltorefresh.PullToRefreshExpandableListView;
import com.pulltorefresh.PullToRefreshListView;
import com.zhaidou.R;
import com.zhaidou.adapter.TimeLineAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.ChildStatusEntity;
import com.zhaidou.model.GroupStatusEntity;
import com.zhaidou.model.Logistics;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LogisticsMsgFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class LogisticsMsgFragment extends BaseFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ExpandableListView mLogisticsView;

    private TimeLineAdapter statusAdapter;
    private Context context;
    private WebView mWebView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LogisticsMsgFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LogisticsMsgFragment newInstance(String param1, String param2) {
        LogisticsMsgFragment fragment = new LogisticsMsgFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public LogisticsMsgFragment() {
        // Required empty public constructor
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
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_logistics, container, false);
        context = getActivity();
        mLogisticsView = (ExpandableListView)view.findViewById(R.id.lv_logistics);
        mWebView=(WebView)view.findViewById(R.id.wv_logistics);
        mWebView.getSettings().setJavaScriptEnabled(true);
        WebSettings webSettings = mWebView.getSettings();
//        webSettings.setUseWideViewPort(true);
//        webSettings.setLoadWithOverviewMode(true);
//        mWebView.setVerticalScrollBarEnabled(false);
//        mWebView.setVerticalScrollbarOverlay(false);
//        mWebView.setHorizontalScrollbarOverlay(false);
//        mWebView.setHorizontalFadingEdgeEnabled(false);
//        mWebView.setInitialScale(1);
        mWebView.loadUrl("http://m.kuaidi100.com/index_all.html?type=huitongkuaidi&postid=50109307408462#result");

        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                mWebView.loadUrl("javascript:$('.smart-header').remove();$('.adsbygoogle').hide();$('#result').css('padding-top','0px');" +
                        "$('.smart-footer').remove();");
//                mWebView.loadUrl("javascript:$('.adsbygoogle').hide();");
//                mWebView.loadUrl("javascript:$('#result').css('padding-top','0px');");
//                mWebView.loadUrl("javascript:$('.smart-footer').removeClass('ui-footer-fixed');");
            }
        });
        initExpandListView();
        return view;
    }

    private void initExpandListView() {
        statusAdapter = new TimeLineAdapter(context, getListData());
        mLogisticsView.setAdapter(statusAdapter);
        mLogisticsView.setGroupIndicator(null); // 去掉默认带的箭头
        mLogisticsView.setSelection(0);// 设置默认选中项

        // 遍历所有group,将所有项设置成默认展开
        int groupCount = mLogisticsView.getCount();
        for (int i = 0; i < groupCount; i++) {
            mLogisticsView.expandGroup(i);
        }

        mLogisticsView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                // TODO Auto-generated method stub
                return true;
            }
        });
    }

    private List<GroupStatusEntity> getListData() {
        List<GroupStatusEntity> groupList;
        String[] strArray = new String[] { "10月22日方法嘎嘎水电费撒咖啡壶看撒富士康大公司酷狗", "10月23日", "10月25日" };
        String[][] childTimeArray = new String[][] {
                { " ", " ", " ", " " },
                { " " }, { " " } };
        groupList = new ArrayList<GroupStatusEntity>();
        for (int i = 0; i < strArray.length; i++) {
            GroupStatusEntity groupStatusEntity = new GroupStatusEntity();
            groupStatusEntity.setGroupName(strArray[i]);

            List<ChildStatusEntity> childList = new ArrayList<ChildStatusEntity>();

            for (int j = 0; j < childTimeArray[i].length; j++) {
                ChildStatusEntity childStatusEntity = new ChildStatusEntity();
                childStatusEntity.setCompleteTime(childTimeArray[i][j]);
                childStatusEntity.setIsfinished(true);
                childList.add(childStatusEntity);
            }

            groupStatusEntity.setChildList(childList);
            groupList.add(groupStatusEntity);
        }
        return groupList;
    }
}