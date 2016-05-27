package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
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
import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.CartGoodsItem;
import com.zhaidou.model.Category;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.ListViewForScrollView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 软装清单
 */
public class GoodsArticleListFragment extends BaseFragment
{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_CATEGORY = "category";

    private String mParam1;
    private Category mCategory;

    private WeakHashMap<Integer, View> mHashMap = new WeakHashMap<Integer, View>();
    private PullToRefreshScrollView scrollView;
    private ListViewForScrollView listView;
    private int currentPage = 1;

    private Dialog mDialog;
    private Context mContext;

    private static final int UPDATE_HOMELIST = 3;
    private RequestQueue mRequestQueue;
    private List<CartGoodsItem> articleList = new ArrayList<CartGoodsItem>();

    private GoodsAdapter mGoodsAdapter;

    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if (mDialog != null)
            {
                mDialog.dismiss();
            }
            mGoodsAdapter.notifyDataSetChanged();
            scrollView.onRefreshComplete();
            if (articleList.size() < currentPage * 10)
            {
                scrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            } else
            {
                scrollView.setMode(PullToRefreshBase.Mode.BOTH);
            }

        }
    };

    private PullToRefreshBase.OnRefreshListener2 onRefreshListener = new PullToRefreshBase.OnRefreshListener2()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView)
        {
            FetchData();
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {
            ++currentPage;
            FetchData();
        }
    };

    public static GoodsArticleListFragment newInstance(String param1, Category category)
    {
        GoodsArticleListFragment fragment = new GoodsArticleListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putSerializable(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    public GoodsArticleListFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mCategory = (Category) getArguments().getSerializable(ARG_CATEGORY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_article_list, container, false);

        mContext = getActivity();

        scrollView = (PullToRefreshScrollView) view.findViewById(R.id.scrollView);
        scrollView.setMode(PullToRefreshBase.Mode.BOTH);
        scrollView.setOnRefreshListener(onRefreshListener);
        listView = (ListViewForScrollView) view.findViewById(R.id.lv_special_list);
        mGoodsAdapter = new GoodsAdapter(getActivity(), articleList);
        listView.setAdapter(mGoodsAdapter);

        mRequestQueue = Volley.newRequestQueue(getActivity());


        if (NetworkUtils.isNetworkAvailable(getActivity()))
        {
            mDialog = CustomLoadingDialog.setLoadingDialog(getActivity(), "loading");
            FetchData();
        } else
        {
            Toast.makeText(getActivity(), "抱歉,网络链接失败", Toast.LENGTH_SHORT).show();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {

            }
        });
        return view;
    }

    private void FetchData()
    {
        String url ="";
        ToolUtils.setLog(url);
        JsonObjectRequest jr = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                    articleList.clear();
                scrollView.onRefreshComplete();

                Message message = new Message();
                message.what = UPDATE_HOMELIST;
                handler.sendMessage(message);
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                if (mDialog != null)
                {
                    mDialog.dismiss();
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

    public class GoodsAdapter extends BaseListAdapter<CartGoodsItem>
    {
        Context context;

        public GoodsAdapter(Context context, List<CartGoodsItem> list)
        {
            super(context, list);
            this.context = context;
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent)
        {
            convertView = mHashMap.get(position);

            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_article_goods, null);

            TextView goodsNameTv = ViewHolder.get(convertView, R.id.goodsNameTv);
            TextView goodsSizeTv = ViewHolder.get(convertView, R.id.goodsSizeTv);
            ImageView goodsImageTv = ViewHolder.get(convertView, R.id.goodsImageTv);
            TextView goodsPriceTv = ViewHolder.get(convertView, R.id.goodsPriceTv);
            TextView goodsNumTv = ViewHolder.get(convertView, R.id.goodsNumTv);
            TextView goodsTypeTv = ViewHolder.get(convertView, R.id.goodsTypeTv);
            TextView goodsBuyTv = ViewHolder.get(convertView, R.id.goodsBuyTv);

            CartGoodsItem goodsItem = getList().get(position);

            goodsNameTv.setText(goodsItem.name);
            ToolUtils.setImageCacheUrl(goodsItem.imageUrl, goodsImageTv, R.drawable.icon_loading_defalut);

            if (true)
            {
                goodsBuyTv.setText("宅豆");
                goodsBuyTv.setTextColor(getResources().getColor(R.color.green_color));
            }
            else
            {
                goodsBuyTv.setText("淘宝");
                goodsBuyTv.setTextColor(Color.parseColor("#FD783A"));
            }


            mHashMap.put(position, convertView);
            return convertView;
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("软装清单");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("软装清单");
    }
}
