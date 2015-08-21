package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.zhaidou.adapter.ShopTodaySpecialAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.CountTime;
import com.zhaidou.model.ShopSpecialItem;
import com.zhaidou.model.ShopTodayItem;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.ListViewForScrollView;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;


/**
 * Created by roy on 15/7/23.
 */
public class ShopTodaySpecialFragment extends BaseFragment {
    private static final String PAGE = "page";
    private static final String INDEX = "index";

    private String shareUrl = ZhaiDou.shopSpecialListShareUrl;
    private String mPage;
    private int mIndex;
    private View mView;
    private Context mContext;
    private Dialog mDialog;
    private int id;
    private String mTitle;
    private String introduce;//引文介绍
    private final int UPDATE_COUNT_DOWN_TIME = 1;
    private final int UPDATE_UI_TIMER_FINISH = 2;
    private final int UPDATE_TIMER_START = 3;

    private MyTimer mTimer;

    private RequestQueue mRequestQueue;

    private ImageView shareBtn;
    private TypeFaceTextView backBtn, titleTv, introduceTv, timeTv;
    private PullToRefreshScrollView mScrollView;
    private ListViewForScrollView mListView;
    private LinearLayout loadingView, nullNetView, nullView;
    private TextView reloadBtn, reloadNetBtn;

    private TextView myCartTips;
    private ImageView myCartBtn;

    private List<ShopTodayItem> items = new ArrayList<ShopTodayItem>();
    private ShopTodaySpecialAdapter adapter;
    private ShopSpecialItem shopSpecialItem;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ZhaiDou.IntentRefreshCartGoodsTag)) {
                initCartTips();
            }
            if (action.equals(ZhaiDou.IntentRefreshLoginTag)) {
                initCartTips();
            }
            if (action.equals(ZhaiDou.IntentRefreshLoginExitTag)) {
                initCartTips();
            }
        }
    };


    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 4:
                    loadingView.setVisibility(View.GONE);
                    introduceTv.setText(introduce);
                    adapter.notifyDataSetChanged();
                    break;
                case UPDATE_TIMER_START:
                    String date = (String) msg.obj;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        long millionSeconds = sdf.parse(date).getTime();//毫秒
                        long hour = 3600 * 1000;
                        long minute = 60 * 1000;
                        millionSeconds = millionSeconds + hour * 23 + minute * 59 + 59 * 1000;
                        long temp = millionSeconds - System.currentTimeMillis();
                        mTimer = new MyTimer(temp, 1000);
                        mTimer.start();
                    } catch (Exception e) {
                        Log.i("Exception e", e.getMessage());
                    }
                    break;
                case UPDATE_COUNT_DOWN_TIME:
                    CountTime time = (CountTime) msg.obj;
                    String timerFormat = mContext.getResources().getString(R.string.timer);
                    String hourStr = String.format("%02d", time.getHour());
                    String minStr = String.format("%02d", time.getMinute());
                    String secondStr = String.format("%02d", time.getSecond());
                    String timer = String.format(timerFormat, time.getDay(), hourStr, minStr, secondStr);
                    timeTv.setText(timer);
                    break;
                case UPDATE_UI_TIMER_FINISH:
                    timeTv.setText("已结束");
                    break;
            }
        }
    };

    /**
     * 下拉刷新
     */
    private PullToRefreshBase.OnRefreshListener2 refreshListener = new PullToRefreshBase.OnRefreshListener2() {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView) {
            mScrollView.onRefreshComplete();

            items.removeAll(items);
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
            FetchData(id);
            adapter.notifyDataSetChanged();
            loadingView.setVisibility(View.GONE);
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        }
    };

    /**
     * adapter短点击事件
     */
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance(items.get(i).title, items.get(i).id);
            ((MainActivity) getActivity()).navigationToFragment(goodsDetailsFragment);
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
                        ToolUtils.setToast(getActivity(), "抱歉，尚未登录");
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


    public static ShopTodaySpecialFragment newInstance(String page, int index) {
        ShopTodaySpecialFragment fragment = new ShopTodaySpecialFragment();
        Bundle args = new Bundle();
        args.putString(PAGE, page);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    public ShopTodaySpecialFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getString(PAGE);
            mIndex = getArguments().getInt(INDEX);
            id = mIndex;
            mTitle = mPage;
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
        intentFilter.addAction(ZhaiDou.IntentRefreshCartGoodsTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginExitTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginTag);
        mContext.registerReceiver(broadcastReceiver, intentFilter);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
        if (NetworkUtils.isNetworkAvailable(mContext)) {
            FetchData(id);
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

        mScrollView = (PullToRefreshScrollView) mView.findViewById(R.id.sv_shop_today_special_scrollview);
        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        timeTv = (TypeFaceTextView) mView.findViewById(R.id.shopTimeTv);

        mScrollView = (PullToRefreshScrollView) mView.findViewById(R.id.sv_shop_today_special_scrollview);
        mScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mScrollView.setOnRefreshListener(refreshListener);

        mListView = (ListViewForScrollView) mView.findViewById(R.id.shopListView);
        adapter = new ShopTodaySpecialAdapter(mContext, items);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(onItemClickListener);

        introduceTv = (TypeFaceTextView) mView.findViewById(R.id.adText);

        myCartTips = (TextView) mView.findViewById(R.id.myCartTipsTv);
        myCartBtn = (ImageView) mView.findViewById(R.id.myCartBtn);
        myCartBtn.setOnClickListener(onClickListener);

        mRequestQueue = Volley.newRequestQueue(mContext);

        initCartTips();


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
        if (MainActivity.num > 0) {
            myCartTips.setVisibility(View.VISIBLE);
            myCartTips.setText("" + MainActivity.num);
        } else {
            myCartTips.setVisibility(View.GONE);
        }
    }

    /**
     * 分享
     */
    private void share() {
        ShareSDK.initSDK(mContext);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(mTitle);
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl(shareUrl);
        // text是分享文本，所有平台都需要这个字段
        oks.setText(mTitle + "   " + shareUrl);
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
//        oks.setImageUrl(coverUrl);//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl(shareUrl);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
//            oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(shareUrl);

        oks.show(mContext);
    }

    /**
     * 加载列表数据
     */
    private void FetchData(int id) {
        final String url;
        url = ZhaiDou.shopSpecialTadayUrl + id;
        JsonObjectRequest jr = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (mDialog != null)
                    mDialog.dismiss();
                if (response == null) {
                    nullView.setVisibility(View.VISIBLE);
                    nullNetView.setVisibility(View.GONE);
                }
                String result = response.toString();
                JSONObject obj;
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject josnObject1 = jsonObject.optJSONObject("sale");
                    int id = josnObject1.optInt("id");
                    String title = josnObject1.optString("title");
                    String time = josnObject1.optString("day");
                    String startTime = josnObject1.optString("start_time");
                    String endTime = josnObject1.optString("end_time");
                    String overTime = josnObject1.optString("over_day");
                    introduce = josnObject1.optString("quotation");
                    shopSpecialItem = new ShopSpecialItem(id, title, null, time, startTime, endTime, overTime, null);
                    handler.obtainMessage(UPDATE_TIMER_START, endTime).sendToTarget();//开始倒计时

                    JSONArray jsonArray = josnObject1.optJSONArray("merchandises");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        obj = jsonArray.optJSONObject(i);
                        int Baseid = obj.optInt("id");
                        String Listtitle = obj.optString("title");
                        String designer = obj.optString("designer");
                        double price = obj.optDouble("price");
                        double cost_price = obj.optDouble("cost_price");
                        String imageUrl = obj.optString("img");
                        int num = obj.optInt("total_count");
                        ShopTodayItem shopTodayItem = new ShopTodayItem(Baseid, Listtitle, designer, imageUrl, price, cost_price, num);
                        items.add(shopTodayItem);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Message message = new Message();
                message.what = 4;
                handler.sendMessage(message);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (mDialog != null)
                    mDialog.dismiss();
                nullView.setVisibility(View.VISIBLE);
                nullNetView.setVisibility(View.GONE);
                mScrollView.onRefreshComplete();
            }
        });
        mRequestQueue.add(jr);
    }

    /**
     * 倒计时
     */
    private class MyTimer extends CountDownTimer {
        private MyTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            long day = 24 * 3600 * 1000;
            long hour = 3600 * 1000;
            long minute = 60 * 1000;
            //两个日期想减得到天数
            long dayCount = l / day;
            long hourCount = (l - (dayCount * day)) / hour;
            long minCount = (l - (dayCount * day) - (hour * hourCount)) / minute;
            long secondCount = (l - (dayCount * day) - (hour * hourCount) - (minCount * minute)) / 1000;
            CountTime time = new CountTime(dayCount, hourCount, minCount, secondCount);

            handler.obtainMessage(UPDATE_COUNT_DOWN_TIME, time).sendToTarget();//刷新倒计时
        }

        @Override
        public void onFinish() {
            handler.sendEmptyMessage(UPDATE_UI_TIMER_FINISH);
        }
    }

    @Override
    public void onDestroy() {
        if (broadcastReceiver != null)
            mContext.unregisterReceiver(broadcastReceiver);
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        super.onDestroy();
    }
}
