package com.zhaidou.fragments;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.zhaidou.model.Order;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReturnFragment extends BaseFragment implements View.OnClickListener{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private ListView mListView;
    private Dialog mDialog;
    private LinearLayout loadingView;

    private RequestQueue mRequestQueue;
    private ReturnAdapter returnAdapter;
    private List<Order> orders;
    private String STATUS_RETURN_LIST="678";
    private final int UPDATE_RETURN_LIST=1;
    private String token;
    private View rootView;
    private Context mContext;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_RETURN_LIST:
                    loadingView.setVisibility(View.GONE);
                    returnAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
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
        if (null != rootView) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent) {
                parent.removeView(rootView);
            }
        } else {
            rootView = inflater.inflate(R.layout.fragment_return,container, false);
            initView(rootView);// 控件初始化
        }
        return rootView;
    }

    private void initView(View view){

        mDialog= CustomLoadingDialog.setLoadingDialog(getActivity(), "loading");
        loadingView=(LinearLayout)view.findViewById(R.id.loadingView);
        mContext=getActivity();
        orders=new ArrayList<Order>();
        mListView=(ListView)view.findViewById(R.id.lv_return);
        returnAdapter=new ReturnAdapter(getActivity(),orders);
        mListView.setAdapter(returnAdapter);
        mRequestQueue= Volley.newRequestQueue(getActivity());
        token=(String) SharedPreferencesUtil.getData(getActivity(),"token","");
        FetchReturnData();
        returnAdapter.setOnInViewClickListener(R.id.orderlayout,new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                Order order=(Order)values;
                OrderDetailFragment orderDetailFragment = OrderDetailFragment.newInstance(order.getOrderId() + "", order.getOver_at(),order);
                ((MainActivity) getActivity()).navigationToFragment(orderDetailFragment);
            }
        }) ;  
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
            tv_order_time.setText(item.getCreated_at_for());
            tv_order_number.setText(item.getNumber());
            tv_order_amount.setText("￥"+item.getAmount()+"");
            tv_order_status.setText(item.getStatus_ch());
            ToolUtils.setImageCacheUrl(item.getImg(), iv_order_img);
            return convertView;
        }
    }
    private void FetchReturnData(){
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.URL_ORDER_LIST, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (mDialog!=null) mDialog.dismiss();
                Log.i("jsonObject----------->", jsonObject.toString());
                if (jsonObject != null) {
                    JSONArray orderArr = jsonObject.optJSONArray("orders");
                    if (orderArr != null && orderArr.length() > 0) {
                        orders.clear();
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
                Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
                if (getActivity()!=null)
                Toast.makeText(mContext, "网络异常", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onStart() {
        FetchReturnData();
        super.onStart();
    }
}
