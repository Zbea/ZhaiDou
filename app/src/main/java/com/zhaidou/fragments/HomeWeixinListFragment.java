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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshListView;
import com.pulltorefresh.PullToRefreshScrollView;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.ArticleWebViewActivity;
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Article;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
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
 * 微信文章列表
 */
public class HomeWeixinListFragment extends BaseFragment
{
    private static final String ARG_PARAM = "param";
    private static final String ARG_STRING = "string";

    private String mParam;
    private String mString;
    private View view;
    private TypeFaceTextView titleTv;

    private WeakHashMap<Integer, View> mHashMap = new WeakHashMap<Integer, View>();
    private PullToRefreshScrollView scrollView;
    private ListViewForScrollView listView;
    private int currentPage = 1;
    private int pageSize;
    private int pageCount;

    private Dialog mDialog;
    private Context mContext;

    private static final int UPDATE_HOMELIST = 1;
    private RequestQueue mRequestQueue;
    private List<Article> articleList = new ArrayList<Article>();

    private HomeAdapter mHomeAdapter;

    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case UPDATE_HOMELIST:
                    mHomeAdapter.setList(articleList);
                    mHomeAdapter.notifyDataSetChanged();
                    if (pageCount > articleList.size())
                    {
                        scrollView.setMode(PullToRefreshBase.Mode.BOTH);
                    } else
                    {
                        scrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    }
                    break;
            }
        }
    };

    private PullToRefreshBase.OnRefreshListener2 onRefreshListener=new PullToRefreshBase.OnRefreshListener2()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView)
        {
            currentPage = 1;
            FetchData();
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {
            ++currentPage;
            FetchData();
        }
    };

    public static HomeWeixinListFragment newInstance(String param, String string)
    {
        HomeWeixinListFragment fragment = new HomeWeixinListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM, param);
        args.putString(ARG_STRING, string);
        fragment.setArguments(args);
        return fragment;
    }

    public HomeWeixinListFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam = getArguments().getString(ARG_PARAM);
            mString = getArguments().getString(ARG_STRING);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        if (view == null)
        {
            view =  inflater.inflate(R.layout.fragment_home_article_list, container, false);
            mContext = getActivity();
            initView();
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null)
        {
            parent.removeView(view);
        }
        return view;
    }

    private void initView()
    {
        titleTv = (TypeFaceTextView) view.findViewById(R.id.tv_title);
        titleTv.setText(mParam);

        scrollView=(PullToRefreshScrollView)view.findViewById(R.id.scrollView);
        scrollView.setMode(PullToRefreshBase.Mode.BOTH);
        scrollView.setOnRefreshListener(onRefreshListener);
        listView=(ListViewForScrollView)view.findViewById(R.id.lv_special_list);
        mHomeAdapter = new HomeAdapter(mContext,articleList);
        listView.setAdapter(mHomeAdapter);

        mRequestQueue = Volley.newRequestQueue(mContext);

        if (NetworkUtils.isNetworkAvailable(mContext))
        {
            mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
            FetchData();
        } else
        {
            Toast.makeText(mContext, "抱歉,网络链接失败", Toast.LENGTH_SHORT).show();
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Article article = articleList.get(position);
                if ("true".equalsIgnoreCase(article.getIs_new()))
                {
                    SharedPreferencesUtil.saveData(mContext, "WeixinList_" + article.getId(), false);
                    parent.findViewById(R.id.newsView).setVisibility(View.GONE);
                }

                Intent detailIntent = new Intent(getActivity(), ArticleWebViewActivity.class);
                detailIntent.putExtra("id", article.getId());
                detailIntent.putExtra("title", article.getTitle());
                detailIntent.putExtra("imageUrl", article.getImg_url());
                detailIntent.putExtra("url", article.getIs_new());
                detailIntent.putExtra("show_share", true);
                detailIntent.putExtra("show_title", false);
                startActivity(detailIntent);
            }
        });

    }

    private void FetchData()
    {
        JsonObjectRequest jr = new JsonObjectRequest(ZhaiDou.HomeWeixinListUrl + currentPage, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                if (mDialog != null)
                {
                    mDialog.dismiss();
                }
                scrollView.onRefreshComplete();
                if (currentPage == 1)
                    articleList.clear();
                if (response != null)
                {
                    ToolUtils.setLog(response.toString());
                    int code = response.optInt("code");
                    JSONObject dataObject = response.optJSONObject("data");
                    if (code == 200)
                    {
                        pageSize = dataObject.optInt("pageSize");
                        pageCount = dataObject.optInt("totalCount");
                        JSONArray articles = dataObject.optJSONArray("articleListPOs");
                        if (articles != null)
                        {
                            for (int i = 0; i < articles.length(); i++)
                            {
                                JSONObject article = articles.optJSONObject(i);
                                int id = article.optInt("id");
                                String title = article.optString("title");
                                String img_url = article.optString("imgUrl");
                                String articleUrl = article.optString("articleUrl");
                                Article item = new Article(id, title, img_url, articleUrl, 0);
                                articleList.add(item);
                            }
                            handler.sendEmptyMessage(UPDATE_HOMELIST);
                        }
                    } else
                    {
                        if (currentPage > 1)
                            currentPage--;
                        ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                    }
                }
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
                scrollView.onRefreshComplete();
                if (currentPage > 1)
                {
                    currentPage--;
                }
                ToolUtils.setToast(mContext, R.string.loading_fail_txt);
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

    public class HomeAdapter extends BaseListAdapter<Article>
    {
        Context context;

        public HomeAdapter(Context context, List<Article> list)
        {
            super(context, list);
            this.context = context;
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent)
        {
            convertView = mHashMap.get(position);

            if (convertView == null)
                convertView = mInflater.inflate(R.layout.home_item_list, null);

            TextView title = ViewHolder.get(convertView, R.id.title);
            LinearLayout articleViews = ViewHolder.get(convertView, R.id.relativeLayout);
            ImageView cover = ViewHolder.get(convertView, R.id.cover);
            cover.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth,screenWidth*316/722));
            ImageView newView = ViewHolder.get(convertView, R.id.newsView);

            Article article = getList().get(position);

            title.setText(article.getTitle());
            articleViews.setVisibility(View.GONE);
            ToolUtils.setImageCacheUrl(article.getImg_url(), cover);

            if (article.getIs_new().equals("true"))
            {
                if (!(Boolean) SharedPreferencesUtil.getData(mContext, "WeixinList_" + article.getId(), true))
                {
                    newView.setVisibility(View.GONE);
                } else
                {
                    newView.setVisibility(View.VISIBLE);
                }
            } else
            {
                newView.setVisibility(View.GONE);
            }

            mHashMap.put(position, convertView);
            return convertView;
        }
    }

}
