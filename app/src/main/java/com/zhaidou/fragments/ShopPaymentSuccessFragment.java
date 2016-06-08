package com.zhaidou.fragments;


import android.app.Dialog;
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

import com.alibaba.fastjson.JSON;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.model.DeliveryAddress;
import com.zhaidou.model.OrderItem;
import com.zhaidou.model.Store;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.DialogUtils;
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
    private static final String ARG_ORDERCODE= "ordercode";
    private static final String INDEX = "index";
    private static final String ARG_AMOUNT = "amount";

    private String mOrderCode;
    private double mIndex;
    private String mAmount;
    private View mView;
    private Context mContext;
    private TypeFaceTextView backBtn, titleTv;
    private RequestQueue mRequestQueue;
    private String token;
    private final int UPDATE_PAY_SUCCESS_PAG = 1;
    private final int UPDATE_PAY_FAIL_PAG = 2;
    private DialogUtils mDialogUtils;
    private Dialog mDialog;
    private String mUserId;
    private TextView tv_receiver, tv_mobile, tv_address, tv_amount, tv_mall, tv_order_detail;
    private List<OrderItem> orderItems = new ArrayList<OrderItem>();
    private List<Store> mStoreList;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PAY_SUCCESS_PAG:
                    if (mStoreList.size() > 0) {
                        Store store = mStoreList.get(0);
                        DeliveryAddress deliveryAddressPO = store.deliveryAddressPO;
                        tv_receiver.setText("收件人：" + deliveryAddressPO.realName);
                        tv_address.setText("地址：" +deliveryAddressPO.provinceName+","+deliveryAddressPO.cityName+","+deliveryAddressPO.regionName+","+deliveryAddressPO.address);
                        tv_mobile.setText("电话：" + deliveryAddressPO.mobile);
                        tv_amount.setText("￥" + mAmount);
                    }
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
                case R.id.tv_mall:
                    ToolUtils.setLog("前往商城");
                    colseFragment(ShopPaymentSuccessFragment.this);
                    ((MainActivity) mContext).allfragment();
                    ((MainActivity) mContext).toHomeFragment();
                    break;
                case R.id.tv_order_detail:
                    ToolUtils.setLog("前往订单");
                    OrderDetailFragment1 orderDetailFragment1=OrderDetailFragment1.newInstance(mOrderCode,1);
                    ((MainActivity)getActivity()).navigationToFragment(orderDetailFragment1);
                    orderDetailFragment1.setOnColseSuccess(new OrderDetailFragment1.OnColseSuccess()
                    {
                        @Override
                        public void colsePage()
                        {
                            colseFragment(ShopPaymentSuccessFragment.this);
                        }
                    });
                    break;
            }
        }
    };

    public static ShopPaymentSuccessFragment newInstance(String orderCode, double index, String amount) {
        ShopPaymentSuccessFragment fragment = new ShopPaymentSuccessFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDERCODE, orderCode);
        args.putDouble(INDEX, index);
        args.putString(ARG_AMOUNT, amount);
        fragment.setArguments(args);
        return fragment;
    }

    public ShopPaymentSuccessFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mOrderCode = getArguments().getString(ARG_ORDERCODE);
            mIndex = getArguments().getDouble(INDEX);
            mAmount=getArguments().getString(ARG_AMOUNT);
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
        titleTv = (TypeFaceTextView) mView.findViewById(R.id.title_tv);
        titleTv.setText(R.string.shop_payment_success_text);
        tv_receiver = (TextView) mView.findViewById(R.id.successAddressNameTv);
        tv_mobile = (TextView) mView.findViewById(R.id.successAddressPhoneTv);
        tv_address = (TextView) mView.findViewById(R.id.successAddressinfoTv);
        tv_amount = (TextView) mView.findViewById(R.id.successTotalNum);
        tv_mall = (TextView) mView.findViewById(R.id.tv_mall);
        tv_order_detail = (TextView) mView.findViewById(R.id.tv_order_detail);
        tv_mall.setOnClickListener(onClickListener);
        tv_order_detail.setOnClickListener(onClickListener);
        mRequestQueue = Volley.newRequestQueue(getActivity());
        mDialogUtils=new DialogUtils(mContext);
        token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        mUserId = SharedPreferencesUtil.getData(mContext, "userId", -1) + "";
        mStoreList = new ArrayList<Store>();
        FetchOrderDetail(mOrderCode);
    }

    private void FetchOrderDetail(String orderCode) {
        mDialog = mDialogUtils.showLoadingDialog();
        Map<String, String> params = new HashMap<String, String>();
        params.put("businessType", "01");
        params.put("clientType", "ANDROID");
        params.put("version", "1.0.1");
        params.put("userId", mUserId);
        params.put("orderCode", orderCode);
        ZhaiDouRequest request = new ZhaiDouRequest(mContext,Request.Method.POST, new ZhaiDou().URL_ORDER_DETAIL_LIST_URL,params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (mDialog != null)
                    mDialog.dismiss();
                JSONArray array = jsonObject.optJSONArray("data");
                List<Store> stores = JSON.parseArray(array.toString(), Store.class);
                mStoreList.addAll(stores);
                mHandler.sendEmptyMessage(UPDATE_PAY_SUCCESS_PAG);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mDialog.dismiss();
            }
        });
        mRequestQueue.add(request);
    }

    private void colsePayment() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag((ShopPaymentFragment.class).getClass().getSimpleName());
        if (fragment != null)
            fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();
        fragmentManager.popBackStack();
    }

    @Override
    public void onDestroyView() {
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
