package com.zhaidou.fragments;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pulltorefresh.PullToRefreshBase;
import com.viewpagerindicator.TabPageIndicator;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.adapter.GoodsImageAdapter;
import com.zhaidou.adapter.SpecificationAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.model.GoodDetail;
import com.zhaidou.model.GoodInfo;
import com.zhaidou.model.Specification;
import com.zhaidou.utils.CollectionUtils;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.ChildGridView;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

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

    private GridView mGridView;
    private RequestQueue mRequestQueue;
    private TabPageIndicator mTabPageIndicator;
    private ViewPager mViewPager;
    private List<GoodInfo> goodInfos = new ArrayList<GoodInfo>();
    private int mSpecificationSelectPosition=0;

    private TextView tv_comment,mCurrentPrice,mOldPrice,mDiscount,mTitle;

    private static final int UPDATE_GOOD_DETAIL=0;

    SpecificationAdapter specificationAdapter;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.i("handleMessage------------>", msg.what + "");
            switch (msg.what){
                case UPDATE_GOOD_DETAIL:
                    GoodDetail detail=(GoodDetail)msg.obj;
                    mCurrentPrice.setText("￥"+detail.getPrice()+"");
                    mOldPrice.setText("￥"+detail.getCost_price()+"");
                    tv_comment.setText(detail.getDesigner());
                    mTitle.setText(detail.getTitle());
                    mDiscount.setText(detail.getDiscount()+"折");
                    specificationAdapter.addAll(detail.getSpecifications());

                    List<String> urls=new ArrayList<String>();
                    initData(urls);
                    break;
            }
        }
    };
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

        mTabPageIndicator=(TabPageIndicator)mView.findViewById(R.id.tab_goods_detail);
        mViewPager=(ViewPager)mView.findViewById(R.id.vp_goods_detail);
        mViewPager.setAdapter(new GoodsDetailFragmentAdapter(getChildFragmentManager(),goodInfos));
        mTabPageIndicator.setViewPager(mViewPager);
        specificationAdapter=new SpecificationAdapter(getActivity(),new ArrayList<Specification>(),mSpecificationSelectPosition);
        mGridView.setAdapter(specificationAdapter);

        specificationAdapter.setOnInViewClickListener(R.id.sizeTitleTv,new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                specificationAdapter.setCheckPosition(mSpecificationSelectPosition=position);
                specificationAdapter.notifyDataSetChanged();
            }
        });
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

        mGridView=(ChildGridView)mView.findViewById(R.id.gv_specification);

        myCartBtn=(LinearLayout)mView.findViewById(R.id.goodsMyCartBtn);
        myCartBtn.setOnClickListener(onClickListener);
        ljBtn=(LinearLayout)mView.findViewById(R.id.goodsLjBuyBtn);
        ljBtn.setOnClickListener(onClickListener);
        addCartBtn=(LinearLayout)mView.findViewById(R.id.goodsAddBuyBtn);
        addCartBtn.setOnClickListener(onClickListener);

        tv_comment=(TextView)mView.findViewById(R.id.tv_comment);
        mCurrentPrice=(TextView)mView.findViewById(R.id.goodsCurrentPrice);
        mOldPrice=(TextView)mView.findViewById(R.id.goodsFormerPrice);
        mOldPrice.getPaint().setAntiAlias(true);
        mOldPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);

        mDiscount=(TextView)mView.findViewById(R.id.tv_discount);
        mTitle=(TextView)mView.findViewById(R.id.tv_title);

        mRequestQueue= Volley.newRequestQueue(getActivity());

        initData(null);
        FetchDetailData();

    }

    /**
     * 初始化数据
     */
    private void initData(List<String> urls)
    {
        if (CollectionUtils.isNotNull(urls)){
            for (String url:urls){
                ImageView imageView=new ImageView(mContext);
                ToolUtils.setImageCacheUrl(url,imageView);
                adPics.add(imageView);
            }
            dots=new ImageView[adPics.size()];
            for (int i = 0; i < adPics.size(); i++){
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
            viewPager.setCurrentItem(adPics.size() * 100);
        }

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

    public void FetchDetailData(){
        JsonObjectRequest request=new JsonObjectRequest("http://192.168.199.173/special_mall/api/merchandises/6",new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {

                if (jsonObject!=null){
                    Log.i("jsonObject!=null---------------------->",jsonObject.toString());
                    JSONObject merchandise=jsonObject.optJSONObject("merchandise");
                    int id=merchandise.optInt("id");
                    String title=merchandise.optString("title");
                    String designer=merchandise.optString("designer");
                    int total_count=merchandise.optInt("total_count");
                    double price=merchandise.optDouble("price");
                    double cost_price=merchandise.optDouble("cost_price");
                    int discount=merchandise.optInt("discount");
                    GoodDetail detail=new GoodDetail(id,title,designer,total_count,price,cost_price,discount);

                    JSONArray imgsArray = merchandise.optJSONArray("imgs");
                    if (imgsArray!=null&&imgsArray.length()>0){
                        List<String> imgsList=new ArrayList<String>();
                        for (int i=0;i<imgsArray.length();i++){
                            JSONObject imgObj = imgsArray.optJSONObject(i);
                            String url=imgObj.optString("url");
                            imgsList.add(url);
                        }
                        detail.setImgs(imgsList);
                    }

                    JSONArray specifications=merchandise.optJSONArray("specifications");
                    if (specifications!=null&&specifications.length()>0){
                        List<Specification> specificationList=new ArrayList<Specification>();
                        for (int i=0;i<specifications.length();i++){
                            JSONObject specificationObj=specifications.optJSONObject(i);
                            int specificationId=specificationObj.optInt("id");
                            String specificationTitle=specificationObj.optString("title");
                            Specification specification=new Specification(specificationId,specificationTitle);
                            specificationList.add(specification);
                        }
                        detail.setSpecifications(specificationList);
                    }
                    Message message=new Message();
                    message.what=UPDATE_GOOD_DETAIL;
                    message.obj=detail;
                    handler.sendMessage(message);
                }else {
                    ShowToast("加载出错");
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getActivity(),"网络异常",Toast.LENGTH_SHORT).show();
            }
        });
        mRequestQueue.add(request);
    }

    private class GoodsDetailFragmentAdapter extends FragmentPagerAdapter {
        private List<GoodInfo> mData;
        public GoodsDetailFragmentAdapter(FragmentManager fragmentManager,List<GoodInfo> infos) {
            super(fragmentManager);
            mData=infos;
        }
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    return GoodsDetailsChildFragment.newInstance(mData,0);
                }
                case 1: {
                    return SaleServiceFragment.newInstance("","");
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position==0)
                return "商品信息";
            return "咨询与售后服务";
        }

    }
}
