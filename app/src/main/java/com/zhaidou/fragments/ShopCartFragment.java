package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.easemob.chat.EMChatManager;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshScrollView;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.LoginActivity;
import com.zhaidou.adapter.ShopCartAdapter;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.CartCountManager;
import com.zhaidou.base.CountManager;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.easeui.helpdesk.ui.ConversationListFragment;
import com.zhaidou.model.CartArrayItem;
import com.zhaidou.model.CartGoodsItem;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.Api;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by Zbea on 15/7/24.
 */
public class ShopCartFragment extends BaseFragment implements CartCountManager.OnCartCountListener,CountManager.onCommentChangeListener
{
    private static final String PAGE = "page";
    private static final String INDEX = "index";

    private String mPage;
    private int mIndex;
    private View mView;
    private Context mContext;
    private Dialog mDialog;

    private TextView backBtn, titleTv;
    private Button okBuyBtn;
    private LinearLayout nullView;
    private RelativeLayout contentView;
    private TypeFaceTextView numTv;
    private TextView totalMoneyTv, saveMoneyTv;
    private CheckBox allCb;
    private PullToRefreshScrollView mScrollView;
    private ListView listView;
    private LinearLayout loadingView;
    private TextView unreadMsg;
    private ImageView messageIv;

    private RequestQueue mRequestQueue;

    private int userId;
    private String token;
    private List<CartGoodsItem> itemsIsOver = new ArrayList<CartGoodsItem>();
    private List<CartGoodsItem> itemsIsDate = new ArrayList<CartGoodsItem>();
    private List<CartGoodsItem> itemsIsPublish = new ArrayList<CartGoodsItem>();
    private List<CartGoodsItem> items = new ArrayList<CartGoodsItem>();
    private ArrayList<CartArrayItem> arrays = new ArrayList<CartArrayItem>();
    private ArrayList<CartArrayItem> arraysCheck = new ArrayList<CartArrayItem>();
    private List<CartGoodsItem> itemsCheck = new ArrayList<CartGoodsItem>();
    private DialogUtils mDialogUtil;
    private boolean isGoods;//是否存在商品
    private int cartCount;//购物车商品数量
    private final static int UPDATE_CART_LIST=1;

    private ShopCartAdapter shopCartAdapter;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(ZhaiDou.IntentRefreshCartGoodsTag))
            {
                refreshData();
            }
            if (action.equals(ZhaiDou.IntentRefreshCartPaySuccessTag))
            {
                for (String key : shopCartAdapter.getIsSelected().keySet()) {
                    shopCartAdapter.getIsSelected().put(key,false);
                }
                refreshData();
            }

        }
    };

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case UPDATE_CART_LIST:
                    if (isGoods)
                    {
                        addCartGoods();
                    } else
                    {
                        loadingView.setVisibility(View.GONE);
                        nullView.setVisibility(View.VISIBLE);
                        contentView.setVisibility(View.GONE);
                    }
                    break;
                case 10:
                    setGoodsCheckChange();
                    break;
            }
        }
    };

    private PullToRefreshBase.OnRefreshListener2 onRefreshListener = new PullToRefreshBase.OnRefreshListener2()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView)
        {
            for (String key : shopCartAdapter.getIsSelected().keySet()) {
                shopCartAdapter.getIsSelected().put(key,false);
            }
            refreshData();
        }
        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {

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
                case R.id.okBuyBtn:
                    if (itemsCheck.size() > 0)
                    {
                        commitCartOrder();
                    } else
                    {
                        ToolUtils.setToast(mContext, "抱歉,先选择商品");
                    }
                    break;
                case R.id.iv_message:
                    Integer userId= (Integer) SharedPreferencesUtil.getData(mContext,"userId",-1);
                    if (userId==-1){
                        Intent intent =new Intent(mContext, LoginActivity.class);
                        startActivity(intent);
                        return;
                    }
                    ConversationListFragment conversationListFragment=new ConversationListFragment();
                    ((BaseActivity) mContext).navigationToFragment(conversationListFragment);
                    break;
            }
        }
    };

    public static ShopCartFragment newInstance(String page, int index)
    {
        ShopCartFragment fragment = new ShopCartFragment();
        Bundle args = new Bundle();
        args.putString(PAGE, page);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    public ShopCartFragment()
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        if (mView == null)
        {
            mContext = getActivity();
            initBroadcastReceiver();
            mView = inflater.inflate(R.layout.shop_cart_page, container, false);
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

    /**
     * 初始化数据
     */
    private void initView()
    {
        mRequestQueue = Volley.newRequestQueue(mContext);
        mDialogUtil = new DialogUtils(mContext);

        backBtn=(TextView) mView.findViewById(R.id.ll_back);
        if (mIndex == 1)
        {
            backBtn.setVisibility(View.GONE);
        }

        titleTv = (TypeFaceTextView) mView.findViewById(R.id.title_tv);
        titleTv.setText(R.string.shop_cart_text);

        unreadMsg = (TextView) mView.findViewById(R.id.unreadMsg);
        messageIv = (ImageView) mView.findViewById(R.id.iv_message);
        messageIv.setOnClickListener(onClickListener);

        nullView = (LinearLayout) mView.findViewById(R.id.cartNullLine);
        contentView = (RelativeLayout) mView.findViewById(R.id.cartContentLine);
        loadingView = (LinearLayout) mView.findViewById(R.id.loadingView);

        okBuyBtn = (Button) mView.findViewById(R.id.okBuyBtn);
        okBuyBtn.setOnClickListener(onClickListener);

        numTv = (TypeFaceTextView) mView.findViewById(R.id.cartNum);
        totalMoneyTv = (TextView) mView.findViewById(R.id.moneyTotalTv);
        saveMoneyTv = (TextView) mView.findViewById(R.id.moneySaveTv);
        allCb = (CheckBox) mView.findViewById(R.id.allCB);
        allCb.setId(0);
        allCb.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (allCb.isChecked())
                {
                    for (int i = 0; i <getUserItems().size() ; i++)
                    {
                        String sizeId=getUserItems().get(i).sizeId;
                        shopCartAdapter.getIsSelected().put(sizeId,true);
                    }
                    allCb.setChecked(true);
                }
                else
                {
                    for (int i = 0; i <getUserItems().size() ; i++)
                    {
                        String sizeId=getUserItems().get(i).sizeId;
                        shopCartAdapter.getIsSelected().put(sizeId,false);
                    }
                    allCb.setChecked(false);
                }
                shopCartAdapter.notifyDataSetChanged();
                shopCartAdapter.setRefreshCheckView();
            }
        });

        mScrollView = (PullToRefreshScrollView) mView.findViewById(R.id.scrollView);
        mScrollView.setOnRefreshListener(onRefreshListener);
        mScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        listView=(ListView)mView.findViewById(R.id.cartListView);
        shopCartAdapter=new ShopCartAdapter(mContext,items,mHandler);
        listView.setAdapter(shopCartAdapter);
        shopCartAdapter.setOnInViewClickListener(R.id.cartContentLine,new BaseListAdapter.onInternalClickListener()
        {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values)
            {
                CartGoodsItem cartGoodsItem = items.get(position);
                GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance(cartGoodsItem.name, cartGoodsItem.goodsId);
                ((BaseActivity) getActivity()).navigationToFragmentWithAnim(goodsDetailsFragment);
            }
        });
        shopCartAdapter.setOnInViewClickListener(R.id.cartItemSubBtn,new BaseListAdapter.onInternalClickListener()
        {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values)
            {
                CartGoodsItem cartGoodsItem = items.get(position);
                if (cartGoodsItem.num - 1 > 0)
                {
                    FetchEditDate(cartGoodsItem.num - 1, cartGoodsItem, 2);
                } else
                {
                    ToolUtils.setToast(mContext, "抱歉,当前数量不能再减");
                }
            }
        });
        shopCartAdapter.setOnInViewClickListener(R.id.cartItemAddBtn,new BaseListAdapter.onInternalClickListener()
        {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values)
            {
                CartGoodsItem cartGoodsItem = items.get(position);
                FetchEditDate (cartGoodsItem.num + 1, cartGoodsItem, 1);
            }
        });

        shopCartAdapter.setOnInViewClickListener(R.id.cartItemDelBtn,new BaseListAdapter.onInternalClickListener()
        {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values)
            {
               final CartGoodsItem cartGoodsItem = items.get(position);
                mDialogUtil.showDialog(mContext.getResources().getString(R.string.dialog_hint_delete), new DialogUtils.PositiveListener()
                {
                    @Override
                    public void onPositive()
                    {
                        FetchGoodsDeleteData(cartGoodsItem);
                    }
                }, null);
            }
        });


        Integer userId= (Integer) SharedPreferencesUtil.getData(mContext,"userId",-1);
        if (userId!=-1)
            Api.getUnReadComment(userId,null,null);
        CountManager.getInstance().setOnCommentChangeListener(this);
        CartCountManager.newInstance().setOnCartCountListener(this);
        initData();

    }

    /**
     * 注册广播
     */
    private void initBroadcastReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshCartGoodsTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshCartPaySuccessTag);
        mContext.registerReceiver(broadcastReceiver, intentFilter);
    }

    public boolean checkLogin()
    {
        token = (String) SharedPreferencesUtil.getData(mContext, "token", "");
        userId = (Integer) SharedPreferencesUtil.getData(mContext, "userId", -1);
        boolean isLogin = !TextUtils.isEmpty(token) && userId > -1;
        return isLogin;
    }


    /**
     * 刷新数据
     */
    public void refreshData()
    {
        isGoods = false;
        arrays.clear();
        arraysCheck.clear();
        itemsCheck.clear();
        checkLogin();
        FetchDetailData();
        FetchCountData();
    }


    /**
     * 初始化
     */
    private void initData()
    {
        if (NetworkUtils.isNetworkAvailable(mContext))
        {
            arrays.clear();
            arraysCheck.clear();
            itemsCheck.clear();
            mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
            checkLogin();
            FetchDetailData();
            FetchCountData();
        } else
        {
            ToolUtils.setToast(mContext, R.string.net_fail_txt);
        }
    }


    /**
     * 添加商品信息
     */
    private void addCartGoods()
    {
        items.clear();
        itemsIsDate.clear();
        itemsIsOver.clear();
        itemsIsPublish.clear();
        if (arrays.size() > 0)
            for (int i = 0; i < arrays.size(); i++)
            {
                List<CartGoodsItem> cartGoodsItems=arrays.get(i).goodsItems;
                for (int j = 0; j < cartGoodsItems.size(); j++)
                {
                    if (cartGoodsItems.get(j).isPublish.equalsIgnoreCase("true"))
                    {
                        itemsIsPublish.add(cartGoodsItems.get(j));
                    }
                    else if (cartGoodsItems.get(j).isOver.equalsIgnoreCase("true"))
                    {
                        itemsIsOver.add(cartGoodsItems.get(j));
                    }
                    else if (cartGoodsItems.get(j).isDate.equalsIgnoreCase("true"))
                    {
                        itemsIsDate.add(cartGoodsItems.get(j));
                    }
                    else
                    {
                        items.add(cartGoodsItems.get(j));
                    }
                }
            }
        sortGoods(items);
        sortGoods(itemsIsOver);
        sortGoods(itemsIsPublish);
        sortGoods(itemsIsDate);
        items.addAll(itemsIsOver);
        items.addAll(itemsIsPublish);
        items.addAll(itemsIsDate);

        shopCartAdapter.setList(items);
        shopCartAdapter.setRefreshCheckView();

        loadingView.setVisibility(View.GONE);
        nullView.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
        if (mDialog != null)
            mDialog.dismiss();
    }

    /**
     * 按时间降序
     * @param goods
     * @return
     */
    private List<CartGoodsItem> sortGoods(List<CartGoodsItem> goods)
    {
        Collections.sort(goods,new Comparator<CartGoodsItem>()
        {
            @Override
            public int compare(CartGoodsItem lhs, CartGoodsItem rhs)
            {
                if (lhs.createTime<rhs.createTime)
                {
                    return 1;
                }
                if (lhs.createTime==rhs.createTime)
                {
                    return 0;
                }
                return -1;
            }
        });
        return goods;
    }

    /**
     * 提交订单
     */
    private void commitCartOrder()
    {
        itemsCheck=shopCartAdapter.getItemChecks();
        arraysCheck = arrays;
        ArrayList<CartArrayItem> deleteArrays = new ArrayList<CartArrayItem>();
        for (int i = 0; i < arraysCheck.size(); i++)
        {
            CartArrayItem cartArrayItem = arraysCheck.get(i);
            cartArrayItem.goodsItems.clear();
            for (int j = 0; j < itemsCheck.size(); j++)
            {
                if (cartArrayItem.storeId.equalsIgnoreCase(itemsCheck.get(j).storeId))
                {
                    cartArrayItem.goodsItems.add(itemsCheck.get(j));
                }
            }
            if (cartArrayItem.goodsItems.size() == 0)
            {
                deleteArrays.add(cartArrayItem);
            }
        }
        arraysCheck.removeAll(deleteArrays);

        ShopOrderOkFragment shopOrderOkFragment = ShopOrderOkFragment.newInstance("", 0);
        Bundle bundle = new Bundle();
        bundle.putInt("flags", 2);
        bundle.putSerializable("goodsList", arraysCheck);
        shopOrderOkFragment.setArguments(bundle);
        ((BaseActivity) getActivity()).navigationToFragment(shopOrderOkFragment);
    }

    /**
     * 判断是否可以操作商品
     * @param cartGoodsItem
     * @return
     */
    private boolean isUser(CartGoodsItem cartGoodsItem)
    {
        if (!cartGoodsItem.isOver.equals("true")&&!cartGoodsItem.isPublish.equals("true")&&!cartGoodsItem.isDate.equals("true"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * 得到可用商品
     * @return
     */
    private List<CartGoodsItem> getUserItems()
    {
        List<CartGoodsItem> userItems=new ArrayList<CartGoodsItem>();
        for (int i = 0; i <items.size() ; i++)
        {
            CartGoodsItem cartGoodsItem=items.get(i);
            if (isUser(cartGoodsItem))
            {
                userItems.add(cartGoodsItem);
            }
        }
        return userItems;
    }

    /**
     * 设置选中商品价格数量变化
     */
    private void setGoodsCheckChange()
    {
        itemsCheck=shopCartAdapter.getItemChecks();
        if (getUserItems().size()!=0)
            allCb.setChecked(itemsCheck.size()==getUserItems().size()?true:false);

        int num = 0;
        double totalMoney = 0;
        double saveMoney = 0;
        for (int i = 0; i < itemsCheck.size(); i++)
        {
            CartGoodsItem cartGoodsItem = itemsCheck.get(i);
            num = num + cartGoodsItem.num;
            totalMoney = totalMoney + cartGoodsItem.num * cartGoodsItem.currentPrice;
            saveMoney = saveMoney + ((cartGoodsItem.formalPrice - cartGoodsItem.currentPrice) * cartGoodsItem.num);
        }
        numTv.setText("" + num);
        DecimalFormat df = new DecimalFormat("###.00");
        saveMoney = Double.parseDouble(df.format(saveMoney));
        totalMoney = Double.parseDouble(df.format(totalMoney));

        totalMoneyTv.setText("  ￥" + ToolUtils.isIntPrice("" + totalMoney));
        saveMoneyTv.setText("  ￥" + ToolUtils.isIntPrice("" + saveMoney));

    }

    /**
     * 请求购物车列表数据
     */
    public void FetchDetailData()
    {
        String url = ZhaiDou.CartGoodsListUrl + userId;
        ZhaiDouRequest request=new ZhaiDouRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                mScrollView.onRefreshComplete();
                arrays.clear();
                items.clear();
                if (jsonObject != null)
                {
                    JSONObject dataObject = jsonObject.optJSONObject("data");
                    int totalCount = dataObject.optInt("totalQuantity");
                    double totalMoney = dataObject.optDouble("totalAmount");

                    JSONArray storeArray = dataObject.optJSONArray("productStoreArray");
                    if (storeArray != null && storeArray.length() > 0)
                    {
                        for (int i = 0; i < storeArray.length(); i++)
                        {
                            JSONObject storeObject = storeArray.optJSONObject(i);
                            String storeId = storeObject.optString("storeId");
                            String storeName = storeObject.optString("storeName");
                            int storeCount = storeObject.optInt("subQuantity");
                            double storeMoney = storeObject.optDouble("subAmount");
                            JSONArray goodsArray = storeObject.optJSONArray("productSKUArray");
                            List<CartGoodsItem> goodsItems = new ArrayList<CartGoodsItem>();
                            if (goodsArray != null && goodsArray.length() > 0)
                                for (int j = 0; j < goodsArray.length(); j++)
                                {
                                    JSONObject goodsObject = goodsArray.optJSONObject(j);
                                    String useId = goodsObject.optString("userId");
                                    int createTime = goodsObject.optInt("createTime");
                                    String goodsId = goodsObject.optString("productId");
                                    String goodsName = goodsObject.optString("productName");
                                    String goodsUrl = goodsObject.optString("productSKUPicUrl");
                                    String goodsSKU = goodsObject.optString("productSKUId");
                                    String specification = goodsObject.optString("productSKUSpecification");
                                    String isOSale = goodsObject.optString("businessType").equals("01") ? "false" : "true";
                                    double goodsPrice = goodsObject.optDouble("salePrice");
                                    double formalPrice = goodsObject.optDouble("markerPrice");
                                    int goodsCount = goodsObject.optInt("quantity");
                                    int count = goodsObject.optInt("stock");
                                    double goodsTotal = goodsObject.optDouble("subTotal");
                                    String isPublish = goodsObject.optString("productShelves").equals("1") ? "false" : "true";
                                    String isOver = count > 0 ? "false" : "true";
                                    CartGoodsItem goodsItem = new CartGoodsItem();
                                    goodsItem.userId = useId;
                                    goodsItem.storeId = storeId;
                                    goodsItem.goodsId = goodsId;
                                    goodsItem.createTime = createTime;
                                    goodsItem.name = goodsName;
                                    goodsItem.imageUrl = goodsUrl;
                                    goodsItem.size = specification;
                                    goodsItem.sizeId = goodsSKU;
                                    goodsItem.currentPrice = goodsPrice;
                                    goodsItem.formalPrice = formalPrice;
                                    goodsItem.num = goodsCount;
                                    goodsItem.count = count;
                                    goodsItem.totalMoney = goodsTotal;
                                    goodsItem.isOver = isOver;
                                    goodsItem.isPublish = isPublish;
                                    goodsItem.isDate = "false";
                                    goodsItem.isOSale = isOSale;

                                    goodsItems.add(goodsItem);
                                    if (goodsItems.size() > 0)
                                    {
                                        isGoods = true;
                                    }
                                }

                            CartArrayItem storeItem = new CartArrayItem();
                            storeItem.storeId = storeId;
                            storeItem.storeName = storeName;
                            storeItem.storeCount = storeCount;
                            storeItem.storeMoney = storeMoney;
                            storeItem.goodsItems = goodsItems;
                            arrays.add(storeItem);

                        }

                    }
                    mHandler.sendEmptyMessage(UPDATE_CART_LIST);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                mScrollView.onRefreshComplete();
                ToolUtils.setToast(mContext, R.string.loading_fail_txt);
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
                    ((MainActivity) mContext).CartTip(cartCount);
                    CartCountManager.newInstance().notify(cartCount);
                }
            }
        }, null);
    }

    /**
     * 删除商品数据
     */
    public void FetchGoodsDeleteData(final CartGoodsItem cartGoodsItem)
    {
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
        String url = ZhaiDou.CartGoodsDeleteUrl + userId + "&productSKUId=" + "[" + cartGoodsItem.sizeId + "]";
        ZhaiDouRequest request=new ZhaiDouRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                if (jsonObject != null)
                {
                    int status = jsonObject.optInt("status");
                    if (status == 200)
                    {
                        //刷新购物车数量
                        cartCount = cartCount - cartGoodsItem.num;
                        CartCountManager.newInstance().notify(cartCount);

                        items.remove(cartGoodsItem);

                        shopCartAdapter.setIsSelected(cartGoodsItem);
                        shopCartAdapter.notifyDataSetChanged();
                        shopCartAdapter.setRefreshCheckView();
                        if (items.size()==0)
                        {
                            loadingView.setVisibility(View.GONE);
                            nullView.setVisibility(View.VISIBLE);
                            contentView.setVisibility(View.GONE);
                        }
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

    /**
     * 修改数据请求
     *
     * @param mCartGoodsItem
     */
    private void FetchEditDate(final int num, final CartGoodsItem mCartGoodsItem, final int type)
    {
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
        String url = ZhaiDou.CartGoodsEditUrl + userId + "&quantity=" + num + "&productSKUId=" + mCartGoodsItem.sizeId;
        ToolUtils.setLog("url:" + url);
        ZhaiDouRequest request=new ZhaiDouRequest(mContext,url,new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                int status = jsonObject.optInt("status");
                String message = jsonObject.optString("message");
                if (status == 200)
                {
                    mCartGoodsItem.num = num;
                    if (type == 1)
                    {
                        cartCount = cartCount + 1;
                    } else
                    {
                        cartCount = cartCount - 1;
                    }
                    CartCountManager.newInstance().notify(cartCount);
                    shopCartAdapter.notifyDataSetChanged();
                    shopCartAdapter.setRefreshCheckView();
                } else
                {
                    ToolUtils.setToastLong(mContext, message);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                ToolUtils.setToast(mContext, R.string.loading_fail_txt);
            }
        });
        mRequestQueue.add(request);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.shop_cart_text));
    }

    public void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.shop_cart_text));
    }

    @Override
    public void onDestroy()
    {
        if (broadcastReceiver!=null)
            mContext.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        super.onHiddenChanged(hidden);
        if (!hidden)
        {
            if (arrays == null | arrays.size() < 1)
            {
                refreshData();
            }
        }
    }

    /**
     * 刷新购物车商品数量
     * @param count
     */
    @Override
    public void onChange(int count)
    {
        if (cartCount!=count)
        {
            refreshData();
        }
        cartCount=count;
        ((MainActivity) mContext).CartTip(cartCount);
    }

    @Override
    public void onChange() {
        Integer userId= (Integer) SharedPreferencesUtil.getData(mContext,"userId",-1);
        int unreadMsgsCount = EMChatManager.getInstance().getUnreadMsgsCount();
        Integer UnReadComment= (Integer) SharedPreferencesUtil.getData(ZDApplication.getInstance(),"UnReadComment",0);
        unreadMsg.setVisibility((unreadMsgsCount + UnReadComment) > 0&&userId!=-1? View.VISIBLE : View.GONE);
        unreadMsg.setText((unreadMsgsCount+UnReadComment) > 99 ? "99+" : (unreadMsgsCount+UnReadComment) + "");
    }
}
