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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.dialog.CustomToastDialog;
import com.zhaidou.model.Address;
import com.zhaidou.model.CartItem;
import com.zhaidou.sqlite.CreatCartDB;
import com.zhaidou.sqlite.CreatCartTools;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomEditText;
import com.zhaidou.view.TypeFaceEditText;
import com.zhaidou.view.TypeFaceTextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
    private LinearLayout orderGoodsListLine, orderVerifyLine;
    private TypeFaceEditText bzInfo;
    private TextView moneyTv, moneyYfTv, moneyTotalTv;
    private TextView addressNameTv, addressPhoneTv, addressinfoTv, noFreeTv;
    private ArrayList<CartItem> items;
    private List<CartItem> erroritems = new ArrayList<CartItem>();
    private List<Address> addressList = new ArrayList<Address>();
    private String Str_token;

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
    private final int UPDATE_VERFIY = 9;
    private final int UPDATE_VERFIY_SUCCESSS = 10;
    private final int UPDATE_GETSMS_SUCCESSS = 11;
    private final int UPDATE_GETSMS_FAIL = 12;

    private boolean isOSaleBuy;//是否已经购买过零元特卖
    private boolean isOSale;//是否含有零元特卖
    private boolean isljOsale;//是否是来自立即购买的零元特卖
    private boolean isNoFree;//是否不免邮，当只有一个商品且为零元特卖时为真
    private boolean isVerify = true;//帐号是否需要验证

    private Address address;
    private CreatCartDB creatCartDB;
    private String token;
    private int userId;
    private int flags = 0;//1代表立即购买

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case UPDATE_DEFALUE_ADDRESS_INFO:
                    if (mDialog != null)
                        mDialog.dismiss();
                    loadingView.setVisibility(View.GONE);

                    orderAddressInfoLine.setVisibility(View.VISIBLE);
                    orderAddressEditLine.setVisibility(View.VISIBLE);
                    orderAddressNullLine.setVisibility(View.GONE);

                    address = addressList.get(0);
                    setYFMoney(address.getPrice());
                    addressPhoneTv.setText("电话：" + address.getPhone());
                    addressNameTv.setText("收件人：" + address.getName());
                    addressinfoTv.setText(address.getProvince() + address.getCity() + address.getArea() + address.getAddress());
                    break;
                case 2:
                    mDialog.dismiss();
                    if (mDialog != null)
                        mDialog.dismiss();
                    loadingView.setVisibility(View.GONE);
                    orderAddressInfoLine.setVisibility(View.GONE);
                    orderAddressEditLine.setVisibility(View.GONE);
                    orderAddressNullLine.setVisibility(View.VISIBLE);
                    break;
                case 3:
                    mDialog.dismiss();
                    Toast.makeText(mContext, "抱歉,提交订单失败", Toast.LENGTH_LONG).show();
                    break;
                case 4:
                    mDialog.dismiss();

                    if (flags != 1)
                    {
                        for (int i = 0; i < items.size(); i++)
                        {
                            CreatCartTools.deleteByData(creatCartDB, items.get(i));
                        }
                    }

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
                    Intent intent = new Intent(ZhaiDou.IntentRefreshCartGoodsCheckTag);
                    mContext.sendBroadcast(intent);
                    //发送刷新购物车数量广播
                    Intent intent1 = new Intent(ZhaiDou.IntentRefreshCartGoodsTag);
                    mContext.sendBroadcast(intent1);
                    //关闭本页面
                    ((MainActivity) getActivity()).popToStack(ShopOrderOkFragment.this);

                    String result = (String) msg.obj;
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject orderObj = jsonObject.optJSONObject("order");
                        int orderId = orderObj.optInt("id");
                        double amount = orderObj.optDouble("amount");
                        double fare = moneyYF;
                        ShopPaymentFragment shopPaymentFragment = ShopPaymentFragment.newInstance(orderId, amount, fare,((mContext.getResources().getInteger(R.integer.timer_countdown)) / 1000), null, 1);
                        ((MainActivity) getActivity()).navigationToFragment(shopPaymentFragment);
                    } catch (Exception e)
                    {

                    }
                    break;
                case 5:
                    if (isOSaleBuy)
                    {
                        mDialog.dismiss();
                        CustomToastDialog.setToastDialog(mContext, "抱歉,您已经购买过零元特卖商品,今天已经不能购买");
                    } else
                    {
                        commit();
                    }
                    break;
                case 6:
                    mDialog.dismiss();
                    String json = msg.obj.toString();
                    String json1 = json.substring(2);
                    CustomToastDialog.setToastDialog(mContext, json1.substring(0, json1.length() - 2));
                    break;
                case 7:
                    mDialog.dismiss();
                    String goodsbuy = msg.obj.toString();
                    ToolUtils.setToastLong(mContext, goodsbuy);
                    break;
                case 8:
                    mDialog.dismiss();
                    String publish = msg.obj.toString();
                    String[] publishStr = new String[]{};
                    publishStr = publish.split(",");

                    for (int i = 0; i < items.size(); i++)
                    {
                        if (items.get(i).sizeId == Integer.valueOf(publishStr[1]))
                        {
                            CustomToastDialog.setToastDialog(mContext, items.get(i).name + "的活动已经下架");
                        }
                    }
                    break;
                case UPDATE_VERFIY:
                    if (isVerify)
                    {
                        verifyView.setVisibility(View.VISIBLE);
                    } else
                    {
                        verifyView.setVisibility(View.GONE);
                    }
                    FetchAddressData();
                    break;
                case UPDATE_VERFIY_SUCCESSS:
                    isVerify=false;
                    ToolUtils.setLog("提交验证码成功");
                    verifyView.setVisibility(View.GONE);
                    judageCommit();
                    break;
                case UPDATE_GETSMS_SUCCESSS:
                    ToolUtils.setLog("获取到验证码");
                    codeTimer();
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
                case R.id.back_btn:
                    ((MainActivity) getActivity()).popToStack(ShopOrderOkFragment.this);
                    break;

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
                            judageCommit();
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
                    ((MainActivity) getActivity()).navigationToFragment(addrManageFragment);
                    addrManageFragment.setAddressListener(new AddrSelectFragment.AddressListener()
                    {
                        @Override
                        public void onDefalueAddressChange(Address maddress)
                        {
                            ToolUtils.setLog(maddress.toString());
                            address = maddress;
                            setYFMoney(address.getPrice());
                            addressPhoneTv.setText("电话：" + address.getPhone());
                            addressNameTv.setText("收件人：" + address.getName());
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
                    ((MainActivity) getActivity()).navigationToFragment(newAddrFragment);
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
                            addressPhoneTv.setText("电话：" + addr.getPhone());
                            addressNameTv.setText("收件人：" + addr.getName());
                            addressinfoTv.setText(address.getProvince() + address.getCity() + address.getArea() + addr.getAddress());
                            ((MainActivity) getActivity()).popToStack(newAddrFragment);
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
            items = (ArrayList<CartItem>) getArguments().getSerializable("goodsList");
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

        loadingView = (LinearLayout) mView.findViewById(R.id.loadingView);
        nullNetView = (LinearLayout) mView.findViewById(R.id.nullNetline);
        nullView = (LinearLayout) mView.findViewById(R.id.nullline);

        reloadBtn = (TextView) mView.findViewById(R.id.nullReload);
        reloadBtn.setOnClickListener(onClickListener);

        reloadNetBtn = (TextView) mView.findViewById(R.id.netReload);
        reloadNetBtn.setOnClickListener(onClickListener);

        mRequestQueue = Volley.newRequestQueue(getActivity());
        backBtn = (TypeFaceTextView) mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);
        titleTv = (TypeFaceTextView) mView.findViewById(R.id.title_tv);
        titleTv.setText(R.string.shop_order_ok_text);

        noFreeTv = (TextView) mView.findViewById(R.id.jsNofree);

        okBtn = (Button) mView.findViewById(R.id.jsOkBtn);
        okBtn.setOnClickListener(onClickListener);

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
        moneyTv = (TextView) mView.findViewById(R.id.jsPriceTotalTv);
        moneyYfTv = (TextView) mView.findViewById(R.id.jsPriceYfTv);
        moneyTotalTv = (TextView) mView.findViewById(R.id.jsTotalMoney);

        addressinfoTv = (TextView) mView.findViewById(R.id.jsAddressinfoTv);
        addressNameTv = (TextView) mView.findViewById(R.id.jsAddressNameTv);
        addressPhoneTv = (TextView) mView.findViewById(R.id.jsAddressPhoneTv);

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

        creatCartDB = new CreatCartDB(mContext);

        initDataView();
        initData();
    }

    private void initData()
    {
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
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
        totalMoney = Double.parseDouble(df.format(money + moneyYF));
        moneyTotalTv.setText("￥" + ToolUtils.isIntPrice("" + totalMoney));
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
            CartItem cartItem = items.get(i);
            num = num + cartItem.num;
            money = money + cartItem.num * cartItem.currentPrice;
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

            final CartItem cartItem = items.get(position);

            itemName.setText(cartItem.name);
            itemSize.setText(cartItem.size);
            itemCurrentPrice.setText("￥" + ToolUtils.isIntPrice("" + cartItem.currentPrice));
            itemFormalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            itemFormalPrice.setText("￥" + ToolUtils.isIntPrice("" + cartItem.formalPrice));
            itemNum.setText("" + cartItem.num);
            ToolUtils.setImageCacheUrl(cartItem.imageUrl, itemImage);

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
                FetchOSaleData(5);//判断零元特卖是否已经当天购买过
            } else
            {
                commit();
            }
        } else
        {
            ToolUtils.setToast(mContext, "抱歉,您未填写收货地址");
        }

    }

    /**
     * 提交订单接口
     */
    private void commit()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if (mDialog!=null)
                mDialog.dismiss();
                String result = FetchRequset();
                if (result != null && result.length() > 10)
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        ToolUtils.setLog(jsonObject.toString());
                        int status = jsonObject.optInt("status");
                        if (status == 201)
                        {
                            JSONObject orderObj = jsonObject.optJSONObject("order");
                            int id = orderObj.optInt("id");
                            String number = orderObj.optString("number");
                            double amount = orderObj.optDouble("amount");
                            int count = orderObj.optInt("count");

                            Message message = new Message();
                            message.what = 4;
                            message.obj = result;
                            handler.sendMessage(message);
                        } else if (status == 400)
                        {
                            String errorArr = jsonObject.optJSONObject("message").optString("order_items.merchandise_id");
                            if (errorArr.length() > 3)
                            {
                                Message message = new Message();
                                message.what = 6;
                                message.obj = errorArr;
                                handler.sendMessage(message);
                            }
                        } else if (status == 401||status==402)
                        {
                            String errorArr = jsonObject.optString("message");
                            Message message = new Message();
                            message.what = 7;
                            message.obj = errorArr;
                            handler.sendMessage(message);
                        }
                        else if (status == 403)
                        {
                            String errorArr = jsonObject.optJSONObject("user").optString("phone");
                            Message message = new Message();
                            message.what = 6;
                            message.obj = errorArr;
                            handler.sendMessage(message);
                        }
                        else
                        {
                            handler.sendEmptyMessage(3);
                        }

                    } catch (Exception e)
                    {
                    }

                } else
                {
                    handler.sendEmptyMessage(3);
                }
            }
        }).start();

    }

    /**
     * 请求订单
     * @return
     */
    private String FetchRequset()
    {
        String result = null;
        BufferedReader in = null;
        try
        {
            // 定义HttpClient
            HttpClient client = new DefaultHttpClient();

            // 实例化HTTP方法
            HttpPost request = new HttpPost(ZhaiDou.orderCommitUrl);
            request.addHeader("SECAuthorization", token);
            request.addHeader("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
            // 创建名/值组列表
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("sale_order[receiver_id]", "" + address.getId()));
            params.add(new BasicNameValuePair("sale_order[node]", bzInfo_Str));
            params.add(new BasicNameValuePair("phone", phone_Str));
            params.add(new BasicNameValuePair("vcode", code_Str));
            for (int i = 0; i < items.size(); i++)
            {
                params.add(new BasicNameValuePair("sale_order[order_items_attributes[" + i + "][merchandise_id]]", items.get(i).id + ""));
                params.add(new BasicNameValuePair("sale_order[order_items_attributes[" + i + "][specification_id]]", items.get(i).sizeId + ""));
                params.add(new BasicNameValuePair("sale_order[order_items_attributes[" + i + "][count]]", items.get(i).num + ""));
            }
            // 创建UrlEncodedFormEntity对象
            UrlEncodedFormEntity formEntiry = new UrlEncodedFormEntity(
                    params, HTTP.UTF_8);
            request.setEntity(formEntiry);
            // 执行请求
            HttpResponse response = client.execute(request);

            in = new BufferedReader(new InputStreamReader(response.getEntity()
                    .getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null)
            {
                sb.append(line + NL);
            }
            in.close();
            result = sb.toString();
            return result;

        } catch (Exception e)
        {

        } finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 请求收货地址信息
     */
    private void FetchAddressData()
    {
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.ORDER_RECEIVER_URL, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                ToolUtils.setLog(jsonObject.toString());
                if (jsonObject != null)
                {
                    JSONArray receivers = jsonObject.optJSONArray("receivers");
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
                        handler.sendEmptyMessage(2);
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

    /*
     * 判断是否已经验证手机号码
     */
    public void FetchVerifyData()
    {
        String url = ZhaiDou.accountOrPhone;
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
                        isVerify = false;
                    }
                    else
                    {
                        isVerify =true;
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
        String url = ZhaiDou.orderGetSMS+phone_Str;
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
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
                    String time=jsonObject.optString("time");
                    if (flag==201)
                    {
                        handler.sendEmptyMessage(UPDATE_GETSMS_SUCCESSS);
                    }
                    else
                    {
                        if (msg.equals("no"))
                        {
                            msg="验证码十分钟内有效,请使用之前下发的验证码";
                        }
                        ToolUtils.setToastLong(mContext,msg);
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
                    Log.i("isOSaleBuy---------------------->", "" + isOSaleBuy);
                }
                if (i == 5)
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
                headers.put("SECAuthorization", token);
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        mRequestQueue.add(request);
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
