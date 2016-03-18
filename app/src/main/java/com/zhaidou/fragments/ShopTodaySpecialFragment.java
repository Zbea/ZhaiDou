package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.zhaidou.activities.LoginActivity;
import com.zhaidou.adapter.ShopTodaySpecialAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.ShopSpecialItem;
import com.zhaidou.model.ShopTodayItem;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.ListViewForScrollView;
import com.zhaidou.view.TimerTextView;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;


/**
 * Created by roy on 15/7/23.
 */
public class ShopTodaySpecialFragment extends BaseFragment
{
    private static final String PAGE = "page";
    private static final String INDEX = "index";
    private static final String IMAGEURL = "image";

    private String shareUrl = ZhaiDou.shopSpecialListShareUrl;
    private String mString;
    private String mImageUrl;
    private String mIndex;
    private View mView;
    private Context mContext;
    private Dialog mDialog;
    private String mTitle;
    private String introduce;//引文介绍
    private final int UPDATE_CONTENT = 0;
    private final int UPDATE_TIMER_START_AND_DETAIL_DATA = 1;
    private final int UPDATE_CARTCAR_DATA = 2;

    private int page = 1;
    private int pageSize;
    private int pageCount;
    private PullToRefreshScrollView mScrollView;

    private long initTime;
    private long systemTime;
    private boolean isFrist;

    private RequestQueue mRequestQueue;

    private ImageView shareBtn;
    private TypeFaceTextView backBtn, titleTv, introduceTv;
    private ListViewForScrollView mListView;
    private LinearLayout loadingView, nullNetView, nullView;
    private TextView reloadBtn, reloadNetBtn;
    private TimerTextView timeTvs;

    private TextView myCartTips;
    private ImageView myCartBtn;

    private List<ShopTodayItem> items = new ArrayList<ShopTodayItem>();
    private ShopTodaySpecialAdapter adapter;
    private ShopSpecialItem shopSpecialItem;
    private int cartCount;//购物车商品数量
    private int userId;
    private String token;
    private boolean isChlick;//防止重复点击

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
                checkLogin();
                FetchCountData();
            }
            if (action.equals(ZhaiDou.IntentRefreshLoginExitTag))
            {
                initCartTips();
            }
        }
    };


    private Handler handler = new Handler()
    {
        public void handleMessage(final Message msg)
        {
            switch (msg.what)
            {
                case UPDATE_CONTENT:
                    introduceTv.setText(introduce);
                    titleTv.setText(shopSpecialItem.title);
                    if (shopSpecialItem != null)
                        initTime = shopSpecialItem.endTime - System.currentTimeMillis();
                    if (initTime > 0)
                    {
                        timeTvs.setTimes(initTime);
                        if (!timeTvs.isRun())
                        {
                            timeTvs.start();
                        }
                    } else
                    {
                        timeTvs.setText("已结束");
                    }
                    break;
                case UPDATE_TIMER_START_AND_DETAIL_DATA:
                    loadingView.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    if (pageCount > pageSize * page)
                    {
                        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
                    } else
                    {
                        mScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    }
                    break;
                case UPDATE_CARTCAR_DATA:
                    initCartTips();
                    if ((((MainActivity) mContext).cart_dot).getVisibility() == View.GONE)
                    {
                        ((MainActivity) mContext).CartTip(cartCount);
                    }
                    break;
            }
        }
    };

    private PullToRefreshBase.OnRefreshListener2 onRefreshListener = new PullToRefreshBase.OnRefreshListener2()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView)
        {
            items.clear();
            page = 1;
            FetchData();
            if (checkLogin())
            {
                FetchCountData();
            }
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {
            page++;
            FetchData();
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
                    ((MainActivity) getActivity()).popToStack(ShopTodaySpecialFragment.this);
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
                case R.id.share_iv:
                    share();
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


    public static ShopTodaySpecialFragment newInstance(String page, String index, String imageUrl)
    {
        ShopTodaySpecialFragment fragment = new ShopTodaySpecialFragment();
        Bundle args = new Bundle();
        args.putString(PAGE, page);
        args.putString(INDEX, index);
        args.putString(IMAGEURL, imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    public ShopTodaySpecialFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mString = getArguments().getString(PAGE);
            mIndex = getArguments().getString(INDEX);
            mImageUrl = getArguments().getString(IMAGEURL);
            mTitle = mString;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        ToolUtils.setLog("111111");
        if (mView == null)
        {
            mView = inflater.inflate(R.layout.shop_today_special_page, container, false);
            mContext = getActivity();
            initBroadcastReceiver();
            initView();
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
        intentFilter.addAction(ZhaiDou.IntentRefreshCartGoodsCheckTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshAddCartTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginExitTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginTag);
        mContext.registerReceiver(broadcastReceiver, intentFilter);
    }


    /**
     * 初始化数据
     */
    private void initView()
    {
        ToolUtils.setLog("22222");
        shareUrl = shareUrl + mIndex;

        loadingView = (LinearLayout) mView.findViewById(R.id.loadingView);
        nullNetView = (LinearLayout) mView.findViewById(R.id.nullNetline);
        nullView = (LinearLayout) mView.findViewById(R.id.nullline);
        reloadBtn = (TextView) mView.findViewById(R.id.nullReload);
        reloadBtn.setOnClickListener(onClickListener);
        reloadNetBtn = (TextView) mView.findViewById(R.id.netReload);
        reloadNetBtn.setOnClickListener(onClickListener);

        shareBtn = (ImageView) mView.findViewById(R.id.share_iv);
        shareBtn.setOnClickListener(onClickListener);

        backBtn = (TypeFaceTextView) mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);
        titleTv = (TypeFaceTextView) mView.findViewById(R.id.title_tv);
        timeTvs = (TimerTextView) mView.findViewById(R.id.shopTime1Tv);
        introduceTv = (TypeFaceTextView) mView.findViewById(R.id.adText);
        myCartTips = (TextView) mView.findViewById(R.id.myCartTipsTv);
        myCartBtn = (ImageView) mView.findViewById(R.id.myCartBtn);
        myCartBtn.setOnClickListener(onClickListener);



        mListView = (ListViewForScrollView) mView.findViewById(R.id.shopListView);
        adapter = new ShopTodaySpecialAdapter(mContext, items);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (!isChlick)
                {
                    isChlick = true;
                    mDialog.show();
                    GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance(items.get(position).title, items.get(position).goodsId);
                    ((MainActivity) getActivity()).navigationToFragmentWithAnim(goodsDetailsFragment);
                    mDialog.dismiss();
                }
            }
        });

        mScrollView = (PullToRefreshScrollView) mView.findViewById(R.id.scrollView);
        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mScrollView.setOnRefreshListener(onRefreshListener);

        mRequestQueue = Volley.newRequestQueue(mContext);

        initData();


    }

    public boolean checkLogin()
    {
        token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        userId = (Integer) SharedPreferencesUtil.getData(getActivity(), "userId", -1);
        boolean isLogin = !TextUtils.isEmpty(token) && userId > -1;
        return isLogin;
    }

    /**
     * 初始化数据
     */
    private void initData()
    {
        if (NetworkUtils.isNetworkAvailable(mContext))
        {
            mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading", isDialogFirstVisible);
            isDialogFirstVisible = false;
            FetchData();
            if (checkLogin())
            {
                FetchCountData();
            }
        } else
        {
            if (mDialog != null)
                mDialog.dismiss();
            nullView.setVisibility(View.GONE);
            nullNetView.setVisibility(View.VISIBLE);
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
                myCartTips.setVisibility(View.VISIBLE);
                myCartTips.setText("" + cartCount);
            } else
            {
                myCartTips.setVisibility(View.GONE);
            }
        } else
        {
            cartCount = 0;
            myCartTips.setVisibility(View.GONE);
        }
    }


    /**
     * 分享
     */
    private void share()
    {
        DialogUtils mDialogUtils = new DialogUtils(mContext);
        mDialogUtils.showShareDialog(mTitle, mTitle + "  " + shareUrl, mImageUrl, shareUrl, new PlatformActionListener()
        {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> stringObjectHashMap)
            {
                Toast.makeText(mContext, mContext.getString(R.string.share_completed), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable)
            {
                Toast.makeText(mContext, mContext.getString(R.string.share_error), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel(Platform platform, int i)
            {
                Toast.makeText(mContext, mContext.getString(R.string.share_cancel), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 加载列表数据
     */
    private void FetchData()
    {
        String url = ZhaiDou.HomeGoodsListUrl + mIndex + "&pageNo=" + page + "&typeEnum=" + 1;
        ToolUtils.setLog(url);
        JsonObjectRequest jr = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                mScrollView.onRefreshComplete();
                if (response == null)
                {
                    if (page == 1)
                    {
                        nullView.setVisibility(View.VISIBLE);
                        nullNetView.setVisibility(View.GONE);
                    } else
                    {
                        ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                    }
                    return;
                }
                JSONObject obj;
                JSONObject jsonObject1 = response.optJSONObject("data");
                if (jsonObject1 != null)
                {
                    JSONObject jsonObject = jsonObject1.optJSONObject("activityPO");
                    String id = jsonObject.optString("activityCode");
                    String title = jsonObject.optString("activityName");
                    long startTime = jsonObject.optLong("startTime");
                    long endTime = jsonObject.optLong("endTime");
                    ToolUtils.setLog("" + endTime);
                    int overTime = Integer.parseInt((String.valueOf((endTime - startTime) / (24 * 60 * 60 * 1000))));
                    introduce = jsonObject.optString("description");
                    int isNew = jsonObject.optInt("newFlag");
                    shopSpecialItem = new ShopSpecialItem(id, title, null, startTime, endTime, overTime, null, isNew);
                    handler.sendEmptyMessage(UPDATE_CONTENT);
                    JSONObject jsonObject2 = jsonObject1.optJSONObject("pagePO");
                    if (jsonObject2 != null)
                    {
                        JSONArray jsonArray = jsonObject2.optJSONArray("items");
                        pageCount = jsonObject2.optInt("totalCount");
                        pageSize = jsonObject2.optInt("pageSize");
                        if (jsonArray != null)

                            for (int i = 0; i < jsonArray.length(); i++)
                            {
                                obj = jsonArray.optJSONObject(i);
                                String Baseid = obj.optString("productId");
                                String Listtitle = obj.optString("productName");
                                double price = obj.optDouble("price");
                                double cost_price = obj.optDouble("marketPrice");
                                String imageUrl = obj.optString("productPicUrl");
                                String comment = obj.optString("comment") == "null" ? "" : obj.optString("comment");
                                JSONObject jsonObject3 = obj.optJSONObject("expandedResponse");

                                int num = jsonObject3.optInt("stock");
                                int totalCount = 100;
                                int percentum = obj.optInt("progressPercentage");
                                ShopTodayItem shopTodayItem = new ShopTodayItem(Baseid, Listtitle, imageUrl, price, cost_price, num, totalCount);
                                shopTodayItem.percentum = percentum;
                                shopTodayItem.comment = comment;
                                items.add(shopTodayItem);
                            }
                    }

                }
                Message message = new Message();
                message.what = UPDATE_TIMER_START_AND_DETAIL_DATA;
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
                if (page > 1)
                {
                    page--;
                    ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                } else
                {
                    nullView.setVisibility(View.VISIBLE);
                    nullNetView.setVisibility(View.GONE);
                }
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
        mRequestQueue.add(jr);
    }

    /**
     * 请求购物车列表数据
     */
    public void FetchCountData()
    {
        String url = ZhaiDou.CartGoodsCountUrl + userId;
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
                    handler.sendEmptyMessage(UPDATE_CARTCAR_DATA);
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
                headers.put("SECAuthorization", token);
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        mRequestQueue.add(request);
    }


    @Override
    public void onResume()
    {
        if (isFrist)
        {
            long temp = Math.abs(systemTime - System.currentTimeMillis());
            initTime = timeTvs.getTimes() - temp;
            timeTvs.setTimes(initTime);
        }
        super.onResume();
        MobclickAgent.onPageStart(mTitle);
    }

    @Override
    public void onPause()
    {
        isChlick = false;
        systemTime = System.currentTimeMillis();
        isFrist = true;
        super.onPause();
        MobclickAgent.onPageEnd(mTitle);
    }

    @Override
    public void onDestroy()
    {
        if (broadcastReceiver != null)
            mContext.unregisterReceiver(broadcastReceiver);
        timeTvs.stop();
        mRequestQueue.stop();
        super.onDestroy();
    }

}
