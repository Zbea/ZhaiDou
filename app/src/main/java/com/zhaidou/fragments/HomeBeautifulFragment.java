package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.ArticleWebViewActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.ListViewForScrollView;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;


/**
 * 美丽家
 */
public class HomeBeautifulFragment extends BaseFragment
{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private TextView titleTv;
    private View mView;
    private Dialog loading;
    private ListViewForScrollView listView;
    private PullToRefreshScrollView mScrollView;
    private int currentPage=1;
    private int pageTotal=0;
    private List<JSONObject> listItem=new ArrayList<JSONObject>();
    private RequestQueue mRequestQueue;
    private ImageAdapter homeItemsAdapter;

    private WeakHashMap<Integer, View> mHashMap = new WeakHashMap<Integer, View>();


    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if (msg.what == 1)
            {
                homeItemsAdapter.notifyDataSetChanged();
            }

        }
    };


    private PullToRefreshBase.OnRefreshListener2 onRefreshListener2=new PullToRefreshBase.OnRefreshListener2()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView)
        {
            listItem.clear();
            currentPage = 1;
            loadMoreData();
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {
            currentPage++;
            loadMoreData();
        }
    };

    private AdapterView.OnItemClickListener itemSelectListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
        {
            try
            {
                JSONObject item = listItem.get(i);
                Intent detailIntent = new Intent(getActivity(), ArticleWebViewActivity.class);
                detailIntent.putExtra("id", item.optInt("id"));
                detailIntent.putExtra("title", item.optString("title").toString());
                detailIntent.putExtra("imageUrl", item.optString("imageUrl").toString());
                detailIntent.putExtra("url", item.optString("url").toString());
                detailIntent.putExtra("show_share", true);
                detailIntent.putExtra("show_title", false);
                startActivity(detailIntent);
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    };

    public static HomeBeautifulFragment newInstance(String param1, String param2) {
        HomeBeautifulFragment fragment = new HomeBeautifulFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2,param2);
        fragment.setArguments(args);
        return fragment;
    }

    public HomeBeautifulFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        mContext = getActivity();
        if (mView == null) {
            mView= inflater.inflate(R.layout.fragment_home_beautiful, container, false);
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
     * 初始化
     */
    private void initView()
    {

        titleTv = (TypeFaceTextView) mView.findViewById(R.id.title_tv);
        titleTv.setText(R.string.title_beauty);

        listView = (ListViewForScrollView) mView.findViewById(R.id.homeItemList);
        listItem = new ArrayList<JSONObject>();
        mRequestQueue = Volley.newRequestQueue(mContext);

        mScrollView = (PullToRefreshScrollView) mView.findViewById(R.id.sv_scrollview);
        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mScrollView.setOnRefreshListener(onRefreshListener2);

        homeItemsAdapter = new ImageAdapter(mContext);
        listView.setAdapter(homeItemsAdapter);
        listView.setOnItemClickListener(itemSelectListener);

        loadMoreData();
        loading = CustomLoadingDialog.setLoadingDialog(getActivity(), "loading");
    }


    /**
     * 开始数据请求
     */
    private void loadMoreData()
    {
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.HomeBeautifulUrl +currentPage, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                if (loading.isShowing())
                {
                    loading.dismiss();
                }
                mScrollView.onRefreshComplete();
                if (response != null)
                {
                    ToolUtils.setLog(response.toString());
                    JSONObject jsonObject=response.optJSONObject("data");
                    pageTotal=jsonObject.optInt("totalCount");
                    JSONArray jsonArray = jsonObject.optJSONArray("postsPOList");
                    if (jsonArray != null)
                    {
                        for (int i = 0; i < jsonArray.length(); i++)
                        {
                            JSONObject jsonObj = jsonArray.optJSONObject(i);
                            listItem.add(jsonObj);
                        }
                        handler.sendEmptyMessage(1);
                    }
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                if (loading.isShowing())
                {
                    loading.dismiss();
                }
                mScrollView.onRefreshComplete();
                if(currentPage>1)
                {
                    currentPage=currentPage-1;
                }
                ToolUtils.setToast(mContext,R.string.loading_fail_txt);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        mRequestQueue.add(request);
    }

    public class ImageAdapter extends BaseAdapter
    {

        private LayoutInflater inflater;

        public ImageAdapter(Context context)
        {
            this.inflater = LayoutInflater.from(context);
        }
        public void clear()
        {
            listItem.clear();
        }
        public int getCount()
        {
            return listItem.size();
        }
        public Object getItem(int position)
        {
            return listItem.get(position);
        }
        public long getItemId(int position)
        {
            return listItem.get(position).hashCode();
        }
        public View getView(int position, View view, ViewGroup parent)
        {
            view = mHashMap.get(position);
            if (view == null)
            {
                view = inflater.inflate(R.layout.item_home_design_case_list, null);
            }

            TextView title = (TextView) view.findViewById(R.id.title);
            TextView articleViews = (TextView) view.findViewById(R.id.views);
            ImageView cover = (ImageView) view.findViewById(R.id.cover);
            cover.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenWidth * 316 / 722));
            View lineTo = (View) view.findViewById(R.id.spaceView);
            if (position==0)
            {
                lineTo.setVisibility(View.GONE);
            }
            else
            {
                lineTo.setVisibility(View.VISIBLE);
            }

            final JSONObject item = listItem.get(position);
            try
            {
                title.setText(item.get("title").toString());
                articleViews.setText(item.optString("views"));
                ToolUtils.setImageCacheUrl(item.optString("imageUrl"), cover,R.drawable.icon_loading_item);

            } catch (Exception e)
            {
                e.printStackTrace();
            }
            mHashMap.put(position, view);
            return view;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        super.onHiddenChanged(hidden);
        if (!hidden)
        {
            if (listItem==null|listItem.size()<1)
            {
                listItem.clear();
                currentPage = 1;
                loadMoreData();
            }
        }

    }

    public void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_beauty));
    }

    public void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_beauty));
    }
}
