package com.zhaidou.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
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
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.ChildGridView;
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

public class GoodsDetailsFragment extends BaseFragment
{
    private static final String PAGE = "page";
    private static final String INDEX = "index";

    private String mPage;
    private int mIndex;
    private View mView;
    private int flags;
    private Context mContext;
    private Dialog mDialog;

    private int count = 0;
    private ImageView shareBtn;
    private String shareUrl=ZhaiDou.goodsDetailsShareUrl;
    private TextView backBtn, titleTv, mCartCount;
    private ImageView[] dots;
    private List<View> adPics = new ArrayList<View>();
    private ViewPager viewPager;
    private LinearLayout viewGroupe;//指示器容器
    private LinearLayout ljBtn;
    private LinearLayout addCartBtn;
    private View myCartBtn;

    private GridView mGridView;
    private RequestQueue mRequestQueue;
    private ViewPager mViewPager;
    private List<Fragment> fragments=new ArrayList<Fragment>();
    private GoodsDetailsChildFragment goodsDetailsChildFragment;
    private SaleServiceFragment saleServiceFragment;
    private GoodsChildFragmentAdapter goodsChildFragmentAdapter;

    private ArrayList<GoodInfo> goodInfos = new ArrayList<GoodInfo>();
    private int mSpecificationSelectPosition = -1;

    private TextView tv_comment, mCurrentPrice, mOldPrice, mDiscount, mTitle;



    private final int UPDATE_GOOD_DETAIL = 0;
    private final int UPDATE_COUNT_DOWN_TIME = 1;
    private final int UPDATE_UI_TIMER_FINISH = 2;
    private final int UPDATE_TIMER_START = 3;

    private CreatCartDB creatCartDB;
    private List<CartItem> items = new ArrayList<CartItem>();

    private int num;
    private ScrollView scrollView;
    private ImageView topBtn;
    private LinearLayout iconView,iconOSaleView;

    private LinearLayout loadingView,nullNetView,nullView;
    private TextView  reloadBtn,reloadNetBtn;

    private GoodDetail detail;
    private SpecificationAdapter specificationAdapter;
    private GoodsImageAdapter imageAdapter;
    private Specification mSpecification;//选中规格
    private List<Specification> specificationList;
    private ImageView mTipView;
    private FrameLayout animation_viewGroup;
    private RadioGroup radioGroup;
    //动画时间
    private int AnimationDuration = 1000;
    //正在执行的动画数量
    private int number = 0;
    //是否完成清理
    private boolean isClean = false;
    private MyTimer mTimer;
    private TextView mTimerView;

    private boolean isOSaleBuy;
    private boolean isClick;
    private int mClick=-1;

    private int userId;
    private String token;

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
            if (action.equals(ZhaiDou.IntentRefreshLoginTag))
            {
                initCartTips();
            }
            if (action.equals(ZhaiDou.IntentRefreshLoginExitTag))
            {
                initCartTips();
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
                    if (detail!=null)
                    loadingView.setVisibility(View.GONE);

                    detail = (GoodDetail) msg.obj;
                    setChildFargment(detail,goodInfos);

                    mCurrentPrice.setText("￥" + detail.getPrice() + "");
                    mOldPrice.setText("￥" + detail.getCost_price() + "");
                    tv_comment.setText(detail.getDesigner());
                    mTitle.setText(detail.getTitle());
                    setDiscount(detail.getPrice(), detail.getCost_price());

                    if (detail.getSpecifications() != null)
                        specificationAdapter.addAll(detail.getSpecifications());

                    initData(detail.getImgs());

                    String end_date = detail.getEnd_time();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                    try
                    {
                        long millionSeconds = sdf.parse(end_date).getTime();//毫秒
                        long hour = 3600 * 1000;
                        long minute = 60 * 1000;
                        millionSeconds = millionSeconds + hour * 23 + minute * 59 + 59 * 1000;
                        long temp = millionSeconds - System.currentTimeMillis();
                        if (temp==0)
                        {
                            ToolUtils.setToast(mContext,"特卖活动已结束");
                        }
                        mTimer = new MyTimer(temp, 1000);
                        mTimer.start();
                    } catch (Exception e)
                    {
                        Log.i("Exception e", e.getMessage());
                    }
                    break;
                case UPDATE_COUNT_DOWN_TIME:
                    CountTime time = (CountTime) msg.obj;
                    String timerFormat = getActivity().getResources().getString(R.string.timer);
                    String hourStr = String.format("%02d", time.getHour());
                    String minStr = String.format("%02d", time.getMinute());
                    String secondStr = String.format("%02d", time.getSecond());
                    String timer = String.format(timerFormat, time.getDay(), hourStr, minStr, secondStr);
                    mTimerView.setText(timer);
                    break;
                case UPDATE_UI_TIMER_FINISH:
                    mTimerView.setText("已结束");
                    break;
                case 5:
                    if (mDialog != null)
                        mDialog.dismiss();
                    if (isOSaleBuy)
                    {
                        Toast.makeText(mContext, "抱歉,您已经购买了零元特卖商品,今天已经不能购买", Toast.LENGTH_LONG).show();
                    } else
                    {
                        buyGoods();
                    }
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
            setImageBackground(i % adPics.size());
        }

        @Override
        public void onPageScrollStateChanged(int i)
        {

        }
    };

    /**
     * 商品信息和售后选择
     */
    private ViewPager.OnPageChangeListener onPageChange = new ViewPager.OnPageChangeListener()
    {
        @Override
        public void onPageScrolled(int i, float v, int i2)
        {
        }
        @Override
        public void onPageSelected(int i)
        {
            if (i==0)
            {
                radioGroup.check(R.id.infoRb);
            }
            if (i==1)
            {
                radioGroup.check(R.id.afterSaleRb);
            }
        }
        @Override
        public void onPageScrollStateChanged(int i)
        {
        }
    };

    /**
     * radiobutton选择改变事件
     */
    private RadioGroup.OnCheckedChangeListener onCheckedChangeListener=new RadioGroup.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i)
        {
            if (i==R.id.infoRb)
            {
                mViewPager.setCurrentItem(0);
            }
            if (i==R.id.afterSaleRb)
            {
                mViewPager.setCurrentItem(1);
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
                        ToolUtils.setToast(mContext, "抱歉,尚未登录");
                    }
                    break;
                case R.id.goodsLjBuyBtn:
                    if (checkLogin())
                    {
                        if (flags == 1)//判断零元特卖是否已经购买郭
                        {
                            FetchOSaleData(5);
                        } else
                        {
                            buyGoods();
                        }
                    } else
                    {
                        ToolUtils.setToast(mContext, "抱歉，尚未登录");
                    }
                    break;
                case R.id.goodsAddBuyBtn:
                    addGoods();
                    break;
                case R.id.goodsTop:
                    scrollView.scrollTo(0, 0);
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
            flags = getArguments().getInt("flags");
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
        shareUrl=shareUrl+mIndex;

        shareBtn=(ImageView)mView.findViewById(R.id.share_iv);
        shareBtn.setOnClickListener(onClickListener);
        if (flags==1)//零元特卖不能分享
        {
            shareBtn.setVisibility(View.GONE);
        }

        loadingView = (LinearLayout) mView.findViewById(R.id.loadingView);
        nullNetView= (LinearLayout) mView.findViewById(R.id.nullNetline);
        nullView= (LinearLayout) mView.findViewById(R.id.nullline);

        reloadBtn = (TextView) mView.findViewById(R.id.nullReload);
        reloadBtn.setOnClickListener(onClickListener);

        reloadNetBtn = (TextView) mView.findViewById(R.id.netReload);
        reloadNetBtn.setOnClickListener(onClickListener);

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
        addCartBtn = (LinearLayout) mView.findViewById(R.id.goodsAddBuyBtn);
        addCartBtn.setOnClickListener(onClickListener);

        RelativeLayout relativeLayout=(RelativeLayout)mView.findViewById(R.id.imageRl);
        relativeLayout.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenWidth*630/720));

        iconView=(LinearLayout)mView.findViewById(R.id.iconView);
        iconOSaleView=(LinearLayout)mView.findViewById(R.id.iconOSaleView);
        if (flags==1)
        {
            iconView.setVisibility(View.GONE);
            iconOSaleView.setVisibility(View.VISIBLE);
        }
        else
        {
            iconView.setVisibility(View.VISIBLE);
            iconOSaleView.setVisibility(View.GONE);
        }

        mView.findViewById(R.id.shopping_cart).setOnClickListener(onClickListener);
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

        scrollView = (ScrollView) mView.findViewById(R.id.sv_goods_detail);
        scrollView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE)
                {
                    int scrollY = view.getScrollY();
                    if (scrollY != 0)
                    {
                        topBtn.setVisibility(View.VISIBLE);
                    } else
                    {
                        topBtn.setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });
        topBtn = (ImageView) mView.findViewById(R.id.goodsTop);
        topBtn.setOnClickListener(onClickListener);

        mViewPager = (ViewPager) mView.findViewById(R.id.vp_goods_detail);

        radioGroup=(RadioGroup)mView.findViewById(R.id.goodsRG);
        radioGroup.setOnCheckedChangeListener(onCheckedChangeListener);

        specificationAdapter = new SpecificationAdapter(getActivity(), new ArrayList<Specification>(), mSpecificationSelectPosition);
        mGridView.setAdapter(specificationAdapter);

        specificationAdapter.setOnInViewClickListener(R.id.sizeTitleTv, new BaseListAdapter.onInternalClickListener()
        {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values)
            {

                if (((Specification)values).num>0 )
                {
                    if (mClick==position)
                    {
                        if (isClick==false)
                        {
                            mClick=position;
                            specificationAdapter.setCheckPosition(mSpecificationSelectPosition = position);
                            specificationAdapter.notifyDataSetChanged();
                            sizeEvent(position);
                            isClick=true;

                        } else
                        {
                            mClick=-1;
                            isClick=false;
                            specificationAdapter.setCheckPosition(mSpecificationSelectPosition=-1);
                            specificationAdapter.notifyDataSetChanged();
                            mSpecification=null;

                            mCurrentPrice.setText("￥" + detail.getPrice() + "");
                            mOldPrice.setText("￥" + detail.getCost_price() + "");
                            setDiscount(detail.getPrice(), detail.getCost_price());
                        }
                    }
                    else
                    {
                        mClick=position;
                        specificationAdapter.setCheckPosition(mSpecificationSelectPosition = position);
                        specificationAdapter.notifyDataSetChanged();
                        sizeEvent(position);
                        isClick=true;
                    }


                }
            }
        });

        creatCartDB = new CreatCartDB(mContext);
        initCartTips();

        initData();

    }

    /**
     * 数据加载
     */
    private void initData()
    {
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
        if (NetworkUtils.isNetworkAvailable(mContext))
        {
            FetchDetailData(mIndex);
            if (checkLogin())
            {
                if (flags==1)
                {
                    FetchOSaleData(0);
                }
            }
        }
        else
        {
            if (mDialog!=null)
            mDialog.dismiss();
            nullView.setVisibility(View.GONE);
            nullNetView.setVisibility(View.VISIBLE);
        }

    }

    public boolean checkLogin()
    {
        token = (String) SharedPreferencesUtil.getData(mContext, "token", "");
        userId = (Integer) SharedPreferencesUtil.getData(mContext, "userId", -1);
        boolean isLogin = !TextUtils.isEmpty(token) && userId > -1;
        ToolUtils.setLog(""+isLogin);
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
            mCurrentPrice.setText("￥" + mSpecification.price);
            mOldPrice.setText("￥" + mSpecification.oldPrice);
            setDiscount(mSpecification.price, mSpecification.oldPrice);
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
     * @param detail
     * @param goodInfos
     */
    private void setChildFargment(GoodDetail detail, ArrayList<GoodInfo> goodInfos)
    {
        fragments.removeAll(fragments);
        goodsDetailsChildFragment=GoodsDetailsChildFragment.newInstance(detail,goodInfos);
        saleServiceFragment=SaleServiceFragment.newInstance("","");
        fragments.add(goodsDetailsChildFragment);
        fragments.add(saleServiceFragment);
        goodsChildFragmentAdapter=new GoodsChildFragmentAdapter(getChildFragmentManager());
        mViewPager.setOnPageChangeListener(onPageChange);
        mViewPager.setAdapter(goodsChildFragmentAdapter);
        mViewPager.setCurrentItem(0);

    }

    /**
     * 分享
     */
    private void share()
    {
        ShareSDK.initSDK(mContext);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(mPage);
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl(shareUrl);
        // text是分享文本，所有平台都需要这个字段
        oks.setText(mPage+"   "+shareUrl);
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        if (detail!=null)
        {
            oks.setImageUrl(detail.getImgs().get(0));//确保SDcard下面存在此张图片
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
    private void initBroadcastReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ZhaiDou.IntentRefreshCartGoodsTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginExitTag);
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
            if (num > 0)
            {
                mCartCount.setVisibility(View.VISIBLE);
                mCartCount.setText("" + num);
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
        if (detail != null)
            if (mSpecification != null)
            {
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
                cartItem.isOSale = "true";

                ArrayList<CartItem> itemsCheck = new ArrayList<CartItem>();
                itemsCheck.add(cartItem);

                ShopOrderOkFragment shopOrderOkFragment = ShopOrderOkFragment.newInstance("", 0);
                Bundle bundle = new Bundle();
                bundle.putSerializable("goodsList", itemsCheck);
                shopOrderOkFragment.setArguments(bundle);
                ((MainActivity) getActivity()).navigationToFragment(shopOrderOkFragment);

            } else
            {
                scrollView.scrollTo(0, 0);
                Toast.makeText(mContext, "抱歉,先选择规格", Toast.LENGTH_SHORT).show();
            }
    }

    /**
     * 添加商品
     */
    private void addGoods()
    {
        if (checkLogin())
        {
            if (flags == 1)
            {
                if (mSpecification != null)
                {
                    if (isOSaleBuy)
                    {
                        ToolUtils.setToast(mContext, "抱歉,您已经购买了零元特卖商品不能添加该商品");
                    } else
                    {
                        for (int i = 0; i < items.size(); i++)
                        {
                            if (items.get(i).isOSale.equals("true"))
                            {
                                ToolUtils.setToast(mContext, "将替换掉原来的零元特卖商品");
                                CreatCartTools.deleteByData(creatCartDB, items.get(i));
                            }
                        }
                    }
                }
                else
                {
                    scrollView.scrollTo(0, 0);
                    Toast.makeText(mContext, "抱歉,先选择规格", Toast.LENGTH_SHORT).show();
                }
            }
            if (detail != null)
            {
                if (mSpecification != null)
                {
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
                    if (detail.getImgs() != null)
                    {
                        cartItem.imageUrl = detail.getImgs().get(0);
                    }
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
                    scrollView.scrollTo(0, 0);
                    Toast.makeText(mContext, "抱歉,先选择规格", Toast.LENGTH_SHORT).show();
                }

            }
        } else
        {
            ToolUtils.setToast(mContext, "抱歉，尚未登录");
        }

    }

    /**
     * 初始化数据
     */
    private void initData(List<String> urls)
    {
        viewGroupe.removeAllViews();
        if (CollectionUtils.isNotNull(urls))
        {
            ToolUtils.setLog(""+urls.size());
            if (urls.size()>4)
            {
                List<String> urlss=new ArrayList<String>();
                urlss.addAll(urls);
                urls.removeAll(urls);
                urls.add(urlss.get(0));
                urls.add(urlss.get(1));
                urls.add(urlss.get(2));
                urls.add(urlss.get(3));
            }
            ToolUtils.setLog(""+urls.size());

            for (String url : urls)
            {
                ImageView imageView = new ImageView(mContext);
                imageView.setImageResource(R.drawable.icon_loading_item);
                imageView.setScaleType(ImageView.ScaleType.CENTER);
                imageView.setBackgroundColor(Color.parseColor("#ffffff"));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                imageView.setLayoutParams(params);
                ToolUtils.setImageCacheUrl(url, imageView);
                adPics.add(imageView);
            }
            dots = new ImageView[adPics.size()];

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
            viewPager = (ViewPager) mView.findViewById(R.id.goods_adv_pager);
            viewPager.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenWidth*630/720));
            imageAdapter = new GoodsImageAdapter(mContext, adPics);
            viewPager.setAdapter(imageAdapter);
            viewPager.setOnPageChangeListener(onPageChangeListener);
            viewPager.setCurrentItem(0);
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
        String url = ZhaiDou.goodsDetailsUrlUrl + id;
        Log.i("url---------------------->", url);
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {

                if (mDialog != null)
                    mDialog.dismiss();

                if (jsonObject != null)
                {
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

                    JSONArray imgsArray = merchandise.optJSONArray("imgs");
                    if (imgsArray != null && imgsArray.length() > 0)
                    {
                        ArrayList<String> imgsList = new ArrayList<String>();
                        for (int i = 0; i < imgsArray.length(); i++)
                        {
                            JSONObject imgObj = imgsArray.optJSONObject(i);
                            String url = imgObj.optString("url");
                            imgsList.add(url);
                        }
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

                            Specification specification = new Specification(specificationId, specificationTitle, num, sizePrice, sizeOldPrice);
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
                Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
            }
        });
        mRequestQueue.add(request);
    }

    /**
     * 零元特卖是否购买请求
     */
    public void FetchOSaleData(final int i)
    {
        String url = ZhaiDou.orderCheckOSaleUrl;
        Log.i("url---------------------->", url);
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {

                if (jsonObject != null)
                {
                    isOSaleBuy = jsonObject.optBoolean("flag");
                }
                if (i==5)
                {
                    handler.sendEmptyMessage(5);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                Toast.makeText(getActivity(), "抱歉,请求失败", Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("SECAuthorization", "Yk77mfWaq_xYyeEibAxx");
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


    private class GoodsChildFragmentAdapter extends FragmentPagerAdapter
    {
        private GoodsChildFragmentAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public int getCount()
        {
            return fragments.size();
        }

        @Override
        public Fragment getItem(int i)
        {
            return fragments.get(i);
        }
    }

}
