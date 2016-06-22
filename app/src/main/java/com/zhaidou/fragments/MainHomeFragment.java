package com.zhaidou.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
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
import com.easemob.chat.EMChatManager;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshScrollView;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.LoginActivity;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.CountManager;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.easeui.helpdesk.ui.ConversationListFragment;
import com.zhaidou.model.Article;
import com.zhaidou.model.SwitchImage;
import com.zhaidou.utils.Api;
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
        PullToRefreshBase.OnRefreshListener2<ScrollView>,CountManager.onCommentChangeListener

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

    private HorizontalScrollView horizontalScrollView;
    private ArticleAdapter adapterList;
    private RequestQueue mRequestQueue;
    private List<SwitchImage> banners = new ArrayList<SwitchImage>();
    private List<SwitchImage> codes = new ArrayList<SwitchImage>();
    private List<SwitchImage> goods = new ArrayList<SwitchImage>();
    private List<Article> articles = new ArrayList<Article>();
    private LinearLayout loadingView, nullNetView, nullView;
    private TextView reloadBtn, reloadNetBtn;
    private CustomBannerView customBannerView;
    private LinearLayout linearLayout, codeView,goodsView;
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
                setGoodsView();
                setCodeView();

            }
        }
    };
    private TextView unreadMsg;


    /**
     * 广告轮播设置
     */
    private void setAdView()
    {
        if (customBannerView == null)
        {
            customBannerView = new CustomBannerView(mContext, banners, true);
            customBannerView.setLayoutParams(screenWidth, screenWidth * 400 / 750);
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
     * 添加首页三个按钮
     */
    private void setGoodsView()
    {
        horizontalScrollView.setVisibility(goods.size()>0?View.VISIBLE:View.GONE);
        goodsView.removeAllViews();
        for (int i = 0; i < goods.size(); i++)
        {
            final int pos = i;
           ImageView imgView=new ImageView(mContext);
            imgView.setScaleType(ImageView.ScaleType.FIT_XY);
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams((screenWidth-80)/3,(screenWidth-80)/3);
            if (i>0)
            {
                param.leftMargin=20;
            }
            imgView.setLayoutParams(param);
            ToolUtils.setImageCacheUrl(goods.get(i).imageUrl, imgView, R.drawable.icon_loading_defalut);
            imgView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (isTimeInterval())
                    {
                        SwitchImage item = goods.get(pos);
                        ToolUtils.setBannerGoto(item, mContext);
                    }
                }
            });
            goodsView.addView(imgView);
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

        horizontalScrollView=(HorizontalScrollView)view.findViewById(R.id.homeGoodsLine);
        goodsView= (LinearLayout) view.findViewById(R.id.homeGoodsView);

        codeView = (LinearLayout) view.findViewById(R.id.homeCodeView);

        mSearchView = (ImageView) view.findViewById(R.id.iv_message);
        mSearchView.setOnClickListener(this);

        currentPage = 1;

        mRequestQueue = Volley.newRequestQueue(getActivity());

        linearLayout = (LinearLayout) view.findViewById(R.id.bannerView);
        unreadMsg = (TextView) view.findViewById(R.id.unreadMsg);
        initDate();
        Integer userId= (Integer) SharedPreferencesUtil.getData(mContext,"userId",-1);
        if (userId!=-1)
            Api.getUnReadComment(userId,null,null);
        CountManager.getInstance().setOnCommentChangeListener(this);
    }

    private void initDate()
    {
        goods.clear();
        banners.clear();
        codes.clear();
        articles.clear();
        if (NetworkUtils.isNetworkAvailable(mContext))
        {
            FetchSpecialData();
            FetchData(currentPage);
        } else
        {
            mDialog.dismiss();
            nullNetView.setVisibility(View.VISIBLE);
            nullView.setVisibility(View.GONE);
        }

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

    @Override
    public void onChange() {
        Integer userId= (Integer) SharedPreferencesUtil.getData(mContext,"userId",-1);
        int unreadMsgsCount = EMChatManager.getInstance().getUnreadMsgsCount();
        Integer UnReadComment= (Integer) SharedPreferencesUtil.getData(ZDApplication.getInstance(),"UnReadComment",0);
        unreadMsg.setVisibility((unreadMsgsCount + UnReadComment) > 0&&userId!=-1? View.VISIBLE : View.GONE);
        unreadMsg.setText((unreadMsgsCount+UnReadComment) > 99 ? "99+" : (unreadMsgsCount+UnReadComment) + "");
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
                Integer userId= (Integer) SharedPreferencesUtil.getData(mContext,"userId",-1);
                if (userId==-1){
                    Intent intent =new Intent(mContext, LoginActivity.class);
                    startActivity(intent);
                    return;
                }
                ConversationListFragment conversationListFragment=new ConversationListFragment();
                ((BaseActivity) mContext).navigationToFragment(conversationListFragment);
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
                            int num = obj.optInt("commentCount");
                            Article article=new Article(id,title,imageUrl,
                                    "",num,info);

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

        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.HomeBannerUrl + "03,05,06", new Response.Listener<JSONObject>()
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
                                        codes.add(switchImage);
                                    }
                                    if (flags.equals("06"))
                                    {
                                        goods.add(switchImage);
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
    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l)
    {
        HomeArticleGoodsDetailsFragment homeArticleGoodsDetailsFragment=HomeArticleGoodsDetailsFragment.newInstance("",""+articles.get(position).getId());
        ((BaseActivity)mContext).navigationToFragment(homeArticleGoodsDetailsFragment);
        homeArticleGoodsDetailsFragment.setOnCommentListener(new HomeArticleGoodsDetailsFragment.OnCommentListener()
        {
            @Override
            public void setComment(int num)
            {
                if (num!=0)
                {
                    articles.get(position).setReviews(num);
                    adapterList.notifyDataSetChanged();
                }
            }
        });
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
        goods.clear();
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
            cover.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenWidth * 400/ 750));
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

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (articles == null | articles.size() < 1) {
                initDate();
            }
            Integer userId= (Integer) SharedPreferencesUtil.getData(mContext,"userId",-1);
            if (userId!=-1)
                Api.getUnReadComment(userId, null, null);
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
