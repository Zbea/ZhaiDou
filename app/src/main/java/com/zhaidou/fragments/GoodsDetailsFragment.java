package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.viewpagerindicator.TabPageIndicator;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.adapter.GoodsImageAdapter;
import com.zhaidou.adapter.SpecificationAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.CartItem;
import com.zhaidou.model.CountTime;
import com.zhaidou.model.GoodDetail;
import com.zhaidou.model.GoodInfo;
import com.zhaidou.model.Specification;
import com.zhaidou.sqlite.CreatCartDB;
import com.zhaidou.sqlite.CreatCartTools;
import com.zhaidou.utils.CollectionUtils;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.ChildGridView;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class GoodsDetailsFragment extends BaseFragment {
    private static final String PAGE = "page";
    private static final String INDEX = "index";

    private String mPage;
    private int mIndex;
    private View mView;
    private Context mContext;
    private Dialog mDialog;

    private int count=0;
    private TextView backBtn, titleTv,mCartCount;
    private ImageView[] dots;
    private List<View> adPics = new ArrayList<View>();
    private ViewPager viewPager;
    private LinearLayout viewGroupe;//指示器容器
    private LinearLayout ljBtn;
    private RelativeLayout addCartBtn;
    private View myCartBtn;

    private GridView mGridView;
    private RequestQueue mRequestQueue;
    private TabPageIndicator mTabPageIndicator;
    private ViewPager mViewPager;
    private List<GoodInfo> goodInfos = new ArrayList<GoodInfo>();
    private int mSpecificationSelectPosition = -1;

    private TextView tv_comment, mCurrentPrice, mOldPrice, mDiscount, mTitle;

    private final int UPDATE_GOOD_DETAIL = 0;
    private final int UPDATE_COUNT_DOWN_TIME=1;
    private final int UPDATE_UI_TIMER_FINISH=2;
    private final int UPDATE_TIMER_START=3;

    private CreatCartDB creatCartDB;
    private List<CartItem> items = new ArrayList<CartItem>();

    private int num;
    private ScrollView scrollView;
    private ImageView topBtn;

    private GoodDetail detail;
    private SpecificationAdapter specificationAdapter;
    private GoodsImageAdapter imageAdapter;
    private Specification mSpecification;//选中规格
    private List<Specification> specificationList;
    private ImageView mTipView;
    private FrameLayout animation_viewGroup;
    //动画时间
    private int AnimationDuration = 1000;
    //正在执行的动画数量
    private int number = 0;
    //是否完成清理
    private boolean isClean = false;
    private MyTimer mTimer;
    private TextView mTimerView;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(ZhaiDou.IntentRefreshCartGoodsTag))
            {
                initCartTips();
            }
        }
    };

    private Handler myHandler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case 0:
                    //用来清除动画后留下的垃圾
                    try{
                        animation_viewGroup.removeAllViews();
                    }catch(Exception e){

                    }

                    isClean = false;

                    break;
                default:
                    break;
            }
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_GOOD_DETAIL:
                    GoodDetail detail = (GoodDetail) msg.obj;
                    mCurrentPrice.setText("￥" + detail.getPrice() + "");
                    mOldPrice.setText("￥" + detail.getCost_price() + "");
                    tv_comment.setText(detail.getDesigner());
                    mTitle.setText(detail.getTitle());
                    setDiscount(detail.getPrice(), detail.getCost_price());
                    specificationAdapter.addAll(detail.getSpecifications());

                    initData(detail.getImgs());

                    String end_date=detail.getEnd_time();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                    try{
                        long millionSeconds = sdf.parse(end_date).getTime();//毫秒
                        long hour=3600*1000;
                        long minute=60*1000;
                        millionSeconds=millionSeconds+hour*23+minute*59+59*1000;
                        long temp = millionSeconds-System.currentTimeMillis();
                        mTimer=new MyTimer(temp,1000);
                        mTimer.start();
                    }catch (Exception e){
                        Log.i("Exception e",e.getMessage());
                    }
                    break;
                case UPDATE_COUNT_DOWN_TIME:
                    CountTime time = (CountTime)msg.obj;
                    String timerFormat = getActivity().getResources().getString(R.string.timer);
                    String hourStr=String.format("%02d", time.getHour());
                    String minStr=String.format("%02d", time.getMinute());
                    String secondStr=String.format("%02d", time.getSecond());
                    String timer = String.format(timerFormat,time.getDay(),hourStr,minStr,secondStr);
                    mTimerView.setText(timer);
                    break;
                case UPDATE_UI_TIMER_FINISH:
                    mTimerView.setText("已结束");
                    break;
            }
        }
    };


    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i2) {
        }
        @Override
        public void onPageSelected(int i) {
            setImageBackground(i % adPics.size());
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.back_btn:
                    ((MainActivity) getActivity()).popToStack(GoodsDetailsFragment.this);
                    break;
                case R.id.goodsMyCartBtn:
                    ShopCartFragment shopCartFragment = ShopCartFragment.newInstance("", 0);
                    ((MainActivity) getActivity()).navigationToFragment(shopCartFragment);
                    break;
                case R.id.goodsLjBuyBtn:
                    buyGoods();
                    break;
                case R.id.goodsAddBuyBtn:
                    addGoods();
                    break;
                case R.id.goodsTop:
                    scrollView.scrollTo(0,0);
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

        mContext = getActivity();

        if (mView == null)
        {
            mView = inflater.inflate(R.layout.goods_details_page, container, false);
            initView();
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) mView.getParent();
        if (parent != null)
        {
            parent.removeView(mView);
        }


        return mView;
    }



    private void initView() {

        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");

        initBroadcastReceiver();

        backBtn = (TypeFaceTextView) mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);
        titleTv = (TypeFaceTextView) mView.findViewById(R.id.title_tv);
        titleTv.setText("商品详情");

        viewGroupe = (LinearLayout) mView.findViewById(R.id.goods_viewGroup);

        mGridView = (ChildGridView) mView.findViewById(R.id.gv_specification);

        myCartBtn = (View) mView.findViewById(R.id.goodsMyCartBtn);
        myCartBtn.setOnClickListener(onClickListener);
        ljBtn = (LinearLayout) mView.findViewById(R.id.goodsLjBuyBtn);
        ljBtn.setOnClickListener(onClickListener);
        addCartBtn = (RelativeLayout) mView.findViewById(R.id.goodsAddBuyBtn);
        addCartBtn.setOnClickListener(onClickListener);

        mView.findViewById(R.id.shopping_cart).setOnClickListener(onClickListener);
        tv_comment = (TextView) mView.findViewById(R.id.tv_comment);
        mCurrentPrice = (TextView) mView.findViewById(R.id.goodsCurrentPrice);
        mOldPrice = (TextView) mView.findViewById(R.id.goodsFormerPrice);
        mOldPrice.getPaint().setAntiAlias(true);
        mOldPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);

        mCartCount=(TextView)mView.findViewById(R.id.tv_cart_count);
        mTipView=(ImageView)mView.findViewById(R.id.myCartTipsTv);
        mDiscount = (TextView) mView.findViewById(R.id.tv_discount);
        mTitle = (TextView) mView.findViewById(R.id.tv_title);
        mTimerView=(TextView)mView.findViewById(R.id.tv_count_time);
        animation_viewGroup = createAnimLayout();
        mRequestQueue = Volley.newRequestQueue(getActivity());

        scrollView=(ScrollView)mView.findViewById(R.id.sv_goods_detail);
        scrollView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                if (motionEvent.getAction()==MotionEvent.ACTION_MOVE)
                {
                    int scrollY=view.getScrollY();
                    if(scrollY!=0)
                    {
                        topBtn.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        topBtn.setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });
        topBtn=(ImageView)mView.findViewById(R.id.goodsTop);
        topBtn.setOnClickListener(onClickListener);

        mTabPageIndicator = (TabPageIndicator) mView.findViewById(R.id.tab_goods_detail);
        mViewPager = (ViewPager) mView.findViewById(R.id.vp_goods_detail);
        mViewPager.setAdapter(new GoodsDetailFragmentAdapter(getChildFragmentManager(), goodInfos));
        mTabPageIndicator.setViewPager(mViewPager);
        specificationAdapter = new SpecificationAdapter(getActivity(), new ArrayList<Specification>(), mSpecificationSelectPosition);
        mGridView.setAdapter(specificationAdapter);

        specificationAdapter.setOnInViewClickListener(R.id.sizeTitleTv, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                specificationAdapter.setCheckPosition(mSpecificationSelectPosition = position);
                specificationAdapter.notifyDataSetChanged();

                sizeEvent(position);

            }
        });

        creatCartDB = new CreatCartDB(mContext);
        initCartTips();

        FetchDetailData(mIndex);

    }

    /**
     * 选择规格事件处理
     */
    private void sizeEvent(int position)
    {
        if (detail != null & specificationList.size()>0)
        {
            mSpecification = specificationList.get(position);
            mCurrentPrice.setText("￥"+mSpecification.price);
            setDiscount(mSpecification.price,detail.getCost_price());
        }
    }

    /**
     * 折扣处理事件
     * @param current
     * @param old
     */
    private void setDiscount(double current,double old)
    {
        mDiscount.setVisibility(View.VISIBLE);
        if(current!=0&old!=0)
        {
            DecimalFormat df = new DecimalFormat("##.0");
            String zk=df.format(current/old*10);
            if (zk.contains(".0"))
            {
                int sales=(int)Double.parseDouble(zk);
                mDiscount.setText(sales+"折");
            }
            else
            {
                Double sales=Double.parseDouble(zk);
                mDiscount.setText(sales+"折");
            }
        }
        else
        {
            mDiscount.setVisibility(View.GONE);
        }
    }

    /**
     * 注册广播
     */
    private void initBroadcastReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ZhaiDou.IntentRefreshCartGoodsTag);
        mContext.registerReceiver(broadcastReceiver, intentFilter);
    }

    /**
     * 红色标识提示显示数量
     */
    private void initCartTips()
    {
        items = CreatCartTools.selectByAll(creatCartDB);
        for (int i = 0; i < items.size(); i++)
        {
            if (items.get(i).isPublish.equals("true")|items.get(i).isOver.equals("true"))
            {
                items.remove(items.get(i));
            }
        }
        if (items.size() > 0)
        {
            num = 0;
            for (int i = 0; i < items.size(); i++)
            {
                num = num + items.get(i).num;
            }
            mCartCount.setVisibility(View.VISIBLE);
            mCartCount.setText("" + num);
        } else
        {
            mCartCount.setVisibility(View.GONE);
        }
    }


    /**
     * 立即购买
     */
    private void buyGoods()
    {
        if (detail != null)
            if (mSpecification != null)
            {
                CartItem cartItem = new CartItem();
                cartItem.id = detail.getId();
                cartItem.name = detail.getTitle();
                cartItem.creatTime = System.currentTimeMillis();
                cartItem.imageUrl = detail.getImgs().get(0);
                cartItem.currentPrice = mSpecification.price;//规格的价格
                cartItem.formalPrice = detail.getCost_price();
                DecimalFormat df = new DecimalFormat("##.0");
                double saveMoney = Double.parseDouble(df.format(detail.getCost_price() - mSpecification.price));
                cartItem.saveMoney = saveMoney;
                cartItem.saveTotalMoney = saveMoney;
                cartItem.totalMoney = detail.getPrice();
                cartItem.num = 1;
                cartItem.size = mSpecification.getTitle();
                cartItem.sizeId = mSpecification.getId();
                cartItem.isPublish = "false";

                ArrayList<CartItem> itemsCheck = new ArrayList<CartItem>();
                itemsCheck.add(cartItem);

                ShopOrderOkFragment shopOrderOkFragment = ShopOrderOkFragment.newInstance("", 0);
                Bundle bundle = new Bundle();
                bundle.putSerializable("goodsList", itemsCheck);
                shopOrderOkFragment.setArguments(bundle);
                ((MainActivity) getActivity()).navigationToFragment(shopOrderOkFragment);

            } else
            {
                Toast.makeText(mContext, "抱歉,先选择规格", Toast.LENGTH_SHORT).show();
            }
    }

    /**
     * 添加商品
     */
    private void addGoods()
    {
        if (detail != null)
        {
            if (mSpecification != null)
            {
                int[] location = new int[2];
                mTipView.getLocationInWindow(location);
                Drawable drawable =mTipView.getDrawable();
                doAnim(drawable,location);

                items = CreatCartTools.selectByAll(creatCartDB);
                CartItem cartItem = new CartItem();
                cartItem.id = detail.getId();
                cartItem.name = detail.getTitle();
                cartItem.creatTime = System.currentTimeMillis();
                cartItem.imageUrl = detail.getImgs().get(0);
                cartItem.currentPrice = mSpecification.price;//规格的价格
                cartItem.formalPrice = detail.getCost_price();
                DecimalFormat df = new DecimalFormat("##.0");
                double saveMoney = Double.parseDouble(df.format(detail.getCost_price() - mSpecification.price));
                cartItem.saveMoney = saveMoney;
                cartItem.saveTotalMoney = saveMoney;
                cartItem.totalMoney = detail.getPrice();
                cartItem.num = 1;
                cartItem.size = mSpecification.getTitle();
                cartItem.sizeId = mSpecification.getId();
                cartItem.isPublish = "false";
                cartItem.isOver = "false";

                CreatCartTools.insertByData(creatCartDB, items, cartItem);

                Intent intent=new Intent(ZhaiDou.IntentRefreshCartGoodsTag);
                mContext.sendBroadcast(intent);
//                List<Specification> itemsSp=detail.getSpecifications();
//                for (int i = 0; i <itemsSp.size() ; i++)
//                {
//                    if (mSpecification.getId() == itemsSp.get(i).getId())
//                    {
//                        itemsSp.get(i).num=itemsSp.get(i).num-1;
//                    }
//                }

            } else
            {
                Toast.makeText(mContext, "抱歉,先选择规格", Toast.LENGTH_SHORT).show();
            }

        }
    }

    /**
     * 初始化数据
     */
    private void initData(List<String> urls) {
        viewGroupe.removeAllViews();
        if (CollectionUtils.isNotNull(urls))
        {
            for (String url : urls) {
                ImageView imageView = new ImageView(mContext);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                imageView.setLayoutParams(params);
                ToolUtils.setImageCacheUrl(url, imageView);
                adPics.add(imageView);
            }
            dots = new ImageView[adPics.size()];

            for (int i = 0; i < adPics.size(); i++) {
                ImageView dot_iv = new ImageView(mContext);
                dot_iv.setScaleType(ImageView.ScaleType.FIT_XY);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.bottomMargin = 10;
                if (i == 0) {
                    params.leftMargin = 0;
                } else {
                    params.leftMargin = 20;
                }

                dot_iv.setLayoutParams(params);
                dots[i] = dot_iv;
                viewGroupe.addView(dot_iv);
                if (i == 0) {
                    dots[i].setBackgroundResource(R.drawable.home_tips_foucs_icon);
                } else {
                    dots[i].setBackgroundResource(R.drawable.home_tips_icon);
                }
            }
            viewPager = (ViewPager) mView.findViewById(R.id.goods_adv_pager);
            imageAdapter = new GoodsImageAdapter(mContext, adPics);
            viewPager.setAdapter(imageAdapter);
            viewPager.setOnPageChangeListener(onPageChangeListener);
        }
    }

    /**
     * 设置指示器
     */
    private void setImageBackground(int position) {
        for (int i = 0; i < dots.length; i++) {
            if (i == position) {
                dots[i].setBackgroundResource(R.drawable.home_tips_foucs_icon);
            } else {
                dots[i].setBackgroundResource(R.drawable.home_tips_icon);
            }
        }
    }

    public void FetchDetailData(int id) {
        String url=ZhaiDou.goodsDetailsUrlUrl+id;
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {

                if (mDialog!=null)
                    mDialog.dismiss();

                if (jsonObject != null) {
                    Log.i("jsonObject!=null---------------------->", jsonObject.toString());
                    JSONObject merchandise = jsonObject.optJSONObject("merchandise");
                    int id = merchandise.optInt("id");
                    String title = merchandise.optString("title");
                    String designer = merchandise.optString("designer");
                    int total_count = merchandise.optInt("total_count");
                    double price = merchandise.optDouble("price");
                    double cost_price = merchandise.optDouble("cost_price");
                    int discount = merchandise.optInt("discount");
                    String end_time=merchandise.optString("end_time");
                    detail = new GoodDetail(id, title, designer, total_count, price, cost_price, discount);
                    detail.setEnd_time(end_time);

                    JSONArray imgsArray = merchandise.optJSONArray("imgs");
                    if (imgsArray != null && imgsArray.length() > 0) {
                        List<String> imgsList = new ArrayList<String>();
                        for (int i = 0; i < imgsArray.length(); i++) {
                            JSONObject imgObj = imgsArray.optJSONObject(i);
                            String url = imgObj.optString("url");
                            imgsList.add(url);
                        }
                        detail.setImgs(imgsList);
                    }

                    JSONArray specifications = merchandise.optJSONArray("specifications");
                    if (specifications != null && specifications.length() > 0) {
                        specificationList = new ArrayList<Specification>();
                        for (int i = 0; i < specifications.length(); i++) {
                            JSONObject specificationObj = specifications.optJSONObject(i);
                            int specificationId = specificationObj.optInt("id");
                            String specificationTitle = specificationObj.optString("title");
                            int num = specificationObj.optInt("count");
                            double sizePrice = specificationObj.optDouble("price");

                            Specification specification = new Specification(specificationId, specificationTitle,num,sizePrice);
                            specificationList.add(specification);
                        }
                        detail.setSpecifications(specificationList);
                    }
                    Message message = new Message();
                    message.what = UPDATE_GOOD_DETAIL;
                    message.obj = detail;
                    handler.sendMessage(message);
                } else {
                    ShowToast("加载出错");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (mDialog!=null)
                mDialog.dismiss();
                Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
            }
        });
        mRequestQueue.add(request);
    }

    private class GoodsDetailFragmentAdapter extends FragmentPagerAdapter {
        private List<GoodInfo> mData;

        public GoodsDetailFragmentAdapter(FragmentManager fragmentManager, List<GoodInfo> infos) {
            super(fragmentManager);
            mData = infos;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    return GoodsDetailsChildFragment.newInstance(mData, mIndex);
                }
                case 1: {
                    return SaleServiceFragment.newInstance("", "");
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
            if (position == 0)
                return "商品信息";
            return "咨询与售后服务";
        }
    }





    private void doAnim(Drawable drawable,int[] start_location){
        if(!isClean){
            setAnim(drawable,start_location);
        }else{
            try{
                animation_viewGroup.removeAllViews();
                isClean = false;
                setAnim(drawable,start_location);
            }catch(Exception e){
                e.printStackTrace();
            }
            finally{
                isClean = true;
            }
        }
    }

    private void setAnim(Drawable drawable,int[] start_location){
        Animation mScaleAnimation = new ScaleAnimation(1.5f,0.0f,1.5f,0.0f,Animation.RELATIVE_TO_SELF,0.1f,Animation.RELATIVE_TO_SELF,0.1f);
        mScaleAnimation.setDuration(AnimationDuration);
        mScaleAnimation.setFillAfter(true);


        final ImageView iview = new ImageView(getActivity());
        iview.setImageDrawable(drawable);
        final View view = addViewToAnimLayout(animation_viewGroup,iview,start_location);
        view.setAlpha(0.6f);

        int[] end_location = new int[2];
        myCartBtn.getLocationInWindow(end_location);
        int endX =-start_location[0]+dip2px(getActivity(),30);
        int endY = end_location[1]-start_location[1];

        Animation mTranslateAnimation = new TranslateAnimation(0,endX,0,endY);
        Animation mRotateAnimation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateAnimation.setDuration(AnimationDuration);
        mTranslateAnimation.setDuration(AnimationDuration);
        AnimationSet mAnimationSet = new AnimationSet(true);

        mAnimationSet.setFillAfter(true);
//        mAnimationSet.addAnimation(mRotateAnimation);
        mAnimationSet.addAnimation(mScaleAnimation);
        mAnimationSet.addAnimation(mTranslateAnimation);

        mAnimationSet.setAnimationListener(new Animation.AnimationListener(){

            @Override
            public void onAnimationStart(Animation animation) {
                number++;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                number--;
                if(number==0){
                    isClean = true;
                    myHandler.sendEmptyMessage(0);
                }
                mTipView.setVisibility(View.GONE);
//                mCartCount.setText("" + num);
//                mCartCount.setVisibility(count==0?View.GONE:View.VISIBLE);
                initCartTips();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(mAnimationSet);

    }

    private View addViewToAnimLayout(ViewGroup vg,View view,int[] location){
        int x = location[0];
        int y = location[1];
        vg.addView(view);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                dip2px(getActivity(),90),dip2px(getActivity(),90));
        lp.leftMargin = x;
        lp.topMargin = y;
        view.setPadding(5, 5, 5, 5);
        view.setLayoutParams(lp);

        return view;
    }

    private int dip2px(Context context,float dpValue){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale +0.5f);
    }

    private FrameLayout createAnimLayout(){
        ViewGroup rootView = (ViewGroup)getActivity().getWindow().getDecorView();
        FrameLayout animLayout = new FrameLayout(getActivity());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
        animLayout.setLayoutParams(lp);
        animLayout.setBackgroundResource(android.R.color.transparent);
        rootView.addView(animLayout);
        return animLayout;

    }

    private class MyTimer extends CountDownTimer {
        private MyTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            long day=24*3600*1000;
            long hour=3600*1000;
            long minute=60*1000;
            //两个日期想减得到天数
            long dayCount= l/day;
            long hourCount= (l-(dayCount*day))/hour;
            long minCount=(l-(dayCount*day)-(hour*hourCount))/minute;
            long secondCount=(l-(dayCount*day)-(hour*hourCount)-(minCount*minute))/1000;
            CountTime time = new CountTime(dayCount,hourCount,minCount,secondCount);
            Message message =new Message();
            message.what=UPDATE_COUNT_DOWN_TIME;
            message.obj=time;
            handler.sendMessage(message);
        }

        @Override
        public void onFinish() {
            handler.sendEmptyMessage(UPDATE_UI_TIMER_FINISH);
        }
    }
    @Override
    public void onDestroyView() {
        if (mTimer!=null){
            mTimer.cancel();
            mTimer=null;
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy()
    {
        mContext.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}
