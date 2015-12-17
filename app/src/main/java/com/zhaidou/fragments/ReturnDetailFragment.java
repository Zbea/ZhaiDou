package com.zhaidou.fragments;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.OrderItem1;
import com.zhaidou.model.Store;
import com.zhaidou.utils.ToolUtils;

import java.util.List;

public class ReturnDetailFragment extends BaseFragment {

    private static final String ARG_STORE = "store";

    private Store mStore;
    private View rootView;
    private TextView mOrderNumber, mOrderTime, mOrderStatus,
            mReceiverName, mReceiverPhone, mReceiverAddress, mReceiverTime,
            mOrderAmount, mOrderEdit, mCancelOrder, mOrderTimer, goodsInfo;
    private ListView mListView;
    private LinearLayout loadingView;
    private Context mContext;
    private OrderItemAdapter orderItemAdapter;
    private RequestQueue requestQueue;
    public static ReturnDetailFragment newInstance(Store store) {
        ReturnDetailFragment fragment = new ReturnDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_STORE, store);
        fragment.setArguments(args);
        return fragment;
    }
    public ReturnDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStore = (Store) getArguments().getSerializable(ARG_STORE);
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
            rootView = inflater.inflate(R.layout.fragment_order_detail, container, false);
            initView(rootView);
        }
        return rootView;
    }
    private void initView(View view) {
        mContext = getActivity();
//        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading", true);
        loadingView = (LinearLayout) view.findViewById(R.id.loadingView);
        mOrderNumber = (TextView) view.findViewById(R.id.tv_order_number);
        mOrderTime = (TextView) view.findViewById(R.id.tv_order_time);
        mOrderStatus = (TextView) view.findViewById(R.id.tv_order_status);
        mReceiverName = (TextView) view.findViewById(R.id.tv_receiver_name);
        mReceiverPhone = (TextView) view.findViewById(R.id.tv_receiver_phone);
        mReceiverAddress = (TextView) view.findViewById(R.id.tv_receiver_address);
        mReceiverTime = (TextView) view.findViewById(R.id.tv_receiver_name);
        mOrderAmount = (TextView) view.findViewById(R.id.tv_order_amount);
        mOrderEdit = (TextView) view.findViewById(R.id.tv_order_edit);
        mCancelOrder = (TextView) view.findViewById(R.id.tv_cancel_order);
        goodsInfo = (TextView) view.findViewById(R.id.goodsInfo);
        mListView = (ListView) view.findViewById(R.id.lv_order_list);
        orderItemAdapter = new OrderItemAdapter(getActivity(), mStore.mallReturnFlowDetailDTOList);
        mListView.setAdapter(orderItemAdapter);
//        mListView.setOnItemClickListener(onItemClickListener);
        requestQueue = Volley.newRequestQueue(getActivity());
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//                FetchOrderDetail(mOrderId);
//            }
//        }, 300);

        mCancelOrder.setOnClickListener(this);

        loadingView.setVisibility(View.GONE);

        mOrderNumber.setText(mStore.orderCode);
        mOrderTime.setText(mStore.createTime);
        mOrderStatus.setText(mStore.statusShowName);
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
            ToolUtils.setImageCacheUrl(item.thumbnailPicUrl, iv_order_img, R.drawable.icon_loading_defalut);
            mPrice.setText("￥" +item.salePrice);
            mOldPrice.setText("￥" + ToolUtils.isIntPrice("" + item.price));
            mOldPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            return convertView;
        }
    }
}
