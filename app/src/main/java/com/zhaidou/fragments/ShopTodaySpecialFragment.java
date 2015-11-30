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
public class ShopTodaySpecialFragment extends BaseFragment {
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
    private final int UPDATE_TIMER_START_AND_DETAIL_DATA = 3;
    private final int UPDATE_CARTCAR_DATA=5;

    private long initTime;

    private RequestQueue mRequestQueue;

    private ImageView shareBtn;
    private TypeFaceTextView backBtn, titleTv, introduceTv, timeTv;
    private ListViewForScrollView mListView;
    private LinearLayout loadingView, nullNetView, nullView;
    private TextView reloadBtn, reloadNetBtn;
    private TimerTextView timeTvs;

    private TextView myCartTips;
    private ImageView myCartBtn;
    private long currentTime;

    private List<ShopTodayItem> items = new ArrayList<ShopTodayItem>();
    private ShopTodaySpecialAdapter adapter;
    private ShopSpecialItem shopSpecialItem;
    private boolean isFrist;

    private int page=1;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ZhaiDou.IntentRefreshCartGoodsCheckTag)) {
                initCartTips();
            }
            if (action.equals(ZhaiDou.IntentRefreshLoginTag)) {
                checkLogin();
                initCartTips();
            }
            if (action.equals(ZhaiDou.IntentRefreshLoginExitTag)) {
                checkLogin();
                initCartTips();
            }
        }
    };


    private Handler handler = new Handler() {
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case 4:
                    adapter.notifyDataSetChanged();
                    break;
                case UPDATE_TIMER_START_AND_DETAIL_DATA:
                    adapter.notifyDataSetChanged();
                    introduceTv.setText(introduce);
                    loadingView.setVisibility(View.GONE);
                    initTime =  shopSpecialItem.endTime - System.currentTimeMillis();
                    if (initTime>0)
                    {
                        timeTvs.setTimes(initTime);
                        if (!timeTvs.isRun())
                        {
                            timeTvs.start();
                        }
                    }
                    else
                    {
                       timeTvs.setText("已结束");
                    }
                    break;
                case UPDATE_CARTCAR_DATA:
                    int num=msg.arg2;
                    myCartTips.setVisibility(num>0?View.VISIBLE:View.GONE);
                    myCartTips.setText("" + num);
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
            ((MainActivity) getActivity()).navigationToFragmentWithAnim(goodsDetailsFragment);
        }
    };

    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.back_btn:
                    ((MainActivity) getActivity()).popToStack(ShopTodaySpecialFragment.this);
                    break;
                case R.id.myCartBtn:
                    if (checkLogin()) {
                        ShopCartFragment shopCartFragment = ShopCartFragment.newInstance("", 0);
                        ((MainActivity) getActivity()).navigationToFragment(shopCartFragment);
                    } else {
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


    public static ShopTodaySpecialFragment newInstance(String page, String index, String imageUrl) {
        ShopTodaySpecialFragment fragment = new ShopTodaySpecialFragment();
        Bundle args = new Bundle();
        args.putString(PAGE, page);
        args.putString(INDEX, index);
        args.putString(IMAGEURL, imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    public ShopTodaySpecialFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mString = getArguments().getString(PAGE);
            mIndex = getArguments().getString(INDEX);
            mImageUrl = getArguments().getString(IMAGEURL);
            mTitle = mString;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mContext = getActivity();
        initBroadcastReceiver();
        if (mView == null) {
            mView = inflater.inflate(R.layout.shop_today_special_page, container, false);
            initView();
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) mView.getParent();
        if (parent != null) {
            parent.removeView(mView);
        }
        return mView;
    }

    /**
     * 注册广播
     */
    private void initBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ZhaiDou.IntentRefreshCartGoodsCheckTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginExitTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginTag);
        mContext.registerReceiver(broadcastReceiver, intentFilter);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        if (NetworkUtils.isNetworkAvailable(mContext)) {
            mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading", isDialogFirstVisible);
            isDialogFirstVisible = false;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    FetchData();
                }
            }, 300);
        } else {
            if (mDialog != null)
                mDialog.dismiss();
            nullView.setVisibility(View.GONE);
            nullNetView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化数据
     */
    private void initView() {
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
        titleTv.setText(mTitle);
        timeTv = (TypeFaceTextView) mView.findViewById(R.id.shopTimeTv);
        timeTvs = (TimerTextView) mView.findViewById(R.id.shopTime1Tv);

        mListView = (ListViewForScrollView) mView.findViewById(R.id.shopListView);
        mListView.setOnItemClickListener(onItemClickListener);
        adapter = new ShopTodaySpecialAdapter(mContext, items);
        mListView.setAdapter(adapter);

        introduceTv = (TypeFaceTextView) mView.findViewById(R.id.adText);

        myCartTips = (TextView) mView.findViewById(R.id.myCartTipsTv);
        myCartBtn = (ImageView) mView.findViewById(R.id.myCartBtn);
        myCartBtn.setOnClickListener(onClickListener);

        mRequestQueue = Volley.newRequestQueue(mContext);


        initData();


    }

    public boolean checkLogin() {
        String token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        int id = (Integer) SharedPreferencesUtil.getData(getActivity(), "userId", -1);
        boolean isLogin = !TextUtils.isEmpty(token) && id > -1;
        return isLogin;
    }

    /**
     * 红色标识提示显示数量
     */
    private void initCartTips() {
        if (((MainActivity) getActivity()).getNum() > 0) {
            myCartTips.setVisibility(View.VISIBLE);
            myCartTips.setText("" + ((MainActivity) getActivity()).getNum());
        } else {
            myCartTips.setVisibility(View.GONE);
        }
    }


    /**
     * 分享
     */
    private void share() {
        DialogUtils mDialogUtils=new DialogUtils(mContext);
        mDialogUtils.showShareDialog(mTitle,mTitle+"  "+shareUrl,mImageUrl,shareUrl,new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> stringObjectHashMap) {
                Toast.makeText(mContext, mContext.getString(R.string.share_completed), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                Toast.makeText(mContext,mContext.getString(R.string.share_error),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel(Platform platform, int i) {
                Toast.makeText(mContext,mContext.getString(R.string.share_cancel),Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 加载列表数据
     */
    private void FetchData() {
        String url = ZhaiDou.HomeGoodsListUrl+mIndex+"&pageNo="+ page +"&typeEnum="+1;
        ToolUtils.setLog(url);
        JsonObjectRequest jr = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (mDialog != null)
                    mDialog.dismiss();
                if (response == null) {
                    nullView.setVisibility(View.VISIBLE);
                    nullNetView.setVisibility(View.GONE);
                    return;
                }
                JSONObject obj;
                JSONObject jsonObject1 = response.optJSONObject("data");
                JSONObject jsonObject = jsonObject1.optJSONObject("activityPO");
                String id = jsonObject.optString("activityCode");
                String title = jsonObject.optString("activityName");
                long startTime = jsonObject.optLong("startTime");
                long endTime = jsonObject.optLong("endTime");
                ToolUtils.setLog(""+endTime);
                int overTime = Integer.parseInt((String.valueOf((endTime-startTime)/(24*60*60*1000))));
                introduce = jsonObject.optString("description");
                int isNew = jsonObject.optInt("newFlag");
                shopSpecialItem = new ShopSpecialItem(id, title, null,startTime, endTime, overTime, null,isNew);

                JSONObject jsonObject2 = jsonObject1.optJSONObject("pagePO");
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

                Message message = new Message();
                message.what = UPDATE_TIMER_START_AND_DETAIL_DATA;
                handler.sendMessage(message);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (mDialog != null)
                    mDialog.dismiss();
                nullView.setVisibility(View.VISIBLE);
                nullNetView.setVisibility(View.GONE);
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


    @Override
    public void onResume()
    {
        initCartTips();
        super.onResume();
        MobclickAgent.onPageStart(mTitle);
    }

    @Override
    public void onPause() {
        isFrist=true;
        currentTime = System.currentTimeMillis();
        super.onPause();
        MobclickAgent.onPageEnd(mTitle);
    }

    @Override
    public void onDestroy() {
        if (broadcastReceiver != null)
            mContext.unregisterReceiver(broadcastReceiver);
        timeTvs.stop();
        mRequestQueue.stop();
        super.onDestroy();
    }

}
