package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.zhaidou.view.ListViewForScrollView;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by roy on 15/7/23.
 */
public class ShopTodaySpecialFragment extends BaseFragment {
    private static final String PAGE = "page";
    private static final String INDEX = "index";

    private String mPage;
    private int mIndex;
    private View mView;
    private Context mContext;
    private Dialog mDialog;
    private int id;
    private String mTitle;
    private String introduce;//引文介绍
    private final int UPDATE_COUNT_DOWN_TIME=1;
    private final int UPDATE_UI_TIMER_FINISH=2;
    private final int UPDATE_TIMER_START=3;

    private MyTimer mTimer;

    private RequestQueue mRequestQueue;

    private TypeFaceTextView backBtn,titleTv,introduceTv,timeTv;
    private PullToRefreshScrollView mScrollView;
    private ListViewForScrollView mListView;

    private TextView myCartTips;
    private RelativeLayout myCartBtn;

    private List<ShopTodayItem> items=new ArrayList<ShopTodayItem>();
    private ShopTodaySpecialAdapter adapter;
    private ShopSpecialItem shopSpecialItem;


    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if (mDialog!=null)
                mDialog.dismiss();
            switch (msg.what)
            {
                case 4:
                    introduceTv.setText(introduce);
                    adapter.notifyDataSetChanged();
                    break;
                case UPDATE_TIMER_START:
                    String date = (String)msg.obj;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    try{
                        long millionSeconds = sdf.parse(date).getTime();//毫秒
                        long hour=3600*1000;
                        long minute=60*1000;
                        millionSeconds=millionSeconds+hour*23+minute*59+59*1000;
                        long temp = millionSeconds-System.currentTimeMillis();
                        mTimer=new MyTimer(temp,1000);
                        mTimer.start();
                    }catch (Exception e){
                        Log.i("Exception e",e.getMessage());
                    }
                    break;
                case UPDATE_COUNT_DOWN_TIME:
                    CountTime time = (CountTime)msg.obj;
                    String timerFormat = getResources().getString(R.string.timer);
                    String hourStr=String.format("%02d", time.getHour());
                    String minStr=String.format("%02d", time.getMinute());
                    String secondStr=String.format("%02d", time.getSecond());
                    String timer = String.format(timerFormat,time.getDay(),hourStr,minStr,secondStr);
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
    private PullToRefreshBase.OnRefreshListener2 refreshListener=new PullToRefreshBase.OnRefreshListener2()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView)
        {
            mScrollView.onRefreshComplete();

            items.removeAll(items);
            initDate();
            adapter.notifyDataSetChanged();
        }
        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {
        }
    };

    /**
     * adapter短点击事件
     */
    private AdapterView.OnItemClickListener onItemClickListener=new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
        {
            GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance(items.get(i).title, 0);
            ((MainActivity) getActivity()).navigationToFragment(goodsDetailsFragment);
        }
    };

    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener=new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            switch (view.getId())
            {
                case R.id.back_btn:
                    ((MainActivity)getActivity()).popToStack(ShopTodaySpecialFragment.this);
                break;
                case R.id.myCartBtn:
                    ShopCartFragment shopCartFragment = ShopCartFragment.newInstance("", 0);
                    ((MainActivity) getActivity()).navigationToFragment(shopCartFragment);
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
            id=mIndex;
            mTitle=mPage;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView=inflater.inflate(R.layout.shop_today_special_page, container, false);
        mContext=getActivity();

        initView();
        initDate();

        return mView;
    }

    /**
     * 初始化数据
     */
    private void initDate()
    {
        if (items.size()>0 )
        {
            adapter.notifyDataSetChanged();
            mDialog.dismiss();
        }
        else
        {
            FetchData(id);
        }

    }

    /**
     * 初始化数据
     */
    private void initView()
    {
        mDialog= CustomLoadingDialog.setLoadingDialog(mContext, "loading");

        backBtn=(TypeFaceTextView)mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);
        titleTv=(TypeFaceTextView)mView.findViewById(R.id.title_tv);
        titleTv.setText(mTitle);

        mScrollView = (PullToRefreshScrollView)mView.findViewById(R.id.sv_shop_today_special_scrollview);
        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        timeTv=(TypeFaceTextView)mView.findViewById(R.id.shopTimeTv);

        mScrollView = (PullToRefreshScrollView)mView.findViewById(R.id.scrollview);
        mScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mScrollView.setOnRefreshListener(refreshListener);

        mListView=(ListViewForScrollView)mView.findViewById(R.id.shopListView);
        adapter=new ShopTodaySpecialAdapter(mContext,items);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(onItemClickListener);

        introduceTv=(TypeFaceTextView)mView.findViewById(R.id.adText);

        myCartTips=(TextView)mView.findViewById(R.id.myCartTipsTv);
        myCartBtn=(RelativeLayout)mView.findViewById(R.id.myCartBtn);
        myCartBtn.setOnClickListener(onClickListener);

        mRequestQueue= Volley.newRequestQueue(mContext);

    }

    /**
     * 加载列表数据
     */
    private void FetchData(int id)
    {
        final String url;
        url = ZhaiDou.shopSpecialTadayUrl+id;
        JsonObjectRequest jr = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                String result=response.toString();
                JSONObject obj;
                try
                {
                    JSONObject jsonObject=new JSONObject(result);
                    JSONObject josnObject1=jsonObject.optJSONObject("sale");
                    int id=josnObject1.optInt("id");
                    String title=josnObject1.optString("title");
                    String time=josnObject1.optString("day");
                    String startTime=josnObject1.optString("start_time");
                    String endTime=josnObject1.optString("end_time");
                    String overTime=josnObject1.optString("over_day");
                    introduce=josnObject1.optString("quotation");
                    shopSpecialItem=new ShopSpecialItem(id,title,null,time,startTime,endTime,overTime,null);
                    Log.i("zhaidou","endTime:"+endTime);
                    handler.obtainMessage(UPDATE_TIMER_START,endTime).sendToTarget();//开始倒计时

                    JSONArray jsonArray=josnObject1.optJSONArray("merchandises");
                    for (int i = 0; i <jsonArray.length() ; i++)
                    {
                        obj=jsonArray.optJSONObject(i);
                        int Baseid=obj.optInt("id");
                        String Listtitle=obj.optString("title");
                        String designer=obj.optString("designer");
                        double price=obj.optDouble("price");
                        double cost_price=obj.optDouble("cost_price");
                        String imageUrl=obj.optString("img");
                        int num=obj.optInt("total_count");
                        ShopTodayItem shopTodayItem=new ShopTodayItem(Baseid,Listtitle,designer,imageUrl,price,cost_price,num);
                        items.add(shopTodayItem);
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
                Message message = new Message();
                message.what = 4;
                handler.sendMessage(message);
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                mDialog.dismiss();
                Toast.makeText(mContext, "加载失败", Toast.LENGTH_SHORT).show();
                mScrollView.onRefreshComplete();
            }
        });
        mRequestQueue.add(jr);
    }

    /**
     * 倒计时
     */
    private class MyTimer extends CountDownTimer
    {
        private MyTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        @Override
        public void onTick(long l) {
            long day=24*3600*1000;
            long hour=3600*1000;
            long minute=60*1000;
            //两个日期想减得到天数
            long dayCount= l/day;
            long hourCount= (l-(dayCount*day))/hour;
            long minCount=(l-(dayCount*day)-(hour*hourCount))/minute;
            long secondCount=(l-(dayCount*day)-(hour*hourCount)-(minCount*minute))/1000;
            CountTime time = new CountTime(dayCount,hourCount,minCount,secondCount);

            handler.obtainMessage(UPDATE_COUNT_DOWN_TIME,time).sendToTarget();//刷新倒计时
        }
        @Override
        public void onFinish() {
            handler.sendEmptyMessage(UPDATE_UI_TIMER_FINISH);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mTimer!=null)
        {
            mTimer.cancel();
        }
    }
}
