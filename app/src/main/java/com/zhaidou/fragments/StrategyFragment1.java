package com.zhaidou.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshListView;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.Article;
import com.zhaidou.model.Product;
import com.zhaidou.utils.AsyncImageLoader1;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.XListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;


public class StrategyFragment1 extends BaseFragment implements PullToRefreshBase.OnRefreshListener2<ListView>{


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private static final String LIVING_ROOM_TAG = "1";
    private static final String ENTIRE_PART_TAG = "2";

    private PullToRefreshListView listView;
    private RequestQueue mRequestQueue;

    private int currentpage=1;
    private int sort;
    private int count;

    private List<Article> articleList = new ArrayList<Article>();
    private StrategyAdapter strategyAdapter;
    private WeakHashMap<Integer,View> mHashMap = new WeakHashMap<Integer, View>();

    private static final int UPDATE_Adapter=3;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_Adapter:
                    strategyAdapter.setList(articleList);
                    listView.onRefreshComplete();
                    break;
                default:
                    break;
            }
        }
    };
    public static StrategyFragment1 newInstance(String param1, String param2) {
        Log.i("StrategyFragment1----------->","newInstance");
        StrategyFragment1 fragment = new StrategyFragment1();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public StrategyFragment1() {
        // Required empty public constructor
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

        View view = inflater.inflate(R.layout.fragment_strategy1, container, false);

        listView=(PullToRefreshListView)view.findViewById(R.id.listview);
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        listView.setOnRefreshListener(this);
        listView.setEmptyView(mEmptyView);
        mRequestQueue= Volley.newRequestQueue(getActivity());
        strategyAdapter=new StrategyAdapter(getActivity(),articleList);
        listView.setAdapter(strategyAdapter);
        if ("category".equalsIgnoreCase(mParam2)){
            FetchCategoryData(mParam1,0,1);
        }else {
            FetchData(mParam1,0,1);
        }

        strategyAdapter.setOnInViewClickListener(R.id.rl_fragment_strategy,new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                Article article=(Article)values;
                Intent detailIntent = new Intent(getActivity(), ItemDetailActivity.class);
                detailIntent.putExtra("article", article);
                detailIntent.putExtra("id", article.getId()+"");
                detailIntent.putExtra("title", article.getTitle());
                detailIntent.putExtra("cover_url", article.getImg_url());
                detailIntent.putExtra("from", "product");
                detailIntent.putExtra("url",ZhaiDou.ARTICLE_DETAIL_URL+article.getId());
                startActivity(detailIntent);
            }
        });
        return view;
    }

    public void FetchData(String msg,int sort,int page){
        Log.i("sort------------------>",sort+"");
        this.sort=sort;
        currentpage=page;
        if (page==1) articleList.clear();
        Map<String, String> params = new HashMap<String, String>();
        params.put("per_page", "10");
        params.put("search", msg);
        params.put("page", page+"");
        if (sort==1){
            params.put("reviews", "desc");
        }else if (sort==2){
            params.put("price","asc");
        }else if (sort==3){
            params.put("price","desc");
        }


        JsonObjectRequest newMissRequest = new JsonObjectRequest(
                Request.Method.POST, ZhaiDou.SEARCH_ARTICLES_URL,
                new JSONObject(params), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject json) {
                Log.i("FetchData--------------->",json.toString());
                listView.onRefreshComplete();
                JSONArray articles = json.optJSONArray("articles");
                JSONObject meta = json.optJSONObject("meta");
                int size = meta.optInt("size");
                count=meta==null?0:meta.optInt("count");
                if (count<size)
                    listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                if (articles==null) return;
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
                handler.sendEmptyMessage(UPDATE_Adapter);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("onErrorResponse",error.toString());
            }
        });
        if (mRequestQueue==null) mRequestQueue=Volley.newRequestQueue(getActivity());
        mRequestQueue.add(newMissRequest);
    }

    public class StrategyAdapter extends BaseListAdapter<Article> {
        public StrategyAdapter(Context context, List<Article> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            convertView = mHashMap.get(position);
            if (convertView==null)
                convertView=mInflater.inflate(R.layout.home_item_list,null);

            TextView title = ViewHolder.get(convertView, R.id.title);
            TextView articleViews = ViewHolder.get(convertView,R.id.views);
            ImageView cover = ViewHolder.get(convertView,R.id.cover);

            Article article = getList().get(position);

            title.setText(article.getTitle());
            articleViews.setText(article.getReviews()+"");
            ToolUtils.setImageCacheUrl(article.getImg_url(),cover);

            mHashMap.put(position,convertView);
            return convertView;
        }
    }

    public void FetchCategoryData(String id,int sort,int page){
        String url=ZhaiDou.ARTICLES_WITH_CATEGORY+id;
        JsonObjectRequest fetchCategoryTask = new JsonObjectRequest(url,new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.i("FetchCategoryData->onResponse",jsonObject.toString());
                JSONArray articles = jsonObject.optJSONArray("articles");
                if (articles==null) return;
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
                handler.sendEmptyMessage(UPDATE_Adapter);
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        if (mRequestQueue==null) mRequestQueue=Volley.newRequestQueue(getActivity());
        mRequestQueue.add(fetchCategoryTask);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
        refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
        Log.i("onPullDownToRefresh--->","onPullDownToRefresh");
        FetchData(mParam1,sort,currentpage=1);
        listView.setMode(PullToRefreshBase.Mode.BOTH);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        Log.i("onPullUpToRefresh---->","onPullUpToRefresh");
        listView.onRefreshComplete();
        FetchData(mParam1,sort,++currentpage);
        if (count!=-1&&strategyAdapter.getCount()==count){
            Toast.makeText(getActivity(), "已经加载完毕", Toast.LENGTH_SHORT).show();
            listView.onRefreshComplete();
            listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        }
    }
}
