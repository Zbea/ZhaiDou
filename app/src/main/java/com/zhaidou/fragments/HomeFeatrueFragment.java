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
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.zhaidou.adapter.ShopTodaySpecialAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.ShopSpecialItem;
import com.zhaidou.model.ShopTodayItem;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.LargeImgView;
import com.zhaidou.view.ListViewForScrollView;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFeatrueFragment extends BaseFragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_TITLE = "title";

    private String mParam1;
    private String mParam2;
    private String mTitle;
    private int type=1;//如果type=1则为特卖列表 type=2则为专题页

    private GridView mGridView;
    private TypeFaceTextView introduceTv;
    private ProductAdapter mAdapter;
    private Map<Integer, View> mHashMap = new HashMap<Integer, View>();
    private RequestQueue requestQueue;
    private List<ShopTodayItem> items = new ArrayList<ShopTodayItem>();

    private LinearLayout loadingView, nullNetView, nullView,nullDataView;
    private TextView reloadBtn, reloadNetBtn;

    private final int UPDATE_ADAPTER = 0;
    private final int UPDATE_TIMER_START_AND_DETAIL_DATA = 1;

    private Dialog mDialog;

    private ImageView myCartBtn;
    private TextView titleTv;
    private View rootView;
    private boolean isLogin;
    private Context mContext;
    private LargeImgView bannerLine;
    private PullToRefreshScrollView mScrollView,mScrollView1;
    private ListViewForScrollView mListView;
    private ShopTodaySpecialAdapter adapter;
    private String introduce;//引文介绍
    private int page = 1;
    private int pageSize;
    private int pageCount;
    private String imageUrl;
    private ShopSpecialItem shopSpecialItem;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_ADAPTER:
                    setAddImage();
                    titleTv.setText(shopSpecialItem.title);
                    if (pageCount > pageSize * page)
                    {
                        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
                    } else
                    {
                        mScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    }
                    loadingView.setVisibility(View.GONE);
                    mAdapter.notifyDataSetChanged();
                    mScrollView.setVisibility(View.VISIBLE);
                    mScrollView1.setVisibility(View.GONE);
                    break;
            }
        }
    };

    private Handler handler = new Handler() {
        public void handleMessage(final Message msg) {
            switch (msg.what)
            {
                case UPDATE_TIMER_START_AND_DETAIL_DATA:
                    adapter.notifyDataSetChanged();
                    titleTv.setText(shopSpecialItem.title);
                    if (pageCount > pageSize * page)
                    {
                        mScrollView1.setMode(PullToRefreshBase.Mode.BOTH);
                    } else
                    {
                        mScrollView1.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    }
                    introduceTv.setText(introduce);
                    loadingView.setVisibility(View.GONE);
                    mScrollView.setVisibility(View.GONE);
                    mScrollView1.setVisibility(View.VISIBLE);
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

    /**
     * adapter短点击事件
     */
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance(items.get(i).title, items.get(i).goodsId);
            Bundle bundle=new Bundle();
            bundle.putString("page",items.get(i).title);
            bundle.putString("index",items.get(i).goodsId);
            bundle.putBoolean("canShare",false);
            goodsDetailsFragment.setArguments(bundle);
            ((MainActivity) getActivity()).navigationToFragmentWithAnim(goodsDetailsFragment);
        }
    };

    public static HomeFeatrueFragment newInstance(String title, String param1, String param2) {
        HomeFeatrueFragment fragment = new HomeFeatrueFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    public HomeFeatrueFragment() {
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
            rootView = inflater.inflate(R.layout.fragment_home_featrue, container, false);

            loadingView = (LinearLayout) rootView.findViewById(R.id.loadingView);
            bannerLine = (LargeImgView) rootView.findViewById(R.id.bannersView);
            bannerLine.setDrawingCacheEnabled(true);

            mScrollView = (PullToRefreshScrollView)rootView.findViewById(R.id.scrollView);
            mScrollView.setOnRefreshListener(onRefreshListener);
            mGridView = (GridView) rootView.findViewById(R.id.gv_sale);
            mGridView.setEmptyView(mEmptyView);

            mAdapter = new ProductAdapter(getActivity(), items);
            mGridView.setAdapter(mAdapter);
            rootView.findViewById(R.id.ll_back).setOnClickListener(this);

            titleTv=(TextView) rootView.findViewById(R.id.tv_title);
//            titleTv.setText(mTitle);
            loadingView = (LinearLayout) rootView.findViewById(R.id.loadingView);
            nullNetView = (LinearLayout) rootView.findViewById(R.id.nullNetline);
            nullDataView = (LinearLayout) rootView.findViewById(R.id.nullDataline);
            nullView = (LinearLayout) rootView.findViewById(R.id.nullline);
            reloadBtn = (TextView) rootView.findViewById(R.id.nullReload);
            reloadBtn.setOnClickListener(onClickListener);
            reloadNetBtn = (TextView) rootView.findViewById(R.id.netReload);
            reloadNetBtn.setOnClickListener(onClickListener);

            introduceTv = (TypeFaceTextView) rootView.findViewById(R.id.adText);

            mListView = (ListViewForScrollView) rootView.findViewById(R.id.shopListView);
            mListView.setOnItemClickListener(onItemClickListener);
            adapter = new ShopTodaySpecialAdapter(mContext, items);
            mListView.setAdapter(adapter);
            mScrollView1 = (PullToRefreshScrollView) rootView.findViewById(R.id.scrollView1);
            mScrollView1.setOnRefreshListener(onRefreshListener);

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
            nullDataView.setVisibility(View.GONE);
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
                     if (bitmap.getHeight() < 4000)
                            {
                                imageView1.setImageBitmap(bitmap);
                            } else
                            {
                                imageView1.setImageBitmapLarge(bitmap);
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_back:
                ((MainActivity) getActivity()).popToStack(HomeFeatrueFragment.this);
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
                        mScrollView1.onRefreshComplete();
                        if (response == null)
                        {
                            if (page==1)
                            {
                                nullNetView.setVisibility(View.GONE);
                                nullView.setVisibility(View.VISIBLE);
                                nullDataView.setVisibility(View.GONE);
                            }
                            else
                            {
                                ToolUtils.setToast(mContext,R.string.loading_fail_txt);
                            }
                            return;
                        }
                        JSONObject obj;
                        int status = response.optInt("status");
                        JSONObject jsonObject1 = response.optJSONObject("data");
                        if (jsonObject1!=null)
                        {
                            JSONObject jsonObject = jsonObject1.optJSONObject("activityPO");
                            String id = jsonObject.optString("activityCode");
                            String title = jsonObject.optString("activityName");
                            long startTime = jsonObject.optLong("startTime");
                            long endTime = jsonObject.optLong("endTime");
                            imageUrl= jsonObject.optString("mainPic");
                            type= jsonObject.optInt("composingType");
                            ToolUtils.setLog(""+type);
                            int overTime = Integer.parseInt((String.valueOf((endTime-startTime)/(24*60*60*1000))));
                            introduce = jsonObject.optString("description");
                            int isNew = jsonObject.optInt("newFlag");
                            shopSpecialItem = new ShopSpecialItem(id, title, null,startTime, endTime, overTime, null,isNew);

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
                                        String comment = obj.optString("comment")=="null"?"":obj.optString("comment");
                                        ShopTodayItem shopTodayItem = new ShopTodayItem(Baseid, Listtitle, imageUrl, price, cost_price, num, totalCount);
                                        shopTodayItem.percentum=percentum;
                                        shopTodayItem.comment=comment;
                                        items.add(shopTodayItem);
                                    }
                            }
                            if (type==1)
                            {
                                handler.sendEmptyMessage(UPDATE_TIMER_START_AND_DETAIL_DATA);
                            }
                            else
                            {
                                mHandler.sendEmptyMessage(UPDATE_ADAPTER);
                            }
                        }
                        else
                        {
                            if (status==200)
                            {
                                if (page==1)
                                {
                                    nullNetView.setVisibility(View.GONE);
                                    nullView.setVisibility(View.GONE);
                                    nullDataView.setVisibility(View.VISIBLE);
                                }
                                else
                                {
                                    ToolUtils.setToast(mContext,R.string.loading_fail_txt);
                                }
                            }
                            else
                            {
                                if (page==1)
                                {
                                    nullNetView.setVisibility(View.GONE);
                                    nullView.setVisibility(View.VISIBLE);
                                    nullDataView.setVisibility(View.GONE);
                                }
                                else
                                {
                                    ToolUtils.setToast(mContext,R.string.loading_fail_txt);
                                }
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mDialog.dismiss();
                mScrollView.onRefreshComplete();
                if (page==1)
                {
                    nullNetView.setVisibility(View.GONE);
                    nullView.setVisibility(View.VISIBLE);
                    nullDataView.setVisibility(View.GONE);
                }
                else
                {
                    page--;
                    ToolUtils.setToast(mContext,R.string.loading_fail_txt);
                }
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
