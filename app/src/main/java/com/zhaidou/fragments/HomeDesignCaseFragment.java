package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.ShopTodayItem;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomProgressWebview;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 微信文章列表
 */
public class HomeDesignCaseFragment extends BaseFragment
{
    private static final String ARG_PARAM = "param";
    private static final String ARG_STRING = "string";

    private String mParam;
    private String mString;
    private View view;
    private TextView titleTv, backTv;
    private ImageView imageIv;
    private CustomProgressWebview webview;
    private GridView mGridView;
    private LinearLayout loadingView, nullNetView, nullView, nullDataView;
    private TextView reloadBtn, reloadNetBtn;
    private RelativeLayout contactQQ;

    private WeakHashMap<Integer, View> mHashMap = new WeakHashMap<Integer, View>();
    private PullToRefreshScrollView mScrollView;
    private ArticleShoppingAdapter articleShoppingAdapter;

    private Dialog mDialog;
    private Context mContext;

    private RequestQueue mRequestQueue;
    private int page = 1;
    private int pageSize;
    private int pageCount;
    private String imageUrl;
    private String title;
    private String introduce;//引文介绍
    private List<ShopTodayItem> items = new ArrayList<ShopTodayItem>();


    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if (msg.what == 1)
            {
                ToolUtils.setImageCacheUrl(imageUrl, imageIv, R.drawable.icon_loading_defalut);

                webview.setWebViewClient(new WebViewClient()
                {
                    @Override
                    public void onPageFinished(WebView view, String url)
                     {
                        view.loadUrl("javascript:!function(){" +

                                "s=document.createElement('style');s.innerHTML="

                                + "\"@font-face{font-family:FZLTXHK;src:url('**injection**/FZLTXHK.TTF');}*{font-family:FZLTXHK !important;}\";"

                                + "document.getElementsByTagName('head')[0].appendChild(s);" +

                                "document.getElementsByTagName('body')[0].style.fontFamily = \"FZLTXHK\";}()");
                        super.onPageFinished(view, url);
                    }
                }
                );
                webview.loadData(introduce, "text/html; charset=UTF-8", "UTF-8");

                if (pageCount > pageSize*page)
                {
                    mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
                } else
                {
                    mScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                }
                titleTv.setText(title);
                loadingView.setVisibility(View.GONE);
                contactQQ.setVisibility(View.VISIBLE);


                articleShoppingAdapter.notifyDataSetChanged();
                mScrollView.onRefreshComplete();
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
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {
            page=page+1;
            FetchData();
        }
    };

    public static HomeDesignCaseFragment newInstance(String param, String string)
    {
        HomeDesignCaseFragment fragment = new HomeDesignCaseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM, param);
        args.putString(ARG_STRING, string);
        fragment.setArguments(args);
        return fragment;
    }

    public HomeDesignCaseFragment()
    {
    }

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
                    MagicDesignFragment magicDesignFragment = MagicDesignFragment.newInstance("", "");
                    ((MainActivity) mContext).navigationToFragmentWithAnim(magicDesignFragment);
                    break;
                case R.id.back_btn:
                    ((MainActivity) getActivity()).popToStack(HomeDesignCaseFragment.this);
                    break;
            }
        }
    };


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
            view = inflater.inflate(R.layout.fragment_magic_case_details, container, false);
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
        titleTv.setText(mParam);

        backTv = (TextView) view.findViewById(R.id.back_btn);
        backTv.setOnClickListener(onClickListener);

        loadingView = (LinearLayout) view.findViewById(R.id.loadingView);
        nullNetView = (LinearLayout) view.findViewById(R.id.nullNetline);
        nullDataView = (LinearLayout) view.findViewById(R.id.nullDataline);
        nullView = (LinearLayout) view.findViewById(R.id.nullline);
        reloadBtn = (TextView) view.findViewById(R.id.nullReload);
        reloadBtn.setOnClickListener(onClickListener);
        reloadNetBtn = (TextView) view.findViewById(R.id.netReload);
        reloadNetBtn.setOnClickListener(onClickListener);

        imageIv = (ImageView) view.findViewById(R.id.detailsImageIv);
        imageIv.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenWidth * 3 / 4));
        webview = (CustomProgressWebview) view.findViewById(R.id.detailsWebView);
        mScrollView = (PullToRefreshScrollView) view.findViewById(R.id.scrollView);
        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mScrollView.setOnRefreshListener(onRefreshListener);
        mGridView = (GridView) view.findViewById(R.id.homeItemList);
        mGridView.setEmptyView(mEmptyView);
        articleShoppingAdapter = new ArticleShoppingAdapter(getActivity(), items);
        mGridView.setAdapter(articleShoppingAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                enterGoods(position);
            }
        });

        contactQQ = (RelativeLayout) view.findViewById(R.id.rl_qq_contact);
        contactQQ.setOnClickListener(onClickListener);
        mRequestQueue = Volley.newRequestQueue(mContext);


        initData();

    }

    private void initData()
    {
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
        ((MainActivity) getActivity()).navigationToFragmentWithAnim(goodsDetailsFragment);
    }

    public void FetchData()
    {
        String url = ZhaiDou.MagicClassicCaseDetailsUrl + mString + "&pageNo=" + page + "&pageSize=10";
        JsonObjectRequest request = new JsonObjectRequest(url,
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
                        JSONObject obj;
                        int status = response.optInt("status");
                        JSONObject jsonObject1 = response.optJSONObject("data");
                        if (jsonObject1 != null)
                        {
                            pageCount = jsonObject1.optInt("totalCount");
                            pageSize = jsonObject1.optInt("pageSize");
                            JSONObject jsonObject = jsonObject1.optJSONObject("freeClassicsCasePO");
                            String id = jsonObject.optString("id");
                            title = jsonObject.optString("caseName");
                            long startTime = jsonObject.optLong("updateTime");
                            imageUrl = jsonObject.optString("mainPic");
                            introduce = jsonObject.optString("caseDesc");

                            JSONArray jsonArray = jsonObject1.optJSONArray("freeClassicsCaseProductPOs");
                            if (jsonArray != null)
                                for (int i = 0; i < jsonArray.length(); i++)
                                {
                                    obj = jsonArray.optJSONObject(i);
                                    String Baseid = obj.optString("productId");
                                    String code = obj.optString("productCode");
                                    String Listtitle = obj.optString("productName");
                                    double price = obj.optDouble("tshPrice");
                                    double cost_price = obj.optDouble("marketPrice");
                                    String imageUrl = obj.optString("mainPic");
                                    int num = obj.optInt("totalStock");
                                    ShopTodayItem shopTodayItem = new ShopTodayItem(code, Listtitle, imageUrl, price, cost_price, num, 0);
                                    items.add(shopTodayItem);
                                }
                            handler.sendEmptyMessage(1);
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
                            return;
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
        )
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

}
