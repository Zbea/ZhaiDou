package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshGridView;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.WebViewActivity;
import com.zhaidou.adapter.ProductAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Product;
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

public class CollectFragment extends BaseFragment implements PullToRefreshBase.OnRefreshListener2<GridView> {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private List<Product> products = new ArrayList<Product>();

    private PullToRefreshGridView mGridView;
    private TextView titleTv;

    private RequestQueue mRequestQueue;
    private ProductAdapter productAdapter;

    private int count;
    private int currentpage = 1;

    private LinearLayout loadingView, nullLine;
    private Dialog mDialog;

    private long lastClickTime;
    private Context mContext;
    private DialogUtils mDialogUtils;
    private CollectCountChangeListener collectCountChangeListener;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mDialog != null) {
                mDialog.dismiss();
            }
            loadingView.setVisibility(View.GONE);
            productAdapter.notifyDataSetChanged();
            if (products.size() == 0) {
                loadingView.setVisibility(View.VISIBLE);
                nullLine.setVisibility(View.VISIBLE);
            }
        }
    };

    public static CollectFragment newInstance(String param1, String param2) {
        CollectFragment fragment = new CollectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public CollectFragment() {
        // Required empty public constructor
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
        View view = inflater.inflate(R.layout.fragment_collect, container, false);

        titleTv = (TypeFaceTextView) view.findViewById(R.id.title_tv);
        titleTv.setText(R.string.title_collect);
        mGridView = (PullToRefreshGridView) view.findViewById(R.id.gv_collect);
        mGridView.setMode(PullToRefreshBase.Mode.BOTH);
        mGridView.setOnRefreshListener(this);
        mContext = getActivity();
        mDialogUtils = new DialogUtils(mContext);

        mRequestQueue = Volley.newRequestQueue(mContext);
        productAdapter = new ProductAdapter(getActivity(), products, 2, screenWidth);
        mGridView.setAdapter(productAdapter);

        productAdapter.setOnInViewClickListener(R.id.ll_single_layout, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                if (System.currentTimeMillis() - lastClickTime < 1000) {
                    Toast.makeText(getActivity(), "豆豆，你点击太快啦。。。", Toast.LENGTH_SHORT).show();
                    lastClickTime = System.currentTimeMillis();
                    return;
                }
                Product product = (Product) values;
                Intent intent = new Intent();
                intent.putExtra("url", product.getUrl());
                intent.setClass(getActivity(), WebViewActivity.class);
                getActivity().startActivity(intent);
            }
        });
        productAdapter.setOnInViewClickListener(R.id.ll_collect_heart, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, final Integer position, Object values) {
                final Product product = (Product) values;

                mDialogUtils.showDialog(mContext.getString(R.string.dialog_hint_collect), new DialogUtils.PositiveListener() {
                    @Override
                    public void onPositive() {
                        int itemId = product.getId();
                        deleteTask(itemId, position);
                    }
                }, null);
            }
        });

        loadingView = (LinearLayout) view.findViewById(R.id.loadingView);
        nullLine = (LinearLayout) view.findViewById(R.id.nullLine);

        if (NetworkUtils.isNetworkAvailable(getActivity())) {
            mDialog = CustomLoadingDialog.setLoadingDialog(getActivity(), "loading",true);
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            },300);
            FetchCollectData();
        } else {
            ToolUtils.setToast(getActivity(), "抱歉,网络连接失败");
        }

        return view;
    }

    /**
     * author Scoield
     * created at 15/9/9 15:20
     * Description 请求收藏数据
     */
    private void FetchCollectData() {
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.USER_COLLECT_ITEM_URL + currentpage, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                mGridView.onRefreshComplete();
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                if (jsonObject != null) {
                    JSONArray article_items = jsonObject.optJSONArray("article_items");
                    JSONObject meta = jsonObject.optJSONObject("meta");
                    count = meta == null ? 0 : meta.optInt("count");
                    if (article_items != null && article_items.length() > 0) {
                        for (int i = 0; i < article_items.length(); i++) {
                            JSONObject articleObj = article_items.optJSONObject(i);
                            int id = articleObj.optInt("id");
                            int bean_likes_count = articleObj.optInt("bean_likes_count");
                            String title = articleObj.optString("title");
                            double price = articleObj.optDouble("price");
                            String url = articleObj.optString("url");
                            JSONArray asset_imgs = articleObj.optJSONArray("asset_imgs");
                            String thumb = null;
                            if (asset_imgs != null && asset_imgs.length() > 0) {
                                JSONObject picObj = asset_imgs.optJSONArray(0).optJSONObject(1);
                                thumb = picObj.optJSONObject("picture").optJSONObject("thumb").optString("url");
                            }
                            Product product = new Product(id, title, price, url, bean_likes_count, null, thumb);
                            product.setCollect(true);
                            products.add(product);
                        }
                    }
                    Message message = new Message();
                    message.arg1 = count;
                    mHandler.sendMessage(message);
                } else {
                    nullLine.setVisibility(View.VISIBLE);
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
                String token = (String) SharedPreferencesUtil.getData(mContext, "token", "");
                headers.put("SECAuthorization", token);
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        mRequestQueue.add(request);
    }

    /**
     * author Scoield
     * created at 15/9/9 15:24
     * description 删除收藏
     * param itemId   单品id
     * param position item在adapter中的position
     */
    private void deleteTask(int itemId, final int position) {
        mDialog = CustomLoadingDialog.setLoadingDialog(getActivity(), "");
        int userId = (Integer) SharedPreferencesUtil.getData(mContext, "userId", -1);
        final String token = (String) SharedPreferencesUtil.getData(mContext, "token", "");
        Map<String, String> params = new HashMap<String, String>();
        params.put("liker_id", userId + "");
        params.put("article_item_id", itemId + "");
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.USER_DELETE_COLLECT_ITEM_URL, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (mDialog != null)
                    mDialog.dismiss();
                if (jsonObject != null) {
                    boolean like_state = jsonObject.optBoolean("like_state");
                    int likes = jsonObject.optInt("likes");
                    if (position < productAdapter.getCount()) {
                        productAdapter.remove(position);
                        if (collectCountChangeListener != null && like_state == false) {
                            collectCountChangeListener.onCountChange(productAdapter.getCount(), CollectFragment.this);
                        }
                        if (productAdapter.getCount() == 0) {
                            loadingView.setVisibility(View.VISIBLE);
                            nullLine.setVisibility(View.VISIBLE);
                        }
                        productAdapter.notifyDataSetChanged();
                    }
                } else {
                    ShowToast("抱歉,取消失败");
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
    }

    public void setCollectCountChangeListener(CollectCountChangeListener collectCountChangeListener) {
        this.collectCountChangeListener = collectCountChangeListener;
    }

    public interface CollectCountChangeListener {
        public void onCountChange(int count, Fragment fragment);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
        String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
        refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

        currentpage = 1;
        products.clear();
        FetchCollectData();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mGridView.onRefreshComplete();
            }
        }, 3000);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
        if (count != -1 && productAdapter.getCount() == count) {
            Toast.makeText(getActivity(), "已经加载完毕", Toast.LENGTH_SHORT).show();
            mGridView.onRefreshComplete();
            mGridView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            return;
        }
        ++currentpage;
        FetchCollectData();
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_collect));
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_collect));
    }
}
