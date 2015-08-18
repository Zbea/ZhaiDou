package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.zhaidou.alipay.PayDemoActivity;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.Order;
import com.zhaidou.model.OrderItem;
import com.zhaidou.model.Receiver;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDetailFragment extends BaseFragment
{
    private static final String ARG_ID = "id";
    private static final String ARG_TIMESTMP = "timestmp";
    private static final String ARG_ORDER = "order";
    // TODO: Rename and change types of parameters
    private String mOrderId;
    private long mParam2;
    private Order mOrder;


    private RequestQueue requestQueue;
    private TextView mOrderNumber, mOrderTime, mOrderStatus,
            mReceiverName, mReceiverPhone, mReceiverAddress, mReceiverTime,
            mOrderAmount, mOrderEdit, mCancelOrder, mOrderTimer;
    private ListView mListView;
    private TextView mSaleServiceTV;
    private OrderItemAdapter orderItemAdapter;
    private final int UPDATE_COUNT_DOWN_TIME = 2;
    private final int UPDATE_UI_TIMER_FINISH = 3;
    private MyTimer timer;
    private OrderListener orderListener;

    private long timeLeft;
    int amount;
    private View rootView;
    private Context mContext;
    private List<OrderItem> orderItems = new ArrayList<OrderItem>();
    private String token;
    private FrameLayout mBottomLayout;
    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 1:
                    Order order = (Order) msg.obj;
                    mOrderNumber.setText(order.getNumber());
                    mOrderTime.setText(order.getCreated_at_for());
                    mOrderStatus.setText(order.getStatus_ch());
                    mReceiverName.setText(order.getReceiver().getName());
                    mReceiverPhone.setText(order.getReceiver().getPhone());
                    mReceiverAddress.setText(order.getReceiver().getAddress());
//                    mReceiverTime.setText(order.getReceiver().get);
                    orderItemAdapter.notifyDataSetChanged();
                    break;
                case UPDATE_COUNT_DOWN_TIME:
                    String time = (String) msg.obj;
                    mOrderTimer.setText(time);
                    break;
                case UPDATE_UI_TIMER_FINISH:
                    mOrderTimer.setText(getResources().getString(R.string.timer_finish));
                    break;
            }
        }
    };


    public static OrderDetailFragment newInstance(String id, long timestmp, Order order)
    {
        OrderDetailFragment fragment = new OrderDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, id);
        args.putLong(ARG_TIMESTMP, timestmp);
        args.putSerializable(ARG_ORDER, order);
        fragment.setArguments(args);
        return fragment;
    }

    public OrderDetailFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mOrderId = getArguments().getString(ARG_ID);
            mParam2 = getArguments().getLong(ARG_TIMESTMP);
            mOrder = (Order) getArguments().getSerializable(ARG_ORDER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        if (null != rootView)
        {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent)
            {
                parent.removeView(rootView);
            }
        } else
        {
            rootView = inflater.inflate(R.layout.fragment_order_detail, container, false);
            initView(rootView);
        }
        return rootView;
    }

    private void initView(View view)
    {
        mContext = getActivity();
        mOrderNumber = (TextView) view.findViewById(R.id.tv_order_number);
        mOrderTime = (TextView) view.findViewById(R.id.tv_order_time);
        mOrderStatus = (TextView) view.findViewById(R.id.tv_order_status);
        mReceiverName = (TextView) view.findViewById(R.id.tv_receiver_name);
        mReceiverPhone = (TextView) view.findViewById(R.id.tv_receiver_phone);
        mReceiverAddress = (TextView) view.findViewById(R.id.tv_receiver_address);
        mReceiverTime = (TextView) view.findViewById(R.id.tv_receiver_name);
        mOrderAmount = (TextView) view.findViewById(R.id.tv_order_amount);
        mOrderEdit = (TextView) view.findViewById(R.id.tv_order_edit);
        mCancelOrder = (TextView) view.findViewById(R.id.tv_cancel_order);
        mBottomLayout = (FrameLayout) view.findViewById(R.id.fl_bottom);
        mOrderTimer = (TextView) view.findViewById(R.id.tv_order_time_left);
        mOrderTimer.setOnClickListener(this);
        mListView = (ListView) view.findViewById(R.id.lv_order_list);
        orderItemAdapter = new OrderItemAdapter(getActivity(), orderItems);
        mListView.setAdapter(orderItemAdapter);
        requestQueue = Volley.newRequestQueue(getActivity());
        timer = new MyTimer(mParam2, 1000);
//        timer.start();
        FetchOrderDetail(mOrderId);

        mCancelOrder.setOnClickListener(this);
//        if (mOrder != null && "678".contains(mOrder.getStatus())) {
//            view.findViewById(R.id.tv_order_time_left).setVisibility(View.GONE);
//            ((TextView) view.findViewById(R.id.tv_cancel_order)).setText(getResources().getString(R.string.sale_service_personal));
//        }
        token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");

        switch (Integer.parseInt(mOrder.getStatus()))
        {
            case ZhaiDou.STATUS_UNPAY:
                if (mParam2<1)
                {
                    mBottomLayout.setVisibility(View.GONE);
                    mOrderStatus.setText(mContext.getResources().getString(R.string.order_colse));
                }
                break;
            case ZhaiDou.STATUS_PAYED:
                mCancelOrder.setText(mContext.getResources().getString(R.string.order_return_money));
                mOrderTimer.setVisibility(View.GONE);
                mCancelOrder.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.btn_green_click_bg));
                break;
            case ZhaiDou.STATUS_OVER_TIME:
                mBottomLayout.setVisibility(View.GONE);
                break;
            case ZhaiDou.STATUS_ORDER_CANCEL_PAYED:
                mBottomLayout.setVisibility(View.GONE);
                break;
            case ZhaiDou.STATUS_DELIVERY:
                mCancelOrder.setText(mContext.getResources().getString(R.string.order_logistics));
                mOrderTimer.setText(mContext.getResources().getString(R.string.order_received));
                mCancelOrder.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.btn_green_click_bg));
                mOrderTimer.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.btn_red_click_selector));
                break;
            case ZhaiDou.STATUS_DEAL_SUCCESS:
                Log.i("STATUS_DELIVERY----------------->", "STATUS_DELIVERY");
                mCancelOrder.setText(mContext.getResources().getString(R.string.order_logistics));
                mOrderTimer.setText(mContext.getResources().getString(R.string.order_return_good));
                mCancelOrder.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.btn_green_click_bg));
                mOrderTimer.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.btn_green_click_bg));
                break;
            case ZhaiDou.STATUS_APPLY_GOOD_RETURN:
                mOrderTimer.setVisibility(View.GONE);
                mCancelOrder.setText(getResources().getString(R.string.sale_service_personal));
                break;
            case ZhaiDou.STATUS_GOOD_RETURNING:
                mOrderTimer.setVisibility(View.GONE);
                mCancelOrder.setText(getResources().getString(R.string.sale_service_personal));
                break;
            case ZhaiDou.STATUS_RETURN_GOOD_SUCCESS:
                mOrderTimer.setVisibility(View.GONE);
                mCancelOrder.setText(getResources().getString(R.string.sale_service_personal));
                break;
            case ZhaiDou.STATUS_UNPAY_CANCEL:
                mBottomLayout.setVisibility(View.GONE);
                break;
            case ZhaiDou.STATUS_DEAL_CLOSE:
                mBottomLayout.setVisibility(View.GONE);
                break;
            case ZhaiDou.STATUS_RETURN_MONEY_SUCCESS:
                mBottomLayout.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.tv_cancel_order:
                if (("" + ZhaiDou.STATUS_DEAL_SUCCESS).equalsIgnoreCase(mOrder.getStatus()))
                {
                    LogisticsMsgFragment logisticsMsgFragment = LogisticsMsgFragment.newInstance("", "");
                    ((MainActivity) getActivity()).navigationToFragment(logisticsMsgFragment);
                    return;
                } else if (mContext.getResources().getString(R.string.order_return_money).equalsIgnoreCase(mCancelOrder.getText().toString()))
                {
                    AfterSaleFragment afterSaleFragment = AfterSaleFragment.newInstance(mOrderId, mOrder.getStatus());
                    ((MainActivity) getActivity()).navigationToFragment(afterSaleFragment);
                    return;
                } else if (mContext.getResources().getString(R.string.order_logistics).equalsIgnoreCase(mCancelOrder.getText().toString()))
                {
                    LogisticsMsgFragment logisticsMsgFragment = LogisticsMsgFragment.newInstance("", "");
                    ((MainActivity) getActivity()).navigationToFragment(logisticsMsgFragment);
                    return;
                }
                if (mOrder != null && "678".contains(mOrder.getStatus()))
                {
                    String url = "mqqwpa://im/chat?chat_type=wpa&uin=11300";
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return;
                }
                cancalOrderDialog();
                break;
            case R.id.tv_order_time_left:
                if (("" + ZhaiDou.STATUS_DEAL_SUCCESS).equalsIgnoreCase(mOrder.getStatus()))
                {
                    AfterSaleFragment afterSaleFragment = AfterSaleFragment.newInstance(mOrderId, mOrder.getStatus() + "");
                    ((MainActivity) getActivity()).navigationToFragment(afterSaleFragment);
                    return;
                } else if (mContext.getResources().getString(R.string.order_received).equalsIgnoreCase(mOrderTimer.getText().toString()))
                {
                    orderOkReciver();
                    return;
                } else if (mContext.getResources().getString(R.string.timer_finish).equalsIgnoreCase(mOrderTimer.getText().toString()))
                {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.order_had_order_time), Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.i("mOrder.getOrderId()------------->", mOrder.getOrderId() + "");
                Intent intent1 = new Intent(getActivity(), PayDemoActivity.class);
                intent1.putExtra("id", Integer.parseInt(mOrderId + ""));
                intent1.putExtra("amount", mOrder.getAmount());
                ShopPaymentFragment shopPaymentFragment = ShopPaymentFragment.newInstance(Integer.parseInt(mOrderId), amount, 10, timeLeft, mOrder);
                ((MainActivity) getActivity()).navigationToFragment(shopPaymentFragment);
                shopPaymentFragment.setOrderListener(new Order.OrderListener()
                {
                    @Override
                    public void onOrderStatusChange(Order order)
                    {
                        Log.i("onOrderStatusChange---------->", order.toString());
                        mParam2 = order.getOver_at();
                    }
                });
                break;
        }
        super.onClick(view);
    }

    private void FetchOrderDetail(String id)
    {
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.URL_ORDER_LIST + "/" + id, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                Log.i("jsonObject--------->", jsonObject.toString());
                if (jsonObject != null)
                {
                    JSONObject orderObj = jsonObject.optJSONObject("order");
                    amount = orderObj.optInt("amount");
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
                    Receiver receiver = new Receiver(receiverId, address, phone, name);


                    JSONArray order_items = orderObj.optJSONArray("order_items");
//                    List<OrderItem> orderItems=new ArrayList<OrderItem>();
                    if (order_items != null && order_items.length() > 0)
                    {
                        for (int i = 0; i < order_items.length(); i++)
                        {
                            JSONObject item = order_items.optJSONObject(i);
                            int itemId = item.optInt("id");
                            int itemPrice = item.optInt("price");
                            int count = item.optInt("count");
                            int cost_price = item.optInt("cost_price");
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
                    message.what = 1;
                    handler.sendMessage(message);
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
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("SECAuthorization", token);
                return headers;
            }
        };
        requestQueue.add(request);
    }

    /**
     * ????dialog
     */
    private void cancalOrderDialog()
    {
        final Dialog dialog = new Dialog(getActivity(), R.style.custom_dialog);
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_custom_collect_hint, null);
        TextView textView = (TextView) dialogView.findViewById(R.id.tv_msg);
        textView.setText(mContext.getResources().getString(R.string.order_cancel_ok));
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
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.URL_ORDER_LIST + "/" + mOrder.getOrderId() + "/update_status?status=" + ("0".equalsIgnoreCase(mOrder.getStatus()) ? "9" : "3"), new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject jsonObject)
                    {
                        int status = jsonObject.optInt("status");
                        if (201 == status)
                        {
                            JSONObject orderObj = jsonObject.optJSONObject("order");
                            if (orderObj != null)
                            {
                                double amount = orderObj.optDouble("amount");
                                int id = orderObj.optInt("id");
                                long over_at = orderObj.optLong("over_at");
                                String status1 = orderObj.optString("status");
                                String created_at_for = orderObj.optString("created_at_for");
                                String created_at = orderObj.optString("created_at");
                                String status_ch = orderObj.optString("status_ch");
                                String number = orderObj.optString("number");
                                Order order = new Order(id, number, amount, status1, status_ch, created_at_for, created_at, "", 0);
                                ToolUtils.setLog(order.toString());
                                if (orderListener != null)
                                    orderListener.onOrderStatusChange(order);
                                ((MainActivity) getActivity()).popToStack(OrderDetailFragment.this);

                                Intent intent=new Intent(ZhaiDou.IntentRefreshUnPayDesTag);
                                mContext.sendBroadcast(intent);
                            }
                        }
                        dialog.dismiss();
                    }
                }, new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError volleyError)
                    {
                        dialog.dismiss();
                        ToolUtils.setToast(mContext, "????");
                    }
                })
                {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError
                    {
                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put("SECAuthorization", token);
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
    }

    /**
     * ??????
     */
    private void orderOkReciver()
    {
        final Dialog dialog = new Dialog(getActivity(), R.style.custom_dialog);

        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_custom_collect_hint, null);
        TextView textView = (TextView) dialogView.findViewById(R.id.tv_msg);
        textView.setText(mContext.getResources().getString(R.string.order_confirm));
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
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.URL_ORDER_LIST + "/" + mOrder.getOrderId() + "/update_status?status=5", new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject jsonObject)
                    {
                        JSONObject orderObj = jsonObject.optJSONObject("order");
                        if (orderObj != null)
                        {
                            String status = orderObj.optString("status");
                            mOrder.setStatus(status);
                            if (orderListener != null)
                                orderListener.onOrderStatusChange(mOrder);
                            ((BaseActivity) getActivity()).popToStack(OrderDetailFragment.this);
                        }
                    }
                }, new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError volleyError)
                    {
                        ToolUtils.setToast(mContext, "????");
                    }
                })
                {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError
                    {
                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put("SECAuthorization", token);
                        return headers;
                    }
                };
                requestQueue.add(request);
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.addContentView(dialogView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog.show();
    }


    public class OrderItemAdapter extends BaseListAdapter<OrderItem>
    {
        public OrderItemAdapter(Context context, List<OrderItem> list)
        {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_order_detail, null);
            TextView tv_name = ViewHolder.get(convertView, R.id.tv_name);
            TextView tv_specification = ViewHolder.get(convertView, R.id.tv_specification);
            TextView tv_count = ViewHolder.get(convertView, R.id.tv_count);
            ImageView iv_order_img = ViewHolder.get(convertView, R.id.iv_order_img);
            LinearLayout ll_count = ViewHolder.get(convertView, R.id.ll_count);
            TextView tv_zero_msg = ViewHolder.get(convertView, R.id.tv_zero_msg);

            OrderItem item = getList().get(position);
            if (item.getSale_cate() == 0)
            {
                ll_count.setVisibility(View.VISIBLE);
                tv_zero_msg.setVisibility(View.GONE);
            } else
            {
                ll_count.setVisibility(View.GONE);
                tv_zero_msg.setVisibility(View.VISIBLE);
            }
            tv_name.setText(item.getMerchandise());
            tv_specification.setText(item.getSpecification());
            tv_count.setText(item.getCount() + "");
            ToolUtils.setImageCacheUrl(item.getMerch_img(), iv_order_img);
            return convertView;
        }
    }


    private class MyTimer extends CountDownTimer
    {
        private MyTimer(long millisInFuture, long countDownInterval)
        {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l)
        {
//            Log.i("onTick------------>", l + "");
            timeLeft = l;
            long day = 24 * 3600 * 1000;
            long hour = 3600 * 1000;
            long minute = 60 * 1000;
            long dayCount = l / day;
            long hourCount = (l - (dayCount * day)) / hour;
            long minCount = (l - (dayCount * day) - (hour * hourCount)) / minute;
            long secondCount = (l - (dayCount * day) - (hour * hourCount) - (minCount * minute)) / 1000;
            Message message = new Message();
            String data = getResources().getString(R.string.timer_start);
            data = String.format(data, minCount + ":" + secondCount);
            message.what = UPDATE_COUNT_DOWN_TIME;
            message.obj = data;
            handler.sendMessage(message);
        }

        @Override
        public void onFinish()
        {
//            Log.i("onFinish---------->", "onFinish");
            handler.sendEmptyMessage(UPDATE_UI_TIMER_FINISH);
        }
    }

    @Override
    public void onResume()
    {
        Log.i("onResume------------->", "onResume");
        if ((ZhaiDou.STATUS_UNPAY + "").equalsIgnoreCase(mOrder.getStatus()))
        {
            if (timer == null)
                timer = new MyTimer(mParam2, 1000);
            timer.start();
        }
        super.onResume();
    }

    @Override
    public void onDestroyView()
    {
        if (timer != null)
        {
            timer.cancel();
            timer = null;
        }
        mOrder.setOver_at(timeLeft);
        if (orderListener != null)
            orderListener.onOrderStatusChange(mOrder);
        super.onDestroyView();
    }

    public void setOrderListener(OrderListener orderListener)
    {
        this.orderListener = orderListener;
    }

    public interface OrderListener
    {
        public void onOrderStatusChange(Order order);
    }
}
