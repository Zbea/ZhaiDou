package com.zhaidou.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshScrollView;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.HomeCompetitionActivity;
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.activities.LoginActivity;
import com.zhaidou.activities.SearchActivity;
import com.zhaidou.adapter.ShopSpecialAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.ShopSpecialItem;
import com.zhaidou.model.SwitchImage;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomBannerView;
import com.zhaidou.view.ListViewForScrollView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainHomeFragment extends BaseFragment implements
        AdapterView.OnItemClickListener, View.OnClickListener,
        PullToRefreshBase.OnRefreshListener2<ScrollView>

{
    private static final String URL = "targetUrl";
    private static final String TYPE = "type";

    private ListView listView;

    private int currentPage = 1;
    private int count = -1;


    private static final int UPDATE_BANNER = 4;

    private ImageView mSearchView;
    private View view;
    private Dialog mDialog;
    private Context mContext;

    private List<ShopSpecialItem> items = new ArrayList<ShopSpecialItem>();
    private ShopSpecialAdapter adapterList;
    private RequestQueue mRequestQueue;
    private List<SwitchImage> banners = new ArrayList<SwitchImage>();

    private LinearLayout loadingView, nullNetView, nullView;
    private TextView reloadBtn, reloadNetBtn;
    private CustomBannerView customBannerView;
    private LinearLayout linearLayout;
    private PullToRefreshScrollView mScrollView;
    private TextView mCountTv;
    private FrameLayout mCartIv;


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(ZhaiDou.IntentRefreshCartGoodsTag))
            {
                initCartTips();
            }
            if (action.equals(ZhaiDou.IntentRefreshLoginTag))
            {
                initCartTips();
            }
            if (action.equals(ZhaiDou.IntentRefreshLoginExitTag))
            {
                initCartTips();
            }
        }
    };

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1001)  {
                mScrollView.onRefreshComplete();
                adapterList.notifyDataSetChanged();
                if (mDialog != null)
                    mDialog.dismiss();
                loadingView.setVisibility(View.GONE);

            } else if (msg.what == UPDATE_BANNER) {
                setAdView();
            }
        }
    };

    /**
     * 广告轮播设置
     */
    private void setAdView() {
        if (customBannerView==null)
        {
            customBannerView=new CustomBannerView(mContext,banners,true);
            customBannerView.setLayoutParams(screenWidth, screenWidth * 300 / 750);
            customBannerView.setOnBannerClickListener(new CustomBannerView.OnBannerClickListener()
            {
                @Override
                public void onClick(int postion)
                {
                    SwitchImage item = banners.get(postion);
                    ToolUtils.setBannerGoto(item,mContext);
                }
            });
            linearLayout.addView(customBannerView);
        }
        else
        {
            customBannerView.setImages(banners);
        }
    }

    private OnFragmentInteractionListener mListener;

    public static MainHomeFragment newInstance(String url, String type) {
        MainHomeFragment fragment = new MainHomeFragment();
        Bundle args = new Bundle();
        args.putString(URL, url);
        args.putString(TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    public MainHomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_home, container, false);
            mContext = getActivity();
            initView();
            initBroadcastReceiver();

        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
        return view;
    }

    private void initView() {
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");

        loadingView = (LinearLayout) view.findViewById(R.id.loadingView);
        nullNetView = (LinearLayout) view.findViewById(R.id.nullNetline);
        nullView = (LinearLayout) view.findViewById(R.id.nullline);
        reloadBtn = (TextView) view.findViewById(R.id.nullReload);
        reloadBtn.setOnClickListener(this);
        reloadNetBtn = (TextView) view.findViewById(R.id.netReload);
        reloadNetBtn.setOnClickListener(this);

        listView = (ListViewForScrollView) view.findViewById(R.id.homeItemList);
        listView.setOnItemClickListener(this);
        adapterList = new ShopSpecialAdapter(mContext, items);
        listView.setAdapter(adapterList);

        mScrollView = (PullToRefreshScrollView) view.findViewById(R.id.sv_home_scrollview);
        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mScrollView.setOnRefreshListener(this);

        view.findViewById(R.id.ll_lottery).setOnClickListener(this);
        view.findViewById(R.id.ll_special_shop).setOnClickListener(this);
        view.findViewById(R.id.ll_sale).setOnClickListener(this);
        view.findViewById(R.id.ll_forward).setOnClickListener(this);

        mSearchView = (ImageView) view.findViewById(R.id.iv_search);
        mSearchView.setOnClickListener(this);
        mCountTv = (TextView) view.findViewById(R.id.tv_cart_count);
        mCartIv=(FrameLayout)view.findViewById(R.id.tv_shopping_cart);
        mCartIv.setOnClickListener(this);

        currentPage = 1;

        mRequestQueue = Volley.newRequestQueue(getActivity());

        linearLayout=(LinearLayout)view.findViewById(R.id.bannerView);
        initDate();
        initCartTips();
    }

    /**
     * 注册广播
     */
    private void initBroadcastReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ZhaiDou.IntentRefreshCartGoodsTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginExitTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginTag);
        mContext.registerReceiver(broadcastReceiver, intentFilter);
    }

    private void initDate() {
        if (NetworkUtils.isNetworkAvailable(mContext)) {
            getBannerData();
            FetchData(currentPage);
        } else {
            mDialog.dismiss();
            nullNetView.setVisibility(View.VISIBLE);
            nullView.setVisibility(View.GONE);
        }

    }

    /**
     * 红色标识提示显示数量
     */
    private void initCartTips()
    {
        if (((MainActivity) mContext).getNum() > 0)
        {
            mCountTv.setVisibility(View.VISIBLE);
            mCountTv.setText("" + ((MainActivity) mContext).getNum());
        } else
        {
            mCountTv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_search:
                SearchFragment searchFragment = SearchFragment.newInstance("", "");
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(searchFragment);
                break;
            case R.id.ll_lottery:
                Intent detailIntent = new Intent(getActivity(), HomeCompetitionActivity.class);
                detailIntent.putExtra("url", ZhaiDou.PRIZE_SCRAPING_URL);
                detailIntent.putExtra("from", "lottery");
                detailIntent.putExtra("title", "天天刮奖");
                getActivity().startActivity(detailIntent);
                break;

            case R.id.ll_special_shop:
                HomeStrategyFragment shopSpecialFragment = HomeStrategyFragment.newInstance("", 0);
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(shopSpecialFragment);
                break;

            case R.id.ll_sale:
                SpecialSaleFragment specialSaleFragment = SpecialSaleFragment.newInstance("", "");
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(specialSaleFragment);
                break;
            case R.id.ll_forward:
                Intent intent1 = new Intent();
                intent1.putExtra("url", ZhaiDou.FORWARD_URL);
                intent1.putExtra("from", "beauty");
                intent1.putExtra("title", "转发有喜");
                intent1.setClass(getActivity(), ItemDetailActivity.class);
                getActivity().startActivity(intent1);
                break;
            case R.id.nullReload:
                mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
                initDate();
                break;
            case R.id.netReload:
                mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
                initDate();
                break;
            case R.id.tv_shopping_cart:
                if (checkLogin())
                {
                    ShopCartFragment shopCartFragment = ShopCartFragment.newInstance("", 0);
                    ((MainActivity) getActivity()).navigationToFragmentWithAnim(shopCartFragment);
                }
                else
                {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    intent.setFlags(1);
                    mContext.startActivity(intent);
                }
                break;

        }
    }

    /**
     * 加载列表数据
     */
    private void FetchData(final int page)
    {
        final String url;
        url = ZhaiDou.shopSpecialListUrl + "&page=" + page;
        JsonObjectRequest jr = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                if (response == null)
                {
                    if (mDialog != null)
                        mDialog.dismiss();
                    mScrollView.onRefreshComplete();
                    mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
                    nullView.setVisibility(View.VISIBLE);
                    nullNetView.setVisibility(View.GONE);
                    return;
                }
                JSONArray jsonArray = response.optJSONArray("sales");

                if (jsonArray != null)
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject obj = jsonArray.optJSONObject(i);
                        int id = obj.optInt("id");
                        String title = obj.optString("title");
                        String sales = obj.optString("tags");
                        String time = obj.optString("day");
                        String startTime = obj.optString("start_time");
                        String endTime = obj.optString("end_time");
                        String overTime = obj.optString("over_day");
                        String imageUrl = obj.optString("banner");

                        ShopSpecialItem shopSpecialItem = new ShopSpecialItem(id, title, sales, time, startTime, endTime, overTime, imageUrl);
                        items.add(shopSpecialItem);
                    }
                Message message = new Message();
                message.what = 1001;
                handler.sendMessage(message);
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                mScrollView.onRefreshComplete();
                mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
                if (items.size()!=0)
                {
                    currentPage--;
                    ToolUtils.setToast(mContext,R.string.loading_fail_txt);
                }
                else
                {
                    nullView.setVisibility(View.VISIBLE);
                    nullNetView.setVisibility(View.GONE);
                    mScrollView.onRefreshComplete();
                    mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
                }
            }
        })        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        mRequestQueue.add(jr);
    }




    /**
     * 获得广告数据
     */
    private void getBannerData() {
        String url = ZhaiDou.BannerUrl + 2;
        banners.removeAll(banners);
        JsonObjectRequest bannerRequest = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                JSONArray jsonArray = jsonObject.optJSONArray("sale_banners");
                if (jsonArray != null && jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.optJSONObject(i);
                        int id = obj.optInt("id");
                        int type = obj.optInt("r_type");
                        String typeValue = obj.optString("r_value");
                        String imageUrl = obj.optString("imgs");
                        String title = obj.optString("title");
                        SwitchImage switchImage = new SwitchImage();
                        switchImage.id = id;
                        switchImage.type = type;
                        switchImage.typeValue = typeValue;
                        switchImage.imageUrl = imageUrl;
                        switchImage.title = title;
                        banners.add(switchImage);
                    }
                    Message message = new Message();
                    message.what = UPDATE_BANNER;
                    message.obj = banners;
                    handler.sendMessage(message);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers=new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        mRequestQueue.add(bannerRequest);
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        ShopTodaySpecialFragment shopTodaySpecialFragment = ShopTodaySpecialFragment.newInstance(items.get(position).title, items.get(position).id, items.get(position).imageUrl);
        ((MainActivity) getActivity()).navigationToFragmentWithAnim(shopTodaySpecialFragment);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
        String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
        refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
        getBannerData();
        items.clear();
        FetchData(currentPage = 1);
        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
        if (count != -1 && items.size() == currentPage*10) {
            Toast.makeText(getActivity(), "已经加载完毕", Toast.LENGTH_SHORT).show();
            mScrollView.onRefreshComplete();
            mScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            return;
        }
        FetchData(++currentPage);
    }


    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_home));
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_home));
    }

    @Override
    public void onDestroy()
    {
        mContext.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}
