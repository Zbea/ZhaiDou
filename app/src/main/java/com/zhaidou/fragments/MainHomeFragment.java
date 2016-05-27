package com.zhaidou.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
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
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Article;
import com.zhaidou.model.SwitchImage;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomBannerView;
import com.zhaidou.view.ListViewForScrollView;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class MainHomeFragment extends BaseFragment implements
        AdapterView.OnItemClickListener, View.OnClickListener,
        PullToRefreshBase.OnRefreshListener2<ScrollView>

{
    private static final String URL = "targetUrl";
    private static final String TYPE = "type";

    private ListView listView;

    private int currentPage = 1;
    private int pageSize;
    private int pageCount;


    private static final int UPDATE_BANNER = 4;

    private ImageView mSearchView;
    private View view;
    private Dialog mDialog;
    private Context mContext;

    private ArticleAdapter adapterList;
    private RequestQueue mRequestQueue;
    private List<SwitchImage> banners = new ArrayList<SwitchImage>();
    private List<SwitchImage> codes = new ArrayList<SwitchImage>();
    private List<Article> articles = new ArrayList<Article>();
    private LinearLayout loadingView, nullNetView, nullView;
    private TextView reloadBtn, reloadNetBtn;
    private CustomBannerView customBannerView;
    private LinearLayout linearLayout, codeView;
    private PullToRefreshScrollView mScrollView;
    private WeakHashMap<Integer, View> mHashMap = new WeakHashMap<Integer, View>();
    private long formerTime;

    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if (msg.what == 1001)
            {
                mScrollView.onRefreshComplete();
                adapterList.notifyDataSetChanged();
                if (mDialog != null)
                    mDialog.dismiss();
                loadingView.setVisibility(View.GONE);
                if (pageCount > articles.size())
                {
                    mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
                } else
                {
                    mScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                }

            } else if (msg.what == UPDATE_BANNER)
            {
                setAdView();
                setCodeView();
            }
        }
    };

    /**
     * 广告轮播设置
     */
    private void setAdView()
    {
        if (customBannerView == null)
        {
            customBannerView = new CustomBannerView(mContext, banners, true);
            customBannerView.setLayoutParams(screenWidth, screenWidth * 300 / 750);
            customBannerView.setOnBannerClickListener(new CustomBannerView.OnBannerClickListener()
            {
                @Override
                public void onClick(int postion)
                {
                    SwitchImage item = banners.get(postion);
                    ToolUtils.setBannerGoto(item, mContext);
                    FetchClickStatisticalData(item.title,item.typeValue,item.type,postion);
                }
            });
            linearLayout.addView(customBannerView);
        } else
        {
            customBannerView.setImages(banners);
        }
    }

    /**
     * 添加 banner 下面四个按钮
     */
    private void setCodeView()
    {
        codeView.removeAllViews();
        for (int i = 0; i < codes.size(); i++)
        {
            final int pos = i;
            final View mView = LayoutInflater.from(mContext).inflate(R.layout.item_home_code, null);
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            mView.setLayoutParams(param);
            TextView codeName = (TextView) mView.findViewById(R.id.codeName);
            codeName.setText(codes.get(i).title);
            ImageView imageIv = (ImageView) mView.findViewById(R.id.codeImage);
            ToolUtils.setImageCacheUrl(codes.get(i).imageUrl, imageIv, R.drawable.icon_loading_circle);
            mView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (isTimeInterval())
                    {
                        SwitchImage item = codes.get(pos);
                        ToolUtils.setBannerGoto(item, mContext);
                    }
                }
            });
            codeView.addView(mView);
        }
    }

    /**
     * 设置时间间隔,防止重复点击
     */
    private boolean isTimeInterval()
    {
        long currentTime=System.currentTimeMillis();

        if ((currentTime- formerTime)>1000)
        {
            formerTime =currentTime;
            return true;
        }
        else
        {
            formerTime =currentTime;
            return false;
        }
    }



    private OnFragmentInteractionListener mListener;

    public static MainHomeFragment newInstance(String url, String type)
    {
        MainHomeFragment fragment = new MainHomeFragment();
        Bundle args = new Bundle();
        args.putString(URL, url);
        args.putString(TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    public MainHomeFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        if (view == null)
        {
            view = inflater.inflate(R.layout.fragment_main_home, container, false);
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
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");

        loadingView = (LinearLayout) view.findViewById(R.id.loadingView);
        nullNetView = (LinearLayout) view.findViewById(R.id.nullNetline);
        nullView = (LinearLayout) view.findViewById(R.id.nullline);
        reloadBtn = (TextView) view.findViewById(R.id.nullReload);
        reloadBtn.setOnClickListener(this);
        reloadNetBtn = (TextView) view.findViewById(R.id.netReload);
        reloadNetBtn.setOnClickListener(this);

        listView = (ListViewForScrollView) view.findViewById(R.id.homeItemList);
        listView.setOnItemClickListener(this);
        adapterList = new ArticleAdapter(mContext, articles);
        listView.setAdapter(adapterList);

        mScrollView = (PullToRefreshScrollView) view.findViewById(R.id.sv_home_scrollview);
        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mScrollView.setOnRefreshListener(this);

        codeView = (LinearLayout) view.findViewById(R.id.homeCodeView);

        mSearchView = (ImageView) view.findViewById(R.id.iv_message);
        mSearchView.setOnClickListener(this);

        currentPage = 1;

        mRequestQueue = Volley.newRequestQueue(getActivity());

        linearLayout = (LinearLayout) view.findViewById(R.id.bannerView);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenWidth * 300 / 750));
        initDate();
    }

    private void initDate()
    {
        banners.clear();
        codes.clear();
        if (NetworkUtils.isNetworkAvailable(mContext))
        {
            initData();
            FetchSpecialData();
            FetchData(currentPage);
        } else
        {
            mDialog.dismiss();
            nullNetView.setVisibility(View.VISIBLE);
            nullView.setVisibility(View.GONE);
        }

    }

    private void initData()
    {
        Article article=new Article(0,"改造加|都是知性优雅风","http://imgs.zhaidou.com/activity/67/HD2016FUN19432367/ac1_20160001.jpg",
                "",12,"北欧，是一个温暖而美丽的词汇，宁静、唯美、简单、温馨……很多人都喜欢北欧简约的风格设计");
        Article article1=new Article(0,"改造加|都是知性优雅风","http://imgs.zhaidou.com/activity/97/HD2016FQU04587497/ac1_20160002.jpg",
                "",12,"北欧，是一个温暖而美丽的词汇，宁静、唯美、简单、温馨……很多人都喜欢北欧简约的风格设计");
        articles.add(article);
        articles.add(article1);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener
    {
        public void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.iv_message:

                break;
            case R.id.nullReload:
                mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
                initDate();
                break;
            case R.id.netReload:
                mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
                initDate();
                break;
        }
    }

    /**
     * 加载列表数据
     */
    private void FetchData(final int page)
    {
        final String url = ZhaiDou.HomeArticleGoodsUrl + page;
        ToolUtils.setLog(url);
        JsonObjectRequest jr = new JsonObjectRequest(url ,new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                if (response == null)
                {
                    if (mDialog != null)
                        mDialog.dismiss();
                    mScrollView.onRefreshComplete();
                    if (currentPage == 1)
                    {
                        nullView.setVisibility(View.VISIBLE);
                        nullNetView.setVisibility(View.GONE);
                    }
                    return;
                }
                ToolUtils.setLog(response.toString());
                int code = response.optInt("code");
                if (code == 500)
                {
                    if (mDialog != null)
                        mDialog.dismiss();
                    mScrollView.onRefreshComplete();
                    mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
                    nullView.setVisibility(View.VISIBLE);
                    nullNetView.setVisibility(View.GONE);
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
                            Article article=new Article(id,title,imageUrl,
                                    "",12,info);

                            articles.add(article);
                        }
                    Message message = new Message();
                    message.what = 1001;
                    handler.sendMessage(message);
                }

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                mScrollView.onRefreshComplete();
                mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
                if (articles.size() != 0)
                {
                    currentPage--;
                    ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                } else
                {
                    nullView.setVisibility(View.VISIBLE);
                    nullNetView.setVisibility(View.GONE);
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

    private void FetchSpecialData()
    {
        final Map<String, String> headers = new HashMap<String, String>();
        headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));

        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.HomeBannerUrl + "03,05", new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                if (response != null)
                {
                    ToolUtils.setLog(response.toString());
                    JSONArray jsonArray = response.optJSONArray("data");
                    if (jsonArray != null)
                    {
                        for (int i = 0; i < jsonArray.length(); i++)
                        {
                            JSONObject jsonObj = jsonArray.optJSONObject(i);
                            String flags = jsonObj.optString("boardCode");
                            JSONArray array = jsonObj.optJSONArray("programPOList");
                            if (array != null)
                                for (int j = 0; j < array.length(); j++)
                                {
                                    JSONObject obj = array.optJSONObject(j);
                                    int type = obj.optInt("type");
                                    String typeValue = obj.optString("code");
                                    String imageUrl = obj.optString("pictureUrl");
                                    String title = obj.optString("name");
                                    if (type == 1)
                                    {
                                        typeValue = obj.optString("url");
                                    }
                                    SwitchImage switchImage = new SwitchImage();
                                    switchImage.id = j;
                                    switchImage.type = type;
                                    switchImage.typeValue = typeValue;
                                    switchImage.imageUrl = imageUrl;
                                    switchImage.title = title;
                                    switchImage.template_type = j == 0 ? 0 : 1;
                                    if (flags.equals("05"))
                                    {
                                        banners.add(switchImage);
                                    }
                                    if (flags.equals("03"))
                                    {
                                        ToolUtils.setLog("switchImage:" + switchImage.type);
                                        codes.add(switchImage);
                                    }
                                }
                        }
                        handler.sendEmptyMessage(UPDATE_BANNER);
                    }
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                return headers;
            }
        };
        mRequestQueue.add(request);
    }

    /**
     * 点击统计数据
     */
    private void FetchClickStatisticalData(String name,String url,int bannerType,int bannerIndex)
    {
        int userId = (Integer) SharedPreferencesUtil.getData(mContext, "userId", -1);
        String surl;
        if (checkLogin())
        {
            surl=ZhaiDou.HomeClickStatisticalUrl+name+"&url="+url+"&userId="+userId+"&sourceCode=3&bannerType="+bannerType+"&bannerIndex="+bannerIndex;
        }
        else
        {
            surl=ZhaiDou.HomeClickStatisticalUrl+name+"&url="+url+"&sourceCode=3&bannerType="+bannerType+"&bannerIndex="+bannerIndex;
        }

        JsonObjectRequest request = new JsonObjectRequest(surl, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                if (response != null)
                {
                    ToolUtils.setLog(response.toString());
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
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
        mRequestQueue.add(request);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
    {

    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView)
    {
        String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
        refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
        articles.clear();
        banners.clear();
        codes.clear();
        FetchData(currentPage = 1);
        FetchSpecialData();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView)
    {
        FetchData(++currentPage);
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
            cover.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenWidth * 316 / 722));
            TextView title = ViewHolder.get(convertView, R.id.title);
            TypeFaceTextView info = ViewHolder.get(convertView, R.id.info);
            TypeFaceTextView comments = ViewHolder.get(convertView, R.id.comments);

            Article article = getList().get(position);
            ToolUtils.setImageCacheUrl(article.getImg_url(), cover,R.drawable.icon_loading_item);
            title.setText(article.getTitle());
            info.setText(article.getInfo());
            comments.setText("评论:"+article.getReviews());

            mHashMap.put(position, convertView);
            return convertView;
        }
    }


    public void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_home));
    }

    public void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_home));
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

}
