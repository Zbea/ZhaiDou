package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
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
import com.zhaidou.alipay.PayDemoActivity;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Order;
import com.zhaidou.model.Order1;
import com.zhaidou.model.OrderDetail;
import com.zhaidou.model.OrderItem1;
import com.zhaidou.model.Store;
import com.zhaidou.utils.DeviceUtils;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDetailFragment1 extends BaseFragment {
    private static final String ARG_ID = "id";
    private static final String ARG_TIMESTMP = "timestmp";
    private static final String ARG_ORDER = "order";
    private String mOrderId;
    private long mParam2;
    private Order1 mOrder;
    private int flags = 0;


    private RequestQueue requestQueue;
    private TextView mOrderNumber, mOrderTime, mOrderStatus,
            mReceiverName, mReceiverPhone, mReceiverAddress, mReceiverTime,mTotalView,
            mOrderAmount, mOrderEdit, mCancelOrder, mOrderTimer, goodsInfo;
    private ListView mListView;
    private TextView mSaleServiceTV;
    private Dialog mDialog;
    private OrderItemAdapter orderItemAdapter;
    private final int UPDATE_COUNT_DOWN_TIME = 2;
    private final int UPDATE_UI_TIMER_FINISH = 3;
    private MyTimer timer;
    private boolean isTimerStart = false;
    private OrderListener orderListener;
    private LinearLayout loadingView;

    private OnColseSuccess onColseSuccess;
    private long timeLeft;
    double amount;
    private View rootView;
    private Context mContext;
    private List<OrderItem1> orderItems = new ArrayList<OrderItem1>();
    private String token;
    private FrameLayout mBottomLayout;
    private Order order;
    private ListView mStoreList;
    private StoreAdapter mStoreAdapter;
    private DialogUtils mDialogUtils;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mStoreList.setAdapter(mStoreAdapter);
                    loadingView.setVisibility(View.GONE);
//                    OrderDetail orderDetail = (OrderDetail) msg.obj;
//                    mOrderNumber.setText(orderDetail.orderCode);
//                    mOrderTime.setText(orderDetail.creationTime);
//                    mOrderStatus.setText(orderDetail.orderShowStatus);
//                    goodsInfo.setText(orderDetail.remark);
//                    if (orderDetail.status == ZhaiDou.STATUS_UNPAY && mParam2 <= 0) {
//                        mOrderStatus.setText(mContext.getResources().getString(R.string.order_colse));
//                    }
//                    mReceiverName.setText(mOrder.childOrderPOList.get(0).buyerNick);
//                    mReceiverPhone.setText(mOrder.childOrderPOList.get(0).buyerMobile);
//                    mTotalView.setText("￥"+mOrder.orderActualAmount);
//                    mReceiverAddress.setText(orderDetail.deliveryAddressPO.provinceName + orderDetail.deliveryAddressPO.cityName + orderDetail.deliveryAddressPO.address);
//                    orderItemAdapter.notifyDataSetChanged();
                    break;
//                case UPDATE_COUNT_DOWN_TIME:
//                    String time = (String) msg.obj;
//                    mOrderTimer.setText(time);
//                    break;
//                case UPDATE_UI_TIMER_FINISH:
//                    mOrderTimer.setText(mContext.getResources().getString(R.string.timer_finish));
//                    mOrderTimer.setBackgroundResource(R.drawable.btn_no_click_selector);
//                    mOrderTimer.setClickable(true);
//                    mOrder.setStatus("" + ZhaiDou.STATUS_DEAL_CLOSE);
//                    mOrderStatus.setText(mContext.getResources().getString(R.string.order_colse));
//                    break;
            }
        }
    };

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//            if (flags != 1) {
//                GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance(orderItems.get(i).getMerchandise(), orderItems.get(i).getId());
//                Bundle bundle = new Bundle();
//                if (orderItems.get(i).getSale_cate() == 1) {
//                    bundle.putInt("flags", 1);
//                }
//                bundle.putInt("index", orderItems.get(i).getMerchandise_id());
//                bundle.putString("page", orderItems.get(i).getMerchandise());
//                goodsDetailsFragment.setArguments(bundle);
//                ((MainActivity) getActivity()).navigationToFragment(goodsDetailsFragment);
//            }
        }
    };

    public static OrderDetailFragment1 newInstance(String id, long timestmp, Order1 order, int flags) {
        OrderDetailFragment1 fragment = new OrderDetailFragment1();
        Bundle args = new Bundle();
        args.putString(ARG_ID, id);
        args.putInt("flags", flags);
        args.putLong(ARG_TIMESTMP, timestmp);
        args.putSerializable(ARG_ORDER, order);
        fragment.setArguments(args);
        return fragment;
    }

    public OrderDetailFragment1() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mOrderId = getArguments().getString(ARG_ID);
            mParam2 = getArguments().getLong(ARG_TIMESTMP);
            flags = getArguments().getInt("flags");
            mOrder = (Order1) getArguments().getSerializable(ARG_ORDER);
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
            rootView = inflater.inflate(R.layout.fragment_order_detail_list, container, false);
            initView(rootView);
        }
        return rootView;
    }

    private void initView(View view) {
        mContext = getActivity();
        System.out.println("OrderDetailFragment1.initView----------->"+getActivity());
//        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading", true);
        loadingView = (LinearLayout) view.findViewById(R.id.loadingView);
mDialogUtils=new DialogUtils(mContext);
        mStoreList=(ListView)view.findViewById(R.id.storeList);

        mStoreAdapter=new StoreAdapter(mContext,new ArrayList<Store>());
//        mStoreList.setAdapter(mStoreAdapter);
        requestQueue = Volley.newRequestQueue(getActivity());
        mReceiverName = (TextView) view.findViewById(R.id.tv_receiver_name);
        mReceiverPhone = (TextView) view.findViewById(R.id.tv_receiver_phone);
        mReceiverAddress = (TextView) view.findViewById(R.id.tv_receiver_address);
        mReceiverTime = (TextView) view.findViewById(R.id.tv_receiver_name);
        mOrderAmount = (TextView) view.findViewById(R.id.tv_order_amount);
        mTotalView=(TextView)view.findViewById(R.id.total);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                FetchOrderDetail(mOrderId);
            }
        }, 300);

//        if (mOrder != null && "678".contains(mOrder.getStatus())) {
//            view.findViewById(R.id.tv_order_time_left).setVisibility(View.GONE);
//            ((TextView) view.findViewById(R.id.tv_cancel_order)).setText(getResources().getString(R.string.sale_service_personal));
//        }
        token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");

        if (ZhaiDou.STATUS_PAYED == mOrder.status) {
            mBottomLayout.setVisibility(View.GONE);
        }

//        switch (mOrder.status) {
//            case ZhaiDou.STATUS_UNPAY:
//                if (mOrder.status == ZhaiDou.STATUS_UNPAY && mParam2 < 1) {
//                    mBottomLayout.setVisibility(View.GONE);
//                }
//                break;
//            case ZhaiDou.STATUS_PAYED:
//                mCancelOrder.setText(mContext.getResources().getString(R.string.order_return_money));
//                mOrderTimer.setVisibility(View.GONE);
//                mCancelOrder.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.btn_green_click_bg));
//                break;
//            case ZhaiDou.STATUS_OVER_TIME:
//                mBottomLayout.setVisibility(View.GONE);
//                break;
//            case ZhaiDou.STATUS_ORDER_CANCEL_PAYED:
//                mBottomLayout.setVisibility(View.VISIBLE);
//                mCancelOrder.setText(mContext.getResources().getString(R.string.after_sale_service));
//                mOrderTimer.setVisibility(View.GONE);
//                break;
//            case ZhaiDou.STATUS_DELIVERY:
//                mCancelOrder.setText(mContext.getResources().getString(R.string.order_logistics));
//                mOrderTimer.setText(mContext.getResources().getString(R.string.order_received));
//                mCancelOrder.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.btn_green_click_bg));
//                mOrderTimer.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.btn_red_click_selector));
//                break;
//            case ZhaiDou.STATUS_DEAL_SUCCESS:
//                Log.i("STATUS_DELIVERY----------------->", "STATUS_DELIVERY");
//                mCancelOrder.setText(mContext.getResources().getString(R.string.order_logistics));
//                mOrderTimer.setText(mContext.getResources().getString(R.string.order_return_good));
//                mCancelOrder.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.btn_green_click_bg));
//                mOrderTimer.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.btn_green_click_bg));
//                break;
//            case ZhaiDou.STATUS_APPLY_GOOD_RETURN:
//                mOrderTimer.setVisibility(View.GONE);
//                mCancelOrder.setText(getResources().getString(R.string.sale_service_personal));
//                break;
//            case ZhaiDou.STATUS_GOOD_RETURNING:
//                mOrderTimer.setVisibility(View.GONE);
//                mCancelOrder.setText(getResources().getString(R.string.sale_service_personal));
//                break;
//            case ZhaiDou.STATUS_RETURN_GOOD_SUCCESS:
//                mOrderTimer.setVisibility(View.GONE);
//                mCancelOrder.setText(getResources().getString(R.string.sale_service_personal));
//                break;
//            case ZhaiDou.STATUS_UNPAY_CANCEL:
//                mBottomLayout.setVisibility(View.GONE);
//                break;
//            case ZhaiDou.STATUS_DEAL_CLOSE:
//                mBottomLayout.setVisibility(View.GONE);
//                break;
//            case ZhaiDou.STATUS_RETURN_MONEY_SUCCESS:
//                mBottomLayout.setVisibility(View.GONE);
//                break;

//        }
        loadingView.setVisibility(View.GONE);
        mStoreAdapter.setOnInViewClickListener(R.id.bt_logistics,new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                System.out.println("StoreAdapter.OnClickListener-------bt_logistics");
                Store store=(Store)values;
                if (ZhaiDou.STATUS_DEAL_SUCCESS==store.status){
                    OrderLogisticsMsgFragment logisticsMsgFragment=OrderLogisticsMsgFragment.newInstance("","",null);
                    ((BaseActivity)getActivity()).navigationToFragment(logisticsMsgFragment);
                }
            }
        });
        mStoreAdapter.setOnInViewClickListener(R.id.bt_received,new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                System.out.println("parentV = [" + parentV + "], v = [" + v + "], position = [" + position + "], values = [" + values + "]");
                Store store = (Store)values;
                if (ZhaiDou.STATUS_DEAL_SUCCESS==store.status){
                    mDialogUtils.showDialog(mContext.getResources().getString(R.string.order_apply_return_goods),new DialogUtils.PositiveListener() {
                        @Override
                        public void onPositive() {

                        }
                    },null);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel_order:
                if (ZhaiDou.STATUS_DEAL_SUCCESS == mOrder.status) {

                    OrderLogisticsMsgFragment logisticsMsgFragment = OrderLogisticsMsgFragment.newInstance("", "", order);
                    ((MainActivity) getActivity()).navigationToFragment(logisticsMsgFragment);
                    return;
                } else if (mContext.getResources().getString(R.string.order_return_money).equalsIgnoreCase(mCancelOrder.getText().toString())) {
                    final Dialog dialog = new Dialog(getActivity(), R.style.custom_dialog);
                    View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_custom_collect_hint, null);
                    TextView textView = (TextView) dialogView.findViewById(R.id.tv_msg);
                    textView.setText("是否申请退款?");
                    TextView cancelTv = (TextView) dialogView.findViewById(R.id.cancelTv);
                    cancelTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    TextView okTv = (TextView) dialogView.findViewById(R.id.okTv);
                    okTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                            mDialog = CustomLoadingDialog.setLoadingDialog(getActivity(), "loading");
                            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.URL_ORDER_LIST + "/" + order.getOrderId() + "/update_status?status=5", new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    JSONObject orderObj = jsonObject.optJSONObject("order");
                                    if (orderObj != null) {
                                        String status = orderObj.optString("status");
                                        if ("5".equalsIgnoreCase(status)) {
//                                            orders.remove(order);
                                        } else {
                                            ToolUtils.setToast(getActivity(), "抱歉,确认收货失败");
                                        }
//                                        handler.sendEmptyMessage(STATUS_UNRECEIVE_LIST);
                                        mDialog.dismiss();

                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    mDialog.dismiss();
                                    ToolUtils.setToast(getActivity(), "抱歉,申请退款失败");
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
                            requestQueue.add(request);
                        }
                    });
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.setCancelable(true);
                    dialog.addContentView(dialogView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    dialog.show();
//                    AfterSaleFragment afterSaleFragment = AfterSaleFragment.newInstance(mOrderId, mOrder.getStatus());
//                    ((MainActivity) getActivity()).navigationToFragment(afterSaleFragment);
                    return;
                } else if (mContext.getResources().getString(R.string.order_logistics).equalsIgnoreCase(mCancelOrder.getText().toString())) {

                    OrderLogisticsMsgFragment logisticsMsgFragment = OrderLogisticsMsgFragment.newInstance("", "", order);
                    ((MainActivity) getActivity()).navigationToFragment(logisticsMsgFragment);
                    return;
                }
                if (mOrder != null && "3678".contains(mOrder.status + "")) {
                    if (DeviceUtils.isApkInstalled(getActivity(), "com.tencent.mobileqq")) {
                        String url = "mqqwpa://im/chat?chat_type=wpa&uin=" + mContext.getResources().getString(R.string.QQ_Number);
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    } else {
                        ShowToast("没有安装QQ客户端哦");
                    }
                    return;
                }
                cancalOrderDialog();
                break;
            case R.id.tv_order_time_left:
                if (("" + ZhaiDou.STATUS_DEAL_SUCCESS).equalsIgnoreCase(mOrder.status + "")) {
                    OrderAfterSaleFragment afterSaleFragment = OrderAfterSaleFragment.newInstance(mOrderId, mOrder.status + "");
                    ((MainActivity) getActivity()).navigationToFragment(afterSaleFragment);
                    afterSaleFragment.setOrderListener(new Order.OrderListener() {
                        @Override
                        public void onOrderStatusChange(Order order) {
//                            mOrder.setStatus(order.getStatus());
                            mOrderStatus.setText("申请退货");
                            mOrderTimer.setVisibility(View.GONE);
                            mCancelOrder.setText(getResources().getString(R.string.sale_service_personal));
                        }
                    });
                    return;
                } else if (mContext.getResources().getString(R.string.order_received).equalsIgnoreCase(mOrderTimer.getText().toString())) {
                    orderOkReciver();
                    return;
                } else if (mContext.getResources().getString(R.string.timer_finish).equalsIgnoreCase(mOrderTimer.getText().toString())) {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.order_had_order_time), Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent1 = new Intent(getActivity(), PayDemoActivity.class);
                intent1.putExtra("id", Integer.parseInt(mOrderId + ""));
                intent1.putExtra("amount", mOrder.orderPayAmount);
//                ShopPaymentFragment shopPaymentFragment = ShopPaymentFragment.newInstance(Integer.parseInt(mOrderId), amount, 10, 1000, null, 2);
//                ((MainActivity) getActivity()).navigationToFragment(shopPaymentFragment);
//                shopPaymentFragment.setOrderListener(new Order.OrderListener() {
//                    @Override
//                    public void onOrderStatusChange(Order order) {
//                        if (order.getStatus().equals("" + ZhaiDou.STATUS_PAYED)) {
//                            order.setStatus("" + ZhaiDou.STATUS_PAYED);
//                            order.setOver_at(0);
//                            mCancelOrder.setText(mContext.getResources().getString(R.string.order_return_money));
//                            mOrderTimer.setVisibility(View.GONE);
//                            mCancelOrder.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.btn_green_click_bg));
//                            mOrderStatus.setText("已付款");
//                        } else {
//                            mParam2 = order.getOver_at();
//                        }
//                    }
//                });
                break;
        }
        super.onClick(view);
    }

    private void FetchOrderDetail(String id) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("userId", ZhaiDou.TESTUSERID);
        params.put("clientType", "ANDROID");
        params.put("clientVersion", "45");
        params.put("businessType", "01");
        params.put("orderCode", mOrder.orderCode);//"MCMST3407850_1"
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.URL_ORDER_DETAIL, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (mDialog != null)
                    mDialog.dismiss();
                JSONObject dataObj = jsonObject.optJSONObject("data");
//                Order1 order = JSON.toJavaObject(dataObj, Order1.class);
                OrderDetail orderDetail = JSON.parseObject(dataObj.toString(), OrderDetail.class);

                mStoreAdapter.addAll(mOrder.childOrderPOList);
                if (orderDetail.orderItemPOList != null)
                    orderItems.addAll(orderDetail.orderItemPOList);
                Message message = new Message();
                message.obj = orderDetail;
                message.what = 1;
                handler.sendMessage(message);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mDialog.dismiss();
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
        requestQueue.add(request);
    }

    /**
     * ????dialog
     */
    private void cancalOrderDialog() {
        DialogUtils mDialogUtils = new DialogUtils(getActivity());
        final Map<String, String> params = new HashMap();
        params.put("businessType", "01");
        params.put("clientType", "ANDROID");
        params.put("version", "1.0.1");
        params.put("userId", "28129");
        params.put("clientVersion", "45");
        params.put("orderCode", mOrder.orderCode);
        mDialogUtils.showDialog(mContext.getResources().getString(R.string.order_cancel_ok), new DialogUtils.PositiveListener() {
            @Override
            public void onPositive() {
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.URL_ORDER_CANCEL, new JSONObject(params), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        int status = jsonObject.optInt("status");
                        String message = jsonObject.optString("message");
                        ShowToast(status == 200 ? "取消订单成功" : message);
                        mOrder.status=ZhaiDou.STATUS_ORDER_CANCEL;
                        mOrder.orderShowStatus="已取消";
                        if (orderListener != null)
                            orderListener.onOrderStatusChange(mOrder);
                        ((MainActivity) getActivity()).popToStack(OrderDetailFragment1.this);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
                requestQueue.add(request);
            }
        }, null);


//        final Dialog dialog = new Dialog(getActivity(), R.style.custom_dialog);
//        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_custom_collect_hint, null);
//        TextView textView = (TextView) dialogView.findViewById(R.id.tv_msg);
//        textView.setText(mContext.getResources().getString(R.string.order_cancel_ok));
//        TextView cancelTv = (TextView) dialogView.findViewById(R.id.cancelTv);
//        cancelTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//            }
//        });
//
//        TextView okTv = (TextView) dialogView.findViewById(R.id.okTv);
//        okTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mDialog.show();
//                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.URL_ORDER_LIST + "/" + mOrder.orderId + "/update_status?status=" + ("0".equalsIgnoreCase(mOrder.status + "") ? "9" : "3"), new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject jsonObject) {
//                        mDialog.dismiss();
//                        int status = jsonObject.optInt("status");
//                        if (201 == status) {
//                            JSONObject orderObj = jsonObject.optJSONObject("order");
//                            if (orderObj != null) {
//                                double amount = orderObj.optDouble("amount");
//                                int id = orderObj.optInt("id");
//                                long over_at = orderObj.optLong("over_at");
//                                String status1 = orderObj.optString("status");
//                                String created_at_for = orderObj.optString("created_at_for");
//                                String created_at = orderObj.optString("created_at");
//                                String status_ch = orderObj.optString("status_ch");
//                                String number = orderObj.optString("number");
//                                Order order = new Order(id, number, amount, status1, status_ch, created_at_for, created_at, "", 0);
//                                order.setOver_at(0);
//                                if (orderListener != null)
//                                    orderListener.onOrderStatusChange(order);
//                                ((MainActivity) getActivity()).popToStack(OrderDetailFragment.this);
//                            }
//                        }
//                        dialog.dismiss();
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError volleyError) {
//                        dialog.dismiss();
//                        ToolUtils.setToast(mContext, "加载失败");
//                    }
//                }) {
//                    @Override
//                    public Map<String, String> getHeaders() throws AuthFailureError {
//                        Map<String, String> headers = new HashMap<String, String>();
//                        headers.put("SECAuthorization", token);
//                        headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
//                        return headers;
//                    }
//                };
//                requestQueue.add(request);
//
//            }
//        });
//        dialog.setCanceledOnTouchOutside(true);
//        dialog.setCancelable(true);
//        dialog.addContentView(dialogView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        dialog.show();
    }

    /**
     * ??????
     */
    private void orderOkReciver() {
//        final Dialog dialog = new Dialog(getActivity(), R.style.custom_dialog);
//
//        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_custom_collect_hint, null);
//        TextView textView = (TextView) dialogView.findViewById(R.id.tv_msg);
//        textView.setText(mContext.getResources().getString(R.string.order_confirm));
//        TextView cancelTv = (TextView) dialogView.findViewById(R.id.cancelTv);
//        cancelTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//            }
//        });
//
//        TextView okTv = (TextView) dialogView.findViewById(R.id.okTv);
//        okTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.URL_ORDER_LIST + "/" + mOrder.getOrderId() + "/update_status?status=5", new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject jsonObject) {
//                        JSONObject orderObj = jsonObject.optJSONObject("order");
//                        if (orderObj != null) {
//                            String status = orderObj.optString("status");
//                            mOrder.setStatus(status);
//                            if (orderListener != null)
//                                orderListener.onOrderStatusChange(mOrder);
//                            ((BaseActivity) getActivity()).popToStack(OrderDetailFragment.this);
//                        }
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError volleyError) {
//                        ToolUtils.setToast(mContext, "加载失败");
//                    }
//                }) {
//                    @Override
//                    public Map<String, String> getHeaders() throws AuthFailureError {
//                        Map<String, String> headers = new HashMap<String, String>();
//                        headers.put("SECAuthorization", token);
//                        headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
//                        return headers;
//                    }
//                };
//                requestQueue.add(request);
//                dialog.dismiss();
//            }
//        });
//        dialog.setCanceledOnTouchOutside(true);
//        dialog.setCancelable(true);
//        dialog.addContentView(dialogView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        dialog.show();
    }


    public class OrderItemAdapter extends BaseListAdapter<OrderItem1> {
        public OrderItemAdapter(Context context, List<OrderItem1> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_order_detail, null);
            System.out.println("OrderItemAdapter.bindView");
            TextView tv_name = ViewHolder.get(convertView, R.id.tv_name);
            TextView tv_specification = ViewHolder.get(convertView, R.id.tv_specification);
            TextView tv_count = ViewHolder.get(convertView, R.id.tv_count);
            ImageView iv_order_img = ViewHolder.get(convertView, R.id.iv_order_img);
            LinearLayout ll_count = ViewHolder.get(convertView, R.id.ll_count);
            TextView tv_zero_msg = ViewHolder.get(convertView, R.id.tv_zero_msg);
            TextView mPrice = ViewHolder.get(convertView, R.id.orderItemCurrentPrice);
            TextView mOldPrice = ViewHolder.get(convertView, R.id.orderItemFormalPrice);

            OrderItem1 item = getList().get(position);
            tv_name.setText(item.productName);
            tv_specification.setText(item.specifications);
            tv_count.setText(item.quantity + "");
            ToolUtils.setImageCacheUrl(item.pictureMiddleUrl, iv_order_img, R.drawable.icon_loading_defalut);
            mPrice.setText("￥" + ToolUtils.isIntPrice("" + item.price));
            mOldPrice.setText("￥" + ToolUtils.isIntPrice("" + item.price));
            mOldPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            return convertView;
        }
    }


    private class MyTimer extends CountDownTimer {
        private MyTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            timeLeft = l;
//            mOrder.setOver_at(l / 1000);
//            Message message = new Message();
//            String data = getResources().getString(R.string.timer_start);
//            data = String.format(data, new SimpleDateFormat("mm:ss").format(new Date(l)));
//            message.what = UPDATE_COUNT_DOWN_TIME;
//            message.obj = data;
//            handler.sendMessage(message);
        }

        @Override
        public void onFinish() {
            handler.sendEmptyMessage(UPDATE_UI_TIMER_FINISH);
        }
    }

    @Override
    public void onResume() {
        if ((ZhaiDou.STATUS_UNPAY + "").equalsIgnoreCase(mOrder.status + "")) {
            if (timer == null)
                timer = new MyTimer(mParam2 * 1000, 1000);
            if (!isTimerStart) {
                isTimerStart = true;
                timer.start();
            }
        }
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_order_detail));
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_order_detail));
    }

    @Override
    public void onDestroyView() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
//        if (orderListener != null)
//            orderListener.onOrderStatusChange(mOrder);
        if (onColseSuccess != null)
            onColseSuccess.colsePage();
        super.onDestroyView();
    }

    @Override
    public void onStop() {
        if (timer != null) {
            timer.cancel();
            isTimerStart = false;
        }
        super.onStop();
    }

    public void setOrderListener(OrderListener orderListener) {
        this.orderListener = orderListener;
    }

    public interface OrderListener {
        public void onOrderStatusChange(Order1 order);
    }

    public void setOnColseSuccess(OnColseSuccess OnColseSuccess) {
        this.onColseSuccess = OnColseSuccess;
    }

    public interface OnColseSuccess {
        public void colsePage();
    }

    private class StoreAdapter extends BaseListAdapter<Store>{

        public StoreAdapter(Context context, List<Store> list) {
            super(context, list);
        }

        @Override
        public View bindView(final int position, View convertView, ViewGroup parent) {
            if (convertView==null)
                convertView=mInflater.inflate(R.layout.item_order_detail1,null);
            TextView mOrderNumber=ViewHolder.get(convertView,R.id.tv_order_number);
            TextView mOrderTime = ViewHolder.get(convertView,R.id.tv_order_time);
            TextView mOrderStatus =ViewHolder.get(convertView,R.id.tv_order_status);
            ListView mListView =ViewHolder.get(convertView,R.id.lv_order_list);
            TextView btn1=ViewHolder.get(convertView,R.id.bt_received);
            TextView btn2=ViewHolder.get(convertView,R.id.bt_logistics);
            LinearLayout mBottomLayout =ViewHolder.get(convertView,R.id.ll_bottom);

            final LinearLayout detailContainer=ViewHolder.get(convertView,R.id.detailContainer);
            TextView mReceiverName=ViewHolder.get(convertView,R.id.tv_receiver_name);
            TextView mReceiverPhone=ViewHolder.get(convertView,R.id.tv_receiver_phone);
            TextView mReceiverAddress=ViewHolder.get(convertView,R.id.tv_receiver_address);
            TextView mOrderAmount=ViewHolder.get(convertView,R.id.total);
            mReceiverName.setText(mOrder.childOrderPOList.get(0).buyerNick);
            mReceiverPhone.setText(mOrder.childOrderPOList.get(0).buyerMobile);
            mOrderAmount.setText("￥"+mOrder.orderActualAmount);
//            mReceiverAddress.setText(orderDetail.deliveryAddressPO.provinceName + orderDetail.deliveryAddressPO.cityName + orderDetail.deliveryAddressPO.address);

            final Store store = getList().get(position);
            mOrderNumber.setText(store.orderCode+"");
            mOrderTime.setText(store.creationTime);
            mOrderStatus.setText(store.orderShowStatus);

            if (ZhaiDou.STATUS_DEAL_SUCCESS==store.status){
                btn1.setText("申请退货");
                btn2.setText("查看物流");
                btn1.setBackgroundResource(R.drawable.btn_green_click_bg);
            }else if (ZhaiDou.STATUS_UNDELIVERY==store.status||ZhaiDou.STATUS_UNPAY==store.status){
                mBottomLayout.setVisibility(View.GONE);
            }

            final OrderItemAdapter adapter = new OrderItemAdapter(mContext,store.orderItemPOList){
                @Override
                public int getCount() {
                    if (!store.isExpand){
                        detailContainer.setVisibility(View.GONE);
                        return 1;
                    }
                    detailContainer.setVisibility(View.VISIBLE);
                    return super.getCount();
                }
            };
            mListView.setAdapter(adapter);
            setOnInViewClickListener(R.id.moreDetail,new onInternalClickListener() {
                @Override
                public void OnClickListener(View parentV, View v, Integer position, Object values) {
                    Store store1= (Store) values;
//                    if (store1.orderItemPOList!=null&&store1.orderItemPOList.size()<2)
//                    store1.orderItemPOList.addAll(store1.orderItemPOList);
                    store1.isExpand=!store1.isExpand;
                    adapter.notifyDataSetChanged();
                }
            });

            return convertView;
        }
    }
}
