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
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class OrderReturnFragment extends BaseFragment implements View.OnClickListener{
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
    private final int UPDATE_RETURN_LIST=1;
    private String token;
    private View rootView;
    private Context mContext;
    private View mEmptyView,mNetErrorView;
    private WeakHashMap<Integer,View> mHashMap=new WeakHashMap<Integer, View>();
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
    public static OrderReturnFragment newInstance(String param1, String param2) {
        OrderReturnFragment fragment = new OrderReturnFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public OrderReturnFragment() {
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
            rootView = inflater.inflate(R.layout.fragment_return,container, false);
            initView(rootView);
        }
        return rootView;
    }

    private void initView(View view){

//        mDialog= CustomLoadingDialog.setLoadingDialog(getActivity(), "loading");
        loadingView=(LinearLayout)view.findViewById(R.id.loadingView);
        mEmptyView=rootView.findViewById(R.id.nullline);
        mNetErrorView=rootView.findViewById(R.id.nullNetline);
        view.findViewById(R.id.netReload).setOnClickListener(this);
        mContext=getActivity();
        orders=new ArrayList<Order>();
        mListView=(ListView)view.findViewById(R.id.lv_return);
        returnAdapter=new ReturnAdapter(getActivity(),orders);
        mListView.setAdapter(returnAdapter);
        mRequestQueue= Volley.newRequestQueue(getActivity());
        token=(String) SharedPreferencesUtil.getData(getActivity(),"token","");
//        initData();
        returnAdapter.setOnInViewClickListener(R.id.orderlayout,new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                Order order=(Order)values;
                OrderDetailFragment orderDetailFragment = OrderDetailFragment.newInstance(order.getOrderId() + "", order.getOver_at(),order,0);
                ((MainActivity) getActivity()).navigationToFragment(orderDetailFragment);
            }
        }) ;
        returnAdapter.setOnInViewClickListener(R.id.iv_delete, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, final Integer position, Object values) {
                final Order order = (Order) values;

                final Dialog dialog = new Dialog(getActivity(), R.style.custom_dialog);

                View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_custom_collect_hint, null);
                TextView textView = (TextView) view.findViewById(R.id.tv_msg);
                textView.setText("是否删除订单?");
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
                        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.URL_ORDER_LIST + "/" + order.getOrderId() + "/delete_order", new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                Log.i("jsonObject---iv_delete->", jsonObject.toString());
                                if (jsonObject != null) {
                                    int status = jsonObject.optInt("status");
                                    if (201 == status) {
                                        orders.remove(order);
                                        returnAdapter.notifyDataSetChanged();
                                    } else if (400 == status) {
                                        ShowToast("删除失败");
                                    }
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
                                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
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
    }
    private void initData() {
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading",isDialogFirstVisible);
        isDialogFirstVisible=false;
        if (NetworkUtils.isNetworkAvailable(mContext)) {
            mNetErrorView.setVisibility(View.GONE);
            loadingView.setVisibility(View.GONE);
            FetchReturnData();
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
        switch (view.getId()){
            case R.id.netReload:
                initData();
                break;
        }
    }
    public class ReturnAdapter extends BaseListAdapter<Order> {
        public ReturnAdapter(Context context, List<Order> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            convertView=mHashMap.get(position);
            if (convertView==null)
                convertView=mInflater.inflate(R.layout.item_order_return,null);
            TextView tv_order_time = ViewHolder.get(convertView, R.id.tv_order_time);
            TextView tv_order_number = ViewHolder.get(convertView, R.id.tv_order_number);
            TextView tv_order_amount = ViewHolder.get(convertView, R.id.tv_order_amount);
            TextView tv_order_status = ViewHolder.get(convertView, R.id.tv_order_status);
            RelativeLayout mBottomLayout=ViewHolder.get(convertView,R.id.rl_btn);
            ImageView iv_order_img=ViewHolder.get(convertView,R.id.iv_order_img);
            ImageView iv_delete=ViewHolder.get(convertView,R.id.iv_delete);
            Order item = getList().get(position);
            tv_order_time.setText(item.getCreated_at_for());
            tv_order_number.setText(item.getNumber());
            tv_order_amount.setText("￥"+ToolUtils.isIntPrice("" +item.getAmount()+""));
            tv_order_status.setText(item.getStatus_ch());
            ToolUtils.setImageCacheUrl(item.getImg(), iv_order_img,R.drawable.icon_loading_defalut);
            mBottomLayout.setVisibility(View.GONE);
            if ((""+ZhaiDou.STATUS_RETURN_GOOD_SUCCESS).equalsIgnoreCase(item.getStatus())|(""+ZhaiDou.STATUS_RETURN_MONEY_SUCCESS).equalsIgnoreCase(item.getStatus())){
                iv_delete.setVisibility(View.VISIBLE);
            }else {
                iv_delete.setVisibility(View.GONE);
            }
            mHashMap.put(position,convertView);
            return convertView;
        }
    }
    private void FetchReturnData(){
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.URL_ORDER_LIST + "?status=3,6,7,8,11", new Response.Listener<JSONObject>() {
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
//                            if ("6".equalsIgnoreCase(status)||"7".equalsIgnoreCase(status)||"8".equalsIgnoreCase(status)||"11".equalsIgnoreCase(status))
                            orders.add(order);
                        }
                        handler.sendEmptyMessage(UPDATE_RETURN_LIST);
                    }
                    else
                    {
                        mListView.setVisibility(View.GONE);
                        mEmptyView.setVisibility(View.VISIBLE);
                        loadingView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (mDialog!=null) mDialog.dismiss();
                if (getActivity()!=null)
                Toast.makeText(mContext, "加载失败", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("SECAuthorization",token);
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        mRequestQueue.add(request);
    }

    @Override
    public void onStart() {
        initData();
        super.onStart();
    }
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_order_return));
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_order_return));
    }
}
