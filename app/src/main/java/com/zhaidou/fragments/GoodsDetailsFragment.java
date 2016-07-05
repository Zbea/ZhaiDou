package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
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
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.LoginActivity;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.CartCountManager;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.CartArrayItem;
import com.zhaidou.model.CartGoodsItem;
import com.zhaidou.model.GoodDetail;
import com.zhaidou.model.GoodInfo;
import com.zhaidou.model.Specification;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.Api;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.EaseUtils;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomProgressWebview;
import com.zhaidou.view.FlowLayout;
import com.zhaidou.view.LargeImgView;
import com.zhaidou.view.TimerTextView;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;

public class GoodsDetailsFragment extends BaseFragment implements CartCountManager.OnCartCountListener
{
    private static final String PAGE = "page";
    private static final String INDEX = "index";
    private static final String SIZEID = "sizeId";
    private static final String CANSHARE = "canShare";

    private String mPage;
    private String mIndex;
    private String mSizeId;
    private View mView;
    private int flags;//1代表零元特卖；2代表已下架商品
    private Context mContext;
    private ImageView shareBtn;
    private String shareUrl = ZhaiDou.goodsDetailsShareUrl;
    private TextView  titleTv, mCartCount;
    private List<View> adPics = new ArrayList<View>();
    private ViewPager viewPager;
    private RelativeLayout relativeLayout;
    private LinearLayout viewGroupe;//指示器容器
    private LinearLayout ljBtn;
    private LinearLayout addCartBtn;
    private TextView publishBtn;
    private View myCartBtn;

    private Dialog mDialog;
    private DialogUtils mDialogUtil;
    private RequestQueue mRequestQueue;

    private ArrayList<GoodInfo> goodInfos = new ArrayList<GoodInfo>();
    private TextView tv_comment, mCurrentPrice, mOldPrice, mDiscount, mTitle, tv_baoyou;

    private final int UPDATE_GOOD_DETAIL = 0;
    private final int UPDATE_CARTCAR_DATA = 1;
    private final int UPDATE_ISOSALEBUY = 2;//判断进来时候零元特卖是否已经购买了，购买了才让按钮不能点击
    private final int UPDATE_LJBUY_ISOSALEBUY = 3;//零元特卖立即购买时候判断是否已经购买郭
    private final int UPDATE_LOGIN_ISOSALEBUY = 4;//判断进来时候零元特卖是否已经购买了，购买了才让按钮不能点击
    private final int UPDATE_ADD_CART = 5;//加入购物车
    private final int UPDATE_ISADD_CART = 6;//零元特卖是否已经加入购物车了
    private final int UPDATE_OSALE_DELETE = 7;//删除购物车里面的零元特卖
    private final int UPDATE_SHARE_TOAST = 8;

    private ScrollView scrollView;
    private ImageView topBtn;
    private LinearLayout iconView, iconOSaleView, commentView;
    private TextView imageNull;
    private TimerTextView mTimerView;
    private LinearLayout loadingView, nullNetView, nullView;
    private TextView reloadBtn, reloadNetBtn;
    private TextView sizeNameParentTv, sizeNameSubclassTv;
    private LinearLayout sizeParentLine, sizeSubjectLine;
    private String sizeNameParent, sizeNameSubclass;
    private FlowLayout flowLayoutParent, flowLayoutSubclass;
    private boolean isSizeParent;//当为true时父不存在
    private boolean isSizeSubclass;//当为true时子不存在
    private List<TextView> textSingleTvs = new ArrayList<TextView>();//一维点击 TextView 集合
    private List<TextView> textParentTvs = new ArrayList<TextView>();//二维父点击 TextView 集合
    private List<TextView> textSubclassTvs = new ArrayList<TextView>();//二维子点击 TextView 集合
    private int sigleClickPosition = -1;//一位规格选中的位置
    private int doubleClickParentPos = 0;//二维规格选中的位置
    private boolean isClickParent;//规格是否可以点击
    private boolean isClickSingle;//规格是否可以点击
    private boolean isClickSubclass;//规格是否可以点击
    private Specification mSpecificationParent;//选中规格第一个
    private Specification mSpecificationSubclass;//选中型号第二个

    private GoodDetail detail;
    private ImageView mTipView;
    private RadioGroup radioGroup;
    private ListView mListView;
    private GoodInfoAdapter mAdapter;
    private LinearLayout mImageContainer;
    private LinearLayout goodsImagesView;
    private LinearLayout goodsInfoView;
    private CustomProgressWebview webView;

    private boolean isPublish;
    private boolean canShare;
    private boolean isOSaleBuy;//是否购买过零元特卖
    private boolean isOSaleAdd;//是否添加过零元特卖
    private boolean isOver = true;//是否卖光
    private boolean isAddOrBuy = true;//是添加还是立即购买
    private boolean isFristSubclass = true;//二维第一次但是展示加入

    private long initTime;
    private long systemTime;
    private boolean isFrist;
    private int userId;
    private String token;
    long mTime = 0;
    private int cartCount;//购物车商品数量
    private ArrayList<CartArrayItem> arrayItems = new ArrayList<CartArrayItem>();
    private List<Specification> specificationList = new ArrayList<Specification>();//父集合
    private List<Specification> subclassSizes = new ArrayList<Specification>();//子规格集合
    private CartArrayItem cartArrayItem = new CartArrayItem();
    private CartGoodsItem cartGoodsItem = new CartGoodsItem();
    private List<String> imageInits = new ArrayList<String>();//图片集合
    private List<String> images = new ArrayList<String>();//图片集合
    private List<ImageView> dots = new ArrayList<ImageView>();
    private ImageAdapter imageAdapter;
    private String sku;
    private List<ImageView> mImageViews = new ArrayList<ImageView>();

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(ZhaiDou.IntentRefreshOGoodsDetailsTag))
            {
                isOSaleBuy = true;
                setAddOrBuyShow("您今天已经购买过零元特卖商品", false);
                setRefreshSpecification();
            }
            if (action.equals(ZhaiDou.IntentRefreshGoodsDetailsTag))
            {
                if (isTwoSize())
                {
                    mSpecificationSubclass.num = mSpecificationSubclass.num - 1;
                } else
                {
                    mSpecificationParent.num = mSpecificationParent.num - 1;
                }
                setRefreshSpecification();
            }
        }
    };

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case UPDATE_GOOD_DETAIL:
                    if (detail == null)
                    {
                        return;
                    }
                    setNextEventView();
                    if (flags == 1)//如果是零元特卖商品UI显示处理
                    {
                        iconView.setVisibility(View.GONE);
                        iconOSaleView.setVisibility(View.VISIBLE);
                        commentView.setVisibility(View.GONE);
                        tv_baoyou.setVisibility(View.GONE);
                        canShare = false;
                    } else
                    {
                        iconView.setVisibility(View.VISIBLE);
                        iconOSaleView.setVisibility(View.GONE);
                        commentView.setVisibility(View.VISIBLE);
                        tv_baoyou.setVisibility(View.VISIBLE);
                    }
                    shareBtn.setVisibility(canShare ? View.VISIBLE : View.GONE);
                    detail = (GoodDetail) msg.obj;
                    setChildFargment(detail, goodInfos);
                    mCurrentPrice.setText("￥" + ToolUtils.isIntPrice("" + detail.price + ""));
                    mOldPrice.setText("￥" + ToolUtils.isIntPrice("" + detail.cost_price + ""));
                    tv_comment.setText(detail.designer);
                    mTitle.setText(goodsName);
                    setDiscount(detail.price, detail.cost_price);
                    if (detail.specifications != null)
                    {
                        setSizeView();
                    }
                    if (isPublish)
                    {
                        setAddOrBuyShow("此商品已下架", false);
                    }
                    if (isOver)
                    {
                        setAddOrBuyShow("已卖光", false);
                    }
                    try
                    {
                        initTime = detail.end_time - System.currentTimeMillis();
                        if (initTime <= 0)
                        {
                            mTimerView.setText("已结束");
                        } else
                        {
                            mTimerView.setTimes(initTime);
                            mTimerView.start();
                        }
                    } catch (Exception e)
                    {
                    }
                    viewPager.setFocusable(true);
                    viewPager.setFocusableInTouchMode(true);
                    viewPager.requestFocus();
                    break;
                case UPDATE_CARTCAR_DATA://更新购物车数量
                    initCartTips();
                    break;
                case UPDATE_LJBUY_ISOSALEBUY://零元特卖
                    if (isOSaleBuy)
                    {
                        if (mDialog != null)
                            mDialog.dismiss();
                        setAddOrBuyShow("您今天已经购买过零元特卖商品", false);
                    } else
                    {
                        FetchOSaleAddData();
                    }
                    break;
                case UPDATE_ISOSALEBUY:
                    if (mDialog != null)
                        mDialog.dismiss();
                    loadingView.setVisibility(View.GONE);
                    if (isOSaleBuy)
                    {
                        setAddOrBuyShow("您今天已经购买过零元特卖商品", false);
                    }
                    break;

                case UPDATE_ADD_CART://添加商品成功
                    cartCount = cartCount + 1;
                    CartCountManager.newInstance().notify(cartCount);

                    mTipView.setVisibility(View.VISIBLE);
                    AnimationSet animationSet = new AnimationSet(true);
                    animationSet.setDuration(1000);
                    ScaleAnimation scaleAnimation = new ScaleAnimation(3f, 1f, 3f, 1f,
                            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.add_cart_anim);
                    animation.setFillAfter(true);
                    animationSet.addAnimation(scaleAnimation);
                    animationSet.addAnimation(animation);
                    mTipView.startAnimation(animationSet);
                    animationSet.setAnimationListener(new Animation.AnimationListener()
                    {
                        @Override
                        public void onAnimationStart(Animation animation)
                        {
                        }
                        @Override
                        public void onAnimationEnd(Animation animation)
                        {
                            mTipView.setVisibility(View.GONE);
                            mTipView.clearAnimation();
                            initCartTips();
                        }
                        @Override
                        public void onAnimationRepeat(Animation animation)
                        {
                        }
                    });
                    break;
                case UPDATE_ISADD_CART://校正是否零元特卖已经加入购物车
                    mDialog.dismiss();
                    if (isOSaleAdd)//如果已经存在零元特卖
                    {
                        String delStr = isAddOrBuy == true ? "购物车已有一件零元特卖商品继续添加则会删除该商品，是否继续?" : "购物车已有一件零元特卖商品继续购买则会删除该商品，是否继续?";
                        mDialogUtil.showDialog(delStr, new DialogUtils.PositiveListener()
                        {
                            @Override
                            public void onPositive()
                            {
                                mDialog.show();
                                FetchGoodsDeleteData();
                            }
                        }, null);
                    } else
                    {
                        if (isAddOrBuy)//如果是添加
                        {
                            FetchAddCartData();
                        } else
                        {
                            CommitLjBuy();
                        }
                    }
                    break;
                case UPDATE_OSALE_DELETE://删除成功后
                    cartCount = cartCount - 1;
                    CartCountManager.newInstance().notify(cartCount);
                    if (isAddOrBuy)//如果已经存在零元特卖
                    {
                        FetchAddCartData();
                    } else
                    {
                        mDialog.dismiss();
                        CommitLjBuy();
                    }
                    break;
                case UPDATE_SHARE_TOAST:
                    mDialogUtil.dismiss();
                    String result = (String) msg.obj;
                    Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    };

    /**
     * radiobutton选择改变事件
     */
    private RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i)
        {
            if (i == R.id.infoRb)
            {
                goodsImagesView.setVisibility(View.VISIBLE);
                goodsInfoView.setVisibility(View.GONE);
            }
            if (i == R.id.afterSaleRb)
            {
                goodsImagesView.setVisibility(View.GONE);
                goodsInfoView.setVisibility(View.VISIBLE);
            }
        }
    };

    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            switch (view.getId())
            {
                case R.id.goodsMyCartBtn:
                    if (checkLogin())
                    {
                        ShopCartFragment shopCartFragment = ShopCartFragment.newInstance("", 0);
                        ((BaseActivity) getActivity()).navigationToFragment(shopCartFragment);
                    } else
                    {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(3);
                        startActivityForResult(intent, 5001);
                    }
                    break;
                case R.id.goodsLjBuyBtn:
                    isAddOrBuy = false;
                    PrepareAddOrBuy();
                    break;
                case R.id.goodsAddBuyBtn:
                    if ((System.currentTimeMillis() - mTime) > 1000)
                    {
                        mTime = System.currentTimeMillis();
                        isAddOrBuy = true;
                        PrepareAddOrBuy();
                    }
                    break;
                case R.id.goodsTop:
                    scrollView.fullScroll(ScrollView.FOCUS_UP);
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

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener()
    {
        @Override
        public void onPageScrolled(int i, float v, int i2)
        {

        }

        @Override
        public void onPageSelected(int i)
        {
            setImageBackground(i);
        }

        @Override
        public void onPageScrollStateChanged(int i)
        {

        }
    };
    private String goodsName;

    public static GoodsDetailsFragment newInstance(String page, String index)
    {
        GoodsDetailsFragment fragment = new GoodsDetailsFragment();
        System.out.println("index = " + index);
        Bundle args = new Bundle();
        args.putString(PAGE, page);
        args.putString(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    public GoodsDetailsFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mPage = getArguments().getString(PAGE);
            mIndex = getArguments().getString(INDEX);
            flags = getArguments().getInt("flags");
            isPublish = (flags == 3 ? true : false);
            canShare = getArguments().getBoolean(CANSHARE, true);
            mSizeId = getArguments().getString(SIZEID);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

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

    private void initView()
    {
        shareUrl = shareUrl + mIndex;
        mDialogUtil = new DialogUtils(mContext);
        shareBtn = (ImageView) mView.findViewById(R.id.share_iv);
        shareBtn.setOnClickListener(onClickListener);
        shareBtn.setVisibility(canShare ? View.VISIBLE : View.GONE);

        loadingView = (LinearLayout) mView.findViewById(R.id.loadingView);
        nullNetView = (LinearLayout) mView.findViewById(R.id.nullNetline);
        nullView = (LinearLayout) mView.findViewById(R.id.nullline);

        reloadBtn = (TextView) mView.findViewById(R.id.nullReload);
        reloadBtn.setOnClickListener(onClickListener);

        reloadNetBtn = (TextView) mView.findViewById(R.id.netReload);
        reloadNetBtn.setOnClickListener(onClickListener);

        initBroadcastReceiver();

        titleTv = (TypeFaceTextView) mView.findViewById(R.id.title_tv);
        titleTv.setText(mContext.getResources().getString(R.string.title_goods_detail));

        sizeParentLine = (LinearLayout) mView.findViewById(R.id.sizeLine);
        sizeSubjectLine = (LinearLayout) mView.findViewById(R.id.attributeNameLine);
        sizeNameParentTv = (TextView) mView.findViewById(R.id.attributeName1);
        sizeNameSubclassTv = (TextView) mView.findViewById(R.id.attributeName2);
        flowLayoutParent = (FlowLayout) mView.findViewById(R.id.flowLayout);
        flowLayoutSubclass = (FlowLayout) mView.findViewById(R.id.flowLayout1);
        myCartBtn = (View) mView.findViewById(R.id.goodsMyCartBtn);
        myCartBtn.setOnClickListener(onClickListener);
        ljBtn = (LinearLayout) mView.findViewById(R.id.goodsLjBuyBtn);
        ljBtn.setOnClickListener(onClickListener);
        addCartBtn = (LinearLayout) mView.findViewById(R.id.goodsAddBuyBtn);
        addCartBtn.setOnClickListener(onClickListener);
        publishBtn = (TextView) mView.findViewById(R.id.goodsPublish);
        viewPager = (ViewPager) mView.findViewById(R.id.goods_adv_pager);
        viewPager.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        viewPager.setOnPageChangeListener(onPageChangeListener);
        viewGroupe = (LinearLayout) mView.findViewById(R.id.goods_viewGroup);

        relativeLayout = (RelativeLayout) mView.findViewById(R.id.imageRl);
        relativeLayout.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenWidth));

        iconView = (LinearLayout) mView.findViewById(R.id.iconView);
        iconOSaleView = (LinearLayout) mView.findViewById(R.id.iconOSaleView);
        commentView = (LinearLayout) mView.findViewById(R.id.commentView);

        mView.findViewById(R.id.timeLine).setVisibility(View.GONE);

        if (isPublish)
        {
            setAddOrBuyShow("此商品已下架", false);
        }

        tv_comment = (TextView) mView.findViewById(R.id.tv_comment);
        tv_baoyou = (TextView) mView.findViewById(R.id.tv_baoyou);
        mCurrentPrice = (TextView) mView.findViewById(R.id.goodsCurrentPrice);
        mOldPrice = (TextView) mView.findViewById(R.id.goodsFormerPrice);
        mOldPrice.getPaint().setAntiAlias(true);
        mOldPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);

        mCartCount = (TextView) mView.findViewById(R.id.tv_cart_count);
        mTipView = (ImageView) mView.findViewById(R.id.myCartTipsTv);
        mDiscount = (TextView) mView.findViewById(R.id.tv_discount);
        mTitle = (TextView) mView.findViewById(R.id.tv_title);
        mTimerView = (TimerTextView) mView.findViewById(R.id.tv_count_time);
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
                    if (scrollY > viewPager.getHeight())
                    {
                        topBtn.setVisibility(View.VISIBLE);
                    }
                    if (scrollY < 700)
                    {
                        topBtn.setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });

        radioGroup = (RadioGroup) mView.findViewById(R.id.goodsRG);
        radioGroup.setOnCheckedChangeListener(onCheckedChangeListener);

        goodsImagesView = (LinearLayout) mView.findViewById(R.id.goodInfoView);
        webView = (CustomProgressWebview) mView.findViewById(R.id.goodsWebView);
        goodsInfoView = (LinearLayout) mView.findViewById(R.id.goodInfo1View);

        mListView = (ListView) mView.findViewById(R.id.lv_good_info);
        mImageContainer = (LinearLayout) mView.findViewById(R.id.ll_img_container);
        imageNull = (TextView) mView.findViewById(R.id.img_null);
        imageNull.setVisibility(View.GONE);

        mView.findViewById(R.id.rl_qq_contact).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                EaseUtils.startKeFuActivity(mContext);
            }
        });

        CartCountManager.newInstance().setOnCartCountListener(this);

        initData();

    }

    /**
     * 注册广播
     */
    private void initBroadcastReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ZhaiDou.IntentRefreshGoodsDetailsTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshOGoodsDetailsTag);

        mContext.registerReceiver(broadcastReceiver, intentFilter);
    }

    /**
     * 登录回调重写
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == 5001)
        {
            checkLogin();
            FetchCountData();
            if (flags == 1)
            {
                if (mDialog != null)
                    mDialog.show();
                FetchOSaleData(UPDATE_LOGIN_ISOSALEBUY);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 数据加载
     */
    private void initData()
    {
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "");
        if (NetworkUtils.isNetworkAvailable(mContext))
        {
            FetchDetailData();
            if (checkLogin())
            {
                FetchCountData();
            }
        } else
        {
            if (mDialog != null)
                mDialog.dismiss();
            nullView.setVisibility(View.GONE);
            nullNetView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 商品信息请求完了，请求商品是否购买过零元特卖商品
     */
    private void setNextEventView()
    {
        if (checkLogin())
        {
            if (flags == 1)
            {
                //请求账户是否已经购买过零元特卖商品
                FetchOSaleData(UPDATE_ISOSALEBUY);
            } else
            {
                if (mDialog != null)
                    mDialog.dismiss();
                loadingView.setVisibility(View.GONE);
            }
        } else
        {
            if (mDialog != null)
                mDialog.dismiss();
            loadingView.setVisibility(View.GONE);
        }
    }

    /**
     * 设置立即购买、添加购物车、是否下架显示的问题
     *
     * @param msg
     */
    private void setAddOrBuyShow(String msg, boolean isShow)
    {
        if (isShow)
        {
            publishBtn.setVisibility(View.GONE);
            ljBtn.setVisibility(View.VISIBLE);
            addCartBtn.setVisibility(View.VISIBLE);
        } else
        {
            publishBtn.setVisibility(View.VISIBLE);
            publishBtn.setText(msg);
            ljBtn.setVisibility(View.GONE);
            addCartBtn.setVisibility(View.GONE);
        }
    }

    /**
     * 设置规格 view
     */
    private void setSizeView()
    {
        sizeNameParentTv.setText(sizeNameParent);
        sizeNameSubclassTv.setText(sizeNameSubclass);
        if (isTwoSize())
        {
            //关联,将所有有相同父的子合并在一起加入到父中去
            for (int i = 0; i < specificationList.size(); i++)
            {
                for (int j = 0; j < subclassSizes.size(); j++)
                {
                    if (specificationList.get(i).title.equals(subclassSizes.get(j).title))
                    {
                        specificationList.get(i).sizess.add(subclassSizes.get(j));
                    }
                }
            }
            sizeParentLine.setVisibility(View.VISIBLE);
            sizeSubjectLine.setVisibility(View.VISIBLE);
            addSizeParentView();
            return;
        } else if (isSizeParent && !isSizeSubclass)
        {
            sizeParentLine.setVisibility(View.GONE);
            sizeSubjectLine.setVisibility(View.VISIBLE);
            addSizeSingleView();
        } else if (!isSizeParent && isSizeSubclass)
        {
            sizeParentLine.setVisibility(View.VISIBLE);
            sizeSubjectLine.setVisibility(View.GONE);
            addSizeSingleView();
        } else
        {
            sizeParentLine.setVisibility(View.GONE);
            sizeSubjectLine.setVisibility(View.GONE);
        }

    }

    /**
     * 添加规格布局（如果是一维的）
     */
    private void addSizeSingleView()
    {
        ToolUtils.setLog("一位规格");
        if (isSizeSubclass)
        {
            flowLayoutParent.removeAllViews();
        }
        if (isSizeParent)
        {
            flowLayoutSubclass.removeAllViews();
        }
        MarginLayoutParams lp = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.leftMargin = 10;
        lp.rightMargin = 5;
        lp.topMargin = 10;
        lp.bottomMargin = 5;
        for (int i = 0; i < specificationList.size(); i++)
        {
            final int position = i;
            final Specification specification = specificationList.get(i);
            ToolUtils.setLog(specification.title);
            View view = LayoutInflater.from(mContext).inflate(R.layout.goods_details_size_item, null);
            final TextView textView = (TextView) view.findViewById(R.id.sizeTitleTv);
            textView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (sigleClickPosition == position)//用来判断是否是否是点击的同一个按钮
                    {
                        if (isClickSingle)
                        {
                            sigleClickPosition = position;
                            for (int j = 0; j < textSingleTvs.size(); j++)
                            {
                                textSingleTvs.get(j).setSelected(false);
                            }
                            textView.setSelected(true);
                            sizeEvent(specification);
                        } else
                        {
                            sigleClickPosition = -1;
                            textView.setSelected(false);
                            mSpecificationParent = null;
                            setResetSize();
                        }
                    } else
                    {
                        sigleClickPosition = position;
                        for (int j = 0; j < textSingleTvs.size(); j++)
                        {
                            textSingleTvs.get(j).setSelected(false);
                        }
                        textView.setSelected(true);
                        sizeEvent(specification);
                    }
                }
            });
            textView.setText(specification.title);
            if (specification.num < 1)
            {
                textView.setBackgroundResource(R.drawable.goods_no_click_selector);
                textView.setTextColor(Color.parseColor("#999999"));
                textView.setClickable(false);
            } else
            {
                if (mSizeId!=null)//当初始规格不为null时，默认选中初始规格
                {
                    if (specification.sizeId.equals(mSizeId+""))
                    {
                        sigleClickPosition = position;
                        textView.setSelected(true);
                        sizeEvent(specification);
                    }
                }
                textSingleTvs.add(textView);
            }
            if (isSizeSubclass)
            {
                flowLayoutParent.addView(view, lp);
            }
            if (isSizeParent)
            {
                flowLayoutSubclass.addView(view, lp);
            }
        }
    }

    /**
     * 添加规格布局
     */
    private void addSizeParentView()
    {
        ToolUtils.setLog("父规格");
        flowLayoutParent.removeAllViews();
        MarginLayoutParams lp = new MarginLayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.leftMargin = 10;
        lp.rightMargin = 5;
        lp.topMargin = 10;
        lp.bottomMargin = 5;
        for (int i = 0; i < specificationList.size(); i++)
        {
            final int position = i;
            View view = LayoutInflater.from(mContext).inflate(R.layout.goods_details_size_item, null);
            final TextView textView = (TextView) view.findViewById(R.id.sizeTitleTv);
            textView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

                    if (doubleClickParentPos == position)//用来判断是否是否是点击的同一个按钮
                    {
                        if (isClickParent)
                        {
                            doubleClickParentPos = position;
                            for (int j = 0; j < textParentTvs.size(); j++)
                            {
                                textParentTvs.get(j).setSelected(false);
                            }
                            textView.setSelected(true);
                            mSpecificationParent = specificationList.get(position);
                            addSizeSubclassView(mSpecificationParent.sizess);
                        } else
                        {
                            doubleClickParentPos = -1;
                            textView.setSelected(false);
                            mSpecificationParent = null;
                            addSizeSubclassView(specificationList.get(0).sizess);
                            setResetSize();
                        }
                    } else
                    {
                        doubleClickParentPos = position;
                        for (int j = 0; j < textParentTvs.size(); j++)
                        {
                            textParentTvs.get(j).setSelected(false);
                        }
                        textView.setSelected(true);
                        mSpecificationParent = specificationList.get(position);
                        addSizeSubclassView(mSpecificationParent.sizess);
                    }
                }
            });
            Specification specification = specificationList.get(i);
            textView.setText(specification.title);

            if (mSizeId!=null)
            {
                for (int j = 0; j < specification.sizess.size(); j++)
                {
                    if (mSizeId.equals(specification.sizess.get(j).sizeId))
                    {
                        doubleClickParentPos = i;
                        textView.setSelected(true);
                        mSpecificationParent = specificationList.get(i);
                        addSizeSubclassView(specification.sizess);
                    }
                }
            } else
            {
                if (0 == position)
                {
                    textView.setSelected(true);
                    mSpecificationParent = specificationList.get(i);
                }
                addSizeSubclassView(specificationList.get(0).sizess);
            }
            textParentTvs.add(textView);
            flowLayoutParent.addView(view, lp);
        }

    }


    /**
     * 添加子规格布局
     */
    private void addSizeSubclassView(List<Specification> sizes)
    {
        ToolUtils.setLog("子规格");
        isClickSubclass = false;
        sigleClickPosition = -1;
        mSpecificationSubclass = null;
        flowLayoutSubclass.removeAllViews();
        MarginLayoutParams lp = new MarginLayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.leftMargin = 10;
        lp.rightMargin = 5;
        lp.topMargin = 10;
        lp.bottomMargin = 5;
        for (int i = 0; i < sizes.size(); i++)
        {
            final int position = i;
            final Specification specification = sizes.get(i);
            View view = LayoutInflater.from(mContext).inflate(R.layout.goods_details_size_item, null);
            final TextView textView = (TextView) view.findViewById(R.id.sizeTitleTv);
            textView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (sigleClickPosition == position)//用来判断是否是否是点击的同一个按钮
                    {
                        if (isClickSubclass)
                        {
                            isClickSubclass = false;
                            sigleClickPosition = -1;
                            textView.setSelected(false);
                            mSpecificationSubclass = null;
                            setResetSize();
                        }
                    } else
                    {
                        isClickSubclass = true;
                        if (mSpecificationParent == null)
                        {
                            ToolUtils.setToast(mContext, "抱歉，请先选择" + sizeNameParent);
                            return;
                        }
                        sigleClickPosition = position;
                        for (int j = 0; j < textSubclassTvs.size(); j++)
                        {
                            textSubclassTvs.get(j).setSelected(false);
                        }
                        textView.setSelected(true);
                        sizeEvent(specification);
                    }
                }
            });
            textView.setText(specification.title1);
            if (specification.num < 1)
            {
                textView.setBackgroundResource(R.drawable.goods_no_click_selector);
                textView.setTextColor(Color.parseColor("#999999"));
                textView.setClickable(false);
            } else
            {
                textView.setSelected(false);
                if (specification.sizeId.equals(mSizeId+""))
                {
                    isClickSubclass = true;
                    sigleClickPosition = 0;
                    textView.setSelected(true);
                    sizeEvent(specification);
                }

//                if (isFristSubclass)
//                {
//                    isFristSubclass = false;
//                    if (specificationList.size() == 1)
//                        if (specificationList.get(0).sizess.size() == 1)
//                        {
//                            isClickSubclass=true;
//                            sigleClickPosition = 0;
//                            textView.setSelected(true);
//                            sizeEvent(specification);
//                        }
//                }
                textSubclassTvs.add(textView);
            }
            flowLayoutSubclass.addView(view, lp);
        }
    }

    /**
     * 复原初始规格
     */
    private void setResetSize()
    {
        setImagesReset();
        mCurrentPrice.setText("￥" + ToolUtils.isIntPrice(detail.price + ""));
        mOldPrice.setText("￥" + ToolUtils.isIntPrice(detail.cost_price + ""));
        setDiscount(detail.price, detail.cost_price);
        if (isPublish)
        {
            setAddOrBuyShow("此商品已下架", false);
        }
    }

    /**
     * 手动刷新规格数据(0零元特卖1普通特卖购买成功后2立即购买请求是否已经购买)刷新规格数量
     */
    private void setRefreshSpecification()
    {
        for (int i = 0; i < specificationList.size(); i++)
        {
            if (flags == 1)//零元特卖
            {
                //判断是否为二维
                if (isTwoSize())
                {
                    for (int j = 0; j < specificationList.get(i).sizess.size(); j++)
                    {
                        specificationList.get(i).sizess.get(j).isBuy = true;
                    }
                } else
                {
                    specificationList.get(i).isBuy = true;
                }
            } else
            {
                //判断是否为二维
                if (isTwoSize())
                {
                    if (mSpecificationSubclass != null)
                    {
                        for (int j = 0; j < specificationList.get(i).sizess.size(); j++)
                        {
                            if (specificationList.get(i).sizess.get(j).sizeId.equals(mSpecificationSubclass.sizeId))
                            {
                                specificationList.get(i).sizess.get(j).num = mSpecificationSubclass.num;
                            }
                        }
                        if (mSpecificationSubclass.num < 1)
                        {
                            mSpecificationSubclass = null;
                            addSizeParentView();
                        }
                    }
                } else
                {
                    if (specificationList.get(i).sizeId.equals(mSpecificationParent.sizeId))
                    {
                        specificationList.get(i).num = mSpecificationParent.num;
                    }
                    if (mSpecificationParent.num < 1)
                    {
                        mSpecificationParent = null;
                        addSizeSingleView();
                    }
                }
            }
        }

    }


    public boolean checkLogin()
    {
        token = (String) SharedPreferencesUtil.getData(mContext, "token", "");
        userId = (Integer) SharedPreferencesUtil.getData(mContext, "userId", -1);
        boolean isLogin = !TextUtils.isEmpty(token) && userId > -1;
        return isLogin;
    }

    /**
     * 选择规格事件处理
     */
    private void sizeEvent(Specification spe)
    {
        if (isTwoSize())
        {
            mSpecificationSubclass = spe;
        } else
        {
            mSpecificationParent = spe;
        }
        setAddOrBuyShow("", true);
        mCurrentPrice.setText("￥" + ToolUtils.isIntPrice("" + spe.price));
        mOldPrice.setText("￥" + ToolUtils.isIntPrice("" + spe.oldPrice));
        setDiscount(spe.price, spe.oldPrice);

        //设置选择规格顶部图片变化
        if (spe.images != null && spe.images.size() > 0)
        {
            if (images != spe.images)
                images.clear();
            images.addAll(spe.images);
            setImageViews();
        }

        if (isOSaleBuy)
        {
            setAddOrBuyShow("您今天已经购买过零元特卖商品", false);
        }
        if (isPublish)
        {
            setAddOrBuyShow("此商品已下架", false);
        }
    }


    /**
     * 折扣处理事件
     *
     * @param current
     * @param old
     */
    private void setDiscount(double current, double old)
    {
        mDiscount.setVisibility(View.VISIBLE);
        if (current != 0 & old != 0)
        {
            DecimalFormat df = new DecimalFormat("##.0");
            String zk = df.format(current / old * 10);
            if (zk.contains(".0"))
            {
                int sales = (int) Double.parseDouble(zk);
                mDiscount.setText(sales + "折");
            } else
            {
                Double sales = Double.parseDouble(zk);
                mDiscount.setText(sales + "折");
            }
        } else
        {
            mDiscount.setVisibility(View.GONE);
        }
    }


    /**
     * 加载子fargment信息
     *
     * @param detail
     * @param goodInfos
     */
    private void setChildFargment(GoodDetail detail, ArrayList<GoodInfo> goodInfos)
    {
        mAdapter = new GoodInfoAdapter(mContext, goodInfos);
        mListView.setAdapter(mAdapter);
//        webView.loadData(detail.webUrl, "text/html; charset=UTF-8", "UTF-8");
        setImageViews();

        mImageContainer.removeAllViews();

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.icon_loading_osale)
                .showImageForEmptyUri(R.drawable.icon_loading_osale)
                .showImageOnFail(R.drawable.icon_loading_osale)
                .resetViewBeforeLoading(true)//default 设置图片在加载前是否重置、复位
                .cacheInMemory(true) // default  设置下载的图片是否缓存在内存中
                .cacheOnDisk(true) // default  设置下载的图片是否缓存在SD卡中
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .build();

        if (detail.imgs != null && detail.imgs.size() > 0)
        {
            for (int i = 0; i < detail.imgs.size(); i++)
            {
                LargeImgView imageView = new LargeImgView(mContext);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                ImageLoader.getInstance().displayImage(detail.imgs.get(i), imageView, options, new ImageLoadingListener()
                {
                    @Override
                    public void onLoadingStarted(String s, View view)
                    {
                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason)
                    {
                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap)
                    {
                        if (bitmap != null)
                        {
                            LargeImgView imageView1 = (LargeImgView) view;
                            imageView1.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
                            if (bitmap.getHeight() < 4000)
                            {
                                imageView1.setScaleType(ImageView.ScaleType.FIT_XY);
                                imageView1.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, bitmap.getHeight() * screenWidth / bitmap.getWidth()));
                                imageView1.setImageBitmap(bitmap);
                            } else
                            {
                                imageView1.setImageBitmapLarge(bitmap);
                            }
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view)
                    {

                    }
                });
                mImageContainer.addView(imageView);
            }
        } else
        {
            imageNull.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 分享
     */
    private void share()
    {
        mDialogUtil = new DialogUtils(mContext);
        mDialogUtil.showShareDialog(mPage, mPage + "  " + shareUrl, detail != null ? detail.imageUrl : null, shareUrl, new PlatformActionListener()
        {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> stringObjectHashMap)
            {
                Message message = handler.obtainMessage(UPDATE_SHARE_TOAST, mContext.getString(R.string.share_completed));
                handler.sendMessage(message);
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable)
            {
                Message message = handler.obtainMessage(UPDATE_SHARE_TOAST, mContext.getString(R.string.share_error));
                handler.sendMessage(message);
            }

            @Override
            public void onCancel(Platform platform, int i)
            {
                Message message = handler.obtainMessage(UPDATE_SHARE_TOAST, mContext.getString(R.string.share_cancel));
                handler.sendMessage(message);
            }
        });
    }


    /**
     * 红色标识提示显示数量
     */
    private void initCartTips()
    {
        if (checkLogin())
        {
            if (cartCount > 0)
            {
                mCartCount.setVisibility(View.VISIBLE);
                mCartCount.setText("" + cartCount);
            } else
            {
                mCartCount.setVisibility(View.GONE);
            }
        } else
        {
            mCartCount.setVisibility(View.GONE);
        }
    }

    /**
     * 顶部图片复位到初始状态
     */
    private void setImagesReset()
    {
        if (images != imageInits)
            images.clear();
        images.addAll(imageInits);
        setImageViews();
    }

    /**
     * 设置顶部滑动图片更新
     */
    public void setImageViews()
    {
        mImageViews.clear();
        for (int i = 0; i < images.size(); i++)
        {
            ImageView imageView = new ImageView(mContext);
            LayoutParams layoutParams = new LayoutParams(screenWidth, screenWidth);
            imageView.setLayoutParams(layoutParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            ToolUtils.setImageCacheUrl(images.get(i), imageView, R.drawable.icon_loading_goods_details);
            mImageViews.add(imageView);
        }
        setInitImageBackground();
        imageAdapter = new ImageAdapter();
        viewPager.setAdapter(imageAdapter);
        viewPager.setOnPageChangeListener(onPageChangeListener);
        imageAdapter.initImags(mImageViews);
    }

    /**
     * 设置顶部导航点初始view
     */
    private void setInitImageBackground()
    {
        viewGroupe.removeAllViews();
        if (images != null)
            if (images.size() == 1)
            {
                return;
            }
        for (int i = 0; i < images.size(); i++)
        {
            ImageView dot_iv = new ImageView(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i == 0)
            {
                params.leftMargin = 0;
            } else
            {
                params.leftMargin = 20;
            }
            dot_iv.setLayoutParams(params);
            if (i == 0)
            {
                dot_iv.setBackgroundResource(R.drawable.home_tips_foucs_icon);
            } else
            {
                dot_iv.setBackgroundResource(R.drawable.home_tips_icon);
            }
            viewGroupe.addView(dot_iv);
            dots.add(dot_iv);
        }
    }

    /**
     * 设置指示器
     */
    private void setImageBackground(int position)
    {
        for (int i = 0; i < dots.size(); i++)
        {
            if (i == position)
            {
                dots.get(position).setBackgroundResource(R.drawable.home_tips_foucs_icon);
            } else
            {
                dots.get(i).setBackgroundResource(R.drawable.home_tips_icon);
            }
        }
    }

    /**
     * 是否是二维规格
     *
     * @return
     */
    private boolean isTwoSize()
    {
        if (isSizeParent == false && isSizeSubclass == false)
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * 添加或者购买前准备
     */
    private void PrepareAddOrBuy()
    {
        if (checkLogin())
        {
            if (detail != null)
                if (isTwoSize())//如果是二维
                {
                    if (mSpecificationParent == null)
                    {
                        scrollView.scrollTo(0, 450);
                        ToolUtils.setToast(mContext, "抱歉，请先选择" + sizeNameParent);
                        return;
                    }
                    if (mSpecificationSubclass != null)
                    {
                        if (isAddOrBuy)
                        {
                            AddCart();
                        } else//立即购买
                        {
                            if (flags == 1)//判断零元特卖是否已经购买过
                            {
                                mDialog.show();
                                FetchOSaleAddData();
                            } else
                            {
                                CommitLjBuy();
                            }
                        }
                    } else
                    {
                        scrollView.scrollTo(0, 700);
                        Toast.makeText(mContext, "抱歉,先选择" + sizeNameSubclass, Toast.LENGTH_SHORT).show();
                    }
                } else
                {
                    if (mSpecificationParent != null)
                    {
                        if (isAddOrBuy)
                        {
                            AddCart();
                        } else//立即购买
                        {
                            if (flags == 1)//判断零元特卖是否已经添加过
                            {
                                mDialog.show();
                                FetchOSaleAddData();
                            } else
                            {
                                CommitLjBuy();
                            }
                        }
                    } else
                    {
                        scrollView.scrollTo(0, 450);
                        Toast.makeText(mContext, "抱歉,先选择" + sizeNameParent, Toast.LENGTH_SHORT).show();
                    }
                }
        } else
        {
            Intent intent = new Intent(mContext, LoginActivity.class);
            intent.setFlags(3);
            startActivityForResult(intent, 5001);
        }
    }

    /**
     * 加入购物车
     */
    private void AddCart()
    {
        mDialog.show();
        if (flags == 1)
        {
            FetchOSaleAddData();
        } else
        {
            FetchAddCartData();
        }
    }

    /**
     * 立即购买
     */
    private void CommitLjBuy()
    {
        arrayItems.clear();
        cartGoodsItem.num = 1;
        cartGoodsItem.name = detail.title;
        if (isTwoSize())
        {
            cartGoodsItem.size = mSpecificationSubclass.title + mSpecificationSubclass.title1;
            cartGoodsItem.sizeId = mSpecificationSubclass.sizeId;
            cartGoodsItem.currentPrice = mSpecificationSubclass.price;
            cartGoodsItem.formalPrice = mSpecificationSubclass.oldPrice;

            cartArrayItem.storeMoney = mSpecificationSubclass.price;
            if (mSpecificationSubclass.images != null)
            {
                cartGoodsItem.imageUrl = mSpecificationSubclass.images.get(0);
            } else
            {
                cartGoodsItem.imageUrl = detail.imageUrl;
            }
        } else
        {
            cartGoodsItem.size = mSpecificationParent.title;
            cartGoodsItem.sizeId = mSpecificationParent.sizeId;
            cartGoodsItem.currentPrice = mSpecificationParent.price;
            cartGoodsItem.formalPrice = mSpecificationParent.oldPrice;

            cartArrayItem.storeMoney = mSpecificationParent.price;
            if (mSpecificationParent.images != null)
            {
                cartGoodsItem.imageUrl = mSpecificationParent.images.get(0);
            } else
            {
                cartGoodsItem.imageUrl = detail.imageUrl;
            }
        }


        if (flags == 1)
        {
            cartGoodsItem.isOSale = "true";
        } else
        {
            cartGoodsItem.isOSale = "false";
        }
        List<CartGoodsItem> goodsItems = new ArrayList<CartGoodsItem>();
        goodsItems.clear();
        goodsItems.add(cartGoodsItem);
        cartArrayItem.storeCount = 1;
        cartArrayItem.goodsItems = goodsItems;
        arrayItems.add(cartArrayItem);

        ShopOrderOkFragment shopOrderOkFragment = ShopOrderOkFragment.newInstance("", 0);
        Bundle bundle = new Bundle();
        bundle.putInt("flags", 1);
        bundle.putSerializable("goodsList", arrayItems);
        shopOrderOkFragment.setArguments(bundle);
        ((BaseActivity) getActivity()).navigationToFragment(shopOrderOkFragment);

    }


    public void FetchDetailData()
    {
        subclassSizes.clear();
        String url = ZhaiDou.HomeGoodsDetailsUrl + mIndex;
        ZhaiDouRequest request = new ZhaiDouRequest(mContext,url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                if (jsonObject != null)
                {
                    long endTime = jsonObject.optLong("timestamp");
                    JSONObject dataObject = jsonObject.optJSONObject("data");
                    if (dataObject == null)
                    {
                        if (mDialog != null)
                            mDialog.dismiss();
                        nullView.setVisibility(View.VISIBLE);
                        nullNetView.setVisibility(View.GONE);
                        return;
                    }
                    String goodsId = dataObject.optString("productId");
                    goodsName = dataObject.optString("productName");
                    sizeNameParent = dataObject.optString("attributeName1");
                    sizeNameSubclass = dataObject.optString("attributeName2");
                    flags = dataObject.optString("businessType").equals("01") ? 0 : 1;
                    isPublish = dataObject.optInt("isProductShelves") == 0 ? true : false;

                    JSONObject expandObject = dataObject.optJSONObject("expandedResponse");
                    String designer = expandObject.optString("productDescription");//豆豆点评
                    String storeId = expandObject.optString("storeId");//店铺 ID
                    String storeName = expandObject.optString("storeName");// 店铺名称
                    String brandId = expandObject.optString("brandId");//店铺商标ID
                    String brandName = expandObject.optString("brandName");// 店铺商标名称
                    String builtPlaceId = expandObject.optString("builtPlaceId");//商品所在产地标识
                    String builtPlaceName = expandObject.optString("builtPlaceName");//商品所在产地名称
                    int praiseCount = expandObject.optInt("praiseCount");//点赞数
                    int collectCount = expandObject.optInt("collectCount");//收藏数
                    int commentCount = expandObject.optInt("commentCount");//评论数
                    int saleCount = expandObject.optInt("saleCount");//销量
                    int orderCount = expandObject.optInt("orderCount");//收藏数
                    int viewCount = expandObject.optInt("viewCount");//浏览数
                    String score = expandObject.optString("score");//商品评分
                    String discount = expandObject.optString("discount");
                    int totalCount = expandObject.optInt("productStock");
                    double price = expandObject.optDouble("salePrice");
                    double markerPrice = expandObject.optDouble("markerPrice");
                    String imageUrl = expandObject.optString("imageUrl") + ".jpg";
                    images.add(imageUrl);
                    imageInits.add(imageUrl);
                    int userMaxNum = expandObject.optInt("userMaxNum");// 一定时间内特卖限购数量
                    int zeroMaxCount = expandObject.optInt("zeroMaxCount");//0元每天购限购数量
                    int userMaxType = expandObject.optInt("userMaxType");//特卖限购时间  单位小时
                    String integrityDesc = expandObject.optString("integrityDesc");//图文详情

                    JSONArray descriptions = expandObject.optJSONArray("attributeList");
                    if (descriptions != null && descriptions.length() > 0)
                    {
                        for (int i = 0; i < descriptions.length(); i++)
                        {
                            JSONObject description = descriptions.optJSONObject(i);
                            int descriptionsId = description.optInt("id");
                            String descriptionsTitle = description.optString("attributeName");
                            String value = description.optString("attributeValue");
                            GoodInfo goodInfo = new GoodInfo(descriptionsId, descriptionsTitle, value);
                            goodInfos.add(goodInfo);
                        }

                    }
                    JSONArray imgArrays = expandObject.optJSONArray("productInfoImages");
                    ArrayList<String> imgsList = new ArrayList<String>();
                    if (imgArrays != null && imgArrays.length() > 0 && !imgArrays.equals(""))
                    {
                        for (int i = 0; i < imgArrays.length(); i++)
                        {
                            String url = imgArrays.optString(i);
                            imgsList.add(url);
                        }
                    }
                    JSONArray specifications = dataObject.optJSONArray("productSKUArray");
                    if (specifications != null && specifications.length() > 0 && !specifications.equals(""))
                    {
                        specificationList = new ArrayList<Specification>();
                        for (int i = 0; i < specifications.length(); i++)
                        {
                            JSONObject specificationObj = specifications.optJSONObject(i);
                            int isMajor = specificationObj.optInt("isMajor");
                            String specificationTitle = specificationObj.optString("attributeValue1");
                            String specificationTitle1 = specificationObj.optString("attributeValue2");
                            if (isMajor == 1)//主规格
                            {
                                if (specificationTitle.equals("F"))
                                {
                                    isSizeParent = true;
                                }
                                if (specificationTitle1.equals("F"))
                                {
                                    isSizeSubclass = true;
                                }
                            }
                        }
                        for (int i = 0; i < specifications.length(); i++)
                        {
                            JSONObject specificationObj = specifications.optJSONObject(i);
                            String specificationId = specificationObj.optString("productSKUId");
                            String specificationTitle = specificationObj.optString("attributeValue1");//父标题
                            String specificationTitle1 = specificationObj.optString("attributeValue2");//子标题

                            JSONArray imageArray = specificationObj.optJSONArray("productSKUImagArray");
                            ArrayList<String> sizeImages = new ArrayList<String>();
                            if (imageArray != null && imageArray.length() > 0)
                            {
                                for (int j = 0; j < imageArray.length(); j++)
                                {
                                    JSONObject imageObj = imageArray.optJSONObject(j);
                                    String url = imageObj.optString("imageUrl") + imageObj.optString("imageFileType");
                                    sizeImages.add(url);
                                }
                            }
                            int num = specificationObj.optInt("stock");
                            if (num > 0)
                            {
                                isOver = false;
                            }
                            double sizePrice = specificationObj.optDouble("price");
                            double sizeOldPrice = specificationObj.optDouble("marketPrice");
                            double returnPrice = specificationObj.optDouble("returnAmount");//返现金额

                            if (isTwoSize())
                            {
                                //子集规格集合
                                Specification specificationSubclassItem = new Specification();//子
                                specificationSubclassItem.sizeId = specificationId;
                                specificationSubclassItem.title = specificationTitle;
                                specificationSubclassItem.title1 = specificationTitle1;
                                specificationSubclassItem.price = sizePrice;
                                specificationSubclassItem.oldPrice = sizeOldPrice;
                                specificationSubclassItem.num = num;
                                specificationSubclassItem.images = sizeImages;
                                subclassSizes.add(specificationSubclassItem);

                                Specification specificationParentItem = new Specification();//父
                                specificationParentItem.title = specificationTitle;
                                for (int j = 0; j < specificationList.size(); j++)//将相同的父规格合并在一起
                                {
                                    if (specificationList.get(j).title.equals(specificationTitle))
                                    {
                                        specificationList.remove(j);
                                    }
                                }
                                specificationList.add(specificationParentItem);
                            } else
                            {
                                Specification specification = new Specification();
                                specification.sizeId = specificationId;
                                if (isSizeParent)
                                {
                                    specification.title = specificationTitle1;
                                }
                                if (isSizeSubclass)
                                {
                                    specification.title = specificationTitle;
                                }
                                specification.price = sizePrice;
                                specification.oldPrice = sizeOldPrice;
                                specification.num = num;
                                specification.images = sizeImages;

                                specificationList.add(specification);
                            }
                        }
                    }
                    cartArrayItem.storeId = storeId;
                    cartArrayItem.storeName = storeName;

                    detail = new GoodDetail();
                    detail.goodsId = goodsId;
                    detail.title = goodsName;
                    detail.designer = designer;
                    detail.discount = discount;
                    detail.end_time = endTime;
                    detail.total_count = totalCount;
                    detail.price = price;
                    detail.cost_price = markerPrice;
                    detail.imageUrl = imageUrl;
                    detail.imgs = imgsList;
                    detail.specifications = specificationList;
                    detail.goodsInfo = goodInfos;
                    detail.webUrl = integrityDesc;

                    handler.obtainMessage(UPDATE_GOOD_DETAIL, detail).sendToTarget();

                } else
                {
                    if (mDialog != null)
                        mDialog.dismiss();
                    nullView.setVisibility(View.VISIBLE);
                    nullNetView.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                nullView.setVisibility(View.VISIBLE);
                nullNetView.setVisibility(View.GONE);
            }
        });
        mRequestQueue.add(request);
    }

    /**
     * 请求购物车列表数据
     */
    public void FetchCountData()
    {
        Api.getCartCount(userId, new Api.SuccessListener()
        {
            @Override
            public void onSuccess(Object jsonObject)
            {
                if (jsonObject != null)
                {
                    JSONObject object = ((JSONObject) jsonObject).optJSONObject("data");
                    cartCount = object.optInt("totalQuantity");
                    handler.sendEmptyMessage(UPDATE_CARTCAR_DATA);
                    CartCountManager.newInstance().notify(cartCount);
                }
            }
        }, null);

    }

    /**
     * 加入购物车接口请求
     */
    public void FetchAddCartData()
    {
        String url = "";
        if (isTwoSize())
        {
            url = ZhaiDou.GoodsDetailsAddUrl + userId + "&productSKUId=" + mSpecificationSubclass.sizeId;
        } else
        {
            url = ZhaiDou.GoodsDetailsAddUrl + userId + "&productSKUId=" + mSpecificationParent.sizeId;
        }
        ToolUtils.setLog(url);
        ZhaiDouRequest request = new ZhaiDouRequest(mContext,url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                if (jsonObject != null)
                {
                    int status = jsonObject.optInt("status");
                    String message = jsonObject.optString("message");
                    if (status == 200)
                    {
                        handler.sendEmptyMessage(UPDATE_ADD_CART);
                    } else
                    {
                        ToolUtils.setToast(mContext, message);
                    }
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                ToolUtils.setToastLong(mContext, R.string.loading_fail_txt);
            }
        });
        mRequestQueue.add(request);
    }

    /**
     * 零元特卖是否购买请求
     */
    public void FetchOSaleData(final int i)
    {
        String url = ZhaiDou.IsBuyOSaleUrl + userId;
        ToolUtils.setLog(url);
        ZhaiDouRequest request = new ZhaiDouRequest(mContext,url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                if (jsonObject != null)
                {
                    isOSaleBuy = jsonObject.optInt("ifBuy") == 1 ? false : true;
                }
                if (i == UPDATE_LJBUY_ISOSALEBUY)
                {
                    handler.sendEmptyMessage(UPDATE_LJBUY_ISOSALEBUY);
                } else if (i == UPDATE_ISOSALEBUY)
                {
                    handler.sendEmptyMessage(UPDATE_ISOSALEBUY);
                } else if (i == UPDATE_LOGIN_ISOSALEBUY)
                {
                    handler.sendEmptyMessage(UPDATE_ISOSALEBUY);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                if (i == UPDATE_ISOSALEBUY)
                {
                    loadingView.setVisibility(View.GONE);
                } else
                {
                    ToolUtils.setToastLong(mContext, R.string.loading_fail_txt);
                }
            }
        });
        mRequestQueue.add(request);
    }

    /**
     * 零元特卖是否已经加入购物车了
     */
    public void FetchOSaleAddData()
    {
        String url = ZhaiDou.IsAddOSaleUrl + userId;
        ToolUtils.setLog(url);
        ZhaiDouRequest request = new ZhaiDouRequest(mContext,url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                mDialog.dismiss();
                int status = jsonObject.optInt("status");
                if (status != 500)
                {
                    if (jsonObject != null)
                    {
                        sku = jsonObject.optString("productSKUId");
                        isOSaleAdd = sku.length() > 0 ? true : false;
                    }
                    handler.sendEmptyMessage(UPDATE_ISADD_CART);
                } else
                {
                    ToolUtils.setToastLong(mContext, R.string.loading_fail_txt);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                ToolUtils.setToastLong(mContext, R.string.loading_fail_txt);
            }
        });
        mRequestQueue.add(request);
    }

    /**
     * 删除商品数据
     */
    public void FetchGoodsDeleteData()
    {
        String url = ZhaiDou.CartGoodsDeleteUrl + userId + "&productSKUId=" + "[" + sku + "]";
        ToolUtils.setLog("url:" + url);
        ZhaiDouRequest request = new ZhaiDouRequest(mContext,url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                if (jsonObject != null)
                {
                    int status = jsonObject.optInt("status");
                    if (status == 200)
                    {
                        handler.sendEmptyMessage(UPDATE_OSALE_DELETE);
                    }
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                if (mDialog != null)
                    mDialog.dismiss();
            }
        });
        mRequestQueue.add(request);
    }

    @Override
    public void onDestroy()
    {
        mTimerView.stop();
        mContext.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onResume()
    {
        if (isFrist)
        {
            long temp = Math.abs(systemTime - System.currentTimeMillis());
            initTime = mTimerView.getTimes() - temp;
            mTimerView.setTimes(initTime);
        }
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_goods_detail));
        mDialogUtil.dismiss();
    }

    @Override
    public void onPause()
    {
        systemTime = System.currentTimeMillis();
        isFrist = true;
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_goods_detail));
    }

    /**
     * 购物车数量变化刷新
     * @param count
     */
    @Override
    public void onChange(int count)
    {
        cartCount=count;
        initCartTips();
    }

    /**
     * 规格适配器
     */
    public class GoodInfoAdapter extends BaseListAdapter<GoodInfo>
    {
        public GoodInfoAdapter(Context context, List<GoodInfo> list)
        {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent)
        {
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


    public class ImageAdapter extends PagerAdapter
    {
        List<ImageView> imageViews = new ArrayList<ImageView>();

        public void initImags(List<ImageView> imageViewss)
        {
            imageViews.addAll(imageViewss);
            notifyDataSetChanged();
        }

        @Override
        public int getCount()
        {
            return imageViews.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o)
        {
            return view == o;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object)
        {
            container.removeView(imageViews.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            container.addView(imageViews.get(position));
            return imageViews.get(position);
        }
    }

}
