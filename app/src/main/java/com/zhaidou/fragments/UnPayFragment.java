package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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
import com.zhaidou.model.CountTime;
import com.zhaidou.model.Order;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UnPayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UnPayFragment extends BaseFragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private RequestQueue mRequestQueue;
    private List<Order> orders = new ArrayList<Order>();
    private final int UPDATE_UNPAY_LIST = 1;
    private ListView mListView;
    private UnPayAdapter unPayAdapter;
    private final int UPDATE_COUNT_DOWN_TIME = 2;
    private final int UPDATE_UI_TIMER_FINISH=3;
    private final String STATUS_UNPAY_LIST = "0";

    private Dialog mDialog;
    private LinearLayout loadingView;

    private Map<Integer, View> mHashMap = new WeakHashMap<Integer, View>();
    private View rootView;
    private MyTimer timer;
    private String token;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_UNPAY_LIST:
                    unPayAdapter.notifyDataSetChanged();
                    timer = new MyTimer(15 * 60 * 1000, 1000);
                    timer.start();
                    break;
                case UPDATE_COUNT_DOWN_TIME:
                    loadingView.setVisibility(View.GONE);
                    unPayAdapter.notifyDataSetChanged();
                    break;
                case UPDATE_UI_TIMER_FINISH:

                    break;
            }
        }
    };

    public static UnPayFragment newInstance(String param1, String param2) {
        UnPayFragment fragment = new UnPayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public UnPayFragment() {
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
        if (null != rootView) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent) {
                parent.removeView(rootView);
            }
        } else {
            rootView = inflater.inflate(R.layout.fragment_unpay, container, false);
//            initView(rootView);

            mDialog= CustomLoadingDialog.setLoadingDialog(getActivity(),"loading");
            loadingView=(LinearLayout)rootView.findViewById(R.id.loadingView);
            mListView = (ListView) rootView.findViewById(R.id.lv_unpaylist);
            unPayAdapter = new UnPayAdapter(getActivity(), orders);
            mListView.setAdapter(unPayAdapter);
            token=(String) SharedPreferencesUtil.getData(getActivity(),"token","");
            mRequestQueue = Volley.newRequestQueue(getActivity());

            unPayAdapter.setOnInViewClickListener(R.id.ll_unpay, new BaseListAdapter.onInternalClickListener() {
                @Override
                public void OnClickListener(View parentV, View v, Integer position, Object values) {
                    final Order order = (Order) values;
                    TextView textView=(TextView)v.findViewById(R.id.bt_order_timer);
                    OrderDetailFragment orderDetailFragment = OrderDetailFragment.newInstance(order.getOrderId() + "", order.getOver_at(),order);
                    ((MainActivity) getActivity()).navigationToFragment(orderDetailFragment);
                    orderDetailFragment.setOrderListener(new OrderDetailFragment.OrderListener() {
                        @Override
                        public void onOrderStatusChange(Order o) {
                            order.setStatus(o.getStatus());
                            order.setStatus_ch("已取消");
                        }
                    });
                }
            });
        }
        FetchData();
        return rootView;
    }

    private void FetchData() {
        orders.clear();
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.URL_ORDER_LIST, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                if (mDialog!=null) mDialog.dismiss();
                Log.i("jsonObject----------->", jsonObject.toString());
                if (jsonObject != null)
                {
                    JSONArray orderArr = jsonObject.optJSONArray("orders");
                    if (orderArr != null && orderArr.length() > 0)
                    {
                        for (int i = 0; i < orderArr.length(); i++)
                        {
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
                            if (STATUS_UNPAY_LIST.equalsIgnoreCase(status))
                                orders.add(order);
                        }
                        handler.sendEmptyMessage(UPDATE_UNPAY_LIST);
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
                headers.put("SECAuthorization",token);
                return headers;
            }
        };
        mRequestQueue.add(request);
    }

    private class UnPayAdapter extends BaseListAdapter<Order> {

        public UnPayAdapter(Context context, List<Order> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            convertView = mHashMap.get(position);
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_pre_pay, null);
            TextView mOrderTime = ViewHolder.get(convertView, R.id.tv_order_time);
            TextView mOrderNum = ViewHolder.get(convertView, R.id.tv_order_number);
            TextView mOrderAmount = ViewHolder.get(convertView, R.id.tv_order_amount);
            TextView mOrderStatus = ViewHolder.get(convertView, R.id.order_status);
            TextView mTimerBtn = ViewHolder.get(convertView, R.id.bt_order_timer);
            ImageView mOrderImg = ViewHolder.get(convertView, R.id.iv_order_img);
            Order item = orders.get(position);
            mOrderTime.setText("订单时间 " + item.getCreated_at_for());
            mOrderNum.setText("订单编号 " + item.getNumber());
            mOrderAmount.setText("订单金额 " + item.getAmount());
            mOrderStatus.setText("订单状态 " + item.getStatus_ch());
            ToolUtils.setImageCacheUrl(item.getImg(), mOrderImg);


            if (mTimerBtn.getTag() == null) {
                mTimerBtn.setTag(item.getOver_at());
//                mOrderTime.setTag(System.currentTimeMillis());
            }

            long l = Long.parseLong(mTimerBtn.getTag() + "");
            long day = 24 * 3600 * 1000;
            long hour = 3600 * 1000;
            long minute = 60 * 1000;
            //两个日期想减得到天数
            long dayCount = l / day;
            long hourCount = (l - (dayCount * day)) / hour;
            long minCount = (l - (dayCount * day) - (hour * hourCount)) / minute;
            long secondCount = (l - (dayCount * day) - (hour * hourCount) - (minCount * minute)) / 1000;
//            if ((System.currentTimeMillis()-Long.parseLong(mOrderTime.getTag()+""))>=1000){
            if (minCount>0||secondCount>0){
                mTimerBtn.setText("支付" + minCount + ":" + secondCount + "");
            }else {
                mTimerBtn.setText("超时过期");
            }

            mTimerBtn.setTag(Long.parseLong(mTimerBtn.getTag() + "") - 1000);
            item.setOver_at(Long.parseLong(mTimerBtn.getTag() + "") - 1000);
//                mOrderTime.setTag(System.currentTimeMillis());
//            }

            mHashMap.put(position, convertView);
            return convertView;
        }
    }

    private class MyTimer extends CountDownTimer {
        private MyTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
//            Log.i("onTick------------>", l + "");
            handler.sendEmptyMessage(UPDATE_COUNT_DOWN_TIME);
        }

        @Override
        public void onFinish() {
//            Log.i("onFinish---------->", "onFinish");
//            mHandler.sendEmptyMessage(UPDATE_UI_TIMER_FINISH);
        }
    }

    public interface TimerListener {
        public void onTick(TextView mTimerView, CountTime countTime, long l);
    }

    @Override
    public void onDestroyView() {
        if (timer!=null){
            timer.cancel();
            timer=null;
        }
        super.onDestroyView();
    }
}
