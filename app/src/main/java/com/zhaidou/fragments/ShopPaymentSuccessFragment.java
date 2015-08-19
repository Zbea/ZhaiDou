package com.zhaidou.fragments;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pulltorefresh.PullToRefreshBase;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.model.Order;
import com.zhaidou.model.OrderItem;
import com.zhaidou.model.Receiver;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by roy on 15/7/24.
 */
public class ShopPaymentSuccessFragment extends BaseFragment {
    private static final String ARG_ORDERID = "orderId";
    private static final String INDEX = "index";

    private long mOrderId;
    private double mIndex;
    private View mView;
    private Context mContext;

    private TypeFaceTextView backBtn, titleTv;
    private RequestQueue mRequestQueue;
    private String token;
    private final int UPDATE_PAY_SUCCESS_PAG=1;
    private TextView tv_receiver,tv_mobile,tv_address,tv_amount,tv_mall,tv_order_detail;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_PAY_SUCCESS_PAG:
                    Order order=(Order)msg.obj;
                    tv_receiver.setText(order.getReceiver_name());
                    tv_mobile.setText(order.getReceiver_phone());
                    Receiver receiver=order.getReceiver();
                    tv_address.setText(receiver.getProvince()+","+receiver.getCity()+","+receiver.getArea()+","+receiver.getAddress());
                    tv_amount.setText(order.getAmount()+"");
                    break;
            }
        }
    };
    /**
     * 下拉刷新
     */
    private PullToRefreshBase.OnRefreshListener2 refreshListener = new PullToRefreshBase.OnRefreshListener2() {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView) {

        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView) {

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
                    ((MainActivity) getActivity()).popToStack(ShopPaymentSuccessFragment.this);
                    break;
                case R.id.tv_mall:
                    ShopSpecialFragment shopSpecialFragment = ShopSpecialFragment.newInstance("", 0);
                    ((MainActivity) getActivity()).navigationToFragment(shopSpecialFragment);
                    colseFragment(ShopPaymentSuccessFragment.this);

                    FragmentManager fragmentManager=getFragmentManager();
                    Fragment fragment=fragmentManager.findFragmentByTag((GoodsDetailsFragment.class).getClass().getSimpleName());
                    Fragment fragment1=fragmentManager.findFragmentByTag((ShopCartFragment.class).getClass().getSimpleName());
                    Fragment fragment2=fragmentManager.findFragmentByTag((ShopTodaySpecialFragment.class).getClass().getSimpleName());
                    Fragment fragment3=fragmentManager.findFragmentByTag((OrderDetailFragment.class).getClass().getSimpleName());
                    FragmentTransaction transaction=fragmentManager.beginTransaction();
                    transaction.remove(fragment);
                    transaction.remove(fragment1);
                    transaction.remove(fragment2);
                    transaction.remove(fragment3);
                    transaction.commitAllowingStateLoss();

                    fragmentManager.popBackStack();


                    break;
                case R.id.tv_order_detail:
                    OrderDetailFragment orderDetailFragment=OrderDetailFragment.newInstance(mOrderId+"",0,null);
                    ((MainActivity)getActivity()).navigationToFragment(orderDetailFragment);
                    break;
            }
        }
    };

    public static ShopPaymentSuccessFragment newInstance(long orderId, double index) {
        ShopPaymentSuccessFragment fragment = new ShopPaymentSuccessFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ORDERID, orderId);
        args.putDouble(INDEX, index);
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
                if (jsonObject != null) {
                    JSONObject orderObj = jsonObject.optJSONObject("order");
                    int amount = orderObj.optInt("amount");
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
                    Receiver receiver=new Receiver(receiverId,address,province,city,area,phone,name);
                    Order order = new Order("", id, number, amount, status, status_ch, created_at_for, created_at, receiver, null, receiver_address, receiver_phone, deliver_number, receiver_name);
                    Message message = new Message();
                    message.obj = order;
                    message.what = UPDATE_PAY_SUCCESS_PAG;
                    mHandler.sendMessage(message);
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
                return headers;
            }
        };
        mRequestQueue.add(request);
    }

}
