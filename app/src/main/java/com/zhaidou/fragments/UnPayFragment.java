package com.zhaidou.fragments;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.zhaidou.model.CountTime;
import com.zhaidou.model.Order;
import com.zhaidou.utils.TimerUtils;
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
 *
 */
public class UnPayFragment extends BaseFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RequestQueue mRequestQueue;
    private List<Order> orders=new ArrayList<Order>();
    private final int UPDATE_UNPAY_LIST=1;
    private ListView mListView;
    private UnPayAdapter unPayAdapter;
    private final int UPDATE_COUNT_DOWN_TIME=2;

    private Map<Integer,View> mHashMap=new WeakHashMap<Integer, View>();
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_UNPAY_LIST:
                    unPayAdapter.notifyDataSetChanged();
                    break;
                case UPDATE_COUNT_DOWN_TIME:
                    Log.i("UPDATE_COUNT_DOWN_TIME------>","UPDATE_COUNT_DOWN_TIME");
                    TextView textView=(TextView)msg.obj;
                    int sec=msg.arg2;
//                    textView.setText(sec+"");
                    break;
            }
        }
    };

    // TODO: Rename and change types and number of parameters
    public static UnPayFragment newInstance(String param1, String param2) {
        UnPayFragment fragment = new UnPayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public UnPayFragment() {
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
        View view=inflater.inflate(R.layout.fragment_unpay, container, false);
        mListView=(ListView)view.findViewById(R.id.lv_unpaylist);
        unPayAdapter=new UnPayAdapter(getActivity(),orders);
        mListView.setAdapter(unPayAdapter);
        mRequestQueue= Volley.newRequestQueue(getActivity());
        FetchData();
        return view;
    }

    private void FetchData(){
        JsonObjectRequest request=new JsonObjectRequest("http://192.168.199.173/special_mall/api/orders",new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.i("jsonObject----------->",jsonObject.toString());
                if (jsonObject!=null){
                    JSONArray orderArr=jsonObject.optJSONArray("orders");
                    if (orderArr!=null&&orderArr.length()>0){
                        for (int i=0;i<orderArr.length();i++){
                            JSONObject orderObj =orderArr.optJSONObject(i);
                            int id=orderObj.optInt("id");
                            String number=orderObj.optString("number");
                            int amount =orderObj.optInt("amount");
                            String status=orderObj.optString("status");
                            String status_ch=orderObj.optString("status_ch");
                            String created_at=orderObj.optString("created_at");
                            String created_at_for=orderObj.optString("created_at_for");
                            String img=orderObj.optString("merch_img");
                            long over_at=orderObj.optLong("over_at");
                            Order order=new Order(id,number,amount,status,status_ch,created_at_for,created_at,"",0);
                            order.setImg(img);
                            order.setOver_at(over_at);
                            orders.add(order);
                        }
                        handler.sendEmptyMessage(UPDATE_UNPAY_LIST);
                    }
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
//                Toast.makeText(getActivity(),"网络异常",Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers=new HashMap<String, String>();
                headers.put("SECAuthorization", "o56MZD7xJY7JVNRT3C2R");
                return headers;
            }
        };
        mRequestQueue.add(request);
    }
    private class UnPayAdapter extends BaseListAdapter<Order> {
        private TimerUtils timerUtils;
//        private Handler mHandler =new Handler(){
//            @Override
//            public void handleMessage(Message msg) {
//                switch (msg.what){
//                    case UPDATE_COUNT_DOWN_TIME:
//                        break;
//                }
//            }
//        };
        public UnPayAdapter(Context context, List<Order> list) {
            super(context, list);
            timerUtils=new TimerUtils();
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            Log.i("bindView-------------->","bindView");
            convertView=mHashMap.get(position);
            if (convertView==null)
                convertView=mInflater.inflate(R.layout.item_pre_pay,null);
            TextView mOrderTime = ViewHolder.get(convertView, R.id.tv_order_time);
            TextView mOrderNum = ViewHolder.get(convertView, R.id.tv_order_number);
            TextView mOrderAmount = ViewHolder.get(convertView, R.id.tv_order_amount);
            TextView mOrderStatus = ViewHolder.get(convertView, R.id.order_status);
            final TextView mTimerBtn = ViewHolder.get(convertView, R.id.bt_order_timer);
            ImageView mOrderImg = ViewHolder.get(convertView, R.id.iv_order_img);
            Order item = getList().get(position);
            mOrderTime.setText("订单时间 "+item.getCreated_at_for());
            mOrderNum.setText("订单编号 "+item.getNumber());
            mOrderAmount.setText("订单金额 "+item.getAmount());
            mOrderStatus.setText("订单状态 "+item.getStatus_ch());
            ToolUtils.setImageCacheUrl(item.getImg(), mOrderImg);
            if (mTimerBtn.getTag()==null)
                mTimerBtn.setTag(item.getOver_at());
Log.i("-----",System.currentTimeMillis()+"----------"+position);
            timerUtils.stateTimer(mListView,mTimerBtn,Long.parseLong(mTimerBtn.getTag()+""),position,new TimerUtils.TimerListener() {
                @Override
                public void onTick(final TextView mTimerView,final CountTime time,long l) {
                    Log.i("second--------------->", time.getMinute() + ":"+time.getSecond()+"===="+System.currentTimeMillis());
                    Message message=new Message();
                    message.arg1=Integer.parseInt(time.getMinute()+"");
                    message.arg2=Integer.parseInt(time.getSecond()+"");
                    message.obj=mTimerView;
                    message.what=UPDATE_COUNT_DOWN_TIME;
                    handler.sendMessage(message);
                    mTimerView.setTag(l);
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.i("second--------------->", "dad");
//                            mTimerView.setText("iii");
//                        }
//                    },1);
//                    mTimerView.setText(time.getMinute() + ":" + time.getSecond());
                }
            });
            mHashMap.put(position,convertView);
            return convertView;
        }
    }

//    class MyTimer extends CountDownTimer {
//        int pos;
//
//        public MyTimer(long millisInFuture, long countDownInterval, int pos) {
//            super(millisInFuture, countDownInterval);
//            this.pos = pos;
//        }
//
//        @Override
//        public void onFinish() {
//            int firstVisiblePosition = lv.getFirstVisiblePosition();
//            int i = pos - firstVisiblePosition;
//            if (i >= 0) {
//                View view = lv.getChildAt(i);
//                if (view != null) {
//                    ViewHolder mHolder = (ViewHolder) view.getTag();
//                    mHolder.tv.setText("end");
//                }
//            }
//            list.set(pos, "end");
//
//        }
//
//        @Override
//        public void onTick(long millisUntilFinished) {
//            int firstVisiblePosition = lv.getFirstVisiblePosition();
//            int i = pos - firstVisiblePosition;
//            if (i >= 0) {
//                View view = lv.getChildAt(i);
//                if (view != null) {
//                    ViewHolder mHolder = (ViewHolder) view.getTag();
//                    mHolder.tv.setText("ing" + millisUntilFinished / 1000);
//                }
//            }
//            list.set(pos, "ing" + millisUntilFinished / 1000);
//        }
//
//    }
}
