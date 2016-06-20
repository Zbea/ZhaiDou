package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
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
import com.easemob.chat.EMChatManager;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshScrollView;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.adapter.ShopSpecialAdapter;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.CountManager;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.ShopSpecialItem;
import com.zhaidou.model.SwitchImage;
import com.zhaidou.utils.Api;
import com.zhaidou.utils.EaseUtils;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomBannerView;
import com.zhaidou.view.ListViewForScrollView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class MainGoodsFragment extends BaseFragment implements
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
    private static final int UPDATE_SEARCH = 5;

    private View mSpecialLayout;
    private LinearLayout mSearchView;
    private TextView titleTv;
    private ImageView categoryIv,messageTv;
    private View view;
    private Dialog mDialog;
    private Context mContext;

    private List<ShopSpecialItem> items = new ArrayList<ShopSpecialItem>();
    private ShopSpecialAdapter adapterList;
    private RequestQueue mRequestQueue;
    private List<SwitchImage> banners = new ArrayList<SwitchImage>();
    private List<SwitchImage> codes = new ArrayList<SwitchImage>();
    private List<SwitchImage> specials = new ArrayList<SwitchImage>();
    private LinearLayout loadingView, nullNetView, nullView;
    private TextView reloadBtn, reloadNetBtn;
    private CustomBannerView customBannerView;
    private LinearLayout linearLayout, codeView, moduleView;
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
                if (pageCount > items.size())
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
                setModuleView();
            }
            else if (msg.what == UPDATE_SEARCH)
            {
                titleTv.setText(msg.obj.toString());
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

    private void setModuleView()
    {
        moduleView.removeAllViews();
        if (specials.size() <= 0)
        {
            return;
        }
        int num = specials.size() / 5 ;
        for (int i = 0; i < num; i++)
        {
            final int pos = i * 5;
            final View mView = LayoutInflater.from(mContext).inflate(R.layout.item_home_module, null);
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    screenWidth, screenWidth * (1260 + 32) / 1194);
            mView.setLayoutParams(param);
            ImageView imageIv1 = (ImageView) mView.findViewById(R.id.moduleIv1);
            ToolUtils.setImageCacheUrl(specials.get(pos).imageUrl, imageIv1, R.drawable.icon_loading_home_topic_big);
            imageIv1.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (isTimeInterval())
                    {
                        SwitchImage item = specials.get(pos);
                        ToolUtils.setBannerGoto(item, mContext);
                    }
                }
            });
            ImageView imageIv2 = (ImageView) mView.findViewById(R.id.moduleIv2);
            ToolUtils.setImageCacheUrl(specials.get(pos + 1).imageUrl, imageIv2, R.drawable.icon_loading_home_topic_small);
            imageIv2.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (isTimeInterval())
                    {
                        SwitchImage item = specials.get(pos + 1);
                        ToolUtils.setBannerGoto(item, mContext);
                    }
                }
            });
            ImageView imageIv3 = (ImageView) mView.findViewById(R.id.moduleIv3);
            ToolUtils.setImageCacheUrl(specials.get(pos + 2).imageUrl, imageIv3, R.drawable.icon_loading_home_topic_small);
            imageIv3.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (isTimeInterval())
                    {
                        SwitchImage item = specials.get(pos + 2);
                        ToolUtils.setBannerGoto(item, mContext);
                    }
                }
            });
            ImageView imageIv4 = (ImageView) mView.findViewById(R.id.moduleIv4);
            ToolUtils.setImageCacheUrl(specials.get(pos + 3).imageUrl, imageIv4, R.drawable.icon_loading_home_topic_small);
            imageIv4.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (isTimeInterval())
                    {
                        SwitchImage item = specials.get(pos + 3);
                        ToolUtils.setBannerGoto(item, mContext);
                    }
                }
            });
            ImageView imageIv5 = (ImageView) mView.findViewById(R.id.moduleIv5);
            ToolUtils.setImageCacheUrl(specials.get(pos + 4).imageUrl, imageIv5, R.drawable.icon_loading_home_topic_small);
            imageIv5.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (isTimeInterval())
                    {
                        SwitchImage item = specials.get(pos + 4);
                        ToolUtils.setBannerGoto(item, mContext);
                    }
                }
            });
            moduleView.addView(mView);
        }
    }

    public static MainGoodsFragment newInstance(String url, String type)
    {
        MainGoodsFragment fragment = new MainGoodsFragment();
        Bundle args = new Bundle();
        args.putString(URL, url);
        args.putString(TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    public MainGoodsFragment()
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
            view = inflater.inflate(R.layout.fragment_main_goods, container, false);
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
        adapterList = new ShopSpecialAdapter(mContext, items, screenWidth);
        listView.setAdapter(adapterList);

        mScrollView = (PullToRefreshScrollView) view.findViewById(R.id.sv_home_scrollview);
        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mScrollView.setOnRefreshListener(this);

        codeView = (LinearLayout) view.findViewById(R.id.homeCodeView);
        moduleView = (LinearLayout) view.findViewById(R.id.moduleView);

        mSearchView = (LinearLayout) view.findViewById(R.id.iv_searchs);
        mSearchView.setOnClickListener(this);

        titleTv= (TextView) view.findViewById(R.id.tv_title);

        categoryIv = (ImageView) view.findViewById(R.id.iv_category);
        categoryIv.setOnClickListener(this);
        messageTv = (ImageView) view.findViewById(R.id.iv_message);
        messageTv.setOnClickListener(this);
        mSpecialLayout = view.findViewById(R.id.specialLayout);
        mSpecialLayout.setVisibility(View.GONE);
        unreadMsg = (TextView) view.findViewById(R.id.unreadMsg);

        currentPage = 1;

        mRequestQueue = Volley.newRequestQueue(getActivity());

        linearLayout = (LinearLayout) view.findViewById(R.id.bannerView);
//        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenWidth * 400 / 750));
        initDate();
        Integer userId= (Integer) SharedPreferencesUtil.getData(mContext,"userId",-1);
        if (userId!=-1)
            Api.getUnReadComment(userId,null,null);
        CountManager.getInstance().setOnCommentChangeListener(this);
    }

    private void initDate()
    {
        banners.clear();
        specials.clear();
        codes.clear();
        items.clear();
        if (NetworkUtils.isNetworkAvailable(mContext))
        {
            FetchSpecialData();
            FetchData(currentPage);
            FetchSearchData();
        } else
        {
            mDialog.dismiss();
            nullNetView.setVisibility(View.VISIBLE);
            nullView.setVisibility(View.GONE);
        }

    }



    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.iv_searchs:
                SearchFragment searchFragment = SearchFragment.newInstance("", 1);
                ((BaseActivity) getActivity()).navigationToFragmentWithAnim(searchFragment);
//                ((MainActivity) getActivity()).gotoCategory();
                break;
            case R.id.iv_category:
                MainCategoryFragment mainCategoryFragment = MainCategoryFragment.newInstance("", "");
                ((BaseActivity) getActivity()).navigationToFragmentWithAnim(mainCategoryFragment);
                break;
            case R.id.iv_message:
                EaseUtils.startConversationListActivity(mContext);
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
        final String url = ZhaiDou.HomeShopListUrl + page + "&typeEnum=1";
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
                    JSONArray jsonArray = jsonObject.optJSONArray("themeList");

                    if (jsonArray != null)
                        for (int i = 0; i < jsonArray.length(); i++)
                        {
                            JSONObject obj = jsonArray.optJSONObject(i);
                            String id = obj.optString("activityCode");
                            String title = obj.optString("activityName");
                            String sales = obj.optString("discountLabel");
                            long startTime = obj.optLong("startTime");
                            long endTime = obj.optLong("endTime");
                            int overTime = Integer.parseInt((String.valueOf((endTime - System.currentTimeMillis()) / (24 * 60 * 60 * 1000))));
                            if ((endTime - System.currentTimeMillis()) % (24 * 60 * 60 * 1000) > 0)
                            {
                                overTime = overTime + 1;
                            }

                            String imageUrl = obj.optString("mainPic");
                            int isNew = obj.optInt("newFlag");
                            ShopSpecialItem shopSpecialItem = new ShopSpecialItem(id, title, sales, startTime, endTime, overTime, imageUrl, isNew);

                            items.add(shopSpecialItem);
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
                if (items.size() != 0)
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

        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.HomeBannerUrl + "01,02,03", new Response.Listener<JSONObject>()
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
                                    if (flags.equals("01"))
                                    {
                                        banners.add(switchImage);
                                    }
                                    if (flags.equals("02"))
                                    {
                                        specials.add(switchImage);
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


    private void FetchSearchData()
    {
        final Map<String, String> headers = new HashMap<String, String>();
        headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.HomeSearchStringUrl, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                if (response != null)
                {
                    ToolUtils.setLog(response.toString());
                    JSONArray jsonArray=response.optJSONArray("data");
                    String search="";
                    if (jsonArray!=null)
                        for (int i = 0; i < jsonArray.length(); i++)
                        {
                            search=search+jsonArray.optString(i);
                        }
                        handler.obtainMessage(UPDATE_SEARCH,search).sendToTarget();

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
        ShopTodaySpecialFragment shopTodaySpecialFragment = ShopTodaySpecialFragment.newInstance(items.get(position).title, items.get(position).goodsId, items.get(position).imageUrl);
        ((BaseActivity) getActivity()).navigationToFragmentWithAnim(shopTodaySpecialFragment);
        if ("1".equalsIgnoreCase(items.get(position).isNew + ""))
        {
            SharedPreferencesUtil.saveData(mContext, "homeNews_" + items.get(position).goodsId, false);
            view.findViewById(R.id.newsView).setVisibility(View.GONE);
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView)
    {
        String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
        refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
        items.clear();
        banners.clear();
        codes.clear();
        specials.clear();
        FetchData(currentPage = 1);
        FetchSpecialData();
        FetchSearchData();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView)
    {
        FetchData(++currentPage);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (items == null | items.size() < 1) {
                initDate();
            }
        }
        Integer userId= (Integer) SharedPreferencesUtil.getData(mContext,"userId",-1);
        if (userId!=-1)
        Api.getUnReadComment(userId,null,null);
    }


    public void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_home));
        System.out.println("MainGoodsFragment.onResume");
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

    @Override
    public void onChange() {
        System.out.println("MainGoodsFragment.onChange");
        int unreadMsgsCount = EMChatManager.getInstance().getUnreadMsgsCount();
        Integer UnReadComment= (Integer) SharedPreferencesUtil.getData(ZDApplication.getInstance(),"UnReadComment",0);
        unreadMsg.setVisibility((unreadMsgsCount + UnReadComment) > 0 ? View.VISIBLE : View.GONE);
        unreadMsg.setText((unreadMsgsCount + UnReadComment) > 99 ? "99+" : (unreadMsgsCount + UnReadComment) + "");
    }
}
