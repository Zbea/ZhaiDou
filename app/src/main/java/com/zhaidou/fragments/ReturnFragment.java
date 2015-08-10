package com.zhaidou.fragments;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.Order;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReturnFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReturnFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class ReturnFragment extends BaseFragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ListView mListView;
    private RequestQueue mRequestQueue;
    private ReturnAdapter returnAdapter;
    private List<Order> orders;
    private String STATUS_RETURN_LIST="678";
    private final int UPDATE_RETURN_LIST=1;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_RETURN_LIST:
                    Log.i("UPDATE_RETURN_LIST-------->",orders.size()+"");
                    returnAdapter.notifyDataSetChanged();
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
     * @return A new instance of fragment ReturnFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReturnFragment newInstance(String param1, String param2) {
        ReturnFragment fragment = new ReturnFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public ReturnFragment() {
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
        View view=inflater.inflate(R.layout.fragment_return, container, false);
//        view.findViewById(R.id.tv_apply_support).setOnClickListener(this);
        orders=new ArrayList<Order>();
        mListView=(ListView)view.findViewById(R.id.lv_return);
        returnAdapter=new ReturnAdapter(getActivity(),orders);
        mListView.setAdapter(returnAdapter);
        mRequestQueue= Volley.newRequestQueue(getActivity());
        FetchReturnData();
        returnAdapter.setOnInViewClickListener(R.id.orderlayout,new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                Order order=(Order)values;
                OrderDetailFragment orderDetailFragment = OrderDetailFragment.newInstance(order.getOrderId() + "", order.getOver_at(),order);
                ((MainActivity) getActivity()).navigationToFragment(orderDetailFragment);
            }
        });
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
//            case R.id.tv_apply_support:
//                AfterSaleFragment afterSaleFragment=AfterSaleFragment.newInstance("","");
//                ((MainActivity)getActivity()).navigationToFragment(afterSaleFragment);
//                break;
        }
    }
    public class ReturnAdapter extends BaseListAdapter<Order> {
        public ReturnAdapter(Context context, List<Order> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            if (convertView==null)
                convertView=mInflater.inflate(R.layout.item_order_return,null);
            TextView tv_order_time = ViewHolder.get(convertView, R.id.tv_order_time);
            TextView tv_order_number = ViewHolder.get(convertView, R.id.tv_order_number);
            TextView tv_order_amount = ViewHolder.get(convertView, R.id.tv_order_amount);
            TextView tv_order_status = ViewHolder.get(convertView, R.id.tv_order_status);
            ImageView iv_order_img=ViewHolder.get(convertView,R.id.iv_order_img);
            Order item = getList().get(position);
            Log.i("item----------->",item.toString());
            tv_order_time.setText("下单时间："+item.getCreated_at_for());
            tv_order_number.setText("订单编号："+item.getNumber());
            tv_order_amount.setText("订单金额："+item.getAmount()+"");
            tv_order_status.setText("订单状态："+item.getStatus_ch());
            ToolUtils.setImageCacheUrl(item.getImg(), iv_order_img);
            return convertView;
        }
    }
    private void FetchReturnData(){
        JsonObjectRequest request = new JsonObjectRequest("http://192.168.199.173/special_mall/api/orders", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
//                Log.i("jsonObject----------->", jsonObject.toString());
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
                            if (STATUS_RETURN_LIST.contains(status)){
                                orders.add(order);
                                Log.i("order---->",order.toString());
                            }
                        }
                        handler.sendEmptyMessage(UPDATE_RETURN_LIST);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("SECAuthorization", "ysyFfLMqfYFfD_PSj7Nd");
                return headers;
            }
        };
        mRequestQueue.add(request);
    }
}
