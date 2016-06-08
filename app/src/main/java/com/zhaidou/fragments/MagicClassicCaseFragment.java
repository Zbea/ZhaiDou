package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshScrollView;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Article;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 软装图例
 */
public class MagicClassicCaseFragment extends BaseFragment
{
    private static final String ARG_PARAM = "param";
    private static final String ARG_STRING = "string";

    private String mParam;
    private String mString;
    private View view;

    private WeakHashMap<Integer, View> mHashMap = new WeakHashMap<Integer, View>();
    private int currentPage = 1;
    private int pageSize;
    private int pageCount;

    private Dialog mDialog;
    private Context mContext;
    private ListView listView;
    private PullToRefreshScrollView scrollView;
    private TextView titleTv;

    private static final int UPDATE_HOMELIST = 1;
    private List<Article> articleList = new ArrayList<Article>();
    private ArticleAdapter mHomeAdapter;


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
            currentPage=1;
            articleList.clear();
            FetchData();
        }
        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {
            currentPage++;
            FetchData();
        }
    };


    public static MagicClassicCaseFragment newInstance(String param, String string)
    {
        MagicClassicCaseFragment fragment = new MagicClassicCaseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM, param);
        args.putString(ARG_STRING, string);
        fragment.setArguments(args);
        return fragment;
    }

    public MagicClassicCaseFragment()
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
            view = inflater.inflate(R.layout.fragment_magic_classic_case, container, false);
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
        titleTv = (TypeFaceTextView) view.findViewById(R.id.title_tv);
        titleTv.setText(R.string.title_magic_class_case);

        scrollView=(PullToRefreshScrollView)view.findViewById(R.id.scrollView);
        scrollView.setMode(PullToRefreshBase.Mode.BOTH);
        scrollView.setOnRefreshListener(onRefreshListener);
        listView=(ListView)view.findViewById(R.id.lv_special_list);
        mHomeAdapter = new ArticleAdapter(mContext,articleList);
        listView.setAdapter(mHomeAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                HomeArticleGoodsDetailsFragment homeArticleGoodsDetailsFragment=HomeArticleGoodsDetailsFragment.newInstance("",""+articleList.get(position).getId());
                ((MainActivity)mContext).navigationToFragment(homeArticleGoodsDetailsFragment);
            }
        });


        if (NetworkUtils.isNetworkAvailable(mContext))
        {
            mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
            FetchData();
        } else
        {
            Toast.makeText(mContext, "抱歉,网络链接失败", Toast.LENGTH_SHORT).show();
        }


    }


    /**
     * 加载列表数据
     */
    private void FetchData()
    {
        final String url = ZhaiDou.HomeArticleGoodsUrl + currentPage;
        ToolUtils.setLog(url);
        JsonObjectRequest jr = new JsonObjectRequest(url ,new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                if (response == null)
                {
                    scrollView.onRefreshComplete();
                    return;
                }
                ToolUtils.setLog(response.toString());
                int code = response.optInt("code");
                if (code == 500)
                {
                    if (mDialog != null)
                        mDialog.dismiss();
                    scrollView.onRefreshComplete();
                    scrollView.setMode(PullToRefreshBase.Mode.BOTH);
                    return;
                }
                JSONObject jsonObject = response.optJSONObject("data");
                if (jsonObject != null)
                {
                    pageCount = jsonObject.optInt("totalCount");
                    pageSize = jsonObject.optInt("pageSize");
                    JSONArray jsonArray = jsonObject.optJSONArray("freeClassicsCasePOs");

                    if (jsonArray != null)
                        for (int i = 0; i < jsonArray.length(); i++)
                        {
                            JSONObject obj = jsonArray.optJSONObject(i);
                            int id = obj.optInt("id");
                            String title = obj.optString("caseName");
                            String info = obj.optString("mainDesc");
                            String imageUrl = obj.optString("mainPic");
                            int num = obj.optInt("commentCount");
                            Article article=new Article(id,title,imageUrl,"",num,info);

                            articleList.add(article);
                        }
                    Message message = new Message();
                    message.what = UPDATE_HOMELIST;
                    handler.sendMessage(message);
                }

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                scrollView.onRefreshComplete();
                if (mDialog != null)
                    mDialog.dismiss();
                if (articleList.size() != 0)
                {
                    currentPage--;
                    ToolUtils.setToast(mContext, R.string.loading_fail_txt);
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
        ZDApplication.mRequestQueue.add(jr);
    }


    public class ArticleAdapter extends BaseListAdapter<Article>
    {
        Context context;

        public ArticleAdapter(Context context, List<Article> list)
        {
            super(context, list);
            this.context = context;
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent)
        {
            convertView = mHashMap.get(position);
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_home_article_list, null);
            ImageView cover = ViewHolder.get(convertView, R.id.cover);
            cover.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenWidth * 400/ 750));
            TextView title = ViewHolder.get(convertView, R.id.title);
            TypeFaceTextView info = ViewHolder.get(convertView, R.id.info);
            TypeFaceTextView comments = ViewHolder.get(convertView, R.id.comments);

            Article article = getList().get(position);
            ToolUtils.setImageCacheUrl(article.getImg_url(), cover, R.drawable.icon_loading_item);
            title.setText(article.getTitle());
            info.setText(article.getInfo());
            comments.setText("评论:" + article.getReviews());

            mHashMap.put(position, convertView);
            return convertView;
        }
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_magic_class_case)); //统计页面
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_magic_class_case));
    }


}
