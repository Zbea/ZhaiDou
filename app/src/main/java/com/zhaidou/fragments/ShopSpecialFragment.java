package com.zhaidou.fragments;



import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshScrollView;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.adapter.ShopSpecialAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.model.ShopSpecialItem;
import com.zhaidou.model.TodayShopItem;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.ImageSwitchWall;
import com.zhaidou.view.ListViewForScrollView;
import com.zhaidou.view.TypeFaceTextView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by roy on 15/7/20.
 */
public class ShopSpecialFragment extends BaseFragment {
    private static final String PAGE = "page";
    private static final String INDEX = "index";

    private String mPage;
    private int mIndex;
    private View mView;
    private Context mContext;
    private String url="http://stg.zhaidou.com/uploads/article/article/asset_img/303/99d2fa9df325d76ac941b246ecf1488c.jpg";

    private ImageView adIv;
    private TypeFaceTextView backBtn,titleTv;
    private PullToRefreshScrollView mScrollView;
    private ListViewForScrollView mListView;
    private List<ShopSpecialItem> items=new ArrayList<ShopSpecialItem>();
    private ShopSpecialAdapter adapter;

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
            ShopTodaySpecialFragment shopTodaySpecialFragment = ShopTodaySpecialFragment.newInstance("", 0);
            ((MainActivity) getActivity()).navigationToFragment(shopTodaySpecialFragment);
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
                    ((MainActivity)getActivity()).popToStack(ShopSpecialFragment.this);
                break;
            }
        }
    };

    public static ShopSpecialFragment newInstance(String page, int index) {
        ShopSpecialFragment fragment = new ShopSpecialFragment();
        Bundle args = new Bundle();
        args.putString(PAGE, page);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }
    public ShopSpecialFragment() {
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
        mView=inflater.inflate(R.layout.shop_special_page, container, false);
        mContext=getActivity();

        initView();
        initDate();

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
        titleTv.setText(R.string.home_shop_special_text);
        adIv=(ImageView)mView.findViewById(R.id.shopAdImage);
        ToolUtils.setImageCacheUrl(url,adIv);

        mScrollView = (PullToRefreshScrollView)mView.findViewById(R.id.sv_special_scrollview);
        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mScrollView.setOnRefreshListener(refreshListener);

        mListView=(ListViewForScrollView)mView.findViewById(R.id.shopListView);
        adapter=new ShopSpecialAdapter(mContext,items);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(onItemClickListener);

    }

    /**
     * 初始化数据
     */
    private void initDate()
    {
        for (int i = 0; i < 4; i++)
        {
            ShopSpecialItem shopSpecialItem=new ShopSpecialItem(i,url,""+(i+1),""+(i+1),"DISSION女装专场"+i);
            items.add(shopSpecialItem);
        }
    }


}
