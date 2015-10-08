package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
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
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Order;
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
import java.util.WeakHashMap;


public class OrderUnPayFragment extends BaseFragment implements View.OnClickListener{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private int count;

    private RequestQueue mRequestQueue;
    private List<Order> orders = new ArrayList<Order>();
    private final int UPDATE_UNPAY_LIST = 1;
    private ListView mListView;
    private UnPayAdapter unPayAdapter;
    private final int UPDATE_COUNT_DOWN_TIME = 2;
    private final int UPDATE_UI_TIMER_FINISH = 3;
    private final String STATUS_UNPAY_LIST = "0";

    private Dialog mDialog;
    private LinearLayout loadingView;

    private Map<Integer, View> mHashMap = new WeakHashMap<Integer, View>();
    private View rootView;
    private MyTimer timer;
    private String token;
    private Context mContext;
    private boolean isTimerStart = false;
    private long preTime = 0;
    private long timeStmp = 0;
    private View mEmptyView,mNetErrorView;
    private Map<Integer, Boolean> timerMap = new HashMap<Integer, Boolean>();
    private BackCountListener backClickListener;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_UNPAY_LIST:
                    if (orders != null && orders.size() > 0) {
                        mListView.setVisibility(View.VISIBLE);
                        loadingView.setVisibility(View.GONE);
                        unPayAdapter.notifyDataSetChanged();
                    } else {
                        mListView.setVisibility(View.GONE);
                        mEmptyView.setVisibility(View.VISIBLE);
                        loadingView.setVisibility(View.VISIBLE);
                    }
                    if (count!=orders.size())
                    {
                        Intent intent=new Intent(ZhaiDou.IntentRefreshUnPayTag);
                        mContext.sendBroadcast(intent);
                    }

                    break;
                case UPDATE_COUNT_DOWN_TIME:
                    unPayAdapter.notifyDataSetChanged();
                    break;
                case UPDATE_UI_TIMER_FINISH:
                    break;
            }
        }
    };

    public static OrderUnPayFragment newInstance(String param1, String param2,int count) {
        OrderUnPayFragment fragment = new OrderUnPayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putInt("count", count);
        fragment.setArguments(args);
        return fragment;
    }

    public OrderUnPayFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            count= getArguments().getInt("count");
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
            mContext = getActivity();
            loadingView = (LinearLayout) rootView.findViewById(R.id.loadingView);
            mEmptyView=rootView.findViewById(R.id.nullline);
            mNetErrorView=rootView.findViewById(R.id.nullNetline);
            rootView.findViewById(R.id.netReload).setOnClickListener(this);
            mListView = (ListView) rootView.findViewById(R.id.lv_unpaylist);
            unPayAdapter = new UnPayAdapter(getActivity(), orders);
            mListView.setAdapter(unPayAdapter);
            token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
            mRequestQueue = Volley.newRequestQueue(getActivity());

            unPayAdapter.setOnInViewClickListener(R.id.ll_unpay, new BaseListAdapter.onInternalClickListener() {
                @Override
                public void OnClickListener(View parentV, View v, Integer position, Object values) {
                    final Order order = (Order) values;
                    final TextView btn2 = (TextView) v.findViewById(R.id.bt_order_timer);
                    if (btn2.getTag() != null)
                        preTime = Long.parseLong(btn2.getTag().toString());
                    OrderDetailFragment orderDetailFragment = OrderDetailFragment.newInstance(order.getOrderId() + "", order.getOver_at(), order,0);
                    ((MainActivity) getActivity()).navigationToFragment(orderDetailFragment);
                    orderDetailFragment.setOrderListener(new OrderDetailFragment.OrderListener() {
                        @Override
                        public void onOrderStatusChange(Order o) {
                            if (o.getStatus().equals(""+ZhaiDou.STATUS_PAYED))
                            {
                                orders.remove(order);
                                if ( orders.size() <1)
                                {
                                    mListView.setVisibility(View.GONE);
                                    loadingView.setVisibility(View.VISIBLE);
                                }
                            }
                            else
                            {
                                long time = o.getOver_at();
                                order.setStatus(o.getStatus());
                                order.setOver_at(o.getOver_at());
                                if (!isTimerStart) {
                                    timeStmp = preTime - time;
                                    timerMap.clear();
                                } else {
                                    btn2.setTag(o.getOver_at());
                                }
                            }
                        }
                    });
                }
            });
            unPayAdapter.setOnInViewClickListener(R.id.bt_order_timer, new BaseListAdapter.onInternalClickListener() {
                @Override
                public void OnClickListener(View parentV, View v, Integer position, Object values) {
                    final Order order = (Order) values;
                    TextView textView = (TextView) v;
                    if (mContext.getResources().getString(R.string.timer_finish).equalsIgnoreCase(textView.getText().toString())) {
                        ShowToast(mContext.getResources().getString(R.string.order_had_order_time));
                        return;
                    }
                    ShopPaymentFragment shopPaymentFragment = ShopPaymentFragment.newInstance(order.getOrderId(), order.getAmount(), 0, order.getOver_at(), order, 2);
                    ((BaseActivity) getActivity()).navigationToFragment(shopPaymentFragment);
                    shopPaymentFragment.setOrderListener(new Order.OrderListener()
                    {
                        @Override
                        public void onOrderStatusChange(Order ordera)
                        {
                            if (ordera.getStatus().equals(""+ZhaiDou.STATUS_PAYED))
                            {
                                orders.remove(order);
                                if ( orders.size() <1)
                                {
                                    mListView.setVisibility(View.GONE);
                                    loadingView.setVisibility(View.VISIBLE);
                                }
                            }
                            else
                            {
                                long time = ordera.getOver_at();
                                order.setStatus(ordera.getStatus());
                                order.setOver_at(ordera.getOver_at());
                                if (!isTimerStart)
                                {
                                    timeStmp = preTime - time;
                                    timerMap.clear();
                                }
                            }
                        }
                    });
                }
            });
            initData();
        }

        return rootView;
    }

    private void initData() {
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
        if (NetworkUtils.isNetworkAvailable(mContext)) {
            mNetErrorView.setVisibility(View.GONE);
            loadingView.setVisibility(View.GONE);
            FetchData();
        } else {
            if (mDialog != null)
                mDialog.dismiss();
            mEmptyView.setVisibility(View.GONE);
            mNetErrorView.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.VISIBLE);
        }
    }


    private void FetchData() {
        token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        orders.clear();
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.URL_ORDER_LIST + "?status=0", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.i("FetchData------------------>",jsonObject.toString());
                if (mDialog != null)
                    mDialog.dismiss();
                if (jsonObject != null)
                {
                    JSONArray orderArr = jsonObject.optJSONArray("orders");
                    if (orderArr != null && orderArr.length() > 0)
                    {
                        orders.clear();
                        for (int i = 0; i < orderArr.length(); i++)
                        {
                            JSONObject orderObj = orderArr.optJSONObject(i);
                            int id = orderObj.optInt("id");
                            String number = orderObj.optString("number");
                            double amount = orderObj.optDouble("amount");
                            String status = orderObj.optString("status");
                            String status_ch = orderObj.optString("status_ch");
                            String created_at = orderObj.optString("created_at");
                            String created_at_for = orderObj.optString("created_at_for");
                            String img = orderObj.optString("merch_img");
                            long over_at = orderObj.optLong("over_at");
                            Order order = new Order(id, number, amount, status, status_ch, created_at_for, created_at, "", 0);
                            order.setImg(img);
                            order.setOver_at(over_at);
                            if (over_at>0)
                            {
                                orders.add(order);
                            }
                        }
                        handler.sendEmptyMessage(UPDATE_UNPAY_LIST);
                    } else {
                        handler.sendEmptyMessage(UPDATE_UNPAY_LIST);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (mDialog!=null)
                mDialog.dismiss();
                if (getActivity() != null)
                    Toast.makeText(getActivity(), mContext.getResources().getString(R.string.network_load_error), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.netReload:
                initData();
                break;
        }
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
            TextView mOrderStatus = ViewHolder.get(convertView, R.id.tv_order_status);
            TextView mTimerBtn = ViewHolder.get(convertView, R.id.bt_order_timer);
            ImageView mOrderImg = ViewHolder.get(convertView, R.id.iv_order_img);
            RelativeLayout mBottomLayout = ViewHolder.get(convertView, R.id.rl_pay);
            Order item = orders.get(position);
            mOrderTime.setText(item.getCreated_at_for());
            mOrderNum.setText(item.getNumber());
            mOrderAmount.setText("￥" + item.getAmount());
            mOrderStatus.setText(item.getStatus_ch());
            ToolUtils.setImageCacheUrl(item.getImg(), mOrderImg);


            if (mTimerBtn.getTag() == null) {
                mTimerBtn.setTag(item.getOver_at());
            }

            long l = Long.parseLong(mTimerBtn.getTag() + "");
            if (("" + ZhaiDou.STATUS_UNPAY).equalsIgnoreCase(item.getStatus()))
            {
                if (l > 0)
                {
                    if (timeStmp > 0 && timerMap != null && (timerMap.get(position) == null || !timerMap.get(position))) {
                        l = l - timeStmp;
                        mTimerBtn.setTag(l);
                        item.setOver_at(l);
                        timerMap.put(position, true);
                    } else {
                        mTimerBtn.setTag(Long.parseLong(mTimerBtn.getTag() + "") - 1);
                        item.setOver_at(Long.parseLong(mTimerBtn.getTag() + "") - 1);
                    }
                    mTimerBtn.setText(String.format(getResources().getString(R.string.timer_start), new SimpleDateFormat("mm:ss").format(new Date(l * 1000))));
                }
                else {
                    mTimerBtn.setText(mContext.getResources().getString(R.string.timer_finish));
                    mOrderStatus.setText(mContext.getResources().getString(R.string.order_colse));
                    mTimerBtn.setBackgroundResource(R.drawable.btn_no_click_selector);
                    item.setStatus(ZhaiDou.STATUS_UNPAY + "");
                    item.setOver_at(0);//剩余时间
//                    //刷新代付款数量显示
//                    Intent intent = new Intent(ZhaiDou.IntentRefreshUnPayDesTag);
//                    mContext.sendBroadcast(intent);
                }
            }
            else {
                mOrderStatus.setText(mContext.getResources().getString(R.string.order_colse));
                mTimerBtn.setText(mContext.getResources().getString(R.string.timer_finish));
                mOrderStatus.setText(mContext.getResources().getString(R.string.order_colse));
                mTimerBtn.setBackgroundResource(R.drawable.btn_no_click_selector);
                item.setOver_at(0);//剩余时间
//                //刷新代付款数量显示
//                Intent intent = new Intent(ZhaiDou.IntentRefreshUnPayDesTag);
//                mContext.sendBroadcast(intent);
            }

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
            handler.sendEmptyMessage(UPDATE_COUNT_DOWN_TIME);
        }

        @Override
        public void onFinish() {
//            mHandler.sendEmptyMessage(UPDATE_UI_TIMER_FINISH);
        }
    }

    @Override
    public void onResume() {
        Log.i("AllOrdersFragment----------->", "onResume");

        if (!isTimerStart) {
            if (timer == null)
                timer = new MyTimer(15 * 60 * 1000, 1000);
            isTimerStart = true;
            timer.start();
            Log.i("onResume--->timer.start();----------->","timer.start()---------->");
        }
        super.onResume();
    }

    @Override
    public void onStop() {
        if (timer != null) {
            timer.cancel();
            isTimerStart = false;
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        if (orders!=null)
            if (backClickListener!=null)
            {
                for (int i = 0; i <orders.size() ; i++)
                {
                    if (orders.get(i).getOver_at()<=0)
                    {
                        orders.remove(orders.get(i));
                    }
                }
                backClickListener.onBackCount(orders.size());
            }
        super.onDestroyView();
    }

    public void setBackClickListener(BackCountListener backClickListener) {
        this.backClickListener = backClickListener;
    }

    public interface BackCountListener{
        public void onBackCount(int count);
    }






}