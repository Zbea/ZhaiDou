package com.zhaidou.fragments;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

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
public class ShopTodaySpecialFragment extends BaseFragment {
    private static final String PAGE = "page";
    private static final String INDEX = "index";

    private String mPage;
    private int mIndex;
    private View mView;
    private Context mContext;

    private TypeFaceTextView backBtn,titleTv;
    private PullToRefreshScrollView mScrollView;
    private ListViewForScrollView mListView;

    private List<TodayShopItem> items=new ArrayList<TodayShopItem>();
    private ShopTodaySpecialAdapter adapter;

    /**
     * 下拉刷新
     */
    private PullToRefreshBase.OnRefreshListener2 refreshListener=new PullToRefreshBase.OnRefreshListener2()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView)
        {
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    mScrollView.onRefreshComplete();
                    items.removeAll(items);
                    initDate();
                    adapter.notifyDataSetChanged();
                }
            },2000);
        }
        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    mScrollView.onRefreshComplete();
                    initDate();
                    adapter.notifyDataSetChanged();
                }
            },2000);
        }
    };

    /**
     * adapter短点击事件
     */
    private AdapterView.OnItemClickListener onItemClickListener=new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
        {
            GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance(items.get(i).title, 0);
            ((MainActivity) getActivity()).navigationToFragment(goodsDetailsFragment);
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
                    ((MainActivity)getActivity()).popToStack(ShopTodaySpecialFragment.this);
                break;
            }
        }
    };

    public static ShopTodaySpecialFragment newInstance(String page, int index) {
        ShopTodaySpecialFragment fragment = new ShopTodaySpecialFragment();
        Bundle args = new Bundle();
        args.putString(PAGE, page);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }
    public ShopTodaySpecialFragment() {
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
        mView=inflater.inflate(R.layout.shop_today_special_page, container, false);
        mContext=getActivity();

        initView();
        initDate();

        return mView;
    }

    /**
     * 初始化数据
     */
    private void initDate()
    {
        for (int i = 0; i < 4; i++)
        {
            TodayShopItem todayShopItem=new TodayShopItem("简述护手霜哈哈哈哈哈哈哈哈啊哈哈啊哈哈啊哈哈哈哈哈啊哈哈","哈哈哈哈哈哈哈哈哈哈啊哈哈哈哈啊哈哈哈哈啊哈"
                    ,"http://stg.zhaidou.com/uploads/article/article/asset_img/303/99d2fa9df325d76ac941b246ecf1488c.jpg",1999,3000);
            items.add(todayShopItem);

        }
    }

    /**
     * 初始化数据
     */
    private void initView()
    {
        backBtn=(TypeFaceTextView)mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);
        titleTv=(TypeFaceTextView)mView.findViewById(R.id.title_tv);
        titleTv.setText(R.string.shop_taday_special_text);

        mScrollView = (PullToRefreshScrollView)mView.findViewById(R.id.scrollview);
        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mScrollView.setOnRefreshListener(refreshListener);

        mListView=(ListViewForScrollView)mView.findViewById(R.id.shopListView);
        adapter=new ShopTodaySpecialAdapter(mContext,items);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(onItemClickListener);


    }

}
