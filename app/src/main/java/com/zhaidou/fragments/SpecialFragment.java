package com.zhaidou.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.zhaidou.model.Category;
import com.zhaidou.model.SwitchImage;
import com.zhaidou.utils.AsyncImageLoader1;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SpecialFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SpecialFragment extends BaseFragment implements PullToRefreshBase.OnRefreshListener2<ListView>{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_CATEGORY = "category";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private Category mCategory;

    private WeakHashMap<Integer,View> mHashMap = new WeakHashMap<Integer, View>();
    private AsyncImageLoader1 imageLoader;
    private PullToRefreshListView listView;
    private int currentPage=1;
    private int count=-1;

    private static final int UPDATE_HOMELIST=3;
    private RequestQueue mRequestQueue;
    private List<Article> articleList = new ArrayList<Article>();

    private HomeAdapter mHomeAdapter;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            mHomeAdapter.notifyDataSetChanged();
        }
    };
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param category Parameter 2.
     * @return A new instance of fragment SpecialFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SpecialFragment newInstance(String param1, Category category) {
        SpecialFragment fragment = new SpecialFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putSerializable(ARG_CATEGORY,category);
        fragment.setArguments(args);
        return fragment;
    }
    public SpecialFragment() {
        // Required empty public constructor
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
        View view=inflater.inflate(R.layout.fragment_special, container, false);
        listView=(PullToRefreshListView)view.findViewById(R.id.lv_special_list);
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        listView.setOnRefreshListener(this);
        listView.setEmptyView(mEmptyView);
        mHomeAdapter = new HomeAdapter(getActivity(),articleList);
        listView.setAdapter(mHomeAdapter);
        mRequestQueue = Volley.newRequestQueue(getActivity());
        FetchData(currentPage=1,mCategory);

        mHomeAdapter.setOnInViewClickListener(R.id.rl_fragment_strategy,new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                Article article=(Article)values;
                Intent detailIntent = new Intent(getActivity(), ItemDetailActivity.class);
                detailIntent.putExtra("id", article.getId() + "");
                detailIntent.putExtra("from", "product");
                detailIntent.putExtra("title", article.getTitle());
                detailIntent.putExtra("cover_url", article.getImg_url());
                detailIntent.putExtra("url",ZhaiDou.ARTICLE_DETAIL_URL+article.getId());
                startActivity(detailIntent);
            }
        });
        return view;
    }

    private void FetchData(final int page,Category category){
        String categoryId=(category==null?"":category.getId()+"");
//        categoryId=14+"";
        String url;
        url= ZhaiDou.HOME_CATEGORY_URL+page+((category==null)?"&catetory_id":"&catetory_id="+categoryId);

        JsonObjectRequest jr = new JsonObjectRequest(url,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("FetchData--->data---->",response.toString());
                if (page==1)
                    articleList.clear();
                listView.onRefreshComplete();
                JSONArray articles = response.optJSONArray("articles");
                JSONObject meta = response.optJSONObject("meta");
                count=meta==null?0:meta.optInt("count");
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
                Log.i("articleList----------------->",articleList.size()+"");

                Message message = new Message();
                message.what=UPDATE_HOMELIST;
                handler.sendMessage(message);
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Log.i("onErrorResponse------->",error.getMessage());
            }
        });
        mRequestQueue.add(jr);
    }

    public class HomeAdapter extends BaseListAdapter<Article> {
        public HomeAdapter(Context context, List<Article> list) {
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

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
        refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
        Log.i("onPullDownToRefresh--->", "onPullDownToRefresh");
//        articleList.clear();
        FetchData(currentPage = 1, mCategory);
        listView.setMode(PullToRefreshBase.Mode.BOTH);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        if (count!=-1&&mHomeAdapter.getCount()==count){
            Toast.makeText(getActivity(),"已经加载完毕",Toast.LENGTH_SHORT).show();
            listView.onRefreshComplete();
            listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            return;
        }
        FetchData(++currentPage, mCategory);
    }
}
