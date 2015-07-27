package com.zhaidou.fragments;



import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
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
import com.zhaidou.adapter.ShopSpecialAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.ShopSpecialItem;
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
public class ShopSpecialFragment extends BaseFragment {
    private static final String PAGE = "page";
    private static final String INDEX = "index";

    private String mPage;
    private int mIndex;
    private View mView;
    private Context mContext;
    private int page=1;
    private Dialog mDialog;

    private RequestQueue mRequestQueue;
    String url="http://stg.zhaidou.com/uploads/article/article/asset_img/303/99d2fa9df325d76ac941b246ecf1488c.jpg";

    private ImageView adIv;
    private TypeFaceTextView backBtn,titleTv;
    private TextView myCartTips;
    private RelativeLayout myCartBtn;
    private PullToRefreshScrollView mScrollView;
    private ListViewForScrollView mListView;
    private List<ShopSpecialItem> items=new ArrayList<ShopSpecialItem>();
    private ShopSpecialAdapter adapter;


    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
        if (mDialog!=null)
            mDialog.dismiss();
            switch (msg.what)
            {
                case 1001:
                        adapter.notifyDataSetChanged();
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
            page=1;
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
    private AdapterView.OnItemClickListener onItemClickListener=new AdapterView.OnItemClickListener()
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
    private View.OnClickListener onClickListener=new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            switch (view.getId())
            {
                case R.id.back_btn:
                    ((MainActivity)getActivity()).popToStack(ShopSpecialFragment.this);
                break;
                case R.id.myCartBtn:
                    ShopCartFragment shopCartFragment = ShopCartFragment.newInstance("", 0);
                    ((MainActivity) getActivity()).navigationToFragment(shopCartFragment);
                    break;
            }
        }
    };

    public static ShopSpecialFragment newInstance(String page, int index) {
        ShopSpecialFragment fragment = new ShopSpecialFragment();
        Bundle args = new Bundle();
        args.putString(PAGE, page);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }
    public ShopSpecialFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getString(PAGE);
            mIndex = getArguments().getInt(INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView=inflater.inflate(R.layout.shop_special_page, container, false);
        mContext=getActivity();

        initView();
        initDate();

        return mView;
    }

    /**
     * 初始化数据
     */
    private void initView()
    {
        mDialog= CustomLoadingDialog.setLoadingDialog(mContext,"loading");
        backBtn=(TypeFaceTextView)mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);
        titleTv=(TypeFaceTextView)mView.findViewById(R.id.title_tv);
        titleTv.setText(R.string.home_shop_special_text);
        adIv=(ImageView)mView.findViewById(R.id.shopAdImage);
        ToolUtils.setImageCacheUrl(url,adIv);

        mScrollView = (PullToRefreshScrollView)mView.findViewById(R.id.sv_special_scrollview);
        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mScrollView.setOnRefreshListener(refreshListener);

        mListView=(ListViewForScrollView)mView.findViewById(R.id.shopListView);
        adapter=new ShopSpecialAdapter(mContext,items);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(onItemClickListener);

        myCartTips=(TextView)mView.findViewById(R.id.myCartTipsTv);
        myCartBtn=(RelativeLayout)mView.findViewById(R.id.myCartBtn);
        myCartBtn.setOnClickListener(onClickListener);

        mRequestQueue= Volley.newRequestQueue(mContext);

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
            FetchData(page);
        }
    }

    /**
     * 加载列表数据
     */
    private void FetchData(int currentPage)
    {
        final String url;
        url = ZhaiDou.shopSpecialListUrl+"&page="+currentPage;
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
                    JSONArray jsonArray=jsonObject.optJSONArray("sales");
                    for (int i = 0; i <jsonArray.length() ; i++)
                    {
                        obj=jsonArray.optJSONObject(i);
                        int id=obj.optInt("id");
                        String title=obj.optString("title");
                        String sales=obj.optString("tags");
                        String time=obj.optString("day");
                        String startTime=obj.optString("start_time");
                        String endTime=obj.optString("end_time");
                        String overTime=obj.optString("over_day");
                        String imageUrl=obj.optString("banner");

                        ShopSpecialItem shopSpecialItem=new ShopSpecialItem(id,title,sales,time,startTime,endTime,overTime,imageUrl);
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
                Toast.makeText(mContext, "加载失败", Toast.LENGTH_SHORT).show();
                mScrollView.onRefreshComplete();
                mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
            }
        });
        mRequestQueue.add(jr);
    }


}
