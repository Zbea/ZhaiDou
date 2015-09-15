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

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pulltorefresh.PullToRefreshBase;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.dialog.CustomShopCartDeleteDialog;
import com.zhaidou.dialog.CustomToastDialog;
import com.zhaidou.model.CartItem;
import com.zhaidou.sqlite.CreatCartDB;
import com.zhaidou.sqlite.CreatCartTools;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


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
    private TypeFaceTextView ggBtn;
    private RelativeLayout contentView;
    private TypeFaceTextView numTv;
    private TextView totalMoneyTv, saveMoneyTv;
    private CheckBox allCb;
    private LinearLayout cartGoodsLine;//添加商品view
    private TextView textNumView;
    private int tags;
    private LinearLayout loadingView;

    private RequestQueue mRequestQueue;

    private int userId;
    private int count;//单个商品的数量
    private String Str_publish;
    private CreatCartDB creatCartDB;
    private List<CartItem> items = new ArrayList<CartItem>();
    private List<CartItem> itemsServer = new ArrayList<CartItem>();
    private ArrayList<CartItem> itemsCheck = new ArrayList<CartItem>();
    private List<CheckBox> boxs = new ArrayList<CheckBox>();
    private List<View> views=new ArrayList<View>();
    private CartItem mCartItem;
    private boolean isBuySuccess;


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(ZhaiDou.IntentRefreshCartGoodsTag))
            {
                items.removeAll(items);
                items = CreatCartTools.selectByAll(creatCartDB, userId);
                if (items.size() > 0)
                {
                    setGoodsCheckChange();
                } else
                {
                    nullView.setVisibility(View.VISIBLE);
                    contentView.setVisibility(View.GONE);
                }
            }
            if (action.equals(ZhaiDou.IntentRefreshCartGoodsCheckTag))
            {
                isBuySuccess=true;
                items = CreatCartTools.selectByAll(creatCartDB, userId);
                ToolUtils.setLog("开始刷新购物车");
                if (items.size() > 0)
                {
                    addCartGoods();

                } else
                {
                    mDialog.dismiss();
                    nullView.setVisibility(View.VISIBLE);
                    contentView.setVisibility(View.GONE);
                }
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
                    loadingView.setVisibility(View.GONE);
                    initData();
                    break;
                case 2:
                    mDialog.dismiss();
                    if (Str_publish.equals("true"))
                    {
                        Toast.makeText(mContext,"抱歉,该商品已经下架,将刷新购物车",Toast.LENGTH_LONG).show();
                        mCartItem.isPublish=Str_publish;
                        CreatCartTools.editIsLoseByData(creatCartDB,mCartItem);
                        addCartGoods();
                    }
                    else
                    {
                        if (tags == 1)
                        {
                            if (count > mCartItem.num)
                            {
                                mCartItem.num = mCartItem.num + 1;
                                CreatCartTools.editNumByData(creatCartDB, mCartItem);
                                sendBroadCastEditAll();
                                textNumView.setText("" + mCartItem.num);
                            } else
                            {
                                CustomToastDialog.setToastDialog(mContext, "库存不足,商品只剩"+count+"件");
                            }

                        } else
                        {
                            if (count<1)
                            {
                                mCartItem.num = 1;
                            }
                            if (count <mCartItem.num&&count>0)
                            {
                                CustomToastDialog.setToastDialog(mContext, "抱歉,该商品只剩"+count+"件,及时更新购物车");
                                mCartItem.num = count;
                            }
                            else
                            {
                                mCartItem.num = mCartItem.num - 1;
                            }
                            CreatCartTools.editNumByData(creatCartDB, mCartItem);
                            sendBroadCastEditAll();
                            textNumView.setText("" + mCartItem.num);

                        }
                    }
                    break;
            }
        }
    };

    /**
     * 下拉刷新
     */
    private PullToRefreshBase.OnRefreshListener2 refreshListener = new PullToRefreshBase.OnRefreshListener2()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView)
        {

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
                case R.id.cartGgTv:
                    ShopSpecialFragment shopSpecialFragment = ShopSpecialFragment.newInstance("", 0);
                    ((MainActivity) getActivity()).navigationToFragment(shopSpecialFragment);
                    break;
                case R.id.okBuyBtn:
                    if (itemsCheck.size() > 0)
                    {
                        ShopOrderOkFragment shopOrderOkFragment = ShopOrderOkFragment.newInstance("", 0);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("goodsList", itemsCheck);
                        shopOrderOkFragment.setArguments(bundle);
                        ((MainActivity) getActivity()).navigationToFragment(shopOrderOkFragment);
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
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");

        backBtn = (TypeFaceTextView) mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);
        titleTv = (TypeFaceTextView) mView.findViewById(R.id.title_tv);
        titleTv.setText(R.string.shop_cart_text);

        okBuyBtn = (Button) mView.findViewById(R.id.okBuyBtn);
        okBuyBtn.setOnClickListener(onClickListener);

        nullView = (LinearLayout) mView.findViewById(R.id.cartNullLine);
        ggBtn = (TypeFaceTextView) mView.findViewById(R.id.cartGgTv);
        ggBtn.setOnClickListener(onClickListener);
        contentView = (RelativeLayout) mView.findViewById(R.id.cartContentLine);
        loadingView=(LinearLayout)mView.findViewById(R.id.loadingView);

        numTv = (TypeFaceTextView) mView.findViewById(R.id.cartNum);
        totalMoneyTv = (TextView) mView.findViewById(R.id.moneyTotalTv);
        saveMoneyTv = (TextView) mView.findViewById(R.id.moneySaveTv);
        allCb = (CheckBox) mView.findViewById(R.id.allCB);
        allCb.setOnCheckedChangeListener(onCheckedChangeListener);

        cartGoodsLine = (LinearLayout) mView.findViewById(R.id.cartGoodsLine);
        creatCartDB = new CreatCartDB(mContext);
        checkLogin();
        items = CreatCartTools.selectByAll(creatCartDB, userId);

        if (items.size() > 0)
        {
            FetchDetailData();

        } else
        {
            mDialog.dismiss();
            nullView.setVisibility(View.VISIBLE);
            contentView.setVisibility(View.GONE);
            loadingView.setVisibility(View.GONE);
        }

    }

    /**
     * 注册广播
     */
    private void initBroadcastReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ZhaiDou.IntentRefreshCartGoodsTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshCartGoodsCheckTag);
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
     * 本地数据和服务器数据进行对比比较
     */
    private void initData()
    {
        nullView.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
        items = CreatCartTools.selectByAll(creatCartDB, userId);
        if (itemsServer.size() > 0)
        {
            for (int i = 0; i < itemsServer.size(); i++)
            {
                CartItem itemServer = itemsServer.get(i);
                for (int j = 0; j < items.size(); j++)
                {
                    CartItem itemLocal = items.get(j);
                    if (itemServer.sizeId == itemLocal.sizeId)
                    {
                        if (!itemServer.isPublish.equals(itemLocal.isPublish))
                        {
                            itemLocal.isPublish=itemServer.isPublish;
                            ToolUtils.setLog("修改是否下架");
                            ToolUtils.setLog(itemServer.isPublish);
                            CreatCartTools.editIsLoseByData(creatCartDB, itemServer);//修改本地数据
                        }
                        if (!itemServer.isOver.equals(itemLocal.isOver))
                        {
                            itemLocal.isOver=itemServer.isOver;
                            ToolUtils.setLog("修改是否卖光");
                            ToolUtils.setLog(itemServer.isPublish);
                            CreatCartTools.editIsOverByData(creatCartDB, itemServer);//修改本地数据
                        }
                        if (!itemServer.isDate.equals(itemLocal.isDate))
                        {
                            items.get(j).isDate=itemServer.isDate;
                            ToolUtils.setLog("修改是否已过期");

                        }
                    }
                }
            }
        }
        sendBroadCastEditAll();
        addCartGoods();

    }

    /**
     * 添加商品信息
     */
    private void addCartGoods()
    {
        cartGoodsLine.removeAllViews();
        boxs.removeAll(boxs);
        itemsCheck.removeAll(itemsCheck);
        for (int position = 0; position < items.size(); position++)
        {
            final int tag=position;
            final View childeView = LayoutInflater.from(mContext).inflate(R.layout.shop_cart_goods_item, null);
            LinearLayout lineView=(LinearLayout)childeView.findViewById(R.id.lineView);
            lineView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(items!=null&&items.size()>0)
                    {
                        GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance(items.get(tag).name, items.get(tag).id);
                        Bundle bundle = new Bundle();
                        if (items.get(tag).isOSale.equals("true"))
                        {
                            bundle.putInt("flags", 1);
                        }
                        if (items.get(tag).isPublish.equals("true"))
                        {
                            bundle.putInt("flags", 2);
                        }
                        bundle.putString("page",items.get(tag).name);
                        bundle.putInt("index", items.get(tag).id);
                        goodsDetailsFragment.setArguments(bundle);
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

            final CartItem cartItem = items.get(position);

            //判断商品是否下架或者卖光处理
            if (cartItem.isOver.equals("true") | cartItem.isPublish.equals("true")| cartItem.isDate.equals("true"))
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
                            ToolUtils.setLog("itemCheck"+""+tag+":"+b);
                            cartItem.isCheck = true;
                            itemsCheck.add(cartItem);
                        } else
                        {
                            cartItem.isCheck = false;
                            itemsCheck.remove(cartItem);
                        }
                        setGoodsCheckChange();
                    }
                });
            }

            if (cartItem.isOver.equals("true"))
            {
                isOver.setVisibility(View.VISIBLE);
                islose.setVisibility(View.GONE);
                isDate.setVisibility(View.GONE);
            }
            if (cartItem.isPublish.equals("true"))
            {
                isOver.setVisibility(View.GONE);
                islose.setVisibility(View.VISIBLE);
                isDate.setVisibility(View.GONE);
            }
            ToolUtils.setLog("是否已过期:"+cartItem.isDate);
            if (cartItem.isDate.equals("true"))
            {
                isOver.setVisibility(View.GONE);
                islose.setVisibility(View.GONE);
                isDate.setVisibility(View.VISIBLE);
            }

            //零元特卖不给修改数量
            if(cartItem.isOSale.equals("true"))
            {
                cartNumView.setVisibility(View.GONE);
                cartNumLoseView.setVisibility(View.VISIBLE);
            }

            itemName.setText(cartItem.name);
            itemSize.setText(cartItem.size);
            itemCurrentPrice.setText("￥" + cartItem.currentPrice);
            itemFormalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            itemFormalPrice.setText("￥" + cartItem.formalPrice);
            itemNum.setText("" + cartItem.num);
            itemLoseNum.setText("" + cartItem.num);
            ToolUtils.setImageCacheUrl(cartItem.imageUrl, itemImage);

            itemDeleteBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    items.remove(cartItem);
                    itemsCheck.remove(cartItem);
                    boxs.remove(itemCheck);
                    CustomShopCartDeleteDialog.setDelateDialog(mContext, cartItem, cartGoodsLine, childeView);
                }
            });
            itemSubBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (cartItem.num - 1 > 0)
                    {
                        FetchEditDate(itemNum, 2, cartItem);
                    }
                }
            });
            itemAddBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    FetchEditDate(itemNum, 1, cartItem);
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
        Intent intent = new Intent(ZhaiDou.IntentRefreshCartGoodsTag);
        mContext.sendBroadcast(intent);
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
            CartItem cartItem = itemsCheck.get(i);
            num = num + cartItem.num;
            totalMoney = totalMoney + cartItem.num * cartItem.currentPrice;
            saveMoney = saveMoney + ((cartItem.formalPrice - cartItem.currentPrice) * cartItem.num);
        }
        numTv.setText("" + num);
        DecimalFormat df = new DecimalFormat("###.00");
        saveMoney = Double.parseDouble(df.format(saveMoney));
        totalMoney = Double.parseDouble(df.format(totalMoney));

        totalMoneyTv.setText("  ￥" + totalMoney);
        saveMoneyTv.setText("  ￥" + saveMoney);

    }

    /**
     * 初始请求数据对比
     */
    public void FetchDetailData()
    {
        String url = ZhaiDou.goodsCartGoodsUrl;
        for (int i = 0; i < items.size(); i++)
        {
            if (i == items.size() - 1)
            {
                url = url + items.get(i).id;
            } else
            {
                url = url + items.get(i).id + ",";
            }
        }
        ToolUtils.setLog("url:" + url);
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {

                if (jsonObject != null)
                {
                    JSONArray jsonArray = jsonObject.optJSONArray("merchandises");
                    JSONObject obj = null;
                    if (jsonArray != null)
                    {
                        for (int i = 0; i < jsonArray.length(); i++)
                        {

                            obj = jsonArray.optJSONObject(i);
                            int id = obj.optInt("id");
                            String name = obj.optString("title");
                            String endtime = obj.optString("end_time");
                            long over;
                            String isDate="false";
                            try
                            {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                                over=sdf.parse(endtime).getTime()-System.currentTimeMillis();
                                if (over<=0)
                                    isDate="true";

                            } catch (ParseException e)
                            {
                                e.printStackTrace();
                            }
                            String isPublish = obj.optBoolean("is_publish") ==false? "true" : "false";
                            JSONArray array = obj.optJSONArray("specifications");
                            for (int j = 0; j < array.length(); j++)
                            {
                                JSONObject object = array.optJSONObject(j);
                                int sizeId = object.optInt("id");
                                String size = object.optString("title");
                                int count = object.optInt("count");
                                String isOver;
                                if (count > 0)
                                {
                                    isOver = "false";
                                } else
                                {
                                    isOver = "true";
                                }
                                CartItem item = new CartItem();
                                item.id = id;
                                item.userId=userId;
                                item.name = name;
                                item.isPublish = isPublish;
                                item.size = size;
                                item.sizeId = sizeId;
                                item.num = count;
                                item.isOver = isOver;
                                item.isDate = isDate;
                                itemsServer.add(item);
                            }
                        }
                    }
                    mHandler.sendEmptyMessage(1);
                } else
                {
                    mHandler.sendEmptyMessage(1);
                    ShowToast("加载失败");
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                mHandler.sendEmptyMessage(1);
                Toast.makeText(getActivity(), "加载失败", Toast.LENGTH_SHORT).show();
            }
        });
        mRequestQueue.add(request);
    }

    /**
     * 修改数据请求
     *
     * @param itemNum
     * @param tags
     * @param mCartItem
     */
    private void FetchEditDate(TextView itemNum, int tags, CartItem mCartItem)
    {
        this.textNumView = itemNum;
        this.tags = tags;
        this.mCartItem = mCartItem;
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
        String url = ZhaiDou.goodsCartEditGoodsUrl + mCartItem.id + "/merchandise_specification?specification_id=" + mCartItem.sizeId;
        ToolUtils.setLog("url:" + url);
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                JSONObject obj;
                if (jsonObject != null)
                {
                    obj = jsonObject.optJSONObject("specification");
                    if (obj!=null &&obj.length()>0)
                    {
                        count = obj.optInt("count");
                        Str_publish = (obj.optBoolean("is_publish"))==false?"true":"false";
                        mHandler.sendEmptyMessage(2);
                    }
                } else
                {
                    ShowToast("加载失败");
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                mDialog.dismiss();
                Toast.makeText(getActivity(), "加载失败", Toast.LENGTH_SHORT).show();
            }
        });
        mRequestQueue.add(request);
    }

    @Override
    public void onResume()
    {
        if (isBuySuccess)
        {
            isBuySuccess=false;
            for (int i = 0; i <boxs.size() ; i++)
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

    public void onPause() {
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
