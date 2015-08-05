package com.zhaidou.fragments;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pulltorefresh.PullToRefreshBase;
import com.viewpagerindicator.TabPageIndicator;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.adapter.GoodsImageAdapter;
import com.zhaidou.adapter.GoodsSizeAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.GoodInfo;
import com.zhaidou.model.GoodsSizeItem;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.ChildGridView;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by roy on 15/7/23.
 */
public class GoodsDetailsChildFragment extends BaseFragment
{
    private static final String DATA = "page";
    private static final String INDEX = "index";

    private View mView;

    private String mPage;
    private int mIndex;
    private Context mContext;
    private ListView mListView;
    private GoodInfoAdapter mAdapter;
    private List<GoodInfo> goodInfos;
    private RequestQueue mRequestQueue;
    private static final int UPDATE_GOOD_INFO = 0;
    private List<String> imgsList;
    private LinearLayout mImageContainer;


    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case UPDATE_GOOD_INFO:
                    mAdapter.notifyDataSetChanged();
                    addImageToContainer(imgsList);
                    break;
            }
        }
    };

    public static GoodsDetailsChildFragment newInstance(List<GoodInfo> infos, int index)
    {
        GoodsDetailsChildFragment fragment = new GoodsDetailsChildFragment();
        Bundle args = new Bundle();
//        args.putString(PAGE, page);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    public GoodsDetailsChildFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
//            mPage = getArguments().getString(PAGE);
            mIndex = getArguments().getInt(INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        if (mView == null)
        {
            mView = inflater.inflate(R.layout.goods_details_info_page, container, false);
            mContext=getActivity();
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

    private void initView()
    {
        mListView = (ListView) mView.findViewById(R.id.lv_good_info);
        mImageContainer = (LinearLayout) mView.findViewById(R.id.ll_img_container);
        mRequestQueue = Volley.newRequestQueue(getActivity());
        goodInfos = new ArrayList<GoodInfo>();
        mAdapter = new GoodInfoAdapter(getActivity(), goodInfos);
        mListView.setAdapter(mAdapter);
        FetchDetailData();
    }

    /**
     * Created by wangclark on 15/6/10.
     */
    public class GoodInfoAdapter extends BaseListAdapter<GoodInfo>
    {
        public GoodInfoAdapter(Context context, List<GoodInfo> list)
        {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_goods_info, null);
            TextView tv_key = ViewHolder.get(convertView, R.id.tv_key);
            TextView tv_value = ViewHolder.get(convertView, R.id.tv_value);
            GoodInfo goodInfo = getList().get(position);
            tv_key.setText(goodInfo.getTitle() + ": ");
            tv_value.setText(goodInfo.getValue());
            return convertView;
        }
    }

    private void addImageToContainer(List<String> urls) {
        mImageContainer.removeAllViews();
        for (String url : urls) {
            ImageView imageView = new ImageView(getActivity());
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            imageView.setLayoutParams(layoutParams);
            ToolUtils.setImageCacheUrl(url, imageView);
            mImageContainer.addView(imageView);
        }
    }


    public void FetchDetailData()
    {
        String url = ZhaiDou.goodsDetailsUrlUrl + mIndex;
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                if (jsonObject != null)
                {
                    JSONObject merchandise = jsonObject.optJSONObject("merchandise");
                    JSONArray descriptions = merchandise.optJSONArray("descriptions");
                    if (descriptions != null && descriptions.length() > 0)
                    {
                        for (int i = 0; i < descriptions.length(); i++)
                        {
                            JSONObject description = descriptions.optJSONObject(i);
                            int id = description.optInt("id");
                            String title = description.optString("title");
                            String value = description.optString("value");
                            GoodInfo goodInfo = new GoodInfo(id, title, value);
                            goodInfos.add(goodInfo);
                        }
                        JSONArray imgsArray = merchandise.optJSONArray("imgs");
                        if (imgsArray != null && imgsArray.length() > 0) {
                            imgsList = new ArrayList<String>();
                            for (int i = 0; i < imgsArray.length(); i++) {
                                JSONObject imgObj = imgsArray.optJSONObject(i);
                                String url = imgObj.optString("url");
                                imgsList.add(url);
                            }
                        }

                        handler.sendEmptyMessage(UPDATE_GOOD_INFO);
                    }
                } else
                {
                    Toast.makeText(getActivity(), "加载错误", Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                if (volleyError != null)
                    Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
            }
        });
        mRequestQueue.add(request);
    }
}
