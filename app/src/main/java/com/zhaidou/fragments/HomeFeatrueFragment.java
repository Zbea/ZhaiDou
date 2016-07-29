package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshScrollView;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.LoginActivity;
import com.zhaidou.activities.WebViewActivity;
import com.zhaidou.adapter.ShopTodaySpecialAdapter;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.CartCountManager;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.ShopSpecialItem;
import com.zhaidou.model.ShopTodayItem;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.Api;
import com.zhaidou.utils.EaseUtils;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomProgressWebview;
import com.zhaidou.view.LargeImgView;
import com.zhaidou.view.ListViewForScrollView;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 专题
 */
public class HomeFeatrueFragment extends BaseFragment implements CartCountManager.OnCartCountListener
{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_TITLE = "title";

    private String mParam1;
    private String mParam2;
    private String mTitle;
    private int type = 1;//如果type=1则为特卖列表 type=2则为专题页,type=3文章倒流

    private GridView singleGridView, articleGridView;
    private TypeFaceTextView introduceTv;

    private Map<Integer, View> mHashMap = new HashMap<Integer, View>();
    private RequestQueue requestQueue;
    private List<ShopTodayItem> items = new ArrayList<ShopTodayItem>();
    private TextView infoTv;

    private LinearLayout loadingView, nullNetView, nullView, nullDataView;
    private TextView reloadBtn, reloadNetBtn;
    private final int UPDATE_SINGLE_LIST = 0;
    private final int UPDATE_DOUBLE_LIST = 1;
    private final int UPDATE_ARTICLE_SHOPPING = 2;//文章商城
    private final int UPDATE_CART_DATA = 3;
    private Dialog mDialog;
    private Context mContext;
    private View rootView;
    private TextView cartTipsTv;
    private TextView titleTv;
    private CustomProgressWebview bannerLine;
    private CustomProgressWebview infoImage;
    private ImageView myCartBtn, imageIv;
    private RelativeLayout contactQQ, cartView;
    private PullToRefreshScrollView mScrollView;
    private LinearLayout singleLine, doubleLine, articleLine;
    private ListViewForScrollView mListView;
    private ShopTodaySpecialAdapter shopTodaySpecialAdapter;
    private ProductAdapter productAdapter;
    private ArticleShoppingAdapter articleShoppingpAdapter;
    private String introduce;//引文介绍
    private int page = 1;
    private int pageSize;
    private int pageCount;
    private String mainPic, mainUrl, casePic, caseUrl;
    private ShopSpecialItem shopSpecialItem;
    private String token;
    private int userId;
    private int cartCount;//购物车商品数量
    private CommentSendFragment commentSendFragment;

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {

            switch (msg.what)
            {
                case UPDATE_SINGLE_LIST:
                    shopTodaySpecialAdapter.notifyDataSetChanged();
                    mScrollView.onRefreshComplete();
                    if (pageCount > pageSize * page)
                    {
                        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
                    } else
                    {
                        mScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    }
                    titleTv.setText(shopSpecialItem.title);
                    introduceTv.setText(introduce);
                    loadingView.setVisibility(View.GONE);
                    doubleLine.setVisibility(View.GONE);
                    singleLine.setVisibility(View.VISIBLE);
                    articleLine.setVisibility(View.GONE);
                    contactQQ.setVisibility(View.GONE);
                    break;
                case UPDATE_DOUBLE_LIST:
                    bannerLine.loadUrl(mainPic);
                    mScrollView.onRefreshComplete();
                    if (pageCount > pageSize * page)
                    {
                        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
                    } else
                    {
                        mScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    }
                    titleTv.setText(shopSpecialItem.title);
                    productAdapter.notifyDataSetChanged();
                    loadingView.setVisibility(View.GONE);
                    doubleLine.setVisibility(View.VISIBLE);
                    singleLine.setVisibility(View.GONE);
                    articleLine.setVisibility(View.GONE);
                    contactQQ.setVisibility(View.GONE);
                    break;
                case UPDATE_ARTICLE_SHOPPING:
                    setAddImage(imageIv, mainPic, true);
                    infoImage.loadUrl(casePic);
                    infoTv.setText(introduce);
                    articleShoppingpAdapter.notifyDataSetChanged();
                    mScrollView.onRefreshComplete();
                    if (pageCount > pageSize * page)
                    {
                        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
                    } else
                    {
                        mScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    }
                    titleTv.setText(shopSpecialItem.title);
                    loadingView.setVisibility(View.GONE);
                    singleLine.setVisibility(View.GONE);
                    doubleLine.setVisibility(View.GONE);
                    articleLine.setVisibility(View.VISIBLE);
                    contactQQ.setVisibility(View.VISIBLE);
                    cartView.setVisibility(View.GONE);
                    break;

                case UPDATE_CART_DATA:
                    initCartTips();
                    break;
            }
        }
    };


    private PullToRefreshBase.OnRefreshListener2 onRefreshListener = new PullToRefreshBase.OnRefreshListener2()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView)
        {
            items.clear();
            page = 1;
            FetchData();
            if (type != 3)
            {
                FetchCountData();
            }
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {
            page++;
            FetchData();
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            switch (view.getId())
            {
                case R.id.nullReload:
                    initData();
                    break;
                case R.id.netReload:
                    initData();
                    break;
                case R.id.rl_qq_contact:
                    EaseUtils.startKeFuActivity(mContext);
                    break;
                case R.id.detailsImageIvs:
                    setMainUrl(mainUrl, mainPic);
                    break;
                case R.id.infoImage:

                    break;

                case R.id.myCartBtn:
                    if (checkLogina())
                    {
                        ShopCartFragment shopCartFragment = ShopCartFragment.newInstance("", 0);
                        ((BaseActivity) getActivity()).navigationToFragment(shopCartFragment);
                    } else
                    {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(1);
                        getActivity().startActivity(intent);
                    }
                    break;
            }
        }
    };


    public static HomeFeatrueFragment newInstance(String title, String param1, String param2)
    {
        HomeFeatrueFragment fragment = new HomeFeatrueFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    public HomeFeatrueFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mainPic = getArguments().getString(ARG_PARAM2);
            mTitle = getArguments().getString(ARG_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        if (rootView == null)
        {
            mContext = getActivity();
            rootView = inflater.inflate(R.layout.fragment_home_featrue, container, false);
            titleTv = (TextView) rootView.findViewById(R.id.tv_title);

            loadingView = (LinearLayout) rootView.findViewById(R.id.loadingView);
            nullNetView = (LinearLayout) rootView.findViewById(R.id.nullNetline);
            nullDataView = (LinearLayout) rootView.findViewById(R.id.nullDataline);
            nullView = (LinearLayout) rootView.findViewById(R.id.nullline);
            reloadBtn = (TextView) rootView.findViewById(R.id.nullReload);
            reloadBtn.setOnClickListener(onClickListener);
            reloadNetBtn = (TextView) rootView.findViewById(R.id.netReload);
            reloadNetBtn.setOnClickListener(onClickListener);

            bannerLine = (CustomProgressWebview) rootView.findViewById(R.id.bannersView);
            bannerLine.getSettings().setJavaScriptEnabled(true);
            //扩大比例的缩放
            bannerLine.getSettings().setUseWideViewPort(true);
            //自适应屏幕
            bannerLine.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            bannerLine.getSettings().setLoadWithOverviewMode(true);
            bannerLine.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    setMainUrl(mainUrl, mainPic);
                    return false;
                }
            });

            mScrollView = (PullToRefreshScrollView) rootView.findViewById(R.id.scrollView);
            mScrollView.setOnRefreshListener(onRefreshListener);

            singleLine = (LinearLayout) rootView.findViewById(R.id.singleLine);
            singleGridView = (GridView) rootView.findViewById(R.id.gv_sale);
            singleGridView.setEmptyView(mEmptyView);
            productAdapter = new ProductAdapter(getActivity(), items);
            singleGridView.setAdapter(productAdapter);
            productAdapter.setOnInViewClickListener(R.id.ll_single_layout, new BaseListAdapter.onInternalClickListener()
            {
                @Override
                public void OnClickListener(View parentV, View v, Integer position, Object values)
                {
                    enterGoods(position);
                }
            });
            introduceTv = (TypeFaceTextView) rootView.findViewById(R.id.adText);
            introduceTv.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("text", introduceTv.getText().toString());
                    clipboardManager.setPrimaryClip(clipData);
                    ToolUtils.setToast(mContext, "复制成功");
                    return false;
                }
            });

            doubleLine = (LinearLayout) rootView.findViewById(R.id.doubleLine);
            mListView = (ListViewForScrollView) rootView.findViewById(R.id.shopListView);
            shopTodaySpecialAdapter = new ShopTodaySpecialAdapter(mContext, items, 1);
            mListView.setAdapter(shopTodaySpecialAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    enterGoods(position);
                }
            });

            imageIv = (ImageView) rootView.findViewById(R.id.detailsImageIvs);
            imageIv.setOnClickListener(onClickListener);
            imageIv.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
            infoTv = (TextView) rootView.findViewById(R.id.infoTv);
            infoImage = (CustomProgressWebview) rootView.findViewById(R.id.infoImage);
            infoImage.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                        setMainUrl(caseUrl, null);
                    return false;
                }
            });
            WebSettings webSettings = infoImage.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webSettings.setAllowFileAccess(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setUseWideViewPort(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

            articleLine = (LinearLayout) rootView.findViewById(R.id.articleLine);
            articleGridView = (GridView) rootView.findViewById(R.id.magicItemList);
            articleGridView.setEmptyView(mEmptyView);
            articleShoppingpAdapter = new ArticleShoppingAdapter(getActivity(), items);
            articleGridView.setAdapter(articleShoppingpAdapter);
            articleGridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    enterGoods(position);
                }
            });
            contactQQ = (RelativeLayout) rootView.findViewById(R.id.rl_qq_contact);
            contactQQ.setOnClickListener(onClickListener);

            requestQueue = ZDApplication.newRequestQueue();
            cartView = (RelativeLayout) rootView.findViewById(R.id.cartView);
            myCartBtn = (ImageView) rootView.findViewById(R.id.myCartBtn);
            myCartBtn.setOnClickListener(onClickListener);
            cartTipsTv = (TextView) rootView.findViewById(R.id.myCartTipsTv);

            CartCountManager.newInstance().setOnCartCountListener(this);
            checkLogina();
            initData();

        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null)
        {
            parent.removeView(rootView);
        }
        return rootView;
    }

    /**
     * 初始化收据
     */
    private void initData()
    {
        mDialog = CustomLoadingDialog.setLoadingDialog(getActivity(), "loading");
        if (NetworkUtils.isNetworkAvailable(getActivity()))
        {

            FetchData();
            if (checkLogina())
            {
                FetchCountData();
            }

        } else
        {
            if (mDialog != null)
                mDialog.dismiss();
            nullView.setVisibility(View.GONE);
            nullNetView.setVisibility(View.VISIBLE);
            nullDataView.setVisibility(View.GONE);
        }
    }

    /**
     * 跳转
     *
     * @param url
     */
    private void setMainUrl(String url, String imageUrl)
    {
        if (!TextUtils.isEmpty(url))
        {
            Intent web = new Intent();
            web.putExtra("url", url);
            web.putExtra("imageUrl", imageUrl);
            web.setClass(mContext, WebViewActivity.class);
            mContext.startActivity(web);
        }
    }

    public boolean checkLogina()
    {
        token = (String) SharedPreferencesUtil.getData(mContext, "token", "");
        userId = (Integer) SharedPreferencesUtil.getData(mContext, "userId", -1);
        boolean isLogin = !TextUtils.isEmpty(token) && userId > -1;
        return isLogin;
    }

    /**
     * 跳转
     *
     * @param position
     */
    private void enterGoods(int position)
    {
        GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance(items.get(position).title, items.get(position).goodsId);
        Bundle bundle = new Bundle();
        bundle.putString("index", items.get(position).goodsId);
        bundle.putString("page", items.get(position).title);
        bundle.putBoolean("canShare", false);
        goodsDetailsFragment.setArguments(bundle);
        ((BaseActivity) getActivity()).navigationToFragmentWithAnim(goodsDetailsFragment);
    }

    private void setAddImage(ImageView imgView, String url, final boolean flags)
    {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.icon_loading_item)
                .showImageForEmptyUri(R.drawable.icon_loading_item)
                .showImageOnFail(R.drawable.icon_loading_item)
//                .resetViewBeforeLoading(true)//default 设置图片在加载前是否重置、复位
                .cacheInMemory(true) // default  设置下载的图片是否缓存在内存中
                .cacheOnDisk(true) // default  设置下载的图片是否缓存在SD卡中
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();
        ToolUtils.setLog("url:" + url);
        ImageLoader.getInstance().displayImage(url, imgView, options, new ImageLoadingListener()
        {
            @Override
            public void onLoadingStarted(String s, View view)
            {
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason)
            {
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap)
            {
                if (flags)
                {
                    ImageView imageView1 = (ImageView) view;
                    imageView1.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageView1.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, bitmap.getHeight() * screenWidth / bitmap.getWidth()));
                    imageView1.setImageBitmap(bitmap);
                } else
                {
                    if (bitmap != null)
                    {
                        ToolUtils.setLog("bitmap.getHeight():" + bitmap.getHeight());
                        ToolUtils.setLog("bitmap.getWidth():" + bitmap.getWidth());

                        LargeImgView imageView1 = (LargeImgView) view;
                        imageView1.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, bitmap.getHeight() * screenWidth / bitmap.getWidth()));
                        if (bitmap.getHeight() < 1000)
                        {
                            imageView1.setScaleType(ImageView.ScaleType.FIT_XY);
                            imageView1.setImageBitmap(bitmap);
                        } else
                        {
                            imageView1.setImageBitmapLarge(bitmap);
                        }
                    }
                }

            }

            @Override
            public void onLoadingCancelled(String s, View view)
            {
            }
        });
    }

    /**
     * 红色标识提示显示数量
     */
    private void initCartTips()
    {
        if (checkLogina())
        {
            if (cartCount > 0)
            {
                cartTipsTv.setVisibility(View.VISIBLE);
                cartTipsTv.setText("" + cartCount);
            } else
            {
                cartTipsTv.setVisibility(View.GONE);
            }
        } else
        {
            cartCount = 0;
            cartTipsTv.setVisibility(View.GONE);
        }
    }

    public void FetchData()
    {
        String url = ZhaiDou.HomeGoodsListUrl + mParam1 + "&pageNo=" + page + "&typeEnum=3";
        ZhaiDouRequest request = new ZhaiDouRequest(url,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        if (mDialog != null)
                            mDialog.dismiss();
                        mScrollView.onRefreshComplete();
                        if (response == null)
                        {
                            if (page == 1)
                            {
                                nullNetView.setVisibility(View.GONE);
                                nullView.setVisibility(View.VISIBLE);
                                nullDataView.setVisibility(View.GONE);
                            } else
                            {
                                ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                            }
                            return;
                        }
                        ToolUtils.setLog(response.toString());
                        JSONObject obj;
                        int status = response.optInt("status");
                        JSONObject jsonObject1 = response.optJSONObject("data");
                        if (jsonObject1 != null)
                        {
                            JSONObject jsonObject = jsonObject1.optJSONObject("activityPO");
                            String id = jsonObject.optString("activityCode");
                            String title = jsonObject.optString("activityName");
                            long startTime = jsonObject.optLong("startTime");
                            long endTime = jsonObject.optLong("endTime");
                            mainPic = jsonObject.optString("mainPic");
                            mainUrl = jsonObject.optString("mainUrl");
                            type = jsonObject.optInt("composingType");
                            casePic = jsonObject.optString("casePic");
                            caseUrl = jsonObject.optString("caseUrl");
                            int overTime = Integer.parseInt((String.valueOf((endTime - startTime) / (24 * 60 * 60 * 1000))));
                            introduce = jsonObject.optString("description");
                            int isNew = jsonObject.optInt("newFlag");
                            shopSpecialItem = new ShopSpecialItem(id, title, null, startTime, endTime, overTime, null, isNew);

                            JSONObject jsonObject2 = jsonObject1.optJSONObject("pagePO");
                            pageCount = jsonObject2.optInt("totalCount");
                            pageSize = jsonObject2.optInt("pageSize");
                            if (jsonObject2 != null)
                            {
                                JSONArray jsonArray = jsonObject2.optJSONArray("items");
                                if (jsonArray != null)

                                    for (int i = 0; i < jsonArray.length(); i++)
                                    {
                                        obj = jsonArray.optJSONObject(i);
                                        String Baseid = obj.optString("productId");
                                        String Listtitle = obj.optString("productName");
                                        double price = obj.optDouble("price");
                                        double cost_price = obj.optDouble("marketPrice");
                                        String imageUrl = obj.optString("productPicUrl");
                                        JSONObject jsonObject3 = obj.optJSONObject("expandedResponse");
                                        int num = jsonObject3.optInt("stock");
                                        int totalCount = 100;
                                        int percentum = obj.optInt("progressPercentage");
                                        String comment = obj.optString("comment") == "null" ? "" : obj.optString("comment");
                                        ShopTodayItem shopTodayItem = new ShopTodayItem(Baseid, Listtitle, imageUrl, price, cost_price, num, totalCount);
                                        shopTodayItem.percentum = percentum;
                                        shopTodayItem.comment = comment;
                                        items.add(shopTodayItem);
                                    }
                            }
                            if (type == 1)
                            {
                                mHandler.sendEmptyMessage(UPDATE_SINGLE_LIST);
                            } else if (type == 2)
                            {
                                mHandler.sendEmptyMessage(UPDATE_DOUBLE_LIST);
                            } else
                            {
                                mHandler.sendEmptyMessage(UPDATE_ARTICLE_SHOPPING);
                            }
                        } else
                        {
                            if (status == 200)
                            {
                                if (page == 1)
                                {
                                    nullNetView.setVisibility(View.GONE);
                                    nullView.setVisibility(View.GONE);
                                    nullDataView.setVisibility(View.VISIBLE);
                                } else
                                {
                                    ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                                }
                            } else
                            {
                                if (page == 1)
                                {
                                    nullNetView.setVisibility(View.GONE);
                                    nullView.setVisibility(View.VISIBLE);
                                    nullDataView.setVisibility(View.GONE);
                                } else
                                {
                                    ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                                }
                            }
                        }
                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                mDialog.dismiss();
                mScrollView.onRefreshComplete();
                if (page == 1)
                {
                    nullNetView.setVisibility(View.GONE);
                    nullView.setVisibility(View.VISIBLE);
                    nullDataView.setVisibility(View.GONE);
                } else
                {
                    page--;
                    ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                }
            }
        }
        );
        requestQueue.add(request);
    }

    /**
     * 请求购物车列表数据
     */
    public void FetchCountData()
    {
        Api.getCartCount(userId, new Api.SuccessListener()
        {
            @Override
            public void onSuccess(Object jsonObject)
            {
                if (jsonObject != null)
                {
                    JSONObject object = ((JSONObject) jsonObject).optJSONObject("data");
                    cartCount = object.optInt("totalQuantity");
                    mHandler.sendEmptyMessage(UPDATE_CART_DATA);
                    CartCountManager.newInstance().notify(cartCount);
                }
            }
        }, null);
    }


    public class ProductAdapter extends BaseListAdapter<ShopTodayItem>
    {
        public ProductAdapter(Context context, List<ShopTodayItem> list)
        {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent)
        {
            convertView = mHashMap.get(position);
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_goods_sale, null);
            TextView tv_name = ViewHolder.get(convertView, R.id.tv_name);
            ImageView image = ViewHolder.get(convertView, R.id.iv_single_item);
            image.setLayoutParams(new RelativeLayout.LayoutParams((screenWidth - 30) / 2, (screenWidth - 30) / 2));
            TextView tv_money = ViewHolder.get(convertView, R.id.tv_money);
            TextView tv_price = ViewHolder.get(convertView, R.id.tv_price);
            TextView tv_count = ViewHolder.get(convertView, R.id.tv_count);
            ImageView ll_sale_out = ViewHolder.get(convertView, R.id.ll_sale_out);
            TextView shopSaleTv = ViewHolder.get(convertView, R.id.shopSaleTv);
            ll_sale_out.setLayoutParams(new RelativeLayout.LayoutParams((screenWidth - 30) / 2, (screenWidth - 30) / 2));
            ShopTodayItem shopTodayItem = getList().get(position);
            tv_name.setText(shopTodayItem.title);
            ToolUtils.setImageNoResetUrl(shopTodayItem.imageUrl, image, R.drawable.icon_loading_defalut);
            tv_price.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            tv_price.setText("￥" + ToolUtils.isIntPrice("" + shopTodayItem.formerPrice));
            tv_count.setText("剩余 " + (100 - shopTodayItem.percentum) + "%");
            tv_money.setText("￥" + ToolUtils.isIntPrice("" + shopTodayItem.currentPrice));
            ll_sale_out.setVisibility(shopTodayItem.num == 0 ? View.VISIBLE : View.GONE);
            DecimalFormat df = new DecimalFormat("##.0");
            String zk = df.format(shopTodayItem.currentPrice / shopTodayItem.formerPrice * 10);
            if (zk.contains(".0"))
            {
                int sales = (int) Double.parseDouble(zk);
                shopSaleTv.setText(sales + "折");
            } else
            {
                Double sales = Double.parseDouble(zk);
                shopSaleTv.setText(sales + "折");
            }
            shopSaleTv.setVisibility(View.VISIBLE);
            mHashMap.put(position, convertView);
            return convertView;
        }
    }

    public class ArticleShoppingAdapter extends BaseListAdapter<ShopTodayItem>
    {
        public ArticleShoppingAdapter(Context context, List<ShopTodayItem> list)
        {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent)
        {
            convertView = mHashMap.get(position);
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_magic_single_goods, null);
            TextView tv_num = ViewHolder.get(convertView, R.id.tv_num);
            TextView tv_name = ViewHolder.get(convertView, R.id.tv_name);
            ImageView image = ViewHolder.get(convertView, R.id.iv_single_item);
            image.setLayoutParams(new RelativeLayout.LayoutParams((screenWidth - 30) / 2, (screenWidth - 30) / 2));
            ImageView ll_sale_out = ViewHolder.get(convertView, R.id.ll_sale_out);
            ll_sale_out.setLayoutParams(new RelativeLayout.LayoutParams((screenWidth - 30) / 2, (screenWidth - 30) / 2));
            TextView tv_money = ViewHolder.get(convertView, R.id.tv_money);
            TextView tv_price = ViewHolder.get(convertView, R.id.tv_price);
            TextView shopSaleTv = ViewHolder.get(convertView, R.id.shopSaleTv);

            ShopTodayItem shopTodayItem = getList().get(position);

            tv_num.setText("" + (position + 1));
            tv_name.setText("    " + shopTodayItem.title);
            ToolUtils.setImageNoResetUrl(shopTodayItem.imageUrl, image, R.drawable.icon_loading_defalut);
            tv_price.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            tv_price.setText("￥" + ToolUtils.isIntPrice("" + shopTodayItem.formerPrice));
            tv_money.setText("￥" + ToolUtils.isIntPrice("" + shopTodayItem.currentPrice));
            ll_sale_out.setVisibility(shopTodayItem.num == 0 ? View.VISIBLE : View.GONE);
            DecimalFormat df = new DecimalFormat("##.0");
            String zk = df.format(shopTodayItem.currentPrice / shopTodayItem.formerPrice * 10);
            if (zk.contains(".0"))
            {
                int sales = (int) Double.parseDouble(zk);
                shopSaleTv.setText(sales + "折");
            } else
            {
                Double sales = Double.parseDouble(zk);
                shopSaleTv.setText(sales + "折");
            }
            shopSaleTv.setVisibility(View.VISIBLE);
            mHashMap.put(position, convertView);
            return convertView;
        }
    }

    /**
     * 购物车数量变化刷新
     *
     * @param count
     */
    @Override
    public void onChange(int count)
    {
        cartCount = count;
        initCartTips();
    }

}
