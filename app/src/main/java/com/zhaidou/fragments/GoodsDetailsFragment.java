package com.zhaidou.fragments;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshScrollView;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.adapter.AdViewAdpater;
import com.zhaidou.adapter.GoodsImageAdapter;
import com.zhaidou.adapter.GoodsSizeAdapter;
import com.zhaidou.adapter.ShopTodaySpecialAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.model.GoodsSizeItem;
import com.zhaidou.model.SwitchImage;
import com.zhaidou.model.TodayShopItem;
import com.zhaidou.view.ChildGridView;
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
    private ImageView[] dots;
    private List<View> adPics = new ArrayList<View>();
    private ViewPager viewPager;
    private LinearLayout viewGroupe;//指示器容器
    private LinearLayout myCartBtn,ljBtn,addCartBtn;

    private ChildGridView colorGridView,sizeGridView;
    private List<GoodsSizeItem> itemsColor=new ArrayList<GoodsSizeItem>();//color集合
    private GoodsSizeAdapter adapterColor;
    private List<GoodsSizeItem> itemsSize=new ArrayList<GoodsSizeItem>();//size集合
    private GoodsSizeAdapter adapterSize;


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

    private ViewPager.OnPageChangeListener onPageChangeListener=new ViewPager.OnPageChangeListener()
    {
        @Override
        public void onPageScrolled(int i, float v, int i2)
        {
        }
        @Override
        public void onPageSelected(int i)
        {
            setImageBackground(i%adPics.size());
        }
        @Override
        public void onPageScrollStateChanged(int i)
        {

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
                case R.id.goodsMyCartBtn:
                    ShopCartFragment shopCartFragment=ShopCartFragment.newInstance("",0);
                    ((MainActivity)getActivity()).navigationToFragment(shopCartFragment);
                    break;
                case R.id.goodsLjBuyBtn:
                    ShopOrderOkFragment shopOrderOkFragment=ShopOrderOkFragment.newInstance("",0);
                    ((MainActivity)getActivity()).navigationToFragment(shopOrderOkFragment);
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
        titleTv.setText("商品详情");

        viewGroupe=(LinearLayout)mView.findViewById(R.id.goods_viewGroup);
        viewPager=(ViewPager)mView.findViewById(R.id.goods_adv_pager);

        colorGridView=(ChildGridView)mView.findViewById(R.id.goodsColorListView);
        sizeGridView=(ChildGridView)mView.findViewById(R.id.goodsSizeListView);

        myCartBtn=(LinearLayout)mView.findViewById(R.id.goodsMyCartBtn);
        myCartBtn.setOnClickListener(onClickListener);
        ljBtn=(LinearLayout)mView.findViewById(R.id.goodsLjBuyBtn);
        ljBtn.setOnClickListener(onClickListener);
        addCartBtn=(LinearLayout)mView.findViewById(R.id.goodsAddBuyBtn);
        addCartBtn.setOnClickListener(onClickListener);


        initData();

        dots=new ImageView[adPics.size()];
        for (int i = 0; i < adPics.size(); i++)
        {
            ImageView dot_iv = new ImageView(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = 10;
            if (i == 0)
            {
                params.leftMargin = 0;
            } else
            {
                params.leftMargin = 20;
            }

            dot_iv.setLayoutParams(params);
            dots[i] = dot_iv;
            viewGroupe.addView(dot_iv);
            if (i == 0)
            {
                dots[i].setBackgroundResource(R.drawable.home_tips_foucs_icon);
            } else
            {
                dots[i].setBackgroundResource(R.drawable.home_tips_icon);
            }
        }
        GoodsImageAdapter adapter=new GoodsImageAdapter(mContext,adPics);
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(onPageChangeListener);
        viewPager.setCurrentItem(adPics.size()*100);

        adapterColor=new GoodsSizeAdapter(mContext,itemsColor);
        colorGridView.setAdapter(adapterColor);

        adapterSize=new GoodsSizeAdapter(mContext,itemsSize);
        sizeGridView.setAdapter(adapterSize);

    }

    /**
     * 初始化数据
     */
    private void initData()
    {
        ImageView imageView=new ImageView(mContext);
        imageView.setImageResource(R.drawable.goods1);
//        imageView.setBackgroundResource(R.drawable.goods1);
        adPics.add(imageView);

        ImageView imageView1=new ImageView(mContext);
        imageView1.setImageResource(R.drawable.goods2);
//        imageView1.setBackgroundResource(R.drawable.goods2);
        adPics.add(imageView1);

        ImageView imageView2=new ImageView(mContext);
        imageView2.setImageResource(R.drawable.goods3);
//        imageView2.setBackgroundResource(R.drawable.goods3);
        adPics.add(imageView2);

        ImageView imageView3=new ImageView(mContext);
        imageView3.setImageResource(R.drawable.goods4);
//        imageView3.setBackgroundResource(R.drawable.goods4);
        adPics.add(imageView3);

        GoodsSizeItem goodsSizeItem=new GoodsSizeItem(1,"蓝色",false,false);
        itemsColor.add(goodsSizeItem);
        GoodsSizeItem goodsSizeItem1=new GoodsSizeItem(1,"红色",false,false);
        itemsColor.add(goodsSizeItem1);
        GoodsSizeItem goodsSizeItem2=new GoodsSizeItem(1,"蓝色",true,false);
        itemsColor.add(goodsSizeItem2);
        GoodsSizeItem goodsSizeItem4=new GoodsSizeItem(1,"蓝色",false,false);
        itemsColor.add(goodsSizeItem4);

        GoodsSizeItem goodsSizeItem5=new GoodsSizeItem(1,"M",false,false);
        itemsSize.add(goodsSizeItem5);
        GoodsSizeItem goodsSizeItem6=new GoodsSizeItem(1,"L",false,false);
        itemsSize.add(goodsSizeItem6);
        GoodsSizeItem goodsSizeItem7=new GoodsSizeItem(1,"XL",true,false);
        itemsSize.add(goodsSizeItem7);

    }

    /**
     * 设置指示器
     */
    private void setImageBackground(int position)
    {
        for(int i=0; i<dots.length; i++)
        {
            if (i == position)
            {
                dots[i].setBackgroundResource(R.drawable.home_tips_foucs_icon);
            } else
            {
                dots[i].setBackgroundResource(R.drawable.home_tips_icon);
            }
        }
    }

}
