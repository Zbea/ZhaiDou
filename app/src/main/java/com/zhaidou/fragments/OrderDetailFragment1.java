package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.Order;
import com.zhaidou.model.Order1;
import com.zhaidou.model.OrderItem1;
import com.zhaidou.model.Store;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDetailFragment1 extends BaseFragment {
    private static final String ARG_ID = "id";
    private static final String ARG_TIMESTMP = "timestmp";
    private static final String ARG_ORDERCODE = "orderCode";
    private String mOrderCode;
    private long mParam2;
    private Order1 mOrder;
    private int flags = 0;

    private RequestQueue requestQueue;
    private ListView mListView;
    private TextView mSaleServiceTV;
    private OrderItemAdapter orderItemAdapter;
    private final int UPDATE_COUNT_DOWN_TIME = 2;
    private final int UPDATE_UI_TIMER_FINISH = 3;

    private boolean isTimerStart = false;
    private LinearLayout mNetWorkLayout;
    private OnColseSuccess onColseSuccess;

    double amount;
    private View rootView;
    private Context mContext;
    private List<OrderItem1> orderItems = new ArrayList<OrderItem1>();
    private String token;
    private FrameLayout mBottomLayout;
    private Order order;
    private ListView mStoreList;
    private StoreAdapter mStoreAdapter;
    private DialogUtils mDialogUtils;
    private Dialog mDialog;
    private String mUserId;

    public static OrderDetailFragment1 newInstance(String orderCode, int flags) {
        OrderDetailFragment1 fragment = new OrderDetailFragment1();
        Bundle args = new Bundle();
        args.putInt("flags", flags);
        args.putSerializable(ARG_ORDERCODE, orderCode);
        fragment.setArguments(args);
        return fragment;
    }

    public OrderDetailFragment1() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mOrderCode = getArguments().getString(ARG_ORDERCODE);
            flags = getArguments().getInt("flags");
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
            rootView = inflater.inflate(R.layout.fragment_order_detail_list, container, false);
            initView(rootView);
        }
        return rootView;
    }

    private void initView(View view) {
        mContext = getActivity();
        mNetWorkLayout = (LinearLayout) view.findViewById(R.id.noNetWork);
        view.findViewById(R.id.reload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckNetWork();
            }
        });
        mDialogUtils = new DialogUtils(mContext);
        mStoreList = (ListView) view.findViewById(R.id.storeList);

        mStoreAdapter = new StoreAdapter(mContext, new ArrayList<Store>());
        requestQueue = Volley.newRequestQueue(getActivity());

        token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        mUserId = SharedPreferencesUtil.getData(mContext, "userId", -1) + "";

        mStoreAdapter.setOnInViewClickListener(R.id.bt_logistics, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                Store store = (Store) values;
                if (ZhaiDou.STATUS_DEAL_SUCCESS == store.status || ZhaiDou.STATUS__DELIVERYED == store.status) {
                    OrderLogisticsMsgFragment logisticsMsgFragment = OrderLogisticsMsgFragment.newInstance(store);
                    ((BaseActivity) getActivity()).navigationToFragment(logisticsMsgFragment);
                }
            }
        });
        mStoreAdapter.setOnInViewClickListener(R.id.bt_received, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(final View parentV, final View v, Integer position, Object values) {
                final Store store = (Store) values;
                if (ZhaiDou.STATUS_DEAL_SUCCESS == store.status) {
                    OrderAfterSaleFragment afterSaleFragment = OrderAfterSaleFragment.newInstance(store, "");
                    ((MainActivity) getActivity()).navigationToFragment(afterSaleFragment);
                    afterSaleFragment.setOnReturnSuccess(new OnReturnSuccess() {
                        @Override
                        public void onSuccess(Store st) {
                            store.returnGoodsFlag=st.returnGoodsFlag;
                            mStoreAdapter.notifyDataSetChanged();
                        }
                    });
                } else if (ZhaiDou.STATUS__DELIVERYED == store.status) {
                    mDialogUtils.showDialog(mContext.getResources().getString(R.string.order_confirm), new DialogUtils.PositiveListener() {
                        @Override
                        public void onPositive() {
                            Map<String, String> params = new HashMap();
                            params.put("businessType", "01");
                            params.put("clientType", "ANDROID");
                            params.put("userId", mUserId);
                            params.put("version", versionName);
                            params.put("clientVersion", versionCode);
                            params.put("orderCode", store.orderCode);
                            ZhaiDouRequest request = new ZhaiDouRequest(mContext,Request.Method.POST, ZhaiDou.URL_ORDER_CONFIRM, params, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    int status = jsonObject.optInt("status");
                                    String message = jsonObject.optString("message");
                                    if (200 == status) {
                                        ShowToast("确认收货成功");
                                        store.orderShowStatus="交易完成";
                                        store.status=ZhaiDou.STATUS_DEAL_SUCCESS;
                                        mStoreAdapter.notifyDataSetChanged();
                                    } else {
                                        ShowToast(message);
                                    }
                                }
                            }, null);
                            requestQueue.add(request);
                        }
                    }, null);
                }
            }
        });
        mStoreList.setAdapter(mStoreAdapter);
        mStoreAdapter.notifyDataSetChanged();
        CheckNetWork();
    }

    private void CheckNetWork() {
        if (NetworkUtils.isNetworkAvailable(mContext)) {
            FetchOrderDetail(mOrderCode);
        } else {
            mNetWorkLayout.setVisibility(View.VISIBLE);
            ShowToast("网络异常");
        }
    }

    private void FetchOrderDetail(String orderCode) {
        mDialog = mDialogUtils.showLoadingDialog();
        Map<String, String> params = new HashMap<String, String>();
        params.put("businessType", "01");
        params.put("clientType", "ANDROID");
        params.put("version", versionName);
        params.put("clientVersion", versionCode);
        params.put("userId", mUserId);//mUserId//29650+""
        params.put("orderCode", orderCode);
        ZhaiDouRequest request = new ZhaiDouRequest(mContext,Request.Method.POST, ZhaiDou.URL_ORDER_DETAIL_LIST_URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (mDialog != null)
                    mDialog.dismiss();
                JSONArray array = jsonObject.optJSONArray("data");
                List<Store> stores = JSON.parseArray(array.toString(), Store.class);
                mStoreAdapter.addAll(stores);
                mStoreAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mDialog.dismiss();
                mNetWorkLayout.setVisibility(View.VISIBLE);
            }
        });
        requestQueue.add(request);
    }

    public class OrderItemAdapter extends BaseListAdapter<OrderItem1> {
        public OrderItemAdapter(Context context, List<OrderItem1> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_order_detail, null);
            TextView tv_name = ViewHolder.get(convertView, R.id.tv_name);
            TextView tv_specification = ViewHolder.get(convertView, R.id.tv_specification);
            TextView tv_count = ViewHolder.get(convertView, R.id.tv_count);
            ImageView iv_order_img = ViewHolder.get(convertView, R.id.iv_order_img);
            LinearLayout ll_count = ViewHolder.get(convertView, R.id.ll_count);
            TextView tv_zero_msg = ViewHolder.get(convertView, R.id.tv_zero_msg);
            TextView mPrice = ViewHolder.get(convertView, R.id.orderItemCurrentPrice);
            TextView mOldPrice = ViewHolder.get(convertView, R.id.orderItemFormalPrice);
            OrderItem1 item = getList().get(position);
            tv_name.setText(item.productName);
            tv_specification.setText(item.specifications);
            tv_count.setText(item.quantity + "");
            ToolUtils.setImageCacheUrl(item.pictureMiddleUrl, iv_order_img, R.drawable.icon_loading_defalut);
            mPrice.setText("￥" + ToolUtils.isIntPrice("" + item.price));
            mOldPrice.setText("￥" + ToolUtils.isIntPrice("" + item.marketPrice));
            mOldPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            return convertView;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_order_detail));
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_order_detail));
    }

    @Override
    public void onDestroyView() {
        if (onColseSuccess != null)
            onColseSuccess.colsePage();
        super.onDestroyView();
    }

    private class StoreAdapter extends BaseListAdapter<Store> {

        public StoreAdapter(Context context, List<Store> list) {
            super(context, list);
        }

        @Override
        public View bindView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_order_detail1, null);
            TextView mOrderNumber = ViewHolder.get(convertView, R.id.tv_order_number);
            TextView mOrderTime = ViewHolder.get(convertView, R.id.tv_order_time);
            TextView mOrderStatus = ViewHolder.get(convertView, R.id.tv_order_status);
            ListView mListView = ViewHolder.get(convertView, R.id.lv_order_list);
            TextView btn1 = ViewHolder.get(convertView, R.id.bt_received);
            TextView btn2 = ViewHolder.get(convertView, R.id.bt_logistics);
            LinearLayout mBottomLayout = ViewHolder.get(convertView, R.id.ll_bottom);

            final LinearLayout detailContainer = ViewHolder.get(convertView, R.id.detailContainer);
            TextView mReceiverName = ViewHolder.get(convertView, R.id.tv_receiver_name);
            TextView mReceiverPhone = ViewHolder.get(convertView, R.id.tv_receiver_phone);
            final TextView mReceiverAddress = ViewHolder.get(convertView, R.id.tv_receiver_address);
            TextView mOrderAmount = ViewHolder.get(convertView, R.id.total);

            final Store store = getList().get(position);
            mReceiverName.setText(store.deliveryAddressPO.realName);
            mReceiverPhone.setText(store.deliveryAddressPO.mobile);
            mReceiverAddress.setText(store.deliveryAddressPO.provinceName + store.deliveryAddressPO.cityName + store.deliveryAddressPO.address);
            mOrderAmount.setText("￥" + store.orderActualAmount);
            mOrderNumber.setText(store.orderCode + "");
            mOrderTime.setText(store.creationTime);
            mOrderStatus.setText(store.orderShowStatus);

            if (ZhaiDou.STATUS_DEAL_SUCCESS == store.status) {/**交易成功*/
                mBottomLayout.setVisibility(View.VISIBLE);
                btn1.setText("申请退货");
                btn2.setText("查看物流");
                btn1.setBackgroundResource(R.drawable.btn_green_click_bg);
                btn2.setBackgroundResource(R.drawable.btn_green_click_bg);
                btn1.setVisibility(store.isFinishAfterTime==1?View.GONE:View.VISIBLE);
                if (store.returnGoodsFlag == 1)
                    mBottomLayout.setVisibility(View.GONE);
            } else if (ZhaiDou.STATUS_UNDELIVERY == store.status || ZhaiDou.STATUS_PICKINGUP == store.status || ZhaiDou.STATUS_UNPAY == store.status||ZhaiDou.STATUS_ORDER_APPLY_CANCEL==store.status) {/**待发货,已拣货,待支付,退款申请*/
                mBottomLayout.setVisibility(View.GONE);
            } else if (ZhaiDou.STATUS__DELIVERYED == store.status) {
                mBottomLayout.setVisibility(View.VISIBLE);
                btn2.setText("查看物流");
                btn1.setText("确认收货");
                btn2.setBackgroundResource(R.drawable.btn_green_click_bg);
                btn1.setBackgroundResource(R.drawable.btn_red_click_selector);
            } else if (ZhaiDou.STATUS_ORDER_CANCEL == store.status||ZhaiDou.STATUS_RETURN_MONEY_SUCCESS==store.status) {//已取消//退款完成
                mBottomLayout.setVisibility(View.GONE);
            }

            final OrderItemAdapter adapter = new OrderItemAdapter(mContext, store.orderItemPOList) {
                @Override
                public int getCount() {
                    if (!store.isExpand) {
                        detailContainer.setVisibility(View.GONE);
                        return 1;
                    }
                    detailContainer.setVisibility(View.VISIBLE);
                    return super.getCount();
                }
            };
            mListView.setAdapter(adapter);
            setOnInViewClickListener(R.id.moreDetail, new onInternalClickListener() {
                @Override
                public void OnClickListener(View parentV, View v, Integer position, Object values) {
                    Store store1 = (Store) values;
                    store1.isExpand = !store1.isExpand;
                    adapter.notifyDataSetChanged();
                }
            });
            adapter.setOnInViewClickListener(R.id.itemLayout, new onInternalClickListener() {
                @Override
                public void OnClickListener(View parentV, View v, Integer position, Object values) {
                    OrderItem1 item = (OrderItem1) values;
                    GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance(item.productName, item.productCode+"");
                    ((BaseActivity) mContext).navigationToFragment(goodsDetailsFragment);
                }
            });

            return convertView;
        }
    }

    public void setOnColseSuccess(OnColseSuccess OnColseSuccess) {
        this.onColseSuccess = OnColseSuccess;
    }

    public interface OnColseSuccess {
        public void colsePage();
    }
}