package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshScrollView;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.activities.LoginActivity;
import com.zhaidou.activities.WebViewActivity;
import com.zhaidou.adapter.AdViewAdpater;
import com.zhaidou.adapter.GoodsImageAdapter;
import com.zhaidou.adapter.ShopSpecialAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.CartItem;
import com.zhaidou.model.Category;
import com.zhaidou.model.ShopSpecialItem;
import com.zhaidou.model.SwitchImage;
import com.zhaidou.sqlite.CreatCartDB;
import com.zhaidou.sqlite.CreatCartTools;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomBannerView;
import com.zhaidou.view.ListViewForScrollView;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by roy on 15/7/20.
 */
public class ShopSpecialFragment extends BaseFragment
{
    private static final String PAGE = "page";
    private static final String INDEX = "index";

    private String mPage;
    private int mIndex;
    private View mView;
    private Context mContext;
    private int page = 1;
    private Dialog mDialog;
    private LinearLayout loadingView, nullNetView, nullView;
    private TextView reloadBtn, reloadNetBtn;

    private RequestQueue mRequestQueue;

    private final static int UPDATE_BANNER = 1003;

    private TypeFaceTextView backBtn, titleTv;
    private TextView cartTipsTv;
    private ImageView myCartBtn;
    private PullToRefreshScrollView mScrollView;
    private ListViewForScrollView mListView;
    private List<ShopSpecialItem> items = new ArrayList<ShopSpecialItem>();
    private ShopSpecialAdapter adapterList;


    private List<SwitchImage> banners= new ArrayList<SwitchImage>();
    private CustomBannerView customBannerView;
    private LinearLayout bannerLine;
    boolean isStop = true;
    private final int UPDATE_CARTCAR_DATA = 4;

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


    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 1001:
                    mScrollView.onRefreshComplete();
                    adapterList.notifyDataSetChanged();
                    if (mDialog != null)
                        mDialog.dismiss();
                    loadingView.setVisibility(View.GONE);
                    break;
                case UPDATE_BANNER:
                    setAdView();
                    break;
                case UPDATE_CARTCAR_DATA:
                    int num = msg.arg2;
                    cartTipsTv.setVisibility(num > 0 ? View.VISIBLE : View.GONE);
                    cartTipsTv.setText("" + num);
                    break;
            }
        }
    };

    /**
     * 下拉刷新
     */
    private PullToRefreshBase.OnRefreshListener2 refreshListener = new PullToRefreshBase.OnRefreshListener2()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView)
        {
            page = 1;
            items.clear();
            mScrollView.onRefreshComplete();
            getBannerData();
            FetchData(page);
            adapterList.notifyDataSetChanged();
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {
            page++;
            mScrollView.onRefreshComplete();
            FetchData(page);
            adapterList.notifyDataSetChanged();
        }
    };

    /**
     * adapter短点击事件
     */
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
        {
            ShopTodaySpecialFragment shopTodaySpecialFragment = ShopTodaySpecialFragment.newInstance(items.get(i).title, items.get(i).id, items.get(i).imageUrl);
            ((MainActivity) getActivity()).navigationToFragmentWithAnim(shopTodaySpecialFragment);
        }
    };

    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            switch (view.getId())
            {
                case R.id.back_btn:
                    ((MainActivity) getActivity()).popToStack(ShopSpecialFragment.this);
                    break;
                case R.id.myCartBtn:
                    if (checkLogin())
                    {
                        ShopCartFragment shopCartFragment = ShopCartFragment.newInstance("", 0);
                        ((MainActivity) getActivity()).navigationToFragment(shopCartFragment);
                    } else
                    {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(1);
                        getActivity().startActivity(intent);
                    }
                    break;

                case R.id.nullReload:
                    initData();
                    break;
                case R.id.netReload:
                    initData();
                    break;
            }
        }
    };

    public static ShopSpecialFragment newInstance(String page, int index)
    {
        ShopSpecialFragment fragment = new ShopSpecialFragment();
        Bundle args = new Bundle();
        args.putString(PAGE, page);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    public ShopSpecialFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mPage = getArguments().getString(PAGE);
            mIndex = getArguments().getInt(INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        mContext = getActivity();
        initBroadcastReceiver();
        if (mView == null)
        {
            mView = inflater.inflate(R.layout.shop_special_page, container, false);
            initView();
            initData();
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) mView.getParent();
        if (parent != null)
        {
            parent.removeView(mView);
        }
        return mView;
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

    /**
     * 初始化数据
     */
    private void initView()
    {
        loadingView = (LinearLayout) mView.findViewById(R.id.loadingView);
        nullNetView = (LinearLayout) mView.findViewById(R.id.nullNetline);
        nullView = (LinearLayout) mView.findViewById(R.id.nullline);
        reloadBtn = (TextView) mView.findViewById(R.id.nullReload);
        reloadBtn.setOnClickListener(onClickListener);
        reloadNetBtn = (TextView) mView.findViewById(R.id.netReload);
        reloadNetBtn.setOnClickListener(onClickListener);

        bannerLine=(LinearLayout) mView.findViewById(R.id.bannerView);

        backBtn = (TypeFaceTextView) mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);
        titleTv = (TypeFaceTextView) mView.findViewById(R.id.title_tv);
        titleTv.setText(R.string.home_shop_special_text);

        mScrollView = (PullToRefreshScrollView) mView.findViewById(R.id.sv_special_scrollview);
        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mScrollView.setOnRefreshListener(refreshListener);

        mListView = (ListViewForScrollView) mView.findViewById(R.id.shopListView);
        adapterList = new ShopSpecialAdapter(mContext, items);
        mListView.setAdapter(adapterList);
        mListView.setOnItemClickListener(onItemClickListener);

        myCartBtn = (ImageView) mView.findViewById(R.id.myCartBtn);
        myCartBtn.setOnClickListener(onClickListener);
        cartTipsTv = (TextView) mView.findViewById(R.id.myCartTipsTv);

        mRequestQueue = Volley.newRequestQueue(mContext);

    }

    public boolean checkLogin()
    {
        String token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        int id = (Integer) SharedPreferencesUtil.getData(getActivity(), "userId", -1);
        boolean isLogin = !TextUtils.isEmpty(token) && id > -1;
        return isLogin;
    }

    /**
     * 红色标识提示显示数量
     */
    private void initCartTips()
    {
        if (((MainActivity) mContext).getNum() > 0)
        {
            cartTipsTv.setVisibility(View.VISIBLE);
            cartTipsTv.setText("" + ((MainActivity) mContext).getNum());
        } else
        {
            cartTipsTv.setVisibility(View.GONE);
        }
    }

    /**
     * 广告轮播设置
     */
    private void setAdView()
    {
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
            bannerLine.addView(customBannerView);
        }
        else
        {
            customBannerView.setImages(banners);
        }
    }

    /**
     * 初始化数据
     */
    private void initData()
    {
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading", isDialogFirstVisible);
        isDialogFirstVisible = false;
        if (NetworkUtils.isNetworkAvailable(mContext))
        {
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {

                    getBannerData();
                    FetchData(page);
                }
            }, 300);
        } else
        {
            if (mDialog != null)
                mDialog.dismiss();
            nullView.setVisibility(View.GONE);
            nullNetView.setVisibility(View.VISIBLE);
        }

    }

    /**
     * 获得广告数据
     */
    private void getBannerData()
    {
        banners.removeAll(banners);
        String url = ZhaiDou.BannerUrl + 1;
        ToolUtils.setLog(url);
        JsonObjectRequest bannerRequest = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                JSONArray jsonArray = jsonObject.optJSONArray("sale_banners");
                if (jsonArray != null && jsonArray.length() > 0)
                {
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
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
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
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
        mRequestQueue.add(bannerRequest);
    }

    /**
     * 加载列表数据
     */
    private void FetchData(int currentPage)
    {
        final String url;
        url = ZhaiDou.shopSpecialListUrl + "&page=" + currentPage;
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
                nullView.setVisibility(View.VISIBLE);
                nullNetView.setVisibility(View.GONE);
                mScrollView.onRefreshComplete();
                mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
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

    public void onResume()
    {
        super.onResume();
        initCartTips();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.home_shop_special_text));
    }

    public void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.home_shop_special_text));
    }

    @Override
    public void onDestroy()
    {
        isStop = false;
        mContext.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

}
