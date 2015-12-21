package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshListView;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Order;
import com.zhaidou.model.Order1;
import com.zhaidou.model.Store;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OrderAllOrdersFragment extends BaseFragment implements View.OnClickListener, PullToRefreshBase.OnRefreshListener2<ListView> {
    private static final String ARG_TYPE = "type";
    private static final String ARG_PARAM2 = "param2";


    private String mParam2;

    private Dialog mDialog;
    private LinearLayout loadingView;

    private RequestQueue mRequestQueue;
    private PullToRefreshListView mListView;
    private List<Order> orders = new ArrayList<Order>();
    AllOrderAdapter allOrderAdapter;
    private final int UPDATE_ORDER_LIST = 1;
    private final int UPDATE_COUNT_DOWN_TIME = 2;
    private final int UPDATE_UI_TIMER_FINISH = 3;
    private MyTimer timer;
    private View rootView;
    private String token;
    private long preTime = 0;
    private long timeStmp = 0;
    private Context mContext;
    private boolean isViewDestroy = false;
    private View mEmptyView, mNetErrorView;
    private Map<Integer, Boolean> timerMap = new HashMap<Integer, Boolean>();
    private boolean isTimerStart = false;
    private List<Order1> mOrderList;
    private DialogUtils mDialogUtils;
    private int mCurrentPage;
    private String mCurrentType;
    private int initTime = 900;
    private boolean isDataLoaded = false;
    private String mUserId;
    private Map<Integer, Long> timerMapStamp = new HashMap<Integer, Long>();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            isViewDestroy = !isViewDestroy;
            switch (msg.what) {
                case UPDATE_ORDER_LIST:
                    loadingView.setVisibility(View.GONE);
                    mListView.setVisibility(View.VISIBLE);
                    allOrderAdapter.notifyDataSetChanged();
                    mListView.onRefreshComplete();
                    if (timer == null) {
                        timer = new MyTimer(15 * 60 * 1000, 1000);
                    }
                    timer.start();
                    break;
                case UPDATE_COUNT_DOWN_TIME:
                    if (mOrderList != null && mOrderList.size() > 0) {
                        loadingView.setVisibility(View.GONE);
                        allOrderAdapter.notifyDataSetChanged();
                    } else {
                        mListView.setVisibility(View.GONE);
                        loadingView.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    };

    public static OrderAllOrdersFragment newInstance(String type, String param2) {
        OrderAllOrdersFragment fragment = new OrderAllOrdersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public OrderAllOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCurrentType = getArguments().getString(ARG_TYPE);
            mParam2 = getArguments().getString(ARG_PARAM2);
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
            rootView = inflater.inflate(R.layout.fragment_all_orders, container, false);
            mContext = getActivity();
            mOrderList = new ArrayList<Order1>();
            mDialogUtils = new DialogUtils(mContext);
            loadingView = (LinearLayout) rootView.findViewById(R.id.loadingView);
            mEmptyView = rootView.findViewById(R.id.nullline);
            mNetErrorView = rootView.findViewById(R.id.nullNetline);
            rootView.findViewById(R.id.netReload).setOnClickListener(this);
            mListView = (PullToRefreshListView) rootView.findViewById(R.id.lv_all_orderlist);
            mListView.setMode(PullToRefreshBase.Mode.BOTH);
            mListView.setOnRefreshListener(this);

            mRequestQueue = Volley.newRequestQueue(getActivity());
            allOrderAdapter = new AllOrderAdapter(getActivity(), mOrderList);
            mListView.setAdapter(allOrderAdapter);
            token = (String) SharedPreferencesUtil.getData(mContext, "token", "");
            mUserId=SharedPreferencesUtil.getData(mContext, "userId", -1)+"";

            initData();
            allOrderAdapter.setOnInViewClickListener(R.id.orderlayout, new BaseListAdapter.onInternalClickListener() {
                @Override
                public void OnClickListener(View parentV, View v, Integer position, Object values) {
                    final Order1 order = (Order1) values;
                    Log.i("order--------->", order.toString());
                    final TextView btn2 = (TextView) parentV.findViewById(R.id.bt_received);
//                    if (ZhaiDou.STATUS_DEAL_SUCCESS == Integer.parseInt(order.getStatus())) {
//                        AfterSaleFragment afterSaleFragment = AfterSaleFragment.newInstance(order.getOrderId() + "", "return_good");
//                        ((MainActivity) getActivity()).navigationToFragment(afterSaleFragment);
//                        return;
//                    }
                    if (btn2.getTag() != null)
                        preTime = Long.parseLong(btn2.getTag().toString());
                    OrderDetailFragment1 orderDetailFragment = OrderDetailFragment1.newInstance(order.orderCode, 2);
                    ((MainActivity) getActivity()).navigationToFragment(orderDetailFragment);
//                    orderDetailFragment.setOrderListener(new OrderDetailFragment.OrderListener() {
//                        @Override
//                        public void onOrderStatusChange(Order1 o) {
//                            Log.i("AllOrdersFragment---------o-->", o.toString());
//                            order.status=o.status;
//                            order.orderShowStatus=o.orderShowStatus;
//                            allOrderAdapter.notifyDataSetChanged();
//                            long time = o.getOver_at();
//                            if (!isTimerStart) {
//                                timeStmp = preTime - time;
//                                Log.i("timeStmp----------->", timeStmp + "");
//                                isViewDestroy = false;
//                                timerMap.clear();
//                            } else {
//                                btn2.setTag(o.getOver_at());
//                                order.setOver_at(o.getOver_at());
//                                order.setStatus(o.getStatus());
//                            }
//                        }
//                    });
                }
            });
            allOrderAdapter.setOnInViewClickListener(R.id.bt_logistics, new BaseListAdapter.onInternalClickListener() {
                @Override
                public void OnClickListener(View parentV, View v, Integer position, Object values) {
                    final Order1 order = (Order1) values;
                    if (ZhaiDou.STATUS_UNDELIVERY == order.status) {
                        List<Store> childOrderPOList = order.childOrderPOList;
                        if (childOrderPOList.size() == 1 && childOrderPOList.get(0).orderItemPOList.size() == 1 && childOrderPOList.get(0).orderItemPOList.get(0).productType == 2) {
                            ShowToast("零元特卖商品不可以退哦！");
                            return;
                        }
                        mDialogUtils.showDialog(mContext.getResources().getString(R.string.order_apply_return_money), new DialogUtils.PositiveListener() {
                            @Override
                            public void onPositive() {
                                final Map<String, String> params = new HashMap();
                                params.put("businessType", "01");
                                params.put("clientType", "ANDROID");
                                params.put("version", "1.0.1");
                                params.put("userId", mUserId);
                                params.put("clientVersion", "45");
                                params.put("orderCode", order.orderCode);
                                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.URL_ORDER_APPLY_CANCEL, new JSONObject(params), new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject jsonObject) {
                                        int status = jsonObject.optInt("status");
                                        String message = jsonObject.optString("message");
                                        if (200 == status) {
                                            order.status = ZhaiDou.STATUS_ORDER_APPLY_CANCEL;
                                            order.orderShowStatus = "申请取消中";
                                        }
                                        ShowToast(status == 200 ? "正在申请退款" : message);
                                    }
                                }, null);
                                mRequestQueue.add(request);
                            }
                        }, null);
                    } else if (ZhaiDou.STATUS_UNPAY == order.status) {
                        final Map<String, String> params = new HashMap();
                        params.put("businessType", "01");
                        params.put("clientType", "ANDROID");
                        params.put("version", "1.0.1");
                        params.put("userId",mUserId);
                        params.put("clientVersion", "45");
                        params.put("orderCode", order.orderCode);
                        mDialogUtils.showDialog(mContext.getResources().getString(R.string.order_cancel_ok), new DialogUtils.PositiveListener() {
                            @Override
                            public void onPositive() {
                                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.URL_ORDER_CANCEL, new JSONObject(params), new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject jsonObject) {
                                        int status = jsonObject.optInt("status");
                                        String message = jsonObject.optString("message");
                                        if (200 == status) {
                                            order.status = ZhaiDou.STATUS_ORDER_CANCEL;
                                            order.orderShowStatus = "已取消";
                                        }
                                        ShowToast(status == 200 ? "取消订单成功" : message);
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError volleyError) {

                                    }
                                });
                                mRequestQueue.add(request);
                            }
                        }, null);
                    }
//                    Log.i("v---------->", v.toString());
//                    TextView textView = (TextView) v;
//                    if (mContext.getResources().getString(R.string.order_logistics).equalsIgnoreCase(textView.getText().toString())) {
//
//                        OrderLogisticsMsgFragment logisticsMsgFragment = OrderLogisticsMsgFragment.newInstance("", "",order);
//                        ((MainActivity) getActivity()).navigationToFragment(logisticsMsgFragment);
//
//                    } else if (mContext.getResources().getString(R.string.order_return_money).equalsIgnoreCase(textView.getText().toString())) {
//                        System.out.println("OrderAllOrdersFragment.OnClickListener------->"+order.isZero());
//                        if (order.isZero()){
//                            ShowToast("零元特卖的商品不可以退哦");
//                            return;
//                        }
//                        final Dialog dialog = new Dialog(getActivity(), R.style.custom_dialog);
//
//                        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_custom_collect_hint, null);
//                        TextView textView1 = (TextView) view.findViewById(R.id.tv_msg);
//                        textView1.setText(mContext.getResources().getString(R.string.order_apply_return_money));
//                        TextView cancelTv = (TextView) view.findViewById(R.id.cancelTv);
//                        cancelTv.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                dialog.dismiss();
//                            }
//                        });
//
//                        TextView okTv = (TextView) view.findViewById(R.id.okTv);
//                        okTv.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.URL_ORDER_LIST + "/" + order.getOrderId() + "/update_status?status=3", new Response.Listener<JSONObject>() {
//                                    @Override
//                                    public void onResponse(JSONObject jsonObject) {
//                                        JSONObject orderObj = jsonObject.optJSONObject("order");
//                                        if (orderObj != null) {
//                                            String status = orderObj.optString("status");
//                                            order.setStatus(status);
//                                        }
//                                    }
//                                }, new Response.ErrorListener() {
//                                    @Override
//                                    public void onErrorResponse(VolleyError volleyError) {
//
//                                    }
//                                }) {
//                                    @Override
//                                    public Map<String, String> getHeaders() throws AuthFailureError {
//                                        Map<String, String> headers = new HashMap<String, String>();
//                                        headers.put("SECAuthorization", token);
//                                        headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
//                                        return headers;
//                                    }
//                                };
//                                mRequestQueue.add(request);
//                                dialog.dismiss();
//                            }
//                        });
//                        dialog.setCanceledOnTouchOutside(true);
//                        dialog.setCancelable(true);
//                        dialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//                        dialog.show();
//                    }
                }
            });
            allOrderAdapter.setOnInViewClickListener(R.id.bt_received, new BaseListAdapter.onInternalClickListener() {
                @Override
                public void OnClickListener(View parentV, View v, Integer position, Object values) {
                    final Order1 order = (Order1) values;
                    final TextView btn2 = (TextView) v;
                    if (ZhaiDou.STATUS_UNPAY == order.status) {
                        if (order.orderRemainingTime<=0){
                            ShowToast("订单已超时");
                            return;
                        }
                        ShopPaymentFragment shopPaymentFragment = ShopPaymentFragment.newInstance(order.orderId,order.orderCode ,Double.parseDouble(order.orderTotalAmount), Integer.parseInt(order.orderRemainingTime+""), null, 2);
                        ((BaseActivity) getActivity()).navigationToFragment(shopPaymentFragment);
                    } else if (ZhaiDou.STATUS__DELIVERYED == order.status) {
                        mDialogUtils.showDialog(mContext.getResources().getString(R.string.order_confirm), new DialogUtils.PositiveListener() {
                            @Override
                            public void onPositive() {
                                System.out.println("OrderAllOrdersFragment.onPositive");
                                Map<String, String> params = new HashMap();
                                params.put("businessType", "01");
                                params.put("clientType", "ANDROID");
                                params.put("userId",mUserId);
                                params.put("clientVersion", "45");
                                params.put("orderCode", order.orderCode);
                                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.URL_ORDER_CONFIRM, new JSONObject(params), new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject jsonObject) {
                                        int status = jsonObject.optInt("status");
                                        String message = jsonObject.optString("message");
                                        if (200 == status) {
                                            order.status = 50;
                                            order.orderShowStatus = "交易完成";
                                            allOrderAdapter.notifyDataSetChanged();
                                        } else {
                                            ShowToast(message);
                                        }
                                    }
                                }, null);
                                mRequestQueue.add(request);
                            }
                        }, null);
                    }
                }
            });
            allOrderAdapter.setOnInViewClickListener(R.id.iv_delete, new BaseListAdapter.onInternalClickListener() {
                @Override
                public void OnClickListener(View parentV, View v, final Integer position, Object values) {
                    final Order1 order = (Order1) values;
                    final Map<String, String> params = new HashMap();
                    params.put("businessType", "01");
                    params.put("userId",mUserId);
                    params.put("orderCode", order.orderCode);
                    DialogUtils mDialogUtils = new DialogUtils(getActivity());
                    mDialogUtils.showDialog(mContext.getResources().getString(R.string.order_delete), new DialogUtils.PositiveListener() {
                        @Override
                        public void onPositive() {
                            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.URL_ORDER_DELETE, new JSONObject(params), new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    int status = jsonObject.optInt("status");
                                    String message = jsonObject.optString("message");
                                    if (200 == status) {
                                        mOrderList.remove(order);
                                        allOrderAdapter.notifyDataSetChanged();
                                    } else {
                                        ShowToast(message);
                                    }
                                }
                            }, null);
                            mRequestQueue.add(request);
                        }
                    }, null);
//                    final Dialog dialog = new Dialog(getActivity(), R.style.custom_dialog);
//
//                    View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_custom_collect_hint, null);
//                    TextView textView = (TextView) view.findViewById(R.id.tv_msg);
//                    textView.setText(mContext.getResources().getString(R.string.order_delete));
//                    TextView cancelTv = (TextView) view.findViewById(R.id.cancelTv);
//                    cancelTv.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            dialog.dismiss();
//                        }
//                    });
//
//                    TextView okTv = (TextView) view.findViewById(R.id.okTv);
//                    okTv.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            dialog.dismiss();
//                            mDialog.show();
//                            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.URL_ORDER_LIST + "/" + order.getOrderId() + "/delete_order", new Response.Listener<JSONObject>() {
//                                @Override
//                                public void onResponse(JSONObject jsonObject) {
//                                    mDialog.dismiss();
//                                    Log.i("jsonObject---iv_delete->", jsonObject.toString());
//                                    if (jsonObject != null) {
//                                        int status = jsonObject.optInt("status");
//                                        if (201 == status) {
//                                            orders.remove(order);
//                                        } else if (400 == status) {
//                                            ShowToast("删除失败");
//                                        }
//                                    }
//                                }
//                            }, new Response.ErrorListener() {
//                                @Override
//                                public void onErrorResponse(VolleyError volleyError) {
//                                }
//                            }) {
//                                @Override
//                                public Map<String, String> getHeaders() throws AuthFailureError {
//                                    Map<String, String> headers = new HashMap<String, String>();
//                                    headers.put("SECAuthorization", token);
//                                    headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
//                                    return headers;
//                                }
//                            };
//                            mRequestQueue.add(request);
//                        }
//                    });
//                    dialog.setCanceledOnTouchOutside(true);
//                    dialog.setCancelable(true);
//                    dialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//                    dialog.show();

                }
            });
        }

        return rootView;
    }

    private void initData() {
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading", true);
        if (NetworkUtils.isNetworkAvailable(mContext)) {
            isDataLoaded = true;
            mNetErrorView.setVisibility(View.GONE);
            loadingView.setVisibility(View.GONE);
            FetchOrderList(mCurrentPage = 0, mCurrentType);
        } else {
            if (mDialog != null)
                mDialog.dismiss();
            mEmptyView.setVisibility(View.GONE);
            mNetErrorView.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.ll_order_detail:
//                OrderDetailFragment orderDetailFragment=OrderDetailFragment.newInstance("",0);
//                ((MainActivity)getActivity()).navigationToFragment(orderDetailFragment);
//                break;
            case R.id.netReload:
                initData();
                break;
        }
    }

    private void FetchOrderList(int page, String type) {
        Map<String, String> params = new HashMap();//28129
        params.put("userId",mUserId);//64410//16665
        params.put("clientType", "ANDROID");
        params.put("clientVersion", "45");
        params.put("businessType", "01");
        params.put("type", type);
        params.put("pageNo", page + "");
        params.put("pageSize", "10");// ZhaiDou.URL_ORDER_LIST,
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.URL_ORDER_LIST, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (mDialog != null) mDialog.dismiss();
                isDataLoaded = false;
                int status = jsonObject.optInt("status");
                String message = jsonObject.optString("message");
                if (status==200){
                    JSONArray dataArray = jsonObject.optJSONArray("data");
                    int pageNo = jsonObject.optInt("pageNo");
                    if (dataArray != null && dataArray.length() < 10) {
                        ShowToast("订单加载完毕");
                        mListView.onRefreshComplete();
                        mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    }
                    List<Order1> orderList = JSON.parseArray(dataArray.toString(), Order1.class);
                    if (pageNo == 0) {
                        mOrderList.clear();
                        timerMapStamp.clear();
                    }
                    mOrderList.addAll(orderList);
                    if (mOrderList.size() > 0) {
                        handler.sendEmptyMessage(UPDATE_ORDER_LIST);
                    } else {
                        mListView.setVisibility(View.GONE);
                        loadingView.setVisibility(View.VISIBLE);
                        mEmptyView.setVisibility(View.VISIBLE);
                    }
                }else {
                    ShowToast(message);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (mDialog != null) mDialog.dismiss();
                if (allOrderAdapter.getCount()>0){
                    if (timer == null) {
                        timer = new MyTimer(15 * 60 * 1000, 1000);
                    }
                    timer.start();
                }
                Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        System.out.println("OrderAllOrdersFragment.onPullDownToRefresh");
        FetchOrderList(mCurrentPage = 0, mCurrentType);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        System.out.println("OrderAllOrdersFragment.onPullUpToRefresh");
        FetchOrderList(++mCurrentPage, mCurrentType);
    }

    public class AllOrderAdapter extends BaseListAdapter<Order1> {
        public AllOrderAdapter(Context context, List<Order1> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_order_return, null);
            TextView tv_order_time = ViewHolder.get(convertView, R.id.tv_order_time);
            TextView tv_order_number = ViewHolder.get(convertView, R.id.tv_order_number);
            TextView tv_order_amount = ViewHolder.get(convertView, R.id.tv_order_amount);
            TextView tv_order_status = ViewHolder.get(convertView, R.id.tv_order_status);
            ImageView iv_order_img = ViewHolder.get(convertView, R.id.iv_order_img);
            TextView btn1 = ViewHolder.get(convertView, R.id.bt_logistics);
            TextView btn2 = ViewHolder.get(convertView, R.id.bt_received);
            ImageView iv_delete = ViewHolder.get(convertView, R.id.iv_delete);
            RelativeLayout ll_btn = ViewHolder.get(convertView, R.id.rl_btn);
//            Order order = getList().get(position);
//            tv_order_time.setText(order.getCreated_at_for());
//            tv_order_number.setText(order.getNumber());
//            tv_order_amount.setText("￥" + ToolUtils.isIntPrice("" + order.getAmount()));
//            tv_order_status.setText(order.getStatus_ch());
//            ToolUtils.setImageCacheUrl(order.getImg(), iv_order_img, R.drawable.icon_loading_defalut);
//            switch (Integer.parseInt(order.getStatus())) {
//                case ZhaiDou.STATUS_UNPAY:
//                    iv_delete.setVisibility(View.GONE);
//                    tv_order_status.setText("未付款");
//                    ll_btn.setVisibility(View.VISIBLE);
//                    btn1.setVisibility(View.GONE);
//                    btn2.setVisibility(View.VISIBLE);
//                    if (btn2.getTag() == null)
//                        btn2.setTag(order.getOver_at());
//                    long l = Long.parseLong(btn2.getTag() + "");
//
//                    if (l > 0) {
//                        if (timeStmp > 0 && timerMap != null && (timerMap.get(position) == null || !timerMap.get(position))) {
//                            l = l - timeStmp;
//                            btn2.setTag(l);
//                            order.setOver_at(l);
//                            timerMap.put(position, true);
//                        } else {
//                            btn2.setTag(Long.parseLong(btn2.getTag() + "") - 1);
//                            order.setOver_at(Long.parseLong(btn2.getTag() + "") - 1);
//                        }
//                        btn2.setText("支付" + new SimpleDateFormat("mm:ss").format(new Date(l * 1000)));
//                    } else {
//                        btn2.setText("超时过期");
//                        order.setStatus(ZhaiDou.STATUS_DEAL_CLOSE + "");
//                    }
//                    btn2.setBackgroundResource(R.drawable.btn_red_click_selector);
//                    break;
//                case ZhaiDou.STATUS_PAYED:
//                    ll_btn.setVisibility(View.VISIBLE);
//                    iv_delete.setVisibility(View.GONE);
//                    tv_order_status.setText("已付款");
//                    btn2.setVisibility(View.GONE);
//                    btn1.setVisibility(order.isZero() ? View.GONE : View.VISIBLE);
//                    btn1.setText("申请退款");
//                    btn1.setBackgroundResource(R.drawable.btn_green_click_bg);
//                    break;
//                case ZhaiDou.STATUS_OVER_TIME:
//                    iv_delete.setVisibility(View.VISIBLE);
//                    tv_order_status.setText("交易关闭");
//                    ll_btn.setVisibility(View.GONE);
//                    break;
//                case ZhaiDou.STATUS_ORDER_CANCEL_PAYED:
//                    iv_delete.setVisibility(View.GONE);
//                    tv_order_status.setText("申请退款");
//                    ll_btn.setVisibility(View.GONE);
//                    break;
//                case ZhaiDou.STATUS_DELIVERY:
//                    iv_delete.setVisibility(View.GONE);
//                    tv_order_status.setText("已发货");
//                    ll_btn.setVisibility(View.VISIBLE);
//                    btn1.setVisibility(View.VISIBLE);
//                    btn2.setVisibility(View.VISIBLE);
//                    btn2.setText("确认收货");
//                    btn1.setText("查看物流");
//                    btn1.setBackgroundResource(R.drawable.btn_green_click_bg);
//                    btn2.setBackgroundResource(R.drawable.btn_red_click_selector);
//                    break;
//                case ZhaiDou.STATUS_DEAL_SUCCESS:
//                    iv_delete.setVisibility(View.VISIBLE);
//                    tv_order_status.setText("交易完成");
//                    ll_btn.setVisibility(View.VISIBLE);
//                    btn1.setVisibility(View.VISIBLE);
//                    btn2.setVisibility(order.isZero() ? View.GONE : View.VISIBLE);
//                    btn2.setText("申请退货");
//                    btn2.setBackgroundResource(R.drawable.btn_green_click_bg);
//                    btn1.setText("查看物流");
//                    btn1.setBackgroundResource(R.drawable.btn_green_click_bg);
//                    break;
//                case ZhaiDou.STATUS_APPLY_GOOD_RETURN:
//                    iv_delete.setVisibility(View.GONE);
//                    tv_order_status.setText("申请退货");
//                    ll_btn.setVisibility(View.GONE);
//                    break;
//                case ZhaiDou.STATUS_GOOD_RETURNING:
//                    iv_delete.setVisibility(View.GONE);
//                    tv_order_status.setText("退货中");
//                    ll_btn.setVisibility(View.GONE);
//                    break;
//                case ZhaiDou.STATUS_RETURN_GOOD_SUCCESS:
//                    iv_delete.setVisibility(View.VISIBLE);
//                    tv_order_status.setText("退货成功");
//                    ll_btn.setVisibility(View.GONE);
//                    break;
//                case ZhaiDou.STATUS_UNPAY_CANCEL:
//                    iv_delete.setVisibility(View.VISIBLE);
//                    tv_order_status.setText("交易关闭");
//                    ll_btn.setVisibility(View.GONE);
//                    break;
//                case ZhaiDou.STATUS_DEAL_CLOSE:
//                    iv_delete.setVisibility(View.VISIBLE);
//                    tv_order_status.setText("交易关闭");
//                    ll_btn.setVisibility(View.GONE);
//                    break;
//                case ZhaiDou.STATUS_RETURN_MONEY_SUCCESS:
//                    iv_delete.setVisibility(View.VISIBLE);
//                    tv_order_status.setText("退款成功");
//                    ll_btn.setVisibility(View.GONE);
//                    break;
//            }
            Order1 order = getList().get(position);
            tv_order_time.setText(order.creationTime);
            tv_order_number.setText(order.orderCode);
            tv_order_amount.setText("￥" + order.orderPayAmount);
            tv_order_status.setText(order.orderShowStatus);
            ToolUtils.setImageCacheUrl(order.childOrderPOList.get(0).orderItemPOList.get(0).pictureMiddleUrl, iv_order_img, R.drawable.icon_loading_defalut);
            switch (order.status) {
                case ZhaiDou.STATUS_UNPAY:
                    iv_delete.setVisibility(View.GONE);
                    tv_order_status.setText("未付款");
                    ll_btn.setVisibility(View.VISIBLE);
                    btn1.setVisibility(View.VISIBLE);
                    btn2.setVisibility(View.VISIBLE);
                    btn1.setText("取消订单");
                    if (timerMapStamp.get(position) == null) {
                        timerMapStamp.put(position, order.orderRemainingTime);
                    }
                    long l = Long.parseLong(timerMapStamp.get(position) + "");
                    if (l > 0) {
                        timerMapStamp.put(position, timerMapStamp.get(position) - 1);
                        order.orderRemainingTime = timerMapStamp.get(position) - 1;

                        btn2.setText("支付" + new SimpleDateFormat("mm:ss").format(new Date(l * 1000)));
                        btn2.setBackgroundResource(R.drawable.btn_red_click_selector);
                    } else {
                        btn2.setText("超时过期");
                        btn2.setBackgroundResource(R.drawable.btn_no_click_selector);
                    }
                    break;
                case ZhaiDou.STATUS_OVER_TIME:
                    iv_delete.setVisibility(View.VISIBLE);
                    tv_order_status.setText("交易关闭");
                    ll_btn.setVisibility(View.GONE);
                    break;
                case ZhaiDou.STATUS_ORDER_CANCEL_PAYED:
                    iv_delete.setVisibility(View.GONE);
                    tv_order_status.setText("申请退款");
                    ll_btn.setVisibility(View.GONE);
                    break;
                case ZhaiDou.STATUS_DELIVERY:
                    iv_delete.setVisibility(View.GONE);
                    tv_order_status.setText("已发货");
                    ll_btn.setVisibility(View.VISIBLE);
                    btn1.setVisibility(View.VISIBLE);
                    btn2.setVisibility(View.VISIBLE);
                    btn2.setText("确认收货");
                    btn1.setText("查看物流");
                    btn1.setBackgroundResource(R.drawable.btn_green_click_bg);
                    btn2.setBackgroundResource(R.drawable.btn_red_click_selector);
                    break;
                case ZhaiDou.STATUS_DEAL_SUCCESS:
                    iv_delete.setVisibility(View.VISIBLE);
                    tv_order_status.setText("交易完成");
                    ll_btn.setVisibility(View.GONE);
                    btn1.setVisibility(View.VISIBLE);
                    btn2.setText("申请退货");
                    btn2.setBackgroundResource(R.drawable.btn_green_click_bg);
                    btn1.setText("查看物流");
                    btn1.setBackgroundResource(R.drawable.btn_green_click_bg);
                    break;
                case ZhaiDou.STATUS_APPLY_GOOD_RETURN:
                    iv_delete.setVisibility(View.GONE);
                    tv_order_status.setText("申请退货");
                    ll_btn.setVisibility(View.GONE);
                    break;
                case ZhaiDou.STATUS_GOOD_RETURNING:
                    iv_delete.setVisibility(View.GONE);
                    tv_order_status.setText("退货中");
                    ll_btn.setVisibility(View.GONE);
                    break;
                case ZhaiDou.STATUS_RETURN_GOOD_SUCCESS:
                    iv_delete.setVisibility(View.VISIBLE);
                    tv_order_status.setText("退货成功");
                    ll_btn.setVisibility(View.GONE);
                    break;
                case ZhaiDou.STATUS_UNPAY_CANCEL:
                    iv_delete.setVisibility(View.VISIBLE);
                    tv_order_status.setText("交易关闭");
                    ll_btn.setVisibility(View.GONE);
                    break;
                case ZhaiDou.STATUS_DEAL_CLOSE:
                    iv_delete.setVisibility(View.VISIBLE);
                    tv_order_status.setText("交易关闭");
                    ll_btn.setVisibility(View.GONE);
                    break;
                case ZhaiDou.STATUS_RETURN_MONEY_SUCCESS:
                    iv_delete.setVisibility(View.VISIBLE);
                    tv_order_status.setText("退款成功");
                    ll_btn.setVisibility(View.GONE);
                    break;
                case ZhaiDou.STATUS_UNDELIVERY:
                    iv_delete.setVisibility(View.GONE);
                    ll_btn.setVisibility(View.VISIBLE);
                    btn2.setVisibility(View.GONE);
                    btn1.setText("申请退款");
                    break;
                case ZhaiDou.STATUS_PART_DELIVERY:
                    iv_delete.setVisibility(View.GONE);
                    ll_btn.setVisibility(View.GONE);
                    break;
                case ZhaiDou.STATUS_ORDER_CANCEL:
                    iv_delete.setVisibility(View.VISIBLE);
                    ll_btn.setVisibility(View.GONE);
                    break;
                case ZhaiDou.STATUS_ORDER_APPLY_CANCEL://申请取消中
                    iv_delete.setVisibility(View.GONE);
                    ll_btn.setVisibility(View.GONE);
                    break;
            }
            return convertView;
        }
    }

    @Override
    public void onDestroyView() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        isViewDestroy = true;
        super.onDestroyView();
    }


    @Override
    public void onResume() {
//        if (timer == null)
//            timer = new MyTimer((mContext.getResources().getInteger(R.integer.timer_countdown)), 1000);
//        if (!isTimerStart) {
//            isTimerStart = true;
//            timer.start();
//        }
        System.out.println("OrderAllOrdersFragment.onResume");
        if (!isDataLoaded) {
            FetchOrderList(mCurrentPage = 0, mCurrentType);
        }
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_all_order));
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_all_order));
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        System.out.println("OrderAllOrdersFragment.onStop");
        if (timer != null) {
            timer.cancel();
            isTimerStart = false;
        }
        super.onStop();
    }

    private class MyTimer extends CountDownTimer {
        private MyTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            handler.sendEmptyMessage(UPDATE_COUNT_DOWN_TIME);
        }

        @Override
        public void onFinish() {
            handler.sendEmptyMessage(UPDATE_UI_TIMER_FINISH);
        }
    }
}
