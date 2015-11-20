package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.activities.LoginActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Product;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.LargeImgView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpecialSaleFragment1 extends BaseFragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_TITLE = "title";

    private String mParam1;
    private String mParam2;
    private String mTitle;


    private GridView mGridView;
    private TextView mTimerView;
    private ProductAdapter mAdapter;
    private Map<Integer, View> mHashMap = new HashMap<Integer, View>();
    private RequestQueue requestQueue;
    private List<Product> products = new ArrayList<Product>();

    private LinearLayout loadingView, nullNetView, nullView;
    private TextView reloadBtn, reloadNetBtn;

    private final int UPDATE_ADAPTER = 0;
    private final int UPDATE_TIMER_START = 3;

    private Dialog mDialog;

    private ImageView myCartBtn;

    private View rootView;
    private boolean isLogin;
    private long time;
    private Context mContext;

    private LargeImgView bannerLine;
    private ScrollView scrollView;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_ADAPTER:
                    loadingView.setVisibility(View.GONE);
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.nullReload:
                    initData();
                    break;
                case R.id.netReload:
                    initData();
                    break;
            }
        }
    };


    public static SpecialSaleFragment1 newInstance(String title, String param1, String param2) {
        SpecialSaleFragment1 fragment = new SpecialSaleFragment1();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    public SpecialSaleFragment1() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mTitle = getArguments().getString(ARG_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (rootView == null) {
            System.out.println("SpecialSaleFragment1.onCreateView");
            mContext = getActivity();
            rootView = inflater.inflate(R.layout.fragment_special_sale_list, container, false);

            loadingView = (LinearLayout) rootView.findViewById(R.id.loadingView);
            bannerLine = (LargeImgView) rootView.findViewById(R.id.bannerView);
            bannerLine.setDrawingCacheEnabled(true);
            ImageLoader.getInstance().displayImage(mParam2, bannerLine, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {
                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    if (bitmap != null) {
                        LargeImgView imageView1 = (LargeImgView) view;
                        imageView1.setScaleType(ImageView.ScaleType.FIT_XY);
                        imageView1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (screenWidth * bitmap.getHeight() / bitmap.getWidth())));
                        if (imageView1.getDrawingCache() != null){
                            imageView1.setImageBitmapLarge(bitmap);
                        }else {
                            if (bitmap.isRecycled()){
                                bitmap.recycle();
                                bitmap=null;
                            }
                        }
                    }
                }

                @Override
                public void onLoadingCancelled(String s, View view) {

                }
            });

            scrollView = (ScrollView) rootView.findViewById(R.id.scrollView);
            mGridView = (GridView) rootView.findViewById(R.id.gv_sale);
            mGridView.setEmptyView(mEmptyView);

            mAdapter = new ProductAdapter(getActivity(), products);
            mGridView.setAdapter(mAdapter);
            rootView.findViewById(R.id.ll_back).setOnClickListener(this);

            ((TextView) rootView.findViewById(R.id.tv_title)).setText(mTitle);
            loadingView = (LinearLayout) rootView.findViewById(R.id.loadingView);
            nullNetView = (LinearLayout) rootView.findViewById(R.id.nullNetline);
            nullView = (LinearLayout) rootView.findViewById(R.id.nullline);
            reloadBtn = (TextView) rootView.findViewById(R.id.nullReload);
            reloadBtn.setOnClickListener(onClickListener);
            reloadNetBtn = (TextView) rootView.findViewById(R.id.netReload);
            reloadNetBtn.setOnClickListener(onClickListener);

            requestQueue = Volley.newRequestQueue(getActivity());
            myCartBtn = (ImageView) rootView.findViewById(R.id.myCartBtn);
            myCartBtn.setOnClickListener(this);

            initData();

            mAdapter.setOnInViewClickListener(R.id.ll_single_layout, new BaseListAdapter.onInternalClickListener() {
                @Override
                public void OnClickListener(View parentV, View v, Integer position, Object values) {
                    GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance(products.get(position).getTitle(), products.get(position).getId());
                    Bundle bundle = new Bundle();
                    bundle.putInt("flags", 1);
                    bundle.putInt("index", products.get(position).getId());
                    bundle.putString("page", products.get(position).getTitle());
                    bundle.putBoolean("timer", false);
                    goodsDetailsFragment.setArguments(bundle);
                    ((MainActivity) getActivity()).navigationToFragmentWithAnim(goodsDetailsFragment);
                }
            });
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    /**
     * 初始化收据
     */
    private void initData() {
        mDialog = CustomLoadingDialog.setLoadingDialog(getActivity(), "loading", true);
        if (NetworkUtils.isNetworkAvailable(getActivity())) {
            FetchData(1);
        } else {
            if (mDialog != null)
                mDialog.dismiss();
            nullView.setVisibility(View.GONE);
            nullNetView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_back:
                ((MainActivity) getActivity()).popToStack(SpecialSaleFragment1.this);
                break;

            case R.id.myCartBtn:
                if (isLogin) {
                    ShopCartFragment shopCartFragment = ShopCartFragment.newInstance("", 0);
                    ((MainActivity) getActivity()).navigationToFragment(shopCartFragment);
                } else {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(1);
                    getActivity().startActivity(intent);
                }
                break;
        }
    }

    public void FetchData(int page) {
        JsonObjectRequest request = new JsonObjectRequest("http://stg.zhaidou.com/special_mall/api/sales/" + mParam1,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        mDialog.dismiss();
                        ToolUtils.setLog(jsonObject.toString());
                        JSONObject saleJson = jsonObject.optJSONObject("sale");
                        if (saleJson != null) {
                            String end_date = saleJson.optString("end_time");
                            Message timerMsg = new Message();
                            timerMsg.what = UPDATE_TIMER_START;
                            timerMsg.obj = end_date;
                            mHandler.sendMessage(timerMsg);
                            JSONArray items = saleJson.optJSONArray("merchandises");
                            if (items != null && items.length() > 2) {
                                if (items != null && items.length() > 0) {
                                    for (int i = 0; i < items.length(); i++) {
                                        JSONObject item = items.optJSONObject(i);
                                        int id = item.optInt("id");
                                        String title = item.optString("title");
                                        double price = item.optDouble("price");
                                        double cost_price = item.optDouble("cost_price");
                                        String image = item.optString("img");
//                                        int remaining = item.optInt("total_count");
                                        int remaining = item.optInt("percentum");
                                        Product product = new Product();
                                        product.setId(id);
                                        product.setPrice(price);
                                        product.setCost_price(cost_price);
                                        product.setTitle(title);
                                        product.setImage(image);
                                        product.setRemaining(remaining);
                                        products.add(product);
                                    }
                                    mHandler.sendEmptyMessage(UPDATE_ADAPTER);
                                }
                            } else {
                                mHandler.sendEmptyMessage(UPDATE_ADAPTER);
                            }

                        } else {
                            mHandler.sendEmptyMessage(UPDATE_ADAPTER);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mDialog.dismiss();
                nullView.setVisibility(View.VISIBLE);
                nullNetView.setVisibility(View.GONE);
            }
        }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        requestQueue.add(request);
    }

    public class ProductAdapter extends BaseListAdapter<Product> {
        public ProductAdapter(Context context, List<Product> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            convertView = mHashMap.get(position);
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_fragment_sale, null);
            TextView tv_name = ViewHolder.get(convertView, R.id.tv_name);
            ImageView image = ViewHolder.get(convertView, R.id.iv_single_item);
            image.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth / 2 - 1, screenWidth / 2 - 1));
            TextView tv_money = ViewHolder.get(convertView, R.id.tv_money);
            TextView tv_price = ViewHolder.get(convertView, R.id.tv_price);
            TextView tv_count = ViewHolder.get(convertView, R.id.tv_count);
            ImageView ll_sale_out = ViewHolder.get(convertView, R.id.ll_sale_out);
            TextView shopSaleTv = ViewHolder.get(convertView, R.id.shopSaleTv);
            ll_sale_out.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth / 2 - 1, screenWidth / 2 - 1));
            Product product = getList().get(position);
            tv_name.setText(product.getTitle());
            ToolUtils.setImageCacheUrl(product.getImage(), image, R.drawable.icon_loading_defalut);
            tv_price.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            tv_price.setText("￥" + ToolUtils.isIntPrice("" + product.getCost_price()));
            tv_count.setText("剩余 " + product.getRemaining() + "%");
            tv_money.setText("￥" + product.getPrice());
            ll_sale_out.setVisibility(product.getRemaining() == 0 ? View.VISIBLE : View.GONE);
            DecimalFormat df = new DecimalFormat("##.0");
            String zk = df.format(product.getPrice() / product.getCost_price() * 10);
            if (zk.contains(".0")) {
                int sales = (int) Double.parseDouble(zk);
                shopSaleTv.setText(sales + "折");
            } else {
                Double sales = Double.parseDouble(zk);
                shopSaleTv.setText(sales + "折");
            }
            shopSaleTv.setVisibility(View.VISIBLE);
            mHashMap.put(position, convertView);
            return convertView;
        }
    }
}
