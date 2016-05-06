package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alipay.sdk.app.PayTask;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.alipay.PayResult;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.CountManage;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.CartGoodsItem;
import com.zhaidou.model.Order;
import com.zhaidou.model.Order1;
import com.zhaidou.model.OrderItem;
import com.zhaidou.model.OrderItem1;
import com.zhaidou.model.Store;
import com.zhaidou.model.User;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by roy on 15/7/24.
 */
public class ShopPaymentFailFragment extends BaseFragment {
    private static final String ARG_ORDERID = "orderId";
    private static final String ARG_AMOUNT = "amount";
    private static final String ARG_FARE = "fare";
    private static final String ARG_TIMER = "timer";
    private static final String ARG_ORDER = "order";

    private long mTimeStamp;
    private Order mOrder;
    private Context mContext;

    private TypeFaceTextView backBtn, titleTv;
    private TypeFaceTextView timeInfoTv;
    private Timer mTimer;

    private CheckBox cb_weixin;
    private CheckBox cb_zhifubao;

    private ArrayList<CartGoodsItem> items;
    private int num = 0;
    private double total = 0;
    private double fare = 0;
    private double amount = 0;
    private TextView tv_total, tv_fare, tv_amount, mCouponMoney, mProductNum;
    private int mCheckPosition = 0;
    private Button bt_pay;
    private String token, userName;
    private int userId;
    private IWXAPI api;
    private RequestQueue mRequestQueue;
    private List<OrderItem> orderItems = new ArrayList<OrderItem>();
    private View rootView;
    private static final int SDK_PAY_FLAG = 1;
    private static final int SDK_CHECK_FLAG = 2;
    private static final int UPDATE_FEE_DETAIL = 3;

    private double payMoney;//订单获取的金额
    private double payGoodsMoney;//商品金额
    private double payYFMoney;//运费
    private long payOrderId;
    private String payOrderCode;
    private Dialog mDialog;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG:
                    PayResult payResult = new PayResult((String) msg.obj);

                    // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                    String resultInfo = payResult.getResult();

                    String resultStatus = payResult.getResultStatus();

                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        Toast.makeText(getActivity(), "支付成功",
                                Toast.LENGTH_SHORT).show();
                        CountManage.getInstance().minus(CountManage.TYPE.TAG_PREPAY);
                        ShopPaymentSuccessFragment shopPaymentSuccessFragment = ShopPaymentSuccessFragment.newInstance(payOrderCode, 0, payMoney + "");
                        ((MainActivity) getActivity()).navigationToFragment(shopPaymentSuccessFragment);
                        // 判断resultStatus 为非“9000”则代表可能支付失败
                        // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                    } else if (TextUtils.equals(resultStatus, "8000")) {
                        Toast.makeText(getActivity(), "支付结果确认中",
                                Toast.LENGTH_SHORT).show();

                    } else if (TextUtils.equals(resultStatus, "4000")) {
                        // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                        Toast.makeText(getActivity(), "支付失败",
                                Toast.LENGTH_SHORT).show();
                    } else if (TextUtils.equals(resultStatus, "6002")) {
                        // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                        Toast.makeText(getActivity(), "网络连接出错",
                                Toast.LENGTH_SHORT).show();

                    } else if (TextUtils.equals(resultStatus, "6001")) {
                        // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                        Toast.makeText(getActivity(), "支付取消",
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case SDK_CHECK_FLAG: {
                    Toast.makeText(getActivity(), "检查结果为：" + msg.obj,
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                case UPDATE_FEE_DETAIL:
                    Order1 order = (Order1) msg.obj;
                    System.out.println("order = " + order);
                    tv_amount.setText("￥" + order.itemTotalAmount);
                    tv_fare.setText("￥" + order.deliveryFee);
                    tv_total.setText("￥" + order.orderPayAmount);
                    mCouponMoney.setText("￥" + (Double.parseDouble(order.discountAmount)>0?"-"+order.discountAmount:order.discountAmount));
                    List<Store> childOrderPOList = order.childOrderPOList;
                    int num=0;
                    for (Store store:childOrderPOList){
                        List<OrderItem1> orderItemPOList = store.orderItemPOList;
                        for(OrderItem1 orderItem1:orderItemPOList){
                            num+= orderItem1.quantity;
                        }
                    }
                    mProductNum.setText(num+ "");
                    mTimer = new Timer();
                    mTimer.schedule(new MyTimer(), 1000, 1000);
                    break;
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
                    ((MainActivity) getActivity()).popToStack(ShopPaymentFailFragment.this);
                    break;
                case R.id.bt_pay:
                    mDialog.show();
                    payment();
                    break;
            }
        }
    };

    public static ShopPaymentFailFragment newInstance(long orderId, double amount, double fare, long timer, String orderCode) {
        ShopPaymentFailFragment fragment = new ShopPaymentFailFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_FARE, fare);
        args.putLong(ARG_ORDERID, orderId);
        args.putDouble(ARG_AMOUNT, amount);
        args.putLong(ARG_TIMER, timer);
        args.putSerializable(ARG_ORDER, orderCode);
        fragment.setArguments(args);
        return fragment;
    }

    public ShopPaymentFailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            payOrderId = getArguments().getLong(ARG_ORDERID);
            payMoney = getArguments().getDouble(ARG_AMOUNT);
            payYFMoney = getArguments().getDouble(ARG_FARE);
            mTimeStamp = getArguments().getLong(ARG_TIMER);
            payOrderCode = (String) getArguments().getSerializable(ARG_ORDER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != rootView) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent) {
                parent.removeView(rootView);
            }
        } else {
            rootView = inflater.inflate(R.layout.shop_payment_fail_page, container, false);
            mContext = getActivity();
            initView(rootView);// 控件初始化
        }
        return rootView;
    }

    /**
     * 初始化数据
     */
    private void initView(View mView) {
        api = WXAPIFactory.createWXAPI(mContext, null);
        api.registerApp("wxce03c66622e5b243");
        mRequestQueue = Volley.newRequestQueue(getActivity());
        token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        userName = (String) SharedPreferencesUtil.getData(getActivity(), "nickName", "");
        userId = (Integer) SharedPreferencesUtil.getData(mContext, "userId", -1);
        backBtn = (TypeFaceTextView) mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);
        titleTv = (TypeFaceTextView) mView.findViewById(R.id.title_tv);
        titleTv.setText(R.string.shop_payment_fail_text);

        bt_pay = (Button) mView.findViewById(R.id.bt_pay);
        bt_pay.setOnClickListener(onClickListener);
        timeInfoTv = (TypeFaceTextView) mView.findViewById(R.id.failTimeInfo);
        tv_amount = (TextView) mView.findViewById(R.id.failPriceTotalTv);
        tv_fare = (TextView) mView.findViewById(R.id.failPriceYfTv);
        tv_total = (TextView) mView.findViewById(R.id.failTotalMoney);
        mCouponMoney = (TextView) mView.findViewById(R.id.couponMoney);
        mProductNum = (TextView) mView.findViewById(R.id.jsTotalNum);

        cb_weixin = (CheckBox) mView.findViewById(R.id.cb_weixin);
        cb_weixin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    cb_zhifubao.setChecked(false);
                    mCheckPosition = 0;
                }
            }
        });
        cb_zhifubao = (CheckBox) mView.findViewById(R.id.cb_zhifubao);
        cb_zhifubao.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    cb_weixin.setChecked(false);
                    mCheckPosition = 1;
                }
            }
        });

        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "");
        FetchOrderDetail();

    }

    class MyTimer extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTimeStamp = mTimeStamp - 1;
                    timeInfoTv.setText(new SimpleDateFormat("mm:ss").format(new Date(mTimeStamp * 1000)));
                    if (mTimeStamp <= 0) {
                        if (mTimer != null) {
                            mTimer.cancel();
                            timeInfoTv.setText("00:00");
                            stopView();
                        }
                        CountManage.getInstance().minus(CountManage.TYPE.TAG_PREPAY);
                    }
                }
            });
        }
    }

    /**
     * 获取订单详情
     */
    private void FetchOrderDetail() {
        User user = SharedPreferencesUtil.getUser(mContext);
        Map<String, String> params = new HashMap<String, String>();//28129
        params.put("userId", user.getId() + "");//64410//16665//29650//mUserId
        params.put("clientType", "ANDROID");
        params.put("clientVersion", "45");
        params.put("businessType", "01");
        params.put("type", "0");
        params.put("orderCode", payOrderCode);
        System.out.println("payOrderCode = " + payOrderCode);
        ZhaiDouRequest request = new ZhaiDouRequest(mContext, Request.Method.POST, ZhaiDou.URL_ORDER_LIST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                mDialog.dismiss();
                if (jsonObject != null) {
                    int status = jsonObject.optInt("status");
                    if (status == 200) {
                        JSONArray dataObject = jsonObject.optJSONArray("data");
                        List<Order1> orders = JSON.parseArray(dataObject.toString(), Order1.class);
                        Message message = new Message();
                        message.obj = orders.get(0);
                        System.out.println("store = " + orders);
                        message.what = UPDATE_FEE_DETAIL;
                        mHandler.sendMessage(message);
//                        if (dataObject != null)
//                            payMoney = dataObject.optDouble("orderTotalAmount")!=0?dataObject.optDouble("orderTotalAmount"):payMoney;
//                        payGoodsMoney = dataObject.optDouble("itemTotalAmount");
//                        payYFMoney = payMoney - payGoodsMoney;
//                        payOrderId = dataObject.optLong("orderId")!=0?dataObject.optLong("orderId"):payOrderId;
//                        payOrderCode = dataObject.optString("orderCode")!=""?dataObject.optString("orderCode"):payOrderCode;
//                        mTimeStamp = dataObject.optInt("orderRemainingTime");
//                        Message message=new Message();
//                        message.obj=store;
//                        message.what=UPDATE_FEE_DETAIL;
//                        mHandler.sendMessage(message);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mDialog.dismiss();
                mHandler.sendEmptyMessage(UPDATE_FEE_DETAIL);
                if (mContext != null)
                    ToolUtils.setToast(mContext, R.string.loading_fail_txt);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("SECAuthorization", token);
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        mRequestQueue.add(request);
    }

    private void stopView() {
        mTimeStamp = 0;
        bt_pay.setClickable(false);
        bt_pay.setBackgroundResource(R.drawable.btn_no_click_selector);
    }

    private void payment() {
        JSONObject json = null;
        JSONObject maps = new JSONObject();
        try {
            json = new JSONObject();
            json.put("userName", userName);
            json.put("cashAmount", payMoney + "");
            json.put("orderId", payOrderId + "");
            json.put("userId", userId + "");
            json.put("orderCode", payOrderCode);
            if (mCheckPosition == 0) {
                json.put("channelCode", "WXMALLANDROID");
            } else {
                json.put("channelCode", "ZFBMALLANDROID");
            }
            json.put("notifyUrl", "");
            json.put("returnUrl", "");
            maps.put("businessType ", "01");
            maps.put("clientType ", "ANDROID");
            maps.put("data ", json);
            maps.put("version ", "1.0.0");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.CommitPaymentUrl, maps, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (jsonObject != null) {
                    int status = jsonObject.optInt("status");
                    if (status == 200) {
                        JSONObject object = jsonObject.optJSONObject("data");
                        if (object != null) {
                            if (mCheckPosition == 0) {
                                if (api.isWXAppInstalled()) {
                                    final String appId = object.optString("appid");
                                    final String timeStamp = object.optString("timestamp");
                                    final String signType = object.optString("signType");
                                    final String mpackage = object.optString("packageValue");
                                    final String nonceStr = object.optString("nonceString");
                                    final String prepayId = object.optString("prepayId");
                                    final String paySign = object.optString("sign");
                                    final String partnerId = object.optString("partnerId");
                                    PayReq request = new PayReq();

                                    request.appId = appId;
                                    request.partnerId = partnerId;
                                    request.prepayId = prepayId;
                                    request.packageValue = mpackage;
                                    request.nonceStr = nonceStr;
                                    request.timeStamp = timeStamp;
                                    request.sign = paySign;
                                    api.sendReq(request);
                                    ToolUtils.setLog("request:" + request.checkArgs());
                                    ToolUtils.setLog("api:" + api.sendReq(request));

                                } else {
                                    ShowToast("没有安装微信客户端哦");
                                }

                            } else if (mCheckPosition == 1) {
                                final String url = object.optString("notifyUrl");
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        pay(url);
                                    }
                                }, 0);
                            }
                        } else {
                            ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                        }
                    } else {
                        ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("SECAuthorization", token);
                map.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return map;
            }
        };
        mRequestQueue.add(request);
    }

    public void pay(final String url) {
        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(getActivity());
                // 调用支付接口，获取支付结果
                String result = alipay.pay(url);

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    public void handleWXPayResult(int result) {
        switch (result) {
            case 800://商户订单号重复或生成错误
                break;
            case 0://支付成功
                Log.i("----->", "支付成功");
                CountManage.getInstance().minus(CountManage.TYPE.TAG_PREPAY);
                ShopPaymentSuccessFragment shopPaymentSuccessFragment = ShopPaymentSuccessFragment.newInstance(payOrderCode, 0, payMoney + "");
                ((MainActivity) getActivity()).navigationToFragment(shopPaymentSuccessFragment);
                break;
            case -1://支付失败
                Log.i("----->", "支付失败");
                Toast.makeText(getActivity(), "支付失败", Toast.LENGTH_SHORT).show();
                break;
            case -2://取消支付
                Log.i("----->", "取消支付");
                Toast.makeText(getActivity(), "取消支付", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.shop_payment_fail_text));
    }

    public void onPause() {
        mDialog.dismiss();
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.shop_payment_fail_text));
    }
}
