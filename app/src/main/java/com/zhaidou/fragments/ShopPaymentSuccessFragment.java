package com.zhaidou.fragments;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.zhaidou.model.Order;
import com.zhaidou.model.OrderItem;
import com.zhaidou.model.Receiver;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by roy on 15/7/24.
 */
public class ShopPaymentSuccessFragment extends BaseFragment {
    private static final String ARG_ORDERID = "orderId";
    private static final String INDEX = "index";
    private static final String ARG_ORDER = "order";

    private long mOrderId;
    private double mIndex;
    private View mView;
    private Context mContext;
    private Order mOrder;
    private TypeFaceTextView backBtn, titleTv;
    private RequestQueue mRequestQueue;
    private String token;
    private final int UPDATE_PAY_SUCCESS_PAG=1;
    private final int UPDATE_PAY_FAIL_PAG=2;
    private TextView tv_receiver,tv_mobile,tv_address,tv_amount,tv_mall,tv_order_detail;
    private List<OrderItem> orderItems = new ArrayList<OrderItem>();

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_PAY_SUCCESS_PAG:
                    Order order=(Order)msg.obj;
                    tv_receiver.setText("收件人："+order.getReceiver().getName());
                    tv_address.setText("地址："+order.getReceiver().getProvince()+","+ order.getReceiver().getCity()+","+ order.getReceiver().getArea()+","+order.getReceiver().getAddress());
                    tv_mobile.setText("电话："+order.getReceiver().getPhone());
                    Receiver receiver=order.getReceiver();
                    tv_amount.setText("￥"+ToolUtils.isIntPrice("" +order.getAmount()+""));
                    mOrder=order;
                    break;
                case UPDATE_PAY_FAIL_PAG:
                    tv_order_detail.setClickable(false);
                    break;
            }
        }
    };

    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.back_btn:
                    colseFragment(ShopPaymentSuccessFragment.this);
                    break;
                case R.id.tv_mall:
                    ToolUtils.setLog("前往商城");
                    ((MainActivity)mContext).allfragment();
                    ((MainActivity)mContext).toHomeFragment();
                    break;
                case R.id.tv_order_detail:
//                    ToolUtils.setLog("前往订单");
//                    OrderDetailFragment orderDetailFragment=OrderDetailFragment.newInstance(mOrderId+"",0,mOrder,1);
//                    ((MainActivity)getActivity()).navigationToFragment(orderDetailFragment);
//                    orderDetailFragment.setOnColseSuccess(new OrderDetailFragment.OnColseSuccess()
//                    {
//                        @Override
//                        public void colsePage()
//                        {
//                            colseFragment(ShopPaymentSuccessFragment.this);
//                        }
//                    });
                    break;
            }
        }
    };


    public static ShopPaymentSuccessFragment newInstance(long orderId, double index,Order order) {
        ShopPaymentSuccessFragment fragment = new ShopPaymentSuccessFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ORDERID, orderId);
        args.putDouble(INDEX, index);
        args.putSerializable(ARG_ORDER,order);
        fragment.setArguments(args);
        return fragment;
    }

    public ShopPaymentSuccessFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mOrderId = getArguments().getLong(ARG_ORDERID);
            mIndex = getArguments().getDouble(INDEX);
            mOrder=(Order)getArguments().getSerializable(ARG_ORDER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.shop_payment_success_page, container, false);
            mContext = getActivity();
            initView();
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) mView.getParent();
        if (parent != null) {
            parent.removeView(mView);
        }

        return mView;
    }


    /**
     * 初始化数据
     */
    private void initView() {
        backBtn = (TypeFaceTextView) mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);
        titleTv = (TypeFaceTextView) mView.findViewById(R.id.title_tv);
        titleTv.setText(R.string.shop_payment_success_text);
        tv_receiver=(TextView)mView.findViewById(R.id.successAddressNameTv);
        tv_mobile=(TextView)mView.findViewById(R.id.successAddressPhoneTv);
        tv_address=(TextView)mView.findViewById(R.id.successAddressinfoTv);
        tv_amount=(TextView)mView.findViewById(R.id.successTotalNum);
        tv_mall=(TextView)mView.findViewById(R.id.tv_mall);
        tv_order_detail=(TextView)mView.findViewById(R.id.tv_order_detail);
        tv_mall.setOnClickListener(onClickListener);
        tv_order_detail.setOnClickListener(onClickListener);
        mRequestQueue= Volley.newRequestQueue(getActivity());
        token=(String) SharedPreferencesUtil.getData(getActivity(),"token","");

        FetchData(mOrderId);
    }


    private void FetchData(long orderId) {
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.URL_ORDER_LIST + "/" + orderId, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (jsonObject != null)
                {
                    JSONObject orderObj = jsonObject.optJSONObject("order");
                    double amount = orderObj.optDouble("amount");
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
                    String province=receiverObj.optString("parent_name");
                    String city=receiverObj.optString("city_name");
                    String area=receiverObj.optString("provider_name");
                    String address = receiverObj.optString("address");
                    String phone = receiverObj.optString("phone");
                    String name = receiverObj.optString("name");

                    JSONArray order_items = orderObj.optJSONArray("order_items");
                    if (order_items != null && order_items.length() > 0) {
                        for (int i = 0; i < order_items.length(); i++) {
                            JSONObject item = order_items.optJSONObject(i);
                            int itemId = item.optInt("id");
                            double itemPrice = item.optDouble("price");
                            int count = item.optInt("count");
                            double cost_price = item.optDouble("cost_price");
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

                    Receiver receiver=new Receiver(receiverId,address,province,city,area,phone,name);
                    Order order = new Order("", id, number, amount, status, status_ch, created_at_for, created_at, receiver, null, receiver_address, receiver_phone, deliver_number, receiver_name);
                    Message message = new Message();
                    message.obj = order;
                    message.what = UPDATE_PAY_SUCCESS_PAG;
                    mHandler.sendMessage(message);
                }
                else
                {
                    mHandler.sendEmptyMessage(UPDATE_PAY_FAIL_PAG);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                mHandler.sendEmptyMessage(UPDATE_PAY_FAIL_PAG);
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

    private void colsePayment()
    {
        FragmentManager fragmentManager=getFragmentManager();
        Fragment fragment=fragmentManager.findFragmentByTag((ShopPaymentFragment.class).getClass().getSimpleName());
        if (fragment!=null)
            fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();
        fragmentManager.popBackStack();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.shop_payment_success_text));
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.shop_payment_success_text));
    }
}
