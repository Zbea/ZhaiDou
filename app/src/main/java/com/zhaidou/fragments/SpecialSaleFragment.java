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
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.activities.LoginActivity;
import com.zhaidou.activities.WebViewActivity;
import com.zhaidou.adapter.AdViewAdpater;
import com.zhaidou.adapter.GoodsImageAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Category;
import com.zhaidou.model.CountTime;
import com.zhaidou.model.Product;
import com.zhaidou.model.SwitchImage;
import com.zhaidou.model.User;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpecialSaleFragment extends BaseFragment implements View.OnClickListener, RegisterFragment.RegisterOrLoginListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;


    private GridView mGridView;
    private TextView mTimerView;
    private ProductAdapter mAdapter;
    private Map<Integer, View> mHashMap = new HashMap<Integer, View>();
    private MyTimer mTimer;
    private RequestQueue requestQueue;
    private List<Product> products = new ArrayList<Product>();

    private LinearLayout loadingView, nullNetView, nullView;
    private TextView reloadBtn, reloadNetBtn;

    private final int UPDATE_ADAPTER = 0;
    private final int UPDATE_COUNT_DOWN_TIME = 1;
    private final int UPDATE_UI_TIMER_FINISH = 2;
    private final int UPDATE_TIMER_START = 3;
    private final int UPDATE_BANNER = 4;

    private Dialog mDialog;

    private TextView cartTipsTv;
    private ImageView myCartBtn;

    private View rootView;
    private boolean isLogin;
    private long time;
    private long currentTime;
    private Context mContext;

    private ViewPager viewPager;
    private LinearLayout tipsLine;
    private List<SwitchImage> banners;
    private ImageView[] dots;
    private List<View> adPics = new ArrayList<View>();
    private AdViewAdpater adpaters;
    private GoodsImageAdapter adapter;
    private int currentItem = 5000;
    boolean nowAction = false;
    boolean isStop = true;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ZhaiDou.IntentRefreshCartGoodsTag)) {
                initCartTips();
            }
            if (action.equals(ZhaiDou.IntentRefreshLoginTag)) {
                isLogin = true;
                initCartTips();
            }
            if (action.equals(ZhaiDou.IntentRefreshLoginExitTag)) {
                isLogin = false;
                initCartTips();
            }
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_ADAPTER:
                    loadingView.setVisibility(View.GONE);
                    mAdapter.notifyDataSetChanged();
                    break;
                case UPDATE_COUNT_DOWN_TIME:
                    CountTime time = (CountTime) msg.obj;
                    String timerFormat = mContext.getResources().getString(R.string.timer);
                    String hourStr = String.format("%02d", time.getHour());
                    String minStr = String.format("%02d", time.getMinute());
                    String secondStr = String.format("%02d", time.getSecond());
                    String timer = String.format(timerFormat, time.getDay(), hourStr, minStr, secondStr);
                    mTimerView.setText(timer);
                    break;
                case UPDATE_UI_TIMER_FINISH:
                    mTimerView.setText("已结束");
                    break;
                case UPDATE_TIMER_START:
                    String date = (String) msg.obj;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    try {
                        long millionSeconds = sdf.parse(date).getTime();//毫秒
                        long temp = millionSeconds - System.currentTimeMillis();
                        mTimer = new MyTimer(temp, 1000);
                        mTimer.start();
                    } catch (Exception e) {
                        Log.i("Exception e", "E--->" + e == null ? "null" : e.getMessage());
                    }
                    break;
                case UPDATE_BANNER:
                    setAdView();
                    break;
                case 1002:
                    viewPager.setCurrentItem(currentItem);
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


    public static SpecialSaleFragment newInstance(String param1, String param2) {
        SpecialSaleFragment fragment = new SpecialSaleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public SpecialSaleFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (rootView == null) {
            mContext = getActivity();
            initBroadcastReceiver();
            rootView = inflater.inflate(R.layout.fragment_special_sale, container, false);

            loadingView = (LinearLayout) rootView.findViewById(R.id.loadingView);

            mGridView = (GridView) rootView.findViewById(R.id.gv_sale);
            mGridView.setEmptyView(mEmptyView);
            mTimerView = (TextView) rootView.findViewById(R.id.tv_count_time);

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

            viewPager = (ViewPager) rootView.findViewById(R.id.home_adv_pager);
            viewPager.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenWidth * 400 / 750));

            isLogin = checkLogin();

            initCartTips();

            initData();

            mAdapter.setOnInViewClickListener(R.id.ll_single_layout, new BaseListAdapter.onInternalClickListener() {
                @Override
                public void OnClickListener(View parentV, View v, Integer position, Object values) {
                    GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance(products.get(position).getTitle(), products.get(position).getId());
                    Bundle bundle = new Bundle();
                    bundle.putInt("flags", 1);
                    bundle.putInt("index", products.get(position).getId());
                    bundle.putString("page", products.get(position).getTitle());
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
     * 注册广播
     */
    private void initBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ZhaiDou.IntentRefreshCartGoodsTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginExitTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginTag);
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }

    public boolean checkLogin() {
        String token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        int id = (Integer) SharedPreferencesUtil.getData(getActivity(), "userId", -1);
        boolean isLogin = !TextUtils.isEmpty(token) && id > -1;
        return isLogin;
    }

    /**
     * 初始化收据
     */
    private void initData() {
        mDialog = CustomLoadingDialog.setLoadingDialog(getActivity(), "loading", true);
        if (NetworkUtils.isNetworkAvailable(getActivity())) {

            getBannerData();
            FetchData();
        } else {
            if (mDialog != null)
                mDialog.dismiss();
            nullView.setVisibility(View.GONE);
            nullNetView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 广告轮播设置
     */
    private void setAdView() {
        tipsLine = (LinearLayout) rootView.findViewById(R.id.home_viewGroup);
        tipsLine.removeAllViews();
        if (banners.size() > 1) {
            for (int i = 0; i < banners.size(); i++) {
                final int tag = i;
                final ImageView img = new ImageView(mContext);
                img.setImageResource(R.drawable.icon_loading_item);
                img.setScaleType(ImageView.ScaleType.FIT_XY);
                img.setLayoutParams(new ViewGroup.LayoutParams(screenWidth, screenWidth * 300 / 750));
                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                      r_type=0：0元特卖商城r_type=1：H5页面r_type=2：文章r_type=3：单品r_type=4：分类
                        SwitchImage item = banners.get(tag);
                        ToolUtils.setBannerGoto(item,mContext);
                    }
                });
                ToolUtils.setImageCacheUrl(banners.get(i).imageUrl, img);
                adPics.add(img);
            }
            dots = new ImageView[adPics.size()];
            for (int i = 0; i < adPics.size(); i++) {
                ImageView dot_iv = new ImageView(mContext);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.bottomMargin = 10;
                if (i == 0) {
                    params.leftMargin = 0;
                } else {
                    params.leftMargin = 20;
                }
                dot_iv.setLayoutParams(params);
                dots[i] = dot_iv;
                tipsLine.addView(dot_iv);
                if (i == 0) {
                    dots[i].setBackgroundResource(R.drawable.home_tips_foucs_icon);
                } else {
                    dots[i].setBackgroundResource(R.drawable.home_tips_icon);
                }
            }
            if (adpaters == null) {
                adpaters = new AdViewAdpater(mContext, adPics);
                viewPager.setAdapter(adpaters);
                viewPager.setOnPageChangeListener(new MyPageChangeListener());
                viewPager.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        return false;
                    }
                });
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (isStop) {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (!nowAction) {
                                currentItem = currentItem + 1;
                                mHandler.sendEmptyMessage(1002);
                            }
                        }
                    }
                }).start();
            } else {
                adpaters.notifyDataSetChanged();
            }

        } else if (banners.size() == 1) {
            isStop = false;
            for (int i = 0; i < banners.size(); i++) {
                final int tag = i;
                final ImageView img = new ImageView(mContext);
                img.setImageResource(R.drawable.icon_loading_item);
                img.setScaleType(ImageView.ScaleType.FIT_XY);
                img.setLayoutParams(new ViewGroup.LayoutParams(screenWidth, screenWidth * 300 / 750));
                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                      r_type=0：0元特卖商城r_type=1：H5页面r_type=2：文章r_type=3：单品r_type=4：分类
                        SwitchImage item = banners.get(tag);
                        ToolUtils.setBannerGoto(item,mContext);
                    }
                });
                ToolUtils.setImageCacheUrl(banners.get(i).imageUrl, img, R.drawable.icon_loading_item);
                adPics.add(img);
            }
            if (adapter == null) {
                adapter = new GoodsImageAdapter(mContext, adPics);
                viewPager.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }

        }
    }

    /**
     * 红色标识提示显示数量
     */
    private void initCartTips() {
        if (((MainActivity) getActivity()).getNum() > 0) {
            cartTipsTv.setVisibility(View.VISIBLE);
            cartTipsTv.setText("" + ((MainActivity) getActivity()).getNum());
        } else {
            cartTipsTv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_back:
                ((MainActivity) getActivity()).popToStack(SpecialSaleFragment.this);
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
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.SPECIAL_SALE_URL,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        mDialog.dismiss();
                        ToolUtils.setLog(jsonObject.toString());
                        if (jsonObject.equals("")) {
                            nullView.setVisibility(View.VISIBLE);
                            nullNetView.setVisibility(View.GONE);
                            ToolUtils.setToast(getActivity(), "加载失败");
                            return;
                        }
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
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers=new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        requestQueue.add(request);
    }

    /**
     * 获得广告数据
     */
    private void getBannerData() {
        String url = ZhaiDou.BannerUrl + 0;
        ToolUtils.setLog(url);
        banners = new ArrayList<SwitchImage>();
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
                    mHandler.sendMessage(message);
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
        requestQueue.add(bannerRequest);
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
            image.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth / 2 - 1, (screenWidth / 2 - 1) * 175 / 186));
            TextView tv_money = ViewHolder.get(convertView, R.id.tv_money);
            TextView tv_price = ViewHolder.get(convertView, R.id.tv_price);
            TextView tv_count = ViewHolder.get(convertView, R.id.tv_count);
            ImageView ll_sale_out = ViewHolder.get(convertView, R.id.ll_sale_out);
            ll_sale_out.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth / 2 - 1, (screenWidth / 2 - 1) * 175 / 186));
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
    public void onDestroyView() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        super.onDestroyView();
    }

    private class MyTimer extends CountDownTimer {
        private MyTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            time = l;
            long day = 24 * 3600 * 1000;
            long hour = 3600 * 1000;
            long minute = 60 * 1000;
            //两个日期想减得到天数
            long dayCount = l / day;
            long hourCount = (l - (dayCount * day)) / hour;
            long minCount = (l - (dayCount * day) - (hour * hourCount)) / minute;
            long secondCount = (l - (dayCount * day) - (hour * hourCount) - (minCount * minute)) / 1000;
            CountTime time = new CountTime(dayCount, hourCount, minCount, secondCount);
            Message message = new Message();
            message.what = UPDATE_COUNT_DOWN_TIME;
            message.obj = time;
            mHandler.sendMessage(message);
        }

        @Override
        public void onFinish() {
            mHandler.sendEmptyMessage(UPDATE_UI_TIMER_FINISH);
        }
    }

    @Override
    public void onRegisterOrLoginSuccess(User user, Fragment fragment) {
        SharedPreferencesUtil.saveUser(getActivity(), user);
        getActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onResume() {
        long temp = System.currentTimeMillis() - currentTime;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        mTimer = new MyTimer(time - temp, 1000);
        mTimer.start();
        super.onResume();
    }

    @Override
    public void onPause() {
        currentTime = System.currentTimeMillis();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    /**
     * 广告轮播指示器
     */
    private class MyPageChangeListener implements ViewPager.OnPageChangeListener {
        public void onPageSelected(int position) {
            currentItem = position;
            if (adPics.size() != 0) {
                changeDotsBg(currentItem % adPics.size());
            }
        }

        public void onPageScrollStateChanged(int arg0) {
            if (arg0 == 0) {
                nowAction = false;
            }
            if (arg0 == 1) {
                nowAction = true;
            }
            if (arg0 == 2) {
            }
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
            viewPager.getParent().requestDisallowInterceptTouchEvent(true);
        }

        private void changeDotsBg(int currentitem) {
            for (int i = 0; i < dots.length; i++) {
                if (currentitem == i) {
                    dots[currentitem].setBackgroundResource(R.drawable.home_tips_foucs_icon);
                } else {
                    dots[i].setBackgroundResource(R.drawable.home_tips_icon);
                }
            }
        }
    }
}
