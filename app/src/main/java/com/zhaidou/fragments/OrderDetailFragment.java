package com.zhaidou.fragments;



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
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.Order;
import com.zhaidou.model.OrderItem;
import com.zhaidou.model.Receiver;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrderDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class OrderDetailFragment extends BaseFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_ID = "id";
    private static final String ARG_TIMESTMP = "timestmp";

    // TODO: Rename and change types of parameters
    private String mOrderId;
    private long mParam2;


    private RequestQueue requestQueue;
    private TextView mOrderNumber,mOrderTime,mOrderStatus,
                     mReceiverName,mReceiverPhone,mReceiverAddress,mReceiverTime,
                     mOrderAmount,mOrderEdit,mCancelOrder,mOrderTimer;
    private ListView mListView;
    private OrderItemAdapter orderItemAdapter;
    private final int UPDATE_COUNT_DOWN_TIME = 2;
    private final int UPDATE_UI_TIMER_FINISH=3;
    private MyTimer timer;

    private List<OrderItem> orderItems=new ArrayList<OrderItem>();
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Order order=(Order)msg.obj;
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
                    String time=(String)msg.obj;
                    mOrderTimer.setText(time);
                    break;
                case UPDATE_UI_TIMER_FINISH:
                    mOrderTimer.setText(getResources().getString(R.string.timer_finish));
                    break;
            }
        }
    };
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OrderDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrderDetailFragment newInstance(String id, long timestmp) {
        OrderDetailFragment fragment = new OrderDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, id);
        args.putLong(ARG_TIMESTMP, timestmp);
        fragment.setArguments(args);
        return fragment;
    }
    public OrderDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mOrderId = getArguments().getString(ARG_ID);
            mParam2 = getArguments().getLong(ARG_TIMESTMP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i("OrderDetailFragment--------->","onCreateView");
        View view=inflater.inflate(R.layout.fragment_order_detail, container, false);
        mOrderNumber=(TextView)view.findViewById(R.id.tv_order_number);
        mOrderTime=(TextView)view.findViewById(R.id.tv_order_time);
        mOrderStatus=(TextView)view.findViewById(R.id.tv_order_status);
        mReceiverName=(TextView)view.findViewById(R.id.tv_receiver_name);
        mReceiverPhone=(TextView)view.findViewById(R.id.tv_receiver_phone);
        mReceiverAddress=(TextView)view.findViewById(R.id.tv_receiver_address);
        mReceiverTime=(TextView)view.findViewById(R.id.tv_receiver_name);
        mOrderAmount=(TextView)view.findViewById(R.id.tv_order_amount);
        mOrderEdit=(TextView)view.findViewById(R.id.tv_order_edit);
        mCancelOrder=(TextView)view.findViewById(R.id.tv_cancel_order);
        mOrderTimer=(TextView)view.findViewById(R.id.tv_order_time_left);
        mListView=(ListView)view.findViewById(R.id.lv_order_list);
        orderItemAdapter=new OrderItemAdapter(getActivity(),orderItems);
        mListView.setAdapter(orderItemAdapter);
        requestQueue= Volley.newRequestQueue(getActivity());
        timer = new MyTimer(mParam2, 1000);
        timer.start();
        FetchOrderDetail(mOrderId);
        return view;
    }

    private void FetchOrderDetail(String id){
        JsonObjectRequest request=new JsonObjectRequest("http://192.168.199.173/special_mall/api/orders/"+id,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.i("jsonObject--------->",jsonObject.toString());
                if (jsonObject!=null){
                    JSONObject orderObj=jsonObject.optJSONObject("order");
                    int amount=orderObj.optInt("amount");
                    int id=orderObj.optInt("id");
                    String status=orderObj.optString("status");
                    String created_at_for=orderObj.optString("created_at_for");
                    String receiver_address=orderObj.optString("receiver_address");
                    String created_at=orderObj.optString("created_at");
                    String status_ch=orderObj.optString("status_ch");
                    String number=orderObj.optString("number");
                    String receiver_phone=orderObj.optString("receiver_phone");
                    String deliver_number=orderObj.optString("deliver_number");
                    String receiver_name=orderObj.optString("receiver_name");

                    JSONObject receiverObj = orderObj.optJSONObject("receiver");
                    int receiverId=receiverObj.optInt("id");
                    String address=receiverObj.optString("address");
                    String phone=receiverObj.optString("phone");
                    String name=receiverObj.optString("name");
                    Receiver receiver=new Receiver(receiverId,address,phone,name);


                    JSONArray order_items=orderObj.optJSONArray("order_items");
//                    List<OrderItem> orderItems=new ArrayList<OrderItem>();
                    if (order_items!=null&&order_items.length()>0){
                        for (int i=0;i<order_items.length();i++){
                            JSONObject item = order_items.optJSONObject(i);
                            int itemId=item.optInt("id");
                            int itemPrice=item.optInt("price");
                            int count=item.optInt("count");
                            int cost_price=item.optInt("cost_price");
                            String merchandise=item.optString("merchandise");
                            String specification=item.optString("specification");
                            int merchandise_id=item.optInt("merchandise_id");
                            String merch_img=item.optString("merch_img");
                            OrderItem orderItem=new OrderItem(itemId,itemPrice,count,cost_price,merchandise,specification,merchandise_id,merch_img);
                            orderItems.add(orderItem);
                        }
                    }
                    Order order=new Order("",id,number,amount,status,status_ch,created_at_for,created_at,receiver,orderItems,receiver_address,receiver_phone,deliver_number,receiver_name);
                    Message message=new Message();
                    message.obj=order;
                    message.what=1;
                    handler.sendMessage(message);
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
                headers.put("SECAuthorization", "o56MZD7xJY7JVNRT3C2R");
                return headers;
            }
        };
        requestQueue.add(request);
    }

    public class OrderItemAdapter extends BaseListAdapter<OrderItem> {
        public OrderItemAdapter(Context context, List<OrderItem> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            if (convertView==null)
                convertView=mInflater.inflate(R.layout.item_order_detail,null);
            TextView tv_name = ViewHolder.get(convertView, R.id.tv_name);
            TextView tv_specification=ViewHolder.get(convertView,R.id.tv_specification);
            TextView tv_count=ViewHolder.get(convertView,R.id.tv_count);
            ImageView iv_order_img=ViewHolder.get(convertView,R.id.iv_order_img);

            OrderItem item = getList().get(position);
            tv_name.setText(item.getMerchandise());
            tv_specification.setText(item.getSpecification());
            tv_count.setText(item.getCount()+"");
            ToolUtils.setImageCacheUrl(item.getMerch_img(),iv_order_img);
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
            long day = 24 * 3600 * 1000;
            long hour = 3600 * 1000;
            long minute = 60 * 1000;
            //??????????
            long dayCount = l / day;
            long hourCount = (l - (dayCount * day)) / hour;
            long minCount = (l - (dayCount * day) - (hour * hourCount)) / minute;
            long secondCount = (l - (dayCount * day) - (hour * hourCount) - (minCount * minute)) / 1000;
            Message message=new Message();
//            String msg="?? "+minCount+":"+secondCount;
            String data = getResources().getString(R.string.timer_start);
            data = String.format(data,minCount+":"+secondCount);
            message.what=UPDATE_COUNT_DOWN_TIME;
            message.obj=data;
            handler.sendMessage(message);
        }

        @Override
        public void onFinish() {
//            Log.i("onFinish---------->", "onFinish");
            handler.sendEmptyMessage(UPDATE_UI_TIMER_FINISH);
        }
    }
}
