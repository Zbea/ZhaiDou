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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.zhaidou.activities.HomePTActivity;
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.adapter.AdViewAdpater;
import com.zhaidou.adapter.ShopSpecialAdapter;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.ShopSpecialItem;
import com.zhaidou.model.SwitchImage;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.ListViewForScrollView;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


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
    private View contentView;
    private LinearLayout loadingView,nullNetView,nullView;
    private TextView  reloadBtn,reloadNetBtn;

    private RequestQueue mRequestQueue;

    private final static int UPDATE_BANNER = 1003;

    private TypeFaceTextView backBtn, titleTv;
    private TextView cartTipsTv;
    private ImageView myCartBtn;
    private PullToRefreshScrollView mScrollView;
    private ListViewForScrollView mListView;
    private List<ShopSpecialItem> items = new ArrayList<ShopSpecialItem>();
    private ShopSpecialAdapter adapter;

    private ViewPager viewPager;
    private LinearLayout tipsLine;//轮播指示标志
    private List<SwitchImage> banners;
    private ImageView[] dots;
    private List<View> adPics = new ArrayList<View>();
    private AdViewAdpater adViewAdpater;
    private int currentItem = 5000;
    boolean nowAction = false;
    boolean isStop = true;


    private BroadcastReceiver broadcastReceiver=new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action=intent.getAction();
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
            if (mDialog != null)
                mDialog.dismiss();
            switch (msg.what)
            {
                case 1001:
                    loadingView.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    break;
                case 1002:
                    viewPager.setCurrentItem(currentItem);
                    break;

                case UPDATE_BANNER:
                    setAdView();
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
            mScrollView.onRefreshComplete();
            items.removeAll(items);
            banners.removeAll(banners);
            page = 1;
            getBannerData(1);
            FetchData(page);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {
            page++;
            mScrollView.onRefreshComplete();
            FetchData(page);
            adapter.notifyDataSetChanged();
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
            ShopTodaySpecialFragment shopTodaySpecialFragment = ShopTodaySpecialFragment.newInstance(items.get(i).title, items.get(i).id);
            ((MainActivity) getActivity()).navigationToFragment(shopTodaySpecialFragment);
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
                    }
                    else
                    {
                        ToolUtils.setToast(getActivity(), "抱歉，尚未登录");
                    }
                    break;

                case R.id.nullReload:
                    initDate();
                    break;
                case R.id.netReload:
                    initDate();
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
        if(mView==null)
        {
            mView = inflater.inflate(R.layout.shop_special_page, container, false);
            initView();
            initDate();
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
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(ZhaiDou.IntentRefreshCartGoodsTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginExitTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginTag);
        mContext.registerReceiver(broadcastReceiver,intentFilter);
    }

    /**
     * 初始化数据
     */
    private void initView()
    {
        loadingView = (LinearLayout) mView.findViewById(R.id.loadingView);
        nullNetView= (LinearLayout) mView.findViewById(R.id.nullNetline);
        nullView= (LinearLayout) mView.findViewById(R.id.nullline);
        reloadBtn = (TextView) mView.findViewById(R.id.nullReload);
        reloadBtn.setOnClickListener(onClickListener);
        reloadNetBtn = (TextView) mView.findViewById(R.id.netReload);
        reloadNetBtn.setOnClickListener(onClickListener);

        backBtn = (TypeFaceTextView) mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);
        titleTv = (TypeFaceTextView) mView.findViewById(R.id.title_tv);
        titleTv.setText(R.string.home_shop_special_text);

        mScrollView = (PullToRefreshScrollView)mView.findViewById(R.id.sv_special_scrollview);
        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mScrollView.setOnRefreshListener(refreshListener);

        mListView = (ListViewForScrollView) mView.findViewById(R.id.shopListView);
        adapter = new ShopSpecialAdapter(mContext, items);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(onItemClickListener);

        myCartBtn = (ImageView) mView.findViewById(R.id.myCartBtn);
        myCartBtn.setOnClickListener(onClickListener);
        cartTipsTv=(TextView)mView.findViewById(R.id.myCartTipsTv);


        viewPager = (ViewPager) mView.findViewById(R.id.home_adv_pager);
        viewPager.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenWidth*300/750));

        ImageView imageView=(ImageView)mView.findViewById(R.id.shopBanner);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenWidth*300/750));

        mRequestQueue = Volley.newRequestQueue(mContext);
        initCartTips();


    }

    public boolean checkLogin()
    {
        String token=(String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        int id=(Integer)SharedPreferencesUtil.getData(getActivity(),"userId",-1);
        boolean isLogin=!TextUtils.isEmpty(token)&&id>-1;
        return isLogin;
    }

    /**
     * 红色标识提示显示数量
     */
    private void initCartTips()
    {
        if (MainActivity.num>0)
        {
            cartTipsTv.setVisibility(View.VISIBLE);
            cartTipsTv.setText(""+MainActivity.num);
        }
        else
        {
            cartTipsTv.setVisibility(View.GONE);
        }
    }

    /**
     * 广告轮播设置
     */
    private void setAdView()
    {
        tipsLine = (LinearLayout) mView.findViewById(R.id.home_viewGroup);
        tipsLine.removeAllViews();
        if (banners.size() > 1)
        {
            for (int i = 0; i < banners.size(); i++)
            {
                final int tag = i;
                final ImageView img = new ImageView(mContext);
                img.setImageResource(R.drawable.icon_loading_item);
                img.setScaleType(ImageView.ScaleType.FIT_XY);
                img.setLayoutParams(new ViewGroup.LayoutParams(screenWidth, screenWidth * 300 / 750));
                img.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent1 = new Intent();
                        intent1.putExtra("url", banners.get(tag).url);
                        intent1.putExtra("title", banners.get(tag).getTitle());
                        intent1.setClass(getActivity(), HomePTActivity.class);
                        getActivity().startActivity(intent1);
                    }
                });
                ToolUtils.setImageCacheUrl(banners.get(i).imageUrl, img);
                adPics.add(img);
            }
            dots = new ImageView[adPics.size()];
            for (int i = 0; i < adPics.size(); i++)
            {
                ImageView dot_iv = new ImageView(mContext);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.bottomMargin = 10;
                if (i == 0)
                {
                    params.leftMargin = 0;
                } else
                {
                    params.leftMargin = 20;
                }
                dot_iv.setLayoutParams(params);
                dots[i] = dot_iv;
                tipsLine.addView(dot_iv);
                if (i == 0)
                {
                    dots[i].setBackgroundResource(R.drawable.home_tips_foucs_icon);
                } else
                {
                    dots[i].setBackgroundResource(R.drawable.home_tips_icon);
                }
            }
            if(adViewAdpater==null)
            {
                adViewAdpater = new AdViewAdpater(mContext, adPics);
                viewPager.setAdapter(adViewAdpater);
                viewPager.setOnPageChangeListener(new MyPageChangeListener());
                viewPager.setOnTouchListener(new View.OnTouchListener()
                {
                    @Override
                    public boolean onTouch(View v, MotionEvent event)
                    {
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        return false;
                    }
                });
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        while (isStop)
                        {
                            try
                            {
                                Thread.sleep(5000);
                            } catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                            if (!nowAction)
                            {
                                currentItem = currentItem + 1;
                                handler.sendEmptyMessage(1002);
                            }
                        }
                    }
                }).start();
            }
            else
            {
                adViewAdpater.notifyDataSetChanged();
            }

        } else if (banners.size() == 1)
        {
            isStop = false;
            for (int i = 0; i < banners.size(); i++)
            {
                final int tag = i;
                final ImageView img = new ImageView(mContext);
                img.setImageResource(R.drawable.icon_loading_item);
                img.setScaleType(ImageView.ScaleType.FIT_XY);
                img.setLayoutParams(new ViewGroup.LayoutParams(screenWidth, screenWidth * 300 / 750));
                img.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent1 = new Intent();
                        intent1.putExtra("url", banners.get(tag).url);
                        intent1.putExtra("from", "beauty");
                        intent1.putExtra("title", banners.get(tag).getTitle());
                        intent1.setClass(getActivity(), ItemDetailActivity.class);
                        getActivity().startActivity(intent1);
                    }
                });
                ToolUtils.setImageCacheUrl(banners.get(i).imageUrl, img);
                adPics.add(img);
            }
            if(adViewAdpater==null)
            {
                adViewAdpater = new AdViewAdpater(mContext, adPics);
                viewPager.setAdapter(adViewAdpater);
            }
            else
            {
                adViewAdpater.notifyDataSetChanged();
            }

        }
    }

    /**
     * 初始化数据
     */
    private void initDate()
    {
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
        if (NetworkUtils.isNetworkAvailable(mContext))
        {
            getBannerData(1);
            FetchData(page);
        }
        else
        {
            if (mDialog!=null)
                mDialog.dismiss();
            nullView.setVisibility(View.GONE);
            nullNetView.setVisibility(View.VISIBLE);
        }

    }

    /**
     * 获得广告数据
     */
    private void getBannerData(int i)
    {
        String url = ZhaiDou.shopSpecialBannerUrl + i;
        banners = new ArrayList<SwitchImage>();
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
                        String name = obj.optString("title");
                        String imageUrl = obj.optString("imgs");
                        String url = obj.optString("url");
                        SwitchImage switchImage = new SwitchImage(url, id, name, imageUrl);
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
        });
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
                if (response==null)
                {
                    mDialog.dismiss();
                    nullView.setVisibility(View.VISIBLE);
                    nullNetView.setVisibility(View.GONE);
                }
                String result = response.toString();
                JSONObject obj;
                try
                {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray jsonArray = jsonObject.optJSONArray("sales");
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        obj = jsonArray.optJSONObject(i);
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
                } catch (JSONException e)
                {
                    e.printStackTrace();
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
                mDialog.dismiss();
                nullView.setVisibility(View.VISIBLE);
                nullNetView.setVisibility(View.GONE);
                Toast.makeText(mContext, "加载失败", Toast.LENGTH_SHORT).show();
                mScrollView.onRefreshComplete();
                mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
            }
        });
        mRequestQueue.add(jr);
    }

    /**
     * 广告轮播指示器
     */
    private class MyPageChangeListener implements ViewPager.OnPageChangeListener
    {
        public void onPageSelected(int position)
        {
            currentItem = position;
            if (adPics.size() != 0)
            {
                changeDotsBg(currentItem % adPics.size());
            }
        }

        public void onPageScrollStateChanged(int arg0)
        {
            if (arg0 == 0)
            {
                nowAction = false;
            }
            if (arg0 == 1)
            {
                nowAction = true;
            }
            if (arg0 == 2)
            {
            }
        }

        public void onPageScrolled(int arg0, float arg1, int arg2)
        {
            viewPager.getParent().requestDisallowInterceptTouchEvent(true);
        }

        private void changeDotsBg(int currentitem)
        {
            for (int i = 0; i < dots.length; i++)
            {
                if (currentitem == i)
                {
                    dots[currentitem].setBackgroundResource(R.drawable.home_tips_foucs_icon);
                } else
                {
                    dots[i].setBackgroundResource(R.drawable.home_tips_icon);
                }
            }
        }
    }

    @Override
    public void onDestroy()
    {
        isStop = false;
        mContext.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

}
