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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Order;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AllOrdersFragment extends BaseFragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;

    private Dialog mDialog;
    private LinearLayout loadingView;

    private RequestQueue mRequestQueue;
    private ListView mListView;
    private List<Order> orders = new ArrayList<Order>();
    AllOrderAdapter allOrderAdapter;
    private final int UPDATE_ORDER_LIST = 1;
    private final int UPDATE_COUNT_DOWN_TIME = 2;
    private final int UPDATE_UI_TIMER_FINISH = 3;
    private MyTimer timer;
    private View rootView;
    private String token;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_ORDER_LIST:
                    allOrderAdapter.notifyDataSetChanged();
                    if (timer == null)
                        timer = new MyTimer(15 * 60 * 1000, 1000);
                    timer.start();
                    break;
                case UPDATE_COUNT_DOWN_TIME:
                    loadingView.setVisibility(View.GONE);
                    allOrderAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    public static AllOrdersFragment newInstance(String param1, String param2) {
        AllOrdersFragment fragment = new AllOrdersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public AllOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i("AllOrdersFragment------------>", "onCreateView");
        if (null != rootView) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent) {
                parent.removeView(rootView);
            }
        } else {
            rootView = inflater.inflate(R.layout.fragment_all_orders, container, false);

            mDialog= CustomLoadingDialog.setLoadingDialog(getActivity(), "loading");
            loadingView=(LinearLayout)rootView.findViewById(R.id.loadingView);
            mListView = (ListView) rootView.findViewById(R.id.lv_all_orderlist);
            mRequestQueue = Volley.newRequestQueue(getActivity());
            allOrderAdapter = new AllOrderAdapter(getActivity(), orders);
            mListView.setAdapter(allOrderAdapter);
            token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
            FetchAllOrder();

            allOrderAdapter.setOnInViewClickListener(R.id.orderlayout, new BaseListAdapter.onInternalClickListener() {
                @Override
                public void OnClickListener(View parentV, View v, Integer position, Object values) {
                    final Order order = (Order) values;
                    Log.i("order--------->", order.toString());
                    final TextView btn2 = (TextView) parentV.findViewById(R.id.bt_received);
//                    if (ZhaiDou.STATUS_DEAL_SUCCESS == Integer.parseInt(order.getStatus())) {
//                        AfterSaleFragment afterSaleFragment = AfterSaleFragment.newInstance(order.getOrderId() + "", "return_good");
//                        ((MainActivity) getActivity()).navigationToFragment(afterSaleFragment);
//                        return;
//                    }
                    OrderDetailFragment orderDetailFragment = OrderDetailFragment.newInstance(order.getOrderId() + "", order.getOver_at(), order);
                    ((MainActivity) getActivity()).navigationToFragment(orderDetailFragment);
                    orderDetailFragment.setOrderListener(new OrderDetailFragment.OrderListener() {
                        @Override
                        public void onOrderStatusChange(Order o) {
                            Log.i("orderDetailFragment----------->", o.toString());
                            order.setStatus(o.getStatus());
                            order.setStatus_ch(o.getStatus_ch());
                            btn2.setTag(o.getOver_at());
                        }
                    });
                }
            });
            allOrderAdapter.setOnInViewClickListener(R.id.bt_logistics, new BaseListAdapter.onInternalClickListener() {
                @Override
                public void OnClickListener(View parentV, View v, Integer position, Object values) {
                    Order order = (Order) values;
                    Log.i("v---------->", v.toString());
                    TextView textView = (TextView) v;
                    if ("查看物流".equalsIgnoreCase(textView.getText().toString())) {
                        LogisticsMsgFragment logisticsMsgFragment = LogisticsMsgFragment.newInstance("", "");
                        ((MainActivity) getActivity()).navigationToFragment(logisticsMsgFragment);
                    } else if ("申请退款".equalsIgnoreCase(textView.getText().toString())) {
                        AfterSaleFragment afterSaleFragment = AfterSaleFragment.newInstance(order.getOrderId() + "", "return_money");
                        ((MainActivity) getActivity()).navigationToFragment(afterSaleFragment);
                    }
                }
            });
            allOrderAdapter.setOnInViewClickListener(R.id.bt_received, new BaseListAdapter.onInternalClickListener() {
                @Override
                public void OnClickListener(View parentV, View v, Integer position, Object values) {
                    final Order order = (Order) values;
                    if ((""+ZhaiDou.STATUS_DEAL_SUCCESS).equalsIgnoreCase(order.getStatus())){
                        AfterSaleFragment afterSaleFragment=AfterSaleFragment.newInstance(order.getOrderId()+"",order.getStatus()+"");
                        ((MainActivity) getActivity()).navigationToFragment(afterSaleFragment);
                        return;
                    }
                    final Dialog dialog = new Dialog(getActivity(), R.style.custom_dialog);

                    View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_custom_collect_hint, null);
                    TextView textView = (TextView) view.findViewById(R.id.tv_msg);
                    textView.setText("是否确认收货?");
                    TextView cancelTv = (TextView) view.findViewById(R.id.cancelTv);
                    cancelTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });

                    TextView okTv = (TextView) view.findViewById(R.id.okTv);
                    okTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.URL_ORDER_LIST + "/" + order.getOrderId() + "/update_status?status=5", new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    JSONObject orderObj = jsonObject.optJSONObject("order");
                                    if (orderObj != null) {
                                        String status = orderObj.optString("status");
                                        order.setStatus(status);
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {

                                }
                            }) {
                                @Override
                                public Map<String, String> getHeaders() throws AuthFailureError {
                                    Map<String, String> headers = new HashMap<String, String>();
                                    headers.put("SECAuthorization", token);
                                    return headers;
                                }
                            };
                            mRequestQueue.add(request);
                            dialog.dismiss();
                        }
                    });
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.setCancelable(true);
                    dialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    dialog.show();
                }
            });
            allOrderAdapter.setOnInViewClickListener(R.id.iv_delete, new BaseListAdapter.onInternalClickListener() {
                @Override
                public void OnClickListener(View parentV, View v, Integer position, Object values) {
                    final Order order=(Order)values;
                    JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST,ZhaiDou.URL_ORDER_LIST+"/"+order.getOrderId()+"/delete_order",new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            Log.i("jsonObject---->",jsonObject.toString());
                            if (jsonObject!=null){
                                int status=jsonObject.optInt("status");
                                if (201==status){
                                    orders.remove(order);
                                }else if (400==status){
                                    ShowToast("删除失败");
                                }
                            }
                        }
                    },new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    }){
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> headers = new HashMap<String, String>();
                            headers.put("SECAuthorization", token);
                            return headers;
                        }
                    };
                    mRequestQueue.add(request);
                }
            });

        }

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.ll_order_detail:
//                OrderDetailFragment orderDetailFragment=OrderDetailFragment.newInstance("",0);
//                ((MainActivity)getActivity()).navigationToFragment(orderDetailFragment);
//                break;
        }
    }

    private void FetchAllOrder() {
        Log.i("token-------------->", token);
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.URL_ORDER_LIST, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (mDialog!=null) mDialog.dismiss();
                Log.i("FetchAllOrder----------->", jsonObject.toString());
                if (jsonObject != null) {
                    JSONArray orderArr = jsonObject.optJSONArray("orders");
                    if (orderArr != null && orderArr.length() > 0) {
                        for (int i = 0; i < orderArr.length(); i++) {
                            JSONObject orderObj = orderArr.optJSONObject(i);
                            int id = orderObj.optInt("id");
                            String number = orderObj.optString("number");
                            int amount = orderObj.optInt("amount");
                            String status = orderObj.optString("status");
                            String status_ch = orderObj.optString("status_ch");
                            String created_at = orderObj.optString("created_at");
                            String created_at_for = orderObj.optString("created_at_for");
                            String img = orderObj.optString("merch_img");
                            long over_at = orderObj.optLong("over_at");
                            Order order = new Order(id, number, amount, status, status_ch, created_at_for, created_at, "", 0);
                            order.setImg(img);
                            order.setOver_at(over_at);
                            orders.add(order);
                        }
                        handler.sendEmptyMessage(UPDATE_ORDER_LIST);
                    }
                    else
                    {
                        mListView.setVisibility(View.GONE);
                        loadingView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (mDialog!=null) mDialog.dismiss();
                Toast.makeText(getActivity(),"网络异常", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("SECAuthorization", token);
                return headers;
            }
        };
        mRequestQueue.add(request);
    }

    public class AllOrderAdapter extends BaseListAdapter<Order> {
        public AllOrderAdapter(Context context, List<Order> list) {
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
            Order order = getList().get(position);
            tv_order_time.setText(order.getCreated_at_for());
            tv_order_number.setText(order.getNumber());
            tv_order_amount.setText("￥"+order.getAmount());
            tv_order_status.setText(order.getStatus_ch());
            ToolUtils.setImageCacheUrl(order.getImg(), iv_order_img);
            switch (Integer.parseInt(order.getStatus())) {
                case ZhaiDou.STATUS_UNPAY:
                    iv_delete.setVisibility(View.GONE);
                    tv_order_status.setText("未付款");
                    ll_btn.setVisibility(View.VISIBLE);
                    btn1.setVisibility(View.GONE);
                    if (btn2.getTag() == null)
                        btn2.setTag(order.getOver_at());
                    long l = Long.parseLong(btn2.getTag() + "");
                    long day = 24 * 3600 * 1000;
                    long hour = 3600 * 1000;
                    long minute = 60 * 1000;
                    //两个日期想减得到天数
                    long dayCount = l / day;
                    long hourCount = (l - (dayCount * day)) / hour;
                    long minCount = (l - (dayCount * day) - (hour * hourCount)) / minute;
                    long secondCount = (l - (dayCount * day) - (hour * hourCount) - (minCount * minute)) / 1000;
                    if (minCount > 0 || secondCount > 0) {
                        btn2.setText("支付" + minCount + ":" + secondCount + "");
                    } else {
                        btn2.setText("超时过期");
                    }
                    btn2.setBackgroundResource(R.drawable.btn_red_click_selector);
                    btn2.setTag(Long.parseLong(btn2.getTag() + "") - 1000);
                    order.setOver_at(Long.parseLong(btn2.getTag() + "") - 1000);
                    break;
                case ZhaiDou.STATUS_PAYED:
                    ll_btn.setVisibility(View.VISIBLE);
                    iv_delete.setVisibility(View.GONE);
                    tv_order_status.setText("已付款");
                    btn2.setVisibility(View.GONE);
                    btn1.setText("申请退款");
                    btn1.setBackgroundResource(R.drawable.btn_green_click_bg);
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
                    btn2.setText("确认收货");
                    btn1.setText("查看物流");
                    btn1.setBackgroundResource(R.drawable.btn_green_click_bg);
                    btn2.setBackgroundResource(R.drawable.btn_red_click_selector);
                    break;
                case ZhaiDou.STATUS_DEAL_SUCCESS:
                    iv_delete.setVisibility(View.VISIBLE);
                    tv_order_status.setText("交易完成");
                    ll_btn.setVisibility(View.VISIBLE);
                    btn1.setVisibility(View.VISIBLE);
                    btn2.setVisibility(View.VISIBLE);
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
            }
            return convertView;
        }
    }

    @Override
    public void onDestroyView() {
        Log.i("AllOrdersFragment----------->", "onDestroyView");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        Log.i("AllOrdersFragment----------->", "onResume");
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private class MyTimer extends CountDownTimer {
        private MyTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
//            Log.i("onTick----------->", l + "");
            handler.sendEmptyMessage(UPDATE_COUNT_DOWN_TIME);
        }

        @Override
        public void onFinish() {
            handler.sendEmptyMessage(UPDATE_UI_TIMER_FINISH);
        }
    }


}
