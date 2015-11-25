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
import com.zhaidou.utils.DeviceUtils;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.ChildGridView;
import com.zhaidou.view.FlowLayout;
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

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;

public class GoodsDetailsFragment extends BaseFragment
{
    private static final String PAGE = "page";
    private static final String INDEX = "index";
    private static final String ISSHOWTIMER = "timer";
    private static final String CANSHARE = "canShare";

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
    private FlowLayout flowLayout;

    private Dialog mDialog;
    private ChildGridView mGridView;
    private RequestQueue mRequestQueue;

    private ArrayList<GoodInfo> goodInfos = new ArrayList<GoodInfo>();
    private int mSpecificationSelectPosition = -1;

    private TextView tv_comment, mCurrentPrice, mOldPrice, mDiscount, mTitle,tv_baoyou;


    private final int UPDATE_GOOD_DETAIL = 0;
    private final int UPDATE_COUNT_DOWN_TIME = 1;
    private final int UPDATE_UI_TIMER_FINISH = 2;
    private final int UPDATE_TIMER_START = 3;
    private final int UPDATE_CARTCAR_DATA = 4;
    private final int UPDATE_LJBUY_ISOSALEBUY = 5;//零元特卖立即购买时候判断是否已经购买郭
    private final int UPDATE_ISOSALEBUY = 6;//判断进来时候零元特卖是否已经购买了，购买了才让按钮不能点击
    private final int UPDATE_LOGIN_ISOSALEBUY = 7;//判断进来时候零元特卖是否已经购买了，购买了才让按钮不能点击
    private final int UPDATE_LJ_ISBUY = 8;//判断立即购买时普通特卖是否购买过
    private final int UPDATE_ADD_ISBUY = 9;//判断加入购物车时普通特卖是否购买过

    private List<CartItem> items = new ArrayList<CartItem>();
    private CreatCartDB creatCartDB;
    private int num;
    private ScrollView scrollView;
    private ImageView topBtn;
    private LinearLayout iconView, iconOSaleView, commentView;

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
    private boolean isPublish=false;
    private boolean isShowTimer;
    private boolean canShare;
    private MyTimer mTimer;
    private TextView mTimerView, imageNull;
    private ArrayList<String> listUrls = new ArrayList<String>();
    private List<TextView> texts = new ArrayList<TextView>();

    private boolean isOSaleBuy;//是否购买过零元特卖
    private boolean isBuy;//是否购买过普通特卖该商品规格
    private boolean isClick;
    private int mClick = -1;
    private long temp;

    private int userId;
    private String token;
    private long temptime;
    private long currentTime;
    long mTime = 0;
    private Integer template_type=-1;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(ZhaiDou.IntentRefreshCartGoodsCheckTag))
            {
                initCartTips();
            }
            if (action.equals(ZhaiDou.IntentRefreshLoginTag))
            {
                initCartTips();
            }
            if (action.equals(ZhaiDou.IntentRefreshLoginExitTag))
            {
                initCartTips();
            }
            if (action.equals(ZhaiDou.IntentRefreshOGoodsDetailsTag))
            {
                setAddOrBuyShow("不能重复购买",false);
                setRefreshSpecification(0);
            }
            if (action.equals(ZhaiDou.IntentRefreshGoodsDetailsTag))
            {
                if (mSpecification!=null)
                setAddOrBuyShow("不能重复购买",false);
                mSpecification.isBuy=true;
                mSpecification.num=mSpecification.num-1;
                setRefreshSpecification(1);
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

                    mCurrentPrice.setText("￥" + ToolUtils.isIntPrice("" + detail.getPrice() + ""));
                    mOldPrice.setText("￥" + ToolUtils.isIntPrice("" + detail.getCost_price() + ""));
                    tv_comment.setText(detail.getDesigner());
                    mTitle.setText(detail.getTitle());
                    setDiscount(detail.getPrice(), detail.getCost_price());
                    ToolUtils.setImageCacheUrl(detail.getImageUrl(), goodsImage, R.drawable.icon_loading_goods_details);

                    boolean isOver = true;
                    if (detail.getSpecifications()!=null)
                    {
                        specificationAdapter.addAll(detail.getSpecifications());
                        addSpecificationView();
                        for (int i = 0; i < detail.getSpecifications().size(); i++)
                        {
                            //遍历该商品的全部规格的库存数，如果当有库存数大于0的，则isOver标志位为false
                            if (detail.getSpecifications().get(i).num > 0)
                            {
                                isOver = false;
                                break;
                            }
                        }
                    }
                    if (isOver)
                    {
                        setAddOrBuyShow("已卖光",false);
                    }
                    if (isPublish)
                    {
                        setAddOrBuyShow("此商品已下架",false);
                    }
                    String end_date = detail.getEnd_time();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    System.out.println("GoodsDetailsFragment.handleMessage------------>"+template_type);
                    mView.findViewById(R.id.timeLine).setVisibility(template_type!=0?View.VISIBLE:View.GONE);
                    shareBtn.setVisibility(template_type!=0?View.VISIBLE:View.GONE);
                    try
                    {
                        long millionSeconds = sdf.parse(end_date).getTime();//毫秒
                        temp = millionSeconds - System.currentTimeMillis();
                        ToolUtils.setLog("temp:" + temp);
                        if (temp <= 0)
                        {
                            mTimerView.setText("已结束");
                            setAddOrBuyShow("活动已结束",false);
                        }
                        if (isShowTimer||template_type==0){
                            mTimer = new MyTimer(temp, 1000);
                            mTimer.start();
                        }
                    } catch (Exception e)
                    {
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
                    mTimerView.setText("已结束");
                    setAddOrBuyShow("活动已结束",false);
                    break;
                case UPDATE_LJBUY_ISOSALEBUY:
                    if (mDialog != null)
                        mDialog.dismiss();
                    if (isOSaleBuy)
                    {
                        setAddOrBuyShow("不能重复购买",false);
                    } else
                    {
                        for (int i = 0; i < items.size(); i++)
                        {
                            if (items.get(i).isOSale.equals("true"))
                            {
                                ljBuyOkDialog(items.get(i),"购物车已经有一件零元特卖商品,继续购买将删除掉该商品，是否删除？");
                                return;
                            }
                        }
                        buyGoods();
                    }
                    break;
                case UPDATE_ISOSALEBUY:
                    if (mDialog != null)
                        mDialog.dismiss();
                    loadingView.setVisibility(View.GONE);
                    if (isOSaleBuy)
                    {
                        setAddOrBuyShow("不能重复购买",false);
                    }
                    break;
                case UPDATE_CARTCAR_DATA:
                    int num = msg.arg2;
                    mCartCount.setVisibility(num > 0 ? View.VISIBLE : View.GONE);
                    mCartCount.setText("" + num);
                    break;
                case UPDATE_LJ_ISBUY:
                    if (mDialog != null)
                        mDialog.dismiss();
                    if (isBuy)
                    {
                        setRefreshSpecification(2);
                        setAddOrBuyShow("不能重复购买",false);
                        ToolUtils.setToastLong(mContext,"抱歉,今天已经购买过该规格的商品,请明天再购买");
                    }
                    else
                    {
                        for (int i = 0; i < items.size(); i++)
                        {
                            if (items.get(i).sizeId==mSpecification.getId())
                            {
                                ljBuyOkDialog(items.get(i),"购物车已经有一件该商品,继续购买将删除掉该商品，是否删除？");
                                return;
                            }
                        }
                        buyGoods();
                    }
                    break;
                case UPDATE_ADD_ISBUY:
                    if (mDialog != null)
                        mDialog.dismiss();
                    if (isBuy)
                    {
                        setRefreshSpecification(2);
                        setAddOrBuyShow("不能重复购买",false);
                        ToolUtils.setToastLong(mContext,"抱歉,今天已经购买过该规格的商品,请明天再试");
                    }
                    else
                    {
                        addCartGoods();
                    }
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
                        startActivityForResult(intent,5001);
                    }
                    break;
                case R.id.goodsLjBuyBtn:
                    if (checkLogin())
                    {
                        if (detail != null)
                            if (mSpecification != null)
                            {
                                mDialog.show();
                                if (flags == 1)//判断零元特卖是否已经购买郭
                                {
                                    FetchOSaleData(UPDATE_LJBUY_ISOSALEBUY);
                                } else
                                {
                                    FetchGoodsData(UPDATE_LJ_ISBUY);
                                }
                            } else
                            {
                                scrollView.scrollTo(0, 405);
                                Toast.makeText(mContext, "抱歉,先选择规格", Toast.LENGTH_SHORT).show();
                            }
                    } else
                    {
                        Intent intent = new Intent(mContext, LoginActivity.class);
                        intent.setFlags(3);
                        startActivityForResult(intent,5001);
                    }
                    break;
                case R.id.goodsAddBuyBtn:
                    if ((System.currentTimeMillis() - mTime) > 1000)
                    {
                        mTime = System.currentTimeMillis();
                        addGoods();
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

    public static GoodsDetailsFragment newInstance(String page, int index)
    {
        GoodsDetailsFragment fragment = new GoodsDetailsFragment();
        Bundle args = new Bundle();
        args.putString(PAGE, page);
        args.putInt(INDEX, index);
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
            mIndex = getArguments().getInt(INDEX);
            flags=getArguments().getInt("flags");
            isPublish = (flags==2?true:false);
            isShowTimer=getArguments().getBoolean(ISSHOWTIMER,true);
            canShare=getArguments().getBoolean(CANSHARE,true);
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
        shareBtn.setVisibility(canShare?View.VISIBLE:View.GONE);

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
        goodsImage.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenWidth));

        relativeLayout = (RelativeLayout) mView.findViewById(R.id.imageRl);
        relativeLayout.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenWidth));

        iconView = (LinearLayout) mView.findViewById(R.id.iconView);
        iconOSaleView = (LinearLayout) mView.findViewById(R.id.iconOSaleView);
        commentView = (LinearLayout) mView.findViewById(R.id.commentView);

        mView.findViewById(R.id.timeLine).setVisibility(View.GONE);

        if (isPublish)
        {
            setAddOrBuyShow("此商品已下架",false);
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

        flowLayout=(FlowLayout) mView.findViewById(R.id.flowLayout);

        specificationAdapter = new SpecificationAdapter(getActivity(), new ArrayList<Specification>(), mSpecificationSelectPosition);
        mGridView.setAdapter(specificationAdapter);

        specificationAdapter.setOnInViewClickListener(R.id.sizeTitleTv, new BaseListAdapter.onInternalClickListener()
        {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values)
            {
                if (((Specification) values).num > 0)
                {
                    if (mClick == position)
                    {
                        if (isClick == false)
                        {
                            mClick = position;
                            specificationAdapter.setCheckPosition(mSpecificationSelectPosition = position);
                            specificationAdapter.notifyDataSetChanged();
                            sizeEvent(position);
                            isClick = true;

                        } else
                        {
                            mClick = -1;
                            isClick = false;
                            specificationAdapter.setCheckPosition(mSpecificationSelectPosition = -1);
                            specificationAdapter.notifyDataSetChanged();
                            mSpecification = null;

                            mCurrentPrice.setText("￥" + ToolUtils.isIntPrice(detail.getPrice() + ""));
                            mOldPrice.setText("￥" + ToolUtils.isIntPrice(detail.getCost_price() + ""));
                            setDiscount(detail.getPrice(), detail.getCost_price());
                            if (isPublish)
                            {
                                setAddOrBuyShow("此商品已下架",false);
                            }
                        }
                    } else
                    {
                        mClick = position;
                        specificationAdapter.setCheckPosition(mSpecificationSelectPosition = position);
                        specificationAdapter.notifyDataSetChanged();
                        sizeEvent(position);
                        isClick = true;
                    }
                }
            }
        });

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
            if (DeviceUtils.isApkInstalled(getActivity(),"com.tencent.mobileqq")){
                String url = "mqqwpa://im/chat?chat_type=wpa&uin=" + mContext.getResources().getString(R.string.QQ_Number);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }else {
                ShowToast("没有安装QQ客户端哦");
            }
        }
    });

        creatCartDB = new CreatCartDB(mContext);
        initCartTips();

        initData();

    }

    /**
     * 登录回调重写
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode==5001)
        {
            if (flags==1)
            {
                if (mDialog!=null)
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
            FetchDetailData(mIndex);
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
            }
            else
            {
                if (mDialog != null)
                    mDialog.dismiss();
                loadingView.setVisibility(View.GONE);
            }
        }
        else
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
    private void setAddOrBuyShow(String msg,boolean isShow)
    {
        if (isShow)
        {
            publishBtn.setVisibility(View.GONE);
            ljBtn.setVisibility(View.VISIBLE);
            addCartBtn.setVisibility(View.VISIBLE);
        }
        else
        {
            publishBtn.setVisibility(View.VISIBLE);
            publishBtn.setText(msg);
            ljBtn.setVisibility(View.GONE);
            addCartBtn.setVisibility(View.GONE);
        }
    }

    /**
     * 添加规格布局
     */
    private void addSpecificationView()
    {
        MarginLayoutParams lp = new MarginLayoutParams(
                LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        lp.leftMargin = 10;
        lp.rightMargin = 5;
        lp.topMargin = 10;
        lp.bottomMargin = 5;
        for (int i = 0; i <specificationList.size(); i++)
        {
            final int position = i;
            View view= LayoutInflater.from(mContext).inflate(R.layout.goods_details_size_item,null);
            final TextView textView =(TextView)view.findViewById(R.id.sizeTitleTv);
            textView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    boolean isChlic=true;
                    if (mClick == position)//用来判断是否是否是点击的同一个按钮
                    {
                        if (isClick)
                        {
                            mClick = position;
                            isChlic=false;
                            for (int j = 0; j < texts.size(); j++)
                            {
                                texts.get(j).setSelected(false);
                            }
                            textView.setSelected(true);
                            sizeEvent(position);
                        } else
                        {
                            mClick = -1;
                            isChlic=true;
                            textView.setSelected(false);
                            mSpecification = null;
                            mCurrentPrice.setText("￥" + ToolUtils.isIntPrice(detail.getPrice() + ""));
                            mOldPrice.setText("￥" + ToolUtils.isIntPrice(detail.getCost_price() + ""));
                            setDiscount(detail.getPrice(), detail.getCost_price());
                            if (isPublish)
                            {
                                setAddOrBuyShow("此商品已下架",false);
                            }

                        }
                    } else
                    {
                        mClick = position;
                        isChlic=false;
                        for (int j = 0; j < texts.size(); j++)
                        {
                            texts.get(j).setSelected(false);
                        }
                        textView.setSelected(true);
                        sizeEvent(position);
                    }
                }
            });
            Specification specification=specificationList.get(i);
            textView.setText(specification.getTitle());
            if (specification.num<1)
            {
                textView.setBackgroundResource(R.drawable.goods_no_click_selector);
                textView.setTextColor(Color.parseColor("#999999"));
                textView.setClickable(false);
            }
            else
            {
                textView.setSelected(false);
                texts.add(textView);
            }
            flowLayout.addView(view,lp);
        }
    }

    /**
     * 刷新规格数据(0零元特卖1普通特卖购买成功后2立即购买请求是否已经购买)刷新规格数量
     */
    private void setRefreshSpecification(int flag)
    {
        for (int i = 0; i < specificationList.size(); i++)
        {
            if (flag==0)
            {
                specificationList.get(i).isBuy=true;
            }
            else if(flag==1)
            {
                if (specificationList.get(i).getId()==mSpecification.getId())
                {
                    specificationList.get(i).isBuy=true;
                    specificationList.get(i).num=specificationList.get(i).num-1;
                }
            }
            else
            {
                if (specificationList.get(i).getId()==mSpecification.getId())
                {
                    specificationList.get(i).isBuy=true;
                    specificationList.get(i).num=mSpecification.num;
                }
            }
        }
        if (mSpecification.num<1)
        {
            mSpecification=null;
        }
        specificationAdapter.notifyDataSetChanged();
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
    private void sizeEvent(int position)
    {
        if (detail != null & specificationList.size() > 0)
        {
            mSpecification = specificationList.get(position);
            if (mSpecification.isBuy)
            {
                setAddOrBuyShow("不能重复购买",false);
            }
            else
            {
                setAddOrBuyShow("",true);
                mCurrentPrice.setText("￥" + ToolUtils.isIntPrice("" + mSpecification.price));
                mOldPrice.setText("￥" + ToolUtils.isIntPrice("" + mSpecification.oldPrice));
                setDiscount(mSpecification.price, mSpecification.oldPrice);
            }
            if (isPublish)
            {
                setAddOrBuyShow("此商品已下架",false);
            }
            if (temp <= 0)
            {
                mTimerView.setText("已结束");
                setAddOrBuyShow("活动已结束",false);
            }
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
        ToolUtils.setLog(""+goodInfos.size());
        mAdapter = new GoodInfoAdapter(mContext, goodInfos);
        mListView.setAdapter(mAdapter);
        mImageContainer.removeAllViews();
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.icon_loading_osale)
//                        .showImageForEmptyUri(R.drawable.icon_loading_osale)
//                        .showImageOnFail(R.drawable.icon_loading_osale)
//                        .resetViewBeforeLoading(true)//default 设置图片在加载前是否重置、复位
//                        .bitmapConfig(Bitmap.Config.RGB_565)
//                        .imageScaleType(ImageScaleType.NONE)
//                        .cacheInMemory(true) // default
                .build();
        if (detail.getImgs() != null)
        {
            for (int i = 0; i < detail.getImgs().size(); i++)
            {
                LargeImgView imageView = new LargeImgView(mContext);
                imageView.setScaleType(ImageView.ScaleType.MATRIX);
                ImageLoader.getInstance().displayImage(detail.getImgs().get(i), imageView, new ImageLoadingListener()
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
                                imageView1.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, bitmap.getHeight()*screenWidth/bitmap.getWidth()));
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
//        ShareSDK.initSDK(mContext);
//        OnekeyShare oks = new OnekeyShare();
//        //关闭sso授权
//        oks.disableSSOWhenAuthorize();
//        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
//        oks.setTitle(mPage);
//        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
//        oks.setTitleUrl(shareUrl);
//        // text是分享文本，所有平台都需要这个字段
//        oks.setText(mPage + "   " + shareUrl);
//        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
//        if (detail != null)
//        {
//            oks.setImageUrl(detail.getImageUrl());//确保SDcard下面存在此张图片
//        }
//        // url仅在微信（包括好友和朋友圈）中使用
//        oks.setUrl(shareUrl);
//        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
////            oks.setComment("我是测试评论文本");
//        // site是分享此内容的网站名称，仅在QQ空间使用
//        oks.setSite(getString(R.string.app_name));
//        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
//        oks.setSiteUrl(shareUrl);
//
//        oks.show(mContext);

        DialogUtils mDialogUtils=new DialogUtils(mContext);
        mDialogUtils.showShareDialog(mPage,mPage+"  "+shareUrl,detail.getImageUrl(),shareUrl,new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> stringObjectHashMap) {
                Toast.makeText(mContext,mContext.getString(R.string.share_completed),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                Toast.makeText(mContext,mContext.getString(R.string.share_error),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel(Platform platform, int i) {
                Toast.makeText(mContext,mContext.getString(R.string.share_cancel),Toast.LENGTH_SHORT).show();
            }
        });
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
     * 红色标识提示显示数量
     */
    private void initCartTips()
    {
        if (checkLogin())
        {
            num = 0;
            getGoodsItems();
            for (int i = 0; i < items.size(); i++)
            {
                if (items.get(i).isPublish.equals("false") && items.get(i).isOver.equals("false"))
                {
                    num = num + items.get(i).num;
                }
            }
            Message message = new Message();
            message.arg2 = num;
            message.what = UPDATE_CARTCAR_DATA;
            handler.sendMessage(message);
        } else
        {
            Message message = new Message();
            message.arg2 = num;
            message.what = UPDATE_CARTCAR_DATA;
            handler.sendMessage(message);
        }
    }

    /**
     * 获得当前userId的所有商品
     */
    private void getGoodsItems()
    {
        items = CreatCartTools.selectByAll(creatCartDB, userId);
    }

    /**
     * 立即购买
     */
    private void buyGoods()
    {
        CartItem cartItem = new CartItem();
        cartItem.userId = userId;
        cartItem.id = detail.getId();
        cartItem.name = detail.getTitle();
        cartItem.creatTime = System.currentTimeMillis();
        cartItem.imageUrl = detail.getImageUrl();
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
        if (flags == 1)
        {
            cartItem.isOSale = "true";
        } else
        {
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
     * 添加商品
     */
    private void addGoods()
    {
        if (checkLogin())
        {
            if (mSpecification != null)
            {
                if (isAdd())
                {
                    ToolUtils.setToastLong(mContext, "抱歉,已经添加过该商品");
                    return;
                }
                if (flags == 1)
                {
                    if (isOSaleBuy)
                    {
                        ToolUtils.setToastLong(mContext, "抱歉,您已经购买过零元特卖商品,今天不能再添加了");
                        return;
                    } else
                    {
                        for (int i = 0; i < items.size(); i++)
                        {
                            if (items.get(i).isOSale.equals("true"))
                            {
                                addCartOkDialog(items.get(i));
                                return;
                            }
                        }
                        addCartGoods();
                    }

                } else
                {
                    if (mDialog!=null)
                        mDialog.show();
                    FetchGoodsData(UPDATE_ADD_ISBUY);
                }
            } else
            {
                scrollView.scrollTo(0, 405);
                ToolUtils.setToast(mContext, "抱歉,先选择规格");
            }

        } else
        {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(3);
            startActivityForResult(intent,5001);
        }

    }

    /**
     * 判断商品是否库存不走
     *
     * @return true 库存足
     */
    private boolean isOver()
    {
        for (int i = 0; i < items.size(); i++)
        {
            if (items.get(i).sizeId == mSpecification.getId())
            {
                if (mSpecification.num >= items.get(i).num + 1)
                {
                    return true;
                } else
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 是否已经添加过该规格的商品
     *
     * @return
     */
    private boolean isAdd()
    {
        for (int i = 0; i < items.size(); i++)
        {
            if (items.get(i).sizeId == mSpecification.getId())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 加入到购物车
     */
    private void addCartGoods()
    {
        if (detail != null)
        {
            if (isOver())
            {
                int[] location = new int[2];
                mTipView.getLocationInWindow(location);
                Drawable drawable = mTipView.getDrawable();
                doAnim(drawable, location);

                CartItem cartItem = new CartItem();
                cartItem.userId = userId;
                cartItem.id = detail.getId();
                cartItem.name = detail.getTitle();
                cartItem.creatTime = System.currentTimeMillis();
                cartItem.imageUrl = detail.getImageUrl();
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
                } else
                {
                    cartItem.isOSale = "false";
                }
                CreatCartTools.insertByData(creatCartDB, items, cartItem);

                Intent intent = new Intent(ZhaiDou.IntentRefreshCartGoodsTag);
                mContext.sendBroadcast(intent);
            } else
            {
                CustomToastDialog.setToastDialog(mContext, "抱歉,商品数量不足,请勿继续添加");
            }
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

    public void FetchDetailData(int id)
    {
        listUrls.clear();
        String url = ZhaiDou.goodsDetailsUrlUrl + id;
        Log.i("url---------------------->", url);
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                if (jsonObject != null)
                {
                    JSONObject merchandise = jsonObject.optJSONObject("merchandise");
                    template_type=merchandise.optInt("template_type");
                    int id = merchandise.optInt("id");
                    String title = merchandise.optString("title");
                    String designer = merchandise.optString("designer");
                    int total_count = merchandise.optInt("total_count");
                    double price = merchandise.optDouble("price");
                    double cost_price = merchandise.optDouble("cost_price");
                    int discount = merchandise.optInt("discount");
                    String end_time = merchandise.optString("end_time");
                    isPublish= merchandise.optBoolean("is_publish")==true?false:true;
                    flags=merchandise.optInt("sale_cate");
                    detail = new GoodDetail(id, title, designer, total_count, price, cost_price, discount);
                    detail.setEnd_time(end_time);

                    JSONArray imgArrays = merchandise.optJSONArray("imgs");
                    if (imgArrays != null && imgArrays.length() > 0)
                    {
                        ArrayList<String> imgsList = new ArrayList<String>();
                        for (int i = 0; i < imgArrays.length(); i++)
                        {
                            JSONObject imgObj = imgArrays.optJSONObject(i);
                            String url = imgObj.optString("url");
                            imgsList.add(url);
                        }
                        detail.setImageUrl(imgsList.get(0));
                        imgsList.remove(0);
                        detail.setImgs(imgsList);
                    }

                    JSONArray specifications = merchandise.optJSONArray("specifications");
                    if (specifications != null && specifications.length() > 0)
                    {
                        specificationList = new ArrayList<Specification>();
                        for (int i = 0; i < specifications.length(); i++)
                        {
                            JSONObject specificationObj = specifications.optJSONObject(i);
                            int specificationId = specificationObj.optInt("id");
                            String specificationTitle = specificationObj.optString("title");
                            int num = specificationObj.optInt("count");
                            double sizePrice = specificationObj.optDouble("price");
                            double sizeOldPrice = specificationObj.optDouble("cost_price");

                            Specification specification = new Specification(specificationId, specificationTitle, sizePrice, sizeOldPrice);
                            specification.num=num;
                            specificationList.add(specification);
                        }
                        detail.setSpecifications(specificationList);
                    }

                    JSONArray descriptions = merchandise.optJSONArray("descriptions");
                    if (descriptions != null && descriptions.length() > 0)
                    {
                        for (int i = 0; i < descriptions.length(); i++)
                        {
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
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers=new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
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
                }
                else if(i == UPDATE_LOGIN_ISOSALEBUY)
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
                }
                else
                {
                   ToolUtils.setToastLong(mContext,R.string.loading_fail_txt);
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

    /*
    * 普通特卖限购请求
    */
    public void FetchGoodsData(final int i)
    {
        String url = ZhaiDou.orderCheckGoodsUrl+detail.getId()+"/merchandise_specification?specification_id="+mSpecification.getId();
        ToolUtils.setLog(url);
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                if (jsonObject != null)
                {
                    JSONObject jsonObject1=jsonObject.optJSONObject("specification");
                    mSpecification.isBuy=isBuy = jsonObject1.optBoolean("buy_flag");
                    mSpecification.setNum(jsonObject1.optInt("count"));
                }
                if (i == UPDATE_LJ_ISBUY)
                {
                    handler.sendEmptyMessage(UPDATE_LJ_ISBUY);
                } else if (i == UPDATE_ADD_ISBUY)
                {
                    handler.sendEmptyMessage(UPDATE_ADD_ISBUY);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                ToolUtils.setToastLong(mContext,R.string.loading_fail_txt);
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
     * 立即购买清除掉购物车中的零元特卖
     *
     * @param cartItem
     */
    private void ljBuyOkDialog(final CartItem cartItem,String msg)
    {
        final Dialog dialog = new Dialog(mContext, R.style.custom_dialog);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_collect_hint, null);
        TextView tvMsg = (TextView) view.findViewById(R.id.tv_msg);
        tvMsg.setText(msg);
        TextView okTv = (TextView) view.findViewById(R.id.okTv);
        okTv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                CreatCartTools.deleteByData(creatCartDB, cartItem);
                Intent intent = new Intent(ZhaiDou.IntentRefreshCartGoodsTag);
                mContext.sendBroadcast(intent);
                dialog.dismiss();
                buyGoods();
            }
        });
        TextView cancelTv = (TextView) view.findViewById(R.id.cancelTv);
        cancelTv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
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
    private void addCartOkDialog(final CartItem cartItem)
    {
        final Dialog dialog = new Dialog(mContext, R.style.custom_dialog);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_collect_hint, null);
        TextView tvMsg = (TextView) view.findViewById(R.id.tv_msg);
        tvMsg.setText("购物车已经有一件零元特卖商品,继续添加将删除掉该商品，是否删除？");
        TextView okTv = (TextView) view.findViewById(R.id.okTv);
        okTv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                CreatCartTools.deleteByData(creatCartDB, cartItem);
                Intent intent = new Intent(ZhaiDou.IntentRefreshCartGoodsTag);
                mContext.sendBroadcast(intent);
                dialog.dismiss();
                addCartGoods();
            }
        });
        TextView cancelTv = (TextView) view.findViewById(R.id.cancelTv);
        cancelTv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog.show();

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
//                mCartCount.setText("" + num);
//                mCartCount.setVisibility(count==0?View.GONE:View.VISIBLE);
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

    private class MyTimer extends CountDownTimer
    {
        private MyTimer(long millisInFuture, long countDownInterval)
        {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l)
        {
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
        public void onFinish()
        {
            handler.sendEmptyMessage(UPDATE_UI_TIMER_FINISH);
        }
    }

    @Override
    public void onDestroyView()
    {
        if (mTimer != null)
        {
            mTimer.cancel();
            mTimer = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy()
    {
        mContext.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onResume()
    {
        long temp1 = System.currentTimeMillis() - currentTime;
        if (temptime - temp1 > 0&&isShowTimer)
        {
            if (mTimer != null)
            {
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
    public void onPause()
    {
        currentTime = System.currentTimeMillis();
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
