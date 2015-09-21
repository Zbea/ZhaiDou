package com.zhaidou.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshScrollView;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.HomeCompetitionActivity;
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.activities.SearchActivity;
import com.zhaidou.activities.WebViewActivity;
import com.zhaidou.adapter.AdViewAdpater;
import com.zhaidou.adapter.GoodsImageAdapter;
import com.zhaidou.adapter.HomeListAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Article;
import com.zhaidou.model.Category;
import com.zhaidou.model.ShopSpecialItem;
import com.zhaidou.model.SwitchImage;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.HeaderLayout;
import com.zhaidou.view.ListViewForScrollView;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class HomeFragment extends BaseFragment implements
        HeaderLayout.onLeftImageButtonClickListener,
        HeaderLayout.onRightImageButtonClickListener,
        HomeCategoryFragment.CategorySelectedListener,
        AdapterView.OnItemClickListener, View.OnClickListener,
        PullToRefreshBase.OnRefreshListener2<ScrollView>

{
    private static final String URL = "targetUrl";
    private static final String TYPE = "type";

    private ListView listView;
    private ZhaiDou.ListType listType;

    private String targetUrl;
    private int currentPage = 1;
    private int count = -1;

    private boolean loadedAll = false;
    private final int LOADED = 1;

    private WeakHashMap<Integer, View> mHashMap = new WeakHashMap<Integer, View>();
    /* Data Definition*/
    List<JSONObject> listItem;
    private static final int STATUS_REFRESH = 0;
    private static final int STATUS_LOAD_MORE = 1;
    private static final int UPDATE_CATEGORY = 2;
    private static final int UPDATE_HOMELIST = 3;
    private static final int UPDATE_BANNER = 4;

    private ImageView mSearchView;
    private static ImageView mCategoryView,mDotView;
    private TextView mTitleView;
    private int screenWidth;
    private View view;
    private Dialog mDialog;
    private Context mContext;

    private PopupWindow mPopupWindow = null;
    private LinearLayout ll_poplayout;
    private static FrameLayout fl_category_menu;
    private GridView gv_category;
    private List<String> categoryList;
    private RequestQueue mRequestQueue;
    private List<Article> articleList = new ArrayList<Article>();

    private LinearLayout itemBtn;

    public HomeListAdapter mListAdapter;
    private ViewPager viewPager;
    private LinearLayout tipsLine;//轮播指示标志
    private List<SwitchImage> banners;
    private ImageView[] dots;
    private List<View> adPics = new ArrayList<View>();
    private AdViewAdpater adpaters;
    private GoodsImageAdapter adapter;
    private int currentItem = 5000;
    boolean nowAction = false;
    boolean isStop = true;

    public HomeCategoryFragment homeCategoryFragment;
    private Category mCategory;
    private ShopSpecialItem shopSpecialItem;

    //特卖view初始化
    private ImageView itemTipsIv;
    private ImageView itemImageIv;
    private TypeFaceTextView itemNameTv;
    private TypeFaceTextView itemTimeTv;
    private TypeFaceTextView itemSaleTv;

    private LinearLayout loadingView,nullNetView,nullView;
    private TextView  reloadBtn,reloadNetBtn;

    private PullToRefreshScrollView mScrollView;
    /* Log cat */
    public static final String ERROR_CAT = "ERROR";
    public static final String DEBUG_CAT = "DEBUG";


    private BroadcastReceiver broadcastReceiver=new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String tag=intent.getAction();
            if (tag.equals(ZhaiDou.IntentRefreshListTag))
            {
                refresh();
            }

        }
    };

    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if (msg.what == LOADED)
            {

            } else if (msg.what == UPDATE_CATEGORY)
            {
            } else if (msg.what == UPDATE_HOMELIST)
            {
                loadingView.setVisibility(View.GONE);
                if (mListAdapter == null)
                {
                    mListAdapter = new HomeListAdapter(mContext, articleList,screenWidth);
                    listView.setAdapter(mListAdapter);
                    if (banners==null||banners.size()<1)
                    {
                        getBannerData();
                    }
                    if (shopSpecialItem==null)
                    {
                        FetchShopData();
                    }
                }
                mScrollView.onRefreshComplete();
                mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
                if (mDialog.isShowing())
                {
                    mDialog.dismiss();
                }

            } else if (msg.what == UPDATE_BANNER)
            {
                adPics.removeAll(adPics);
                setAdView();
            }
            else if(msg.what==1001)
            {
                if(shopSpecialItem!=null)
                {
                    itemBtn.setVisibility(View.VISIBLE);
                    ToolUtils.setImageCacheUrl(shopSpecialItem.imageUrl,itemImageIv);
                    itemNameTv.setText(shopSpecialItem.title);
                    itemTimeTv.setText(shopSpecialItem.overTime);
                    itemSaleTv.setText(shopSpecialItem.sale);
                }

            }
            if (mListAdapter != null)
                mListAdapter.notifyDataSetChanged();
        }
    };

    private Handler mhandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            viewPager.setCurrentItem(currentItem);
        }
    };


    /**
     * 广告轮播设置
     */
    private void setAdView()
    {
        tipsLine.removeAllViews();
        if (banners.size() > 0)
        {
            for (int i = 0; i < banners.size(); i++)
            {
                final int tag = i;
                final ImageView img = new ImageView(mContext);
                img.setBackgroundResource(R.drawable.icon_loading_item);
                img.setScaleType(ImageView.ScaleType.FIT_XY);
                img.setLayoutParams(new ViewGroup.LayoutParams(screenWidth, screenWidth * 300 / 750));
                img.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
//              r_type=0：0元特卖商城r_type=1：H5页面r_type=2：文章r_type=3：单品r_type=4：分类
                        SwitchImage item=banners.get(tag);
                        if (item.type==0)
                        {
                            SpecialSaleFragment specialSaleFragment = SpecialSaleFragment.newInstance("", "");
                            ((MainActivity) getActivity()).navigationToFragment(specialSaleFragment);
                        }
                        else if (item.type==1)
                        {
                            Intent intent = new Intent();
                            intent.putExtra("url", item.typeValue);
                            intent.setClass(getActivity(), WebViewActivity.class);
                            getActivity().startActivity(intent);
                        }
                        else if (item.type==2)
                        {
                            Intent detailIntent = new Intent(getActivity(), ItemDetailActivity.class);
                            detailIntent.putExtra("id", item.id + "");
                            detailIntent.putExtra("from", "product");
                            detailIntent.putExtra("title", item.title);
                            detailIntent.putExtra("cover_url", item.imageUrl);
                            detailIntent.putExtra("url",ZhaiDou.ARTICLE_DETAIL_URL+item.id);
                            mContext.startActivity(detailIntent);
                        }
                        else if (item.type==3)
                        {
                            GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance("", 0);
                            Bundle bundle = new Bundle();
                            bundle.putInt("flags", 1);
                            bundle.putInt("index", Integer.valueOf(item.typeValue));
                            bundle.putString("page", item.title);
                            goodsDetailsFragment.setArguments(bundle);
                            ((MainActivity) getActivity()).navigationToFragment(goodsDetailsFragment);
                        }else
                        {
                            Category category=new Category();
                            category.setId(Integer.parseInt(item.typeValue));
                            SpecialFragment shopTodaySpecialFragment = SpecialFragment.newInstance("",category);
                            ((MainActivity) getActivity()).navigationToFragment(shopTodaySpecialFragment);
                        }
                    }
                });
                ToolUtils.setImageCacheUrl(banners.get(i).imageUrl, img,R.drawable.icon_loading_item);
                adPics.add(img);
            }
            dots = new ImageView[adPics.size()];
            for (int i = 0; i < adPics.size(); i++)
            {
                ImageView dot_iv = new ImageView(mContext);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.bottomMargin = 10;
                if (i == 0)
                {
                    params.leftMargin = 0;
                } else
                {
                    params.leftMargin = 20;
                }

                dot_iv.setLayoutParams(params);
                dots[i] = dot_iv;
                tipsLine.addView(dot_iv);
                if (i == 0)
                {
                    dots[i].setBackgroundResource(R.drawable.home_tips_foucs_icon);
                } else
                {
                    dots[i].setBackgroundResource(R.drawable.home_tips_icon);
                }

            }
            if (adpaters == null)
            {
                adpaters = new AdViewAdpater(mContext, adPics);
                viewPager.setAdapter(adpaters);
                viewPager.setOnPageChangeListener(new MyPageChangeListener());
                viewPager.setOnTouchListener(new View.OnTouchListener()
                {
                    @Override
                    public boolean onTouch(View v, MotionEvent event)
                    {
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        return false;
                    }
                });

                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        while (isStop)
                        {
                            try
                            {
                                Thread.sleep(5000);
                            } catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                            if (!nowAction)
                            {
                                currentItem = currentItem + 1;
                                mhandler.obtainMessage().sendToTarget();
                            }
                        }
                    }
                }).start();
            } else
            {
                adpaters.notifyDataSetChanged();
            }

        }


    }

    private OnFragmentInteractionListener mListener;

    public static HomeFragment newInstance(String url, String type)
    {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(URL, url);
        args.putString(TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    public HomeFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            targetUrl = getArguments().getString(URL);
        }
    }

    /**
     * 刷新mAdapterList
     */
    public void  refresh()
    {
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        if(view==null)
        {
            view = inflater.inflate(R.layout.fragment_home, container, false);
            mContext = getActivity();
            initBroadcastReceiver();
            WindowManager wm = ((Activity)mContext).getWindowManager();
            screenWidth = wm.getDefaultDisplay().getWidth();

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
        nullNetView= (LinearLayout) view.findViewById(R.id.nullNetline);
        nullView= (LinearLayout) view.findViewById(R.id.nullline);
        reloadBtn = (TextView) view.findViewById(R.id.nullReload);
        reloadBtn.setOnClickListener(this);
        reloadNetBtn = (TextView) view.findViewById(R.id.netReload);
        reloadNetBtn.setOnClickListener(this);

        listView = (ListViewForScrollView) view.findViewById(R.id.homeItemList);
        listView.setOnItemClickListener(this);
        fl_category_menu = (FrameLayout) view.findViewById(R.id.fl_category_menu);
        mScrollView = (PullToRefreshScrollView) view.findViewById(R.id.sv_home_scrollview);
        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mScrollView.setOnRefreshListener(this);

        view.findViewById(R.id.ll_lottery).setOnClickListener(this);
        view.findViewById(R.id.ll_special_shop).setOnClickListener(this);
        view.findViewById(R.id.ll_sale).setOnClickListener(this);
        view.findViewById(R.id.ll_forward).setOnClickListener(this);
        view.findViewById(R.id.ll_category_view).setOnClickListener(this);

        mSearchView = (ImageView) view.findViewById(R.id.iv_search);
        mSearchView.setOnClickListener(this);
        mCategoryView = (ImageView) view.findViewById(R.id.iv_category);
        mCategoryView.setOnClickListener(this);
        mTitleView = (TextView) view.findViewById(R.id.tv_title);
        mDotView=(ImageView)view.findViewById(R.id.iv_dot);
        viewPager = (ViewPager) view.findViewById(R.id.home_adv_pager);
        tipsLine = (LinearLayout) view.findViewById(R.id.home_viewGroup);

        itemBtn=(LinearLayout)view.findViewById(R.id.home_item_goods);
        itemBtn.setVisibility(View.GONE);
        itemBtn.setOnClickListener(this);

        currentPage = 1;
        loadedAll = false;

        mRequestQueue = Volley.newRequestQueue(getActivity());
        listItem = new ArrayList<JSONObject>();

        if (homeCategoryFragment == null)
        {
            homeCategoryFragment = HomeCategoryFragment.newInstance("", "");
            getChildFragmentManager().beginTransaction().add(R.id.fl_category_menu, homeCategoryFragment
                    , HomeCategoryFragment.TAG).hide(homeCategoryFragment).addToBackStack(null).commit();
            homeCategoryFragment.setCategorySelectedListener(this);
        }

        itemTipsIv=(ImageView)view.findViewById(R.id.homeGoodsTips);
        itemImageIv=(ImageView)view.findViewById(R.id.homeGoodsImage);
        itemImageIv.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenWidth * 316 / 722));
        itemNameTv=(TypeFaceTextView)view.findViewById(R.id.homeGoodsName);
        itemTimeTv=(TypeFaceTextView)view.findViewById(R.id.shop_time_item);
        itemSaleTv=(TypeFaceTextView)view.findViewById(R.id.homeGoodsSale);
        initDate();

    }

    private void initDate()
    {
        if (NetworkUtils.isNetworkAvailable(mContext))
        {
            FetchShopData();
            getBannerData();
            FetchData(currentPage, null);
        }
        else
        {
            mDialog.dismiss();
            nullNetView.setVisibility(View.VISIBLE);
            nullView.setVisibility(View.GONE);
        }

    }

    /**
     * 广播注册
     */
    private void initBroadcastReceiver()
    {
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(ZhaiDou.IntentRefreshListTag);
        mContext.registerReceiver(broadcastReceiver,intentFilter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

    }



    public void onButtonPressed(Uri uri)
    {
        if (mListener != null)
        {
            mListener.onFragmentInteraction(uri);
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


    public interface OnFragmentInteractionListener
    {
        public void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.iv_search:
                startActivity(new Intent(getActivity(), SearchActivity.class));
                break;
            case R.id.ll_category_view:
                toggleMenu();
                break;
            case R.id.ll_lottery:
                Intent detailIntent = new Intent(getActivity(), HomeCompetitionActivity.class);
                detailIntent.putExtra("url", ZhaiDou.PRIZE_SCRAPING_URL);
                detailIntent.putExtra("from", "lottery");
                detailIntent.putExtra("title", "天天刮奖");
                getActivity().startActivity(detailIntent);
                break;

            case R.id.ll_special_shop:
                ShopSpecialFragment shopSpecialFragment = ShopSpecialFragment.newInstance("", 0);
                ((MainActivity) getActivity()).navigationToFragment(shopSpecialFragment);
                break;

            case R.id.ll_sale:
                SpecialSaleFragment specialSaleFragment = SpecialSaleFragment.newInstance("", "");
                ((MainActivity) getActivity()).navigationToFragment(specialSaleFragment);
                break;
            case R.id.ll_forward:
                Intent intent1 = new Intent();
                intent1.putExtra("url", ZhaiDou.FORWARD_URL);
                intent1.putExtra("from", "beauty");
                intent1.putExtra("title", "转发有喜");
                intent1.setClass(getActivity(), ItemDetailActivity.class);
                getActivity().startActivity(intent1);
                break;
            case R.id.ll_back:
                mCategoryView.setVisibility(View.VISIBLE);
                break;

            case R.id.home_item_goods:
                if (shopSpecialItem != null)
                {
                    ShopTodaySpecialFragment shopTodaySpecialFragment = ShopTodaySpecialFragment.newInstance(shopSpecialItem.title, shopSpecialItem.id,shopSpecialItem.imageUrl);
                    ((MainActivity) getActivity()).navigationToFragment(shopTodaySpecialFragment);
                    break;
                }
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

    private void FetchData(int page, Category category)
    {
        currentPage = page;
        if (page == 1)
        {
            loadedAll = false;
        }
        if (loadedAll)
        {
            Toast.makeText(getActivity(), "已经加载完毕了哦！！！", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(mContext, "加载失败", Toast.LENGTH_SHORT).show();
                if (mDialog.isShowing())
                {
                    mDialog.dismiss();
                }
                nullView.setVisibility(View.VISIBLE);
                nullNetView.setVisibility(View.GONE);
                mScrollView.onRefreshComplete();
                mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
            }
        });
        mRequestQueue.add(jr);
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

            getChildFragmentManager().beginTransaction().show(homeCategoryFragment).commit();
        } else
        {
            mCategoryView.setImageResource(R.drawable.icon_category);
            fl_category_menu.setVisibility(View.GONE);
            getChildFragmentManager().beginTransaction().hide(homeCategoryFragment).commit();
        }
        homeCategoryFragment.notifyDataSetChanged();
    }



    @Override
    public void onCategorySelected(Category category)
    {
        mDialog = CustomLoadingDialog.setLoadingDialog(getActivity(), "loading");
        articleList.removeAll(articleList);
        FetchData(currentPage = 1, mCategory = category);
        if (category!=null)
        {
            mDotView.setVisibility(View.VISIBLE);
        }
        else
        {
            mDotView.setVisibility(View.GONE);
        }
        toggleMenu();
    }

    /**
     * 获得广告数据
     */
    private void getBannerData()
    {
        String url = ZhaiDou.BannerUrl+2;
        banners = new ArrayList<SwitchImage>();
        JsonObjectRequest bannerRequest = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                JSONArray jsonArray = jsonObject.optJSONArray("sale_banners");
                if (jsonArray != null && jsonArray.length() > 0)
                {
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject obj = jsonArray.optJSONObject(i);
                        int id = obj.optInt("id");
                        int type = obj.optInt("r_type");
                        String typeValue = obj.optString("r_value");
                        String imageUrl =obj.optString("imgs");
                        String title = obj.optString("title");
                        SwitchImage switchImage = new SwitchImage();
                        switchImage.id=id;
                        switchImage.type=type;
                        switchImage.typeValue=typeValue;
                        switchImage.imageUrl=imageUrl;
                        switchImage.title=title;
                        banners.add(switchImage);
                    }
                    Message message = new Message();
                    message.what = UPDATE_BANNER;
                    message.obj = banners;
                    handler.sendMessage(message);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {

            }
        });
        mRequestQueue.add(bannerRequest);
    }

    /**
     * 加载列表数据
     */
    private void FetchShopData()
    {
        final String url;
        url = ZhaiDou.shopHomeSpecialUrl;
        JsonObjectRequest jr = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                String result=response.toString();
                JSONObject obj;
                try
                {
                    JSONObject jsonObject=new JSONObject(result);
                    JSONArray jsonArray=jsonObject.optJSONArray("sales");
                    if (jsonArray.length()>0)
                    {
                        obj=jsonArray.optJSONObject(0);
                        int id=obj.optInt("id");
                        String title=obj.optString("title");
                        String sales=obj.optString("tags");
                        String time=obj.optString("day");
                        String startTime=obj.optString("start_time");
                        String endTime=obj.optString("end_time");
                        String overTime=obj.optString("over_day");
                        String imageUrl=obj.optString("banner");
                        shopSpecialItem=new ShopSpecialItem(id,title,sales,time,startTime,endTime,overTime,imageUrl);
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
                Message message = new Message();
                message.what = 1001;
                handler.sendMessage(message);
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                mDialog.dismiss();
                mScrollView.onRefreshComplete();
                mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
            }
        });
        mRequestQueue.add(jr);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
    {
        Article article = articleList.get(position);
        Log.i("id----->", article.getId() + "");
        Intent detailIntent = new Intent(getActivity(), ItemDetailActivity.class);
        detailIntent.putExtra("article", article);
        detailIntent.putExtra("id", article.getId() + "");
        detailIntent.putExtra("from", "product");
        detailIntent.putExtra("title", article.getTitle());
        detailIntent.putExtra("cover_url", article.getImg_url());
        detailIntent.putExtra("url",ZhaiDou.ARTICLE_DETAIL_URL+article.getId());
        startActivityForResult(detailIntent, 100);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView)
    {
        String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
        refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
        getBannerData();
        FetchShopData();
        articleList.clear();
        FetchData(currentPage = 1, mCategory);
        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView)
    {
        if (count != -1 && articleList.size() == count)
        {
            Toast.makeText(getActivity(), "已经加载完毕", Toast.LENGTH_SHORT).show();
            mScrollView.onRefreshComplete();
            mScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            return;
        }
        FetchData(++currentPage, mCategory);
    }

    /**
     * 广告轮播指示器
     */
    private class MyPageChangeListener implements ViewPager.OnPageChangeListener
    {
        public void onPageSelected(int position)
        {
            currentItem = position;
            if (adPics.size() != 0)
            {
                changeDotsBg(currentItem % adPics.size());
            }
        }
        public void onPageScrollStateChanged(int arg0)
        {
            if (arg0 == 0)
            {
                nowAction = false;
            }
            if (arg0 == 1)
            {
                nowAction = true;
            }
            if (arg0 == 2)
            {
            }
        }
        public void onPageScrolled(int arg0, float arg1, int arg2)
        {
            viewPager.getParent().requestDisallowInterceptTouchEvent(true);
        }

        private void changeDotsBg(int currentitem)
        {
            for (int i = 0; i < dots.length; i++)
            {
                if (currentitem == i)
                {
                    dots[currentitem].setBackgroundResource(R.drawable.home_tips_foucs_icon);
                } else
                {
                    dots[i].setBackgroundResource(R.drawable.home_tips_icon);
                }
            }
        }
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_home));
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_home));
    }
    @Override
    public void onDestroy()
    {
        isStop = false;
        mContext.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    /**
     * 处理切换图标变换
     */
    public static void getHomeCategory()
    {
        mCategoryView.setImageResource(R.drawable.icon_category);
        fl_category_menu.setVisibility(View.GONE);
    }

}
