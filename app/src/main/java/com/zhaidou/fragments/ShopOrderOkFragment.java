package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.CountManager;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.dialog.CustomToastDialog;
import com.zhaidou.model.Address;
import com.zhaidou.model.CartArrayItem;
import com.zhaidou.model.CartGoodsItem;
import com.zhaidou.model.Coupon;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.NetService;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomEditText;
import com.zhaidou.view.TypeFaceEditText;
import com.zhaidou.view.TypeFaceTextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by roy on 15/7/24.
 */
public class ShopOrderOkFragment extends BaseFragment
{
    private static final String PAGE = "page";
    private static final String INDEX = "index";

    private String mPage;
    private int mIndex;
    private View mView;
    private Context mContext;
    private Dialog mDialog;
    private String bzInfo_Str;
    private String phone_Str;
    private String code_Str;

    private TypeFaceTextView backBtn, titleTv;
    private Button okBtn;
    private LinearLayout orderAddressInfoLine, orderAddressNullLine, orderAddressEditLine;
    private LinearLayout orderGoodsListLine, orderVerifyLine,orderCouponLine;
    private TextView couponNameTv, moneyCouponTv;
    private TypeFaceEditText bzInfo;
    private TextView moneyTv, moneyYfTv, moneyTotalTv;
    private TextView addressNameTv, addressPhoneTv,addressDefaultTv, addressinfoTv, noFreeTv;
    private List<CartGoodsItem> items=new ArrayList<CartGoodsItem>();
    private List<Address> addressList = new ArrayList<Address>();

    private int num = 0;
    private double money = 0;
    private double moneyYF = 0;
    private double totalMoney = 0;

    private ScrollView mScrollView;
    private LinearLayout verifyView;
    private CustomEditText mCodeView, mPhoneView;
    private TextView mGetCode;

    private LinearLayout loadingView, nullNetView, nullView;
    private TextView reloadBtn, reloadNetBtn;

    private int initTime = 0;
    private Timer mTimer;

    private RequestQueue mRequestQueue;
    private int STATUS_FROM_ORDER = 3;
    private final int UPDATE_DEFALUE_ADDRESS_INFO = 0;
    private final int UPDATE_ADDRESS_LOADING_FAIL = 2;
    private final int UPDATE_ISBUYOSALE =1;
    private final int UPDATE_LOADING_FAIL= 3;
    private final int UPDATE_COMMIT_SUCCESS= 4;
    private final int UPDATE_COMMIT_FAIL= 5;
    private final int UPDATE_VERFIY = 6;
    private final int UPDATE_VERFIY_SUCCESSS = 7;
    private final int UPDATE_GETSMS_SUCCESSS = 8;
    private final int UPDATE_COUPON_SUCCESSS = 9;
    private final int UPDATE_NULLCOUPON_SUCCESSS = 10;
    private final int UPDATE_COUPON_RESULT = 11;

    private boolean isOSaleBuy;//是否已经购买过零元特卖
    private boolean isOSale;//是否含有零元特卖
    private boolean isljOsale;//是否是来自立即购买的零元特卖
    private boolean isNoFree;//是否不免邮，当只有一个商品且为零元特卖时为真
    private boolean isVerify;//帐号是否需要验证

    private Address address;
    private String token;
    private int userId;
    private int flags = 2;//1代表立即购买
    private ArrayList<CartArrayItem> cartArrayItems=new ArrayList<CartArrayItem>();
    private List<CartArrayItem> orderArrayItems=new ArrayList<CartArrayItem>();
    private JSONArray goodsArray;
    private Coupon mCoupon;
    private int mStatus=0;


    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case UPDATE_DEFALUE_ADDRESS_INFO:
                    mDialog.dismiss();
                    loadingView.setVisibility(View.GONE);
                    orderAddressInfoLine.setVisibility(View.VISIBLE);
                    orderAddressEditLine.setVisibility(View.VISIBLE);
                    orderAddressNullLine.setVisibility(View.GONE);

                    address = addressList.get(0);
                    setYFMoney(address.getPrice());
                    addressPhoneTv.setText(address.getPhone());
                    addressNameTv.setText(address.getName());
                    addressinfoTv.setText(address.getProvince() + address.getCity() + address.getArea() + address.getAddress());
                    addressDefaultTv.setVisibility(address.isIs_default()?View.VISIBLE:View.GONE);
                    break;
                case UPDATE_ISBUYOSALE:
                    if (isOSaleBuy)
                    {
                        mDialog.dismiss();
                        CustomToastDialog.setToastDialog(mContext, "您今天已经购买过零元特卖商品，请勿重复购买");
                    }
                    else
                    {
                        commit();
                    }
                    break;
                case UPDATE_ADDRESS_LOADING_FAIL:
                    if (mDialog != null)
                        mDialog.dismiss();
                    loadingView.setVisibility(View.GONE);
                    orderAddressInfoLine.setVisibility(View.GONE);
                    orderAddressEditLine.setVisibility(View.GONE);
                    orderAddressNullLine.setVisibility(View.VISIBLE);
                    break;
                case UPDATE_LOADING_FAIL:
                    mDialog.dismiss();
                    CustomToastDialog.setToastDialog(mContext,"抱歉,提交订单失败");
                    break;
                case UPDATE_COMMIT_SUCCESS:
                    mDialog.dismiss();
                    if (isljOsale)
                    {
                        //发送刷新商品详情零元特卖购买广播
                        Intent intent= new Intent(ZhaiDou.IntentRefreshOGoodsDetailsTag);
                        mContext.sendBroadcast(intent);
                    }
                    else
                    {
                        //发送刷新商品详情普通特卖该规格购买广播
                        Intent intent= new Intent(ZhaiDou.IntentRefreshGoodsDetailsTag);
                        mContext.sendBroadcast(intent);
                    }
                    //发送刷新购物车广播
                    Intent intent = new Intent(ZhaiDou.IntentRefreshCartPaySuccessTag);
                    mContext.sendBroadcast(intent);
                    //关闭本页面
                    ((BaseActivity) getActivity()).popToStack(ShopOrderOkFragment.this);

                    String result = (String) msg.obj;
                    ToolUtils.setLog(result);
                    CountManager.getInstance().add(CountManager.TYPE.TAG_PREPAY);
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject orderObj = jsonObject.optJSONObject("data");
                        int orderId = orderObj.optInt("orderId");
                        String orderCode = orderObj.optString("orderCode");
                        double amount = orderObj.optDouble("orderPayAmount");
                        long time = orderObj.optLong("orderRemainingTime");
                        ShopPaymentFragment shopPaymentFragment = ShopPaymentFragment.newInstance(orderId,orderCode ,amount,time, null);
                        ((BaseActivity) getActivity()).navigationToFragment(shopPaymentFragment);
                    } catch (Exception e)
                    {

                    }
                    break;
                case UPDATE_COMMIT_FAIL:
                    mDialog.dismiss();
                    CustomToastDialog.setToastDialog(mContext, msg.obj.toString());
                    break;
                case UPDATE_VERFIY:
                    if (isVerify)
                    {
                        verifyView.setVisibility(View.VISIBLE);
                    } else
                    {
                        verifyView.setVisibility(View.GONE);
                    }
                    GetCoupon();
                    break;
                case UPDATE_VERFIY_SUCCESSS:
                    isVerify=false;
                    verifyView.setVisibility(View.GONE);
                    judageCommit();
                    break;
                case UPDATE_GETSMS_SUCCESSS:
                    codeTimer();
                    break;
                case UPDATE_COUPON_SUCCESSS:
                    couponNameTv.setText(mCoupon.info);
                    moneyCouponTv.setText("￥-"+ToolUtils.isIntPrice(mCoupon.money+""));
                    setYFMoney(0);
                    break;
                case UPDATE_NULLCOUPON_SUCCESSS:
                    couponNameTv.setText("不使用优惠券");
                    moneyCouponTv.setText("￥"+0);
                    break;
                case UPDATE_COUPON_RESULT:
                    if(mDialog!=null)
                        mDialog.dismiss();
                    if (msg.obj!=null)
                        JsonParse(msg.obj.toString());
                    FetchAddressData();
                    break;
            }
        }
    };


    private TextWatcher textWatcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
        {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
        {
            bzInfo_Str = charSequence.toString();
        }

        @Override
        public void afterTextChanged(Editable editable)
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
                case R.id.jsOkBtn:
                    //当需要验证手机号码时候，需要先判断
                    if (isVerify)
                    {
                        if (TextUtils.isEmpty(phone_Str))
                        {
                            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);//滑动到最底部
                            mPhoneView.setShakeAnimation();
                            return;
                        }
                        if (TextUtils.isEmpty(code_Str))
                        {
                            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);//滑动到最底部
                            mCodeView.setShakeAnimation();
                            return;
                        }
                        if (ToolUtils.isPhoneOk(phone_Str))
                        {
                            mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "提交中");
                            FetchBlindPhoneData();
                        } else
                        {
                            ToolUtils.setToast(mContext, R.string.phone_lose_txt);
                        }
                    } else
                    {
                        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "提交中");
                        judageCommit();
                    }
                    break;
                case R.id.jsEditAddressBtn:
                    AddrSelectFragment addrManageFragment = AddrSelectFragment.newInstance("", STATUS_FROM_ORDER, address);
                    ((BaseActivity) getActivity()).navigationToFragment(addrManageFragment);
                    addrManageFragment.setAddressListener(new AddrSelectFragment.AddressListener()
                    {
                        @Override
                        public void onDefalueAddressChange(Address maddress)
                        {
                            ToolUtils.setLog(maddress.toString());
                            address = maddress;
                            setYFMoney(address.getPrice());
                            addressDefaultTv.setVisibility(address.isIs_default()?View.VISIBLE:View.GONE);
                            addressPhoneTv.setText(address.getPhone());
                            addressNameTv.setText(address.getName());
                            addressinfoTv.setText(address.getProvince() + address.getCity() + address.getArea() + address.getAddress());
                        }

                        @Override
                        public void onDeleteFinishAddress()
                        {
                            orderAddressInfoLine.setVisibility(View.GONE);
                            orderAddressNullLine.setVisibility(View.VISIBLE);
                            orderAddressEditLine.setVisibility(View.GONE);
                        }
                    });
                    break;
                case R.id.jsAddressNullLine:

                    final AddrNewAddrFragment newAddrFragment = AddrNewAddrFragment.newInstance(0, "", "", "", "", 0, 2);
                    ((BaseActivity) getActivity()).navigationToFragment(newAddrFragment);
                    newAddrFragment.setAddrSaveSuccessListener(new AddrNewAddrFragment.AddrSaveSuccessListener()
                    {
                        @Override
                        public void onSaveListener(JSONObject receiver, int status, double yfprice, String province, String city, String area)
                        {
                            int id = receiver.optInt("id");
                            int user_id = receiver.optInt("user_id");
                            String name = receiver.optString("name");
                            String phone = receiver.optString("phone");
                            int provider_id = receiver.optInt("provider_id");
                            String addresss = receiver.optString("address");
                            boolean is_default = receiver.optBoolean("is_default");
                            Address addr = new Address(id, name, is_default, phone, user_id, addresss, provider_id, yfprice);
                            addr.setProvince(province);
                            addr.setCity(city);
                            addr.setArea(area);
                            address = addr;
                            setYFMoney(addr.getPrice());
                            orderAddressInfoLine.setVisibility(View.VISIBLE);
                            orderAddressNullLine.setVisibility(View.GONE);
                            orderAddressEditLine.setVisibility(View.VISIBLE);
                            addressPhoneTv.setText(addr.getPhone());
                            addressNameTv.setText(addr.getName());
                            addressinfoTv.setText(address.getProvince() + address.getCity() + address.getArea() + addr.getAddress());
                            addressDefaultTv.setVisibility(View.VISIBLE);
                            ((BaseActivity) getActivity()).popToStack(newAddrFragment);
                        }
                    });
                    break;
                case R.id.bt_getCode:
                    if (TextUtils.isEmpty(phone_Str))
                    {
                        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);//滑动到最底部
                        mPhoneView.setShakeAnimation();
                        return;
                    }
                    if (ToolUtils.isPhoneOk(phone_Str))
                    {
                        mDialog.show();
                        FetchSMSData();
                    }
                    else
                    {
                        ToolUtils.setToast(mContext, R.string.phone_lose_txt);
                    }
                    break;
                case R.id.jsCouponLine:
                    if (goodsArray==null)
                        setGoodsArray();
                    ShopOrderSelectCouponFragment s = ShopOrderSelectCouponFragment.newInstance("", mStatus, mCoupon, goodsArray.toString());
                    ((BaseActivity) getActivity()).navigationToFragment(s);
                    s.setOnCouponListener(new ShopOrderSelectCouponFragment.OnCouponListener()
                    {
                        @Override
                        public void onDefaultCouponChange(Coupon coupon)
                        {
                            if (coupon==null)
                            {
                                mCoupon=null;
                                couponNameTv.setText("不使用优惠券");
                                moneyCouponTv.setText("￥"+0);
                            }
                            else
                            {
                                mCoupon=coupon;
                                couponNameTv.setText(mCoupon.info);
                                moneyCouponTv.setText("￥-"+ToolUtils.isIntPrice(mCoupon.money+""));
                            }
                            setYFMoney(0);

                        }
                    });

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


    public static ShopOrderOkFragment newInstance(String page, int index)
    {
        ShopOrderOkFragment fragment = new ShopOrderOkFragment();
        Bundle args = new Bundle();
        args.putString(PAGE, page);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    public ShopOrderOkFragment()
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
            cartArrayItems = (ArrayList<CartArrayItem>) getArguments().getSerializable("goodsList");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        if (mView == null)
        {
            mView = inflater.inflate(R.layout.shop_settlement_page, container, false);
            mContext = getActivity();
            initView();
        }
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
        if (flags!=1)
        {
            Intent intent = new Intent(ZhaiDou.IntentRefreshCartGoodsTag);
            mContext.sendBroadcast(intent);
        }

        loadingView = (LinearLayout) mView.findViewById(R.id.loadingView);
        nullNetView = (LinearLayout) mView.findViewById(R.id.nullNetline);
        nullView = (LinearLayout) mView.findViewById(R.id.nullline);

        reloadBtn = (TextView) mView.findViewById(R.id.nullReload);
        reloadBtn.setOnClickListener(onClickListener);

        reloadNetBtn = (TextView) mView.findViewById(R.id.netReload);
        reloadNetBtn.setOnClickListener(onClickListener);

        mRequestQueue = Volley.newRequestQueue(getActivity());
        titleTv = (TypeFaceTextView) mView.findViewById(R.id.title_tv);
        titleTv.setText(R.string.shop_order_ok_text);

        noFreeTv = (TextView) mView.findViewById(R.id.jsNofree);

        okBtn = (Button) mView.findViewById(R.id.jsOkBtn);
        okBtn.setOnClickListener(onClickListener);

        items.clear();
        for (int i = 0; i <cartArrayItems.size() ; i++)
        {
            items.addAll(cartArrayItems.get(i).goodsItems);
        }
        orderArrayItems.addAll(cartArrayItems);

        if (flags == 1)
        {
            if (items.get(0).isOSale.equals("true"))
            {
                isljOsale = true;
            }
        }

        if (items.size() == 1)
        {
            if (items.get(0).isOSale.equals("true"))
            {
                isNoFree = true;
            }
        }

        orderCouponLine = (LinearLayout) mView.findViewById(R.id.jsCouponLine);
        orderCouponLine.setOnClickListener(onClickListener);
        couponNameTv = (TextView) mView.findViewById(R.id.jsCouponTv);
        moneyCouponTv = (TextView) mView.findViewById(R.id.jsPriceCouponTv);

        moneyTv = (TextView) mView.findViewById(R.id.jsPriceTotalTv);
        moneyYfTv = (TextView) mView.findViewById(R.id.jsPriceYfTv);
        moneyTotalTv = (TextView) mView.findViewById(R.id.jsTotalMoney);

        addressinfoTv = (TextView) mView.findViewById(R.id.jsAddressinfoTv);
        addressNameTv = (TextView) mView.findViewById(R.id.jsAddressNameTv);
        addressPhoneTv = (TextView) mView.findViewById(R.id.jsAddressPhoneTv);
        addressDefaultTv= (TextView) mView.findViewById(R.id.jsAddressDefaultTv);

        bzInfo = (TypeFaceEditText) mView.findViewById(R.id.jsEditBzInfo);
        bzInfo.addTextChangedListener(textWatcher);

        orderAddressInfoLine = (LinearLayout) mView.findViewById(R.id.jsAddressInfoLine);
        orderAddressNullLine = (LinearLayout) mView.findViewById(R.id.jsAddressNullLine);
        orderAddressNullLine.setOnClickListener(onClickListener);
        orderAddressEditLine = (LinearLayout) mView.findViewById(R.id.jsEditAddressBtn);
        orderAddressEditLine.setOnClickListener(onClickListener);

        orderGoodsListLine = (LinearLayout) mView.findViewById(R.id.orderGoodsList);

        verifyView = (LinearLayout) mView.findViewById(R.id.jsVerifyView);
        mCodeView = (CustomEditText) mView.findViewById(R.id.tv_code);
        mCodeView.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
            @Override
            public void afterTextChanged(Editable s)
            {
                code_Str=s.toString();
            }
        });
        mPhoneView = (CustomEditText) mView.findViewById(R.id.tv_phone);
        mPhoneView.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
            @Override
            public void afterTextChanged(Editable s)
            {
                phone_Str=s.toString();
            }
        });
        mGetCode = (TextView) mView.findViewById(R.id.bt_getCode);
        mGetCode.setOnClickListener(onClickListener);

        mScrollView = (ScrollView) mView.findViewById(R.id.scrollView);

        token = (String) SharedPreferencesUtil.getData(mContext, "token", "");
        userId = (Integer) SharedPreferencesUtil.getData(mContext, "userId", -1);

        setGoodsArray();
        initDataView();
        initData();
    }

    /**
     * 设置商品数据jsonArray
     */
    private void setGoodsArray()
    {
        goodsArray=new JSONArray();
        try
        {
            for (int i = 0; i <items.size(); i++)
            {
                CartGoodsItem cartArrayItem=items.get(i);
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("skucode",cartArrayItem.sizeId);
                jsonObject.put("num",cartArrayItem.num);
                goodsArray.put(jsonObject);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void initData()
    {
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "");
        if (NetworkUtils.isNetworkAvailable(mContext))
        {
            FetchVerifyData();
        } else
        {
            if (mDialog != null)
                mDialog.dismiss();
            nullView.setVisibility(View.GONE);
            nullNetView.setVisibility(View.VISIBLE);
        }

    }

    /**
     * 设置运费
     */
    private void setYFMoney(double mm)
    {
        if (isNoFree)
        {
            moneyYF = mm;
            moneyYfTv.setText("￥" + ToolUtils.isIntPrice("" + moneyYF));
            noFreeTv.setVisibility(View.GONE);
        } else
        {
            moneyYF = 0;
            moneyYfTv.setText("￥" + 0);
            noFreeTv.setVisibility(View.VISIBLE);
        }
        ToolUtils.setLog("运费：" + moneyYF);
        DecimalFormat df = new DecimalFormat("###.00");
        if (mCoupon!=null)
        {
            totalMoney = Double.parseDouble(df.format(money + moneyYF-mCoupon.money));
            if (totalMoney<0)
                totalMoney=0;
            moneyTotalTv.setText("￥" + ToolUtils.isIntPrice("" + totalMoney));
        }
        else
        {
            totalMoney = Double.parseDouble(df.format(money + moneyYF));
            moneyTotalTv.setText("￥" + ToolUtils.isIntPrice("" + totalMoney));
        }

    }

    /**
     * 设置商品信息
     */
    private void initDataView()
    {
        if (items.size() > 0)
        {
            addGoodsView();
        }

        for (int i = 0; i < items.size(); i++)
        {
            CartGoodsItem cartGoodsItem = items.get(i);
            num = num + cartGoodsItem.num;
            money = money + cartGoodsItem.num * cartGoodsItem.currentPrice;
        }

        DecimalFormat df = new DecimalFormat("###.00");
        money = Double.parseDouble(df.format(money));
        totalMoney = Double.parseDouble(df.format(money + 0));
        moneyTv.setText("￥" + ToolUtils.isIntPrice("" + money));
        moneyTotalTv.setText("￥" + ToolUtils.isIntPrice("" + totalMoney));
        moneyYfTv.setText("￥" + 0);

    }

    ;

    /**
     * 添加商品集合
     */
    private void addGoodsView()
    {
        orderGoodsListLine.removeAllViews();
        for (int position = 0; position < items.size(); position++)
        {
            View childeView = LayoutInflater.from(mContext).inflate(R.layout.shop_settlement_goods_item, null);
            TypeFaceTextView itemName = (TypeFaceTextView) childeView.findViewById(R.id.orderItemNameTv);
            TypeFaceTextView itemSize = (TypeFaceTextView) childeView.findViewById(R.id.orderItemSizeTv);
            TextView itemCurrentPrice = (TextView) childeView.findViewById(R.id.orderItemCurrentPrice);
            TextView itemFormalPrice = (TextView) childeView.findViewById(R.id.orderItemFormalPrice);
            final TypeFaceTextView itemNum = (TypeFaceTextView) childeView.findViewById(R.id.orderItemNum);
            ImageView itemImage = (ImageView) childeView.findViewById(R.id.orderImageItemTv);
            ImageView itemLine = (ImageView) childeView.findViewById(R.id.orderItemLine);

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

            itemName.setText(cartGoodsItem.name);
            itemSize.setText(cartGoodsItem.size);
            itemCurrentPrice.setText("￥" + ToolUtils.isIntPrice("" + cartGoodsItem.currentPrice));
            itemFormalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            itemFormalPrice.setText("￥" + ToolUtils.isIntPrice("" + cartGoodsItem.formalPrice));
            itemNum.setText("" + cartGoodsItem.num);
            ToolUtils.setImageCacheUrl(cartGoodsItem.imageUrl, itemImage);

            orderGoodsListLine.addView(childeView);
        }
    }

    /**
     * 验证码倒计时事件处理
     */
    private void codeTimer()
    {
        initTime = ZhaiDou.VERFIRY_TIME;
        mGetCode.setBackgroundResource(R.drawable.btn_no_click_selector);
        mGetCode.setText("重新获取(" + initTime + ")");
        mGetCode.setClickable(false);
        mTimer = new Timer();
        mTimer.schedule(new MyTimer(), 1000, 1000);
    }

    /**
     * 倒计时
     */
    class MyTimer extends TimerTask
    {
        @Override
        public void run()
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    initTime = initTime - 1;
                    mGetCode.setText("重新获取(" + initTime + ")");
                    if (initTime <= 0)
                    {
                        mTimer.cancel();
                        mGetCode.setText("获取验证码");
                        mGetCode.setBackgroundResource(R.drawable.btn_green_click_bg);
                        mGetCode.setClickable(true);
                    }
                }
            });
        }
    }

    /**
     * 提交前判断地址等信息
     */
    private void judageCommit()
    {
        if (address != null)
        {
            for (int i = 0; i < items.size(); i++)
            {
                if (items.get(i).isOSale.equals("true"))
                {
                    isOSale = true;
                }
            }
            if (isOSale)
            {
                FetchOSaleData();//判断零元特卖是否已经当天购买过
            } else
            {
                commit();
            }
        } else
        {
            mDialog.dismiss();
            CustomToastDialog.setToastDialog(mContext, "抱歉,您未填写收货地址");
        }

    }

    /**
     * 提交订单接口
     */
    private void commit()
    {
        final Map<String,String> headers=new HashMap<String, String>();
        headers.put("SECAuthorization", token);
        headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));

        // 创建名/值组列表
        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        try
        {
            params.add(new BasicNameValuePair("businessType", "01"));
            params.add(new BasicNameValuePair("userId", userId + ""));
            if (mCoupon != null)
            {
                BigDecimal bd1 = new BigDecimal(Double.toString(money));
                BigDecimal bd2 = new BigDecimal(Double.toString(mCoupon.money));
                params.add(new BasicNameValuePair("orderPayAmount", bd1.subtract(bd2).doubleValue()+""));
                params.add(new BasicNameValuePair("couponsKey", mCoupon.couponCode));
                params.add(new BasicNameValuePair("couponsAmount",mCoupon.money+""));
            } else
            {
                params.add(new BasicNameValuePair("orderPayAmount", money+""));
            }
            params.add(new BasicNameValuePair("userAddressId", address.getId()+""));
            params.add(new BasicNameValuePair("token", token));
            params.add(new BasicNameValuePair("version", mContext.getResources().getString(R.string.app_versionName)));
            params.add(new BasicNameValuePair("clientType", "ANDROID"));
            params.add(new BasicNameValuePair("clientVersion", (ZDApplication.localVersionCode+3)+""));
            params.add(new BasicNameValuePair("remark", bzInfo.getText().toString()));

            JSONArray storeArray=new JSONArray();
            for (int i = 0; i < orderArrayItems.size(); i++)
            {
                CartArrayItem cartArrays=orderArrayItems.get(i);
                JSONObject storeObject=new JSONObject();
                storeObject.put("storeId",cartArrays.storeId);
                storeObject.put("storeDeliveryId",2);
                storeObject.put("storeDeliveryFee",moneyYF);
                storeObject.put("messageToStore",bzInfo_Str);
                JSONArray goodsArray=new JSONArray();

                for (int j = 0; j <cartArrays.goodsItems.size(); j++)
                {
                    CartGoodsItem cartGoodsItem=cartArrays.goodsItems.get(j);
                    JSONObject goodsObject=new JSONObject();
                    goodsObject.put("productSKUCode",cartGoodsItem.sizeId);
                    goodsObject.put("quantity",cartGoodsItem.num);
                    goodsObject.put("price",cartGoodsItem.currentPrice);
                    goodsObject.put("points",0);
                    goodsArray.put(goodsObject);
                }
                storeObject.put("orderItemList",goodsArray);
                storeArray.put(storeObject);
            }
            params.add(new BasicNameValuePair("storeOrderItemPOList", storeArray.toString()));
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String result= NetService.GETHttpPostService(mContext,ZhaiDou.CommitOrdersUrl,headers,params);
                if (result != null && result.length() > 0)
                {
                    try
                    {
                        mDialog.dismiss();
                        JSONObject jsonObject = new JSONObject(result);
                        ToolUtils.setLog(jsonObject.toString());
                        int status = jsonObject.optInt("status");
                        if (status == 200)
                        {
                            handler.obtainMessage(UPDATE_COMMIT_SUCCESS,result).sendToTarget();
                        } else if (status == 500)
                        {
                            String errorArr = jsonObject.optString("message");
                            handler.obtainMessage(UPDATE_COMMIT_FAIL,errorArr).sendToTarget();
                        }
                        else
                        {
                            handler.sendEmptyMessage(UPDATE_LOADING_FAIL);
                        }

                    } catch (Exception e)
                    {
                    }

                } else
                {
                    handler.sendEmptyMessage(UPDATE_LOADING_FAIL);
                }
            }
        }).start();

    }

    /**
     * 获取优惠券接口
     */
    private void GetCoupon()
    {
        Map<String,String> mParams=new Hashtable<String, String>();
        mParams.put("userId", userId + "");
        mParams.put("skuAndNumLists", goodsArray.toString());
        ZhaiDouRequest request = new ZhaiDouRequest(mContext, Request.Method.POST, ZhaiDou.GetOrderCouponDefaultUrl, mParams, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (jsonObject!=null)
                    ToolUtils.setLog(jsonObject.toString());
                handler.obtainMessage(UPDATE_COUPON_RESULT,jsonObject).sendToTarget();
            }
        }, null);
        ((ZDApplication) mContext.getApplicationContext()).mRequestQueue.add(request);

    }


    /**
     * 请求收货地址信息
     */
    private void FetchAddressData()
    {
        ZhaiDouRequest request = new ZhaiDouRequest(mContext,ZhaiDou.AddressListUrl, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                mDialog.dismiss();
                ToolUtils.setLog(jsonObject.toString());
                int status=jsonObject.optInt("status");
                if (status!=200)
                {
                    ToolUtils.setToast(mContext,R.string.loading_fail_txt);
                }
                JSONObject dataObject=jsonObject.optJSONObject("data");
                if (dataObject != null)
                {
                    JSONArray receivers = dataObject.optJSONArray("receivers");
                    if (receivers != null && receivers.length() > 0)
                    {
                        for (int i = 0; i < receivers.length(); i++)
                        {
                            JSONObject receiver = receivers.optJSONObject(i);
                            int id = receiver.optInt("id");
                            int user_id = receiver.optInt("user_id");
                            String name = receiver.optString("name");
                            String phone = receiver.optString("phone");
                            int provider_id = receiver.optInt("provider_id");
                            String addr = receiver.optString("address");
                            boolean is_default = receiver.optBoolean("is_default");
                            String province = receiver.optString("parent_name");
                            String city = receiver.optString("city_name");
                            String area = receiver.optString("provider_name");
                            double price = receiver.optDouble("price");
                            Address address = new Address(id, name, is_default, phone, user_id, addr, provider_id, price);
                            address.setProvince(province);
                            address.setCity(city);
                            address.setArea(area);
                            addressList.add(address);
                        }
                        Message message = new Message();
                        message.what = UPDATE_DEFALUE_ADDRESS_INFO;
                        message.obj = addressList;
                        handler.sendMessage(message);
                    } else
                    {
                        handler.sendEmptyMessage(UPDATE_ADDRESS_LOADING_FAIL);
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
                nullNetView.setVisibility(View.GONE);
                nullView.setVisibility(View.VISIBLE);
            }
        });
        mRequestQueue.add(request);
    }

    /*
     * 判断是否已经验证手机号码
     */
    public void FetchVerifyData()
    {
        String url = ZhaiDou.OrderAccountOrPhone;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                ToolUtils.setLog(jsonObject.toString());
                if (jsonObject != null)
                {
                    if(jsonObject.optInt("status")==200)
                    {
                        JSONObject object=jsonObject.optJSONObject("data");
                        if (object!=null)
                        {
                            if (object.optInt("status")==400)
                            {
                                isVerify =true;
                            }
                        }
                    }
                    handler.sendEmptyMessage(UPDATE_VERFIY);
                } else
                {
                    if (mDialog != null)
                        mDialog.dismiss();
                    nullNetView.setVisibility(View.GONE);
                    nullView.setVisibility(View.VISIBLE);
                }

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                nullNetView.setVisibility(View.GONE);
                nullView.setVisibility(View.VISIBLE);
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
  * 获取短信
  */
    public void FetchSMSData()
    {
        String url = ZhaiDou.OrderGetSMS+phone_Str;
        ZhaiDouRequest request = new ZhaiDouRequest(mContext,url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                if (jsonObject != null)
                {
                    int flag=jsonObject.optInt("status");
                    String msg=jsonObject.optString("message");
                    if (flag==200)
                    {
                        JSONObject object=jsonObject.optJSONObject("data");
                        if (object!=null)
                        {
                            if (object.optInt("status")!=201)
                            {
                                ToolUtils.setToastLong(mContext,object.optString("message"));
                            }
                            else
                            {
                                handler.sendEmptyMessage(UPDATE_GETSMS_SUCCESSS);
                            }
                        }
                        else
                        {
                            handler.sendEmptyMessage(UPDATE_GETSMS_SUCCESSS);
                        }

                    }
                } else
                {
                    ToolUtils.setToastLong(mContext,R.string.loading_fail_txt);
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
        });
        mRequestQueue.add(request);
    }

    /*
     * 获取短信
     */
    public void FetchBlindPhoneData()
    {
        String url = ZhaiDou.OrderBlindPhone;
        Map<String,String> maps=new HashMap<String, String>();
        maps.put("phone", phone_Str);
        maps.put("vcode", code_Str);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,url,new JSONObject(maps), new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                if (jsonObject != null)
                {
                    int flag=jsonObject.optInt("status");
                    String msg=jsonObject.optString("message");
                    JSONObject object=jsonObject.optJSONObject("data");
                    if (object!=null)
                    {
                        if (object.optInt("status")!=201)
                        {
                            ToolUtils.setToastLong(mContext,object.optString("message"));
                        }
                        else
                        {
                            handler.sendEmptyMessage(UPDATE_VERFIY_SUCCESSS);
                        }
                    }
                    else
                    {
                        handler.sendEmptyMessage(UPDATE_VERFIY_SUCCESSS);
                    }
                } else
                {
                    ToolUtils.setToastLong(mContext,R.string.loading_fail_txt);
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
     * 零元特卖是否购买请求
     */
    public void FetchOSaleData()
    {
        String url = ZhaiDou.IsBuyOSaleUrl+userId;
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                if (jsonObject != null)
                {
                    isOSaleBuy = jsonObject.optInt("ifBuy")==1?false:true;
                    if (jsonObject.optInt("status")==500)
                    {
                        isOSaleBuy=false;
                    }
                    handler.sendEmptyMessage(UPDATE_ISBUYOSALE);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                ToolUtils.setToast(mContext,R.string.loading_fail_txt);
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("SECAuthorization", token);
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        mRequestQueue.add(request);
    }


    /**
     * json解析
     * @param json
     */
    private void JsonParse(String json)
    {
        ToolUtils.setLog(json);
        JSONObject jsonObject= null;
        try
        {
            jsonObject = new JSONObject(json);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        int status=jsonObject.optInt("status");
        if (status!=200)
        {
            handler.sendEmptyMessage(UPDATE_NULLCOUPON_SUCCESSS);
            ToolUtils.setToast(mContext,"抱歉,优惠券加载失败");
            return;
        }
        JSONObject object=jsonObject.optJSONObject("data");
        if(object==null)
        {
            mStatus=1;
            handler.sendEmptyMessage(UPDATE_NULLCOUPON_SUCCESSS);
            return;
        }
        JSONObject datasObject = object.optJSONObject("data");
        if (datasObject != null && datasObject.length() > 0)
        {
            int id=datasObject.optInt("id");
            int couponId=datasObject.optInt("couponId");
            int couponRuleId=datasObject.optInt("couponRuleId");
            String couponCode=datasObject.optString("couponCode");
            double enoughValue=datasObject.optDouble("enoughValue");
            double money=datasObject.optDouble("bookValue");
            String info=datasObject.optString("couponName");
            String startTime=datasObject.optString("startTime");
            String endTime=datasObject.optString("endTime");
            int days=0;
            try
            {
                SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date endDate=format.parse(endTime);
                long diff=endDate.getTime()-System.currentTimeMillis();
                days=(int) (diff / (1000 * 60 * 60 * 24)+((diff %(1000 * 60 * 60 * 24))>0?1:0));
            } catch (ParseException e)
            {
                e.printStackTrace();
            }
            endTime=endTime.split(" ")[0];
            String statu=datasObject.optString("status");
            String property=datasObject.optString("property");
            String goodsType=datasObject.optString("goodsType");
            mCoupon=new Coupon();
            mCoupon.id=id;
            mCoupon.couponId=couponId;
            mCoupon.couponRuleId=couponRuleId;
            mCoupon.couponCode=couponCode;
            mCoupon.enoughMoney=enoughValue;
            mCoupon.money=money;
            mCoupon.info="满"+ToolUtils.isIntPrice(enoughValue+"")+"减"+ToolUtils.isIntPrice(money+"");
            mCoupon.startDate=startTime;
            mCoupon.endDate=endTime;
            mCoupon.time=days;
            mCoupon.status=statu;
            mCoupon.property=property;
            mCoupon.type=goodsType;
            mCoupon.isDefault=false;

            handler.sendEmptyMessage(UPDATE_COUPON_SUCCESSS);
        } else
        {
            mStatus=1;
            handler.sendEmptyMessage(UPDATE_NULLCOUPON_SUCCESSS);
        }
    }

    public void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.shop_order_ok_text));
    }

    public void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.shop_order_ok_text));
    }
}
