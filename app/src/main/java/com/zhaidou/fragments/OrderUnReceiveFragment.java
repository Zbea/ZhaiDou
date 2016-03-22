package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
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
import com.zhaidou.model.Order1;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OrderUnReceiveFragment extends BaseFragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private Dialog mDialog;
    private LinearLayout loadingView;

    private RequestQueue mRequestQueue;
    private UnReceiveAdapter unReceiveAdapter;
    private ListView mListView;
    private List<Order> orders;
    private final int STATUS_UNRECEIVE_LIST = 14;
    private String token;
    private View rootView;
    private View mEmptyView, mNetErrorView;
    private Context mContext;
    private List<Order1> mOrderList;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STATUS_UNRECEIVE_LIST:
                    loadingView.setVisibility(View.GONE);
                    mListView.setVisibility(View.VISIBLE);
                    unReceiveAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    public static OrderUnReceiveFragment newInstance(String param1, String param2) {
        OrderUnReceiveFragment fragment = new OrderUnReceiveFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public OrderUnReceiveFragment() {
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
            rootView = inflater.inflate(R.layout.fragment_un_receive, container, false);
            initView(rootView);
        }
        return rootView;
    }

    private void initView(View view) {
        mContext = getActivity();
        mOrderList = new ArrayList<Order1>();
        loadingView = (LinearLayout) view.findViewById(R.id.loadingView);
        mEmptyView = rootView.findViewById(R.id.nullline);
        mNetErrorView = rootView.findViewById(R.id.nullNetline);
        mListView = (ListView) view.findViewById(R.id.lv_unreceivelist);
        view.findViewById(R.id.netReload).setOnClickListener(this);
        token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        orders = new ArrayList<Order>();
        unReceiveAdapter = new UnReceiveAdapter(getActivity(), mOrderList);
        mListView.setAdapter(unReceiveAdapter);
        mRequestQueue = Volley.newRequestQueue(getActivity());
        initData();
        unReceiveAdapter.setOnInViewClickListener(R.id.bt_logistics, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                final Order order = (Order) values;
                if ("4".equalsIgnoreCase(order.getStatus())) {

//                    OrderLogisticsMsgFragment logisticsMsgFragment = OrderLogisticsMsgFragment.newInstance("", "",order);
//                    ((MainActivity) getActivity()).navigationToFragment(logisticsMsgFragment);

                } else if ("1".equalsIgnoreCase(order.getStatus())) {
//                    AfterSaleFragment afterSaleFragment = AfterSaleFragment.newInstance(order.getOrderId() + "", order.getStatus());
//                    ((MainActivity) getActivity()).navigationToFragment(afterSaleFragment);
                    if (order.isZero()) {
                        ShowToast("零元特卖的商品不可以退哦");
                        return;
                    }
                    final Dialog dialog = new Dialog(getActivity(), R.style.custom_dialog);

                    View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_custom_collect_hint, null);
                    TextView textView = (TextView) view.findViewById(R.id.tv_msg);
                    textView.setText("是否申请退款?");
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
                            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.URL_ORDER_LIST + "/" + order.getOrderId() + "/update_status?status=3", new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    JSONObject orderObj = jsonObject.optJSONObject("order");
                                    if (orderObj != null) {
                                        String status = orderObj.optString("status");
                                        if ("3".equalsIgnoreCase(status)) {
                                            orders.remove(order);
                                        }
                                        handler.sendEmptyMessage(STATUS_UNRECEIVE_LIST);
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
            }
        });
        unReceiveAdapter.setOnInViewClickListener(R.id.bt_received, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, final Integer position, Object values) {
                final Order order = (Order) values;

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
                        dialog.dismiss();
                        mDialog = CustomLoadingDialog.setLoadingDialog(getActivity(), "loading");
                        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.URL_ORDER_LIST + "/" + order.getOrderId() + "/update_status?status=5", new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                JSONObject orderObj = jsonObject.optJSONObject("order");
                                if (orderObj != null) {
                                    String status = orderObj.optString("status");
                                    if ("5".equalsIgnoreCase(status)) {
                                        orders.remove(order);
                                    } else {
                                        ToolUtils.setToast(getActivity(), "抱歉,确认收货失败");
                                    }
                                    handler.sendEmptyMessage(STATUS_UNRECEIVE_LIST);
                                    mDialog.dismiss();

                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                mDialog.dismiss();
                                ToolUtils.setToast(getActivity(), "抱歉,确认收货失败");
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
                    }
                });
                dialog.setCanceledOnTouchOutside(true);
                dialog.setCancelable(true);
                dialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                dialog.show();
            }
        });
        unReceiveAdapter.setOnInViewClickListener(R.id.ll_unreceive, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                Order1 order =(Order1)values;
                OrderDetailFragment1 orderDetailFragment = OrderDetailFragment1.newInstance(order.orderCode , 2);
                ((MainActivity) getActivity()).navigationToFragment(orderDetailFragment);
            }
        });
    }

    private void initData() {
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading",isDialogFirstVisible);
        isDialogFirstVisible=false;
        if (NetworkUtils.isNetworkAvailable(mContext)) {
            mNetErrorView.setVisibility(View.GONE);
            loadingView.setVisibility(View.GONE);
            FetchReceiveData();
        } else {
            if (mDialog != null)
                mDialog.dismiss();
            mEmptyView.setVisibility(View.GONE);
            mNetErrorView.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.VISIBLE);
        }
    }

    private void FetchReceiveData() {
        Map<String,String> params = new HashMap();
        params.put("userId",ZhaiDou.TESTUSERID);
        params.put("clientType","ANDROID");
        params.put("clientVersion","45");
        params.put("businessType","01");
        params.put("type", ZhaiDou.TYPE_ORDER_PRERECEIVE);
        ZhaiDouRequest request = new ZhaiDouRequest(mContext,Request.Method.POST,ZhaiDou.URL_ORDER_LIST ,params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (mDialog != null)
                    mDialog.dismiss();
                JSONArray dataArray = jsonObject.optJSONArray("data");
                mOrderList.addAll(JSON.parseArray(dataArray.toString(), Order1.class));
                if (mOrderList.size() > 0) {
                    handler.sendEmptyMessage(STATUS_UNRECEIVE_LIST);
                } else {
                    mListView.setVisibility(View.GONE);
                    loadingView.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (mDialog != null)
                    mDialog.dismiss();
                Toast.makeText(getActivity(), "加载失败", Toast.LENGTH_SHORT).show();
            }
        });
        mRequestQueue.add(request);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.netReload:
                initData();
                break;
        }
    }

    public class UnReceiveAdapter extends BaseListAdapter<Order1> {
        public UnReceiveAdapter(Context context, List<Order1> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_order_unreceive, null);
            TextView tv_order_time = ViewHolder.get(convertView, R.id.tv_unreceive_order_time);
            TextView tv_order_number = ViewHolder.get(convertView, R.id.tv_order_number);
            TextView tv_order_amount = ViewHolder.get(convertView, R.id.tv_order_amount);
            TextView tv_order_status = ViewHolder.get(convertView, R.id.tv_order_status);
            TextView bt_received = ViewHolder.get(convertView, R.id.bt_received);
            TextView bt_logistics = ViewHolder.get(convertView, R.id.bt_logistics);
            ImageView iv_order_img = ViewHolder.get(convertView, R.id.iv_order_img);
            Order1 order1 = getList().get(position);

            tv_order_time.setText(order1.creationTime);
            tv_order_number.setText(order1.orderCode);
            tv_order_amount.setText("￥" + order1.orderActualAmount);
            tv_order_status.setText(order1.orderShowStatus);
            if (1==order1.status) {
                bt_received.setVisibility(View.GONE);
                bt_logistics.setText("申请退款");
//                bt_logistics.setVisibility(item.isZero()?View.GONE:View.VISIBLE);
            } else if (4==order1.status) {
                bt_received.setVisibility(View.VISIBLE);
                bt_logistics.setText("查看物流");
            }
            ToolUtils.setImageCacheUrl(order1.childOrderPOList.get(0).orderItemPOList.get(0).pictureMiddleUrl, iv_order_img,R.drawable.icon_loading_defalut);
            return convertView;
        }
    }
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_order_unreceive));
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_order_unreceive));
    }
}
