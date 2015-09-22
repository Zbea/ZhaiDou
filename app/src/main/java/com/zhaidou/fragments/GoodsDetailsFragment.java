package com.zhaidou.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
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
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.LoginActivity;
import com.zhaidou.adapter.GoodsImageAdapter;
import com.zhaidou.adapter.SpecificationAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.dialog.CustomToastDialog;
import com.zhaidou.model.CartItem;
import com.zhaidou.model.CountTime;
import com.zhaidou.model.GoodDetail;
import com.zhaidou.model.GoodInfo;
import com.zhaidou.model.Specification;
import com.zhaidou.sqlite.CreatCartDB;
import com.zhaidou.sqlite.CreatCartTools;
import com.zhaidou.utils.CollectionUtils;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.ChildGridView;
import com.zhaidou.view.LargeImgView;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

public class GoodsDetailsFragment extends BaseFragment {
    private static final String PAGE = "page";
    private static final String INDEX = "index";

    private String mPage;
    private int mIndex;
    private View mView;
    private int flags;//1代表零元特卖；2代表已下架商品
    private Context mContext;
    private int count = 0;
    private ImageView shareBtn;
    private String shareUrl = ZhaiDou.goodsDetailsShareUrl;
    private TextView backBtn, titleTv, mCartCount;
    private ImageView[] dots;
    private List<View> adPics = new ArrayList<View>();
    private ViewPager viewPager;
    private RelativeLayout relativeLayout;
    private LinearLayout viewGroupe;//指示器容器
    private LinearLayout ljBtn;
    private LinearLayout addCartBtn;
    private TextView publishBtn;
    private View myCartBtn;

    private Dialog mDialog;
    private GridView mGridView;
    private RequestQueue mRequestQueue;

    private ArrayList<GoodInfo> goodInfos = new ArrayList<GoodInfo>();
    private int mSpecificationSelectPosition = -1;

    private TextView tv_comment, mCurrentPrice, mOldPrice, mDiscount, mTitle;


    private final int UPDATE_GOOD_DETAIL = 0;
    private final int UPDATE_COUNT_DOWN_TIME = 1;
    private final int UPDATE_UI_TIMER_FINISH = 2;
    private final int UPDATE_TIMER_START = 3;
    private final int UPDATE_CARTCAR_DATA=4;
    private final int UPDATE_LJBUY_ISOSALEBUY = 5;//零元特卖立即购买时候判断是否已经购买郭
    private CreatCartDB creatCartDB;

    private final int UPDATE_ISOSALEBUY = 6;//判断进来时候零元特卖是否已经购买了，购买了才让按钮不能点击
    private List<CartItem> items = new ArrayList<CartItem>();

    private int num;
    private ScrollView scrollView;
    private ImageView topBtn;
    private LinearLayout iconView, iconOSaleView,commentView;

    private LinearLayout loadingView, nullNetView, nullView;
    private TextView reloadBtn, reloadNetBtn;

    private GoodDetail detail;
    private SpecificationAdapter specificationAdapter;
    private GoodsImageAdapter imageAdapter;
    private Specification mSpecification;//选中规格
    private List<Specification> specificationList;
    private ImageView mTipView;
    private ImageView goodsImage;
    private FrameLayout animation_viewGroup;
    private RadioGroup radioGroup;

    private ListView mListView;
    private GoodInfoAdapter mAdapter;
    private LinearLayout mImageContainer;
    private LinearLayout goodsImagesView;
    private LinearLayout goodsInfoView;
    //动画时间
    private int AnimationDuration = 1000;
    //正在执行的动画数量
    private int number = 0;
    //是否完成清理
    private boolean isClean = false;
    private MyTimer mTimer;
    private TextView mTimerView,imageNull;
    private ArrayList<String> listUrls = new ArrayList<String>();

    private boolean isOSaleBuy;
    private boolean isClick;
    private int mClick = -1;

    private int userId;
    private String token;
    private long temptime;
    private long currentTime;
    private OnCartNumChangeListener mListener;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ZhaiDou.IntentRefreshCartGoodsTag)) {
                initCartTips();
            }
            if (action.equals(ZhaiDou.IntentRefreshLoginTag)) {
                initCartTips();
            }
            if (action.equals(ZhaiDou.IntentRefreshLoginExitTag)) {
                initCartTips();
            }
            if (action.equals(ZhaiDou.IntentRefreshGoodsDetailsTag)) {
                setAddOrBuyShow("不能重复购买");
                FetchOSaleData(UPDATE_ISOSALEBUY);
            }

        }
    };

    private Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    //用来清除动画后留下的垃圾
                    try {
                        animation_viewGroup.removeAllViews();
                    } catch (Exception e) {

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
                    if (detail != null)
                        ljBtn.setVisibility(View.VISIBLE);
                    addCartBtn.setVisibility(View.VISIBLE);
                    loadingView.setVisibility(View.GONE);

                    detail = (GoodDetail) msg.obj;
                    setChildFargment(detail, goodInfos);

                    mCurrentPrice.setText("￥" + ToolUtils.isIntPrice(""+detail.getPrice() + ""));
                    mOldPrice.setText("￥" + ToolUtils.isIntPrice(""+detail.getCost_price() + ""));
                    tv_comment.setText(detail.getDesigner());
                    mTitle.setText(detail.getTitle());
                    setDiscount(detail.getPrice(), detail.getCost_price());

                    if (detail.getSpecifications() != null)
                        specificationAdapter.addAll(detail.getSpecifications());

                    boolean isOver = true;

                    for (int i = 0; i < detail.getSpecifications().size(); i++) {
                        if (detail.getSpecifications().get(i).num > 0) {
                            isOver = false;
                            break;
                        }
                    }
                    if (isOver) {
                        if (flags == 2) {
                            setAddOrBuyShow("此商品已下架");
                        } else {
                            setAddOrBuyShow("已卖光");
                        }

                    }
//                    initImageData(detail.getImageUrl());
                    ToolUtils.setImageCacheUrl(detail.getImageUrl(),goodsImage,R.drawable.icon_loading_goods_details);

                    if (detail.getImgs()==null&&detail.getSpecifications()==null&&detail.getEnd_time()==null)
                    {
                        setAddOrBuyShow("此商品已下架");
                    }

                    String end_date = detail.getEnd_time();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

                    try {
                        long millionSeconds = sdf.parse(end_date).getTime();//毫秒
                        long temp = millionSeconds - System.currentTimeMillis();
                        ToolUtils.setLog("temp:" + temp);
                        if (temp <= 0) {
                            mTimerView.setText("已结束");
                            setAddOrBuyShow("活动已结束");
                        }
                        mTimer = new MyTimer(temp, 1000);
                        mTimer.start();
                    } catch (Exception e) {
                        Log.i("Exception e", e.getMessage());
                    }
                    break;
                case UPDATE_COUNT_DOWN_TIME:
                    CountTime time = (CountTime) msg.obj;
                    String timerFormat = mContext.getResources().getString(R.string.timer);
                    String hourStr = String.format("%02d", time.getHour());
                    String minStr = String.format("%02d", time.getMinute());
                    String secondStr = String.format("%02d", time.getSecond());
                    String timer = String.format(timerFormat, time.getDay(), hourStr, minStr, secondStr);
                    mTimerView.setText(timer);
                    break;
                case UPDATE_UI_TIMER_FINISH:
                    ToolUtils.setLog("temp:" + 1);
                    mTimerView.setText("已结束");
                    setAddOrBuyShow("活动已结束");
                    break;
                case 5:
                    if (mDialog != null)
                        mDialog.dismiss();
                    if (isOSaleBuy) {
                        setAddOrBuyShow("不能重复购买");
                    } else {
                        for (int i = 0; i < items.size(); i++) {
                            if (items.get(i).isOSale.equals("true")) {
                                ljBuyOkDialog(items.get(i));
                                return;
                            }
                        }
                        buyGoods();
                    }
                    break;
                case UPDATE_ISOSALEBUY:
                    if (mDialog != null)
                        mDialog.dismiss();
                    if (isOSaleBuy) {
                        setAddOrBuyShow("不能重复购买");
                    }
                    break;
                case UPDATE_CARTCAR_DATA:
                    int visible=msg.arg1;
                    int num=msg.arg2;
                    mCartCount.setVisibility(visible);
                    mCartCount.setText("" + num);
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
     * radiobutton选择改变事件
     */
    private RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            if (i == R.id.infoRb) {
               goodsImagesView.setVisibility(View.VISIBLE);
                goodsInfoView.setVisibility(View.GONE);
            }
            if (i == R.id.afterSaleRb) {
                goodsImagesView.setVisibility(View.GONE);
                goodsInfoView.setVisibility(View.VISIBLE);
            }
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
                    if (checkLogin()) {
                        ShopCartFragment shopCartFragment = ShopCartFragment.newInstance("", 0);
                        ((MainActivity) getActivity()).navigationToFragment(shopCartFragment);
                    } else {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(1);
                        getActivity().startActivity(intent);
                    }
                    break;
                case R.id.goodsLjBuyBtn:
                    if (checkLogin()) {
                        if (detail != null)
                            if (mSpecification != null) {
                                if (flags == 1)//判断零元特卖是否已经购买郭
                                {
                                    mDialog.show();
                                    FetchOSaleData(UPDATE_LJBUY_ISOSALEBUY);
                                } else {
                                    buyGoods();
                                }

                            } else {
                                scrollView.scrollTo(0, 405);
                                Toast.makeText(mContext, "抱歉,先选择规格", Toast.LENGTH_SHORT).show();
                            }
                    } else {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(1);
                        getActivity().startActivity(intent);
                    }
                    break;
                case R.id.goodsAddBuyBtn:
                    addGoods();
                    break;
                case R.id.goodsTop:
                    scrollView.scrollTo(0, 0);
                    topBtn.setVisibility(View.GONE);
                    break;
                case R.id.share_iv:
                    share();
                    break;
                case R.id.nullReload:
                    initData();
                    break;
                case R.id.netReload:
                    initData();
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

    /**
     * 加载子fargment信息
     *
     * @param detail
     * @param goodInfos
     */
    private void setChildFargment(GoodDetail detail, ArrayList<GoodInfo> goodInfos) {
        mAdapter = new GoodInfoAdapter(mContext, goodInfos);
        mListView.setAdapter(mAdapter);
        mImageContainer.removeAllViews();
        DisplayImageOptions options=new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.icon_loading_osale)
//                        .showImageForEmptyUri(R.drawable.icon_loading_osale)
//                        .showImageOnFail(R.drawable.icon_loading_osale)
//                        .resetViewBeforeLoading(true)//default 设置图片在加载前是否重置、复位
//                        .bitmapConfig(Bitmap.Config.RGB_565)
//                        .imageScaleType(ImageScaleType.NONE)
//                        .cacheInMemory(true) // default
                .build();
        if (detail.getImgs() != null) {
            for (int i = 0; i < detail.getImgs().size(); i++) {
                LargeImgView imageView = new LargeImgView(mContext);
                imageView.setScaleType(ImageView.ScaleType.MATRIX);
                ImageLoader.getInstance().displayImage(detail.getImgs().get(i),imageView,new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {
                    }
                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {
                    }
                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                        if (bitmap!=null){
                            LargeImgView imageView1=(LargeImgView)view;
                            imageView1.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
                            if (bitmap.getHeight()<4000){
                                imageView1.setScaleType(ImageView.ScaleType.FIT_XY);
                                imageView1.setImageBitmap(bitmap);
                            } else {
                                imageView1.setImageBitmapLarge(bitmap);
                            }
                        }
                    }
                    @Override
                    public void onLoadingCancelled(String s, View view) {

                    }
                });
                mImageContainer.addView(imageView);
            }
        }
        else
        {
            imageNull.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getString(PAGE);
            mIndex = getArguments().getInt(INDEX);
            flags = getArguments().getInt("flags");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mContext = getActivity();

        if (mView == null) {
            mView = inflater.inflate(R.layout.goods_details_page, container, false);
            initView();
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) mView.getParent();
        if (parent != null) {
            parent.removeView(mView);
        }
        return mView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnCartNumChangeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnCartNumChangeListener");
        }
    }

    private void initView() {
        shareUrl = shareUrl + mIndex;

        shareBtn = (ImageView) mView.findViewById(R.id.share_iv);
        shareBtn.setOnClickListener(onClickListener);

        loadingView = (LinearLayout) mView.findViewById(R.id.loadingView);
        nullNetView = (LinearLayout) mView.findViewById(R.id.nullNetline);
        nullView = (LinearLayout) mView.findViewById(R.id.nullline);

        reloadBtn = (TextView) mView.findViewById(R.id.nullReload);
        reloadBtn.setOnClickListener(onClickListener);

        reloadNetBtn = (TextView) mView.findViewById(R.id.netReload);
        reloadNetBtn.setOnClickListener(onClickListener);

        initBroadcastReceiver();

        backBtn = (TypeFaceTextView) mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);
        titleTv = (TypeFaceTextView) mView.findViewById(R.id.title_tv);
        titleTv.setText(mContext.getResources().getString(R.string.title_goods_detail));

        viewGroupe = (LinearLayout) mView.findViewById(R.id.goods_viewGroup);
        mGridView = (ChildGridView) mView.findViewById(R.id.gv_specification);

        myCartBtn = (View) mView.findViewById(R.id.goodsMyCartBtn);
        myCartBtn.setOnClickListener(onClickListener);
        ljBtn = (LinearLayout) mView.findViewById(R.id.goodsLjBuyBtn);
        ljBtn.setOnClickListener(onClickListener);
        addCartBtn = (LinearLayout) mView.findViewById(R.id.goodsAddBuyBtn);
        addCartBtn.setOnClickListener(onClickListener);

        publishBtn = (TextView) mView.findViewById(R.id.goodsPublish);

        goodsImage = (ImageView) mView.findViewById(R.id.goodsImageView);
        goodsImage.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenWidth * 678 / 720));

        relativeLayout = (RelativeLayout) mView.findViewById(R.id.imageRl);
        relativeLayout.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenWidth * 678 / 720));

        iconView = (LinearLayout) mView.findViewById(R.id.iconView);
        iconOSaleView = (LinearLayout) mView.findViewById(R.id.iconOSaleView);
        commentView= (LinearLayout) mView.findViewById(R.id.commentView);
        if (flags == 1) {
            iconView.setVisibility(View.GONE);
            iconOSaleView.setVisibility(View.VISIBLE);
            commentView.setVisibility(View.GONE);
        } else {
            iconView.setVisibility(View.VISIBLE);
            iconOSaleView.setVisibility(View.GONE);
            commentView.setVisibility(View.VISIBLE);
        }

        if (flags == 2) {
            setAddOrBuyShow("此商品已下架");
        }

        tv_comment = (TextView) mView.findViewById(R.id.tv_comment);
        mCurrentPrice = (TextView) mView.findViewById(R.id.goodsCurrentPrice);
        mOldPrice = (TextView) mView.findViewById(R.id.goodsFormerPrice);
        mOldPrice.getPaint().setAntiAlias(true);
        mOldPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);

        mCartCount = (TextView) mView.findViewById(R.id.tv_cart_count);
        mTipView = (ImageView) mView.findViewById(R.id.myCartTipsTv);
        mDiscount = (TextView) mView.findViewById(R.id.tv_discount);
        mTitle = (TextView) mView.findViewById(R.id.tv_title);
        mTimerView = (TextView) mView.findViewById(R.id.tv_count_time);
        animation_viewGroup = createAnimLayout();
        mRequestQueue = Volley.newRequestQueue(getActivity());

        topBtn = (ImageView) mView.findViewById(R.id.goodsTop);
        topBtn.setOnClickListener(onClickListener);

        scrollView = (ScrollView) mView.findViewById(R.id.sv_goods_detail);
        scrollView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                scrollView.getParent().requestDisallowInterceptTouchEvent(true);
                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE)
                {
                    int scrollY = view.getScrollY();
                    if (scrollY > goodsImage.getHeight())
                    {
                        topBtn.setVisibility(View.VISIBLE);
                    }
                    if (scrollY <600)
                    {
                        topBtn.setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });

        radioGroup = (RadioGroup) mView.findViewById(R.id.goodsRG);
        radioGroup.setOnCheckedChangeListener(onCheckedChangeListener);

        specificationAdapter = new SpecificationAdapter(getActivity(), new ArrayList<Specification>(), mSpecificationSelectPosition);
        mGridView.setAdapter(specificationAdapter);

        specificationAdapter.setOnInViewClickListener(R.id.sizeTitleTv, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {

                if (((Specification) values).num > 0) {
                    if (mClick == position) {
                        if (isClick == false) {
                            mClick = position;
                            specificationAdapter.setCheckPosition(mSpecificationSelectPosition = position);
                            specificationAdapter.notifyDataSetChanged();
                            sizeEvent(position);
                            isClick = true;

                        } else {
                            mClick = -1;
                            isClick = false;
                            specificationAdapter.setCheckPosition(mSpecificationSelectPosition = -1);
                            specificationAdapter.notifyDataSetChanged();
                            mSpecification = null;

                            mCurrentPrice.setText("￥" + ToolUtils.isIntPrice(detail.getPrice() + ""));
                            mOldPrice.setText("￥" + ToolUtils.isIntPrice(detail.getCost_price() + ""));
                            setDiscount(detail.getPrice(), detail.getCost_price());
                        }
                    } else {
                        mClick = position;
                        specificationAdapter.setCheckPosition(mSpecificationSelectPosition = position);
                        specificationAdapter.notifyDataSetChanged();
                        sizeEvent(position);
                        isClick = true;
                    }


                }
            }
        });

        goodsImagesView=(LinearLayout)mView.findViewById(R.id.goodInfoView);
        goodsInfoView=(LinearLayout)mView.findViewById(R.id.goodInfo1View);

        mListView = (ListView) mView.findViewById(R.id.lv_good_info);
        mImageContainer = (LinearLayout) mView.findViewById(R.id.ll_img_container);
        imageNull=(TextView)mView.findViewById(R.id.img_null);
        imageNull.setVisibility(View.GONE);

        mView.findViewById(R.id.rl_qq_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url="mqqwpa://im/chat?chat_type=wpa&uin="+mContext.getResources().getString(R.string.QQ_Number);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        });

        creatCartDB = new CreatCartDB(mContext);
        initCartTips();

        initData();

    }

    /**
     * 数据加载
     */
    private void initData() {
        mDialog= CustomLoadingDialog.setLoadingDialog(mContext, "");
        if (NetworkUtils.isNetworkAvailable(mContext)) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    FetchDetailData(mIndex);
                    if (checkLogin())
                    {
                        if (flags == 1) {
                            FetchOSaleData(UPDATE_ISOSALEBUY);
                        }
                    }
                }
            }, 300);
        } else {
            if (mDialog != null)
                mDialog.dismiss();
            nullView.setVisibility(View.GONE);
            nullNetView.setVisibility(View.VISIBLE);
        }

    }

    /**
     * 设置立即购买、添加购物车、是否下架显示的问题
     *
     * @param msg
     */
    private void setAddOrBuyShow(String msg) {
        publishBtn.setVisibility(View.VISIBLE);
        publishBtn.setText(msg);
        ljBtn.setVisibility(View.GONE);
        addCartBtn.setVisibility(View.GONE);
    }

    public boolean checkLogin() {
        token = (String) SharedPreferencesUtil.getData(mContext, "token", "");
        userId = (Integer) SharedPreferencesUtil.getData(mContext, "userId", -1);
        boolean isLogin = !TextUtils.isEmpty(token) && userId > -1;
        return isLogin;
    }

    /**
     * 选择规格事件处理
     */
    private void sizeEvent(int position) {
        if (detail != null & specificationList.size() > 0) {
            mSpecification = specificationList.get(position);
            mCurrentPrice.setText("￥" + ToolUtils.isIntPrice(""+mSpecification.price));
            mOldPrice.setText("￥" + ToolUtils.isIntPrice(""+mSpecification.oldPrice));
            setDiscount(mSpecification.price, mSpecification.oldPrice);
        }
    }

    /**
     * 折扣处理事件
     *
     * @param current
     * @param old
     */
    private void setDiscount(double current, double old) {
        mDiscount.setVisibility(View.VISIBLE);
        if (current != 0 & old != 0) {
            DecimalFormat df = new DecimalFormat("##.0");
            String zk = df.format(current / old * 10);
            if (zk.contains(".0")) {
                int sales = (int) Double.parseDouble(zk);
                mDiscount.setText(sales + "折");
            } else {
                Double sales = Double.parseDouble(zk);
                mDiscount.setText(sales + "折");
            }
        } else {
            mDiscount.setVisibility(View.GONE);
        }
    }

    /**
     * 分享
     */
    private void share() {
        ShareSDK.initSDK(mContext);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(mPage);
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl(shareUrl);
        // text是分享文本，所有平台都需要这个字段
        oks.setText(mPage + "   " + shareUrl);
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        if (detail != null) {
            oks.setImageUrl(detail.getImageUrl());//确保SDcard下面存在此张图片
        }
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl(shareUrl);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
//            oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(shareUrl);

        oks.show(mContext);
    }

    /**
     * 注册广播
     */
    private void initBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ZhaiDou.IntentRefreshCartGoodsTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginExitTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshGoodsDetailsTag);
        mContext.registerReceiver(broadcastReceiver, intentFilter);
    }

    /**
     * 红色标识提示显示数量
     */
    private void initCartTips() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (checkLogin()) {
                    num = 0;
                    getGoodsItems();
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).isPublish.equals("false") && items.get(i).isOver.equals("false")) {
                            num = num + items.get(i).num;
                        }
                    }
                    Message message=new Message();
                    message.arg1=(num>0?View.VISIBLE:View.GONE);
                    message.arg2=num;
                    message.what=UPDATE_CARTCAR_DATA;
                    handler.sendMessage(message);
                } else {
                    Message message=new Message();
                    message.arg1=View.GONE;
                    message.arg2=num;
                    message.what=UPDATE_CARTCAR_DATA;
                    handler.sendMessage(message);
                }
            }
        }).start();
    }

    /**
     * 获得当前userId的所有商品
     */
    private void getGoodsItems() {
        items = CreatCartTools.selectByAll(creatCartDB, userId);
    }

    /**
     * 立即购买
     */
    private void buyGoods() {
        CartItem cartItem = new CartItem();
        cartItem.userId = userId;
        cartItem.id = detail.getId();
        cartItem.name = detail.getTitle();
        cartItem.creatTime = System.currentTimeMillis();
        if (detail.getImgs() != null)
            cartItem.imageUrl = detail.getImgs().get(0);
        cartItem.currentPrice = mSpecification.price;//规格的价格
        cartItem.formalPrice = mSpecification.oldPrice;
        DecimalFormat df = new DecimalFormat("##.0");
        double saveMoney = Double.parseDouble(df.format(mSpecification.oldPrice - mSpecification.price));
        cartItem.saveMoney = saveMoney;
        cartItem.saveTotalMoney = saveMoney;
        cartItem.totalMoney = detail.getPrice();
        cartItem.num = 1;
        cartItem.size = mSpecification.getTitle();
        cartItem.sizeId = mSpecification.getId();
        cartItem.isPublish = "false";
        if (flags == 1) {
            cartItem.isOSale = "true";
        } else {
            cartItem.isOSale = "false";
        }


        ArrayList<CartItem> itemsCheck = new ArrayList<CartItem>();
        itemsCheck.add(cartItem);

        ShopOrderOkFragment shopOrderOkFragment = ShopOrderOkFragment.newInstance("", 0);
        Bundle bundle = new Bundle();
        bundle.putSerializable("flags", 1);
        bundle.putSerializable("goodsList", itemsCheck);
        shopOrderOkFragment.setArguments(bundle);
        ((MainActivity) getActivity()).navigationToFragment(shopOrderOkFragment);

    }

    /**
     ** 设置头部相片
     */
    private void initImageData(List<String> urls) {
        viewGroupe.removeAllViews();
        if (urls==null||urls.size()<1)
        {
            if (listUrls!=null&&listUrls.size()>0)
            {
                urls=listUrls;
            }
        }
        if (CollectionUtils.isNotNull(urls))
        {
            if (urls.size()>=4)
            {
                for (int i = 0; i < 4; i++)
                {
                    ImageView imageView = new ImageView(mContext);
                    imageView.setImageResource(R.drawable.icon_loading_goods_details);
                    imageView.setScaleType(ImageView.ScaleType.CENTER);
                    ToolUtils.setImageCacheUrl(urls.get(i), imageView);
                    adPics.add(imageView);
                }
            }
            else
            {
                for (String url : urls)
                {
                    ImageView imageView = new ImageView(mContext);
                    imageView.setImageResource(R.drawable.icon_loading_goods_details);
                    imageView.setScaleType(ImageView.ScaleType.CENTER);
                    ToolUtils.setImageCacheUrl(url, imageView);
                    adPics.add(imageView);
                }
            }
            dots = new ImageView[adPics.size()];

            for (int i = 0; i < adPics.size(); i++) {
                ImageView dot_iv = new ImageView(mContext);
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
            viewPager.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenWidth * 678 / 720));
            imageAdapter = new GoodsImageAdapter(mContext, adPics);
            viewPager.setAdapter(imageAdapter);
            viewPager.setOnPageChangeListener(onPageChangeListener);
            viewPager.setCurrentItem(0);
        }
    }

    /**
     * 添加商品
     */
    private void addGoods() {
        if (checkLogin()) {
            if (flags == 1) {
                if (mSpecification != null) {
                    if (isOSaleBuy) {
                        Toast.makeText(mContext, "抱歉,您已经购买了零元特卖商品,今天不能再添加了", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        for (int i = 0; i < items.size(); i++) {
                            if (items.get(i).isOSale.equals("true")) {
                                addCartOkDialog(items.get(i));
                                return;
                            }
                        }
                        addCartGoods();
                    }
                } else {
                    scrollView.scrollTo(0, 405);
                    Toast.makeText(mContext, "抱歉,先选择规格", Toast.LENGTH_SHORT).show();
                }
            } else {
                addCartGoods();
            }

        } else {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(1);
            getActivity().startActivity(intent);
        }

    }

    /**
     * 判断商品是否库存不走
     *
     * @return true 库存足
     */
    private boolean isOver() {
        items = CreatCartTools.selectByAll(creatCartDB, userId);

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).sizeId == mSpecification.getId()) {
                if (mSpecification.num >= items.get(i).num + 1) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private void addCartGoods() {
        if (detail != null) {
            if (mSpecification != null) {
                if (isOver()) {
                    int[] location = new int[2];
                    mTipView.getLocationInWindow(location);
                    Drawable drawable = mTipView.getDrawable();
                    doAnim(drawable, location);

                    getGoodsItems();

                    CartItem cartItem = new CartItem();
                    cartItem.userId = userId;
                    cartItem.id = detail.getId();
                    cartItem.name = detail.getTitle();
                    cartItem.creatTime = System.currentTimeMillis();
                    if (detail.getImgs() != null) {
                        cartItem.imageUrl = detail.getImgs().get(0);
                    }
                    cartItem.currentPrice = mSpecification.price;//规格的价格
                    cartItem.formalPrice = mSpecification.oldPrice;
                    DecimalFormat df = new DecimalFormat("###.00");
                    double saveMoney = Double.parseDouble(df.format(mSpecification.oldPrice - mSpecification.price));
                    cartItem.saveMoney = saveMoney;
                    cartItem.saveTotalMoney = saveMoney;
                    cartItem.totalMoney = detail.getPrice();
                    cartItem.num = 1;
                    cartItem.size = mSpecification.getTitle();
                    cartItem.sizeId = mSpecification.getId();
                    cartItem.isPublish = "false";
                    cartItem.isOver = "false";
                    if (flags == 1)//是否零元特卖
                    {
                        cartItem.isOSale = "true";
                    } else {
                        cartItem.isOSale = "false";
                    }
                    CreatCartTools.insertByData(creatCartDB, items, cartItem);
                    mListener.onCartNumIncrease(1);

                    Intent intent = new Intent(ZhaiDou.IntentRefreshCartGoodsTag);
                    mContext.sendBroadcast(intent);
                } else {
                    CustomToastDialog.setToastDialog(mContext, "抱歉,商品数量不足,请勿继续添加");
                }

            } else {
                scrollView.scrollTo(0,405);
                Toast.makeText(mContext, "抱歉,先选择规格", Toast.LENGTH_SHORT).show();
            }
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
        listUrls.clear();
        String url = ZhaiDou.goodsDetailsUrlUrl + id;
        Log.i("url---------------------->", url);
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {

                if (mDialog != null)
                    mDialog.dismiss();

                if (jsonObject != null) {
                    JSONObject merchandise = jsonObject.optJSONObject("merchandise");
                    int id = merchandise.optInt("id");
                    String title = merchandise.optString("title");
                    String designer = merchandise.optString("designer");
                    int total_count = merchandise.optInt("total_count");
                    double price = merchandise.optDouble("price");
                    double cost_price = merchandise.optDouble("cost_price");
                    int discount = merchandise.optInt("discount");
                    String end_time = merchandise.optString("end_time");
                    detail = new GoodDetail(id, title, designer, total_count, price, cost_price, discount);
                    detail.setEnd_time(end_time);

                    JSONArray imgArrays = merchandise.optJSONArray("imgs");
                    if (imgArrays != null && imgArrays.length() > 0)
                    {
                        ArrayList<String> imgsList = new ArrayList<String>();
                        for (int i = 0; i < imgArrays.length(); i++) {
                            JSONObject imgObj = imgArrays.optJSONObject(i);
                            String url = imgObj.optString("url");
                            imgsList.add(url);
                        }
                        detail.setImageUrl(imgsList.get(0));
                        imgsList.remove(0);
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
                            double sizeOldPrice = specificationObj.optDouble("cost_price");

                            Specification specification = new Specification(specificationId, specificationTitle, num, sizePrice, sizeOldPrice);
                            specificationList.add(specification);
                        }
                        detail.setSpecifications(specificationList);
                    }

                    JSONArray descriptions = merchandise.optJSONArray("descriptions");
                    if (descriptions != null && descriptions.length() > 0) {
                        for (int i = 0; i < descriptions.length(); i++) {
                            JSONObject description = descriptions.optJSONObject(i);
                            int descriptionsId = description.optInt("id");
                            String descriptionsTitle = description.optString("title");
                            String value = description.optString("value");
                            GoodInfo goodInfo = new GoodInfo(descriptionsId, descriptionsTitle, value);
                            goodInfos.add(goodInfo);
                        }
                        detail.setGoodsInfo(goodInfos);
                    }

                    Message message = new Message();
                    message.what = UPDATE_GOOD_DETAIL;
                    message.obj = detail;
                    handler.sendMessage(message);
                } else {
                    nullView.setVisibility(View.VISIBLE);
                    nullNetView.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (mDialog != null)
                    mDialog.dismiss();
                nullView.setVisibility(View.VISIBLE);
                nullNetView.setVisibility(View.GONE);
            }
        });
        mRequestQueue.add(request);
    }

    /**
     * 零元特卖是否购买请求
     */
    public void FetchOSaleData(final int i) {
        String url = ZhaiDou.orderCheckOSaleUrl;
        Log.i("url---------------------->", url);
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {

                if (jsonObject != null) {
                    isOSaleBuy = jsonObject.optBoolean("flag");
                }
                if (i == UPDATE_LJBUY_ISOSALEBUY) {
                    handler.sendEmptyMessage(UPDATE_LJBUY_ISOSALEBUY);
                } else if (i == UPDATE_ISOSALEBUY) {
                    handler.sendEmptyMessage(UPDATE_ISOSALEBUY);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (mDialog != null)
                    mDialog.dismiss();
                Toast.makeText(mContext, "抱歉,请求失败", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("SECAuthorization", token);
                return headers;
            }
        };
        mRequestQueue.add(request);
    }

    /**
     * 立即购买清除掉购物车中的零元特卖
     *
     * @param cartItem
     */
    private void ljBuyOkDialog(final CartItem cartItem) {
        final Dialog dialog = new Dialog(mContext, R.style.custom_dialog);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_collect_hint, null);
        TextView tvMsg = (TextView) view.findViewById(R.id.tv_msg);
        tvMsg.setText("购物车已经有一件零元特卖商品,继续购买将删除掉该商品，是否删除？");
        TextView okTv = (TextView) view.findViewById(R.id.okTv);
        okTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreatCartTools.deleteByData(creatCartDB, cartItem);
                Intent intent = new Intent(ZhaiDou.IntentRefreshCartGoodsTag);
                mContext.sendBroadcast(intent);
                dialog.dismiss();
                buyGoods();
            }
        });
        TextView cancelTv = (TextView) view.findViewById(R.id.cancelTv);
        cancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog.show();

    }

    /**
     * 加入购物车清除掉购物车中的零元特卖
     *
     * @param cartItem
     */
    private void addCartOkDialog(final CartItem cartItem) {
        final Dialog dialog = new Dialog(mContext, R.style.custom_dialog);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_collect_hint, null);
        TextView tvMsg = (TextView) view.findViewById(R.id.tv_msg);
        tvMsg.setText("购物车已经有一件零元特卖商品,继续添加将删除掉该商品，是否删除？");
        TextView okTv = (TextView) view.findViewById(R.id.okTv);
        okTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreatCartTools.deleteByData(creatCartDB, cartItem);
                Intent intent = new Intent(ZhaiDou.IntentRefreshCartGoodsTag);
                mContext.sendBroadcast(intent);
                dialog.dismiss();
                addCartGoods();
            }
        });
        TextView cancelTv = (TextView) view.findViewById(R.id.cancelTv);
        cancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog.show();

    }


    private void doAnim(Drawable drawable, int[] start_location) {
        if (!isClean) {
            setAnim(drawable, start_location);
        } else {
            try {
                animation_viewGroup.removeAllViews();
                isClean = false;
                setAnim(drawable, start_location);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isClean = true;
            }
        }
    }

    private void setAnim(Drawable drawable, int[] start_location) {
        Animation mScaleAnimation = new ScaleAnimation(1.5f, 0.0f, 1.5f, 0.0f, Animation.RELATIVE_TO_SELF, 0.1f, Animation.RELATIVE_TO_SELF, 0.1f);
        mScaleAnimation.setDuration(AnimationDuration);
        mScaleAnimation.setFillAfter(true);


        final ImageView iview = new ImageView(getActivity());
        iview.setImageDrawable(drawable);
        final View view = addViewToAnimLayout(animation_viewGroup, iview, start_location);
        view.setAlpha(0.6f);

        int[] end_location = new int[2];
        myCartBtn.getLocationInWindow(end_location);
        int endX = -start_location[0] + dip2px(getActivity(), 30);
        int endY = end_location[1] - start_location[1];

        Animation mTranslateAnimation = new TranslateAnimation(0, endX, 0, endY);
        Animation mRotateAnimation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateAnimation.setDuration(AnimationDuration);
        mTranslateAnimation.setDuration(AnimationDuration);
        AnimationSet mAnimationSet = new AnimationSet(true);

        mAnimationSet.setFillAfter(true);
//        mAnimationSet.addAnimation(mRotateAnimation);
        mAnimationSet.addAnimation(mScaleAnimation);
        mAnimationSet.addAnimation(mTranslateAnimation);

        mAnimationSet.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                number++;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                number--;
                if (number == 0) {
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

    private View addViewToAnimLayout(ViewGroup vg, View view, int[] location) {
        int x = location[0];
        int y = location[1];
        vg.addView(view);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                dip2px(getActivity(), 90), dip2px(getActivity(), 90));
        lp.leftMargin = x;
        lp.topMargin = y;
        view.setPadding(5, 5, 5, 5);
        view.setLayoutParams(lp);

        return view;
    }

    private int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private FrameLayout createAnimLayout() {
        ViewGroup rootView = (ViewGroup) getActivity().getWindow().getDecorView();
        FrameLayout animLayout = new FrameLayout(getActivity());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        animLayout.setLayoutParams(lp);
        animLayout.setBackgroundColor(Color.parseColor("#00000000"));
        rootView.addView(animLayout);
        return animLayout;

    }

    private class MyTimer extends CountDownTimer {
        private MyTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            temptime = l;
            long day = 24 * 3600 * 1000;
            long hour = 3600 * 1000;
            long minute = 60 * 1000;
            //两个日期想减得到天数
            long dayCount = l / day;
            long hourCount = (l - (dayCount * day)) / hour;
            long minCount = (l - (dayCount * day) - (hour * hourCount)) / minute;
            long secondCount = (l - (dayCount * day) - (hour * hourCount) - (minCount * minute)) / 1000;
            CountTime time = new CountTime(dayCount, hourCount, minCount, secondCount);
            Message message = new Message();
            message.what = UPDATE_COUNT_DOWN_TIME;
            message.obj = time;
            handler.sendMessage(message);
        }

        @Override
        public void onFinish() {
            handler.sendEmptyMessage(UPDATE_UI_TIMER_FINISH);
        }
    }

    @Override
    public void onDestroyView() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mContext.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        long temp1 = System.currentTimeMillis() - currentTime;
        if (temptime - temp1 > 0) {
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
            mTimer = new MyTimer(temptime - temp1, 1000);
            mTimer.start();
        }
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_goods_detail));
    }

    @Override
    public void onPause() {
        currentTime = System.currentTimeMillis();
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_goods_detail));
    }

    /**
     * 规格适配器
     */
    public class GoodInfoAdapter extends BaseListAdapter<GoodInfo> {
        public GoodInfoAdapter(Context context, List<GoodInfo> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_goods_info, null);
            TextView tv_key = ViewHolder.get(convertView, R.id.tv_key);
            tv_key.setMaxWidth(screenWidth / 2 - 20);
            TextView tv_value = ViewHolder.get(convertView, R.id.tv_value);
            GoodInfo goodInfo = getList().get(position);
            tv_key.setText(goodInfo.getTitle());
            tv_value.setText(goodInfo.getValue());
            return convertView;
        }
    }

    public interface OnCartNumChangeListener{
        public void onCartNumIncrease(int num);

        public void onCartNumDecrease(int num);
    }
}
