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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshScrollView;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Article;
import com.zhaidou.model.Category;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.ListViewForScrollView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * 文章列表（最实用）
 */
public class HomeArticleListFragment extends BaseFragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_CATEGORY = "category";

    private String mParam1;
    private Category mCategory;

    private WeakHashMap<Integer,View> mHashMap = new WeakHashMap<Integer, View>();
    private PullToRefreshScrollView scrollView;
    private ListViewForScrollView listView;
    private int currentPage=1;

    private Dialog mDialog;
    private Context mContext;

    private static final int UPDATE_HOMELIST=3;
    private RequestQueue mRequestQueue;
    private List<Article> articleList = new ArrayList<Article>();

    private HomeAdapter mHomeAdapter;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (mDialog!=null)
            {
                mDialog.dismiss();
            }
            mHomeAdapter.notifyDataSetChanged();
            scrollView.onRefreshComplete();
            if (articleList.size()<currentPage*10)
            {
                scrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            }
            else
            {
                scrollView.setMode(PullToRefreshBase.Mode.BOTH);
            }

        }
    };

    private PullToRefreshBase.OnRefreshListener2 onRefreshListener=new PullToRefreshBase.OnRefreshListener2()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView)
        {
            FetchData(currentPage = 1, mCategory);
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {
            FetchData(++currentPage, mCategory);
        }
    };

    public static HomeArticleListFragment newInstance(String param1, Category category) {
        HomeArticleListFragment fragment = new HomeArticleListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putSerializable(ARG_CATEGORY,category);
        fragment.setArguments(args);
        return fragment;
    }
    public HomeArticleListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mCategory = (Category)getArguments().getSerializable(ARG_CATEGORY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_home_article_list, container, false);

        mContext=getActivity();

        scrollView=(PullToRefreshScrollView)view.findViewById(R.id.scrollView);
        scrollView.setMode(PullToRefreshBase.Mode.BOTH);
        scrollView.setOnRefreshListener(onRefreshListener);
        listView=(ListViewForScrollView)view.findViewById(R.id.lv_special_list);
        mHomeAdapter = new HomeAdapter(getActivity(),articleList);
        listView.setAdapter(mHomeAdapter);

        mRequestQueue = ZDApplication.newRequestQueue();


        if (NetworkUtils.isNetworkAvailable(getActivity()))
        {
            mDialog= CustomLoadingDialog.setLoadingDialog(getActivity(),"loading");
            FetchData(currentPage=1,mCategory);
        }
        else
        {
            Toast.makeText(getActivity(),"抱歉,网络链接失败",Toast.LENGTH_SHORT).show();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Article article=articleList.get(position);
                if ("true".equalsIgnoreCase(article.getIs_new())){
                    SharedPreferencesUtil.saveData(mContext,"is_new_"+article.getId(),false);
                    parent.findViewById(R.id.newsView).setVisibility(View.GONE);
                }
                Intent detailIntent = new Intent(getActivity(), ItemDetailActivity.class);
                detailIntent.putExtra("article", article);
                detailIntent.putExtra("id", article.getId() + "");
                detailIntent.putExtra("from", "product");
                detailIntent.putExtra("title", article.getTitle());
                detailIntent.putExtra("cover_url", article.getImg_url());
                detailIntent.putExtra("show_header", true);
                detailIntent.putExtra("url",ZhaiDou.ARTICLE_DETAIL_URL+article.getId());
                startActivity(detailIntent);
            }
        });
        return view;
    }

    private void FetchData(final int page,Category category){
        String categoryId=(category==null?"":category.getId()+"");
        String url= ZhaiDou.HOME_CATEGORY_URL+page+((category==null)?"&catetory_id":"&catetory_id="+categoryId);
        ToolUtils.setLog(url);
        ZhaiDouRequest jr = new ZhaiDouRequest(url,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (page==1)
                    articleList.clear();
                scrollView.onRefreshComplete();
                JSONArray articles = response.optJSONArray("articles");
                JSONObject meta = response.optJSONObject("meta");
//                count=meta==null?0:meta.optInt("count");
                if (articles==null||articles.length()<=0){
                    articleList.clear();
                    handler.sendEmptyMessage(UPDATE_HOMELIST);
                    return;
                }
                for (int i=0;i<articles.length();i++){
                    JSONObject article =articles.optJSONObject(i);
                    int id =article.optInt("id");
                    String title=article.optString("title");
                    String img_url=article.optString("img_url");
                    String is_new=article.optString("is_new");
                    int reviews = article.optInt("reviews");
                    Article item =new Article(id,title,img_url,is_new,reviews);
                    articleList.add(item);
                }

                Message message = new Message();
                message.what=UPDATE_HOMELIST;
                handler.sendMessage(message);
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Log.i("onErrorResponse------->",error.getMessage());
                if (mDialog!=null)
                {
                    mDialog.dismiss();
                }
            }
        });
        mRequestQueue.add(jr);
    }

    public class HomeAdapter extends BaseListAdapter<Article> {
        Context context;
        public HomeAdapter(Context context, List<Article> list) {
            super(context, list);
            this.context=context;
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            convertView = mHashMap.get(position);

            if (convertView==null)
                convertView=mInflater.inflate(R.layout.item_strategy_list,null);

            TextView title = ViewHolder.get(convertView, R.id.title);
            TextView articleViews = ViewHolder.get(convertView,R.id.views);
            ImageView cover = ViewHolder.get(convertView,R.id.cover);
            ImageView newView = ViewHolder.get(convertView,R.id.newsView);

            Article article = getList().get(position);

            title.setText(article.getTitle());
            articleViews.setText(article.getReviews()+"");
            ToolUtils.setImageCacheUrl(article.getImg_url(), cover);

            if (article.getIs_new().equals("true"))
            {
                if (!(Boolean)SharedPreferencesUtil.getData(mContext,"is_new_"+article.getId(),true))
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

            mHashMap.put(position,convertView);
            return convertView;
        }
    }
}
