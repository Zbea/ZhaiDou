package com.zhaidou.fragments;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;

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
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.dialog.CustomToastDialog;
import com.zhaidou.model.CartArrayItem;
import com.zhaidou.model.CartGoodsItem;
import com.zhaidou.model.CountTime;
import com.zhaidou.model.GoodDetail;
import com.zhaidou.model.GoodInfo;
import com.zhaidou.model.Specification;
import com.zhaidou.utils.DeviceUtils;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.FlowLayout;
import com.zhaidou.view.LargeImgView;
import com.zhaidou.view.TimerTextView;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;

public class GoodsDetailsFragment extends BaseFragment
{
    private static final String PAGE = "page";
    private static final String INDEX = "index";
    private static final String ISSHOWTIMER = "timer";
    private static final String CANSHARE = "canShare";

    private String mPage;
    private String mIndex;
    private View mView;
    private int flags;//1代表零元特卖；2代表已下架商品
    private Context mContext;
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
    private RequestQueue mRequestQueue;

    private ArrayList<GoodInfo> goodInfos = new ArrayList<GoodInfo>();
    private TextView tv_comment, mCurrentPrice, mOldPrice, mDiscount, mTitle, tv_baoyou;

    private final int UPDATE_GOOD_DETAIL = 0;
    private final int UPDATE_CARTCAR_DATA = 1;
    private final int UPDATE_LJBUY_ISOSALEBUY = 2;//零元特卖立即购买时候判断是否已经购买郭
    private final int UPDATE_ISOSALEBUY = 3;//判断进来时候零元特卖是否已经购买了，购买了才让按钮不能点击
    private final int UPDATE_LOGIN_ISOSALEBUY = 4;//判断进来时候零元特卖是否已经购买了，购买了才让按钮不能点击
    private final int UPDATE_LJ_ISBUY = 5;//判断立即购买时普通特卖是否购买过
    private final int UPDATE_ADD_ISBUY = 6;//判断加入购物车时普通特卖是否购买过
    private final int UPDATE_ADD_CART = 7;//加入购物车

    private ScrollView scrollView;
    private ImageView topBtn;
    private LinearLayout iconView, iconOSaleView, commentView;

    private LinearLayout loadingView, nullNetView, nullView;
    private TextView reloadBtn, reloadNetBtn;
    private TextView sizeNameTv1, sizeNameTv2;
    private LinearLayout sizeLine1, sizeLine2;
    private String sizeName1, sizeName2;
    private FlowLayout flowLayout, flowLayout1;
    private boolean isSizeShow1;
    private boolean isSizeShow2;

    private GoodDetail detail;
    private GoodsImageAdapter imageAdapter;
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
    private boolean isPublish = false;
    private boolean canShare;
    private TextView imageNull;
    private TimerTextView mTimerView;
    private List<TextView> texts = new ArrayList<TextView>();
    private List<TextView> texts1 = new ArrayList<TextView>();
    private List<TextView> texts2 = new ArrayList<TextView>();
    private int mClick = -1;
    private int mClick1 = -1;
    private int mClick2 = -1;
    private boolean isClick;//规格是否可以点击
    private boolean isClick1;//规格是否可以点击
    private Specification mSpecification;//选中型号
    private Specification mSpecification1;//选中规格
    private List<Specification> specificationList;

    private boolean isOSaleBuy;//是否购买过零元特卖
    private boolean isBuy;//是否购买过普通特卖该商品规格

    private long initTime;
    private long systemTime;
    private boolean isFrist;
    private int userId;
    private String token;
    long mTime = 0;
    private int cartCount;//购物车商品数量
    private Integer template_type = -1;
    private ArrayList<CartArrayItem> arrayItems = new ArrayList<CartArrayItem>();
    private List<Specification> subclassSizes = new ArrayList<Specification>();
    private CartArrayItem cartArrayItem = new CartArrayItem();
    private CartGoodsItem cartGoodsItem = new CartGoodsItem();
    boolean isOver = true;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(ZhaiDou.IntentRefreshCartGoodsCheckTag))
            {
                FetchCountData();
            }
            if (action.equals(ZhaiDou.IntentRefreshLoginTag))
            {
                FetchCountData();
            }
            if (action.equals(ZhaiDou.IntentRefreshLoginExitTag))
            {
                initCartTips();
            }
            if (action.equals(ZhaiDou.IntentRefreshOGoodsDetailsTag))
            {
                setAddOrBuyShow("不能重复购买", false);
                setRefreshSpecification(0);
            }
            if (action.equals(ZhaiDou.IntentRefreshGoodsDetailsTag))
            {
                if (mSpecification != null)
                {
//                    mSpecification.isBuy=true;
                    mSpecification.num = mSpecification.num - 1;
                    setRefreshSpecification(1);
                }
            }

        }
    };

    private Handler myHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 0:
                    //用来清除动画后留下的垃圾
                    try
                    {
                        animation_viewGroup.removeAllViews();
                    } catch (Exception e)
                    {

                    }
                    isClean = false;

                    break;
                default:
                    break;
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
                    } else
                    {
                        iconView.setVisibility(View.VISIBLE);
                        iconOSaleView.setVisibility(View.GONE);
                        commentView.setVisibility(View.VISIBLE);
                        tv_baoyou.setVisibility(View.VISIBLE);
                    }

                    detail = (GoodDetail) msg.obj;
                    setChildFargment(detail, goodInfos);

                    mCurrentPrice.setText("￥" + ToolUtils.isIntPrice("" + detail.price + ""));
                    mOldPrice.setText("￥" + ToolUtils.isIntPrice("" + detail.cost_price + ""));
                    tv_comment.setText(detail.designer);
                    mTitle.setText(detail.title);
                    setDiscount(detail.price, detail.cost_price);
                    ToolUtils.setImageCacheUrl(detail.imageUrl, goodsImage, R.drawable.icon_loading_goods_details);

                    if (detail.specifications != null)
                    {
                        setSizeView();
                    }
                    if (isOver)
                    {
                        setAddOrBuyShow("已卖光", false);
                    }
                    if (isPublish)
                    {
                        setAddOrBuyShow("此商品已下架", false);
                    }
                    shareBtn.setVisibility(template_type != 0 ? View.VISIBLE : View.GONE);
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
                        Log.i("Exception e", e.getMessage());
                    }
                    break;
                case UPDATE_CARTCAR_DATA:
                    initCartTips();
                    break;
                case UPDATE_LJBUY_ISOSALEBUY:
                    if (mDialog != null)
                        mDialog.dismiss();
                    if (isOSaleBuy)
                    {
                        setAddOrBuyShow("不能重复购买", false);
                    }
                    break;
                case UPDATE_ISOSALEBUY:
                    if (mDialog != null)
                        mDialog.dismiss();
                    loadingView.setVisibility(View.GONE);
                    if (isOSaleBuy)
                    {
                        setAddOrBuyShow("不能重复购买", false);
                    }
                    break;

                case UPDATE_ADD_CART:
                    cartCount = cartCount + 1;
                    Intent intent = new Intent(ZhaiDou.IntentRefreshAddCartTag);
                    mContext.sendBroadcast(intent);
                    int[] location = new int[2];
                    mTipView.getLocationInWindow(location);
                    Drawable drawable = mTipView.getDrawable();
                    doAnim(drawable, location);
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
                case R.id.back_btn:
                    ((MainActivity) getActivity()).popToStack(GoodsDetailsFragment.this);
                    break;
                case R.id.goodsMyCartBtn:
                    if (checkLogin())
                    {
                        ShopCartFragment shopCartFragment = ShopCartFragment.newInstance("", 0);
                        ((MainActivity) getActivity()).navigationToFragment(shopCartFragment);
                    } else
                    {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(3);
                        startActivityForResult(intent, 5001);
                    }
                    break;
                case R.id.goodsLjBuyBtn:
                    if (checkLogin())
                    {
                        if (detail != null)
                            if (isSizeShow1==false&&isSizeShow2==false)
                            {
                                if (mSpecification1==null)
                                {
                                    scrollView.scrollTo(0, 405);
                                    ToolUtils.setToast(mContext,"抱歉，请先选择"+sizeName1);
                                    return;
                                }
                            }
                            if (mSpecification != null)
                            {

                                if (flags == 1)//判断零元特卖是否已经购买郭
                                {
                                    mDialog.show();
                                } else
                                {
                                    commitLjBuy();
                                }
                            } else
                            {
                                scrollView.scrollTo(0, 600);
                                Toast.makeText(mContext, "抱歉,先选择"+sizeName2, Toast.LENGTH_SHORT).show();
                            }
                    } else
                    {
                        Intent intent = new Intent(mContext, LoginActivity.class);
                        intent.setFlags(3);
                        startActivityForResult(intent, 5001);
                    }
                    break;
                case R.id.goodsAddBuyBtn:
                    if ((System.currentTimeMillis() - mTime) > 1000)
                    {
                        mTime = System.currentTimeMillis();
                        AddCart();
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

    public static GoodsDetailsFragment newInstance(String page, String index)
    {
        GoodsDetailsFragment fragment = new GoodsDetailsFragment();
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
            isPublish = (flags == 2 ? true : false);
            canShare = getArguments().getBoolean(CANSHARE, true);
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

        backBtn = (TypeFaceTextView) mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);
        titleTv = (TypeFaceTextView) mView.findViewById(R.id.title_tv);
        titleTv.setText(mContext.getResources().getString(R.string.title_goods_detail));

        sizeLine1 = (LinearLayout) mView.findViewById(R.id.sizeLine);
        sizeLine2 = (LinearLayout) mView.findViewById(R.id.attributeNameLine);
        sizeNameTv1 = (TextView) mView.findViewById(R.id.attributeName1);
        sizeNameTv2 = (TextView) mView.findViewById(R.id.attributeName2);
        flowLayout = (FlowLayout) mView.findViewById(R.id.flowLayout);
        flowLayout1 = (FlowLayout) mView.findViewById(R.id.flowLayout1);

        viewGroupe = (LinearLayout) mView.findViewById(R.id.goods_viewGroup);

        myCartBtn = (View) mView.findViewById(R.id.goodsMyCartBtn);
        myCartBtn.setOnClickListener(onClickListener);
        ljBtn = (LinearLayout) mView.findViewById(R.id.goodsLjBuyBtn);
        ljBtn.setOnClickListener(onClickListener);
        addCartBtn = (LinearLayout) mView.findViewById(R.id.goodsAddBuyBtn);
        addCartBtn.setOnClickListener(onClickListener);

        publishBtn = (TextView) mView.findViewById(R.id.goodsPublish);

        goodsImage = (ImageView) mView.findViewById(R.id.goodsImageView);
        goodsImage.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenWidth));

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
                    if (scrollY < 600)
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
                if (DeviceUtils.isApkInstalled(getActivity(), "com.tencent.mobileqq"))
                {
                    String url = "mqqwpa://im/chat?chat_type=wpa&uin=" + mContext.getResources().getString(R.string.QQ_Number);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } else
                {
                    ShowToast("没有安装QQ客户端哦");
                }
            }
        });

        initCartTips();

        initData();

    }

    /**
     * 注册广播
     */
    private void initBroadcastReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ZhaiDou.IntentRefreshCartGoodsCheckTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshCartGoodsTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginExitTag);
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


    private void setSizeView()
    {
        sizeNameTv1.setText(sizeName1);
        sizeNameTv2.setText(sizeName2);
        if (isSizeShow1)
        {
            sizeLine1.setVisibility(View.GONE);
            sizeLine2.setVisibility(View.VISIBLE);
            addSpecificationView();
        }
        if (isSizeShow2)
        {
            sizeLine1.setVisibility(View.VISIBLE);
            sizeLine2.setVisibility(View.GONE);
            addSpecificationView();
        }
        if (!isSizeShow1 && !isSizeShow2)
        {
            //关联
            for (int i = 0; i < specificationList.size(); i++)
            {
                for (int j = 0; j <subclassSizes.size(); j++)
                {
                    if (specificationList.get(i).title.equals(subclassSizes.get(j).title))
                    {
                        specificationList.get(i).sizess.add(subclassSizes.get(j));
                    }
                }
            }
            sizeLine1.setVisibility(View.VISIBLE);
            sizeLine2.setVisibility(View.VISIBLE);
            addSpecificationView1();
        }
    }

    /**
     * 添加规格布局
     */
    private void addSpecificationView1()
    {
        flowLayout.removeAllViews();
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

                    if (mClick1 == position)//用来判断是否是否是点击的同一个按钮
                    {
                        if (isClick1)
                        {
                            mClick1 = position;
                            for (int j = 0; j < texts1.size(); j++)
                            {
                                texts1.get(j).setSelected(false);
                            }
                            textView.setSelected(true);
                            mSpecification1=specificationList.get(position);
                            addSpecificationView2(mSpecification1.sizess);
                        } else
                        {
                            mClick1 = -1;
                            textView.setSelected(false);
                            mSpecification1=null;
                            addSpecificationView2(specificationList.get(0).sizess);

                        }
                    } else
                    {
                        mClick1= position;
                        for (int j = 0; j < texts1.size(); j++)
                        {
                            texts1.get(j).setSelected(false);
                        }
                        textView.setSelected(true);
                        mSpecification1=specificationList.get(position);
                        addSpecificationView2(mSpecification1.sizess);
                    }
                }
            });
            Specification specification = specificationList.get(i);
            textView.setText(specification.title);
                if (0 == position)
                {
                    textView.setSelected(true);
                    mSpecification1=specificationList.get(i);
                }
                texts1.add(textView);
            flowLayout.addView(view, lp);
        }
        addSpecificationView2(specificationList.get(0).sizess);
    }

    /**
     * 添加规格布局（如果是一维的）
     */
    private void addSpecificationView()
    {
        flowLayout.removeAllViews();
        MarginLayoutParams lp = new MarginLayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.leftMargin = 10;
        lp.rightMargin = 5;
        lp.topMargin = 10;
        lp.bottomMargin = 5;
        for (int i = 0; i < specificationList.size(); i++)
        {
            final int position = i;
            final Specification specification = specificationList.get(i);
            View view = LayoutInflater.from(mContext).inflate(R.layout.goods_details_size_item, null);
            final TextView textView = (TextView) view.findViewById(R.id.sizeTitleTv);
            textView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

                    if (mClick == position)//用来判断是否是否是点击的同一个按钮
                    {
                        if (isClick)
                        {
                            mClick = position;
                            for (int j = 0; j < texts.size(); j++)
                            {
                                texts.get(j).setSelected(false);
                            }
                            textView.setSelected(true);
                            sizeEvent(position,specification);
                        } else
                        {
                            mClick = -1;
                            textView.setSelected(false);
                            mSpecification = null;
                            mCurrentPrice.setText("￥" + ToolUtils.isIntPrice(detail.price + ""));
                            mOldPrice.setText("￥" + ToolUtils.isIntPrice(detail.cost_price + ""));
                            setDiscount(detail.price, detail.cost_price);
                            if (isPublish)
                            {
                                setAddOrBuyShow("此商品已下架", false);
                            }

                        }
                    } else
                    {
                        mClick = position;
                        for (int j = 0; j < texts.size(); j++)
                        {
                            texts.get(j).setSelected(false);
                        }
                        textView.setSelected(true);
                        sizeEvent(position,specification);
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
                if (mClick == position)
                {
                    textView.setSelected(true);
                } else
                {
                    textView.setSelected(false);
                }
                texts.add(textView);
            }
            flowLayout.addView(view, lp);
        }
    }


    /**
     * 添加子规格布局
     */
    private void addSpecificationView2(List<Specification> sizes)
    {
        flowLayout1.removeAllViews();
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

                    if (mClick == position)//用来判断是否是否是点击的同一个按钮
                    {
                        if (isClick)
                        {
                            if (isSizeShow1==false&&isSizeShow2==false)
                            {
                                if (mSpecification1==null)
                                {
                                    ToolUtils.setToast(mContext,"抱歉，请先选择"+sizeName1);
                                    return;
                                }
                            }
                            mClick = position;
                            for (int j = 0; j < texts2.size(); j++)
                            {
                                texts2.get(j).setSelected(false);
                            }
                            textView.setSelected(true);
                            sizeEvent(position,specification);
                        } else
                        {
                            mClick = -1;
                            textView.setSelected(false);
                            mSpecification = null;
                            mCurrentPrice.setText("￥" + ToolUtils.isIntPrice(detail.price + ""));
                            mOldPrice.setText("￥" + ToolUtils.isIntPrice(detail.cost_price + ""));
                            setDiscount(detail.price, detail.cost_price);
                            if (isPublish)
                            {
                                setAddOrBuyShow("此商品已下架", false);
                            }

                        }
                    } else
                    {
                        if (isSizeShow1==false&&isSizeShow2==false)
                        {
                            if (mSpecification1==null)
                            {
                                ToolUtils.setToast(mContext,"抱歉，请先选择"+sizeName1);
                                return;
                            }
                        }
                        mClick = position;
                        for (int j = 0; j < texts2.size(); j++)
                        {
                            texts2.get(j).setSelected(false);
                        }
                        textView.setSelected(true);
                        sizeEvent(position,specification);
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
                if (mClick == position)
                {
                    textView.setSelected(true);
                } else
                {
                    textView.setSelected(false);
                }
                texts2.add(textView);
            }
            flowLayout1.addView(view, lp);
        }
    }

    /**
     * 刷新规格数据(0零元特卖1普通特卖购买成功后2立即购买请求是否已经购买)刷新规格数量
     */
    private void setRefreshSpecification(int flag)
    {
        for (int i = 0; i < specificationList.size(); i++)
        {
            if (flag == 0)
            {
                specificationList.get(i).isBuy = true;
            } else if (flag == 1)
            {
                if (specificationList.get(i).sizeId.equals(mSpecification.sizeId))
                {
                    specificationList.get(i).isBuy = true;
                    specificationList.get(i).num = specificationList.get(i).num - 1;
                }
            } else
            {
                if (specificationList.get(i).sizeId.equals(mSpecification.sizeId))
                {
                    specificationList.get(i).num = mSpecification.num;
                }
            }
        }
        if (mSpecification.num < 1)
        {
            mSpecification = null;
            addSpecificationView();
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
    private void sizeEvent(int position,Specification spe)
    {
        mSpecification = spe;
        if (mSpecification.isBuy)
        {
            setAddOrBuyShow("不能重复购买", false);
        } else
        {
            setAddOrBuyShow("", true);
            mCurrentPrice.setText("￥" + ToolUtils.isIntPrice("" + mSpecification.price));
            mOldPrice.setText("￥" + ToolUtils.isIntPrice("" + mSpecification.oldPrice));
            setDiscount(mSpecification.price, mSpecification.oldPrice);
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

        mImageContainer.removeAllViews();
        if (detail.imgs != null)
        {
            for (int i = 0; i < detail.imgs.size(); i++)
            {
                LargeImgView imageView = new LargeImgView(mContext);
                imageView.setScaleType(ImageView.ScaleType.MATRIX);
                ImageLoader.getInstance().displayImage(detail.imgs.get(i), imageView, new ImageLoadingListener()
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
        DialogUtils mDialogUtils = new DialogUtils(mContext);
        mDialogUtils.showShareDialog(mPage, mPage + "  " + shareUrl, detail.imageUrl, shareUrl, new PlatformActionListener()
        {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> stringObjectHashMap)
            {
                Toast.makeText(mContext, mContext.getString(R.string.share_completed), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable)
            {
                Toast.makeText(mContext, mContext.getString(R.string.share_error), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel(Platform platform, int i)
            {
                Toast.makeText(mContext, mContext.getString(R.string.share_cancel), Toast.LENGTH_SHORT).show();
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
     * 设置指示器
     */
    private void setImageBackground(int position)
    {
        for (int i = 0; i < dots.length; i++)
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

    /**
     * 加入购物车
     */
    private void AddCart()
    {
        if (checkLogin())
        {
            if (detail != null)
                if (isSizeShow1==false&&isSizeShow2==false)
                {
                    if (mSpecification1==null)
                    {
                        scrollView.scrollTo(0, 405);
                        ToolUtils.setToast(mContext,"抱歉，请先选择"+sizeName1);
                        return;
                    }
                }
                if (mSpecification != null)
                {
                    mDialog.show();
                    FetchAddCartData();
                } else
                {
                    scrollView.scrollTo(0, 600);
                    Toast.makeText(mContext, "抱歉,先选择"+sizeName2, Toast.LENGTH_SHORT).show();
                }
        } else
        {
            Intent intent = new Intent(mContext, LoginActivity.class);
            intent.setFlags(3);
            startActivityForResult(intent, 5001);
        }
    }

    /**
     * 立即购买
     */
    private void commitLjBuy()
    {
        cartGoodsItem.num = 1;
        cartGoodsItem.name = detail.title;
        cartGoodsItem.size = mSpecification.title;
        cartGoodsItem.sku = mSpecification.sizeId;
        cartGoodsItem.currentPrice = mSpecification.price;
        cartGoodsItem.formalPrice = mSpecification.oldPrice;
        cartGoodsItem.imageUrl = detail.imageUrl;
        if (flags == 1)
        {
            cartGoodsItem.isOSale = "true";
        } else
        {
            cartGoodsItem.isOSale = "false";
        }
        List<CartGoodsItem> goodsItems = new ArrayList<CartGoodsItem>();
        goodsItems.add(cartGoodsItem);
        cartArrayItem.storeCount = 1;
        cartArrayItem.storeMoney = mSpecification.price;
        cartArrayItem.goodsItems = goodsItems;
        arrayItems.add(cartArrayItem);

        ShopOrderOkFragment shopOrderOkFragment = ShopOrderOkFragment.newInstance("", 0);
        Bundle bundle = new Bundle();
        bundle.putInt("flags", 1);
        bundle.putSerializable("goodsList", arrayItems);
        shopOrderOkFragment.setArguments(bundle);
        ((MainActivity) getActivity()).navigationToFragment(shopOrderOkFragment);

    }


    public void FetchDetailData()
    {
        subclassSizes.clear();
        String url = ZhaiDou.HomeGoodsDetailsUrl + mIndex;
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
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
                    }
                    String goodsId = dataObject.optString("productId");
                    String goodsName = dataObject.optString("productName");
                    sizeName1 = dataObject.optString("attributeName1");
                    sizeName2 = dataObject.optString("attributeName2");
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
                    int userMaxNum = expandObject.optInt("userMaxNum");// 一定时间内特卖限购数量
                    int zeroMaxCount = expandObject.optInt("zeroMaxCount");//0元每天购限购数量
                    int userMaxType = expandObject.optInt("userMaxType");//特卖限购时间  单位小时

                    JSONArray descriptions = expandObject.optJSONArray("attributeList");
                    if (descriptions != null && descriptions.length() > 0 && !descriptions.equals(""))
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

                    JSONArray imgArrays = dataObject.optJSONArray("productImageArray");
                    ArrayList<String> imgsList = new ArrayList<String>();
                    if (imgArrays != null && imgArrays.length() > 0 && !imgArrays.equals(""))
                    {
                        for (int i = 0; i < imgArrays.length(); i++)
                        {
                            JSONObject imgObj = imgArrays.optJSONObject(i);
                            String url = imgObj.optString("imageUrl") + imgObj.optString("imageFileType");
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
                            String specificationId = specificationObj.optString("productSKUId");
                            String specificationTitle = specificationObj.optString("attributeValue1");
                            String specificationTitle1 = specificationObj.optString("attributeValue2");
                            if (isMajor == 1)
                            {
                                if (specificationTitle.equals("F"))
                                {
                                    isSizeShow1 = true;
                                }
                                if (specificationTitle1.equals("F"))
                                {
                                    isSizeShow2 = true;
                                }
                            }
                            JSONArray imageArray = specificationObj.optJSONArray("productSKUImagArray");
                            if (imageArray != null && imageArray.length() > 0)
                            {
                                for (int j = 0; j < imageArray.length(); j++)
                                {
                                    JSONObject imageObj = imageArray.optJSONObject(i);
                                    if (imageObj != null)
                                    {
                                        String url = imageObj.optString("imageArray") + imageObj.optString("imageFileType");
                                    }
                                }
                            }
                            int num = specificationObj.optInt("stock");
                            if (num>0)
                            {
                                isOver = false;
                            }
                            double sizePrice = specificationObj.optDouble("price");
                            double sizeOldPrice = specificationObj.optDouble("marketPrice");
                            double returnPrice = specificationObj.optDouble("returnAmount");//返现金额

                            if (isSizeShow1==false && isSizeShow2==false)
                            {
                                //子集规格集合
                                Specification specificationSubclassItem = new Specification();
                                specificationSubclassItem.sizeId = specificationId;
                                specificationSubclassItem.title = specificationTitle;
                                specificationSubclassItem.title1 = specificationTitle1;
                                specificationSubclassItem.price = sizePrice;
                                specificationSubclassItem.oldPrice = sizeOldPrice;
                                specificationSubclassItem.num = num;
                                subclassSizes.add(specificationSubclassItem);

                                Specification specification = new Specification();
                                specification.title = specificationTitle;
                                for (int j = 0; j <specificationList.size() ; j++)
                                {
                                    if (specificationList.get(j).title.equals(specificationTitle))
                                    {
                                        specificationList.remove(j);
                                    }
                                }
                                specificationList.add(specification);
                            }
                            else
                            {
                                Specification specification = new Specification();
                                specification.sizeId = specificationId;
                                specification.title = specificationTitle;
                                specification.title = specificationTitle1;
                                specification.price = sizePrice;
                                specification.oldPrice = sizeOldPrice;
                                specification.num = num;
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

                    Message message = new Message();
                    message.what = UPDATE_GOOD_DETAIL;
                    message.obj = detail;
                    handler.sendMessage(message);
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
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        mRequestQueue.add(request);
    }

    /**
     * 请求购物车列表数据
     */
    public void FetchCountData()
    {
        String url = ZhaiDou.CartGoodsCountUrl;
        ToolUtils.setLog("url:" + url);
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                if (jsonObject != null)
                {
                    JSONObject object = jsonObject.optJSONObject("data");
                    cartCount = object.optInt("totalQuantity");
                    handler.sendEmptyMessage(UPDATE_CARTCAR_DATA);
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
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        mRequestQueue.add(request);
    }

    /**
     * 加入购物车接口请求
     */
    public void FetchAddCartData()
    {
        String url = ZhaiDou.GoodsDetailsAddUrl + mSpecification.sizeId;
        ToolUtils.setLog(url);
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
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
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                headers.put("SECAuthorization", token);
                return headers;
            }
        };
        mRequestQueue.add(request);
    }

    /**
     * 零元特卖是否购买请求
     */
    public void FetchOSaleData(final int i)
    {
        String url = ZhaiDou.orderCheckOSaleUrl;
        ToolUtils.setLog(url);
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                if (jsonObject != null)
                {
                    isOSaleBuy = jsonObject.optBoolean("flag");
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
                    nullView.setVisibility(View.VISIBLE);
                    nullNetView.setVisibility(View.GONE);
                } else
                {
                    ToolUtils.setToastLong(mContext, R.string.loading_fail_txt);
                }
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                headers.put("SECAuthorization", token);
                return headers;
            }
        };
        mRequestQueue.add(request);
    }


    private void doAnim(Drawable drawable, int[] start_location)
    {
        if (!isClean)
        {
            setAnim(drawable, start_location);
        } else
        {
            try
            {
                animation_viewGroup.removeAllViews();
                isClean = false;
                setAnim(drawable, start_location);
            } catch (Exception e)
            {
                e.printStackTrace();
            } finally
            {
                isClean = true;
            }
        }
    }

    private void setAnim(Drawable drawable, int[] start_location)
    {
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

        mAnimationSet.setAnimationListener(new Animation.AnimationListener()
        {

            @Override
            public void onAnimationStart(Animation animation)
            {
                number++;
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                number--;
                if (number == 0)
                {
                    isClean = true;
                    myHandler.sendEmptyMessage(0);
                }
                mTipView.setVisibility(View.GONE);
                initCartTips();
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
            }
        });
        view.startAnimation(mAnimationSet);

    }

    private View addViewToAnimLayout(ViewGroup vg, View view, int[] location)
    {
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

    private int dip2px(Context context, float dpValue)
    {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private FrameLayout createAnimLayout()
    {
        ViewGroup rootView = (ViewGroup) getActivity().getWindow().getDecorView();
        FrameLayout animLayout = new FrameLayout(getActivity());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        animLayout.setLayoutParams(lp);
        animLayout.setBackgroundColor(Color.parseColor("#00000000"));
        rootView.addView(animLayout);
        return animLayout;

    }


    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
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
    }

    @Override
    public void onPause()
    {
        systemTime = System.currentTimeMillis();
        isFrist = true;
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_goods_detail));
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        super.onHiddenChanged(hidden);
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


}
