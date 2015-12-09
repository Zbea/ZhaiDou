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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshScrollView;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.LoginActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Product;
import com.zhaidou.model.ShopSpecialItem;
import com.zhaidou.model.ShopTodayItem;
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
    private List<ShopTodayItem> items = new ArrayList<ShopTodayItem>();

    private LinearLayout loadingView, nullNetView, nullView;
    private TextView reloadBtn, reloadNetBtn;

    private final int UPDATE_ADAPTER = 0;

    private Dialog mDialog;

    private ImageView myCartBtn;

    private View rootView;
    private boolean isLogin;
    private long time;
    private Context mContext;

    private LargeImgView bannerLine;
    private PullToRefreshScrollView mScrollView;
    private int page = 1;
    private int pageSize;
    private int pageCount;
    private String imageUrl;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_ADAPTER:
                    setAddImage();
                    if (pageCount > pageSize * page)
                    {
                        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
                    } else
                    {
                        mScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    }
                    loadingView.setVisibility(View.GONE);
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    private PullToRefreshBase.OnRefreshListener2 onRefreshListener=new PullToRefreshBase.OnRefreshListener2()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView)
        {
            items.clear();
            page=1;
            FetchData();
        }
        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {
            page++;
            FetchData();
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
            imageUrl = getArguments().getString(ARG_PARAM2);
            mTitle = getArguments().getString(ARG_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (rootView == null) {
            mContext = getActivity();
            rootView = inflater.inflate(R.layout.fragment_special_sale_list, container, false);

            loadingView = (LinearLayout) rootView.findViewById(R.id.loadingView);
            bannerLine = (LargeImgView) rootView.findViewById(R.id.bannerView);
            bannerLine.setDrawingCacheEnabled(true);

            mScrollView = (PullToRefreshScrollView)rootView.findViewById(R.id.scrollView);
            mScrollView.setOnRefreshListener(onRefreshListener);
            mGridView = (GridView) rootView.findViewById(R.id.gv_sale);
            mGridView.setEmptyView(mEmptyView);

            mAdapter = new ProductAdapter(getActivity(), items);
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
                    GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance(items.get(position).title, items.get(position).goodsId);
                    Bundle bundle = new Bundle();
                    bundle.putInt("flags", 1);
                    bundle.putString("index", items.get(position).goodsId);
                    bundle.putString("page", items.get(position).title);
                    bundle.putBoolean("timer", false);
                    bundle.putBoolean("canShare", false);
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

            FetchData();

        } else {
            if (mDialog != null)
                mDialog.dismiss();
            nullView.setVisibility(View.GONE);
            nullNetView.setVisibility(View.VISIBLE);
        }
    }

    private void setAddImage()
    {
        DisplayImageOptions options=new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.icon_loading_item)
                .showImageForEmptyUri(R.drawable.icon_loading_item)
                .showImageOnFail(R.drawable.icon_loading_item)
                .resetViewBeforeLoading(true)//default 设置图片在加载前是否重置、复位
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();
        ImageLoader.getInstance().displayImage(imageUrl, bannerLine,options ,new ImageLoadingListener() {
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
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (screenWidth * bitmap.getHeight() / bitmap.getWidth()));
                    bannerLine.setLayoutParams(params);
                    imageView1.setLayoutParams(params);
                    if (imageView1.getDrawingCache() != null) {
                        imageView1.setImageBitmapLarge(bitmap);
                    } else {
                        if (bitmap.isRecycled()) {
                            bitmap.recycle();
                            bitmap = null;
                        }
                    }
                }
            }
            @Override
            public void onLoadingCancelled(String s, View view) {
                FetchData();
            }
        });
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

    public void FetchData() {
        String url=ZhaiDou.HomeGoodsListUrl +mParam1+"&pageNo="+ page + "&typeEnum=3";
        JsonObjectRequest request = new JsonObjectRequest(url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (mDialog != null)
                            mDialog.dismiss();
                        mScrollView.onRefreshComplete();
                        if (response == null) {
                            nullNetView.setVisibility(View.GONE);
                            nullView.setVisibility(View.VISIBLE);
                            return;
                        }
                        JSONObject obj;
                        JSONObject jsonObject1 = response.optJSONObject("data");
                        if (jsonObject1!=null)
                        {
                            JSONObject jsonObject = jsonObject1.optJSONObject("activityPO");
                            String id = jsonObject.optString("activityCode");
                            String title = jsonObject.optString("activityName");
                            long startTime = jsonObject.optLong("startTime");
                            long endTime = jsonObject.optLong("endTime");
                            imageUrl= jsonObject.optString("mainPic");
                            ToolUtils.setLog(""+endTime);
                            int overTime = Integer.parseInt((String.valueOf((endTime-startTime)/(24*60*60*1000))));
                            String introduce = jsonObject.optString("description");
                            int isNew = jsonObject.optInt("newFlag");
                            ShopSpecialItem shopSpecialItem = new ShopSpecialItem(id, title, null,startTime, endTime, overTime, null,isNew);

                            JSONObject jsonObject2 = jsonObject1.optJSONObject("pagePO");
                            pageCount=jsonObject2.optInt("totalCount");
                            pageSize=jsonObject2.optInt("pageSize");
                            if (jsonObject2!=null)
                            {
                                JSONArray jsonArray = jsonObject2.optJSONArray("items");
                                if (jsonArray!=null)

                                    for (int i = 0; i < jsonArray.length(); i++)
                                    {
                                        obj = jsonArray.optJSONObject(i);
                                        String Baseid = obj.optString("productId");
                                        String Listtitle = obj.optString("productName");
                                        double price = obj.optDouble("price");
                                        double cost_price = obj.optDouble("marketPrice");
                                        String imageUrl = obj.optString("productPicUrl");
                                        JSONObject jsonObject3=obj.optJSONObject("expandedResponse");
                                        int num = jsonObject3.optInt("stock");
                                        int totalCount = 100;
                                        int percentum =obj.optInt("progressPercentage");
                                        ShopTodayItem shopTodayItem = new ShopTodayItem(Baseid, Listtitle, imageUrl, price, cost_price, num, totalCount);
                                        shopTodayItem.percentum=percentum;
                                        items.add(shopTodayItem);
                                    }
                            }

                        }
                        mHandler.sendEmptyMessage(UPDATE_ADAPTER);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mDialog.dismiss();
                mScrollView.onRefreshComplete();
                if (page>1)
                {
                    page--;
                }
                nullNetView.setVisibility(View.GONE);
                nullView.setVisibility(View.VISIBLE);
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

    public class ProductAdapter extends BaseListAdapter<ShopTodayItem> {
        public ProductAdapter(Context context, List<ShopTodayItem> list) {
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
            ShopTodayItem shopTodayItem = getList().get(position);
            tv_name.setText(shopTodayItem.title);
            ToolUtils.setImageCacheUrl(shopTodayItem.imageUrl, image, R.drawable.icon_loading_defalut);
            tv_price.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            tv_price.setText("￥" + ToolUtils.isIntPrice("" + shopTodayItem.formerPrice));
            tv_count.setText("剩余 " + (100-shopTodayItem.percentum) + "%");
            tv_money.setText("￥" + ToolUtils.isIntPrice(""+shopTodayItem.currentPrice));
            ll_sale_out.setVisibility(shopTodayItem.num == 0 ? View.VISIBLE : View.GONE);
            DecimalFormat df = new DecimalFormat("##.0");
            String zk = df.format(shopTodayItem.currentPrice / shopTodayItem.formerPrice * 10);
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
