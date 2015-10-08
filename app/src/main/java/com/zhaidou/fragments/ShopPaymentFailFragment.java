package com.zhaidou.fragments;


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

import com.alipay.sdk.app.PayTask;
import com.android.volley.AuthFailureError;
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
import com.zhaidou.model.CartItem;
import com.zhaidou.model.Order;
import com.zhaidou.model.OrderItem;
import com.zhaidou.model.Receiver;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
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

    private long mOrderId;
    private double mAmount;
    private double mFare;
    private long mTimeStamp;
    private Order mOrder;
    private Context mContext;

    private TypeFaceTextView backBtn, titleTv;
    private TypeFaceTextView timeInfoTv;
    private Timer mTimer;

    private CheckBox cb_weixin;
    private CheckBox cb_zhifubao;

    private ArrayList<CartItem> items;
    private int num = 0;
    private double total = 0;
    private double fare = 0;
    private double amount = 0;
    private TextView tv_total, tv_fare, tv_amount;
    private int mCheckPosition = 0;
    private Button bt_pay;
    private String token;
    private IWXAPI api;
    private RequestQueue mRequestQueue;
    private List<OrderItem> orderItems = new ArrayList<OrderItem>();
    private View rootView;
    private static final int SDK_PAY_FLAG = 1;

    private static final int SDK_CHECK_FLAG = 2;
    private static final int UPDATE_FEE_DETAIL=3;
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
                        ShopPaymentSuccessFragment shopPaymentSuccessFragment = ShopPaymentSuccessFragment.newInstance(mOrderId, 0, mOrder);
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
                    Order order=(Order)msg.obj;
                    double amount=order.getAmount();
                    List<OrderItem> items = order.getOrderItems();
                    double price=0;
                    for (OrderItem item:items)
                    {
                        price+=item.getPrice()*item.getCount();
                    }
                    DecimalFormat df = new DecimalFormat("###.00");
                    price = Double.parseDouble(df.format(price));
                    tv_amount.setText("￥" + ToolUtils.isIntPrice("" + price));
                    tv_fare.setText("￥" + Integer.parseInt(df.format((amount-price))));
                    tv_total.setText("￥" + ToolUtils.isIntPrice("" +amount));
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
                    payment();
                    break;
            }
        }
    };

    public static ShopPaymentFailFragment newInstance(long orderId, double amount, double fare, long timer, Order order) {
        ShopPaymentFailFragment fragment = new ShopPaymentFailFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_FARE, fare);
        args.putLong(ARG_ORDERID, orderId);
        args.putDouble(ARG_AMOUNT, amount);
        args.putLong(ARG_TIMER, timer);
        args.putSerializable(ARG_ORDER, order);
        fragment.setArguments(args);
        return fragment;
    }

    public ShopPaymentFailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mOrderId = getArguments().getLong(ARG_ORDERID);
            mAmount = getArguments().getDouble(ARG_AMOUNT);
            mFare = getArguments().getInt(ARG_FARE);
            mTimeStamp = getArguments().getLong(ARG_TIMER);
            mOrder = (Order) getArguments().getSerializable(ARG_ORDER);
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
        FetchDetail();
        mTimer = new Timer();
        mTimer.schedule(new MyTimer(), 1000, 1000);

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
                    }
                }
            });
        }
    }

    private void stopView() {
        mTimeStamp = 0;
//        paymentView.setVisibility(View.GONE);
//        loseView.setVisibility(View.VISIBLE);
        bt_pay.setClickable(false);
        bt_pay.setBackgroundResource(R.drawable.btn_no_click_selector);
    }

    /**
     * 付款
     */
    private void payment() {
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.URL_ORDER_LIST + "/" + mOrderId + "/order_payment?payment_id=" + mCheckPosition, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.i("jsonObject--------->", jsonObject.toString());
                if (jsonObject != null) {
                    int status = jsonObject.optInt("status");
                    if (status == 201) {
                        String appId = jsonObject.optString("appId");
                        String timeStamp = jsonObject.optString("timeStamp");
                        String signType = jsonObject.optString("signType");
                        String mpackage = jsonObject.optString("package");
                        String nonceStr = jsonObject.optString("nonceStr");
                        String prepayId = jsonObject.optString("prepayId");
                        int order_id = jsonObject.optInt("order_id");
                        String paySign = jsonObject.optString("paySign");
                        if (mCheckPosition == 0) {
                            PayReq request = new PayReq();
                            request.appId = appId;
                            request.partnerId = "1254327401";
                            request.prepayId = prepayId;
                            request.packageValue = mpackage;
                            request.nonceStr = nonceStr;
                            request.timeStamp = timeStamp;
                            request.sign = paySign;
                            api.sendReq(request);
                        } else if (mCheckPosition == 1) {
                            final String url = jsonObject.optString("url");
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    pay(url);
                                }
                            }, 0);

                        }
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
                Log.i("----->", "商户订单号重复或生成错误");
                break;
            case 0://支付成功
                Log.i("----->", "支付成功");
                ShopPaymentSuccessFragment shopPaymentSuccessFragment = ShopPaymentSuccessFragment.newInstance(mOrderId, mAmount + mFare, mOrder);
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

    private void FetchDetail() {
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.URL_ORDER_LIST + "/" + mOrderId, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.i("jsonObject--------->", jsonObject.toString());
                if (jsonObject != null) {
                    JSONObject orderObj = jsonObject.optJSONObject("order");
                    amount = orderObj.optDouble("amount");
                    int id = orderObj.optInt("id");
                    String status = orderObj.optString("status");
                    String created_at_for = orderObj.optString("created_at_for");
                    String receiver_address = orderObj.optString("receiver_address");
                    String created_at = orderObj.optString("created_at");
                    String status_ch = orderObj.optString("status_ch");
                    String number = orderObj.optString("number");
                    String receiver_phone = orderObj.optString("receiver_phone");
                    String deliver_number = orderObj.optString("deliver_number");
                    String receiver_name = orderObj.optString("receiver_name");

                    JSONObject receiverObj = orderObj.optJSONObject("receiver");
                    int receiverId = receiverObj.optInt("id");
                    String address = receiverObj.optString("address");
                    String phone = receiverObj.optString("phone");
                    String name = receiverObj.optString("name");
                    String city_name = receiverObj.optString("city_name");
                    String parent_name = receiverObj.optString("parent_name");
                    String provider_name = receiverObj.optString("provider_name");
                    Receiver receiver = new Receiver(receiverId, address, parent_name, city_name, provider_name, phone, name);


                    JSONArray order_items = orderObj.optJSONArray("order_items");
                    if (order_items != null && order_items.length() > 0) {
                        for (int i = 0; i < order_items.length(); i++) {
                            JSONObject item = order_items.optJSONObject(i);
                            int itemId = item.optInt("id");
                            double itemPrice = item.optDouble("price");
                            int count = item.optInt("count");
                            double cost_price = item.optDouble("cost_price");
                            String merchandise = item.optString("merchandise");
                            String specification = item.optString("specification");
                            int merchandise_id = item.optInt("merchandise_id");
                            String merch_img = item.optString("merch_img");
                            int sale_cate = item.optInt("sale_cate");
                            OrderItem orderItem = new OrderItem(itemId, itemPrice, count, cost_price, merchandise, specification, merchandise_id, merch_img);
                            orderItem.setSale_cate(sale_cate);
                            orderItems.add(orderItem);
                        }
                    }
                    Order order = new Order("", id, number, amount, status, status_ch, created_at_for, created_at, receiver, orderItems, receiver_address, receiver_phone, deliver_number, receiver_name);
                    Message message = new Message();
                    message.obj = order;
                    message.what = UPDATE_FEE_DETAIL;
                    mHandler.sendMessage(message);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (getActivity() != null)
                    Toast.makeText(mContext, "加载失败", Toast.LENGTH_SHORT).show();
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
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.shop_payment_fail_text));
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.shop_payment_fail_text));
    }
}
