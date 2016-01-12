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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.zhaidou.model.Order;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by roy on 15/7/31.
 */
public class ShopPaymentFragment extends BaseFragment
{
    private static final String ARG_ORDERID = "orderId";
    private static final String ARG_AMOUNT = "amount";
    private static final String ARG_FARE = "fare";
    private static final String ARG_TIME = "timeLeft";
    private static final String ARG_ORDER = "order";

    private Order mOrder=new Order();
    private View mView;
    private Context mContext;

    private long initTime = 0;
    private final static int UPDATE_ORDER_DETAILS = 1001;

    private TypeFaceTextView backBtn, titleTv;
    private TypeFaceTextView timeInfoTv;
    private Timer mTimer;

    private LinearLayout paymentView;
    private LinearLayout loseView;
    private Button paymentBtn;

    private CheckBox cb_weixin;
    private CheckBox cb_zhifubao;
    private int mCheckPosition = 0;
    RequestQueue mRequestQueue;
    private boolean isTimerStart = false;
    private Order.OrderListener orderListener;

    private static final int SDK_PAY_FLAG = 1;

    private static final int SDK_CHECK_FLAG = 2;
    private String token,userName;
    private int userId;
    private IWXAPI api;
    private boolean isSuccess;
    private double payMoney;//订单获取的金额
    private long payOrderId;
    private String payOrderCode;
    public Dialog mDialog;

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            paymentBtn.setClickable(true);
            switch (msg.what)
            {
                case SDK_PAY_FLAG:
                    Log.i("SDK_PAY_FLAG------------>", "SDK_PAY_FLAG");
                    PayResult payResult = new PayResult((String) msg.obj);
                    // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                    String resultInfo = payResult.getResult();

                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000"))
                    {
                        isSuccess = true;
                        notificationPaySuccess();
                        CountManage.getInstance().minus(CountManage.TYPE.TAG_PREPAY);
                        ShopPaymentSuccessFragment shopPaymentSuccessFragment = ShopPaymentSuccessFragment.newInstance(payOrderCode, 0, payMoney+"");
                        ((MainActivity) getActivity()).navigationToFragment(shopPaymentSuccessFragment);
//                        ((MainActivity) getActivity()).popToStack(ShopPaymentFragment.this);
                        // 判断resultStatus 为非“9000”则代表可能支付失败
                        // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                    } else if (TextUtils.equals(resultStatus, "8000"))
                    {
                        Toast.makeText(getActivity(), "支付结果确认中",

                                Toast.LENGTH_SHORT).show();

                    } else if (TextUtils.equals(resultStatus, "4000"))
                    {
                        // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                        ShopPaymentFailFragment shopPaymentFailFragment = ShopPaymentFailFragment.newInstance(payOrderId, payMoney, 0, initTime, payOrderCode);
                        ((MainActivity) getActivity()).navigationToFragment(shopPaymentFailFragment);
                    } else if (TextUtils.equals(resultStatus, "6002"))
                    {
                        // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                        Toast.makeText(getActivity(), "网络连接出错",
                                Toast.LENGTH_SHORT).show();
                    } else if (TextUtils.equals(resultStatus, "6001"))
                    {
                        // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                        Toast.makeText(getActivity(), "支付取消",
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case SDK_CHECK_FLAG:
                {
                    Toast.makeText(getActivity(), "检查结果为：" + msg.obj,
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                case UPDATE_ORDER_DETAILS:
                {
                    if (mTimer == null)
                        mTimer = new Timer();
                    mTimer.schedule(new MyTimer(), 1000, 1000);
                    break;
                }
                default:
                    break;
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
                    backDialog();
                    break;
                case R.id.paymentBtn:
                    if (isSuccess)
                    {
                        Toast.makeText(mContext, "您已经购买过了，请勿重新支付", Toast.LENGTH_LONG).show();
                    } else
                    {
                        mDialog.show();
                        paymentBtn.setClickable(false);
                        payment();
                    }
                    break;
            }
        }
    };

    public static ShopPaymentFragment newInstance(long orderId,String orderCode, double amount, long timeLeft, Order order)
    {
        ShopPaymentFragment fragment = new ShopPaymentFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ORDERID, orderId);
        args.putString("orderCode", orderCode);
        args.putDouble(ARG_AMOUNT, amount);
        args.putLong(ARG_TIME, timeLeft);
        args.putSerializable(ARG_ORDER, order);
        fragment.setArguments(args);
        return fragment;
    }


    public ShopPaymentFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            payOrderId=getArguments().getLong(ARG_ORDERID);
            payOrderCode=getArguments().getString("orderCode");
            payMoney=getArguments().getDouble(ARG_AMOUNT);
            initTime = getArguments().getLong(ARG_TIME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        if (mView == null)
        {
            mView = inflater.inflate(R.layout.shop_payment_page, container, false);
            mContext = getActivity();
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
        api = WXAPIFactory.createWXAPI(mContext, null);
        api.registerApp("wxce03c66622e5b243");
        ToolUtils.setLog("api:"+api.registerApp("wxce03c66622e5b243"));
        token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        userName = (String) SharedPreferencesUtil.getData(getActivity(), "nickName", "");
        userId = (Integer) SharedPreferencesUtil.getData(mContext, "userId", -1);
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
        cb_weixin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                if (b)
                {
                    cb_zhifubao.setChecked(false);
                    mCheckPosition = 0;
                    paymentBtn.setClickable(true);
                }
            }
        });
        cb_zhifubao = (CheckBox) mView.findViewById(R.id.cb_zhifubao);
        cb_zhifubao.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                if (b)
                {
                    cb_weixin.setChecked(false);
                    paymentBtn.setClickable(true);
                    mCheckPosition = 1;
                }
            }
        });
        TextView mAccountView = (TextView) mView.findViewById(R.id.tv_cash);
        mAccountView.setText("￥" +payMoney);
        mView.findViewById(R.id.tv_pinkage).setVisibility(View.GONE);
        mDialog= CustomLoadingDialog.setLoadingDialog(mContext,"");
        mDialog.dismiss();
        mHandler.sendEmptyMessage(UPDATE_ORDER_DETAILS);
    }

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
                    timeInfoTv.setText(new SimpleDateFormat("mm:ss").format(new Date(initTime * 1000)));
                    if (initTime <= 0)
                    {
                        if (mTimer != null)
                        {
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
     * 支付超时处理
     */
    private void stopView()
    {
        initTime = 0;
        paymentView.setVisibility(View.GONE);
        loseView.setVisibility(View.VISIBLE);
        paymentBtn.setClickable(false);
        paymentBtn.setBackgroundResource(R.drawable.btn_no_click_selector);
    }

    /**
     * 返回弹窗确认
     */
    public void backDialog()
    {
        final Dialog dialog = new Dialog(getActivity(), R.style.custom_dialog);
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_custom_collect_hint, null);
        TextView textView = (TextView) dialogView.findViewById(R.id.tv_msg);
        textView.setText("确认要放弃支付?");
        TextView cancelTv = (TextView) dialogView.findViewById(R.id.cancelTv);
        cancelTv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dialog.dismiss();
            }
        });
        TextView okTv = (TextView) dialogView.findViewById(R.id.okTv);
        okTv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dialog.dismiss();
                ((MainActivity) getActivity()).popToStack(ShopPaymentFragment.this);
            }
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.addContentView(dialogView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog.show();
    }

    /**
     * 付款ZFBMALLANDROID,WXMALLANDROID
     */
    private void getPaymentCode()
    {
        Map<String,String> maps=new HashMap<String, String>();
        maps.put("businessType ","01");
        maps.put("clientType ","ANDROID");
        maps.put("clientVersion ",mContext.getResources().getString(R.string.app_versionName).substring(1));
        maps.put("signature ","0459764e251e372215beec19050771c8");
        maps.put("timestamp ",System.currentTimeMillis()+"");
        maps.put("token ",token);
        maps.put("userId ",userId+"");
        maps.put("version ","1.0.0");
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,ZhaiDou.CommitPaymentUrl,new JSONObject(maps), new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                if (jsonObject != null)
                {
                    int status = jsonObject.optInt("status");
                    if (status == 200)
                    {
                        JSONArray array=jsonObject.optJSONArray("data");
                        if (array!=null)
                        {
                            for (int i = 0; i <array.length() ; i++)
                            {
                                JSONObject obj=array.optJSONObject(i);
                                String channelId=obj.optString("channelId");
                                String channelCode=obj.optString("channelCode");
                            }
                        }
                    }
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> map = new HashMap<String, String>();
                map.put("SECAuthorization", token);
                map.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return map;
            }
        };
        mRequestQueue.add(request);
    }

    private void payment()
    {
        JSONObject json = null;
        JSONObject maps = new JSONObject();
        try
        {
            json = new JSONObject();
            json.put("userName", userName);
            json.put("cashAmount", payMoney + "");
            json.put("orderId", payOrderId + "");
            json.put("userId", userId + "");
            json.put("orderCode", payOrderCode);
            if (mCheckPosition == 0)
            {
                json.put("channelCode", "WXMALLANDROID");
            } else
            {
                json.put("channelCode", "ZFBMALLANDROID");
            }
            json.put("notifyUrl", "");
            json.put("returnUrl", "");

            maps.put("businessType ", "01");
            maps.put("clientType ", "ANDROID");
            maps.put("data ", json);
            maps.put("version ", "1.0.0");

        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.CommitPaymentUrl, maps, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                if (jsonObject != null)
                {
                    int status = jsonObject.optInt("status");
                    if (status == 200)
                    {
                        JSONObject object = jsonObject.optJSONObject("data");
                        if (object != null)
                        {
                            if (mCheckPosition == 0)
                            {
                                if (api.isWXAppInstalled())
                                {
                                   final String appId =object.optString("appid");
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
                                    ToolUtils.setLog("request:"+request.checkArgs());
                                    ToolUtils.setLog("api:"+api.sendReq(request));
                                    if (!api.sendReq(request))
                                    {
                                        paymentBtn.setClickable(true);
                                    }
                                } else
                                {
                                    paymentBtn.setClickable(true);
                                    setDialogDismiss();
                                    ShowToast("没有安装微信客户端哦");
                                }

                            } else if (mCheckPosition == 1)
                            {
                                final String url = object.optString("notifyUrl");
                                mHandler.postDelayed(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        pay(url);
                                    }
                                }, 0);
                            }
                        }
                        else
                        {
                            paymentBtn.setClickable(true);
                            ToolUtils.setToast(mContext,R.string.loading_fail_txt);
                        }
                    }
                    else
                    {
                        paymentBtn.setClickable(true);
                        ToolUtils.setToast(mContext,R.string.loading_fail_txt);
                    }
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                mDialog.dismiss();
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> map = new HashMap<String, String>();
                map.put("SECAuthorization", token);
                map.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return map;
            }
        };
        mRequestQueue.add(request);
    }


    public void pay(final String url)
    {
        Runnable payRunnable = new Runnable()
        {

            @Override
            public void run()
            {
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



    public void handleWXPayResult(int result)
    {
        paymentBtn.setClickable(true);
        Log.i("----->", "paymentBtn");
        System.out.println("handleWXPayResult------------>" + result);
        switch (result)
        {
            case 800://商户订单号重复或生成错误
                Log.i("----->", "商户订单号重复或生成错误");
                break;
            case 0://支付成功
                Log.i("----->", "支付成功");
                isSuccess = true;
                CountManage.getInstance().minus(CountManage.TYPE.TAG_PREPAY);
                notificationPaySuccess();
                ShopPaymentSuccessFragment shopPaymentSuccessFragment = ShopPaymentSuccessFragment.newInstance(payOrderCode, 0, payMoney+"");
                ((MainActivity) getActivity()).navigationToFragment(shopPaymentSuccessFragment);
                break;
            case -1://支付失败
                Log.i("----->", "支付失败");
                ShopPaymentFailFragment shopPaymentFailFragment = ShopPaymentFailFragment.newInstance(payOrderId, payMoney, 0, initTime, payOrderCode);
                ((MainActivity) getActivity()).navigationToFragment(shopPaymentFailFragment);
                break;
            case -2://取消支付
                Log.i("----->", "取消支付");
                ToolUtils.setToast(mContext,R.string.loading_fail_txt);
                break;
            default:
                break;
        }
    }

    private void notificationPaySuccess()
    {
        if (orderListener != null)
        {
            mOrder.setStatus("" + ZhaiDou.STATUS_PAYED);
            mOrder.setOver_at(0);
        }

    }

    @Override
    public void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.shop_payment_text));
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    @Override
    public void onDestroyView()
    {
        if (orderListener != null)
        {
            mOrder.setOver_at(initTime);
            orderListener.onOrderStatusChange(mOrder);
        }
        if (mTimer != null)
        {
            mTimer.cancel();
            mTimer = null;
        }
        super.onDestroyView();
    }


    public void setPayment()
    {
        paymentBtn.setClickable(true);
    }

    public void setDialogDismiss()
    {
        if (mDialog!=null)
        mDialog.dismiss();
    }

    public void onPause()
    {
        super.onPause();
        setDialogDismiss();
        setPayment();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.shop_payment_text));
    }
}
