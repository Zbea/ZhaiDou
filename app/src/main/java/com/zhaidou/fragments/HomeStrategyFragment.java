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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.adapter.HomeListAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Article;
import com.zhaidou.model.Category;
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


/**
 * Created by roy on 15/10/19.
 * 最实用攻略
 */
public class HomeStrategyFragment extends BaseFragment
{
    private static final String PAGE = "page";
    private static final String INDEX = "index";

    private String mPage;
    private int mIndex;
    private View mView;
    private Context mContext;
    private int currentPage = 1;
    private Dialog mDialog;
    private LinearLayout loadingView, nullNetView, nullView;
    private TextView reloadBtn, reloadNetBtn;
    private RequestQueue mRequestQueue;
    private static FrameLayout fl_category_menu;
    private static ImageView mCategoryView, mDotView;

    private TypeFaceTextView  titleTv;
    private PullToRefreshScrollView mScrollView;
    private ListViewForScrollView mListView;
    private boolean loadedAll = false;
    private int count = -1;
    private static final int UPDATE_HOMELIST = 1;
    private List<Article> articleList = new ArrayList<Article>();
    public HomeCategoryFragment homeCategoryFragment;
    private Category mCategory;
    public HomeListAdapter mListAdapter;


    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if (msg.what == UPDATE_HOMELIST)
            {
                loadingView.setVisibility(View.GONE);
                if (mListAdapter == null)
                {
                    mListAdapter = new HomeListAdapter(mContext, articleList, screenWidth);
                    mListView.setAdapter(mListAdapter);
                }
                mScrollView.onRefreshComplete();
                mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
            }
            if (mListAdapter != null)
                mListAdapter.notifyDataSetChanged();
        }
    };

    /**
     * 下拉刷新
     */
    private PullToRefreshBase.OnRefreshListener2 refreshListener = new PullToRefreshBase.OnRefreshListener2()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView)
        {
            articleList.clear();
            FetchData(currentPage = 1, mCategory);
            mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {
            if (count != -1 && articleList.size() == count)
            {
                Toast.makeText(mContext, "已经加载完毕", Toast.LENGTH_SHORT).show();
                mScrollView.onRefreshComplete();
                mScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                return;
            }
            FetchData(++currentPage, mCategory);

        }
    };

    private HomeCategoryFragment.CategorySelectedListener categorySelectedListener = new HomeCategoryFragment.CategorySelectedListener()
    {
        @Override
        public void onCategorySelected(Category category)
        {
            mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
            articleList.removeAll(articleList);
            FetchData(currentPage = 1, mCategory = category);
            if (category != null)
            {
                mDotView.setVisibility(View.VISIBLE);
            } else
            {
                mDotView.setVisibility(View.GONE);
            }
            toggleMenu();
        }
    };

    /**
     * adapter短点击事件
     */
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
        {
            Article article = articleList.get(position);
            Intent detailIntent = new Intent(getActivity(), ItemDetailActivity.class);
            detailIntent.putExtra("article", article);
            detailIntent.putExtra("id", article.getId() + "");
            detailIntent.putExtra("from", "product");
            detailIntent.putExtra("title", article.getTitle());
            detailIntent.putExtra("cover_url", article.getImg_url());
            detailIntent.putExtra("show_header", true);
            detailIntent.putExtra("url", ZhaiDou.ARTICLE_DETAIL_URL + article.getId());
            startActivityForResult(detailIntent, 100);
            if ("true".equalsIgnoreCase(article.getIs_new()))
            {
                SharedPreferencesUtil.saveData(mContext, "is_new_" + article.getId(), false);
                view.findViewById(R.id.newsView).setVisibility(View.GONE);
            }
        }
    };

    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            switch (view.getId())
            {
                case R.id.ll_category_view:
                    toggleMenu();
                    break;
                case R.id.nullReload:
                    initData();
                    break;
                case R.id.netReload:
                    initData();
                    break;
            }
        }
    };

    public static HomeStrategyFragment newInstance(String page, int index)
    {
        HomeStrategyFragment fragment = new HomeStrategyFragment();
        Bundle args = new Bundle();
        args.putString(PAGE, page);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    public HomeStrategyFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mPage = getArguments().getString(PAGE);
            mIndex = getArguments().getInt(INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        mContext = getActivity();
        if (mView == null)
        {
            mView = inflater.inflate(R.layout.fragment_home_strategy_list, container, false);
            initView();
            initData();
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) mView.getParent();
        if (parent != null)
        {
            parent.removeView(mView);
        }
        return mView;
    }

    /**
     * 初始化数据
     */
    private void initView()
    {
        loadingView = (LinearLayout) mView.findViewById(R.id.loadingView);
        nullNetView = (LinearLayout) mView.findViewById(R.id.nullNetline);
        nullView = (LinearLayout) mView.findViewById(R.id.nullline);
        reloadBtn = (TextView) mView.findViewById(R.id.nullReload);
        reloadBtn.setOnClickListener(onClickListener);
        reloadNetBtn = (TextView) mView.findViewById(R.id.netReload);
        reloadNetBtn.setOnClickListener(onClickListener);

        titleTv = (TypeFaceTextView) mView.findViewById(R.id.title_tv);
        titleTv.setText(R.string.title_home_strategy);

        fl_category_menu = (FrameLayout) mView.findViewById(R.id.fl_category_menu);
        mCategoryView = (ImageView) mView.findViewById(R.id.iv_category);
        mDotView = (ImageView) mView.findViewById(R.id.iv_dot);
        mView.findViewById(R.id.ll_category_view).setOnClickListener(onClickListener);

        mScrollView = (PullToRefreshScrollView) mView.findViewById(R.id.sv_special_scrollview);
        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mScrollView.setOnRefreshListener(refreshListener);

        mListView = (ListViewForScrollView) mView.findViewById(R.id.shopListView);
        mListView.setOnItemClickListener(onItemClickListener);

        if (homeCategoryFragment == null)
        {
            homeCategoryFragment = HomeCategoryFragment.newInstance("", "");
            getChildFragmentManager().beginTransaction().add(R.id.fl_category_menu, homeCategoryFragment
                    , HomeCategoryFragment.TAG).hide(homeCategoryFragment).addToBackStack(null).commit();
            homeCategoryFragment.setCategorySelectedListener(categorySelectedListener);
        }

        currentPage = 1;
        loadedAll = false;
        mRequestQueue = Volley.newRequestQueue(mContext);

    }


    /**
     * 初始化数据
     */
    private void initData()
    {
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
        if (NetworkUtils.isNetworkAvailable(mContext))
        {
            FetchData(currentPage, null);
        } else
        {
            if (mDialog != null)
                mDialog.dismiss();
            nullView.setVisibility(View.GONE);
            nullNetView.setVisibility(View.VISIBLE);
        }

    }

    /**
     * 切换
     */
    public void toggleMenu()
    {
        if (homeCategoryFragment.isHidden())
        {
            mCategoryView.setImageResource(R.drawable.icon_close);
            fl_category_menu.setVisibility(View.VISIBLE);

            getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim
                    .slide_in_from_top, R.anim.slide_out_to_top, R.anim.slide_in_from_bottom, R.anim.slide_out_to_bottom).show(homeCategoryFragment).commit();
        } else
        {
            mCategoryView.setImageResource(R.drawable.icon_category);
            fl_category_menu.setVisibility(View.GONE);
            getChildFragmentManager().beginTransaction().hide(homeCategoryFragment).commit();
        }
        homeCategoryFragment.notifyDataSetChanged();
    }


    private void FetchData(int page, Category category)
    {
        currentPage = page;
        if (page == 1)
        {
            loadedAll = false;
        }
        if (loadedAll)
        {
            Toast.makeText(getActivity(), "已经加载完毕了", Toast.LENGTH_SHORT).show();
            mScrollView.onRefreshComplete();
            return;
        }
        String categoryId = (category == null ? "" : category.getId() + "");
        final String url;
        url = ZhaiDou.HOME_CATEGORY_URL + page + ((category == null) ? "&catetory_id" : "&catetory_id=" + categoryId);
        JsonObjectRequest jr = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                JSONArray articles = response.optJSONArray("articles");
                JSONObject meta = response.optJSONObject("meta");
                count = meta == null ? 0 : meta.optInt("count");
                if (articles == null || articles.length() <= 0)
                {
                    Toast.makeText(mContext, "抱歉,暂无精选", Toast.LENGTH_SHORT).show();
                    handler.sendEmptyMessage(UPDATE_HOMELIST);
                    return;
                }
                for (int i = 0; i < articles.length(); i++)
                {
                    JSONObject article = articles.optJSONObject(i);
                    int id = article.optInt("id");
                    String title = article.optString("title");
                    String img_url = article.optString("img_url");
                    String is_new = article.optString("is_new");
                    int reviews = article.optInt("reviews");
                    Article item = new Article(id, title, img_url, is_new, reviews);
                    articleList.add(item);
                }
                Message message = new Message();
                message.what = UPDATE_HOMELIST;
                handler.sendMessage(message);
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {

                if (mDialog.isShowing())
                {
                    mDialog.dismiss();
                }
                if (articleList.size()!=0)
                {
                    ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                }
                else
                {
                    nullView.setVisibility(View.VISIBLE);
                    nullNetView.setVisibility(View.GONE);
                    mScrollView.onRefreshComplete();
                    mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
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


    public void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_home_strategy));
    }

    public void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_home_strategy));
    }

    /**
     * 处理切换图标变换
     */
    public static void getHomeCategory() {
        mCategoryView.setImageResource(R.drawable.icon_category);
        fl_category_menu.setVisibility(View.GONE);
    }

}
