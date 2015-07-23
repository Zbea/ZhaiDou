package com.zhaidou.fragments;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshScrollView;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.adapter.ShopTodaySpecialAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.model.TodayShopItem;
import com.zhaidou.view.ListViewForScrollView;
import com.zhaidou.view.TypeFaceTextView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by roy on 15/7/23.
 */
public class GoodsDetailsFragment extends BaseFragment {
    private static final String PAGE = "page";
    private static final String INDEX = "index";

    private String mPage;
    private int mIndex;
    private View mView;
    private Context mContext;

    private TypeFaceTextView backBtn,titleTv;

    /**
     * 下拉刷新
     */
    private PullToRefreshBase.OnRefreshListener2 refreshListener=new PullToRefreshBase.OnRefreshListener2()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView)
        {
//            new Handler().postDelayed(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    mScrollView.onRefreshComplete();
//                    items.removeAll(items);
//                    initDate();
//                    adapter.notifyDataSetChanged();
//                }
//            },2000);
        }
        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {
//            new Handler().postDelayed(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    mScrollView.onRefreshComplete();
//                    initDate();
//                    adapter.notifyDataSetChanged();
//                }
//            },2000);
        }
    };

    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener=new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            switch (view.getId())
            {
                case R.id.back_btn:
                    ((MainActivity)getActivity()).popToStack(GoodsDetailsFragment.this);
                break;
            }
        }
    };

    public static GoodsDetailsFragment newInstance(String page, int index) {
        GoodsDetailsFragment fragment = new GoodsDetailsFragment();
        Bundle args = new Bundle();
        args.putString(PAGE, page);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }
    public GoodsDetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getString(PAGE);
            mIndex = getArguments().getInt(INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView=inflater.inflate(R.layout.goods_details_page, container, false);
        mContext=getActivity();

        initView();

        return mView;
    }


    /**
     * 初始化数据
     */
    private void initView()
    {
        backBtn=(TypeFaceTextView)mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);
        titleTv=(TypeFaceTextView)mView.findViewById(R.id.title_tv);
        titleTv.setText(mPage);

    }

}
