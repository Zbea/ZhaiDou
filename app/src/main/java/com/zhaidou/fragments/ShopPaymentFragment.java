package com.zhaidou.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pulltorefresh.PullToRefreshBase;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.alipay.ExternalPartner;
import com.zhaidou.alipay.Keys;
import com.zhaidou.alipay.PayDemoActivity;
import com.zhaidou.alipay.PayResult;
import com.zhaidou.alipay.SignUtils;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.model.CartItem;
import com.zhaidou.utils.MD5Util;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.view.TypeFaceTextView;
import com.zhaidou.wxapi.MD5;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by roy on 15/7/31.
 */
public class ShopPaymentFragment extends BaseFragment {
    private static final String ARG_ORDERID = "orderId";
    private static final String ARG_AMOUNT = "amount";
    private static final String ARG_FARE="fare";

    private int mOrderId;
    private int mAmount;
    private int mFare;
    private View mView;
    private Context mContext;

    private int initTime = 15 * 60 * 1000;

    private final int UPDATE_COUNT_DOWN_TIME = 1001, UPDATE_UI_TIMER_FINISH = 1002, UPDATE_TIMER_START = 1003;

    private TypeFaceTextView backBtn, titleTv;
    private TypeFaceTextView timeInfoTv;
    private Timer mTimer;

    private LinearLayout paymentView;
    private LinearLayout loseView;
    private Button paymentBtn;

    private ArrayList<CartItem> items;
    private int num = 0;
    private double money = 0;
    private double moneyYF = 0;
    private double totalMoney = 0;

    private CheckBox cb_weixin;
    private CheckBox cb_zhifubao;
    private int mCheckPosition = 0;
    RequestQueue mRequestQueue;

    private static final int SDK_PAY_FLAG = 1;

    private static final int SDK_CHECK_FLAG = 2;
    private String token;
    private IWXAPI api;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG:
                    Log.i("SDK_PAY_FLAG------------>", "SDK_PAY_FLAG");
                    PayResult payResult = new PayResult((String) msg.obj);

                    // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                    String resultInfo = payResult.getResult();

                    String resultStatus = payResult.getResultStatus();

                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        Toast.makeText(getActivity(), "支付成功",
                                Toast.LENGTH_SHORT).show();
                        ShopPaymentSuccessFragment shopPaymentSuccessFragment = ShopPaymentSuccessFragment.newInstance(mOrderId, 0);
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
                        ShopPaymentFailFragment shopPaymentFailFragment=ShopPaymentFailFragment.newInstance(mOrderId,mAmount,mFare,initTime);
                        ((MainActivity) getActivity()).navigationToFragment(shopPaymentFailFragment);

                    } else if (TextUtils.equals(resultStatus, "6002")) {
                        // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                        Toast.makeText(getActivity(), "网络连接出错",
                                Toast.LENGTH_SHORT).show();

                    } else if (TextUtils.equals(resultStatus, "6001")) {
                        // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                        Toast.makeText(getActivity(), "支付取消",
                                Toast.LENGTH_SHORT).show();
                        Log.i("支付取消----------->","支付取消");
                        ShopPaymentFailFragment shopPaymentFailFragment=ShopPaymentFailFragment.newInstance(mOrderId,mAmount,mFare,initTime);
                        ((MainActivity) getActivity()).navigationToFragment(shopPaymentFailFragment);
                    }
                    break;
                case SDK_CHECK_FLAG: {
                    Toast.makeText(getActivity(), "检查结果为：" + msg.obj,
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }
    };
    /**
     * 下拉刷新
     */
    private PullToRefreshBase.OnRefreshListener2 refreshListener = new PullToRefreshBase.OnRefreshListener2() {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView) {

        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView) {

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
                    ((MainActivity) getActivity()).popToStack(ShopPaymentFragment.this);
                    break;
                case R.id.paymentBtn:
                    payment();
                    break;
            }
        }
    };

    public static ShopPaymentFragment newInstance(int orderId, int amount,int fare) {
        ShopPaymentFragment fragment = new ShopPaymentFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ORDERID, orderId);
        args.putInt(ARG_AMOUNT, amount);
        args.putInt(ARG_FARE,fare);
        fragment.setArguments(args);
        return fragment;
    }

    public ShopPaymentFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mOrderId = getArguments().getInt(ARG_ORDERID);
            mAmount = getArguments().getInt(ARG_AMOUNT);
            mFare=getArguments().getInt(ARG_FARE);
            items = (ArrayList<CartItem>) getArguments().getSerializable("goodsList");
            num = getArguments().getInt("moneyNum");
            money = getArguments().getDouble("money");
            moneyYF = getArguments().getDouble("moneyYF");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        if (mView == null) {
            mView = inflater.inflate(R.layout.shop_payment_page, container, false);
            mContext = getActivity();
            initView();
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) mView.getParent();
        if (parent != null) {
            parent.removeView(mView);
        }

        return mView;
    }


    /**
     * 初始化数据
     */
    private void initView() {
        api = WXAPIFactory.createWXAPI(mContext, null);
        api.registerApp("wxce03c66622e5b243");
        token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        mRequestQueue = Volley.newRequestQueue(mContext);
        backBtn = (TypeFaceTextView) mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);
        titleTv = (TypeFaceTextView) mView.findViewById(R.id.title_tv);
        titleTv.setText(R.string.shop_payment_text);

        timeInfoTv = (TypeFaceTextView) mView.findViewById(R.id.failTimeInfo);
        paymentBtn = (Button) mView.findViewById(R.id.paymentBtn);
        paymentBtn.setOnClickListener(onClickListener);

        paymentView = (LinearLayout) mView.findViewById(R.id.paymentView);
        loseView = (LinearLayout) mView.findViewById(R.id.loseView);

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

        mTimer = new Timer();
        mTimer.schedule(new MyTimer(), 1000, 1000);

    }

    class MyTimer extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initTime = initTime - 1000;
                    timeInfoTv.setText(new SimpleDateFormat("mm:ss").format(new Date(initTime)));
                    if (initTime == 0) {
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

    /**
     * 支付超时处理
     */
    private void stopView() {
        paymentView.setVisibility(View.GONE);
        loseView.setVisibility(View.VISIBLE);
        paymentBtn.setClickable(false);
        paymentBtn.setBackgroundResource(R.drawable.btn_no_click_selector);
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
                        final String appId = jsonObject.optString("appId");
                        final String timeStamp = jsonObject.optString("timeStamp");
                        final String signType = jsonObject.optString("signType");
                        final String mpackage = jsonObject.optString("package");
                        final String nonceStr = jsonObject.optString("nonceStr");
                        final String prepayId = jsonObject.optString("prepayId");
                        int order_id = jsonObject.optInt("order_id");
                        final String paySign = jsonObject.optString("paySign");
                        if (mCheckPosition == 0) {
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    PayReq request = new PayReq();
                                    request.appId = appId;
                                    request.partnerId = "1254327401";
                                    request.prepayId = prepayId;
                                    request.packageValue = mpackage;
                                    request.nonceStr = nonceStr;
                                    request.timeStamp = timeStamp;
                                    request.sign = paySign;
                                    api.sendReq(request);
                                }
                            }, 0);
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

    public void handleWXPayResult(int result){
        switch (result) {
            case 800://商户订单号重复或生成错误
                Log.i("----->", "商户订单号重复或生成错误");
                break;
            case 0://支付成功
                Log.i("----->", "支付成功");
                ShopPaymentSuccessFragment shopPaymentSuccessFragment = ShopPaymentSuccessFragment.newInstance(mOrderId, 0);
                ((MainActivity)getActivity()).navigationToFragment(shopPaymentSuccessFragment);
                break;
            case -1://支付失败
                Log.i("----->", "支付失败");
                ShopPaymentFailFragment shopPaymentFailFragment=ShopPaymentFailFragment.newInstance(mOrderId,mAmount,mFare,initTime);
                ((MainActivity) getActivity()).navigationToFragment(shopPaymentFailFragment);
                break;
            case -2://取消支付
                Log.i("----->", "取消支付");
                ShopPaymentFailFragment shopPaymentFailFragment1=ShopPaymentFailFragment.newInstance(mOrderId,mAmount,mFare,initTime);
                ((MainActivity) getActivity()).navigationToFragment(shopPaymentFailFragment1);
                break;
            default:
                break;
        }
    }
}
