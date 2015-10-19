package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshListView;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.utils.HtmlFetcher;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;


/**
 * 美丽家
 */
public class MainStrategyFragment extends BaseFragment
{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    //WebViewFragment.newInstance("http://buy.zhaidou.com/gl.html", false)
    private View mView;
    private Dialog loading;
    private PullToRefreshListView listView;
    private String targetUrl="http://buy.zhaidou.com/?zdclient=ios&tag=006&count=10&json=1&page={0}";
    private int currentPage=1;
    private boolean loadedAll;
    private final int LOADED = 1;
    private List<JSONObject> listItem;
    private RequestQueue mRequestQueue;
    private ImageAdapter homeItemsAdapter;

    private WeakHashMap<Integer, View> mHashMap = new WeakHashMap<Integer, View>();

    private static final int STATUS_REFRESH = 0;
    private static final int STATUS_LOAD_MORE = 1;

    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if (msg.what == LOADED)
            {

                if (loading.isShowing())
                {
                    loading.dismiss();
                }
            }
            listView.onRefreshComplete();
            homeItemsAdapter.notifyDataSetChanged();
        }
    };


    private PullToRefreshBase.OnRefreshListener2 onRefreshListener2=new PullToRefreshBase.OnRefreshListener2()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView)
        {
            currentPage = 1;
            loadMoreData(STATUS_REFRESH);
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {
            currentPage++;
            loadMoreData(STATUS_LOAD_MORE);
        }
    };

    private AdapterView.OnItemClickListener itemSelectListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
        {
            try
            {
                JSONObject item = listItem.get(i-1);
                Intent detailIntent = new Intent(getActivity(), ItemDetailActivity.class);
                detailIntent.putExtra("id", item.get("id").toString());
                detailIntent.putExtra("title", item.get("title").toString());
                detailIntent.putExtra("cover_url", URLDecoder.decode(item.get("thumbnail").toString()));
                detailIntent.putExtra("from","product");
                detailIntent.putExtra("url", item.get("url").toString());
                detailIntent.putExtra("show_header", false);
                startActivity(detailIntent);
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    };

    public MainStrategyFragment()
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
            mView= inflater.inflate(R.layout.fragment_main_strategy, container, false);
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
        listView = (PullToRefreshListView) mView.findViewById(R.id.homeItemList);
        listItem = new ArrayList<JSONObject>();
        mRequestQueue = Volley.newRequestQueue(mContext);

        homeItemsAdapter = new ImageAdapter(mContext);
        listView.setAdapter(homeItemsAdapter);
        listView.setOnItemClickListener(itemSelectListener);
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        listView.setOnRefreshListener(onRefreshListener2);

        loadMoreData(STATUS_REFRESH);
        loading = CustomLoadingDialog.setLoadingDialog(getActivity(), "loading");
    }


    /**
     * 开始数据请求
     * @param status
     */
    private void loadMoreData(final int status)
    {
        new Thread()
        {
            public void run()
            {
                if (loadedAll)
                {
                    return;
                }
                try
                {
                    String requestUrl = MessageFormat.format(targetUrl, currentPage);
                    java.net.URL url = new URL(requestUrl);
                    String jsonContent = HtmlFetcher.fetch(url);
                    try
                    {
                        JSONObject root = new JSONObject(jsonContent);
                        JSONArray items = root.getJSONArray("posts");
                        if (currentPage == 1)
                            listItem.clear();
                        for (int i = 0; i < items.length(); i++)
                        {
                            listItem.add(items.getJSONObject(i));
                        }
                        Message msg = new Message();
                        msg.what = LOADED;
                        msg.arg1 = status;
                        handler.sendMessage(msg);
                        int count = Integer.valueOf(root.get("count").toString());
                        int pages = Integer.valueOf(root.get("pages").toString());
                        if (listItem.size() >= count * pages)
                        {
                            loadedAll = true;
                        }
                    } catch (Exception ex)
                    {
                        Log.e("Debug Info", ex.getMessage());
                    }
                } catch (Exception ex)
                {
                }
            }
        }.start();
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
                view = inflater.inflate(R.layout.home_item_list, null);
            }

            TextView title = (TextView) view.findViewById(R.id.title);
            TextView articleViews = (TextView) view.findViewById(R.id.views);
            ImageView cover = (ImageView) view.findViewById(R.id.cover);

            final JSONObject item = listItem.get(position);
            try
            {
                title.setText(item.get("title").toString());
                JSONObject customFields = item.getJSONObject("custom_fields");
                articleViews.setText(customFields.getJSONArray("views").get(0).toString());

                DisplayImageOptions options=new DisplayImageOptions.Builder()
                        .showImageOnLoading(R.drawable.icon_loading_item)
                        .showImageForEmptyUri(R.drawable.icon_loading_item)
                        .showImageOnFail(R.drawable.icon_loading_item)
                        .resetViewBeforeLoading(true)//default 设置图片在加载前是否重置、复位
                        .cacheInMemory(true) // default  设置下载的图片是否缓存在内存中
                        .cacheOnDisk(true) // default  设置下载的图片是否缓存在SD卡中
                        .bitmapConfig(Bitmap.Config.RGB_565)
                        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                        .build();
                ImageLoader.getInstance().displayImage(URLDecoder.decode(item.get("thumbnail").toString(), "utf-8"), cover,options);

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
                loadMoreData(STATUS_REFRESH);
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
