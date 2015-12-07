package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshScrollView;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.dialog.CustomToastDialog;
import com.zhaidou.model.CartArrayItem;
import com.zhaidou.model.CartGoodsItem;
import com.zhaidou.sqlite.CreatCartDB;
import com.zhaidou.sqlite.CreatCartTools;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by roy on 15/7/24.
 */
public class ShopCartFragment extends BaseFragment
{
    private static final String PAGE = "page";
    private static final String INDEX = "index";

    private String mPage;
    private int mIndex;
    private View mView;
    private Context mContext;
    private Dialog mDialog;

    private TypeFaceTextView backBtn, titleTv;
    private Button okBuyBtn;
    private LinearLayout nullView;
    private RelativeLayout contentView;
    private TypeFaceTextView numTv;
    private TextView totalMoneyTv, saveMoneyTv;
    private CheckBox allCb;
    private PullToRefreshScrollView mScrollView;
    private LinearLayout cartGoodsLine;//添加商品view
    private LinearLayout loadingView;

    private RequestQueue mRequestQueue;

    private int userId;
    private List<CartGoodsItem> items = new ArrayList<CartGoodsItem>();
    private ArrayList<CartArrayItem> arrays = new ArrayList<CartArrayItem>();
    private ArrayList<CartArrayItem> arraysCheck = new ArrayList<CartArrayItem>();
    private List<CartGoodsItem> itemsCheck = new ArrayList<CartGoodsItem>();
    private List<CheckBox> boxs = new ArrayList<CheckBox>();
    private boolean isBuySuccess;
    private DialogUtils mDialogUtil;
    private int totalCount;
    private double totalMoney;
    private boolean isGoods;//是否存在商品
    private boolean isFrist=true;
    private int cartCount;//购物车商品数量

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(ZhaiDou.IntentRefreshCartGoodsCheckTag))
            {
                if (items.size() > 0)
                {
                    setGoodsCheckChange();
                } else
                {
                    nullView.setVisibility(View.VISIBLE);
                    contentView.setVisibility(View.GONE);
                }
            }
            if (action.equals(ZhaiDou.IntentRefreshCartGoodsTag))
            {
                isBuySuccess = true;
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
                case 1:
                    if (isGoods)
                    {
                        addCartGoods();
                        if (isFrist)
                        {
                            isFrist=false;
                            FetchCountData();
                        }

                    } else
                    {
                        loadingView.setVisibility(View.GONE);
                        nullView.setVisibility(View.VISIBLE);
                        contentView.setVisibility(View.GONE);
                    }
                    break;
                case 2:
                    break;
                case 3:
                    ((MainActivity) mContext).CartTip(cartCount);
                    break;
            }
        }
    };

    private PullToRefreshBase.OnRefreshListener2 onRefreshListener=new PullToRefreshBase.OnRefreshListener2()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView)
        {
            arrays.clear();
            refreshData();

        }
        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {

        }
    };

    /**
     * 全选选择事件处理
     */
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b)
        {
            if (b)
            {
                for (int i = 0; i < boxs.size(); i++)
                {
                    boxs.get(i).setChecked(true);
                }

            } else
            {
                for (int i = 0; i < boxs.size(); i++)
                {
                    boxs.get(i).setChecked(false);
                }
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
                    ((MainActivity) getActivity()).popToStack(ShopCartFragment.this);
                    break;

                case R.id.okBuyBtn:
                    if (itemsCheck.size() > 0)
                    {
                        commitCartOrder();
                    } else
                    {
                        ToolUtils.setToast(mContext, "抱歉,先选择商品");
                    }
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

        backBtn = (TypeFaceTextView) mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);
        if (mIndex == 1)
        {
            backBtn.setVisibility(View.GONE);
        }

        titleTv = (TypeFaceTextView) mView.findViewById(R.id.title_tv);
        titleTv.setText(R.string.shop_cart_text);

        okBuyBtn = (Button) mView.findViewById(R.id.okBuyBtn);
        okBuyBtn.setOnClickListener(onClickListener);

        nullView = (LinearLayout) mView.findViewById(R.id.cartNullLine);
        contentView = (RelativeLayout) mView.findViewById(R.id.cartContentLine);
        loadingView = (LinearLayout) mView.findViewById(R.id.loadingView);

        numTv = (TypeFaceTextView) mView.findViewById(R.id.cartNum);
        totalMoneyTv = (TextView) mView.findViewById(R.id.moneyTotalTv);
        saveMoneyTv = (TextView) mView.findViewById(R.id.moneySaveTv);
        allCb = (CheckBox) mView.findViewById(R.id.allCB);
        allCb.setOnCheckedChangeListener(onCheckedChangeListener);

        mScrollView=(PullToRefreshScrollView)mView.findViewById(R.id.scrollView);
        mScrollView.setOnRefreshListener(onRefreshListener);
        mScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        cartGoodsLine = (LinearLayout) mView.findViewById(R.id.cartGoodsLine);
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
        mContext.registerReceiver(broadcastReceiver, intentFilter);
    }

    public boolean checkLogin()
    {
        String token = (String) SharedPreferencesUtil.getData(mContext, "token", "");
        userId = (Integer) SharedPreferencesUtil.getData(mContext, "userId", -1);
        boolean isLogin = !TextUtils.isEmpty(token) && userId > -1;
        return isLogin;
    }


    /**
     * 刷新数据
     */
    public void refreshData()
    {
        arrays.clear();
        arraysCheck.clear();
        itemsCheck.clear();
        items.clear();
        if (allCb.isChecked())
        {
            allCb.setChecked(false);
        }
        checkLogin();
        FetchDetailData();
        FetchCountData();
    }

    /**
     * flags==1为删除后刷新数据
     */
    private void refreshItems(CartGoodsItem mCartGoodsItem, int flags)
    {
        for (int i = 0; i <arrays.size(); i++)
        {
            for (int j = 0; j <arrays.get(i).goodsItems.size(); j++)
            {
                if (arrays.get(i).goodsItems.get(j).sizeId.equals(mCartGoodsItem.sizeId))
                {
                    if (flags==1)
                    {
                        arrays.get(i).goodsItems.remove(j);
                    }
                    else
                    {
                        arrays.get(i).goodsItems.get(j).num=mCartGoodsItem.num;
                    }

                }
            }
        }
        for (int i = 0; i <itemsCheck.size(); i++)
        {
            if (itemsCheck.get(i).sizeId.equals(mCartGoodsItem.sizeId))
            {
                if (flags==1)
                {
                    itemsCheck.remove(i);
                }
                else
                {
                    itemsCheck.get(i).num=mCartGoodsItem.num;
                }
            }
        }
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
            items.clear();
            mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
            checkLogin();
            FetchDetailData();
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
        loadingView.setVisibility(View.GONE);
        nullView.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
        cartGoodsLine.removeAllViews();
        items.clear();
        boxs.removeAll(boxs);
        itemsCheck.removeAll(itemsCheck);
        items = arrays.get(0).goodsItems;
        for (int position = 0; position < items.size(); position++)
        {
            final int tag = position;
            final View childeView = LayoutInflater.from(mContext).inflate(R.layout.shop_cart_goods_item, null);
            LinearLayout lineView = (LinearLayout) childeView.findViewById(R.id.lineView);
            lineView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (items != null && items.size() > 0)
                    {
                        GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance(items.get(tag).name, items.get(tag).goodsId);
                        Bundle bundle = new Bundle();
                        if (items.get(tag).isPublish.equals("true"))
                        {
                            bundle.putInt("flags", 2);
                        }
                        ((MainActivity) getActivity()).navigationToFragment(goodsDetailsFragment);
                    }
                }
            });
            TypeFaceTextView itemName = (TypeFaceTextView) childeView.findViewById(R.id.cartItemNameTv);
            TypeFaceTextView itemSize = (TypeFaceTextView) childeView.findViewById(R.id.cartItemSizeTv);
            TextView itemflags = (TextView) childeView.findViewById(R.id.cartItemIsFlags);
            TextView itemCurrentPrice = (TextView) childeView.findViewById(R.id.cartItemCurrentPrice);
            TextView itemFormalPrice = (TextView) childeView.findViewById(R.id.cartItemFormalPrice);
            TypeFaceTextView itemSubBtn = (TypeFaceTextView) childeView.findViewById(R.id.cartItemSubBtn);
            TypeFaceTextView itemAddBtn = (TypeFaceTextView) childeView.findViewById(R.id.cartItemAddBtn);
            final TypeFaceTextView itemNum = (TypeFaceTextView) childeView.findViewById(R.id.cartItemNum);
            TypeFaceTextView itemLoseNum = (TypeFaceTextView) childeView.findViewById(R.id.cartItemLoseNum);
            ImageView itemImage = (ImageView) childeView.findViewById(R.id.cartImageItemTv);
            final CheckBox itemCheck = (CheckBox) childeView.findViewById(R.id.chatItemCB);
            itemCheck.setId(position);
            TextView isOver = (TextView) childeView.findViewById(R.id.cartItemIsOver);
            TextView islose = (TextView) childeView.findViewById(R.id.cartItemIsLose);
            TextView isDate = (TextView) childeView.findViewById(R.id.cartItemIsDate);
            ImageView itemDeleteBtn = (ImageView) childeView.findViewById(R.id.cartItemDelBtn);
            ImageView itemLine = (ImageView) childeView.findViewById(R.id.cartItemLine);
            LinearLayout cartNumView = (LinearLayout) childeView.findViewById(R.id.cartNumView);
            LinearLayout cartNumLoseView = (LinearLayout) childeView.findViewById(R.id.cartNumLoseView);

            if (items.size() > 1)
            {
                if (position == items.size() - 1)
                {
                    itemLine.setVisibility(View.GONE);
                }
                if (position == 0)
                {
                    itemLine.setVisibility(View.VISIBLE);
                }
            } else
            {
                itemLine.setVisibility(View.GONE);
            }

            final CartGoodsItem cartGoodsItem = items.get(position);


            //判断商品是否下架或者卖光处理
            if (cartGoodsItem.isOver.equals("true") | cartGoodsItem.isPublish.equals("true") | cartGoodsItem.isDate.equals("true"))
            {
                itemCheck.setVisibility(View.GONE);
                cartNumView.setVisibility(View.GONE);
                itemflags.setVisibility(View.VISIBLE);
                cartNumLoseView.setVisibility(View.VISIBLE);
                itemName.setTextColor(ColorStateList.valueOf(R.color.text_gary_color));
            } else
            {
                itemflags.setVisibility(View.GONE);
                cartNumView.setVisibility(View.VISIBLE);
                cartNumLoseView.setVisibility(View.GONE);
                itemCheck.setVisibility(View.VISIBLE);
                boxs.add(itemCheck);
                itemCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b)
                    {
                        if (b)
                        {
                            cartGoodsItem.isCheck = true;
                            itemsCheck.add(cartGoodsItem);
                        } else
                        {
                            cartGoodsItem.isCheck = false;
                            itemsCheck.remove(cartGoodsItem);
                        }
                        setGoodsCheckChange();
                    }
                });
            }

            if (cartGoodsItem.isOver.equals("true"))
            {
                isOver.setVisibility(View.VISIBLE);
                islose.setVisibility(View.GONE);
                isDate.setVisibility(View.GONE);
            }
            if (cartGoodsItem.isPublish.equals("true"))
            {
                isOver.setVisibility(View.GONE);
                islose.setVisibility(View.VISIBLE);
                isDate.setVisibility(View.GONE);
            }
            if (cartGoodsItem.isDate.equals("true"))
            {
                isOver.setVisibility(View.GONE);
                islose.setVisibility(View.GONE);
                isDate.setVisibility(View.VISIBLE);
            }

            if (cartGoodsItem.isOSale.equals("true"))
            {
                cartNumView.setVisibility(View.GONE);
                cartNumLoseView.setVisibility(View.VISIBLE);
            }

            itemName.setText(cartGoodsItem.name);
            itemSize.setText(cartGoodsItem.size);
            itemCurrentPrice.setText("￥" + ToolUtils.isIntPrice("" + cartGoodsItem.currentPrice));
            itemFormalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            itemFormalPrice.setText("￥" + ToolUtils.isIntPrice("" + cartGoodsItem.formalPrice));
            itemNum.setText("" + cartGoodsItem.num);
            itemLoseNum.setText("" + cartGoodsItem.num);
            ToolUtils.setImageCacheUrl(cartGoodsItem.imageUrl, itemImage, R.drawable.icon_loading_defalut);

            itemDeleteBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    mDialogUtil.showDialog(mContext.getResources().getString(R.string.dialog_hint_delete), new DialogUtils.PositiveListener()
                    {
                        @Override
                        public void onPositive()
                        {
                            FetchGoodsDeleteData(cartGoodsItem,childeView,itemCheck);
                        }
                    }, null);

                }
            });
            itemSubBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (cartGoodsItem.num - 1 > 0)
                    {
                        FetchEditDate(itemNum, cartGoodsItem.num - 1, cartGoodsItem);
                    }
                }
            });
            itemAddBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    FetchEditDate(itemNum, cartGoodsItem.num + 1, cartGoodsItem);
                }
            });
            cartGoodsLine.addView(childeView);
        }
        if (mDialog != null)
            mDialog.dismiss();
    }

    /**
     * 发送全局修改数量广播刷新
     */
    public void sendBroadCastEditAll()
    {
        //发送数量修改广播
        Intent intent = new Intent(ZhaiDou.IntentRefreshCartGoodsCheckTag);
        mContext.sendBroadcast(intent);
    }

    /**
     * 提交订单
     */
    private void commitCartOrder()
    {
        arraysCheck=arrays;
        for (int i = 0; i <arraysCheck.size() ; i++)
        {
            arraysCheck.get(i).goodsItems.clear();
            for (int j = 0; j < itemsCheck.size(); j++)
            {
                if (arraysCheck.get(i).storeId.equals(itemsCheck.get(j).storeId))
                {
                    arraysCheck.get(i).goodsItems.add(itemsCheck.get(j));
                }
            }
           if (arraysCheck.get(i).goodsItems.size()==0)
           {
               arraysCheck.remove(i);
           }
        }
        ShopOrderOkFragment shopOrderOkFragment = ShopOrderOkFragment.newInstance("", 0);
        Bundle bundle = new Bundle();
        bundle.putSerializable("goodsList",arraysCheck);
        shopOrderOkFragment.setArguments(bundle);
        ((MainActivity) getActivity()).navigationToFragment(shopOrderOkFragment);
    }

    /**
     * 设置选中商品价格数量变化
     */
    private void setGoodsCheckChange()
    {
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

        //刷新购物车总数
        cartCount=0;
        for (int i = 0; i <arrays.size(); i++)
        {
            for (int j = 0; j <arrays.get(i).goodsItems.size() ; j++)
            {
                cartCount=cartCount+arrays.get(i).goodsItems.get(j).num;
            }
        }
        ((MainActivity) mContext).CartTip(cartCount);

    }

    /**
     * 请求购物车列表数据
     */
    public void FetchDetailData()
    {
        String url = ZhaiDou.CartGoodsListUrl;
        ToolUtils.setLog("url:" + url);
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                mScrollView.onRefreshComplete();
                if (jsonObject != null)
                {
                    JSONObject dataObject = jsonObject.optJSONObject("data");
                    totalCount = dataObject.optInt("totalQuantity");
                    totalMoney = dataObject.optDouble("totalAmount");

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
                                    String userId = goodsObject.optString("userId");
                                    String goodsId = goodsObject.optString("productId");
                                    String goodsName = goodsObject.optString("productName");
                                    String goodsUrl = goodsObject.optString("productSKUPicUrl");
                                    String goodsSKU = goodsObject.optString("productSKUId");
                                    String specification = goodsObject.optString("productSKUSpecification");
                                    String isOSale=goodsObject.optString("businessType").equals("01")?"false":"true";
                                    double goodsPrice = goodsObject.optDouble("salePrice");
                                    double formalPrice = goodsObject.optDouble("markerPrice");
                                    int goodsCount = goodsObject.optInt("quantity");
                                    double goodsTotal = goodsObject.optDouble("subTotal");
                                    String isPublish = goodsObject.optString("productShelves").equals("1")?"false":"true";
                                    String isOver = goodsObject.optInt("stock")>0?"false":"true";
                                    CartGoodsItem goodsItem = new CartGoodsItem();
                                    goodsItem.userId = userId;
                                    goodsItem.storeId = storeId;
                                    goodsItem.goodsId = goodsId;
                                    goodsItem.name = goodsName;
                                    goodsItem.imageUrl = goodsUrl;
                                    goodsItem.size = specification;
                                    goodsItem.sizeId = goodsSKU;
                                    goodsItem.currentPrice = goodsPrice;
                                    goodsItem.formalPrice = formalPrice;
                                    goodsItem.num = goodsCount;
                                    goodsItem.totalMoney = goodsTotal;
                                    goodsItem.isOver = isOver;
                                    goodsItem.isPublish = isPublish;
                                    goodsItem.isDate = "false";
                                    goodsItem.isOSale=isOSale;

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
                            mHandler.sendEmptyMessage(1);
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
                mScrollView.onRefreshComplete();
                ToolUtils.setToast(mContext, R.string.loading_fail_txt);
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
                    mHandler.sendEmptyMessage(3);
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
     * 删除商品数据
     */
    public void FetchGoodsDeleteData(final CartGoodsItem cartGoodsItem,final View childeView,final CheckBox itemCheck)
    {
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
        String url = ZhaiDou.CartGoodsDeleteUrl + "[" + cartGoodsItem.sizeId + "]";
        ToolUtils.setLog("url:" + url);
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
                    if (status == 200)
                    {
                        items.remove(cartGoodsItem);
                        itemsCheck.remove(cartGoodsItem);
                        boxs.remove(itemCheck);
                        refreshItems(cartGoodsItem, 1);
                        cartGoodsLine.removeView(childeView);
                        //发送广播
                        sendBroadCastEditAll();
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
     * 修改数据请求
     *
     * @param itemNum
     * @param mCartGoodsItem
     */
    private void FetchEditDate(final TextView itemNum, final int num, final CartGoodsItem mCartGoodsItem)
    {
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
        String url = ZhaiDou.CartGoodsEditUrl +num+ "&productSKUId="+mCartGoodsItem.sizeId;
        ToolUtils.setLog("url:" + url);
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                if (mDialog!=null)
                mDialog.dismiss();
                int status = jsonObject.optInt("status");
                String message=jsonObject.optString("message");
                if (status == 200)
                {
                    itemNum.setText(""+num);
                    mCartGoodsItem.num=num;
                    refreshItems(mCartGoodsItem, 2);
                    sendBroadCastEditAll();
                }
                else
                {
                    ToolUtils.setToastLong(mContext, message);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                if (mDialog!=null)
                mDialog.dismiss();
                ToolUtils.setToast(mContext,R.string.loading_fail_txt);
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

    @Override
    public void onResume()
    {
        if (isBuySuccess)
        {
            isBuySuccess = false;
            for (int i = 0; i < boxs.size(); i++)
            {
                boxs.get(i).setChecked(false);
            }
            if (allCb.isChecked())
            {
                allCb.setChecked(false);
            }
        }
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
        mContext.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }


}
