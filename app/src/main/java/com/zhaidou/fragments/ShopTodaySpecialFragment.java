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
import android.util.Log;
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
import com.zhaidou.model.CartItem;
import com.zhaidou.model.ShopSpecialItem;
import com.zhaidou.model.ShopTodayItem;
import com.zhaidou.sqlite.CreatCartDB;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.ListViewForScrollView;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
    private String mPage;
    private String mImageUrl;
    private int mIndex;
    private View mView;
    private Context mContext;
    private Dialog mDialog;
    private int id;
    private String mTitle;
    private String introduce;//引文介绍
    private final int UPDATE_COUNT_DOWN_TIME = 1;
    private final int UPDATE_UI_TIMER_FINISH = 2;
    private final int UPDATE_TIMER_START_AND_DETAIL_DATA = 3;
    private final int UPDATE_CARTCAR_DATA=5;

    //    private MyTimer mTimer;
    private Timer mTimer;
    private boolean isTimerStart = false;
    private long initTime;

    private RequestQueue mRequestQueue;
    private Map<Integer, View> mHashMap = new HashMap<Integer, View>();

    private ImageView shareBtn;
    private TypeFaceTextView backBtn, titleTv, introduceTv, timeTv;
    private ListViewForScrollView mListView;
    private LinearLayout loadingView, nullNetView, nullView;
    private TextView reloadBtn, reloadNetBtn;

    private TextView myCartTips;
    private ImageView myCartBtn;
    private long time;
    private long currentTime;
    private int num;
    private List<CartItem> cartItems = new ArrayList<CartItem>();
    private CreatCartDB creatCartDB;

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
                    String date = (String) msg.obj;
                    ToolUtils.setLog(date);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    try {
                        long millionSeconds = sdf.parse(date).getTime();//毫秒
                        long temp = millionSeconds - System.currentTimeMillis();
                        initTime = temp;
                    } catch (Exception e) {
                        Log.i("Exception e", e.getMessage());
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
            GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance(items.get(i).title, items.get(i).id);
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


    public static ShopTodaySpecialFragment newInstance(String page, int index, String imageUrl) {
        ShopTodaySpecialFragment fragment = new ShopTodaySpecialFragment();
        Bundle args = new Bundle();
        args.putString(PAGE, page);
        args.putInt(INDEX, index);
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
            mPage = getArguments().getString(PAGE);
            mIndex = getArguments().getInt(INDEX);
            mImageUrl = getArguments().getString(IMAGEURL);
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
        intentFilter.addAction(ZhaiDou.IntentRefreshCartGoodsSubTag);
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

                    FetchData(id);
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


        mListView = (ListViewForScrollView) mView.findViewById(R.id.shopListView);
        mListView.setOnItemClickListener(onItemClickListener);
        adapter = new ShopTodaySpecialAdapter(mContext, items);
        mListView.setAdapter(adapter);

        introduceTv = (TypeFaceTextView) mView.findViewById(R.id.adText);

        myCartTips = (TextView) mView.findViewById(R.id.myCartTipsTv);
        myCartBtn = (ImageView) mView.findViewById(R.id.myCartBtn);
        myCartBtn.setOnClickListener(onClickListener);

        mRequestQueue = Volley.newRequestQueue(mContext);
        creatCartDB = new CreatCartDB(mContext);

//        initCartTips();


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
//        ShareSDK.initSDK(mContext);
//        OnekeyShare oks = new OnekeyShare();
//        //关闭sso授权
//        oks.disableSSOWhenAuthorize();
//        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
//        oks.setTitle(mTitle);
//        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
//        oks.setTitleUrl(shareUrl);
//        // text是分享文本，所有平台都需要这个字段
//        oks.setText(mTitle + "   " + shareUrl);
//        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
//        oks.setImageUrl(mImageUrl);//确保SDcard下面存在此张图片
//        // url仅在微信（包括好友和朋友圈）中使用
//        oks.setUrl(shareUrl);
//        // site是分享此内容的网站名称，仅在QQ空间使用
//        oks.setSite(getString(R.string.app_name));
//        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
//        oks.setSiteUrl(shareUrl);
//
//        oks.show(mContext);

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
    private void FetchData(int id) {
        String url = ZhaiDou.shopSpecialTadayUrl + id;
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
                JSONObject jsonObject = response.optJSONObject("sale");
                int id = jsonObject.optInt("id");
                String title = jsonObject.optString("title");
                String time = jsonObject.optString("day");
                String startTime = jsonObject.optString("start_time");
                String endTime = jsonObject.optString("end_time");
                String overTime = jsonObject.optString("over_day");
                introduce = jsonObject.optString("quotation");
                shopSpecialItem = new ShopSpecialItem(id, title, null, time, startTime, endTime, overTime, null);

                JSONArray jsonArray = jsonObject.optJSONArray("merchandises");
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    obj = jsonArray.optJSONObject(i);
                    int Baseid = obj.optInt("id");
                    String Listtitle = obj.optString("title");
                    String designer = obj.optString("designer");
                    double price = obj.optDouble("price");
                    double cost_price = obj.optDouble("cost_price");
                    int percentum =100-obj.optInt("percentum");
                    String imageUrl = obj.optString("img");
                    int num = obj.optInt("total_count");
                    int totalCount = obj.optInt("total");
                    int buyCount = totalCount - num;
                    ShopTodayItem shopTodayItem = new ShopTodayItem(Baseid, Listtitle, designer, imageUrl, price, cost_price, num, totalCount, buyCount);
                    shopTodayItem.percentum=percentum;
                    items.add(shopTodayItem);
                }

                introduceTv.setText(introduce);
                loadingView.setVisibility(View.GONE);
                Message message = new Message();
                message.what = UPDATE_TIMER_START_AND_DETAIL_DATA;
                message.obj = endTime;
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
    public void onResume() {
        long temp = System.currentTimeMillis() - currentTime;
        if (!isTimerStart) {
            isTimerStart = true;
            if (mTimer == null)
                mTimer = new Timer();
            if (initTime > 0)
                initTime -= temp;
            mTimer.schedule(new MyTimerTask(), 1000, 1000);
        } else {

        }
        initCartTips();
        super.onResume();
        MobclickAgent.onPageStart(mTitle);
    }

    @Override
    public void onPause() {
        currentTime = System.currentTimeMillis();
        if (isTimerStart) {
            isTimerStart = false;
            if (mTimer != null) {
                mTimer.cancel();
                mTimer.purge();
                mTimer = null;
            }
        }
        super.onPause();
        MobclickAgent.onPageEnd(mTitle);
    }

    @Override
    public void onDestroy() {
        if (broadcastReceiver != null)
            mContext.unregisterReceiver(broadcastReceiver);
        isTimerStart = false;
        mRequestQueue.stop();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
        super.onDestroy();
    }

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initTime = initTime - 1000;
                    long day = 24 * 3600 * 1000;
                    long hour = 3600 * 1000;
                    long minute = 60 * 1000;
                    //两个日期想减得到天数
                    long dayCount = initTime / day;
                    long hourCount = (initTime - (dayCount * day)) / hour;
                    long minCount = (initTime - (dayCount * day) - (hour * hourCount)) / minute;
                    long secondCount = (initTime - (dayCount * day) - (hour * hourCount) - (minCount * minute)) / 1000;
                    String hourStr = String.format("%02d", hourCount);
                    String minStr = String.format("%02d", minCount);
                    String secondStr = String.format("%02d", secondCount);
                    String timerFormat = mContext.getResources().getString(R.string.timer);
                    String timer = String.format(timerFormat, dayCount, hourStr, minStr, secondStr);
                    timeTv.setText(timer);
                    if (initTime <= 0) {
                        if (mTimer != null) {
                            mTimer.cancel();
                            timeTv.setText("已结束");
                        }
                    }
                }
            });
        }
    }
}
