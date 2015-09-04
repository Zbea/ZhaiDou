package com.zhaidou.alipay;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
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
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.model.CartItem;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class PayDemoActivity extends FragmentActivity {

    private int mOrderId;
    private int mAmount;
    private View mView;
    private Context mContext;

    private long initTime = 15 * 60 * 1000;

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

    //商户PID
    public static final String PARTNER = "2088411949734190";
    //商户收款账号
    public static final String SELLER = "pay@zhaidou.com";
    public static final String RSA_PRIVATE ="MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALESAvyOZuWnVgO+E/qe1Scxz4n1hP3+pmfvLrZXs6rrfQNaxC5FzngRXAIIpAuffacXGqV8I2scRbQ+P1ikeCf6QL/C92t9VWMiKC0NJzTLr0uI33szRZJDwOxLhV9TTwD0/3iEsP2ZaDRHfhreOaWg82SKA+WGcH9hnYMnXXffAgMBAAECgYBPjj0jRcO7aOx/b/ZAAv7Xxkxtuo7PI4uWZONC77J8l6MqT6yW8awrS/lfvIwf4L+uw/Wn5ldvd1tl+RWy+Oo2pIxy8MdoqvxJkw33cUCcbXwUAaW0+YrNFlvALLD1mp8oi2ItS8yfsjUKWwu2PAUxnvJSTxUvArBGcB5PRUGb+QJBANySRRmSZsXleDDQtlpLKNaxhUnoUKt4HKPwu4BfCV3lXlslRuORo+87ax5e01+xg4W+qa1F/EZqLuwWZ2x/u50CQQDNgwNBACdFwKWhOpPJmCgbn7L7Z9Hh6253syFJuiJaiWKFK9ozcjXLRqVkXRwc1x0+uOER1TgrEbhOvshN7d6rAkB9N5h81Oz9SbD63XG4PtTXVP4bIASz40M3GpIHZWx23qC7U6UzydlsFapRGnoa6DaHNd8zm/iErQEoS+u436bNAkEAow8jYsybXHXZNQ7EOfzXPeu9WEpstiNV9/WSIOxl244MNHux2oXw9sOr8PELoDpyAtUwBzU1Jr8djKVFSsfElwJAJ9fiHrOBx7SAXkkOZKWTi4PLcNOxIsty1aYy7IPzNV18CGWsvR5Z87DhkQUJrRsmk7Td7dxNqV17dFnFtl/9DA==";
    //商户私钥，pkcs8格式
//    public static final String RSA_PRIVATE = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAN8Q5H/uOFnJWK25x3Zf/dL8nntOiWo6WmZXICjhAiXkDGdRjApet0p47tYaRBT9QhoDVCkUUF6BerSuTOw2GUCsr5JrF1Naqn6R4n2J7wBu49NNPdy5hop3YSH8E29hEbnL+SG1qk8t+1piJP9HaY83MlDLs3hT2TAjG+0f6VZNAgMBAAECgYBNGY2hGjmn7wwTn+7tX2hgEKjGffLJILo2PU8EUiFn71bKL0l0HCDGQN5sak+14YODcjsYp8jPoXlEEirMSjaXmubTDlkir0I42XO8X1QtUI2ai1aTGscyq8umn7SA5gVBXbcfv2wkgSDJWr43uUjXRLnzSRXvjoP+uYB+mFRYAQJBAO8By+8xBXBqezJToM54SFu2ojh9/Qs0S5en0G9rpJlVYMBjEZwVz5XaT4MgnFJMD3isjpqYIpE/ljHUHogA0M0CQQDu7PNnvFd+g7rphPEKS9tTvhYVJMpfrrhaYtAK1Z+3QaqbprouJmDLd9rHEBy2onFZ4XsYGB4+D9hx8d6tZJuBAkEAn3x/bTk0+/LSCp8raxtwjWKtlSzdMiDPYH+m4vLdf0Qtr7NsCM+1GbX34PRd27zNhiT/c8GZL3tS6iU1ymNg8QJAHU3GlMSO3p99f0Kk5aRkJCM+Rh2bDJ07UyqnZYzJ6AoPyMNsNljSqmHq3VtmiifmSyXSmSPsIaSD4YXOz+l1AQJAbVKe4gtJZpfu2tBUlpkklw0hHsJJ95dPJbj6ePsYx3pij3XZbUPPSLU/WC2Rqm/S9QGu0Sp73tooNZsNhiEvvQ==";
    //支付宝公钥
    public static final String RSA_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";


    private static final int SDK_PAY_FLAG = 1;

    private static final int SDK_CHECK_FLAG = 2;
    private IWXAPI api;

    private String token;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    PayResult payResult = new PayResult((String) msg.obj);

                    // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                    String resultInfo = payResult.getResult();

                    String resultStatus = payResult.getResultStatus();

                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        Toast.makeText(PayDemoActivity.this, "支付成功",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // 判断resultStatus 为非“9000”则代表可能支付失败
                        // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            Toast.makeText(PayDemoActivity.this, "支付结果确认中",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                            Toast.makeText(PayDemoActivity.this, "支付失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    finish();
                    break;
                }
                case SDK_CHECK_FLAG: {
                    Toast.makeText(PayDemoActivity.this, "检查结果为：" + msg.obj,
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shop_payment_page);
        mContext = this;
        api = WXAPIFactory.createWXAPI(mContext, null);
        api.registerApp("wxce03c66622e5b243");
        mOrderId = getIntent().getIntExtra("id", 0);
        mAmount = getIntent().getIntExtra("amount", 0);
        long time=getIntent().getLongExtra("time",-1);
        if (time>-1)
            initTime=time;
        Log.i("mOrderId-----onCreate-------->", mOrderId + "");
        Log.i("mOrder.getOrderId()---------onCreate---->", mAmount + "");
        initView();
    }

    /**
     * 初始化数据
     */
    private void initView() {
        mRequestQueue = Volley.newRequestQueue(mContext);
        backBtn = (TypeFaceTextView) findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);
        titleTv = (TypeFaceTextView) findViewById(R.id.title_tv);
        titleTv.setText(R.string.shop_payment_text);

        timeInfoTv = (TypeFaceTextView) findViewById(R.id.failTimeInfo);
        paymentBtn = (Button) findViewById(R.id.paymentBtn);
        paymentBtn.setOnClickListener(onClickListener);

        paymentView = (LinearLayout) findViewById(R.id.paymentView);
        loseView = (LinearLayout) findViewById(R.id.loseView);

        cb_weixin = (CheckBox) findViewById(R.id.cb_weixin);
        cb_weixin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    cb_zhifubao.setChecked(false);
                    mCheckPosition = 0;
                }
            }
        });
        cb_zhifubao = (CheckBox) findViewById(R.id.cb_zhifubao);
        cb_zhifubao.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    cb_weixin.setChecked(false);
                    mCheckPosition = 1;
                }
            }
        });

        token=(String) SharedPreferencesUtil.getData(PayDemoActivity.this,"token","");
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

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.back_btn:
                    finish();
                    break;
                case R.id.paymentBtn:
                    payment();
                    break;
            }
        }
    };

    private void payment() {
//        pay("");
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.URL_ORDER_LIST+"/" + mOrderId + "/order_payment?payment_id=" + mCheckPosition, new Response.Listener<JSONObject>() {
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
                            },0);
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
                map.put("SECAuthorization",token);
                return map;
            }
        };
        mRequestQueue.add(request);
    }

    /**
     * call alipay sdk pay. 调用SDK支付
     */
    public void pay(final String url) {
        // 订单
        String orderInfo = getOrderInfo("测试的商品", "该测试商品的详细描述", "0.01");

        // 对订单做RSA 签名
        String sign = sign(orderInfo);
        try {
            // 仅需对sign 做URL编码
            sign = URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // 完整的符合支付宝参数规范的订单信息
        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
                + getSignType();

        Log.i("url------------->", url.toString());
        final String test =  "partner=\"2088411949734190\"&seller_id=\"pay@zhaidou.com\"&out_trade_no=\"20150812844358\"&subject=\"普通特卖第一期\"&body=\"普通特卖第一期\"&total_fee=\"0.01\"&notify_url=\"http://notify.msp.hk/notify.htm\"&service=\"mobile.securitypay.pay\"&payment_type=\"1\"&_input_charset=\"utf-8\"&it_b_pay=\"30m\"&return_url=\"m.alipay.com\"&sign=\"K6IP%2FptXwxGsuGnfHro1haIFPzdR1674Cp5Ayq7y1sCckhxvBriaeUs%2BmALS4IrhkzflYKt7UBqtdBW7G5f50iPF3SP9EaM5qJlv7d%2FfHsX6EYMTvGQoCOp0QdcuzyAu%2FwhCkgFC1gSUU3Hw%2Fvwi7cWRgZJlvlUkNGaoogcjgyw%3D\"&sign_type=\"RSA\"";
        final String right = "partner=\"2088812267197656\"&seller_id=\"89929355@qq.com\"&out_trade_no=\"081114395620120\"&subject=\"测试的商品\"&body=\"该测试商品的详细描述\"&total_fee=\"0.01\"&notify_url=\"http://notify.msp.hk/notify.htm\"&service=\"mobile.securitypay.pay\"&payment_type=\"1\"&_input_charset=\"utf-8\"&it_b_pay=\"30m\"&return_url=\"m.alipay.com\"&sign=\"W%2FMTYIstSQL%2BvKsnIDM10ECo%2B37flZiFKB9ShZx0vMM6rsu835GQJEsWoJe4Et1gvsFYj6NygQftVgFLjc%2B3yE29Xl7u0fv8zVw2qnryC%2Fibw%2BtSnqdM%2FmbjwqYV7%2FQmljgh9EGm1t8vuii5qZVrwrTwB9D3MZ95%2Frr7yomTJQ0%3D\"&sign_type=\"RSA\"";
        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(PayDemoActivity.this);
                // 调用支付接口，获取支付结果
                String result = alipay.pay(url);
                Log.i("result------------------>", result.toString());

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

    /**
     * check whether the device has authentication alipay account.
     * 查询终端设备是否存在支付宝认证账户
     */
    public void check(View v) {
        Runnable checkRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask payTask = new PayTask(PayDemoActivity.this);
                // 调用查询接口，获取查询结果
                boolean isExist = payTask.checkAccountIfExist();

                Message msg = new Message();
                msg.what = SDK_CHECK_FLAG;
                msg.obj = isExist;
                mHandler.sendMessage(msg);
            }
        };

        Thread checkThread = new Thread(checkRunnable);
        checkThread.start();

    }

    /**
     * get the sdk version. 获取SDK版本号
     */
    public void getSDKVersion() {
        PayTask payTask = new PayTask(this);
        String version = payTask.getVersion();
        Toast.makeText(this, version, Toast.LENGTH_SHORT).show();
    }

    /**
     * create the order info. 创建订单信息
     */
    public String getOrderInfo(String subject, String body, String price) {
        // 签约合作者身份ID
        String orderInfo = "partner=" + "\"" + PARTNER + "\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

        // 商户网站唯一订单号
        orderInfo += "&out_trade_no=" + "\"" + getOutTradeNo() + "\"";

        // 商品名称
        orderInfo += "&subject=" + "\"" + subject + "\"";

        // 商品详情
        orderInfo += "&body=" + "\"" + body + "\"";

        // 商品金额
        orderInfo += "&total_fee=" + "\"" + price + "\"";

        // 服务器异步通知页面路径
        orderInfo += "&notify_url=" + "\"" + "http://notify.msp.hk/notify.htm"
                + "\"";

        // 服务接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 参数编码， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";

        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay=\"30m\"";

        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        orderInfo += "&return_url=\"m.alipay.com\"";

        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";

        return orderInfo;
    }

    /**
     * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
     */
    public String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss",
                Locale.getDefault());
        Date date = new Date();
        String key = format.format(date);

        Random r = new Random();
        key = key + r.nextInt();
        key = key.substring(0, 15);
        return key;
    }

    /**
     * sign the order info. 对订单信息进行签名
     *
     * @param content 待签名订单信息
     */
    public String sign(String content) {
        return SignUtils.sign(content, RSA_PRIVATE);
    }

    /**
     * get the sign type we use. 获取签名方式
     */
    public String getSignType() {
        return "sign_type=\"RSA\"";
    }

}
