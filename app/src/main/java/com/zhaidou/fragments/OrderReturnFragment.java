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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshListView;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Store;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class OrderReturnFragment extends BaseFragment implements View.OnClickListener, PullToRefreshBase.OnRefreshListener2<ListView> {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private PullToRefreshListView mListView;
    private Dialog mDialog;
    private LinearLayout loadingView;
    private TextView titleTv;
    private RequestQueue mRequestQueue;
    private ReturnAdapter returnAdapter;
    private List<Store> mStoreList;
    private final int UPDATE_RETURN_LIST = 1;
    private String token;
    private View rootView;
    private Context mContext;
    private View mEmptyView, mNetErrorView;
    private WeakHashMap<Integer, View> mHashMap = new WeakHashMap<Integer, View>();
    private String mUserId;
    private int mCurrentPage = 1;
    private DialogUtils mDialogUtils;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
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
            rootView = inflater.inflate(R.layout.fragment_return, container, false);
            initView(rootView);
        }
        return rootView;
    }

    private void initView(View view) {
        titleTv = (TypeFaceTextView) view.findViewById(R.id.title_tv);
        titleTv.setText(R.string.title_order_return);

        loadingView = (LinearLayout) view.findViewById(R.id.loadingView);
        mEmptyView = rootView.findViewById(R.id.nullline);
        mNetErrorView = rootView.findViewById(R.id.nullNetline);
        view.findViewById(R.id.netReload).setOnClickListener(this);
        mContext = getActivity();
        mDialogUtils = new DialogUtils(mContext);
        mStoreList = new ArrayList<Store>();
        mListView = (PullToRefreshListView) view.findViewById(R.id.lv_return);
        mListView.setMode(PullToRefreshBase.Mode.BOTH);
        mListView.setOnRefreshListener(this);
        returnAdapter = new ReturnAdapter(getActivity(), mStoreList);
        mListView.setAdapter(returnAdapter);
        mRequestQueue = Volley.newRequestQueue(getActivity());
        token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        mUserId = SharedPreferencesUtil.getData(mContext, "userId", -1) + "";
        returnAdapter.setOnInViewClickListener(R.id.orderlayout, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                Store store = (Store) values;
                ReturnDetailFragment returnDetailFragment = ReturnDetailFragment.newInstance(store);
                ((BaseActivity) getActivity()).navigationToFragment(returnDetailFragment);
            }
        });
        returnAdapter.setOnInViewClickListener(R.id.iv_delete, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, final Integer position, Object values) {
                final Store store = (Store) values;
                final Map<String, String> params = new HashMap();
                params.put("businessType", "01");
                params.put("userId",mUserId);
                params.put("orderCode", store.orderCode);
                DialogUtils mDialogUtils = new DialogUtils(getActivity());
                mDialogUtils.showDialog(mContext.getResources().getString(R.string.order_delete), new DialogUtils.PositiveListener() {
                    @Override
                    public void onPositive() {
                        ZhaiDouRequest request = new ZhaiDouRequest(mContext,Request.Method.POST, ZhaiDou.URL_ORDER_DELETE, params, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                int status = jsonObject.optInt("status");
                                String message = jsonObject.optString("message");
                                if (200 == status) {
                                    mStoreList.remove(store);
                                    returnAdapter.notifyDataSetChanged();
                                } else {
                                    ShowToast(message);
                                }
                            }
                        }, null);
                        mRequestQueue.add(request);
                    }
                }, null);
            }
        });
        initData();
    }

    private void initData() {
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
        if (NetworkUtils.isNetworkAvailable(mContext)) {
            mNetErrorView.setVisibility(View.GONE);
            loadingView.setVisibility(View.GONE);
            FetchReturnData(mCurrentPage = 1);
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
        switch (view.getId()) {
            case R.id.netReload:
                initData();
                break;
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        if (NetworkUtils.isNetworkAvailable(mContext)) {
            mStoreList.clear();
            FetchReturnData(mCurrentPage = 1);
        } else {
            ShowToast("网络不稳定");
        }
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        if (NetworkUtils.isNetworkAvailable(mContext)) {
            FetchReturnData(++mCurrentPage);
        } else {
            ShowToast("网络不稳定");
        }
    }

    public class ReturnAdapter extends BaseListAdapter<Store> {
        public ReturnAdapter(Context context, List<Store> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            convertView = mHashMap.get(position);
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_order_list, null);
            TextView tv_order_time = ViewHolder.get(convertView, R.id.tv_order_time);
            TextView tv_order_number = ViewHolder.get(convertView, R.id.tv_order_number);
            TextView tv_order_amount = ViewHolder.get(convertView, R.id.tv_order_amount);
            TextView tv_order_status = ViewHolder.get(convertView, R.id.tv_order_status);
            TextView remark = ViewHolder.get(convertView, R.id.remark);
            RelativeLayout mBottomLayout = ViewHolder.get(convertView, R.id.rl_btn);
            ImageView iv_order_img = ViewHolder.get(convertView, R.id.iv_order_img);
            LinearLayout iv_delete = ViewHolder.get(convertView, R.id.iv_delete);
            Store store = getList().get(position);
            tv_order_time.setText(store.createTime);
            tv_order_number.setText(store.orderCode);
            remark.setText("备注:" + store.mallReturnFlowDetailDTOList.get(0).remark);
            remark.setVisibility(View.GONE);
            tv_order_amount.setText("￥"+ store.actualAmount);
            tv_order_status.setText(store.statusShowName);
            ToolUtils.setImageCacheUrl(store.mallReturnFlowDetailDTOList.get(0).thumbnailPicUrl, iv_order_img, R.drawable.icon_loading_defalut);
            mBottomLayout.setVisibility(View.GONE);
            iv_delete.setVisibility(View.GONE);
            mHashMap.put(position, convertView);
            return convertView;
        }
    }

    private void FetchReturnData(final int page) {
        Map<String, String> params = new HashMap();
        params.put("userId",mUserId);//mUserId//29650+""
        params.put("clientType", "ANDROID");
        params.put("clientVersion", "45");
        params.put("businessType", "01");
        ZhaiDouRequest request = new ZhaiDouRequest(mContext,Request.Method.POST, ZhaiDou.URL_ORDER_RETURN_LIST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (mDialog != null)
                    mDialog.dismiss();
                int status = jsonObject.optInt("status");
                String message = jsonObject.optString("message");
                if (status == 200) {
                    JSONObject dataObj = jsonObject.optJSONObject("data");
                    int pageSize = dataObj.optInt("pageSize");
                    JSONArray array = dataObj.optJSONArray("items");
                    List<Store> stores = JSON.parseArray(array == null ? "" : array.toString(), Store.class);
                    if (stores.size() == 0 && page == 1) {
                        mListView.setVisibility(View.GONE);
                        mEmptyView.setVisibility(View.VISIBLE);
                        loadingView.setVisibility(View.VISIBLE);
                    }else if (stores.size()<pageSize){
                        mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    }
                    mStoreList.addAll(stores);
                    returnAdapter.notifyDataSetChanged();
                    mListView.onRefreshComplete();
                } else {
                    ShowToast(message);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        mRequestQueue.add(request);
    }

    @Override
    public void onStart() {
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
