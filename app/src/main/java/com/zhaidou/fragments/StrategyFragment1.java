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
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.Article;
import com.zhaidou.model.Product;
import com.zhaidou.utils.AsyncImageLoader1;
import com.zhaidou.view.XListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;


public class StrategyFragment1 extends BaseFragment {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private static final String LIVING_ROOM_TAG = "1";
    private static final String ENTIRE_PART_TAG = "2";

    private XListView listView;
    private RequestQueue mRequestQueue;

    private List<Article> articleList = new ArrayList<Article>();
    private AsyncImageLoader1 imageLoader;
    private StrategyAdapter strategyAdapter;
    private WeakHashMap<Integer,View> mHashMap = new WeakHashMap<Integer, View>();

    private static final int UPDATE_Adapter=3;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_Adapter:
                    strategyAdapter.setList(articleList);
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

        listView=(XListView)view.findViewById(R.id.listview);
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
                Log.i("position------------>",position+"");
                Log.i("values--------------->",values.toString());
                Article article=(Article)values;
                Intent detailIntent = new Intent(getActivity(), ItemDetailActivity.class);
                detailIntent.putExtra("id", article.getId()+"");
                detailIntent.putExtra("title", article.getTitle());
                detailIntent.putExtra("cover_url", article.getImg_url());
                detailIntent.putExtra("url","http://192.168.1.45/article/articles/"+article.getId());
                startActivity(detailIntent);
            }
        });
        return view;
    }

    public void FetchData(String msg,int sort,int page){
        Log.i("page-------------------->",page+"");
        Log.i("articleList------------->",articleList==null?"null":articleList.toString());
        if (page==1) articleList.clear();
        Log.i("FetchData------>","FetchData");
        Map<String, String> params = new HashMap<String, String>();
        params.put("reviews", "desc");
        params.put("per_page", "10");
        params.put("search", msg);
        params.put("page", page+"");
        if (sort==1){
            params.put("hot_d","desc");
        }else if (sort==2){
            params.put("price","asc");
        }else if (sort==3){
            params.put("price","desc");
        }


        JsonObjectRequest newMissRequest = new JsonObjectRequest(
                Request.Method.POST, "http://192.168.1.45/article/api/articles/search",
                new JSONObject(params), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject json) {
                Log.i("StrategyFragment1----->",json.toString());
                JSONArray articles = json.optJSONArray("articles");
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
            imageLoader=new AsyncImageLoader1(context);
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
            imageLoader.LoadImage(article.getImg_url(),cover);

            mHashMap.put(position,convertView);
            return convertView;
        }
    }

    public void FetchCategoryData(String id,int sort,int page){
        Log.i("FetchCategoryData--------->","FetchCategoryData");
//        if (page==1) products.clear();
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("price", "desc");
//        params.put("hot_d", "desc");
//        params.put("search", msg);
//        params.put("page",page+"");
//        if (sort==1){
//            params.put("hot_d","desc");
//        }else if (sort==2){
//            params.put("price","asc");
//        }else if (sort==3){
//            params.put("price","desc");
//        }

        String url="http://192.168.1.45/article/api/articles?catetory_id="+id;
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
}
