package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
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
import com.zhaidou.model.CountTime;
import com.zhaidou.model.Product;
import com.zhaidou.model.SwitchImage;
import com.zhaidou.model.User;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomBannerView;
import com.zhaidou.view.TimerTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpecialSaleFragment extends BaseFragment implements View.OnClickListener, RegisterFragment.RegisterOrLoginListener
{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;


    private GridView mGridView;
    private TimerTextView mTimerView;
    private ProductAdapter mAdapter;
    private PullToRefreshScrollView mScrollView;
    private Map<Integer, View> mHashMap = new HashMap<Integer, View>();
    private RequestQueue requestQueue;
    private List<Product> products = new ArrayList<Product>();

    private LinearLayout loadingView, nullNetView, nullView;
    private TextView reloadBtn, reloadNetBtn;

    private final int UPDATE_ADAPTER = 0;
    private final int UPDATE_TIMER_START = 1;
    private final int UPDATE_BANNER = 2;
    private final int UPDATE_CARTCAR_DATA = 3;

    private Dialog mDialog;

    private TextView cartTipsTv;
    private ImageView myCartBtn;

    private View rootView;
    private long end_date;
    private long time;
    private long systemTime;
    private boolean isFrist;
    private Context mContext;

    private List<SwitchImage> banners;
    private CustomBannerView customBannerView;
    private LinearLayout bannerLine;
    private int page = 1;
    private int pageTotal;
    private int pageSize;
    private int cartCount;//购物车商品数量
    private boolean isFristCount = true;
    private String token;
    private int userId;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(ZhaiDou.IntentRefreshCartGoodsCheckTag))
            {
                FetchCountData();
            }
            if (action.equals(ZhaiDou.IntentRefreshAddCartTag))
            {
                FetchCountData();
            }
            if (action.equals(ZhaiDou.IntentRefreshLoginTag))
            {
                FetchCountData();
            }
            if (action.equals(ZhaiDou.IntentRefreshLoginExitTag))
            {
                initCartTips();
            }
        }
    };

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case UPDATE_ADAPTER:
                    loadingView.setVisibility(View.GONE);
                    mAdapter.notifyDataSetChanged();
                    if (page * pageSize < pageTotal)
                    {
                        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
                    } else
                    {
                        mScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    }
                    if (isFristCount)
                    {
                        isFristCount = false;
                        FetchCountData();
                    }
                    break;
                case UPDATE_TIMER_START:
                    time = end_date - System.currentTimeMillis();
                    if (time > 0)
                    {
                        ToolUtils.setLog("开始：" + time);
                        mTimerView.setTimes(time);
                        if (!mTimerView.isRun())
                        {
                            mTimerView.start();
                        }
                    } else
                    {
                        mTimerView.setText("已结束");
                    }
                    break;
                case UPDATE_BANNER:
                    loadingView.setVisibility(View.GONE);
                    setAdView();
                    break;
                case UPDATE_CARTCAR_DATA:
                    initCartTips();
                    break;
            }
        }
    };

    private PullToRefreshBase.OnRefreshListener2 onRefreshListener2 = new PullToRefreshBase.OnRefreshListener2()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView)
        {
            page = 1;
            products.clear();
            banners.clear();
            getBannerData();
            FetchData();
            FetchCountData();
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {
            page++;
            FetchData();
            getBannerData();
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            switch (view.getId())
            {
                case R.id.nullReload:
                    initData();
                    break;
                case R.id.netReload:
                    initData();
                    break;
            }
        }
    };


    public static SpecialSaleFragment newInstance(String param1, String param2)
    {
        SpecialSaleFragment fragment = new SpecialSaleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public SpecialSaleFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        if (rootView == null)
        {
            mContext = getActivity();
            initBroadcastReceiver();

            rootView = inflater.inflate(R.layout.fragment_special_sale, container, false);

            mScrollView = (PullToRefreshScrollView) rootView.findViewById(R.id.scrollView);
            mScrollView.setOnRefreshListener(onRefreshListener2);
            mScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

            loadingView = (LinearLayout) rootView.findViewById(R.id.loadingView);
            bannerLine = (LinearLayout) rootView.findViewById(R.id.bannerView);
            bannerLine.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenWidth * 400 / 750));
            mGridView = (GridView) rootView.findViewById(R.id.gv_sale);
            mGridView.setEmptyView(mEmptyView);
            mTimerView = (TimerTextView) rootView.findViewById(R.id.tv_count_time);

            mAdapter = new ProductAdapter(getActivity(), products);
            mGridView.setAdapter(mAdapter);
            rootView.findViewById(R.id.ll_back).setOnClickListener(this);

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
            cartTipsTv = (TextView) rootView.findViewById(R.id.myCartTipsTv);

            initData();

            mAdapter.setOnInViewClickListener(R.id.ll_single_layout, new BaseListAdapter.onInternalClickListener()
            {
                @Override
                public void OnClickListener(View parentV, View v, Integer position, Object values)
                {
                    GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance(products.get(position).getTitle(), products.get(position).goodsId);
                    Bundle bundle = new Bundle();
                    bundle.putInt("flags", 1);
                    bundle.putString("index", products.get(position).goodsId);
                    bundle.putString("page", products.get(position).getTitle());
                    goodsDetailsFragment.setArguments(bundle);
                    ((MainActivity) getActivity()).navigationToFragmentWithAnim(goodsDetailsFragment);
                }
            });
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null)
        {
            parent.removeView(rootView);
        }
        return rootView;
    }

    /**
     * 注册广播
     */
    private void initBroadcastReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ZhaiDou.IntentRefreshCartGoodsCheckTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshAddCartTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginExitTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginTag);
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }

    public boolean checkLogin()
    {
        token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        userId = (Integer) SharedPreferencesUtil.getData(getActivity(), "userId", -1);
        boolean isLogin = !TextUtils.isEmpty(token) && userId > -1;
        return isLogin;
    }

    /**
     * 初始化收据
     */
    private void initData()
    {
        mDialog = CustomLoadingDialog.setLoadingDialog(getActivity(), "loading", true);
        if (NetworkUtils.isNetworkAvailable(getActivity()))
        {
            getBannerData();
            FetchData();
        } else
        {
            if (mDialog != null)
                mDialog.dismiss();
            nullView.setVisibility(View.GONE);
            nullNetView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 广告轮播设置
     */
    private void setAdView()
    {

        if (customBannerView == null)
        {
            customBannerView = new CustomBannerView(mContext, banners, true);
            customBannerView.setLayoutParams(screenWidth, screenWidth * 400 / 750);
            customBannerView.setOnBannerClickListener(new CustomBannerView.OnBannerClickListener()
            {
                @Override
                public void onClick(int postion)
                {
                    SwitchImage item = banners.get(postion);
                    ToolUtils.setBannerGoto(item, mContext);
                }
            });
            bannerLine.addView(customBannerView);
        } else
        {
            customBannerView.setImages(banners);
        }
    }

    /**
     * 红色标识提示显示数量
     */
    private void initCartTips()
    {
        if (checkLogin())
        {
            if (cartCount > 0)
            {
                cartTipsTv.setVisibility(View.VISIBLE);
                cartTipsTv.setText("" + cartCount);
            } else
            {
                cartTipsTv.setVisibility(View.GONE);
            }
        } else
        {
            cartTipsTv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.ll_back:
                ((MainActivity) getActivity()).popToStack(SpecialSaleFragment.this);
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
        }
    }

    public void FetchData()
    {
        String url=ZhaiDou.HomeGoodsListUrl+mParam2+"&pageNo="+ page +"&typeEnum="+2;
        JsonObjectRequest request = new JsonObjectRequest(url,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject jsonObject)
                    {
                        mDialog.dismiss();
                        mScrollView.onRefreshComplete();
                        ToolUtils.setLog(jsonObject.toString());
                        JSONObject object = jsonObject.optJSONObject("data");
                        if (object == null)
                        {
                            nullNetView.setVisibility(View.GONE);
                            mTimerView.setText("已结束");
                            ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                            return;
                        }
                        JSONObject totalObject = object.optJSONObject("activityPO");
                        if (totalObject != null)
                        {
                            end_date = totalObject.optLong("endTime");
                            String description = totalObject.optString("description");
                            mHandler.obtainMessage(UPDATE_TIMER_START, end_date).sendToTarget();
                        } else
                        {
                            mHandler.sendEmptyMessage(UPDATE_ADAPTER);
                        }
                        JSONObject itemObject = object.optJSONObject("pagePO");
                        pageSize = totalObject.optInt("pageSize");
                        pageTotal = totalObject.optInt("totalCount");
                        JSONArray items = itemObject.optJSONArray("items");
                        if (items != null && items.length() > 0)
                        {
                            for (int i = 0; i < items.length(); i++)
                            {
                                JSONObject item = items.optJSONObject(i);
                                int id = item.optInt("id");
                                String goodsId = item.optString("productId");
                                String title = item.optString("productName");
                                double price = item.optDouble("price");
                                double cost_price = item.optDouble("marketPrice");
                                String image = item.optString("productPicUrl");
                                JSONObject countObject = item.optJSONObject("expandedResponse");
                                int remaining = countObject.optInt("stock");
                                Product product = new Product();
                                product.goodsId=goodsId;
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

                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                mDialog.dismiss();
                mScrollView.onRefreshComplete();
                if (page > 1)
                {
                    page--;
                } else
                {
                    isFristCount=true;
                    nullView.setVisibility(View.VISIBLE);
                    nullNetView.setVisibility(View.GONE);
                }
            }
        }
        )
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        requestQueue.add(request);
    }

    /**
     * 获得广告数据
     */
    private void getBannerData()
    {
        String url = ZhaiDou.HomeBannerUrl + "04";
        ToolUtils.setLog(url);
        banners = new ArrayList<SwitchImage>();
        JsonObjectRequest bannerRequest = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                if (jsonObject != null)
                {
                    ToolUtils.setLog(jsonObject.toString());
                    JSONArray jsonArray = jsonObject.optJSONArray("data");
                    if (jsonArray != null)
                    {
                        for (int i = 0; i < jsonArray.length(); i++)
                        {
                            JSONObject jsonObj = jsonArray.optJSONObject(i);
                            JSONArray array = jsonObj.optJSONArray("programPOList");
                            if (array != null)
                                for (int j = 0; j < array.length(); j++)
                                {
                                    JSONObject obj = array.optJSONObject(j);
                                    int type = obj.optInt("type");
                                    String typeValue = obj.optString("code");
                                    String imageUrl = obj.optString("pictureUrl");
                                    String title = obj.optString("name");
                                    if (type == 1)
                                    {
                                        typeValue = obj.optString("url");
                                    }
                                    SwitchImage switchImage = new SwitchImage();
                                    switchImage.id = j;
                                    switchImage.type = type;
                                    switchImage.typeValue = typeValue;
                                    switchImage.imageUrl = imageUrl;
                                    switchImage.title = title;
                                    banners.add(switchImage);
                                }
                        }
                    }
                    Message message = new Message();
                    message.what = UPDATE_BANNER;
                    message.obj = banners;
                    mHandler.sendMessage(message);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        requestQueue.add(bannerRequest);
    }

    /**
     * 请求购物车列表数据
     */
    public void FetchCountData()
    {
        String url = ZhaiDou.CartGoodsCountUrl+userId;
        ToolUtils.setLog("url:" + url);
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                if (jsonObject != null)
                {
                    JSONObject object = jsonObject.optJSONObject("data");
                    cartCount = object.optInt("totalQuantity");
                    mHandler.sendEmptyMessage(UPDATE_CARTCAR_DATA);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                if (mDialog != null)
                    mDialog.dismiss();
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                headers.put("SECAuthorization", token);
                return headers;
            }
        };
        requestQueue.add(request);
    }


    public class ProductAdapter extends BaseListAdapter<Product>
    {
        public ProductAdapter(Context context, List<Product> list)
        {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent)
        {
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
            ll_sale_out.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth / 2 - 1, screenWidth / 2 - 1));
            Product product = getList().get(position);
            tv_name.setText(product.getTitle());
            ToolUtils.setImageCacheUrl(product.getImage(), image, R.drawable.icon_loading_defalut);
            tv_price.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            tv_price.setText("￥" + ToolUtils.isIntPrice("" + product.getCost_price()));
            tv_count.setText("剩余 " + product.getRemaining() + "%");

            ll_sale_out.setVisibility(product.getRemaining() == 0 ? View.VISIBLE : View.GONE);
            mHashMap.put(position, convertView);
            return convertView;
        }
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }

    @Override
    public void onRegisterOrLoginSuccess(User user, Fragment fragment)
    {
        SharedPreferencesUtil.saveUser(getActivity(), user);
        getActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onResume()
    {
        if(isFrist)
        {
            long temp=Math.abs(systemTime-System.currentTimeMillis());
            time =mTimerView.getTimes()-temp;
            mTimerView.setTimes(time);
        }
        super.onResume();
    }

    @Override
    public void onPause()
    {
        systemTime=System.currentTimeMillis();
        isFrist=true;
        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        getActivity().unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}
