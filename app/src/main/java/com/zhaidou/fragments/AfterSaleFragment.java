package com.zhaidou.fragments;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
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
 * Use the {@link AfterSaleFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class AfterSaleFragment extends BaseFragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mOrderId;
    private String mParam2;


    private TextView mOldPrice;
    private RequestQueue requestQueue;
    private ListView mListView;
    private final int UPDATE_RETURN_LIST=1;
    private AfterSaleAdapter afterSaleAdapter;
    List<OrderItem> orderItems=new ArrayList<OrderItem>();
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_RETURN_LIST:
                    afterSaleAdapter.notifyDataSetChanged();
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
     * @return A new instance of fragment AfterSaleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AfterSaleFragment newInstance(String param1, String param2) {
        AfterSaleFragment fragment = new AfterSaleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public AfterSaleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mOrderId = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_after_sale, container, false);
        mOldPrice=(TextView)view.findViewById(R.id.tv_outdated);
        TextPaint textPaint=mOldPrice.getPaint();
        textPaint.setAntiAlias(true);
        textPaint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        requestQueue= Volley.newRequestQueue(getActivity());
        mListView=(ListView)view.findViewById(R.id.lv_aftersale);
        afterSaleAdapter=new AfterSaleAdapter(getActivity(),orderItems);
        mListView.setAdapter(afterSaleAdapter);
        FetchOrderDetail(mOrderId);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

        }
    }

    private void FetchOrderDetail(String id){
        JsonObjectRequest request=new JsonObjectRequest("http://192.168.199.173/special_mall/api/orders/"+id,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.i("FetchOrderDetail-------------->",jsonObject.toString());
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
                    message.what=UPDATE_RETURN_LIST;
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

    public class AfterSaleAdapter extends BaseListAdapter<OrderItem> {
        public AfterSaleAdapter(Context context, List<OrderItem> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            if (convertView==null)
                convertView=mInflater.inflate(R.layout.item_order_return1,null);
            TextView tv_name = ViewHolder.get(convertView, R.id.tv_name);
            TextView tv_specification=ViewHolder.get(convertView,R.id.tv_specification);
            TextView tv_count=ViewHolder.get(convertView,R.id.tv_count);
            ImageView iv_order_img=ViewHolder.get(convertView,R.id.iv_order_img);
            TextView tv_old_price=ViewHolder.get(convertView,R.id.tv_old_price);
            TextView tv_price=ViewHolder.get(convertView,R.id.tv_price);

            OrderItem item = getList().get(position);
            tv_name.setText(item.getMerchandise());
            tv_specification.setText(item.getSpecification());
            tv_count.setText(item.getCount()+"");
            tv_price.setText("￥"+item.getPrice());
            tv_old_price.setText("￥"+item.getCost_price());
            TextPaint textPaint=tv_old_price.getPaint();
            textPaint.setAntiAlias(true);
            textPaint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            ToolUtils.setImageCacheUrl(item.getMerch_img(), iv_order_img);
            return convertView;
        }
    }

}
